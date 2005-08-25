/*
** Copyright 1998, 1999, 2001-2003 by Gregory L. Guerin.
** Terms of use:
**  - Briefly: OPEN SOURCE under Artistic License -- credit fairly, use freely, alter carefully.
**  - Fully: <http://www.amug.org/~glguerin/sw/artistic-license.html>
** This file is from the MacBinary Toolkit for Java:
**   <http://www.amug.org/~glguerin/sw/#macbinary> 
*/

package glguerin.io;

import java.io.*;


// --- Revision History ---
// 23Mar99 GLG  create based on redesign of now-deceased MacFileMunger
// 24Mar99 GLG  rework static-factory methods
// 24Mar99 GLG  add resolved()
// 25Mar99 GLG  add setLock()
// 29Mar99 GLG  add support for withComment arg to two methods
// 30Mar99 GLG  rework comment support, removing withComment arg from setCatalogInfo()
// 30Mar99 GLG  add selfResolve()
// 01Apr99 GLG  tweak some names
// 02Apr99 GLG  expand doc-comments
// 05Apr99 GLG  add comments on when SecurityManager is called
// 06Apr99 GLG  add comments that setLock() isn't called by setCatalogInfo()
// 06Apr99 GLG  add null and empty checks to SetFactoryName()
// 13Apr99 GLG  add abstract determined() method
// 18Apr99 GLG  doc-comments
// 20Apr99 GLG  more doc-comments
// 26Apr99 GLG  cover some name changes
// 04May99 GLG  add exists(), isFile(), isDirectory(), isAlias()
// 05May99 GLG  add factoryClass as cache
// 05May99 GLG  add replicateTo() and replicateFork()
// 06May99 GLG  add 'replace' arg to replicateTo()
// 07May99 GLG  minor tweaks and editing
// 07May99 GLG  add canRead(), canWrite(), delete(), renameLeaf(), moveTo()
// 08May99 GLG  revise moveTo()'s behavior
// 09May99 GLG  doc-comment revs
// 10May99 GLG  make replicateTo() return MacFilePathname as convenience
// 26Apr01 GLG  remove DEFAULT_FACTORY String
// 26Apr01 GLG  revise MakeOne() to throw IllegalArgumentException on failures
// 26Apr01 GLG  revise getMacPath() into two forms of getInternalPath()
// 03May01 GLG  convert to use Pathname
// 04May01 GLG  revise to give writeEntireFork( boolean, RandomRW, byte[] )
// 08May01 GLG  eliminate arg to MakeOne(), revise factory-name methods
// 10May01 GLG  add getNameLimit() -- D'UH!
// 10May01 GLG  change package and class name
// 17May01 GLG  remove determined() -- redundant: identical to makeResolved()
// 17May01 GLG  provide default imp of selfResolve()
// 18May01 GLG  add semantic element info to makeResolved() and selfResolve() doc-comments
// 21May01 GLG  remove direct escaping support
// 21May01 GLG  make replicateTo() take byte[] arg instead of buffer-size
// 22May01 GLG  add isHidden()
// 25May01 GLG  add list(FilenameFilter)
// 30May01 GLG  move replicateTo() et al. to FileTools
// 31May01 GLG  add length(boolean)
// 05Jun01 GLG  revise get/setCommentBytes() to get/setComment()
// 06Jun01 GLG  add setDefaultTypes(), getDefaultFileType(), getDefaultFileCreator()
// 15Jun01 GLG  add abstract getFileAccess(), setFileAccess()
// 16Jun01 GLG  cut setLock(), merging it into setFileAccess() boolean barnacle, er, I mean arg
// 19Jun01 GLG  cut getInternalPath()
// 25Jun01 GLG  add makeDir() and makeDirs()
// 11Dec01 GLG  add to setFileAccess() comments
// 07May2002 GLG  refactor to add SetFactoryClass()
// 09May2002 GLG  eliminate actualTarget(), add replacements: getPathname(), usePathname()
// 09May2002 GLG  add replacements: getPathReplica(), setPathReplica()
// 04Dec2002 GLG  add nested Alias class and makeAlias() method
// 05Dec2002 GLG  add createAliasFile() to FileForker
// 09Dec2002 GLG  add exception details in SetFactory(): class-name of nested Throwable
// 10Dec2002 GLG  add Alias.getAliasType() and some named constants
// 11Dec2002 GLG  add boolean arg & return-value to createAliasFile()
// 11Dec2002 GLG  add isSymlink() with default imp, since we can now create them with createAliasFile()
// 11Dec2002 GLG  cut IOExceptions thrown from Alias's methods
// 13Dec2002 GLG  change default isSymlink() to always return false (duh); edit doc-comments
// 13Dec2002 GLG  add Alias.getCapabilities(), Alias.hasCapability(), and Alias.CAN_XXX masks
// 01Jan2003 GLG  add Alias.update() and CAN_UPDATE capability mask
// 06Jan2003 GLG  add signalChange()
// 09Jan2003 GLG  add nested Watcher class and makeWatcher() method
// 17Jan2003 GLG  add resolveLeading()
// 22Jan2003 GLG  edit doc-comments
// 24Jan2003 GLG  cut Alias.update() facility for now
// 04Feb2003 GLG  revise to use Pathname.replica()
// 05Feb2003 GLG  change setPathReplica() to assign null if given Pathname is null
// 01May2003 GLG  revise text in IllegalArgumentExceptions thrown by factory-mgmt methods


