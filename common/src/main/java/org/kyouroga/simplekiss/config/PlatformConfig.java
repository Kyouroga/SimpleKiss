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
package org.kyouroga.simplekiss.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.util.Locale;
import java.util.Map;

/**
 * Immutable configuration shared by the platform modules.
 */
public final class PlatformConfig {
    private final String platformName;
    private final double distance;
    private final int chargeTime;
    private final double lookAngle;
    private final long cooldownTicks;
    private final int particleCount;
    private final double particleOffset;
    private final boolean updateCheckEnabled;
    private final String updateNotifyPermission;
    private final String githubOwner;
    private final String githubRepo;
    private final String githubBranch;
    private final String githubTagPrefix;

    /**
     * Creates a configuration from already parsed values.
     */
    public PlatformConfig(String platformName,
                          double distance,
                          int chargeTime,
                          double lookAngle,
                          long cooldownTicks,
                          int particleCount,
                          double particleOffset,
                          boolean updateCheckEnabled,
                          String updateNotifyPermission,
                          String githubOwner,
                          String githubRepo,
                          String githubBranch,
                          String githubTagPrefix) {
        this.platformName = platformName;
        this.distance = distance;
        this.chargeTime = chargeTime;
        this.lookAngle = lookAngle;
        this.cooldownTicks = cooldownTicks;
        this.particleCount = particleCount;
        this.particleOffset = particleOffset;
        this.updateCheckEnabled = updateCheckEnabled;
        this.updateNotifyPermission = updateNotifyPermission;
        this.githubOwner = githubOwner;
        this.githubRepo = githubRepo;
        this.githubBranch = githubBranch;
        this.githubTagPrefix = githubTagPrefix;
    }

    public static PlatformConfig from(Plugin plugin) {
        FileConfiguration config = plugin.getConfig();
        return fromMap(plugin.getName(), config.getValues(true));
    }

    /**
     * Reads values from a nested map and applies defaults for missing or invalid entries.
     */
    public static PlatformConfig fromMap(String platformName, Map<String, Object> config) {
        return new PlatformConfig(
                platformName,
                getDouble(config, "distance", 8.0),
                getInt(config, "charge-time", 20),
                getDouble(config, "look-angle", 15.0),
                getLong(config, "cooldown", 100L),
                getInt(config, "particles.count", 10),
                getDouble(config, "particles.offset", 0.5),
                getBoolean(config, "update-check.enabled", true),
                getString(config, "update-check.notify-permission", "simplekiss.reload"),
                getString(config, "update-check.github-owner", "KyourOga"),
                getString(config, "update-check.github-repo", "SimpleKiss"),
                getString(config, "update-check.github-branch", "main"),
                getString(config, "update-check.github-tag-prefix", "v")
        );
    }

    /**
     * Reads a boolean setting or returns the supplied default when it is absent or invalid.
     */
    private static boolean getBoolean(Map<String, Object> config, String path, boolean defaultValue) {
        Object value = getValue(config, path);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return defaultValue;
    }

    /**
     * Reads a string setting or returns the supplied default when it is absent or invalid.
     */
    private static String getString(Map<String, Object> config, String path, String defaultValue) {
        Object value = getValue(config, path);
        if (value instanceof String) {
            return (String) value;
        }
        return defaultValue;
    }

    /**
     * Reads an integer setting or returns the supplied default when it is absent or invalid.
     */
    private static int getInt(Map<String, Object> config, String path, int defaultValue) {
        Object value = getValue(config, path);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return defaultValue;
    }

    /**
     * Reads a long setting or returns the supplied default when it is absent or invalid.
     */
    private static long getLong(Map<String, Object> config, String path, long defaultValue) {
        Object value = getValue(config, path);
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        return defaultValue;
    }

    /**
     * Reads a decimal setting or returns the supplied default when it is absent or invalid.
     */
    private static double getDouble(Map<String, Object> config, String path, double defaultValue) {
        Object value = getValue(config, path);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return defaultValue;
    }

    @SuppressWarnings("unchecked")
    /**
     * Finds a value in a nested configuration map using a dot-separated path.
     */
    private static Object getValue(Map<String, Object> config, String path) {
        String[] parts = path.split("\\.");
        Map<String, Object> current = config;

        for (int i = 0; i < parts.length - 1; i++) {
            Object section = current.get(parts[i]);
            if (!(section instanceof Map)) {
                return null;
            }
            current = (Map<String, Object>) section;
        }
        return current.get(parts[parts.length - 1]);
    }

    /** Returns the platform name used in messages. */
    public String getPlatformName() {
        return platformName;
    }

    /** Returns the maximum distance allowed between players. */
    public double getDistance() {
        return distance;
    }

    /** Returns the number of ticks needed to charge a kiss. */
    public int getChargeTime() {
        return chargeTime;
    }

    /** Returns the maximum angle a player may look away from a target. */
    public double getLookAngle() {
        return lookAngle;
    }

    /** Returns the cooldown length in server ticks. */
    public long getCooldownTicks() {
        return cooldownTicks;
    }

    /** Returns the number of heart particles to show. */
    public int getParticleCount() {
        return particleCount;
    }

    /** Returns the particle spread around the target. */
    public double getParticleOffset() {
        return particleOffset;
    }

    /** Returns whether update checks are enabled. */
    public boolean isUpdateCheckEnabled() {
        return updateCheckEnabled;
    }

    /** Returns the permission required to receive update notices. */
    public String getUpdateNotifyPermission() {
        return updateNotifyPermission;
    }

    /** Returns the GitHub repository owner used for update checks. */
    public String getGithubOwner() {
        return githubOwner;
    }

    /** Returns the GitHub repository name used for update checks. */
    public String getGithubRepo() {
        return githubRepo;
    }

    /** Returns the branch compared during update checks. */
    public String getGithubBranch() {
        return githubBranch;
    }

    /** Returns the prefix expected on release tags. */
    public String getGithubTagPrefix() {
        return githubTagPrefix;
    }

    /**
     * Returns whether the current values would require a safe reset to defaults.
     */
    public boolean isInvalid() {
        return distance <= 0
                || chargeTime <= 0
                || lookAngle < 1.0
                || lookAngle > 90.0
                || particleCount <= 0;
    }

    /**
     * Adds the uppercase platform name to a user-facing message.
     */
    public String formatMessage(String message) {
        return String.format(Locale.ROOT, "[%s] %s", platformName.toUpperCase(Locale.ROOT), message);
    }
}
