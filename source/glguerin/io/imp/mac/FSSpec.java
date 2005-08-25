/*
** Copyright 1998, 1999, 2001 by Gregory L. Guerin.
** Terms of use:
**  - Briefly: OPEN SOURCE under Artistic License -- credit fairly, use freely, alter carefully.
**  - Fully: <http://www.amug.org/~glguerin/sw/artistic-license.html>
** This file is from the MacBinary Toolkit for Java:
**   <http://www.amug.org/~glguerin/sw/#macbinary> 
*/

package glguerin.io.imp.mac;

import java.io.*;

import glguerin.util.Byter;
import glguerin.util.MacPlatform;


// --- Revision History ---
// 24Mar99 GLG  first cut
// 31Mar99 GLG  add resolve() methods that resolve aliases and set
// 05Apr99 GLG  expand doc-comments
// 12Apr99 GLG  move resolveEmbeddedAliases() here
// 13Apr99 GLG  rework as abstract but mostly-implemented base-class
// 07May99 GLG  add delete() and deleteMe()
// 07May99 GLG  add rename(), renameMe(), moveTo(), and moveMe()
// 17May01 GLG  cut some rarely-used stuff
// 17May01 GLG  add resolved() and resolveMe()
// 21May01 GLG  add NAME_LIMIT and set( int, int, byte[] )
// 22May01 GLG  cut unused methods
// 12Jun01 GLG  add setFileLock() and lockMe()
// 18Jun01 GLG  add create() and createMe()
// 21Jun01 GLG  add openFork() and openMe()
// 02Jul01 GLG  FIX: make set() limit name-length to MacPlatform.LIMIT_NAME_HFS
// 17Jan2003 GLG  add resolve() and RESOLVED


/**
** An FSSpec is a means to refer to files or folders in the file-system,
** and to resolve aliases.
** It is more than a mere FSSpec structure that you have in C, since its methods
** resolve aliases and path-names.
** The core methods that actually call the Mac OS are declared abstract here,
** and are provided by concrete sub-classes.
**<p>
** No methods in this class are synchronized, even when a thread-safety issue
** is known to exist.
** If you need thread-safety you must provide it yourself.  
** The most common uses of this class do not involve shared access
** by multiple threads, so synchronizing seemed like a time-wasting overkill.
** If you disagree, you have the source...
**<p>
** The Apple reference materials I used to make this class are:
**<ul type="disc">
**   <li> <i>Inside Macintosh: Files</i><br>
**    Chapter 2 describes an FSSpec and the FSMakeFSSpec() function.
**   </li>
**   <li> <i>Inside Macintosh: Files</i>
**    Chapter 4 is "Alias Manager". 
**   </li>
**   <li> <i>Inside Macintosh: Macintosh Toolbox Essentials</i>
**    Chapter 7 is "Finder Interface", 
**   ResolveAliasFile() is on p. 7-52. 
**   </li>
**</ul>
**
** @author Gregory Guerin
*/

