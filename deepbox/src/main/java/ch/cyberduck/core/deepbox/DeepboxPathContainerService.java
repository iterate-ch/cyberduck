package ch.cyberduck.core.deepbox;

/*
 * Copyright (c) 2002-2024 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.DefaultPathContainerService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.preferences.HostPreferences;

import org.apache.commons.lang3.StringUtils;

public class DeepboxPathContainerService extends DefaultPathContainerService {

    private final DeepboxSession session;

    public DeepboxPathContainerService(final DeepboxSession session) {
        this.session = session;
    }

    /**
     * Key to use in preferences to save the pinned locale.
     */
    static String toPinnedLocalizationPropertyKey(final String name) {
        return String.format("deepbox.localization.%s", name);
    }

    public String getPinnedLocalization(final String name) {
        final String localized = new HostPreferences(session.getHost()).getProperty(toPinnedLocalizationPropertyKey(name));
        if(null == localized) {
            return name;
        }
        return DeepboxPathNormalizer.name(localized);
    }

    @Override
    public boolean isContainer(final Path file) {
        if(this.isCompany(file)) {
            return true;
        }
        if(this.isDeepbox(file)) {
            return true;
        }
        if(this.isBox(file)) {
            return true;
        }
        if(this.isFourthLevel(file)) {
            return true;
        }
        return false;
    }

    public boolean isCompany(final Path file) {
        if(file.isRoot()) {
            return false;
        }
        return file.isDirectory() && file.getParent().isRoot();
    }

    public boolean isDeepbox(final Path file) {
        if(file.isRoot()) {
            return false;
        }
        return file.isDirectory() && !file.getParent().isRoot() && file.getParent().getParent().isRoot();
    }

    public boolean isBox(final Path file) {
        if(file.isRoot()) {
            return false;
        }
        return file.isDirectory() && !file.getParent().isRoot() && !file.getParent().getParent().isRoot() &&
                file.getParent().getParent().getParent().isRoot();
    }

    public boolean isFourthLevel(final Path file) {
        if(file.isRoot()) {
            return false;
        }
        return file.isDirectory() && !file.getParent().isRoot() && !file.getParent().getParent().isRoot() &&
                !file.getParent().getParent().getParent().isRoot() && file.getParent().getParent().getParent().getParent().isRoot();
    }

    public boolean isTrash(final Path file) {
        return this.isFourthLevel(file)
                && StringUtils.equals(file.getName(), this.getPinnedLocalization(DeepboxListService.TRASH));
    }

    public boolean isInbox(final Path file) {
        return this.isFourthLevel(file)
                && StringUtils.equals(file.getName(), this.getPinnedLocalization(DeepboxListService.INBOX));
    }

    public boolean isDocuments(final Path file) {
        return this.isFourthLevel(file)
                && StringUtils.equals(file.getName(), this.getPinnedLocalization(DeepboxListService.DOCUMENTS));
    }

    public boolean isInDocuments(final Path file) {
        final Path documents = this.getFourthLevelPath(file);
        if(null == documents) {
            return false;
        }
        return StringUtils.equals(documents.getName(), this.getPinnedLocalization(DeepboxListService.DOCUMENTS));
    }

    public boolean isInTrash(final Path file) {
        final Path trash = this.getFourthLevelPath(file);
        if(null == trash) {
            return false;
        }
        return StringUtils.equals(trash.getName(), this.getPinnedLocalization(DeepboxListService.TRASH));
    }

    public boolean isInInbox(final Path file) {
        final Path inbox = this.getFourthLevelPath(file);
        if(null == inbox) {
            return false;
        }
        return StringUtils.equals(inbox.getName(), getPinnedLocalization(DeepboxListService.INBOX));
    }

    protected Path getFourthLevelPath(final Path file) {
        if(this.isCompany(file)) {
            return null;
        }
        if(this.isDeepbox(file)) {
            return null;
        }
        if(this.isBox(file)) {
            return null;
        }
        Path fourthLevel = file;
        while(!fourthLevel.isRoot() && !this.isFourthLevel(fourthLevel)) {
            fourthLevel = fourthLevel.getParent();
        }
        if(fourthLevel.isRoot()) {
            return null;
        }
        return fourthLevel;
    }

    protected Path getBoxPath(final Path file) {
        if(this.isDeepbox(file)) {
            return null;
        }
        Path box = file;
        while(!box.isRoot() && !this.isBox(box)) {
            box = box.getParent();
        }
        if(box.isRoot()) {
            return null;
        }
        return box;
    }

    protected Path getDeepboxPath(final Path file) {
        if(this.isCompany(file)) {
            return null;
        }
        Path deepbox = file;
        while(!deepbox.isRoot() && !this.isDeepbox(deepbox)) {
            deepbox = deepbox.getParent();
        }
        if(deepbox.isRoot()) {
            return null;
        }
        return deepbox;
    }

    protected Path getCompanyPath(final Path file) {
        Path company = file;
        while(!company.isRoot() && !this.isCompany(company)) {
            company = company.getParent();
        }
        if(company.isRoot()) {
            return null;
        }
        return company;
    }
}
