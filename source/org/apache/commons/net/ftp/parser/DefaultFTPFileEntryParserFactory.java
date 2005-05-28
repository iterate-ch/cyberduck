package org.apache.commons.net.ftp.parser;

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


import java.io.IOException;

import com.enterprisedt.net.ftp.FTPException;
import org.apache.commons.net.ftp.FTPFileEntryParser;

/**
 * This is the default implementation of the
 * FTPFileEntryParserFactory interface.  This is the
 * implementation that will be used by
 * org.apache.commons.net.ftp.FTPClient.listFiles()
 * if no other implementation has been specified.
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
		if(null != key) {
			ukey = key.toUpperCase();
			if(ukey.indexOf("UNIX") >= 0) {
				return this.createUnixFTPEntryParser();
			}
			else if(ukey.indexOf("VMS") >= 0) {
				throw new FTPException("\""+key+"\" is not currently a supported system.");
				//return createVMSFTPEntryParser();
			}
			else if(ukey.indexOf("NETWARE") >= 0) {
				return this.createNetwareFTPEntryParser();
			}
			else if(ukey.indexOf("WINDOWS") >= 0) {
				return this.createNTFTPEntryParser();
			}
			else if(ukey.indexOf("OS/2") >= 0) {
				return this.createOS2FTPEntryParser();
			}
			else if(ukey.indexOf("OS/400") >= 0) {
				return this.createOS400FTPEntryParser();
			}
            else if(ukey.indexOf("MVS") >= 0) {
                return this.createMVSEntryParser();
            }
		}
		return new UnixFTPEntryParser();
	}

	private FTPFileEntryParser createUnixFTPEntryParser() {
		return new UnixFTPEntryParser();
	}

	private FTPFileEntryParser createNetwareFTPEntryParser() {
		return new NetwareFTPEntryParser();
	}

	private FTPFileEntryParser createVMSVersioningFTPEntryParser() {
		return new VMSVersioningFTPEntryParser();
	}

	private FTPFileEntryParser createNTFTPEntryParser() {
		return new CompositeFileEntryParser(new FTPFileEntryParser[]
		{
			new NTFTPEntryParser(),
			new UnixFTPEntryParser()
		});
	}

	private FTPFileEntryParser createOS2FTPEntryParser() {
		return new OS2FTPEntryParser();
	}

	private FTPFileEntryParser createOS400FTPEntryParser() {
		return new CompositeFileEntryParser(new FTPFileEntryParser[]
		{
			new OS400FTPEntryParser(),
			new UnixFTPEntryParser()
		});
	}

    private FTPFileEntryParser createMVSEntryParser() {
        return new MVSFTPEntryParser();
    }
}

