package ch.ethz.ssh2.crypto.cipher;

/**
 * BlockCipher.
 * 
 * @author Christian Plattner, plattner@inf.ethz.ch
 * @version $Id$
 */
public interface BlockCipher
{
	public void init(boolean forEncryption, byte[] key);

	public int getBlockSize();

	public void transformBlock(byte[] src, int srcoff, byte[] dst, int dstoff);
}