/**
** FileForker is an abstract class with facilities to supplement
** or replace the java.io.File methods which refer to the file-system.
** It uses a Pathname to hold the pathname of the target file or directory.
**<p>
** An important difference between the design of a FileForker and a File is that a
** FileForker instance is designed to be mutable, while a File is not.
** A single FileForker instance can be used to refer to different files and directories
** simply by changing its pathname.  Even the Pathname used by a FileForker is mutable,
** and is easy to change in its entirety or part by part.
** This mutability has benefits and shortcomings, but on balance it made more sense that way.
** It may feel strange to be reusing a single FileForker over and over, especially if
** you've become accustomed to using new File() frequently.
** The change will be worth it, though.
**<p>
** The FileForker class is the focal point for all kinds of Mac-oriented file-system additions -- 
** file-info, file comments, alias-resolving, alias-creation, and fork access.
** You can use a concrete FileForker for almost any action on the file-system,
** frequently as a replacement for using a File.  If you can't, it's easy to instantiate
** a File using the FileForker's pathname, conveniently returned from FileForker.toString().
**<p>
** A FileForker replicates many of the methods that File provides,
** mainly because it's easier than instantiating a new File for such 
** trivialities as existence, writability, etc.
** Some non-trivial methods such as moving, renaming, and deletion are also
** provided because here they can operate without resolving aliases, and also take care
** of the details that a specific implementation might need, yet which a mere File would omit.
**<p>
** On non-Mac platforms, all Mac-specific abstractions will be translated into concrete actions
** on the file-system of the running platform.  This translation is the responsibility of a concrete
** sub-class of FileForker that you, gentle reader, may have to provide (but don't panic yet -- read on).
**<p>
** A java.io.File represents a moderately cross-platform but rather impoverished view of a file-system.
** Some important aspects may be entirely absent, 
** such as creation-date in JDK 1.1,
** or a particular platform's name-length or path-length limit (not even on JDK 1.4).
** Other aspects may be adequate but piecemeal.
** For example, you can get the length of a file-system item with File.length(), but you
** can get that value and much more from FileForker.getFileInfo(boolean) in a single method-call.
** And of course, many useful or critical things are completely missing from File,
** such as access-permissions, ownership ID's, and whether something is a symlink or not.
**<p>
** The purely syntactic name-manipulation capabilities of java.io.File have been
** separated out into the distinct and far more capable <b>Pathname</b> class.
** A Pathname operates on name-strings according to certain rules
** without actually referring to any underlying file-system.  
** That is, a Pathname is solely for expressing and manipulating names and name-parts.
**<p>
** Conversely, a FileForker instance actually refers to the file-system, and it uses
** a Pathname to name the file-system-item it operates on.
** This separation into name and referent is intentional, but is quite different from java.io.File,
** where a single entity represents operations on file-names <i>and</i> operations on the file-system.
**<p>
** Another major difference between File and FileForker is that a FileForker is
** an abstract factory, where a File isn't.  This leads us to discuss...
**
** <h3>Factories Within Factories</h3>
**
** The FileForker class provides a singleton abstract-factory capability, embodied under the
** static methods SetFactory(), MakeOne(), and GetFactory().
** This makes it easy to isolate other code from a particular implementation of FileForker,
** since you typically set the factory once by name during setup and don't change it.
** The default factory-name is uninstantiable.  As a result, an IllegalArgumentException will
** be thrown from MakeOne() until you set the factory name correctly.
**<p>
** If you want, you can call SetFactoryClass() instead of SetFactory().
** This might be necessary if the prevailing classloaders don't allow SetFactory() to load
** a specified class by name.  If you supply a Class to SetFactoryClass(), it must nonetheless
** have an appropriate constructor (no args, public scope) and be a concrete FileForker imp.
** If you pass null to SetFactoryClass(), it's accepted and any prior factory class is removed. 
**<p>
** Each concrete instance of a FileForker is itself a factory object, i.e. it embodies the 
** Abstract Factory and Factory Method design patterns ("Design Patterns" by Gamma at al.).
** The main factory methods are makeForkInputStream(), makeForkOutputStream(), and
** makeForkRandomRW().  A FileForker always provides these factory methods.
** Two other factory methods are optional: makeAlias() and makeWatcher().
** If unimplemented, they return null.
**<p>
** All these methods are abstract factories because the same method in
** a different instance can return a different concrete type.
** The concrete type returned from each factory method is only of concern
** to a particular concrete FileForker itself.
** For example, no other class knows nor cares exactly what concrete type
** makeForkInputStream() returns, as long as its a subclass of InputStream.
**
** <h3>Notes On Portability</h3>
**
** FileForker is intended to be cross-platform to the extent that Mac-equivalents actually
** exist on non-Mac platforms.  For example, the Mac's <i>data fork </i> is just the contents of
** a file on other platforms.  Thus, any reference to a data-fork can quite easily be mapped to a File
** on non-Mac platforms.  Although concepts like length are trivial to map, ones like file-type and
** file-creator do not have simple mappings to things like "file-suffix" on other platforms.
** Other concepts like <i>alias </i> may have correspondences on various platforms, but the
** actual implementations vary across platforms (e.g. Windows short-cuts, Unix sym-links).
**
** <h3>Notes On Aliases</h3>
**
** Aliases are objects that refer to other files.  Alias-files are files that refer to other files.
** Other platforms also know them as links, symlinks, or shortcuts.
** The exact form and behavior of aliases and alias-files is platform-dependent, and even format-dependent.
** For example, on Unix systems a <b>symlink</b> is allowed in a File's pathname, and is
** automatically resolved to its referent.  On Mac OS X, which is Unix-based, symlinks are allowed
** in File pathnames, but alias-files created by the Finder are not. 
** In effect, Finder aliases are black holes or dead-ends for Java programs using File.
** This is bad.
**<p>
** Historically (before 2003), FileForker implementations never provided the ability to use
** Finder alias-files in pathnames, except for makeResolved() or related alias-resolving methods.
** Symlinks on Mac OS X worked, but not alias-files.
** Now, a FileForker implementation is free to allow alias-files as directories in Pathnames,
** and provide automatic on-the-fly resolving.  That is, a FileForker implementation can allow
** alias-files to be used on a par with symlinks, as non-leaf Pathname parts leading to a target item.
**<p>
** If a FileForker implementation provides on-the-fly alias-resolving, it always does so
** under these two rules:
**<ol>
**  <li><b>ONLY</b> resolve non-leaf Pathname parts.</li>
**  <li><b>NEVER</b> change the Pathname itself.</li>
**</ol>
** In short, any and all on-the-fly alias-resolving magic only occurs
** on the Pathname parts leading up to a target, never on the target item itself.
** This behavior is essential.  
** If the leaf-item was resolved, then you'd never be able to refer to an alias-file
** or symlink file directly.  Any action you tried to take on it would instead be taken on the
** original referent that was being pointed to.  That would be a grave mistake.
**<p>
** Because of the historical behavior, I have chosen not to change the alias-resolving abilities of
** the default Mac OS implementations named in the MacPlatform class.
** Instead, I have created new implementations, in the same
** packages as the old, with an initial "Resolving" on the class name.  For example, on Mac OS 9,
** the NineForker's alias-resolving implementation is called ResolvingNineForker.  And so on for
** the other historical Mac OS implementations.
**<p>
** Since new FileForker imps can provide on-the-fly non-leaf alias-resolving by default,
** all new imps should describe their behavior in their doc-comments.
** The latest and best Mac OS X implementation, MacOSXForker using JNI, always and only
** resolves non-leaf aliases on-the-fly.  It doesn't have a non-resolving implementation, so it
** doesn't have a "Resolving" name to distinguish it.
**<p>
** In hindsight, the decision to not resolve non-leaf aliases on-the-fly was dumb.
** Even dumber was not allowing implementations to choose their own behavior.
** Resolving non-leaf aliases on-the-fly has no speed cost when a Pathname part isn't an alias,
** yet it greatly improves the overall usefulness of FileForkers and Pathnames.
** As Maxwell Smart would say, "Sorry about that, Chief".
**
** <h3>Cross-Platform Implementations</h3>
**
** Some things expressed by FileForker are necessarily platform-specific.
** Some will have certain correlations across platforms, even if an imperfect mapping.
** FileForker is abstract, with a generic implementation provided by PlainForker.
** That implementation maps the expressible Mac OS concepts to plain platform-neutral Java, 
** such as the above example of a data-fork mapping to a File's data.  
** Some unexpressible Mac-specific features are quietly ignored and don't cause exceptions,
** such as aliases or file-comments.
**<p>
** If you want to ignore resource-forks altogether, then PlainForker may be useful.
** If you want resource-fork operations to throw IOExceptions, then use JavaOnlyForker instead.
** The best choice of implementation depends on what you're trying to accomplish.
**<p>
** You can also subclass the PlainForker implementation and add resource-fork support yourself, 
** say by prefixing ".r_" to the file-name,
** or putting the forks in a separate directory-tree, or whatever you find practical.
** Just put your implementation in your own package, feed its name to FileForker.SetFactory(),
** and you're in business.
**
** @author Gregory Guerin
**
** @see Pathname
** @see FileInfo
** @see glguerin.io.imp.gen.PlainForker
** @see glguerin.io.imp.gen.JavaOnlyForker
*/

abstract public class FileForker
{
	/**
	** A non-null but empty (zero-length) array of bytes.
	*/
	public static final byte[] NO_BYTES = new byte[ 0 ];



	// ###  S T A T I C   S I N G L E T O N   F A C T O R Y  ###

	/**
	** The factory Class.  Set by SetFactory() or SetFactoryClass(), used by MakeOne().
	*/
	private static Class factoryClass = null;


	/**
	** This is a static factory-method that creates a new FileForker of the 
	** specific type given by the last-assigned factory.
	** If no factory has ever been set, or any other error occurs,
	** an IllegalArgumentException is thrown.  You should arrange to catch this.
	** If you don't, your program will terminate abruptly.
	**<p>
	** Earlier versions of this method would create a GenericForker instance if there was
	** a factory-class problem.  That feature proved to be widely problematic, since people
	** could not easily recognize the error of not setting the factory first.  Instead of an error,
	** the factory would fail-over to a FileForker that was unexpected, and often puzzling.
	** At least with an IllegalArgumentException, the program terminates with a visible reason.
	** The IllegalArgumentException may not give quite as much detail as one might like, though.
	**
	** @see #SetFactory
	** @see #GetFactory
	*/
	public static FileForker
	MakeOne()
	  throws IllegalArgumentException
	{
		String reason = "Unknown error ";

		try
		{
			// If any of this throws an exception, the factory has failed.
			return ( (FileForker) factoryClass.newInstance() );
		}
		catch ( NullPointerException why )
		{  reason = "Null factory Class: ";  }
		catch ( ClassCastException why )
		{  reason = "Must be a FileForker subclass: ";  }
//		catch ( NoSuchMethodException why )
//		{  reason = "No default constructor: ";  }
		catch ( InstantiationException why )
		{  reason = "Can't be abstract: ";  }
		catch ( IllegalAccessException why )
		{  reason = "Constructor must be public: ";  }
		catch ( Throwable why )
		{  reason = "Can't use factory: caught " + why.getClass().getName() + ": " + why.getMessage() + ": ";  }

//		reason += String.valueOf( factoryClass );
//		explainFactoryFailure( reason );
		throw new IllegalArgumentException( reason + String.valueOf( factoryClass ) );
	}


	/**
	** Set the factory to be the given fully-qualified class name, or null.
	** This method checks that a non-null className
	** refers to a viable and instantiable Class of the correct type.
	** A null className is accepted, and results in no factory Class.
	** All errors throw an IllegalArgumentException.
	*/
	public static void
	SetFactory( String className )
	  throws IllegalArgumentException
	{
		if ( className == null )
		{
			factoryClass = null;
			return;
		}

		String reason = "Unknown error ";
		try
		{
			// If any exceptions are thrown, the factoryClass must end up null.
			SetFactoryClass( Class.forName( className ) );

			// Getting here, the factory is known to work, so keep it.
			return;
		}
		catch ( IllegalArgumentException why )
		{
			// Force factory to null, then rethrow the exception.
			factoryClass = null;
			throw why;
		}
		catch ( ClassNotFoundException why )
		{  reason = "Class not found: ";  }
		catch ( Throwable why )
		{  reason = "Can't set factory: caught " + why.getClass().getName() + ": " + why.getMessage() + ": ";  }

		// Getting here, the factoryClass doesn't work, so it must be set to null.
		factoryClass = null;

//		reason += String.valueOf( className );
//		explainFactoryFailure( reason );
		throw new IllegalArgumentException( reason + String.valueOf( className ) );
	}


