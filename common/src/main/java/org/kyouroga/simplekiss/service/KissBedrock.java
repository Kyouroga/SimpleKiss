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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.UUID;

public final class KissBedrock {

    /**
     * Returns whether the supplied player is detected as a Bedrock player by Geyser or Floodgate.
     *
     * @param player the player to inspect
     * @return true when Bedrock support is available and the player is identified as Bedrock, otherwise false
     */
    public boolean isBedrockPlayer(Object player) {
        if (player == null) {
            return false;
        }

        UUID uniqueId = resolveUniqueId(player);
        if (uniqueId == null) {
            return false;
        }

        return isFloodgatePlayer(uniqueId) || isGeyserPlayer(uniqueId);
    }

    /**
     * Returns a short description of the current Bedrock bridge integration status.
     *
     * @return human-readable support status for startup logging and diagnostics
     */
    public String describeSupport() {
        return "Bedrock support: Geyser/Floodgate detection enabled via optional bridge APIs";
    }

    /**
     * Reads a UUID from the player object using common platform accessor names.
     */
    private UUID resolveUniqueId(Object player) {
        try {
            Method method = player.getClass().getMethod("getUniqueId");
            Object value = method.invoke(player);
            if (value instanceof UUID) {
                return (UUID) value;
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
            // Fall back to the common UUID accessor used by some proxy implementations.
        }

        try {
            Method method = player.getClass().getMethod("getUUID");
            Object value = method.invoke(player);
            if (value instanceof UUID) {
                return (UUID) value;
            }
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
            return null;
        }

        return null;
    }

    /**
     * Checks Floodgate for the supplied player UUID when its API is available.
     */
    private boolean isFloodgatePlayer(UUID uniqueId) {
        return invokeBridgeCheck("org.geysermc.floodgate.api.FloodgateApi", uniqueId,
                "isFloodgatePlayer", "isBedrockPlayer");
    }

    /**
     * Checks Geyser for the supplied player UUID when its API is available.
     */
    private boolean isGeyserPlayer(UUID uniqueId) {
        return invokeBridgeCheck("org.geysermc.geyser.api.GeyserApi", uniqueId,
                "isBedrockPlayer", "isFloodgatePlayer");
    }

    /**
     * Invokes a Bedrock-player check from an optional bridge API.
     */
    private boolean invokeBridgeCheck(String className, UUID uniqueId, String primaryMethod, String fallbackMethod) {
        try {
            Class<?> apiClass = Class.forName(className);
            Object api = invokeStaticAccessor(apiClass);
            if (api == null) {
                return false;
            }

            Method method = findMethod(apiClass, primaryMethod, UUID.class);
            if (method == null) {
                method = findMethod(apiClass, fallbackMethod, UUID.class);
            }
            if (method == null) {
                return false;
            }

            Object result = method.invoke(api, uniqueId);
            return Boolean.TRUE.equals(result);
        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
            return false;
        }
    }

    /**
     * Obtains an API instance through the accessor exposed by a bridge implementation.
     */
    private Object invokeStaticAccessor(Class<?> apiClass) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        try {
            Method instanceMethod = apiClass.getMethod("getInstance");
            if (instanceMethod != null) {
                return instanceMethod.invoke(null);
            }
        } catch (NoSuchMethodException ignored) {
            // Fall back to the direct accessor used by some bridge builds.
        }

        try {
            Method accessor = apiClass.getMethod("api");
            return accessor.invoke(null);
        } catch (NoSuchMethodException ignored) {
            return null;
        }
    }

    /**
     * Looks up a public API method and returns {@code null} when it is unavailable.
     */
    private Method findMethod(Class<?> apiClass, String methodName, Class<?> parameterType) throws NoSuchMethodException {
        try {
            return apiClass.getMethod(methodName, parameterType);
        } catch (NoSuchMethodException ignored) {
            return null;
        }
    }
}
