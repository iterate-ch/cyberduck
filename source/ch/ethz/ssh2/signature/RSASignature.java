
package ch.ethz.ssh2.signature;

import java.math.BigInteger;


/**
 * RSASignature.
 * 
 * @author Christian Plattner, plattner@inf.ethz.ch
 * @version $Id$
 */

public class RSASignature
{
	BigInteger s;

	public BigInteger getS()
	{
		return s;
	}

	public RSASignature(BigInteger s)
	{
		this.s = s;
	}
}