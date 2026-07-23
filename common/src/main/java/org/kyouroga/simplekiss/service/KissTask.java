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
package org.kyouroga.simplekiss.service;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;
import org.kyouroga.simplekiss.api.SimpleKissBridge;
import org.kyouroga.simplekiss.config.PlatformConfig;

/**
 * Checks eligible players each server tick and triggers kiss particles when a charge completes.
 */
public class KissTask extends BukkitRunnable {

    private final SimpleKissBridge plugin;
    private final KissManager manager;
    private final PlatformConfig config;

    /**
     * Creates the repeating task for a plugin instance.
     */
    public KissTask(SimpleKissBridge plugin, KissManager manager, PlatformConfig config) {
        this.plugin = plugin;
        this.manager = manager;
        this.config = config;
    }

    @Override
    public void run() {

        double maxDistance = config.getDistance();
        double maxAngle = Math.toRadians(config.getLookAngle());
        int chargeTime = config.getChargeTime();

        // A charge is kept only while the player remains sneaking and looking at a valid target.
        for (Player p : plugin.getServer().getOnlinePlayers()) {

            if (!p.isSneaking()) {
                manager.resetCharge(p);
                continue;
            }

            Player target = getTargetPlayer(p, maxDistance, maxAngle);

            if (target == null || !canPerformKiss(p, target)) {
                manager.resetCharge(p);
                continue;
            }

            if (manager.isOnCooldown(p)) continue;

            manager.addCharge(p);

            if (manager.getCharge(p) >= chargeTime) {

                triggerKiss(p, target);

                manager.setCooldown(p);
                manager.resetCharge(p);
            }
        }
    }

    private boolean canPerformKiss(Player p, Player target) {
        if (p.getGameMode() == GameMode.SPECTATOR || target.getGameMode() == GameMode.SPECTATOR) {
            return false;
        }

        boolean pInvisible = p.isInvisible();
        boolean targetInvisible = target.isInvisible();
        boolean canSeeEachOther = p.canSee(target) && target.canSee(p);

        if (pInvisible || targetInvisible) {
            return pInvisible && targetInvisible && canSeeEachOther;
        }

        return canSeeEachOther;
    }

    private void triggerKiss(Player p, Player target) {
        boolean bothHidden = p.isInvisible() && target.isInvisible();

        spawnHearts(target, bothHidden ? new Player[]{p, target} : null);

        if (target.isSneaking()) {
            spawnHearts(p, bothHidden ? new Player[]{p, target} : null);
        }
    }

    private void spawnHearts(Player player, Player[] viewers) {
        Location loc = player.getLocation().add(0, 1.6, 0);

        int count = config.getParticleCount();
        double offset = config.getParticleOffset();

        if (viewers == null) {
            player.getWorld().spawnParticle(
                    Particle.HEART,
                    loc,
                    count,
                    offset, offset, offset,
                    0.01
            );
            return;
        }

        for (Player viewer : viewers) {
            viewer.spawnParticle(
                    Particle.HEART,
                    loc,
                    count,
                    offset, offset, offset,
                    0.01
            );
        }
    }

    private Player getTargetPlayer(Player p, double maxDistance, double maxAngle) {
        Location eye = p.getEyeLocation();
        Vector direction = eye.getDirection().normalize();

        Player best = null;
        double bestDistance = maxDistance;

        // Select the nearest player inside the configured view cone.
        for (Player other : p.getWorld().getPlayers()) {
            if (other == p) continue;

            Location head = other.getLocation().add(0, 1.5, 0);
            double distance = head.distance(eye);

            if (distance > bestDistance) continue;

            Vector toTarget = head.toVector().subtract(eye.toVector()).normalize();
            double angle = direction.angle(toTarget);

            if (angle > maxAngle) continue;

            best = other;
            bestDistance = distance;
        }

        return best;
    }
}
