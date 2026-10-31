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

/*
 * @author sejoujp
 * Stays as draft for now until new versioning scheme is confirmed and tested.
 */
package org.kyouroga.simplekiss.update;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Checks the latest Modrinth release metadata and compares it to the running plugin version.
 */
public final class ModrinthUpdateChecker {
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private static final Pattern VERSION_OBJECT_PATTERN = Pattern.compile(
            "\\{.*?\"version_number\"\\s*:\\s*\"([^\"]+)\".*?\"version_type\"\\s*:\\s*\"([^\"]+)\".*?\\}",
            Pattern.DOTALL
    );

    private ModrinthUpdateChecker() {
    }

    /**
     * Returns whether the latest discovered version is newer than the running one.
     */
    public static boolean isNewer(String currentVersion, String latestVersion) {
        return compareVersions(currentVersion, latestVersion) > 0;
    }

    /**
     * Calculates a rough version gap when the current version is behind the newest candidate.
     */
    public static int calculateBehindBy(String currentVersion, String latestVersion) {
        if (currentVersion == null || currentVersion.isBlank() || latestVersion == null || latestVersion.isBlank()) {
            return 0;
        }
        if (compareVersions(latestVersion, currentVersion) <= 0) {
            return 0;
        }

        List<Integer> currentNumbers = extractNumericParts(currentVersion);
        List<Integer> latestNumbers = extractNumericParts(latestVersion);
        int maxLength = Math.max(currentNumbers.size(), latestNumbers.size());

        for (int i = 0; i < maxLength; i++) {
            int current = i < currentNumbers.size() ? currentNumbers.get(i) : 0;
            int latest = i < latestNumbers.size() ? latestNumbers.get(i) : 0;
            if (current != latest) {
                return Math.max(1, Math.abs(latest - current));
            }
        }

        return 1;
    }

    /**
    * Queries Modrinth for its newest published project version, regardless of release channel.
     */
    public static UpdateStatus check(String projectId, String currentVersion) throws IOException, InterruptedException {
        if (projectId == null || projectId.isBlank()) {
            return UpdateStatus.none();
        }

        String projectUrl = "https://api.modrinth.com/v2/project/" + projectId.trim() + "/version";
        HttpRequest request = HttpRequest.newBuilder(URI.create(projectUrl))
                .timeout(Duration.ofSeconds(10))
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() != 200) {
            return UpdateStatus.none();
        }

