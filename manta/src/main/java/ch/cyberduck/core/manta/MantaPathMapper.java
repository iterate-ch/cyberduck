package ch.cyberduck.core.manta;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.AbstractPath;
import ch.cyberduck.core.AbstractPath.Type;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.shared.DefaultHomeFinderService;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
import org.apache.log4j.Logger;

import java.util.EnumSet;

import com.joyent.manta.client.MantaObject;

public class MantaPathMapper {

    static final String HOME_PATH_PRIVATE = "stor";
    static final String HOME_PATH_PUBLIC = "public";

    enum Volume {
        PUBLIC,
        PRIVATE;

        @Override
        public String toString() {
            switch(this) {
                case PUBLIC:
                    return HOME_PATH_PUBLIC;
                case PRIVATE:
                    return HOME_PATH_PRIVATE;
                default:
                    throw new Error("Unreachable code");
            }
        }

        public Path forAccount(final MantaSession session) {
            return forAccount(session.pathMapper.accountRoot);
        }

        public Path forAccount(final Path accountRoot) {
            return new Path(accountRoot, this.toString(), EnumSet.of(Type.volume, Type.directory));
        }
    }

    private final String accountOwner;

    private final Path accountRoot;
    private final Path normalizedHomePath;

    MantaPathMapper(MantaSession session) {
        Validate.notNull(session.getHost().getCredentials(), "Credentials missing");
        Validate.notNull(session.getHost().getCredentials().getUsername(), "Username missing");
        final String username = session.getHost().getCredentials().getUsername();

        // try to create a root path. at worst the user will fail to list global buckets later
        Path accountRootPath;
        try {
            accountRootPath = new Path(
                    (StringUtils.contains(username, "/") ? username.split("/")[0] : username),
                    EnumSet.of(Type.placeholder)
            );
        }
        catch(NullPointerException npe) {
            accountRootPath = new Path("/", EnumSet.of(Type.placeholder));
        }

        accountRoot = accountRootPath;
        accountOwner = accountRoot.getName();
        normalizedHomePath = buildNormalizedHomePath(session.getHost().getDefaultPath());
    }

    private Path buildNormalizedHomePath(final String rawHomePath) {
        final String defaultPath = StringUtils.defaultIfBlank(rawHomePath, Path.HOME);
        final String accountRootRegex = "^/?(" + accountRoot.getAbsolute() + "|~~?)/?";
        final String subdirectoryRawPath = defaultPath.replaceFirst(accountRootRegex, "");
        if(StringUtils.isEmpty(subdirectoryRawPath)) {
            return accountRoot;
        }

        final String[] subdirectoryPathSegments = StringUtils.split(subdirectoryRawPath, Path.DELIMITER);
        Path homePath = accountRoot;

        for(final String pathSegment : subdirectoryPathSegments) {
            EnumSet<Type> types = EnumSet.of(Type.directory);
            if(homePath.getParent().equals(accountRoot)
                    && StringUtils.equalsAny(pathSegment, HOME_PATH_PRIVATE, HOME_PATH_PUBLIC)) {
                types.add(Type.volume);
            }

            homePath = new Path(homePath, pathSegment, types);
        }

        return homePath;
    }

    String requestPath(final Path homeRelativeRemote) {
        return homeRelativeRemote.getAbsolute();
    }

    Path getNormalizedHomePath() {
        return normalizedHomePath;
    }

    boolean isUserWritable(final MantaObject mantaObject) {
        return isUserWritable(mantaObject.getPath());
    }

    boolean isUserWritable(final Path path) {
        return isUserWritable(path.getAbsolute());
    }

    private boolean isUserWritable(final String path) {
        return StringUtils.startsWithAny(
                path,
                getPublicRoot().getAbsolute(),
                getPrivateRoot().getAbsolute());
    }

    boolean isWorldReadable(final MantaObject mantaObject) {
        return isWorldReadable(mantaObject.getPath());
    }

    boolean isWorldReadable(final Path path) {
        return isWorldReadable(path.getAbsolute());
    }

    private boolean isWorldReadable(final String path) {
        return StringUtils.startsWithAny(path, getPublicRoot().getAbsolute());
    }

    Path getAccountRoot() {
        return accountRoot;
    }

    String getAccountOwner() {
        return accountOwner;
    }

    Path getPrivateRoot() {
        return Volume.PRIVATE.forAccount(accountRoot);
    }

    Path getPublicRoot() {
        return Volume.PUBLIC.forAccount(accountRoot);
    }
}
