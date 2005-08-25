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
** A NamingStrategy embodies an arbitrary strategy for resolving file-name collisions or conflicts.
** This condition typically arises when a target output file with a desired name might already exist, 
** and one must either algorithmically produce a unique name in a certain way, or fail gracefully.
** Graceful failure means not overwriting the existing file, while distinctly signalling the
** decision not to overwrite.
**<p>
** This class is a Strategy.  It can embody any desired conflict-resolving strategy, including
** unique-name generation, not overwriting, or plain brute-force replacement.
**<p>
** The simplest possible strategy is to always allow overwriting any existing target.
** That strategy is embodied here, in the default base class.
** The strategy can fail if the target is not actually overwritable.
** This class does not actually check the target to see if it's overwritable, it just composes
** a name from given arguments and lets the caller go ahead with it.
**<p>
** The next simplest strategy is "fail if target-name exists".
** NamingStrategyKeep embodies that strategy.
**<p>
** NamingStrategyUnique embodies a unique-naming strategy that generates a unique
** name for an output file so that no file is overwritten.
**
** @author Gregory Guerin
**
** @see NamingStrategyKeep
** @see NamingStrategyUnique
** @see glguerin.macbinary.MacBinaryReceiver
*/

public class NamingStrategy
{
	/**
	** Create the strategy.
	*/
	public
	NamingStrategy()
	{  super();  }



	/**
	** Compose a new leaf name from the given elements, with a 
	** fully composed and suffixed leaf-name no longer than limit characters.
	** All the Strings must be non-null, but they may be empty (zero-length).
	** Subclasses can implement any name-composing strategy at all.
	**<p>
	** The Pathname 'where' represents a directory where the named result will reside.
	** Typically, all the directories named in where must already exist and be accessible.
	** The Pathname itself may be temporarily add()'ed to, cut() from, or otherwise 
	** manipulated by this method,
	** but it is always returned with the same parts and format it had upon entry.
	**<p>
	** The returned String is the composed leaf name or null.
	** If the target does not yet exist,
	** the returned leaf name will represent the original parameter elements, appropriately assembled.
	**<p>
	** If null is returned, it means this strategy cannot compose a non-replacing name.
	** The caller should then take whatever action it deems appropriate in such circumstances.
	**<p>
	** The suffix is used verbatim, so must include any "." or other suffix-separator needed.
	** To compose a leaf name without any other suffix, pass a suffix-String of "".
	** To compose a leaf name with a unique part before the suffix, separate the suffix from the name
	** before calling this method.
	** Each particular algorithm decides where the unique part of a composed name goes, 
	** but it's typically placed between the given leaf and suffix elements.
	*/
	public String
	composeName( Pathname where, String leaf, String suffix, int limit  )
	{
		// Compose an exact literal name with no unique element, escaped as needed.
		return ( compose( new StringBuffer(), leaf, suffix, "", limit ).toString() );
	}


	/**
	** Clear the StringBuffer and assemble the String elements into it, enforcing the limit.
	** If necessary, the leaf-name component will be truncated, so that appending the suffix and uniquely
	** elements does not exceed the limit.  Neither the suffix nor uniquely elements are ever truncated.
	**<p>
	** All the Strings are assumed to be in literal form.
	*/
	protected StringBuffer
	compose( StringBuffer build, String leafName, String suffix, String uniquely, int limit )
	{
		// Determine how much of original leafName we'll keep.
		int keep = limit - suffix.length() - uniquely.length();

		// Put the literal leaf-name into the cleared StringBuffer, then do any truncation on it.
		build.setLength( 0 );
		build.append( leafName );
		if ( build.length() > keep )
			build.setLength( keep );

		// At this point, build contains a possibly truncated
		// leaf-name to which uniquely & then suffix should be appended.
		return ( build.append( uniquely ).append( suffix ) );
	}

}
