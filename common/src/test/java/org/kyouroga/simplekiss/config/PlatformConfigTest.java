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

import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlatformConfigTest {

    @Test
    void fromMapUsesNestedConfigValuesAndDefaults() {
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("distance", 12.5);
        config.put("charge-time", 30);
        config.put("look-angle", 25.0);
        config.put("cooldown", 250L);

        Map<String, Object> particles = new LinkedHashMap<>();
        particles.put("count", 7);
        particles.put("offset", 1.2);
        config.put("particles", particles);

        Map<String, Object> updateCheck = new LinkedHashMap<>();
        updateCheck.put("enabled", false);
        config.put("update-check", updateCheck);

        PlatformConfig platformConfig = PlatformConfig.fromMap("TestPlatform", config);

        assertEquals("TestPlatform", platformConfig.getPlatformName());
        assertEquals(12.5, platformConfig.getDistance());
        assertEquals(30, platformConfig.getChargeTime());
        assertEquals(25.0, platformConfig.getLookAngle());
        assertEquals(250L, platformConfig.getCooldownTicks());
        assertEquals(7, platformConfig.getParticleCount());
        assertEquals(1.2, platformConfig.getParticleOffset());
        assertFalse(platformConfig.isUpdateCheckEnabled());
        assertEquals("simplekiss.reload", platformConfig.getUpdateNotifyPermission());
        assertEquals("KyourOga", platformConfig.getGithubOwner());
    }

    @Test
    void formatMessageUsesUppercasePlatformName() {
        PlatformConfig platformConfig = new PlatformConfig(
                "testPlatform",
                8.0,
                20,
                15.0,
                100L,
                10,
                0.5,
                true,
                "simplekiss.reload",
                "KyourOga",
                "SimpleKiss",
                "main",
                "v"
        );

        assertEquals("[TESTPLATFORM] ready", platformConfig.formatMessage("ready"));
        assertTrue(platformConfig.formatMessage("ready").startsWith("[TESTPLATFORM]"));
    }
}
