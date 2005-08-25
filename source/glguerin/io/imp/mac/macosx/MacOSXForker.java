/*
** Copyright 2002, 2003 by Gregory L. Guerin.
** Terms of use:
**  - Briefly: OPEN SOURCE under Artistic License -- credit fairly, use freely, alter carefully.
**  - Fully: <http://www.amug.org/~glguerin/sw/artistic-license.html>
** This file is from the MacBinary Toolkit for Java:
**   <http://www.amug.org/~glguerin/sw/#macbinary> 
*/

package glguerin.io.imp.mac.macosx;

import java.io.*;

import glguerin.io.*;
import glguerin.io.imp.mac.*;
import glguerin.util.LibLoader;


// --- Revision History ---
// 02May2002 GLG  create
// 04Dec2002 GLG  add makeAlias()
// 05Dec2002 GLG  move makeAlias() into FSItem subclass
// 07Dec2002 GLG  add override of makeAlias() that identifies app-bundles
// 09Dec2002 GLG  move core makeAlias() definition to FSForker
// 09Dec2002 GLG  improve app-bundle recognition in makeAliasInfo()
// 10Dec2002 GLG  cover LibLoader's package change
// 11Dec2002 GLG  change package name
// 11Dec2002 GLG  add isSymlink() override
// 12Dec2002 GLG  refactor makeAliasInfo() to avoid strange behavior; clarity improves, too
// 01Jan2003 GLG  refactor makeAliasInfo() to call isAppBundleName()
// 07Jan2003 GLG  cut old Alias-making code
// 07Jan2003 GLG  add signalChange()
// 11Jan2003 GLG  add preliminary makeWatcher()
// 13Jan2003 GLG  regretfully turn makeWatcher() into conditional non-implementation
// 14Jan2003 GLG  factor makeWatcher() into CarbonMacOSXForker subclass
// 16Jan2003 GLG  FIX: isHidden() now handles "/" pathnames without NullPointerException


/**
** MacOSXForker is the principal FileForker implementation for the Mac OS X JNI-based imp.
** This imp's native-code library requires 10.1 or higher, since it uses an Icon Services
** function that only exists in 10.1 or higher.
**<p>
** The corresponding native-code library is "MacOSXForkerIO", and is loaded here with a
** static initializer block, even though this class has no native methods itself.
** Loading the library here puts the library-name in one place.
** Since the classes that need the library are only referenced here, this should work.
**<p>
** On Mac OS X, the filename of the library is "libMacOSXForkerIO.jnilib".
** LibLoader looks for it in the "Contents/Resources/Java" folder of the app-bundle (if possible),
** as well as the other locations for native libraries, including those used by
** Mac OS X's "domain" system of Java extensions.
**<p>
** A MacOSXForker resolves all non-leaf symlinks and alias-files on-the-fly.
** That is, a pathname with a non-leaf part that's an alias of a directory works
** the same as if the part were a symlink of a directory, or the actual directory itself.
** A MacOSXForker can distinguish symlinks from other kinds of aliases using isSymlink().
**<p>
** A MacOSXForker provides a FileForker.Alias
** implementation that can create alias-files and symlinks.
**<p>
** A MacOSXForker supports signalChange() directly on directories.
** If an existing file is targeted, changes are signaled on the parent directory.
** If a non-existing file is target, changes are signaled on the existing parent directory.
** Non-specific changes can be signaled on anything.
**<p>
** A MacOSXForker does not support makeWatcher().
** An implementation requires the process to have a Carbon-event thread, and that would
** be fatal to a Cocoa-Java program.  If you're using Carbon, and want to use Watchers,
** then use the CarbonMacOSXForker subclass.
** If you're using AWT or Swing on 1.3.1, you're using Carbon.
**
** @author Gregory Guerin
*/

