/*
** Copyright 2002, 2003 by Gregory L. Guerin.
** Terms of use:
**  - Briefly: OPEN SOURCE under Artistic License -- credit fairly, use freely, alter carefully.
**  - Fully: <http://www.amug.org/~glguerin/sw/artistic-license.html>
** This file is from the MacBinary Toolkit for Java:
**   <http://www.amug.org/~glguerin/sw/#macbinary> 
*/

package glguerin.io;

import java.io.*;


// --- Revision History ---
// 29May01 GLG  create
// 30May01 GLG  rename to FileTools and move replicate() et al. here
// 09Jun01 GLG  change package
// 15Jun01 GLG  add note to duplicate() on intentionally non-duplicated FileAccess
// 14Nov01 GLG  revise duplicate()'s comments
// 09May2002 GLG  factor out and combining repeated code from various places
// 09May2002 GLG  replace calls to actualTarget() with calls to getPathname()
// 10May2002 GLG  add writeAllThenClose(), getInfo()
// 28Jun2002 GLG  move to glguerin.io package
// 12Dec2002 GLG  FIX: WalkAction.doNode()'s doc-comment had sense of returned boolean inverted (code correct)
// 04Feb2003 GLG  revise to use Pathname.replica()


/**
** The FileHelper class represents some features that frequently arise
** when working with a FileForker and/or a NamingStrategy, but which don't
** really belong in FileForker or NamingStrategy.  For example, duplicating a file (copying it)
** while preserving forks and metadata.
**<p>
** This class also contains some static utility methods previously found in FileTools.
** As a result, there are static and instance versions of some of these features.
** Use whichever ones make the most sense in your situation.
**
** @author Gregory Guerin
*/

public class FileHelper
{
	private FileForker myForker;

	private NamingStrategy myStrategy;


	/**
	** Make an empty FileHelper.
	** At a minimum, you must assign it a FileForker with setForker()
	** before it will do anything useful.
	*/
	public 
	FileHelper() 
	{  super();  }

	/**
	** Make a FileHelper that uses the given FileForker.
	*/
	public 
	FileHelper( FileForker forker) 
	{
		this();
		useForker( forker );
	}


	/**
	** Return the actual FileForker in use, which may be null.
	*/
	public FileForker
	getForker()
	{  return ( myForker );  }

	/**
	** Assign a FileForker for subsequent use, which may be null.
	** If the FileForker has no Pathname, a new Pathname is assigned to it.
	*/
	public void
	useForker( FileForker toUse )
	{
		// If toUse has no Pathname, make one for it.
		if ( toUse != null  &&  toUse.getPathname() == null )
			toUse.usePathname( new Pathname() );

		// Assign toUse as myForker, even if toUse is null.
		myForker = toUse;
	}


	/**
	** Return the current NamingStrategy, which may be null.
	*/
	public NamingStrategy
	getStrategy()
	{  return ( myStrategy );  }

	/**
	** Use the given NamingStrategy for subsequent operations
	** involving a NamingStrategy.
	** The given strategy may be null, which will cause applyStrategy() to fail.
	*/
	public void
	useStrategy( NamingStrategy strategy )
	{  myStrategy = strategy;  }



	/**
	** Return the actual Pathname in use, which may be null.
	** If non-null, the Pathname represents what the FileForker currently refers to.
	** Null is returned if the FileForker is null, or if its current Pathname is null.
	*/
	public Pathname
	getPathname()
	{
		FileForker forker = getForker();
		if ( forker != null )
			return ( forker.getPathname() );

		return ( null );
	}


	/**
	** Get the Pathname's current representation as a String.
	** A non-null FileForker and Pathname must be active.
	*/
	public String
	getPath()
	{  return ( getForker().getPath() );  }

	/**
	** Get the Pathname's current representation as a File.
	** A non-null FileForker and Pathname must be active.
	*/
	public File
	getPathFile()
	{  return ( new File( getPath() ) );  }


	/**
	** Set the Pathname to the path represented by the File.
	** A non-null FileForker and Pathname must be active.
	*/
	public void
	setPath( File file )
	{  getPathname().setFilePath( file );  }

