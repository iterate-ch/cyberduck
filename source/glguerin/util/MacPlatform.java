/*
** Copyright 1998, 1999, 2001-2003 by Gregory L. Guerin.
** Terms of use:
**  - Briefly: OPEN SOURCE under Artistic License -- credit fairly, use freely, alter carefully.
**  - Fully: <http://www.amug.org/~glguerin/sw/artistic-license.html>
** This file is from the MacBinary Toolkit for Java:
**   <http://www.amug.org/~glguerin/sw/#macbinary> 
*/

package glguerin.util;

import java.io.UnsupportedEncodingException;
import java.util.Hashtable;
import java.util.StringTokenizer;

import glguerin.io.FileForker;
import glguerin.io.MacRoman;


// --- Revision History ---
// 12Apr99 GLG  create
// 14Apr99 GLG  add isMRJ() -- duh!
// 14Apr99 GLG  change package
// 22Apr99 GLG  add isMacOS() that uses mechanism different from isMRJ()
// 23Apr99 GLG  change package
// 02Jun99 GLG  change package yet again
// 17Aug99 GLG  minor tweaks and fixes
// 24Apr01 GLG  revise isMacOS() and getJDirectVersion() to recognize Mac OS X final
// 27Apr01 GLG  add getOSVersion() and decodeElements()
// 18May01 GLG  move selectFactoryName() et al. here
// 25May01 GLG  add clearFactoryBindings() -- d'uh
// 27May01 GLG  move repairFD() here, where it's visible to wider use
// 04Jun01 GLG  move fromMacRoman() and getOSTypeString() here, revising implementation
// 26Jun01 GLG  add fallback arg to getFactoryBinding()
// 26Jun01 GLG  change how selectFactoryName() looks for bindings
// 02Jul01 GLG  make needsRepair protected and final
// 02Jul01 GLG  FIX: getFactoryBinding() again takes 1 arg, but doesn't do fallback itself
// 05Jul01 GLG  add binding for NineForker
// 05Jul01 GLG  add null/empty check to selectFactoryName()
// 28Mar2002 GLG  move repairFD() to app.util.Fixes
// 28Mar2002 GLG  revise to use glguerin.io.MacRoman
// 08Dec2002 GLG  refactor to call MacRoman.getOSTypeString()
// 09Dec2002 GLG  revise last-resort binding to be "glguerin.io.imp.gen.PlainForker"
// 01May2003 GLG  add error-message explaining failures and fallback in selectFactoryName()
// 01May2003 GLG  change isMRJ() to use "mrj.version" property
// 08May2003 GLG  replace all factory-binding underpinnings with an Implicator.Chain instance
// 09May2003 GLG  revise MacPlatform.Imp to take don't-care on JDirect version
// 09May2003 GLG  add MacOSXForker as known imp, if available
// 12May2003 GLG  resequence Imps so 10.0 always gets TenForker, never MacOSXForker


/**
** MacPlatform contains static methods that return certain information
** about the platform, or perform tasks that support a platform.
** Though this information is often Mac-specific, when the platform is not a Mac,
** safe values are returned indicating "not a Mac".  These may be zero or null.
**<p>
** This is straight Java and contains no Mac-platform dependencies,
** at least in the sense that it should run without crashing or dying on any platform.
** It may throw a SecurityException or other exception, but that's a different question.
**<p>
** This class could have been more accurately named Platform instead of MacPlatform.
** I didn't do that because the methods embodied here all distinguish Mac platforms from
** non-Mac platforms, or distinguish gradations amongst Mac OS platforms.
** Thus, it seemed appropriate to name this MacPlatform.
**
** @author Gregory Guerin
*/

public class MacPlatform
{
	/**
	** This is a limit on name-length on HFS volumes.
	** This is a byte-count, not a character-count.  
	** When multi-byte scripts are used, the character-count will be less than this.
	*/
	public static final int LIMIT_NAME_HFS = 31;


	/**
	** Determine whether the current platform is the Mac OS or not.
	** This method tests whether the "os.name" property's value starts with
	** the String "Mac OS", under a case-sensitive comparison.
	** This is the test appropriate to Mac OS X final release, 
	** which also works on all classic versions of Mac OS.
	**<p>
	** Pre-release versions of Mac OS X had an "os.name" value of "Darwin".
	** That value <b>IS NOT</b> recognized as being Mac OS by this method.
	** That is, this method (as written), will return false on pre-release versions
	** of Mac OS X.  Since you have the source, you can change that if you like.
	**
	** @return  True if this platform is Mac OS according to the "os.name" property, false if not.
	*/
	public static boolean
	isMacOS()
	{
		return ( System.getProperty( "os.name", "???" ).startsWith( "Mac OS" ) );
	}

