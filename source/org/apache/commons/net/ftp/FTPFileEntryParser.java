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
import java.util.List;

import ch.cyberduck.core.Path;

/**
 * FTPFileEntryParser defines the interface for parsing a single FTP file
 * listing and converting that information into an
 * <a href="org.apache.commons.net.ftp.FTPFile.html"> FTPFile </a> instance.
 * Sometimes you will want to parse unusual listing formats, in which
 * case you would create your own implementation of FTPFileEntryParser and
 * if necessary, subclass FTPFile.
 * <p>
 * Here are some examples showing how to use one of the classes that
 * implement this interface.  
 * <p>
 * The first example shows how to get an <b>iterable</b> list of files in which the 
 * more expensive <code>FTPFile</code> objects are not created until needed.  This
 * is suitable for paged displays.   It requires that a parser object be created
 * beforehand: <code>parser</code> is an object (in the package 
 * <code>org.apache.commons.net.ftp.parser</code>)
 * implementing this inteface.
 * 
 * <pre>
 *    FTPClient f=FTPClient();
 *    f.connect(server);
 *    f.login(username, password);
 *    FTPFileList list = f.createFileList(directory, parser);
 *    FTPFileIterator iter = list.iterator();
 * 
 *    while (iter.hasNext()) {
 *       FTPFile[] files = iter.getNext(25);  // "page size" you want
 *       //do whatever you want with these files, display them, etc.
 *       //expensive FTPFile objects not created until needed.
 *    }
 * </pre>
 * 
 * The second example uses the revised <code>FTPClient.listFiles()</code>
 * API to pull the whole list from the subfolder <code>subfolder</code> in 
 * one call, attempting to automatically detect the parser type.  This 
 * method, without a parserKey parameter, indicates that autodection should 
 * be used.
 * 
 * <pre>
 *    FTPClient f=FTPClient();
 *    f.connect(server);
 *    f.login(username, password);
 *    FTPFile[] files = f.listFiles("subfolder");
 * </pre>
 * 
 * The third example uses the revised <code>FTPClient.listFiles()</code>>
 * API to pull the whole list from the current working directory in one call, 
 * but specifying by classname the parser to be used.  For this particular
 * parser class, this approach is necessary since there is no way to 
 * autodetect this server type.
 * 
 * <pre>
 *    FTPClient f=FTPClient();
 *    f.connect(server);
 *    f.login(username, password);
 *    FTPFile[] files = f.listFiles(
 *      "org.apache.commons.net.ftp.parser.EnterpriseUnixFTPFileEntryParser", 
 *      ".");
 * </pre>
 *
 * The fourth example uses the revised <code>FTPClient.listFiles()</code>
 * API to pull a single file listing in an arbitrary directory in one call, 
 * specifying by KEY the parser to be used, in this case, VMS.  
 * 
 * <pre>
 *    FTPClient f=FTPClient();
 *    f.connect(server);
 *    f.login(username, password);
 *    FTPFile[] files = f.listFiles("VMS", "subfolder/foo.java");
 * </pre>
 *
 * @author <a href="mailto:scohen@apache.org">Steve Cohen</a>
 * @version $Id$
 * @see org.apache.commons.net.ftp.FTPFile
 * @see org.apache.commons.net.ftp.FTPClient#createFileList
 */
public interface FTPFileEntryParser
{
    /**
     * Parses a line of an FTP server file listing and converts it into a usable
     * format in the form of an <code> FTPFile </code> instance.  If the
     * file listing line doesn't describe a file, <code> null </code> should be
     * returned, otherwise a <code> FTPFile </code> instance representing the
     * files in the directory is returned.
     * <p>
     * @param listEntry A line of text from the file listing
     * @return An FTPFile instance corresponding to the supplied entry
     */
    Path parseFTPEntry(Path parent, String listEntry);

    /**
     * Reads the next entry using the supplied BufferedReader object up to 
     * whatever delemits one entry from the next.  Implementors must define
     * this for the particular ftp system being parsed.  In many but not all 
     * cases, this can be defined simply by calling BufferedReader.readLine().
     * 
     * @param reader The BufferedReader object from which entries are to be 
     * read.
     * 
     * @return A string representing the next ftp entry or null if none found.
     * @exception IOException thrown on any IO Error reading from the reader.
     */
    String readNextEntry(BufferedReader reader) throws IOException;

    
    /**
     * This method is a hook for those implementors (such as
     * VMSVersioningFTPEntryParser, and possibly others) which need to
     * perform some action upon the FTPFileList after it has been created
     * from the server stream, but before any clients see the list.
     * 
     * The default implementation can be a no-op.
     * 
     * @param original Original list after it has been created from the server stream
     * 
     * @return Original list as processed by this method.
     */
    List preParse(Path parent, List original);
}