        return fromVersionResponse(response.body(), projectId, currentVersion);
    }

    /**
     * Compatibility overload; updates are selected by publication recency, not channel.
     */
    @Deprecated
    public static UpdateStatus check(String projectId, String currentVersion, String preferredChannel) throws IOException, InterruptedException {
        return check(projectId, currentVersion);
    }

    static UpdateStatus fromVersionResponse(String payload, String projectId, String currentVersion) {
        if (payload == null || currentVersion == null || currentVersion.isBlank()) {
            return UpdateStatus.none();
        }

        Matcher matcher = VERSION_OBJECT_PATTERN.matcher(payload);
        if (!matcher.find()) {
            return UpdateStatus.none();
        }

        String latestVersion = matcher.group(1);
        String versionType = matcher.group(2).toLowerCase(Locale.ROOT);

        if (latestVersion == null || versionType == null) {
            return UpdateStatus.none();
        }

        if (normalizeVersion(latestVersion).equals(normalizeVersion(currentVersion))) {
            return UpdateStatus.none();
        }

        int behindBy = Math.max(1, calculateBehindBy(currentVersion, latestVersion));
        return new UpdateStatus(true, behindBy, currentVersion, "latest", "https://modrinth.com/project/" + projectId.trim(),
                latestVersion, versionType);
    }

    /**
     * Compares two version strings using a simple semver-like ordering. This keeps the logic short and reliable
     * enough for plugin updates such as 1.2.0, beta-1.2, and 1.2.0-beta.1.
     */
    private static List<Integer> extractNumericParts(String version) {
        if (version == null || version.isBlank()) {
            return List.of(0);
        }
        Matcher matcher = Pattern.compile("\\d+").matcher(normalizeVersion(version));
        List<Integer> numbers = new ArrayList<>();
        while (matcher.find()) {
            numbers.add(Integer.parseInt(matcher.group()));
        }
        return numbers.isEmpty() ? List.of(0) : numbers;
    }

    public static int compareVersions(String leftVersion, String rightVersion) {
        if (leftVersion == null || leftVersion.isBlank()) {
            return rightVersion == null || rightVersion.isBlank() ? 0 : -1;
        }
        if (rightVersion == null || rightVersion.isBlank()) {
            return 1;
        }

        VersionParts left = parseVersion(leftVersion);
        VersionParts right = parseVersion(rightVersion);

        int majorCompare = compareNumericLists(left.coreNumbers, right.coreNumbers);
        if (majorCompare != 0) {
            return majorCompare;
        }

        int leftRank = left.channelRank;
        int rightRank = right.channelRank;
        if (leftRank != rightRank) {
            return Integer.compare(leftRank, rightRank);
        }

        return Integer.compare(left.prereleaseNumber, right.prereleaseNumber);
    }

    private static VersionParts parseVersion(String version) {
        String normalized = normalizeVersion(version);
        Matcher matcher = Pattern.compile("(?:alpha|beta|rc)|\\d+").matcher(normalized);
        List<Integer> coreNumbers = new ArrayList<>();
        List<Integer> prereleaseNumbers = new ArrayList<>();
        int channelRank = 3;
        boolean inPrerelease = false;
        boolean hasSeenLabel = false;

        while (matcher.find()) {
            String token = matcher.group();
            if (token.equals("alpha")) {
                channelRank = 0;
                inPrerelease = true;
                hasSeenLabel = true;
                continue;
            }
            if (token.equals("beta")) {
                channelRank = 1;
                inPrerelease = true;
                hasSeenLabel = true;
                continue;
            }
            if (token.equals("rc")) {
                channelRank = 2;
                inPrerelease = true;
                hasSeenLabel = true;
                continue;
            }

            int number = Integer.parseInt(token);
            if (inPrerelease) {
                prereleaseNumbers.add(number);
            } else {
                coreNumbers.add(number);
            }
        }

        if (coreNumbers.isEmpty() && !prereleaseNumbers.isEmpty()) {
            coreNumbers = new ArrayList<>(prereleaseNumbers);
            prereleaseNumbers.clear();
        }

        if (coreNumbers.isEmpty()) {
            coreNumbers.add(0);
        }

        if (channelRank == 3 && !hasSeenLabel && !prereleaseNumbers.isEmpty()) {
            prereleaseNumbers.clear();
        }

        return new VersionParts(coreNumbers, channelRank, prereleaseNumbers.isEmpty() ? 0 : prereleaseNumbers.get(0));
    }

    private static int compareNumericLists(List<Integer> left, List<Integer> right) {
        int maxLength = Math.max(left.size(), right.size());
        for (int i = 0; i < maxLength; i++) {
            int leftPart = i < left.size() ? left.get(i) : 0;
            int rightPart = i < right.size() ? right.get(i) : 0;
            if (leftPart != rightPart) {
                return Integer.compare(leftPart, rightPart);
            }
        }
        return 0;
    }

    private static String normalizeVersion(String version) {
        return version.trim().replace('_', '.').replace(" ", "").toLowerCase(Locale.ROOT).replaceFirst("^v", "");
    }

    private static final class VersionParts {
        private final List<Integer> coreNumbers;
        private final int channelRank;
        private final int prereleaseNumber;

        private VersionParts(List<Integer> coreNumbers, int channelRank, int prereleaseNumber) {
            this.coreNumbers = coreNumbers;
            this.channelRank = channelRank;
            this.prereleaseNumber = prereleaseNumber;
        }
    }
}
