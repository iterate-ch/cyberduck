
package ch.ethz.ssh2.crypto.cipher;

/**
 * This is CTR mode as described in draft-ietf-secsh-newmodes-XY.txt
 * 
 * @author Christian Plattner, plattner@inf.ethz.ch
 * @version $Id$
 */
public class CTRMode implements BlockCipher
{
	byte[] X;
	byte[] Xenc;

	BlockCipher bc;
	int blockSize;
	boolean doEncrypt;

	int count = 0;

	public void init(boolean forEncryption, byte[] key)
	{
	}

	public CTRMode(BlockCipher tc, byte[] iv, boolean doEnc) throws IllegalArgumentException
	{
		bc = tc;
		blockSize = bc.getBlockSize();
		doEncrypt = doEnc;

		if (blockSize != iv.length)
			throw new IllegalArgumentException("IV must be " + blockSize + " bytes long! (currently " + iv.length + ")");

		X = new byte[blockSize];
		Xenc = new byte[blockSize];

		System.arraycopy(iv, 0, X, 0, blockSize);
	}

	public final int getBlockSize()
	{
		return blockSize;
	}

	public final void transformBlock(byte[] src, int srcoff, byte[] dst, int dstoff)
	{
		bc.transformBlock(X, 0, Xenc, 0);

		for (int i = 0; i < blockSize; i++)
		{
			dst[dstoff + i] = (byte) (src[srcoff + i] ^ Xenc[i]);
		}

		for (int i = (blockSize - 1); i >= 0; i--)
		{
			X[i]++;
			if (X[i] != 0)
				break;

		}
	}
}
