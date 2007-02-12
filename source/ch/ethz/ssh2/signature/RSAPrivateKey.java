package ch.ethz.ssh2.signature;

import java.math.BigInteger;

/**
 * RSAPrivateKey.
 * 
 * @author Christian Plattner, plattner@inf.ethz.ch
 * @version $Id$
 */
public class RSAPrivateKey
{
	private BigInteger d;
	private BigInteger e;
	private BigInteger n;

	public RSAPrivateKey(BigInteger d, BigInteger e, BigInteger n)
	{
		this.d = d;
		this.e = e;
		this.n = n;
	}

	public BigInteger getD()
	{
		return d;
	}
	
	public BigInteger getE()
	{
		return e;
	}

	public BigInteger getN()
	{
		return n;
	}
	
	public RSAPublicKey getPublicKey()
	{
		return new RSAPublicKey(e, n);
	}
}