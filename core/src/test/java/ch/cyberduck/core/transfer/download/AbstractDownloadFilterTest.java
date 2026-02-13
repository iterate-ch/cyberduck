package ch.cyberduck.core.transfer.download;

/*
 * Copyright (c) 2002-2021 iterate GmbH. All rights reserved.
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

import ch.cyberduck.core.transfer.download.features.SegmentedFeatureFilter;

import org.junit.Test;

import static ch.cyberduck.core.transfer.download.AbstractDownloadFilterTest.Unit.GiB;
import static ch.cyberduck.core.transfer.download.AbstractDownloadFilterTest.Unit.MiB;
import static org.junit.Assert.assertEquals;

public class AbstractDownloadFilterTest {

    enum Unit {KiB, MiB, GiB, TiB}

    static long convertSize(final long size, final Unit from) {
        switch(from) {
            case KiB:
                return size * 1024L;
            case MiB:
                return size * 1048576L;
            case GiB:
                return size * 1073741824L;
            case TiB:
                return size * 1099511627776L;
            default:
                return size;
        }
    }

    @Test
    public void testFindSegmentSize() {
        final SegmentSizePair[] tests = new SegmentSizePair[]{
            // split 20 MiB on one connection down to two 10 MiB segments
            new SegmentSizePair(
                    convertSize(20, MiB), 1,
                    convertSize(10, MiB), convertSize(128, MiB), 128)
                .withExpected(convertSize(10, MiB)),
            // split 16 GiB down to 128 segments of size 128 MiB
            new SegmentSizePair(
                    convertSize(16, GiB), 1,
                    convertSize(10, MiB), convertSize(128, MiB), 128)
                .withExpected(convertSize(128, MiB)),
            // halving allowed segments increases segment size for 16 GiB file
            new SegmentSizePair(
                    convertSize(16, GiB), 1,
                    convertSize(10, MiB), convertSize(128, MiB), 64)
                .withExpected(convertSize(256, MiB)),
            // doubling file size
            new SegmentSizePair(
                    convertSize(32, GiB), 1,
                    convertSize(10, MiB), convertSize(128, MiB), 128)
                .withExpected(convertSize(256, MiB)),
            new SegmentSizePair(
                    convertSize(20, MiB) + 1, 1,
                    convertSize(10, MiB), convertSize(128, MiB), 128)
                .withExpected(convertSize(5, MiB)),
            new SegmentSizePair(
                    convertSize(20, GiB), 1,
                    convertSize(10, MiB), convertSize(128, MiB), 128)
                .withExpected(convertSize(256, MiB)),
            new SegmentSizePair(
                    4893263872L, 2,
                    convertSize(10, MiB), convertSize(128, MiB), 128)
                .withExpected(38228624L)
        };

        for(final SegmentSizePair test : tests) {
            assertEquals(test.toString(), test.expected,
                SegmentedFeatureFilter.findSegmentSize(
                    test.length, test.connections, test.segmentThreshold,
                    test.segmentSizeMaximum, test.segmentCount));
        }
    }

    static class SegmentSizePair {
        public final long length;
        public final int connections;
        public final long segmentThreshold;
        public final long segmentSizeMaximum;
        public final long segmentCount;
        private long expected;

        long getExpected() {
            return expected;
        }

        SegmentSizePair(final long length, final int connections, final long segmentThreshold, final long segmentSizeMaximum, final long segmentCount) {
            this.length = length;
            this.connections = connections;
            this.segmentThreshold = segmentThreshold;
            this.segmentSizeMaximum = segmentSizeMaximum;
            this.segmentCount = segmentCount;
        }

        public SegmentSizePair withExpected(final long expected) {
            this.expected = expected;
            return this;
        }

        @Override
        public String toString() {
            return "{" +
                "length=" + length +
                ", connections=" + connections +
                ", segmentThreshold=" + segmentThreshold +
                ", segmentSizeMaximum=" + segmentSizeMaximum +
                ", segmentCount=" + segmentCount +
                '}';
        }
    }
}
