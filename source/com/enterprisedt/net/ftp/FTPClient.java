/**
 *
 *  Java FTP client library.
 *
 *  Copyright (C) 2000-2003  Enterprise Distributed Technologies Ltd
 *
 *  www.enterprisedt.com
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *  Bug fixes, suggestions and comments should be sent to bruce@enterprisedt.com
 */

package com.enterprisedt.net.ftp;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.util.Properties;
import java.util.Vector;

import org.apache.log4j.Logger;

import ch.cyberduck.core.Preferences;

//import ch.cyberduck.core.Transcript;
//import ch.cyberduck.core.TranscriptFactory;

/**
 * Supports client-side FTP. Most common
 * FTP operations are present in this class.
 *
 * @author Bruce Blackshaw
 * @version $Revision$
 */
public class FTPClient {
    private static Logger log = Logger.getLogger(FTPClient.class);

    /**
     * Socket responsible for controlling
     * the connection
     */
    private FTPControlSocket control = null;

    /**
     * Socket responsible for transferring
     * the data
     */
    private FTPDataSocket data = null;

    /**
     * Socket timeout for both data and control. In
     * milliseconds
     */
    private int timeout = 0;

    /**
     * Record of the transfer type - make the default ASCII
     */
    private FTPTransferType transferType = FTPTransferType.ASCII;

    /**
     * Record of the connect mode - make the default PASV (as this was
     * the original mode supported)
     */
    private FTPConnectMode connectMode = FTPConnectMode.PASV;

    /**
     * Holds the last valid reply from the server on the control socket
     */
    private FTPReply lastValidReply;

//    private Transcript transcript;

    public FTPClient() {
        super();
    }

    /**
     * Creates the control
     * socket
     *
     * @param remoteHost  the remote hostname
     * @param controlPort port for control stream
     */
    public void connect(String remoteHost, int controlPort) throws IOException, FTPException {
        control = new FTPControlSocket(remoteHost, controlPort);
//        transcript = TranscriptFactory.getImpl(remoteHost);
    }

    /**
     * Set the TCP timeout on the underlying socket.
     * <p/>
     * If a timeout is set, then any operation which
     * takes longer than the timeout value will be
     * killed with a java.io.InterruptedException. We
     * set both the control and data connections
     *
     * @param millis The length of the timeout, in milliseconds
     */
    public void setTimeout(int millis) throws IOException {

        this.timeout = millis;
        control.setTimeout(millis);
    }

    /**
     * Set the connect mode
     *
     * @param mode ACTIVE or PASV mode
     */
    public void setConnectMode(FTPConnectMode mode) {
        connectMode = mode;
    }

    /**
     * Login into an account on the FTP server. This
     * call completes the entire login process
     *
     * @param user     user name
     * @param password user's password
     */
    public void login(String user, String password) throws IOException, FTPException {

        this.user(user);
        if (lastValidReply.getReplyCode().equals("331")) {
            this.password(password);
        }
    }


    /**
     * Supply the user name to log into an account
     * on the FTP server. Must be followed by the
     * password() method - but we allow for
     *
     * @param user user name
     */
    private void user(String user) throws IOException, FTPException {

        String reply = control.sendCommand("USER " + user);

        // we allow for a site with no password - 230 response
        String[] validCodes = {"230", "331"};
        lastValidReply = control.validateReply(reply, validCodes);
    }


    /**
     * Supplies the password for a previously supplied
     * username to log into the FTP server. Must be
     * preceeded by the user() method
     *
     * @param password user's password
     */
    private void password(String password) throws IOException, FTPException {

        String reply = control.sendCommand("PASS " + password);

        // we allow for a site with no passwords (202)
        String[] validCodes = {"230", "202"};
        lastValidReply = control.validateReply(reply, validCodes);
    }

    /**
     * Set up SOCKS v4/v5 proxy settings. This can be used if there
     * is a SOCKS proxy server in place that must be connected thru.
     * Note that setting these properties directs <b>all</b> TCP
     * sockets in this JVM to the SOCKS proxy
     *
     * @param port SOCKS proxy port
     * @param host SOCKS proxy hostname
     */
    public static void initSOCKS(String port, String host) {
        Properties props = System.getProperties();
        props.put("socksProxyPort", port);
        props.put("socksProxyHost", host);
        System.setProperties(props);
    }

