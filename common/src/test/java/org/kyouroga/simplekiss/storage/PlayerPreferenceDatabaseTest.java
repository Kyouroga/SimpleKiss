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
package org.kyouroga.simplekiss.storage;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerPreferenceDatabaseTest {
    @TempDir
    Path temporaryDirectory;

    @Test
    void distinguishesMissingPreferencesFromSavedValues() {
        UUID playerId = UUID.fromString("00000000-0000-0000-0000-000000000001");
        try (PlayerPreferenceDatabase database = new PlayerPreferenceDatabase(
                temporaryDirectory.resolve("preferences.db"))) {
            assertTrue(database.findEnabled(playerId).isEmpty());
            database.saveEnabled(playerId, false);
            assertFalse(database.findEnabled(playerId).orElseThrow());
            database.saveEnabled(playerId, true);
            assertTrue(database.findEnabled(playerId).orElseThrow());
        }
    }
}
