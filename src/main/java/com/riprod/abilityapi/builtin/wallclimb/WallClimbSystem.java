package com.riprod.abilityapi.builtin.wallclimb;

import com.riprod.abilityapi.systems.AbilityConditionService;
import com.hypixel.hytale.component.ArchetypeChunk;
import com.hypixel.hytale.component.Ref;
import com.hypixel.hytale.math.shape.Box;
import com.hypixel.hytale.server.core.asset.type.model.config.Model;
import com.hypixel.hytale.protocol.MovementStates;
import com.hypixel.hytale.server.core.modules.entity.component.ModelComponent;
import com.hypixel.hytale.component.CommandBuffer;
import com.hypixel.hytale.component.Store;
import com.hypixel.hytale.component.query.Query;
import com.hypixel.hytale.component.system.tick.EntityTickingSystem;
import com.hypixel.hytale.math.util.ChunkUtil;
import com.hypixel.hytale.math.util.MathUtil;
import com.hypixel.hytale.math.vector.Rotation3fc;
import com.hypixel.hytale.protocol.BlockMaterial;
import com.hypixel.hytale.server.core.asset.type.blocktype.config.BlockType;
import com.hypixel.hytale.server.core.entity.movement.MovementStatesComponent;
import com.hypixel.hytale.server.core.modules.entity.component.TransformComponent;
import com.hypixel.hytale.server.core.modules.physics.component.Velocity;
import com.hypixel.hytale.server.core.modules.physics.systems.IVelocityModifyingSystem;
import com.hypixel.hytale.server.core.universe.PlayerRef;
import com.hypixel.hytale.server.core.universe.world.World;
import com.hypixel.hytale.server.core.universe.world.chunk.WorldChunk;
import com.hypixel.hytale.server.core.universe.world.storage.EntityStore;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import com.hypixel.hytale.protocol.ChangeVelocityType;
import org.joml.Vector3d;

/**
 * Allows players with wall_climb to climb any solid surface (spider-style).
 * Uses the game's movement pipeline: Velocity.addInstruction() so that PlayerVelocityInstructionSystem
 * sends ChangeVelocity to the client (same path as knockback and launch pads). Runs as IVelocityModifyingSystem.
 * Uses game-scale vertical velocities so movement is clearly visible (stick ~1.0 to counteract gravity, climb ~0.4 up).
 */
public class WallClimbSystem extends EntityTickingSystem<EntityStore> implements IVelocityModifyingSystem {
    /** Query only PlayerRef so registration never sees null. */
    private static final Query<EntityStore> QUERY = WallClimbComponent.getComponentType();

    @Nonnull
    @Override
    public Query<EntityStore> getQuery() {
        return QUERY;
    }

    @Override
    public void tick(
            float dt,
            int index,
            @Nonnull ArchetypeChunk<EntityStore> archetypeChunk,
            @Nonnull Store<EntityStore> store,
            @Nonnull CommandBuffer<EntityStore> commandBuffer) {
        World world = store.getExternalData().getWorld();
        if (world == null) return;

        var ref = archetypeChunk.getReferenceTo(index);
        if (ref == null || !ref.isValid()) return;

        PlayerRef playerRefComponent = archetypeChunk.getComponent(index, PlayerRef.getComponentType());
        if (playerRefComponent == null) return;

        WallClimbComponent component = archetypeChunk.getComponent(index, WallClimbComponent.getComponentType());
        if (component == null || component.getAbilityId() == null) return;

        if (!AbilityConditionService.isAbilityActive(ref, store, world, playerRefComponent.getUuid(), component.getAbilityId())) {
            return;
        }

        MovementStatesComponent movementStatesComponent = store.getComponent(ref, MovementStatesComponent.getComponentType());
        if (movementStatesComponent == null) return;

        var movementStates = movementStatesComponent.getMovementStates();
        // Only climb when actively pressing forward into the wall (W), not just standing against it
        boolean pressingForward = movementStates.walking || movementStates.running || movementStates.sprinting;
        if (!pressingForward) return;

        TransformComponent transformComponent = store.getComponent(ref, TransformComponent.getComponentType());
        if (transformComponent == null) return;

        Vector3d position = transformComponent.getPosition();
        Rotation3fc rotation = transformComponent.getRotation();
        float yaw = rotation.yaw();
        double forwardX = -Math.sin(yaw);
        double forwardZ = -Math.cos(yaw);
        double len = Math.sqrt(forwardX * forwardX + forwardZ * forwardZ);
        if (len < 1e-6) return;
        forwardX /= len;
        forwardZ /= len;

        WallClimbConfig config = component.configOrDefault(WallClimbConfig.class, WallClimbConfig.DEFAULTS);
        double bodyHeight = bodyHeight(ref, store, movementStates);
        if (!isSolidWallInFront(world, position, forwardX, forwardZ, config.getProbeDistance(), bodyHeight)) {
            return;
        }

        Velocity velocityComponent = store.getComponent(ref, Velocity.getComponentType());
        if (velocityComponent == null) return;

        // Allow climb when on ground too – walk into wall with W to start climbing
        boolean jumpHeld = movementStates.jumping;
        boolean crouchHeld = movementStates.crouching;

        double vy = config.getVelocity();
        if (jumpHeld) vy += config.getVelocityUp();
        else if (crouchHeld) vy -= config.getVelocityDown();

        Vector3d climbVelocity = new Vector3d(0, vy, 0);
        velocityComponent.addInstruction(climbVelocity, null, ChangeVelocityType.Add);
    }

    private static double bodyHeight(
            @Nonnull Ref<EntityStore> ref,
            @Nonnull Store<EntityStore> store,
            @Nullable MovementStates movementStates) {
        ModelComponent modelComponent = store.getComponent(ref, ModelComponent.getComponentType());
        if (modelComponent == null) return 0.0;
        Model model = modelComponent.getModel();
        if (model == null) return 0.0;
        Box box = model.getBoundingBox(movementStates);
        return box != null ? box.max.y() : 0.0;
    }

    private static boolean isSolidWallInFront(World world, Vector3d position, double forwardX, double forwardZ,
                                              double probeDistance, double bodyHeight) {
        double probeX = position.x + forwardX * probeDistance;
        double probeZ = position.z + forwardZ * probeDistance;
        int blockX = MathUtil.floor(probeX);
        int blockZ = MathUtil.floor(probeZ);
        int blockYFeet = MathUtil.floor(position.y);
        int blockYHead = MathUtil.floor(position.y + bodyHeight);
        WorldChunk chunk = world.getChunkIfInMemory(ChunkUtil.indexChunkFromBlock(blockX, blockZ));
        if (chunk == null) return false;
        for (int by = blockYFeet; by <= blockYHead; by++) {
            if (by < ChunkUtil.MIN_Y || by >= ChunkUtil.HEIGHT) continue;
            int blockId = chunk.getBlock(blockX, by, blockZ);
            BlockType blockType = blockId != 0 ? BlockType.getAssetMap().getAsset(blockId) : null;
            if (blockType != null && blockType.getMaterial() == BlockMaterial.Solid) {
                return true;
            }
        }
        return false;
    }
}
