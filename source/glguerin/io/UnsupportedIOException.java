/*
** Copyright 1998, 1999, 2001 by Gregory L. Guerin.
** Terms of use:
**  - Briefly: OPEN SOURCE under Artistic License -- credit fairly, use freely, alter carefully.
**  - Fully: <http://www.amug.org/~glguerin/sw/artistic-license.html>
** This file is from the MacBinary Toolkit for Java:
**   <http://www.amug.org/~glguerin/sw/#macbinary> 
*/

package glguerin.io;

import java.io.IOException;


// --- Revision History ---
// 02Apr99 GLG  create
// 03Apr99 GLG  change package
// 03May01 GLG  expand doc-comments


/**
** A sub-class of IOException, thrown when a class does not provide a particular I/O
** capability, or when some I/O operation cannot be accomplished.
** An example of the first use is when
** a Java-only implementation can't provide Mac-specific features like resource-forks.
** An example of the second use is when
** an existing target file already exists and must not be overwritten.
**
** @author Gregory Guerin
*/

public class UnsupportedIOException
  extends IOException
{
	/**
	** Construct with no reason. 
	*/
	public
	UnsupportedIOException()
	{  super();  }


	/**
	** Construct with given reason. 
	*/
	public
	UnsupportedIOException( String reason )
	{  super( reason );  }

}

