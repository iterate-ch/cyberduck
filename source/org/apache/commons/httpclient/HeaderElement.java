/*
 * $Header$
 * $Revision$
 * $Date$
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 1999 The Apache Software Foundation.  All rights
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
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Tomcat", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
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
 *
 * [Additional notices, if required by prior licensing conditions]
 *
 */

package org.apache.commons.httpclient;

import java.util.BitSet;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.Vector;
import org.apache.commons.httpclient.log.Log;
import org.apache.commons.httpclient.log.LogSource;

/**
 * <p>One element of an HTTP header's value.</p>
 * <p>
 * Some HTTP headers (such as the set-cookie header) have values that
 * can be decomposed into multiple elements.  Such headers must be in the
 * following form:
 * </p>
 * <pre>
 * header  = [ element ] *( "," [ element ] )
 * element = name [ "=" [ value ] ] *( ";" [ param ] )
 * param   = name [ "=" [ value ] ]
 *
 * name    = token
 * value   = ( token | quoted-string )
 *
 * token         = 1*&lt;any char except "=", ",", ";", &lt;"&gt; and
 *                       white space&gt;
 * quoted-string = &lt;"&gt; *( text | quoted-char ) &lt;"&gt;
 * text          = any char except &lt;"&gt;
 * quoted-char   = "\" char
 * </pre>
 * <p>
 * Any amount of white space is allowed between any part of the
 * header, element or param and is ignored. A missing value in any
 * element or param will be stored as the empty {@link String};
 * if the "=" is also missing <var>null</var> will be stored instead.
 * </p>
 * <p>
 * This class represents an individual header element, containing
 * both a name/value pair (value may be <tt>null</tt>) and optionally
 * a set of additional parameters.
 * </p>
 * <p>
 * This class also exposes a {@link #parse} method for parsing a
 * {@link Header} value into an array of elements.
 * </p>
 *
 * @see Header
 *
 * @author <a href="mailto:bcholmes@interlog.com">B.C. Holmes</a>
 * @author <a href="mailto:jericho@thinkfree.com">Park, Sung-Gu</a>
 * @version $Revision$ $Date$
 */
public class HeaderElement extends NameValuePair {

    // ----------------------------------------------------------- Constructors

    /**
     * Default constructor.
     */
    public HeaderElement() {
        this(null,null,null);
    }

    /**
      * Constructor.
      * @param name my name
      * @param value my (possibly <tt>null</tt>) value
      */
    public HeaderElement(String name, String value) {
        this(name,value,null);
    }

    /**
      * Constructor.
      * @param name my name
      * @param value my (possibly <tt>null</tt>) value
      * @param parameter my (possibly <tt>null</tt>) parameters
      */
    public HeaderElement(String name, String value,
            NameValuePair[] parameters) {
        super(name, value);
        this.parameters = parameters;
    }

    // -------------------------------------------------------- Constants

    /** <tt>org.apache.commons.httpclient.HeaderElement</tt> log. */
    static private final Log log = LogSource.getInstance("org.apache.commons.httpclient.HeaderElement");

    /**
     * Map of numeric values to whether or not the
     * corresponding character is a "separator
     * character" (tspecial).
     */
    private static final BitSet SEPARATORS = new BitSet(128);

    /**
     * Map of numeric values to whether or not the
     * corresponding character is a "token
     * character".
     */
    private static final BitSet TOKEN_CHAR = new BitSet(128);

    /**
     * Map of numeric values to whether or not the
     * corresponding character is an "unsafe
     * character".
     */
    private static final BitSet UNSAFE_CHAR = new BitSet(128);

    /**
     * Static initializer for {@link #SEPARATORS},
     * {@link #TOKEN_CHAR}, and {@link #UNSAFE_CHAR}.
     */
    static {
        // rfc-2068 tspecial
        SEPARATORS.set('(');
        SEPARATORS.set(')');
        SEPARATORS.set('<');
        SEPARATORS.set('>');
        SEPARATORS.set('@');
        SEPARATORS.set(',');
        SEPARATORS.set(';');
        SEPARATORS.set(':');
        SEPARATORS.set('\\');
        SEPARATORS.set('"');
        SEPARATORS.set('/');
        SEPARATORS.set('[');
        SEPARATORS.set(']');
        SEPARATORS.set('?');
        SEPARATORS.set('=');
        SEPARATORS.set('{');
        SEPARATORS.set('}');
        SEPARATORS.set(' ');
        SEPARATORS.set('\t');

        // rfc-2068 token
        for (int ch = 32; ch < 127; ch++) {
            TOKEN_CHAR.set(ch);
        }
        TOKEN_CHAR.xor(SEPARATORS);

        // rfc-1738 unsafe characters, including CTL and SP, and excluding
        // "#" and "%"
        for (int ch = 0; ch < 32; ch++) {
            UNSAFE_CHAR.set(ch);
        }
        UNSAFE_CHAR.set(' ');
        UNSAFE_CHAR.set('<');
        UNSAFE_CHAR.set('>');
        UNSAFE_CHAR.set('"');
        UNSAFE_CHAR.set('{');
        UNSAFE_CHAR.set('}');
        UNSAFE_CHAR.set('|');
        UNSAFE_CHAR.set('\\');
        UNSAFE_CHAR.set('^');
        UNSAFE_CHAR.set('~');
        UNSAFE_CHAR.set('[');
        UNSAFE_CHAR.set(']');
        UNSAFE_CHAR.set('`');
        UNSAFE_CHAR.set(127);
    }

