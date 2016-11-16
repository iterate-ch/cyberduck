package ch.cyberduck.core.worker;

/*
 * Copyright (c) 2002-2016 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import ch.cyberduck.core.DisabledProgressListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.NullSession;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.Session;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Headers;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class ReadWriteMetadataWorkerTest {
    @Test
    public void test() throws Exception {
        Map<String, String> metadataAServer = new HashMap<>();
        {
            // Initial Values for A:
            // equal: value
            // diff: diff1
            // removed: removed
            // unique: unique
            metadataAServer.put("equal", "value");
            metadataAServer.put("diff", "diff1");
            metadataAServer.put("removed", "removed");
            metadataAServer.put("unique", "unique");
        }
        Map<String, String> metadataBServer = new HashMap<>();
        {
            // Initial Values for B:
            // equal: value
            // diff: diff2
            // removed: removed
            metadataBServer.put("equal", "value");
            metadataBServer.put("diff", "diff2");
            metadataBServer.put("removed", "removed");
        }
        Map<String, String> metadataCServer = new HashMap<>();
        {
            // Initial Values for C:
            // equal: value
            // diff: diff2
            // removed: removed
            metadataCServer.put("equal", "value");
            metadataCServer.put("diff", "diff3");
            metadataCServer.put("removed", "removed");
            metadataCServer.put("uniqueremoved", "uniqueremoved");
        }
        Map<String, String> expectedReadMetadata = new HashMap<>();
        {
            // expected values:
            // equal: value
            // removed: removed
            // diff: null
            // unique: null
            expectedReadMetadata.put("equal", "value");
            expectedReadMetadata.put("diff", null);
            expectedReadMetadata.put("removed", "removed");
            expectedReadMetadata.put("unique", null);
            expectedReadMetadata.put("uniqueremoved", null);
        }
        Map<String, String> expectedMetadataA = new HashMap<>();
        {
            // result for written metadata on file A is
            // equal: newvalue
            // diff: diff1
            // unique: unique
            // removed should have been removed by now
            expectedMetadataA.put("equal", "newvalue");
            expectedMetadataA.put("diff", "diff1");
            expectedMetadataA.put("unique", "unique");
        }
        Map<String, String> expectedMetadataB = new HashMap<>();
        {
            // result for written metadata on file B is
            // equal: newvalue
            // diff: diff2
            expectedMetadataB.put("equal", "newvalue");
            expectedMetadataB.put("diff", "diff2");
        }
        Map<String, String> expectedMetadataC = new HashMap<>();
        {
            // result for written metadata on file C is
            // equal: newvalue
            // diff: diff3
            expectedMetadataC.put("equal", "newvalue");
            expectedMetadataC.put("diff", "diff3");
        }
        // Setup session used for reader and writer.
        Session testSession = new NullSession(new Host(new TestProtocol())) {
            @Override
            @SuppressWarnings("unchecked")
            public <T> T getFeature(final Class<T> type) {
                if(type == Headers.class) {
                    return (T) new Headers() {

                        @Override
                        public Map<String, String> getDefault(Local local) {
                            return Collections.emptyMap();
                        }

                        @Override
                        public Map<String, String> getMetadata(Path file) throws BackgroundException {
                            // Ensure server answers with correct values
                            switch(file.getName()) {
                                case "a":
                                    return metadataAServer;
                                case "b":
                                    return metadataBServer;
                                case "c":
                                    return metadataCServer;
                                default:
                                    fail();
                                    return null;
                            }
                        }

                        @Override
                        public void setMetadata(final Path file, final Map<String, String> metadata) throws BackgroundException {
                            // Test metadata against expected.
                            switch(file.getName()) {
                                case "a":
                                    assertEquals(expectedMetadataA, metadata);
                                    break;
                                case "b":
                                    assertEquals(expectedMetadataB, metadata);
                                    break;
                                case "c":
                                    assertEquals(expectedMetadataC, metadata);
                                    break;
                                default:
                                    fail();
                                    break;
                            }
                        }
                    };
                }
                return super.getFeature(type);
            }
        };

        final Path fileA = new Path("a", EnumSet.of(Path.Type.file));
        final Path fileB = new Path("b", EnumSet.of(Path.Type.file));
        final Path fileC = new Path("c", EnumSet.of(Path.Type.file));
        final List<Path> files = Arrays.asList(
                fileA,
                fileB,
                fileC
        );

        // setup reader
        ReadMetadataWorker readWorker = new ReadMetadataWorker(files);
        // execute read test.
        Map<String, String> actualReadMetadata = readWorker.run(testSession);

        // check if actual is expected
        assertEquals(expectedReadMetadata, actualReadMetadata);
        assertEquals(metadataAServer, fileA.attributes().getMetadata());
        assertEquals(metadataBServer, fileB.attributes().getMetadata());
        assertEquals(metadataCServer, fileC.attributes().getMetadata());


        Map<String, String> updatedMetadata = new HashMap<>(actualReadMetadata);
        {
            // put new value for key equal
            updatedMetadata.put("equal", "newvalue");
            // remove key "removed" (for writing)
            updatedMetadata.remove("removed");
            updatedMetadata.remove("uniqueremoved");
        }
        // -> this checks multiple scenarios at once:
        // - Removing an entry
        // - updating a single property keeping everything else untouched
        // - different values on multiple files
        // - unique values
        // - equal values

        // setup write worker
        WriteMetadataWorker writeWorker = new WriteMetadataWorker(files, updatedMetadata, false, new DisabledProgressListener());

        // execute write test
        writeWorker.run(testSession);
    }
}
