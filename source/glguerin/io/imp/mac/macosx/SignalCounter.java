/*
** Copyright 2003 by Gregory L. Guerin.
** Terms of use:
**  - Briefly: OPEN SOURCE under Artistic License -- credit fairly, use freely, alter carefully.
**  - Fully: <http://www.amug.org/~glguerin/sw/artistic-license.html>
*/

package glguerin.io.imp.mac.macosx;


// --- Revision History ---
// 10Jan2003 GLG  create


/**
** A SignalCounter is a specialized thread-safe integer counter.
** It's behavior is intended for situations where two or more threads
** are producing and consuming simple signals (notifications), and a queue is overkill.
** Each signal received by the SignalCounter changes the counter.
** Threads can safely retrieve the current count at any time.
** Threads can also wait for the count to change, with or without a time-limit.
**<p>
** The counter's current value is returned from counter().
** The counter is changed with add(), supplying an integer to add to the counter.
** Zero is allowed, which doesn't change the counter but does awaken waiting threads.
**<p>
** Waiting for a change is always in reference to a caller-supplied integer value.
** That is, a SignalCounter client waits for the count to change from
** the supplied reference value to any other value.
** This is done because one or more signals may have incremented the counter
** while the client was away, and the client should be able to discern this fact.
**<p>
** All the wait()/notify()/notifyAll() and thread-safe state
** manipulation is encapsulated in SignalCounter's methods.
** Normally, you should not externally synchronize on a SignalCounter,
** nor should you externally call wait() or notify() on it. 
** Doing so may adversely affect the SignalCounter's desired behavior.
** For example, if you externally synchronize on a SignalCounter,
** you will block all access to its add() and counter() methods.
** The whole point is to NOT block out add()'ing and counter()'ing.
**<p>
** <b>A SignalCounter is not a counting semaphore.</b>
** It bears some superficial resemblances to a counting semaphore,
** but it is woefully inadequate for that use.  If you're thinking of using
** a SignalCounter as a counting semaphore, then you don't understand
** either one of them adequately.
**
** @author Gregory Guerin
*/

public class SignalCounter 
{
	private int counter;

	/**
	** Create with an initial count of 0.
	*/
	public
	SignalCounter()
	{  super();  }


	/** Return the current counter value. */
	public synchronized int
	counter()
	{  return ( counter );  }

	/**
	** Add the given increment to the counter, calling notifyAll() so all waiting clients awaken.
	** If the increment is zero, the counter's value is unchanged, but clients are still awakened.
	*/
	public synchronized void
	add( int increment )
	{
		counter += increment;
		this.notifyAll();
	}

	/**
	** Wait for the counter's value to change to a value other than refCount,
	** returning the current counter value on return.
	** The returned counter is only correct at the moment of return.
	** The internal counter may change at any point after this method returns.
	**<p>
	** If add( 0 ) is called, the counter is unchanged but waiting clients are awakened.
	** If you don't want that to happen, then don't call add( 0 ).
	**<p>
	** For negative intervals, this method does not wait() at all.
	** It simply returns the current counter immediately.
	**<p>
	** For zero intervals, the current Thread waits indefinitely (approximately forever)
	** for the counter to change.
	**<p>
	** For positive intervals, the current Thread waits up to that number of milliseconds
	** for the counter to change, returning the current counter value on return.
	**<p>
	** If the counter's value already differs from refCount when this method is called,
	** then the current counter is immediately returned and no wait() occurs at all,
	** regardless of the value of interval.
	**
	** @exception  java.lang.InterruptedException
	**   is thrown when the wait was interrupted.
	*/
	public synchronized int
	waitFor( long interval, int refCount )
	  throws InterruptedException
	{
		if ( counter == refCount  &&  interval >= 0 )
		{  this.wait( interval );  }

		// Always return the current counter to the caller.
		return ( counter );
	}
}
