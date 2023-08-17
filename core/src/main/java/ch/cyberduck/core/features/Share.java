package ch.cyberduck.core.features;

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

import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.PasswordCallback;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;

import java.util.Collections;
import java.util.Set;

@Optional
public interface Share<Download, Upload> {
    boolean isSupported(Path file, Type type);

    /**
     * Retrieve list of users from server
     *
     * @return List of possible users to select from
     */
    default Set<Sharee> getSharees(final Type type) throws BackgroundException {
        return Collections.singleton(Sharee.world);
    }

    DescriptiveUrl toDownloadUrl(Path file, Sharee sharee, Download options, PasswordCallback callback) throws BackgroundException;

    DescriptiveUrl toUploadUrl(Path file, Sharee sharee, Upload options, PasswordCallback callback) throws BackgroundException;

    enum Type {
        download,
        upload
    }

    class Sharee {
        private final String identifier;
        private final String description;

        public Sharee(final String identifier, final String description) {
            this.identifier = identifier;
            this.description = description;
        }

        public String getIdentifier() {
            return identifier;
        }

        public String getDescription() {
            return description;
        }

        @Override
        public String toString() {
            final StringBuilder sb = new StringBuilder("Sharee{");
            sb.append("identifier='").append(identifier).append('\'');
            sb.append(", description='").append(description).append('\'');
            sb.append('}');
            return sb.toString();
        }

        public static final Sharee world = new Sharee(null, LocaleFactory.localizedString("AllUsers", "S3"));
    }

    interface ShareeCallback {
        Sharee prompt(Type type, Set<Sharee> sharees) throws ConnectionCanceledException;

        ShareeCallback disabled = new ShareeCallback() {
            @Override
            public Sharee prompt(final Type type, final Set<Sharee> sharees) throws ConnectionCanceledException {
                throw new ConnectionCanceledException();
            }
        };
    }
}
