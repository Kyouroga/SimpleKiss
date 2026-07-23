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
package org.kyouroga.simplekiss.api;

import org.bukkit.plugin.Plugin;

/**
 * Common plugin contract used by the shared SimpleKiss services.
 */
public interface SimpleKissBridge extends Plugin {

    /**
     * Returns the name used in user-facing log messages.
     */
    default String getDisplayName() {
        return getName();
    }

    /**
     * Writes an informational message through the platform logger.
     */
    default void logInfo(String message) {
        getLogger().info(message);
    }

    /**
     * Writes a warning message through the platform logger.
     */
    default void logWarning(String message) {
        getLogger().warning(message);
    }
}
