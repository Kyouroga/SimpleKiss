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

public class KissTask extends BukkitRunnable {

    private final SimpleKissBridge plugin;
    private final KissManager manager;
    private final PlatformConfig config;
    private final KissBedrock kissBedrock;

    public KissTask(SimpleKissBridge plugin, KissManager manager, PlatformConfig config) {
        this.plugin = plugin;
        this.manager = manager;
        this.config = config;
        this.kissBedrock = new KissBedrock();
    }

    /**
     * Advances charging players and triggers completed kisses.
     */
    @Override
    public void run() {
        manager.advanceTick();

        double maxDistance = config.getDistance();
        double maxAngle = Math.toRadians(config.getLookAngle());
        double maxDistanceSquared = maxDistance * maxDistance;
        int chargeTime = config.getChargeTime();

        // A charge is kept only while the player remains sneaking and looking at a valid target.
        for (Player p : plugin.getServer().getOnlinePlayers()) {

            if (!manager.isEnabled(KissManager.PlayerIdentity.from(p))) {
                manager.resetCharge(p);
                continue;
            }

            if (!p.isSneaking()) {
                manager.resetCharge(p);
                continue;
            }

            /**
             * Bedrock players are treated with a slightly wider aiming cone so interaction feels natural.
             */
            boolean bedrockPlayer = kissBedrock.isBedrockPlayer(p);
            Player target = getTargetPlayer(p, maxDistanceSquared, maxAngle, bedrockPlayer);

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

    /**
     * Checks whether both players are eligible to perform a kiss.
     */
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

    /**
     * Shows the kiss effect for the target and, when reciprocated, the initiator.
     */
    private void triggerKiss(Player p, Player target) {
        boolean bothHidden = p.isInvisible() && target.isInvisible();

        spawnHearts(target, bothHidden ? new Player[]{p, target} : null);

        if (target.isSneaking()) {
            spawnHearts(p, bothHidden ? new Player[]{p, target} : null);
        }
    }

    /**
     * Spawns heart particles for the player, optionally limiting who can see them.
     */
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

    /**
     * Finds the nearest player inside the initiator's allowed view cone.
     */
    private Player getTargetPlayer(Player p, double maxDistanceSquared, double maxAngle, boolean bedrockPlayer) {
        Location eye = p.getEyeLocation();
        Vector direction = eye.getDirection().normalize();
        double eyeX = eye.getX();
        double eyeY = eye.getY();
        double eyeZ = eye.getZ();
        double directionX = direction.getX();
        double directionY = direction.getY();
        double directionZ = direction.getZ();
        double effectiveMaxAngle = bedrockPlayer ? Math.max(maxAngle, Math.toRadians(20.0)) : maxAngle;
        double minimumDirectionDot = Math.cos(effectiveMaxAngle);
        double minimumDirectionDotSquared = minimumDirectionDot * minimumDirectionDot;

        Player best = null;
        double bestDistanceSquared = maxDistanceSquared;

        // Select the nearest player inside the configured view cone, with a small tolerance for Bedrock players.
        for (Player other : p.getWorld().getPlayers()) {
            if (other == p || !manager.isEnabled(KissManager.PlayerIdentity.from(other))) continue;

            Location otherLocation = other.getLocation();
            double dx = otherLocation.getX() - eyeX;
            double dy = otherLocation.getY() + 1.5 - eyeY;
            double dz = otherLocation.getZ() - eyeZ;
            double distanceSquared = dx * dx + dy * dy + dz * dz;

            if (distanceSquared > bestDistanceSquared) continue;

            double directionDot = directionX * dx + directionY * dy + directionZ * dz;
            if (distanceSquared > 0.0) {
                if (directionDot < 0.0
                        || (effectiveMaxAngle < Math.PI / 2.0
                        && directionDot * directionDot < minimumDirectionDotSquared * distanceSquared)) {
                    continue;
                }
            }

            best = other;
            bestDistanceSquared = distanceSquared;
        }

        return best;
    }
}