	/**
	** Set the Pathname to be a copy of the given path.
	** A non-null FileForker and Pathname must be active.
	*/
	public void
	setPath( Pathname path )
	{  getPathname().set( path );  }



	/**
	** Return a FileInfo describing the target, or null if unable to get a FileInfo.
	** Since null is returned and an IOException is never thrown, the caller cannot
	** discover why a valid FileInfo can't be returned.
	** If you need to discover why not, call FileForker.getFileInfo() directly.
	*/
	public FileInfo 
	getInfo( boolean withComment ) 
	{
		try
		{  return ( getForker().getFileInfo( withComment ) );  }
		catch ( IOException why )
		{  /* FALL THRU */  }
		return ( null );
	}




	/**
	** Resolve aliases in the current Pathname according to the flag.
	** If resolveAliases is F, this method does nothing at all.
	** If resolveAliases is T, then the Pathname and FileForker are used to resolve all
	** aliases in the current Pathname.  The result goes back into the original Pathname.
	** Failures in resolving aliases throw an IOException.
	**<p>
	** This method takes a boolean arg so you can have a configurable boolean,
	** such as one assigned by a system property,
	** and you just pass its T/F value to this method.
	*/
	public void 
	resolveAliases( boolean resolveAliases )
	  throws IOException
	{
		if ( resolveAliases )
			getForker().selfResolve();
	}


	/**
	** Apply a NamingStrategy to the current FileForker's Pathname using the given parameters,
	** composing an appropriate name in its Pathname or throwing an IOException.
	** If no NamingStrategy is assigned, an IOException will be thrown.
	** For proper results, all directories in the Pathname should already exist
	** and be alias-resolved.  If they aren't, the NamingStrategy might misinterpret
	** the aliases and do something you don't expect, such as overwriting it or worse.
	**<p>
	** If cutLeaf is T, then the current leaf is cut from the Pathname before applying a strategy.
	** If cutLeaf is F, then the current Pathname is used as-is,
	** and presumably names the directory in which the strategy should be applied.
	**<p>
	** If the given leafName is null or empty, the current leaf name is used,
	** and cutLeaf should normally be T.
	**<p>
	** If the given suffix is null or empty, then no suffix is used.
	**<p>
	** If the nameLimit is 0 or negative, then the current FileForker's name-limit is used.
	**<p>
	** An IOException is thrown if the NamingStrategy forbids writing (or overwriting).
	** This can happen because the NamingStrategy is null,
	** or because a non-null NamingStrategy actively forbids overwriting.
	** So if no NamingStrategy is assigned, 
	** the result is the same as if a NamingStrategyKeep were assigned.
	*/
	public void
	compose( boolean cutLeaf, String leafName, String suffix, int nameLimit )
	  throws IOException
	{
		// If we don't have a strategy, fail.  If you want a keeping strategy, assign one.
		NamingStrategy strategy = getStrategy();
		if ( strategy == null )
			throw new IOException( "Needs a NamingStrategy" );

		Pathname path = getPathname();

		// Do not check initially for target's existence.
		// This lets strategy be applied when cutLeaf is F.

		// Prepare all args with defaults.
		if ( leafName == null  ||  leafName.length() == 0 )
			leafName = path.last();

		if ( suffix == null )
			suffix = "";

		if ( nameLimit <= 0 )
			nameLimit = getForker().getNameLimit();

		// Remove current leaf from Pathname, if requested.
		if ( cutLeaf )
			path.cut();

		// If composed is null after the strategy is finished, it means "forbid overwriting".
		// The newly composed name IS NOT added to the Pathname yet.
		String composed = strategy.composeName( path, leafName, suffix, nameLimit );

		// If composed is null at this point, the strategy we have forbids writing, so fail.
		if ( composed == null )
			throw new IOException( "Can't overwrite" );

		// Otherwise composed leaf name is valid, so add it back onto Pathname.
		path.add( composed );

		// At this point, the FileForker's Pathname holds the desired pathname,
		// but no file has been created or truncated or opened yet.
	}




