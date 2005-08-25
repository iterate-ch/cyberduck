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
// 04Dec2002 GLG  create
// 05Dec2002 GLG  more work
// 06Dec2002 GLG  still more work
// 08Dec2002 GLG  add aliasInfo to constructor; getter method
// 10Dec2002 GLG  move mapAliasInfo() here, as support for getAliasType()
// 10Dec2002 GLG  add getAliasType()
// 11Dec2002 GLG  change package name
// 13Dec2002 GLG  add getCapabilities()
// 13Dec2002 GLG  remove 'final' on class to allow subclassing (for the daring)
// 01Jan2003 GLG  add always-false update() stub
// 02Jan2003 GLG  add non-stub update() support
// 04Jan2003 GLG  more work on update()
// 20Jan2003 GLG  add getAliasHandleData() and supporting native methods
// 24Jan2003 GLG  cut update()


/**
** TinAlias is the FileForker.Alias for MacOSXForker.
** It maintains an AliasHandle internally, in addition to a Pathname
** and a couple of FileInfo's.
** 
**
** @author Gregory Guerin
*/

public class TinAlias
  extends FileForker.Alias
{
	// ## Rely on MacOSXForker to load JNI library that has my native functions in it.

	private int aliasHand;  // actually an opaque token used by TinFSRefItem
	private Pathname pathname;
	private FileInfo targetInfo, aliasInfo;

	private String pathStr;

	/** Lazily instantiated in update().  Must be a TinFSRefItem. */
//	private FSItem tinRef;


	/**
	** Only constructor.  Not accessible outside package or subclasses.
	** The Pathname and FileInfo's are
	** always unshared instances, which this Alias can keep and modify.
	*/
	protected
	TinAlias( int aliasHandle, Pathname path, FileInfo targetInfo, FileInfo aliasInfo )
	{
		super();
		aliasHand = aliasHandle;
		pathname = path;
		this.targetInfo = targetInfo;
		this.aliasInfo = aliasInfo;
		pathStr = path.getPath();
	}


	/** Return assigned AliasHandle, masquerading as an 'int'. */
	protected int
	getAliasHandle()
	{  return ( aliasHand );  }


	/**
	** Create an array of icon-hints, with an empty slot at [0] for
	** TinFSRefItem.createAliasFile() to return a value.
	**<br> iconHints[0]: empty slot
	**<br> iconHints[1]: creator-hint or 0
	**<br> iconHints[2]: type-hint or 0
	**<p>
	** Current status: no hints in returned array.
	** Icon Services functions called in TinFSRefItem.createAliasFile()'s
	** native code suffices for Mac OS 10.2 and 10.1.
	** It will probably suffice for 10.0, too, but need a different Icon Services function-call.
	*/
	protected int[]
	makeIconHints()
	{
		int[] hints = new int[ 3 ];

		return ( hints );
	}


	/**
	** Sets result's filetype and creator, but not its Finder-flags or any other field.
	** If destroy()'ed, then result is unchanged.
	*/
	protected void
	mapAliasInfo( FileInfo result )
	{
		// If aliasInfo was assigned, use its type and creator exactly as given.
		FileInfo info = aliasInfo;
		if ( info != null )
		{
			result.setFileType( info.getFileType() );
			result.setFileCreator( info.getFileCreator() );
			return;
		}

		// Getting here, we have to map original's info into an alias-type.
		// By default: result has same type & creator as original.
		info = targetInfo;
		if ( info == null )
			return;

		result.setFileType( info.getFileType() );
		result.setFileCreator( info.getFileCreator() );

		// Treat all originals that are directories as folders, never as bundles.
		if ( info.isDirectory() )
		{
			result.setFileType( 0x66647270 );  // 'fdrp'
			result.setFileCreator( FileInfo.OSTYPE_MACOS );  // 'MACS'
		}
		else if ( info.getFileType() == 0x4150504C )  // an 'APPL'-type file
		{
			// Classical applications have file-type 'adrp', creator = app-signature.
			result.setFileType( 0x61647270 );  // 'adrp'
		}
	}


	/**
	** Return the platform-dependent pathname of the original referent,
	** in a form appropriate for a java.io.File or a suitably platform-aware Pathname.
	** Initially, this is the value of getPath() of the FileForker at the time the
	** Alias was made by makeAlias().
	**<p>
	** Returns null after destroy().
	*/
	public String
	originalPath()
	{  return ( pathStr );  }


	/**
	** Return an identifying value representing the type of this Alias.
	** The value may legitimately be zero, even when the implementation supports file-types.
	** Certain specific values have meanings
	** as described for the OSTYPE_XXX named constants in this class.
	**<p>
	** Returns -1 after destroy().
	*/
	public int
	getAliasType()
	{
		// Create a temporary FileInfo, with -1 as file-type.
		FileInfo result = new BasicFileInfo( false, "" );
		result.setFileType( -1 );
		mapAliasInfo( result );
		return ( result.getFileType() );
	}


	/**
	** Return a set of bits, signifying capabilities, in an int.
	**<p>
	** This imp can make alias-files and symlinks, so return
	** CAN_ALIAS_FILE | CAN_SYMLINK.
	*/
	public int
	getCapabilities()
	{  return ( CAN_ALIAS_FILE | CAN_SYMLINK );  }


	/**
	** Destroy all the internal elements of this Alias, making it unusable.
	** Calling destroy() more than once on the same Alias is always harmless.
	**<p>
	** The number and kind of internal resources for an Alias is implementation-dependent.
	** You can call this method to speed up the freeing of an Alias's internal resources.
	** This would be wise if you're creating lots of Alias'es and the GC'er isn't working fast enough.
	*/
	public void
	destroy()
	{
		pathname = null;
		pathStr = null;
		targetInfo = aliasInfo = null;

		int hand = aliasHand;
		aliasHand = 0;
		if ( hand != 0 )
			freeHand( hand );

		return;
	}


	/**
	** Return the contents of the internal aliasHand, or null if destroyed.
	** Public visibility so package outsiders can use it.
	*/
	public byte[]
	getAliasHandleData()
	{
		byte[] bytes = null;
		int hand = aliasHand;
		if ( hand != 0 )
		{
			int len = getHandleSize( hand );
			if ( len >= 0 )
			{
				bytes = new byte[ len ];
				getHandleData( hand, bytes, 0, len );
			}
		}

		return ( bytes );
	}




	// ###  J N I   F U N C T I O N   B I N D I N G S  ###

	/*
	**xxx
	**
	** REFERENCES:
	** -- On synchronized static native methods:
	**		JNI Pgmr's Guide & Spec: section 8.1.2, p.95
	*/

	/**
	 * @return  size, always less than 2 GB; or negative error-code
	## int getHandleSize( int anyHand );
	 */
	private static native int
		getHandleSize( int anyHand );

	/**
	 * @return  result-code
	## int getHandleData( int anyHand, byte[] bytes, int offset, int count );
	 */
	private static native int
		getHandleData( int anyHand, byte[] bytes, int offset, int count );

	/**
	 * @return  result-code
	 */
	private static native int
		freeHand( int aliasHand );

}
