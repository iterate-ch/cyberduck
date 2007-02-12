
package ch.ethz.ssh2.sftp;

/**
 * 
 * Values for the 'text-hint' field in the SFTP ATTRS data type.
 *
 * @author Christian Plattner, plattner@inf.ethz.ch
 * @version $Id$
 *
 */
public class AttrTextHints
{
	/**
	 * The server knows the file is a text file, and should be opened
	 * using the SSH_FXF_ACCESS_TEXT_MODE flag.
	 */
	public static final int SSH_FILEXFER_ATTR_KNOWN_TEXT = 0x00;

	/**
	 * The server has applied a heuristic or other mechanism and
	 * believes that the file should be opened with the
	 * SSH_FXF_ACCESS_TEXT_MODE flag.
	 */
	public static final int SSH_FILEXFER_ATTR_GUESSED_TEXT = 0x01;

	/**
	 * The server knows the file has binary content.
	 */
	public static final int SSH_FILEXFER_ATTR_KNOWN_BINARY = 0x02;

	/**
	 * The server has applied a heuristic or other mechanism and
	 * believes has binary content, and should not be opened with the
	 * SSH_FXF_ACCESS_TEXT_MODE flag.
	 */
	public static final int SSH_FILEXFER_ATTR_GUESSED_BINARY = 0x03;
}