	/**
	** This method calls the static walk() using the assigned FileForker.
	** @return  true if stopped by the WalkAction, false if ended normally (exhausted).
	** @see #walk
	*/
	public boolean
	walk( boolean recursive, FileHelper.WalkAction action, Pathname working )
	  throws IOException
	{  return ( walk( getForker(), recursive, action, working ) );  }


	/**
	** Duplicate the assigned FileForker's target file to the given destination Pathname.
	** This method calls the static duplicate().
	*/
	public Pathname
	duplicate( Pathname destination, boolean replace, boolean withComment, byte[] buffer )
	  throws IOException
	{  return ( duplicate( getForker(), destination, replace, withComment, buffer ) );  }


	/**
	** Duplicate the assigned FileForker's target file to the given destination Pathname,
	** applying a NamingStrategy to avoid overwriting.  This method will never replace an existing
	** file, even if the NamingStrategy allows it.
	** The original file represented by the FileForker can refer to an alias and that alias will be duplicated, 
	** however, aliases leading up to the leaf-name should already be resolved.
	** The destination Pathname must also be appropriately alias-resolved.
	** If the destination Pathname names a directory, the duplicate is placed in that directory and given
	** the same name as the original, with the NamingStrategy applied at the destination.
	** This method eventually uses the static duplicate().
	*/
	public Pathname
	duplicate( Pathname destination, boolean withComment, byte[] buffer )
	  throws IOException
	{
		// The current Pathname represents the original (input) file to be duplicated.
		// Handle the case of it being the identical Pathname assigned to the FileForker.
		Pathname original = getPathname();
		if ( original == destination )
			destination = original.replica();

		try
		{
			// Use 'destination' while composing an output-file name from current leaf name.
			// This has to cover the case of 'destination' naming a directory.
			getForker().usePathname( destination );
			if ( getForker().isDirectory() )
				compose( false, original.last(), "", 0 );
			else
				compose( true, null, "", 0 );

			// At this point, an output name has been composed in destination Pathname.
		}
		finally
		{  getForker().usePathname( original );  }

		// At this point, original Pathname is always the assigned Pathname.
		// Overwriting is forbidden here, even if the NamingStrategy did not forbid it.
		return ( duplicate( destination, false, withComment, buffer ) );
	}


	/**
	** Use the assigned NamingStrategy to compose a new name in the current location,
	** then duplicate the current target file into it. 
	** The active FileForker's parent directory should already be alias-resolved, or this method will fail.
	** A NamingStrategy must also be assigned, and it must not be an overwriting strategy.
	** This method eventually calls the static duplicate() method.
	*/
	public Pathname
	duplicate( boolean withComment, byte[] buffer )
	  throws IOException
	{
		// The current Pathname represents the original (input) file to be duplicated.
		// So get a replica of it and cut the leaf-name, then pass off to other duplicate().
		Pathname destination = getForker().getPathReplica();
		destination.cut();

		return ( duplicate( destination, withComment, buffer ) );
	}



	// ###  W A L K   A   D I R E C T O R Y   T R E E  ###

	/**
	** A WalkAction determines what is done at each tree node of a walk(),
	** and whether the tree-walk continues into sub-directories or not.
	** The implementation must never rely on a particular walking sequence,
	** since walk() is free to walk the tree in any order it wants to.
	**<p>
	** This interface represents the Visitor design pattern, where the nodes of
	** the file-system are the objects visited during a walk().
	*/
	public static interface WalkAction
	{
		/**
		** Do something with the node identified by the FileForker,
		** returning false to continue walking, or true to stop at the current node.
		** Every node visited by walk() will be passed to this method.
		*/
		abstract public boolean
		doNode( FileForker forker )
		  throws IOException;

		/**
		** The FileForker refers to a directory -- return true to walk its contents, false to not.
		** The implementation need not use the FileForker, but it can.
		** That is, the implementation can always return true, meaning that all directory nodes
		** are always recursively walked.
		**<p>
		** The walk() method only calls this method when it has a directory node, and its
		** recursive argument is true.  It's never called for non-directory nodes, nor if recursive is false.
		*/
		abstract public boolean
		shouldWalk( FileForker forker )
		  throws IOException;
	}


