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
// 18Mar99 GLG  create 1st version
// 18Mar99 GLG  add constructors that open on instantiation
// 19Mar99 GLG  refactor to take boolean in constructors, open
// 22Mar99 GLG  declare SecurityException thrown by open() and non-vanilla constructors
// 05Apr99 GLG  expand doc-comments
// 25Apr01 GLG  change args to open() to String pair
// 06Jun01 GLG  match ForkRW changes, cut open()
// 06Jun01 GLG  refactor from ForkOutputStream
// 11May2002 GLG  add subclosing option to constructor


/**
** A RandomRWOutputStream is an OutputStream that writes to a
** data-destination represented by a RandomRW, which must itself be writable.
** It is unbuffered.
** This class does not provide random access, since it's an OutputStream.
**<p>
** You can create a RandomRWOutputStream that does not close its
** underlying RandomRW container.  The single-arg constructor always closes
** its container.  To control subclosing, use the two-arg constructor
**
** @author Gregory Guerin
**
** @see ForkRW
** @see java.io.OutputStream
*/

public class RandomRWOutputStream
  extends OutputStream
{
	private final boolean willClose;
	private RandomRW myRW;

	/**
	** Create with given RandomRW, which must be positioned
	** and ready for writing.  When this OutputStream is closed,
	** its underlying container is also closed.
	*/
	public
	RandomRWOutputStream( RandomRW container )
	{  this( true, container );  }

	/**
	** Create with given RandomRW, which must be positioned
	** and ready for writing.  When this OutputStream is closed,
	** its underlying container will be closed or left open according to
	** the state of closeContainer.
	*/
	public
	RandomRWOutputStream( boolean closeContainer, RandomRW container )
	{
		super();
		willClose = closeContainer;
		myRW = container;
	}


	/**
	** Flush any buffered output.
	*/
	public void
	flush()
	  throws IOException
	{  myRW.flush();  }

	/**
	** Write one byte.
	*/
	public void
	write( int abyte )
	  throws IOException
	{  myRW.write( abyte );  }

	/**
	** Write range of byte-array.
	*/
	public void
	write( byte[] bytes, int offset, int count )
	  throws IOException
	{  myRW.write( bytes, offset, count );  }


	/**
	** Close.
	** This will also close() the underlying RandomRW container or not,
	** depending on how this instance was constructed.
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

}
