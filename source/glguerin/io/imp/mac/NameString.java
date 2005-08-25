/*
** Copyright 1998, 1999, 2001 by Gregory L. Guerin.
** Terms of use:
**  - Briefly: OPEN SOURCE under Artistic License -- credit fairly, use freely, alter carefully.
**  - Fully: <http://www.amug.org/~glguerin/sw/artistic-license.html>
** This file is from the MacBinary Toolkit for Java:
**   <http://www.amug.org/~glguerin/sw/#macbinary> 
*/

package glguerin.io.imp.mac;


// --- Revision History ---
// 12Apr99 GLG  refactor and rework to provide abstract-factory features
// 13Apr99 GLG  remove everything but PStr conversion methods
// 14Apr99 GLG  doc-comments
// 26Apr99 GLG  edit doc-comments
// 24May01 GLG  throw IllegalArgumentException from toPStr() for too-long results


/**
** Has static methods that convert between String form and PStr-in-byte[] form.
** They encode and decode using the default String name-encodings.
** Since this class is presumed to run only on a Mac, that platform's default encoding should
** be acceptable.  If you port this to a non-Mac (though I don't see why that would be useful)
** you should hard-wire it to use "MacRoman" or "MacTEC" or some other Mac-relevant encoding-name.
**
** @author Gregory Guerin
*/

public class NameString
{
	/**
	** The max length, in bytes, of a PStr, not including the leading count-byte.
	*/
	public static final int LIMIT_PSTR = 255;

	/**
	** Convert to a P-String form in a new byte-array, truncating to no more than byteLimit bytes
	** after the count-byte.  For example, to limit to a Str31-form, byteLimit is 31.
	** The largest effective byteLimit is 255, since P-Strings are inherently length-limited
	** to 255 bytes after the count-byte.
	**<p>
	** If the resulting P-String would be longer than 255, an IllegalArgumentException is thrown
	** rather than truncating to 255.  This occurs even if the byteLimit is 255.
	** If the resulting P-String is shorter than 255 but longer than byteLimit, it is quietly
	** truncated and no exception is thrown.
	**<p>
	** The current encoding, as designated by the "file.encoding" property, is used to encode.
	*/
	public static byte[]
	toPStr( String str, int byteLimit )
	{
		if ( str == null )
			return ( new byte[ 1 ] );		// appears as new PStr with 0 count-byte

		byte[] bytes = str.getBytes();
		int len = bytes.length;
		if ( len > LIMIT_PSTR )
			throw new IllegalArgumentException( "Longer than 255: " + str );

		// Getting here, a too-long result will be quietly truncated to size.
		if ( len > byteLimit )  { len = byteLimit; }
		
		byte[] PStr = new byte[ len + 1 ];
		System.arraycopy( bytes, 0, PStr, 1, len );
		PStr[ 0 ] = (byte) len;
		return ( PStr );
	}

	/**
	** Convert from a P-String form in the byte-array, beginning at the given offset,
	** into a String of UniCode characters.  That is, the P-String's count-byte is at offset,
	** and the string-data proceeds for that number of bytes after the count.
	** The offset is often zero, but in some struct-objects it will be non-zero.
	*/
	public static String
	fromPStr( byte[] PStrBytes, int offset )
	{
		if ( PStrBytes == null )
			return ( "" );

		int len = 0x00FF & PStrBytes[ offset ];
		return ( new String( PStrBytes, offset + 1, len ) );
	}


}
