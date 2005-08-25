/*
** Copyright 1998, 1999, 2001 by Gregory L. Guerin.
** Terms of use:
**  - Briefly: OPEN SOURCE under Artistic License -- credit fairly, use freely, alter carefully.
**  - Fully: <http://www.amug.org/~glguerin/sw/artistic-license.html>
** This file is from the MacBinary Toolkit for Java:
**   <http://www.amug.org/~glguerin/sw/#macbinary> 
*/

package glguerin.io.imp.mac;

import java.io.*;

import glguerin.io.RandomRW;


// --- Revision History ---
// 17Mar99 GLG  first stub version using JDirect2
// 18Mar99 GLG  refactor somewhat
// 18Mar99 GLG  add finalize() that closes the fork
// 19Mar99 GLG  add Stream-like read() and write() methods
// 19Mar99 GLG  add Thread.yield() in several methods
// 20Mar99 GLG  add isWritable(); remove some junk
// 31Mar99 GLG  rework internal name-handling
// 05Apr99 GLG  expand doc-comments
// 12Apr99 GLG  factor out as abstract class common to JDirect 1 & 2 implementations
// 13Apr99 GLG  doc-comments
// 14Apr99 GLG  minor tweaks
// 26Apr99 GLG  remove synchronization; add thread-safety warning to doc-comment
// 22Jun99 GLG  change myRefNum to a short
// 22Jun99 GLG  add forceClose() to ease close-at-exit implementations
// 05Jun01 GLG  cut isOpen(), change toString()
// 06Jun01 GLG  FIX: rework for correct long-pathname support
// 07Jun01 GLG  refactor to give readRaw(), writeRaw(), and intRef
// 20Jun01 GLG  refactor so abstract protected methods return osErr, not throw IOException
// 21Jun01 GLG  continute refactoring to work with FSRef's


/**
** A ForkRW provides basic read/write access to the bytes in a file-fork,
** either data-fork or resource-fork, using parameter types that will work for
** implementations based on FSSpec or FSRef APIs.  
** Resource-fork contents are not interpreted as resources, but are simple raw bytes.
** This suffices for reading and writing the fork, such as in file-copying, encode/decode
** of MacBinary, compress/decompress, and various other tasks.
**<p>
** No methods in this class are synchronized, even when a thread-safety issue
** is known to exist.
** If you need thread-safety you must provide it yourself.  
** The most common uses of this class do not involve shared access
** by multiple threads, so synchronizing seemed like a time-wasting overkill.
** If you disagree, you have the source...
**<p>
** The Apple reference materials I used to make this class are:
**<ul type="disc">
**   <li> <i>Inside Macintosh: Files</i><br>
**    Chapter 2 describes the refNum and other OS elements 
**    that this class is principally based upon.  
**   </li>
**   <li>xxx URL to <b>FileManger.pdf</b><br>
**    Describes the new FSRef-based API introduced with Mac OS 9.
**   </li>
**   <li>xxx URL to <b>Carbon File Manager docs</b><br>
**    Summarizes the FSRef API and says what's in Carbon and what's not.
**   </li>
**</ul>
**
** @author Gregory Guerin
*/

