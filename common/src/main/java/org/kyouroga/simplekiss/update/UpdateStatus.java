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

/**
 * Result of comparing the running version with the configured update source.
 */
public final class UpdateStatus {
    private final boolean updateAvailable;
    private final int behindBy;
    private final String currentTag;
    private final String targetBranch;
    private final String repoUrl;

    /**
     * Creates an update result.
     */
    public UpdateStatus(boolean updateAvailable, int behindBy, String currentTag, String targetBranch, String repoUrl) {
        this.updateAvailable = updateAvailable;
        this.behindBy = behindBy;
        this.currentTag = currentTag;
        this.targetBranch = targetBranch;
        this.repoUrl = repoUrl;
    }

    /**
     * Returns the result used when no update is available.
     */
    public static UpdateStatus none() {
        return new UpdateStatus(false, 0, "", "", "");
    }

    public boolean isUpdateAvailable() {
        return updateAvailable;
    }

    public int getBehindBy() {
        return behindBy;
    }

    public String getCurrentTag() {
        return currentTag;
    }

    public String getTargetBranch() {
        return targetBranch;
    }

    public String getRepoUrl() {
        return repoUrl;
    }
}