	/**
	** Set the factory to the given Class, 
	** which must be a concrete FileForker subclass or null.
	** If it's not, an IllegalArgumentException is thrown.
	** If the factory Class is not acceptable, it still remains as the factory Class until
	** a new Class is set.  This differs from SetFactory(), where failures cause a null factory Class
	** to be installed.
	**<p>
	** The reason for this method is that SetFactory() may not be able to load a named Class,
	** even though it's in scope and accessible.  This could happen when the MacBinary Toolkit
	** is a Java extension, but the desired FileForker imp is not.  
	** Apparently, an extension class (i.e. an extension ClassLoader)
	** may not be allowed to load a classpath class by name.
	** I don't know if this is a bug or a feature, but it's the case under Mac OS X Java 1.3.1,
	** so I have to account for it.
	*/
	public static void
	SetFactoryClass( Class factory )
	  throws IllegalArgumentException
	{
		factoryClass = factory;

		// If a null Class was assigned, we're done.
		// If a non-null Class was assigned, we make one to confirm its efficacy.
		// We don't need the new instance, so ignore the instance MakeOne() returns.
		if ( factory != null )
			MakeOne();
	}


	/**
	** Return the current factory Class, or null.
	** This represents the Class for instances returned by MakeOne().
	*/
	public static Class
	GetFactory()
	{  return ( factoryClass );  }


	/**
	** Will emit an error message on stderr stream (System.err),
	** unless the boolean property "glguerin.io.FileForker.Factory.quiet" is set to true.
	** Returns T if the message was emitted, F if not.
	*/
/*
	public static boolean
	explainFactoryFailure( String message )
	{
		boolean verbose = ! Boolean.getBoolean( "glguerin.io.FileForker.Factory.quiet" );
		if ( verbose )
		{
			System.out.flush();
			System.err.println( "## FileForker factory failure" );
			System.err.print( "## " );
			System.err.println( message );
			System.err.flush();
		}

		return ( verbose );
	}
*/



	// ###  I N S T A N C E   M E M B E R S  ###

	private Pathname myTarget = null;


	/**
	** Vanilla constructor, visible to sub-classes, but not public.
	*/
	protected
	FileForker()
	{  super();  }


	/**
	** Override Object.toString(), returning getPath().
	*/
	public String
	toString()
	{  return ( getPath() );  }



	/**
	** Return the actual current Pathname, which may be null.
	** Contrast this with getPathReplica(), which returns a replica of the current Pathname.
	** @see #getPathReplica
	*/
	public Pathname
	getPathname()
	{  return ( myTarget );  }

	/**
	** Use the given Pathname instance as the actual internal Pathname reference.
	** The pathname may be null, in which case the internal Pathname is set to null.
	** You must assign this FileForker a non-null Pathname before doing anything
	** that refers to the Pathname.
	**<p>
	** This method is for when you want the actual Pathname given to be used,
	** rather than making a replica of it like setPathReplica() does.
	** For example, walking a directory tree
	** by listing names is more convenient with a single Pathname target 
	** where you just change its last part.  As another example, a FilenameFilter may be more
	** efficient to use a single Pathname and only change names or parts as needed.
	*/
	public void
	usePathname( Pathname pathname )
	{  myTarget = pathname;  }


	/**
	** Return a replica of the current Pathname, or null if no Pathname is assigned.
	** Contrast this with getPathname(), which returns the current actual Pathname in use.
	** @see #getPathname
	*/
	public Pathname
	getPathReplica()
	{
		Pathname current = getPathname();
		if ( current == null )
			return ( null );
		else
			return ( current.replica() );
	}

	/**
	** Assign a replica of the given Pathname, or assign null if 'path' is null.
	** That is, the internal Pathname is set to a replica of path, rather than
	** an actual reference to path as usePathname() does.
	** This implementation calls Pathname.replica() to make the replica
	** and usePathname() to assign it.
	**<p>
	** You should use this method when you want a FileForker to copy a Pathname you are using,
	** so the FileForker's Pathname and your Pathname can vary independently.
	** For example, if you are generating random names you might want to do it with your own
	** Pathname, rather than doing it directly on a Pathname being held by a FileForker.
	*/
	public void
	setPathReplica( Pathname path )
	{
		// If 'path' is null, then null is assigned.  If non-null, its replica() is assigned.
		usePathname( path == null ? null : path.replica() );
	}



	/**
	** Use the actual Pathname instance given as the internally kept target reference.
	** @deprecated  since 09May2002; call usePathname() instead.
	** @see #usePathname()
	*/
	public final void
	useTarget( Pathname target )
	{  usePathname( target );  }

	/**
	** Return a replica (copy) of the current target.
	** @deprecated  since 09May2002; call getPathReplica() instead.
	** @see #getPathReplica()
	*/
	public final Pathname
	getTarget()
	{  return ( getPathReplica() );  }

	/**
	** Set the target to a replica (copy) of the given Pathname.
	** @deprecated  since 09May2002; call setPathReplica() instead.
	** @see #setPathReplica()
	*/
	public final void
	setTarget( Pathname target )
	{  setPathReplica( target );  }


	/**
	** Return any length-limit this implementation imposes on individual Pathname parts.
	** This limit should be observed when generating unique file-names and at other times.
	** This limit represents a length limit for EACH PART OF A PATHNAME, not an
	** overall limit on the length of pathname Strings.  For example, classic Mac OS under HFS
	** imposes a 31-byte limit on element names.  This is returned as a 31-char limit by Mac OS
	** implementations.
	*/
	abstract public int
	getNameLimit();


	/**
	** Return the leaf-name of the current target, i.e. its last() part, or null if none.
	** The "leaf-name" is the last named part of a path-name, and may or may not be
	** an actual leaf-file (i.e. it may be a directory), and may or may not exist.
	** The returned String will be null if no target has been set, or if the target was set to null,
	** or if the target is empty (has no parts).
	**
	** @see glguerin.io.Pathname#last
	*/
	public String
	getLeafName()
	{
		Pathname target = getPathname();
		return ( (target != null) ? target.last() : null );  
	}

	/**
	** Return the path of the target Pathname, i.e. its getPath().
	** Returns a zero-length String if no target is set.
	** Never returns null.
	**
	** @see glguerin.io.Pathname#getPath
	*/
	public String
	getPath()
	{  
		Pathname target = getPathname();
		return ( (target != null) ? target.getPath() : "" );
	}


	// ###  I S O M O R P H S   O F   F I L E   M E T H O D S  ###

	/**
	** Return true if the current target and all directories leading
	** to it exist, false if not.
	** Non-leaf aliases may or may not be resolved on-the-fly, according to the implementation.
	**
	** @see java.io.File#exists
	*/
	abstract public boolean
	exists();

	/**
	** Return true if the current target is an ordinary file (i.e. a non-directory) and all directories leading
	** to it exist and are readable, false if not.
	** Non-leaf aliases may or may not be resolved on-the-fly, according to the implementation.
	** Leaf alias-files will return true for this method.
	**
	** @see java.io.File#isFile
	*/
	abstract public boolean
	isFile();

	/**
	** Return true if the current target is a directory and all directories leading
	** to it exist and are readable, false if not.
	** Non-leaf aliases may or may not be resolved on-the-fly, according to the implementation.
	** An alias-file of a directory is not itself a directory.
	**
	** @see java.io.File#isDirectory
	*/
	abstract public boolean
	isDirectory();

	/**
	** Return true if the current target is an alias of some sort, false if not.
	** Non-leaf aliases may or may not be resolved on-the-fly, according to the implementation.
	**
	** @see glguerin.io.FileInfo#isAlias
	*/
	abstract public boolean
	isAlias();

	/**
	** Return true if the current target is a symlink, false if not.
	** Non-leaf aliases may or may not be resolved on-the-fly, according to the implementation.
	** A symlink will also return true from isAlias(); this method is to further
	** distinguish the kinds of alias files.
	**<p>
	** This default implementation always returns false.
	**
	** @see #isAlias
	*/
	public boolean
	isSymlink()
	{  return ( false );  }

	/**
	** Return true if the current target is normally hidden (invisible), false if not.
	** Non-leaf aliases may or may not be resolved on-the-fly, according to the implementation.
	**
	** @see glguerin.io.FileInfo#getFinderFlags
	*/
	abstract public boolean
	isHidden();

	/**
	** Return true if the current target is readable, false if not.
	** Non-leaf aliases may or may not be resolved on-the-fly, according to the implementation.
	** If the target is a directory, "readable" means that you can list its contents
	** and use it in path-names to refer to its contents.
	**
	** @see java.io.File#canRead
	*/
	abstract public boolean
	canRead();

	/**
	** Return true if the current target is writable, false if not.
	** Non-leaf aliases may or may not be resolved on-the-fly, according to the implementation.
	** If the target is a directory, "writable" means that you can create
	** or delete files or directories in it.
	**
	** @see java.io.File#canWrite
	*/
	abstract public boolean
	canWrite();