	/**
	** Decode the "os.version" property's value into an array of char's, one
	** for each element of a dot-delimited sequence.  For example, the
	** value "10.1.2" returns an array of 3 chars having the numeric values
	** 0x0A, 0x01, and 0x02.  Any non-digit characters in the "os.version" property
	** cause the value Character.MIN_VALUE to be stored in the corresponding
	** array element.
	**<p>
	** An array of chars is returned, rather than bytes or ints, because a char[] is
	** easy to turn into a String for performing multi-element comparisons,
	** such as with String.compareTo().
	**
	** @return  An array of chars, one for each element in the "os.version" property.
	*/
	public static char[]
	getOSVersion()
	{
		return ( decodeElements( System.getProperty( "os.version", "0.0" ), "." ) );
	}

	/**
	** Decode the sequence's numeric values into an array of char's, one
	** for each element of the delimited sequence.  For example, the
	** value "10.1.2" with "." for delimiters returns an array of 3 chars having 
	** the numeric values 0x0A, 0x01, and 0x02.  
	**<p>
	** The delimiters comprise a set of delimiter characters, as for StringTokenizer.
	** For example, the value "8.2.4_31" with delimiters "._" will
	** return an array of 4 chars having 
	** the numeric values 0x08, 0x02, 0x04, and 0x1F.
	** All numeric elements are parsed as base-10 (decimal) numbers, so letters will not
	** convert and Character.MIN_VALUE is returned in that element's place.
	**<p>
	** White-space in the sequence is trimmed from each delimited element using String.trim(),
	** but embedded white-space within an element is significant and will thwart numeric conversion.
	** Any remaining non-white, non-digit, non-delimiter characters in the sequence
	** cause the value Character.MIN_VALUE to be stored in the corresponding
	** array element.  Numeric values outside the range of valid Character values are pinned
	** at the appropriate extreme.
	**<p>
	** An array of chars is returned, rather than bytes or ints, because a char[] is
	** easy to turn into a String to perform multi-element comparisons with,
	** i.e. String.compareTo().
	**
	** @return  An array of chars, one for each element in the delimited sequence.
	*/
	public static char[]
	decodeElements( String sequence, String delimiters )
	{
		StringTokenizer parser = new StringTokenizer( sequence, delimiters );
		int count = parser.countTokens();
		char[] elements = new char[ count ];

		for ( int i = 0;  i < count; ++i )
		{
			char value = Character.MIN_VALUE;
			try
			{
				int number = Integer.parseInt( parser.nextToken().trim() );

				if ( number < Character.MIN_VALUE )
					number = Character.MIN_VALUE;

				if ( number > Character.MAX_VALUE )
					number = Character.MAX_VALUE;

				value = (char) number;
			}
			catch ( NumberFormatException ignored )
			{  /* IGNORED since value has correct default if unparseable or out-of-range. */  }

			elements[ i ] = value;
		}

		return ( elements );
	}



	/**
	** Return true on MRJ platforms, false on others,
	** by examining the value of the "mrj.version" property.
	** The property must be a numeric version-number with the first number non-zero.
	**<p>
	** This method used to call getJDirectVersion() to determine "MRJ-ness".
	** That approach won't work on Mac OS X's Java 1.4.1, though, because it doesn't have JDirect.
	** But it's actually still an MRJ platform, so I had to change this implementation.
	*/
	public static boolean
	isMRJ()
	{
		char[] mrjVersion = decodeElements( System.getProperty( "mrj.version", "0.0" ), "." );
		return ( mrjVersion[ 0 ] != 0 );
	}

	/**
	** Determine which version of JDirect is (1, 2, or 3) available, or whether none is available at all.
	** As a rule, the most recent version of JDirect available should be used.
	** When JDirect is unavailable in any form, zero is returned.
	**<p>
	** Note that the Classes looked for are not instantiated -- we only need to know
	** whether the Class itself is present.  If you happen to be running on a non-JDirect platform
	** with certain internal classes present from JDirect, then you could confuse this method
	** into thinking it has JDirect when it really doesn't.  So don't do that.
	**<p>
	** Arguably, <b>ANY</b> Throwable thrown when trying to load a Class
	** should be caught and result in moving to the next trial Class.
	** I chose not to do this because someone might want a SecurityException to
	** be caught at a higher level, and I didn't want to preclude that.
	**<p>
	** Since this method tries to load classes, and the packages may be restricted,
	** this method will throw a SecurityException if the current codebase isn't
	** allowed to access the necessary package.  Applets may encounter this.
	*/
	public static int
	getJDirectVersion()
	{
		// JDirect 3 is needed over JDirect 2, when both are available.
		try
		{
			Class.forName( "com.apple.mrj.jdirect.Linker" );
			return ( 3 );
		}
		catch ( ClassNotFoundException why )
		{ /* FALL THROUGH */ }

		// JDirect 2 is preferred over JDirect 1, when both are available.
		try
		{
			Class.forName( "com.apple.mrj.jdirect.JDirectLinker" );
			return ( 2 );
		}
		catch ( ClassNotFoundException why )
		{  /* FALL THROUGH */  }

		try
		{
			// A JDirect 1 class...
			Class.forName( "com.apple.memory.MemoryObject" );
			return ( 1 );
		}
		catch ( ClassNotFoundException why )
		{  /* FALL THROUGH */  }

		return ( 0 );
	}



