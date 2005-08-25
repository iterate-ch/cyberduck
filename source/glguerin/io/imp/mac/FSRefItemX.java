/*
** Copyright 2001, 2002, 2003 by Gregory L. Guerin.
** Terms of use:
**  - Briefly: OPEN SOURCE under Artistic License -- credit fairly, use freely, alter carefully.
**  - Fully: <http://www.amug.org/~glguerin/sw/artistic-license.html>
** This file is from the MacBinary Toolkit for Java:
**   <http://www.amug.org/~glguerin/sw/#macbinary> 
*/

package glguerin.io.imp.mac;

import java.io.*;

import glguerin.io.*;
import glguerin.util.Byter;


// --- Revision History ---
// 03Jan2003 GLG  factor out as base class for .ten.FSRefItem10 and .macosx.TinFSRefItem
// 04Jan2003 GLG  add referTo(), rework probeAppBundle()
// 06Jan2003 GLG  rework probeAppBundle() to not throw IOException
// 09Jan2003 GLG  add additional checks to possibleAppBundle()
// 13Jan2003 GLG  FIX: make probeAppBundle() accept PkgInfo files longer than 8 bytes
// 21Jan2003 GLG  add experimental override of refPart()
// 22Jan2003 GLG  eliminate override of refPart()
// 22Jan2003 GLG  restructure refRoot() a little
// 24Jan2003 GLG  cut bootItem() and bootRef() as abstract methods
// 11Feb2003 GLG  move whichFSRef() here so TenForker imp can use it


/**
** FSRefItemX is a specialized subclass of FSRefItem, designed for Mac OS X.
** It knows how Mac OS X's Unix-style pathnames relate to the FSRef API's
** representation of classically named volumes.  It also knows something about
** symlinks, mount-points, and other things in the FSRef file-system representation
** that should be specially handled as if they were Unix-like things.
**<p>
** FSRefItemX is also responsible for transforming classical Pathname assemblages
** into the Unix-style form that Mac OS X understands.
** It has a primitive, even naive, belief that any mounted volume (other than the boot-vol)
** has a pathname originating in the "/Volumes/" directory.
** This may not be true, but I know of no other way to handle this.
**<p>
** FSRefItemX provides some underlying methods for doing FileForker.Alias'es on Mac OS X.
** It does not define newAlias() or makeAlias(), leaving those for the concrete class to provide.
** The methods of interest to Alias'es are 
** possibleAppBundle(), probeAppBundle(), and referTo().
** 
**
** @author Gregory Guerin
*/

