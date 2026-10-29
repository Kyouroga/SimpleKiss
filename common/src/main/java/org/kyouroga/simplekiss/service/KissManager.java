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
import org.bukkit.scheduler.BukkitTask;
import org.kyouroga.simplekiss.api.SimpleKissBridge;
import org.kyouroga.simplekiss.config.PlatformConfig;
import org.kyouroga.simplekiss.storage.PlayerPreferenceDatabase;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
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
    private long currentTick;

    // Players who have opted out of kiss interactions.
    private final Set<UUID> disabledPlayers;
    private final PlayerPreferenceDatabase preferenceDatabase;
    private BukkitTask task;

    /**
     * Creates a manager backed by the platform plugin, configuration, and preference database.
     */
    public KissManager(
            SimpleKissBridge plugin,
            PlatformConfig config,
            PlayerPreferenceDatabase preferenceDatabase
    ) {
        this.plugin = plugin;
        this.config = config;
        this.preferenceDatabase = preferenceDatabase;
        this.disabledPlayers = new HashSet<>(preferenceDatabase.loadDisabledPlayers());
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

    public void resetAll() {
        chargeTicks.clear();
        cooldowns.clear();
    }

    /**
     * Returns whether a player has kiss interactions enabled.
     */
    public boolean isEnabled(PlayerIdentity player) {
        return !disabledPlayers.contains(player.getUniqueId());
    }

    /**
     * Enables or disables kiss interactions for a player.
     */
    public void setEnabled(PlayerIdentity player, boolean enabled) {
        UUID playerId = player.getUniqueId();
        // Persist first so a failed database write cannot diverge from in-memory state.
        preferenceDatabase.saveEnabled(playerId, enabled);
        if (enabled) {
            disabledPlayers.remove(playerId);
        } else {
            disabledPlayers.add(playerId);
            resetCharge(player);
        }
    }

    /**
     * Attaches the repeating task so reload and shutdown can cancel it reliably.
     *
     * @param task scheduled task owned by this manager
     */
    public void setTask(BukkitTask task) {
        this.task = task;
    }

    /**
     * Cancels this manager's repeating task without closing the shared database.
     */
    public void stopTask() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    /**
     * Returns the database shared by this manager and its reload replacement.
     *
     * @return the active player preference database
     */
    public PlayerPreferenceDatabase getPreferenceDatabase() {
        return preferenceDatabase;
    }

    /**
     * Stops the repeating task and closes the preference database on plugin shutdown.
     */
    public void close() {
        stopTask();
        preferenceDatabase.close();
    }

    /**
     * Advances the manager clock once for each server tick.
     */
    void advanceTick() {
        currentTick++;
    }

    public boolean isOnCooldown(Player p) {
        return isOnCooldown(PlayerIdentity.from(p));
    }

    public boolean isOnCooldown(PlayerIdentity p) {
        if (!cooldowns.containsKey(p.getUniqueId())) return false;

        long last = cooldowns.get(p.getUniqueId());
        return currentTick - last < config.getCooldownTicks();
    }

    public void setCooldown(Player p) {
        setCooldown(PlayerIdentity.from(p));
    }

    public void setCooldown(PlayerIdentity p) {
        cooldowns.put(p.getUniqueId(), currentTick);
    }

    public interface PlayerIdentity {
        /** Returns the stable UUID used to track this player. */
        UUID getUniqueId();

        /** Creates an identity view for a Bukkit player. */
        static PlayerIdentity from(Player player) {
            return player::getUniqueId;
        }
    }
}