	/**
	** Holds Implicators that evaluate the platform and return a classname String.
	*/
	private static Implicator.Chain factoryImps;
	static
	{
		// The FileForker-of-last-resort is assigned to the Implicator.Chain,
		// rather than creating and adding an Implicator for it.
		// One consequence of this is that clear()'ing the chain won't affect this name.
		factoryImps = new Implicator.Chain( "glguerin.io.imp.gen.PlainForker" );
		factoryImps.capacity( 7 );

		// Each of these Implicators will prequalify using the Mac OS version number,
		// and then try to instantiate the FileForker class named.
		// A negative major or minor OS version number means "don't care", so the
		// instantiation alone must succeed.  In all cases, isMacOS() must be true.
		// The last Implicator added is the first one evaluated:
		//   -- on any Mac OS version that can instantiate JD1Forker.
		//   -- on any Mac OS version that can instantiate JD2Forker.
		//   -- on Mac OS 9.*, try using NineForker (uses JDirect-2).
		factoryImps.add( new Imp( -1, -1, "glguerin.io.imp.mac.jd1.JD1Forker" ) );
		factoryImps.add( new Imp( -1, -1, "glguerin.io.imp.mac.jd2.JD2Forker" ) );
		factoryImps.add( new Imp( 9, -1, "glguerin.io.imp.mac.jd2.NineForker" ) );

		// On Mac OS X, MacOSXForker is preferred over TenForker, since it has more features.
		// Except that TenForker is needed on 10.0, because MacOSXForker won't work on 10.0.
		// Because 10.0 evaluates before any 10.* Implicators, it will be implicated first,
		// but only on 10.0.
		//   -- on Mac OS 10.*, try using TenForker (uses JDirect-3).
		//   -- on Mac OS 10.*, try using MacOSXForker (uses JNI, distributed in separate jar).
		//   -- on Mac OS 10.0, try using TenForker (uses JDirect-3).
		factoryImps.add( new Imp( 10, -1, "glguerin.io.imp.mac.ten.TenForker" ) );
		factoryImps.add( new Imp( 10, -1, "glguerin.io.imp.mac.macosx.MacOSXForker" ) );
		factoryImps.add( new Imp( 10, 0, "glguerin.io.imp.mac.ten.TenForker" ) );
	}


	/**
	** Return the Implicator.Chain used by selectFactoryName().
	** The returned object is initialized by a static initializer block in this class.
	**<p>
	** Pass null to evaluate() the Implicator.Chain.
	** None of the provided Implicators uses the passed value.
	**<p>
	** If you want to make any changes to the returned Implicator.Chain, you
	** must make those changes <b>BEFORE</b> calling selectFactoryName()
	** or selectFactoryBinding().  That's because the contents of the Implicator.Chain
	** only affects those two methods.  It has no effect at all on FileForker.SetFactory().
	** That is, <i>selecting</i> the factory and <i>setting</i> the factory are two
	** completely different and distinct operations.
	*/
	public static Implicator.Chain
	getFactoryImplicators()
	{  return ( factoryImps );  }


	/**
	** Select a factory name using getFactoryImplicators().
	** The Implicators held by the Implicator.Chain should return a String
	** that is the name of the appropriate FileForker implementation.
	** The first Implicator to return a non-null Object is the value returned,
	** or the FileForker-of-last-resort assigned to the Implicator.Chain itself.
	**<p>
	** This method actually just needs evaluate()'s returned Object to
	** have a toString() method that yields a class-name.
	** It uses Object.toString() rather than casting to type String.
	*/
	public static String
	selectFactoryBinding()
	{
		// Pass a null Object to evaluate the Implicator.Chain.  No Imps use it or care.
		Object evaluated = getFactoryImplicators().evaluate( null );

		// Be defensive about evaluated Implicator.Chain.
		// Normally won't evaluate() to null, but someone else may have changed the chain.
		if ( evaluated != null )
			return ( evaluated.toString() );

		return ( null );
	}



