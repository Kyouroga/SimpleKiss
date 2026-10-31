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

import org.bukkit.entity.Player;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.kyouroga.simplekiss.service.KissManager;
import org.kyouroga.simplekiss.storage.PlayerPreferenceMessage;

import java.io.IOException;

/**
 * Applies preference messages received from a BungeeCord or Velocity proxy.
 */
final class PlayerPreferenceSyncListener implements PluginMessageListener {
    private final SimpleKiss plugin;

    /**
     * Creates a listener for the active Paper plugin instance.
     *
     * @param plugin active Paper plugin
     */
    PlayerPreferenceSyncListener(SimpleKiss plugin) {
        this.plugin = plugin;
    }

    /**
     * Validates and applies a preference request or update from the proxy.
     */
    @Override
    public void onPluginMessageReceived(String channel, Player player, byte[] messageBytes) {
        if (!PlayerPreferenceMessage.CHANNEL.equals(channel) || plugin.getManager() == null) {
            return;
        }

        try {
            PlayerPreferenceMessage.Message message = PlayerPreferenceMessage.decode(messageBytes);
            if (!player.getUniqueId().equals(message.playerId())) {
                plugin.getLogger().warning("Rejected a proxy preference message for a different player.");
                return;
            }

            KissManager.PlayerIdentity identity = KissManager.PlayerIdentity.from(player);
            if (message.request()) {
                plugin.syncPreferenceToProxy(player, identity);
            } else {
                plugin.getManager().setEnabled(identity, message.enabled());
            }
        } catch (IOException | IllegalStateException exception) {
            plugin.getLogger().warning("Unable to process proxy preference message: " + exception.getMessage());
        }
    }
}
