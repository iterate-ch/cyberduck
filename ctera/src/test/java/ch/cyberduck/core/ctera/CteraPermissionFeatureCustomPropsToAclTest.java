/*
 * Copyright (c) 2023 iterate GmbH. All rights reserved.
 */

package ch.cyberduck.core.ctera;

import ch.cyberduck.core.Acl;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameter;
import org.junit.runners.Parameterized.Parameters;

import java.util.AbstractMap;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static ch.cyberduck.core.ctera.CteraAclPermissionFeature.*;
import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class CteraPermissionFeatureCustomPropsToAclTest {

    @Parameters(name = "{0} {1}")
    public static Collection<Object[]> data() {
        return Arrays.asList(
                new Object[][]{
                        {Collections.emptyMap(), Acl.EMPTY},
                        {
                                (Map<String, String>) Stream.of(
                                        new AbstractMap.SimpleEntry<>(READPERMISSION.getName(), "false"),
                                        new AbstractMap.SimpleEntry<>(WRITEPERMISSION.getName(), "false"),
                                        new AbstractMap.SimpleEntry<>(EXECUTEPERMISSION.getName(), "false"),
                                        new AbstractMap.SimpleEntry<>(DELETEPERMISSION.getName(), "false"),
                                        new AbstractMap.SimpleEntry<>(TRAVERSEPERMISSION.getName(), "true"),
                                        new AbstractMap.SimpleEntry<>(CREATEFILEPERMISSION.getName(), "false"),
                                        new AbstractMap.SimpleEntry<>(CREATEDIRECTORIESPERMISSION.getName(), "false")
                                ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)),
                                new Acl(new Acl.CanonicalUser(), TRAVERSEPERMISSION)
                        },
                        {
                                (Map<String, String>) Stream.of(
                                        new AbstractMap.SimpleEntry<>(READPERMISSION.getName(), "false"),
                                        new AbstractMap.SimpleEntry<>(WRITEPERMISSION.getName(), "true"),
                                        new AbstractMap.SimpleEntry<>(EXECUTEPERMISSION.getName(), "false"),
                                        new AbstractMap.SimpleEntry<>(DELETEPERMISSION.getName(), "false"),
                                        new AbstractMap.SimpleEntry<>(TRAVERSEPERMISSION.getName(), "false"),
                                        new AbstractMap.SimpleEntry<>(CREATEFILEPERMISSION.getName(), "false"),
                                        new AbstractMap.SimpleEntry<>(CREATEDIRECTORIESPERMISSION.getName(), "true")
                                ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)),
                                new Acl(new Acl.CanonicalUser(), WRITEPERMISSION, CREATEDIRECTORIESPERMISSION)
                        }
                }
        );
    }

    @Parameter
    public Map<String, String> map;

    @Parameter(1)
    public Acl expected;


    @Test
    public void testCustomPropsToAcl() {
        assertEquals(expected, customPropsToAcl(map));
    }
}