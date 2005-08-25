/*
** Written by Gregory L. Guerin, April 2002.
** Freely released AS-IS AND WITHOUT WARRANTY OF ANY KIND into the public domain.
*/

package glguerin.util;

import java.lang.reflect.*;
import java.io.*;


// --- Revision History ---
// 29Apr2002 GLG  create a work-around for Cocoa-Java's obstinate JNI-lib stupidity
// 02May2002 GLG  revise how getTinker() decides which imp to return
// 02May2002 GLG  copy to tenjni package
// 03May2002 GLG  revise to keep a thread-safe singleton
// 21May2002 GLG  name changes
// 05Nov2002 GLG  rework substantially, calling NSBundle with reflection
// 18Nov2002 GLG  add diagnostics to System.err, and null-File checks
// 05Dec2002 GLG  revise how property is used in makeLoader()
// 10Dec2002 GLG  change package and properties


/**
** A LibLoader encapsulates different ways to call System.load() or loadLibrary(),
** using one or more fallback locations as places to look for JNI libraries.
** This implementation is specifically designed for use with Cocoa-Java, which won't
** normally search for JNI libs in the Contents/Resources/Java section of an app-bundle.
**<p>
** Although designed for Cocoa-Java, it will work in any situation, since it relies first
** on System.loadLibrary(), and only after that fails does it resort to any fallback strategies.
** Also, this class will work on non-Mac platforms, because it always uses reflection to
** refer to any Mac-specific classes and methods.
** This makes the code more difficult to understand, but is much safer. 
** Reflection is only used in LibLoader's constructor.  Thereafter, a File is used, so there
** is no ongoing performance cost for the fact that reflection is used.
**<p>
** In general, call the static method getLoader() or makeLoader(),
** rather than instantiating a new LibLoader directly.
** Calling getLoader() will instantiate a LibLoader as needed,
** managing a private singleton thread-safely.
** Calling makeLoader() always makes a new LibLoader.
** In either case, the class instantiated may be controlled with
** the "glguerin.util.LibLoader.imp" property, which should be a fully qualified class name
** of a concrete LibLoader implementation.  Normally, you won't have to set the property.
**<p>
** The principal method in LibLoader is loadLibrary().
** It calls System.loadLibrary() first, then its own fallback load() method if that fails.
** Only when System.loadLibrary() fails do any fallback strategies come into play.
** If there are no fallbacks in place, or none are necessary,
** then LibLoader.loadLibrary() is equivalent to System.loadLibrary().
**<p>
** Properties used:
**  <br><b>glguerin.util.LibLoader.imp</b> -- 
**    The fully qualified class name of a LibLoader implementation used by makeLoader(),
**    which is also called by getLoader().
**  <br><b>glguerin.util.LibLoader.debug</b> -- 
**    A boolean ("true" or "false") controls whether diagnostics are printed to System.err
**    (the stderr stream) by load() and makeFallbackDir().  Default is false.
**
** @author Gregory Guerin
*/

public class LibLoader
{
	/**
	** Set this flag to T for all LibLoader's to be verbose on System.err stream.
	** The flag is initialized from the property "glguerin.util.LibLoader.debug",
	** but can be changed programmatically, as well.
	*/
	public static boolean isVerbose = Boolean.getBoolean( "glguerin.util.LibLoader.debug" );


	/** Singleton used and managed by getLoader(). */
	private static LibLoader knownLoader;


	/**
	** Get a LibLoader appropriate to the configuration.
	** It will reuse a known singleton, or make one if no singleton is known.
	*/
	public static synchronized LibLoader
	getLoader()
	{
		if ( knownLoader == null )
		{
			knownLoader = makeLoader();
//			System.err.println( "# LibLoader: " + knownLoader.getClass() + ", using " + knownLoader );
		}
		return ( knownLoader );
	}


	/**
	** Make a LibLoader appropriate to the configuration.
	** Not synchronized because it doesn't refer to any static variables.
	*/
	public static LibLoader
	makeLoader()
	{
		// Look for a system property overtly specifying a class name.
		// Absent such a property, the default is to use this LibLoader, which has a
		// fallback strategy that can call a Cocoa NSBundle using reflection.
		String className = System.getProperty( "glguerin.util.LibLoader.imp", "" );

		if ( className != null  &&  className.length() > 0 )
		{
			try
			{
				// If any of this throws any kind of exception, return a vanilla LibLoader instead.
				return ( (LibLoader) Class.forName( className ).newInstance() );
			}
			catch ( ThreadDeath mustRethrow )
			{  throw mustRethrow;  }
			catch ( Throwable everythingElse )
			{  /* FALL THROUGH */  }
		}

		// Return a vanilla LibLoader instance, hoping for the best.
		return ( new LibLoader() );
	}




	/**
	** Passed to load(String,File) as location to look in for fallback strategy.
	** This may be null if makeFallbackDir() returns null.
	*/
	protected final File dirFallback;

	/** A default LibLoader has a fallback strategy appropriate for Cocoa-Java on Mac OS X. */
	public
	LibLoader()
	{
		dirFallback = makeFallbackDir();
		blab();
	}

