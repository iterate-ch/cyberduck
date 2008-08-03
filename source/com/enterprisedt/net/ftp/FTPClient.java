/**
 *
 *  edtFTPj
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
 *
 */

package com.enterprisedt.net.ftp;

import ch.cyberduck.core.Preferences;

import org.apache.log4j.Logger;

import java.io.*;
import java.net.InetAddress;
import java.nio.charset.Charset;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Supports client-side FTP. Most common
 * FTP operations are present in this class.
 *
 * @author Bruce Blackshaw
 * @version $Revision$
 */
public class FTPClient {
    protected static Logger log = Logger.getLogger(FTPClient.class);

    /**
     * SOCKS port property name
     */
    final private static String SOCKS_PORT = "socksProxyPort";

    /**
     * SOCKS host property name
     */
    final private static String SOCKS_HOST = "socksProxyHost";

    /**
     * Socket responsible for controlling
     * the connection
     */
    protected FTPControlSocket control = null;

    /**
     * Socket responsible for transferring
     * the data
     */
    protected FTPDataSocket data = null;

    /**
     * Socket timeout for both data and control. In
     * milliseconds
     */
    private int timeout = 0;

    /**
     * Use strict return codes if true
     */
    private boolean strictReturnCodes = true;

    /**
     * Can be used to cancel a transfer
     */
    private boolean cancelTransfer = false;

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
    protected FTPReply lastValidReply;

    private FTPMessageListener listener;

    /**
     *
     * @param encoding
     * @param listener
     */
    public FTPClient(final String encoding, final FTPMessageListener listener) {
        this.listener = listener;
        this.control = new FTPControlSocket(encoding, listener);
    }

    private String[] features;

    /**
     *
     * @param remoteHost  the remote hostname
     * @param controlPort port for control stream (use -1 for the default port)
     * @throws IOException
     * @throws FTPException
     */
    public void connect(final String remoteHost, int controlPort)
            throws IOException, FTPException {
        this.control.connect(InetAddress.getByName(remoteHost), controlPort);
    }

    /**
     * @return true if the control socket isn't null
     */
    public boolean isConnected() {
        if(null == this.control) {
            return false;
        }
        return this.control.isConnected();
    }

    /**
     *
     * @throws IOException
     */
    public void interrupt() throws IOException {
        if(null == this.control) {
            log.warn("No control channel to interrupt");
            return;
        }
        control.interrupt();
        if(null == this.data) {
            log.info("No data channel to interrupt");
            return;
        }
        data.close();
    }

    /**
     * Set strict checking of FTP return codes. If strict
     * checking is on (the default) code must exactly match the expected
     * code. If strict checking is off, only the first digit must match.
     *
     * @param strict true for strict checking, false for loose checking
     */
    public void setStrictReturnCodes(boolean strict) {
        this.strictReturnCodes = strict;
        if(control != null)
            control.setStrictReturnCodes(strict);
    }

    /**
     * Determine if strict checking of return codes is switched on. If it is
     * (the default), all return codes must exactly match the expected code.
     * If strict checking is off, only the first digit must match.
     *
     * @return true if strict return code checking, false if non-strict.
     */
    public boolean isStrictReturnCodes() {
        return strictReturnCodes;
    }

