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
import org.kyouroga.simplekiss.api.SimpleKissBridge;
import org.kyouroga.simplekiss.config.PlatformConfig;

import java.lang.reflect.Proxy;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KissManagerTest {

    @Test
    void chargeAndCooldownLifecycleWorks() {
        SimpleKissBridge plugin = createPlugin();
        PlatformConfig config = new PlatformConfig(
                "Test",
                8.0,
                20,
                15.0,
                5L,
                10,
                0.5,
                true,
                "simplekiss.reload",
                "KyourOga",
                "SimpleKiss",
                "main",
                "v"
        );
        KissManager manager = new KissManager(plugin, config);

        TestPlayer player = new TestPlayer();

        assertEquals(0, manager.getCharge(player));
        manager.addCharge(player);
        assertEquals(1, manager.getCharge(player));

        manager.resetCharge(player);
        assertEquals(0, manager.getCharge(player));

        assertFalse(manager.isOnCooldown(player));
        manager.setCooldown(player);
        assertTrue(manager.isOnCooldown(player));

        manager.resetAll();
        assertFalse(manager.isOnCooldown(player));
    }

    private static SimpleKissBridge createPlugin() {
        return (SimpleKissBridge) Proxy.newProxyInstance(
                SimpleKissBridge.class.getClassLoader(),
                new Class<?>[]{SimpleKissBridge.class},
                (proxy, method, args) -> {
                    if (method.getName().equals("getName")) {
                        return "TestPlugin";
                    }
                    if (method.getName().equals("toString")) {
                        return "TestPlugin";
                    }
                    return null;
                }
        );
    }

    private static final class TestPlayer implements KissManager.PlayerIdentity {
        private final UUID uniqueId = UUID.fromString("00000000-0000-0000-0000-000000000001");

        @Override
        public UUID getUniqueId() {
            return uniqueId;
        }
    }
}