	/**
	** MacPlatform.Imp is an Implicator subclass that looks for a version of Mac OS,
	** or not, and then loads and instantiates a className to ensure accessibility.
	** It always calls MacPlatform.isMacOS(), and may also call getOSVersion()
	** as prequalifiers before trying to instantiate the named class as a FileForker.
	** The prequalifiers are meant to check for either a specific or wildcard version
	** of the OS, and to prevent loading a class until absolutely necessary.
	*/
	public static class Imp
	  extends Implicator
	{
		private final int major, minor;
		private final String className;

		/** Create.  A negative majorVersion or minorVersion means "don't care". */
		public Imp( int majorVersion, int minorVersion, String className )
		{
			major = majorVersion;
			minor = minorVersion;
			this.className = className;
		}

		/**
		** Evaluate the available Mac OS and JDirect version, and the assigned class's accessibility.
		** Does not use the given Object at all in the evaluation.
		*/
		public Object evaluate( Object given )
		{
//			System.out.println( " in Imp.evaluate() for: " + className );

			// Must be an appropriate version of Mac OS.
			if ( matchOS() )
			{
				// Ultimately, the named class must be instantiable as a FileForker.
				try
				{
					// Try loading the Class, and instantiating it.
					FileForker trial = (FileForker) Class.forName( className ).newInstance();

					// On success, return the classname String.
					return ( className );
				}
				catch ( Throwable why )
				{  /* FALL THROUGH to return null */  }
			}
			return ( null );
		}

		/**
		** Return T only if MacOS, and only if OS version matches major and minor.
		** "Match" includes evaluating the don't-care-ness of major and minor.
		*/
		private boolean
		matchOS()
		{
			// Must be some version of Mac OS.
			if ( MacPlatform.isMacOS() )
			{
				// A negative major value means "don't care" about OS version, so return T.
				if ( major < 0 )
					return ( true );

				// Otherwise we have to evaluate major and/or minor versions.
				// Blindly assume getOSVersion() returns at least 2 elements in array.
				char[] version = MacPlatform.getOSVersion();
				if ( major == version[ 0 ] )
				{
					// Getting here, major versions match, so evaluate minor versions.
					if ( minor < 0  ||  minor == version[ 1 ] )
						return ( true );
				}
			}

			// Default: something didn't match.
			return ( false );
		}

	}



	/**
	** Select a class-name to use as the FileForker factory-name.
	** The property with the designated name is retrieved using System.getProperty().
	** The value of the property should be the fully qualified class name of a FileForker
	** that's usable on the current platform, or it should be null to use the default
	** for the platform.
	** The default FileForker factory is selected by calling selectFactoryBinding().
	**<p>
	** The class represented by the given propName is loaded here using Class.forName().
	** The class is instantiated with Class.newInstance().
	** If the property names a class that can't be loaded, or is otherwise unusable on
	** the current platform, an error message is sent to System.err, and the default
	** for the current platform is returned instead.
	**<p>
	** You can disable this method's error message by setting the boolean system property
	** "glguerin.util.MacPlatform.selectFactoryName.quiet" to "true".
	**
	** @see #selectFactoryBinding
	** @see glguerin.io.FileForker
	*/
	public static String
	selectFactoryName( String propName )
	{
		String factory;
		boolean tell = false;

		// If no usable propName, skip this part.
		if ( propName != null  &&  propName.length() != 0 )
		{
			// If property's value is null or empty, don't attempt to load class.
			factory = System.getProperty( propName, null );
			if ( factory != null  &&  factory.length() != 0 )
			{
				String explanation = "<unknown>";
				try
				{
					// Try loading the Class, and instantiating it.
					FileForker trial = (FileForker) Class.forName( factory ).newInstance();
					return ( factory );
				}
				catch ( Throwable why )
				{  explanation = why.getClass().getName() + ": " + why.getMessage();  }

				// Arrives here if there was an error getting or loading the class.
				// Fall through to selectFactoryBinding(), but only after possibly logging an error.
				// A specific boolean property can disable this message, but it's enabled by default.
				// I''m doing this because of the number of problems understanding
				// this method's result when a desired FileForker imp is unloadable.
				if ( ! Boolean.getBoolean( "glguerin.util.MacPlatform.selectFactoryName.quiet" ) )
				{
					System.out.flush();
					System.err.println( "## MacPlatform.selectFactoryName() failed:" );
					System.err.println( "##   can't load class: " + factory );
					System.err.println( "##   caught exception: " + explanation );
					System.err.println( "## Returning value from selectFactoryBinding():" );
					tell = true;
				}
			}
		}

		// However we got here, we return the default factory binding for the platform.
		factory = selectFactoryBinding();

		// We also tell what was selected if 'tell' is true.
		if ( tell )
		{
			System.err.println( "##   " + factory );
			System.err.flush();
		}

		return ( factory );
	}





	// # # #   M A C - R O M A N   # # #

	/**
	** Returns a UniCode String of 4 chars length, holding the expression
	** of the macOSType as converted from MacRoman-bytes to UniCode.
	**
	** @see glguerin.io.MacRoman#getOSTypeString
	*/
	public static String
	getOSTypeString( int macOSType )
	{  return ( MacRoman.getOSTypeString( macOSType ) );  }

}

