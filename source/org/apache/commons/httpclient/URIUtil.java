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

import java.io.UnsupportedEncodingException;
import java.util.BitSet;

/**
 * <p>
 * General purpose methods for encoding URI's, as described in
 * <a href="http://www.ietf.org/rfc/rfc2396.txt?number=2396">RFC 2396</a>.
 * </p>
 * <p>
 * This class provides a number of methods useful for encoding and
 * decoding the "%HH" format, as used in various HTTP related
 * formats such as URIs and the <nobr>x-www-form-urlencoded</nobr>
 * MIME type.
 * It can be seen as a more flexible (and more robust) form
 * of the core JDK {@link java.net.URLEncoder} and
 * {@link java.net.URLDecoder} classes.
 * </p>
 *
 * @author Craig R. McClanahan
 * @author Tim Tye
 * @author Remy Maucherat
 * @author Park, Sung-Gu
 * @author Rodney Waldhoff
 * @version $Revision$ $Date$
 */

public class URIUtil {

	// --------------------------------------------------------- Public Methods

	/**
	 * Unescape the given {@link String}, converting all <tt>%HH</tt>
	 * sequences into the UTF-8 character <tt>0xHH</tt>.
	 *
	 * @param str the escaped {@link String}
	 * @exception IllegalArgumentException if a '%'
	 *            character is not followed by a
	 *            valid 2-digit hexadecimal number
	 */
	public static final String decode(String str) {
		return decode(str, false);
	}

