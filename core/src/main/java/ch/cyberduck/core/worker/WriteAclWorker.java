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

import ch.cyberduck.core.*;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.features.AclPermission;

import java.text.MessageFormat;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class WriteAclWorker extends Worker<Boolean> {
    /**
     * Permissions to apply to files.
     */
    private final AclOverwrite acl;

    /**
     * Descend into directories
     */
    private final RecursiveCallback<Acl> callback;

    private final ProgressListener listener;

    public WriteAclWorker(final AclOverwrite acl, final boolean recursive,
                          final ProgressListener listener) {
        this(acl, new BooleanRecursiveCallback<Acl>(recursive), listener);
    }

    public WriteAclWorker(final AclOverwrite acl, final RecursiveCallback<Acl> callback,
                          final ProgressListener listener) {
        this.acl = acl;
        this.callback = callback;
        this.listener = listener;
    }

    @Override
    public Boolean run(final Session<?> session) throws BackgroundException {
        final AclPermission feature = session.getFeature(AclPermission.class);
        for (Map.Entry<Path, List<Acl.UserAndRole>> file : acl.originalAcl.entrySet()) {
            this.write(session, feature, file);
        }
        return true;
    }

    protected void write(final Session<?> session, final AclPermission feature, final Map.Entry<Path, List<Acl.UserAndRole>> aclEntry) throws BackgroundException {
        Map<Acl.User, Set<String>> configMap = Stream.concat(
                aclEntry.getValue().stream().map(x -> new AbstractMap.SimpleImmutableEntry<>(x.getUser(), "OLD")),
                acl.acl.entrySet().stream().map(x -> new AbstractMap.SimpleImmutableEntry<>(x.getKey(), "NEW"))
        ).collect(Collectors.groupingBy(x -> x.getKey(), Collectors.mapping(x -> x.getValue(), Collectors.toSet())));

        this.write(session, feature, aclEntry.getKey(), configMap);
    }

    protected void write(final Session<?> session, final AclPermission feature, final Path file, Map<Acl.User, Set<String>> configMap) throws BackgroundException {
        if (this.isCanceled()) {
            throw new ConnectionCanceledException();
        }

        Acl originalAcl = file.attributes().getAcl();
        for (Map.Entry<Acl.User, Set<String>> entry : configMap.entrySet()) {
            Set<String> config = entry.getValue();
            Acl.Role value = acl.acl.get(entry.getKey());

            if (!config.contains("NEW"))
                originalAcl.remove(entry.getKey());
            else if (value != null)
                originalAcl.put(entry.getKey(), new HashSet<>(Collections.singletonList(value))); // discard any existing values
        }

        listener.message(MessageFormat.format(LocaleFactory.localizedString("Changing permission of {0} to {1}", "Status"),
                file.getName(), originalAcl));
        feature.setPermission(file, originalAcl);

        if (file.isDirectory() && !file.isVolume()) {
            if (callback.recurse(file, originalAcl)) {
                for (Path child : session.list(file, new ActionListProgressListener(this, listener))) {
                    this.write(session, feature, child, configMap);
                }
            }
        }
    }

    @Override
    public Boolean initialize() {
        return false;
    }

    protected String toString(final Set<Path> files) {
        if (files.isEmpty()) {
            return LocaleFactory.localizedString("None");
        }
        final String name = files.stream().findAny().get().getName();
        if (files.size() > 1) {
            return String.format("%s… (%s) (%d)", name, LocaleFactory.localizedString("Multiple files"), files.size());
        }
        return String.format("%s…", name);
    }

    @Override
    public String getActivity() {
        return MessageFormat.format(LocaleFactory.localizedString("Changing permission of {0} to {1}", "Status"),
                this.toString(acl.originalAcl.keySet()), acl);
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Set<Path> files = acl.originalAcl.keySet();
        final WriteAclWorker that = (WriteAclWorker) o;
        Set<Path> thatFiles = that.acl.originalAcl.keySet();
        return files != null ? files.equals(thatFiles) : thatFiles == null;
    }

    @Override
    public int hashCode() {
        Set<Path> files = acl.originalAcl.keySet();
        return files != null ? files.hashCode() : 0;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("WriteAclWorker{");
        sb.append("files=").append(acl.originalAcl.keySet());
        sb.append('}');
        return sb.toString();
    }
}
