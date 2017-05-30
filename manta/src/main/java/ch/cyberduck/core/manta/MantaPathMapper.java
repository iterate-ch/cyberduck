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

public class MantaPathMapper {

    static final String MANTA_HOME_SHORTCUT = "~~";
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

    private final Path privateRoot;
    private final Path publicRoot;

    MantaPathMapper(MantaSession session) {
        Validate.notNull(session.getHost().getCredentials(), "Credentials missing");

        accountRoot = extractAccountRoot(session.getHost().getCredentials().getUsername());
        accountOwner = accountRoot.getName();

        normalizedHomePath = buildNormalizedHomePath(session.getHost().getDefaultPath());

        privateRoot = Volume.PRIVATE.forAccount(accountRoot);
        publicRoot = Volume.PUBLIC.forAccount(accountRoot);
    }

    private Path buildNormalizedHomePath(final String homePath) {
        final String defaultPath = StringUtils.defaultIfBlank(homePath, MANTA_HOME_SHORTCUT);
        final String accountRootRegex = "^/?(" + accountRoot.getAbsolute() + "|~~?)/?";
        final String subdirectoryPath = defaultPath.replaceFirst(accountRootRegex, "");
        if(StringUtils.isEmpty(subdirectoryPath)) {
            return accountRoot;
        }

        return new Path(accountRoot, subdirectoryPath, EnumSet.of(Type.volume, Type.directory));
    }

    String requestPath(final Path homeRelativeRemote) {
        return buildNormalizedHomePath(homeRelativeRemote.getAbsolute()).getAbsolute();
    }

    Path getNormalizedHomePath() {
        return normalizedHomePath;
    }

    private String trimAccountOwner(String remotePath) {
        Validate.isTrue(StringUtils.startsWith(remotePath, normalizedHomePath.getAbsolute()));
        return remotePath.replace(normalizedHomePath.getAbsolute(), "");
    }

    boolean isUserWritable(final MantaObject mantaObject) {
        return isUserWritable(mantaObject.getPath());
    }

    boolean isUserWritable(final Path path) {
        return isUserWritable(path.getAbsolute());
    }

    private boolean isUserWritable(final String path) {
        return StringUtils.startsWith(path, accountRoot.getAbsolute() + Path.DELIMITER + HOME_PATH_PUBLIC)
                || StringUtils.startsWith(path, accountRoot.getAbsolute() + Path.DELIMITER + HOME_PATH_PRIVATE);
    }

    boolean isWorldReadable(final MantaObject mantaObject) {
        return isWorldReadable(mantaObject.getPath());
    }

    boolean isWorldReadable(final Path path) {
        return isWorldReadable(path.getAbsolute());
    }

    private boolean isWorldReadable(final String path) {
        return StringUtils.startsWith(path, accountRoot.getAbsolute() + Path.DELIMITER + HOME_PATH_PUBLIC);
    }

    public Path getAccountRoot() {
        return accountRoot;
    }

    private Path extractAccountRoot(final String username) {
        return new Path(
                '/' +
                        (StringUtils.contains(username, "/")
                                ? username.split("/")[0]
                                : username),
                EnumSet.of(Type.placeholder)
        );
    }

    public String getAccountOwner() {
        return accountOwner;
    }

    public Path getPrivateRoot() {
        return privateRoot;
    }

    public Path getPublicRoot() {
        return publicRoot;
    }
}
