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


// --- Revision History ---
// 15Jun01 GLG  create
// 16Jun01 GLG  add assert(), deny(), and has()
// 16Jun01 GLG  add file-lock bit, add'l names
// 23Jun01 GLG  add assign()
// 25Jun01 GLG  change ID_UNKNOWN to be -1
// 28Mar2002 GLG  add hashCode() and equals()
// 15Dec2002 GLG  for JDK 1.4: change assert() to affirm()


/**
** FileAccess represents file-access privileges and ID's, mainly the canonical
** Unix-type mode-bits, owner-ID, and group-ID, but also the classical Mac OS file-lock.
** A particular platform may not support these elements exactly, since not all platforms
** are Unix replicas or even Unix-like, despite certain quasi-religious beliefs to the contrary.
**<p>
** If a platform does not support these elements exactly, it is free to provide its own
** interpretation of file-access controls and ID's in any way that makes sense.
** A platform is even permitted to support none of the elements of FileAccess, and simply
** act as if all file-access is permitted and any user or group ID is as good as another,
** i.e. the Blissful Ignorance design pattern.
**<p>
** The classical Mac OS file-locking flag is contained in FileAccess, and can be
** manipulated by changing it in the FileAccess and then calling
** You can also obtain it directly from a FileInfo, and control it directly with FileForker.
** At some future time, those single-boolean mechanisms will become deprecated, and 
** eventually be eliminated, but that probably won't happen for quite a while.
**<p>
** The various _EXEC masks may represent different things for files than for directories.
** For files, _EXEC means that the item is "executable" in some way.
** This could mean that it's an actual executable chunk of native code, or it
** could mean that an interpreter will interpret the text when "executed".
** Different platforms have different requirements for what constitutes an "executable",
** and I won't even attempt to clarify the meaning of the bit in all those cases.
** Platforms should interpret the presence or absence of the bit as it makes sense for them.
** Programs that rely on a specific interpretation for the _EXEC bits without considering
** the actual host platform are making a gravely flawed assumption.
**<p>
** For directories, _EXEC bits are traditionally interpreted to mean "searchable", meaning
** that the directory can be named in a pathname even though its contents might
** not be listable or readable.  Again, a platform is free to interpret this in other ways,
** though in the interest of a certain clarity of meaning, it would be prudent not
** to stray too far from the conventional Unixy meaning.
**<p>
** For various reasons, both historical and personal, FileAccess is a concrete class,
** while its apparently close relative FileInfo is an abstract interface.
** One reason is that it just made more sense that way (to me, at least).
**
** @author Gregory Guerin
**
** @see FileForker
** @see FileInfo
*/

public class FileAccess
{
	/**
	** Traditionally, an ID of zero is interpreted as the super-user (root user).
	** On any platform, it would be unwise to default to this user-ID, to avoid
	** confusion on platforms that actually have a super-user ID.
	*/
	public static final int ID_SUPER_USER = 0;

	/**
	** This is a default owner and group ID when no other value is supplied.
	** As far as I know, no platform allows it as a valid user or group ID.
	*/
	public static final int ID_UNKNOWN = -1;


	/**
	** The getPrivileges()/setPrivileges() bit-mask representing
	** classical Mac OS file-locking control.  This flag is also visible in
	** FileInfo.isLocked(), and controlled in FileForker.setFileAccess().
	**<p>
	** On platforms that support both
	** file-locking and access privileges, e.g. Mac OS X, these are distinct flags.
	** On platforms that only support file-locking, these may not be distinct.
	** That is, setting the lock-flag on a file may affect the XXX_WRITE privileges it
	** returns from getFileAccess().  This is all implementation dependent.
	*/
	public static final int
		IS_LOCKED = 0x100000;


	/**
	** The getPrivileges()/setPrivileges() bit-masks representing owner privileges.
	*/
	public static final int
		OWNER_ALL = 0700,
		OWNER_READ = 0400,
		OWNER_WRITE = 0200,
		OWNER_EXEC = 0100;

	/**
	** The getPrivileges()/setPrivileges() bit-masks representing group privileges.
	*/
	public static final int
		GROUP_ALL = 0070,
		GROUP_READ = 0040,
		GROUP_WRITE = 0020,
		GROUP_EXEC = 0010;

	/**
	** The getPrivileges()/setPrivileges() bit-masks representing public (everyone else) privileges.
	*/
	public static final int
		PUBLIC_ALL = 0007,
		PUBLIC_READ = 0004,
		PUBLIC_WRITE = 0002,
		PUBLIC_EXEC = 0001;

	/**
	** The getPrivileges()/setPrivileges() bit-masks representing additional privileges.
	** These bits are even more platform-dependent than the access-privilege bits.
	** The sticky-bit is probably the most platform-dependent of all.
	*/
	public static final int
		SET_USER_ID = 04000,
		SET_GROUP_ID = 02000,
		STICKY = 01000;