	/**
	** Return the length of the data-fork for false, or the resource-fork for true.
	** Always returns zero for directories.
	** Non-leaf aliases may or may not be resolved on-the-fly, according to the implementation.
	**
	** @see java.io.File#length
	*/
	abstract public long
	length( boolean resFork );

	/**
	** Return a list of String names representing the named contents of the directory,
	** omitting any entries for the targeted directory itself or its parent.
	** Non-leaf aliases may or may not be resolved on-the-fly, according to the implementation.
	**<p>
	** If the target is not a directory, null is returned.
	** If the target is an empty directory, a String[0] is returned.
	** If the target can't be listed, the result will probably be a String[0]...
	**<p>
	** The returned array holds names that have undergone a pass through the 
	** target Pathname's getFormat().asLiteral(String) method.  That is, all the Strings
	** in the array will be literal names immediately suitable for Pathname.add().
	** Contrast this with File.list(), where you'd have to call Pathname.addPath() with a name,
	** in order to ensure that each name was literalized.
	**
	** @see java.io.File#list
	** @see glguerin.io.Pathname#addPath
	** @see glguerin.io.PathnameFormat#asLiteral
	*/
	abstract public String[]
	list();



	// ###  D I S K - A L T E R I N G   I S O M O R P H S   O F   F I L E   M E T H O D S  ###

	/**
	** Create all directories leading to the current target, also creating the target
	** itself as a directory, returning true only if one or more directories
	** were actually created, returning false if all directories already exist.
	** Throws an IOException if a directory can't be created as requested,
	** or if an item in the Pathname exists but is not a directory.
	**<p>
	** Non-leaf aliases may or may not be resolved on-the-fly, according to the implementation.
	**<p>
	** This method calls makeDir() as needed to make directories, walking up and down
	** the target Pathname as needed.  The current target Pathname itself is not changed.
	** Instead, a temporary Pathname is used as needed, and the original target Pathname
	** is restored no matter what happens.
	**
	** @see #makeDir
	** @see java.io.File#mkdir
	*/
	public boolean
	makeDirs()
	  throws IOException
	{
		boolean result = false;

		Pathname original = getPathname();
		try
		{
			// We need a stack of Pathname parts and a working Pathname as the target.
			Pathname stack = new Pathname( original.count(), null );
			Pathname working = original.replica();
			usePathname( working );

			// Ascend toward target's root until a directory is found or we exhaust the path.
			while ( ! isDirectory()  &&  working.count() != 0 )
			{  stack.add( working.cut() );  }

			// We don't have to check for exhausted Pathname, since trying to makeDir() on it
			// while re-descending will throw an IOException just as surely as anything else.
			// The working Pathname now represents a directory where we start makeDir()'s.
			// IOExceptions thrown during descent abort the dir-making at the point of failure.
			while ( stack.count() != 0 )
			{
				// Pop a leaf-name from the stack and make a directory with its name.
				working.add( stack.cut() );
				result |= makeDir();
			}

			// Getting here, we've got a full set of directories, either just-made or existing.
			// The boolean result reflects what was made.
		}
		finally
		{
			// Always restore original target Pathname.
			usePathname( original );
		}

		return ( result );
	}

	/**
	** Create the current target as a directory, returning true only if a directory
	** was actually created, returning false if a directory already exists.
	** Throws an IOException if the directory can't be created as requested,
	** or if the target item exists but is not a directory.
	**<p>
	** Non-leaf aliases may or may not be resolved on-the-fly, according to the implementation.
	**<p>
	** Note that an IOException may be thrown if the target item is an alias
	** to a directory.  End users may find this confusing, since the distinction between
	** an actual directory and its alias may not be clear to them.  Thus, you may want to
	** resolve aliases or perform other pre-filtering before calling this method.
	**
	** @see java.io.File#mkdir
	*/
	abstract public boolean
	makeDir()
	  throws IOException;

	/**
	** Delete the current target, returning true if successful or false if not.
	** If unsuccessful, the reason for failure is unknowable.
	** If the target is a directory, it must be empty in order to be deleted.
	**<p>
	** Non-leaf aliases may or may not be resolved on-the-fly, according to the implementation.
	**
	** @see java.io.File#delete
	*/
	abstract public boolean
	delete();

	/**
	** Rename the current target's leaf-item, throwing an IOException on failure.
	** When successful, the target's leaf-name becomes the given name.  This differs from java.io.File.
	** Also unlike File.renameTo(), this method throws an IOException for errors.
	**<p>
	** Unlike File.renameTo(), this method will only rename an item without moving it.
	** To move an item without renaming it, use moveTo().
	** To do both, use both methods.
	**<p>
	** Non-leaf aliases may or may not be resolved on-the-fly, according to the implementation.
	**
	** @see java.io.File#renameTo()
	** @see moveTo()
	*/
	abstract public void
	renameLeaf( String newName )
	  throws IOException;

	/**
	** Move the current target to a new location on the target's volume, throwing an IOException on failure.
	** The given destination must be an existing directory or disk-volume.
	** Non-leaf aliases may or may not be resolved on-the-fly, according to the implementation.
	** If the current target-item is a directory, the entire sub-tree is moved to the new location.
	** When successful, the active target will refer to the moved item at its new location.
	** This differs from java.io.File.
	**<p>
	** You can't move items across volumes, only on the same volume.
	** To move across volumes, you can use FileHelper.duplicate() to copy items one at a time.
	** You can't use this method to rename an item -- use renameLeaf() for that.
	**<p>
	** Non-leaf aliases may or may not be resolved on-the-fly, according to the implementation.
	**
	** @see java.io.File#renameTo()
	** @see #renameLeaf()
	** @see glguerin.io.FileHelper#duplicate()
	*/
	abstract public void
	moveTo( Pathname destination )
	  throws IOException;


	// ###  F O R K E R   I / O - F A C T O R I E S  ###

	/**
	** A factory-method that constructs a new
	** read-only InputStream reading the current target's designated fork.
	** The target must exist and be readable.
	**<p>
	** Non-leaf aliases may or may not be resolved on-the-fly, according to the implementation.
	**<p>
	** SecurityManager.checkRead() is invoked to determine if reading is allowed.
	*/
	abstract public InputStream
	makeForkInputStream( boolean resFork )
	  throws IOException;

	/**
	** A factory-method that constructs a new
	** write-only OutputStream writing to the current target's designated fork.
	** If the target doesn't exist, it is created with default file-type and creator, and
	** an undesignated fork of zero-length.
	** If the target already exists and append is false, its designated fork is 
	** truncated to zero-length upon opening, and the undesignated fork is unaffected.
	** If append is true, its designated fork is appended to by first seeking to the end.
	** If the target exists but the designated fork is not writable, an IOException is thrown.
	** If the target's designated fork is already open for writing, an IOException may be thrown.
	**<p>
	** Non-leaf aliases may or may not be resolved on-the-fly, according to the implementation.
	**<p>
	** SecurityManager.checkWrite() is invoked to determine if writing is allowed.
	*/
	abstract public OutputStream
	makeForkOutputStream( boolean resFork, boolean append )
	  throws IOException;

	/**
	** A factory-method that constructs a new
	** RandomRW with given access to the designated fork of the current target.
	** If readWrite is false, then the target must exist, though the designated fork may be zero-length.
	** If readWrite is true and the target doesn't exist, it's created with both forks zero-length.
	** If readWrite is true and the target already exists, the designated fork is not truncated.
	**<p>
	** Non-leaf aliases may or may not be resolved on-the-fly, according to the implementation.
	**<p>
	** SecurityManager.checkRead() and checkWrite() are invoked to determine if
	** reading and/or writing are allowed, according to the value of readWrite.
	** I.e. checkWrite() is called only when readWrite is true.
	*/
	abstract public RandomRW
	makeForkRandomRW( boolean resFork, boolean readWrite )
	  throws IOException;



	// ###  F O R K E R   C A T A L O G - I N F O   &   A C C E S S   P R I V I L E G E S  ###

	/**
	** Set the creator and file types that newly created files will have by default.
	** Calling this method on any concrete FileForker instance sets the defaults for
	** all concrete instances of the same class.
	**<p>
	** <b>IMPLEMENTATION NOTE: </b>
	** Since the defaults are shared by the concrete implementation,
	** each concrete class must implement this method and the two getters for itself.
	** An implementation CANNOT be done by inheriting from a base-class, because then the
	** sharing of the defaults would be among all derived classes, which would be wrong.
	*/
	abstract public void
	setDefaultTypes( int defaultFileType, int defaultFileCreator );

	/**
	** Called by makeForkOutputStream() and makeForkRandomRW(),
	** or anywhere else a file needs to be created.  NOT called when an existing
	** file is merely truncated.
	*/
	abstract public int
	getDefaultFileType();

	/**
	** Called by makeForkOutputStream() and makeForkRandomRW(),
	** or anywhere else a file needs to be created.  NOT called when an existing
	** file is merely truncated.
	*/
	abstract public int
	getDefaultFileCreator();


