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

import com.velocitypowered.api.proxy.ProxyServer;
import org.kyouroga.simplekiss.config.PlatformConfig;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.logging.Level;

public final class VelocityPlugin {
    private final PlatformConfig config;
    private VelocityUpdateNotifier updateNotifier;

    /**
     * Creates the entry point with the shared platform configuration.
     */
    public VelocityPlugin(PlatformConfig config) {
        this.config = config;
    }

    /**
     * Checks for a supported Velocity 4.x proxy.
     */
    public boolean isSupported(ProxyServer proxyServer) {
        if (proxyServer == null) {
            return false;
        }

        return proxyServer.getVersion().getName().startsWith("Velocity")
                && proxyServer.getVersion().getVersion().startsWith("4.");
    }

    public String describeServer(ProxyServer proxyServer) {
        if (proxyServer == null) {
            return config.formatMessage("Velocity server unavailable");
        }

        return config.formatMessage("Velocity server="
                + proxyServer.getVersion().getName()
                + " "
                + proxyServer.getVersion().getVersion());
    }

    public String status() {
        return config.formatMessage("Velocity compatibility layer active");
    }

    /**
     * Returns the running plugin version so the updater compares the actual plugin build, not the proxy runtime.
     */
    public String getPluginVersion() {
        String version = VelocityPlugin.class.getPackage().getImplementationVersion();
        return version == null || version.isBlank() ? "unknown" : version;
    }

    /**
     * Starts the Velocity-side Modrinth update polling and join-based notifier for operators.
     */
    public void enable(ProxyServer proxyServer) {
        updateNotifier = VelocityUpdateNotifier.start(this, config, proxyServer);
    }

    /**
     * Cancels the periodic update check when the proxy is shutting down.
     */
    public void disable() {
        if (updateNotifier != null) {
            updateNotifier.shutdown();
        }
    }

    /**
     * Restores the default Velocity configuration file when the current file is missing or invalid.
     */
    public void ensureDefaultConfig(File dataFolder) {
        if (dataFolder == null) {
            return;
        }

        if (!dataFolder.exists() && !dataFolder.mkdirs()) {
            return;
        }

        File configFile = new File(dataFolder, "config.yml");
        if (configFile.exists() && configFile.length() > 0) {
            return;
        }

        try (InputStream defaults = getClass().getClassLoader().getResourceAsStream("config.yml")) {
            if (defaults == null) {
                return;
            }
            Files.copy(defaults, configFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to restore Velocity config.yml", exception);
        }
    }
}
