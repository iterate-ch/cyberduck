/*
** Copyright 1998, 1999, 2001-2003 by Gregory L. Guerin.
** Terms of use:
**  - Briefly: OPEN SOURCE under Artistic License -- credit fairly, use freely, alter carefully.
**  - Fully: <http://www.amug.org/~glguerin/sw/artistic-license.html>
** This file is from the MacBinary Toolkit for Java:
**   <http://www.amug.org/~glguerin/sw/#macbinary> 
*/

package glguerin.io;

import java.io.File;
import java.io.IOException;


// --- Revision History ---
// 25Apr01 GLG  refactor from now-obsolete Path and subclasses
// 03May01 GLG  revise setPath(), getPath(), etc.
// 03May01 GLG  add constructor with File arg
// 07May01 GLG  misc revisions
// 08May01 GLG  add doc-comments about relative vs. absolute distinctions in setPath()
// 12May01 GLG  make isUsablePart() public, name changes
// 16May01 GLG  add addPath()
// 17May01 GLG  make replicate() and set() ignore other == this
// 18May01 GLG  prefer canonical over absolute path in setFilePath()
// 21May01 GLG  revise 
// 14Jun01 GLG  add ops() support
// 12Dec2002 GLG  add negative-index support in part()
// 16Dec2002 GLG  improve hashCode with Josh Bloch's sub-element algorithm
// 04Feb2003 GLG  add replica() factory-method


/**
** A Pathname is an alterable ordered sequence of Strings (the parts), representing a progressively
** more qualified name sequence.
** It can also represent any other ordered series of Strings,
** such as a search-path or DNS name.
** The part Strings are presumed to be in literal form, with any encoding or escaping removed.
** Parsing, encoding, escaping, formatting, and interpretation are all done by a PathnameFormat
** associated with each Pathname.  The Pathname works with the literal Strings, while
** the PathnameFormat is responsible for all the transformations to and from the literal Strings.
**<p>
** The part Strings are randomly retrievable by index, starting with 0 for the first part.
** Part Strings can also be retrieved by negative index, which counts from the last
** part backwards toward the first part.  That is, part(-1) is the last part, part(-2) is
** the one before that, and so on.
**<p>
** Part Strings are also retrievable as a String array,
** or as a single path-String with embedded separators (courtesy of a PathnameFormat).
** The parts cannot be randomly altered.  Instead, only the last part in the sequence
** can be changed by adding, cutting, or swapping an entire String part.
** Not supporting random-access alteration, insertion, etc. makes Pathnames simpler and more
** efficient than other structures that one might otherwise use, such as a java.util.Vector.
** It also matches the way that pathnames are typically manipulated.
**<p>
** A Pathname maintains an ongoing count of the operations which have changed its parts.
** This may seem pointless, if not downright silly, at first glance.
** However, the purpose of counting ops is that other classes can use it to discover when Pathnames
** have changed.  In particular, an implementation of FileForker may wish to optimize its
** internal use of Pathnames, and only update internal structures when a Pathname is
** observed to be different than it was during some earlier FileForker operation.
** The count returned by the ops() method will tell it what it needs to know.
**<p>
** The overall order/sequence of a Pathname's parts can be reversed by reverse().
** This is useful when you need to build a Pathname in reverse order from the eventual
** ordered sequence you want.  You can also use it to prepend a part, invoking
** the methods as: reverse(), add(newPart), reverse().
** I'm sure you can think of other uses.
**<p>
** A Pathname relies on a PathnameFormat for parsing, formatting, and similar things.
** Many Pathnames will typically share a single PathnameFormat instance, thereby
** ensuring that they all parse and format Strings in the same way.
** By substituting different PathnameFormats with different characteristics, one can
** use Pathnames in different areas (e.g. manipulating DNS names) much more easily
** than if one were using a Vector or String[].
**<p>
** The only methods that affect the current PathnameFormat are
** setFormat() and replicate() -- and the constructors, of course.  
** Several methods rely on the current PathnameFormat, including
** getPath(), format(), addPath(), setPath(), and toString().
** The setFilePath() method DOES NOT use the current PathnameFormat,
** instead parsing the File with PathnameFormat.LOCAL_FILE.
**<p>
** No methods in this class are synchronized, even when a thread-safety issue
** is known to exist.
** If you need thread-safety you must provide it yourself, say with a synchronized sub-class.
** The most common uses of this class do not involve shared access
** by multiple threads, so synchronizing seemed like time-wasting overkill.
** If you disagree, you have the source...
**
** @author Gregory Guerin
**
** @see PathnameFormat
*/

