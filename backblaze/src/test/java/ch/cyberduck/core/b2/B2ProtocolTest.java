package ch.cyberduck.core.b2;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import ch.cyberduck.core.DefaultPathAttributes;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.io.Checksum;
import ch.cyberduck.core.io.HashAlgorithm;
import ch.cyberduck.core.synchronization.Comparison;
import ch.cyberduck.core.synchronization.ComparisonService;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class B2ProtocolTest {

    @Test
    public void testCompareChecksum() {
        final ComparisonService comparison = new B2Protocol().getFeature(ComparisonService.class);
        assertEquals(Comparison.equal, comparison.compare(Path.Type.file,
                new DefaultPathAttributes().setChecksum(new Checksum(HashAlgorithm.sha1, "da39a3ee5e6b4b0d3255bfef95601890afd80709")),
                new DefaultPathAttributes().setChecksum(new Checksum(HashAlgorithm.sha1, "da39a3ee5e6b4b0d3255bfef95601890afd80709"))));
        assertEquals(Comparison.notequal, comparison.compare(Path.Type.file,
                new DefaultPathAttributes().setChecksum(new Checksum(HashAlgorithm.sha1, "da39a3ee5e6b4b0d3255bfef95601890afd80709")),
                new DefaultPathAttributes().setChecksum(new Checksum(HashAlgorithm.sha1, "adc83b19e793491b1c6ea0fd8b46cd9f32e592fc"))));
    }

    @Test
    public void testCompareLargeFileNoChecksum() {
        final ComparisonService comparison = new B2Protocol().getFeature(ComparisonService.class);
        // Large file uploads have no SHA1 checksum (contentSha1: "none") on either side. Compare by version id
        assertEquals(Comparison.equal, comparison.compare(Path.Type.file,
                new DefaultPathAttributes().setVersionId("4_z571f17096c61cef88b180613_f200de1010975a471_d20260722_m231810_c001_v0001130_t0022_u01784762290204"),
                new DefaultPathAttributes().setVersionId("4_z571f17096c61cef88b180613_f200de1010975a471_d20260722_m231810_c001_v0001130_t0022_u01784762290204")));
        assertEquals(Comparison.notequal, comparison.compare(Path.Type.file,
                new DefaultPathAttributes().setVersionId("4_z571f17096c61cef88b180613_f200de1010975a471_d20260722_m231810_c001_v0001130_t0022_u01784762290204"),
                new DefaultPathAttributes().setVersionId("4_z571f17096c61cef88b180613_f21907ba376b3a4d1_d20260722_m231334_c001_v0001100_t0043_u01784762014000")));
    }

    @Test
    public void testCompareUnknown() {
        final ComparisonService comparison = new B2Protocol().getFeature(ComparisonService.class);
        assertEquals(Comparison.unknown, comparison.compare(Path.Type.file,
                new DefaultPathAttributes(), new DefaultPathAttributes()));
    }
}
