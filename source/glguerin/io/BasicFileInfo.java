/*
** Copyright 1998, 1999, 2001 by Gregory L. Guerin.
** Terms of use:
**  - Briefly: OPEN SOURCE under Artistic License -- credit fairly, use freely, alter carefully.
**  - Fully: <http://www.amug.org/~glguerin/sw/artistic-license.html>
** This file is from the MacBinary Toolkit for Java:
**   <http://www.amug.org/~glguerin/sw/#macbinary> 
*/

package glguerin.io;

import java.io.*;
import glguerin.util.SmallPoint;


// --- Revision History ---
// 20Feb99 GLG  create
// 09Mar99 GLG  rework to give getLeafName() and setLeafName()
// 17Mar99 GLG  edit doc-comments
// 26Mar99 GLG  add isAlias()
// 28Mar99 GLG  add myDirectoryFlag, boolean-arg constructor, revised methods
// 01Apr99 GLG  cover class-name change to MacFiling
// 02Apr99 GLG  expand doc-comments
// 22Apr99 GLG  cover class-name change to MacEscaping
// 26Apr99 GLG  edit doc-comments
// 14Jun99 GLG  use new setFinderIconAt() method
// 22Jun99 GLG  add getForkLength(boolean)
// 10May01 GLG  change package
// 21May01 GLG  remove direct escaping support
// 01Jun01 GLG  cut setLeafName() and setForkLength()
// 01Jun01 GLG  add constructors to take name and FileInfo
// 05Jun01 GLG  add setComment(), etc.
// 16Jan2003 GLG  add hasFinderFlags()


/**
** Simple implementation of FileInfo.
**
** @author Gregory Guerin
*/

