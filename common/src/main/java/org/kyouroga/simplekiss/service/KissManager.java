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

import org.bukkit.entity.Player;
import org.kyouroga.simplekiss.api.SimpleKissBridge;
import org.kyouroga.simplekiss.config.PlatformConfig;

import java.util.HashMap;
import java.util.UUID;

/**
 * Stores charge progress and cooldown timestamps for players.
 */
public class KissManager {

    private final SimpleKissBridge plugin;
    private final PlatformConfig config;

    // Number of consecutive valid ticks spent charging for each player.
    private final HashMap<UUID, Integer> chargeTicks = new HashMap<>();

    // Time at which each player's last kiss was triggered.
    private final HashMap<UUID, Long> cooldowns = new HashMap<>();

    /**
     * Creates a manager backed by the platform plugin and its configuration.
     */
    public KissManager(SimpleKissBridge plugin, PlatformConfig config) {
        this.plugin = plugin;
        this.config = config;
    }

    public int getCharge(Player p) {
        return getCharge(PlayerIdentity.from(p));
    }

    public int getCharge(PlayerIdentity p) {
        return chargeTicks.getOrDefault(p.getUniqueId(), 0);
    }

    public void addCharge(Player p) {
        addCharge(PlayerIdentity.from(p));
    }

    public void addCharge(PlayerIdentity p) {
        chargeTicks.put(p.getUniqueId(), getCharge(p) + 1);
    }

    public void resetCharge(Player p) {
        resetCharge(PlayerIdentity.from(p));
    }

    public void resetCharge(PlayerIdentity p) {
        chargeTicks.remove(p.getUniqueId());
    }

    /**
     * Clears all charge and cooldown state, normally during a reload.
     */
    public void resetAll() {
        chargeTicks.clear();
        cooldowns.clear();
    }

    public boolean isOnCooldown(Player p) {
        return isOnCooldown(PlayerIdentity.from(p));
    }

    public boolean isOnCooldown(PlayerIdentity p) {
        if (!cooldowns.containsKey(p.getUniqueId())) return false;

        long last = cooldowns.get(p.getUniqueId());
        long cooldownMillis = config.getCooldownTicks() * 50L;

        return System.currentTimeMillis() - last < cooldownMillis;
    }

    public void setCooldown(Player p) {
        setCooldown(PlayerIdentity.from(p));
    }

    public void setCooldown(PlayerIdentity p) {
        cooldowns.put(p.getUniqueId(), System.currentTimeMillis());
    }

    /**
     * Minimal player identity required by the state manager.
     */
    public interface PlayerIdentity {
        UUID getUniqueId();

        static PlayerIdentity from(Player player) {
            return player::getUniqueId;
        }
    }
}
