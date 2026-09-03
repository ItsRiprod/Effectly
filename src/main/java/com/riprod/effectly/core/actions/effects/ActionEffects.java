package com.riprod.effectly.core.actions.effects;

import com.hypixel.hytale.codec.Codec;
import com.hypixel.hytale.codec.KeyedCodec;
import com.hypixel.hytale.codec.builder.BuilderCodec;
import com.hypixel.hytale.codec.codecs.EnumCodec;
import com.hypixel.hytale.codec.validation.Validators;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.component.spatial.SpatialResource;
import com.hypixel.hytale.protocol.AnimationSlot;
import com.hypixel.hytale.protocol.SoundCategory;
import com.hypixel.hytale.protocol.packets.entities.SpawnModelParticles;
import com.hypixel.hytale.server.core.asset.type.camera.CameraEffect;
import com.hypixel.hytale.server.core.asset.type.entityeffect.config.EntityEffect;
import com.hypixel.hytale.server.core.asset.type.itemanimation.config.ItemPlayerAnimations;
import com.hypixel.hytale.server.core.asset.type.model.config.ModelParticle;
import com.hypixel.hytale.server.core.asset.type.particle.config.WorldParticle;
import com.hypixel.hytale.server.core.asset.type.soundevent.config.SoundEvent;
import com.hypixel.hytale.server.core.asset.type.soundevent.validator.SoundEventValidators;
import com.hypixel.hytale.server.core.entity.AnimationUtils;
import com.hypixel.hytale.server.core.entity.effect.EffectControllerComponent;
import com.hypixel.hytale.server.core.modules.entity.EntityModule;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.entity.tracker.NetworkId;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.ParticleUtil;
import com.hypixel.hytale.server.core.universe.world.PlayerUtil;
import com.hypixel.hytale.server.core.universe.world.SoundUtil;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import com.riprod.effectly.builtin.capabilities.OtherEntityCapability;
import com.riprod.effectly.core.actions.ActionContext;

