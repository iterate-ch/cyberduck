
package ch.ethz.ssh2;

/**
 * A <code>SFTPv3FileHandle</code>.
 * 
 * @author Christian Plattner, plattner@inf.ethz.ch
 * @version $Id$
 */

public class SFTPv3FileHandle
{
	final SFTPv3Client client;
	final byte[] fileHandle;
	boolean isClosed = false;

	/* The constructor is NOT public */

	SFTPv3FileHandle(SFTPv3Client client, byte[] h)
	{
		this.client = client;
		this.fileHandle = h;
	}

	/**
	 * Get the SFTPv3Client instance which created this handle. 
	 * 
	 * @return A SFTPv3Client instance.
	 */
	public SFTPv3Client getClient()
	{
		return client;
	}

	/**
	 * Check if this handle was closed with the {@link SFTPv3Client#closeFile(SFTPv3FileHandle)} method
	 * of the <code>SFTPv3Client</code> instance which created the handle.
	 * 
	 * @return if the handle is closed.
	 */
	public boolean isClosed()
	{
		return isClosed;
	}
}
