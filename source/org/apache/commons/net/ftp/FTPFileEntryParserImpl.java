package org.apache.commons.net.ftp;

/*
 * Copyright 2001-2004 The Apache Software Foundation
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import ch.cyberduck.core.Path;

/**
 * This abstract class implements both the older FTPFileListParser and
 * newer FTPFileEntryParser interfaces with default functionality.
 * All the classes in the parser subpackage inherit from this.
 * 
 * @author Steve Cohen <scohen@apache.org>
 */
public abstract class FTPFileEntryParserImpl implements FTPFileEntryParser {
    /**
     * The constructor for a FTPFileEntryParserImpl object.
     */
    public FTPFileEntryParserImpl() {
    }

    /**
     * Reads the next entry using the supplied BufferedReader object up to
     * whatever delemits one entry from the next.  This default implementation
     * simply calls BufferedReader.readLine().
     *
     * @param reader The BufferedReader object from which entries are to be
     *               read.
     * @return A string representing the next ftp entry or null if none found.
     * @throws java.io.IOException thrown on any IO Error reading from the reader.
     */
    public String readNextEntry(BufferedReader reader) throws IOException {
        return reader.readLine();
    }

    /**
     * This method is a hook for those implementors (such as
     * VMSVersioningFTPEntryParser, and possibly others) which need to
     * perform some action upon the FTPFileList after it has been created
     * from the server stream, but before any clients see the list.
     * <p/>
     * This default implementation is a no-op.
     *
     * @param original Original list after it has been created from the server stream
     * @return <code>original</code> unmodified.
     */
    public List preParse(Path parent, List original) {
        Iterator it = original.iterator();
        while (it.hasNext()) {
            String entry = (String)it.next();
            if (null == parseFTPEntry(parent, entry)) {
                it.remove();
            }
            else {
                break;
            }
        }
        return original;
    }
}

/* Emacs configuration
 * Local variables:        **
 * mode:             java  **
 * c-basic-offset:   4     **
 * indent-tabs-mode: nil   **
 * End:                    **
 */
