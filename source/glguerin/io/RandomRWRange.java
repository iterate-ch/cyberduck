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
// 19Feb99 GLG  improve exception messages in constructor
// 22Feb99 GLG  fix range-check in constructor
// 25Feb99 GLG  change name to RandomAccessInputRange (duh)
// 02Apr99 GLG  expand doc-comments
// 11May99 GLG  FIX: read(byte[],int,int) now returns correct count at EOF
// 06Jun01 GLG  refactor into RandomRange
// 07Jun01 GLG  add write() imp's to get RandomRWRange
// 08Jun01 GLG  refactor to subclass RandomRWWrapper
// 07May2002 GLG  add subClosing constructor and imp


/**
** A RandomRWRange is a sub-range of a RandomRW byte-container, and is
** itself a RandomRW.
** The underlying container can be any RandomRW implementation.
** This class provides the sub-range constraints on the underlying container,
** making it appear to be a subsection with its own offset and length that are always
** taken into account.
**<p>
** A sub-range's effective writability depends on the flag passed to the constructor
** as well as the writability of the underlying container.
**<p>
** The RandomRWRange's sub-range DOES NOT have a file-pointer separate from
** the underlying container's file-pointer.  That is, reading or seeking on
** the RandomRWRange affects the underlying container's file-pointer -- its at() position.
**<p>
** A RandomRWRange can be instantiated to close the underlying container on close(),
** or to keep the underlying container open.  If you have multiple sub-ranges open on the same
** underlying container, you may not want the container closed when the sub-range is closed.
** If so, use the constructor form with the boolean subClose arg.
** Not closing the container means that you are responsible for doing so when you're done.
**
** @author Gregory Guerin
*/

public class RandomRWRange
  extends RandomRWWrapper
{
	/** Determines whether close() will close the underlying container or not. */
	private final boolean willClose;

	/**
	** Offset of my first byte within container.
	*/
	private long myFirst;

	/**
	** Offset of my last byte within container.
	** Reading or seeking past this location yields an EOF condition.
	** Trying to write past this location throws an IOException.
	*/
	private long myLast;

	/** A buffer used by write(int). */
	private byte[] oneByte;


	/**
	** Establish a sub-range of the container.
	** This constructor WILL close the underlying container on close().
	** To avoid this, use the other constructor form.
	*/
	public
	RandomRWRange( RandomRW container, long offset, long count, boolean allowWriting )
	  throws IOException
	{  this( true, container, offset, count, allowWriting );  }

	/**
	** Establish a sub-range of the container.
	** The underlying container will be closed on close() according to the
	** given subClose flag.  Since a RandomRWRange shares the R/W position of
	** its underlying container, this controls how containers are handled when
	** subranges are closed.  You may want the container itself to remain open.
	*/
	public
	RandomRWRange( boolean subClose, RandomRW container, long offset, long count, boolean allowWriting )
	  throws IOException
	{
		super( container, allowWriting );
		willClose = subClose;

		if ( offset < 0L  ||  count < 0L )
			throw new IOException( "Negative offset or count: " + offset + ", " + count );

		myFirst = offset;
		myLast = offset + count;

		oneByte = new byte[ 1 ];
		seek( 0L );
	}


	/**
	** Close the underlying container or not, according to the
	** constructor's subClose flag.
	*/
	public void
	close()
	  throws IOException
	{
		// Ensure that myRW is null, even if an IOException occurs in close()'ing it.
		// Breaking this connection ensures that subsequent operations fail.
		RandomRW container = myRW;
		myRW = null;

		if ( willClose )
			container.close();  
	}



	/**
	** Return count of bytes remaining.
	*/
	public long 
	remaining()
	  throws IOException
	{  return ( myLast - myRW.at() );  }

	/**
	** Return the length of the sub-range, which does not change.
	*/
	public long 
	length()
	  throws IOException
	{  return ( myLast - myFirst );  }

	/**
	** Return the relative position within the sub-range.
	** That is, if the position is at the beginning of the sub-range, zero is returned.
	*/
	public long 
	at()
	  throws IOException
	{  return ( myRW.at() - myFirst );  }

	/**
	** Seek to a relative position within the sub-range.
	** That is, seeking to 0 seeks to the beginning of the sub-range.
	*/
	public void 
	seek( long position )
	  throws IOException
	{  myRW.seek( position + myFirst );  }



	/**
	** Read one byte, returning it unsigned in low 8-bits of int,
	** or return -1 on EOF.
	*/
	public int
	read()
	  throws IOException
	{
		if ( remaining() > 0L )
			return ( myRW.read() );
		else
			return ( -1 );
	}

	/**
	** Read bytes into a range of an array, returning count actually read,
	** or -1 on EOF.
	*/
	public int 
	read( byte[] buffer, int offset, int count )
	  throws IOException
	{
		long remain = remaining();
		if ( remain <= 0L )
			return ( -1 );

		// If read would go past EOF, only read what remains before EOF.
		if ( count > remain )
			count = (int) remain;

		return ( myRW.read( buffer, offset, count ) );
	}


	/**
	** Does nothing except check that the underlying container is writable.
	** The length of a sub-range can neither be extended nor reduced.
	** This method quietly has no effect, rather than throwing an IOException.
	*/
	public void 
	setLength( long length )
	  throws IOException
	{  checkWritable();  }

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
	** If the write would extend past the limit of the range, no bytes are written at all
	** and an IOException is thrown.
	*/
	public void
	write( byte[] bytes, int offset, int count )
	  throws IOException
	{
		// Throw IOException if unwritable, or the write would extend past the limit.
		checkWritable();
		if ( count > remaining() )
			throw new IOException( "Can't extend" );

		// Write the bytes.
		myRW.write( bytes, offset, count );
	}

}
