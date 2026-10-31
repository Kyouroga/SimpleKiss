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
package org.kyouroga.simplekiss.bungee;

import net.md_5.bungee.api.ProxyServer;
import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.connection.Server;
import net.md_5.bungee.api.event.PluginMessageEvent;
import net.md_5.bungee.api.event.ServerConnectedEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.event.EventHandler;
import org.kyouroga.simplekiss.config.PlatformConfig;
import org.kyouroga.simplekiss.storage.PlayerPreferenceDatabase;
import org.kyouroga.simplekiss.storage.PlayerPreferenceMessage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Level;

public final class BungeePlugin extends Plugin implements Listener {
    private PlatformConfig config;
    private BungeeUpdateNotifier updateNotifier;
    private PlayerPreferenceDatabase preferenceDatabase;

    /**
     * Creates an instance without loaded configuration.
     */
    public BungeePlugin() {
        this.config = PlatformConfig.fromMap("BungeeCord", Map.of());
    }

    /**
     * Creates an instance with the shared platform configuration.
     */
    public BungeePlugin(PlatformConfig config) {
        this.config = config;
    }

    @Override
    public void onEnable() {
        try {
            ensureDefaultConfig();
            config = loadConfig();
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to load BungeeCord configuration.", exception);
        }
        getProxy().registerChannel(PlayerPreferenceMessage.CHANNEL);
        preferenceDatabase = new PlayerPreferenceDatabase(
                getDataFolder().toPath().resolve("player-preferences.db")
        );
        getProxy().getPluginManager().registerListener(this, this);
        getProxy().getPluginManager().registerCommand(this, new BungeeToggleCommand(this, preferenceDatabase));
        getLogger().info("BungeeCord compatibility layer active");
        updateNotifier = BungeeUpdateNotifier.start(this, config);
    }

    @Override
    public void onDisable() {
        if (updateNotifier != null) {
            updateNotifier.shutdown();
        }
        getProxy().getPluginManager().unregisterListeners(this);
        getProxy().getPluginManager().unregisterCommands(this);
        getProxy().unregisterChannel(PlayerPreferenceMessage.CHANNEL);
        if (preferenceDatabase != null) {
            preferenceDatabase.close();
        }
    }

    /**
     * Creates or restores the bundled config.yml when the proxy configuration is missing or invalid.
     */
    private void ensureDefaultConfig() throws IOException {
        File dataFolder = getDataFolder();
        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            throw new IOException("Unable to create BungeeCord data folder.");
        }

        File configFile = new File(dataFolder, "config.yml");
        if (configFile.exists() && configFile.length() > 0) {
            return;
        }

        try (InputStream defaults = getResourceAsStream("config.yml")) {
            if (defaults == null) {
                throw new IOException("No bundled BungeeCord config.yml found.");
            }
            Files.copy(defaults, configFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private PlatformConfig loadConfig() throws IOException {
        PlatformConfig loaded = PlatformConfig.loadYaml(
                getDataFolder().toPath().resolve("config.yml"),
                "BungeeCord"
        );
        if (loaded.isInvalid()) {
            throw new IOException("BungeeCord configuration contains invalid settings.");
        }
        return loaded;
    }

    void reloadConfig(CommandSender sender) {
        try {
            PlatformConfig loaded = loadConfig();
            config = loaded;
            getLogger().info("reload complete");
            if (sender instanceof ProxiedPlayer) {
                sender.sendMessage(new net.md_5.bungee.api.chat.TextComponent("SimpleKiss config reloaded."));
            }
        } catch (IOException exception) {
            getLogger().log(Level.WARNING, "Unable to reload BungeeCord configuration.", exception);
            if (sender instanceof ProxiedPlayer) {
                sender.sendMessage(new net.md_5.bungee.api.chat.TextComponent(
                        "Could not reload SimpleKiss configuration. Check the proxy log."
                ));
            }
        }
    }

    /**
     * Sends a preference update to the player's connected Paper backend.
     *
     * @param player online proxy player
     * @param enabled current preference
     */
    void sendPreference(ProxiedPlayer player, boolean enabled) {
        player.sendData(
                PlayerPreferenceMessage.CHANNEL,
                PlayerPreferenceMessage.encodePreference(player.getUniqueId(), enabled)
        );
    }

    /**
     * Requests the backend's persisted state after a player connects or switches servers.
     */
    @EventHandler
    public void onServerConnected(ServerConnectedEvent event) {
        ProxiedPlayer player = event.getPlayer();
        try {
            Optional<Boolean> savedPreference = preferenceDatabase.findEnabled(player.getUniqueId());
            byte[] message = savedPreference
                    .map(enabled -> PlayerPreferenceMessage.encodePreference(player.getUniqueId(), enabled))
                    .orElseGet(() -> PlayerPreferenceMessage.encodeRequest(player.getUniqueId()));
            player.sendData(PlayerPreferenceMessage.CHANNEL, message);
        } catch (IllegalStateException exception) {
            getLogger().log(Level.SEVERE, "Unable to synchronize player preference.", exception);
        }
    }

    /**
     * Applies preference updates sent from an installed Paper backend.
     */
    @EventHandler
    public void onPluginMessage(PluginMessageEvent event) {
        if (!PlayerPreferenceMessage.CHANNEL.equals(event.getTag())) {
            return;
        }
        event.setCancelled(true);
        if (!(event.getSender() instanceof Server)
                || !(event.getReceiver() instanceof ProxiedPlayer player)) {
            return;
        }

        try {
            PlayerPreferenceMessage.Message message = PlayerPreferenceMessage.decode(event.getData());
            if (message.request() || !player.getUniqueId().equals(message.playerId())) {
                getLogger().warning("Rejected an invalid backend preference message.");
                return;
            }
            preferenceDatabase.saveEnabled(message.playerId(), message.enabled());
        } catch (IOException | IllegalStateException exception) {
            getLogger().log(Level.WARNING, "Unable to process backend preference message.", exception);
        }
    }

    /**
     * Checks whether the current proxy identifies itself as BungeeCord.
     */
    public boolean isProxySupported() {
        ProxyServer server = ProxyServer.getInstance();
        return server != null && server.getVersion().contains("BungeeCord");
    }
}