	/**
	** Get a FileInfo describing the current target, which must exist.
	** Non-leaf aliases may or may not be resolved on-the-fly, according to the implementation.
	** If withComment is false, the returned FileInfo will have no comment-text.
	** If withComment is true, the target's comment-text from this.getComment()  
	** are attached to the returned FileInfo.  
	** Retrieving the comment takes extra time, which may not suit your purposes.
	**<p>
	** SecurityManager.checkRead() is invoked to determine if reading is allowed.
	**	**
	** @exception java.io.FileNotFoundException
	**   Thrown if the target-item does not exist, or any of the directories leading to it don't exist,
	**  or the volume or drive doesn't exist.
	** @exception java.io.IOException
	**   Thrown when some other error occurs, including when access to the item is denied.
	**
	** @see #getComment
	*/
	abstract public FileInfo
	getFileInfo( boolean withComment )
	  throws IOException;

	/**
	** Set the current target's FileInfo, creating an empty ordinary file if
	** the current target-file does not exist.
	** All directories leading up to the target must already exist.
	** Non-leaf aliases may or may not be resolved on-the-fly, according to the implementation.
	**<p>
	** The following attributes available in the given FileInfo are not
	** assigned to the current target's catalog-info:
	**<ul type="disc">
	**  <li> The <b>String getLeafName()</b> has no effect on the target's leaf-name.
	**    To change the name of a target, use File.renameTo().
	**  <li> The <b>boolean isLocked()</b> is not applied.
	**    To set or clear the file-lock on a target, you must invoke setFileAccess().
	**  <li> The <b>long getForkLength()</b> values have no effect on the target's forks.
	**    To truncate or extend a target's data or resource fork, use an OutputStream or writable RandomRW
	**    obtained from the relevant factory method.
	**  <li> The <b>boolean isDirectory()</b> does not affect the target in any way.
	**    You can't change a directory to an ordinary file, nor vice versa.
	**</ul>
	** All other attributes are assigned verbatim to the current target, including all Finder-flags,
	** file-type, file-creator, when-created, when-modified, etc.  
	**<p>
	** The comment is treated specially.
	** If the given FileInfo has a non-empty comment, then the comment is saved
	** by calling setComment().
	** If the given FileInfo has a zero-length comment, then no comment is saved,
	** nor is any existing comment attached to the target file removed.
	** To remove a comment from an existing target, you must call setComment()
	** with a null or zero-length text.
	**<p>
	** SecurityManager.checkWrite() is invoked to determine if writing is allowed.
	**
	** @exception java.io.FileNotFoundException
	**   Thrown if the target-item does not exist, or any of the directories leading to it don't exist,
	**  or the volume or drive doesn't exist.
	** @exception java.io.IOException
	**   Thrown when some other error occurs, including when access to the item is denied.
	**
	** @see #setComment
	** @see #setLock
	*/
	abstract public void
	setFileInfo( FileInfo info )
	  throws IOException;


	/**
	** Return a FileAccess describing the current target's access privileges.
	** Non-leaf aliases may or may not be resolved on-the-fly, according to the implementation.
	** The exact meaning and extent of the values in the returned FileAccess is platform-dependent.
	**<p>
	** A SecurityException may be thrown if the access privileges cannot be determined.
	**
	** @exception java.io.FileNotFoundException
	**   Thrown if the target-item does not exist, or any of the directories leading to it don't exist,
	**  or the volume or drive doesn't exist.
	** @exception java.io.IOException
	**   Thrown when some other error occurs, including when access to the item is denied.
	*/
	abstract public FileAccess
	getFileAccess()
	  throws IOException;

	/**
	** Set as much as possible of the current target's access privileges from the given FileAccess
	** and the boolean flag.
	** The target must already exist; if it doesn't, it is not created, and an IOException is thrown.
	** Non-leaf aliases may or may not be resolved on-the-fly, according to the implementation.
	**<p>
	** The value of isLocked is only used when the FileAccess is null.
	** Otherwise the desired state of the file-lock is presumed to be in the non-null FileAccess.
	** When the FileAccess is non-null, the boolean is a don't-care.
	** This quirk of the design is to simplify backwards compatibility with earlier versions of this toolkit,
	** so that callers don't have to create an entire FileAccess just to encapsulate a single boolean flag state.
	**<p>
	** Platforms and implementations are free to interpret the values of the FileAccess in whatever
	** way makes sense to them, including ignoring all the values and doing nothing.
	** Implementations are also free to ignore individual values which are unsettable for that imp,
	** without throwing an IOException.  For example, an imp may choose to ignore the owner-ID value
	** entirely, meaning it has no effect on the target file, nor does it play any other role.  Another imp,
	** however, may choose to compare the desired owner-ID with the target's actual owner-ID and throw
	** an IOException if they don't match.  Yet another imp may do the same for the group-ID as well.
	**<p>
	** Some elements may be unsettable unless the current program is running in a context that
	** allows setting those elements.  For example, changing the owner may only be possible running under
	** an effective user-ID of the super-user.  Whether or not a forbidden action causes an IOException
	** or is just quietly ignored depends entirely on the platform and the implementation.
	** If you really need to know whether a setFileAccess() worked in its entirety, you should
	** call getFileAccess() after you call setFileAccess() and compare the actual results with
	** the expected results.
	**<p>
	** A SecurityException may be thrown if the access privileges cannot be changed for security reasons,
	** or some other security permission is denied.
	**
	** @exception java.io.FileNotFoundException
	**   Thrown if the target-item does not exist, or any of the directories leading to it don't exist,
	**  or the volume or drive doesn't exist.
	** @exception java.io.IOException
	**   Thrown when some other error occurs, including when access to the item is denied.
	*/
	abstract public void
	setFileAccess( FileAccess desired, boolean isLocked )
	  throws IOException;



	// ###  F O R K E R   F I L E - C O M M E N T S  ###

	/**
	** Get the comment-text of the current target <b>WITHOUT</b> resolving any aliases
	** that the target may contain.
	** This String may be zero-length, but will never be null.
	**<p>
	** SecurityManager.checkRead() is invoked to determine if reading is allowed.
	**
	** @exception java.io.IOException
	**    Thrown if the target-item does not exist, or any of the directories leading to it don't exist,
	**  or the volume or drive doesn't exist.  Actually throws a FileNotFoundException in those cases.
	**  Throws a vanilla IOException if there's any other error while completing the operation.
	*/
	abstract public String
	getComment()
	  throws IOException;

	/**
	** Set the comment-text of the current target, but DO NOT create the target if it doesn't exist.
	** Non-leaf aliases may or may not be resolved on-the-fly, according to the implementation.
	** All directories leading up to the target must already exist.
	**<p>
	** The target's comment is set by internally truncating the String's length as needed.
	** If the String is null, the comment is set to zero-length (i.e. effectively removed).
	**<p>
	** SecurityManager.checkWrite() is invoked to determine if writing is allowed.
	**
	** @exception java.io.IOException
	**    Thrown if the target-item does not exist, or any of the directories leading to it don't exist,
	**  or the volume or drive doesn't exist.  Actually throws a FileNotFoundException in those cases.
	**  Throws a vanilla IOException if there's any other error while completing the operation.
	*/
	abstract public void
	setComment( String text )
	  throws IOException;


	// ###  A L I A S E S  ###


