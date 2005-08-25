/*
** Copyright 1998, 1999, 2001, 2002 by Gregory L. Guerin.
** Terms of use:
**  - Briefly: OPEN SOURCE under Artistic License -- credit fairly, use freely, alter carefully.
**  - Fully: <http://www.amug.org/~glguerin/sw/artistic-license.html>
*/

package glguerin.io;


// --- Revision History ---
// 25Mar2002 GLG  refactor from PathnameFormat source file and MacEscaping class


/**
** PF_MacOS is a non-public
** PathnameFormat only for use on classic Mac OS (before Mac OS X).
** This PathnameFormat subclass is required for Pathnames to work on Mac OS, so
** if it's unavailable, Pathnames won't work and an IllegalArgumentException
** will be thrown if you try.  
**<p>
** Although the class itself is public, its constructor is not.
** That means anyone can call the public static methods, but only package-mates
** can instantiate it.  That precisely suits the needs of PathnameFormat's static
** initializer block that knows about this imp.
**<p>
** This subclass represents the classic Mac OS platform's File-compatible format.
** Classic Mac OS represents file-names in MRJ using an escaped form for names that
** have an embedded "/" or "%".  This form is a historical design artifact that only
** applies on classic Mac OS, and does not apply on Mac OS X.  The code that handles
** the escaping and unescaping was previously in the class MacEscaping.  That code is
** now here, because here is the only place it's now relevant.
**<p>
** This class also composes accents using AccentComposer, producing more readable names. 
**<p>
** This class overrides asLiteral() and asFormatted() from PathnameFormat.
** It also provides static utility methods for working directly with escaped
** and unescaped Mac OS file-names, those methods previously being in the MacEscaping class.
** Those methods translate between the escaped (i.e. classic MRJ form)
** and unescaped (i.e. raw) forms of names.  They do not translate from
** the Java/MRJ form where "/" is a separator, to the native-Mac form
** where ":" is a separator (and different semantics also exist).
** Escaping is used under classic MRJ because the Mac OS allows /'s in its file-names,
** and at the same time uses "/" as the File.separator to represent path-names.
** Indeed, Mac OS allows any character in a file-name except the native ":" separator.
**<p>
** The reason behind the MRJ escaped-name design arises out of Java's
** limited and Unix-centered view of file-systems.  
** Though java.io.File is intended to hide these, it doesn't entirely succeed.
** At the very least, the programmers using File were very Unix-centric in their use of File, 
** which leads to the same result -- things that should be abstract and hidden aren't:
**<ol>
**   <li>The semantics of relative vs. absolute paths is Unix-centric, and
**     exactly opposite to the Mac OS's native semantics.  That is, under Unix an absolute
**     path starts with File.separator, but under Mac OS that syntax means a relative path.
**   </li>
**   <li>The meaning of repeated adjacent File.separator's is presumed to follow
**     the Unix-centered approach, i.e. repeats are ignored.  This is not true of Mac OS.
**   </li>
**   <li>Named references to "this directory" and "parent directory" are presumed
**     to be "." and "..".  This is not true of Mac OS, where the native separator ":"
**     is used for this purpose.
**   </li>
**   <li>File-system names are presumed to be legal if they start with ".".
**     Mac OS used to forbid this, and many existing programs are still unable to
**     handle this correctly.  Since Mac OS 7, it's not illegal, though this has not fixed
**     the programs (or programmers) who still do things poorly.  Not an issue for new
**     code, until you throw an old broken program into the work-flow.  Oops.
**</ol>
** And this doesn't even cover other tacit assumptions, such as case-sensitivity (or not),
** name length limits (both as name elements and for the overall pathname),
** accented-character representations (composed vs. decomposed),
** and whether disk-volumes have unique names or not.
**<p>
** To avoid some of the difficulties from all these faulty or tacit assumptions,
** MRJ's creators chose to just provide Unix-like semantics as much as possible.
** This includes the use of "/" as separator, the use of "." and ".." as directory-names, 
** and various other aspects.  None of these concepts or semantics are natively Mac,
** despite being the "native MRJ" representation.
**<p>
** One difficulty with this scheme is that a literal "/" is not uncommon in native Mac OS file-names.
** So MRJ uses an escaping scheme similar to the HTTP URL scheme, i.e. the character '%'
** introduces a two-hex-digit escape encoding the character-value.
** In MRJ 2.0 and earlier, some ASCII characters were escaped in addition to "/" and "%" themselves.
** Also, MacRoman extended characters (i.e. accented and special chars)
** were escaped as their MacRoman byte-values rather than
** converted to Unicode chars, 
** e.g. %83 represents <b>capital-E acute</b>, not a Unicode control-char.
** Under MRJ 2.1 and later, only "/" and "%" are escaped, and MacRoman characters are
** translated to their Unicode characters using the special-purpose MacRoman class.
**<p>
** Note that these MRJ escapes are all Java-only constructs, and appear only to Java programs running
** under MRJ.  Calls to native Mac OS code still require the Mac-format path-name without escapes.
** Users also expect to see a file named "4/99 Budget" as exactly that, not "4%2f99 Budget",
** so handling escapes is an important user-visible feature, too.
**<p>
** Mercifully, all this wackiness is now hidden by PathnameFormat, so Pathname.part() will
** return "4/99 Budget" if that's what the filename looks like in unescaped form.
** Even more mercifully, almost all this wackiness disappears in Mac OS X.
** The one remaining anomaly is that what the user perceives as embedded "/" in a
** file-name is transposed with ":" to Unix programs.  And the JVM is effectively a Unix program,
** so that's what Java normally sees.  Oh, and Mac OS X presents decomposed accents.
** Still, a simple "/"-to-":" transposition and a bit of accent-composing is much simpler
** to deal with than all this escaping stuff.  Which is why PF_MacOSX is so much simpler
** than PF_MacOS.
**
** @see MacRoman
** @see PF_MacOSX
*/

