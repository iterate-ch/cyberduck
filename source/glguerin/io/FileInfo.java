/*
** Copyright 1998, 1999, 2001 by Gregory L. Guerin.
** Terms of use:
**  - Briefly: OPEN SOURCE under Artistic License -- credit fairly, use freely, alter carefully.
**  - Fully: <http://www.amug.org/~glguerin/sw/artistic-license.html>
** This file is from the MacBinary Toolkit for Java:
**   <http://www.amug.org/~glguerin/sw/#macbinary> 
*/

package glguerin.io;

import java.io.IOException;

import glguerin.util.SmallPoint;


// --- Revision History ---
// 12Feb99 GLG  create
// 18Feb99 GLG  add OSTYPE_UNKNOWN
// 21Feb99 GLG  add more doc-comments
// 21Feb99 GLG  make getFinderWindowing() return an array
// 21Feb99 GLG  add Comment support
// 22Feb99 GLG  add isDirectory() as read-only attribute; setting is up to impl
// 27Feb99 GLG  add LIMIT_DATAFORK as long constant
// 01Mar99 GLG  edit doc-comments
// 09Mar99 GLG  rework to give getLeafName() and setLeafName()
// 17Mar99 GLG  edit doc-comments
// 25Mar99 GLG  add pseudo OSType constants for folders
// 26Mar99 GLG  add OSType constant for "Mac OS" owner
// 26Mar99 GLG  add isAlias()
// 01Apr99 GLG  cover class-name change to MacFiling
// 02Apr99 GLG  expand doc-comments
// 16Apr99 GLG  fix doc-comments
// 22Apr99 GLG  cover class-name change to MacEscaping
// 26Apr99 GLG  cover some name changes
// 14Jun99 GLG  replace get/setFinderWindowing() with get/setFinderIconAt()
// 22Jun99 GLG  add getForkLength(boolean)
// 10May01 GLG  change package
// 21May01 GLG  remove direct escaping support
// 31May01 GLG  revise
// 01Jun01 GLG  cut setters for unsettables: name, fork lengths
// 01Jun01 GLG  rework to use Java-epoch times for created and modified
// 01Jun01 GLG  change name to FileInfo
// 05Jun01 GLG  move all LIMIT_* constants to other classes, except LIMIT_RSRCFORK
// 05Jun01 GLG  rename getCommentBytes() to getComment()
// 05Jun01 GLG  cut setCommentBytes() entirely -- can't affect the file-system with it
// 16Jun01 GLG  remove setLocked() -- can't affect the file-system with it
// 10Dec2002 GLG  add MASK_FINDER_X_HIDESUFFIX for 10.1+'s "hide extension" option
// 16Jan2003 GLG  add hasFinderFlags()


/**
** FileInfo represents meta-data for a file or directory on the file-system.  
** Where possible, it uses conventional Java representations and types, such as
** milliseconds for times and longs for file lengths.  Note that an implementation
** may or may not support files longer than 2 GB, and this may even vary depending
** on what format the resident file's volume is in.
**<p>
** Certain elements of FileInfo, such as fork length or directory-ness, have no "setter" methods
** defined by this interface.  Implementations may provide such methods, scoped as needed.
** Or they may not.
** Within an implementation, copyFrom() must copy fork lengths across, but how it's done is 
** left to the implementation.  Copying of isDirectory() may or may not be supported, again
** according to the implementation.
**<p>
** One reason these elements have no explicit setters is that you can't affect an
** existing file by changing them here.  When applied to an existing file
** in the file-system, these values are ignored.  In short, you can't use this object
** to change the length -- or certain other characteristics -- of an existing file.
**
** @author Gregory Guerin
**
** @see glguerin.util.MacTime
*/

public interface FileInfo
{
	/**
	** Flags to some methods are F for data-fork, T for resource-fork.
	*/
	public static final boolean
		DATAFORK = false,
		RSRCFORK = true;


	/**
	** Always enforce a 16 MB limit on resource-fork length, under all circumstances.
	** This limitation is a consequence of the format of a resource-fork.
	** No valid resource-fork can or will be longer than this limit.
	*/
	public static final long LIMIT_RSRCFORK = (16 * 1024 * 1024) - 1;	



	/**
	** Clear all the current info, or set to some reasonably "empty" default.
	** For example, the file-type and file-creator may be set to OSTYPE_UNKNOWN
	** rather than cleared to zero.
	*/
	public void clear();

	/**
	** Copy all of otherInfo into this, or as much as possible.
	** If this object can set its internal isDirectory flag, then it should do so.
	** Otherwise, lacking a setDirectory() method, the isDirectory()
	** state of otherInfo is not copied to this object.
	*/
	public void copyFrom( FileInfo otherInfo );


	/**
	** Return the literal leaf name of the file, absent any path or location information.
	*/
	public String getLeafName();

	/**
	** Return the length of the designated fork.
	*/
	public long getForkLength( boolean resFork );


	/**
	** Return the 32-bit OSType value representing the file-type.
	** Though conventionally interpreted as 4 MacRoman characters, this is
	** actually an integer value.  If you need to display it as characters,
	** the static method MacRoman.getOSTypeString( int ) may be useful.
	**
	** @see glguerin.util.MacRoman#getOSTypeString
	*/
	public int getFileType();

	/**
	** Set the file-type to the given 32-bit OSType value.
	*/
	public void setFileType( int osType );

	/**
	** Return the 32-bit OSType value representing the file-creator.
	** Though conventionally interpreted as 4 MacRoman characters, this is
	** actually an integer value.  If you need to display it as characters,
	** the static method MacRoman.getOSTypeString( int ) may be useful.
	**
	** @see glguerin.util.MacRoman#getOSTypeString
	*/
	public int getFileCreator();