	/**
	** Resolve all the non-leaf aliases and semantic elements in the current target Pathname,
	** without resolving or affecting the current leaf part.  The current Pathname is changed accordingly.
	** The returned boolean signifies a change to the Pathname: T if it changed, F if it didn't.
	**<p>
	** If the leaf item itself is a resolveable alias or semantic element, it WILL NOT be resolved.
	** A semantic element means a "." or ".." or a similar element which
	** does not represent the principal name of that element.
	**<p>
	** The current target Pathname is assigned the resulting pathname, if any.
	** If an IOException is thrown, the current target Pathname is unaffected.
	** The current target leaf need not exist, but all the folders or aliases leading up to it must,
	** and all the aliases, symlinks, etc. must resolve to accessible directories.
	**<p>
	** If any non-leaf aliases are resolved, or if any Pathname part differs from before, T is returned.
	** If nothing in the Pathname differs from before, F is returned.
	** This comparison is done with Pathname.equals(), so it is case-sensitive even if the
	** underlying file-system is case-insensitive.  That is, T may be returned even if
	** the Pathname names the same file-system item, and only the caseness of a name changed.
	**<p>
	** This default implementation calls makeResolved() to do the resolving,
	** and then evaluates the outcome. 
	** A concrete subclass may override this implementation, if it has a better way to
	** resolving leading aliases in the current Pathname than calling makeResolved().
	**<p>
	** <b>IMPLEMENTATION NOTE: </b>
	** This implementation tacitly assumes "." is the semantic element meaning "this directory".
	** Platforms that don't support this meaning in Pathnames MUST override this method.
	** There are ways to do the resolving without overtly using ".", but they're more complicated.
	** It's simpler to use ".", which works with all the provided implementations.
	**
	** @exception java.io.FileNotFoundException
	**    Thrown if any non-leaf part of the target Pathname is an alias that can't be resolved, or isn't a directory.
	** @exception java.io.IOException
	**    Thrown when some other error occurs while resolving the target.
	**  Typically, either an IOException or one of its other subclasses is thrown.
	*/
	public boolean
	resolveLeading()
	  throws IOException
	{
		// If no Pathname is assigned, immediately return F: "no change".
		Pathname target = getPathname();
		if ( target == null )
			return ( false );

		// The seemingly obvious approach is to cut the leaf, call makeResolved(), then add the leaf back.
		// However, since makeResolved() allows a non-existent leaf, we must use a new leaf instead.
		// We must also ensure that all parts leading up to original leaf are dirs or dir-aliases,
		// and they must resolve to named accessible directories.
		// So, the correct approach is to swap "." for the current leaf and then resolve that.
		// Resolving will never leave a "." or ".." in any part of the Pathname.
		// If the original leaf is "." or "..", this approach still works, even though the ultimate
		// resulting Pathname has the leaf "." or ".." put back on it.
		// If the Pathname is empty (i.e. leaf is null), it also works.
		String leaf = target.swap( "." );
		boolean changed = false;

		// A try/finally ensures that target Pathname always gets its original leaf back.
		try
		{
			// A successful makeResolved() will never leave a "." leaf on the Pathname.
			// For Pathname.equals() to work, the resolved Pathname needs its own "." leaf.
			// This also ensures the 'finally' clause removes the "." when restoring original leaf
			// if there was a Pathname change.
			Pathname resolved = makeResolved();
			resolved.add( "." );

			if ( ! resolved.equals( target ) )
			{
				target.set( resolved );
				changed = true;
			}
		}
		finally
		{  target.swap( leaf );  }

		return ( changed );
	}



	/**
	** Works like makeResolved(), but copies the resulting Pathname back into the
	** currently assigned Pathname. 
	** Returns a boolean: T if the Pathname changed, F if it didn't.
	** If an IOException is thrown by makeResolved(), then the current Pathname is unaffected,
	** and the IOException is thrown by this method.
	**<p>
	** A concrete subclass may override this implementation, if it has a better way to
	** affect the current Pathname than calling makeResolved().
	**
	** @exception java.io.FileNotFoundException
	**    Thrown if any part of the target Pathname is an alias that can't be resolved, or isn't a directory.
	** @exception java.io.IOException
	**    Thrown when some other error occurs while resolving the target.
	**  Typically, either an IOException or one of its other subclasses is thrown.
	*/
	public boolean
	selfResolve()
	  throws IOException
	{
		Pathname resolved = makeResolved();
		if ( resolved.equals( getPathname() ) )
			return ( false );

		// Set the actual target, without changing its PathnameFormat.
		getPathname().set( resolved );

		return ( true );
	}


	/**
	** Resolve all aliases and semantic elements in the current target Pathname,
	** returning a new Pathname that reflects the fully resolved path leading to the item.
	** Don't change the current internal target Pathname at all.
	** If the leaf item itself is a resolveable alias, it will be resolved.
	** The current target leaf need not exist, but all the folders or aliases leading up to it must,
	** and all the aliases must be resolvable and accessible directories.  
	**<p>
	** Semantic elements in a Pathname include things like a part named "." or "..", which represent
	** either an identical or parent directory.  Normally, you won't put such elements into a Pathname,
	** but this method must resolve them if they appear.
	** Different platforms may have different semantic elements, or elements with platform-specific meaning.
	**<p>
	** If any resolvable alias resides on an external server, the resolution process may try 
	** to mount that server.  This may result in a timeout if the server cannot be found (usually about 15 secs),
	** during which the computer may be unresponsive to the user.	
	** Implementations are discouraged from doing this unless they must.
	** It is usually better to throw an IOException without resolving the external server,
	** than it is to wait a long time or present a server-login UI.
	**<p>
	** SecurityManager.checkRead() is invoked to determine if reading is allowed.
	** Resolving aliases requires reading the file-system.
	**
	** @exception java.io.FileNotFoundException
	**    Thrown if any part of the target Pathname is an alias that can't be resolved, or isn't a directory.
	** @exception java.io.IOException
	**    Thrown when some other error occurs while resolving the target.
	**  Typically, either an IOException or one of its other subclasses is thrown.
	*/
	abstract public Pathname
	makeResolved()
	  throws IOException;



	/**
	** This factory-method returns a new Alias representing the current Pathname's target,
	** which must exist and be accessible.
	** If the target does not exist or is inaccessible, an IOException is thrown.
	** If Aliases are not supported, null is returned, but the target may still need to be accessible.
	** The only meaning of a null return is "not supported".
	** All errors that can occur will throw an exception of some kind, and never return null.
	**<p>
	** The immediate target of the Alias may itself be a symlink or an alias-file.
	** The Alias will refer to the symlink or the alias-file, not the ultimate referent of those files.
	** That is, an Alias does not automatically resolve its immediate referent into the ultimate referent.
	** If you want that, you should call makeResolved() or selfResolve()
	** before calling makeAlias().
	**<p>
	** The returned Alias is no longer dependent on the FileForker that made it.
	** You can retarget the FileForker and any Aliases already made will be unaffected.
	** You can use any Alias with any FileForker, as long as the types are compatible.
	** That is, as long as the returned Alias is the same concrete type as what the
	** FileForker's concrete implementation would return, and what it will
	** accept for createAliasFile().
	** In practical terms, you can make an Alias with one FileForker, then pass that Alias
	** to another FileForker's createAliasFile() method, as long as the second FileForker
	** accepts the concrete Alias type.
	**<p>
	** This default implementation always returns null, and the target need not be accessible.
	** Subclasses of FileForker need not provide a body for this method.
	** If this method is not overridden in a FileForker subclass,
	** this default method imp will remain in force and always return null.
	**
	** @exception java.io.IOException
	**    Thrown when an Alias can't be created for the current target.
	** @return
	**    An Alias referring to the current target, or null if Aliases are not supported.
	**
	** @see Alias
	*/
	public Alias
	makeAlias()
	  throws IOException
	{  return ( null );  }


	/**
	** Write a platform-dependent representation of the valid Alias
	** to the FileForker's current target, which must not exist.
	** You cannot overwrite or replace any existing file with this method.
	** The file created by this method refers to the Alias's original referent,
	** and can be resolved with makeResolved() or selfResolve().
	**<p>
	** The preferSymlink boolean is T to request a symlink instead of an alias-file,
	** or F to request an alias-file instead of a symlink.  The returned boolean
	** signifies the kind of file actually created: T for a symlink, F for an alias-file.
	** Asking for a particular kind of file does not guarantee you will get it.
	** An Alias's capabilities determine what kinds of alias-files are possible.
	** An implementation may support only alias-files, or only symlinks, or both.
	** Regardless of what you request in preferSymlink, the resulting file is some kind
	** of "file that refers to another file" appropriate to the platform and implementation.
	**<p>
	** The state of the Alias's original referent when this method is invoked
	** determines the nature of the data actually written, if any. 
	** For example, if the Alias's original referent has been removed, this method may fail. 
	** Or it may succeed, only to have a later makeResolved() fail.
	**<p>
	** The preferSymlink boolean is only interpreted as a request, hint, or suggestion.
	** It is not a demand.
	** A given platform and implementation may support one form but not the other,
	** or it may support both, or neither.  See Alias.getCapabilities().
	**<p>
	** In practical terms, a symlink is resolved at the file-system level, while an alias-file is not.
	** This is a bit vague, since different platforms do different things,
	** or different implementations on the same platform may even do different things. 
	** A useful touchstone is whether you can use the alias of a directory in the pathname of a  java.io.File.
	** If you can, and it works without having to explicitly resolve it or canonicalize it,
	** then it's probably a symlink or a symlink-like file-system element.
	** If you have to resolve it in the File's pathname, as with Finder-aliases under Mac OS X Java,
	** then it's probably not a symlink.
	** A FileForker implementation may, however, provide on-the-fly alias-file resolving,
	** thus blurring any effective distinction between symlinks and alias-files.
	**<p>
	** The given Alias does not have to be an Alias created by this FileForker instance.
	** You can make an Alias with one FileForker and then create alias-files
	** using a different FileForker of the same type.
	** The concrete Alias type only has to be acceptable to the concrete FileForker type.
	** As long as you're not mixing FileForker imps and Aliases, this won't be a problem.
	**<p>
	** The given Alias object is not destroyed by this method, and the same Alias can be written
	** to another alias-file or symlink in another location, or under a different name.
	**<p>
	** This default implementation always throws an UnsupportedIOException.
	** That means only those FileForker imps which support Aliases need to override it.
	**
	** @return
	**    T means a symlink-file was created.  F means an alias-file was created.
	**    The returned value <b>IS NOT</b> an indication of success or failure.
	**    Failures always throw an exception; any normal return signifies success.
	** @exception java.lang.IllegalArgumentException
	**    Thrown when the given Alias is the wrong type for this FileForker imp.
	** @exception glguerin.io.UnsupportedIOException
	**    Thrown when alias-file creation is not supported.
	** @exception java.io.IOException
	**    Thrown when the given Alias can't be written to a file.
	*/
	public boolean
	createAliasFile( Alias alias, boolean preferSymlink )
	  throws IOException
	{  throw new UnsupportedIOException( "FileForker.Alias unsupported" );  }


