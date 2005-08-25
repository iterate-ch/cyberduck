/*
** Copyright 2003 by Gregory L. Guerin.
** Terms of use:
**  - Briefly: OPEN SOURCE under Artistic License -- credit fairly, use freely, alter carefully.
**  - Fully: <http://www.amug.org/~glguerin/sw/artistic-license.html>
** This file is from the MacBinary Toolkit for Java:
**   <http://www.amug.org/~glguerin/sw/#macbinary> 
*/

package glguerin.io.imp.mac.macosx;

import java.io.*;

import glguerin.io.*;
import glguerin.io.imp.mac.*;


// --- Revision History ---
// 09Jan2003 GLG  create empty useless stub
// 10Jan2003 GLG  start adding useful innards
// 11Jan2003 GLG  add more useful innards
// 12Jan2003 GLG  more work
// 13Jan2003 GLG  strip out FNSubscription-testing stuff


/**
** TinWatcher is the FileForker.Watcher for CarbonMacOSXForker.
** It uses the FNSubscription API available in 10.1 and higher.
**<p>
** The FNSubscription callbacks require an active Carbon event-loop
** in order for any originating FNNotify change-signals to call the callback.
** In retrospect, this is understandable, given that FNSubscription and FNNotify are
** parts of the Carbon API.  It certainly wasn't documented, though.
**
** @author Gregory Guerin
*/

