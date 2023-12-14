/*
 * Copyright (c) 2023 iterate GmbH. All rights reserved.
 */

package ch.cyberduck.core.ctera;

import ch.cyberduck.core.Acl;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.AbstractMap;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ch.cyberduck.core.ctera.CteraCustomACL.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CteraCustomACLTest {

    private static Stream<Arguments> providerTestCustomPropsToAcl() {
        return Stream.of(
                Arguments.of(Collections.emptyMap(), Acl.EMPTY),
                Arguments.of(
                        Stream.of(
                                new AbstractMap.SimpleEntry<>(readpermission.getName(), "false"),
                                new AbstractMap.SimpleEntry<>(writepermission.getName(), "false"),
                                new AbstractMap.SimpleEntry<>(executepermission.getName(), "false"),
                                new AbstractMap.SimpleEntry<>(deletepermission.getName(), "false"),
                                new AbstractMap.SimpleEntry<>(traversepermission.getName(), "true"),
                                new AbstractMap.SimpleEntry<>(Createfilepermission.getName(), "false"),
                                new AbstractMap.SimpleEntry<>(CreateDirectoriespermission.getName(), "false")
                        ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)),
                        new Acl(new Acl.CanonicalUser(), traversepermission)
                ),
                Arguments.of(
                        Stream.of(
                                new AbstractMap.SimpleEntry<>(readpermission.getName(), "false"),
                                new AbstractMap.SimpleEntry<>(writepermission.getName(), "true"),
                                new AbstractMap.SimpleEntry<>(executepermission.getName(), "false"),
                                new AbstractMap.SimpleEntry<>(deletepermission.getName(), "false"),
                                new AbstractMap.SimpleEntry<>(traversepermission.getName(), "false"),
                                new AbstractMap.SimpleEntry<>(Createfilepermission.getName(), "false"),
                                new AbstractMap.SimpleEntry<>(CreateDirectoriespermission.getName(), "true")
                        ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)),
                        new Acl(new Acl.CanonicalUser(), writepermission, CreateDirectoriespermission)
                )
        );
    }


    @ParameterizedTest
    @MethodSource("providerTestCustomPropsToAcl")
    public void testCustomPropsToAcl(final Map<String, String> map, final Acl expected) {
        assertEquals(expected, customPropsToAcl(map));

    }


    private static Stream<Arguments> providerTestAclToPermission() {
        return Stream.of(
                Arguments.of(Acl.EMPTY, true, true, true),
                Arguments.of(new Acl(new Acl.CanonicalUser(), writepermission), false, true, false),
                Arguments.of(new Acl(new Acl.CanonicalUser(), readpermission), true, false, false),
                Arguments.of(new Acl(new Acl.CanonicalUser(), executepermission), false, false, true),
                Arguments.of(new Acl(new Acl.CanonicalUser(), deletepermission), false, false, false),
                Arguments.of(new Acl(new Acl.CanonicalUser(), traversepermission), false, false, true),
                Arguments.of(new Acl(new Acl.CanonicalUser(), Createfilepermission), false, false, false),
                Arguments.of(new Acl(new Acl.CanonicalUser(), CreateDirectoriespermission), false, false, false)
        );
    }

    @ParameterizedTest
    @MethodSource("providerTestAclToPermission")
    public void testAclToPermission(final Acl acl, final boolean r, final boolean w, final boolean x) {
        assertEquals(r, aclToPermission(acl).isReadable());
        assertEquals(w, aclToPermission(acl).isWritable());
        assertEquals(x, aclToPermission(acl).isExecutable());
    }

}