	/**
	** A FileForker.Alias is an in-memory Object that refers to an existing accessible file or directory.
	** An Alias can be written to disk as an alias-file or shortcut, but is not an alias-file itself.
	** That is, calling FileForker.makeAlias() does not create an alias-file;
	** only FileForker.createAliasFile() creates an alias-file.
	** After an Alias is written to an alias-file, the resulting file is resolveable by
	** FileForker.makeResolved() or FileForker.selfResolve().
	**<p>
	** The FileForker.makeAlias() factory-method is the only public mechanism to create Alias instances.
	** A platform or a FileForker implementation may not support Aliases,
	** in which case FileForker.makeAlias() always returns null.
	** For those FileForker imps, there is no concrete Alias subclass.
	**<p>
	** The capabilities of a particular concrete Alias class can be obtained from getCapabilities().
	** The returned int is a set of bits whose meaning is defined by the CAN_XXX named constants.
	** The value may have more than one bit set, signifying multiple capabilities.
	** Some capabilities indicated by Alias.getCapabilities() are actually capabilities of the
	** parent FileForker instance that created the Alias.
	** This blurring of capability vs. responsibility should not be a problem.
	**<p>
	** You can identify an Alias's type (what it refers to) with getAliasType().
	** This can be useful if, say, you need to identify applications, whether they are in classical
	** Mac OS single-file form, or in Mac OS X's app-bundle form.
	** The defined OSTYPE_XXX values are symbolic when interpreted across platforms.
	** That is, if a platform implements Alias, it will return the designated values from getAliasType(),
	** even if those values have no bearing on what is written to an alias-file on that platform.
	** In short, the FileInfo.getFileType() of a created alias-file may differ from
	** the originating Alias's getAliasType().
	**<p>
	** By default, an Alias does not implement an equals() method other than Object.equals(),
	** which tests for reference equality.  As a result, <b>two Aliases that refer to the same original
	** referent and have identical originalName()'s and alias-types ARE NOT EQUAL.</b> 
	** This is intentional, and mirrors the behavior of FileForker.
	**<p>
	** Conceptually, a FileForker.Alias is similar to an alias-record from Mac OS's Alias Manager.
	** It's actually more than just an alias-record, because a FileForker.Alias also represents
	** the alias-type, and some additional internal information.
	** It can also be easily written to a resolveable alias-file (or symlink).
	*/
	abstract public static class Alias
	{
		/**
		** The bit-mask for getCapabilities() or isCapable() signifying
		** that FileForker.createAliasFile() can create an alias-file with the Aliases it makes.
		*/
		public static final int CAN_ALIAS_FILE = 0x0001;

		/**
		** The bit-mask for getCapabilities() or isCapable() signifying
		** that FileForker.createAliasFile() can create a symlink with the Aliases it makes.
		*/
		public static final int CAN_SYMLINK = 0x0002;


		/** The getAliasType() value representing a single-file (classical) application Alias. */
		public static final int OSTYPE_APPLICATION_ALIAS = 0x61647270;  // 'adrp'

		/**
		** The getAliasType() value representing a bundled application Alias.
		** A bundled application's original is a directory, so this value distinguishes folders
		** from app-bundles.  On platforms that don't support bundled-apps, this value
		** is never returned from getAliasType().
		*/
		public static final int OSTYPE_APP_BUNDLE_ALIAS = 0x66617061;  // 'fapa'

		/** The getAliasType() value representing a folder Alias. */
		public static final int OSTYPE_FOLDER_ALIAS = 0x66647270;  // 'fdrp'


		/** Visible only in subclasses. */
		protected
		Alias()
		{  super();  }

		/** Returns originalPath(), which will be null if this Alias is destroy()'ed. */
		public String
		toString()
		{  return ( originalPath() );  }

		/** Calls destroy() to free up the Alias imp's underlying resources. */
		protected void
		finalize()
		  throws Throwable
		{  destroy();  }

		/**
		** Return the platform-dependent pathname of the original referent,
		** in a form appropriate for a java.io.File or a suitably platform-aware Pathname.
		** Initially, this is the value of getPath() of the FileForker at the time the
		** Alias was made by makeAlias().
		**<p>
		** Returns null after destroy().
		**<p>
		** The term "original" refers to the original referent, as in "pathname of original referent".
		** It does not mean that the pathname String is always the same as when this Alias
		** was originally made.
		*/
		abstract public String
		originalPath();

		/**
		** Return an identifying value representing the type of this Alias.
		** The value may legitimately be zero, even when the implementation supports file-types.
		** Certain specific values have the significance
		** described for the OSTYPE_XXX named constants of this class.
		**<p>
		** The returned value represents the Alias's type, which is not necessarily the
		** file-type that FileForker.createAliasFile() would produce.  Nor is it necessarily
		** the file-type that FileForker.getFileInfo() would return for the original referent.
		** These types might be the same, you can't rely on it always being that way
		** across implementations and platforms.
		**<p>
		** Returns -1 after destroy().  Other negative values may be legal alias-types,
		** so don't assume that every negative value means "destroyed".
		**
		** @see #OSTYPE_APPLICATION_ALIAS
		** @see #OSTYPE_APP_BUNDLE_ALIAS
		** @see #OSTYPE_FOLDER_ALIAS
		*/
		abstract public int
		getAliasType();

		/**
		** Return a set of bits, signifying capabilities, in an int.
		** More than one bit may be set.
		** At least one bit should be set, even in a destroy()'ed Alias.
		**<p>
		** Identify the individual capabilities using the bit-masks
		** defined by the CAN_XXX named constants of this class.
		**
		** @see #isCapable
		** @see #CAN_ALIAS_FILE
		** @see #CAN_SYMLINK
		*/
		abstract public int
		getCapabilities();

		/**
		** Return T if and only if all the capabilities represented by 1-bits
		** in bitMask are present in this Alias's getCapabilities() value.
		** If bitMask has a single set bit, then the returned boolean signifies a single capability.
		** If bitMask has more than one set bit, then the returned boolean signifies the combined capabilities.
		** In short, T is returned only when all capabilities represented by the bitMask are available.
		**<p>
		** This is a simple convenience method that calls getCapabilities() to return:
		**<br>&nbsp;&nbsp;&nbsp;&nbsp;
		** (getCapabilities() & bitMask) == bitMask 
		**
		** @see #getCapabilities
		*/
		public boolean
		isCapable( int bitMask )
		{  return ( (getCapabilities() & bitMask) == bitMask );  }


		/**
		** Destroy all the internal elements of this Alias, making it unusable.
		** Calling destroy() more than once on the same Alias is always harmless.
		**<p>
		** The nature of an Alias's internal resources is implementation-dependent.
		** You can call this method to speed up the freeing of an Alias's internal resources.
		** This would be prudent if you're creating lots of Alias'es and
		** the GC'er may not act quickly enough.
		**<p>
		** This method is called by Alias.finalize(), so the 
		** finalizer will eventually clean up an Alias's internal resources.
		*/
		abstract public void
		destroy();

		/**
		** Throws an IOException saying "Alias destroyed" if the Object is null,
		** or returns normally if the Object is non-null.
		** This is a common building-block for concrete implementations.
		*/
		protected void
		needs( Object something )
		  throws IOException
		{
			if ( something == null )
				throw new IOException( "Alias destroyed" );
		}
	}


	// ###  S I G N A L L I N G   &   W A T C H I N G    F O R    C H A N G E S  ###

