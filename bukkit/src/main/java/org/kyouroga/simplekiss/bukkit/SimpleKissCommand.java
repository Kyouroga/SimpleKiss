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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Handles the administrative /spkiss command.
 */
public final class SimpleKissCommand implements CommandExecutor, TabCompleter {
    private final SimpleKiss plugin;

    /**
     * Creates a command handler for the Bukkit plugin.
     */
    public SimpleKissCommand(SimpleKiss plugin) {
        this.plugin = plugin;
    }

    @Override
    /**
     * Handles reload after checking the sender's permission.
     */
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 1 && "reload".equalsIgnoreCase(args[0])) {
            if (!sender.hasPermission("simplekiss.reload")) {
                sender.sendMessage("You do not have permission to reload SimpleKiss.");
                return true;
            }

            plugin.reloadPluginConfig(sender);
            return true;
        }

        sender.sendMessage("Usage: /spkiss reload");
        return true;
    }

    @Override
    /**
     * Suggests reload only to senders allowed to use it.
     */
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (args.length == 1 && sender.hasPermission("simplekiss.reload")) {
            List<String> suggestions = new ArrayList<>();
            if ("reload".startsWith(args[0].toLowerCase())) {
                suggestions.add("reload");
            }
            return suggestions;
        }
        return Collections.emptyList();
    }
}