	/** Called from constructor, an overridable diagnostic emitting dirFallback. */
	protected void
	blab()
	{
		if ( isVerbose )
			System.err.println( "# LibLoader dir: " + dirFallback );
	}
	

	/**
	** Load the library, using normal and fallback strategies.
	** May be overridden in subclasses.
	** The fallback strategies may be altered by overriding load().
	** The load() method should return normally on success, or
	** throw an UnsatisfiedLinkError on failure.
	**  @see #load
	*/
	public void
	loadLibrary( String libName )
	{
		// We're told not to catch Errors, but there's no other way to find out if a linkage failed.
		// Failures other than UnsatisfiedLinkError get thrown up to caller.
		try
		{  System.loadLibrary( libName );  }
		catch ( UnsatisfiedLinkError failure )
		{  this.load( libName, dirFallback );  }  // use fallback scheme.
	}

	/**
	** Try to load a JNI library from a fallback location represented as a directory File.
	** Return normally on success, or throw an UnsatisfiedLinkError on failure.
	** May be overridden in subclasses.
	**<p>
	** The File may be null, which this method may elect to handle or throw an UnsatisfiedLinkError.
	** This imp throws an UnsatisfiedLinkError when File is null.
	**<p>
	** This imp takes a File designating a directory location, and a hand-made lib-name
	** that follows the Mac OS X convention for JNI library names:  "Foo" loads "libFoo.jnilib".
	** It then calls System.load() to load the resulting absolute pathname.
	** For other platforms or situations, override this method and assemble a library-name
	** under your target platform's conventions.
	**
	** @exception java.lang.UnsatisfiedLinkError
	**  thrown when the named library could not be loaded.
	**  Any implementation of this method must throw this error on failure.
	*/
	protected void
	load( String libName, File location )
	{
		if ( location == null )
			throw new UnsatisfiedLinkError( "Null location to load JNI library: " + libName );

		// This hand-made scheme follows the usual Mac OS X Java JNI-lib naming pattern.
		// Under Java 2 it could use System.mapLibraryName().
		String libPath = new File( location, "lib" + libName + ".jnilib" ).getAbsolutePath();

		if ( isVerbose )
			System.err.println( "# LibLoader: trying " + libPath );

		System.load( libPath );
	}


	/**
	** Create a File representing a directory from which load(String,File) will load JNI libs.
	** May return null if there is no fallback directory in which to look for JNI libs.
	** Called from constructor to get a File, assigning it to dirFallback.
	**<p>
	** This imp uses reflection to attempt to load a Cocoa NSBundle class.
	** On success, it returns a File referring to the "Java" sub-dir of the bundle's resources dir.
	** On failure, it returns null so load(String,File) will fail.
	*/
	protected File
	makeFallbackDir()
	{
		try
		{
			// Both mainBundle() and resourcePath() take no args.
			// The empty Class[] represents the method signature.
			// The empty Object[] represents the invoked method's args.
			Class[] noArgs = new Class[ 0 ];
			Object[] nothing = new Object[ 0 ];

			// Try to load an NSBundle class and reflectively invoke its methods.
			// NSBundle.mainBundle() is a public static method returning an NSBundle.
			// NSBundle.resourcePath() is a public instance method called on the NSBundle instance.
			Class bundleClass = Class.forName( "com.apple.cocoa.foundation.NSBundle" );

			if ( isVerbose )
				System.err.println( "# LibLoader got NSBundle: " + bundleClass );

			// First, get the NSBundle.  A static invocation doesn't care what the target object is.
			// Second, use the NSBundle to invoke the bundlePath() method.
			Object theNSBundle = bundleClass.getMethod( "mainBundle", noArgs ).invoke( null, nothing );
			Object thePath = bundleClass.getMethod( "resourcePath", noArgs ).invoke( theNSBundle, nothing );

			// The resultant value of thePath should be a String holding a full pathname.
			// Don't cast it, just use its toString() method to assemble an appropriate File.
			return ( new File( thePath.toString(), "Java" ) );
		}
		catch ( ClassNotFoundException why )
		{  /* FALL THRU */  }
		catch ( NoSuchMethodException why )
		{  /* FALL THRU */  }
		catch ( IllegalAccessException why )
		{  /* FALL THRU */  }
		catch ( IllegalArgumentException why )
		{  /* FALL THRU */  }
		catch ( InvocationTargetException why )
		{  /* FALL THRU */  }

		// Here's where a "last-chance directory" can be returned.
		// One option is the "user.dir" property, or ".".
		// There are security implications to using the current directory as the location of native-code libraries.
		// Another slightly safer option would be the "java.home" property, or some location therein.
		// I've avoided all those in favor of simply returning null, so load() will fail
		// if there's no NSBundle available.
		return ( null );
	}


	/** Return a String telling something about how libs are loaded. */
	public String
	toString()
	{  return ( "System.loadLibrary()" + File.pathSeparator + dirFallback );  }

}
