package org.apache.commons.net.ftp.parser;

/* ====================================================================
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2001-2004 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution,
 *    if any, must include the following acknowledgment:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowledgment may appear in the software itself,
 *    if and wherever such third-party acknowledgments normally appear.
 *
 * 4. The names "Apache" and "Apache Software Foundation" and
 *    "Apache Commons" must not be used to endorse or promote products
 *    derived from this software without prior written permission. For
 *    written permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache",
 *    nor may "Apache" appear in their name, without
 *    prior written permission of the Apache Software Foundation.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Calendar;
import java.util.StringTokenizer;

//import org.apache.commons.net.ftp.FTPFile;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathFactory;
import org.apache.commons.net.ftp.FTPFileEntryParserImpl;
import org.apache.commons.net.ftp.FTPListParseEngine;

/**
 * Implementation FTPFileEntryParser and FTPFileListParser for VMS Systems.
 * This is a sample of VMS LIST output
 *   
 *  "1-JUN.LIS;1              9/9           2-JUN-1998 07:32:04  [GROUP,OWNER]    (RWED,RWED,RWED,RE)",
 *  "1-JUN.LIS;2              9/9           2-JUN-1998 07:32:04  [GROUP,OWNER]    (RWED,RWED,RWED,RE)",
 *  "DATA.DIR;1               1/9           2-JUN-1998 07:32:04  [GROUP,OWNER]    (RWED,RWED,RWED,RE)",
 * <P><B>
 * Note: VMSFTPEntryParser can only be instantiated through the 
 * DefaultFTPParserFactory by classname.  It will not be chosen
 * by the autodetection scheme.
 * </B>
 * <P>
 * 
 * @author  <a href="Winston.Ojeda@qg.com">Winston Ojeda</a>
 * @author <a href="mailto:scohen@apache.org">Steve Cohen</a>
 * @author <a href="sestegra@free.fr">Stephane ESTE-GRACIAS</a>
 * @version $Id$
 * 
 * @see org.apache.commons.net.ftp.FTPFileEntryParser FTPFileEntryParser (for usage instructions)
 * @see org.apache.commons.net.ftp.parser.DefaultFTPFileEntryParserFactory
 */
public class VMSFTPEntryParser extends FTPFileEntryParserImpl
{


    /**
     * months abbreviations looked for by this parser.  Also used
     * to determine <b>which</b> month has been matched by the parser.
     */
    private static final String MONTHS =
        "(JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC)";

    /**
     * this is the regular expression used by this parser.
     */
    private static final String REGEX =
        "(.*;[0-9]+)\\s*" 
        + "(\\d+)/\\d+\\s*" 
        + "(\\d{1,2})-" 
        + MONTHS 
        + "-([0-9]{4})\\s*"
        + "((?:[01]\\d)|(?:2[0-3])):([012345]\\d):([012345]\\d)\\s*"
        + "\\[(([0-9$A-Za-z_]+)|([0-9$A-Za-z_]+),([0-9$a-zA-Z_]+))\\]\\s*" 
        + "\\([a-zA-Z]*,[a-zA-Z]*,[a-zA-Z]*,[a-zA-Z]*\\)";



    /**
     * Constructor for a VMSFTPEntryParser object.   
     * 
     * @exception IllegalArgumentException
     * Thrown if the regular expression is unparseable.  Should not be seen 
     * under normal conditions.  It it is seen, this is a sign that 
     * <code>REGEX</code> is  not a valid regular expression.
     */
    public VMSFTPEntryParser()
    {
        super(REGEX);
    }


