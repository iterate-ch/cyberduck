package ch.ethz.ssh2.transport;


import ch.ethz.ssh2.DHGexParameters;
import ch.ethz.ssh2.crypto.dh.DhExchange;
import ch.ethz.ssh2.crypto.dh.DhGroupExchange;
import java.math.BigInteger;
import ch.ethz.ssh2.packets.PacketKexInit;

/**
 * KexState.
 * 
 * @author Christian Plattner, plattner@inf.ethz.ch
 * @version $Id$
 */
public class KexState
{
	public PacketKexInit localKEX;
	public PacketKexInit remoteKEX;
	public NegotiatedParameters np;
	public int state = 0;

	public BigInteger K;
	public byte[] H;
	
	public byte[] hostkey;
	
	public DhExchange dhx;
	public DhGroupExchange dhgx;
	public DHGexParameters dhgexParameters;
}
