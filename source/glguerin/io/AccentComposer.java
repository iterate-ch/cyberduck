/*
** Copyright 1998, 1999, 2001 by Gregory L. Guerin.
** Terms of use:
**  - Briefly: OPEN SOURCE under Artistic License -- credit fairly, use freely, alter carefully.
**  - Fully: <http://www.amug.org/~glguerin/sw/artistic-license.html>
** This file is from the MacBinary Toolkit for Java:
**   <http://www.amug.org/~glguerin/sw/#macbinary> 
*/

package glguerin.io;

import java.io.*;
import java.util.Hashtable;


// --- Revision History ---
// 23May01 GLG  create, though I'm a bit annoyed that I have to
// 30May01 GLG  minor name changes
// 01Jun01 GLG  add capital-Y dieresis (\u0178)

/**
** An AccentComposer translates from UniCode's combining accent forms into composite characters
** with embedded accents.  This is necessary on Mac OS X and 9, which return the combining accent forms
** as filenames from many places, including File.list() and FileDialog.
**<p>
** The AccentComposer class is both a general-purpose compositer and a Singleton set of default
** composition mappings.  The Singleton is initialized with a set of mappings that covers the typical
** accents one will see in a Roman or Latin-1 situation on Mac OS 9 or X.  It does not cover all possible
** cases for all of UniCode.  However, since you can add additional mappings, or replace the defaults,
** you can easily control how many or which accents will be composited.
**<p>
** The AccentComposer.composeAccents() method is called by the Mac-specific PathnameFormats
** defined in "PathnameFormat.java".  In reality, only Mac OS 9 and X need this, but it's not worth the
** effort to split out Mac OS into pre-9, 9, and X sub-versions.  At least not yet.
**<p>
** Note that there is no AccentDecomposer, and no such capability in this class.
** This is unnecessary for my uses, since the file-system itself happily takes composited characters
** and turns them into combining form.  Good thing, too, because decomposing accented characters
** is more difficult than combining accents and characters together.
**<p>
** Personally, I think it's lame that Java doesn't have a class in "java.text" that performs compositing.
** There's Collator and different ways of comparing accented Strings, but no way to actually translate
** to and fro between the composited and combining forms.
**
** @author Gregory Guerin
*/

public class AccentComposer
{
	private static AccentComposer singleton;

	static
	{
		singleton = new AccentComposer();

		// These initializers cover mainly the ISO Latin-1 accented chars.
		// I've included capital-Y dieresis (\u0178) because it's in MacRoman, so likely to appear on Mac OS X.
		// I've included a few others simply because they're accented vowels.
		singleton.add( '\u0300',		// grave `
			"AaEeIiOoUu",
			"\u00C0\u00E0\u00C8\u00E8\u00CC\u00EC\u00D2\u00F2\u00D9\u00F9" );

		singleton.add( '\u0301',		// acute '
			"AaEeIiOoUuYy",
			"\u00C1\u00E1\u00C9\u00E9\u00CD\u00ED\u00D3\u00F3\u00DA\u00FA\u00DD\u00FD" );

		singleton.add( '\u0302',		// circumflex ^
			"AaEeIiOoUuYy",
			"\u00C2\u00E2\u00CA\u00EA\u00CE\u00EE\u00D4\u00F4\u00DB\u00FB\u0176\u0177" );

		singleton.add( '\u0303',		// tilde ~
			"AaNnOoUu",
			"\u00C3\u00E3\u00D1\u00F1\u00D5\u00F5\u0168\u0169" );

		singleton.add( '\u0308',		// umlaut/dieresis (two dots above)
			"AaEeIiOoUuYy",
			"\u00C4\u00E4\u00CB\u00EB\u00CF\u00EF\u00D6\u00F6\u00DC\u00FC\u0178\u00FF" );

		singleton.add( '\u030A',		// ring above (as in Angstrom)
			"Aa",
			"\u00C5\u00E5" );

		singleton.add( '\u0327',		// cedilla ,
			"Cc",
			"\u00C7\u00E7" );
	}

	/**
	** Return the Singleton AccentComposer that was statically initialized, and
	** which composeAccents() uses for composing accented characters.
	*/
	public static AccentComposer
	getCompositions()
	{  return ( singleton );  }

	/**
	** Return a String with all combining accents eliminated, and all accented characters
	** converted into their composite-accent forms.  Only accents known to the Singleton
	** AccentComposer will be eliminated or composited.  Unknown accents are unaffected.
	**<p>
	** If the given String is null, empty, or contains no known accents, the original String is returned.
	*/
	public static String
	composeAccents( String str )
	{  return ( singleton.compose( str ) );  }


	
	/**
	** The String myAccents is an optimization.
	** On the theory that it's faster and less memory-intensive to scan a String for a char,
	** myAccents holds the accumulated set of all accent chars passed to add().
	** The idea is that compose() can quickly scan a String with each char it examines, and
	** rapidly determine whether a composition is necessary or not.  This scan is presumed
	** to be faster than instantiating a Character to key into a Hashtable and find no element.
	** While it's fairly obvious that a String scan is faster for small to moderate String lengths,
	** long Strings (i.e. lots of accents) will cause the linear scan to dominate.
	** Since this class is not intended to deal with large numbers of different combining accents,
	** this approach should not become a bottleneck.
	*/
	private String myAccents;

