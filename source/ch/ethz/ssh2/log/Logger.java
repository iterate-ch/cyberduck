
package ch.ethz.ssh2.log;

/**
 * Logger - a very simple logger, mainly used during development.
 * Is not based on log4j (to reduce external dependencies).
 * However, if needed, something like log4j could easily be
 * hooked in.
 * 
 * @author Christian Plattner, plattner@inf.ethz.ch
 * @version $Id$
 */

public class Logger
{
	private static final boolean enabled = false;
	private static final int logLevel = 99;

	private String className;

	public final static Logger getLogger(Class x)
	{
		return new Logger(x);
	}

	public Logger(Class x)
	{
		this.className = x.getName();
	}

	public final boolean isEnabled()
	{
		return enabled;
	}

	public final void log(int level, String message)
	{
		if ((enabled) && (level <= logLevel))
		{
			long now = System.currentTimeMillis();

			synchronized (this)
			{
				System.err.println(now + " : " + className + ": " + message);
				// or send it to log4j or whatever...
			}
		}
	}
}
