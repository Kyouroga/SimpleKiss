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
import org.junit.jupiter.api.io.TempDir;
import org.kyouroga.simplekiss.api.SimpleKissBridge;
import org.kyouroga.simplekiss.config.PlatformConfig;
import org.kyouroga.simplekiss.storage.PlayerPreferenceDatabase;

import java.lang.reflect.Proxy;
import java.nio.file.Path;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KissManagerTest {

    @TempDir
    Path temporaryDirectory;

    /**
     * Verifies that charge and cooldown state can be created and cleared.
     */
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
                "simplekiss.reload"
        );
        KissManager manager = new KissManager(plugin, config, createDatabase("charge.db"));

        TestPlayer player = new TestPlayer("00000000-0000-0000-0000-000000000001");

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
        manager.close();
    }

    /**
     * Verifies that cooldown duration is measured in server ticks rather than wall-clock time.
     */
    @Test
    void cooldownExpiresAfterConfiguredNumberOfTicks() {
        KissManager manager = new KissManager(createPlugin(), testConfig(), createDatabase("tick-cooldown.db"));
        TestPlayer player = new TestPlayer("00000000-0000-0000-0000-000000000004");

        manager.setCooldown(player);
        assertTrue(manager.isOnCooldown(player));

        for (int tick = 0; tick < 4; tick++) {
            manager.advanceTick();
            assertTrue(manager.isOnCooldown(player));
        }

        manager.advanceTick();
        assertFalse(manager.isOnCooldown(player));
        manager.close();
    }

    @Test
    void forgetPlayerClearsTransientState() {
        KissManager manager = new KissManager(createPlugin(), testConfig(), createDatabase("forget-player.db"));
        TestPlayer player = new TestPlayer("00000000-0000-0000-0000-000000000005");

        manager.addCharge(player);
        manager.setCooldown(player);
        manager.forgetPlayer(player);

        assertEquals(0, manager.getCharge(player));
        assertFalse(manager.isOnCooldown(player));
        manager.close();
    }

    /**
     * Verifies that kiss interaction preferences default to enabled and are tracked per player.
     */
    @Test
    void enabledStateIsTrackedPerPlayer() {
        KissManager manager = new KissManager(createPlugin(), new PlatformConfig(
                "Test",
                8.0,
                20,
                15.0,
                5L,
                10,
                0.5,
                true,
                "simplekiss.reload"
        ), createDatabase("preferences.db"));
        TestPlayer firstPlayer = new TestPlayer("00000000-0000-0000-0000-000000000001");
        TestPlayer secondPlayer = new TestPlayer("00000000-0000-0000-0000-000000000002");

        assertTrue(manager.isEnabled(firstPlayer));
        assertTrue(manager.isEnabled(secondPlayer));

        manager.setEnabled(firstPlayer, false);

        assertFalse(manager.isEnabled(firstPlayer));
        assertTrue(manager.isEnabled(secondPlayer));

        manager.setEnabled(firstPlayer, true);

        assertTrue(manager.isEnabled(firstPlayer));
        manager.close();
    }

    /**
     * Verifies that disabled preferences survive closing and reopening the database.
     */
    @Test
    void disabledPreferenceSurvivesDatabaseReopen() {
        TestPlayer player = new TestPlayer("00000000-0000-0000-0000-000000000003");
        PlayerPreferenceDatabase database = createDatabase("persistent-preferences.db");
        KissManager manager = new KissManager(createPlugin(), testConfig(), database);

        manager.setEnabled(player, false);
        manager.close();

        KissManager reloadedManager = new KissManager(
                createPlugin(),
                testConfig(),
                createDatabase("persistent-preferences.db")
        );
        assertFalse(reloadedManager.isEnabled(player));
        reloadedManager.setEnabled(player, true);
        reloadedManager.close();

        KissManager enabledManager = new KissManager(
                createPlugin(),
                testConfig(),
                createDatabase("persistent-preferences.db")
        );
        assertTrue(enabledManager.isEnabled(player));
        enabledManager.close();
    }

    private PlayerPreferenceDatabase createDatabase(String fileName) {
        return new PlayerPreferenceDatabase(temporaryDirectory.resolve(fileName));
    }

    private static PlatformConfig testConfig() {
        return new PlatformConfig(
                "Test",
                8.0,
                20,
                15.0,
                5L,
                10,
                0.5,
                true,
                "simplekiss.reload"
        );
    }

    /**
     * Creates the minimal plugin proxy needed by the manager.
     */
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
        private final UUID uniqueId;

        private TestPlayer(String uniqueId) {
            this.uniqueId = UUID.fromString(uniqueId);
        }

        /** Returns the fixed UUID used by this test player. */
        @Override
        public UUID getUniqueId() {
            return uniqueId;
        }
    }
}
