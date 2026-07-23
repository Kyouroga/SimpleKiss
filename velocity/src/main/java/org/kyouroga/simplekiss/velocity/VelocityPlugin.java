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

/**
 * Velocity compatibility-layer entry point.
 */
public final class VelocityPlugin {
    private final PlatformConfig config;

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

    /**
     * Formats the detected proxy version for a status message.
     */
    public String describeServer(ProxyServer proxyServer) {
        if (proxyServer == null) {
            return config.formatMessage("Velocity server unavailable");
        }

        return config.formatMessage("Velocity server="
                + proxyServer.getVersion().getName()
                + " "
                + proxyServer.getVersion().getVersion());
    }

    /**
     * Returns the standard compatibility-layer status message.
     */
    public String status() {
        return config.formatMessage("Velocity compatibility layer active");
    }
}