    /**
     * Set up SOCKS username and password for SOCKS username/password
     * authentication. Often, no authentication will be required
     * but the SOCKS server may be configured to request these.
     *
     * @param username the SOCKS username
     * @param password the SOCKS password
     */
    public static void initSOCKSAuthentication(String username, String password) {
        Properties props = System.getProperties();
        props.put("java.net.socks.username", username);
        props.put("java.net.socks.password", password);
        System.setProperties(props);
    }

    /**
     * Get the name of the remote host
     *
     * @return remote host name
     */
    String getRemoteHostName() {
        return control.getRemoteHostName();
    }


    /**
     * Issue arbitrary ftp commands to the FTP server.
     *
     * @param command    ftp command to be sent to server
     * @param validCodes valid return codes for this command
     * @return the text returned by the FTP server
     */
    public String quote(String command, String[] validCodes) throws IOException, FTPException {

        String reply = control.sendCommand(command);

        // allow for no validation to be supplied
        if (validCodes != null && validCodes.length > 0) {
            lastValidReply = control.validateReply(reply, validCodes);
            return lastValidReply.getReplyText();
        }
        else {
            throw new FTPException("Valid reply code must be supplied");
        }
    }


    /**
     * Validate that the put() or get() was successful
     *
     * @modified
     */
    public void validateTransfer() throws IOException, FTPException {
        log.debug("validateTransfer");
        // check the control response
        String[] validCodes = {"226", "250"};
        String reply = control.readReply();
        lastValidReply = control.validateReply(reply, validCodes);
    }

    /**
     * Request the server to set up the put
     *
     * @param remoteFile name of remote file in
     *                   current directory
     * @param append     true if appending, false otherwise
     */
    private void initPut(String remoteFile, boolean append) throws IOException, FTPException {

        // set up data channel
        data = control.createDataSocket(connectMode);
        data.setTimeout(timeout);

        // send the command to store
        String cmd = append ? "APPE " : "STOR ";
        String reply = control.sendCommand(cmd + remoteFile);

        // Can get a 125 or a 150
        String[] validCodes = {"125", "150"};
        lastValidReply = control.validateReply(reply, validCodes);
    }


    /**
     * Request to the server that the get is set up
     *
     * @param remoteFile name of remote file
     * @modified
     */
    private void initGet(String remoteFile, long resume) throws IOException, FTPException {
        // set up data channel
        data = control.createDataSocket(connectMode);
        data.setTimeout(timeout);

        // send the restart command
        if (resume > 0) {
            String[] validReplyCodes = {"125", "350"};
            lastValidReply = control.validateReply(control.sendCommand("REST " + resume), validReplyCodes);
        }

        // send the retrieve command
        String reply = control.sendCommand("RETR " + remoteFile);

        // Can get a 125 or a 150
        String[] validCodes1 = {"125", "150"};
        lastValidReply = control.validateReply(reply, validCodes1);
    }


    /**
     * Put as ASCII, i.e. read a line at a time and write
     * inserting the correct FTP separator
     *
     * @param remoteFile name of remote file we are writing to
     * @param append     true if appending, false otherwise
     */
//    public java.io.OutputStream putASCII(String remoteFile, boolean append) throws IOException, FTPException {
//        this.initPut(remoteFile, append);
//        return new ToNetASCIIOutputStream(data.getOutputStream());
//    }

    /**
     * Put as binary, i.e. read and write raw bytes
     *
     * @param remoteFile name of remote file we are writing to
     * @param append     true if appending, false otherwise
     */
    public java.io.OutputStream put(String remoteFile, boolean append) throws IOException, FTPException {
        this.initPut(remoteFile, append);
        return data.getOutputStream();
    }

    /**
     * Get as ASCII, i.e. read a line at a time and write
     * using the correct newline separator for the OS
     *
     * @param localPath  full path of local file to write to
     * @param remoteFile name of remote file
     */
//    public java.io.InputStream getASCII(String remoteFile, long resume) throws IOException, FTPException {
//        this.initGet(remoteFile, resume);
//        return new FromNetASCIIInputStream(data.getInputStream(), lineSeparator);
//    }