	/*
	** Table of composition mappings.
	** Key is a Character representing a combining accent.
	** Value is a String[2] array representing a mapping between
	** an uncombined character in array[0]
	** and its corresponding composited character in array[1].
	**<p>
	** If the accent follows a character NOT listed in array[0], the accent
	** has no effect, i.e. the uncomposited char is unaltered and the accent is discarded.
	** Discarding the accent may not be the best thing to do, but since the idea is to eliminate
	** combining accents, it makes sense.
	** The solution is to add a mapping, not to retain the accent.
	*/
	private Hashtable myComps;


	/**
	** Create.
	*/
	public
	AccentComposer()
	{
		super();
		myAccents = "";
		myComps = new Hashtable();
	}

	/**
	** Clear all mappings.
	*/
	public void
	clear()
	{
		myAccents = "";
		myComps.clear();
	}

	/**
	** Add a mapping for the combining accent.
	** Any existing mapping for the given accent is replaced, so there can be only one mapping per accent.
	**<p>
	** You can't yet remove a mapping.  This is not usually a big problem.
	**<p>
	** Any character can be given as the accent.
	** The pre and post Strings must be non-null and of equal lengths, or the mapping is ignored.
	** It would be unwise for either pre or post to contain any combining accent characters.
	*/
	public void
	add( char accent, String pre, String post )
	{
		if ( pre == null  ||  post == null )
			return;

		if ( pre.length() != post.length() )
			return;
//			throw new IllegalArgumentException( "Unequal lengths: " + pre + ":" + post );

		myAccents = myAccents + accent;
		myComps.put( new Character( accent ), new String[] { pre, post } );
	}



	/**
	** Compose accents in the String.
	** Safely accomodates null, empty, etc.
	*/
	public String
	compose( String str )
	{
		if ( str == null  ||  str.length() == 0 )
			return ( str );

		char[] chars = str.toCharArray();
		int before = str.length();
		int after = compose( chars, 0, before );
		if ( before != after )
			return ( new String( chars, 0, after ) );
		else
			return ( str );
	}


	/**
	** Compose accents in-place in the char[], over the range
	** starting at 'from' and proceeding for 'count' chars.
	** The resulting count of composed chars is returned.
	**<p>
	** Since a composition always results in two chars being combined into one,
	** callers can safely assume that if 'count' and the returned result are identical,
	** no characters were composed.
	*/
	public int
	compose( char[] chars, int offset, int count )
	{
		if ( chars == null  ||  chars.length == 0  ||  offset < 0  ||  count <= 0 )
			return ( 0 );

		String knownAccents = accents();

		// Will need get and put after loop, so declare outside its scope.
		int get = offset, put = offset;
		for ( int end = offset + count;  get < end;  ++get )
		{
			// Optimization: if a char is not in knownAccents, it's a literal char, so put it into place.
			char each = chars[ get ];
			if ( knownAccents.indexOf( each ) < 0 )
			{
				chars[ put++ ] = each;
				continue;
			}

			// Getting here, we have an accent char, so get the mapping String-pair.
			String[] mapping = mapping( each );

			// Translate the prior char only if it's in the 'pre' String.
			// If not, the accent char is discarded altogether and the prior char is unchanged.
			int index = mapping[ 0 ].indexOf( chars[ put - 1 ] );
			if ( index >= 0 )
				chars[ put - 1 ] = mapping[ 1 ].charAt( index );

			// At this point, 'put' is never advanced.
			// If we did a translation, 'put' should not advance because it's already
			// correctly positioned after the just-translated character.
			// If we did NOT do a translation, 'put' should not advance because we
			// want to discard the accent character itself.
		}

		// Getting here, 'get' and 'put' will either be identical or different.
		// If identical, then we did no translations.
		// If different, then we did.
		// Either way, the returned result is 'put - offset'.
		return ( put - offset );
	}


	/**
	** Return a String containing all the accent characters for which a mapping was add()'ed.
	*/
	public String
	accents()
	{  return ( myAccents );  }

	/**
	** Find a mapping for the given accent character, returning a String[], or null if no mapping is known.
	** The String[] always holds exactly two Strings of identical length.
	** The String at [0] is a sequence of accentable characters, i.e. the unaccented character forms.
	** The String at [1] is a sequence of composite accented characters.
	** Each unaccented character in String[0] has a corresponding accented (composite) character
	** at the same position in String[1].
	*/
	public String[]
	mapping( char accent )
	{  return ( (String[]) myComps.get( new Character( accent ) ) );  }

}
