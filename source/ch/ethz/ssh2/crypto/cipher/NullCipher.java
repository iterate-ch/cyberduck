package ch.ethz.ssh2.crypto.cipher;

/**
 * NullCipher.
 * 
 * @author Christian Plattner, plattner@inf.ethz.ch
 * @version $Id$
 */
public class NullCipher implements BlockCipher
{
	private int blockSize = 8;
	
	public NullCipher()
	{
	}

	public NullCipher(int blockSize)
	{
		this.blockSize = blockSize;
	}
	
	public void init(boolean forEncryption, byte[] key)
	{
	}

	public int getBlockSize()
	{
		return blockSize;
	}

	public void transformBlock(byte[] src, int srcoff, byte[] dst, int dstoff)
	{
		System.arraycopy(src, srcoff, dst, dstoff, blockSize);
	}
}
