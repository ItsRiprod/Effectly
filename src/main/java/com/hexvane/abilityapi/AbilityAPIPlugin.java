package com.hexvane.abilityapi;

import com.hypixel.hytale.logger.HytaleLogger;
import com.hypixel.hytale.server.core.plugin.JavaPlugin;
import com.hypixel.hytale.server.core.plugin.JavaPluginInit;

public class AbilityAPIPlugin extends JavaPlugin {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();

    public AbilityAPIPlugin(JavaPluginInit init) {
        super(init);
    }

    @Override
    protected void setup() {
        LOGGER.atInfo().log("AbilityAPI compatibility shim active - 'hexvane:AbilityAPI' resolves to "
                + "Riprod:Effectly. Integrators should migrate to com.riprod.abilityapi.");
    }
}