    /**
     * Get as binary file, i.e. straight transfer of data
     *
     * @param localPath  full path of local file to write to
     * @param remoteFile name of remote file
     */
    public java.io.InputStream get(String remoteFile, long resume) throws IOException, FTPException {
        this.initGet(remoteFile, resume);
        return data.getInputStream();
    }

	
    /**
     * Run a site-specific command on the
     * server. Support for commands is dependent
     * on the server
     *
     * @param command the site command to run
     * @return true if command ok, false if
     *         command not implemented
     */
    public boolean site(String command) throws IOException, FTPException {
        // send the retrieve command
        String reply = control.sendCommand("SITE " + command);

        // Can get a 200 (ok) or 202 (not impl). Some
        // FTP servers return 502 (not impl)
        String[] validCodes = {"200", "202", "502"};
        lastValidReply = control.validateReply(reply, validCodes);

        // return true or false? 200 is ok, 202/502 not
        // implemented
        if (reply.substring(0, 3).equals("200")) {
            return true;
        }
        else {
            return false;
        }
    }

	
    /**
     * List current directory's contents as an array of strings of
     * filenames.
     *
     * @return an array of current directory listing strings
     */
    public String[] dir() throws IOException, FTPException {
        return dir(null, true);
    }

	
    /**
     * List a directory's contents as an array of strings of filenames.
     *
     * @param dirname name of directory(<b>not</b> a file mask)
     * @return an array of directory listing strings
     */
    public String[] dir(String dirname) throws IOException, FTPException {
        return dir(dirname, true);
    }


    /**
     * List a directory's contents as an array of strings. A detailed
     * listing is available, otherwise just filenames are provided.
     * The detailed listing varies in details depending on OS and
     * FTP server. Note that a full listing can be used on a file
     * name to obtain information about a file
     *
     * @param dirname name of directory (<b>not</b> a file mask)
     * @param full    true if detailed listing required
     *                false otherwise
     * @return an array of directory listing strings
     */
    public String[] dir(String dirname, boolean full) throws IOException, FTPException {
        // set up data channel
        data = control.createDataSocket(connectMode);
        data.setTimeout(timeout);

        // send the retrieve command
        String command;
        if (full) {
            if (Preferences.instance().getProperty("ftp.sendExtendedListCommand").equals("true")) {
                command = "LIST -a ";
            }
            else {
                command = "LIST ";
            }
        }
        else {
            command = "NLST ";
        }
        if (dirname != null) {
            command += dirname;
        }

        // some FTP servers bomb out if NLST has whitespace appended
        command = command.trim();
        String reply = control.sendCommand(command);

        // check the control response. wu-ftp returns 550 if the
        // directory is empty, so we handle 550 appropriately. Similarly
        // proFTPD returns 450
        String[] validCodes1 = {"125", "150", "450", "550"};
        lastValidReply = control.validateReply(reply, validCodes1);

        // an empty array of files for 450/550
        String[] result = new String[0];

        // a normal reply ... extract the file list
        String replyCode = lastValidReply.getReplyCode();
        if (!replyCode.equals("450") && !replyCode.equals("550")) {
            // get an character input stream to read data from .
            LineNumberReader in = new LineNumberReader(new InputStreamReader(data.getInputStream(),
                    Preferences.instance().getProperty("browser.charset.encoding")));

            // read a line at a time
            Vector entries = new Vector();
            String line = null;
            while ((line = in.readLine()) != null) {
                entries.add(line);
                //@todo transcript.log(line); //bad performance
            }
            try {
                in.close();
                data.close();
            }
            catch (IOException ignore) {
                log.error(ignore.getMessage());
                ignore.printStackTrace();
            }

            // check the control response
            String[] validCodes2 = {"226", "250"};
            reply = control.readReply();
            lastValidReply = control.validateReply(reply, validCodes2);

            // empty array is default
            if (!entries.isEmpty()) {
                result = (String[])entries.toArray(result);
            }
        }
        return result;
    }

	
    /**
     * Gets the latest valid reply from the server
     *
     * @return reply object encapsulating last valid server response
     */
    public FTPReply getLastValidReply() {
        return lastValidReply;
    }

	
    /**
     * Get the current transfer type
     *
     * @return the current type of the transfer,
     *         i.e. BINARY or ASCII
     */
    public FTPTransferType getTransferType() {
        return transferType;
    }

	
    /**
     * Set the transfer type
     *
     * @param type the transfer type to
     *             set the server to
     */
    public void setTransferType(FTPTransferType type) throws IOException, FTPException {
        // determine the character to send
        String typeStr = FTPTransferType.ASCII_CHAR;
        if (type.equals(FTPTransferType.BINARY)) {
            typeStr = FTPTransferType.BINARY_CHAR;
        }

        // send the command
        String reply = control.sendCommand("TYPE " + typeStr);
        lastValidReply = control.validateReply(reply, "200");

        // record the type
        transferType = type;
    }

	
    /**
     * Wrapper for the command <code>size [fileName]</code>.  If the file does
     * not exist, we return -1;
     */
    public long size(String remoteFile) throws IOException {//, FTPException{
		try {
			String reply = control.sendCommand("SIZE " + remoteFile);
			lastValidReply = control.validateReply(reply, "213");
			
			// parse the reply string .
			String replyText = lastValidReply.getReplyText();
			try {
				return Long.parseLong(replyText);
			}
			catch (NumberFormatException ex) {
				if (replyText.indexOf(' ') != -1) {
					try {
						return Long.parseLong(replyText.substring(0, replyText.indexOf(' ')));
					}
					catch (NumberFormatException e) {
					}
				}
				throw new FTPException("Failed to parse reply: " + replyText);
			}
		}
		catch(FTPException e) {
			log.error(e.getMessage());
			return -1;
		}
    }
	
	
    /**
     * Delete the specified remote file
     *
     * @param remoteFile name of remote file to
     *                   delete
     */
    public void delete(String remoteFile) throws IOException, FTPException {
        String reply = control.sendCommand("DELE " + remoteFile);
        lastValidReply = control.validateReply(reply, "250");
    }