public class Pathname
{
	/**
	** A zero-length array used to avoid having a null-reference internally.
	** All Pathnames with zero capacity share this array, which they then replace
	** with their own array of appropriate capacity as soon as they need it.
	*/
	private static final String[] noParts = new String[ 0 ];


	/**
	** The internal array of parts, automatically resized as needed to grow.
	** This array is never null, but may be zero-length.
	*/
	private String[] myParts;

	/**
	** The current part-count.
	*/
	private int myCount;

	/**
	** The current ops-count.
	*/
	private int myOps;

	/**
	** The current format.  Do not leave it null for long stretches of time.
	** Among others, the toString() method needs a non-null PathnameFormat.
	*/
	private PathnameFormat myFormat;


	/**
	** Create as empty, with a format of PathnameFormat.LOCAL_FILE.
	*/
	public
	Pathname()
	{
		myParts = noParts;
		myCount = myOps = 0;
		myFormat = PathnameFormat.LOCAL_FILE;
	}


	/**
	** Create with initial space for given part-count, using given format.
	** The capacity can be zero.
	** The format can be null, but it should not be left null for very long,
	** otherwise the methods that rely on the format will fail when called.
	** Note that toString() is a method that relies on the PathnameFormat.
	*/
	public
	Pathname( int capacity, PathnameFormat format )
	{
		this();
		ensureCapacity( capacity );
		setFormat( format );
	}


	/**
	** Create as a replica of the given Pathname -- identical parts and PathnameFormat.
	** This is equivalent to calling the default no-args constructor, followed by a call to
	** replicate().
	** If the other Pathname is null, this constructor has the same result as the
	** no-args constructor, i.e. no parts, a format of PathnameFormat.LOCAL_FILE.
	**<p>
	** To create an exact replica of the other Pathname, including its actual type,
	** call its replica() method.
	** If other is a subclass of Pathname, its replica() method will return that subclass.
	** Constructors, of necessity, can't create subclasses.
	**
	** @see #replica
	*/
	public
	Pathname( Pathname other )
	{
		this();
		replicate( other );
	}


	/**
	** Create a default Pathname, then assign its parts from the File's
	** canonical or absolute pathname.
	** This is equivalent to calling the default no-args constructor,
	** followed by a call to setFilePath().
	*/
	public
	Pathname( File theFile )
	{
		this();
		setFilePath( theFile );
	}



	/**
	** Get the current format, which may be null if never set nor defaulted.
	*/
	public PathnameFormat
	getFormat()
	{  return ( myFormat );  }

	/**
	** Set the format, returning the prior one.
	** If the given format is null, the current format is unchanged.
	** You can only set a Pathname's format to a non-null PathnameFormat.
	** The format may, however, be null if it was never set nor defaulted.
	**<p>
	** Setting the format DOES NOT change the ops() count, since it has
	** no effect on the part Strings.
	*/
	public PathnameFormat
	setFormat( PathnameFormat format )
	{
		PathnameFormat was = myFormat;
		if ( format != null )
			myFormat = format;

		return ( was );
	}


	/**
	** Clear all the parts, but don't reduce the current capacity.
	** The internal array is filled with nulls, so the Strings therein may be GC'ed.
	** The current PathnameFormat is unaffected.
	*/
	public void
	clear()
	{
		myCount = 0;
		for ( int i = 0;  i < myParts.length; ++i )
		{  myParts[ i ] = null;  }
		++myOps;
	}

