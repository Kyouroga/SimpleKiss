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
package org.kyouroga.simplekiss.velocity;

import com.google.inject.Inject;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PluginMessageEvent;
import com.velocitypowered.api.event.player.ServerConnectedEvent;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import com.velocitypowered.api.proxy.ServerConnection;
import com.velocitypowered.api.proxy.messages.ChannelIdentifier;
import com.velocitypowered.api.proxy.messages.MinecraftChannelIdentifier;
import org.slf4j.Logger;
import org.kyouroga.simplekiss.config.PlatformConfig;
import org.kyouroga.simplekiss.storage.PlayerPreferenceDatabase;
import org.kyouroga.simplekiss.storage.PlayerPreferenceMessage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;

/**
 * Velocity entry point for proxy commands and Paper preference synchronization.
 */
@Plugin(
        id = "simplekiss",
        name = "SimpleKiss",
        version = "1.1.0",
        description = "A proximity-based kiss effects in Minecraft.",
        authors = {"kyouroga"}
)
public final class VelocityPlugin {
    private final ProxyServer proxyServer;
    private final Logger logger;
    private final Path dataDirectory;
    private final ChannelIdentifier preferenceChannel =
            MinecraftChannelIdentifier.from(PlayerPreferenceMessage.CHANNEL);
    private PlatformConfig config = PlatformConfig.fromMap("Velocity", Map.of());
    private PlayerPreferenceDatabase preferenceDatabase;
    private VelocityUpdateNotifier updateNotifier;
    private CommandMeta toggleCommandMeta;

    /**
     * Creates the Velocity plugin with services supplied by the proxy.
     *
     * @param proxyServer active proxy instance
     * @param logger plugin logger
     * @param dataDirectory plugin data directory
     */
    @Inject
    public VelocityPlugin(ProxyServer proxyServer, Logger logger, @DataDirectory Path dataDirectory) {
        this.proxyServer = proxyServer;
        this.logger = logger;
        this.dataDirectory = dataDirectory;
        // Register early so the proxy initialization event is not missed.
        proxyServer.getEventManager().register(this, this);
    }

    /**
     * Checks whether this plugin is running on the supported Velocity 3.x API family.
     *
     * @param server proxy server to inspect
     * @return true when the proxy identifies as Velocity 3.x
     */
    public boolean isSupported(ProxyServer server) {
        return server != null
                && server.getVersion().getName().startsWith("Velocity")
                && server.getVersion().getVersion().startsWith("3.");
    }

    /**
     * Describes the running Velocity server for compatibility diagnostics.
     *
     * @param server proxy server to describe
     * @return formatted server details
     */
    public String describeServer(ProxyServer server) {
        if (server == null) {
            return config.formatMessage("Velocity server unavailable");
        }
        return config.formatMessage("Velocity server="
                + server.getVersion().getName()
                + " "
                + server.getVersion().getVersion());
    }

    /**
     * Returns the compatibility-layer status message.
     *
     * @return formatted status
     */
    public String status() {
        return config.formatMessage("Velocity compatibility layer active");
    }

    /**
     * Initializes commands, messaging, persistence, and update notifications.
     */
    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        ensureDefaultConfig(dataDirectory);
        config = PlatformConfig.fromMap("Velocity", Map.of());
        preferenceDatabase = new PlayerPreferenceDatabase(dataDirectory.resolve("player-preferences.db"));
        proxyServer.getChannelRegistrar().register(preferenceChannel);

