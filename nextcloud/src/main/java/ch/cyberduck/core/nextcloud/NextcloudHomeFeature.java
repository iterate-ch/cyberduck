package ch.cyberduck.core.nextcloud;

/*
 * Copyright (c) 2002-2021 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.Host;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathNormalizer;
import ch.cyberduck.core.PathRelativizer;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Home;
import ch.cyberduck.core.preferences.HostPreferences;
import ch.cyberduck.core.shared.AbstractHomeFeature;
import ch.cyberduck.core.shared.DelegatingHomeFeature;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.EnumSet;

public class NextcloudHomeFeature extends AbstractHomeFeature {
    private static final Logger log = LogManager.getLogger(NextcloudHomeFeature.class);

    private final Host bookmark;

    public NextcloudHomeFeature(final Host bookmark) {
        this.bookmark = bookmark;
    }

    @Override
    public Path find() throws BackgroundException {
        return this.find(Context.files);
    }

    public <T> Path find(final Context context) throws BackgroundException {
        return context.workdir(bookmark).find();
    }

    public enum Context {
        ocs {
            @Override
            public Home home(final Host bookmark) {
                return () -> new Path(new HostPreferences(bookmark).getProperty("nextcloud.root.ocs"), EnumSet.of(Path.Type.directory));
            }
        },
        files {
            @Override
            public Home home(final Host bookmark) throws BackgroundException {
                return new DelegatingHomeFeature(new UserDavRoot(bookmark, this), new DefaultDavRoot(bookmark));
            }

            @Override
            public Home workdir(final Host bookmark) throws BackgroundException {
                final Path workdir = super.workdir(bookmark).find();
                return new DelegatingHomeFeature(new DefaultPathSuffix(bookmark, workdir), () -> workdir);
            }
        },
        versions,
        meta {
            @Override
            public Home home(final Host bookmark) throws BackgroundException {
                return () -> new Path(MessageFormat.format(
                        new HostPreferences(bookmark).getProperty("nextcloud.root.webdav.user"), this.name(), StringUtils.EMPTY), EnumSet.of(Path.Type.directory));
            }
        };

        public Home home(final Host bookmark) throws BackgroundException {
            return new UserDavRoot(bookmark, this);
        }

        public Home workdir(final Host bookmark) throws BackgroundException {
            final Path home = this.home(bookmark).find();
            return new DelegatingHomeFeature(new WebDavRootPrefix(bookmark, home), () -> home);
        }
    }

    private static final class UserDavRoot implements Home {
        private final Host bookmark;
        private final Context context;

        public UserDavRoot(final Host bookmark, final Context context) {
            this.bookmark = bookmark;
            this.context = context;
        }

        @Override
        public Path find() {
            final String username = bookmark.getCredentials().getUsername();
            if(StringUtils.isBlank(username)) {
                return null;
            }
            return new Path(MessageFormat.format(
                    new HostPreferences(bookmark).getProperty("nextcloud.root.webdav.user"), context.name(), username), EnumSet.of(Path.Type.directory));
        }
    }

    private static final class DefaultDavRoot implements Home {
        private final Host bookmark;

        public DefaultDavRoot(final Host bookmark) {
            this.bookmark = bookmark;
        }

        @Override
        public Path find() {
            return new Path(new HostPreferences(bookmark).getProperty("nextcloud.root.webdav.default"), EnumSet.of(Path.Type.directory));
        }
    }

    private static final class WebDavRootPrefix implements Home {
        private final Host bookmark;
        private final Path home;

        public WebDavRootPrefix(final Host bookmark, final Path home) {
            this.bookmark = bookmark;
            this.home = home;
        }

        @Override
        public Path find() {
            if(StringUtils.isNotBlank(bookmark.getDefaultPath())) {
                for(String s : Arrays.asList(home.getAbsolute(), new HostPreferences(bookmark).getProperty("nextcloud.root.webdav.default"),
                        MessageFormat.format(new HostPreferences(bookmark).getProperty("nextcloud.root.webdav.user"), Context.files.name(),
                                bookmark.getCredentials().getUsername()))) {
                    if(StringUtils.contains(bookmark.getDefaultPath(), PathNormalizer.normalize(s))) {
                        final String prefix = StringUtils.substringBefore(bookmark.getDefaultPath(), PathNormalizer.normalize(s));
                        if(StringUtils.isBlank((prefix))) {
                            return null;
                        }
                        return PathNormalizer.compose(new Path(prefix, EnumSet.of(Path.Type.directory)),
                                PathRelativizer.relativize(String.valueOf(Path.DELIMITER), home.getAbsolute()));
                    }
                }
            }
            return null;
        }
    }

    private static final class DefaultPathSuffix implements Home {
        private final Host bookmark;
        private final Path home;

        public DefaultPathSuffix(final Host bookmark, final Path home) {
            this.bookmark = bookmark;
            this.home = home;
        }

        @Override
        public Path find() {
            if(StringUtils.isNotBlank(bookmark.getDefaultPath())) {
                for(String s : Arrays.asList(home.getAbsolute(), new HostPreferences(bookmark).getProperty("nextcloud.root.webdav.default"),
                        MessageFormat.format(new HostPreferences(bookmark).getProperty("nextcloud.root.webdav.user"), Context.files.name(),
                                bookmark.getCredentials().getUsername()))) {
                    if(StringUtils.contains(bookmark.getDefaultPath(), s)) {
                        final String suffix = StringUtils.substringAfter(bookmark.getDefaultPath(), s);
                        if(StringUtils.isBlank((suffix))) {
                            return null;
                        }
                        return PathNormalizer.compose(home, PathRelativizer.relativize(String.valueOf(Path.DELIMITER), suffix));
                    }
                }
                return PathNormalizer.compose(home, PathRelativizer.relativize(String.valueOf(Path.DELIMITER),
                        PathNormalizer.normalize(bookmark.getDefaultPath())));
            }
            return null;
        }
    }
}
