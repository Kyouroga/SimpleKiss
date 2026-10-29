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

import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.PostLoginEvent;
import com.velocitypowered.api.proxy.Player;
import com.velocitypowered.api.proxy.ProxyServer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.kyouroga.simplekiss.config.PlatformConfig;
import org.kyouroga.simplekiss.update.ModrinthUpdateChecker;
import org.kyouroga.simplekiss.update.UpdateStatus;

import java.io.IOException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public final class VelocityUpdateNotifier implements Runnable {
    private final VelocityPlugin plugin;
    private final PlatformConfig config;
    private final ProxyServer proxyServer;
    private final ScheduledExecutorService scheduler;
    private volatile UpdateStatus latestStatus = UpdateStatus.none();

    /**
     * Creates the notifier with its own daemon scheduler for periodic Modrinth checks.
     */
    private VelocityUpdateNotifier(VelocityPlugin plugin, PlatformConfig config, ProxyServer proxyServer) {
        this.plugin = plugin;
        this.config = config;
        this.proxyServer = proxyServer;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "SimpleKiss-Velocity-UpdateChecker");
            thread.setDaemon(true);
            return thread;
        });
    }

    /**
     * Starts the Motion/Velocity update polling and subscribes the listener to login events.
     */
    public static VelocityUpdateNotifier start(VelocityPlugin plugin, PlatformConfig config, ProxyServer proxyServer) {
        VelocityUpdateNotifier notifier = new VelocityUpdateNotifier(plugin, config, proxyServer);
        if (proxyServer != null) {
            proxyServer.getEventManager().register(plugin, notifier);
        }
        notifier.run();
        notifier.scheduler.scheduleAtFixedRate(notifier, config.getUpdateCheckIntervalHours(), config.getUpdateCheckIntervalHours(), TimeUnit.HOURS);
        return notifier;
    }

    /**
     * Stops the polling loop and ends the scheduled background task cleanly.
     */
    public void shutdown() {
        scheduler.shutdownNow();
    }

    /**
     * Sends an update notice to operators after a player logs in if the proxy is behind the latest Modrinth build.
     */
    @Subscribe
    public void onPostLogin(PostLoginEvent event) {
        if (event == null) {
            return;
        }

        Player player = event.getPlayer();
        if (player == null || !player.hasPermission(config.getUpdateNotifyPermission()) || !latestStatus.isUpdateAvailable()) {
            return;
        }

        player.sendMessage(playerNotice(latestStatus));
    }

    /**
     * Queries Modrinth and stores the newest available version metadata for later join-time notifications.
     */
    public void run() {
        try {
            String currentVersion = plugin.getPluginVersion();

            UpdateStatus previousStatus = latestStatus;
            UpdateStatus checkedStatus = ModrinthUpdateChecker.check(
                    config.getModrinthProjectId(),
                    currentVersion);
            latestStatus = checkedStatus;

            if (proxyServer != null && isNewUpdate(previousStatus, checkedStatus)) {
                proxyServer.getConsoleCommandSource().sendMessage(Component.text(
                    updateDescription(checkedStatus), NamedTextColor.YELLOW));
                for (Player player : proxyServer.getAllPlayers()) {
                    if (player.hasPermission(config.getUpdateNotifyPermission())) {
                        player.sendMessage(playerNotice(checkedStatus));
                    }
                }
            }
        } catch (IOException | InterruptedException exception) {
            Thread.currentThread().interrupt();
        }
    }

    private boolean isNewUpdate(UpdateStatus previousStatus, UpdateStatus checkedStatus) {
        return checkedStatus.isUpdateAvailable()
                && (!previousStatus.isUpdateAvailable()
                || !previousStatus.getLatestVersion().equals(checkedStatus.getLatestVersion()));
    }

    private String updateDescription(UpdateStatus status) {
        return "Update available: " + status.getCurrentTag() + " -> " + status.getDisplayLatestVersion()
                + " (" + status.getReleaseType() + ", " + status.getBehindBy() + " behind)";
    }

    private Component playerNotice(UpdateStatus status) {
        return Component.text("[SimpleKiss] ", NamedTextColor.GOLD)
                .append(Component.text("A newer plugin version is available: ", NamedTextColor.YELLOW))
                .append(Component.text(status.getDisplayLatestVersion(), NamedTextColor.WHITE))
                .append(Component.text(" (you are " + status.getBehindBy() + " behind).", NamedTextColor.YELLOW));
    }
}
