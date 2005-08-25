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
// 03May01 GLG  factor out of earlier code
// 04May01 GLG  refactor to embody "replace existing file" strategy
// 11May01 GLG  add isDir arg to resolveFile()
// 11May01 GLG  return null instead of throwing an exception
// 20May01 GLG  change to NamingStrategy
// 21May01 GLG  remove direct escaping support


/**
** NamingStrategyKeep is a NamingStrategy that composes a single name and either
** returns it to be written or returns null to prevent overwriting.
** This strategy does not produce unique names, nor does it require the target to be unwritable.
** This class has no state and is shareable among any number of threads or objects.
**
** @author Gregory Guerin
*/

public class NamingStrategyKeep 
  extends NamingStrategy
{
	/**
	** Create the strategy.
	*/
	public NamingStrategyKeep()
	{  super();  }


	/**
	** Compose a leaf name from the given elements, with a 
	** fully composed and suffixed leaf-name no longer than limit characters.
	** If a target with the composed name already exists, null is returned rather than
	** a composed name that would allow overwriting.
	*/
	public String
	composeName( Pathname where, String leaf, String suffix, int limit  )
	{
		// Compose a trial name with no unique element, then make it into a File.
		// Check for File's existence and return either null or the composed leaf name.
		where.add( compose( new StringBuffer(), leaf, suffix, "", limit ).toString() );
		if ( new File( where.getPath() ).exists() )
		{
			where.cut();
			return ( null );
		}
		else
			return ( where.cut() );
	}

}
