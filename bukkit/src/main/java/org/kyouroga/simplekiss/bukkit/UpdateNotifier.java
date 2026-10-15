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

import net.kyori.adventure.text.Component;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.kyouroga.simplekiss.config.PlatformConfig;
import org.kyouroga.simplekiss.update.ModrinthUpdateChecker;
import org.kyouroga.simplekiss.update.UpdateStatus;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Periodically checks Modrinth for newer versions and notifies OP players when the server is behind.
 */
public final class UpdateNotifier implements Listener, Runnable {
    private final SimpleKiss plugin;
    private final PlatformConfig config;
    private final ScheduledExecutorService scheduler;
    private volatile UpdateStatus latestStatus = UpdateStatus.none();

    private UpdateNotifier(SimpleKiss plugin, PlatformConfig config) {
        this.plugin = plugin;
        this.config = config;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "SimpleKiss-UpdateChecker");
            thread.setDaemon(true);
            return thread;
        });
    }

    public static UpdateNotifier start(SimpleKiss plugin, PlatformConfig config) {
        UpdateNotifier notifier = new UpdateNotifier(plugin, config);
        notifier.run();
        notifier.scheduler.scheduleAtFixedRate(notifier, config.getUpdateCheckIntervalHours(), config.getUpdateCheckIntervalHours(), TimeUnit.HOURS);
        plugin.getServer().getPluginManager().registerEvents(notifier, plugin);
        plugin.getLogger().info(config.formatMessage("Modrinth updater enabled. Checks run every " + config.getUpdateCheckIntervalHours() + " hours."));
        return notifier;
    }

    public void shutdown() {
        scheduler.shutdownNow();
    }

    @Override
    public void run() {
        try {
            latestStatus = ModrinthUpdateChecker.check(
                    config.getModrinthProjectId(),
                    plugin.getDescription().getVersion(),
                    config.getModrinthVersionType());

            if (latestStatus.isUpdateAvailable()) {
                plugin.getLogger().warning(config.formatMessage(
                        "Update available: " + latestStatus.getCurrentTag() + " -> " + latestStatus.getLatestVersion() +
                                " (" + latestStatus.getReleaseType() + ", " + latestStatus.getBehindBy() + " behind)"));
            }
        } catch (IOException | InterruptedException exception) {
            plugin.getLogger().warning(config.formatMessage("Failed to check Modrinth for updates: " + exception.getMessage()));
            Thread.currentThread().interrupt();
        }
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!event.getPlayer().hasPermission(config.getUpdateNotifyPermission()) || !latestStatus.isUpdateAvailable()) {
            return;
        }

        event.getPlayer().sendMessage(ChatColor.GOLD + "[SimpleKiss] " + ChatColor.YELLOW +
                "A newer plugin version is available: " + ChatColor.WHITE + latestStatus.getLatestVersion() +
                ChatColor.YELLOW + " (you are " + latestStatus.getBehindBy() + " behind)." );
    }
}
