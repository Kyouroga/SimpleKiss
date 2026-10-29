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
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.kyouroga.simplekiss.api.SimpleKissBridge;
import org.kyouroga.simplekiss.bootstrap.BootstrapInitializer;
import org.kyouroga.simplekiss.config.PlatformConfig;
import org.kyouroga.simplekiss.service.KissManager;
import org.kyouroga.simplekiss.storage.PlayerPreferenceMessage;

public class SimpleKiss extends JavaPlugin implements SimpleKissBridge {

    private KissManager manager;
    private UpdateNotifier updateNotifier;

    /**
     * Loads the default configuration, starts shared services, and registers commands.
     */
    @Override
    public void onEnable() {
        saveDefaultConfig();

        manager = BootstrapInitializer.initialize(this);
        getServer().getMessenger().registerOutgoingPluginChannel(this, PlayerPreferenceMessage.CHANNEL);
        getServer().getMessenger().registerIncomingPluginChannel(
                this,
                PlayerPreferenceMessage.CHANNEL,
                new PlayerPreferenceSyncListener(this)
        );
        registerCommands();
        PlatformConfig config = PlatformConfig.from(this);
        if (config.isUpdateCheckEnabled()) {
            updateNotifier = UpdateNotifier.start(this, config);
        }
    }

    private void registerCommands() {
        PluginCommand command = getCommand("spkiss");
        if (command != null) {
            SimpleKissCommand executor = new SimpleKissCommand(this);
            command.setExecutor(executor);
            command.setTabCompleter(executor);
        }
    }

    /**
     * Reloads the Bukkit configuration and restarts the shared services.
     */
    void reloadPluginConfig(org.bukkit.command.CommandSender sender) {
        reloadConfig();
        PlatformConfig config = PlatformConfig.from(this);
        manager = BootstrapInitializer.reload(this, manager, config);

        sender.sendMessage("SimpleKiss config reloaded.");
        getLogger().info(config.formatMessage("Reloaded plugin configuration."));
    }

    @Override
    public void onDisable() {
        getServer().getMessenger().unregisterIncomingPluginChannel(this);
        getServer().getMessenger().unregisterOutgoingPluginChannel(this);
        if (updateNotifier != null) {
            updateNotifier.shutdown();
        }
        if (manager != null) {
            manager.close();
            getLogger().info("SimpleKiss shutting down cleanly");
        }
    }

    /**
     * Returns the manager that tracks player charge and cooldown state.
     */
    public KissManager getManager() {
        return manager;
    }

    /**
     * Sends the player's current backend preference to the connected proxy.
     *
     * @param player online player whose preference changed
     * @param identity identity used by the shared manager
     */
    void syncPreferenceToProxy(Player player, KissManager.PlayerIdentity identity) {
        boolean enabled = manager.isEnabled(identity);
        sendPreferenceMessage(player, PlayerPreferenceMessage.encodePreference(player.getUniqueId(), enabled));
    }

    /**
     * Sends a player preference message through the active backend connection.
     *
     * @param player online player to use for plugin messaging
     * @param message encoded preference message
     */
    void sendPreferenceMessage(Player player, byte[] message) {
        player.sendPluginMessage(this, PlayerPreferenceMessage.CHANNEL, message);
    }
}
