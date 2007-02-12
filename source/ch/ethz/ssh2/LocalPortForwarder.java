
package ch.ethz.ssh2;

import java.io.IOException;
import java.net.InetSocketAddress;

import ch.ethz.ssh2.channel.ChannelManager;
import ch.ethz.ssh2.channel.LocalAcceptThread;

/**
 * A <code>LocalPortForwarder</code> forwards TCP/IP connections to a local
 * port via the secure tunnel to another host (which may or may not be identical
 * to the remote SSH-2 server). Checkout {@link Connection#createLocalPortForwarder(int, String, int)}
 * on how to create one.
 * 
 * @author Christian Plattner, plattner@inf.ethz.ch
 * @version $Id$
 */
public class LocalPortForwarder
{
	ChannelManager cm;

	String host_to_connect;

	int port_to_connect;

	LocalAcceptThread lat;

	LocalPortForwarder(ChannelManager cm, int local_port, String host_to_connect, int port_to_connect)
			throws IOException
	{
		this.cm = cm;
		this.host_to_connect = host_to_connect;
		this.port_to_connect = port_to_connect;

		lat = new LocalAcceptThread(cm, local_port, host_to_connect, port_to_connect);
		lat.setDaemon(true);
		lat.start();
	}

	LocalPortForwarder(ChannelManager cm, InetSocketAddress addr, String host_to_connect, int port_to_connect)
			throws IOException
	{
		this.cm = cm;
		this.host_to_connect = host_to_connect;
		this.port_to_connect = port_to_connect;

		lat = new LocalAcceptThread(cm, addr, host_to_connect, port_to_connect);
		lat.setDaemon(true);
		lat.start();
	}

	/**
	 * Stop TCP/IP forwarding of newly arriving connections.
	 * 
	 * @throws IOException
	 */
	public void close() throws IOException
	{
		lat.stopWorking();
	}
}