	/**
	** Ensure the given capacity, growing the array as needed while preserving current contents.
	** This has no effect on the parts or the part-count, only on available array capacity.
	*/
	public void
	ensureCapacity( int capacity )
	{
		if ( myParts.length < capacity )
		{
			String[] wasParts = myParts;
			myParts = new String[ capacity ];		// new array is created cleared
			for ( int i = 0, end = myCount;  i < end;  ++i )
			{
				myParts[ i ] = wasParts[ i ];
				wasParts[ i ] = null;		// remove old reference
			}
		}
	}

	/**
	** Reverse the sequence of the parts:
	**<br>&nbsp;&nbsp; -- The last shall be first, and the first shall be last.
	**<p>
	** Does not reverse the characters in the individual part-Strings, only the sequence.
	** This implementation calls no other methods.
	*/
	public void
	reverse()
	{
		String[] parts = myParts;
		for ( int a = 0, z = myCount - 1;  a < z;  ++a, --z )
		{
			String temp = parts[ a ];
			parts[ a ] = parts[ z ];
			parts[ z ] = temp;
		}
		++myOps;
	}


	/**
	** Return the elapsed count of part-changing operations.
	** This is not terribly useful as an actual count, but merely as a means
	** to tell when a Pathname has been changed.
	**<p>
	** All operations that affect the part Strings in any way increment the ops counter.
	** This includes add(), cut(), set(), swap(), etc.  It also includes clear(), replicate(), and reverse().
	** It DOES NOT include setFormat(), since that does not affect the part Strings.
	*/
	public int
	ops()
	{  return ( myOps );  }

	/**
	** Return the current part-count.
	*/
	public int
	count()
	{  return ( myCount );  }

	/**
	** Return the indexed part, or null if index is out of range.
	** Negative index values count backwards from the last part,
	** where -1 is the last part, -2 is the one before it, etc.
	** A negative index whose magnitude is out of range returns null.
	*/
	public String
	part( int index )
	{
		// An index of -1 refers to last part.
		// That is, desired array-index is myCount - (-index),
		// which is myCount + index, which is index += myCount.
		if ( index < 0 )
			index += myCount;

		if ( index >= 0  &&  index < myCount )
			return ( myParts[ index ] );
		else
			return ( null );
	}

	/**
	** Return the last part, or null if no parts.
	** This is operationally equivalent to 
	** <b>part( count() - 1 )</b> or <b>part( -1 )</b>
	** but is more concise and efficient.
	** This implementation calls no other methods.
	*/
	public String
	last()
	{
		if ( myCount > 0 )
			return ( myParts[ myCount - 1 ] );
		else
			return ( null );
	}


	/**
	** Return true if the given String is usable as a part, hence would be appended by add().
	** This implementation rejects both null and empty (zero-length) Strings.
	** If you override in sub-classes, you must never accept null, as it makes the 
	** result of some methods ambiguous or prone to throwing NullPointerException's.
	*/
	public boolean
	isUsablePart( String part )
	{  return ( part != null  &&  part.length() != 0 );  }

	/**
	** Append the given part String, growing the internal storage as needed.  
	** If toAppend is unusable, according to isUsablePart(), it's quietly ignored.
	**
	** @see #isUsablePart
	*/
	public void
	add( String toAppend )
	{
		if ( isUsablePart( toAppend ) )
		{
			if ( myCount == myParts.length )
				ensureCapacity( myCount + myCount + 1 );

			myParts[ myCount++ ] = toAppend;
			++myOps;
		}
	}

	/**
	** Remove the last part appended and return it.
	** Returns null if there are no parts left to remove, i.e. this Pathname
	** was empty when the method was called.
	*/
	public String
	cut()
	{
		String result = null;
		if ( myCount > 0 )
		{
			// Ensure internal array retains no reference to removed part.
			result = myParts[ --myCount ];
			myParts[ myCount ] = null;
			++myOps;
		}
		return ( result );
	}

