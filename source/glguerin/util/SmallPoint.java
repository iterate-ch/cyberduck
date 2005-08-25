/*
** Copyright 1998, 1999, 2001 by Gregory L. Guerin.
** Terms of use:
**  - Briefly: OPEN SOURCE under Artistic License -- credit fairly, use freely, alter carefully.
**  - Fully: <http://www.amug.org/~glguerin/sw/artistic-license.html>
** This file is from the MacBinary Toolkit for Java:
**   <http://www.amug.org/~glguerin/sw/#macbinary> 
*/

package glguerin.util;


// --- Revision History ---
// 14Jun99 GLG  create an XY-coordinate point with short values
// 23Jun99 GLG  comment editing
// 24Jun99 GLG  change package
// 17Aug99 GLG  add toString() that says the coordinate values


/**
** A SmallPoint is similar to a java.awt.Point, but keeps its values in short integers
** and has almost no methods.  It's basically just a holder for a pair of shorts.
** It is returned from FileInfo implementations as a "Finder icon at" position.
** I made this class rather than use java.awt.Point, simply to avoid a dependence on java.awt.*.
** Call me paranoid, but Sun likes to deprecate things a little too freely for my peace of mind.
**
** @author Gregory Guerin
**
** @see java.awt.Point
*/

public class SmallPoint 
{
	/**  The X part of the coordinate. */
	public short x;

	/**  The Y part of the coordinate. */
	public short y;


	/**
	** Construct at 0,0.
	*/
	public
	SmallPoint() 
	{  super();  x = y = 0;  }

	/**
	** Construct at given location.
	*/
	public
	SmallPoint( short atX, short atY ) 
	{  super();  set( atX, atY );  }

	/**
	** Construct at same place as atXY.
	** If atXY is null, the new instance has coordinates of 0,0.
	*/
	public
	SmallPoint( SmallPoint atXY ) 
	{  super();  set( atXY );  }


	/**
	** Set both coordinates.  Not thread-safe.
	*/
	public void
	set( short atX, short atY ) 
	{  x = atX;  y = atY;  }

	/**
	** Set both coordinates.  Not thread-safe.
	** If atXY is null, this instance has coordinates set to 0,0.
	*/
	public void
	set( SmallPoint atXY ) 
	{
		if ( atXY == null )
			set( (short) 0, (short) 0 );
		else
			set( atXY.x, atXY.y );
	}


	/**
	** Return a String listing the two coordinate values.
	*/
	public String
	toString() 
	{  return ( "X: " + x + ", Y: " + y );  }

}