	/**
	** Walk a directory tree, returning true if the walk was stopped by the WalkAction,
	** or false if the walk exhausted all the available items.
	** Upon entry, the FileForker must point to an existing directory to walk.
	**<p>
	** Upon return, the FileForker will usually have a different Pathname than it started with.
	** If false is returned, the FileForker's target Pathname will be the last node seen,
	** which could be anything.
	** If true is returned, the FileForker's target Pathname will refer to the node that
	** stopped the traversal, which may be a file or a directory.
	** If an IOException is thrown, the FileForker's target Pathname will refer to the node
	** whence the exception was thrown.
	**<p>
	** Aliases are not resolved during the walk, unless the WalkAction does so.
	** Since some implementations of FileForker treat directory-aliases as directories, it is
	** possible that the tree-walk can enter an infinite recursion.  
	** Detecting and avoiding this is left to the WalkAction implementation.
	**<p>
	** Recursive walks will only occur if recursive is true.
	** When it's false, no sub-directories are ever walked.
	** When recursive is true, sub-directory walking is controlled by 
	** the shouldWalk() method of the given WalkAction.
	**<p>
	** The working Pathname is optional, and may safely be null.
	** Supplying a non-null working Pathname is more efficient if you're doing many independent
	** walks, or if you need the actual Pathname reference held by the FileForker upon return.
	** If you provide a working Pathname, it will normally be assigned as the current target Pathname
	** of the FileForker upon return.  Of course, if your WalkAction changes this, it won't be.
	**<p>
	** The sequence of node traversal during the walk is not specified.
	** In particular, neither a depth-first nor breadth-first traversal should be assumed,
	** nor is any sequence of processing guaranteed for the items within a single directory.
	** The current algorithm provides depth-first traversal using simple self-recursion.
	** This is an easy algorithm to implement, but it can be costly in call-stack depth.
	** A future algorithm may implement breadth-first traversal, or depth-first using
	** an explicit push-down stack of elements.
	** Whatever the implementation, you must never rely on a particular traversal order.
	**<p>
	** This class holds no walk() state, so many threads can be walk()'ing at once.
	** A particular implementation of WalkAction may or may not have state
	** associated with it.  Only you can determine its thread safety.
	** The FileForker, Pathname, and other args used during the walk clearly have an associated
	** state, and must not be used by more than one thread at a time.
	**
	** @return  true if stopped by the WalkAction, false if ended normally (exhausted).
	*/
	public static boolean
	walk( FileForker forker, boolean recursive, FileHelper.WalkAction action, Pathname working )
	  throws IOException
	{
		// These are considered normal "exhaustion" ends of the walk.
		if ( forker == null  ||  action == null )
			return ( false );

		// Get a replica of the original Pathname to use for walking.
		Pathname walking = forker.getPathReplica();
		if ( walking == null )
			return ( false );

		// If no working Pathname given, make one to walk entire sub-tree with.
		if ( working == null )
			working = new Pathname();

		// Use yet another Pathname to hold the list of names in the directory.
		// Its PathnameFormat is irrelevant, since we never format or parse with it.
		// As a convenience, walk it in the same order that list() originally returned.
		// Thus, we must reverse() it so the first item cut() is the first item list()'ed.
		Pathname names = new Pathname();
		names.add( forker.list() );
		names.reverse();

		// After each time around, the walking Pathname has its old leaf part cut off,
		// in preparation for a new leaf name to be add()'ed from names.
		for ( ;  names.count() != 0;  walking.cut() )
		{
			// The next leaf name goes onto walking Pathname.
			walking.add( names.cut() );

			// Replicate walking into the working Pathname, which becomes FileForker's target,
			// so Action.doNode() can't affect the walking Pathname in any way.
			working.replicate( walking );
			forker.usePathname( working );

			// Determine directory-ness now, before Action.doNode() can do anything.
			boolean isDir = forker.isDirectory();

			// If WalkAction wants to stop, we immediately return "halted" indication to caller.
			if ( action.doNode( forker ) )
				return ( true );

			// Decide whether this directory node should be recursively processed.
			// We only check with shouldWalk() if we're recursing and the node is a dir.
			// This saves needless setup if recursing is not possible.
			if ( recursive  &&  isDir )
			{
				// The target must be set again to working, in case doNode() changed it.
				working.replicate( walking );
				forker.usePathname( working );

				// If the new target should be walked, walk it.
				// If the sub-walk returns true, then we've been halted, so return true.
				if ( action.shouldWalk( forker )  &&  walk( forker, recursive, action, working ) )
					return ( true );

				// Getting here, either we didn't do a sub-walk, or we did one that wasn't halted.
				// Either way, we go on to the next node.
			}
		}

		// Getting here, the loop exhausted all nodes, so return false.
		return ( false );
	}



