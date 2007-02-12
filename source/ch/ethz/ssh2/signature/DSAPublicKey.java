package ch.ethz.ssh2.signature;

import java.math.BigInteger;

/**
 * DSAPublicKey.
 * 
 * @author Christian Plattner, plattner@inf.ethz.ch
 * @version $Id$
 */
public class DSAPublicKey
{
	private BigInteger p;
	private BigInteger q;
	private BigInteger g;
	private BigInteger y;

	public DSAPublicKey(BigInteger p, BigInteger q, BigInteger g, BigInteger y)
	{
		this.p = p;
		this.q = q;
		this.g = g;
		this.y = y;
	}

	public BigInteger getP()
	{
		return p;
	}

	public BigInteger getQ()
	{
		return q;
	}

	public BigInteger getG()
	{
		return g;
	}

	public BigInteger getY()
	{
		return y;
	}
}