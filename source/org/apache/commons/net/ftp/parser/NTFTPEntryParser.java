/*
 * Copyright 2001-2005 The Apache Software Foundation
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
package org.apache.commons.net.ftp.parser;

import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathFactory;

import java.text.ParseException;

/**
 * Implementation of FTPFileEntryParser and FTPFileListParser for NT Systems.
 *
 * @author <a href="Winston.Ojeda@qg.com">Winston Ojeda</a>
 * @author <a href="mailto:scohen@apache.org">Steve Cohen</a>
 * @version $Id$
 * @see org.apache.commons.net.ftp.FTPFileEntryParser FTPFileEntryParser (for usage instructions)
 */
public class NTFTPEntryParser extends RegexFTPFileEntryParserImpl {

    /**
     * this is the regular expression used by this parser.
     */
    private static final String REGEX =
            "(\\S+)\\s+(\\S+)\\s+"
                    + "(?:(<DIR>)|([0-9]+))\\s+"
                    + "(\\S.*)";

    /**
     * The sole constructor for an NTFTPEntryParser object.
     *
     * @throws IllegalArgumentException Thrown if the regular expression is unparseable.  Should not be seen
     *                                  under normal conditions.  It it is seen, this is a sign that
     *                                  <code>REGEX</code> is  not a valid regular expression.
     */
    public NTFTPEntryParser() {
        super(REGEX);
    }

    /**
     * Parses a line of an NT FTP server file listing and converts it into a
     * usable format in the form of an <code> FTPFile </code> instance.  If the
     * file listing line doesn't describe a file, <code> null </code> is
     * returned, otherwise a <code> FTPFile </code> instance representing the
     * files in the directory is returned.
     * <p/>
     *
     * @param entry A line of text from the file listing
     * @return An FTPFile instance corresponding to the supplied entry
     */
    public Path parseFTPEntry(Path parent, String entry) {

        Path f = PathFactory.createPath(parent.getSession());

        if (matches(entry)) {
            String datestr = group(1) + " " + group(2);
            String dirString = group(3);
            String size = group(4);
            String name = group(5);
            try {
                f.attributes.setTimestamp(super.parseTimestamp(datestr).getTime());
            }
            catch (ParseException e) {
                ;  // this is a parsing failure too.
            }

            if (null == name || name.equals(".") || name.equals("..")) {
                return null;
            }
            f.setPath(parent.getAbsolute(), name);
            if ("<DIR>".equals(dirString)) {
                f.attributes.setType(Path.DIRECTORY_TYPE);
                f.attributes.setSize(0);
            }
            else {
                f.attributes.setType(Path.FILE_TYPE);
                if (null != size) {
                    f.attributes.setSize(Long.parseLong(size));
                }
            }
            return f;
        }
        return null;
    }
}
