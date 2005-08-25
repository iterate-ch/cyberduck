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
// 08Jun01 GLG  factor out as base class
// 11May2002 GLG  make close() break connection to underlying container


/**
** A RandomRWWrapper is a wrapper around another RandomRW container, and is
** itself a RandomRW.  It is typically used as a base-class, in the same way that 
** FilterInputStream is a base-class for other kinds of InputStream wrappers.
**<p>
** RandomRWWrapper DOES NOT have a file-pointer separate from
** the underlying container's file-pointer.  That is, reading, writing, or seeking on
** the RandomRWWrapper affects the underlying container's R/W-pointer -- its at() position.
** This is essentially the same behavior that FilterInputStream or FilterOutputStream
** have with respect to their underlying streams.
**
** @author Gregory Guerin
*/

public class RandomRWWrapper
  extends RandomRW
{
	/**
	** My underlying RandomRW container.
	*/
	protected RandomRW myRW;


	/**
	** Establish a wrapper around another RandomRW container.
	** The writability of this wrapper is a combination of the underlying container's
	** writability and the given flag.
	*/
	protected
	RandomRWWrapper( RandomRW wrapped, boolean allowWriting )
	{
		// This instance is only writable if the wrapped container is also writable.
		super( allowWriting & wrapped.isWritable() );
		myRW = wrapped;
	}


	/**
	** Return the current length of the underlying container, measured in bytes.
	*/
	public long
	length()
	  throws IOException
	{  return ( myRW.length() );  }

	/**
	** Move the read/write location to the given offset
	** of the underlying container, measured in bytes.
	*/
	public void
	seek( long place )
	  throws IOException
	{  myRW.seek( place );  }

	/**
	** Return the current location at which reading or writing will next occur.
	*/
	public long
	at()
	  throws IOException
	{  return ( myRW.at() );  }

	/**
	** Close the underlying RandomRW container and break the connection to it.
	*/
	public void
	close()
	  throws IOException
	{
		// Ensure that myRW is null, even if an IOException occurs in close()'ing it.
		// Breaking this connection ensures that subsequent operations fail.
		RandomRW container = myRW;
		myRW = null;
		container.close();  
	}


	/**
	** Read one byte, returning it unsigned in low 8-bits of int,
	** or return -1 on EOF.
	*/
	public int
	read()
	  throws IOException
	{  return ( myRW.read() );  }

	/**
	** Read bytes into a range of an array, returning count actually read,
	** or -1 on EOF.
	*/
	public int 
	read( byte[] buffer, int offset, int count )
	  throws IOException
	{  return ( myRW.read( buffer, offset, count ) );  }


	/**
	** Set the length, truncating or extending as needed.  
	** When extended, the new bytes may contain arbitrary
	** and possibly sensitive data from reused disk blocks.  To be certain of the
	** content, you would be wise to overwrite them with zeros.
	**<p>
	** If the underlying container is unwritable, this method will throw an IOException.
	*/
	public void 
	setLength( long length )
	  throws IOException
	{   myRW.setLength( length );  }

	/**
	** Flush any buffered bytes.
	*/
	public void 
	flush()
	  throws IOException
	{   myRW.flush();  }


	/**
	** Write one byte, from the low 8-bits of abyte.
	*/
	public void 
	write( int abyte )
	  throws IOException
	{   myRW.write( abyte );  }

	/**
	** Write the given range of bytes from a  byte-array.
	*/
	public void
	write( byte[] bytes, int offset, int count )
	  throws IOException
	{   myRW.write( bytes, offset, count );  }

}
