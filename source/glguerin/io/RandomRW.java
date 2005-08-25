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


// --- Revision History ---
// 05Jun01 GLG  create
// 06Jun01 GLG  factor out from other classes
// 08Jun01 GLG  add flush() -- duh
// 11Jun01 GLG  improve skip() to work forward or backward


/**
** RandomRW represents random access along with basic reading and
** writing of bytes.  The underlying container of bytes is arbitrary, and need
** not be restricted only to files.
**<p>
** An implementation must provide the write() methods, even if the underlying
** bytes can't be written.  For example, a file opened only for reading would throw
** IOExceptions from the write() methods, when represented as a RandomRW.
**<p>
** This interface intentionally excludes DataInput and DataOutput, since those
** can be implemented as wrappers around an underlying RandomRW.
** Such a design is more in line with Java's stream classes, as distinct from
** the kitchen-sink design of RandomAccessFile.
**
** @author Gregory Guerin
*/

abstract public class RandomRW
{
	/** Assigned in constructor, read-only thereafter. */
	private final boolean isWritable;

	/**
	** Create with the immutable choice of whether to allow writing or not.
	*/
	protected
	RandomRW( boolean allowWriting )
	{
		super();
		isWritable = allowWriting;
	}



	/**
	** Return the current overall length of the byte-container, measured in bytes.  
	** This may effectively be an unsigned int, since implementations are not
	** required to accomodate more than 4 GB.  
	** Heck, they may not even accomodate more than 2 GB,
	** depending on the platform and implementation.
	*/
	abstract public long
	length()
	  throws IOException;

	/**
	** Return the current location at which reading or writing will next occur.
	** This may effectively be an unsigned int, since implementations are not
	** required to accomodate more than 4 GB.  
	** Heck, they may not even accomodate more than 2 GB,
	** depending on the platform and implementation.
	*/
	abstract public long
	at()
	  throws IOException;

	/**
	** Return the number of bytes remaining, measured from the current
	** position, i.e. measured from at().  If the file-pointer is at or past EOF, zero is returned.
	** The returned value is never negative.
	** This is equivalent to <code>length() - at()</code>,
	** but may be faster in its specific implementations.
	**<p>
	** This default implementation returns <code>length() - at()</code>.
	*/
	public long 
	remaining()
	  throws IOException
	{  return ( length() - at() );  }


	/**
	** Move the read/write location to the given offset,
	** measured as a byte-count from the beginning of the container.
	** The <code>position</code> may effectively be treated as an unsigned int, 
	** since implementations are not required to accomodate more than 4 GB.  
	** Heck, they may not even accomodate more than 2 GB,
	** depending on the platform and implementation.
	**<p>
	** Implementations may accept negative positions as equivalent to zero,
	** or they may throw an IOException.
	** They may also accept positions beyond EOF as equivalent to seeking to EOF,
	** or they may throw an IOException.
	*/
	abstract public void
	seek( long position )
	  throws IOException;

	/**
	** Skip forward or backward the given signed number of bytes, 
	** returning the signed count of how many bytes were actually skipped.
	** This is equivalent to seek()'ing relative to the current position, but might
	** be implemented more efficiently than the equivalent calls to other methods.
	** The notes for seek() about support for files longer than 2 GB or 4 GB applies here.
	**<p>
	** An end-of-file or beginning-of-file cannot be skipped.
	** Once reached, an EOF or BOF will impede subsequent skips, returning zero.
	** Reaching EOF will also impede subsequent reads.
	**<p>
	** This default implementation skips forward or backward the given number of bytes
	** using seek(), length(), and at().  It does not skip beyond EOF or BOF, but stops there
	** without causing an IOException by seeking beyond.
	*/
	public long
	skip( long distance )
	  throws IOException
	{
		long here = at();
		long end = length();

		// Calculate where to seek to, keeping it within limits.
		long there = here + distance;
		if ( there < 0 )  { there = 0; }
		if ( there > end )  { there = end; }

		seek( there );

		// Return actual distance skipped, a signed number.
		return ( there - here );
	}


	/**
	** Close the container, flushing any data.
	** Susbsequent operations on the container will fail.
	*/
	abstract public void
	close()
	  throws IOException;


	/**
	** Read one byte, returning it unsigned in low 8-bits of int,
	** or return -1 on EOF.
	*/
	abstract public int
	read()
	  throws IOException;

	/**
	** Read bytes into an array, returning count actually read,
	** or -1 on EOF.
	**<p>
	** This default implementation calls read( buffer, 0, buffer.length ).
	*/
	public int 
	read( byte[] buffer )
	  throws IOException
	{  return ( read( buffer, 0, buffer.length ) );  }

	/**
	** Read bytes into a range of an array, returning count actually read,
	** or -1 on EOF.
	*/
	abstract public int
	read( byte[] buffer, int offset, int count )
	  throws IOException;


	/**
	** Return whether this instance is writable or not.
	*/
	public boolean
	isWritable()
	{  return ( isWritable );  }

	/**
	** If unwritable, throw an IOException, otherwise return normally.
	** This method should be called by all the write() methods and setLength()
	** before actually writing anything.
	*/
	protected void
	checkWritable()
	  throws IOException
	{
		if ( ! isWritable() )
			throw new IOException( "Unwritable" );
	}


	/**
	** Set the length, truncating or extending as needed.  
	** When extended, the new bytes may contain arbitrary
	** and possibly sensitive data from reused disk blocks.  To be certain of the
	** content, you would be wise to overwrite them with zeros.
	**<p>
	** If the underlying container is unwritable, this method will throw an IOException.
	*/
	abstract public void 
	setLength( long length )
	  throws IOException;

	/**
	** Flush any buffered bytes.
	** By default, this method does nothing, and need not be provided by subclasses.
	** It exists mainly so that subclasses can provide explicit control over buffering
	** if and when they need to.
	*/
	public void 
	flush()
	  throws IOException
	{  return;  }


	/**
	** Write one byte, from the low 8-bits of abyte.
	*/
	abstract public void 
	write( int abyte )
	  throws IOException;

	/**
	** Write an entire byte-array.
	*/
	public void
	write( byte[] bytes )
	  throws IOException
	{  write( bytes, 0, bytes.length );  }

	/**
	** Write the given range of bytes from a  byte-array.
	*/
	abstract public void
	write( byte[] bytes, int offset, int count )
	  throws IOException;

}
