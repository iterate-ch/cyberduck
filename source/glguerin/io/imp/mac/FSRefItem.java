/*
** Copyright 1998, 1999, 2001-2003 by Gregory L. Guerin.
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
// 21Jun01 GLG  second shot at defining FSRef-using class
// 22Jun01 GLG  add getName(), exactName(), volRef(), etc.
// 22Jun01 GLG  add imps of rootRef(), refItem, resolved(), get/setInfo(), get/setAccess()
// 22Jun01 GLG  add opaque Object to begin()/next()/end()
// 23Jun01 GLG  add non-existent-item code to refItem() and others
// 23Jun01 GLG  fix moveTo() by adding a 3rd FSRef: myMoveRef
// 23Jun01 GLG  fix setAccess() to start with current mode on null FileAccess arg
// 25Jun01 GLG  fix create() to establish default type and creator
// 05Jul01 GLG  add protected constants used by concrete implementations
// 28Apr2002 GLG  add internal comments to refItem()
// 04Dec2002 GLG  cut exists()
// 02Jan2003 GLG  expand scope of hadAlias to protected
// 15Jan2003 GLG  add refItem() code to resolve embedded symlinks automatically
// 16Jan2003 GLG  refactor refItem(), adding refPart() and abstract mayResolve()
// 21Jan2003 GLG  rework using hadDir instance-var
// 22Jan2003 GLG  FIX: rework refPart() to resolve BEFORE calling makeRef()


/**
** An FSRefItem is an FSItem that uses FSRef's internally.
** An FSRef is the opaque replacement for an FSSpec, introduced with the
** Carbon APIs and also available in InterfaceLib in Mac OS 9.
**<p>
** Because FSRef's are entirely opaque, they don't benefit from having their own
** abstract base class in the same way that the FSSpec class represents an FSSpec struct.
**<p>
** The Apple reference materials I used to make this class are:
**<ul type="disc">
**   <li>xxx URL to <b>FileManger.pdf</b><br>
**    Describes the new FSRef-based API introduced with Mac OS 9.
**   </li>
**   <li>xxx URL to <b>Carbon File Manager docs</b><br>
**    Summarizes the FSRef API and says what's in Carbon and what's not.
**   </li>
**</ul>
**
** @author Gregory Guerin
*/