public class BasicFileInfo
  implements FileInfo
{
	private boolean myDirectoryFlag;

	private String myLeafName = "";
	private long myDataForkLen = 0;
	private long myResForkLen = 0;
	private int myFileType = OSTYPE_UNKNOWN;
	private int myFileCreator = OSTYPE_UNKNOWN;
	private int myFinderFlags = 0;
	private boolean myLocked = false;
	private long myTimeCreated = 0;
	private long myTimeModified = 0;
	private SmallPoint myIconAt = null;
	private String myComment = "";


	/**
	** Construct an instance which will return the given flag from isDirectory(),
	** and has the given leaf name.
	** The flag will not be affected by clear() or copyFrom().
	** It is effectively immutable after the instance is constructed.
	** The name may safely be null or empty.  It's passed to setName().
	*/
	public
	BasicFileInfo( boolean isDirectory, String name )
	{
		super();  
		myDirectoryFlag = isDirectory;
		setName( name );
	}

	/**
	** Construct an instance which duplicates the FileInfo values given. 
	** The new instance correctly reflects the given FileInfo's directory flag. 
	*/
	public
	BasicFileInfo( FileInfo info )
	{
		super();  
		myDirectoryFlag = info.isDirectory();
		copyFrom( info );
	}


	/**
	** Clear all the internal values to their defaults.
	** For the file-type and file-creator, this is OSTYPE_UNKNOWN, not zero.
	** This method has no effect on the internal directory flag, so
	** isDirectory() will not change after calling this method.
	*/
	public void 
	clear()
	{
		myLeafName = "";
		myDataForkLen = myResForkLen = 0;
		myFileType = myFileCreator = OSTYPE_UNKNOWN;
		myFinderFlags = 0;
		myLocked = false;
		myTimeCreated = myTimeModified = 0;
		myComment = "";
	}

	/**
	** Copy everything possible from theInfo.
	** This copying can't change the internal directory flag.
	*/
	public void 
	copyFrom( FileInfo theInfo )
	{
		clear();
		if ( theInfo == null )
			return;

		setName( theInfo.getLeafName() );
		setForks( theInfo.getForkLength( DATAFORK ), theInfo.getForkLength( RSRCFORK ) );
		setFileType( theInfo.getFileType() );
		setFileCreator( theInfo.getFileCreator() );
		setFinderFlags( theInfo.getFinderFlags() );
		setLocked( theInfo.isLocked() );
		setTimeCreated( theInfo.getTimeCreated() );
		setTimeModified( theInfo.getTimeModified() );
		setFinderIconAt( theInfo.getFinderIconAt() );
		setComment( theInfo.getComment() );
	}


	/**
	** Set the literal leaf-name.
	** If the name is too long, or otherwise unacceptable, it may be internally truncated,
	** or it may be retained at full length.
	** A null or zero-length String are accepted without error, and mean that the
	** internal name should be erased or cleared.
	*/
	protected void 
	setName( String leafName )
	{
		if ( leafName == null )
			leafName = "";

		myLeafName = leafName;
	}

	/**
	** Set the fork lengths.
	*/
	public void 
	setForks( long dataLength, long resLength )
	{
		myDataForkLen = dataLength;
		myResForkLen = resLength;
	}

	/**
	** Keep the given String as the comment-text.
	** Null is accepted, and internally transformed into an empty String instead of null.
	*/
	public void 
	setComment( String comment )
	{
		if ( comment == null )
			comment = "";

		myComment = comment;
	}

	/**
	** Set the isLocked() state.
	*/
	public void
	setLocked( boolean isLocked )
	{  myLocked = isLocked;  }



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
	{  
		if ( myDirectoryFlag )
			return ( 0 );

		return ( resFork ? myResForkLen : myDataForkLen );
	}


	/**
	** Return the 32-bit value representing the file-type.
	** Though conventionally interpreted as 4 MacRoman characters, this is
	** actually an integer value.  If you need to display it as characters,
	** the static method MacRoman.getOSTypeString( int ) will be useful.
	**<p>
	** This implementation examines the internal directory-flag, returning
	** OSTYPE_FOLDER when true, or the last set file-type when false.
	**
	** @see glguerin.io.MacRoman#getOSTypeString
	*/
	public int 
	getFileType()
	{  return ( myDirectoryFlag ? OSTYPE_FOLDER : myFileType );  }

	/**
	** Set the file-type.
	** If this instance refers to a directory, invoking this method will not
	** affect what getFileType() returns, though it will assign the given
	** fileType to the internal field.
	*/
	public void
	setFileType( int fileType )
	{  myFileType = fileType;  }

	/**
	** Return the 32-bit value representing the file-creator.
	** Though conventionally interpreted as 4 MacRoman characters, this is
	** actually an integer value.  If you need to display it as characters,
	** the static method MacRoman.getOSTypeString( int ) will be useful.
	**<p>
	** This implementation examines the internal directory-flag, returning
	** OSTYPE_MACOS when true, or the last set creator-ID when false.
	**
	** @see glguerin.util.MacRoman#getOSTypeString
	*/
	public int 
	getFileCreator()
	{  return ( myDirectoryFlag ? OSTYPE_MACOS : myFileCreator );  }

	/**
	** Set the file-creator.
	** If this instance refers to a directory, invoking this method will not
	** affect what getFileCreator() returns, though it will assign the given
	** fileType to the internal field.
	*/
	public void
	setFileCreator( int fileCreator )
	{  myFileCreator = fileCreator;  }


	/**
	** The "usual" Finder flags (FInfo.fdFlags) are in bits 0-15;
	** The extended flags (FXInfo.fdXFlags) are in bits 16-23.
	** The remainder of the value is reserved, and should be zero for now.
	*/
	public int
	getFinderFlags()
	{  return ( myFinderFlags );  }

	/**
	** Return T only if all the 1-bits in flagsMask are set in this FileInfo's Finder flags.
	** Return F otherwise.
	*/
	public boolean
	hasFinderFlags( int flagsMask )
	{  return ( (getFinderFlags() & flagsMask) == flagsMask );  }


	/**
	** Set the Finder flags.
	*/
	public void
	setFinderFlags( int flags )
	{  myFinderFlags = flags;  }


	/**
	** Return true if locked (not writable), false if unlocked (writable).
	*/
	public boolean 
	isLocked()
	{  return ( myLocked );  }

	/**
	** Return true if this refers to a directory, false if it refers to a file.
	** This is a read-only attribute, and has no corresponding setter method.
	** This implementation returns the boolean passed in the constructor, 
	** and that flag is unaffected by clear() or copyFrom().
	*/
	public boolean 
	isDirectory()
	{  return ( myDirectoryFlag );  }


	/**
	** Return true if this refers to an alias of some sort, false if not.
	** This is intended as a convenience-method equivalent to:
	** <br>&nbsp;&nbsp;&nbsp;
	** <code> hasFinderFlags( MASK_FINDER_ISALIAS )</code>
	** <br>An implementation is free to expand on this
	** by evaluating other criteria it may have access to.
	**<p>
	** If the internal directory-flag is set, always returns false, since an alias
	** is never an actual directory, even when it refers to (points at) a directory.
	*/
	public boolean 
	isAlias()
	{  return ( myDirectoryFlag ? false : hasFinderFlags( MASK_FINDER_ISALIAS ) );  }


	/**
	** Return the creation-date,
	** measured as milliseconds before or after 01 Jan 1970 midnight UTC/GMT.
	**
	** @see glguerin.util.MacTime
	*/
	public long
	getTimeCreated()
	{  return ( myTimeCreated );  }

	/**
	** Set the date when the file was created,
	** measured as milliseconds before or after 01 Jan 1970 midnight UTC/GMT.
	**
	** @see glguerin.util.MacTime
	*/
	public void
	setTimeCreated( long millis )
	{  myTimeCreated = millis;  }

	/**
	** Return the modification-date, 
	** measured as milliseconds before or after 01 Jan 1970 midnight UTC/GMT.
	**
	** @see glguerin.util.MacTime
	*/
	public long
	getTimeModified()
	{  return ( myTimeModified );  }

	/**
	** Set the date when the file was last modified,
	** measured as milliseconds before or after 01 Jan 1970 midnight UTC/GMT.
	**
	** @see glguerin.util.MacTime
	*/
	public void
	setTimeModified( long millis )
	{  myTimeModified = millis;  }


	/**
	** Return a new SmallPoint holding
	** the XY location of the item's icon in its Finder window.
	*/
	public SmallPoint 
	getFinderIconAt()
	{  return ( new SmallPoint( myIconAt ) );  }

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
		if ( myIconAt != null )
			myIconAt.set( atXY );
		else
			myIconAt = new SmallPoint( atXY );
	}


	/**
	** Return a non-null String of the comment-text.
	*/
	public String
	getComment()
	{  return ( myComment );  }

}