    /***
     * Parses an FTP server file listing and converts it into a usable format
     * in the form of an array of <code> FTPFile </code> instances.  If the
     * file list contains no files, <code> null </code> should be
     * returned, otherwise an array of <code> FTPFile </code> instances
     * representing the files in the directory is returned.
     * <p>
     * @param listStream The InputStream from which the file list should be
     *        read.
     * @return The list of file information contained in the given path.  null
     *     if the list could not be obtained or if there are no files in
     *     the directory.
     * @exception IOException  If an I/O error occurs reading the listStream.
     ***/
//    public FTPFile[] parseFileList(InputStream listStream) throws IOException {
//    	FTPListParseEngine engine = new FTPListParseEngine(this);
//    	engine.readServerList(listStream);
//    	return engine.getFiles();
//    }



    /**
     * Parses a line of a VMS FTP server file listing and converts it into a
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
        //one block in VMS equals 512 bytes
        long longBlock = 512;

        if (matches(entry))
        {
			Path f = PathFactory.createPath(parent.getSession());
            String name = group(1);
            String size = group(2);
            String day = group(3);
            String mo = group(4);
            String yr = group(5);
            String hr = group(6);
            String min = group(7);
            String sec = group(8);
            String owner = group(9);
            String grp;
            String user;
            StringTokenizer t = new StringTokenizer(owner, ",");
            switch (t.countTokens()) {
                case 1:
                    grp  = null;
                    user = t.nextToken();
                    break;
                case 2:
                    grp  = t.nextToken();
                    user = t.nextToken();
                    break;
                default:
                    grp  = null;
                    user = null;
            }
            
            if (name.lastIndexOf(".DIR") != -1) 
            {
				f.attributes.setType(Path.DIRECTORY_TYPE);
            } 
            else 
            {
				f.attributes.setType(Path.FILE_TYPE);
            }
            //set FTPFile name
            //Check also for versions to be returned or not
            if (isVersioning()) 
            {
                f.setPath(parent.getAbsolute(), name);
            } 
            else 
            {
                name = name.substring(0, name.lastIndexOf(";"));
                f.setPath(parent.getAbsolute(), name);
            }
            //size is retreived in blocks and needs to be put in bytes
            //for us humans and added to the FTPFile array
            Long theSize = new Long(size);
            long sizeInBytes = theSize.longValue() * longBlock;
            f.status.setSize(sizeInBytes);

            //set the date
            Calendar cal = Calendar.getInstance();
            cal.clear();

            cal.set(Calendar.DATE, new Integer(day).intValue());
            cal.set(Calendar.MONTH, MONTHS.indexOf(mo) / 4);
            cal.set(Calendar.YEAR, new Integer(yr).intValue());
            cal.set(Calendar.HOUR_OF_DAY, new Integer(hr).intValue());
            cal.set(Calendar.MINUTE, new Integer(min).intValue());
            cal.set(Calendar.SECOND, new Integer(sec).intValue());
            f.attributes.setTimestamp(cal.getTime());

            f.attributes.setGroup(grp);
            f.attributes.setOwner(user);
            //set group and owner
            //Since I don't need the persmissions on this file (RWED), I'll 
            //leave that for further development. 'Cause it will be a bit 
            //elaborate to do it right with VMSes World, Global and so forth.
            return f;
        }
        return null;
    }


    /**
     * Reads the next entry using the supplied BufferedReader object up to
     * whatever delemits one entry from the next.   This parser cannot use
     * the default implementation of simply calling BufferedReader.readLine(),
     * because one entry may span multiple lines.
     *
     * @param reader The BufferedReader object from which entries are to be 
     * read.
     *
     * @return A string representing the next ftp entry or null if none found.
     * @exception IOException thrown on any IO Error reading from the reader.
     */
    public String readNextEntry(BufferedReader reader) throws IOException
    {
        String line = reader.readLine();
        StringBuffer entry = new StringBuffer();
        while (line != null)
        {
            if (line.startsWith("Directory") || line.startsWith("Total")) {
                line = reader.readLine();
                continue;
            }

            entry.append(line);
            if (line.trim().endsWith(")"))
            {
                break;
            }
            line = reader.readLine();
        }
        return (entry.length() == 0 ? null : entry.toString());
    }

    protected boolean isVersioning() {
        return false;
    }
}