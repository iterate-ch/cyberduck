/*
** Copyright 1998, 1999, 2001, 2002 by Gregory L. Guerin.
** Terms of use:
**  - Briefly: OPEN SOURCE under Artistic License -- credit fairly, use freely, alter carefully.
**  - Fully: <http://www.amug.org/~glguerin/sw/artistic-license.html>
*/

package glguerin.io;

import java.io.File;
import java.util.Hashtable;
import java.util.StringTokenizer;


// --- Revision History ---
// 25Apr01 GLG  created for refactoring
// 26Apr01 GLG  add get() to simplify subclassing
// 08May01 GLG  add doc-comments about relative vs. absolute distinctions in parse()
// 16May01 GLG  refactor, adding fromPart() and toPart() methods
// 16May01 GLG  add toString()
// 20May01 GLG  add preliminary support for global name/format mappings
// 21May01 GLG  rework to have asFormatted() and asLiteral()
// 23May01 GLG  call AccentComposer.composeAccents() in _MacOS and _MacOSX imps
// 25May01 GLG  add clearFormats()
// 25Mar2002 GLG  move the concrete imps into separate files
// 25Mar2002 GLG  add knowsFormat(), Not, and related reworking (less attached to Mac imps)
// 28Mar2002 GLG  minor changes integrating back to MacBinary Toolkit


/**
** A PathnameFormat controls how a Pathname is represented as a String,
** and how a Pathname parses String and File arguments into part sequences.
** Every instance of a PathnameFormat must always be callable by any number
** of Pathnames, since a single PathnameFormat is often shared by many Pathnames
** that all parse and format themselves identically.
**<p>
** The base PathnameFormat class provides a global (static) set of mappings between
** names and an arbitrary collection of PathnameFormat instances.  The default mappings
** have names (keys) that match the "os.name" system property of supported OS'es.
** The mappings are filled dynamically, with a static initializer block that loads
** classes by name.  Failure to load a class means that platform won't be supported,
** and will instead have a Not implementation that throws IllegalArgumentExceptions.
**<p>
** Classic Mac OS and Mac OS X are set up as known imps in a static initializer block.
** If the required imp's class is not available, a non-working Not stand-in
** is assigned as the known PathnameFormat.
**<p>
** By default, all other platforms map to the default imp, PF_Simple.
**<p>
** The mapping to a PathnameFormat does not have to be the "os.name" property.
** You can clear the default mappings and create a table using any key you like, such as
** a substring of "os.name", or the concatenation of "os.name" and "os.version", to give
** only two examples.
**<p>
** A PathnameFormat is somewhat like a java.text.Format sub-class, but I've chosen
** not to actually extend Format.  This is partly because Format is overkill, and there's
** essentially no need here to support FieldPosition and ParsePosition classes.
** The other reason is that typically a Pathname holds its own internal reference to
** a PathnameFormat, and uses it when it is asked to assign or append new parts
** from File or String args.  This is not the way that Format sub-classes are typically used.
**<p>
** No methods in this class are synchronized, since no methods depend on any alterable state.
** That is, all the methods either rely on read-only values (final fields),
** or pass all necessary state as arguments to each method.
** This is an intentional design capability, and is required if multiple Pathnames are to
** successfully share a single PathnameFormat.
**
** @author Gregory Guerin
**
** @see Pathname
*/

public class PathnameFormat
{
	/**
	** This static variable should always hold a non-null PathnameFormat representing
	** the platform's local File-compatible format, based on its File.separatorChar.
	** It thus represents the local file-system's conventions, though it exists on all platforms.
	**<p>
	** This variable is initialized using a static initializer that calls getFormatFor()
	** after setting up some known mappings for several known platforms.
	**<p>
	** If you need more precise control, create your own PathnameFormat,
	** or a subclass thereof, then store it here.  That's why this field isn't final.
	*/
	public static PathnameFormat LOCAL_FILE;

	/**
	** Holds mappings established by setFormatFor().
	*/
	private static Hashtable known;

	static
	{
		known = new Hashtable( 11 );

		// These initializers represent known PathnameFormat implementations.
		// If any one of them is unavailable, a Not stand-in instance is substituted for it.
		knowsFormat( "", "glguerin.io.PF_Simple" );
		knowsFormat( "Mac OS",  "glguerin.io.PF_MacOS" );
		knowsFormat( "Mac OS X", "glguerin.io.PF_MacOSX" );

		// The initial value of LOCAL_FILE depends on the above mappings,
		// so it must come last in the static initializer block.
		LOCAL_FILE = getFormatFor( System.getProperty( "os.name", "???" ) );
	}


