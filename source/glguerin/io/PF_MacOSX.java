/*
** Copyright 1998, 1999, 2001, 2002 by Gregory L. Guerin.
** Terms of use:
**  - Briefly: OPEN SOURCE under Artistic License -- credit fairly, use freely, alter carefully.
**  - Fully: <http://www.amug.org/~glguerin/sw/artistic-license.html>
*/

package glguerin.io;


// --- Revision History ---
// 25Mar2002 GLG  factor out of PathnameFormat source file



/**
** PF_MacOS is a non-public
** PathnameFormat only for use on Mac OS X.
** This PathnameFormat subclass is required for Pathnames to work on Mac OS X, so
** if it's unavailable, Pathnames won't work and an IllegalArgumentException
** will be thrown if you try.  
**<p>
** This subclass represents the Mac OS X platform's File-compatible format.
** Mac OS X interchanges ":" and "/" in Finder-views of names.  That is, what appears as
** an embedded "/" to a user in the Finder will appear as an embedded ":" to a user at
** the shell command-line.  This is how Mac OS X manages to accept "/" in names, just like
** classic Mac OS, while still using "/" as separator in the traditional Unix way.
** Because of this interchange, literal names that contain an embedded "/" must be transformed.
** Likewise, formatted names, including names from the command-line and the OS itself,
** must have ":" transformed into the user-viewable embedded "/" form.
**<p>
** This class also composes accents using AccentComposer, producing more readable names.
** Even if you wanted to see embedded :'s in names, Swing and AWT are both a bit stupid
** when it comes to dealing with decomposed-accents.
*/
public final class PF_MacOSX
  extends PathnameFormat
{
	/** Non-public constructor prevents instantiation outside of package. */
	protected
	PF_MacOSX()
	{  super( '/', true );  }

	/**
	** Called by putPart() and parse() before putting the parsed String element into a Pathname.
	** This implementation transforms embedded ":" into "/",
	** then calls AccentComposer to turn combining accents into composed accented forms.
	** The translation can result in a part String that contains an embedded "/" separator, 
	** but this is precisely the desired result.
	*/
	public String
	asLiteral( String parsed )
	{
		if ( parsed != null  &&  parsed.indexOf( ':' ) >= 0 )
			parsed = parsed.replace( ':', '/' );

		return ( AccentComposer.composeAccents( parsed ) );
	}

	/**
	** Called by getPart() and format() to get the given String element from literal Pathname form
	** into a form suitable for a formatted String or StringBuffer.
	** This implementation transforms the literal Pathname form into a form
	** suitable for use in a path String containing separators.
	*/
	public String
	asFormatted( String part )
	{
		if ( part != null  &&  part.indexOf( '/' ) >= 0 )
			part = part.replace( '/', ':' );

		return ( part );
	}

}

