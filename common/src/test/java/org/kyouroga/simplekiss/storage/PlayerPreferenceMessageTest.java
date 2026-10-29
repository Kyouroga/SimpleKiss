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

import java.io.IOException;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PlayerPreferenceMessageTest {
    @Test
    void preferenceMessageRoundTrips() throws IOException {
        UUID playerId = UUID.fromString("00000000-0000-0000-0000-000000000001");

        PlayerPreferenceMessage.Message disabled = PlayerPreferenceMessage.decode(
                PlayerPreferenceMessage.encodePreference(playerId, false)
        );
        assertFalse(disabled.request());
        assertEquals(playerId, disabled.playerId());
        assertFalse(disabled.enabled());

        PlayerPreferenceMessage.Message enabled = PlayerPreferenceMessage.decode(
                PlayerPreferenceMessage.encodePreference(playerId, true)
        );
        assertTrue(enabled.enabled());
    }

    @Test
    void requestMessageRoundTrips() throws IOException {
        UUID playerId = UUID.fromString("00000000-0000-0000-0000-000000000002");

        PlayerPreferenceMessage.Message request = PlayerPreferenceMessage.decode(
                PlayerPreferenceMessage.encodeRequest(playerId)
        );
        assertTrue(request.request());
        assertEquals(playerId, request.playerId());
    }

    @Test
    void malformedMessagesAreRejected() {
        assertThrows(IOException.class, () -> PlayerPreferenceMessage.decode(new byte[0]));
        assertThrows(IOException.class, () -> PlayerPreferenceMessage.decode(new byte[18]));

        byte[] invalidPreference = PlayerPreferenceMessage.encodePreference(
                UUID.fromString("00000000-0000-0000-0000-000000000003"),
                true
        );
        invalidPreference[17] = 2;
        assertThrows(IOException.class, () -> PlayerPreferenceMessage.decode(invalidPreference));
    }
}
