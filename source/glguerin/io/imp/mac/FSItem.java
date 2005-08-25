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
// 14Jun01 GLG  create as a more opaque class than FSSpec
// 15Jun01 GLG  add getAccess(), setAccess()
// 16Jun01 GLG  adjust some methods
// 19Jun01 GLG  make move() take a Pathname
// 21Jun01 GLG  put same() here
// 04Jul01 GLG  cut boolean arg from refRoot()
// 04Dec2002 GLG  cut exists(); callers should use getInfo(false) in its place
// 05Dec2002 GLG  add FileForker.Alias methods: canAlias(), newAlias(), writeAlias(), referAlias()
// 08Dec2002 GLG  add aliasInfo arg to newAlias()
// 11Dec2002 GLG  change args to writeAlias()
// 02Jan2003 GLG  rescope 'isValid' to protected, so subclasses can set it to T
// 02Jan2003 GLG  change arg of newAlias() to be a Pathname
// 02Jan2003 GLG  add refAlias()
// 03Jan2003 GLG  refactor newAlias(), cut canAlias()
// 06Jan2003 GLG  add informOfChange()
// 07Jan2003 GLG  add int arg to informOfChange()
// 07Jan2003 GLG  expand invalid()'s scope to public
// 15Jan2003 GLG  add isLeaf arg to refItem()


/**
** An FSItem is a reference to a file or folder, i.e. an item in the file-system.
** The "FS" in its name stands for "file-system", so this class represents a file-system item.
** It is used to resolve aliases, to open forks, to delete or rename, and so on.
** Unlike its predecessor class FSSpec, its implementation is entirely opaque to any of its callers.
** Its external API works with platform-neutral things like Pathnames,
** while the internals can be anything.
**<p>
**<p>
** Similar to the way that an FSRef structure is opaque on Mac OS 9 and X, 
** this class is essentially opaque in its implementation.  This does not imply that
** it can only be implemented with an FSRef, only that the opacity and capabilities
** are important reasons why I moved away from a not-so-opaque FSSpec.
** The reason FSItem is opaque is that I want to
** eventually replace FSSpec and all its older kin with completely new implementations
** derived solely from FSItem, even for Mac platforms that don't have the underlying
** FSRef-based libraries.  That is, an FSItem will eventually have to accomodate representations
** implemented with an FSRef and with an FSSpec.
**
** @author Gregory Guerin
*/

abstract public class FSItem
{
	/** T if this FSItem is valid (refers to something), F if invalid (refers to nothing). */
	protected boolean isValid;


	/**
	** Construct an empty FSItem.
	*/
	protected
	FSItem()
	{
		super();
		isValid = false;
	}


	/**
	** Return any length-limit this implementation imposes on pathname elements.
	** This represents a limit on Pathname part Strings, not a limit on overall pathname length.
	*/
	abstract public int
	nameLimit();


	/**
	** Return whether the Pathname part Strings are the same, according to file-system rules.
	** Ideally, this is done differently for each volume format, but that's impossible, since we
	** have no way of knowing what the volume format is and dealing with it accordingly.
	** Therefore, we have this method that does a CASE-INSENSITIVE COMPARISON,
	** using String.equalsIgnoreCase(), and let subclasses override it as needed.
	*/
	protected boolean
	same( String part1, String part2 )
	{  return ( part1 != null  &&  part1.equalsIgnoreCase( part2 ) );  }




	/**
	** Call this in implementations to ensure we have a valid reference.
	** The reference() method sets and clears the isValid flag that controls
	** whether an IOException is thrown here or not.
	*/
	protected void
	valid()
	  throws IOException
	{
		if ( ! isValid )
			throw new IOException( "Not valid" );
	}

	/** Invalidate this FSItem, so valid() will fail. */
	public void
	invalid()
	{  isValid = false;  }




	/**
	** Set the given FSItem so it references the item with the given Pathname,
	** resolving aliases as requested.
	** On success, subsequent calls to create(), openFork(), delete(), rename(), move(), etc. will work.
	** If the Pathname is empty, the behavior is implementation dependent.
	** If the Pathname is null, this FSItem is cleared and references no valid item.
	**<p>
	** The leaf item in the Pathname need not exist nor be accessible, but
	** all the directories leading up to it must exist, be accessible, and be resolvable.
	**<p>
	** One way to interpret an empty Pathname arg
	** would be to reference a read-only pseudo-directory representing all
	** available mounted volumes.  This is platform-dependent, however.
	** On Mac OS X, the root of the file-system is represented by an empty Pathname,
	** i.e. by "/".  This is not the same as the list of mounted volumes, "/Volumes".
	*/
	public void
	reference( Pathname path, boolean resolveAliases )
	  throws IOException
	{
//		System.out.println( "   FSItem...reference(): " + path );
		invalid();
		if ( path != null )
		{
			// Start at the root of the Pathname and work towards leaf.
			for ( int limit = path.count(), i = refRoot( path );  i < limit;  ++i )
			{  refItem( path.part( i ), resolveAliases );  }

			// Getting here without exception, assume we're valid.
			isValid = true;
		}
	}

