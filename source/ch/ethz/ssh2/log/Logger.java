
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
	private static final boolean enabled = true;

	private String className;

	public static Logger getLogger(Class x)
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
		if(enabled)
		{
            org.apache.log4j.Logger.getLogger(className).debug(message);

        }
	}
}
