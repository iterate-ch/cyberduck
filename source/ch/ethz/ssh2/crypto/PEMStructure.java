
package ch.ethz.ssh2.crypto;

/**
 * Parsed PEM structure.
 * 
 * @author Christian Plattner, plattner@inf.ethz.ch
 * @version $Id$
 */

public class PEMStructure
{
	int pemType;
	String dekInfo[];
	String procType[];
	byte[] data;
}