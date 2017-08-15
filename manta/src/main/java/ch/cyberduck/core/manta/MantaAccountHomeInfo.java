package ch.cyberduck.core.manta;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.AbstractPath;
import ch.cyberduck.core.Path;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import java.util.EnumSet;

import com.joyent.manta.util.MantaUtils;

import static ch.cyberduck.core.manta.MantaSession.HOME_PATH_PRIVATE;
import static ch.cyberduck.core.manta.MantaSession.HOME_PATH_PUBLIC;

/**
 * Created by tomascelaya on 8/15/17.
 */
public class MantaAccountHomeInfo {

    private final String accountOwner;

    private final Path accountRoot;

    private final Path normalizedHomePath;
    private final Path accountPublicRoot;
    private final Path accountPrivateRoot;

    public MantaAccountHomeInfo(final String username, final String defaultPath) {
        Validate.notNull(username, "Username must not be null");

        String[] accountPathParts = MantaUtils.parseAccount(username);

        accountRoot = new Path(accountPathParts[0], EnumSet.of(AbstractPath.Type.placeholder));
        accountOwner = accountRoot.getName();
        normalizedHomePath = buildNormalizedHomePath(defaultPath);

        accountPublicRoot = new Path(
                accountRoot,
                HOME_PATH_PUBLIC,
                EnumSet.of(AbstractPath.Type.volume, AbstractPath.Type.directory));
        accountPrivateRoot = new Path(
                accountRoot,
                HOME_PATH_PRIVATE,
                EnumSet.of(AbstractPath.Type.volume, AbstractPath.Type.directory));
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
            EnumSet<AbstractPath.Type> types = EnumSet.of(AbstractPath.Type.directory);
            if(homePath.getParent().equals(accountRoot)
                    && StringUtils.equalsAny(pathSegment, HOME_PATH_PRIVATE, HOME_PATH_PUBLIC)) {
                types.add(AbstractPath.Type.volume);
            }

            homePath = new Path(homePath, pathSegment, types);
        }

        return homePath;
    }

    public String getAccountOwner() {
        return accountOwner;
    }

    public Path getAccountRoot() {
        return accountRoot;
    }

    public Path getNormalizedHomePath() {
        return normalizedHomePath;
    }

    public Path getAccountPublicRoot() {
        return accountPublicRoot;
    }

    public Path getAccountPrivateRoot() {
        return accountPrivateRoot;
    }
}