    // ----------------------------------------------------- Instance Variables

    /** My parameters, if any. */
    protected NameValuePair[] parameters = null;

    // ------------------------------------------------------------- Properties

    /** Get parameters, if any. */
    public NameValuePair[] getParameters() {
        return this.parameters;
    }

    // --------------------------------------------------------- Public Methods

    /**
     * This parses the value part of a header. The result is an array of
     * HeaderElement objects.
     *
     * @param headerValue  the string representation of the header value
     *                     (as received from the web server).
     * @return the header elements containing <code>Header</code> elements.
     * @exception HttpException if the above syntax rules are violated.
     */
    public final static HeaderElement[] parse(String headerValue)
            throws HttpException
    {
        if (headerValue == null)
            return null;
        Vector elements = new Vector();

        StringTokenizer tokenizer =
            new StringTokenizer(headerValue.trim(), ",");

        while (tokenizer.countTokens() > 0) {
            String nextToken = tokenizer.nextToken();

            // careful... there may have been a comma in a quoted string
            try {
                while (HeaderElement.hasOddNumberOfQuotationMarks(
                       nextToken)) {
                    nextToken += "," + tokenizer.nextToken();
                }
            } catch (NoSuchElementException exception) {
                throw new HttpException(
                    "Bad header format: wrong number of quotation marks");
            }

            try {
                /**
                 * Following to RFC 2109 and 2965, in order not to conflict
                 * with the next header element, make it sure to parse tokens.
                 * the expires date format is "Wdy, DD-Mon-YY HH:MM:SS GMT".
                 * Notice that there is always comma(',') sign.
                 * For the general cases, rfc1123-date, rfc850-date.
                 */
                if (tokenizer.hasMoreTokens()) {
                    if (nextToken.endsWith("Mon") ||
                        nextToken.endsWith("Tue") ||
                        nextToken.endsWith("Wed") ||
                        nextToken.endsWith("Thu") ||
                        nextToken.endsWith("Fri") ||
                        nextToken.endsWith("Sat") ||
                        nextToken.endsWith("Sun") ||
                        nextToken.endsWith("Monday") ||
                        nextToken.endsWith("Tuesday") ||
                        nextToken.endsWith("Wednesday") ||
                        nextToken.endsWith("Thursday") ||
                        nextToken.endsWith("Friday") ||
                        nextToken.endsWith("Saturday") ||
                        nextToken.endsWith("Sunday" )) {

                        nextToken += tokenizer.nextToken(",");
                    }
                }
            } catch (NoSuchElementException exception) {
                throw new HttpException
                    ("Bad header format: parsing with wrong header elements");
            }

            String tmp = nextToken.trim();
            if (!tmp.endsWith(";")) {
                tmp += ";";
            }
            char[] header = tmp.toCharArray();

            boolean inAString = false;
            int startPos = 0;
            HeaderElement element = new HeaderElement();
            Vector paramlist = new Vector();
            for (int i = 0 ; i < header.length ; i++) {
                if (header[i] == ';' && !inAString) {
                    NameValuePair pair = parsePair(header, startPos, i);
                    if (pair == null) {
                        throw new HttpException(
                            "Bad header format: empty name/value pair in" +
                            nextToken);

                    // the first name/value pair are handled differently
                    } else if (startPos == 0) {
                        element.setName(pair.getName());
                        element.setValue(pair.getValue());
                    } else {
                        paramlist.addElement(pair);
                    }
                    startPos = i + 1;
                } else if (header[i] == '"' &&
                           !(inAString && i > 0 && header[i-1] == '\\')) {
                    inAString = !inAString;
                }
            }

            // now let's add all the parameters into the header element
            if (paramlist.size() > 0) {
                NameValuePair[] tmp2 = new NameValuePair[paramlist.size()];
                paramlist.copyInto((NameValuePair[]) tmp2);
                element.parameters = tmp2;
                paramlist.removeAllElements();
            }

            // and save the header element into the list of header elements
            elements.addElement(element);
        }

        HeaderElement[] headerElements = new HeaderElement[elements.size()];
        elements.copyInto((HeaderElement[]) headerElements);
        return headerElements;
    }

    /**
     * Return <tt>true</tt> if <i>string</i> has
     * an odd number of <tt>"</tt> characters.
     */
    private final static boolean hasOddNumberOfQuotationMarks(String string) {
        boolean odd = false;
        int start = -1;
        while ((start = string.indexOf('"', start+1)) != -1) {
            odd = !odd;
        }
        return odd;
    }

    private final static NameValuePair parsePair(
            char[] header, int start, int end)
            throws HttpException {

        boolean done = false;
        NameValuePair pair = null;
        String name = new String(header, start, end - start).trim();
        String value = null;

        int index = name.indexOf("=");
        if (index >= 0) {
            if ((index + 1) < name.length()) {
                value = name.substring(index+1).trim();
                // strip quotation marks
                if (value.startsWith("\"") && value.endsWith("\"")) {
                    value = value.substring(1,value.length()-1);
                }
            }
            name = name.substring(0,index).trim();
        }

        pair = new NameValuePair(name, value);

        return pair;
    }

}