	/**
	** Set this FSItem so it references the root directory for the given Pathname,
	** and returning the index of the Pathname part
	** whose String should first be passed to refItem().
	** Since volume-names can never be represented as an alias at the root level,
	** alias-resolving is meaningless.  Thus, there is no resolveAliases flag passed.
	*/
	abstract protected int
	refRoot( Pathname path )
	  throws IOException;

	/**
	** Set this FSItem so it references the named item relative to its current reference,
	** resolving aliases as requested.
	*/
	abstract protected void
	refItem( String part, boolean resolveAliases )
	  throws IOException;



	/**
	** Fill in the given Pathname with the names referencing this FSItem.
	** That is, do the opposite of the most recent call to reference().
	** If the most recent reference() resolved aliases, then the given Pathname
	** is filled in with the names leading to the resolved original item.
	*/
	abstract public void
	resolved( Pathname path )
	  throws IOException;



	/**
	** Create a new FileForker.Alias that refers to the current target
	** as previously set by reference() and refItem(),
	** or return null if Aliases are not supported.
	** In general, the target must exist and be accessible, or an IOException is thrown.
	**<p>
	** The Pathname should contain the pathname of the current target.
	** The Pathname must be a replica, since it may be assigned to the new Alias,
	** or it may be manipulated during the Alias-making process.
	**<p>
	** This default imp always returns null, signifying "Aliases not supported".
	*/
	public FileForker.Alias
	newAlias( Pathname path )
	  throws IOException
	{  return ( null );  }

	/**
	** Write the FileForker.Alias to the current target (as previously set).
	** If the Alias isn't the correct type for the FSItem type, a ClassCastException is thrown.
	** The Alias may be updated by this method, which may affect the Alias's pathname.
	**<p>
	** Return T if a symlink is created, F if an alias-file.
	**<p>
	** This default imp always throws an UnsupportedIOException.
	*/
	public boolean
	writeAlias( boolean preferSymlink, Pathname targetPath, FileForker.Alias alias )
	  throws IOException
	{  throw new UnsupportedIOException( "FileForker.Alias unsupported" );  }

	/**
	** Set this FSItem so it references the Alias's referent, returning T
	** if the Alias was internally changed, or F if it wasn't.
	** Throws an IOException if the Alias's referent can't be located or accessed.
	** Throws an UnsupportedIOException if this feature isn't supported.
	**<p>
	** This method is mainly used by an Alias imp for re-locating its original referent
	** and re-resolving the pathname to it.  Calls to this method are typically followed
	** by a call to resolved(), in order to assemble a Pathname.
	** Unlike the newAlias() and writeAlias() methods, this method
	** may be unimplemented even when canAlias() returns T. 
	** An Alias imp should signify a working refAlias() imp under Alias.getCapabilities().
	** This requires coordination between the FSItem imp and the Alias imp.
	**<p>
	** This default imp always throws an UnsupportedIOException.
	*/
	public boolean
	refAlias( FileForker.Alias alias )
	  throws IOException
	{  throw new UnsupportedIOException( "FileForker.Alias unsupported" );  }


	/**
	** Inform listeners that a referenced item changed, or that everything changed.
	** Return T if a message was sent, F if not.
	** This method is for implementing FileForker.signalChange().
	**<p>
	** This default imp always returns F.
	*/
	public boolean
	informOfChange( int messageValue, boolean specifically )
	{  return ( false );  }



	/**
	** Create the current target (both forks), throwing an IOException if problematic,
	** and returning true if the target was actually created as requested.
	** If the target already exists, the existing item is left as-is and false is returned
	** -- no IOException is thrown.  This happens even if the requested item is
	** not the same kind as the existing item (directory vs. file).
	**<p>
	** If the target item is created and is not a directory, it is given the default
	** creator and file-type supplied.  If it's created and is a directory, the default
	** creator and file-type are irrelevant, since directories don't have them.
	**<p>
	** If the target is indeed created, this FSItem continues to refer to that target,
	** even if creating the target changes some internal data.
	*/
	abstract public boolean
	create( int defaultType, int defaultCreator, boolean isDirectory )
	  throws IOException;


