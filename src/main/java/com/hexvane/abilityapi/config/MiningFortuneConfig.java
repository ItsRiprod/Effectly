package com.hexvane.abilityapi.config;

import com.hypixel.hytale.logger.HytaleLogger;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import javax.annotation.Nonnull;

@Deprecated
public final class MiningFortuneConfig {
    private static final HytaleLogger LOGGER = HytaleLogger.forEnclosingClass();
    private static final String LEGACY_FILE_NAME = "mining_fortune_blocks.json";

    private MiningFortuneConfig() {}

    @Deprecated
    public static void initialize(@Nonnull Path pluginDataDirectory) {
        Path legacy = pluginDataDirectory.resolve(LEGACY_FILE_NAME);
        if (Files.isRegularFile(legacy)) {
            LOGGER.atWarning().log(
                    "%s is no longer read. Move its block ids into MiningFortuneBlocks in "
                            + "Server/Configs/AbilityAPI.json, then delete the old file.",
                    legacy);
        }
    }

    @Deprecated
    @Nonnull
    public static Set<String> getAffectedBlockIds() {
        return AbilityApiConfig.get().getMiningFortuneBlocks();
    }
}
