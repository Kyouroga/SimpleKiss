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

import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.command.SimpleCommand;
import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import org.kyouroga.simplekiss.storage.PlayerPreferenceDatabase;

import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

/**
 * Handles proxy-side /spkiss toggle commands.
 */
final class VelocityToggleCommand implements SimpleCommand {
    private final VelocityPlugin plugin;
    private final PlayerPreferenceDatabase database;

    /**
     * Creates the command using the plugin's persistent preferences.
     *
     * @param plugin active proxy plugin
     * @param database preference database
     */
    VelocityToggleCommand(VelocityPlugin plugin, PlayerPreferenceDatabase database) {
        this.plugin = plugin;
        this.database = database;
    }

    /**
     * Persists the requested state and forwards it to the player's current Paper server.
     */
    @Override
    public void execute(Invocation invocation) {
        CommandSource source = invocation.source();
        String[] args = invocation.arguments();
        if (args.length == 1 && "reload".equalsIgnoreCase(args[0])) {
            if (!source.hasPermission("simplekiss.reload")) {
                source.sendMessage(Component.text("You do not have permission to reload SimpleKiss."));
                return;
            }
            plugin.reloadConfig(source);
            return;
        }

        if (args.length > 0 && "toggle".equalsIgnoreCase(args[0])) {
            if (!(source instanceof Player player)) {
                source.sendMessage(Component.text("Only players can check their own kiss setting."));
                return;
            }

            if (args.length == 1) {
                try {
                    boolean enabled = database.findEnabled(player.getUniqueId()).orElse(true);
                    player.sendMessage(Component.text(
                            "Kiss interactions are currently " + (enabled ? "enabled." : "disabled.")
                    ));
                } catch (IllegalStateException exception) {
                    plugin.logPreferenceFailure(exception);
                    player.sendMessage(Component.text("Could not read your kiss preference. Please try again later."));
                }
                return;
            }

            if (args.length != 2 || !("true".equalsIgnoreCase(args[1]) || "false".equalsIgnoreCase(args[1]))) {
                source.sendMessage(Component.text("Usage: /spkiss toggle [true | false]"));
                return;
            }

            boolean enabled = Boolean.parseBoolean(args[1]);
            try {
                database.saveEnabled(player.getUniqueId(), enabled);
                plugin.sendPreference(player, enabled);
                player.sendMessage(Component.text("Kiss interactions " + (enabled ? "enabled." : "disabled.")));
            } catch (IllegalStateException exception) {
                plugin.logPreferenceFailure(exception);
                player.sendMessage(Component.text("Could not save your kiss preference. Please try again later."));
            }
            return;
        }

        source.sendMessage(Component.text("Usage: /spkiss toggle"));
        source.sendMessage(Component.text("Usage: /spkiss toggle <true | false>"));
        if (source.hasPermission("simplekiss.reload")) {
            source.sendMessage(Component.text("Usage: /spkiss reload"));
        }
    }

    /**
     * Suggests the public toggle arguments.
     */
    @Override
    public List<String> suggest(Invocation invocation) {
        String[] args = invocation.arguments();
        if (args.length == 1) {
            String partial = args[0].toLowerCase(Locale.ROOT);
            if ("toggle".startsWith(partial)) {
                return List.of("toggle");
            }
            if (invocation.source().hasPermission("simplekiss.reload") && "reload".startsWith(partial)) {
                return List.of("reload");
            }
            return List.of();
        }
        if (args.length == 2 && "toggle".equalsIgnoreCase(args[0])) {
            String partial = args[1].toLowerCase(Locale.ROOT);
            return Stream.of("true", "false")
                    .filter(value -> value.startsWith(partial))
                    .toList();
        }
        return List.of();
    }
}
