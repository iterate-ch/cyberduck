
package ch.ethz.ssh2;

import ch.cyberduck.core.sftp.SCPInputStream;
import ch.cyberduck.core.sftp.SCPOutputStream;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.UnsupportedCharsetException;

/**
 * A very basic <code>SCPClient</code> that can be used to copy files from/to
 * the SSH-2 server. On the server side, the "scp" program must be in the PATH.
 * <p>
 * This scp client is thread safe - you can download (and upload) different sets
 * of files concurrently without any troubles. The <code>SCPClient</code> is
 * actually mapping every request to a distinct {@link Session}.
 * 
 * @author Christian Plattner, plattner@inf.ethz.ch
 * @version $Id$
 */

public class SCPClient
{
	Connection conn;

    String charsetName = null;

    /**
     * Set the charset used to convert between Java Unicode Strings and byte encodings
     * used by the server for paths and file names.
     *
     * @see #getCharset()
     * @param charset the name of the charset to be used or <code>null</code> to use the platform's
     *        default encoding.
     * @throws IOException
     */
    public void setCharset(String charset) throws IOException
    {
        if (charset == null)
        {
            charsetName = charset;
            return;
        }

        try
        {
            Charset.forName(charset);
        }
        catch (UnsupportedCharsetException e)
        {
            throw (IOException) new IOException("This charset is not supported").initCause(e);
        }
        charsetName = charset;
    }

    /**
     * The currently used charset for filename encoding/decoding.
     *
     * @see #setCharset(String)
     *
     * @return The name of the charset (<code>null</code> if the platform's default charset is being used)
     */
    public String getCharset()
    {
        return charsetName;
    }

    public class LenNamePair
	{
		public long length;
		String filename;
	}

	public SCPClient(Connection conn)
	{
		if (conn == null)
			throw new IllegalArgumentException("Cannot accept null argument!");
		this.conn = conn;
	}

	public void readResponse(InputStream is) throws IOException
	{
		int c = is.read();

		if (c == 0)
			return;

		if (c == -1)
			throw new IOException("Remote scp terminated unexpectedly.");

		if ((c != 1) && (c != 2))
			throw new IOException("Remote scp sent illegal error code.");

		if (c == 2)
			throw new IOException("Remote scp terminated with error.");

		String err = receiveLine(is);
		throw new IOException("Remote scp terminated with error (" + err + ").");
	}

	public String receiveLine(InputStream is) throws IOException
	{
		StringBuilder sb = new StringBuilder(30);

		while (true)
		{
			/* This is a random limit - if your path names are longer, then adjust it */

			if (sb.length() > 8192)
				throw new IOException("Remote scp sent a too long line");

			int c = is.read();

			if (c < 0)
				throw new IOException("Remote scp terminated unexpectedly.");

			if (c == '\n')
				break;

			sb.append((char) c);

		}
		return sb.toString();
	}

	public LenNamePair parseCLine(String line) throws IOException
	{
		/* Minimum line: "xxxx y z" ---> 8 chars */

        if (line.length() < 8)
			throw new IOException("Malformed C line sent by remote SCP binary, line too short.");

		if ((line.charAt(4) != ' ') || (line.charAt(5) == ' '))
			throw new IOException("Malformed C line sent by remote SCP binary.");

		int length_name_sep = line.indexOf(' ', 5);

		if (length_name_sep == -1)
			throw new IOException("Malformed C line sent by remote SCP binary.");

		String length_substring = line.substring(5, length_name_sep);
		String name_substring = line.substring(length_name_sep + 1);

		if ((length_substring.length() <= 0) || (name_substring.length() <= 0))
			throw new IOException("Malformed C line sent by remote SCP binary.");

		if ((6 + length_substring.length() + name_substring.length()) != line.length())
			throw new IOException("Malformed C line sent by remote SCP binary.");

        final long len;
        try
		{
			len = Long.parseLong(length_substring);
		}
		catch (NumberFormatException e)
		{
			throw new IOException("Malformed C line sent by remote SCP binary, cannot parse file length.");
		}

		if (len < 0)
			throw new IOException("Malformed C line sent by remote SCP binary, illegal file length.");

		LenNamePair lnp = new LenNamePair();
		lnp.length = len;
		lnp.filename = name_substring;

		return lnp;
	}

    /**
     * The session for opened for this SCP transfer must be closed using
     * SCPOutputStream#close
     * @param remoteFile
     * @param length The size of the file to send
     * @param remoteTargetDirectory
     * @param mode
     * @return
     * @throws IOException
     */
    public SCPOutputStream put(final String remoteFile, long length, String remoteTargetDirectory, String mode) throws IOException
    {
        Session sess = null;

        if (null == remoteFile)
            throw new IllegalArgumentException("Null argument.");
        if (null == remoteTargetDirectory)
            remoteTargetDirectory = "";
        if (null == mode)
            mode = "0600";
        if (mode.length() != 4)
            throw new IllegalArgumentException("Invalid mode.");

        for (int i = 0; i < mode.length(); i++)
            if (Character.isDigit(mode.charAt(i)) == false)
                throw new IllegalArgumentException("Invalid mode.");

        remoteTargetDirectory = (remoteTargetDirectory.length() > 0) ? remoteTargetDirectory : ".";

        String cmd = "scp -t -d \"" + remoteTargetDirectory + "\"";

        sess = conn.openSession();
        sess.execCommand(cmd, charsetName);

        return new SCPOutputStream(this, sess, remoteFile, length, mode);
    }

    /**
     * The session for opened for this SCP transfer must be closed using
     * SCPInputStream#close
     * @param remoteFile
     * @return
     * @throws IOException
     */
    public SCPInputStream get(final String remoteFile) throws IOException
    {
        Session sess = null;

        if (null == remoteFile)
            throw new IllegalArgumentException("Null argument.");

        if (remoteFile.length() == 0)
            throw new IllegalArgumentException("Cannot accept empty filename.");

        String cmd = "scp -f";
        cmd += (" \"" + remoteFile + "\"");

        sess = conn.openSession();
        sess.execCommand(cmd, charsetName);

        return new SCPInputStream(this, sess);
    }
}