abstract public class ForkRW
  extends RandomRW
{
	/**
	** The OS's refNum representation of an open file-fork,
	** acceptable to either the classical or FSRef APIs.
	** When zero, this ForkRW is closed.  
	** When non-zero, including negative, this ForkRW is open.
	** At this time, negative refNum's should never arise, because those refer to
	** open devices and drivers, not file-forks.  And since the abstract forkOpen()
	** method is only supposed to open file-forks, not devices nor drivers, it should be moot.
	*/
	private short myRefNum;

	/** The tagged-name assigned during open(). */
	private String myTag;


	/** A common buffer used by single-byte read() and write(). */
	private byte[] oneByte = new byte[ 1 ];

	/** Pointer to long for forkAt() and forkLength(). */
	private long[] longRef = new long[ 1 ];

	/** Pointer to int used by readRaw(), writeRaw(), and subclasses. */
	protected final int[] intRef = new int[ 1 ];


	/**
	** Construct with given parameters, representing a fork that is already open.
	*/
	protected
	ForkRW( boolean forWriting, int forkRefNum, String tag )
	{
		super( forWriting );
		myRefNum = (short) forkRefNum;
		myTag = tag;
	}



	/**
	** Check for an error, throwing an IOException if so.
	** On success, Thread.yield() is called.
	*/
	protected final void
	checkIO( int resultCode  )
	  throws IOException
	{
		Errors.checkIOError( resultCode, null, this );
		Thread.yield();
	}


	/**
	** Check that we're open, i.e. have a non-zero refNum, throwing an IOException if not.
	** If OK, returns the non-zero refNum.
	*/
	protected short
	refOK()
	  throws IOException
	{
		if ( myRefNum == 0 )
			throw new IOException( "Not open" );
		return ( myRefNum );
	}


	/**
	** Return the current length of the open fork.
	*/
	public long
	length()
	  throws IOException
	{
		checkIO( forkLength( refOK(), longRef ) );
		return ( longRef[ 0 ] );
	}


	/**
	** Return the current position within the open fork.
	*/
	public long
	at()
	  throws IOException
	{
		checkIO( forkAt( refOK(), longRef ) );
		return ( longRef[ 0 ] );
	}


	/**
	** Seek to the given position within the open fork, relative to the first byte of the fork.
	*/
	public void
	seek( long position )
	  throws IOException
	{
		if ( position < 0 )
			position = 0;

		checkIO( forkSeek( refOK(), position ) );
	}

	/**
	** Set the length of the open fork.
	** Only works if the fork is open for writing.
	** On success, Thread.yield() is called.
	**<p>
	** When extended, the new bytes in the fork may contain arbitrary
	** and possibly sensitive data from reused disk blocks.  To be certain of the
	** content, you would be wise to overwrite them with zeros.
	*/
	public void
	setLength( long length )
	  throws IOException
	{
		// I don't know if the OS requires open-for-writing in order to set length.
		// To be safe, I check for the condition myself.
		checkWritable();
		checkIO( forkSetLength( refOK(), length ) );
	}


	/**
	** Check that the fork is open and the supplied args are safe for a read or write,
	** throwing an IOException if not.  Also stores the count into intRef[0] in preparation
	** for a call to one of the lower-level methods.
	*/
	private void
	prepRW( byte[] buffer, int count )
	  throws IOException
	{
		refOK();

		if ( buffer == null )
			throw new IOException( "Null buffer" );

		if ( count < 0  ||  count > buffer.length )
			throw new IOException( "Bad count: " + count );
	}


	/**
	** Try to read the requested count of bytes into the buffer, beginning at offset 0.
	** Return the count of bytes actually read, or throw an IOException.
	** This is the most fundamental read method, which other forms are built upon.
	** On success, Thread.yield() is called.
	*/
	protected int
	readRaw( byte[] buffer, int count )
	  throws IOException
	{
		prepRW( buffer, count );
		int osErr = forkRead( myRefNum, buffer, count, intRef );
		checkIO( (osErr == Errors.eofErr) ? 0 : osErr );
		return ( intRef[ 0 ] );
	}

	/**
	** Try to write the requested count of bytes from the buffer, beginning at offset 0.
	** This is the most fundamental write method, which other forms are built upon.
	** On success, Thread.yield() is called.
	*/
	protected void
	writeRaw( byte[] buffer, int count )
	  throws IOException
	{
		prepRW( buffer, count );
		checkIO( forkWrite( myRefNum, buffer, count, intRef ) );
	}



	/**
	** Read one byte, returning it unsigned in low 8-bits of int,
	** or return -1 on EOF.
	*/
	public int
	read()
	  throws IOException
	{
		if ( readRaw( oneByte, 1 ) == 0 )
			return ( -1 );
		else
			return ( (int) (0xFF & oneByte[ 0 ]) );
	}

	/**
	** Read bytes into a range of an array, returning the actual count read, 
	** or -1 if no bytes were read at EOF.
	** If offset is zero, this quickly translates into a call on read( byte[], int ).
	** For a non-zero offset, a temporary byte[] is created and used as a buffer,
	** since read( byte[], int ) can only read into a byte[] starting at offset 0.
	**
	*/
	public int 
	read( byte[] buffer, int offset, int count )
	  throws IOException
	{
		// Distinguish this now, to avoid confusing it with EOF below.
		if ( count == 0 )
			return ( 0 );

		int got;
		if ( offset == 0 )
			got = readRaw( buffer, count );
		else
		{
			byte[] temp = new byte[ count ];
			got = readRaw( temp, count );
			if ( got > 0 )
				System.arraycopy( temp, 0, buffer, offset, got );
		}

		return ( got == 0 ? -1 : got );
	}


	/**
	** Write one byte.
	*/
	public void
	write( int abyte )
	  throws IOException
	{
		oneByte[ 0 ] = (byte) abyte;
		writeRaw( oneByte, 1 );
	}

	/**
	** Write a range from a byte-array.
	*/
	public void
	write( byte[] bytes, int offset, int count )
	  throws IOException
	{
		if ( count <= 0 )
			return;

		// If offset isn't zero, copy data to another array, zero-based.
		// Sucky, but no alternative given ForkRW's write() args.
		if ( offset != 0 )
		{
			byte[] temp = new byte[ count ];
			System.arraycopy( bytes, offset, temp, 0, count );
			bytes = temp;
		}

		writeRaw( bytes, count );
	}



	/**
	** Close the fork if it's open.
	** Only throws an IOException if an error arises while closing, not if already closed.
	** At return, the internal field myRefNum is guaranteed to be zero, regardless of exceptions thrown.
	*/
	protected void
	forceClose()
	  throws IOException
	{
		// By zeroing myRefNum before calling checkIO, we ensure that
		// even if there's an exception in closing, myRefNum will be clear.
		// This is smaller than a "try/finally" structure.
		if ( myRefNum != 0 )
		{
			short refNum = myRefNum;
			myRefNum = 0;
			myTag = "closed";
			checkIO( forkClose( refNum ) );
		}
	}

	/**
	** Close the open fork.
	** Throws an IOException if already closed, or if an error arose while closing.
	** On success, Thread.yield() is called.
	*/
	public void
	close()
	  throws IOException
	{
		refOK();
		forceClose();
	}


	/**
	** Return the String assigned during open(), including its fork-designation.
	** The String is in UniCode, though the internal PString-name was actually used
	** to open the fork, encoded in the default encoding, i.e. NameString.toPStr().
	*/
	public String
	toString()
	{  return ( myTag );  }


	/**
	** Ensure that the fork is closed before this object is GC'ed.
	*/
	protected void
	finalize()
	  throws IOException
	{  forceClose();  }


	// ##  A B S T R A C T   U N D E R L Y I N G   M E T H O D S  ##

	/**
	** Return the length of the given refNum's fork.
	*/
	abstract protected int
	forkLength( short refNum, long[] position );

	/**
	** Return the current R/W position of the given refNum's fork.
	*/
	abstract protected int
	forkAt( short refNum, long[] position );

	/**
	** Seek to the given position in the given refNum's fork, or throw an IOException.
	** The position is always relative to the beginning of the file.
	*/
	abstract protected int
	forkSeek( short refNum, long position );

	/**
	** Set the length of the given refNum's fork, or throw an IOException.
	** When extended, the new bytes in the fork may contain arbitrary
	** and possibly sensitive data from reused disk blocks.
	*/
	abstract protected int
	forkSetLength( short refNum, long length );

	/**
	** Read bytes from the current position in the given refNum's fork,
	** for a byte-count given by requestCount, placing the bytes in the buffer
	** beginning at offset 0,
	** and returning a result-code.
	** Return the actual byte-count read in actualCount[ 0 ].
	** Reading to or past EOF should not throw an IOException, just return
	** the actual number of bytes read, which may be zero.
	*/
	abstract protected int
	forkRead( short refNum, byte[] buffer, int requestCount, int[] actualCount );

	/**
	** Write bytes to the current position in the given refNum's fork,
	** for a byte-count given by requestCount, placing the bytes in the buffer
	** beginning at offset 0,
	** and returning a result-code.
	** Return the actual byte-count written in actualCount[ 0 ].
	*/
	abstract protected int
	forkWrite( short refNum, byte[] buffer, int requestCount, int[] actualCount );

	/**
	** Close the given refNum.
	*/
	abstract protected int
	forkClose( short refNum );

}