	/**
	** Open the designated fork for the designated access.
	** The target file must already exist.
	** No existing fork is truncated or altered, even if opened for writing.
	** It's up to the implementation to decide if multiple ForkRW's can be open
	** for writing to the same file at once.  The canonical Mac OS behavior is
	** to forbid multiple writers, but to allow any number of non-exclusive readers.
	*/
	abstract public ForkRW
	openFork( boolean resFork, boolean forWriting )
	  throws IOException;

	/**
	** Called from typical openFork() imp to
	** make a concrete ForkRW from given parameters.
	*/
	abstract protected ForkRW
	newFork( boolean forWriting, int forkRefNum, String tag );



	/**
	** If this FSItem refers to a directory, assemble its direct (non-recursive)
	** contents as a list of names to the given Pathname, which is cleared first.
	** Any names for the directory itself or its parent are omitted.
	** Only the direct contents is listed, which is not a recursive list.
	** The Pathname is used only as a list of Strings, not as an actual Pathname.
	** This method does not clear the Pathname before adding names to it.
	**<p>
	** The names are not added in any particular order.  Do not assume they are sorted.
	**<p>
	** If this FSItem refers to a non-directory or a non-existent item,
	** an IOException is thrown, so you should first qualify the call to this method.
	**<p>
	** If we were doing something more to a directory than just listing it,
	** an Enumeration would be more appropriate.  Since all we ever want is a
	** list of names, a single method that does only that is appropriate.
	*/
	abstract public void
	contents( Pathname path )
	  throws IOException;


	/**
	** Return either the brief or full form of a FileInfo.
	** The returned FileInfo should not be altered nor retained,
	** since it is probably a singleton internal reference.
	** That is, if you want the information to persist, copy it somewhere else.
	**<p>
	** The brief form returns a FileInfo where only isDirectory(), isAlias(), isLocked(),
	** Finder-flags, and fork-lengths are valid.  In particular, neither leaf-name,
	** nor file-type, nor creator, nor any of the time-stamps are guaranteed to be valid.
	**<p>
	** The full form DOES NOT include the comment, which is always separately accessed.
	*/
	abstract public FileInfo
	getInfo( boolean full )
	  throws IOException;

	/**
	** Set the file info.
	** The target must exist, but can be a file or a directory.
	** Some elements of directories are unsettable, such as file-type and creator.
	*/
	abstract public void
	setInfo( FileInfo info )
	  throws IOException;


	/**
	** Return a FileAccess representing the file access privilege information.
	*/
	abstract public FileAccess
	getAccess()
	  throws IOException;

	/**
	** Apply as much as possible of the given FileAccess to the target item.
	** If FileAccess is null, only the boolean is used, and all other privileges are unchanged.
	** If FileAccess is non-null, the boolean is not used, the lock-state is taken
	** from the FileAccess, and all settable privileges are affected.
	**<p>
	** The extent to which each FileAccess value is supported, and what occurs
	** if a value can't be set, is platform and implementation dependent.
	*/
	abstract public void
	setAccess( FileAccess desired, boolean isLocked )
	  throws IOException;



	/**
	** Delete the current target, throwing an IOException if problematic.
	** The target must already exist.
	** If the target is a directory, it must be empty.
	** If the target is a file, both forks are deleted.
	*/
	abstract public void
	delete()
	  throws IOException;

	/**
	** Rename the referenced item, throwing an IOException if problematic.
	** The current item must already exist and no item with newName can exist.
	** The newName is taken as a literalized name suitable for add()'ing to a Pathname.
	*/
	abstract public void
	rename( String newName )
	  throws IOException;

	/**
	** Move the currently referenced item to the destination Pathname directory.
	** The current item must already exist, and no item of the same name can exist
	** in the destination directory.
	** On success, this FSItem will reference the just-moved item in its new location.
	*/
	abstract public void
	moveTo( Pathname destination )
	  throws IOException;


	/**
	** Get the comment, or return an empty String.
	** Never returns null.
	**<p>
	** If the implementation does not support comments, this method 
	** may always return an empty String, never throwing an IOException.
	*/
	abstract public String
	getComment()
	  throws IOException;

	/**
	** Set or remove the current target's comment.  
	** If comment is null or zero-length, any existing comment is removed.
	** If comment is 1 or more bytes, the comment is set.
	**<p>
	** If the implementation does not support comments, this method 
	** may have no effect but still not throw an IOException.
	*/
	abstract public void
	setComment( String comment )
	  throws IOException;

}
