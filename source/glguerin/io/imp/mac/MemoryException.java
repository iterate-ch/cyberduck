/*
** Copyright 1998, 1999, 2001 by Gregory L. Guerin.
** Terms of use:
**  - Briefly: OPEN SOURCE under Artistic License -- credit fairly, use freely, alter carefully.
**  - Fully: <http://www.amug.org/~glguerin/sw/artistic-license.html>
** This file is from the MacBinary Toolkit for Java:
**   <http://www.amug.org/~glguerin/sw/#macbinary> 
*/

package glguerin.io.imp.mac;


// --- Revision History ---
// 25Mar99 GLG  create
// 14Apr99 GLG  move to ...imp.mac package


/**
** A sub-class of RuntimeException thrown when a platform-specific class
** encounters a memory problem.
** Typically, this will only be thrown from an underlying implementation,
** and only when something really bad happens.
** I wanted a way to distinguish implementation-dependent run-time failures
** from the standard failures like NullPointerException, etc.
** Otherwise I would have just reused an existing RuntimeException class.
**<p>
** This is pure Java and contains no Mac-platform dependencies.
**
** @author Gregory Guerin
*/

public class MemoryException
  extends RuntimeException
{
	/**
	** Construct with no reason. 
	*/
	public
	MemoryException()
	{  super();  }


	/**
	** Construct with given reason. 
	*/
	public
	MemoryException( String reason )
	{  super( reason );  }

}

