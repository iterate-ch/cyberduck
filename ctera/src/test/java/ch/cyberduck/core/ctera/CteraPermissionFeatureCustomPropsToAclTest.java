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
                                        new AbstractMap.SimpleEntry<>(CteraAttributesFinderFeature.READPERMISSION.getName(), "false"),
                                        new AbstractMap.SimpleEntry<>(CteraAttributesFinderFeature.WRITEPERMISSION.getName(), "false"),
                                        new AbstractMap.SimpleEntry<>(CteraAttributesFinderFeature.EXECUTEPERMISSION.getName(), "false"),
                                        new AbstractMap.SimpleEntry<>(CteraAttributesFinderFeature.DELETEPERMISSION.getName(), "true"),
                                        new AbstractMap.SimpleEntry<>(CteraAttributesFinderFeature.CREATEFILEPERMISSION.getName(), "false"),
                                        new AbstractMap.SimpleEntry<>(CteraAttributesFinderFeature.CREATEDIRECTORIESPERMISSION.getName(), "false")
                                ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)),
                                new Acl(new Acl.CanonicalUser(), CteraAttributesFinderFeature.DELETEPERMISSION)
                        },
                        {
                                (Map<String, String>) Stream.of(
                                        new AbstractMap.SimpleEntry<>(CteraAttributesFinderFeature.READPERMISSION.getName(), "false"),
                                        new AbstractMap.SimpleEntry<>(CteraAttributesFinderFeature.WRITEPERMISSION.getName(), "true"),
                                        new AbstractMap.SimpleEntry<>(CteraAttributesFinderFeature.EXECUTEPERMISSION.getName(), "false"),
                                        new AbstractMap.SimpleEntry<>(CteraAttributesFinderFeature.DELETEPERMISSION.getName(), "false"),
                                        new AbstractMap.SimpleEntry<>(CteraAttributesFinderFeature.CREATEFILEPERMISSION.getName(), "false"),
                                        new AbstractMap.SimpleEntry<>(CteraAttributesFinderFeature.CREATEDIRECTORIESPERMISSION.getName(), "true")
                                ).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)),
                                new Acl(new Acl.CanonicalUser(), CteraAttributesFinderFeature.WRITEPERMISSION, CteraAttributesFinderFeature.CREATEDIRECTORIESPERMISSION)
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
        assertEquals(expected, CteraAttributesFinderFeature.customPropsToAcl(map));
    }
}