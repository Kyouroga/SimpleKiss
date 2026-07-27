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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UpdateStatusTest {

    /**
     * Verifies that the empty status contains no update details.
     */
    @Test
    void noneReturnsEmptyUpdateState() {
        UpdateStatus status = UpdateStatus.none();

        assertFalse(status.isUpdateAvailable());
        assertEquals(0, status.getBehindBy());
        assertEquals("", status.getCurrentTag());
        assertEquals("", status.getTargetBranch());
        assertEquals("", status.getRepoUrl());
    }

    /**
     * Verifies that an update status retains its supplied details.
     */
    @Test
    void storesUpdateDetails() {
        UpdateStatus status = new UpdateStatus(true, 2, "v1.2.0", "main", "https://example.com/repo");

        assertTrue(status.isUpdateAvailable());
        assertEquals(2, status.getBehindBy());
        assertEquals("v1.2.0", status.getCurrentTag());
        assertEquals("main", status.getTargetBranch());
        assertEquals("https://example.com/repo", status.getRepoUrl());
    }
}