import org.jetbrains.annotations.NotNull;
import org.joml.Vector3d;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public final class ActionEffects {

    @Nonnull
    public static final BuilderCodec<@NotNull ActionEffects> CODEC = BuilderCodec
            .builder(ActionEffects.class, ActionEffects::new)
            .append(new KeyedCodec<>("Particles", WorldParticle.ARRAY_CODEC),
                    (effects, v) -> effects.particles = v,
                    effects -> effects.particles)
            .documentation("World particles spawned at the holder's position for every player within "
                    + "ViewDistance")
            .add()
            .append(new KeyedCodec<>("ModelParticles", ModelParticle.ARRAY_CODEC),
                    (effects, v) -> effects.modelParticles = v,
                    effects -> effects.modelParticles)
            .documentation("Particles attached to the holder's model, sent to every player that can "
                    + "see the holder")
            .add()
            .append(new KeyedCodec<>("WorldSoundEventId", Codec.STRING),
                    (effects, v) -> effects.worldSoundEventId = v,
                    effects -> effects.worldSoundEventId)
            .addValidator(SoundEvent.VALIDATOR_CACHE.getValidator())
            .addValidator(SoundEventValidators.MONO)
            .documentation("3D sound played at the holder for everyone nearby. A player holder hears "
                    + "the local sound instead")
            .add()
            .append(new KeyedCodec<>("LocalSoundEventId", Codec.STRING),
                    (effects, v) -> effects.localSoundEventId = v,
                    effects -> effects.localSoundEventId)
            .addValidator(SoundEvent.VALIDATOR_CACHE.getValidator())
            .documentation("Sound played only to a player holder. The client picks 2D in first "
                    + "person and 3D in third person. Falls back to WorldSoundEventId")
            .add()
            .append(new KeyedCodec<>("Animation", Animation.CODEC),
                    (effects, v) -> effects.animation = v,
                    effects -> effects.animation)
            .documentation("Animation played on the holder, including for the holder's own client")
            .add()
            .append(new KeyedCodec<>("CameraEffect", CameraEffect.CHILD_ASSET_CODEC),
                    (effects, v) -> effects.cameraEffectId = v,
                    effects -> effects.cameraEffectId)
            .addValidator(CameraEffect.VALIDATOR_CACHE.getValidator())
            .documentation("CameraEffect asset id or inline definition, shaken on a player holder")
            .add()
            .append(new KeyedCodec<>("CameraIntensity", Codec.FLOAT),
                    (effects, v) -> effects.cameraIntensity = v,
                    effects -> effects.cameraIntensity)
            .documentation("Contextual intensity value (such as damage) for the camera effect; omit to "
                    + "use the effect's own default.")
            .add()
            .append(new KeyedCodec<>("EntityEffect", EntityEffect.CHILD_ASSET_CODEC),
                    (effects, v) -> effects.entityEffectId = v,
                    effects -> effects.entityEffectId)
            .addValidator(EntityEffect.VALIDATOR_CACHE.getValidator())
            .documentation("EntityEffect asset id or inline definition applied to the holder with the "
                    + "asset's own duration and overlap behavior")
            .add()
            .append(new KeyedCodec<>("OtherEntityEffect", EntityEffect.CHILD_ASSET_CODEC),
                    (effects, v) -> effects.otherEntityEffectId = v,
                    effects -> effects.otherEntityEffectId)
            .addValidator(EntityEffect.VALIDATOR_CACHE.getValidator())
            .documentation("EntityEffect applied to the trigger's Other_Entity. Skipped when the "
                    + "trigger supplies none")
            .add()
            .append(new KeyedCodec<>("ViewDistance", Codec.DOUBLE),
                    (effects, v) -> effects.viewDistance = v,
                    effects -> effects.viewDistance)
            .addValidator(Validators.greaterThan(0.0))
            .documentation("Radius within which players receive the world particles. Default 75")
            .add()
            .build();

    private WorldParticle[] particles;
    private ModelParticle[] modelParticles;
    private String worldSoundEventId;
    private String localSoundEventId;
    private Animation animation;
    private String cameraEffectId;
    private Float cameraIntensity;
    private String entityEffectId;
    private String otherEntityEffectId;
    private double viewDistance = ParticleUtil.DEFAULT_PARTICLE_DISTANCE;

    public void play(@Nonnull Ref<EntityStore> holder, @Nonnull ActionContext context) {
        var buffer = context.getEntityStore();
        var playerRef = buffer.getComponent(holder, PlayerRef.getComponentType());
        var transform = buffer.getComponent(holder, TransformComponent.getComponentType());

        if (transform != null) {
            var position = transform.getPosition();
            spawnParticles(holder, position, buffer);
            playSounds(holder, playerRef, position, buffer);
        }
        spawnModelParticles(holder, buffer);
        playAnimation(holder, buffer);
        shakeCamera(playerRef);
        applyEntityEffect(holder, entityEffectId, buffer);
        if (otherEntityEffectId != null && context.find(OtherEntityCapability.TYPE) instanceof OtherEntityCapability other) {
            applyEntityEffect(other.getEntity(), otherEntityEffectId, buffer);
        }
    }

    private void spawnParticles(
            @Nonnull Ref<EntityStore> holder,
            @Nonnull Vector3d position,
            @Nonnull CommandBuffer<EntityStore> buffer) {
        if (particles == null || particles.length == 0) return;
        var spatial = buffer.getResource(EntityModule.get().getPlayerSpatialResourceType());
        var players = SpatialResource.<EntityStore>getThreadLocalReferenceList();
        spatial.getSpatialStructure().collect(position, viewDistance, players);
        ParticleUtil.spawnParticleEffects(particles, position, null, players, buffer);
    }

    private void spawnModelParticles(@Nonnull Ref<EntityStore> holder, @Nonnull CommandBuffer<EntityStore> buffer) {
        if (modelParticles == null || modelParticles.length == 0) return;
        var networkId = buffer.getComponent(holder, NetworkId.getComponentType());
        if (networkId == null) return;

        var protocol = new com.hypixel.hytale.protocol.ModelParticle[modelParticles.length];
        for (int i = 0; i < modelParticles.length; i++) {
            protocol[i] = modelParticles[i].toPacket();
        }
        var packet = new SpawnModelParticles(networkId.getId(), protocol);
        PlayerUtil.forEachPlayerThatCanSeeEntity(holder,
                (ref, player, accessor) -> player.getPacketHandler().writeNoCache(packet), buffer);
    }

    private void playSounds(
            @Nonnull Ref<EntityStore> holder,
            @Nullable PlayerRef playerRef,
            @Nonnull Vector3d position,
            @Nonnull CommandBuffer<EntityStore> buffer) {
        int worldIndex = soundIndex(worldSoundEventId);
        int localIndex = soundIndex(localSoundEventId);

        if (worldIndex != SoundEvent.EMPTY_ID) {
            SoundUtil.playSoundEvent3d(holder, worldIndex, position, playerRef != null, buffer);
        }
        if (playerRef != null && (localIndex != SoundEvent.EMPTY_ID || worldIndex != SoundEvent.EMPTY_ID)) {
            SoundUtil.playLocalPlayerSoundEvent(playerRef, localIndex, worldIndex, SoundCategory.SFX);
        }
    }

    private static int soundIndex(@Nullable String id) {
        return id == null ? SoundEvent.EMPTY_ID : SoundEvent.getAssetMap().getIndex(id);
    }

    private void playAnimation(@Nonnull Ref<EntityStore> holder, @Nonnull CommandBuffer<EntityStore> buffer) {
        if (animation == null) return;
        AnimationUtils.playAnimation(holder, animation.slot, animation.itemPlayerAnimationsId,
                animation.animationId, true, buffer);
    }

    private void shakeCamera(@Nullable PlayerRef playerRef) {
        if (cameraEffectId == null || playerRef == null) return;
        var effect = CameraEffect.getAssetMap().getAsset(cameraEffectId);
        if (effect == null) return;
        var packet = cameraIntensity == null
                ? effect.createCameraShakePacket()
                : effect.createCameraShakePacket(cameraIntensity);
        playerRef.getPacketHandler().writeNoCache(packet);
    }

    private static void applyEntityEffect(
            @Nonnull Ref<EntityStore> target,
            @Nullable String effectId,
            @Nonnull CommandBuffer<EntityStore> buffer) {
        if (effectId == null) return;
        var effect = EntityEffect.getAssetMap().getAsset(effectId);
        if (effect == null) return;
        var controller = buffer.getComponent(target, EffectControllerComponent.getComponentType());
        if (controller == null) return;
        controller.addEffect(target, effect, buffer);
    }

    public static final class Animation {

        @Nonnull
        public static final BuilderCodec<Animation> CODEC = BuilderCodec
                .builder(Animation.class, Animation::new)
                .append(new KeyedCodec<>("AnimationId", Codec.STRING),
                        (animation, v) -> animation.animationId = v,
                        animation -> animation.animationId)
                .addValidator(Validators.nonEmptyString())
                .documentation("Animation id to play. Must exist on the holder's model unless the slot "
                        + "is Action or Emote")
                .add()
                .append(new KeyedCodec<>("Slot", new EnumCodec<>(AnimationSlot.class)),
                        (animation, v) -> animation.slot = v,
                        animation -> animation.slot)
                .documentation("Animation slot. Default ServerAction")
                .add()
                .append(new KeyedCodec<>("ItemPlayerAnimationsId", ItemPlayerAnimations.CHILD_CODEC),
                        (animation, v) -> animation.itemPlayerAnimationsId = v,
                        animation -> animation.itemPlayerAnimationsId)
                .addValidator(ItemPlayerAnimations.VALIDATOR_CACHE.getValidator())
                .documentation("Item animation set the animation id resolves against. Its "
                        + "UseFirstPersonOverrides flag selects the first-person variant")
                .add()
                .build();

        private String animationId;
        private AnimationSlot slot = AnimationSlot.ServerAction;
        private String itemPlayerAnimationsId;
    }
}
