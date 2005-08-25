/*
** Copyright 1998, 1999, 2001, 2002, 2003 by Gregory L. Guerin.
** Terms of use:
**  - Briefly: OPEN SOURCE under Artistic License -- credit fairly, use freely, alter carefully.
**  - Fully: <http://www.amug.org/~glguerin/sw/artistic-license.html>
** This file is from the MacBinary Toolkit for Java:
**   <http://www.amug.org/~glguerin/sw/#macbinary> 
*/

package glguerin.io.imp.mac;

import java.io.*;

import glguerin.io.*;


// --- Revision History ---
// 12Apr99 GLG  factor out as common to JDirect 1 & 2 implementations
// 13Apr99 GLG  move remainder of FileForker implementation here
// 13Apr99 GLG  add abstract protected methods for sub-classes to provide
// 04May99 GLG  add exists(), isFile(), isDirectory(), isAlias()
// 07May99 GLG  add canRead(), canWrite(), delete(), renameLeaf(), moveTo()
// 08May99 GLG  revise moveTo()'s behavior
// 28Oct99 GLG  add code to getCatalogInfo() for handling FileNotFoundException
// 03May01 GLG  convert to use Pathname
// 07May01 GLG  rescope some private methods to protected
// 07May01 GLG  revise for CommentAccess changes
// 17May01 GLG  put new imp of makeResolved() here for development
// 18May01 GLG  FIX: add semantically() to cover semantic parts in Pathnames
// 18May01 GLG  FIX: re-transform from internal to external Pathname in makeResolved() et al.
// 21May01 GLG  remove direct escaping support
// 22May01 GLG  refactor to give getCatInfo()
// 24May01 GLG  add prepSpec(), walkPath(), etc. to eliminate 255-char pathname limit
// 25May01 GLG  add boolean arg to walkPath(), use it to walk long paths and to resolve aliase
// 06Jun01 GLG  match changes to ForkRW, also adding default file-type and creator support
// 14Jun01 GLG  refactor into FSForker and FSItem classes
// 15Jun01 GLG  add faked getFileAccess(), setFileAccess()
// 19Jun01 GLG  add implementations of moveTo() and makeResolved()
// 04Dec2002 GLG  change myRef to protected visibility
// 04Dec2002 GLG  change refTarget() into target(), change callers to use returned FSItem
// 05Dec2002 GLG  add createAliasFile() imp that hands off to FSItem
// 07Dec2002 GLG  edit Alias-related comments for clarity
// 08Dec2002 GLG  cover arg change to FSItem.newAlias()
// 09Dec2002 GLG  add makeAliasInfo() and refactor makeAlias()
// 11Dec2002 GLG  revise createAliasFile() for new args and FSItem interface
// 01Jan2003 GLG  refactor to eliminate makeFork()
// 02Jan2003 GLG  refactor makeAlias() for FSItem.newAlias() arg-type change
// 03Jan2003 GLG  refactor makeAlias() again, moving most code into FSItem or its imps
// 06Jan2003 GLG  add signalChange()
// 07Jan2003 GLG  move signalChange() to X-specific class
// 04Feb2003 GLG  revise to use getPathReplica()


/**
** FSForker is an abstract Mac-specific FileForker implementation.
** It is the base-class for handling long UniCode filenames and large-fork files.
** It's also possible for a subclass to only support short MacRoman filenames and 2 GB forks.
**<p>
** It provides every method of public interest, but leaves the instantiation of internally
** used classes as abstract.  This separates the algorithms from the accessors that do the work.
**
** @author Gregory Guerin
**
** @see glguerin.io.Pathname
** @see glguerin.io.RandomRW
** @see CatalogAccess
** @see FSItem
*/

