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


// --- Revision History ---
// 02May2002 GLG  create first partial imp
// 03May2002 GLG  revise how Tinker is used
// 03May2002 GLG  add support for createRef(), setRefInfo(), deleteRef()
// 03May2002 GLG  add support for resolveRef()
// 06May2002 GLG  coalesce to a single getRefInfoX() function
// 06May2002 GLG  refactor so some native methods are protected instance methods
// 07May2002 GLG  more protected native methods
// 07May2002 GLG  add renameRef() support
// 28May2002 GLG  add resolvingLock, just in case
// 28May2002 GLG  add moveRef()
// 28May2002 GLG  add begin(), next(), end()
// 05Dec2002 GLG  package and class name change
// 05Dec2002 GLG  revise how alias-resolving does thread-safety
// 05Dec2002 GLG  add Alias support
// 06Dec2002 GLG  more Alias support
// 08Dec2002 GLG  reduce mapAliasInfo(); better support is in MacOSXForker.mapToAlias()
// 10Dec2002 GLG  move mapAliasInfo() and makeIconHints() to TinAlias
// 11Dec2002 GLG  rework writeAlias() for new args and symlink option
// 11Dec2002 GLG  add createSymlink() native method
// 11Dec2002 GLG  change package name
// 02Jan2003 GLG  add support for Alias.update() capability
// 03Jan2003 GLG  work on refAlias()
// 03Jan2003 GLG  refactor to extend FSRefItemX
// 04Jan2003 GLG  add diagnostic inside newAlias()
// 06Jan2003 GLG  add informOfChange()
// 07Jan2003 GLG  rework informOfChange()
// 08Jan2003 GLG  factor out whichFSRef()
// 09Jan2003 GLG  add nativeInit() and call it in static initializer block
// 09Jan2003 GLG  cut vestigial possibleAppBundle()
// 16Jan2003 GLG  add mayResolve() that always returns T
// 24Jan2003 GLG  refactor bootName()
// 30Jan2003 GLG  FIX: fix informOfChange() so it uses appropriate FSRef byte[]


/**
** A TinFSRefItem is an FSRefItemX for MacOSXForker.
**<p>
** This class assumes that another class (nominally MacOSXForker) has loaded
** the necessary JNI library, and made the function-names in it available.
**<p>
** Some static methods are synchronized.
** This is done because only some of the underlying OS functions are not thread-safe,
** or I don't know what their thread-safety is (typically, whether reentrant or not).
** We use the class's own lock for this thread-safety, so it will block any thread that
** tries to re-enter one of the thread-unsafe alias functions, or anything else where
** I was unsure of the ultimate re-entrancy.
** I assume this is enough.  That is, that I don't have to coordinate thread-safety
** across processes, or with other libraries, or with other native code.
** This assumption will be wrong if there's another JNI library that calls the same
** functions, and doesn't use the same class-lock.  Not much I can do about that, though.
**
** @author Gregory Guerin
*/

