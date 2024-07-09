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
        return file.isDirectory() && (file.getParent().isRoot());
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
        return isThirdLevel(file) && file.getName().equals(PathNormalizer.name(LocaleFactory.localizedString("Trash", "Deepbox")));
    }

    public boolean isInbox(final Path file) {
        return isThirdLevel(file) && file.getName().equals(PathNormalizer.name(LocaleFactory.localizedString("Inbox", "Deepbox")));
    }

    public boolean isDocuments(final Path file) {
        return isThirdLevel(file) && file.getName().equals(PathNormalizer.name(LocaleFactory.localizedString("Documents", "Deepbox")));
    }

    public boolean isInDocuments(Path file) {
        file = getThirdLevelPath(file);
        if(file == null) {
            return false;
        }
        return file.getName().equals(PathNormalizer.name(LocaleFactory.localizedString("Documents", "Deepbox")));
    }

    public boolean isInTrash(Path file) {
        file = getThirdLevelPath(file);
        if(file == null) {
            return false;
        }
        return file.getName().equals(PathNormalizer.name(LocaleFactory.localizedString("Trash", "Deepbox")));
    }

    public boolean isInInbox(Path file) {
        file = getThirdLevelPath(file);
        if(file == null) {
            return false;
        }
        return file.getName().equals(PathNormalizer.name(LocaleFactory.localizedString("Inbox", "Deepbox")));
    }

    public Path getThirdLevelPath(Path file) {
        if(file.isRoot()) {
            return null;
        }
        if(isDeepbox(file)) {
            return null;
        }
        if(isBox(file)) {
            return null;
        }
        while(!isThirdLevel(file)) {
            file = file.getParent();
        }
        return file;
    }

    public Path getBoxPath(Path file) {
        if(file.isRoot()) {
            return null;
        }
        if(isDeepbox(file)) {
            return null;
        }
        while(!isBox(file)) {
            file = file.getParent();
        }
        return file;
    }

    public Path getDeepboxPath(Path file) {
        if(file.isRoot()) {
            return null;
        }
        while(!isDeepbox(file)) {
            file = file.getParent();
        }
        return file;
    }
}