	/**
	** If possible, signal any change-watchers that the current target or its contents have changed.
	** Interested watchers should then re-examine the item or its contents,
	** or perform some other watcher-specific action.
	** The change-watchers may be in the current process, or in other processes,
	** or maybe even on other machines.
	** The propagation distance of the change signal is implementation-specific.
	**<p>
	** The recipient of this method's change-signals is provided as the nested Watcher class.
	** The signalChange() method may be provided even when Watcher is not implemented.
	** These choices are platform and implementation dependent.
	**<p>
	** This method returns T if a signal was sent (not necessarily a Unix-like signal),
	** or it returns F if no signal was sent or the feature is unimplemented.
	** This default implementation does nothing, always returning F.
	** An F return always means "signal not sent", but it can be for any reason.
	** The two most common reasons are "not implemented"
	** and "couldn't signal a change on current target".
	** There is no way to distinguish these two reasons, nor any other reason for returning F.
	**<p>
	** This method takes a boolean arg that defines the intended scope of the change.
	** Pass T to signal change-watchers that the specific target has changed.
	** Pass F to signal change-watchers that all displayed file-system items may have changed.
	** Passing F may cause a flurry of disk activity and screen redrawing, so only use it when
	** a large number of changes have occurred, or when you truly need to signal a major change.
	**<p>
	** If the current target is a file, the change signal may refer to the file's parent directory
	** rather than the file itself.
	** If the current target does not exist, the change signal may refer to the apparent directory,
	** or to one of the directories leading to the target,
	** or it may do nothing at all. 
	** These behaviors are implementation-specific.
	** The most likely behaviors are that non-specific and specific-directory change signals work,
	** if anything does.
	** Less likely is specific-file targeting, and less likely still is non-existent-file targeting.
	**<p>
	** This method is intended for use by programs that wish to communicate file-system changes
	** to a desktop-display agent, such as the Mac OS X Finder or the Windows File Manager.
	** For example, after a series of rename(), moveTo(), delete(), or file-creation actions
	** in one directory, calling signalChange() on the directory
	** will tell any change-watchers that the desktop display should be refreshed.
	** If the actions involve more than one directory, signal that each directory has changed,
	** or signal that "everything" has changed, and let the change-watchers sort it out.
	**<p>
	** The target need not have actually changed.  You can call this method merely
	** to cause all change-watching agents to do whatever it is they are watching for.
	** In some situations, you may be REQUIRED to call signalChange() in order to
	** force a change-watcher to refresh its private internal cache of file-system state.
	**<p>
	** This method may return T before all the change-watchers have had a chance to actually
	** receive the change signal or update themselves.
	** That is, notification may arrive at a watcher asynchronously from this method returning.
	** Or it may not.  It's implementation-specific.
	**<p>
	** The default imp here does nothing and always returns F.
	** An implementation need not use Unix-style signals.  It may use some other mechanism.
	**
	** @see #makeWatcher
	*/
	public boolean
	signalChange( boolean specifically )
	{  return ( false );  }


	/**
	** Make a new instance of the nested Watcher class, watching the current target for change-signals.
	** If unimplemented, this method returns null.
	** If the current target can't be watched, but Watchers are otherwise provided,
	** this method throws an IOException.
	**<p>
	** The boolean determines whether specific change-signals (T)
	** or both specific and blanket change-signals (F) will be received. 
	** A "blanket change-signal" is what happens when <b>signalChange( false )</b> is called.
	** You can't change this flag after the Watcher is created.  To change it, you must create a new Watcher.
	** You can't change the target of a Watcher after creation either.
	**<p>
	** This method can be implemented separately from signalChange().
	** That is, you may be able to signal a change with signalChange(), but
	** be unable to watch for a change using a Watcher.
	** The converse is also possible, though less likely, I think.
	**<p>
	** This default implementation always returns null,
	** so subclasses of FileForker need not provide a body for this method.
	** If this method is not overridden in a FileForker subclass,
	** this default method imp will remain in force and always return null.
	**
	** @exception java.io.IOException
	**    Thrown when a Watcher can't be created for the current target.
	** @return
	**    A Watcher referring to the current target, or null if Watchers are not supported.
	**
	** @see Watcher
	*/
	public Watcher
	makeWatcher( boolean onlySpecificChanges )
	  throws IOException
	{  return ( null );  }


	/**
	** A FileForker.Watcher is the receiver of signalChange()'s.
	** Each Watcher is tied to a single target in the file-system, frequently a directory.
	** After a Watcher is created, the target can't be changed, but the tie can be broken by destroy().
	**<p>
	** Callers interested in changes to the Watcher's target should call its waitForChange() method.
	** More than one Thread may wait for changes on the same Watcher.
	** Between calls to waitForChange(), more than one change-signal may arrive.
	** Each change-signal increments an internal counter in a thread-safe manner,
	** then awakens all Threads waiting for the change-signal.
	** The counter is not a counting semaphore.  It's simply a thread-safe counter,
	** which may roll over if enough change-signals arrive.
	**<p>
	** You can obtain the target pathname String or the current counter value at any time.
	** After destroy(), the pathname is always null, and the counter value is unchanging.
	**<p>
	** An active Watcher on a target may prevent certain changes from affecting that target item.
	** It may prevent some changes (deletion) but allow others (renaming).
	** Or it may not prevent anything.
	** If changes are prevented, then that may affect other processes,
	** so you should destroy() any Watcher as soon as you're done using it.
	** You should also destroy() a Watcher to free up its resources.  Callbacks and messages
	** consume CPU resources, and if nobody cares when they arrive, then the cycles are wasted.
	** 
	** @see FileForker#signalChange
	*/
	abstract public static class Watcher
	{
		/** Visible only in subclasses. */
		protected
		Watcher()
		{  super();  }

		/** Returns watchedPath(), which will be null after destroy(). */
		public String
		toString()
		{  return ( watchedPath() );  }

		/** Calls destroy() to free up the imp's underlying resources. */
		protected void
		finalize()
		  throws Throwable
		{  destroy();  }


		/**
		** Return the platform-dependent pathname of the item being watched,
		** in a form appropriate for a java.io.File or a suitably platform-aware Pathname.
		** This is the value of getPath() of the FileForker at the time the
		** Watcher was made by makeWatcher().
		** The pathname can't be changed after a Watcher is made.
		**<p>
		** Returns null after destroy().
		*/
		abstract public String
		watchedPath();

		/**
		** Return the current cumulative count of received change-signals,
		** which may be zero or negative.
		** A zero count indicates that no change-signals have ever arrived.
		** The count will be negative after it rolls over from Integer.MAX_VALUE.
		** This is not normally a problem, since the actual magnitude of the count rarely matters,
		** only the fact that it changes incrementally as change-signals are received.
		**<p>
		** This method is always implemented in a thread-safe manner.
		**<p>
		** After destroy(), this method will return an unvarying value.
		** That value may represent the accumulated total of change-signals received,
		** or it may be an arbitrary constant value, such as 0 or -1.
		** The actual value after destroy() is implementation-dependent.
		*/
		abstract public int
		getChangeCount();

		/**
		** The current Thread will wait up to the given internal for a change-signal to arrive,
		** or for the internal cumulative count of change-signals to differ from the compareCount.
		** Returns the current cumulative count of received change-signals,
		** which may be zero or negative.  The actual count value is rarely important.
		** The fact that it changed is what's important.
		**<p>
		** Any number of Threads can wait for a change on the same Watcher. 
		** Receiving a change-signal ultimately results in a notifyAll(), not a mere notify().
		** This method is always implemented in a thread-safe manner.
		**<p>
		** The interval determines how many milliseconds the calling Thread will wait for
		** change-signals to arrive.  
		** Negative intervals do not wait at all. 
		** The only delay is the synchronization and implementation latency.  
		** A zero interval waits indefinitely.
		** A positive interval waits up to that many milliseconds.
		**<p>
		** Regardless of the interval given, the calling thread only waits when
		** the given compareCount is initially equal to the current cumulative count.
		** If the counts differ initially, then the current cumulative count is immediately returned.
		** This use of a caller's provided compareCount allows any number of callers to wait for
		** any number of change-signals, letting each one decide for itself which changes
		** it has or hasn't seen.
		**<p>
		** If destroy() is called while threads are waiting, they are all awakened.
		** After a destroy(), no threads will be able to waitForChange().
		** Any waitForChange() calls after a destroy() throw an IllegalStateException.
		**<p>
		** If the target item (frequently a directory) is moved, renamed, or deleted,
		** the behavior of this method is implementation-specific.
		** Some implementations may be able to track changes in location or name.
		** An implementation may be unable to detect target deletion.
		** In that case, change-signals would probably cease to arrive.
		**<p>
		** Although a negative interval or an unchanging compareCount
		** effectively lets you poll a Watcher,
		** it's a very bad idea to do this in a spin-loop that is unbounded by any
		** other delays or blocking opportunities.  Use your head: block a Thread.
		**
		** @exception java.lang.IllegalStateException
		**  is thrown when this Watcher is destroyed, or the target has become inaccessible.
		** @exception java.lang.InterruptedException
		**  is thrown when the wait() was interrupted.
		**
		** @see FileForker#signalChange
		*/
		abstract public int
		waitForChange( long interval, int compareCount )
		  throws InterruptedException;

		/**
		** Destroy all the internal elements of this Watcher, making it unusable.
		** Calling destroy() more than once on a Watcher is always harmless.
		**<p>
		** Calling destroy() while other Threads are waiting for a change will awaken those Threads.
		** Each Thread will have the ultimate counter value returned to it, and then be prohibited
		** from calling waitForChange() thereafter.
		**<p>
		** An undestroyed Watcher may consume OS resources accumulating change-signals,
		** even though no one will ever wait for those changes.
		** You should call destroy() when you want to stop using a Watcher.
		** Eventually, finalize() will call destroy(), but until then it will consume resources
		** for each change-signal sent on the watched target item.
		**<p>
		** This method is called by Watcher.finalize(), so the 
		** finalizer will eventually clean up a Watcher's internal resources.
		*/
		abstract public void
		destroy();
	}


}