abstract public class FSForker
  extends FileForker
{
	private static final FileInfo EMPTY_INFO = new BasicFileInfo( false, null );


	/** My Pathname for preparing FSItem's and such. */
	private Pathname myPrepPath;


	/**
	** My FSItem, an opaque reference used for all operations.
	** The method target() will return this FSItem.
	*/
	protected FSItem myRef;


	/**
	** Vanilla constructor.
	** Assign one instance to each private instance variable,
	** calling the abstract methods once for each.
	*/
	public
	FSForker()
	{  
		super();
		myPrepPath = new Pathname( 0, null );
		myRef = newFSItem();
	}

	/** Make a concrete FSItem. */
	abstract protected FSItem
	newFSItem();


	/**
	** Return any length-limit this implementation imposes on pathname elements.
	** Since that depends on the FSItem implementation, defer to it.
	*/
	public int
	getNameLimit()
	{  return ( myRef.nameLimit() );  }




	/**
	** Check for read-only or read-write access to the given target, according to the given flag.
	** This requires the Java-form of the path-name, i.e. getPath().
	*/
	protected void
	securityCheck( String javaPath, boolean writable )
	  throws SecurityException
	{
		SecurityManager secure = System.getSecurityManager();
		if ( secure != null )
		{
			secure.checkRead( javaPath );
			if ( writable )
				secure.checkWrite( javaPath );
		}
	}

	/**
	** Use my instance variables (fields) to target the current Pathname, without resolving aliases.
	** Also ensure that the target file is created if needCreated is true.
	** The FSItem, which is myRef, is returned as a convenience.
	** Upon return, the FSItem refers to the target, which will exist when needCreated is true,
	** but may not exist when needCreated is false.
	*/
	protected FSItem
	target( boolean forWriting, boolean needCreated )
	  throws IOException
	{
		securityCheck( getPath(), forWriting );

		FSItem ref = myRef;
		ref.reference( getPathname(), false );		// reference it w/o resolving aliases

		// If it's for writing and it needs creating, ensure it exists with proper default types.
		// This uses myRef, as prepared by reference(), but does not alter what myRef references.
		if ( forWriting  &&  needCreated )
			ref.create( getDefaultFileType(), getDefaultFileCreator(), false );

		return ( ref );
	}



	// ###  I S O M O R P H S   O F   F I L E   M E T H O D S  ###

	/**
	** Factored out for common use.
	** If the target is accessible and exists, the FileInfo will be the brief form.
	** If the target does not exist or some error occurs accessing it,
	** the returned FileInfo will be EMPTY_INFO.
	** Null is never returned.
	*/
	protected FileInfo
	briefInfo()
	{
		try
		{  return ( target( false, false ).getInfo( false ) );  }
		catch ( IOException why )
		{  /* FALL THROUGH */  }
		return ( EMPTY_INFO );
	}

	/**
	** Return true if the current target and all directories leading
	** to it exist, false if not.
	** Aliases are not resolved.
	**
	** @see java.io.File#exists
	*/
	public boolean
	exists()
	{  return ( briefInfo() != EMPTY_INFO );  }

	/**
	** Return true if the current target is an ordinary file (i.e. a non-directory) and all directories leading
	** to it exist and are readable, false if not.
	** Aliases are not resolved.
	** Aliases will return true for this method, since they are files.
	**
	** @see java.io.File#isFile
	*/
	public boolean
	isFile()
	{
		FileInfo info = briefInfo();
		return ( info != EMPTY_INFO  &&  ! info.isDirectory() );
	}

	/**
	** Return true if the current target is a directory and all directories leading
	** to it exist and are readable, false if not.
	** Aliases are not resolved.
	** An alias of a directory is not itself a directory.
	**
	** @see java.io.File#isDirectory
	*/
	public boolean
	isDirectory()
	{  return ( briefInfo().isDirectory() );  }


	/**
	** Return true if the current target is readable, false if not.
	** Aliases are not resolved.
	** If the target is a directory, "readable" means that you can list its contents
	** and use it in path-names to refer to its contents.
	**<p>
	** This implementation assumes that if we can get the catalog-info for the target,
	** we can read the target itself.  Since this is identical to exists(), we just call exists().
	**
	** @see #exists
	** @see java.io.File#canRead
	*/
	public boolean
	canRead()
	{  return ( exists() );  }

	/**
	** Return true if the current target is writable, false if not.
	** Aliases are not resolved.
	** If the target is a directory, "writable" means that you can create
	** or delete files or directories in it.
	**<p>
	** This implementation examines the file-lock bit in the catalog-info attributes.
	** This is the inverse of FileInfo.isLocked().
	**
	** @see java.io.File#canRead
	*/
	public boolean
	canWrite()
	{
		FileInfo info = briefInfo();
		return ( info != EMPTY_INFO  &&  ! info.isLocked() );
	}

	/**
	** Return true if the current target is an alias of some sort, false if not.
	** Embedded aliases are not resolved.
	**
	** @see glguerin.io.FileInfo#isAlias
	*/
	public boolean
	isAlias()
	{  return ( briefInfo().isAlias() );  }

	/**
	** Return true if the current target is normally hidden (invisible), false if not.
	** Embedded aliases are not resolved.
	**
	** @see glguerin.io.FileInfo#hasFinderFlags
	*/
	public boolean
	isHidden()
	{  return ( briefInfo().hasFinderFlags( FileInfo.MASK_FINDER_ISINVISIBLE) );  }


	/**
	** Return the length of the data-fork for false, or the resource-fork for true.
	** Always returns zero for directories.
	** Aliases are not resolved. 
	**
	** @see java.io.File#length
	*/
	public long
	length( boolean resFork )
	{  return ( briefInfo().getForkLength( resFork ) );  }


	/**
	** Return a list of String names representing the named contents of the directory,
	** omitting any entries for the targeted directory itself or its parent.
	** Aliases are not resolved.
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
	public String[]
	list()
	{
		String[] names = null;

		// If the immediate target (aliases unresolved) is a directory, get its contents.
		// This method-call does two things: it determines whether we'll do a scan or not,
		// and it prepares myRef so it references the target directory.
		if ( isDirectory() )
		{
			// Collect the directory's contents in myPrepPath, which will be cleared first.
			try
			{  myRef.contents( myPrepPath );  }
			catch ( IOException ignored )
			{  /* IGNORED -- return whatever is collected so far */  }
			
			// How convenient -- make an array containing the Strings we collected.
			// Be sure to leave myPrepPath clear.
			names = myPrepPath.getParts();
			myPrepPath.clear();
		}

		return ( names );	
	}



	// ###  D I S K - A L T E R I N G   I S O M O R P H S   O F   F I L E   M E T H O D S  ###

	/**
	** Create the current target as a directory, returning true only if a directory
	** was actually created, returning false if a directory already exists.
	** Throws an IOException if the directory can't be created as requested,
	** or if the target item exists but is not a directory.
	** Aliases are not resolved.
	**<p>
	** Note that an IOException will be thrown if the target item is an alias
	** to a directory.  End users may find this confusing, since the distinction between
	** an actual directory and its alias may not be clear to them.  Thus, you may want to
	** resolve aliases or perform other pre-filtering before calling this method.
	**
	** @see java.io.File#mkdir
	*/
	public boolean
	makeDir()
	  throws IOException
	{
		// Point myRef at the item, which will be accessed for writing, but not created yet.
		target( true, false );

		// Try creating the target.  Directories don't have a creator or file-type, hence the zeros.
		// If item wasn't created, it must be an existing directory or an IOException is thrown.
		boolean created = myRef.create( 0, 0, true );
		if ( ! created  &&  ! isDirectory() )
			throw new IOException( "Can't create directory: " + getPath() );

		return ( created );
	}


	/**
	** Delete the current target, returning true if successful or false if not.
	** If unsuccessful, the reason for failure is unknowable.
	** Aliases are not resolved.
	** If the target is a directory, it must be empty in order to be deleted.
	**<p>
	** Calls SecurityManager.checkDelete().
	**
	** @see java.io.File#delete
	*/
	public boolean
	delete()
	{
		try
		{
			SecurityManager secure = System.getSecurityManager();
			if ( secure != null )
				secure.checkDelete( getPath() );

			myRef.reference( getPathname(), false );		// reference it w/o resolving aliases
			myRef.delete();

			return ( true );
		}
		catch ( IOException why )
		{ /* FALL THRU */ }
		return ( false );
	}


	/**
	** Rename the current target's leaf-item, throwing an IOException on failure.
	** Aliases are not resolved.
	** when successful, the target's leaf-name becomes the given name.  This differs from java.io.File.
	** Also unlike File.renameTo(), this method throws an IOException for errors.
	**<p>
	** Unlike File.renameTo(), this method will only rename an item without moving it.
	** To move an item without renaming it, use moveTo().
	** To do both, use both methods.
	**
	** @see java.io.File#renameTo()
	** @see moveTo()
	*/
	public void
	renameLeaf( String newName )
	  throws IOException
	{
		target( false, false ).rename( newName );

		// If the above worked, swap newName into target's last position.
		getPathname().swap( newName );
	}


	/**
	** Move the current target to a new location on the target's volume, throwing an IOException on failure.
	** The given destination must be an existing directory or disk-volume.
	** Aliases are not resolved.
	** If the current target-item is a directory, the entire sub-tree is moved to the new location.
	** When successful, the active target will refer to the moved item at its new location.
	** This differs from java.io.File.
	**<p>
	** You can't move items across volumes, only on the same volume.
	** To move across volumes, you can use FileHelper.duplicate() to copy items one at a time.
	** You can't use this method to rename an item -- use renameLeaf() for that.
	**
	** @see #renameLeaf()
	** @see java.io.File#renameTo()
	** @see glguerin.io.FileHelper#duplicate()
	*/
	public void
	moveTo( Pathname destination )
	  throws IOException
	{
		// Both the original item and the destination must be writable under security policy.
		// Though the original item isn't actually written to, we consider moving it to be a "write".
		securityCheck( getPath(), true );
		securityCheck( destination.getPath(), true );

		// Points myRef at the original.  If it doesn't exist, the FSItem.moveTo() will fail.
		target( false, false ).moveTo( destination );

		// Now change the target Pathname to refer to the item in its new location.
		Pathname original = getPathname();
		String leaf = original.last();
		original.set( destination );
		original.add( leaf );
	}



	// ###  F O R K E R   C A T A L O G - I N F O   &   A C C E S S   P R I V I L E G E S  ###

	/**
	** Get a FileInfo describing the current target, which must exist.
	** Aliases are not resolved.
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
	public FileInfo
	getFileInfo( boolean withComment )
	  throws IOException
	{
		BasicFileInfo info = new BasicFileInfo( target( false, false ).getInfo( true ) );

		// If caller wants the comment, get it separately.
		if ( withComment )
			info.setComment( myRef.getComment() );

		return ( info );
	}

	/**
	** Set the current target's FileInfo, creating an empty ordinary file if
	** the current target-file does not exist.
	** All directories leading up to the target must already exist.
	** Aliases are not resolved.
	**<p>
	** The state of isLocked() in the given info is not applied to the target file.
	** To set or clear the file-lock on a target, you must invoke setFileAccess().
	**<p>
	** If the given FileInfo has a non-zero comment, then the comment is saved
	** by calling setComment().
	** If the given FileInfo has a zero-length comment, then no comment is saved,
	** nor is any existing comment attached to the target file removed.
	** To remove a comment from an existing target, you must call setComment()
	** with a null or zero-length array.
	**<p>
	** The needed elements are copied out of the supplied FileInfo.
	** If the file doesn't exist, it is created with zero-length forks, using the current target name.
	**<p>
	** The name in the FileInfo is ignored.  You can't name or rename files with this method.
	** The fork-lengths are ignored, too.  You can't set fork-lengths with this method.
	** An IOException is thrown if the FileInfo is problematic in some way.  
	** You can't create directories with this method, though you can change the creation
	** and modification dates of existing directories with it.
	**<p>
	** SecurityManager.checkWrite() is invoked to determine if writing is allowed.
	**
	** @exception java.io.FileNotFoundException
	**   Thrown if any of the directories leading to the target-item don't exist,
	**  or the volume or drive doesn't exist.
	** @exception java.io.IOException
	**   Thrown when some other error occurs, including when access to the item is denied.
	**
	** @see #setComment
	** @see #setFileAccess
	*/
	public void
	setFileInfo( FileInfo info )
	  throws IOException
	{
		// We always need write access to the target, but only create it if it's not a directory.
		// The rule is that files will be created by setFileInfo(), but directories won't.
		// Set the info.  If target is a directory, but doesn't exist, an IOException is thrown here.
		target( true, ! info.isDirectory() ).setInfo( info );

		// Maybe set the comment, too.
		String comment = info.getComment();
		if ( comment != null  &&  comment.length() > 0 )
			myRef.setComment( comment );
	}


	/**
	** Return a FileAccess describing the current target's access privileges.
	** Aliases are not resolved.
	** The exact meaning of the values in the returned FileAccess is implementation-dependent.
	**<p>
	** This implementation returns a FileAccess with all READ privileges available, 
	** and all WRITE privileges determined by the file-lock state.
	** Non-existent items throw a FileNotFoundException.
	**
	** @exception java.io.FileNotFoundException
	**   Thrown if the target-item does not exist, or any of the directories leading to it don't exist,
	**  or the volume or drive doesn't exist.
	** @exception java.io.IOException
	**   Thrown when some other error occurs, including when access to the item is denied.
	*/
	public FileAccess
	getFileAccess()
	  throws IOException
	{
		return ( target( false, false ).getAccess() );
	}

	/**
	** Set as much as possible of the current target's access privileges from the given FileAccess
	** and the boolean flag.
	** The target must already exist; if it doesn't, it is not created, and an IOException is thrown.
	** Aliases are not resolved.
	**<p>
	** The value of isLocked is only used when the FileAccess is null.
	** Otherwise the desired state of the file-lock is presumed to be in the non-null FileAccess.
	** When the FileAccess is non-null, the boolean is a don't-care.
	**<p>
	** This implementation uses only the isLocked flag or the bit from the FileAccess,
	** ignoring all other values in the FileAccess.
	**
	** @exception java.io.FileNotFoundException
	**   Thrown if the target-item does not exist, or any of the directories leading to it don't exist,
	**  or the volume or drive doesn't exist.
	** @exception java.io.IOException
	**   Thrown when some other error occurs, including when access to the item is denied.
	*/
	public void
	setFileAccess( FileAccess desired, boolean isLocked )
	  throws IOException
	{
		// Target is for writing, but don't create.
		target( true, false ).setAccess( desired, isLocked );
	}


	// ###  F O R K E R   F I L E - C O M M E N T S  ###

	/**
	** Get the comment-text of the current target, which may be zero-length, but will never be null.
	** Aliases are not resolved.
	** All directories leading up to the target must already exist.
	**<p>
	** SecurityManager.checkRead() is invoked to determine if reading is allowed.
	**
	** @exception java.io.FileNotFoundException
	**   Thrown if the target-item does not exist, or any of the directories leading to it don't exist,
	**  or the volume or drive doesn't exist.
	** @exception java.io.IOException
	**   Thrown when some other error occurs, including when access to the item is denied.
	*/
	public String
	getComment()
	  throws IOException
	{
		return ( target( false, false ).getComment() );
	}

	/**
	** Set the comment-text of the current target, but DO NOT create the target if it doesn't exist.
	** Aliases are not resolved.
	** All directories leading up to the target must already exist.
	**<p>
	** The target's comment is set by internally truncating the String's length as needed.
	** If the String is null, the comment is set to zero-length (i.e. effectively removed).
	**<p>
	** SecurityManager.checkWrite() is invoked to determine if writing is allowed.
	**
	** @exception java.io.FileNotFoundException
	**   Thrown if the target-item does not exist, or any of the directories leading to it don't exist,
	**  or the volume or drive doesn't exist.
	** @exception java.io.IOException
	**   Thrown when some other error occurs, including when access to the item is denied.
	*/
	public void
	setComment( String comment )
	  throws IOException
	{
		// Target is for writing, but don't create.
		target( true, false ).setComment( comment );
	}



	// ###  A L I A S E S  ###

	/**
	** For the current target, resolve all aliases contained therein and
	** return a new Pathname whose parts name the actual unaliased volume,
	** folders, and file of the original unaliased item.
	** The current target need not exist, but all the folders or aliases leading up to it must,
	** and all the aliases must be resolvable.
	**<p>
	** If any resolvable alias resides on an AppleShare server, the resolution process may attempt 
	** to mount that server.  This may result in a timeout if the server cannot be found (usually about 15 secs),
	** during which the computer may be unresponsive to the user.	
	**<p>
	** SecurityManager.checkRead() is invoked to determine if reading is allowed.
	** Resolving aliases requires reading the file-system.
	*/
	public Pathname
	makeResolved()
	  throws IOException
	{
		// Security check before anything else.
		securityCheck( getPath(), false );

		// Make a replica of actual target, which is subsequently modified.
		Pathname resolving = getPathReplica();
		myRef.reference( resolving, true );		// reference it while resolving aliases
		myRef.resolved( resolving );					// get what was resolved

		myRef.reference( null, false );				// forget what was resolved

		return ( resolving );
	}


	/**
	** This factory-method returns a new Alias representing the current Pathname's target,
	** which must exist and be accessible.
	** If the target does not exist or is inaccessible, an IOException is thrown.
	** If Aliases are not supported, null is returned, but the target must still be accessible.
	** The only meaning of a null return is "not provided".
	** All errors that can occur will throw an exception of some kind, and never return null.
	**<p>
	** This implementation defers to the underlying FSItem.newAlias() method for FileForker.Alias support.
	** FSItem.newAlias() is responsible for all aspects of Alias-making, though it does
	** require that it be currently referencing the original referent for which the Alias is made.
	**
	** @exception java.io.IOException
	**    Thrown when an Alias can't be created for the current target.
	** @return
	**    An Alias referring to the current target, or null if Aliases are not supported.
	*/
	public Alias
	makeAlias()
	  throws IOException
	{
		// Always call target() to reference the current Pathname target.
		// This means that an Alias-incapable FSItem might still cause an IOException,
		// such as when directories leading to the target are inaccessible.
		// This seems like a fair interpretation of Alias-making capability.

		// Always make a replica of Pathname for the Alias.
		return ( target( false, false ).newAlias( getPathReplica() ) );
	}


	/**
	** Write a platform-dependent representation of the valid Alias
	** to the FileForker's current target, which must not exist.
	** You cannot overwrite or replace any existing file with this method.
	** The resulting file refers to the Alias's original referent,
	** and can be resolved with makeResolved() or selfResolve().
	**<p>
	** The boolean preferSymlink is T to request a symlink instead of an alias-file,
	** or F to request an alias-file instead of a symlink.  The returned boolean
	** signifies the kind of file actually created: T for a symlink, F for an alias-file.
	** Regardless of what you request in preferSymlink, the resulting file is some kind
	** of "file that refers to another file" appropriate to the platform and implementation.
	** The state of the Alias's original referent when this method is invoked
	** determines the nature of the data actually written.  For example, if the Alias's
	** original referent has been removed, this method may fail.
	**<p>
	** The preferSymlink boolean is only interpreted as a request, hint, or suggestion.
	** It is not a demand.
	** A given platform and implementation may support one form but not the other,
	** or it may support both, or neither.
	** In practical terms, a symlink is resolved at the file-system level,
	** while an alias-file is not.  This is a bit vague, since different platforms do different things.
	** A useful touchstone is whether you can use the alias of a directory in the pathname of a  java.io.File.
	** If you can, and it works without having to explicitly resolve it or canonicalize it,
	** then it's probably a symlink.
	** If you have to resolve it in the File's pathname, as with Finder-aliases under Mac OS X Java,
	** then it's definitely not a symlink.
	**<p>
	** The given Alias object is not destroyed by this method, and the same Alias can be written
	** to another alias-file in another location.
	**<p>
	** The Alias itself may be internally updated by this method, and it may subsequently
	** reflect any such change as a change to its originalName().  Or it may not.
	** Alias updating is implementation-dependent.
	**<p>
	** This implementation defers to the FSItem.writeAlias() method.
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
	**    Thrown when the given Alias can't be written.
	*/
	public boolean
	createAliasFile( Alias alias, boolean preferSymlink )
	  throws IOException
	{
		// Let writeAlias() throw a ClassCastException to signal that the Alias is the wrong type.
		// We assume all such exceptions signify an illegal argument, and should
		// be turned into an IllegalArgumentException.
		try
		{
			// Point myRef at the item, which will be accessed for writing, but not created yet.
			// An Alias-incapable FSItem throws UnsupportedIOException.
			return ( target( true, false ).writeAlias( preferSymlink, getPathname(), alias ) );
		}
		catch ( ClassCastException wrongType )
		{  throw new IllegalArgumentException( "Wrong Alias type: " + alias.getClass().getName() );  }
	}




	// ###  F O R K E R   I / O - F A C T O R I E S  ###

	/**
	** A factory-method that constructs a new
	** read-only InputStream reading the current target's designated fork.
	** The target must exist and be readable.
	**<p>
	** SecurityManager.checkRead() is invoked to determine if reading is allowed.
	*/
	public InputStream
	makeForkInputStream( boolean resFork )
	  throws IOException
	{  return ( new RandomRWInputStream( makeForkRandomRW( resFork, false ) ) );  }

	/**
	** A factory-method that constructs a new
	** write-only OutputStream writing to the current target's designated fork.
	** If the target doesn't exist, it is created with default file-type and creator, and
	** an undesignated fork of zero-length.
	** If the target already exists and append is false, its designated fork is 
	** truncated to zero-length upon opening, and the undesignated fork is unaffected.
	** If append is true, its designated fork is appended to by first seeking to the end.
	** If the target exists but the designated fork is not writable, an IOException is thrown.
	** If the target's designated fork is already open for writing, an IOException is thrown.
	**<p>
	** SecurityManager.checkWrite() is invoked to determine if writing is allowed.
	*/
	public OutputStream
	makeForkOutputStream( boolean resFork, boolean append )
	  throws IOException
	{
		RandomRW fork = makeForkRandomRW( resFork, true );

		// If appending, seek to end.  If not appending, truncate to empty.
		if ( append )
			fork.seek( fork.length() );
		else
			fork.setLength( 0L );

		return ( new RandomRWOutputStream( fork ) );  
	}

	/**
	** A factory-method that constructs a new
	** RandomRW with given access to the designated fork of the current target.
	** If readWrite is false, then the target must exist, though the designated fork may be zero-length.
	** If readWrite is true and the target doesn't exist, it's created with both forks zero-length
	** and default file-type and creator.
	** If readWrite is true and the target already exists, the designated fork is not truncated.
	**<p>
	** SecurityManager.checkRead() and checkWrite() are invoked to determine if
	** reading and/or writing are allowed, according to the value of readWrite.
	** I.e. checkWrite() is called only when readWrite is true.
	*/
	public RandomRW
	makeForkRandomRW( boolean resFork, boolean readWrite )
	  throws IOException
	{
		// Prepare target elements accordingly, then open fork.
		return ( target( readWrite, true ).openFork( resFork, readWrite ) );
	}


}
