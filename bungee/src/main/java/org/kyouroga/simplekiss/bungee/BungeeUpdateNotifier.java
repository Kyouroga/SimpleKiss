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
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.event.PostLoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import org.kyouroga.simplekiss.config.PlatformConfig;
import org.kyouroga.simplekiss.update.ModrinthUpdateChecker;
import org.kyouroga.simplekiss.update.UpdateStatus;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class BungeeUpdateNotifier implements Listener, Runnable {
    private final BungeePlugin plugin;
    private final PlatformConfig config;
    private final ScheduledExecutorService scheduler;
    private volatile UpdateStatus latestStatus = UpdateStatus.none();

    /**
     * Creates the notifier and schedules a dedicated daemon thread for polling Modrinth.
     */
    private BungeeUpdateNotifier(BungeePlugin plugin, PlatformConfig config) {
        this.plugin = plugin;
        this.config = config;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "SimpleKiss-Bungee-UpdateChecker");
            thread.setDaemon(true);
            return thread;
        });
    }

    /**
     * Starts the update poller and registers the login listener used to notify admins on proxy join.
     */
    public static BungeeUpdateNotifier start(BungeePlugin plugin, PlatformConfig config) {
        BungeeUpdateNotifier notifier = new BungeeUpdateNotifier(plugin, config);
        notifier.run();
        notifier.scheduler.scheduleAtFixedRate(notifier, config.getUpdateCheckIntervalHours(), config.getUpdateCheckIntervalHours(), TimeUnit.HOURS);
        ProxyServer.getInstance().getPluginManager().registerListener(plugin, notifier);
        plugin.getLogger().info(config.formatMessage("Modrinth updater enabled. Checks run every " + config.getUpdateCheckIntervalHours() + " hours."));
        return notifier;
    }

    /**
     * Stops the scheduled update checks and releases the polling thread.
     */
    public void shutdown() {
        scheduler.shutdownNow();
    }

    /**
     * Polls Modrinth for the newest compatible plugin version and stores the current update state.
     */
    @Override
    public void run() {
        try {
            latestStatus = ModrinthUpdateChecker.check(
                    config.getModrinthProjectId(),
                    plugin.getDescription().getVersion(),
                    config.getModrinthVersionType());

            if (latestStatus.isUpdateAvailable()) {
                plugin.getLogger().warning(config.formatMessage(
                        "Update available: " + latestStatus.getCurrentTag() + " -> " + latestStatus.getLatestVersion()
                                + " (" + latestStatus.getReleaseType() + ", " + latestStatus.getBehindBy() + " behind)"));
            }
        } catch (IOException | InterruptedException exception) {
            plugin.getLogger().warning(config.formatMessage("Failed to check Modrinth for updates: " + exception.getMessage()));
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Notifies a joining operator when the proxy is running behind the latest published Modrinth version.
     */
    @EventHandler
    public void onPostLogin(PostLoginEvent event) {
        if (event == null || event.getPlayer() == null) {
            return;
        }

        if (!event.getPlayer().hasPermission(config.getUpdateNotifyPermission()) || !latestStatus.isUpdateAvailable()) {
            return;
        }

        event.getPlayer().sendMessage(new TextComponent(
                "§6[SimpleKiss] §eA newer plugin version is available: §f" + latestStatus.getLatestVersion()
                        + " §e(you are " + latestStatus.getBehindBy() + " behind)."));
    }
}
