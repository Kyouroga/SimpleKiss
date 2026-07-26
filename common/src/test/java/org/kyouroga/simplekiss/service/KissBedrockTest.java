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

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KissBedrockTest {

    @Test
    void describeSupportIncludesBedrockAndBridgeInformation() {
        KissBedrock support = new KissBedrock();

        String description = support.describeSupport();

        assertTrue(description.contains("Bedrock"));
        assertTrue(description.contains("Geyser") || description.contains("Floodgate") || description.contains("No"));
    }

    @Test
    void nullPlayerIsNeverReportedAsBedrock() {
        KissBedrock support = new KissBedrock();

        assertFalse(support.isBedrockPlayer(null));
    }

    @Test
    void genericPlayerObjectCanBeInspectedWithoutBukkitTypes() {
        KissBedrock support = new KissBedrock();
        Object player = new TestPlayer(UUID.fromString("00000000-0000-0000-0000-000000000001"));

        assertFalse(support.isBedrockPlayer(player));
    }

    private static final class TestPlayer {
        private final UUID uniqueId;

        private TestPlayer(UUID uniqueId) {
            this.uniqueId = uniqueId;
        }

        public UUID getUniqueId() {
            return uniqueId;
        }
    }
}
