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
package org.kyouroga.simplekiss.bukkit;

import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.kyouroga.simplekiss.api.SimpleKissBridge;
import org.kyouroga.simplekiss.bootstrap.BootstrapInitializer;
import org.kyouroga.simplekiss.config.PlatformConfig;
import org.kyouroga.simplekiss.service.KissManager;

/**
 * Bukkit entry point for SimpleKiss.
 */
public class SimpleKiss extends JavaPlugin implements SimpleKissBridge {

    private KissManager manager;

    @Override
    /**
     * Loads the default configuration, starts shared services, and registers commands.
     */
    public void onEnable() {
        saveDefaultConfig();

        manager = BootstrapInitializer.initialize(this);
        registerCommands();
    }

    /**
     * Connects the /spkiss command to its executor and tab completer.
     */
    private void registerCommands() {
        PluginCommand command = getCommand("spkiss");
        if (command != null) {
            SimpleKissCommand executor = new SimpleKissCommand(this);
            command.setExecutor(executor);
            command.setTabCompleter(executor);
        }
    }

    void reloadPluginConfig(org.bukkit.command.CommandSender sender) {
        reloadConfig();
        PlatformConfig config = PlatformConfig.from(this);
        manager = BootstrapInitializer.reload(this, manager, config);

        sender.sendMessage("SimpleKiss config reloaded.");
        getLogger().info(config.formatMessage("Reloaded plugin configuration."));
    }

    @Override
    /**
     * Logs shutdown when the shared manager has been initialized.
     */
    public void onDisable() {
        if (manager != null) {
            getLogger().info("SimpleKiss shutting down cleanly");
        }
    }

    public KissManager getManager() {
        return manager;
    }
}