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
import java.util.Calendar;

import ch.cyberduck.core.Path;
import ch.cyberduck.core.PathFactory;
import org.apache.commons.net.ftp.FTPFileEntryParserImpl;

/**
 * Parser for the Connect Enterprise Unix FTP Server From Sterling Commerce.  
 * Here is a sample of the sort of output line this parser processes:
 *  "-C--E-----FTP B QUA1I1      18128       41 Aug 12 13:56 QUADTEST"
 * <P><B>
 * Note: EnterpriseUnixFTPEntryParser can only be instantiated through the 
 * DefaultFTPParserFactory by classname.  It will not be chosen
 * by the autodetection scheme.
 * </B>
 * @version $Id$
 * @author <a href="Winston.Ojeda@qg.com">Winston Ojeda</a>
 * @see org.apache.commons.net.ftp.FTPFileEntryParser FTPFileEntryParser (for usage instructions)
 * @see org.apache.commons.net.ftp.parser.DefaultFTPFileEntryParserFactory
 */
public class EnterpriseUnixFTPEntryParser extends FTPFileEntryParserImpl
{

    /**
     * months abbreviations looked for by this parser.  Also used
     * to determine <b>which</b> month has been matched by the parser.
     */
    private static final String MONTHS = 
        "(Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)";

    /**
     * this is the regular expression used by this parser.
     */
    private static final String REGEX = 
        "(([\\-]|[A-Z])([\\-]|[A-Z])([\\-]|[A-Z])([\\-]|[A-Z])([\\-]|[A-Z])"
        + "([\\-]|[A-Z])([\\-]|[A-Z])([\\-]|[A-Z])([\\-]|[A-Z])([\\-]|[A-Z]))"
        + "(\\S*)\\s*" 
        + "(\\S+)\\s*" 
        + "(\\S*)\\s*" 
        + "(\\d*)\\s*" 
        + "(\\d*)\\s*" 
        + MONTHS 
        + "\\s*" 
        + "((?:[012]\\d*)|(?:3[01]))\\s*" 
        + "((\\d\\d\\d\\d)|((?:[01]\\d)|(?:2[0123])):([012345]\\d))\\s" 
        + "(\\S*)(\\s*.*)";

    /**
     * The sole constructor for a EnterpriseUnixFTPEntryParser object.
     * 
     */
    public EnterpriseUnixFTPEntryParser()
    {
        super(REGEX);
    }

    /**
     * Parses a line of a unix FTP server file listing and converts  it into a
     * usable format in the form of an <code> FTPFile </code>  instance.  If
     * the file listing line doesn't describe a file,  <code> null </code> is
     * returned, otherwise a <code> FTPFile </code>  instance representing the
     * files in the directory is returned.
     * 
     * @param entry A line of text from the file listing
     * @return An FTPFile instance corresponding to the supplied entry
     */
    public Path parseFTPEntry(Path parent, String entry)
    {
		Path f = PathFactory.createPath(parent.getSession());

        if (matches(entry))
        {
            String usr = group(14);
            String grp = group(15);
            String filesize = group(16);
            String mo = group(17);
            String da = group(18);
            String yr = group(20);
            String hr = group(21);
            String min = group(22);
            String name = group(23);
			
			f.attributes.setType(Path.FILE_TYPE);
            f.attributes.setOwner(usr);
			f.attributes.setGroup(grp);
			try
			{
				f.status.setSize(Long.parseLong(filesize));
            }
            catch (NumberFormatException e)
            {
                // intentionally do nothing
            }

            Calendar cal = Calendar.getInstance();
            cal.set(Calendar.SECOND, 
                    0);
            cal.set(Calendar.MINUTE, 
                    0);
            cal.set(Calendar.HOUR_OF_DAY, 
                    0);
            try
            {
                int pos = MONTHS.indexOf(mo);
                int month = pos / 4;
                if (yr != null)
                {
                    // it's a year
                    cal.set(Calendar.YEAR, 
                            Integer.parseInt(yr));
                }
                else
                {
                    // it must be  hour/minute or we wouldn't have matched
                    int year = cal.get(Calendar.YEAR);

                    // if the month we're reading is greater than now, it must
                    // be last year
                    if (cal.get(Calendar.MONTH) < month)
                    {
                        year--;
                    }
                    cal.set(Calendar.YEAR, 
                            year);
                    cal.set(Calendar.HOUR_OF_DAY, 
                            Integer.parseInt(hr));
                    cal.set(Calendar.MINUTE, 
                            Integer.parseInt(min));
                }
                cal.set(Calendar.MONTH, 
                        month);
                cal.set(Calendar.DATE, 
                        Integer.parseInt(da));
                f.attributes.setTimestamp(cal.getTime());
            }
            catch (NumberFormatException e)
            {
                // do nothing, date will be uninitialized
            }
            f.setPath(parent.getAbsolute(), name);

            return f;
        }
        return null;
    }
}