        toggleCommandMeta = proxyServer.getCommandManager()
                .metaBuilder("spkiss")
                .plugin(this)
                .build();
        proxyServer.getCommandManager().register(
                toggleCommandMeta,
                new VelocityToggleCommand(this, preferenceDatabase)
        );
        logger.info(config.formatMessage("Velocity compatibility layer active"));
        enable(proxyServer);
    }

    /**
     * Starts periodic update checks for the supplied proxy.
     *
     * @param server active proxy
     */
    public void enable(ProxyServer server) {
        if (updateNotifier == null) {
            updateNotifier = VelocityUpdateNotifier.start(this, config, server);
        }
    }

    /**
     * Releases the database, command registration, channel, and update task on shutdown.
     */
    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        disable();
        if (toggleCommandMeta != null) {
            proxyServer.getCommandManager().unregister(toggleCommandMeta);
        }
        proxyServer.getEventManager().unregisterListener(this, this);
        proxyServer.getChannelRegistrar().unregister(preferenceChannel);
        if (preferenceDatabase != null) {
            preferenceDatabase.close();
        }
    }

    /**
     * Stops periodic update checking.
     */
    public void disable() {
        if (updateNotifier != null) {
            updateNotifier.shutdown();
            updateNotifier = null;
        }
    }

    /**
     * Requests the Paper backend's saved state whenever a player connects or switches servers.
     */
    @Subscribe
    public void onServerConnected(ServerConnectedEvent event) {
        Player player = event.getPlayer();
        try {
            byte[] message = preferenceDatabase.findEnabled(player.getUniqueId())
                    .map(enabled -> PlayerPreferenceMessage.encodePreference(player.getUniqueId(), enabled))
                    .orElseGet(() -> PlayerPreferenceMessage.encodeRequest(player.getUniqueId()));
            player.getCurrentServer().ifPresent(connection ->
                    connection.sendPluginMessage(preferenceChannel, message));
        } catch (IllegalStateException exception) {
            logger.error("Unable to synchronize player preference.", exception);
        }
    }

    /**
     * Applies preference updates returned by an installed Paper backend.
     */
    @Subscribe
    public void onPluginMessage(PluginMessageEvent event) {
        if (!preferenceChannel.equals(event.getIdentifier())) {
            return;
        }
        event.setResult(PluginMessageEvent.ForwardResult.handled());

        if (!(event.getSource() instanceof ServerConnection)
                || !(event.getTarget() instanceof Player player)) {
            return;
        }
        try {
            PlayerPreferenceMessage.Message message = PlayerPreferenceMessage.decode(event.getData());
            if (message.request() || !player.getUniqueId().equals(message.playerId())) {
                logger.warn("Rejected an invalid backend preference message.");
                return;
            }
            preferenceDatabase.saveEnabled(message.playerId(), message.enabled());
        } catch (IOException | IllegalStateException exception) {
            logger.warn("Unable to process backend preference message.", exception);
        }
    }

    /**
     * Sends a preference update to the player's current Paper backend, if connected.
     *
     * @param player online proxy player
     * @param enabled current preference
     */
    void sendPreference(Player player, boolean enabled) {
        player.getCurrentServer().ifPresent(connection ->
                connection.sendPluginMessage(
                        preferenceChannel,
                        PlayerPreferenceMessage.encodePreference(player.getUniqueId(), enabled)
                )
        );
    }

    /**
     * Logs a preference persistence failure.
     *
     * @param exception database failure
     */
    void logPreferenceFailure(IllegalStateException exception) {
        logger.warn("Unable to save player preference.", exception);
    }

    /**
     * Returns the running plugin version for update checks.
     *
     * @return version string
     */
    public String getPluginVersion() {
        String version = VelocityPlugin.class.getPackage().getImplementationVersion();
        return version == null || version.isBlank() ? "1.1.0" : version;
    }

    /**
     * Ensures the proxy data directory contains the bundled config defaults.
     *
     * @param directory plugin data directory
     */
    private void ensureDefaultConfig(Path directory) {
        try {
            Files.createDirectories(directory);
            Path configFile = directory.resolve("config.yml");
            if (Files.exists(configFile) && Files.size(configFile) > 0) {
                return;
            }

            try (InputStream defaults = getClass().getClassLoader().getResourceAsStream("config.yml")) {
                if (defaults == null) {
                    throw new IOException("No bundled Velocity config.yml found.");
                }
                Files.copy(defaults, configFile, StandardCopyOption.REPLACE_EXISTING);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to prepare Velocity plugin data.", exception);
        }
    }

    /**
     * Ensures the proxy data directory contains the bundled config defaults.
     *
     * @param directory plugin data directory
     */
    public void ensureDefaultConfig(java.io.File directory) {
        if (directory != null) {
            ensureDefaultConfig(directory.toPath());
        }
    }

}
