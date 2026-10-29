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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.UUID;

/**
 * Encodes preference request and update messages sent between proxies and Paper servers.
 */
public final class PlayerPreferenceMessage {
    public static final String CHANNEL = "simplekiss:preferences";

    private static final byte REQUEST = 0;
    private static final byte SET_PREFERENCE = 1;
    private static final int REQUEST_LENGTH = 17;
    private static final int PREFERENCE_LENGTH = 18;

    private PlayerPreferenceMessage() {
    }

    /**
     * Creates a message asking the connected Paper server for the player's saved preference.
     *
     * @param playerId UUID of the player to query
     * @return encoded request
     */
    public static byte[] encodeRequest(UUID playerId) {
        return encode(REQUEST, playerId, false);
    }

    /**
     * Creates a message carrying a player's enabled state.
     *
     * @param playerId UUID of the player whose preference changed
     * @param enabled whether kiss interactions are enabled
     * @return encoded preference update
     */
    public static byte[] encodePreference(UUID playerId, boolean enabled) {
        return encode(SET_PREFERENCE, playerId, enabled);
    }

    /**
     * Decodes and validates a preference message.
     *
     * @param data serialized message bytes
     * @return decoded request or preference update
     * @throws IOException if the message is malformed or has an unknown type
     */
    public static Message decode(byte[] data) throws IOException {
        if (data == null || data.length < REQUEST_LENGTH) {
            throw new IOException("Player preference message is too short.");
        }

        ByteBuffer buffer = ByteBuffer.wrap(data);
        byte type = buffer.get();
        UUID playerId = new UUID(buffer.getLong(), buffer.getLong());
        if (type == REQUEST && data.length == REQUEST_LENGTH) {
            return new Message(true, playerId, false);
        }
        if (type == SET_PREFERENCE && data.length == PREFERENCE_LENGTH) {
            byte enabled = buffer.get();
            if (enabled != 0 && enabled != 1) {
                throw new IOException("Player preference message has an invalid enabled value.");
            }
            return new Message(false, playerId, enabled == 1);
        }
        throw new IOException("Player preference message has an invalid type or length.");
    }

    private static byte[] encode(byte type, UUID playerId, boolean enabled) {
        ByteBuffer buffer = ByteBuffer.allocate(type == REQUEST ? REQUEST_LENGTH : PREFERENCE_LENGTH);
        buffer.put(type);
        buffer.putLong(playerId.getMostSignificantBits());
        buffer.putLong(playerId.getLeastSignificantBits());
        if (type == SET_PREFERENCE) {
            buffer.put((byte) (enabled ? 1 : 0));
        }
        return buffer.array();
    }

    /**
     * Represents a validated preference request or update.
     *
     * @param request true when the proxy is requesting backend state
     * @param playerId UUID associated with the message
     * @param enabled enabled state for a preference update
     */
    public record Message(boolean request, UUID playerId, boolean enabled) {
    }
}
