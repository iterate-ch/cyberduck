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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;

import com.joyent.manta.client.MantaObject;

/**
 * Created by tomascelaya on 5/24/17.
 */
public class MantaPathMapper {

    private static final String HOME_PATH_PRIVATE = "/stor";
    private static final String HOME_PATH_PUBLIC = "/public";
    private static final String MANTA_HOME_SHORTCUT = "/~~";

    private final MantaSession session;
    private final String accountOwner;
    private final String normalizedHomePath;

    MantaPathMapper(MantaSession session) {
        this.session = session;

        Validate.notNull(session.getHost().getCredentials(), "Credentials missing");

        accountOwner = extractAccountOwner(session.getHost().getCredentials().getUsername());

        final String defpath = session.getHost().getDefaultPath();
        final String fallback = StringUtils.defaultIfBlank(defpath, MANTA_HOME_SHORTCUT);
        final String regex = "^/?(" + accountOwner + "|~~?)/?";

        final String subdirectoryPath = fallback.replaceAll(regex, "");
        String normalizedAccountHome = '/' + accountOwner;

        if (StringUtils.isNotEmpty(subdirectoryPath)) {
            normalizedAccountHome += '/' + subdirectoryPath;
        }

        this.normalizedHomePath = normalizedAccountHome;
    }

    String requestPath(final Path homeRelativeRemote) {
        return normalizedHomePath + homeRelativeRemote.getAbsolute();
    }

    String getNormalizedHomePath() {
        return normalizedHomePath;
    }

    private String trimAccountOwner(MantaObject mantaObject) {
        final String remotePath = mantaObject.getPath();
        Validate.isTrue(StringUtils.startsWith(remotePath, normalizedHomePath));
        return remotePath.replace(normalizedHomePath, "");
    }

    boolean isUserWritable(final MantaObject mantaObject) {
        return isUserWritable(trimAccountOwner(mantaObject));
    }

    boolean isUserWritable(final Path homeRelativePath) {
        return isUserWritable(homeRelativePath.getAbsolute());
    }

    private boolean isUserWritable(final String homeRelativePath) {
        return StringUtils.startsWithAny(homeRelativePath, HOME_PATH_PRIVATE, HOME_PATH_PUBLIC);
    }

    boolean isWorldReadable(final MantaObject mantaObject) {
        return isWorldReadable(trimAccountOwner(mantaObject));
    }

    boolean isWorldReadable(final Path homeRelativePath) {
        return isWorldReadable(homeRelativePath.getAbsolute());
    }

    private boolean isWorldReadable(final String path) {
        return StringUtils.startsWith(path, HOME_PATH_PUBLIC);
    }

    public String getAccountOwner() {
        return accountOwner;
    }

    private String extractAccountOwner(final String username) {
        if(StringUtils.contains(username, "/")) {
            return username.split("/")[0];
        }
        else {
            return username;
        }
    }
}
