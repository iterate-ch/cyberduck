package ch.cyberduck.core.worker;

/*
 * Copyright (c) 2002-2015 David Kocher. All rights reserved.
 * http://cyberduck.ch/
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
 *
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.MappingMimeTypeService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathAttributes;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.SimplePathPredicate;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.AclPermission;
import ch.cyberduck.core.features.AttributesFinder;
import ch.cyberduck.core.features.Encryption;
import ch.cyberduck.core.features.Redundancy;
import ch.cyberduck.core.features.Touch;
import ch.cyberduck.core.features.UnixPermission;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.transfer.TransferStatus;
import ch.cyberduck.ui.browser.SearchFilterFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.MessageFormat;
import java.util.EnumSet;

public class TouchWorker extends Worker<Path> {
    private static final Logger log = LogManager.getLogger(TouchWorker.class);

    private final Path file;

    public TouchWorker(final Path file) {
        this.file = file;
    }

    @Override
    public Path run(final Session<?> session) throws BackgroundException {
        final Touch feature = session.getFeature(Touch.class);
        log.debug("Run with feature {}", feature);
        final TransferStatus status = new TransferStatus()
                .withLength(0L)
                .withModified(System.currentTimeMillis())
                .hidden(!SearchFilterFactory.HIDDEN_FILTER.accept(file))
                .exists(false)
                .withLength(0L)
                .withMime(new MappingMimeTypeService().getMime(file.getName()))
                .withLockId(this.getLockId(file));
        final Encryption encryption = session.getFeature(Encryption.class);
        if(encryption != null) {
            status.setEncryption(encryption.getDefault(file));
        }
        final Redundancy redundancy = session.getFeature(Redundancy.class);
        if(redundancy != null) {
            status.setStorageClass(redundancy.getDefault());
        }
        status.setModified(System.currentTimeMillis());
        if(PreferencesFactory.get().getBoolean("touch.permissions.change")) {
            final UnixPermission permission = session.getFeature(UnixPermission.class);
            if(permission != null) {
                status.setPermission(permission.getDefault(EnumSet.of(Path.Type.file)));
            }
            final AclPermission acl = session.getFeature(AclPermission.class);
            if(acl != null) {
                status.setAcl(acl.getDefault(EnumSet.of(Path.Type.file)));
            }
        }
        final Path result = feature.touch(file, status);
        if(PathAttributes.EMPTY.equals(result.attributes())) {
            return result.withAttributes(session.getFeature(AttributesFinder.class).find(result));
        }
        return result;
    }

    protected String getLockId(final Path file) {
        return null;
    }

    @Override
    public Path initialize() {
        return file;
    }

    @Override
    public String getActivity() {
        return MessageFormat.format(LocaleFactory.localizedString("Uploading {0}", "Status"),
                file.getName());
    }

    @Override
    public boolean equals(final Object o) {
        if(this == o) {
            return true;
        }
        if(o == null || getClass() != o.getClass()) {
            return false;
        }
        final TouchWorker that = (TouchWorker) o;
        return new SimplePathPredicate(file).test(that.file);

    }

    @Override
    public int hashCode() {
        return file != null ? new SimplePathPredicate(file).hashCode() : 0;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("TouchWorker{");
        sb.append("file=").append(file);
        sb.append('}');
        return sb.toString();
    }
}
