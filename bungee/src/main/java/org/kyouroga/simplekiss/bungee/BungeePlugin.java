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
import net.md_5.bungee.api.plugin.Plugin;
import org.kyouroga.simplekiss.config.PlatformConfig;

/**
 * BungeeCord entry point for the compatibility layer.
 */
public final class BungeePlugin extends Plugin {
    private final PlatformConfig config;

    /**
     * Creates an instance without loaded configuration.
     */
    public BungeePlugin() {
        this.config = null;
    }

    /**
     * Creates an instance with the shared platform configuration.
     */
    public BungeePlugin(PlatformConfig config) {
        this.config = config;
    }

    @Override
    /**
     * Announces that the BungeeCord compatibility layer is active.
     */
    public void onEnable() {
        if (config != null) {
            getLogger().info(config.formatMessage("BungeeCord compatibility layer active"));
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
