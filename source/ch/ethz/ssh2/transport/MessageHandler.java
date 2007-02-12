package ch.ethz.ssh2.transport;

import java.io.IOException;

/**
 * MessageHandler.
 * 
 * @author Christian Plattner, plattner@inf.ethz.ch
 * @version $Id$
 */
public interface MessageHandler
{
	public void handleMessage(byte[] msg, int msglen) throws IOException;
}
