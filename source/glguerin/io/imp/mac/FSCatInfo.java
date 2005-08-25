/*
** Copyright 1998, 1999, 2001 by Gregory L. Guerin.
** Terms of use:
**  - Briefly: OPEN SOURCE under Artistic License -- credit fairly, use freely, alter carefully.
**  - Fully: <http://www.amug.org/~glguerin/sw/artistic-license.html>
** This file is from the MacBinary Toolkit for Java:
**   <http://www.amug.org/~glguerin/sw/#macbinary> 
*/

package glguerin.io.imp.mac;

import java.io.IOException;

import glguerin.io.*;
import glguerin.util.Byter;
import glguerin.util.SmallPoint;
import glguerin.util.MacTime;


// --- Revision History ---
// 22Jun01 GLG  create
// 15Dec2002 GLG  change to use FileAccess.assign()
// 16Jan2003 GLG  add hasFinderFlags()


/**
** FSCatInfo is an implementation of FileInfo embodied with an underlying
** FSCatalogInfo struct in a byte-array.
** It is similar in intent to the CatalogAccess interface, but is a concrete class
** and does not include fill() or write() methods.
** Where CatalogAccess represents both a means to access catalog-into and the
** member items themselves, FSCatInfo is a more passive struct-like class.
**<p>
** Because an FSCatalogInfo holds more than just a FileInfo represents, methods
** to set and get those extra members are provided.
**<p>
** The Apple reference materials I used to build this class are:
**<ul type="disc">
**   <li><b>Files.h</b> from Universal Interfaces 3.4<br>
**    Structs, typedefs, function declarations.  
**   </li>
**   <li>xxx URL to <b>FileManger.pdf</b><br>
**    Describes the new FSRef-based API introduced with Mac OS 9.
**    The FSCatalogInfo type is nicely described here.
**   </li>
**   <li>xxx URL to <b>Carbon File Manager docs</b><br>
**    Summarizes the FSRef API and says what's in Carbon and what's not.
**    The FSCatalogInfo type is nicely described here, too.
**   </li>
**   <li>MRJ SDK JDirect-2 sample code<br>
**    The "Files.java" source contains lots of useful tidbits, including a fully dissectable
**    FSCatalogInfoStruct class.
**   </li>
**</ul>
**
** @author Gregory Guerin
**
** @see glguerin.io.FileInfo
*/