	// ###  D U P L I C A T E   ( C O P Y )   A   F I L E  ###

	/**
	** Duplicate the FileForker's current target file to the given destination,
	** which may be a directory or an ordinary file, optionally replacing it if it already exists.
	** Return a new Pathname holding the complete path-name of the duplicated file.
	**<p>
	** Aliases are not resolved by this method.  This makes it possible to duplicate alias-files.
	** If you need aliases resolved, do it for both the original and destination before
	** invoking this method.
	**<p>
	** This method will fail if the FileForker does not refer to an existing ordinary file
	** -- you can't duplicate directories directly using this method.  
	** To duplicate a directory and all its contents recursively,
	** define a FileUtils.WalkAction implementation that duplicates a file and/or creates a directory,
	** then pass it to the FileUtils.walk() method.
	**<p>
	** If the destination Pathname names a directory, the replica will be placed in the destination directory
	** with the same name as the original file.
	** If the destination Pathname does not name a directory, 
	** the replica will have the leaf-name of the given destination.
	** If an error occurs during duplication, an IOException is thrown and any
	** partially duplicated destination-file IS NOT deleted.
	**<p>
	** This implementation is capable of obliterating the original file 
	** when <code>replace</code> is true.
	** If the given destination refers to the original's directory or leaf-file,
	** the duplication algorithm may truncate that original when it creates the replica file.
	** In short, this algorithm does not check for name uniqueness between original and replica
	** and WILL OVERWRITE an existing destination file when <code>replace</code> is true.
	**<p>
	** The replica will not have its file-lock set, even if the original did.
	** If you want to duplicate the file-lock state, call setFileAccess() yourself after duplication.
	**<p>
	** The replica will also not have the same FileAccess privileges as the original.
	** Instead, the default FileAccess privileges as determined by the host platform are used.
	** This is intentional, since FileAccess represents ownership as well as access privileges
	** of a target file, and we probably DO NOT want to set the owner and group to be the same as
	** the original.  Indeed, a good argument can be made against duplicating the access flags, too.
	** If you want to duplicate the FileAccess, call setFileAccess() yourself after duplication.
	**<p>
	** Only the single FileForker given is used, though its active Pathname is switched as needed
	** to refer to both the original and destination.  If an exception is thrown, the FileForker's Pathname
	** is guaranteed to be restored to its original value.
	**<p>
	** This implementation just duplicates the pieces one by one in the obvious way.
	** A smarter implementation might take advantage of some special feature,
	** such as asking an AppleShare server to duplicate the file directly using an AFP call.
	** Such cleverness is left as an exercise for the interested reader.
	*/
	public static Pathname
	duplicate( FileForker forker, Pathname destination, boolean replace, boolean withComment, byte[] buffer )
	  throws IOException
	{
		if ( buffer == null )
			throw new IOException( "Null buffer" );

		// Keep the original Pathname, so we can restore it later as necessary.
		Pathname original = forker.getPathname();
		if ( original == null )
			throw new IOException( "No target" );

		FileInfo info = forker.getFileInfo( withComment );
		if ( info.isDirectory() )
			throw new IOException( "Can't be a directory: " + forker.getPath() );

		try
		{
			// Set destination as target to determine how to employ it.
			// Note that we use a replica of original destination, rather than use it directly.
			// This lets us alter the leaf-name with impunity.
			forker.setPathReplica( destination );
			Pathname replica = forker.getPathname();
			if ( forker.isDirectory() )
				replica.add( original.last() );

			// At this point, our current actual target is the desired replica.
			// If it already exists, we have more tasks before duplication.
			if ( forker.exists() )
			{
				// If replacement forbidden, throw an exception.
				// If replacement allowed, truncate both forks now, so that we
				// can exploit an optimization below.  This also ensures writability now.
				if ( ! replace )
					throw new IOException( "Can't replace: " + forker.getPath() );

				forker.makeForkOutputStream( info.DATAFORK, false ).close();
				forker.makeForkOutputStream( info.RSRCFORK, false ).close();
				forker.setComment( null );		// remove any comment
			}

			// Repoint forker to original Pathname before duplicating forks.
			forker.usePathname( original );

			// Duplicate data-fork, then resource-fork, then file-info with comment.
			// An optimization is that we only duplicate non-empty forks.
			// This avoids needless I/O of opening and closing.
			// This requires that an existing replica was already truncated earlier.
			if ( info.getForkLength( info.DATAFORK ) > 0 )
				duplicateFork( forker, replica, info.DATAFORK, buffer );

			if ( info.getForkLength( info.RSRCFORK ) > 0 )
				duplicateFork( forker, replica, info.RSRCFORK, buffer );

			forker.usePathname( replica );
			forker.setFileInfo( info );
			return ( replica );
		}
		finally
		{
			// Restore original target Pathname.
			forker.usePathname( original );
		}
	}

	
	/**
	** Duplicate a single fork from its current target to the replica
	** using the given buffer.
	** If original and replica refer to the same file, the original's fork may be truncated
	** before it has a chance to be duplicated.
	** This is usually considered A Bad Thing, so don't ask this method to do it.
	*/
	public static void
	duplicateFork( FileForker forker, Pathname replica, boolean resFork, byte[] buffer )
	  throws IOException
	{
		// At entry, forker points at original Pathname, which must be restored later.
		Pathname original = forker.getPathname();

//		System.out.println( "duplicateFork: original: " + original );

		InputStream in = null;
		try
		{
			// Nothing prevents original and replica from referring to the same file, and if they do,
			// the original will almost certainly be nuked when the output stream truncates the fork.
			// The original is only used for reading.
			in = forker.makeForkInputStream( resFork );

			// Point at replica (output file) and write to fork's output stream.
			forker.usePathname( replica );
			writeAllThenClose( in, forker.makeForkOutputStream( resFork, false ), buffer );
		}
		catch ( IOException why )
		{
			if ( in != null )
				in.close();

			throw why;
		}
		finally
		{
			// Always restore original target Pathname.
			forker.usePathname( original );
		}
	}


	/**
	** Write the entire remaining InputStream to the given OutputStream,
	** using the given non-null buffer.
	** Both streams are closed upon return, even if an IOException is thrown.
	**
	** @return  the number of bytes written.
	*/
	public static long
	writeAllThenClose( InputStream in, OutputStream out, byte[] buffer )
	  throws IOException
	{
		try
		{  return ( writeAll( in, out, buffer ) );  }
		finally
		{
			// Nested try/finally ensures 'in' is closed even when out.close() fails.
			try
			{  out.close();  }
			finally
			{  in.close();  }
		}
	}


	/**
	** Write the entire remaining InputStream to the given OutputStream,
	** using the given non-null buffer.
	** Neither stream is closed here.
	**
	** @return  the number of bytes written.
	*/
	public static long
	writeAll( InputStream in, OutputStream out, byte[] buffer )
	  throws IOException
	{
		long total = 0;
		for (;;)
		{
			int got = in.read( buffer );
			if ( got < 0 )
				break;

			out.write( buffer, 0, got );
			total += got;
		}

		return ( total );
	}


}
