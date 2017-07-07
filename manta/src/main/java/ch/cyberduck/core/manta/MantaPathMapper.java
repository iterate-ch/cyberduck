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

import ch.cyberduck.core.AbstractPath.Type;
import ch.cyberduck.core.Path;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.util.EnumSet;

import com.joyent.manta.client.MantaObject;
import com.joyent.manta.exception.MantaException;

public class MantaPathMapper {

    public static final String HOME_PATH_PRIVATE = "stor";
    public static final String HOME_PATH_PUBLIC = "public";

    enum Volume {
        PUBLIC,
        PRIVATE;

        @Override
        public String toString() {
            if (this.equals(PUBLIC)) {
                return HOME_PATH_PUBLIC;
            }
            if (this.equals(PRIVATE)) {
                return HOME_PATH_PRIVATE;
            }
            // unreachable
            return null;
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

    protected String requestPath(final Path homeRelativeRemote) {
        return homeRelativeRemote.getAbsolute();
    }

    protected Path getNormalizedHomePath() {
        return normalizedHomePath;
    }

    protected boolean isUserWritable(final MantaObject mantaObject) {
        return isUserWritable(mantaObject.getPath());
    }

    protected boolean isUserWritable(final Path path) {
        return isUserWritable(path.getAbsolute());
    }

    private boolean isUserWritable(final String path) {
        return StringUtils.startsWithAny(
                path,
                getPublicRoot().getAbsolute(),
                getPrivateRoot().getAbsolute());
    }

    protected boolean isWorldReadable(final MantaObject mantaObject) {
        return isWorldReadable(mantaObject.getPath());
    }

    protected boolean isWorldReadable(final Path path) {
        return isWorldReadable(path.getAbsolute());
    }

    private boolean isWorldReadable(final String path) {
        return StringUtils.startsWithAny(path, getPublicRoot().getAbsolute());
    }

    protected Path getAccountRoot() {
        return accountRoot;
    }

    protected String getAccountOwner() {
        return accountOwner;
    }

    protected Path getPrivateRoot() {
        return Volume.PRIVATE.forAccount(accountRoot);
    }

    protected Path getPublicRoot() {
        return Volume.PUBLIC.forAccount(accountRoot);
    }
}
