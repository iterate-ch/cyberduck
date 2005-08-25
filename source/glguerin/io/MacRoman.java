/*
** Copyright 1998, 1999, 2001-2003 by Gregory L. Guerin.
** Terms of use:
**  - Briefly: OPEN SOURCE under Artistic License -- credit fairly, use freely, alter carefully.
**  - Fully: <http://www.amug.org/~glguerin/sw/artistic-license.html>
*/

package glguerin.io;


// --- Revision History ---
// 25Mar2002 GLG  factor into its own class
// 08Dec2002 GLG  move getOSTypeString() here
// 05Feb2003 GLG  add comments on euro-symbol and code-position 0xDB


/**
** MacRoman contains static methods that translate MacRoman bytes
** into Unicode chars using a built-in table.  This is used by other classes that
** need this translation, and can't rely on an encoding-name being present.
**<p>
** As of Mac OS 8.5, the MacRoman charset has
** the euro symbol U+20AC at code position 0xDB.
** <b>This implementation does not follow that convention.</b>
** The symbol at code-position 0xDB is
** the general-purpose currency symbol U+00A4,
** which is what the original (pre-euro) MacRoman charset defined.
**
** @author Gregory Guerin
*/

public class MacRoman
{
	/**
	** This String is used as a MacRoman-to-Unicode translation table.
	** The initializer was mechanically generated, then edited to substitute:
	**<ul>
	**  <li>position 0x0A gets \\n </li>
	**  <li>position 0x0D gets \\r </li>
	**  <li>position 0x22 gets \\" </li>
	**  <li>position 0x5C gets \\\\ </li>
	**</ul>
	** These substitutions are necessary because the Java compiler interprets
	** Unicode escapes before other parsing occurs, and these code-positions blocked compilation.
	**<p>
	** There are other ways to translate MacRoman into Unicode, but using
	** a String constant has the advantage of working even on platforms that
	** lack the "MacRoman" text-encoding handlers.  This approach turns out to be only
	** a few bytes larger than an approach using the "MacRoman" encoding-name.
	*/
	private static final String macRoman =
	  "\u0000\u0001\u0002\u0003\u0004\u0005\u0006\u0007"
	+ "\u0008\u0009\n\u000B\u000C\r\u000E\u000F"
	+ "\u0010\u0011\u0012\u0013\u0014\u0015\u0016\u0017"
	+ "\u0018\u0019\u001A\u001B\u001C\u001D\u001E\u001F"
	+ "\u0020\u0021\"\u0023\u0024\u0025\u0026\u0027"
	+ "\u0028\u0029\u002A\u002B\u002C\u002D\u002E\u002F"
	+ "\u0030\u0031\u0032\u0033\u0034\u0035\u0036\u0037"
	+ "\u0038\u0039\u003A\u003B\u003C\u003D\u003E\u003F"

	+ "\u0040\u0041\u0042\u0043\u0044\u0045\u0046\u0047"
	+ "\u0048\u0049\u004A\u004B\u004C\u004D\u004E\u004F"
	+ "\u0050\u0051\u0052\u0053\u0054\u0055\u0056\u0057"
	+ "\u0058\u0059\u005A\u005B\\\u005D\u005E\u005F"
	+ "\u0060\u0061\u0062\u0063\u0064\u0065\u0066\u0067"
	+ "\u0068\u0069\u006A\u006B\u006C\u006D\u006E\u006F"
	+ "\u0070\u0071\u0072\u0073\u0074\u0075\u0076\u0077"
	+ "\u0078\u0079\u007A\u007B\u007C\u007D\u007E\u007F"

	// 0x80
	+ "\u00C4\u00C5\u00C7\u00C9\u00D1\u00D6\u00DC\u00E1"
	+ "\u00E0\u00E2\u00E4\u00E3\u00E5\u00E7\u00E9\u00E8"
	// 0x90
	+ "\u00EA\u00EB\u00ED\u00EC\u00EE\u00EF\u00F1\u00F3"
	+ "\u00F2\u00F4\u00F6\u00F5\u00FA\u00F9\u00FB\u00FC"
	// 0xA0
	+ "\u2020\u00B0\u00A2\u00A3\u00A7\u2022\u00B6\u00DF"
	+ "\u00AE\u00A9\u2122\u00B4\u00A8\u2260\u00C6\u00D8"
	// 0xB0
	+ "\u221E\u00B1\u2264\u2265\u00A5\u00B5\u2202\u2211"
	+ "\u220F\u03C0\u222B\u00AA\u00BA\u2126\u00E6\u00F8"

	// 0xC0
	+ "\u00BF\u00A1\u00AC\u221A\u0192\u2248\u2206\u00AB"
	+ "\u00BB\u2026\u00A0\u00C0\u00C3\u00D5\u0152\u0153"
	// 0xD0
	+ "\u2013\u2014\u201C\u201D\u2018\u2019\u00F7\u25CA"
	+ "\u00FF\u0178\u2044\u00A4\u2039\u203A\uFB01\uFB02"
	// 0xE0
	+ "\u2021\u00B7\u201A\u201E\u2030\u00C2\u00CA\u00C1"
	+ "\u00CB\u00C8\u00CD\u00CE\u00CF\u00CC\u00D3\u00D4"
	// 0xF0
	+ "\uF8FF\u00D2\u00DA\u00DB\u00D9\u0131\u02C6\u02DC"
	+ "\u00AF\u02D8\u02D9\u02DA\u00B8\u02DD\u02DB\u02C7";


	/**
	** Interpret the low 8-bits of value as a MacRoman-encoded byte, returning its Unicode char.
	** The other bits of value have no effect, since they are masked off.
	**
	** @return  The Unicode representation of the low 8-bits of value interpreted as a MacRoman byte.
	*/
	public static char
	fromMacRoman( int value )
	{  return ( macRoman.charAt( 0x0FF & value ) );  }


	/**
	** Returns a UniCode String of 4 chars length, holding the expression
	** of the macOSType as converted from MacRoman-bytes to UniCode.
	*/
	public static String
	getOSTypeString( int macOSType )
	{
		// Use the fromMacRoman() method.
		char[] chars = new char[ 4 ];
		chars[ 3 ] = fromMacRoman( macOSType );
		chars[ 2 ] = fromMacRoman( macOSType >> 8 );
		chars[ 1 ] = fromMacRoman( macOSType >> 16 );
		chars[ 0 ] = fromMacRoman( macOSType >> 24 );

		return ( new String( chars ) );
	}

}