public class MacOSXForker
  extends FSForker
{
    
//	static
//	{
//		// Use a smarter loadLibrary() that will look
//		// in Contents/Resources/Java of a Cocoa-Java app.
//		LibLoader.getLoader().loadLibrary( "MacOSXForkerIO" );
//	}

	/**
	** Vanilla constructor.
	*/
	public
	MacOSXForker()
	{  
		super();
	}

	/** Make a concrete FSItem. */
	protected FSItem
	newFSItem()
	{  return ( new TinFSRefItem() );  }




	/**
	** Return true if the current target is a symlink, false if not.
	** Aliases are not resolved.
	**<p>
	** This imp identifies symlinks by examining the FileInfo of the target.
	** On Mac OS X (10.2 and 10.1, possibly earlier) a symlink appears to the FSRef-based
	** code as a file with the following characteristics:
	**<ul>
	**  <li>Finder-flags indicate isAlias</li>
	**  <li>data-fork length non-zero</li>
	**  <li>resource-fork length zero</li>
	**  <li>file-type of 'slnk' 0x736C6E6B</li>
	**  <li>creator of 'rhap' 0x72686170</li>
	**</ul>
	** These characteristics even appear for symlinks on a UFS volume
	** that has no other Mac OS metadata.
	*/
	public boolean
	isSymlink()
	{
		try
		{
			// The FileInfo must have file-type and creator, so the brief getInfo() form won't suffice.
			FileInfo info = target( false, false ).getInfo( true );
			if ( info.isAlias()  &&  info.getForkLength( false ) > 0
					&&  info.getForkLength( true ) == 0
					&&  info.getFileType() == 0x736C6E6B  // 'slnk'
					&&  info.getFileCreator() == 0x72686170 )  // 'rhap'
			{  return ( true );  }
		}
		catch ( IOException why )
		{  /* FALL THROUGH */  }
		return ( false );
	}


	/**
	** On Mac OS X, all names starting with "." are nornally hidden.
	** This should be enforced regardless of whether the item has the INVISIBLE Finder-flag set or not.
	*/
	public boolean
	isHidden()
	{
		if ( super.isHidden() )
			return ( true );

		// Handle an empty Pathname, representing "/".
		// Although "/" is visible, "/." and "/.." are invisible.
		String leaf = getLeafName();
		return ( leaf != null  &&  leaf.startsWith( "." ) );
	}



	private static int fileType = 0;
	private static int fileCreator = 0;


	/**
	** Set the creator and file types that newly created files will have by default.
	** Calling this method on any concrete FileForker instance sets the defaults for
	** all concrete instances of the same class.
	**<p>
	** In this implementation, the built-in defaults are both set to 0,
	** which is the most sensible thing to do on Mac OS X.
	*/
	public void
	setDefaultTypes( int defaultFileType, int defaultFileCreator )
	{
		fileType = defaultFileType;
		fileCreator = defaultFileCreator;
	}

	/**
	** Called by makeForkOutputStream() and makeForkRandomRW(),
	** or anywhere else a file needs to be created.  NOT called when an existing
	** file is merely truncated.
	*/
	public int
	getDefaultFileType()
	{  return ( fileType );  }

	/**
	** Called by makeForkOutputStream() and makeForkRandomRW(),
	** or anywhere else a file needs to be created.  NOT called when an existing
	** file is merely truncated.
	*/
	public int
	getDefaultFileCreator()
	{  return ( fileCreator );  }




	/**
	** If possible, signal any listeners that the current target or its contents have changed.
	** Listeners should then re-examine the item or its contents, or perform some other listener-specific action.
	** The listeners may be in the current process, or in other processes, or maybe even on other machines.
	** The propagation distance of the change signal is implementation-specific.
	**<p>
	** This method takes a boolean arg that defines the intended scope of the change.
	** Pass T to signal listeners that the specific target has changed.
	** Pass F to signal listeners that all displayed directories may have changed.
	**<p>
	** This imp defers to the underlying FSItem.  It could easily be moved to FSForker.
	** When 'specifically' is T, the target may be an existing file or directory,
	** or a non-existing leaf in an existing directory.
	** When the target is an existing file, the change-signal refers to the file's parent directory.
	** The same is true for a non-existing leaf: the parent directory is the effective target.
	** This imp returns F only when the signal wasn't sent.
	*/
	public boolean
	signalChange( boolean specifically )
	{
		// Initially, refer to nothing.  This is for the non-specifically case.
		myRef.invalid();

		// If a specific target is requested, it must be referenceable.
		// The FSItem is responsible for deciding what informOfChange()
		// does when the target is a file, or a directory, or doesn't exist.
		// The FSForker is only responsible for pointing the FSItem at
		// some Pathname'd item.
		if ( specifically )
		{
			// Point myRef at a specific Pathname'd item.
			// If that fails, return F immediately.
			try
			{  target( false, false );  }
			catch ( IOException why )
			{  return ( false );  }
		}

		// According to the docs for FNNotify().
		final int kFNDirectoryModifiedMessage = 1;

		// At this point, myRef is either still invalid, or it refers to something.
		// Let informOfChange() sort it all out.
		return ( myRef.informOfChange( kFNDirectoryModifiedMessage, specifically ) );
	}

}