	/**
	** Set the file-creator to the given 32-bit OSType value.
	*/
	public void setFileCreator( int osType );

	/**
	** The OSType constant representing unknown file-type 
	** and/or unknown creator, i.e. '????'.
	*/
	public static final int OSTYPE_UNKNOWN = 0x3F3F3F3F;

	/**
	** The OSType constant representing the text file-type, i.e. 'TEXT'.
	*/
	public static final int OSTYPE_TEXT = 0x54455854;

	/**
	** The OSType constant representing folders, i.e. 'fold'.
	** I've chosen the same OSType that applications use in their bundles to
	** represent that they accept drag-n-drop folders.  Presumably, this type
	** is disallowed as an actual file-type, so ambiguity is unlikely.
	*/
	public static final int OSTYPE_FOLDER = 0x666F6C64;

	/**
	** The OSType constant representing the Mac OS as the item's owner, i.e. 'MACS'.
	** This is principally an owner/creator ID, not a file-type.
	*/
	public static final int OSTYPE_MACOS = 0x4D414353;


	/**
	** The "usual" Finder flags (FInfo.fdFlags) are in bits 0-15;
	** The extended flags (FXInfo.fdXFlags) are in bits 16-23.
	** The remainder of the value is reserved, and should be zero for now.
	** Unused or reserved flags within each sub-part should also be zero.
	*/
	public int getFinderFlags();

	/**
	** Return T only if all the 1-bits in flagsMask are set in this FileInfo's Finder flags.
	** Return F otherwise.
	**<p>
	** This is a convenience method that simplifies checking for single flags
	** or combinations of flags in the value returned by getFinderFlags().
	**
	** @see #getFinderFlags
	*/
	public boolean hasFinderFlags( int flagsMask );

	/**
	** Set the Finder flags, using the same form as getFinderFlags().
	**
	** @see #getFinderFlags
	*/
	public void setFinderFlags( int flags );

	/**
	** These mask-values isolate bit-ranges or individual bits of getFinderFlags()
	** or hasFinderFlags().
	** If a mask for a bit or bit-range does not exist, it's reserved and should be clear.
	** The meanings are those for System 7 or later.
	*/
	public static final int
		MASK_FINDER_COLOR = 0x000E,
		MASK_FINDER_ISSHARED = 0x0040,
		MASK_FINDER_NOINITS = 0x0080,
		MASK_FINDER_ISINITED = 0x0100,
		MASK_FINDER_HASCUSTOMICON = 0x0400,
		MASK_FINDER_ISSTATIONERY = 0x0800,
		MASK_FINDER_NAMELOCKED = 0x1000,
		MASK_FINDER_HASBUNDLE = 0x2000,
		MASK_FINDER_ISINVISIBLE = 0x4000,
		MASK_FINDER_ISALIAS = 0x8000;

	/**
	** These mask-values represent additional Mac OS X bit-masks for getFinderFlags()
	** or hasFinderFlags().
	** They only have meaning on Mac OS X. 
	** They may mean different things, or be reserved, on classical Mac OS.
	** Some bits are not officially documented, but have been determined empirically.
	*/
	public static final int
		MASK_FINDER_X_HIDESUFFIX = 0x0010;  // empirical only; unused in 10.0(?)


	/**
	** Return true if this FileInfo refers to a directory, false if it refers to a file.
	** This is a read-only attribute, and has no corresponding setter method.
	*/
	public boolean isDirectory();

	/**
	** Return true if this refers to an alias of some sort, false if not.
	** This is intended as a convenience equivalent to:
	** <br>&nbsp;&nbsp;&nbsp;
	** <code> (getFinderFlags() & MASK_FINDER_ISALIAS) != 0 </code>
	** <br>or the equivalent:
	** <br>&nbsp;&nbsp;&nbsp;
	** <code> hasFinderFlags( MASK_FINDER_ISALIAS )</code>
	** <br>An implementation is free to expand on this
	** by evaluating other criteria it may have access to.
	*/
	public boolean isAlias();

	/**
	** Return true if locked (not writable), false if unlocked (writable).
	*/
	public boolean isLocked();


	/**
	** Return the creation-date,
	** measured as milliseconds before or after 01 Jan 1970 midnight UTC/GMT.
	**
	** @see glguerin.util.MacTime
	*/
	public long getTimeCreated();

	/**
	** Set the date when the file was created,
	** measured as milliseconds before or after 01 Jan 1970 midnight UTC/GMT.
	**
	** @see glguerin.util.MacTime
	*/
	public void setTimeCreated( long millis );

	/**
	** Return the modification-date, 
	** measured as milliseconds before or after 01 Jan 1970 midnight UTC/GMT.
	**
	** @see glguerin.util.MacTime
	*/
	public long getTimeModified();

	/**
	** Set the date when the file was last modified,
	** measured as milliseconds before or after 01 Jan 1970 midnight UTC/GMT.
	**
	** @see glguerin.util.MacTime
	*/
	public void setTimeModified( long millis );


	/**
	** Return a new SmallPoint holding
	** the XY location of the item's icon in its Finder window.
	*/
	public SmallPoint getFinderIconAt();

	/**
	** Set the XY location of the item's icon in its Finder window.
	** A location at 0,0 lets the Finder know that it should position
	** the icon automatically.
	** Unless you are restoring a backup, you should normally use auto-positioning.
	** If you pass a SmallPoint of null, the icon position is set to 0,0.
	*/
	public void setFinderIconAt( SmallPoint atXY );


	/**
	** Return a String representing the current comment-text.
	** This String may be zero-length, but will never be null.
	*/
	public String getComment();

}
