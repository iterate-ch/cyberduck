/*
** Copyright 1998, 1999, 2001, 2002 by Gregory L. Guerin.
** Terms of use:
**  - Briefly: OPEN SOURCE under Artistic License -- credit fairly, use freely, alter carefully.
**  - Fully: <http://www.amug.org/~glguerin/sw/artistic-license.html>
** This file is from the MacBinary Toolkit for Java:
**   <http://www.amug.org/~glguerin/sw/#macbinary> 
*/

package glguerin.io;

import java.io.*;


// --- Revision History ---
// 13Feb99 GLG  create
// 01Apr99 GLG  expand doc-comments
// 06May99 GLG  implement read() and read(byte[],int.int)
// 11May2002 GLG  add getArray() and toByteArray() -- DUH!
// 11Jun2002 GLG  add length-limiting constructor


/**
** A RandomRWArray provides random-access read/write to a byte-array
** represented as a RandomRW class.
** The effective range of the byte-array can be reduced by setLength().
** The length can never extend beyond the initial capacity, neither by write()'ing
** nor by setLength().
** If a RandomRWArray has had its effective length reduced by setLength(),
** writing beyond that length will extend the effective length, up to the limit 
** imposed by the actual length of the underlying array.
**<p>
** If the data in the byte-array changes by some external means, then subsequent 
** reading from the RandomRWArray will see the new data.
**<p>
** There are different ways of doing an implementation that can extend the byte-array
** by write()'ing or setLength().  There are tradeoffs involved between complexity, speed,
** memory usage, etc.  Rather than guess which set of tradeoffs you will need, I've left
** all extending implementations to you, the interested reader.
**
** @author Gregory Guerin
*/

public class RandomRWArray
  extends RandomRW
{
	/** A non-null but empty (zero-length) array of bytes. */
	private static final byte[] NONE = new byte[ 0 ];


	/** The containing array, a source of bytes. */
	private byte[] myBytes;

	/**  Limit of range in byte-array (current effective length). */
	private int myLimit;

	/** The absolute position next to read from. */
	private int myPlace;

	/** For write(int). */
	private byte[] oneByte;


	/**
	** Establish read-only random-access on a non-null byte-array.
	*/
	public
	RandomRWArray( byte[] bytes )
	{  this( bytes, -1, false );  }

	/**
	** Establish random-access on a byte-array.
	** The entire range of the byte-array is the initial length.
	*/
	public
	RandomRWArray( byte[] bytes, boolean allowWriting )
	{  this( bytes, -1, allowWriting );  }

	/**
	** Establish random-access on the first count bytes of a byte-array.
	** The given count is the initial length, but not more than the array's actual length.
	** All negative counts are ignored, and the array's length is used instead.
	*/
	public
	RandomRWArray( byte[] bytes, int count, boolean allowWriting )
	{
		super( allowWriting );

		if ( bytes == null )
			bytes = NONE;

		if ( count < 0  ||  count > bytes.length )
			count = bytes.length;

		myBytes = bytes;
		myLimit = count;
		myPlace = 0;

		oneByte = new byte[ 1 ];
	}


	/**
	** Return the actual underlying array.
	** The array's bytes may not all be valid.
	** Use length() to determine how many of the array's bytes are valid.
	*/
	public byte[] 
	getArray()
	{  return ( myBytes );  }

	/**
	** Return a copy of the valid bytes in the underlying array.
	** The returned array's length will be identical to the valid range
	** of the underlying array's data.
	** Changes subsequently made to the returned array have no effect
	** on the underlying array, since the returned array is merely a copy.
	*/
	public byte[] 
	toByteArray()
	{
		byte[] replica = new byte[ myLimit ];
		System.arraycopy( myBytes, 0, replica, 0, myLimit );
		return ( replica );
	}


	/**
	** Return the underlying array-length as a long.
	**
	** @exception java.io.IOException
	**    Thrown when a problem arises.
	*/
	public long 
	length()
	  throws IOException
	{  return ( (long) myLimit );  }

	/**
	** Return the current read/write offset.
	**
	** @exception java.io.IOException
	**    Thrown when a problem arises.
	*/
	public long 
	at()
	  throws IOException
	{  return ( (long) myPlace );  }

	/**
	** Return the number of bytes remaining.
	**
	** @exception java.io.IOException
	**    Thrown when a problem arises.
	*/
	public long 
	remaining()
	  throws IOException
	{  return ( (long) (myLimit - myPlace) );  }

	/**
	** Move to a new read/write position, which must be less
	** than the length of the array.
	**
	** @exception java.io.IOException
	**    Thrown when a problem arises.
	*/
	public void 
	seek( long position )
	  throws IOException
	{  
		if ( position < 0L  ||  position > myLimit )
			throw new IOException( "Seek out of range" );

		myPlace = (int) position;  
	}


	/**
	** Close the container, releasing the reference to the underlying byte-array.
	** The length and position are also set to zero.
	** The underlying byte-array is set to a shared zero-length byte-array.
	**
	** @exception java.io.IOException
	**    Thrown when a problem arises.
	*/
	public void
	close()
	  throws IOException
	{
		myBytes = NONE;
		myLimit = myPlace = 0;
	}


	/**
	** Read one byte, returning it unsigned in low 8-bits of int,
	** or return -1 on EOF.
	*/
	public int
	read()
	  throws IOException
	{
		if ( myPlace >= myLimit )
			return ( -1 );

		return ( 0x0FF & myBytes[ myPlace++ ] );
	}

	/**
	** Read bytes into a range of an array, returning count actually read,
	** or -1 on EOF.
	*/
	public int 
	read( byte[] buffer, int offset, int count )
	  throws IOException
	{
		int remain = myLimit - myPlace;
		if ( remain <= 0 )
			return ( -1 );

		if ( count > remain )
			count = remain;

		System.arraycopy( myBytes, myPlace, buffer, offset, count );
		myPlace += count;
		return ( count );
	}


	/**
	** Set the length, truncating or extending as needed.  
	** When extended, the new bytes may contain arbitrary
	** and possibly sensitive data from reused bytes.  To be certain of the
	** content, you would be wise to overwrite them with zeros.
	**<p>
	** If the underlying container is unwritable, this method will throw an IOException.
	*/
	public void 
	setLength( long length )
	  throws IOException
	{
		checkWritableLength( length );
		myLimit = (int) length;
	}

	/**
	** Write one byte, from the low 8-bits of abyte.
	*/
	public void 
	write( int abyte )
	  throws IOException
	{
		oneByte[ 0 ] = (byte) abyte;
		write( oneByte, 0, 1 );
	}

	/**
	** Write the given range of bytes from a  byte-array.
	** The current effective length can be extended if there is sufficient
	** space in the underlying array, but not extended beyond the array's size.
	*/
	public void
	write( byte[] bytes, int offset, int count )
	  throws IOException
	{
		// Throw IOException if unwritable, or the write would extend beyond the array's size.
		checkWritableLength( myPlace + count );

		// Copy bytes into place, adjusting pointers accordingly.
		System.arraycopy( bytes, offset, myBytes, myPlace, count );
		myPlace += count;
		if ( myLimit < myPlace )
			myLimit = myPlace;
	}


	/**
	** Check that this is writable, and length is within length of underlying array.
	*/
	protected void 
	checkWritableLength( long length )
	  throws IOException
	{
		checkWritable();
		if ( length > myBytes.length )
			throw new IOException( "Can't extend" );
	}


	/**
	** Return a String.
	*/
	public String
	toString()
	{  return ( new StringBuffer().append( myBytes.length ).append( "-byte array" ).toString() );  }

}