public class TinFSRefItem
  extends FSRefItemX
{
	/**
	** Rely on MacOSXForker to load my JNI library.
	*/

	/**
	** A TinFSRefItem instance is statically initialized by calling its refBoot() method,
	** so the bootName() method will be able to always return the literalized boot-name.
	** For example, if the user-visible "literal" name has an embedded slash like "R/W",
	** the returned String would be "R/W", since that's what the FSRef APIs use.
	**<p>
	** This static instance is returned by the instance method bootItem().
	** This allows a common abstract implementation in FSRefItemX, yet allows
	** each concrete implementation to provide the instance in its own way.
	*/
	private static final TinFSRefItem bootItem;
	static
	{
		nativeInit();

		bootItem = new TinFSRefItem();
		bootItem.refBoot();
	}



	/**
	** Construct an empty FSRefItem.
	*/
	public
	TinFSRefItem()
	{  super();  }


	/**
	** Concrete factory-method.
	*/
	protected ForkRW
	newFork( boolean forWriting, int forkRefNum, String tag )
	{  return ( new TinFSFork( forWriting, forkRefNum, tag ) );  }



	/**
	** Set this TinFSRefItem so it references the boot volume, so that subsequent
	** calls to bootName() return the literalized name of the boot volume.
	**<p>
	** This implementation works by leaving the volRefNum (technically, the FSVolumeRefNum)
	** of the startup-volume in myRefNum[0].  The bootName() method then determines the
	** name dynamically, returning it as a literalized String.  This may seem needlessly
	** inefficient, but it's the only way to survive having the boot-volume's name changed.
	** A little elapsed-timing shows that this isn't a significant time-sink
	** in any case, so fears of squandered cycles are largely unfounded.
	*/
	protected void
	refBoot()
	{
		final short STARTUP_VOL = (short) -32768;
		final int SYSTEM_FOLDER = 0x6D616373;			// 'macs'

		int[] dirDontCare = new int[ 1 ];
		int osErr = findFolderX( STARTUP_VOL, SYSTEM_FOLDER, false, myRefNum );

		// Leave isReferenced indicating success/fail.
		isReferenced = (osErr == 0);
	}


	/**
	** Return the boot-vol's literalized name, or throw an IOException.
	** The user can edit the name of the boot volume at any time, which means we must
	** always retrieve the name dynamically using the volRefNum established by bootRef().
	*/
	protected String
	nameBoot()
	  throws IOException
	{
		if ( ! isReferenced )
			throw new IOException( "No boot name" );

		int osErr = volumeRefX( myRefNum[ 0 ], 0, myChars, myRef1 );

		// If unsuccessful, signal failure.
		if ( osErr != 0 )
			throw new IOException( "Can't get boot name" );

		// If successful, return literalized name via getName().
		return ( getName() );
	}


	/**
	** Return the boot name, or throw an IOException.
	*/
	protected String
	bootName()
	  throws IOException
	{  return ( bootItem.nameBoot() );  }


	/**
	** Called by refPart(), which is called by refItem().
	** On entry, myRef1 references an "apparent" directory, symlink, or alias-file.
	** Return T if caller should attempt to resolve it, F if not.
	**<p>
	** An implementation of this method may use myRef1 but must not change it.
	** It may use or change myRef2, and also myInfo, ignored[], and hadAlias[].
	**<p>
	** This imp always returns T, so all non-leaf aliases are resolved on-the-fly.
	*/
	protected boolean
	mayResolve()
	{  return ( true );  }


	/**
	** Create a new FileForker.Alias that refers to the current target
	** as set by reference() and refItem().
	** The target must exist and be accessible.
	**<p>
	** All the args are unshared, so the new Alias can safely keep them and modify them.
	** The Pathname must be a replica.
	** Both targetInfo and aliasInfo must be unshared.
	*/
	public FileForker.Alias
	newAlias( Pathname pathname )
	  throws IOException
	{
		// Must refer to an existing file-system object, whose FSRef is in myRef1.
		validAndRef();

		// Make a FileInfo replica describing the current target (i.e. the "original referent").
		// Must be the full form, not the brief form, since TinAlias needs file-type and creator.
		FileInfo targetInfo = new BasicFileInfo( getInfo( true ) );

		// Need a "pointer to AliasHandle" to hold resulting AliasHandle "token".
		// Let superclass's FSRefItem.check() do error-checking of result-code.
		int[] aliasRef = new int[ 1 ];
		check( newAlias( myRef1, aliasRef ) );

		// By default, the TinAlias has no alias-specific FileInfo.
		FileInfo aliasInfo = null;
		
		// Preliminary check for an app-bundle should be fast, rejecting ordinary folders if possible.
		// If it doesn't reject ordinary folders, the subsequent evaluation may be slower.
		// The desired suffix for app-bundle folders is hard-wired.  Configurable might be nicer.
		if ( possibleAppBundle( targetInfo, ".app" ) )
		{
			aliasInfo = probeAppBundle();

			// Control diagnostic at compile-time with literal true or false.
			if ( false  &&  aliasInfo != null )
			{
				System.err.println( " * TinFSRefItem.newAlias(): " + pathname.last()
						+ ", " + MacRoman.getOSTypeString( aliasInfo.getFileType() )
						+ ", " + MacRoman.getOSTypeString( aliasInfo.getFileCreator() ) );

				System.err.println( " *      probed: " + pathname );
			}
		}

		// Getting here, make a TinAlias from new AliasHandle, Pathname, and FileInfo's.
		return ( new TinAlias( aliasRef[ 0 ], pathname, targetInfo, aliasInfo ) );
	}


// #####

	/**
	** Write the FileForker.Alias to the currently referenced target (as previously set).
	** The target must not exist, but all elements leading up to it must exist.
	** We detect that condition the same way FSRefItem.create() does: the isReferenced flag.
	**<p>
	** If the Alias isn't the correct type for the FSItem type, a ClassCastException is thrown.
	** The Alias may be updated by this method, which may affect the Alias's pathname.
	**<p>
	** This imp knows how to make both alias-files and symlinks.
	*/
	public boolean
	writeAlias( boolean preferSymlink, Pathname targetPath, FileForker.Alias alias )
	  throws IOException
	{
		// Must already be in the "FSRef plus name" state, not referencing the item directly,
		// in order to attempt creating it.  Otherwise we surmise that it already exists.
		// We don't support overwriting by alias-files, so an existing target is an error.
		valid();
		if ( isReferenced )
			check( Errors.dupFNErr );		// throw an IOException with "file exists" text

		// Any ClassCastException thrown here is caught by FSForker.createAliasFile().
		TinAlias tinny = (TinAlias) alias;

		// Alias must not be destroyed.
		if ( tinny.originalPath() == null  ||  tinny.getAliasHandle() == 0 )
			throw new IOException( "Alias destroyed" );

		// This imp knows how to make alias-files and symlinks, so do preferred form.
		if ( preferSymlink )
			createSymlink( tinny, targetPath );
		else
			createFinderAlias( tinny );

		// On success, result is exactly what caller asked for.
		return ( preferSymlink );
	}



	/**
	** Called after writeAlias() does setup and other preliminaries.
	** The TinAlias represents the original's pathname.
	**<p>
	** This method DOES NOT update myRef1 and myRef2.
	** So on return, this FSRefItem will not reference the created symlink file.
	** This is harmless now, since FileForker.createAliasFile() doesn't assume that
	** its FSItem refers to the new file.  However, it may be a latent problem if
	** something is ever added to this class which takes that for granted.
	*/
	private void
	createSymlink( TinAlias alias, Pathname targetPath )
	  throws IOException
	{
		String originalName = alias.originalPath();
		String symlinkName = targetPath.getPath();

		// The native method returns an errno value, not an OSError value.
		// In order to have check() work, we have to translate the errno-codes.
		int errno = createSymlink( originalName, symlinkName );
		if ( errno == 0 )
			return;

		// Only bother translating a few of the most important errno codes.
		if ( errno == 2 )  // ENOENT
			errno = Errors.fnfErr;
		else if ( errno == 17 )  // EEXIST
			errno = Errors.dupFNErr;
		else if ( errno == 20 )  // ENOTDIR
			errno = Errors.errFSNotAFolder;

		// checkIOError() always throws an IOException when errno is non-zero.
		Errors.checkIOError( errno, "Can't create symlink: ", symlinkName );
	}


	/**
	** Called after writeAlias() does setup and other preliminaries.
	*/
	private void
	createFinderAlias( TinAlias alias )
	  throws IOException
	{
		// Alias must not be destroyed.
		int aliasHand = alias.getAliasHandle();

		// Use FileInfo on original and alias to make hints for createAliasFile() to retrieve an icon.
		// iconHints[0]: empty slot to return additional Finder-flags bit-mask
		// iconHints[1]: creator-hint
		// iconHints[2]: type-hint
		int[] iconHints = alias.makeIconHints();

		// Create the file, write the resources, close the resFile.
		// On success, the resulting file's FSRef is in myRef2, so we can set its info below.
		// On success, iconHints[0] will signal whether an 'icns' was added or not.
		check( createAliasFile( aliasHand, myRef1, getName(), myRef2, iconHints ) );
		swapRefs();
		isReferenced = true;

		// Now have to set file-type, creator, and Finder-flags of alias-file.
		// We only want to change a few things in the alias-file's info, so start with its current info.
		check( getRefInfo( myRef1, FSCatInfo.GETSET_FINFO, myInfo.getByteArray(), null, null ) );

		// Map the TinAlias's assigned FileInfo's to a file-type and creator in myInfo.
		// No other fields in FileInfo are affected.
		alias.mapAliasInfo( myInfo );

		// Set the Finder-flags with custom-icon flag from createAliasFile() and forced ISALIAS bit.
		myInfo.setFinderFlags( iconHints[ 0 ] | FileInfo.MASK_FINDER_ISALIAS );

		if ( false )
		{
			System.err.println( " * TinFSRefItem.createFinderAlias(): " + getName()
					+ ", " + MacRoman.getOSTypeString( myInfo.getFileType() )
					+ ", " + MacRoman.getOSTypeString( myInfo.getFileCreator() ) );
		}

		// Set the Finder-info only.
		check( setRefInfo( myRef1, FSCatInfo.GETSET_FINFO, myInfo.getByteArray() ) );
	}



// ##### END OF ALIAS SUPPORT #####


	/**
	** Inform listeners that a referenced item changed, or that everything changed.
	** Return T if a message was sent, F if not.
	** This method is for implementing FileForker.signalChange().
	**<p>
	** This imp calls the static native method changed(),
	** after selecting an FSRef byte-array to pass.
	*/
	public boolean
	informOfChange( int messageValue, boolean specifically )
	{
		// Select which FSRef is passed to native method, or null if can't signal a change.
		byte[] targetRef = whichFSRef( specifically );

		if ( false )
		{
			String what = "null";
			if ( targetRef != null )
				what = (targetRef == myRef1 ? "myRef1" : "myRef2");
			System.err.println( " * TinFSRefItem.informOfChange(): " + what );
		}

		if ( targetRef == null )
			return ( false );

		// Getting here, we're ready to call changed().
		// The native method calls FNNotify() or FNNotifyAll().
		// Result-code is not much use, so ignore it here.
		// Could let it determine returned boolean, but I'd rather indicate
		// that the code ATTEMPTED to signal a change, so always return T.
		int result = changed( messageValue, specifically, targetRef );

		return ( true );
	}



// #### END OF CHANGE-SIGNAL CODE ####


	/**
	** Fill in the name and root FSRef of the indexed volume.
	*/
	protected int
	volRef( int index, short[] volRefNum, char[] nameBuf, byte[] rootFSRef )
	{
		volRefNum[ 0 ] = kFSInvalidVolumeRefNum;
		int osErr = volumeRefX( kFSInvalidVolumeRefNum, index, nameBuf, rootFSRef );
		return ( osErr );
	}


	/**
	** Make the resultFSRef refer to the given file or directory,
	** calling FSMakeFSRefUnicode.
	** Return an OSErr value as the result.
	** None of the items may be null.
	**<p>
	** If the targeted item doesn't exist, an error-code is returned.
	** Unlike with an FSSpec, an FSRef can't refer to a non-existent item.
	** The rest of the code in FSRefItem is responsible for handling non-existent targets,
	** so they can be encapsulated with behavior similar to a non-existent FSSpec.
	** will call .
	*/
	protected native int 
	makeRef( byte[] parentFSRef, String name, byte[] resultFSRef );

	/**
	** Get the FSCatalogInfo for theFSRef.
	** Return an OSErr value as the result.
	** The nameBuf and/or parentFSRef may be null.
	*/
	protected native int 
	getRefInfo( byte[] theFSRef, int infoBits, byte[] catInfo, char[] nameBuf, byte[] parentFSRef  );


	/**
	** Open the item's named fork, calling FSOpenFork.
	** Return an OSError value as the result.
	*/
	protected native int 
	openRef( byte[] theFSRef, char[] forkName, byte permissions, short[] refNum );


	/**
	** Resolve the given FSRef as a possible alias-file.
	** Return an OSError value as the result.
	** On success, theFSRef holds the output FSRef, i.e. the resolved FSRef.
	**<p>
	** See "Alias Manager" Carbon docs.
	** Will call FSResolveAliasFile() or similar function.
	*/
	protected int 
	resolveRef( byte[] theFSRef, boolean resolveChains, byte[] targetIsFolder, byte[] wasAliased )
	{
		return ( resolve( theFSRef, resolveChains, targetIsFolder, wasAliased ) );
	}

	/**
	** This method is synchronized under the class-lock, to prevent re-entrant calls from
	** any other thread in the process from calling the thread-unsafe Alias Mgr functions.
	*/
	private static synchronized native int 
		resolve( byte[] theFSRef, boolean resolveChains, byte[] targetIsFolder, byte[] wasAliased );

	/**
	** This method is synchronized under the class-lock, to prevent re-entrant calls from
	** any other thread in the process from calling the thread-unsafe Alias Mgr functions.
	##  newAlias( byte[] fsRef, int[] aliasRef );
	*/
	private static synchronized native int
		newAlias( byte[] fsRef, int[] aliasRef );

	/**
	** This method is synchronized under the class-lock, to prevent re-entrant calls from
	** any other thread in the process from calling the thread-unsafe Resource Mgr functions.
	##  createAliasFile( int aliasHandle, byte[] parentFSRef, String name, byte[] resultFSRef, int[] iconInfo );
	*/
	private static synchronized native int
		createAliasFile( int aliasHandle, byte[] parentFSRef, String name, byte[] resultFSRef, int[] iconInfo );

	/**
	** This method is synchronized under the class-lock, to block re-entrant calls from
	** any other thread in the process.
	##  createSymlink( String originalName, String symlinkName );
	*/
	private static synchronized native int
		createSymlink( String originalName, String symlinkName );


	/**
	** This method is synchronized under the class-lock, to prevent re-entrant calls from
	** any other thread in the process from calling the thread-safety-unknown FNNotify() functions.
	##  changed( int msgValue, boolean specifically, byte[] theFSRef );
	*/
	private static synchronized native int
		changed( int msgValue, boolean specifically, byte[] theFSRef );



	/**
	** Create the file or directory referenced by the FSRef and other args,
	** calling FSCreateDirectoryUnicode() or FSCreateFileUnicode().
	** Return an OSErr value as the result.
	** None of the items may be null.
	*/
	protected native int 
	createRef( byte[] parentFSRef, String name, boolean isDirectory, byte[] resultFSRef );


	/**
	** Set the FSCatalogInfo for theFSRef, calling FSSetCatalogInfo.
	** Return an OSErr value as the result.
	** Returns OSError.
	*/
	protected native int 
	setRefInfo( byte[] theFSRef, int infoBits, byte[] catInfo  );


	/**
	** Delete the file or directory referenced by the FSRef,
	** without resolving any aliases.
	** Return an OSError value as the result.
	** Will call FSDeleteObject.
	*/
	protected native int 
	deleteRef( byte[] theFSRef  );


	/**
	** Rename the file or directory referenced by the FSRef,
	** without resolving any aliases,
	** calling FSRenameUnicode.
	** Return an OSErr value as the result.
	** None of the items may be null.
	*/
	protected native int
	renameRef( byte[] theFSRef, String newName, byte[] resultFSRef );



	/**
	** Move the file or directory referenced by the FSRef,
	** without resolving any aliases.
	** Return an OSError value as the result.
	** The destination must reference an existing directory.
	*/
	protected native int
	moveRef( byte[] theFSRef, byte[] destinationFSRef );


	/**
	** Return an opaque iterator Object for iterating over theFSRef.
	** Returns an instance of IOException, appropriately prepared, if there was an error.
	** Otherwise returns an instance of an arbitrary Object representing an iterator.
	*/
	protected Object
	begin( byte[] theFSRef )
	{
		// The opaque Object is an int[2] array.
		//   - array[0] is temporary storage for next() to use.
		//   - array[1] holds the FSIterator itself.
		int[] iteratorRef = new int[ 2 ];
		int osErr = openIterator( theFSRef, 0, iteratorRef );

		// If an error occurred, return an IOException instead of an opaque iterator Object.
		try
		{  check( osErr );  }
		catch ( IOException why )
		{  return ( why );  }

		// Move the actual FSIterator into slot[1].
		// This is so next() can use the array[0] slot to hold actual-count.
		iteratorRef[ 1 ] = iteratorRef[ 0 ];

		// Getting here, return the iteratorRef.
		return ( iteratorRef );
	}


	/**
	** Get the name of the next item iterated, or
	** return null when nothing left to iterate.
	** Errors also cause a null return, which simply halts the iteration.
	**<p>
	** The name must be a literalized (i.e. accent-composed) name, 
	** suitable for adding directly to a Pathname.
	**<p>
	** May change the contents of myInfo, myChars, and myRef2.
	*/
	protected String
	next( Object iterator )
	{
		String result = null;
		if ( iterator instanceof int[] )
		{
			// The int at array[1] is the actual FSIterator.
			int[] array = (int[]) iterator;

			int osErr = bulkInfo( array[ 1 ], FSCatInfo.GET_EXIST, myInfo.getByteArray(), myChars );

			// osErr: errFSNoMoreItems = -1417 indicates end of FSIterator
			// Not that we distinguish it from any other errors.
			// Since we used myChars as the name-buffer, getName() returns
			// the appropriately literalized form of the name.
			if ( osErr == 0 )
				result = getName();
		}

		return ( result );
	}


	/**
	** Stop iterating using the FSIterator started by begin().
	*/
	protected void
	end( Object iterator )
	{
		if ( iterator instanceof int[] )
		{
			int[] array = (int[]) iterator;
			closeIterator( array[ 1 ] );
					// don't care about any errors here.
			array[ 1 ] = 0;
		}
		return;
	}




	/**
	** Calls FSOpenIterator()
	*/
	private static native int 
	openIterator( byte[] theFSRef, int iteratorFlags, int[] iteratorRef );

	/**
	** Calls FSGetCatalogInfoBulk() for info on a single item.
	*/
	private static native int 
	bulkInfo( int fsIterator, int whichInfo, byte[] catInfo, char[] name );

	/**
	** Calls FSCloseIterator()
	*/
	private static native int 
	closeIterator( int fsIterator );



	/**
	** Calls FindFolder().
	** Returns OSError.
	*/
	private static native short 
	findFolderX( short volume, int type, boolean createIt, short[] refNum );

	/**
	** will call FSGetVolumeInfo().
	** Returns OSError.
	*/
	private static native short 
	volumeRefX( short volume, int index, char[] hfsUniStr255, byte[] rootFSRef );



	/**
	** This idempotent method performs all the native-side once-only initialization.
	** It's synchronized on the class-lock so it can't be called re-entrantly from other threads.
	** That shouldn't happen, but one never knows.
	*/
	private static synchronized native int
		nativeInit();

}
