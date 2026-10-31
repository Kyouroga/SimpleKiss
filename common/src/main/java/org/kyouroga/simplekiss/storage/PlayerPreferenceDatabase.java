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
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

/**
 * Stores player interaction preferences in a local SQLite database.
 */
public final class PlayerPreferenceDatabase implements AutoCloseable {
    private final Connection connection;
    private boolean closed;

    /**
     * Opens or creates a database at the supplied path and initializes its schema.
     *
     * @param databasePath location of the SQLite database file
     */
    public PlayerPreferenceDatabase(Path databasePath) {
        this.connection = openConnection(databasePath);
    }

    /**
     * Loads the UUIDs whose kiss interactions are disabled.
     *
     * @return a snapshot of disabled player UUIDs
     */
    public synchronized Set<UUID> loadDisabledPlayers() {
        ensureOpen();
        Set<UUID> disabledPlayers = new HashSet<>();
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT uuid FROM player_preferences WHERE enabled = 0");
             ResultSet results = statement.executeQuery()) {
            while (results.next()) {
                String uuid = results.getString("uuid");
                try {
                    disabledPlayers.add(UUID.fromString(uuid));
                } catch (IllegalArgumentException exception) {
                    throw new IllegalStateException("Invalid player UUID in preference database: " + uuid, exception);
                }
            }
            return Set.copyOf(disabledPlayers);
        } catch (SQLException exception) {
            throw databaseError("load player preferences", exception);
        }
    }

    /**
     * Persists the enabled state for a player UUID.
     *
     * @param playerId UUID of the player whose preference changed
     * @param enabled whether kiss interactions are enabled
     */
    public synchronized void saveEnabled(UUID playerId, boolean enabled) {
        ensureOpen();
        try (PreparedStatement statement = connection.prepareStatement(
                """
                INSERT INTO player_preferences (uuid, enabled) VALUES (?, ?)
                ON CONFLICT(uuid) DO UPDATE SET enabled = excluded.enabled
                """)) {
            statement.setString(1, playerId.toString());
            statement.setInt(2, enabled ? 1 : 0);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw databaseError("save player preference", exception);
        }
    }

    /**
     * Returns a player's saved enabled state, or an empty result if no preference exists.
     *
     * @param playerId UUID of the player to query
     * @return the saved state, if present
     */
    public synchronized Optional<Boolean> findEnabled(UUID playerId) {
        ensureOpen();
        try (PreparedStatement statement = connection.prepareStatement(
                "SELECT enabled FROM player_preferences WHERE uuid = ?")) {
            statement.setString(1, playerId.toString());
            try (ResultSet results = statement.executeQuery()) {
                return results.next() ? Optional.of(results.getInt("enabled") == 1) : Optional.empty();
            }
        } catch (SQLException exception) {
            throw databaseError("read player preference", exception);
        }
    }

    /**
     * Closes the database connection.
     */
    @Override
    public synchronized void close() {
        if (closed) {
            return;
        }
        try {
            connection.close();
            closed = true;
        } catch (SQLException exception) {
            throw databaseError("close preference database", exception);
        }
    }

    private static Connection openConnection(Path databasePath) {
        if (databasePath == null) {
            throw new IllegalArgumentException("Database path cannot be null.");
        }

        Connection connection = null;
        try {
            Path absolutePath = databasePath.toAbsolutePath();
            Path parent = absolutePath.getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            Class.forName("org.sqlite.JDBC");
            String jdbcPath = absolutePath.toString().replace('\\', '/');
            connection = DriverManager.getConnection("jdbc:sqlite:" + jdbcPath);
            try (Statement statement = connection.createStatement()) {
                // A preference change is durable as soon as the command reports success.
                statement.execute("PRAGMA synchronous = FULL");
                statement.execute("""
                        CREATE TABLE IF NOT EXISTS player_preferences (
                            uuid TEXT PRIMARY KEY NOT NULL,
                            enabled INTEGER NOT NULL CHECK (enabled IN (0, 1))
                        )
                        """);
            }
            return connection;
        } catch (ClassNotFoundException | IOException | SQLException exception) {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException closeException) {
                    exception.addSuppressed(closeException);
                }
            }
            throw new IllegalStateException("Unable to initialize player preference database.", exception);
        }
    }

    private void ensureOpen() {
        if (closed) {
            throw new IllegalStateException("Player preference database is closed.");
        }
    }

    private static IllegalStateException databaseError(String operation, SQLException exception) {
        return new IllegalStateException("Unable to " + operation + ".", exception);
    }
}
