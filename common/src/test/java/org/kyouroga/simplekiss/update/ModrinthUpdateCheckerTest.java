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
package org.kyouroga.simplekiss.update;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModrinthUpdateCheckerTest {

    @Test
    void newestModrinthBetaIsDetectedEvenWhenItsVersionSortsLower() {
        String response = "[{\"version_number\":\"beta-1.0\",\"version_type\":\"beta\"},"
                + "{\"version_number\":\"1.1.0\",\"version_type\":\"release\"}]";

        UpdateStatus status = ModrinthUpdateChecker.fromVersionResponse(response, "simplekiss", "1.1.0");

        assertTrue(status.isUpdateAvailable());
        assertEquals("beta-1.0", status.getLatestVersion());
        assertEquals("beta", status.getReleaseType());
    }
}