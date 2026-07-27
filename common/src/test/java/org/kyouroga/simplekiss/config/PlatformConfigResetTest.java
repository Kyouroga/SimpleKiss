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

import static org.junit.jupiter.api.Assertions.assertTrue;

class PlatformConfigResetTest {

    /**
     * Verifies that unsafe configuration values require a reset.
     */
    @Test
    void invalidValuesAreDetected() {
        Map<String, Object> config = new LinkedHashMap<>();
        config.put("distance", -1.0);
        config.put("charge-time", 0);
        config.put("look-angle", 95.0);
        config.put("particles", Map.of("count", 0));

        PlatformConfig platformConfig = PlatformConfig.fromMap("TestPlatform", config);

        assertTrue(platformConfig.isInvalid());
    }
}
