/*
 * Copyright 2005 The Apache Software Foundation
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

import org.apache.commons.net.ftp.FTPFileEntryParser;
import org.apache.commons.net.ftp.FTPFileEntryParserImpl;

import ch.cyberduck.core.Path;

/**
 * This implementation allows to pack some FileEntryParsers together
 * and handle the case where to returned dirstyle isnt clearly defined.
 * The matching parser will be cached.
 * If the cached parser wont match due to the server changed the dirstyle,
 * a new matching parser will be searched.
 *
 * @author Mario Ivankovits <mario@ops.co.at>
 */
public class CompositeFileEntryParser extends FTPFileEntryParserImpl {
	private final FTPFileEntryParser[] ftpFileEntryParsers;
	private FTPFileEntryParser cachedFtpFileEntryParser;

	public CompositeFileEntryParser(FTPFileEntryParser[] ftpFileEntryParsers) {
		super();
		this.cachedFtpFileEntryParser = null;
		this.ftpFileEntryParsers = ftpFileEntryParsers;
	}

	public Path parseFTPEntry(Path parent, String listEntry) {
		if(cachedFtpFileEntryParser != null) {
			Path matched = cachedFtpFileEntryParser.parseFTPEntry(parent, listEntry);
			if(matched != null) {
				return matched;
			}
		}
		else {
			for(int iterParser = 0; iterParser < ftpFileEntryParsers.length; iterParser++) {
				FTPFileEntryParser ftpFileEntryParser = ftpFileEntryParsers[iterParser];

				Path matched = ftpFileEntryParser.parseFTPEntry(parent, listEntry);
				if(matched != null) {
					cachedFtpFileEntryParser = ftpFileEntryParser;
					return matched;
				}
			}
		}
		return null;
	}
}
