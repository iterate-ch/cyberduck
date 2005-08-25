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
// 19Mar99 GLG  refactor to take boolean in constructors, open
// 22Mar99 GLG  declare SecurityException thrown by open() and non-vanilla constructors
// 05Apr99 GLG  expand doc-comments
// 12Apr99 GLG  redesign to use abstract-factory-made ForkAccess
// 10May99 GLG  add mark/reset capability
// 25Apr01 GLG  change args to open() to String pair
// 06Jun01 GLG  match ForkRW changes, cut open()
// 06Jun01 GLG  refactored from ForkInputStream
// 11Jun01 GLG  make skip() reject negative counts
// 11May2002 GLG  add subclosing option to constructor


/**
** A RandomRWInputStream is an InputStream that reads from a
** data-source represented by a RandomRW.
** It is unbuffered.
** This class does not provide random access, since it's an InputStream.
**<p>
** The mark() and reset() methods are provided, and do not require internal buffering.
** Instead, the underlying random-access ForkRW is repositioned (seeked) to
** a previous mark.  Though the semantics of mark() provide a readLimit beyond which
** the mark is invalid, it's possible to implement mark() with no readLimit at all.
** I haven't done that because "creative interpretation" of method semantics can
** run aground on the assumptions of others.
**
** @author Gregory Guerin
**
** @see ForkRW
** @see java.io.InputStream
*/

public class RandomRWInputStream
  extends InputStream
{
	private final boolean willClose;
	private RandomRW myRW;

	private long myMark;
	private long myLimit;


	/**
	** Create with given RandomRW, which must be
	** ready for reading at its current position.
	** When this InputStream is closed, its underlying container is also closed.
	*/
	public
	RandomRWInputStream( RandomRW container )
	{  this( true, container );  }

	/**
	** Create with given RandomRW, which must be
	** ready for reading at its current position.
	** When this InputStream is closed, its underlying container
	** will be closed or left open according to the state of closeContainer.
	*/
	public
	RandomRWInputStream( boolean closeContainer, RandomRW container )
	{
		super();
		willClose = closeContainer;
		myRW = container;
		unmark();
	}


	/**
	** Clear any mark.
	*/
	protected void
	unmark()
	{  myMark = myLimit = -1L;  }

	/**
	** Return true, indicating this class supports mark()/reset().
	*/
	public boolean
	markSupported()
	{  return ( true );  }

	/**
	** Set a mark that expires after readLimit bytes have been read.
	**<p>
	** This method is not synchronized even though the original methed in InputStream
	** is synchronized.  I'm not quite sure why synchronization would be needed, since nothing
	** else about an InputStream supports multiple threads reading the same stream
	** without interference with one another.
	**
	** @see java.io.InputStream#mark
	*/
	public void
	mark( int readLimit )
	{
		try
		{
			myMark = myRW.at();
			myLimit = myMark + readLimit;
		}
		catch ( IOException why )
		{  unmark();  }
	}

	/**
	** Reset the input so it reads from the last-marked position.
	**<p>
	** It's unclear from reading Java source or language specs whether reset() should clear
	** an existing mark or leave it in place.  That is, after invoking reset(), must one
	** reinvoke mark() before reading, or does the original mark remain in place?
	** No specific rule is ever stated, and LineNumberInputStream has a persistent
	** marked line-number, so I've implemented reset() to not clear the previous mark.
	** Only reading past the readLimit of the mark causes the mark to be cleared
	** or invalidated.
	**<p>
	** This method is not synchronized even though the original method in InputStream
	** is synchronized.  I'm not quite sure why synchronization would be needed, since nothing
	** else about an InputStream supports multiple threads reading the same stream
	** without interference with one another.
	**
	** @see java.io.InputStream#reset
	*/
	public void
	reset()
	  throws IOException
	{
		// If not marked, throw IOException.
		// If read-limit exceeded, throw IOException.
		// Otherwise reposition to the mark.
		if ( myLimit < 0L )
			throw new IOException( "Not marked: " + myRW );

		if ( myRW.at() > myLimit )
		{
			unmark();
			throw new IOException( "Beyond mark: " + myRW );
		}

		myRW.seek( myMark );  
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
	** Read bytes into a range of an array.
	*/
	public int 
	read( byte[] buffer, int offset, int count )
	  throws IOException
	{  return ( myRW.read( buffer, offset, count ) );  }


	/**
	** Skip the given number of non-negative bytes.
	** This implementation seeks past the given number of bytes without reading them.
	** It does not affect the mark.
	*/
	public long
	skip( long count )
	  throws IOException
	{
		if ( count <= 0 )
			return ( 0 );
		else
			return ( myRW.skip( count ) );
	}


	/**
	** Return the number of bytes available for reading without blocking.
	** This is effectively the same value as remaining(), but constrained to
	** fit within an int.
	*/
	public int
	available()
	  throws IOException
	{
		long left = myRW.remaining();
		if ( left > Integer.MAX_VALUE )
			return ( Integer.MAX_VALUE );
		else
			return ( (int) left );
	}


	/**
	** Close.
	** This will also close() the underlying RandomRW container or not,
	** depending on how this instance was constructed.
	*/
	public void
	close()
	  throws IOException
	{
		// All closed streams are also unmarked.
		unmark();

		// Ensure that myRW is null, even if an IOException occurs in close()'ing it.
		// Breaking this connection ensures that subsequent operations fail.
		RandomRW container = myRW;
		myRW = null;

		if ( willClose )
			container.close();  
	}

}
