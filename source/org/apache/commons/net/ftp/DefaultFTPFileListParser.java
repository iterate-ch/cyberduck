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
package org.apache.commons.net.ftp;
import org.apache.commons.net.ftp.parser.UnixFTPEntryParser;

/**
 * DefaultFTPFileListParser is the default implementation of
 * <a href="org.apache.commons.net.ftp.FTPFileListParser.html"> FTPFileListParser </a>
 * used by <a href="org.apache.commons.net.ftp.FTPClient.html"> FTPClient </a>
 * to parse file listings.
 * Sometimes you will want to parse unusual listing formats, in which
 * case you would create your own implementation of FTPFileListParser and
 * if necessary, subclass FTPFile.
 * <p>
 * As of version 1.2, this class merely extends UnixFTPEntryParser.
 * It will be removed in version 2.0.
 * <p>
 * @author Daniel F. Savarese
 * @see FTPFileListParser
 * @see FTPFile
 * @see FTPClient#listFiles
 * @see org.apache.commons.net.ftp.parser.DefaultFTPFileEntryParserFactory
 * @deprecated This class is deprecated as of version 1.2 and will be 
 * removed in version 2.0 -- use the autodetect mechanism in 
 * DefaultFTPFileEntryParserFactory instead.
 */
public final class DefaultFTPFileListParser extends UnixFTPEntryParser
{



}