    /**
     * Rename a file or directory
     *
     * @param from name of file or directory to rename
     * @param to   intended name
     */
    public void rename(String from, String to) throws IOException, FTPException {
        String reply = control.sendCommand("RNFR " + from);
        lastValidReply = control.validateReply(reply, "350");

        reply = control.sendCommand("RNTO " + to);
        lastValidReply = control.validateReply(reply, "250");
    }


    /**
     * Delete the specified remote working directory
     *
     * @param dir name of remote directory to
     *            delete
     */
    public void rmdir(String dir) throws IOException, FTPException {
        String reply = control.sendCommand("RMD " + dir);

        // some servers return 257, technically incorrect but
        // we cater for it ...
        String[] validCodes = {"250", "257"};
        lastValidReply = control.validateReply(reply, validCodes);
    }


    /**
     * Create the specified remote working directory
     *
     * @param dir name of remote directory to
     *            create
     */
    public void mkdir(String dir) throws IOException, FTPException {
        String reply = control.sendCommand("MKD " + dir);
        lastValidReply = control.validateReply(reply, "257");
    }


    /**
     * Change the remote working directory to
     * that supplied
     *
     * @param dir name of remote directory to
     *            change to
     */
    public void chdir(String dir) throws IOException, FTPException {
        String reply = control.sendCommand("CWD " + dir);
        lastValidReply = control.validateReply(reply, "250");
    }


    /**
     * Change the remote working directory to
     * the enclosing folder
     */
    public void cdup() throws IOException, FTPException {
        String reply = control.sendCommand("CDUP");
        lastValidReply = control.validateReply(reply, "250");
    }


    /**
     *  Get modification time for a remote file
     *
     *  @param    remoteFile   name of remote file
     *  @return   modification time of file as a date
     */
//	public Date modtime(String remoteFile) throws IOException, FTPException {
//		String reply = control.sendCommand("MDTM " + remoteFile);
//		lastValidReply = control.validateReply(reply, "213");
//
//		// parse the reply string ...
//		Date ts = tsFormat.parse(lastValidReply.getReplyText(),
//		    new ParsePosition(0));
//		return ts;
//	}

    /**
     * Get the current remote working directory
     *
     * @return the current working directory
     */
    public String pwd() throws IOException, FTPException {
        String reply = control.sendCommand("PWD");
        lastValidReply = control.validateReply(reply, "257");

        // get the reply text and extract the dir
        // listed in quotes, if we can find it. Otherwise
        // just return the whole reply string
        String text = lastValidReply.getReplyText();
        int start = text.indexOf('"');
        int end = text.lastIndexOf('"');
        if (start >= 0 && end > start) {
            return text.substring(start + 1, end);
        }
        else {
            return text;
        }
    }