public class FSCatInfo
  extends Byter
  implements FileInfo
{
	/**
	** Pass in bitMap argument to check for existence,
	** determine whether directory or file, and whether locked or not.
	** This is also used for resolved() to back-walk out to a root.
	**<p>
	** Bitmap holds:
	** NodeFlags + Volume + ParentDirID
	*/
	public static final int GET_EXIST = 0x00000E;

	/**
	** Pass in bitMap argument to retrieve brief info.
	** The brief form returns a FileInfo where only isDirectory(), isAlias(), isLocked(),
	** Finder-flags, permissions, and fork-lengths are valid.
	** The file-type and creator are also valid, though infrequently used.
	** Neither leaf-name,
	** nor any of the time-stamps,
	** nor the extended FinderXInfo are guaranteed to be valid.
	** The FSPermissionInfo is returned in the brief form,
	** so a FileAccess can be constructed.
	**<p>
	** Bitmap holds:
	** NodeFlags + Volume + ParentDirID
	** + FSPermissionInfo + FinderInfo
	** + DataSizes + RsrcSizes
	*/
	public static final int GET_BRIEF = 0x00CC0E;

	/**
	** Pass in bitMap argument to retrieve full info.
	** This fills in all the values needed for a FileInfo, and to make a FileAccess
	** from the FSPermissionInfo.
	** No additional items not used by FileInfo or FileAccess are represented,
	** such as the AccessDate or BackupDate time-stamps, or the sharing-flags.
	**<p>
	** Bitmap holds:
	** NodeFlags + Volume + ParentDirID
	** + CreateDate + ContentMod
	** + FSPermissionInfo + FinderInfo
	** + FinderXInfo + DataSizes + RsrcSizes
	**<pre>
		  kFSCatInfoGettableInfo        = 0x0003FFFF,
		  kFSCatInfoSettableInfo        = 0x00001FE3,  // flags, dates, permissions, Finder info, text encoding
	**</pre>
	*/
	public static final int GET_FULL = 0x00DC6E;

	/**
	** Pass in bitMap argument to set full info.
	** This is ONLY the elements of a FileInfo, without affecting FileAccess elements.
	** Note that NodeFlags is also omitted, since you can't change file-lock state with a FileInfo.
	**<p>
	** Bitmap holds:
	** CreateDate + ContentMod
	** + FinderInfo
	*/
	public static final int SET_FULL = 0x000860;


	/**
	** Pass in bitMap argument when setting or getting
	** FileAccess privileges (lock + mode) only.
	** Although the entire FSPermissionInfo is retrieved with this bit-mask,
	** the only element of the FSPermissionInfo that's settable is the mode.
	** In the nodeFlags, only the lock is settable (I think), not dir-bit or any others.
	**<p>
	** Bitmap holds:
	** NodeFlags + FSPermissionInfo
	*/
	public static final int GETSET_ACCESS = 0x000402;

	/**
	** Pass in bitMap argument when setting or getting
	** FinderInfo (creator & file-type) only.
	**<p>
	** Bitmap holds:
	** FinderInfo
	*/
	public static final int GETSET_FINFO = 0x000800;



	/** Offsets in FSCatalogInfo struct. */
	private static final int
		NODE_FLAGS_AT = 0,				// a short: holds the file-lock bit, isDir bit, etc.
		VOLUME_AT = 2,					// FSVolumeRefNum: a short
		PARENT_AT = 4,						// parent Dir ID: an int
//		NODE_ID_AT = 8,					// node ID: an int
//		SHARING_FLAGS_AT = 12,		// sharing flags: a byte
//		USER_PRIVILEGES_AT = 13,		// a byte
		CREATED_AT = 16,					// UTCDateTime: a long in 48.16 fixed-point form
		MODIFIED_AT = 24,				// UTCDateTime: a long in 48.16 fixed-point form
//		ATTR_MOD_AT = 32,				// UTCDateTime: a long in 48.16 fixed-point form
//		ACCESSED_AT = 40,				// UTCDateTime: a long in 48.16 fixed-point form
//		BACKUP_AT = 48,					// UTCDateTime: a long in 48.16 fixed-point form
		// --- a 16-byte FSPermissionInfo struct:
		OWNER_ID_AT = 56 + 0,					// UInt32: an int
		GROUP_ID_AT = 56 + 4,					// UInt32: an int
//		USER_ACCESS_AT = 56 + 9,			// UInt8: a byte
		MODE_AT = 56 + 10,						// UInt16: an unsigned short
		// --- a 16-byte FileInfo/FolderInfo struct:
		FINFO_TYPE_AT = 72 + 0,				// OSType: int   
		FINFO_CREATOR_AT = 72 + 4,			// OSType: int
		FINFO_FLAGS_AT = 72 + 8,				// an unsigned short
		FINFO_VERT_AT = 72 + 10,				// Point element: a short
		FINFO_HORZ_AT = 72 + 12,				// Point element: a short
		// --- a 16-byte ExtendedFileInfo/ExtendedFolderInfo struct
		FXINFO_SCRIPT_AT = 88 + 8,			// a byte
		FXINFO_FLAGS_AT = 88 + 9,			// a byte
		// -----
		DATA_LEN_AT = 104,				// UInt64: a long, data-fork logical length
		RSRC_LEN_AT = 120,				// UInt64: a long, resource-fork logical length
		TEXT_ENCODING_HINT_AT = 140;		// an int


	/** Masks for node-flags. */
	private static final int
		NODE_LOCKED = 0x01,
		NODE_DIR = 0x10;


	/**
	** Size of the byte[] representing the struct.
	** According to JDirect2 sample code in "Files.java":
	**   sizeOfFSCatalogInfo = 144
	*/
	public static final int SIZE = 144;



	/** Leaf name for FileInfo only -- unused and unaffected by FSCatalogInfo. */
	private String myLeafName;

	/**
	** Create an empty instance with a byte[] of proper size.
	*/
	public
	FSCatInfo()
	{
		super( SIZE );
		myLeafName = "";
	}



	/**
	** Return the node-flags.
	*/
	public int 
	getNodeFlags()
	{  return ( getUShortAt( NODE_FLAGS_AT ) );  }
	
	/**
	** Return the VRefNum, i.e. the volume reference number.
	*/
	public int 
	getVRefNum()
	{  return ( getShortAt( VOLUME_AT ) );  }
	
	/**
	** Return the parent DirID, i.e. the DirID where the target is located.
	*/
	public int 
	getParentDirID()
	{  return ( getIntAt( PARENT_AT ) );  }


	// ###  F I L E - A C C E S S  ###

	/**
	** Return a new FileAccess representing the access privileges.
	** The FileInfo.isLocked() state appears in the FileAccess.IS_LOCKED privilege-bit.
	*/
	public FileAccess 
	makeFileAccess()
	{
		FileAccess access = new FileAccess( getUShortAt( MODE_AT ) );

		access.assign( isLocked(), FileAccess.IS_LOCKED );

		access.setOwnerID( getIntAt( OWNER_ID_AT ) );
		access.setGroupID( getIntAt( GROUP_ID_AT ) );

		return ( access );
	}

	/**
	** Set the node-flags and mode to match FileAccess.
	*/
	public void 
	setFileAccess( FileAccess desired )
	{
		int flags = getNodeFlags() & ~NODE_LOCKED;
		if ( desired.has( FileAccess.IS_LOCKED ) )
			flags |= NODE_LOCKED;

		putShortAt( (short) flags, NODE_FLAGS_AT );
		putShortAt( (short) desired.getPrivileges(), MODE_AT );
	}


	// ###  A S   F I L E - I N F O  ###

	/**
	** Copy all of theInfo into this, or as much as possible.  
	** The name is cleared, not copied.
	*/
	public void 
	copyFrom( FileInfo theInfo )
	{
		clear();
		if ( theInfo == null )
			return;

		setName( theInfo.getLeafName() );
		setFileType( theInfo.getFileType() );
		setFileCreator( theInfo.getFileCreator() );
		setFinderFlags( theInfo.getFinderFlags() );
		setTimeCreated( theInfo.getTimeCreated() );
		setTimeModified( theInfo.getTimeModified() );
		setFinderIconAt( theInfo.getFinderIconAt() );
	}


	/**
	** Set the leaf-name.
	** This name is only for FileInfo representation.
	** An FSCatalogInfo does not have a name field.
	*/
	protected void 
	setName( String leafName )
	{
		if ( leafName == null )
			leafName = "";

		myLeafName = leafName;
	}




	// ###  A C C E S S O R S  ###

	/**
	** Return true if locked (not writable), false if unlocked (writable).
	*/
	public boolean 
	isLocked()
	{  return ( (getNodeFlags() & NODE_LOCKED) != 0 );  }

	/**
	** Return true if this refers to a directory, false if it refers to a file.
	** This is a read-only attribute, and has no corresponding setter method.
	*/
	public boolean 
	isDirectory()
	{  return ( (getNodeFlags() & NODE_DIR) != 0 );  }

	/**
	** Return true if this refers to an alias of some sort, false if not.
	** This is intended as a convenience equivalent to:
	** <br>&nbsp;&nbsp;&nbsp;
	** <code> hasFinderFlags( MASK_FINDER_ISALIAS )</code>
	** <br>An implementation is free to expand on this
	** by evaluating other criteria it may have access to.
	** This implementation has no other criteria, and only uses the above expression.
	*/
	public boolean 
	isAlias()
	{  return ( hasFinderFlags( MASK_FINDER_ISALIAS ) );  }


	/**
	** Return the literal leaf name of the file, absent any path or location information.
	*/
	public String 
	getLeafName()
	{  return ( myLeafName );  }

	/**
	** Return the length of the designated fork.
	*/
	public long 
	getForkLength( boolean resFork )
	{  return ( getLongAt( resFork ? RSRC_LEN_AT : DATA_LEN_AT ) );  }


	/**
	** Return the 32-bit OSType value representing the file-type.
	** Though conventionally interpreted as 4 MacRoman characters, this is
	** actually an integer value.  If you need to display it as characters, you
	** should translate it into UniCode form.
	*/
	public int 
	getFileType()
	{  return ( getIntAt( FINFO_TYPE_AT ) );  }

	/**
	** Set the file-type to the given 32-bit OSType value.
	*/
	public void 
	setFileType( int osType )
	{  putIntAt(osType, FINFO_TYPE_AT );  }

	/**
	** Return the 32-bit OSType value representing the file-creator.
	** Though conventionally interpreted as 4 MacRoman characters, this is
	** actually an integer value.  If you need to display it as characters, you
	** should translate it into UniCode form.
	*/
	public int 
	getFileCreator()
	{  return ( getIntAt( FINFO_CREATOR_AT ) );  }

	/**
	** Set the file-creator to the given 32-bit OSType value.
	*/
	public void 
	setFileCreator( int osType )
	{  putIntAt( osType, FINFO_CREATOR_AT );  }


	/**
	** The "usual" Finder flags (FInfo.fdFlags) are in bits 0-15;
	** The extended flags (FXInfo.fdXFlags) are in bits 16-23.
	** The remainder of the value is reserved, and should be zero for now.
	** Unused or reserved flags within each sub-part should also be zero.
	*/
	public int 
	getFinderFlags()
	{  
		int combined = getUShortAt( FINFO_FLAGS_AT );
		combined |= getUByteAt( FXINFO_FLAGS_AT ) << 16;
		return ( combined );
	}

	/**
	** Return T only if all the 1-bits in flagsMask are set in this FileInfo's Finder flags.
	** Return F otherwise.
	*/
	public boolean
	hasFinderFlags( int flagsMask )
	{  return ( (getFinderFlags() & flagsMask) == flagsMask );  }

	/**
	** Set the Finder flags.
	** This implementation separates the combined flags and stores the pieces
	** in the appropriate places in the overall struct.
	*/
	public void 
	setFinderFlags( int flags )
	{  
		putShortAt( (short) flags, FINFO_FLAGS_AT );  
		putByteAt( (byte) (flags >> 16), FXINFO_FLAGS_AT );  
	}


	/**
	** Return the creation-date,
	** measured as milliseconds before or after 01 Jan 1970 midnight UTC/GMT.
	**
	** @see glguerin.util.MacTime
	*/
	public long 
	getTimeCreated()
	{  return ( MacTime.macUTCDateTimeToJavaMillis( getLongAt( CREATED_AT ) ) );  }

	/**
	** Set the date when the file was created,
	** measured as milliseconds before or after 01 Jan 1970 midnight UTC/GMT.
	**
	** @see glguerin.util.MacTime
	*/
	public void
	setTimeCreated( long millis )
	{  putLongAt( MacTime.javaMillisToMacUTCDateTime( millis ), CREATED_AT );  }

	/**
	** Return the modification-date, 
	** measured as milliseconds before or after 01 Jan 1970 midnight UTC/GMT.
	**
	** @see glguerin.util.MacTime
	*/
	public long
	getTimeModified()
	{  return ( MacTime.macUTCDateTimeToJavaMillis( getLongAt( MODIFIED_AT ) ) );  }

	/**
	** Set the date when the file was last modified,
	** measured as milliseconds before or after 01 Jan 1970 midnight UTC/GMT.
	**
	** @see glguerin.util.MacTime
	*/
	public void
	setTimeModified( long millis )
	{  putLongAt( MacTime.javaMillisToMacUTCDateTime( millis ), MODIFIED_AT );  }


	/**
	** Return a new SmallPoint holding
	** the XY location of the item's icon in its Finder window.
	*/
	public SmallPoint 
	getFinderIconAt()
	{  return ( new SmallPoint( getShortAt( FINFO_HORZ_AT ), getShortAt( FINFO_VERT_AT ) ) );  }

	/**
	** Set the XY location of the item's icon in its Finder window.
	** A location at 0,0 lets the Finder know that it should position
	** the icon automatically.
	** Unless you are restoring a backup, you should normally use auto-positioning.
	** If you pass a SmallPoint of null, the icon position is set to 0,0.
	*/
	public void 
	setFinderIconAt( SmallPoint atXY )
	{
		short x = 0, y = 0;
		if ( atXY != null )
		{  x = atXY.x;  y = atXY.y;  }

		putShortAt( x, FINFO_HORZ_AT );  
		putShortAt( y, FINFO_VERT_AT );  
	}


	/**
	** Return an empty String.
	*/
	public String
	getComment()
	{  return ( "" );  }

}

