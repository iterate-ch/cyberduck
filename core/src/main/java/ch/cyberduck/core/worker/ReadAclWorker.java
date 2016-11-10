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

import ch.cyberduck.core.Acl;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.features.AclPermission;

import java.text.MessageFormat;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class ReadAclWorker extends Worker<List<Acl.UserAndRole>> {

    private final List<Path> files;

    public ReadAclWorker(final List<Path> files) {
        this.files = files;
    }

    @Override
    public List<Acl.UserAndRole> run(final Session<?> session) throws BackgroundException {
        final AclPermission feature = session.getFeature(AclPermission.class);

        List<Acl.User> removedEntries = new ArrayList<>();
        Map<Path, Map<Acl.User, Acl.Role>> onlineAcl = new HashMap<>();
        Map<Acl.User, Map<Path, Acl.Role>> aclGraph = new HashMap<>();

        for (Path file : files) {
            if (this.isCanceled()) {
                throw new ConnectionCanceledException();
            }

            Acl acl = feature.getPermission(file);
            Map<Acl.User, Acl.Role> filteredAcl = new HashMap<>();
            for (Map.Entry<Acl.User, Set<Acl.Role>> entry : acl.entrySet()) {
                Supplier<Stream<Acl.Role>> roleSupplier = () -> entry.getValue().stream().distinct();
                Acl.Role role = roleSupplier.get().count() == 1 ? roleSupplier.get().findAny().get() : new Acl.Role(null);
                filteredAcl.put(entry.getKey(), role);
            }
            onlineAcl.put(file, filteredAcl);

            for (Map.Entry<Acl.User, Acl.Role> entry : filteredAcl.entrySet()) {
                if (aclGraph.containsKey(entry.getKey())) {
                    aclGraph.get(entry.getKey()).put(file, entry.getValue());
                } else {
                    Map<Path, Acl.Role> map = new HashMap<>();
                    aclGraph.put(entry.getKey(), map);
                    map.put(file, entry.getValue());
                }
            }
        }

        for (Map.Entry<Acl.User, Map<Path, Acl.Role>> entry : aclGraph.entrySet()) {
            if (entry.getValue().size() != files.size()) {
                removedEntries.add(entry.getKey());
            }
        }
        for (Acl.User user : removedEntries) {
            aclGraph.remove(user);
        }
        for (Map.Entry<Path, Map<Acl.User, Acl.Role>> entry : onlineAcl.entrySet()) {
            Acl acl = new Acl();
            removedEntries.clear();
            for (Map.Entry<Acl.User, Acl.Role> aclPair : entry.getValue().entrySet()) {
                if (!aclGraph.containsKey(aclPair.getKey())) {
                    removedEntries.add(aclPair.getKey());
                } else {
                    acl.addAll(aclPair.getKey(), aclPair.getValue());
                }
            }
            for (Acl.User removedEntry : removedEntries) {
                entry.getValue().remove(removedEntry);
            }
            entry.getKey().attributes().setAcl(acl);
        }

        List<Acl.UserAndRole> userAndRoles = new ArrayList<>();
        for (Map.Entry<Acl.User, Map<Path, Acl.Role>> entry : aclGraph.entrySet())
        {
            // single use of streams, reason: distinct is easier in Streams than it would be writing it manually
            Supplier<Stream<Acl.Role>> valueSupplier = () -> entry.getValue().entrySet().stream().map(y -> y.getValue()).distinct();
            // check count against 1, if it is use that value, otherwise use null
            Acl.Role value = valueSupplier.get().count() == 1 ? valueSupplier.get().findAny().get() : new Acl.Role(null);
            // store it
            userAndRoles.add(new Acl.UserAndRole(entry.getKey(), value));
        }
        return userAndRoles;
    }

    @Override
    public String getActivity() {
        return MessageFormat.format(LocaleFactory.localizedString("Getting permission of {0}", "Status"),
                this.toString(files));
    }

    @Override
    public List<Acl.UserAndRole> initialize() {
        return Collections.emptyList();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ReadAclWorker that = (ReadAclWorker) o;
        if (files != null ? !files.equals(that.files) : that.files != null) {
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
        final StringBuilder sb = new StringBuilder("ReadAclWorker{");
        sb.append("files=").append(files);
        sb.append('}');
        return sb.toString();
    }
}