    /**
     * Set the TCP timeout on the underlying socket.
     * If a timeout is set, then any operation which
     * takes longer than the timeout value will be
     * killed with a java.io.InterruptedException. We
     * set both the control and data connections
     *
     * @param millis The length of the timeout, in milliseconds
     */
    public void setTimeout(int millis) throws IOException {
        this.timeout = millis;
        this.control.setTimeout(millis);
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
     * Cancels the current transfer. Generally called from a separate
     * thread. Note that this may leave partially written files on the
     * server or on local disk, and should not be used unless absolutely
     * necessary. The server is not notified
     */
    public void cancelTransfer() {
        cancelTransfer = true;
    }

    public void noop() throws IOException, FTPException {
        FTPReply reply = control.sendCommand("NOOP");
        lastValidReply = control.validateReply(reply, "200");
    }

    /**
     * Login into an account on the FTP server. This
     * call completes the entire login process
     *
     * @param user     user name
     * @param password user's password
     */
    public void login(String user, String password) throws IOException, FTPException {
        FTPReply reply = control.sendCommand("USER "+user);
        // we allow for a site with no password - 230 response
        String[] validCodes = {"230", "331"};
        lastValidReply = control.validateReply(reply, validCodes);
        if(lastValidReply.getReplyCode().equals("230"))
            return;
        else {
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
    public void user(String user) throws IOException, FTPException {
        FTPReply reply = control.sendCommand("USER "+user);
        // we allow for a site with no password - 230 response
        String[] validCodes = {"230", "331"};
        lastValidReply = control.validateReply(reply, validCodes);
    }


    /**
     * Supplies the password for a previously supplied
     * username to log into the FTP server. Must be
     * preceeded by the user() method
     *
     * @param password The password.
     */
    public void password(String password) throws IOException, FTPException {
        FTPReply reply = control.sendCommand("PASS "+password);
        // we allow for a site with no passwords (202)
        String[] validCodes = {"230", "202"};
        lastValidReply = control.validateReply(reply, validCodes);
    }

    public void reinitialize() throws IOException, FTPException {
        FTPReply reply = control.sendCommand("REIN");
        String[] validCodes = {"120", "220"};
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
    public static void initSOCKS(int port, String host) {
        Properties props = System.getProperties();
        props.put(SOCKS_PORT, ""+port);
        props.put(SOCKS_HOST, host);
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
    public static void initSOCKSAuthentication(String username,
                                               String password) {
        Properties props = System.getProperties();
        props.put("java.net.socks.username", username);
        props.put("java.net.socks.password", password);
        System.setProperties(props);
    }

    /**
     * Clear SOCKS settings. Note that setting these properties affects
     * <b>all</b> TCP sockets in this JVM
     */
    public static void clearSOCKS() {
        Properties prop = System.getProperties();
        prop.remove(SOCKS_HOST);
        prop.remove(SOCKS_PORT);
        System.setProperties(prop);
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
     * @return the text returned by the FTP server
     */
    public String quote(String command) throws IOException, FTPException {
        FTPReply reply = control.sendCommand(command);
        return reply.getReplyText();
    }


    /**
     * Get the size of a remote file. This is not a standard FTP command, it
     * is defined in "Extensions to FTP", a draft RFC
     * (draft-ietf-ftpext-mlst-16.txt)
     *
     * @param remoteFile name or path of remote file in current directory
     * @return size of file in bytes
     */
    public long size(String remoteFile) throws IOException, FTPException {
        FTPReply reply = control.sendCommand("SIZE "+remoteFile);
        lastValidReply = control.validateReply(reply, "213");

        // parse the reply string .
        String replyText = lastValidReply.getReplyText();

        // trim off any trailing characters after a space, e.g. webstar
        // responds to SIZE with 213 55564 bytes
        int spacePos = replyText.indexOf(' ');
        if(spacePos >= 0)
            replyText = replyText.substring(0, spacePos);

        // parse the reply
        try {
            return Long.parseLong(replyText);
        }
        catch(NumberFormatException ex) {
            throw new FTPException("Failed to parse reply: "+replyText);
        }
    }

    /**
     * Issue the RESTart command to the remote server
     *
     * @param size the REST param, the mark at which the restart is
     *             performed on the remote file. For STOR, this is retrieved
     *             by SIZE
     * @throws IOException
     * @throws FTPException
     */
    private void restart(long size) throws IOException, FTPException {
        String[] validReplyCodes = {"125", "350"};
        FTPReply reply = control.sendCommand("REST "+size);
        lastValidReply = control.validateReply(reply, validReplyCodes);
    }

    /**
     * Validate that the put() or get() was successful.  This method is not
     * for general use.
     */
    public void validateTransfer() throws IOException, FTPException {
        // check the control response
        String[] validCodes = {"225", "226", "250", "426", "450"};
        FTPReply reply = control.readReply();

        // permit 426/450 error if we cancelled the transfer, otherwise
        // throw an exception
        String code = reply.getReplyCode();
        if((code.equals("426") || code.equals("450")) && !cancelTransfer)
            throw new FTPException(reply);

        lastValidReply = control.validateReply(reply, validCodes);
    }

    /**
     * Close the data socket
     */
    private void closeDataSocket() {
        if(data != null) {
            try {
                data.close();
                data = null;
            }
            catch(IOException ex) {
                //log.warn("Caught exception closing data socket");
            }
        }
    }

    /**
     * Request the server to set up the put
     *
     * @param remoteFile name of remote file in
     *                   current directory
     * @param append     true if appending, false otherwise
     */
    private void initPut(String remoteFile, boolean append) throws IOException, FTPException {
        final String cmd = (append ? "APPE " : "STOR ") + remoteFile;

        this.pret(cmd);

        // set up data channel
        data = control.createDataSocket(connectMode);
        data.setTimeout(timeout);

        // send the command to store
        FTPReply reply = control.sendCommand(cmd);

        // Can get a 125 or a 150
        String[] validCodes = {"125", "150"};
        lastValidReply = control.validateReply(reply, validCodes);
    }

    /**
     * Request to the server that the get is set up
     *
     * @param remoteFile name of remote file
     */
    private void initGet(String remoteFile, long resume) throws IOException, FTPException {
        final String cmd = "RETR "+remoteFile;

        this.pret(cmd);

        // set up data channel
        data = control.createDataSocket(connectMode);
        data.setTimeout(timeout);

        // send the restart command
        if(resume > 0) {
            this.restart(resume);
        }

        // send the retrieve command
        FTPReply reply = control.sendCommand(cmd);

        // Can get a 125 or a 150
        String[] validCodes1 = {"125", "150"};
        lastValidReply = control.validateReply(reply, validCodes1);
    }

    /**
     * Put as binary, i.e. read and write raw bytes
     *
     * @param remoteFile name of remote file we are writing to
     * @param append     true if appending, false otherwise
     */
    public OutputStream put(String remoteFile, boolean append) throws IOException, FTPException {
        this.initPut(remoteFile, append);
        return data.getOutputStream();
    }

    /**
     * Get as binary file, i.e. straight transfer of data
     *
     * @param remoteFile name of remote file
     */
    public InputStream get(String remoteFile, long resume) throws IOException, FTPException {
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
        FTPReply reply = control.sendCommand("SITE "+command);

        // Can get a 200 (ok) or 202 (not impl). Some
        // FTP servers return 502 (not impl)
        String[] validCodes = {"200", "202", "250", "502", "253"};
        lastValidReply = control.validateReply(reply, validCodes);

        // return true or false? 200 is ok, 202/502 not
        // implemented
        return reply.getReplyCode().equals("200");
    }

    private boolean statListSupportedEnabled
            = Preferences.instance().getBoolean("ftp.sendStatListCommand");

    /**
     * Issue the FTP STAT command to the server for a given pathname.  This
     * should produce a listing of the file or directory.
     *
     * Popular FTP servers already support "STAT -l" command to
     * transferring dir list via control channel.
     *
     * @throws IOException
     * @throws FTPException
     * @return Null if there is no result returned from the command and the feature
     * may be not supported
     */
    public BufferedReader stat(String pathname) throws IOException, FTPException {
        if(statListSupportedEnabled) {
            try {
                FTPReply reply = control.sendCommand("STAT " + pathname);
                lastValidReply = control.validateReply(reply, new String[]{"211", "212", "213"});
                if(null == lastValidReply.getReplyData() || 0 == lastValidReply.getReplyData().length) {
                    statListSupportedEnabled = false;
                    return null;
                }
                final StringBuilder result = new StringBuilder();
                for(int i = 0; i < lastValidReply.getReplyData().length; i++) {
                    //Some servers include the status code for every line.
                    final String line = lastValidReply.getReplyData()[i];
                    if(line.startsWith(lastValidReply.getReplyCode())) {
                        try {
                            result.append(line.substring(line.indexOf(lastValidReply.getReplyCode())
                                    + lastValidReply.getReplyCode().length() + 1).trim()).append('\n');
                        }
                        catch(IndexOutOfBoundsException e) {
                            log.error("Failed parsing line '" + line + "':" + e.getMessage());
                            continue;
                        }
                    }
                    else {
                        result.append(line.trim()).append('\n');
                    }
                }
                return new BufferedReader(new StringReader(result.toString()));
            }
            catch(FTPException e) {
                statListSupportedEnabled = false;
                // STAT may not be supported for directory listings. Try standard LIST command instead
                log.error(e.getMessage());
            }
        }
        return null;
    }

    /**
     * The server supports LIST -a
     */
    private boolean extendedListEnabled
            = Preferences.instance().getBoolean("ftp.sendExtendedListCommand");

    /**
     *
     * @param encoding
     * @param flag Try with -a flag first
     * @return May return null for empty directory listing
     * @throws IOException
     * @throws FTPException
     */
    public BufferedReader list(String encoding, boolean flag) throws IOException, FTPException {
        if(extendedListEnabled && flag) {
            try {
                return this.dir(encoding, "LIST -a");
            }
            catch(FTPException e) {
                extendedListEnabled = false;
                // Option -a may not be recognized. Try standard list command instead
                log.error(e.getMessage());
            }
        }
        // Option -a may not be recognized. Try standard list command instead
        return this.dir(encoding, "LIST");
    }

    /**
     * List a directory's contents as an array of strings. A detailed
     * listing is available, otherwise just filenames are provided.
     * The detailed listing varies in details depending on OS and
     * FTP server. Note that a full listing can be used on a file
     * name to obtain information about a file
     *
     * @param command the list command to use. E.g. LIST, LIST -a or NLST
     * @return May return null for empty directory listing
     */
    public BufferedReader dir(String encoding, String command) throws IOException, FTPException {
        this.pret(command);

        // set up data channel
        data = control.createDataSocket(connectMode);
        data.setTimeout(timeout);

        FTPReply reply = control.sendCommand(command);

        // check the control response. wu-ftp returns 550 if the
        // directory is empty, so we handle 550 appropriately. Similarly
        // proFTPD returns 450
        lastValidReply = control.validateReply(reply, new String[]{"125", "150", "450", "550"});

        // a normal reply ... extract the file list
        String replyCode = lastValidReply.getReplyCode();
        if(!replyCode.equals("450") && !replyCode.equals("550")) {
            // get a character input stream to read data from .
            return new BufferedReader(new InputStreamReader(data.getInputStream(),
                    Charset.forName(encoding)
            )) {
                public String readLine() throws IOException {
                    String line = super.readLine();
                    if(null != line) {
                        listener.logReply(line);
                    }
                    return line;
                }
            };
        }
        // 450 or 550 - still need to close data socket
        this.closeDataSocket();
        return null;
    }

    /**
     *
     * @throws IOException
     * @throws FTPException
     */
    public void finishDir() throws IOException, FTPException {
        this.closeDataSocket();

        // check the control response
        lastValidReply = control.validateReply(control.readReply(), new String[]{"226", "250"});
    }

    /**
     * Attempts to read a single line from the given <code>InputStream</code>.
     * The purpose of this method is to permit subclasses to execute
     * any additional code necessary when performing this operation.
     *
     * @param in The <code>LineNumberReader</code> to read from.
     * @return The string read.
     * @throws IOException Thrown if there was an error while reading.
     */
    protected String readLine(LineNumberReader in) throws IOException {
        return in.readLine();
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
        if(!type.equals(this.transferType)) {
            // determine the character to send
            String typeStr = FTPTransferType.ASCII_CHAR;
            if(type.equals(FTPTransferType.BINARY))
                typeStr = FTPTransferType.BINARY_CHAR;

            // send the command
            FTPReply reply = control.sendCommand("TYPE "+typeStr);
            lastValidReply = control.validateReply(reply, "200");
        }
        // record the type
        this.transferType = type;
    }


    /**
     * Delete the specified remote file
     *
     * @param remoteFile name of remote file to
     *                   delete
     */
    public void delete(String remoteFile) throws IOException, FTPException {
        FTPReply reply = control.sendCommand("DELE "+remoteFile);
        lastValidReply = control.validateReply(reply, new String[]{"200", "250"});
    }


    /**
     * Rename a file or directory
     *
     * @param from name of file or directory to rename
     * @param to   intended name
     */
    public void rename(String from, String to) throws IOException, FTPException {
        FTPReply reply = control.sendCommand("RNFR "+from);
        lastValidReply = control.validateReply(reply, "350");

        reply = control.sendCommand("RNTO "+to);
        lastValidReply = control.validateReply(reply, new String[]{"200", "250"});
    }


    /**
     * Delete the specified remote working directory
     *
     * @param dir name of remote directory to
     *            delete
     */
    public void rmdir(String dir) throws IOException, FTPException {
        FTPReply reply = control.sendCommand("RMD "+dir);
        // some servers return 200,257, technically incorrect but
        // we cater for it ...
        lastValidReply = control.validateReply(reply, new String[]{"200", "250", "257"});
    }


    /**
     * Create the specified remote working directory
     *
     * @param dir name of remote directory to
     *            create
     */
    public void mkdir(String dir) throws IOException, FTPException {
        FTPReply reply = control.sendCommand("MKD "+dir);
        // some servers return 200,257, technically incorrect but
        // we cater for it ...
        lastValidReply = control.validateReply(reply, new String[]{"200", "250", "257"});
    }


    /**
     * Change the remote working directory to
     * that supplied
     *
     * @param dir name of remote directory to
     *            change to
     */
    public void chdir(String dir) throws IOException, FTPException {
        FTPReply reply = control.sendCommand("CWD "+dir);
        lastValidReply = control.validateReply(reply, new String[]{"200", "250", "257"});
    }

    /**
     * Change the remote working directory to
     * the enclosing folder
     */
    public void cdup() throws IOException, FTPException {
        FTPReply reply = control.sendCommand("CDUP");
        lastValidReply = control.validateReply(reply, new String[]{"200", "250", "257"});
    }

    /**
     * Format to interpret MTDM timestamp
     */
    private SimpleDateFormat tsFormat =
        new SimpleDateFormat("yyyyMMddHHmmss");

    private boolean getModtimeSupported = true;

    /**
     * Get modification time for a remote file
     *
     * @param remoteFile name of remote file
     * @return modification time of file in milliseconds
     */
    public long modtime(String remoteFile, TimeZone timezone) throws IOException, FTPException {
        if(getModtimeSupported) {
            try {
                FTPReply reply = control.sendCommand("MDTM "+remoteFile);
                lastValidReply = control.validateReply(reply, "213");

                tsFormat.setTimeZone(timezone);
                // parse the reply string ...
                return tsFormat.parse(lastValidReply.getReplyText(),
                        new ParsePosition(0)).getTime();
            }
            catch(FTPException e) {
                this.getModtimeSupported = false;
                throw e;
            }
        }
        throw new FTPException("MDTM not supported");
    }

    private boolean setUtimeSupported = true;

    /**
     * Change modification time for a remote file
     * @param modtime Milliseconds since (00:00:00 GMT, January 1, 1970)
     * @param remoteFile name of remote file
     */
    public void utime(long modtime, long createdtime, String remoteFile, TimeZone timezone)
            throws IOException, FTPException {

        if(this.setUtimeSupported) {
            try {
                // The utime() function sets the access and modification times of the named
                // file from the structures in the argument array timep.
                // The access time is set to the value of the first element,
                // and the modification time is set to the value of the second element
                // Accessed date, modified date, created date
                int offset = timezone.getRawOffset();
                this.site("UTIME "+remoteFile+" "+tsFormat.format(new Date(modtime-offset))
                        +" "+tsFormat.format(new Date(modtime-offset))
                        +" "+tsFormat.format(new Date(createdtime-offset))
                        +" UTC");
            }
            catch(FTPException e) {
                this.setUtimeSupported = false;
                throw e;
            }
        }
        if(!this.setUtimeSupported) {
            throw new FTPException("UTIME not supported");
        }
    }

    private boolean setModtimeSupported = true;

    public void setmodtime(long modtime, String remoteFile, TimeZone timezone) throws IOException, FTPException {
        tsFormat.setTimeZone(timezone);
        if(this.setModtimeSupported) {
            try {
                FTPReply reply = control.sendCommand("MDTM "+tsFormat.format(new Date(modtime))+" "+remoteFile);
                lastValidReply = control.validateReply(reply, "213");
            }
            catch(FTPException e) {
                this.setModtimeSupported = false;
                throw e;
            }
        }
        if(!this.setModtimeSupported) {
            throw new FTPException("MDTM not supported");
        }
    }

    private boolean setChmodSupported = true;

    /**
     * Change the unix permissions of a remote file
     * @param octal
     * @param remoteFile
     */
    public void setPermissions(String octal, String remoteFile) throws IOException, FTPException {
        if(this.setChmodSupported) {
            try {
                this.site("CHMOD "+octal+" "+remoteFile);
            }
            catch(FTPException e) {
                this.setChmodSupported = false;
                throw e;
            }
        }
        if(!this.setChmodSupported) {
            throw new FTPException("CHMOD not supported");
        }
    }

    /**
     * Get the current remote working directory
     *
     * @return the current working directory
     */
    public String pwd() throws IOException, FTPException {
        FTPReply reply = control.sendCommand("PWD");
        lastValidReply = control.validateReply(reply, "257");

        // get the reply text and extract the dir
        // listed in quotes, if we can find it. Otherwise
        // just return the whole reply string
        String text = lastValidReply.getReplyText();
        int start = text.indexOf('"');
        int end = text.indexOf('"', start+1);
        if(start >= 0 && end > start)
            return text.substring(start+1, end);
        else
            return text;
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
        FTPReply reply = control.sendCommand("ABOR");
        lastValidReply = control.validateReply(reply, new String[]{"225", "226", "426", "450", "451"});
        String replyCode = lastValidReply.getReplyCode();
        if(replyCode.equals("426")
            || replyCode.equals("450")
            || replyCode.equals("451")) {
            String[] c = {"225", "226"};
            lastValidReply = control.validateReply(control.readReply(), c);
        }
    }

    private boolean featSupported = true;

    /**
     * Get the server supplied features
     *
     * @return string containing server features, or null if no features or not
     *         supported
     */
    public String[] features() throws IOException, FTPException {
        if(featSupported) {
            try {
                if(null == features) {
                    FTPReply reply = control.sendCommand("FEAT");
                    lastValidReply = control.validateReply(reply, new String[]{"211", "500", "502"});
                    if(lastValidReply.getReplyCode().equals("211")) {
                        features = lastValidReply.getReplyData();
                    }
                    else {
                        throw new FTPException(reply);
                    }
                }
            }
            catch(FTPException e) {
                featSupported = false;
                throw e;
            }
        }
        if(null == features) {
            return new String[]{};
        }
        return features;
    }

    /**
     * Get the type of the OS at the server
     *
     * @return the type of server OS
     */
    public String system() throws IOException, FTPException {
        FTPReply reply = control.sendCommand("SYST");
        lastValidReply = control.validateReply(reply, new String[]{"200", "213", "215"});
        return lastValidReply.getReplyText();
    }

    protected boolean isMDTMSupported() {
        try {
            for(Iterator iter = Arrays.asList(this.features()).iterator(); iter.hasNext();) {
                if("MDTM".equals(((String) iter.next()).trim())) {
                    return true;
                }
            }
            return false;
        }
        catch(IOException e) {
            return false;
        }
    }

    protected boolean isMDTMSetSupported() {
        try {
            for(Iterator iter = Arrays.asList(this.features()).iterator(); iter.hasNext();) {
                if("MDTM yyyyMMddHHmmss".equals(((String) iter.next()).trim())) {
                    return true;
                }
            }
            return false;
        }
        catch(IOException e) {
            return false;
        }
    }

    protected boolean isUTIMESupported() {
        try {
            for(Iterator iter = Arrays.asList(this.features()).iterator(); iter.hasNext();) {
                if("SITE UTIME".equals(((String) iter.next()).trim())) {
                    return true;
                }
            }
            return false;
        }
        catch(IOException e) {
            return false;
        }
    }

    protected boolean isPRETSupported() {
        try {
            for(Iterator iter = Arrays.asList(this.features()).iterator(); iter.hasNext();) {
                if("PRET".equals(((String) iter.next()).trim())) {
                    return true;
                }
            }
            return false;
        }
        catch(IOException e) {
            return false;
        }
    }

    /**
     * http://drftpd.org/index.php/PRET_Specifications
     * @param cmd
     * @throws IOException
     */
    private void pret(String cmd) throws IOException {
        try {
            if(this.isPRETSupported()) {
                // PRET support
                control.sendCommand("PRET "+cmd);
            }
        }
        catch(FTPException e) {
            log.warn("PRET (PRE Transfer) command not supported:"+e.getMessage());
        }
    }

    /**
     * Quit the FTP session
     */
    public void quit() throws IOException, FTPException {
        try {
            FTPReply reply = control.sendCommand("QUIT");
            lastValidReply = control.validateReply(reply, new String[]{"221", "226"});
        }
        finally { // ensure we clean up the connection
            control.logout();
            control = null;
        }
    }
}
