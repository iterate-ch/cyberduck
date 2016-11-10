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
     * Selected files.
     */
    private final List<Path> files;

    /**
     * Permissions to apply to files.
     */
    private final Acl acl;

    /**
     * Descend into directories
     */
    private final RecursiveCallback<Acl> callback;

    private final ProgressListener listener;

    public WriteAclWorker(final List<Path> files,
                          final Acl acl, final boolean recursive,
                          final ProgressListener listener) {
        this(files, acl, new BooleanRecursiveCallback<Acl>(recursive), listener);
    }

    public WriteAclWorker(final List<Path> files,
                          final Acl acl, final RecursiveCallback<Acl> callback,
                          final ProgressListener listener) {
        this.files = files;
        this.acl = acl;
        this.callback = callback;
        this.listener = listener;
    }

    @Override
    public Boolean run(final Session<?> session) throws BackgroundException {
        final AclPermission feature = session.getFeature(AclPermission.class);
        for(Path file : files) {
            if(this.isCanceled()) {
                throw new ConnectionCanceledException();
            }
            this.write(session, feature, file);
        }
        return true;
    }

    protected void write(final Session<?> session, final AclPermission feature, final Path file) throws BackgroundException {
        Map<Acl.User, Set<String>> merged = Stream.concat(
                file.attributes().getAcl().keySet().stream().map(x -> new AbstractMap.SimpleImmutableEntry<>(x, "OLD")),
                acl.keySet().stream().map(x -> new AbstractMap.SimpleImmutableEntry<>(x, "NEW"))
        ).collect(Collectors.groupingBy(x -> x.getKey(), Collectors.mapping(x -> x.getValue(), Collectors.toSet())));

        this.write(session, feature, file, merged);
    }

    protected void write(final Session<?> session, final AclPermission feature, final Path file, Map<Acl.User, Set<String>> configMap) throws BackgroundException {
        if (this.isCanceled()) {
            throw new ConnectionCanceledException();
        }

        Acl originalAcl = feature.getPermission(file);
        for (Map.Entry<Acl.User, Set<String>> entry : configMap.entrySet()) {
            Set<String> config = entry.getValue();
            Set<Acl.Role> value = acl.get(entry.getKey());

            if (!config.contains("NEW")) {
                originalAcl.remove(entry.getKey());
            }
            else if (value != null) {
                Set<Acl.Role> original = originalAcl.get(entry.getKey());
                originalAcl.put(entry.getKey(), acl.get(entry.getKey())); // discard any existing values
            }
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

    @Override
    public String getActivity() {
        return MessageFormat.format(LocaleFactory.localizedString("Changing permission of {0} to {1}", "Status"),
                this.toString(files), acl);
    }

    @Override
    public boolean equals(final Object o) {
        if(this == o) {
            return true;
        }
        if(o == null || getClass() != o.getClass()) {
            return false;
        }
        final WriteAclWorker that = (WriteAclWorker) o;
        if(files != null ? !files.equals(that.files) : that.files != null) {
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
        final StringBuilder sb = new StringBuilder("WriteAclWorker{");
        sb.append("files=").append(files);
        sb.append('}');
        return sb.toString();
    }
}
