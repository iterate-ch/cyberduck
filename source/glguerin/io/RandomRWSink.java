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
// 06Jun01 GLG  refactored from RandomAccessIONothing


/**
** A RandomRWSink is zero-length for reading, and a bit-bucket for writing.
** It accepts all seeking and other operations without exception.
**<p>
** You might think that this class needs a boolean arg for its constructor, but it doesn't.
** If you want a read-only zero-length RandomRW, you should make a
** RandomRWArray with a zero-length array instead.  That gives you full control over
** writability, leaving this class to represent an always-writable bit-bucket.
**
** @author Gregory Guerin
**
** @see java.io.RandomAccessFile
*/

public class RandomRWSink
  extends RandomRW
{
	/**
	** Nothing is as nothing does.
	*/
	public
	RandomRWSink()
	{  super( true );  }


	/**
	** Perform without failure.
	*/
	public void
	setLength( long length )
	  throws IOException
	{  return;  }

	/**
	** Return zero.
	*/
	public long
	length()
	  throws IOException
	{  return ( 0L);  }

	/**
	** Perform without failure.
	*/
	public void
	seek( long place )
	  throws IOException
	{  return;  }

	/**
	** Return zero.
	*/
	public long
	at()
	  throws IOException
	{  return ( 0L);  }


	/**
	** Always return EOF.
	*/
	public int
	read()
	  throws IOException
	{  return ( -1 );  }

	/**
	** Always return EOF.
	*/
	public int 
	read( byte[] buffer, int offset, int count )
	  throws IOException
	{  return ( -1 );  }


	/**
	** Perform without failure.
	*/
	public void
	write( int abyte )
	  throws IOException
	{  return;  }

	/**
	** Perform without failure.
	*/
	public void
	write( byte[] bytes, int offset, int count )
	  throws IOException
	{  return;  }



	/**
	** Perform without failure.
	*/
	public void
	close()
	  throws IOException
	{  return;  }


}