public final class PF_MacOS
  extends PathnameFormat
{
	/**  MRJ's file-separator as Java-code sees it, '/'. */
	public static final char SEP = '/';

	/**  The MRJ escaping character, '%'.*/
	public static final char ESCAPE = '%';

	/** String to replace literal instances of SEP with: "%2F". */
	private static final String SEP_SUB = "%2F";

	/** String to replace literal instances of ESCAPE with: "%25". */
	private static final String ESCAPE_SUB = "%25";



	/** Non-public constructor prevents instantiation outside of package. */
	protected
	PF_MacOS()
	{  super( SEP, true );  }

	/**
	** Called by putPart() and parse() before putting the parsed String element into a Pathname.
	** This implementation transforms an escaped form into a literal unescaped form,
	** then calls AccentComposer to turn combining accents into composed accented forms.
	** The de-escaping process
	** can result in a part String that contains an embedded "/" separator, but this is precisely
	** the desired result.
	*/
	public String
	asLiteral( String parsed )
	{  return ( AccentComposer.composeAccents( toLiteralName( parsed ) ) );  }

	/**
	** Called by getPart() and format() to get the given String element from literal Pathname form
	** into a form suitable for a formatted String or StringBuffer.
	** This implementation transforms the literal Pathname form into an escaped form
	** suitable for use in a path String containing separators.
	*/
	public String
	asFormatted( String part )
	{  return ( toEscapedName( part ) );  }




	/**
	** Determine whether the given String contains any characters that will need
	** escaping or conversion in order to be used as a Mac file-name part.
	** Returns true if if the given String contains characters which need to be
	** escaped on the Mac, false if it contains no such characters.
	** If the given String is null, then false is returned without throwing an exception.
	**<p>
	** This method does not identify the Mac's native separator ":" as being an escapee.
	** This is intentional.
	*/
	public static boolean
	containsMacEscapees( String given )
	{
		if ( given == null  ||  given.length() == 0 )
			return ( false );

		if ( given.indexOf( ESCAPE ) >= 0 )
			return ( true );

		if ( given.indexOf( SEP ) >= 0 )
			return ( true );

		return ( false );
	}


	/**
	** If the given String, a file-name part, contains characters which must
	** be escaped on the Mac, return the escaped form, otherwise return the original String.
	** The given literalName <b>IS NOT</b> a full path-name, only a single part thereof.
	** Separators appearing in literalName are treated as literal characters, not separators,
	** and will be escaped.  This is the whole reason for having escaped names anyway.
	**<p>
	** This method only performs escaping, not encoding from MacRoman or other charsets.
	**<p>
	** If the given String is null, then null is returned without throwing an exception.
	*/
	public static String
	toEscapedName( String literalName )
	{
		if ( ! containsMacEscapees( literalName ) )
			return ( literalName );

		char[] chars = literalName.toCharArray();
		StringBuffer build = new StringBuffer( chars.length );

		for ( int i = 0;  i < chars.length;  ++i )
		{
			int it = chars[ i ];
			if ( it == ESCAPE )
				build.append( ESCAPE_SUB );
			else if ( it == SEP )
				build.append( SEP_SUB );
			else
				build.append( (char) it );
		}
		return ( build.toString() );
	}


	/**
	** Return a String with Mac-escaping removed.
	** If there are no escape-sequences (no '%' chars) at all,  
	** the original String is immediately returned.
	** This method only performs unescaping, not encoding.
	** If the given String is null, then null is returned without throwing an exception.
	**<p>
	** Escapes in the range %80-%FF will be converted as if they represent MacRoman bytes.
	** For example, %83 is converted to Unicode \u00C9 (capital-E acute), rather than to
	** the direct Unicode mapping \u0083 (a control-char).
	** The reason for this intepretation of escapes is that MRJ 2.0 and earlier represent
	** file-name characters in the range 0x80-0xFF as escapes, not as Unicode chars.
	** Since these escapes typically represent a literal MacRoman byte, it would be
	** improper to interpret them as Unicode-encoded escapes.
	**<p>
	** If an apparent escape-sequence can't be decoded, it's kept as-is in the literal form.
	** For example, "%fr" is not valid, because "fr" is not a hex value.
	** The result is that "%fr" is copied verbatim to the result.
	** If there aren't 2 chars after a'%', the sequence is also copied verbatim to the result.
	*/
	public static String
	toLiteralName( String escapedName )
	{
		// If no escapes present, return original.
		if ( escapedName == null  ||  escapedName.indexOf( ESCAPE ) < 0 )
			return ( escapedName );

		char[] chars = escapedName.toCharArray();
		StringBuffer build = new StringBuffer( escapedName.length() );
		for ( int i = 0;  i < chars.length;  ++i )
		{
			int it = chars[ i ];
			if ( it == ESCAPE  &&  i + 2 < chars.length )
			{
				// Only two-hex-digit escapes are supported.
				// Always interpret the escaped 8-bit value as MacRoman.
				int hi = Character.digit( chars[ i + 1 ], 16 );
				int lo = Character.digit( chars[ i + 2 ], 16 );
				if ( hi >= 0  &&  lo >= 0 )
				{  it = MacRoman.fromMacRoman( (hi << 4) + lo );  i += 2;  }
			}
			// At this point, it is a char to append.
			// It's either the original literal char, or the decoded escape-sequence.
			// If it's been decoded, then i has also been moved along as needed.
			build.append( (char) it );
		}

		return ( build.toString() );
	}

}
