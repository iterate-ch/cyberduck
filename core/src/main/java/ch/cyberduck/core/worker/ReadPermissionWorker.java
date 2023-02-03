package ch.cyberduck.core.worker;


/*
 * Copyright (c) 2002-2010 David Kocher. All rights reserved.
 *
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
 * Bug fixes, suggestions and comments should be sent to:
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Permission;
import ch.cyberduck.core.PermissionOverwrite;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.features.UnixPermission;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class ReadPermissionWorker extends Worker<PermissionOverwrite> {
    private static final Logger log = LogManager.getLogger(ReadPermissionWorker.class);

    /**
     * Selected files.
     */
    private final List<Path> files;

    public ReadPermissionWorker(final List<Path> files) {
        this.files = files;
    }

    @Override
    public PermissionOverwrite run(final Session<?> session) throws BackgroundException {
        final UnixPermission feature = session.getFeature(UnixPermission.class);
        if(log.isDebugEnabled()) {
            log.debug(String.format("Run with feature %s", feature));
        }
        final List<Permission> permissions = new ArrayList<>();
        for(Path next : files) {
            if(this.isCanceled()) {
                throw new ConnectionCanceledException();
            }
            if(Permission.EMPTY == next.attributes().getPermission()) {
                next.attributes().setPermission(feature.getUnixPermission(next));
            }
            permissions.add(next.attributes().getPermission());
        }

        final PermissionOverwrite overwrite = new PermissionOverwrite();
        final Supplier<Stream<Permission>> supplier = permissions::stream;

        overwrite.user.read = resolveOverwrite(map(supplier, Permission::getUser, Permission.Action.read));
        overwrite.user.write = resolveOverwrite(map(supplier, Permission::getUser, Permission.Action.write));
        overwrite.user.execute = resolveOverwrite(map(supplier, Permission::getUser, Permission.Action.execute));

        overwrite.group.read = resolveOverwrite(map(supplier, Permission::getGroup, Permission.Action.read));
        overwrite.group.write = resolveOverwrite(map(supplier, Permission::getGroup, Permission.Action.write));
        overwrite.group.execute = resolveOverwrite(map(supplier, Permission::getGroup, Permission.Action.execute));

        overwrite.other.read = resolveOverwrite(map(supplier, Permission::getOther, Permission.Action.read));
        overwrite.other.write = resolveOverwrite(map(supplier, Permission::getOther, Permission.Action.write));
        overwrite.other.execute = resolveOverwrite(map(supplier, Permission::getOther, Permission.Action.execute));

        return overwrite;
    }

    private static Boolean resolveOverwrite(final Supplier<Stream<Boolean>> implies) {
        Supplier<Stream<Boolean>> supplier = () -> implies.get().distinct();
        return supplier.get().count() == 1 ? supplier.get().findAny().get() : null;
    }

    private static Supplier<Stream<Boolean>> map(final Supplier<Stream<Permission>> permissions, final Function<Permission, Permission.Action> selector, final Permission.Action action) {
        return () -> permissions.get().map(permission -> selector.apply(permission).implies(action));
    }

    @Override
    public String getActivity() {
        return MessageFormat.format(LocaleFactory.localizedString("Getting permission of {0}", "Status"),
                this.toString(files));
    }

    @Override
    public PermissionOverwrite initialize() {
        return new PermissionOverwrite();
    }

    @Override
    public boolean equals(final Object o) {
        if(this == o) {
            return true;
        }
        if(o == null || getClass() != o.getClass()) {
            return false;
        }
        final ReadPermissionWorker that = (ReadPermissionWorker) o;
        if(!Objects.equals(files, that.files)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        return files != null ? files.hashCode() : 0;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ReadPermissionWorker{");
        sb.append("files=").append(files);
        sb.append('}');
        return sb.toString();
    }
}