abstract public class FSRefItemX
  extends FSRefItem
{
	/**
	** Construct an empty FSRefItemX.
	*/
	protected
	FSRefItemX()
	{  super();  }


	/**
	** Get the comment, or return an empty String.
	** Never returns null.
	**<p>
	** Easy: no comments on Mac OS X.
	*/
	public String
	getComment()
	  throws IOException
	{  return ( "" );  }

	/**
	** Set or remove the current target's comment.  
	** If comment is null or zero-length, any existing comment is removed.
	** If comment is 1 or more bytes, the comment is set.
	**<p>
	** Easy: no comments on Mac OS X.
	*/
	public void
	setComment( String comment )
	  throws IOException
	{  return;  }



	/** Return the literalized name of the boot volume, or throw an IOException. */
	abstract protected String
	bootName()
	  throws IOException;


	/**
	** Set this FSRefItem so it references the root item for the given Pathname,
	** and returning the index of the Pathname part
	** whose String should first be passed to refItem().
	*/
	protected int
	refRoot( Pathname path )
	  throws IOException
	{
		// Cover the following cases:
		//   a) empty Pathnames --  "/" refers to "<Boot>:"
		//   b) root Pathnames -- "/part" will eventually refer to "<Boot>:part"
		//   c) "Volumes" Pathnames -- "/Volumes/volName" refers to "volName:"
		//   d) "Volumes" items -- "/Volumes/name" not referring to a mounted vol

		// This FSRefItem is not referencing anything yet.
		isReferenced = false;

		// If fewer than 2 parts, then target will be on boot-vol.
		// If 2 or more parts, then could be "/Volumes/volName" pattern.
		// If 1st part is "Volumes", then 2nd part is the alleged vol-name.
		// If 2nd part isn't really a vol-name, then fall through to item on boot-vol.
		if ( path.count() >= 2  &&  same( "Volumes", path.part( 0 ) ) )
		{
			// Look for matching vol-name.  On success, return immediately.
			// On failure, fall through to handle target on boot-vol.
			if ( rootRef( path.part( 1 ), myRef1 ) == 0 )
			{
				isReferenced = true;
				return ( 2 );
			}
		}

		// If not on a "/Volumes" volume, then it must be on boot-volume.
		String bootName = bootName();

		// Make the primary FSRef refer to the root dir of that volume.
		// The rootRef() method must also set myChars appropriately.
		check( rootRef( bootName, myRef1 ) );

		// Getting here, the data in myRef1 references an existing item.
		isReferenced = true;

		// Remainder of path starts at part 0.
		return ( 0 );
	}


	/**
	** The implementation of resolved() for Mac OS X must take the non-classical
	** pathname conventions into account.  That is, the use of "/Volumes" and of
	** boot-referenced names.  This is the converse of what refRoot() has to do.
	*/
	public void
	resolved( Pathname path )
	  throws IOException
	{
		// Exceptions thrown from intermediate points must leave Pathname clear.
		try
		{
			// Start with classically formed Pathname, then reverse() it.
			// We always alter the Pathname, so reversing
			// it brings the relevant parts into altering position.
			super.resolved( path );

//			System.err.println( " * FSRefItemX.resolved(): " + path.toString() );

			path.reverse();

			// If item is located on the boot volume, cut that part of Pathname.
			// Else item's not on the boot volume, so add "Volumes" to Pathname.
			if ( same( bootName(), path.last() ) )
				path.cut();
			else
				path.add( "Volumes" );

			// Reverse again to get proper sequence.
			path.reverse();
		}
		catch ( IOException why )
		{
			path.clear();
			throw why;
		}
	}



	/**
	** Support method for implementing newAlias().
	** This method examines the given FileInfo and returns T if
	** it's a possible app-bundle, or it returns F if it definitely isn't an app-bundle.
	** This method must not modify the current target in myRef1,
	** nor anything in the targetInfo.
	**<p>
	** This test should be fast, because it may be done on many files and directories.
	** It should identify and reject ordinary folders as quickly and frequently as possible.
	** The better it is at rejecting non-bundle directories, the faster newAlias() will be,
	** and the faster FileForker.makeAlias() will be.
	**<p>
	** The purpose of this method is rapid rejection of candidates.
	** If it's uncertain whether something is an app-bundle, better to return T
	** and let probeAppBundle() do its more extensive qualification.
	**<p>
	** This implementation does not rely solely on the bundle-bit to distinguish app-bundles.
	** Either the bundle-bit has to be set, or the FileInfo's leaf-name must have the suffix, or both.
	** If neither the bundle-bit is set nor the suffix is present, F is returned.
	** Also, the targetInfo must always represent a directory,
	** regardless of anything else.
	**<p>
	** The suffix must not be null, but it may be empty.  If empty, every bundle-bitted
	** directory will return T, which will cause the slower probeAppBundle() to be called later.
	**<p>
	** This method is public because a class other than a lineal subclass may have to call it.
	** For example, a FileForker.Alias subclass might need to.
	**
	** @see #probeAppBundle
	*/
	public boolean
	possibleAppBundle( FileInfo targetInfo, String suffix )
	{
		if ( targetInfo.isDirectory() )
		{
			if ( (targetInfo.getFinderFlags() & targetInfo.MASK_FINDER_HASBUNDLE) != 0
					||  targetInfo.getLeafName().endsWith( suffix ) )
			{  return ( true );  }
		}

		return ( false );
	}


	/**
	** Support method for implementing newAlias().
	** This method attempts to read a "Contents/PkgInfo" file
	** relative to the FSRef currently in myRef1.
	** Not finding a file with appropriate contents, it returns null.
	** Finding sufficient hallmarks of an app-bundle, it returns a FileInfo with
	** the type 'fapa' and the signature of the app itself.
	**<p>
	** This method is free to change myRef1 and myRef2, and use myInfo.
	**<p>
	** On success, a non-null FileInfo is returned, having the appropriate
	** file-type and creator for a Mac OS X app-bundle alias-file.
	** Although alias-file creation may not be supported by any given Alias imp,
	** we still have to identify an app-bundle properly.
	**<p>
	** This method is public because a class other than a lineal subclass may need to call it.
	** For example, a FileForker.Alias subclass might need to.
	**
	** @see #possibleAppBundle
	*/
	public FileInfo
	probeAppBundle()
	{
		// This FSRefItem must be valid and refer to existing item.
		// The validAndRef() method could check this, but to avoid exceptions, do tests here.
		if ( ! isValid  ||  ! isReferenced )
			return ( null );

		FileInfo aliasInfo = null;
		RandomRW pkgInfo = null;
		try
		{
			// Reference a "Contents/PkgInfo" sub-file, which must exist and be accessible.
			// Do not resolve aliases when referencing in to the file.
			// If referTo() returns F, it means "target is non-existent", so return immediately.
			// This avoids throwing an exception later on.
			if ( ! referTo( "Contents", false ) )
				return ( null );

			if ( ! referTo( "PkgInfo", false ) )
				return ( null );

			// At this point there's something named "Contents/PkgInfo", so try to read it.
			// Failure to open the file's data-fork means "not an app-bundle".
			pkgInfo = openFork( false, false );  // dataFork, readOnly

			// Required minimum length of Contents/PkgInfo's data-fork.
			// Under spec, the PkgInfo file is always exactly 8 bytes long.
			// There is the occasional exception that has a newline or other misplaced byte.
			// As long as the first 8 bytes are correct, we can overlook the mistake.
			final int LEN = 8;
			if ( pkgInfo.length() < LEN )
				return ( null );

			// Read data from file: 32-bit bundle-type, 32-bit bundle-signature.
			// Blindly assume read() will read fully in one call.
			// Not necessarily true, but it's a fairly safe simplifying assumption.
			// Since disks are block-devices, and 8 bytes is less than a block, the
			// simplifying assumption has pretty high likelihood of being true.
			Byter byter = new Byter( LEN );
			if ( pkgInfo.read( byter.getByteArray() ) != LEN )
				return ( null );

			// Bundle-type must be 'APPL'.
			if ( byter.getIntAt( 0 ) != 0x4150504C )
				return ( null );

			// Getting here, it's an app-bundle, with signature read from PkgInfo.
			// Make a BasicFileInfo for aliasInfo, using a don't-care placeholder name.
			aliasInfo = new BasicFileInfo( false, "An.app" );

			// The aliasInfo only needs a file-type & creator.
			aliasInfo.setFileType( 0x66617061 );  // 'fapa'
			aliasInfo.setFileCreator( byter.getIntAt( 4 ) );  // the app-signature
		}
		catch ( IOException why )
		{
			// Failures cause a null return, even if data was read OK.
			aliasInfo = null;
		}
		finally
		{
			// Must close data-fork if it was opened.
			// Failures cause a null return, even if data was read OK.
			if ( pkgInfo != null )
			{
				try
				{  pkgInfo.close();  }
				catch ( IOException why )
				{  aliasInfo = null;  }
			}
		}

		// At last, return any assembled FileInfo, or null.
		return ( aliasInfo );
	}


	/**
	** This is a wrapper around refItem() that returns T if the referenced item exists,
	** or F if it doesn't.  It's called from probeAppBundle() as it moves in to reference
	** the "Contents/PkgInfo" file.  It's a pretty simple thing, but putting it in a method
	** makes it easier to tweak in subclasses, and easier to understand in probeAppBundle().
	**<p>
	** The reason for having this method is so probeAppBundle() can more easily test
	** for the target's existence without having to incur a throw/catch of an IOException.
	** The rationale for doing this is that creating, throwing, and catching an exception
	** takes a fair amount of time, and we'd rather not pay that cost if we can avoid it.
	** Since an FSRefItem already knows whether the target exists or not, we can avoid the cost.
	** The isReferenced flag tells us whether myRef1 (the primary FSRef) holds the target's
	** FSRef, or whether it holds the parent FSRef and myName holds the name of the
	** non-existent item.
	**<p>
	** Design Pattern: Capitalize on what you already know.
	**<p>
	** This method may still throw an IOException, but the cases when that happens
	** should be far fewer in probeAppBundle() than if this method didn't exist.
	*/
	protected boolean
	referTo( String partName, boolean resolveAliases )
	  throws IOException
	{
		refItem( partName, resolveAliases );
		return ( isReferenced );
	}




	/**
	** This is a support method for implementing informOfChange().
	** Based on internal state and the given boolean, it returns an FSRef
	** byte[] represents the appropriate target, or null.
	**<p>
	** Determine which FSRef byte[] refers to a change-signallable target.
	** This depends on the given boolean and this object's state.
	** The returned byte[] will always be myRef1 or myRef2,
	** or it will be null.  Null is returned if no change-signal can be sent.
	**<p>
	** The FSRef byte[] chosen depends on the following state:
	**<br> invalid -- myRef1, because we don't care.
	**<br> unreferenced -- myRef1, because it's the existing parent dir.
	**<br> referencing a dir -- myRef1, because it's the dir of interest.
	**<br> referencing a file -- myRef2, because it's the file's parent dir.
	**<p>
	** The above rules are modified by 'specifically':
	**<br> specifically and invalid -- return null, since nothing is referenced.
	**<br> specifically but can't getInfo() -- return null, since the reference can't be examined.
	*/
	protected byte[]
	whichFSRef( boolean specifically )
	{
		// By default, myRef1 will be returned.
		byte[] targetRef = myRef1;

		if ( specifically )
		{
			// Caller asked for specific target, but nothing is referenced at all.
			if ( ! isValid )
				return ( null );

			// If unreferenced, parent dir is in myRef1, so do nothing else.
			if ( isReferenced )
			{
				// The target is referenced, so select an FSRef based on its brief FileInfo.
				// An IOException here would be quite unusual, yet still possible.
				try
				{
					FileInfo info = getInfo( false );
					if ( ! info.isDirectory() )
						targetRef = myRef2;
				}
				catch ( IOException why )
				{  return ( null );  }
			}
		}

		return ( targetRef );
	}


}
