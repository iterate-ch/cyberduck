package org.apache.commons.net.ftp;

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
import java.util.Iterator;
import java.util.List;

import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.MatchResult;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;

import ch.cyberduck.core.Path;

/**
 * This abstract class implements both the older FTPFileListParser and
 * newer FTPFileEntryParser interfaces with default functionality.
 * All the classes in the parser subpackage inherit from this.
 * 
 * @author Steve Cohen <scohen@apache.org>
 */
public abstract class FTPFileEntryParserImpl implements FTPFileEntryParser
{
		
    /**
     * internal pattern the matcher tries to match, representing a file 
     * entry
     */
    private Pattern pattern = null;

    /**
     * internal match result used by the parser
     */
    private MatchResult result = null;

    /**
     * Internal PatternMatcher object used by the parser.  It has protected
     * scope in case subclasses want to make use of it for their own purposes.
     */
    protected PatternMatcher _matcher_ = null;

    
    /**
     * The constructor for a FTPFileEntryParserImpl object.
     * 
     * @param regex  The regular expression with which this object is 
     * initialized.
     * 
     * @exception IllegalArgumentException
     * Thrown if the regular expression is unparseable.  Should not be seen in 
     * normal conditions.  It it is seen, this is a sign that a subclass has 
     * been created with a bad regular expression.   Since the parser must be 
     * created before use, this means that any bad parser subclasses created 
     * from this will bomb very quickly,  leading to easy detection.  
     */
    public FTPFileEntryParserImpl(String regex) 
    {
        try 
        {
            _matcher_ = new Perl5Matcher();
            pattern   = new Perl5Compiler().compile(regex);
        } 
        catch (MalformedPatternException e) 
        {
            throw new IllegalArgumentException (
                "Unparseable regex supplied:  " + regex);
        }
    }
    
    /**
     * Convenience method delegates to the internal MatchResult's matches()
     * method.
     *
     * @param s the String to be matched
     * @return true if s matches this object's regular expression.
     */
    public boolean matches(String s)
    {
        this.result = null;
        if (_matcher_.matches(s.trim(), this.pattern))
        {
            this.result = _matcher_.getMatch();
        }
        return null != this.result;
    }

    /**
     * Convenience method delegates to the internal MatchResult's groups()
     * method.
     *
     * @return the number of groups() in the internal MatchResult.
     */
    public int getGroupCnt()
    {
        if (this.result == null)
        {
            return 0;
        }
        return this.result.groups();
    }

    /**
     * Convenience method delegates to the internal MatchResult's group()
     * method.
     * 
     * @param matchnum match group number to be retrieved
     * 
     * @return the content of the <code>matchnum'th<code> group of the internal
     *         match or null if this method is called without a match having 
     *         been made.
     */
    public String group(int matchnum)
    {
        if (this.result == null)
        {
            return null;
        }
        return this.result.group(matchnum);
    }

    /**
     * For debugging purposes - returns a string shows each match group by 
     * number.
     * 
     * @return a string shows each match group by number.
     */
    public String getGroupsAsString()
    {
        StringBuffer b = new StringBuffer();
        for (int i = 1; i <= this.result.groups(); i++)
        {
            b.append(i).append(") ").append(this.result.group(i))
                .append(System.getProperty("line.separator"));
        }
        return b.toString();

    }

    /**
     * Reads the next entry using the supplied BufferedReader object up to
     * whatever delemits one entry from the next.  This default implementation
     * simply calls BufferedReader.readLine().
     *
     * @param reader The BufferedReader object from which entries are to be 
     * read.
     *
     * @return A string representing the next ftp entry or null if none found.
     * @exception IOException thrown on any IO Error reading from the reader.
     */
    public String readNextEntry(BufferedReader reader) throws IOException 
    {
        return reader.readLine();
    }
    /**
     * This method is a hook for those implementors (such as
     * VMSVersioningFTPEntryParser, and possibly others) which need to
     * perform some action upon the FTPFileList after it has been created
     * from the server stream, but before any clients see the list.
     *
     * This default implementation is a no-op.
     *
     * @param original Original list after it has been created from the server stream
     *
     * @return <code>original</code> unmodified.
     */
     public List preParse(Path parent, List original) { 
     	 Iterator it = original.iterator();
     	 while (it.hasNext()){
     	 	String entry = (String) it.next();
     	 	if (null == parseFTPEntry(parent, entry)) {
     	 		it.remove();
     	 	} else {
     	 		break;
     	 	}
     	 }
         return original;                                
     } 
}