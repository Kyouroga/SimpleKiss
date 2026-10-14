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

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.kyouroga.simplekiss.service.KissManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Handles /spkiss commands for Minecraft Java Edition 1.21.x and 26.1.x through 26.3.
 */
public final class SimpleKissCommand implements CommandExecutor, TabCompleter {
    private final SimpleKiss plugin;

    public SimpleKissCommand(SimpleKiss plugin) {
        this.plugin = plugin;
    }

    /**
     * Executes the public player toggle or an authorized configuration reload.
     *
     * @since 26.3
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 2 && "toggle".equalsIgnoreCase(args[0])) {
            if (!("true".equalsIgnoreCase(args[1]) || "false".equalsIgnoreCase(args[1]))) {
                sender.sendMessage("Usage: /spkiss toggle <true|false>");
                return true;
            }
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Only players can change their own kiss setting.");
                return true;
            }

            boolean enabled = Boolean.parseBoolean(args[1]);
            plugin.getManager().setEnabled(KissManager.PlayerIdentity.from(player), enabled);
            sender.sendMessage("Kiss interactions " + (enabled ? "enabled." : "disabled."));
            return true;
        }

        if (args.length == 1 && "reload".equalsIgnoreCase(args[0])) {
            if (!sender.hasPermission("simplekiss.reload")) {
                sender.sendMessage("You do not have permission to reload SimpleKiss.");
                return true;
            }

            plugin.reloadPluginConfig(sender);
            return true;
        }

        sender.sendMessage("Usage: /spkiss toggle <true|false>");
        if (sender.hasPermission("simplekiss.reload")) {
            sender.sendMessage("Usage: /spkiss reload");
        }
        return true;
    }

    /**
     * Suggests the public toggle to everyone and reload only to authorized senders.
     */
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1) {
            List<String> suggestions = new ArrayList<>();
            String partial = args[0].toLowerCase(Locale.ROOT);
            if ("toggle".startsWith(partial)) {
                suggestions.add("toggle");
            }
            if (sender.hasPermission("simplekiss.reload") && "reload".startsWith(partial)) {
                suggestions.add("reload");
            }
            return suggestions;
        }

        if (args.length == 2 && "toggle".equalsIgnoreCase(args[0])) {
            List<String> suggestions = new ArrayList<>();
            String partial = args[1].toLowerCase(Locale.ROOT);
            if ("true".startsWith(partial)) {
                suggestions.add("true");
            }
            if ("false".startsWith(partial)) {
                suggestions.add("false");
            }
            return suggestions;
        }

        return Collections.emptyList();
    }
}
