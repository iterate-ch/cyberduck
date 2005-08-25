/*
** Copyright 1998, 1999, 2001 by Gregory L. Guerin.
** Terms of use:
**  - Briefly: OPEN SOURCE under Artistic License -- credit fairly, use freely, alter carefully.
**  - Fully: <http://www.amug.org/~glguerin/sw/artistic-license.html>
** This file is from the MacBinary Toolkit for Java:
**   <http://www.amug.org/~glguerin/sw/#macbinary> 
*/

package glguerin.io.imp.mac;

import java.io.IOException;
import java.io.FileNotFoundException;
import java.util.Hashtable;


// --- Revision History ---
// 22Mar99 GLG  factor out of FileSystem class
// 30Mar99 GLG  rework checkOSErr
// 01Apr99 GLG  expand doc-comments
// 14Apr99 GLG  edit doc-comments
// 08May99 GLG  add dupFNErr to errTexts
// 14May01 GLG  rename method to checkIOError()
// 02Jun01 GLG  make checkIOError() throw FileNotFoundException for nsvErr
// 02Jun01 GLG  make addErrorText() do removal on null or empty text arg
// 22Jun01 GLG  add errFSNotAFolder and text for -1407 (an FSRef error-code)


/**
** Static constants and methods for working with error-codes and exceptions.
** Provides a static mechanism for designating error-messages for particular error-codes.
** Would be even nicer if it read the messages from a properties-file or other resource,
** but I have to leave something for a rainy day.
**
** @author Gregory Guerin
*/

public class Errors
{
	public static final int nsvErr = -35;
	public static final int eofErr = -39;
	public static final int fnfErr = -43;
	public static final int dupFNErr = -48;
	public static final int dirNFErr = -120;
	public static final int errFSNotAFolder = -1407;


	private static Hashtable errTexts = new Hashtable();

	static
	{
		addErrorText( nsvErr, "No such volume" );
		addErrorText( fnfErr, "No such file" );
		addErrorText( dupFNErr, "Existing item with same name" );
		addErrorText( -54, "Can't write to a locked file" );		// -54: permErr on open file
		addErrorText( dirNFErr, "No such directory" );
		addErrorText( errFSNotAFolder, "Expected folder, got file" );
	}


	/**
	** Return an error-text String for the given resultCode, 
	** or "OSError" if there isn't any.
	*/
	public static String
	getErrorText( int resultCode )
	{
		Integer key = new Integer( resultCode );
		String text = (String) errTexts.get( key );
		return ( (text != null) ? text : "OSError" );
	}

	/**
	** Add a text String to use as an error text,
	** or remove it if text is a null or zero-length String.
	*/
	public static void
	addErrorText( int resultCode, String text )
	{
		Integer key = new Integer( resultCode );
		if ( text == null  ||  text.length() == 0 )
			errTexts.remove( key );
		else
			errTexts.put( key, text );
	}


	/**
	** Make an error message from the given parts.
	*/
	public static String
	makeErrorMessage( int resultCode, String text, Object what )
	{
		StringBuffer build = new StringBuffer( 30 );

		if ( text != null  &&  text.length() > 0 )
			build.append( text ).append( ": " );

		build.append( getErrorText( resultCode ) ).append( '[' ).append( resultCode ).append( ']' );

		if ( what != null )
			build.append( ": " ).append( what );

		return ( build.toString() );
	}


	/**
	** Check resultCode for zero, and throw an IOException if not.
	** The error-codes for
	** fnfErr, dirNFErr, nsvErr, and errFSNotAFolder
	** will throw a FileNotFoundException.
	** All other errors throw a vanilla IOException.
	*/
	public static void
	checkIOError( int resultCode, String text, Object what )
	  throws IOException
	{
		if ( resultCode == 0 )
			return;

		String msg = makeErrorMessage( resultCode, text, what );
		if ( resultCode == fnfErr  ||  resultCode == dirNFErr
				||  resultCode == nsvErr  ||  resultCode == errFSNotAFolder )
			throw new FileNotFoundException( msg );

		throw new IOException( msg );
	}


}
