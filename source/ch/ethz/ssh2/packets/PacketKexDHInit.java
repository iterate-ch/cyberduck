package ch.ethz.ssh2.packets;

import java.math.BigInteger;

/**
 * PacketKexDHInit.
 * 
 * @author Christian Plattner, plattner@inf.ethz.ch
 * @version $Id$
 */
public class PacketKexDHInit
{
	byte[] payload;

	BigInteger e;

	public PacketKexDHInit(BigInteger e)
	{
		this.e = e;
	}

	public byte[] getPayload()
	{
		if (payload == null)
		{
			TypesWriter tw = new TypesWriter();
			tw.writeByte(Packets.SSH_MSG_KEXDH_INIT);
			tw.writeMPInt(e);
			payload = tw.getBytes();
		}
		return payload;
	}
}