public class TinWatcher
  extends FileForker.Watcher
{
	// ## Rely on MacOSXForker to load JNI library that has my native functions in it.
	static
	{
		nativeInit();
	}


	/** Accumulates received messages ("change-signals") in callback(). */
	private final SignalCounter counter;

	/** The pathname of the item being watched; null after destroy(). */
	private String pathname;

	/**
	** Magic token used by native code, but held by Java.
	**<p>
	** I've declared this field as 'volatile' to compel the JVM to always load/store it.
	** This declaration may or may not have the desired effect, however.
	** I don't think 'volatile' will harm anything, though I read somewhere that many JVMs
	** do not actually honor the declaration.  So its benefits are uncertain, at best.
	** Since it seems to be harmless, and signifies what I want to happen, I do it anyway.
	*/
	private volatile int magicToken;


	/**
	** Only constructor.  Not accessible outside package or subclasses.
	**<p>
	** Although a pathname is provided by the String, the internal implementation
	** need not use FNSubscribeByPath().
	*/
	protected
	TinWatcher( String name, boolean ignoreBroadcasts )
	{
		super();
		counter = new SignalCounter();

		pathname = name;

		// If we can't make a magic token, this instance is stillborn.
		magicToken = makeToken( name, ignoreBroadcasts );
		if ( magicToken == 0 )
			destroy();
	}


	/**
	** This is the method the FNSubscriptionRef eventually calls back to.
	** In this implementation, the message value is irrelevant.
	** The counter always increments by 1 for each message (signal) received.
	**<p>
	** This method could be 'private' and the JNI code could still call it.
	**<p>
	** This method isn't synchronized.  All synchronization is done only
	** in SignalCounter.  This ensures that no external code can block the
	** native callback function from returning by grabbing this TinWatcher's
	** object lock and not relinquishing it.  In short, it provides a certain
	** amount of deadlock protection.  It's not perfect, though.
	**<p>
	* Method name:    callback
	* Signature: (I)V
	*/
	protected void
	callback( int message )
	{  counter.add( 1 );  }


	/**
	** Return the platform-dependent pathname of the item being watched,
	** in a form appropriate for a java.io.File or a suitably platform-aware Pathname.
	** This is the value of getPath() of the FileForker at the time the
	** Watcher was made by makeWatcher().
	** The pathname can't be changed once a Watcher is made.
	**<p>
	** Returns null after destroy().
	*/
	public String
	watchedPath()
	{  return ( pathname );  }


	/**
	** Return the current cumulative count of received change-signals,
	** which may be zero or negative.
	** A zero count indicates that no change-signals have ever arrived.
	** The count will be negative after it rolls over from Integer.MAX_VALUE.
	** This is not normally a problem, since the actual magnitude of the count rarely matters,
	** only the fact that it changes incrementally as change-signals are received.
	**<p>
	** This imp always returns the counter value.
	** After destroy(), the counter value stops changing, but is still returned.
	*/
	public int
	getChangeCount()
	{  return ( counter.counter() );  }


	/**
	** The current Thread will wait up to the given internal for a change-signal to arrive,
	** or for the internal cumulative count of change-signals to differ from the compareCount.
	** Returns the current cumulative count of received change-signals,
	** which may be zero or negative.  The actual count value is rarely important.
	** The fact that it changed is what's important.
	**<p>
	** Any number of Threads can wait for a change on the same Watcher. 
	** The interval determines how many milliseconds the calling Thread will wait for
	** change-signals to arrive.  
	** Negative intervals do not wait at all. 
	** The only delay is the synchronization and implementation latency.  
	** A zero interval waits indefinitely.
	** A positive interval waits up to that many milliseconds.
	**<p>
	** Regardless of the interval given, the calling thread only waits when
	** the given compareCount is initially equal to the current cumulative count.
	** If the counts differ initially, then the current cumulative count is immediately returned.
	** This use of a caller's provided compareCount allows any number of callers to wait for
	** any number of change-signals, letting each one decide for itself which changes
	** it has or hasn't seen.
	**<p>
	** If destroy() is called while threads are waiting, they are all awakened.
	** After a destroy(), no threads will be able to waitForChange().
	** Any waitForChange() calls after a destroy() throw an IllegalStateException.
	**<p>
	** If the target directory is moved or renamed, the Watcher continues watching
	** the original referent.  No notice of the move or rename is sent.
	** If the target directory is delered, the Watcher will never receive another change-signal.
	** No notice of the deletion is sent.
	**
	** @exception java.lang.IllegalStateException
	**  is thrown when this Watcher is destroyed, or the target has become inaccessible.
	** @exception java.lang.InterruptedException
	**  is thrown when the wait() was interrupted.
	**
	** @see FileForker#signalChange
	*/
	public int
	waitForChange( long interval, int compareCount )
	  throws InterruptedException
	{
		// ## I wonder if this initial test should be synchronized, e.g. on the SignalCounter.
		// ## One hopes the 'volatile' keyword is effective, but who knows...
		if ( magicToken == 0 )
			throw new IllegalStateException( "Watcher is destroyed" );

		return ( counter.waitFor( interval, compareCount ) );
	}


	/**
	** Destroy all the internal elements of this Watcher, making it unusable.
	** Calling destroy() more than once on the Watcher is always harmless.
	**<p>
	** A Watcher may consume OS resources accumulating change-signals,
	** even though no one will ever wait for those changes.
	** You should call destroy() when you want to stop using a Watcher.
	** Eventually, finalize() will call destroy(), but until then it will consume resources
	** for each change-signal sent on the watched target item.
	**<p>
	** This method is called by Watcher.finalize(), so the 
	** finalizer will eventually clean up a Watcher's internal resources.
	*/
	public void
	destroy()
	{
		// An externally visible sign that this Watcher is destroyed.
		pathname = null;

		// Discard the internal callback()-invoking element.
		int token = magicToken;
		magicToken = 0;
		if ( token != 0 )
			killToken( token );

		// This is a convenient way to awaken all waiting Threads.
		// For a TinWatcher that's already destroyed, it should be harmless (have no effect).
		counter.add( 0 );
	}


	/**
	** This idempotent method performs all the native-side once-only initialization.
	** It's synchronized on the class-lock so it can't be called re-entrantly from other threads.
	** That shouldn't happen, but one never knows.
	*/
	private static synchronized native int
		nativeInit();

	/**
	** Create a magic token for this TinWatcher, using FNSubscribe() and a native FSRef.
	** Or using FNSubscribeByPath().
	**  int makeToken( String targetPath, boolean ignoreBroadcasts );
	*/
	private native int
		makeToken( String targetPath, boolean ignoreBroadcasts );

	/**
	** Destroy the magic token held by this TinWatcher.
	*/
	private native void
		killToken( int token );

}
