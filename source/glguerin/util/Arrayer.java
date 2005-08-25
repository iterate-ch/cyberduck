/*
** Copyright 1999-2003 by Gregory L. Guerin.
** Terms of use:
**  - Briefly: OPEN SOURCE under Artistic License -- credit fairly, use freely.
**  - Fully: <http://www.amug.org/~glguerin/sw/artistic-license.html>
*/

package glguerin.util;

import java.lang.reflect.Array;


// --- Revision History ---
// 08May2003 GLG  create, copying much from Appender, also stripping much
// 09May2003 GLG  make some things protected, for subclassing


/**
** An Arrayer is a helper for building variable-length arrays of Objects.
** It has limited capabilities, and is no replacement for a 
** general-purpose collection when that is called for.
** It is far simpler than Vector,
** and is appropriate when you just want to build arrays of Objects by appending them.
**<p>
** One notable feature is that an Arrayer will never append nulls.
** This is intentional, though it differs from Vector.
** Also, an Arrayer cannot remove individual items in the array.
** This, too, is intentional.
**<p>
** An Arrayer can enforce the types of Objects added to the array, unlike Vector.
** It does this simply by using an array of a supplied type,
** and maintaining that type when it expands the array.
**
** @author Gregory Guerin
*/

public class Arrayer
{
	/** The array currently in use. */
	protected Object[] myArray;

	/** Count of non-null references now present in myArray. */
	protected int myCount;


	/**
	** Create with no array and a size of 0.
	** This constructor is only for subclasses.
	*/
	protected
	Arrayer()
	{  super();  }

	/**
	** Create with an existing array, which is presumed to hold no useful values.
	** The array should be non-null, but it may be zero-length.
	** The component-type of the array should represent a non-primitive type.
	**<p>
	** The given array is filled entirely with null's.
	** The array given is retained internally, not copied.
	*/
	public
	Arrayer( Object[] array )
	{
		this();
		myArray = array;
		clear();
	}


	/**
	** Return the internally held array.
	** This array may be longer than the current size, but all unused slots should hold nulls.
	** If the array has been expanded since this Arrayer was constructed, the
	** returned array won't be the same object as the one given to the constructor.
	*/
	public Object[]
	array()
	{  return ( myArray );  }

	/**
	** Return the actual count of valid items in the array, i.e. omitting nulls.
	** If some Object instance was append()'ed multiple times, each presence is counted.
	*/
	public int
	size()
	{  return ( myCount );  }

	/**
	** Create and return a new array whose length is exactly my current size,
	** of a component-type identical to the current array, and
	** with all my elements copied into it in sequence order without nulls.
	**<p>
	** Returns null if the component-type is a primitive type, not a reference type.
	**<p>
	** The returned Object[] can be used as an Object[], or it can be cast
	** to an array of the component-type it is presumed to have,
	** i.e. the same as the component-type of the array you supplied to the constructor.
	*/
	public Object[]
	replica()
	{
		Object[] replica = newArray( myCount );
		if ( replica != null )
			System.arraycopy( myArray, 0, replica, 0, myCount );

		return ( replica );
	}


	/**
	** Create and return a new empty array of the given length,
	** of a component-type identical to the current array.
	** Null is returned if the component-type is not a reference type.
	*/
	protected Object[]
	newArray( int length )
	{
		Class kind = myArray.getClass().getComponentType();
		if ( kind == null )
			return ( null );

		return ( (Object[]) Array.newInstance( kind, length ) );
	}


	/**
	** Ensure sufficient total capacity,
	** possibly expanding now to avoid doing it later.
	** To expand, makes a new array and copies the original's contents across.
	**<p>
	** You can never reduce the size of the array with this method.
	*/
	public void
	enough( int capacity )
	{
		if ( myArray.length < capacity )
		{
			// "Expand" the array by making a new one with given capacity,
			// and copying current array's valid elements into it.
			Object[] newly = newArray( capacity );
			if ( newly != null )
			{
				System.arraycopy( myArray, 0, newly, 0, myCount );
				myArray = newly;
			}
		}
	}


	/**
	** Remove all items from the array, i.e. fill it with nulls and zero the count.
	** The capacity is unchanged.
	*/
	public void
	clear()
	{
		myCount = 0;
		for ( int i = 0, limit = myArray.length;  i < limit;  ++i )
		{  myArray[ i ] = null;  }		
	}


	/**
	** Append the given non-null Object to the end of the array,
	** regardless of whether it's already present or not.
	** If the Object is null, it's not appended and nothing happens.
	**<p>
	** The internal array is expanded only as necessary.
	** The array expands by doubling its current length and adding 1.
	** This is better than a straight doubling, because it works with zero-length arrays.
	*/
	public void
	append( Object toAppend )
	{
		if ( toAppend != null )
		{
			// If necessary, expand before appending new item.
			if ( myCount >= myArray.length )
				enough( myCount + myCount + 1 );
	
			// At this point, we are certain to have a slot at the myCount position.
			myArray[ myCount++ ] = toAppend;
		}
	}

}