	/**
	** Try loading the PathnameFormat subclass by name, putting an instance of it
	** into the known formats, or putting an instance of Not in instead if
	** the class is not loadable and instantiable here.
	**<p>
	** This method is called by this class's static initializer block
	** for several known PathnameFormat imps, and the default imp.
	*/
	public static void
	knowsFormat( String keyName, String className )
	{
		PathnameFormat format = null;

		try
		{
			// If any of this throws an exception, the class is unavailable or unusable.
			format = (PathnameFormat) Class.forName( className ).newInstance();
		}
		catch ( Throwable why )
		{
			// The requested class is unavailable, so create a non-working stand-in for it.
			format = new Not( className );
		}

		// Bind the name to the PathnameFormat instance.
		setFormatFor( keyName, format );
	}



	/**
	** Clear all mappings available to getFormatFor().
	*/
	public static void
	clearFormats()
	{  known.clear();  }

	/**
	** Set the mapping between a name and a corresponding PathnameFormat.
	** The name String is typically the value of the "os.name" property, but it can also
	** be a portion of that name, such as the "Windows" substring common to Windows platforms.
	**<p>
	** If the given PathnameFormat is null, the corresponding mapping is removed from the known set.
	**<p>
	** A static initializer in this class establishes initial mappings for the following names:
	**  <br><b>""</b> -- "glguerin.io.PF_Simple"
	**  <br><b>"Mac OS"</b> --  "glguerin.io.PF_MacOS"
	**  <br><b>"Mac OS X"</b> --  "glguerin.io.PF_MacOSX"
	**<p>
	** You can remove, substitute, or supplement these default mappings before calling
	** getFormatFor().
	*/
	public static void
	setFormatFor( String keyName, PathnameFormat format )
	{
		if ( keyName != null )
		{
			if ( format == null )
				known.remove( keyName );
			else
				known.put( keyName, format );
		}
	}

	/**
	** Get a PathnameFormat that has been previously associated with the given keyName.
	** If no mapping has been made for the given PathnameFormat, fall back to the mapping made
	** for the DEFAULT_KEY.  Failing that, return null.
	**<p>
	** A static initializer in this class establishes initial mappings for the following names:
	**  <br><b>""</b> -- "glguerin.io.PF_Simple"
	**  <br><b>"Mac OS"</b> --  "glguerin.io.PF_MacOS"
	**  <br><b>"Mac OS X"</b> --  "glguerin.io.PF_MacOSX"
	*/
	public static PathnameFormat
	getFormatFor( String keyName )
	{
		Object value = known.get( keyName );
		if ( value == null )
			value = known.get( "" );

		return ( (PathnameFormat) value );
	}



	/**
	** For parsing and formatting.
	*/
	private final String separator;

	/**
	** For parsing and formatting.
	*/
	private final boolean hasLeadingSeparator;


	/**
	** Create with given separator-char and leading-separator state.
	*/
	public
	PathnameFormat( char separator, boolean hasLeadingSeparator )
	{
		this.separator = String.valueOf( separator );
		this.hasLeadingSeparator = hasLeadingSeparator;
	}


	/**
	** If this PathnameFormat has a leading separator, then it is returned as a String,
	** otherwise an empty (zero-length) String is returned.
	** Therefore, you can always use a PathnameFormat's toString() as the initial part
	** of a formatted pathname String, and it will correctly represent both
	** the separator character and the presence or absence of a leading separator.
	*/
	public String
	toString()
	{  return ( hasLeadingSeparator() ? getSeparator() : "" );  }

	/**
	** Return the separator as a String.
	** Since the constructor takes only a single char, the returned String always
	** contains only a single character.
	*/
	public String
	getSeparator()
	{  return ( separator );  }

	/**
	** Return whether or not the format() method puts a leading separator on
	** its formatted pathname.
	*/
	public boolean
	hasLeadingSeparator()
	{  return ( hasLeadingSeparator );  }




	/**
	** A convenience method that calls format() using the given Pathname and
	** a new StringBuffer.
	*/
	public String
	format( Pathname path )
	{  return ( format( path, new StringBuffer() ).toString() );  }


	/**
	** Format the Pathname by appending it to the StringBuffer,
	** placing a separator between the parts,
	** and also placing it initially if appropriate.
	** The method getPart() is called for each part of the Pathname.
	**<p>
	** When the Pathname has no parts, the returned StringBuffer 
	** is unchanged when hasLeadingSeparator() is false,
	** or has a single separator char appended when hasLeadingSeparator() is true.
	*/
	public StringBuffer
	format( Pathname path, StringBuffer build )
	{
		if ( path != null  &&  build != null )
		{
			// We'll use the separator char over and over below.
			char between = getSeparator().charAt( 0 );

			if ( hasLeadingSeparator() )
				build.append( between );

			// Use tests within the loop to determine the end of the Pathname.
			// This construction ensures we get the separators between the parts without
			// having to remove a tail-end separator.
			for ( int i = 0;  true;  ++i )
			{
				String part = getPart( path, i );
				if ( part == null )
					break;

				build.append( part );

				if ( getPart( path, i + 1 ) != null )
					build.append( between );
			}
		}
		return ( build );
	}