abstract public class FSSpec
  extends Byter
{
	public static final int NAME_LIMIT = 63;


	/**
	** The overall size of the structure.
	*/
	public static final int SIZE = 2 + 4 + 1 + NAME_LIMIT;


	/** Returned from resolve() when an alias is resolved. */
	public static final int RESOLVED = Integer.MAX_VALUE;


	protected final int[] intRef = new int[ 1 ];

	private final short[] shortRef = new short[ 1 ];

	private final byte[] ignored = new byte[ 1 ];
	private final byte[] hadAlias = new byte[ 1 ];



	/**
	** Construct an empty FSSpec.
	*/
	protected
	FSSpec()
	{  super( SIZE );  }


	/**
	** Return the volume reference number.
	*/
	public final int
	getVRefNum()
	{  return ( getShortAt( 0 ) );  }

	/**
	** Return the parent directory ID, i.e. the dirID where the target is located.
	** If the target doesn't actually exist, the dirID represents the directory
	** where the item would exist, or where it will be created.
	*/
	public final int
	getParentDirID()
	{  return ( getIntAt( 2 ) );  }

	/**
	** Return the name of the item.
	*/
	public final String
	getName()
	{  return ( NameString.fromPStr( getByteArray(), 6 ) );  }


	/**
	** Convert to a String form containing the volRefNum, dirID, and name.
	*/
	public String
	toString()
	{
		StringBuffer build = new StringBuffer();
		build.append( "FSSpec[" ).append( getVRefNum() );
		build.append( "," ).append( getParentDirID() );
		build.append( "," ).append( getName() ).append( "]" );
		return ( build.toString() );
	}


	/**
	** Fill in the FSSpec with the information for the given target.
	** The leafName must be a Mac-native name element, NOT A PATHNAME.
	**<p>
	** If the leaf target does not exist, it is not an error.
	** If the dirID does not exist, an IOException is thrown.
	** Ditto if the volume doesn't exist or can't be mounted.
	*/
	public final void
	set( int vRefNum, int dirID, String leafName )
	  throws IOException
	{
		set( vRefNum, dirID, NameString.toPStr( leafName, MacPlatform.LIMIT_NAME_HFS ) );
	}

	/**
	** Fill in the FSSpec with the information for the given target.
	** The namePStr must be a Mac-native PString path-name.
	** It can be relative or absolute, and will be interpreted in light of the
	** other supplied args.
	**<p>
	** If the leaf target does not exist, it is not an error.
	** If any directory in the pathname does not exist, an IOException is thrown.
	** Ditto if the volume doesn't exist or can't be mounted.
	*/
	public final void
	set( int vRefNum, int dirID, byte[] namePStr )
	  throws IOException
	{
		int result = makeFSSpec( (short) vRefNum, dirID, namePStr );
		if ( result == Errors.fnfErr )
			result = 0;

		// Could always execute checkIOError(), but that would always translate the byte[]
		// into a String, which is a waste of time if the call worked.
		if ( result != 0 )
			Errors.checkIOError( result, "Can't make FSSpec", NameString.fromPStr( namePStr, 0 ) );
	}


	/**
	** Resolve the previously set() contents of this FSSpec as a possible alias file,
	** returning whether or not an alias-file was actually resolved.
	** If an alias-file was resolved, this FSSpec will be updated to refer to
	** the target (original) of the alias.
	**<p>
	** If the target of this FSSpec can't be found, but the volume and parent directory can,
	** no IOException is thrown.  Instead, this FSSpec is left pointing to the referenced target,
	** even though it does not exist.
	*/
	public boolean
	resolved()
	  throws IOException
	{
		int result = resolveMe( true, ignored, hadAlias );
		if ( result == Errors.fnfErr )
			result = 0;
		Errors.checkIOError( result, "Can't resolve", this );

		return ( hadAlias[ 0 ] != 0 );
	}

	/**
	** Resolve the previously set() contents of this FSSpec as a possible alias file,
	** returning 0 if the FSSpec was not an alias, RESOLVED if an alias was successfully resolved,
	** or an error-code if something went wrong.
	** No exception is thrown.
	*/
	public int
	resolve()
	{
		int result = resolveMe( true, ignored, hadAlias );
		if ( result == 0  &&  hadAlias[ 0 ] != 0 )
			return ( RESOLVED );
		else
			return ( result );
	}


	/**
	** Set or clear the file-locked flag, throwing an IOException if problematic.
	** The target must already exist.
	*/
	public void
	setFileLock( boolean isLocked )
	  throws IOException
	{  Errors.checkIOError( lockMe( isLocked ), "Can't change lock", this );  }



	/**
	** Create the item.
	*/
	public boolean
	create( int defaultType, int defaultCreator, boolean isDirectory )
	  throws IOException
	{
		boolean created = true;

		int osErr = createMe( defaultType, defaultCreator, isDirectory );
		if ( osErr == Errors.dupFNErr )
		{  osErr = 0;  created = false;  }

		Errors.checkIOError( osErr, "Can't create", this );
		return ( created );
	}


	/**
	** Return refNum, or throw IOException.
	** When resFork is false, open the data-fork; when true, open the resource-fork.
	*/
	public int
	openFork( boolean resFork, boolean forWriting )
	  throws IOException
	{
		byte permission = (byte) ( forWriting ? 0x03 : 0x01 );	// fsRdWrPerm : fsRdPerm
		Errors.checkIOError( openMe( resFork, permission, shortRef ), "Can't open", this );
		return ( shortRef[ 0 ] );
	}


	/**
	** Delete the current target (both forks), throwing an IOException if problematic.
	** The target must already exist (duh).
	** If the target is a directory, it must be empty.
	*/
	public void
	delete()
	  throws IOException
	{  Errors.checkIOError( deleteMe(), "Can't delete", getName() );  }


	/**
	** Rename the referenced item, throwing an IOException if problematic.
	** The current item must already exist (duh), and no item with newName can exist.
	*/
	public void
	rename( String newName )
	  throws IOException
	{
		byte[] namePStr = NameString.toPStr( newName, MacPlatform.LIMIT_NAME_HFS );
		Errors.checkIOError( renameMe( namePStr ), "Can't rename", newName );
	}


	/**
	** Move the current target to a destination on the same volume,
	** which must be a directory, throwing an IOException if problematic.
	** The destination must be an existing directory.
	*/
	public void
	moveTo( FSSpec destination )
	  throws IOException
	{
		int result = moveMe( destination.getByteArray() );
		Errors.checkIOError( result, "Can't move", this.toString() + " --> " + destination.toString() );
	}




	/**
	** Make an FSSpec from the given args, placing the result in this FSSpec,
	** without resolving any aliases.
	** Return an OSError value as the result.
	**<p>
	** This method will typically be implemented by calling the function:<br>
	**   FSMakeFSSpec() from Chapter 2 of<br>
	**   <i>Inside Macintosh: Files</i><br>
	**   with the function itself located in <b>InterfaceLib</b>.
	*/
	abstract protected int
	makeFSSpec( short vRefNum, int dirID, byte[] namePStr );


	/**
	** Create the item.
	** Return an OSErr value as the result.
	** Does not change this FSSpec, which will still refer to the now-existing item.
	*/
	abstract protected int
	createMe( int defaultType, int defaultCreator, boolean isDirectory );

	/**
	** Open this item's designated fork.
	*/
	abstract protected int
	openMe( boolean resFork, byte permission, short[] refNum );


	/**
	** Lock or unlock the file or directory referenced by the current FSSpec contents,
	** without resolving any aliases.
	** Return an OSErr value as the result.
	** Does not change this FSSpec, which will still refer to the old location.
	**<p>
	** This method will typically be implemented by calling the function:<br>
	**   FSpSetFLock() or FSpRstFLock() from Chapter 2 of<br>
	**   <i>Inside Macintosh: Files</i><br>
	**   with the function itself located in <b>InterfaceLib</b>.
	*/
	abstract protected int
	lockMe( boolean state );

	/**
	** Resolve the current FSSpec contents as a possible alias-file.
	** Return an OSError value as the result.
	**<p>
	** This method will typically be implemented by calling the function:<br>
	**   ResolveAliasFile() from Chapter 7 "Finder Interface" of<br>
	**   <i>Inside Macintosh: Macintosh Toolbox Essentials</i><br>
	**   with the function itself located in <b>InterfaceLib</b>.
	*/
	abstract protected int
	resolveMe( boolean resolveChains, byte[] targetIsFolder, byte[] wasAliased );

	/**
	** Delete the file or directory referenced by the current FSSpec contents,
	** without resolving any aliases.
	** Return an OSError value as the result.
	**<p>
	** This method will typically be implemented by calling the function:<br>
	**   FSpDelete() from Chapter 2 of<br>
	**   <i>Inside Macintosh: Files</i><br>
	**   with the function itself located in <b>InterfaceLib</b>.
	*/
	abstract protected int
	deleteMe();

	/**
	** Rename the file or directory referenced by the current FSSpec contents,
	** without resolving any aliases.
	** Return an OSError value as the result.
	** Does not change this FSSpec, which will still refer to the old name.
	**<p>
	** This method will typically be implemented by calling the function:<br>
	**   FSpRename() from Chapter 2 of<br>
	**   <i>Inside Macintosh: Files</i><br>
	**   with the function itself located in <b>InterfaceLib</b>.
	*/
	abstract protected int
	renameMe( byte[] namePStr );

	/**
	** Move the file or directory referenced by the current FSSpec contents,
	** without resolving any aliases.
	** Return an OSError value as the result.
	** Does not change this FSSpec, which will still refer to the old location.
	**<p>
	** This method will typically be implemented by calling the function:<br>
	**   FSpCatMove() from Chapter 2 of<br>
	**   <i>Inside Macintosh: Files</i><br>
	**   with the function itself located in <b>InterfaceLib</b>.
	*/
	abstract protected int
	moveMe( byte[] destFSSpec );

}