	/**
	** Swap the given String with the last part, using cut() and add().  
	** If toSwap is null, the result is equivalent to cut(), since you can't have null parts.
	** If there are no parts to begin with, the result is equivalent to add(),
	** and null is returned.
	*/
	public String
	swap( String toSwap )
	{
		String old = cut();
		add( toSwap );
		return ( old );
	}


	/**
	** Append all the Strings in toAppend using the add(String) method.
	** If toAppend is null or zero-length, nothing is appended.
	**
	** @see #add(String)
	*/
	public void
	add( String[] toAppend )
	{
		if ( toAppend != null  &&  toAppend.length > 0 )
		{
			// To avoid multiple enlargements, ensure capacity once initially.
			ensureCapacity( count() + toAppend.length );

			for ( int i = 0;  i < toAppend.length;  ++i )
			{  add( toAppend[ i ] );  }
		}
	}

	/**
	** Append all the parts of other using the add(String) method.
	** If the other Pathname is null or zero-length, nothing is appended.
	** If the other Pathname is actually this Pathname, then the current
	** list of parts is duplicated once, i.e. foo/bar becomes foo/bar/foo/bar.
	** I don't know why you'd do this, but it's legal.
	**
	** @see #add(String)
	*/
	public void
	add( Pathname other )
	{
		if ( other != null )
		{
			int len = other.count();

			// To avoid multiple enlargements, ensure capacity once initially.
			ensureCapacity( count() + len );

			for ( int i = 0;  i < len;  ++i )
			{  add( other.part( i ) );  }
		}
	}

	/**
	** Call clear(), then add( String[] ).
	** If toCopy is null, this Pathname is cleared and nothing is appended.
	**
	** @see #add(String[])
	*/
	public void
	set( String[] toCopy )
	{
		clear();
		add( toCopy );
	}

	/**
	** Call clear(), then copy all the other's parts to this,
	** but don't change this Pathname's PathnameFormat.
	** If the other Pathname is null, this Pathname is clear()'ed and nothing is appended.
	** If the other Pathname is this, then this Pathname is NOT cleared and nothing happens.
	**
	** @see #add(Pathname)
	*/
	public void
	set( Pathname other )
	{
		if ( other != this )
		{
			clear();
			add( other );
		}
	}


	/**
	** Copy the other's PathnameFormat and all its parts to this.
	** If the other Pathname is this one, nothing happens.
	** If the other Pathname is null, this Pathname is clear()'ed but nothing is appended
	** and this Pathname's PathnameFormat is unchanged.
	**
	** @see #set(Pathname)
	** @see #replica
	*/
	public void
	replicate( Pathname other )
	{
		if ( other != null  &&  other != this )
		{
			setFormat( other.getFormat() );
			set( other );
		}
	}

	/**
	** Create an exact replica Pathname having the same type as this,
	** and copying its parts and PathnameFormat from this.
	**<p>
	** This is a factory-method, as distinct from a constructor,
	** so can be overridden in subclasses.
	** This implementation uses replicate().
	**
	** @see #replicate
	*/
	public Pathname
	replica()
	{
		Pathname replica = new Pathname();
		replica.replicate( this );
		return ( replica );
	}


	/**
	** Return a new array holding all the parts, in sequence.
	** Invokes the methods count() and part(int) to fill the returned array.
	** If there are no parts, the returned array is zero-length, but never null.
	*/
	public String[]
	getParts()
	{
		String[] result = noParts;
		int len = count();
		if ( len > 0 )
		{
			result = new String[ len ];
			for ( int i = 0;  i < len;  ++i )
			{  result[ i ] = part( i );  }
		}
		return ( result );
	}


	/**
	** Parse the path String into parts and append them.
	** The String may represent many parts, one part, or no parts at all.
	** All parsing is done by the current PathnameFormat, which performs everything
	** involving the parsing.
	** If the path is null, it's quietly ignored and nothing happens.
	**<p>
	** The current PathnameFormat is responsible for how the path String
	** is interpreted.  In particular, whether the path is treated as "relative" or "absolute"
	** depends entirely on the PathnameFormat.
	**
	** @see #getPath
	** @see #setPath
	** @see PathnameFormat#parse
	*/
	public void
	addPath( String path )
	{  getFormat().parse( path, this );  }