	/**
	** The getPrivileges()/setPrivileges() bit-masks representing all three levels
	** of bits that control a specific access privilege.
	*/
	public static final int
		ACCESS_READ = OWNER_READ + GROUP_READ + PUBLIC_READ,
		ACCESS_WRITE = OWNER_WRITE + GROUP_WRITE + PUBLIC_WRITE,
		ACCESS_EXEC = OWNER_EXEC + GROUP_EXEC + PUBLIC_EXEC,
		ACCESS_ALL = OWNER_ALL + GROUP_ALL + PUBLIC_ALL;



	/** The privilege bits. */
	private int myBits;

	/** The owner's user ID. */
	private int myOwner;

	/** The owning group ID. */
	private int myGroup;


	/**
	** Create with initial privilege-bits of 0 and
	** initial owner and group ID's of ID_UNKNOWN.
	*/
	public
	FileAccess()
	{
		super();
		myOwner = myGroup = ID_UNKNOWN;
	}

	/**
	** Create with given initial privilege-bits and default owner and group ID's.
	*/
	public
	FileAccess( int privileges )
	{
		this();
		setPrivileges( privileges );
	}

	/**
	** Create with given initial values.
	*/
	public
	FileAccess( int privileges, int ownerID, int groupID )
	{
		this();
		setPrivileges( privileges );
		setOwnerID( ownerID );
		setGroupID( groupID );
	}



	/** Return a value dependent on privileges and both IDs. */
	public int
	hashCode()
	{  return ( getPrivileges() ^ getOwnerID() ^ getGroupID() );  }

	/**  Return a value dependent on privileges and both IDs. */
	public boolean
	equals( Object other)
	{
		if ( other instanceof FileAccess )
		{
			FileAccess that = (FileAccess) other;
			if ( this.getPrivileges() == that.getPrivileges()
					&&  this.getOwnerID() == that.getOwnerID()
					&&  this.getGroupID() == that.getGroupID() )
			{  return ( true );  }
		}
		return ( false );
	}



	/**
	** Copy all of the other FileAccess into this, or as much as possible.
	*/
	public void 
	copyFrom( FileAccess other )
	{
		if ( other != null  &&  other != this )
		{
			setPrivileges( other.getPrivileges() );
			setOwnerID( other.getOwnerID() );
			setGroupID( other.getGroupID() );
		}
	}


	/**
	** Get the current privilege bits.
	*/
	public int
	getPrivileges()
	{  return ( myBits );  }

	/**
	** Set all the privilege bits to the exact sequence of 1's and 0's given.
	*/
	public void
	setPrivileges( int privilegeBits )
	{  myBits = privilegeBits;  }


	/**
	** Affirm or deny the represented privilegeBits according to the given boolean,
	** returning the resulting privilege-bits.
	** This is the same as calling affirm() or deny() according to the boolean.
	*/
	public int
	assign( boolean affirmed, int privilegeBits )
	{  return ( affirmed ? affirm( privilegeBits ) : deny( privilegeBits ) );  }

	/**
	** Affirm (set) the privileges represented by the 1-bits in privilegeBits,
	** returning the resulting privilege-bits.
	** This is the same as OR'ing the privilegeBits with the current privileges.
	*/
	public int
	affirm( int privilegeBits )
	{  return ( myBits |= privilegeBits );  }

	/**
	** Deny (clear) the privileges represented by the 1-bits in privilegeBits,
	** returning the resulting privilege-bits.
	** This is the same as AND'ing the inverse of the privilegeBits with the current privileges.
	*/
	public int
	deny( int privilegeBits )
	{  return ( myBits &= ~privilegeBits );  }

	/**
	** Return true if any of the privilegeBits are set, false if all are clear.
	** If privilegeBits is a multi-bit value, true is returned
	** when any or all of the represented privileges are available.
	*/
	public boolean
	has( int privilegeBits )
	{  return ( (getPrivileges() & privilegeBits) != 0 );  }



	/**
	** Return the owner-ID of the file's owner.
	** Traditionally, an ID of zero is interpreted as the super-user (root user).
	*/
	public int 
	getOwnerID()
	{  return ( myOwner );  }

	/**
	** Set the owner-ID of the file's owner.
	** Traditionally, an ID of zero is interpreted as the super-user (root user).
	*/
	public void 
	setOwnerID( int ownerID )
	{  myOwner = ownerID;  }


	/**
	** Return the group ID.
	*/
	public int 
	getGroupID()
	{  return ( myGroup );  }

	/**
	** Set the group ID.
	*/
	public void 
	setGroupID( int groupID )
	{  myGroup = groupID;  }


}
