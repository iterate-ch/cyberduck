
package ch.ethz.ssh2.channel;

/**
 * RemoteForwardingData. Data about a requested remote forwarding.
 * 
 * @author Christian Plattner, plattner@inf.ethz.ch
 * @version $Id$
 */
public class RemoteForwardingData
{
	public String bindAddress;
	public int bindPort;

	String targetAddress;
	int targetPort;
}