	/**
	** Clear this Pathname, then call addPath().
	**<p>
	** The current PathnameFormat is responsible for how the path String
	** is interpreted.  In particular, whether the path is treated as "relative" or "absolute"
	** depends entirely on the PathnameFormat.
	**
	** @see #getPath
	** @see PathnameFormat#parse
	*/
	public void
	setPath( String path )
	{
		clear();
		addPath( path );
	}

	/**
	** Clear this Pathname, then set its parts from the File's canonical or absolute path,
	** but always parsing with PathnameFormat.LOCAL_FILE rather than the
	** currently assigned PathnameFormat.  This is done because a File always represents
	** the local file-system convention, which may differ from the convention embodied
	** in the currently assigned PathnameFormat.
	** If the File is null, this instance is just cleared.
	**<p>
	** The File's canonical path is preferred over the absolute path, but the absolute
	** path is used if retrieving the canonical path throws an exception.
	** If the File's canonical path resolves symlinks to their original referent, then
	** this Pathname represents the resolved pathname, not the unresolved one.
	**
	** @see #setPath(String)
	*/
	public void
	setFilePath( File theFile )
	{
		clear();
		if ( theFile != null )
		{
			String path;
			try
			{  path = theFile.getCanonicalPath();  }
			catch ( IOException whatever )
			{  path = theFile.getAbsolutePath();  }

			PathnameFormat.LOCAL_FILE.parse( path, this );
		}
	}

	/**
	** Return a String consisting of the parts
	** separated by an appropriate separator,
	** with or without a leading separator,
	** all as determined by the current PathnameFormat.
	**
	** @see #setPath
	*/
	public String
	getPath()
	{  return ( getFormat().format( this ) );  }


	/**
	** Append this Pathname's parts to the StringBuffer, 
	** under control of the current PathnameFormat.
	** The given StringBuffer is returned, so callers can string method-calls together.
	*/
	public StringBuffer
	format( StringBuffer build )
	{  return ( getFormat().format( this, build ) );  }


	/**
	** Override Object.toString(), returning getPath().
	*/
	public String
	toString()
	{  return ( getPath() );  }


	/**
	** Override Object.hashCode(), returning a value calculated from
	** this Pathname's count() and the hashCode() of each part() String.
	** Calls count() and part(), so this imp will work even
	** if count() and part() are overridden in subclasses.
	**<p>
	** The hash-code value <b>DOES NOT</b> depend on the assigned PathnameFormat.
	** This corresponds to the implementation of equals(), which see.
	**<p>
	** Basic algorithm lifted from Effective Java by Joshua Bloch, p. 38.
	*/
	public int
	hashCode()
	{
		int result = 17;
		for ( int i = count();  --i >= 0;  )
		{  result = 37 * result + part( i ).hashCode();  }

		return ( result );
	}


	/**
	** Override Object.equals(Object), returning true if every
	** part() in this Pathname is String.equals() to its corresponding part() in the other.
	** Calls count() and part(), so this imp will work even
	** if count() and part() are overridden in subclasses.
	** Sub-classes may override, e.g. performing String.equalsIgnoreCase() instead
	** of String.equals().
	**<p>
	** Equality <b>DOES NOT</b> depend on the assigned PathnameFormat.
	** That is, one Pathname can equals() another even if they have different PathnameFormats.
	** This is intentional, and is neither a bug nor an oversight.
	** If you don't like it, you have the source.
	*/
	public boolean
	equals( Object other )
	{
		// Doesn't need a null-check of other, because null is never
		// an instance of any type.
		if ( other instanceof Pathname )
		{
			Pathname that = (Pathname) other;
			int count = count();
			if ( count == that.count() )
			{
				// We don't need to check for null-part in loop, since we're
				// certain to never go out of range.
				while ( --count >= 0 )
				{
					if ( ! part( count ).equals( that.part( count ) ) )
						return ( false );
				}
				return ( true );
			}
		}
		return ( false );
	}


}

