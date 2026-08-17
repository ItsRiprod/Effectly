package com.hexvane.abilityapi;

import com.hexvane.abilityapi.commands.AbilityCommand;
import com.hexvane.abilityapi.builtin.combat.PunchDamageHandler;
import com.hexvane.abilityapi.builtin.combat.ResistanceHandler;
import com.hexvane.abilityapi.builtin.combat.StrengthHandler;
import com.hexvane.abilityapi.builtin.darkvision.DarkVisionHandler;
import com.hexvane.abilityapi.builtin.mining.MiningFortuneHandler;
import com.hexvane.abilityapi.builtin.mining.MiningHasteHandler;
import com.hexvane.abilityapi.builtin.healthregen.HealthRegenHandler;
import com.hexvane.abilityapi.builtin.itemmagnet.ItemMagnetHandler;
import com.hexvane.abilityapi.builtin.secondchance.SecondChanceHandler;
import com.hexvane.abilityapi.builtin.staminaregen.StaminaRegenHandler;
import com.hexvane.abilityapi.builtin.movespeed.MoveSpeedHandler;
import com.hexvane.abilityapi.builtin.oxygen.OxygenHandler;
import com.hexvane.abilityapi.builtin.swimspeed.SwimSpeedHandler;
import com.hexvane.abilityapi.core.stat.AbilityStatSystem;
import com.hexvane.abilityapi.core.stat.StatContributions;
import com.hexvane.abilityapi.builtin.wallclimb.WallClimbHandler;
import com.hexvane.abilityapi.builtin.survival.FallDamageImmunityHandler;
import com.hexvane.abilityapi.builtin.survival.InvulnerabilityHandler;
import com.hexvane.abilityapi.builtin.survival.WaterbreathingHandler;
import com.hexvane.abilityapi.builtin.flight.FlightHandler;
import com.hexvane.abilityapi.core.AbilityHandlerRegistry;
import com.hexvane.abilityapi.core.AbilityRoster;
import com.hexvane.abilityapi.core.asset.AbilityAsset;
import com.hexvane.abilityapi.core.system.AbilityLoginSystem;
import com.hypixel.hytale.assetstore.AssetRegistry;
import com.hypixel.hytale.assetstore.map.DefaultAssetMap;
import com.hypixel.hytale.server.core.asset.HytaleAssetStore;
import com.hexvane.abilityapi.config.AbilityApiConfig;
import com.hexvane.abilityapi.config.MiningFortuneConfig;
import com.riprod.configly.Configly;
import com.hexvane.abilityapi.systems.AbilityConditionService;
import com.hypixel.hytale.logger.HytaleLogger;
import java.util.logging.Level;
import com.hypixel.hytale.server.core.modules.entity.damage.DamageCause;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

/**
 * AbilityAPI - Library mod for player abilities.
 */
public class AbilityAPIPlugin extends JavaPlugin {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public AbilityAPIPlugin(JavaPluginInit init) {
        super(init);
        LOGGER.atInfo().log("AbilityAPI v%s loaded", this.getManifest().getVersion());
    }

    @Override
    protected void setup() {
        AbilityHandlerRegistry.reset();
        StatContributions.reset();

        Configly.register(AbilityApiConfig.TYPE, AbilityApiConfig.class, AbilityApiConfig.CODEC);

        AssetRegistry.register(HytaleAssetStore
                .builder(AbilityAsset.class, new DefaultAssetMap<String, AbilityAsset>())
                .setPath(AbilityAsset.ASSET_PATH)
                .setCodec(AbilityAsset.CODEC)
                .setKeyFunction(AbilityAsset::getId)
                .loadsAfter(DamageCause.class)
                .build());

        AbilityRoster.register(this.getEntityStoreRegistry());

        AbilityHandlerRegistry.register(new FlightHandler());
        AbilityHandlerRegistry.register(new DarkVisionHandler());
        AbilityHandlerRegistry.register(new ResistanceHandler());
        AbilityHandlerRegistry.register(new StrengthHandler());
        AbilityHandlerRegistry.register(new PunchDamageHandler());
        AbilityHandlerRegistry.register(new FallDamageImmunityHandler());
        AbilityHandlerRegistry.register(new InvulnerabilityHandler());
        AbilityHandlerRegistry.register(new WaterbreathingHandler());
        AbilityHandlerRegistry.register(new MiningHasteHandler());
        AbilityHandlerRegistry.register(new MiningFortuneHandler());
        AbilityHandlerRegistry.register(new WallClimbHandler());
        AbilityHandlerRegistry.register(new StaminaRegenHandler());
        AbilityHandlerRegistry.register(new HealthRegenHandler());
        AbilityHandlerRegistry.register(new SecondChanceHandler());
        AbilityHandlerRegistry.register(new ItemMagnetHandler());
        AbilityHandlerRegistry.register(new OxygenHandler());
        AbilityHandlerRegistry.register(new MoveSpeedHandler());
        AbilityHandlerRegistry.register(new SwimSpeedHandler());
        AbilityHandlerRegistry.installAll(this.getEntityStoreRegistry());

        // every contributor must be registered before the stat system materialises its OR query
        this.getEntityStoreRegistry().registerSystem(new AbilityStatSystem());

        MiningFortuneConfig.initialize(this.getDataDirectory());

        this.getEntityStoreRegistry().registerSystem(new AbilityLoginSystem());


        this.getCommandRegistry().registerCommand(new AbilityCommand(this));
        LOGGER.atInfo().log("AbilityAPI setup complete");
    }

    @Override
    protected void shutdown() {
        LOGGER.atInfo().log("AbilityAPI shutdown");
    }
}
