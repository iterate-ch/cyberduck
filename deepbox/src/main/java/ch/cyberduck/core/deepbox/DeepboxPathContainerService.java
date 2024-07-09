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
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathNormalizer;

public class DeepboxPathContainerService extends DefaultPathContainerService {

    @Override
    public boolean isContainer(final Path file) {
        if(file.isRoot()) {
            return false;
        }
        return file.isDirectory() && (file.getParent().isRoot() || file.getParent().getParent().isRoot() || file.getParent().getParent().isRoot() || file.getParent().getParent().getParent().isRoot());
    }

    public boolean isDeepbox(final Path file) {
        if(file.isRoot()) {
            return false;
        }
        return file.isDirectory() && file.getParent().isRoot();
    }

    public boolean isBox(final Path file) {
        if(file.isRoot()) {
            return false;
        }
        return file.isDirectory() && !file.getParent().isRoot() && file.getParent().getParent().isRoot();
    }

    public boolean isThirdLevel(final Path file) {
        if(file.isRoot()) {
            return false;
        }
        return file.isDirectory() && !file.getParent().isRoot() && !file.getParent().getParent().isRoot() && file.getParent().getParent().getParent().isRoot();
    }

    public boolean isTrash(final Path file) {
        return this.isThirdLevel(file) && file.getName().equals(PathNormalizer.name(LocaleFactory.localizedString("Trash", "Deepbox")));
    }

    public boolean isInbox(final Path file) {
        return this.isThirdLevel(file) && file.getName().equals(PathNormalizer.name(LocaleFactory.localizedString("Inbox", "Deepbox")));
    }

    public boolean isDocuments(final Path file) {
        return this.isThirdLevel(file) && file.getName().equals(PathNormalizer.name(LocaleFactory.localizedString("Documents", "Deepbox")));
    }

    public boolean isInDocuments(final Path file) {
        final Path documents = this.getThirdLevelPath(file);
        if(null == documents) {
            return false;
        }
        return documents.getName().equals(PathNormalizer.name(LocaleFactory.localizedString("Documents", "Deepbox")));
    }

    public boolean isInTrash(final Path file) {
        final Path trash = this.getThirdLevelPath(file);
        if(null == trash) {
            return false;
        }
        return trash.getName().equals(PathNormalizer.name(LocaleFactory.localizedString("Trash", "Deepbox")));
    }

    public boolean isInInbox(final Path file) {
        final Path inbox = this.getThirdLevelPath(file);
        if(null == inbox) {
            return false;
        }
        return inbox.getName().equals(PathNormalizer.name(LocaleFactory.localizedString("Inbox", "Deepbox")));
    }

    public Path getThirdLevelPath(final Path file) {
        if(file.isRoot()) {
            return null;
        }
        if(this.isDeepbox(file)) {
            return null;
        }
        if(this.isBox(file)) {
            return null;
        }
        Path thirdLevel = file;
        while(!this.isThirdLevel(thirdLevel)) {
            thirdLevel = thirdLevel.getParent();
        }
        return thirdLevel;
    }

    public Path getBoxPath(final Path file) {
        if(file.isRoot()) {
            return null;
        }
        if(this.isDeepbox(file)) {
            return null;
        }
        Path box = file;
        while(!this.isBox(box)) {
            box = box.getParent();
        }
        return box;
    }

    public Path getDeepboxPath(final Path file) {
        if(file.isRoot()) {
            return null;
        }
        Path deepbox = file;
        while(!this.isDeepbox(deepbox)) {
            deepbox = deepbox.getParent();
        }
        return deepbox;
    }
}
