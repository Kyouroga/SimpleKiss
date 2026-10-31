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

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.connection.ProxiedPlayer;
import net.md_5.bungee.api.plugin.Command;
import org.kyouroga.simplekiss.storage.PlayerPreferenceDatabase;

/**
 * Handles proxy-side /spkiss toggle commands.
 */
final class BungeeToggleCommand extends Command {
    private final BungeePlugin plugin;
    private final PlayerPreferenceDatabase database;

    /**
     * Creates the command using the plugin's persistent preferences.
     *
     * @param plugin active proxy plugin
     * @param database preference database
     */
    BungeeToggleCommand(BungeePlugin plugin, PlayerPreferenceDatabase database) {
        super("spkiss");
        this.plugin = plugin;
        this.database = database;
    }

    /**
     * Persists the requested state and forwards it to the player's current Paper server.
     */
    @Override
    public void execute(CommandSender sender, String[] args) {
        if (args.length == 1 && "reload".equalsIgnoreCase(args[0])) {
            if (!sender.hasPermission("simplekiss.reload")) {
                sender.sendMessage(new TextComponent("You do not have permission to reload SimpleKiss."));
                return;
            }
            plugin.reloadConfig(sender);
            return;
        }

        if (args.length > 0 && "toggle".equalsIgnoreCase(args[0])) {
            if (!(sender instanceof ProxiedPlayer player)) {
                sender.sendMessage(new TextComponent("Only players can check their own kiss setting."));
                return;
            }

            if (args.length == 1) {
                try {
                    boolean enabled = database.findEnabled(player.getUniqueId()).orElse(true);
                    player.sendMessage(new TextComponent(
                            "Kiss interactions are currently " + (enabled ? "enabled." : "disabled.")
                    ));
                } catch (IllegalStateException exception) {
                    plugin.getLogger().warning("Unable to read player preference: " + exception.getMessage());
                    player.sendMessage(new TextComponent("Could not read your kiss preference. Please try again later."));
                }
                return;
            }

            if (args.length != 2 || !("true".equalsIgnoreCase(args[1]) || "false".equalsIgnoreCase(args[1]))) {
                sender.sendMessage(new TextComponent("Usage: /spkiss toggle [true | false]"));
                return;
            }

            boolean enabled = Boolean.parseBoolean(args[1]);
            try {
                database.saveEnabled(player.getUniqueId(), enabled);
                plugin.sendPreference(player, enabled);
                player.sendMessage(new TextComponent("Kiss interactions " + (enabled ? "enabled." : "disabled.")));
            } catch (IllegalStateException exception) {
                plugin.getLogger().warning("Unable to save player preference: " + exception.getMessage());
                player.sendMessage(new TextComponent("Could not save your kiss preference. Please try again later."));
            }
            return;
        }

        sender.sendMessage(new TextComponent("Usage: /spkiss toggle"));
        sender.sendMessage(new TextComponent("Usage: /spkiss toggle <true | false>"));
        if (sender.hasPermission("simplekiss.reload")) {
            sender.sendMessage(new TextComponent("Usage: /spkiss reload"));
        }
    }
}
