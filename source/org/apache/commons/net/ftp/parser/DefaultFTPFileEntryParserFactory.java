package org.apache.commons.net.ftp.parser;

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


import java.io.IOException;

import org.apache.commons.net.ftp.FTPFileEntryParser;

import com.enterprisedt.net.ftp.FTPException;

/**
 * This is the default implementation of the
 * FTPFileEntryParserFactory interface.  This is the
 * implementation that will be used by
 * org.apache.commons.net.ftp.FTPClient.listFiles()
 * if no other implementation has been specified.
 *
 * @see org.apache.commons.net.ftp.FTPClient#listFiles
 * @see org.apache.commons.net.ftp.FTPClient#setParserFactory
 */
public class DefaultFTPFileEntryParserFactory implements FTPFileEntryParserFactory {

    /**
     * This default implementation of the FTPFileEntryParserFactory
     *
     * @param key a string containing (case-insensitively) one of the
     *            following keywords:
     *            <ul>
     *            <li><code>unix</code></li>
     *            <li><code>windows</code></li>
     *            <li><code>os/2</code></li>
     *            <li><code>vms</code></li>
     *            </ul>
     * @return the FTPFileEntryParser corresponding to the supplied key.
     * @see FTPFileEntryParser
     */
    public FTPFileEntryParser createFileEntryParser(String key) throws IOException {
        String ukey = null;
        if (null != key) {
            ukey = key.toUpperCase();
            if (ukey.indexOf("UNIX") >= 0) {
                return createUnixFTPEntryParser();
            }
            else if (ukey.indexOf("VMS") >= 0) {
                throw new FTPException("\"" + key + "\" is not currently a supported system. Think about a good motivation for the author of this sofware to write an appropriate parser. See Help > Send Feedback menu.");
                //return createVMSFTPEntryParser();
            }
            else if (ukey.indexOf("NETWARE") >= 0) {
                throw new FTPException("\"" + key + "\" is not currently a supported system. Think about a good motivation for the author of this sofware to write an appropriate parser. See Help > Send Feedback menu.");
                //return createNetwareFTPEntryParser();
            }
            else if (ukey.indexOf("WINDOWS") >= 0) {
                return createNTFTPEntryParser();
            }
            else if (ukey.indexOf("OS/2") >= 0) {
                // @todo NOT TESTED
                return createOS2FTPEntryParser();
            }
            else if (ukey.indexOf("OS/400") >= 0) {
                // @todo NOT TESTED
                return createOS400FTPEntryParser();
            }
        }
        return new UnixFTPEntryParser();
    }

    public FTPFileEntryParser createUnixFTPEntryParser() {
        return new UnixFTPEntryParser();
    }

    public FTPFileEntryParser createVMSVersioningFTPEntryParser() {
        return new VMSVersioningFTPEntryParser();
    }

    public FTPFileEntryParser createNTFTPEntryParser() {
        return new CompositeFileEntryParser(new FTPFileEntryParser[]
        {
            new NTFTPEntryParser(),
            new UnixFTPEntryParser()
        });
    }

    public FTPFileEntryParser createOS2FTPEntryParser() {
        return new OS2FTPEntryParser();
    }

    public FTPFileEntryParser createOS400FTPEntryParser() {
        return new OS400FTPEntryParser();
    }
}

