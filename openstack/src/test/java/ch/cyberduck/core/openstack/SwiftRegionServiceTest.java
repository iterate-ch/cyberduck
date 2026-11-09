package ch.cyberduck.core.openstack;

import ch.cyberduck.core.features.Location;

import org.junit.Test;

import ch.iterate.openstack.swift.model.Region;

import static org.junit.Assert.*;

public class SwiftRegionServiceTest extends AbstractSwiftTest {

    @Test
    public void testLookupDefault() throws Exception {
        final Region lookup = new SwiftRegionService(session).lookup(Location.unknown);
        assertTrue(lookup.isDefault());
        assertNotNull(lookup.getStorageUrl());
    }
}
