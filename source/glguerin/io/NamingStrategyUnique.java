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
// 20May01 GLG  change to NamingStrategy with inner classes
// 21May01 GLG  remove direct escaping support
// 30May01 GLG  add uniquely() to simplify sub-classing for different patterns


/**
** NamingStrategyUnique is a NamingStrategy that composes unique names in a certain pattern,
** so as to prevent overwriting any existing target.
** The pattern is only followed if a desired target already exists.
** The pattern is to insert a ".N" element between the leaf-name and suffix, where the "N" is
** a counting number.  That is, the pattern goes ".1", ".2", ".3", etc.  The numbers are not zero-padded
** nor do they have a fixed field width.
** The pattern starts with ".1" on every call to composeName().
**<p>
** You can change the pattern simply by overriding uniquely().
** You can't change where the pattern is inserted, though, unless you override compose(), too.
**<p>
** This class has no state and is shareable among any number of threads or objects.
**
** @author Gregory Guerin
*/

public class NamingStrategyUnique
  extends NamingStrategy
{
	/**
	** Create the strategy.
	*/
	public
	NamingStrategyUnique()
	{  super();  }


	/**
	** Compose a leaf name from the given elements, with a 
	** fully composed and suffixed leaf-name no longer than limit characters.
	** A unique name is composed and returned.
	** Existence, i.e. uniqueness, is determined using File.exists().
	*/
	public String
	composeName( Pathname where, String leaf, String suffix, int limit  )
	{
		// The first trial File will have a name with no unique element.
		StringBuffer build = new StringBuffer( limit );
		String uniquely = "";

		// Add a DON'T-CARE leaf-name to the Pathname, so swap() will work 1st time through.
		// This leaf-name is never actually used for anything, but it must be present.
		where.add( leaf );

		// Ascend through the counting numbers.
		// The first time through, 'uniquely' is an empty String.
		for ( int i = 1;  true;  ++i )
		{
			// Compose a name, swapping it with Pathname's current leaf name.
			// If a File shows that the item does not exist, return the newly composed leaf.
			where.swap( compose( build, leaf, suffix, uniquely, limit ).toString() );
			if ( ! new File( where.getPath() ).exists() )
				return ( where.cut() );

			// Composed name wasn't unique so regenerate uniquely using build, then go around again.
			build.setLength( 0 );
			uniquely( build, i );
			uniquely = build.toString();
		}
		// Loop never breaks, but returns a String directly.
	}


	/**
	** Append a uniquing pattern to the given StringBuffer.
	** Override this method in a subclass to give a new pattern to the same overall strategy.
	*/
	protected void
	uniquely( StringBuffer build, int num )
	{  build.append( '.' ).append( num );  }

}
