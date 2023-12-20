/*
 * Copyright (c) 2023 iterate GmbH. All rights reserved.
 */

package ch.cyberduck.core.ctera;

import ch.cyberduck.core.Acl;

import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ch.cyberduck.core.ctera.CteraCustomACL.*;
import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
class CteraPermissionFeatureCustomPropsToAclTest {

    @Parameterized.Parameters(name = "{0} {1}")
    public static Collection<Object[]> data() {

        return Arrays.asList(
                new Object[][]{
                        {Collections.emptyMap(), Acl.EMPTY},
                        {
                                (Map<String, String>) Stream.of(
                                        new AbstractMap.SimpleEntry<>(readpermission.getName(), "false"),
                                        new AbstractMap.SimpleEntry<>(writepermission.getName(), "false"),
                                        new AbstractMap.SimpleEntry<>(executepermission.getName(), "false"),
                                        new AbstractMap.SimpleEntry<>(deletepermission.getName(), "false"),
                                        new AbstractMap.SimpleEntry<>(traversepermission.getName(), "true"),
                                        new AbstractMap.SimpleEntry<>(Createfilepermission.getName(), "false"),
                                        new AbstractMap.SimpleEntry<>(CreateDirectoriespermission.getName(), "false")
                                ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)),
                                new Acl(new Acl.CanonicalUser(), traversepermission)
                        },
                        {
                                (Map<String, String>) Stream.of(
                                        new AbstractMap.SimpleEntry<>(readpermission.getName(), "false"),
                                        new AbstractMap.SimpleEntry<>(writepermission.getName(), "true"),
                                        new AbstractMap.SimpleEntry<>(executepermission.getName(), "false"),
                                        new AbstractMap.SimpleEntry<>(deletepermission.getName(), "false"),
                                        new AbstractMap.SimpleEntry<>(traversepermission.getName(), "false"),
                                        new AbstractMap.SimpleEntry<>(Createfilepermission.getName(), "false"),
                                        new AbstractMap.SimpleEntry<>(CreateDirectoriespermission.getName(), "true")
                                ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)),
                                new Acl(new Acl.CanonicalUser(), writepermission, CreateDirectoriespermission)
                        }
                }
        );
    }

    @Parameter
    public Map<String, String> map;

    @Parameter(1)
    public Acl expected;


    public void testCustomPropsToAcl(final Map<String, String> map, final Acl expected) {
        assertEquals(expected, customPropsToAcl(map));

    }
}