/*
 * SimpleKiss is under Kyouroga - https://github.com/Kyouroga/SimpleKiss
 * Copyright (C) 2026 kyouroga
 *
 * This software is licensed under the GNU General Public License, version 3.
 * You are free to use, modify, and redistribute this software under the terms
 * of the GPL as published by the Free Software Foundation.
 *
 * This program is provided without any warranty, including but not limited to
 * the warranties of merchantability or fitness for a particular purpose.
 *
 * For the full license text, see:
 * https://www.gnu.org/licenses/gpl-3.0.html
 */
package org.kyouroga.simplekiss.bootstrap;

import org.kyouroga.simplekiss.api.SimpleKissBridge;
import org.kyouroga.simplekiss.compat.UniversalEntry;
import org.kyouroga.simplekiss.config.PlatformConfig;
import org.kyouroga.simplekiss.service.KissBedrock;
import org.kyouroga.simplekiss.service.KissManager;
import org.kyouroga.simplekiss.service.KissTask;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;

/**
 * Starts and reloads the shared SimpleKiss services for a platform plugin.
 */
public final class BootstrapInitializer {

    private BootstrapInitializer() {
    }

    /**
     * Creates the configuration, manager, and repeating task during plugin startup.
     */
    public static KissManager initialize(SimpleKissBridge plugin) {
        PlatformConfig config = PlatformConfig.from(plugin);
        UniversalEntry universalEntry = new UniversalEntry(config);

        if (config.isInvalid()) {
            plugin.getLogger().warning(config.formatMessage("Invalid plugin settings detected; resetting config to defaults."));
            restoreDefaultConfig(plugin);
            config = PlatformConfig.from(plugin);
        }

        if (!universalEntry.validateSettings()) {
            plugin.getLogger().warning(config.formatMessage("Invalid plugin settings detected; using safe defaults."));
        }

        plugin.getLogger().info(universalEntry.statusMessage());

        /**
         * Announce optional Geyser/Floodgate Bedrock support when the bridge APIs are present.
         */
        KissBedrock kissBedrock = new KissBedrock();
        plugin.getLogger().info(config.formatMessage(kissBedrock.describeSupport()));
        plugin.getLogger().info(config.formatMessage("Starting SimpleKiss core services"));

        KissManager manager = new KissManager(plugin, config);
        new KissTask(plugin, manager, config).runTaskTimer(plugin, 0L, 1L);

        plugin.getLogger().info(config.formatMessage("SimpleKiss bootstrap complete"));
        return manager;
    }

    /**
     * Restores the bundled default configuration file when the current values are unsafe.
     */
    private static void restoreDefaultConfig(SimpleKissBridge plugin) {
        File dataFolder = plugin.getDataFolder();
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            plugin.getLogger().warning("Unable to create plugin data folder for config reset.");
            return;
        }

        File configFile = new File(dataFolder, "config.yml");
        try (InputStream defaults = plugin.getResource("config.yml")) {
            if (defaults == null) {
                plugin.getLogger().warning("No bundled config.yml found for reset operation.");
                return;
            }
            Files.copy(defaults, configFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
            plugin.saveConfig();
            plugin.reloadConfig();
        } catch (IOException exception) {
            plugin.getLogger().log(Level.WARNING, "Failed to reset config.yml to defaults", exception);
        }
    }

    /**
     * Replaces the previous manager with one using the reloaded configuration.
     */
    public static KissManager reload(SimpleKissBridge plugin, KissManager previous, PlatformConfig config) {
        if (previous != null) {
            previous.resetAll();
        }

        plugin.getLogger().info(config.formatMessage("Reloading SimpleKiss plugin configuration"));
        if (config.isInvalid()) {
            plugin.getLogger().warning(config.formatMessage("Invalid plugin settings detected; resetting config to defaults."));
            restoreDefaultConfig(plugin);
            config = PlatformConfig.from(plugin);
        }
        KissManager manager = new KissManager(plugin, config);
        new KissTask(plugin, manager, config).runTaskTimer(plugin, 0L, 1L);
        plugin.getLogger().info(config.formatMessage("SimpleKiss reload complete"));
        return manager;
    }
}
