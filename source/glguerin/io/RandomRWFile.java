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
import java.lang.reflect.*;


// --- Revision History ---
// 26Mar99 GLG  make this generic version
// 02Apr99 GLG  add throwing of UnsupportedIOException for unsupported features
// 08Apr99 GLG  move to package "glguerin.io"
// 06May99 GLG  implement read() and read(byte[],int.int)
// 06Jun01 GLG  refactor from RandomAccessIOFile
// 27Jan2003 GLG  add reflective setLength() implementation


/**
** A RandomRWFile uses a RandomAccessFile internally to provide RandomRW capabilities.
**<p>
** This implementation automatically detects the presence or absence of RandomAccessFile.setLength(),
** and uses reflection to provide the capability in Java 2, without compromising
** compatibility with JDK 1.1.
** That is, this class's setLength() method automatically works on JDK 1.2 or higher,
** but throws an UnsupportedIOException on JDK 1.1 or earlier.
** The speed cost of reflection is inconsequential compared to the speed of doing the I/O itself.
**
** @author Gregory Guerin
**
** @see java.io.RandomAccessFile
*/

public class RandomRWFile
  extends RandomRW
{
	/**
	** The Java-form file-name opened.
	*/
	private String myFileName;

	/**
	** The underlying RandomAccessFile.
	*/
	private RandomAccessFile myIO;


	/**
	** Create with no target-file assigned but read/write option constrained as given.
	** Needs subsequent open() before using.
	*/
	public
	RandomRWFile( boolean allowWriting )
	{  super( allowWriting );  }

	/**
	** Construct with given name, opening for given I/O.
	*/
	public
	RandomRWFile( String name, boolean allowWriting )
	  throws IOException, SecurityException
	{
		this( allowWriting );
		open( name );
	}

	/**
	** Construct with target File, opening for given I/O.
	*/
	public
	RandomRWFile( File target,  boolean allowWriting )
	  throws IOException, SecurityException
	{  this( target.getPath(), allowWriting );  }


	/**
	** Open for access with the given values.
	** Throws an IOException if already open.
	** Throws a SecurityException if target is not accessible for given access.
	*/
	public void
	open( String name )
	  throws IOException, SecurityException
	{
		if ( myIO != null )
			throw new IOException( "Already open: " + myFileName );

		// Open for access determined by constructor's boolean flag.
		myIO = new RandomAccessFile( name, (isWritable() ? "rw" : "r") );
		myFileName = name;
	}


	/** Will be null if we can't setLength(), non-null if we can. */
	private static Method toSetLength;
	static
	{
		try
		{
			// Load the RandomAccessFile class and look for a setLength() method.
			// RandomAccessFile.setLength() takes a 'long' arg, and returns void.
			// The Class[] represents the method signature (types of the args).
			Class randomClass = Class.forName( "java.io.RandomAccessFile" );
			toSetLength = randomClass.getMethod( "setLength", new Class[] { long.class } );
		}
		catch ( ClassNotFoundException why )
		{  /* FALL THRU */  }
		catch ( NoSuchMethodException why )
		{  /* FALL THRU */  }
		catch ( SecurityException why )
		{  /* FALL THRU */  }
	}


	/**
	** Set the length of the underlying container, truncating or extending
	** the container as needed.  When extended, the new bytes may contain arbitrary
	** and possibly sensitive data from reused disk blocks.  To be certain of the
	** content, you would be wise to overwrite them with zeros.
	**<p>
	** This implemention uses reflection to invoke a setLength() Method, if one exists.
	** Absent a setLength(long) method in RandomAccessFile,
	** it always throws an UnsupportedIOException, since there's
	** no way to do setLength() with a 1.1 RandomAccessFile.
	*/
	public void
	setLength( long length )
	  throws IOException
	{
		// To eliminate reflection and be solely Java2-compatible, use this...
//		myIO.setLength( length );		// -- the Java 2 code

		// If there's no Method, fail.  If there is a Method, invoke it.
		if ( toSetLength == null )
			throw new UnsupportedIOException( "Not supported" );

		// The exception-handling is more complex than the actual method-call.
		try
		{
			toSetLength.invoke( myIO, new Object[] { new Long( length ) } );
		}
		catch ( IllegalAccessException why )
		{
			// Shouldn't happen.
			throw new UnsupportedIOException( "IllegalAccessException: " + why.getMessage() );
		}
		catch ( InvocationTargetException why )
		{
			// Rethrow underlying IOException's and RuntimeException's.
			// Everything else is thrown as an IllegalStateException().
			Throwable underlying = why.getTargetException();

			if ( underlying instanceof IOException )
				throw (IOException) underlying;

			if ( underlying instanceof RuntimeException )
				throw (RuntimeException) underlying;

			// I wish there were a RuntimeException that had InvocationTargetException behavior.
			throw new IllegalStateException( "Caught InvocationTargetException wrapping: " + underlying.getClass() );
		}
	}


	/**
	** Return the current length of the byte-container, measured in bytes.
	** Calls the underlying RandomAccessFile.length().
	*/
	public long
	length()
	  throws IOException
	{  return ( myIO.length() );  }

	/**
	** Move the read/write location to the given offset,
	** measured as a byte-count from the beginning of the container.
	** Calls the underlying RandomAccessFile.seek().
	*/
	public void
	seek( long place )
	  throws IOException
	{  myIO.seek( place );  }

	/**
	** Return the current location at which reading or writing will next occur.
	** Calls the underlying RandomAccessFile.getFilePointer().
	*/
	public long
	at()
	  throws IOException
	{  return ( myIO.getFilePointer() );  }


	/**
	** Read one byte, returning it unsigned in low 8-bits of int,
	** or return -1 on EOF.
	*/
	public int
	read()
	  throws IOException
	{  return ( myIO.read() );  }

	/**
	** Read bytes into a range of an array, returning count actually read,
	** or -1 on EOF.
	*/
	public int 
	read( byte[] buffer, int offset, int count )
	  throws IOException
	{  return ( myIO.read( buffer, offset, count ) );  }


	/**
	** Write the low 8-bits of abyte.
	*/
	public void
	write( int abyte )
	  throws IOException
	{  myIO.write( abyte );  }

	/**
	** Write the given range of bytes.
	*/
	public void
	write( byte[] bytes, int offset, int count )
	  throws IOException
	{  myIO.write( bytes, offset, count );  }



	/**
	** Close the file, releasing all internal resources.
	*/
	public void
	close()
	  throws IOException
	{
		if ( myIO != null )
		{
			myIO.close();
			myIO = null;
		}
	}


}