    /**
     * Get the type of the OS at the server
     *
     * @return the type of server OS
     */
    public String system() throws IOException, FTPException {
        String reply = control.sendCommand("SYST");
        String[] validCodes = {"215", "200"};
        //if (syst() == FTPReply.NAME_SYSTEM_TYPE)
        // Technically, we should expect a NAME_SYSTEM_TYPE response, but
        // in practice FTP servers deviate, so we soften the condition to
        // a positive completion.
//        if (__systemName == null && FTPReply.isPositiveCompletion(syst()))
//          __systemName = ((String)_replyLines.elementAt(0)).substring(4);
        lastValidReply = control.validateReply(reply, validCodes);
        return lastValidReply.getReplyText();
    }

    /**
     * Get the help text for the specified command
     *
     * @param command name of the command to get help on
     * @return help text from the server for the supplied command
     */
    public String help(String command) throws IOException, FTPException {
        String reply = control.sendCommand("HELP " + command);
        String[] validCodes = {"211", "214"};
        lastValidReply = control.validateReply(reply, validCodes);
        return lastValidReply.getReplyText();
    }

    /**
     * NOOP
     * 200 NOOP command successful.
     */
    public void noop() throws IOException, FTPException {
        String reply = control.sendCommand("NOOP");
        lastValidReply = control.validateReply(reply, "200");
    }

    /**
     * This command tells the server to abort the previous FTP
     * service command and any associated transfer of data.  The
     * abort command may require "special action", as discussed in
     * the Section on FTP Commands, to force recognition by the
     * server.  No action is to be taken if the previous command
     * has been completed (including data transfer).  The control
     * connection is not to be closed by the server, but the data
     * connection must be closed.
     * <p/>
     * There are two cases for the server upon receipt of this
     * command: (1) the FTP service command was already completed,
     * or (2) the FTP service command is still in progress.
     * <p/>
     * In the first case, the server closes the data connection
     * (if it is open) and responds with a 226 reply, indicating
     * that the abort command was successfully processed.
     * <p/>
     * In the second case, the server aborts the FTP service in
     * progress and closes the data connection, returning a 426
     * reply to indicate that the service request terminated
     * abnormally.  The server then sends a 226 reply,
     * indicating that the abort command was successfully
     * processed.
     */
    public void abor() throws IOException, FTPException {
        String reply = control.sendCommand("ABOR");
        String[] validCodes = {"225", "226", "426", "450", "451"};
        lastValidReply = control.validateReply(reply, validCodes);
        if (reply.substring(0, 3).equals("426") || reply.substring(0, 3).equals("450") || reply.substring(0, 3).equals("451")) {
            String[] c = {"225", "226"};
            lastValidReply = control.validateReply(control.readReply(), c);
        }
    }


    /**
     * This command shall cause a status response to be sent over
     * the control connection in the form of a reply.  The command
     * may be sent during a file transfer (along with the Telnet IP
     * and Synch signals--see the Section on FTP Commands) in which
     * case the server will respond with the status of the
     * operation in progress, or it may be sent between file
     * transfers.  In the latter case, the command may have an
     * argument field.  If the argument is a pathname, the command
     * is analogous to the "list" command except that data shall be
     * transferred over the control connection.  If a partial
     * pathname is given, the server may respond with a list of
     * file names or attributes associated with that specification.
     * If no argument is given, the server should return general
     * status information about the server FTP process.  This
     * should include current values of all transfer parameters and
     * the status of connections.
     */
    public String stat(String remoteFile) throws IOException, FTPException {
        String reply = control.sendCommand("STAT " + remoteFile);
        String[] validCodes = {"211", "212", "213"}; //450 File not available
        lastValidReply = control.validateReply(reply, validCodes);
        return lastValidReply.getReplyText();
    }

    /**
     * Quit the FTP session
     */
    public void quit() throws IOException, FTPException {
        if (this.isAlive()) {
            try {
                String reply = control.sendCommand("QUIT");
                String[] validCodes = {"221", "226"};
                lastValidReply = control.validateReply(reply, validCodes);
            }
            finally { // ensure we clean up the connection
                if (null != control) {
                    control.logout();
                }
                control = null;
            }
        }
    }

    /**
     * @return true if the control socket isn't null
     */
    public boolean isAlive() {
        if (null == control) {
            return false;
        }
        try {
            this.noop();
            return true;
        }
        catch (IOException e) {
            return false;
        }
    }
}