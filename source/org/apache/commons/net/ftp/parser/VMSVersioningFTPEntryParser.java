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

import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.MatchResult;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;

import ch.cyberduck.core.Path;

/**
 * Special implementation VMSFTPEntryParser with versioning turned on.
 * This parser removes all duplicates and only leaves the version with the highest
 * version number for each filename.
 * 
 * This is a sample of VMS LIST output
 *   
 *  "1-JUN.LIS;1              9/9           2-JUN-1998 07:32:04  [GROUP,OWNER]    (RWED,RWED,RWED,RE)",
 *  "1-JUN.LIS;2              9/9           2-JUN-1998 07:32:04  [GROUP,OWNER]    (RWED,RWED,RWED,RE)",
 *  "DATA.DIR;1               1/9           2-JUN-1998 07:32:04  [GROUP,OWNER]    (RWED,RWED,RWED,RE)",
 * <P>
 * 
 * @author  <a href="Winston.Ojeda@qg.com">Winston Ojeda</a>
 * @author <a href="mailto:scohen@apache.org">Steve Cohen</a>
 * @author <a href="sestegra@free.fr">Stephane ESTE-GRACIAS</a>
 * @version $Id$
 * 
 * @see org.apache.commons.net.ftp.FTPFileEntryParser FTPFileEntryParser (for usage instructions)
 */
public class VMSVersioningFTPEntryParser extends VMSFTPEntryParser
{

    private Perl5Matcher _preparse_matcher_;
    private Pattern _preparse_pattern_;
    private static final String PRE_PARSE_REGEX =
        "(.*);([0-9]+)\\s*.*"; 

    /**
     * Constructor for a VMSFTPEntryParser object.  Sets the versioning member 
     * to the supplied value.
     *  
     * @exception IllegalArgumentException
     * Thrown if the regular expression is unparseable.  Should not be seen 
     * under normal conditions.  It it is seen, this is a sign that 
     * <code>REGEX</code> is  not a valid regular expression.
     */
    public VMSVersioningFTPEntryParser()
    {
        super();
        try 
        {
            _preparse_matcher_ = new Perl5Matcher();
            _preparse_pattern_ = new Perl5Compiler().compile(PRE_PARSE_REGEX);
        } 
        catch (MalformedPatternException e) 
        {
            throw new IllegalArgumentException (
                "Unparseable regex supplied:  " + PRE_PARSE_REGEX);
        }

    }


    private class NameVersion {
        String name;
        int versionNumber;
        NameVersion(String name, String vers) {
            this.name = name;
            this.versionNumber = Integer.parseInt(vers);
        }
    }

    /**
     * Implement hook provided for those implementers (such as
     * VMSVersioningFTPEntryParser, and possibly others) which return
     * multiple files with the same name to remove the duplicates ..
     *
     * @param original Original list
     *
     * @return Original list purged of duplicates
     */
    public List preParse(Path parent, List original) {
    	original = super.preParse(parent, original);
        HashMap existingEntries = new HashMap();
        ListIterator iter = original.listIterator();
        while (iter.hasNext()) {
            String entry = ((String)iter.next()).trim();
            MatchResult result = null;
            if (_preparse_matcher_.matches(entry, _preparse_pattern_)) {
                result = _preparse_matcher_.getMatch();
                String name = result.group(1);
                String version = result.group(2);
                NameVersion nv = new NameVersion(name, version);
                NameVersion existing = (NameVersion) existingEntries.get(name);
                if (null != existing) {
                    if (nv.versionNumber < existing.versionNumber) {
                        iter.remove();  // removal removes from original list.
                        continue;
                    }
                }
                existingEntries.put(name, nv);
            }
            
        }
        // we've now removed all entries less than with less than the largest
        // version number for each name that were listed after the largest.
        // we now must remove those with smaller than the largest version number
        // for each name that were found before the largest
        while (iter.hasPrevious()) {
            String entry = ((String)iter.previous()).trim();
            MatchResult result = null;
            if (_preparse_matcher_.matches(entry, _preparse_pattern_)) {
                result = _preparse_matcher_.getMatch();
                String name = result.group(1);
                String version = result.group(2);
                NameVersion nv = new NameVersion(name, version);
                NameVersion existing = (NameVersion) existingEntries.get(name);
                if (null != existing) {
                    if (nv.versionNumber < existing.versionNumber) {
                        iter.remove(); // removal removes from original list.
                    }
                }
            }
            
        }
        return original;
    }

    protected boolean isVersioning() {
        return true;
    }
}