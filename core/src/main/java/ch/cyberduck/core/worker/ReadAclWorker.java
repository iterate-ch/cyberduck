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
import ch.cyberduck.core.features.AclPermission;
import ch.cyberduck.core.stream.ExtendedCollectors;

import java.text.MessageFormat;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ReadAclWorker extends Worker<AclOverwrite> {

    private final List<Path> files;

    public ReadAclWorker(final List<Path> files) {
        this.files = files;
    }

    @Override
    public AclOverwrite run(final Session<?> session) throws BackgroundException {
        final AclPermission feature = session.getFeature(AclPermission.class);

        Map<Path, List<Acl.UserAndRole>> onlineAcl = files.stream().collect(Collectors.toMap(x -> x, x -> {
            List<Acl.UserAndRole> acl = null;
            try {
                Acl tempAcl = feature.getPermission(x);
                x.attributes().setAcl(tempAcl);
                acl = tempAcl.asList();
            } catch (BackgroundException e) {
                acl = Collections.emptyList();
            }
            return acl;
        }));

        Supplier<Stream<Map.Entry<Path, Acl.UserAndRole>>> flatAcl = () -> onlineAcl.entrySet().stream().flatMap(
                x -> x.getValue().stream().map(
                        y -> new AbstractMap.SimpleImmutableEntry<>(x.getKey(), y)));

        Map<Acl.User, Map<Path, Acl.Role>> aclGraph = flatAcl.get().collect(
                Collectors.groupingBy(
                        x -> x.getValue().getUser(),
                        Collectors.toMap(x -> x.getKey(), x -> x.getValue().getRole()))
        ).entrySet().stream().filter(x -> x.getValue().size() == files.size()
        ).collect(Collectors.toMap(x -> x.getKey(), x -> x.getValue()));

        Map<Path, List<Acl.UserAndRole>> pathOriginalAcl = flatAcl.get().filter(
                x -> aclGraph.containsKey(x.getValue().getUser())
        ).collect(Collectors.groupingBy(
                x -> x.getKey(),
                Collectors.mapping(x -> x.getValue(), Collectors.toList())));

        Map<Acl.User, Acl.Role> acl = aclGraph.entrySet().stream().collect(ExtendedCollectors.toMap(
                x -> x.getKey(),
                x -> {
                    Supplier<Stream<Acl.Role>> roleSupplier = () -> x.getValue().entrySet().stream().map(y -> y.getValue()).distinct();
                    return roleSupplier.get().count() == 1 ? roleSupplier.get().findAny().get() : null;
                }));

        return new AclOverwrite(pathOriginalAcl, acl);
    }

    @Override
    public String getActivity() {
        return MessageFormat.format(LocaleFactory.localizedString("Getting permission of {0}", "Status"),
                this.toString(files));
    }

    @Override
    public AclOverwrite initialize() {
        return new AclOverwrite(Collections.emptyMap(), Collections.emptyMap());
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
