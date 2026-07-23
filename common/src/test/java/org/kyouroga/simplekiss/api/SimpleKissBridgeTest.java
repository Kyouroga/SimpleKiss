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
package org.kyouroga.simplekiss.api;

import io.papermc.paper.plugin.configuration.PluginMeta;
import io.papermc.paper.plugin.lifecycle.event.LifecycleEventManager;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.generator.BiomeProvider;
import org.bukkit.generator.ChunkGenerator;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginLoader;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.InputStream;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SimpleKissBridgeTest {

    @Test
    void bridgeExposesConvenienceMethods() {
        TestBridge bridge = new TestBridge();

        assertEquals("TestBridge", bridge.getDisplayName());
        bridge.logInfo("hello");
        bridge.logWarning("warn");
    }

    @SuppressWarnings({"deprecation", "removal"})
    private static final class TestBridge implements SimpleKissBridge {
        @Override
        public String getName() {
            return "TestBridge";
        }

        @Override
        public Logger getLogger() {
            return Logger.getLogger("TestBridge");
        }

        @Override
        public File getDataFolder() {
            return new File(".");
        }

        @Override
        public PluginDescriptionFile getDescription() {
            return null;
        }

        @Override
        public PluginMeta getPluginMeta() {
            return null;
        }

        @Override
        public FileConfiguration getConfig() {
            return null;
        }

        @Override
        public InputStream getResource(String resource) {
            return null;
        }

        @Override
        public void saveConfig() {
        }

        @Override
        public void saveDefaultConfig() {
        }

        @Override
        public void saveResource(String resource, boolean replace) {
        }

        @Override
        public void reloadConfig() {
        }

        @Override
        public PluginLoader getPluginLoader() {
            return null;
        }

        @Override
        public Server getServer() {
            return null;
        }

        @Override
        public boolean isEnabled() {
            return true;
        }

        @Override
        public void onDisable() {
        }

        @Override
        public void onLoad() {
        }

        @Override
        public void onEnable() {
        }

        @Override
        public boolean isNaggable() {
            return false;
        }

        @Override
        public void setNaggable(boolean canNag) {
        }

        @Override
        public ChunkGenerator getDefaultWorldGenerator(String worldName, String id) {
            return null;
        }

        @Override
        public BiomeProvider getDefaultBiomeProvider(String worldName, String id) {
            return null;
        }

        @Override
        public LifecycleEventManager<Plugin> getLifecycleManager() {
            return null;
        }

        @Override
        public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
            return false;
        }

        @Override
        public String namespace() {
            return "simplekiss";
        }

        @Override
        public java.util.List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
            return java.util.Collections.emptyList();
        }
    }
}