	/**
	 * Unescape the given {@link String}, converting all <tt>%HH</tt>
	 * sequences into the UTF-8 character <tt>0xHH</tt>.
	 * <p>
	 * When <i>plusIsSpace</i> is true, <tt>'+'</tt> will
	 * be converted into <tt>' '</tt> (space),
	 * as is used in the <nobr>x-www-form-urlencoded</nobr>
	 * MIME type.
	 *
	 * @param str the escaped {@link String}
	 * @exception IllegalArgumentException if a '%'
	 *            character is not followed by a
	 *            valid 2-digit hexadecimal number
	 */
	public static final String decode(String str, boolean plusIsSpace) {
		try {
			return (str == null) ? null : decode(str.getBytes(), null, plusIsSpace);
		}
		catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Default encoding not supported !?!");
		}
	}

	/**
	 * Unescape the given byte array by first converting all
	 * <tt>'%'</tt>, <tt>'H'</tt>, <tt>'H'</tt> sequences into
	 * the byte <tt>0xHH</tt>, and then converting the bytes
	 * into characters using the default encoding.
	 *
	 * @param bytes the escaped byte array, which
	 *              <i>may be changed</i> by this
	 *              call
	 * @exception IllegalArgumentException if a <tt>'%'</tt>
	 *            byte is not followed by a valid
	 *            2-digit hexadecimal number (as bytes)
	 */
	public static final String decode(byte[] bytes) {
		try {
			return decode(bytes, null, false);
		}
		catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Default encoding not supported !?!");
		}
	}

	/**
	 * Unescape the given byte array by first converting all
	 * <tt>'%'</tt>, <tt>'H'</tt>, <tt>'H'</tt> sequences into
	 * the byte <tt>0xHH</tt>, and then converting the bytes
	 * into characters using the default encoding.
	 * <p>
	 * When <i>plusIsSpace</i> is true, <tt>'+'</tt> will
	 * be converted into <tt>' '</tt> (space),
	 * as is used in the <nobr>x-www-form-urlencoded</nobr>
	 * MIME type.
	 *
	 * @param bytes the escaped byte array, which
	 *              <i>may be changed</i> by this
	 *              call
	 * @exception IllegalArgumentException if a <tt>'%'</tt>
	 *            byte is not followed by a valid
	 *            2-digit hexadecimal number (as bytes)
	 */
	public static final String decode(byte[] bytes, boolean plusIsSpace) {
		try {
			return decode(bytes, null, plusIsSpace);
		}
		catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Default encoding not supported !?!");
		}
	}

	/**
	 * Unescape a fragment of the given byte array, by
	 * first converting all <tt>'%'</tt>, <tt>'H'</tt>,
	 * <tt>'H'</tt> sequences into the byte
	 * <tt>0xHH</tt>, and then converting the bytes
	 * into characters using the default encoding.
	 *
	 * @param bytes the escaped byte array, which
	 *              <i>may be changed</i> by this
	 *              call
	 * @param off the index of the first byte to convert
	 * @param len the number of unescaped bytes to convert
	 * @exception IllegalArgumentException if a <tt>'%'</tt>
	 *            byte is not followed by a valid
	 *            2-digit hexadecimal number (as bytes)
	 */
	public static final String decode(byte[] bytes, int off, int len) {
		try {
			return decode(bytes, off, len, null, false);
		}
		catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Default encoding not supported !?!");
		}
	}

	/**
	 * Unescape a fragment of the given byte array, by
	 * first converting all <tt>'%'</tt>, <tt>'H'</tt>,
	 * <tt>'H'</tt> sequences into the byte
	 * <tt>0xHH</tt>, and then converting the bytes
	 * into characters using the default encoding.
	 * <p>
	 * When <i>plusIsSpace</i> is true, <tt>'+'</tt> will
	 * be converted into <tt>' '</tt> (space),
	 * as is used in the <nobr>x-www-form-urlencoded</nobr>
	 * MIME type.
	 *
	 * @param bytes the escaped byte array, which
	 *              <i>may be changed</i> by this
	 *              call
	 * @param off the index of the first byte to convert
	 * @param len the number of unescaped bytes to convert
	 * @exception IllegalArgumentException if a <tt>'%'</tt>
	 *            byte is not followed by a valid
	 *            2-digit hexadecimal number (as bytes)
	 */
	public static final String decode(byte[] bytes, int off, int len, boolean plusIsSpace) {
		try {
			return decode(bytes, off, len, null, plusIsSpace);
		}
		catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Default encoding not supported !?!");
		}
	}

	/**
	 * Unescape the given byte array by first converting all
	 * <tt>'%'</tt>, <tt>'H'</tt>, <tt>'H'</tt> sequences into
	 * the byte <tt>0xHH</tt>, and then converting the bytes
	 * into characters using the default encoding.
	 *
	 * @param bytes the escaped byte array, which
	 *              <i>may be changed</i> by this
	 *              call
	 * @param enc the encoding to use
	 * @exception IllegalArgumentException if a <tt>'%'</tt>
	 *            byte is not followed by a valid
	 *            2-digit hexadecimal number (as bytes)
	 */
	public static final String decode(byte[] bytes, String enc) throws UnsupportedEncodingException {
		return decode(bytes, 0, bytes.length, enc, false);
	}

	/**
	 * Unescape the given byte array by first converting all
	 * <tt>'%'</tt>, <tt>'H'</tt>, <tt>'H'</tt> sequences into
	 * the byte <tt>0xHH</tt>, and then converting the bytes
	 * into characters using the default encoding.
	 * <p>
	 * When <i>plusIsSpace</i> is true, <tt>'+'</tt> will
	 * be converted into <tt>' '</tt> (space),
	 * as is used in the <nobr>x-www-form-urlencoded</nobr>
	 * MIME type.
	 *
	 * @param bytes the escaped byte array, which
	 *              <i>may be changed</i> by this
	 *              call
	 * @param enc the encoding to use
	 * @exception IllegalArgumentException if a <tt>'%'</tt>
	 *            byte is not followed by a valid
	 *            2-digit hexadecimal number (as bytes)
	 */
	public static final String decode(byte[] bytes, String enc, boolean plusIsSpace) throws UnsupportedEncodingException {
		return decode(bytes, 0, bytes.length, enc, plusIsSpace);
	}

	/**
	 * Unescape a fragment of the given byte array by first
	 * converting all <tt>'%'</tt>, <tt>'H'</tt>, <tt>'H'</tt>
	 * sequences into the byte <tt>0xHH</tt>, and then
	 * converting the bytes into characters using the
	 * specified encoding.
	 *
	 * @param bytes the escaped byte array
	 * @param off the index of the first byte to convert
	 * @param len the number of escaped bytes to convert
	 * @param enc the encoding to use
	 * @exception IllegalArgumentException if a '%'
	 *            character is not followed by a
	 *            valid 2-digit hexadecimal number
	 */
	public static final String decode(byte[] bytes, int off, int len, String enc) throws UnsupportedEncodingException {
		return decode(bytes, 0, bytes.length, enc, false);
	}

	/**
	 * Unescape a fragment of the given byte array by first
	 * converting all <tt>'%'</tt>, <tt>'H'</tt>, <tt>'H'</tt>
	 * sequences into the byte <tt>0xHH</tt>, and then
	 * converting the bytes into characters using the
	 * specified encoding.
	 * <p>
	 * When <i>plusIsSpace</i> is true, a
	 * <tt>'+'</tt> byte will be converted into <tt>' '</tt>,
	 * as is used in the <nobr>x-www-form-urlencoded</nobr>
	 * MIME type.
	 *
	 * @param bytes the escaped byte array
	 * @param off the index of the first byte to convert
	 * @param len the number of escaped bytes to convert
	 * @param enc the encoding to use
	 * @exception IllegalArgumentException if a '%'
	 *            character is not followed by a
	 *            valid 2-digit hexadecimal number
	 */
	public static final String decode(byte[] bytes, int off, int len, String enc, boolean plusIsSpace) throws UnsupportedEncodingException {
		if (null == bytes) {
			return null;
		}
		int end = off + len;
		int ix = off;
		int ox = off;
		while (ix < end) {
			byte b = bytes[ix++];     // Get byte to test
			if (plusIsSpace && b == '+') {
				b = (byte) ' ';
			}
			else if (b == '%') {
				b = (byte) ((convertHexDigit(bytes[ix++]) << 4)
				    + convertHexDigit(bytes[ix++]));
			}
			bytes[ox++] = b;
		}
		if (enc != null) {
			return new String(bytes, off, ox, enc);
		}
		else {
			return new String(bytes, off, ox);
		}
	}

	// ------------------------------------------------------------------------

	/**
	 * Escape the given {@link String} by replacing
	 * all characters not in the default
	 * set of safe characters ({@link #unreserved()})
	 * into the sequence %HH (where HH is the hex
	 * value of the character in the default encoding).
	 *
	 * @param str The unescaped string
	 */
	public static final String encode(String str) {
		return encode(str, null, false);
	}

	/**
	 * Escape the given {@link String} by replacing
	 * all characters not in the default
	 * set of safe characters ({@link #unreserved()})
	 * into the sequence %HH (where HH is the hex
	 * value of the character in the default encoding).
	 * <p>
	 * When <i>spaceAsPlus</i> is true and <tt>' '</tt>
	 * needs to be encoded (i.e., it is not a safe byte),
	 * then it will be converted to <tt>'+'</tt>,
	 * as is used in the <nobr>x-www-form-urlencoded</nobr>
	 * MIME type.
	 *
	 * @param str The unescaped string
	 */
	public static final String encode(String str, boolean spaceAsPlus) {
		return encode(str, null, spaceAsPlus);
	}

	/**
	 * Escape the given {@link String} by replacing
	 * all characters not in the given
	 * set of safe characters into the sequence
	 * %HH (where HH is the hex value of the
	 * character in the default encoding).
	 *
	 * @param str The unescaped string
	 * @param safe The set of "safe" characters (not to be escaped)
	 */
	public static final String encode(String str, BitSet safe) {
		return (str == null) ? null : encode(str.getBytes(), safe, false);
	}

	/**
	 * Escape the given {@link String} by replacing
	 * all characters not in the given
	 * set of safe characters into the sequence
	 * %HH (where HH is the hex value of the
	 * character in the default encoding).
	 * <p>
	 * When <i>spaceAsPlus</i> is true and <tt>' '</tt>
	 * needs to be encoded (i.e., it is not a safe byte),
	 * then it will be converted to <tt>'+'</tt>,
	 * as is used in the <nobr>x-www-form-urlencoded</nobr>
	 * MIME type.
	 *
	 * @param str The unescaped string
	 * @param safe The set of "safe" characters (not to be escaped)
	 */
	public static final String encode(String str, BitSet safe, boolean spaceAsPlus) {
		return (str == null) ? null : encode(str.getBytes(), safe, spaceAsPlus);
	}

	/**
	 * Escape the given byte array by first converting all
	 * bytes not in the given set of "safe" bytes to
	 * the sequence <tt>'%'</tt>, <tt>'H'</tt>, <tt>'H'</tt>
	 * (where HH is the hex value of the byte),
	 * and then converting the bytes
	 * into characters using the default encoding.
	 *
	 * @param bytes the unescaped bytes
	 * @param safe the set of "safe" bytes (not to be escaped)
	 */
	public static final String encode(byte[] bytes, BitSet safe) {
		return (bytes == null) ? null : encode(bytes, 0, bytes.length, safe, false);
	}

	/**
	 * Escape the given byte array by first converting all
	 * bytes not in the given set of "safe" bytes to
	 * the sequence <tt>'%'</tt>, <tt>'H'</tt>, <tt>'H'</tt>
	 * (where HH is the hex value of the byte),
	 * and then converting the bytes
	 * into characters using the default encoding.
	 * <p>
	 * When <i>spaceAsPlus</i> is true and <tt>' '</tt>
	 * needs to be encoded (i.e., it is not a safe byte),
	 * then it will be converted to <tt>'+'</tt>,
	 * as is used in the <nobr>x-www-form-urlencoded</nobr>
	 * MIME type.
	 *
	 * @param bytes the unescaped bytes
	 * @param safe the set of "safe" bytes (not to be escaped)
	 */
	public static final String encode(byte[] bytes, BitSet safe, boolean spaceAsPlus) {
		return (bytes == null) ? null : encode(bytes, 0, bytes.length, safe, spaceAsPlus);
	}

	/**
	 * Escape a fragment of the given byte array by first
	 * converting all bytes not in the given set of
	 * "safe" bytes to the sequence <tt>'%'</tt>,
	 * <tt>'H'</tt>, <tt>'H'</tt>
	 * (where HH is the hex value of the byte),
	 * and then converting the bytes
	 * into characters using the default encoding.
	 *
	 * @param bytes The unescaped bytes
	 * @param off the index of the first byte to convert
	 * @param len the number of unescaped bytes to convert
	 * @param safe The set of "safe" bytes (not to be escaped)
	 */
	public static final String encode(byte[] bytes, int off, int len, BitSet safe) {
		return (bytes == null) ? null : encode(bytes, 0, bytes.length, safe, false);
	}

	/**
	 * Escape a fragment of the given byte array by first
	 * converting all bytes not in the given set of
	 * "safe" bytes to the sequence <tt>'%'</tt>,
	 * <tt>'H'</tt>, <tt>'H'</tt>
	 * (where HH is the hex value of the byte),
	 * and then converting the bytes
	 * into characters using the default encoding.
	 * <p>
	 * When <i>spaceAsPlus</i> is true and <tt>' '</tt>
	 * needs to be encoded (i.e., it is not a safe byte),
	 * then it will be converted to <tt>'+'</tt>,
	 * as is used in the <nobr>x-www-form-urlencoded</nobr>
	 * MIME type.
	 *
	 * @param bytes The unescaped bytes
	 * @param off the index of the first byte to convert
	 * @param len the number of unescaped bytes to convert
	 * @param safe The set of "safe" bytes (not to be escaped)
	 */
	public static final String encode(byte[] bytes, int off, int len, BitSet safe, boolean spaceAsPlus) {
		if (null == bytes) {
			return null;
		}
		if (null == safe) {
			safe = unreserved;
		}
		StringBuffer rewrittenStr = new StringBuffer(len);
		for (int i = off; i < len; i++) {
			char c = (char) bytes[i];
			if (safe.get(c)) {
				rewrittenStr.append(c);
			}
			else {
				if (spaceAsPlus && ' ' == c) {
					rewrittenStr.append('+');
				}
				else {
					byte toEscape = bytes[i];
					rewrittenStr.append('%');
					int low = (int) (toEscape & 0x0f);
					int high = (int) ((toEscape & 0xf0) >> 4);
					rewrittenStr.append(hexadecimal[high]);
					rewrittenStr.append(hexadecimal[low]);
				}
			}
		}
		return rewrittenStr.toString();
	}

	/**
	 * Escape the given {@link String}, first converting
	 * the {@link String} to bytes using the specified
	 * encoding, then replacing all bytes not in the given
	 * set of safe bytes into the sequence %HH (where HH is
	 * the hex value of the byte).
	 *
	 * @param str the unescaped string
	 * @param safe the set of "safe" characters (not to be escaped)
	 * @param enc the encoding to use
	 */
	public static final String encode(String str, BitSet safe, String enc) {
		return encode(str, safe, enc, false);
	}

	/**
	 * Escape the given {@link String}, first converting
	 * the {@link String} to bytes using the specified
	 * encoding, then replacing all bytes not in the given
	 * set of safe bytes into the sequence %HH (where HH is
	 * the hex value of the byte).
	 * <p>
	 * When <i>spaceAsPlus</i> is true and <tt>' '</tt>
	 * needs to be encoded (i.e., it is not a safe byte),
	 * then it will be converted to <tt>'+'</tt>,
	 * as is used in the <nobr>x-www-form-urlencoded</nobr>
	 * MIME type.
	 *
	 * @param str the unescaped string
	 * @param safe the set of "safe" characters (not to be escaped)
	 * @param enc the encoding to use
	 */
	public static final String encode(String str, BitSet safe, String enc, boolean spaceAsPlus) {
		try {
			return encode(str.getBytes(enc), safe, spaceAsPlus);
		}
		catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return encode(str.getBytes(), safe, spaceAsPlus);
		}
	}

	// ------------------------------- RFC 2396 Character Sets : Public Methods

	public static final BitSet unreserved() {
		return unreserved;
	}

	public static final BitSet pathSafe() {
		return pathSafe;
	}

	public static final BitSet queryStringValueSafe() {
		return queryStringValueSafe;
	}

	// ------------------------------------------------------ Private Constants

	/**
	 * Array mapping hexadecimal values to the corresponding ASCII characters.
	 */
	private static final char[] hexadecimal = {
		'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
		'A', 'B', 'C', 'D', 'E', 'F'
	};

	// ---------------------------- RFC 2396 Character Sets : Private Constants

	/**
	 * "Alpha" characters from RFC 2396.
	 * <pre>alpha = ["a"-"z"] | ["A"-"Z"]</pre>
	 */
	private static final BitSet alpha = new BitSet(256);
	/** Static initializer for {@link #alpha}. */
	static {
		for (int i = 'a'; i <= 'z'; i++) {
			alpha.set(i);
		}
		for (int i = 'A'; i <= 'Z'; i++) {
			alpha.set(i);
		}
	}

	/**
	 * "Alphanum" characters from RFC 2396.
	 * <pre>alphanum = {@link #alpha} | ["0"-"9"]</pre>
	 */
	private static final BitSet alphanum = new BitSet(256);
	/** Static initializer for {@link #alphanum}. */
	static {
		alphanum.or(alpha);
		for (int i = '0'; i <= '9'; i++) {
			alphanum.set(i);
		}
	}

	/**
	 * "Reserved" characters from RFC 2396.
	 * <pre>reserved = ";" | "/" | "?" | ":" | "@" | "&amp;" | "=" | "+" | "$" | ","</pre>
	 */
	private static final BitSet reserved = new BitSet(256);
	/** Static initializer for {@link #reserved}. */
	static {
		reserved.set(';');
		reserved.set('/');
		reserved.set('?');
		reserved.set(':');
		reserved.set('@');
		reserved.set('&');
		reserved.set('=');
		reserved.set('+');
		reserved.set('$');
		reserved.set(',');
	}

	/**
	 * "Mark" characters from RFC 2396.
	 * <pre>mark = "-" | "_" | "." | "!" | "~" | "*" | "'" | "(" | ")"</pre>
	 */
	private static final BitSet mark = new BitSet(256);
	/** Static initializer for {@link #mark}. */
	static {
		mark.set('-');
		mark.set('_');
		mark.set('.');
		mark.set('!');
		mark.set('~');
		mark.set('*');
		mark.set('\'');
		mark.set('(');
		mark.set(')');
	}

	/**
	 * "Unreserved" characters from RFC 2396.
	 * <pre>unreserved = {@link #alphanum} | {@link #mark}</pre>
	 */
	private static final BitSet unreserved = new BitSet(256);
	/** Static initializer for {@link #unreserved}. */
	static {
		unreserved.or(alphanum);
		unreserved.or(mark);
	}

	/**
	 * "Delims" characters from RFC 2396.
	 * <pre>delims = "&lt;" | "&gt;" | "#" | "%" | &lt;"&gt;</pre>
	 */
	private static final BitSet delims = new BitSet(256);
	/** Static initializer for {@link #delims}. */
	static {
		delims.set('<');
		delims.set('>');
		delims.set('#');
		delims.set('%');
		delims.set('"');
	}

	/**
	 * "Unwise" characters from RFC 2396.
	 * <pre>unwise = "{" | "}" | "|" | "\" | "^" | "[" | "]" | "`"</pre>
	 */
	private static final BitSet unwise = new BitSet(256);
	/** Static initializer for {@link #unwise}. */
	static {
		unwise.set('{');
		unwise.set('}');
		unwise.set('|');
		unwise.set('\\');
		unwise.set('^');
		unwise.set('[');
		unwise.set(']');
		unwise.set('`');
	}

	private static final BitSet pathReserved = new BitSet(256);
	/** Static initializer for {@link #pathReserved}. */
	static {
		pathReserved.set('/');
		pathReserved.set(';');
		pathReserved.set('=');
		pathReserved.set('?');
	}

	// ------------------------------ "Safe" Character Sets : Private Constants

	private static final BitSet pathSafe = new BitSet(256);

	static {
		pathSafe.or(unreserved);
		pathSafe.or(pathReserved);
	}

	private static final BitSet queryStringValueSafe = new BitSet(256);

	static {
		queryStringValueSafe.or(unreserved);
	}

	// -------------------------------------------------------- Private Methods

	/**
	 * Convert a byte character value to hexidecimal digit value.
	 *
	 * @param b the character value byte
	 */
	private static final byte convertHexDigit(byte b) {
		switch (b) {
			case '0':
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
			case '8':
			case '9':
				return (byte) (b - '0');
			case 'a':
			case 'b':
			case 'c':
			case 'd':
			case 'e':
			case 'f':
				return (byte) (b - 'a' + 10);
			case 'A':
			case 'B':
			case 'C':
			case 'D':
			case 'E':
			case 'F':
				return (byte) (b - 'A' + 10);
			default:
				throw new IllegalArgumentException(b + " is not a hex value");
		}
	}

}

