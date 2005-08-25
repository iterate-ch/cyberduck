/*
** Copyright 2002 by Gregory L. Guerin.
** Terms of use:
**  - Briefly: OPEN SOURCE under Artistic License -- credit fairly, use freely, alter carefully.
**  - Fully: <http://www.amug.org/~glguerin/sw/artistic-license.html>
** This file is from the MacBinary Toolkit for Java:
**   <http://www.amug.org/~glguerin/sw/#macbinary> 
*/

package glguerin.io.imp.mac.macosx;

import java.io.*;

import glguerin.io.*;
import glguerin.io.imp.mac.*;


// --- Revision History ---
// 02May2002 GLG  create
// 03May2002 GLG  revise how Tinker is used
// 05Dec2002 GLG  package and class name change
// 10Dec2002 GLG  refactor forkClose()
// 11Dec2002 GLG  change package name
// 13Dec2002 GLG  remove final on class


/**
** An instance of TinFSFork is a ForkRW representing a single open file-fork.
**<p>
** This class assumes that another class (nominally MacOSXForker) has loaded
** the necessary JNI library, and made the function-names in it available.
**
** @author Gregory Guerin
*/

public class TinFSFork
  extends ForkRW
{
	/**
	** Rely on MacOSXForker to load my JNI library.
	*/


	/**
	** Only constructor.
	*/
	public
	TinFSFork( boolean forWriting, int forkRefNum, String tag )
	{
		super( forWriting, forkRefNum, tag );
	}


	/**
	** Flush anything internally buffered to disk.
	*/
	public void 
	flush()
	  throws IOException
	{
		short refNum = refOK();
		checkIO( flushForkX( refNum ) );
	}



	// ###  J N I   F U N C T I O N   B I N D I N G S  ###

	/** JNI call to flush any buffers to disk. */
	private static native short 
	flushForkX( short refNum );



	/** Close the given refNum. */
	protected native int
	forkClose( short refNum );

	/**
	** Return the length of the given refNum's fork.
	*/
	protected native int
	forkLength( short refNum, long[] length );

	/**
	** Return the current R/W position of the given refNum's fork.
	*/
	protected native int
	forkAt( short refNum, long[] position );

	/**
	** Seek to the given position in the given refNum's fork.
	** The position is always relative to the beginning of the file.
	*/
	protected native int
	forkSeek( short refNum, long position );

	/**
	** Read bytes from the current position in the given refNum's fork,
	** for a byte-count given by requestCount, placing the bytes in the buffer
	** beginning at offset 0.
	** Return the actual byte-count read in actualCount[ 0 ].
	*/
	protected native int
	forkRead( short refNum, byte[] buffer, int requestCount, int[] actualCount );


	/**
	** Set the length of the given refNum's fork.
	** When extended, the new bytes in the fork may contain arbitrary
	** and possibly sensitive data from reused disk blocks.
	*/
	protected native int
	forkSetLength( short refNum, long length );

	/**
	** Write bytes to the current position in the given refNum's fork,
	** for a byte-count given by requestCount, taking the bytes from the buffer
	** beginning at offset 0.
	*/
	protected native int
	forkWrite( short refNum, byte[] buffer, int requestCount, int[] actualCount );

}
