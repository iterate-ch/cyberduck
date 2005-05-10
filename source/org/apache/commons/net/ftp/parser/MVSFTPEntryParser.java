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

import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathFactory;

/**
 * Implementation of FTPFileEntryParser and FTPFileListParser for IBM MVS Systems.
 *
 * @author <a href="jnadler@srcginc.com">Jeff Nadler</a>
 * @author <a href="wnoto@openfinance.com">William Noto</a>
 * @version $Id$
 * @see org.apache.commons.net.ftp.FTPFileEntryParser FTPFileEntryParser (for usage instructions)
 */
public class MVSFTPEntryParser extends RegexFTPFileEntryParserImpl
{  
    /**
     * This is the regular expression used by this parser.
     */
	private static final String REGEX = "(.*)\\s+([^\\s]+)\\s*";
	
    /**
     * Although this parser is now ignoring dates, someone may someday
     * figure out a way to accomodate this and this appears to be the 
     * format used.  For now, it won't be used.
     * SMC 2005/04/08
     */
    static final String DEFAULT_DATE_FORMAT 
		= "yyyy/MM/dd"; // 2001/11/09

        
 	// This is not at all the tightest possible regexp for MVS LIST
	// output, but I'm not a mainframe guru so I have little idea what the
	// range of valid values are.  I just needed to get the filename (Dsname);
	// note that no other FTPFile fields can be filled in with the results of
	// a LIST on MVS.  The 'Referred' date seems to be 'last accessed date'
	// and not 'last modified date' so I didn't bother parsing it.
	//
	// Of course it works perfectly as-is and it distinguishes header lines from
	// file results so that's the important thing.  
	//
	// This parser should be used when SYST returns:
	// 'MVS is the operating system of this server. FTP Server is running on z/OS.'
	//
	// Also note that there is no concept of directories in MVS, just datasets,
	// which have names composed of four dot separated names of up to 8 chars.
	// As a result, FTPFile.FILE_TYPE is always used. -JN 6/2004 jnadler<at>srcginc<dotcom>

	// Sample LIST results from MVS:
	//
	//Volume Unit    Referred Ext Used Recfm Lrecl BlkSz Dsorg Dsname
	//FPFS42 3390   2004/06/23  1    1  FB     128  6144  PS  INCOMING.RPTBM023.D061704
	//FPFS41 3390   2004/06/23  1    1  FB     128  6144  PS  INCOMING.RPTBM056.D061704
	//FPFS25 3390   2004/06/23  1    1  FB     128  6144  PS  INCOMING.WTM204.D061704

    /**
     * The sole constructor for a MVSFTPEntryParser object.
     *
     * @exception IllegalArgumentException
     * Thrown if the regular expression is unparseable.  Should not be seen
     * under normal conditions.  It it is seen, this is a sign that
     * <code>REGEX</code> is  not a valid regular expression.
     */
    public MVSFTPEntryParser()
    {
        super(REGEX);
    }

    /**
     * Parses a line of an MVS FTP server file listing and converts it into a
     * usable format in the form of an <code> FTPFile </code> instance.  If the
     * file listing line doesn't describe a file, <code> null </code> is
     * returned, otherwise a <code> FTPFile </code> instance representing the
     * files in the directory is returned.
     * <p>
     * @param entry A line of text from the file listing
     * @return An FTPFile instance corresponding to the supplied entry
     */
    public Path parseFTPEntry(Path parent, String entry)
    {       
        if (matches(entry))
        {
            Path f = PathFactory.createPath(parent.getSession());
            String dataSetName = group(2);
            f.attributes.setType(Path.FILE_TYPE);
			f.setPath(parent.getAbsolute(), dataSetName);
			return f;
        }
        return null;
    }
}