abstract public class FSRefItem
  extends FSItem
{
	/**
	** In JDirect calls to native code, a NULL pointer has the 'int' type,
	** and contains this NULL value (0).  This is needed because a C NULL pointer
	** is not the same as a Java null reference, and I don't think the JDirect thunks convert.
	*/
	protected static final int NULL = 0;

	/**
	** The TextEncoding hint passed to FSMakeFSRefUnicode() and FSRenameUnicode().
	** The C typedef is from "TextCommon.h":
	**<br>   typedef UInt32  TextEncoding;
	*/
	protected static final int kTextEncodingUnknown = 0xFFFF;

	/**
	** The volRefNum/FSVolumeRefNum passed to FSGetVolumeInfo() to produce indexing.
	*/
	protected static final short kFSInvalidVolumeRefNum = (short) 0;



	/**
	** These are the UniChar * names used to open the data-fork and resource-fork.
	** Do not modify them.
	** Each name's length is simply the array's length.
	*/
	private static final char[]
		DF_NAME = "".toCharArray(),
		RF_NAME = "RESOURCE_FORK".toCharArray();



	/**
	** The principal and secondary opaque FSRef structs are simply byte-arrays.
	** Since an FSRef struct is always opaque, we don't have to make these be Byter
	** objects into which we put or get various member values.
	** They are just opaque "bags of bytes".
	**<p>
	** Many of the FSRef-based functions take either two FSRef-ptr parameters,
	** or take one FSRef-ptr input arg and one FSRef-ptr output arg.
	** Methods in this class will often exchange these references, so that the output
	** result from one call becomes the principal FSRef struct thereafter.
	*/
	protected byte[] myRef1, myRef2;

	/**
	** An FSRef only for doing moveTo().
	** It is null until moveTo() is invoked, then it's created and used for all moves thereafter.
	** That is, it's lazily instantiated with a lifetime equal to its FSRefItem.
	*/
	private byte[] myMoveRef;

	/**
	** Nominally an HFSUniStr255, with the first char being a char-count.
	** Also used as a UniChar buffer for calls that take separate UniChar * and count args.
	*/
	protected final char[] myChars;

	/**
	** The FSCatInfo with which all FileInfo, FileAccess, and similar operations are done.
	** Also used by imps of begin(), next(), end() to iterate over a directory.
	*/
	protected final FSCatInfo myInfo;

	/**
	** This flag is T when myRef1 refers to an existing item,
	** or F when it refers to a non-existent item in conjunction with a name in myChars.
	** It's set and cleared by refRoot() and refItem() as they walk down a Pathname.
	*/
	protected boolean isReferenced;

	/**
	** Pointer to SInt16 for various calls involving low-level functions.
	*/
	protected final short[] myRefNum;

	/** Single-element byte array. */
	protected final byte[] hadAlias;

	/** Single-element byte array. */
	protected final byte[] hadDir;


	/**
	** Construct an empty FSRefItem.
	*/
	protected
	FSRefItem()
	{
		super();
		myRef1 = new byte[ 80 ];	// an FSRef is an 80-byte opaque struct
		myRef2 = new byte[ 80 ];
		myMoveRef = null;

		myChars = new char[ 256 ];

		myRefNum = new short[ 1 ];
		hadAlias = new byte[ 1 ];
		hadDir = new byte[ 1 ];

		myInfo = new FSCatInfo();
	}



	/** Return exactName(). */
	public String
	toString()
	{
//		if ( isReferenced )
//			return ( super.toString() );
//		else
			return ( exactName() );
	}


	/** Return accent-composed name present in myChars. */
	protected String
	getName()
	{  return ( AccentComposer.composeAccents( exactName() ) );  }

	/** Return exact name present in myChars, which is represented as HFSUniStr255. */
	protected String
	exactName()
	{  return ( new String( myChars, 1, myChars[ 0 ] ) );  }



	/** Copy length-limited name into myChars, representing it as HFSUniStr255. */
	protected void
	copyName( String name )
	{
		int count = name.length();
		if ( count >= myChars.length )
			count = myChars.length;

		name.getChars( 0, count, myChars, 1 );
		myChars[ 0 ] = (char) count;
	}


	/** Swap the primary and secondary FSRefs. */
	protected void
	swapRefs()
	{
		byte[] temp = myRef1;
		myRef1 = myRef2;
		myRef2 = temp;
	}


	/**
	** Return any length-limit this implementation imposes on pathname elements.
	** This represents a limit on Pathname part Strings, not a limit on overall pathname length.
	*/
	public int
	nameLimit()
	{  return ( 255 );  }



	/**
	** Check for an error, returning normally if none or throwing an IOException if so.
	*/
	protected void
	check( int resultCode  )
	  throws IOException
	{
		Errors.checkIOError( resultCode, null, this );
	}


	/**
	** Call valid() and also check that isReference is true.
	*/
	protected void
	validAndRef()
	  throws IOException
	{
		valid();
		if ( ! isReferenced )
			check( Errors.fnfErr );		// throw a FileNotFoundException with "not found" text
	}



	/**
	** This is a helper method that a specific implementation of refRoot() can call.
	** The idea is that this method supplies the basic capability of filling in an FSRef
	** from a volume-name, while a specific refRoot() implementation supplies
	** the specific semantics for the platform.
	**<p>
	** This method makes the resultFSRef refer to the given volume's root directory.
	** Return an OSErr value as the result.
	** None of the items may be null.
	** Also sets myChars to the actual volume name matching the supplied volName.
	** Because a match involves both the case-awareness embodied in same(), and
	** the accent-composing of getName(), the name in myChars may not be an exact
	** char-by-char match to the given volName.  It is, nonetheless, the name the
	** OS knows the volume to have -- its "canonical" name, so to speak.
	**<p>
	** On pre-Ten Mac OS, I'm not sure whether duplicate volume-names will be correctly handled or not.
	** They are correctly handled on Mac OS X, because the name in "/Volumes/" is automatically uniqued
	** by appending a number to it.  This occurs even though Finder X shows the original non-uniqued name.
	** One thing is certain: this algorithm cannot distinguish volume-names which volRef() does not
	** represent with unique names.  This is a problem under the FSSpec-based APIs, too, where a
	** duplicate volume-name can cause Java programs to refer to the wrong volume.  Indeed, it's a
	** problem with ANY Java implementation, including java.io.File, which cannot handle the Mac's
	** ability to work with duplicate volume-names.
	** I expect it will always be a problem, since Java's entire conceptual framework
	** of "abstract pathnames" assumes uniquely identifiable names at every level.
	** Duplicate volume-names are just as impossible to comprehend or resolve as
	** two files in the same directory with identical names but different contents.
	*/
	protected int
	rootRef( String volName, byte[] resultFSRef )
	{
		// Call volRef()/FSGetVolumeInfo iteratively with scanning values.
		// When an appropriately named volume shows up, stop the loop.
		// If no such volume can be found, i.e. we get an error-result, 
		// then return an appropriate result-code (Errors.nsvErr).
		for ( int i = 1;  true;  ++i )
		{
			// Get the next volume's name and FSRef.
			// Any non-zero result causes an immediate non-zero return.
			int osErr = volRef( i, myRefNum, myChars, resultFSRef );
			if ( osErr != 0 )
			{
				// Return with this FSRefItem's name holding volName, so errors are informative.
				copyName( volName );
				return ( osErr );
			}

			// Use the exact HFSUniStr255 name in myChars as a trial name String.
			// If names are same(), then return "success" indication.
			if ( same( volName, exactName() ) )
				return ( 0 );

			// Also check against "literalized" name.
			if ( same( volName, getName() ) )
				return ( 0 );
		}
	}



	/**
	** Set this FSItem so it references the named item relative to its current reference,
	** resolving aliases as requested, and handling cases of non-existence properly.
	**<p>
	** This method, called by FSItem.reference(), is essentially identical for Mac OS X or Classic.
	** Contrast this with refRoot(), which has very different semantics between
	** Mac OS X and Classic, so is only defined in the specific imp for each platform.
	** Note that the Mac OS 9 imp of this method must handle the semantics of  "." and ".." itself,
	** since those items actually exist in the file-system under Mac OS X but don't exist on 9.
	*/
	protected void
	refItem( String partName, boolean resolveAliases )
	  throws IOException
	{
		// DO NOT call valid() first, since this method is called from within reference(), and
		// the validity flag won't be set until an entire Pathname is successfully referenced.
		// Must nonetheless fail cases where a nested non-existent item is referenced.
		// That is, we can only handle leaf non-existence, not nested non-existence.
		// The IOException to throw is one representing a "Directory not found" error,
		// since we're trying to treat a non-existent item as a directory.
		if ( ! isReferenced )
			check( Errors.dirNFErr );
			// will always throw an IOException, so no return needed

		// Getting here, we know that myRef1 references an item: an "apparent" directory.
		// Try going into it, one level deeper, and see whether the named part also exists.
		// For that to work, myRef1 must refer to a directory or a symlink to a directory.
		// Let refPart() sort it all out, using myRef1, myRef2, and myInfo.
		// If a symlink or alias is followed, myRef1 on return refers to the original referent.
		if ( refPart( partName ) )
		{
			// The named part exists, so swap myRef1 and myRef2.
			swapRefs();

			// Maybe attempt to resolve each alias as it appears.  Failures here are fatal.
			if ( resolveAliases )
			{
				check( resolveRef( myRef1, true, hadDir, hadAlias ) );

//				System.err.println( " * FSRefItem.refItem() for: " + partName );
//				System.err.println( " * hadDir: " + hadDir[0] + ", hadAlias: " + hadAlias[0] );
			}

			// Getting here, myRef1 references the desired existing target.
			// Also, isReferenced is still true, so we don't need to set it.
		}
		else
		{
			// The named part is inaccessible or non-existent, so keep myRef1 pointing to its
			// "apparent" dir, and set myChars to hold its desired name.  Then clear isReferenced.
			// It's possible that myRef1 doesn't refer to a directory, which is fine for now.
			copyName( partName );
			isReferenced = false;
		}
	}


	/**
	** On entry, myRef1 references an "apparent" directory, and partName is
	** presumed to name an item within it.  
	** Return T if partName really does name an item, F if it doesn't.
	** On T return, myRef2 refers to named item.
	** On T or F return, myRef1 may be alias-resolved to original referent.
	**<p>
	** If myRef1 refers to a symlink, it should be automatically resolved and myRef1
	** will refer to the original referent upon return, 
	** regardless of whether partName exists or not.
	** All resolving of myRef1 prior to calling makeRef() is controlled by mayResolve().
	**<p>
	** This method uses myRef1, myRef2, and possibly myInfo, hadDir[], and hadAlias[].
	*/
	protected boolean
	refPart( String partName )
	{
		// If myRef1 should be resolved, do it before referencing partName.
		// An imp of mayResolve() may examine myRef1's getInfo(), or other data.
		// An imp may always return T or F, too.
		if ( mayResolve() )
		{
//			System.err.println( " * FSRefItem.refPart() resolving before: " + partName );

			// Try resolving myRef1 "in place", asking resolveRef() to follow alias-chains.
			// The caller needs this to happen, whether target partName exists or not.
			// If resolveRef() fails with non-zero result-code, then myRef1 is unresolvable.
			// A non-alias in myRef1 will return 0 result-code and zero hadAlias[0].
			// A dir in myRef1 will return 0 result, zero hadAlias[0], and 1 hadDir[0].
			if ( resolveRef( myRef1, true, hadDir, hadAlias ) != 0 )
				return ( false );

//			System.err.println( " * hadDir: " + hadDir[0] + ", hadAlias: " + hadAlias[0] );
		}

		// We should now be ready to make myRef2 point to myRef1's partName.
		// If makeRef() works here, then myRef1 really did refer to a dir.
		// If makeRef() fails, then myRef1 could be referring to several kinds of things,
		// such as an unresolvable alias, an unreachable dir, or a plain file.
		if ( makeRef( myRef1, partName, myRef2 ) == 0 )
			return ( true );

		return ( false );
	}


	/**
	** Called by refPart(), which is called by refItem().
	** On entry, myRef1 references an "apparent" directory, symlink, or alias-file.
	** Return T if caller should attempt to resolve it, F if not.
	**<p>
	** An implementation may always return T, always return F, or
	** evaluate myRef1 to return T or F.
	** An imp that always returns T will
	** always resolve non-leaf symlinks and aliases.
	** An imp that always returns F will only resolve symlinks and aliases
	** when refItem() is called with resolveAliases of T.
	** An imp that evaluates myRef1 will resolve non-leaf symlinks but not aliases,
	** or vice versa, or whatever myRef1 is evaluated for.
	**<p>
	** An implementation of this method may use myRef1 but must not change it.
	** It may use or change myRef2, and also myInfo, hadDir[], and hadAlias[].
	*/
	abstract protected boolean
	mayResolve();


	/**
	** Fill in the given Pathname with the names referencing this FSItem.
	** That is, do the opposite of the most recent call to reference().
	** If the most recent reference() resolved aliases, then the given Pathname
	** is filled in with the names leading to the resolved original item.
	**<p>
	** This implementation returns a Pathname in classical Mac form, with
	** a leading volume-name in all cases.  The Mac OS X implementation uses this
	** implementation and subsequently processes the classical Pathname into its own form.
	*/
	public void
	resolved( Pathname path )
	  throws IOException
	{
		// This FSItem must be valid, and Pathname empty.
		valid();
		path.clear();

		// If not referencing the item directly, put leaf-name in path immediately.
		// The Pathname itself rejects null or empty Strings.
		if ( ! isReferenced )
			path.add( getName() );

		// The following walk out to the root must not affect myRef1.
		// It can affect myRef2 and myInfo any way it wants,
		// so copy myRef1 into myRef2 before starting loop.
		// We continue to use myRef2 to hold input and output FSRefs during the walk.
		// I really hope that FSGetCatalogInfo works when both FSRefs point to the same storage.
		System.arraycopy( myRef1, 0, myRef2, 0, myRef2.length );
		for (;;)
		{
			// Get the name and parent FSRef of what's now in myRef2.
			// The parent FSRef is left in myRef2 itself.
			check( getRefInfo( myRef2, FSCatInfo.GET_EXIST, myInfo.getByteArray(), myChars, myRef2 ) );

			// At this point, myChars holds the name of what myRef2 previously referenced.
			// Add it to the Pathname after composing its accents into "literalized" form.
			path.add( getName() );
	
			// The parent-dir will be < 2 only after we've filled myInfo with info from
			// the root-dir of a volume, whose leaf-name will be the vol-name.
			// A volume's root-dir always has a DirID of 2.
			if ( myInfo.getParentDirID() < 2 )
				break;

			// Since myRef2 already holds parent FSRef, we're ready to go around again.
		}

		// The parts were added starting with leaf, so reverse them.
		path.reverse();
	}


	/**
	** Create the current target (both forks), throwing an IOException if problematic,
	** and returning true if the target was actually created as requested.
	** If the target already exists, the existing item is left as-is and false is returned
	** -- no IOException is thrown.  This happens even if the requested item is
	** not the same kind as the existing item (directory vs. file).
	**<p>
	** If the target is indeed created, this FSItem continues to refer to that target,
	** even if creating the target changes some internal data.  In particular, this
	** method ensures that the primary FSRef refers to the created item.
	*/
	public boolean
	create( int defaultType, int defaultCreator, boolean isDirectory )
	  throws IOException
	{
		valid();

		// Must already be in the "FSRef plus name" state, not referencing the item directly,
		// in order to attempt creating it.  Otherwise we surmise that it already exists.
		boolean created = ! isReferenced;
		if ( created )
		{
			int osErr = createRef( myRef1, getName(), isDirectory, myRef2 );
			if ( osErr == Errors.dupFNErr )
			{  osErr = 0;  created = false;  }
			check( osErr );
			swapRefs();
			isReferenced = true;

			// If a file was actually created, establish its default creator and type.
			if ( created  &&  ! isDirectory )
			{
				check( getRefInfo( myRef1, FSCatInfo.GETSET_FINFO, myInfo.getByteArray(), null, null ) );
				myInfo.setFileType( defaultType );
				myInfo.setFileCreator( defaultCreator );
				check( setRefInfo( myRef1, FSCatInfo.GETSET_FINFO, myInfo.getByteArray() ) );
			}
		}

		return ( created );
	}

	/**
	** Open the designated fork for the designated access.
	** The target file must already exist.
	** No existing fork is truncated or altered, even if opened for writing.
	** It's up to the implementation to decide if multiple ForkRW's can be open
	** for writing to the same file at once.  The canonical Mac OS behavior is
	** to forbid multiple writers, but to allow any number of non-exclusive readers.
	*/
	public ForkRW
	openFork( boolean resFork, boolean forWriting )
	  throws IOException
	{
		validAndRef();

		char[] forkName = (resFork ? RF_NAME : DF_NAME );
		byte permission = (byte) ( forWriting ? 0x03 : 0x01 );	// fsRdWrPerm : fsRdPerm
		check( openRef( myRef1, forkName, permission, myRefNum ) );

		String tag = getName() + (resFork ? "(RF)" : "(DF)");
		return ( newFork( forWriting, myRefNum[ 0 ], tag ) );
	}



	/**
	** If this FSItem refers to a directory, assemble its direct (non-recursive)
	** contents as a list of names to the given Pathname, which is cleared first.
	** Any names for the directory itself or its parent are omitted.
	** Only the direct contents is listed, which is not a recursive list.
	** The Pathname is used only as a list of Strings, not as an actual Pathname.
	**<p>
	** The names are not added in any particular order.  Do not assume they are sorted.
	**<p>
	** If this FSItem refer to a non-directory or a non-existent item,
	** an IOException may be thrown, so you should first qualify the call to this method.
	**<p>
	** If we were doing something more to a directory than just listing it,
	** an Enumeration would be more appropriate.  Since all we ever want is a
	** list of names, a single method that does only that is appropriate.
	*/
	public void
	contents( Pathname collecting )
	  throws IOException
	{
		validAndRef();
		collecting.clear();

		if ( getInfo( false ).isDirectory() )
		{
			// Getting here, myRef1 reflects the target, which is a directory to list.
			// Make an opaque iterator Object referring to myRef1.
			// If the returned object is an IOException, throw it instead of iterating with it.
			Object iterator = begin( myRef1 );
			if ( iterator instanceof IOException )
				throw (IOException) iterator;

			// We produce the list by calling next() until it returns null.
			// The next() and end() methods are free to use instance variables as they wish,
			// but must not alter myRef1.
			// In particular, it's OK to alter myChars or myInfo or myRef2 during iteration.
			while ( true )
			{
				String name = next( iterator );
				if ( name == null )
					break;

				// Never add "." or "..".
				if ( ".".equals( name )  ||  "..".equals( name ) )
					continue;

				// Add literal name, which is what next() returned.
				collecting.add( name );
			}
			end( iterator );
		}
	}



	/**
	** Return either the brief or full form of a FileInfo.
	** The returned FileInfo should not be altered nor retained,
	** since it is probably a singleton internal reference.
	** That is, if you want the information to persist, copy it somewhere else.
	**<p>
	** The brief form returns a FileInfo where only isDirectory(), isAlias(), isLocked(),
	** Finder-flags, and fork-lengths are valid.  In particular, neither leaf-name,
	** nor file-type, nor creator, nor any of the time-stamps are guaranteed to be valid.
	** The FSPermissionInfo is also guaranteed to be returned in brief form,
	** though it's inaccessible through the FileInfo interface.
	**<p>
	** The full form DOES NOT include the comment, which is always separately accessed.
	*/
	public FileInfo
	getInfo( boolean full )
	  throws IOException
	{
		validAndRef();
		myInfo.setName( null );
		if ( full )
		{
			// Gets full info, and name in myChars[] is correct.
			check( getRefInfo( myRef1, FSCatInfo.GET_FULL, myInfo.getByteArray(), myChars, myRef2 ) );

			// Name in the FileInfo is the "literalized" form of the name in myChars.
			myInfo.setName( getName() );
		}
		else
			check( getRefInfo( myRef1, FSCatInfo.GET_BRIEF, myInfo.getByteArray(), null, null ) );

		return ( myInfo );
	}

	/**
	** Set the file info.
	** The target must exist, but can be a file or a directory.
	** Some elements of directories are unsettable, such as file-type and creator.
	*/
	public void
	setInfo( FileInfo info )
	  throws IOException
	{
		validAndRef();

		// Copy the desired FileInfo into myInfo, which is cleared first.
		// If info.isDirectory() was true, that state IS NOT COPIED to myInfo.
		myInfo.copyFrom( info );
		check( setRefInfo( myRef1, FSCatInfo.SET_FULL, myInfo.getByteArray() ) );
	}


	/**
	** Get a new FileAccess representing the access privileges.
	**<p>
	** On Mac OS X, the privilege bits, owner ID, and group ID are all correct.
	** If the referenced item is on an HFS volume, which does not provide this data, then
	** the defaults provided by the OS are returned (typically: rwx for all, owned by current user).
	*/
	public FileAccess
	getAccess()
	  throws IOException
	{
		validAndRef();
		check( getRefInfo( myRef1, FSCatInfo.GETSET_ACCESS, myInfo.getByteArray(), null, null ) );
		return ( myInfo.makeFileAccess() );
	}

	/**
	** Apply as much as possible of the given FileAccess to the target item.
	** If FileAccess is null, only the boolean is used, and all other privileges are unchanged.
	** If FileAccess is non-null, the boolean is not used, the lock-state is taken
	** from the FileAccess, and all settable privileges are affected.
	**<p>
	** The extent to which each FileAccess value is supported, and what occurs
	** if a value can't be set, is platform and implementation dependent.
	** On Mac OS X, only the privilege-bits are actually altered.  The owner and group ID don't matter.
	** The privilege-bits can only be altered if the process altering them is running with an
	** effective user-ID that matches the owner of the item being affected. 
	*/
	public void
	setAccess( FileAccess desired, boolean isLocked )
	  throws IOException
	{
		validAndRef();

		// A null FileAccess starts with the current access privileges, and
		// assigns the new lock-state according to isLocked.
		if ( desired == null )
		{
			desired = getAccess();
			desired.assign( isLocked, FileAccess.IS_LOCKED );
		}

		myInfo.setFileAccess( desired );
		check( setRefInfo( myRef1, FSCatInfo.GETSET_ACCESS, myInfo.getByteArray() ) );
	}



	/**
	** Delete the current target, throwing an IOException if problematic.
	** The target must already exist.
	** If the target is a directory, it must be empty.
	** If the target is a file, both forks are deleted.
	*/
	public void
	delete()
	  throws IOException
	{
		validAndRef();
		check( deleteRef( myRef1 ) );
		invalid();
	}

	/**
	** Rename the referenced item, throwing an IOException if problematic.
	** The current item must already exist and no item with newName can exist.
	** The newName is taken as a literalized name suitable for add()'ing to a Pathname.
	**<p>
	** The comments in "Files.h" for FSRenameUnicode say that the newRef pointer
	** can point to the same storage as the FSRef being renamed.  This method does
	** that, though it's probably not too significant that it does so.
	*/
	public void
	rename( String newName )
	  throws IOException
	{
		validAndRef();
		check( renameRef( myRef1, newName, myRef1 ) );
	}

	/**
	** Move the currently referenced item to the destination Pathname directory.
	** The current item must already exist, and no item of the same name can exist
	** in the destination directory.  The destination must be an existing directory.
	** On success, this FSItem will be an invalid reference.
	*/
	public void
	moveTo( Pathname destination )
	  throws IOException
	{
		// Do validation and existence test for original reference.
		// An IOException is thrown if this is an invalid FSItem or target is non-existent.
		getInfo( false );

		// We need another FSRef to do the move.
		if ( myMoveRef == null )
			myMoveRef = new byte[ myRef1.length ];

		// Remember the FSRef of item to move.
		System.arraycopy( myRef1, 0, myMoveRef, 0, myMoveRef.length );

		// This try/finally block ensures that invalid() is called no matter whether we
		// complete normally or have an IOException or an early return.
		try
		{
			// Now reference() the destination Pathname, which must be an existing directory.
			reference( destination, false );
			if ( ! getInfo( false ).isDirectory() )
				throw new IOException( "Destination must be a directory: " + destination );

			// At this point, myRef1 references the destination directory, and
			// myMoveRef references the original item, i.e. the item to move.
			moveRef( myMoveRef, myRef1 );
		}
		finally
		{
			// Always leave this in invalid state, since it internally refers to
			// the original item in its original location.
			invalid();
		}
	}




	// ####  A B S T R A C T   P R I M I T I V E S  ####

	/**
	** Fill in the name, volRefNum, and root FSRef of the indexed volume.
	*/
	abstract protected int
	volRef( int index, short[] volRefNum, char[] nameBuf, byte[] rootFSRef );

	/**
	** Make the resultFSRef refer to the given file or directory.
	** Return an OSErr value as the result.
	** None of the items may be null.
	**<p>
	** If the targeted item doesn't exist, an error-code is returned.
	** Unlike with an FSSpec, an FSRef can't refer to a non-existent item.
	** The relevant calling code in this class is responsible for handling non-existent targets,
	** so they can be encapsulated with behavior similar to a non-existent FSSpec.
	*/
	abstract protected int
	makeRef( byte[] parentFSRef, String name, byte[] resultFSRef );


	/**
	** Resolve the given FSRef as a possible alias-file.
	** Return an OSError value as the result.
	**<p>
	** See "Alias Manager" Carbon docs.
	*/
	abstract protected int
	resolveRef( byte[] theFSRef, boolean resolveChains, byte[] targetIsFolder, byte[] wasAliased );


	/**
	** Get the FSCatalogInfo for theFSRef.
	** Return an OSErr value as the result.
	** The nameBuf may be null, in which case a name is not returned.
	** The parentRSRef may also be null.
	*/
	abstract protected int
	getRefInfo( byte[] theFSRef, int infoMask, byte[] infoBuf, char[] nameBuf, byte[] parentFSRef );

	/**
	** Set the FSCatalogInfo for theFSRef.
	** Return an OSErr value as the result.
	*/
	abstract protected int
	setRefInfo( byte[] theFSRef, int infoMask, byte[] infoArray );


	/**
	** Create the file or directory referenced by the FSRef and other args.
	** Return an OSErr value as the result.
	** None of the items may be null.
	*/
	abstract protected int
	createRef( byte[] parentFSRef, String name, boolean isDirectory, byte[] resultFSRef );

	/**
	** Delete the file or directory referenced by the FSRef,
	** without resolving any aliases.
	** Return an OSError value as the result.
	*/
	abstract protected int
	deleteRef( byte[] theFSRef );

	/**
	** Move the file or directory referenced by the FSRef,
	** without resolving any aliases.
	** Return an OSError value as the result.
	** The destination must reference an existing directory.
	*/
	abstract protected int
	moveRef( byte[] theFSRef, byte[] destinationFSRef );

	/**
	** Rename the file or directory referenced by the FSRef,
	** without resolving any aliases.
	** Return an OSError value as the result.
	*/
	abstract protected int
	renameRef( byte[] theFSRef, String newName, byte[] resultFSRef );


	/**
	** Open the item's named fork.
	** Return an OSError value as the result.
	*/
	abstract protected int
	openRef( byte[] theFSRef, char[] forkName, byte permissions, short[] refNum );


	/**
	** Return an opaque iterator Object for iterating over theFSRef.
	** Returns an instance of IOException, appropriately prepared, if there was an error.
	** Otherwise returns an instance of an arbitrary Object representing an iterator.
	*/
	abstract protected Object
	begin( byte[] theFSRef );

	/**
	** Get the name of the next item iterated, or
	** return null when nothing left to iterate.
	** Errors also cause a null return, which simply halts the iteration.
	**<p>
	** The name must be a literalized (i.e. accent-composed) name, 
	** suitable for adding directly to a Pathname.
	*/
	abstract protected String
	next( Object iterator );

	/**
	** Stop iterating using the iterator started by begin().
	*/
	abstract protected void
	end( Object iterator );


}
