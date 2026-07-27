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
package org.kyouroga.simplekiss.compat;

import org.kyouroga.simplekiss.config.PlatformConfig;

public final class UniversalEntry {
    private final PlatformConfig config;

    /**
     * Creates a compatibility entry for the supplied configuration.
     */
    public UniversalEntry(PlatformConfig config) {
        this.config = config;
    }

    /**
     * Checks the values required by the shared kiss logic.
     */
    public boolean validateSettings() {
        return config.getDistance() > 0
                && config.getChargeTime() > 0
                && config.getLookAngle() >= 1.0
                && config.getLookAngle() <= 90.0
                && config.getParticleCount() > 0;
    }

    public String statusMessage() {
        return config.formatMessage("Universal compatibility support active: distance="
                + config.getDistance() + ", charge=" + config.getChargeTime());
    }

    public String summary() {
        return String.format(
                "%s settings: distance=%.1f, charge-time=%d, look-angle=%.1f°, particles=%d",
                config.getPlatformName(),
                config.getDistance(),
                config.getChargeTime(),
                config.getLookAngle(),
                config.getParticleCount()
        );
    }
}
