
package ch.ethz.ssh2;

/**
 * @author David Kocher, dkocher@cyberduck.ch
 * @version $Id$
 */
public interface SCPListener
{
    public void bytesSent(int bytes);

    public void bytesReceived(int bytes);
}