	/**
	** Parse the path String, appending the parts to the Pathname.
	** The method putPart() is called for each parsed token.
	**<p>
	** This method must decide for itself whether or how to distinguish
	** "relative" vs. "absolute" path Strings.
	** The default implementation here makes no distinction.
	** The effect of this is that path Strings should generally be absolute if one is
	** calling Pathname.setPath(), but may be relative if one is calling parse() directly
	** or calling Pathname.addPath().
	**<p>
	** This implementation uses a StringTokenizer for parsing.
	** Multiple adjacent occurrences of the separator in the path are treated as
	** a single separator.  They do not result in empty or null Pathname parts.
	** Leading separators are treated the same as no leading separators,
	** at least for parsing purposes.
	** If your path semantics require a distinction, you must determine this for
	** yourself in an override, e.g. with String.startsWith().
	*/
	public void
	parse( String path, Pathname build )
	{
		if ( build != null  &&  path != null  &&  path.length() > 0 )
		{
			StringTokenizer parser = new StringTokenizer( path, getSeparator() );
			while ( parser.hasMoreTokens() )
			{  putPart( parser.nextToken(), build );  }
		}
	}



	/**
	** Called by format() to get the given String element from the Pathname.
	** An implementation should get the given String element from the Pathname,
	** perform any transformation on the literal part String,
	** and return a String in a form ready to be appended directly to a StringBuffer.
	** This method is called by format(), and may be overridden in subclasses.
	** The default behavior is to simply call asFormatted( path.part( index ) ).
	**<p>
	** This method must return null when index is beyond the range of the parts.
	** The format() method relies on this.
	*/
	public String
	getPart( Pathname path, int index )
	{  return ( asFormatted( path.part( index ) ) );  }

	/**
	** Called by getPart() to possibly translate a part String from its literal Pathname-form 
	** into a form suitable for format().  The part String may be null, so implementations 
	** must correctly handle this situation by returning null.
	**<p>
	** This default implementation simply returns the unaltered original part String.
	*/
	public String
	asFormatted( String part )
	{  return ( part );  }


	/**
	** Called by parse() to put the parsed String element into the Pathname.
	** An implementation should put the parsed String into the Pathname,
	** optionally interpreting any semantic values that may be significant.
	** This method is called by parse(), and may be overridden in subclasses.
	** The default behavior is to simply call build.add( asLiteral( element ) ),
	** without interpreting semantic values.
	**<p>
	** If this method interprets semantic values of parsed, such as "." or "..",
	** it can call any methods of Pathname it deems appropriate.  
	** For example, interpreting ".." as "parent directory" would result in a call 
	** to cut(), rather than adding ".." as an actual Pathname element.  
	** A corresponding interpretation of "." is to add nothing to the Pathname,
	** since the name "." represents the current directory.
	*/
	public void
	putPart( String parsed, Pathname build )
	{
		// By default, Pathname.add() will ignore null and empty elements itself.
		build.add( asLiteral( parsed ) );
	}

	/**
	** Called by putPart() to possibly translate a parsed String element into a literal form
	** suitable for Pathname.add().  The parsed String may be null, so implementations 
	** must correctly handle this situation by returning null.
	**<p>
	** This default implementation simply returns the unaltered original part String.
	*/
	public String
	asLiteral( String parsed )
	{  return ( parsed );  }



	/**
	** A non-public nested PathnameFormat that throws an IllegalArgumentException
	** from its asLiteral() and asFormatted() methods.  Tus, this PathnameFormat won't
	** work in any Pathname, throwing an IllegalArgumentException to signify that
	** a Pathname does not have a valid working PathnameFormat.
	**<p>
	** This subclass is a non-working stand-in for when
	** a PathnameFormat requested of knowsFormat() isn't actually available.
	** By substituting a Not imp, platforms
	** that require a requested imp won't fall back to the default imp and act stupid or broken.
	** That is, an IllegalArgumentException will be thrown rather than blithely acting as
	** if nothing was wrong.
	**<p>
	** Bindings that have Not imps can be explicitly overridden by calling setFormatFor().
	** Since that can only happen after the PathnameFormat class is loaded, 
	** it only happens after the static initializer tries loading known bindings.
	*/
	protected static final class Not
	  extends PathnameFormat
	{
		/** Fully assembled error message. */
		private final String unavailable;

		/** The separator is "?", no leading separator. */
		protected Not( String unavailableClassName )
		{
			super( '?', false );
			unavailable = "Unavailable PathnameFormat: " + unavailableClassName;
		}

		/**
		** Called by putPart() and parse() before putting the parsed String element into a Pathname.
		** This implementation always throws an IllegalArgumentException,
		** with a message indicating that this is a non-working implementation.
		*/
		public String
		asLiteral( String parsed )
		{  throw new IllegalArgumentException( unavailable );  }

		/**
		** Called by getPart() and format() to get the given String element from literal Pathname form
		** into a form suitable for a formatted String or StringBuffer.
		** This implementation always throws an IllegalArgumentException,
		** with a message indicating that this is a non-working implementation.
		*/
		public String
		asFormatted( String part )
		{  throw new IllegalArgumentException( unavailable );  }
	}

}
