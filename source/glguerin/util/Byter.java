/*
** Copyright 1998, 1999, 2001 by Gregory L. Guerin.
** Terms of use:
**  - Briefly: OPEN SOURCE under Artistic License -- credit fairly, use freely, alter carefully.
**  - Fully: <http://www.amug.org/~glguerin/sw/artistic-license.html>
** This file is from the MacBinary Toolkit for Java:
**   <http://www.amug.org/~glguerin/sw/#macbinary> 
*/

package glguerin.util;


// --- Revision History ---
// 17Jun98 GLG  copy and repackage from older GLG utils
// 26Jun98 GLG  edit doc-comments
// 16Feb99 GLG  refactor and rework
// 27Mar99 GLG  name changes
// 02Apr99 GLG  method-name changes
// 11Apr99 GLG  add putBytesAt( byte[], int )


/**
** Wrapper around a byte-array with methods that get or put multi-byte integers
** from arbitrary offsets within the byte-array.  
** The format is presumed to be Java's usual big-endian form.
** Unsigned values are returned in a type of larger size, so ubyte and ushort
** are returned in an int, and uint is returned in a long.
** Floating-point types aren't directly supported, but they are easy to add
** using the float-to-bits and double-to-bits methods of Float and Double.
**
** @author Gregory Guerin
*/

public class Byter
{
	/**
	** The internal array that holds the bytes.
	*/
	private byte[] myBytes;

	/**
	** Vanilla constructor, with no byte[] assigned.
	*/
	public
	Byter()
	{  super();  }

	/**
	** Create with a new array of the given byte-count.
	*/
	public
	Byter( int byteCount )
	{  this( new byte[ byteCount ] );  }

	/**
	** Create with the given array.
	*/
	public
	Byter( byte[] bytes )
	{
		this();
		setByteArray( bytes );
	}


	/**
	** Get the internal byte-array.
	*/
	public final byte[]
	getByteArray()
	{  return ( myBytes );  }


	/**
	** Use the given array to hold bytes.
	** Accepts null and/or zero-length arrays without error,
	** though a subsequent get or put will throw an exception.
	*/
	public final void
	setByteArray( byte[] bytes )
	{  myBytes = bytes;  }


	/**
	** Clear the entire internal array.  Does nothing if no array assigned.
	*/
	public void
	clear()
	{
		if ( myBytes != null )
		{
			for ( int i = 0;  i < myBytes.length;  ++i )
			{  myBytes[ i ] = 0;  }
		}
	}



	/**
	** Return the signed byte at the given offset.
	*/
	public byte
	getByteAt( int offset )
	{
		return ( myBytes[ offset ] );
	}

	/**
	** Return an int holding the unsigned byte at the given offset.
	*/
	public int
	getUByteAt( int offset )
	{
		return ( 0x00FF & myBytes[ offset ] );
	}

	/**
	** Return the signed short at the given offset.
	*/
	public short
	getShortAt( int offset )
	{
		return ( (short) ( (myBytes[ offset ] << 8) + (0x00FF & myBytes[ offset + 1 ]) ) );
	}

	/**
	** Return an int holding the unsigned short at the given offset.
	*/
	public int
	getUShortAt( int offset )
	{
		return ( (0xFF00 & (myBytes[ offset ] << 8)) + (0x00FF & myBytes[ offset + 1 ]) );
	}

	/**
	** Return the signed int at the given offset.
	*/
	public int
	getIntAt( int offset )
	{
		int value = (0x0FF & myBytes[ offset ]) << 24;
		value += (0x0FF & myBytes[ offset + 1 ]) << 16;
		value += (0x0FF & myBytes[ offset + 2 ]) << 8;
		return ( value + (0x0FF & myBytes[ offset + 3 ]) );
	}

	/**
	** Return a long holding the unsigned int at the given offset.
	*/
	public long
	getUIntAt( int offset )
	{
		long value = 0xFFFFFFFFL & (long) getIntAt( offset );
		return ( value );
	}

	/**
	** Return the signed long at the given offset.
	*/
	public long
	getLongAt( int offset )
	{
		long value = getIntAt( offset );
		return ( (value << 32) + getUIntAt( offset + 4 ) );
	}



	/**
	** Put the given byte at the supplied offset.
	*/
	public void
	putByteAt( byte value, int offset )
	{
		myBytes[ offset ] = value;
	}

	/**
	** Put all the given bytes at the supplied offset.
	*/
	public void
	putBytesAt( byte[] data, int offset )
	{
		System.arraycopy( data, 0, myBytes, offset, data.length );
	}

	/**
	** Put the given bytes at the supplied offset.
	*/
	public void
	putBytesAt( byte[] data, int offset, int count )
	{
		System.arraycopy( data, 0, myBytes, offset, count );
	}

	/**
	** Put the given short at the supplied offset.
	*/
	public void
	putShortAt( short value, int offset )
	{
		myBytes[ offset ] = (byte) (value >> 8);
		myBytes[ offset + 1 ] = (byte) (value);
	}

	/**
	** Put the given int at the supplied offset.
	*/
	public void
	putIntAt( int value, int offset )
	{
		myBytes[ offset ] = (byte) (value >> 24);
		myBytes[ offset + 1 ] = (byte) (value >> 16);
		myBytes[ offset + 2 ] = (byte) (value >> 8);
		myBytes[ offset + 3 ] = (byte) (value);
	}

	/**
	** Put the given long at the supplied offset.
	*/
	public void
	putLongAt( long value, int offset )
	{
		putIntAt( (int) (value >> 32), offset );
		putIntAt( (int) (value), offset + 4 );
	}


}

