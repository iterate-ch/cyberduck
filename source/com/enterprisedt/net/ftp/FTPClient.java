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
 *
 *  Change Log:
 *
 *        $Log$
 *        Revision 1.4  2003/04/16 17:24:34  dkocher
 *        No log message.
 *
 *        Revision 1.3  2003/04/01 22:06:58  dkocher
 *        *** empty log message ***
 *
 *        Revision 1.2  2003/03/21 14:14:43  dkocher
 *        No log message.
 *
 *        Revision 1.1.1.1  2003/02/10 20:13:12  dkocher
 *        initial import
 *
 *        Revision 1.4  2002/11/19 22:01:25  bruceb
 *        changes for 1.2
 *
 *        Revision 1.3  2001/10/09 20:53:46  bruceb
 *        Active mode changes
 *
 *        Revision 1.1  2001/10/05 14:42:03  bruceb
 *        moved from old project
 *
 */

package com.enterprisedt.net.ftp;

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.FileReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.File;

import java.text.SimpleDateFormat;
import java.text.ParsePosition;

import java.net.InetAddress;
import java.net.Socket;

import java.util.Date;
import java.util.Vector;
import java.util.Properties;

import org.apache.log4j.Logger;


/**
 *  Supports client-side FTP. Most common
 *  FTP operations are present in this class.
 *
 *  @author      Bruce Blackshaw
 *  @version     $Revision$
 *
 */
public class FTPClient {

    private static Logger log = Logger.getLogger(FTPClient.class);

    /**
     *  Revision control id
     */
    private static String cvsId = "@(#)$Id$";

    /**
     *  Format to interpret MTDM timestamp
     */
    private SimpleDateFormat tsFormat = 
        new SimpleDateFormat("yyyyMMddHHmmss");

    /**
     *  Socket responsible for controlling
     *  the connection
     */
    private FTPControlSocket control = null;

    /**
     *  Socket responsible for transferring
     *  the data
     */
    private FTPDataSocket data = null;

    /**
     *  Socket timeout for both data and control. In
     *  milliseconds
     */
    private int timeout = 0;

    /**
     *  Record of the transfer type - make the default ASCII
     */
    private FTPTransferType transferType = FTPTransferType.ASCII;

    /**
     *  Record of the connect mode - make the default PASV (as this was
     *  the original mode supported)
     */
    private FTPConnectMode connectMode = FTPConnectMode.PASV;

    /**
     *  Holds the last valid reply from the server on the control socket
     */
    private FTPReply lastValidReply;

    public FTPClient() {
	super();
    }

    /**
     *  Creates the control
	*  socket
     *
     *  @param   remoteHost  the remote hostname
     *  @param   controlPort  port for control stream
     */
    public void connect(String remoteHost, int controlPort) throws IOException, FTPException {
	log.debug("connect");
        this.control = new FTPControlSocket(remoteHost, controlPort);
    }

    /**
     *   Set the TCP timeout on the underlying socket.
     *
     *   If a timeout is set, then any operation which
     *   takes longer than the timeout value will be
     *   killed with a java.io.InterruptedException. We
     *   set both the control and data connections
     *
     *   @param millis The length of the timeout, in milliseconds
     */
    public void setTimeout(int millis) throws IOException {
	log.debug("setTimeout");

        this.timeout = millis;
        control.setTimeout(millis);
    }


    /**
	* @return true if the control socket isn't null
     */
    public boolean isAlive() {
	if(null == control)
	    return false;
	try {
	    this.noop();
	    return true;
	}
	catch(IOException e) {
	    return false;
	}
    }
    
    /**
     *  Set the connect mode
     *
     *  @param  mode  ACTIVE or PASV mode
     */
    public void setConnectMode(FTPConnectMode mode) {
        connectMode = mode;
    }

    /**
     *  Login into an account on the FTP server. This
     *  call completes the entire login process
     *
     *  @param   user       user name
     *  @param   password   user's password
     */
    public void login(String user, String password)
        throws IOException, FTPException {

        String response = control.sendCommand("USER " + user);
        lastValidReply = control.validateReply(response, "331");
        response = control.sendCommand("PASS " + password);
        lastValidReply = control.validateReply(response, "230");
    }


    /**
     *  Supply the user name to log into an account
     *  on the FTP server. Must be followed by the
     *  password() method - but we allow for
     *
     *  @param   user       user name
     *  @param   password   user's password
     */
    public void user(String user)
        throws IOException, FTPException {

        String reply = control.sendCommand("USER " + user);

        // we allow for a site with no password - 230 response
        String[] validCodes = {"230", "331"};
        lastValidReply = control.validateReply(reply, validCodes);
    }


    /**
     *  Supplies the password for a previously supplied
     *  username to log into the FTP server. Must be
     *  preceeded by the user() method
     *
     *  @param   user       user name
     *  @param   password   user's password
     */
    public void password(String password)
        throws IOException, FTPException {

        String reply = control.sendCommand("PASS " + password);

        // we allow for a site with no passwords (202)
        String[] validCodes = {"230", "202"};
        lastValidReply = control.validateReply(reply, validCodes);
    }


    /**
	*  Validate that the put() or get() was successful
	*  @modified
	*/
    public void validateTransfer() 
        throws IOException, FTPException {

	    // check the control response
	    String[] validCodes = {"226", "250"};
	    String reply = control.readReply();
	    lastValidReply = control.validateReply(reply, validCodes);
	}

    /**
	*  Request the server to set up the put
	*
	*  @param  remoteFile  name of remote file in
	*                      current directory
	*  @param  append      true if appending, false otherwise
	*/
    private void initPut(String remoteFile, boolean append)
        throws IOException, FTPException {

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
	*  Request to the server that the get is set up
	*
	*  @param  remoteFile  name of remote file
	*  @modified
	*/
    private void initGet(String remoteFile, int resume)
        throws IOException, FTPException {

	    // set up data channel
	    data = control.createDataSocket(connectMode);
	    data.setTimeout(timeout);

	    // send the restart command
	    if(resume > 0) {
		String[] validReplyCodes = {"125", "350"};
		lastValidReply= control.validateReply(control.sendCommand("REST " + resume), validReplyCodes);
	    }

	    // send the retrieve command
	    String reply = control.sendCommand("RETR " + remoteFile);

	    // Can get a 125 or a 150
	    String[] validCodes1 = {"125", "150"};
	    lastValidReply = control.validateReply(reply, validCodes1);
	}

    
    /**
	*  Put as ASCII, i.e. read a line at a time and write
     *  inserting the correct FTP separator
     *
     *  @param localPath   full path of local file to read from
     *  @param remoteFile  name of remote file we are writing to
     *  @param  append      true if appending, false otherwise
     */
    public java.io.Writer putASCII(String remoteFile, boolean append) throws IOException, FTPException {
	        this.initPut(remoteFile, append);
	        return new OutputStreamWriter(data.getOutputStream());
    }
        /**
	*  Put as binary, i.e. read and write raw bytes
	 *
	 *  @param localPath   full path of local file to read from
	 *  @param remoteFile  name of remote file we are writing to
	 *  @param  append      true if appending, false otherwise
	 */
    public java.io.OutputStream putBinary(String remoteFile, boolean append) throws IOException, FTPException {
	        this.initPut(remoteFile, append);
	        return data.getOutputStream();
    }


    /**
	*  Get as ASCII, i.e. read a line at a time and write
     *  using the correct newline separator for the OS
     *
     *  @param localPath   full path of local file to write to
     *  @param remoteFile  name of remote file
     */
    public java.io.Reader getASCII(String remoteFile, int resume) throws IOException, FTPException {
	this.initGet(remoteFile, resume);
	return new InputStreamReader(data.getInputStream());
    }
         /**
	*  Get as binary file, i.e. straight transfer of data
	  *
	  *  @param localPath   full path of local file to write to
	  *  @param remoteFile  name of remote file
	  */
    public java.io.InputStream getBinary(String remoteFile, int resume) throws IOException, FTPException {
	this.initGet(remoteFile, resume);
	return data.getInputStream();
    }

    
    /**
	*  Set up SOCKS v4/v5 proxy settings. This can be used if there
	*  is a SOCKS proxy server in place that must be connected thru.
	*  Note that setting these properties directs <b>all</b> TCP  
	*  sockets in this JVM to the SOCKS proxy
     *
     *  @param  port  SOCKS proxy port
     *  @param  host  SOCKS proxy hostname
     */
    public static void initSOCKS(String port, String host) {
        Properties props = System.getProperties();
        props.put("socksProxyPort", port);
        props.put("socksProxyHost", host);
        System.setProperties(props);
    }

    /**
     *  Set up SOCKS username and password for SOCKS username/password 
     *  authentication. Often, no authentication will be required
     *  but the SOCKS server may be configured to request these.
     *
     *  @param  username   the SOCKS username 
     *  @param  password   the SOCKS password 
     */
    public static void initSOCKSAuthentication(String username, 
                                               String password) {
        Properties props = System.getProperties();
        props.put("java.net.socks.username", username);
        props.put("java.net.socks.password", password);
        System.setProperties(props);
    }

    /**
     *  Get the name of the remote host
     *
     *  @return  remote host name
     */
    String getRemoteHostName() {
        return control.getRemoteHostName();
    }


    /**
     *  Issue arbitrary ftp commands to the FTP server.
     *
     *  @param command     ftp command to be sent to server
     *  @param validCodes  valid return codes for this command
     */
    public void quote(String command, String[] validCodes)
        throws IOException, FTPException {

        String reply = control.sendCommand(command);

        // allow for no validation to be supplied
        if (validCodes != null && validCodes.length > 0)
            lastValidReply = control.validateReply(reply, validCodes);
    }


    /**
     *  Run a site-specific command on the
     *  server. Support for commands is dependent
     *  on the server
     *
     *  @param  command   the site command to run
     *  @return true if command ok, false if
     *          command not implemented
     */
    public boolean site(String command)
        throws IOException, FTPException {

        // send the retrieve command
        String reply = control.sendCommand("SITE " + command);

        // Can get a 200 (ok) or 202 (not impl). Some
        // FTP servers return 502 (not impl)
        String[] validCodes = {"200", "202", "502"};
        lastValidReply = control.validateReply(reply, validCodes);

        // return true or false? 200 is ok, 202/502 not
        // implemented
        if (reply.substring(0, 3).equals("200"))
            return true;
        else
            return false;
    }

    
    /**
     *  List current directory's contents as an array of strings of 
     *  filenames.
     *
     *  @return  an array of current directory listing strings
     */
    public String[] dir()
        throws IOException, FTPException {

        return dir(null, true);
    }

    /**
     *  List a directory's contents as an array of strings of filenames.
     *
     *  @param   dirname  name of directory(<b>not</b> a file mask)
     *  @return  an array of directory listing strings
     */
    public String[] dir(String dirname)
        throws IOException, FTPException {

        return dir(dirname, false);
    }


    /**
     *  List a directory's contents as an array of strings. A detailed
     *  listing is available, otherwise just filenames are provided.
     *  The detailed listing varies in details depending on OS and
     *  FTP server. Note that a full listing can be used on a file
     *  name to obtain information about a file
     *
     *  @param  dirname  name of directory (<b>not</b> a file mask)
     *  @param  full     true if detailed listing required
     *                   false otherwise
     *  @return  an array of directory listing strings
     */
    public String[] dir(String dirname, boolean full)
        throws IOException, FTPException {

        // set up data channel
        data = control.createDataSocket(connectMode);
        data.setTimeout(timeout);

        // send the retrieve command
        String command = full ? "LIST ":"NLST ";
        if (dirname != null)
            command += dirname;        

        // some FTP servers bomb out if NLST has whitespace appended
        command = command.trim();
        String reply = control.sendCommand(command);

        // Can get a 125 or a 150
        String[] validCodes1 = {"125", "150"};
        lastValidReply = control.validateReply(reply, validCodes1);

        // get an character input stream to read data from ... AFTER we
        // have the ok to go ahead
        LineNumberReader in =
            new LineNumberReader(
                new InputStreamReader(data.getInputStream()));

        // read a line at a time
        Vector lines = new Vector();
        String line = null;
        while ((line = in.readLine()) != null) {
            lines.add(line);
        }

        try {
            in.close();
            data.close();
        }
        catch (IOException ignore) {}       

        // check the control response - some servers seem to return
        // with 550 if no files found (although not a standard reply for
        // these commands in RFC 959)
        String[] validCodes2 = {"226", "250", "550"};
        reply = control.readReply();
        lastValidReply = control.validateReply(reply, validCodes2);

        // if we got a 550, return an empty array
        String[] result = new String[0];
        if (!lastValidReply.getReplyCode().equals("550"))       
            result = (String[])lines.toArray(new String[0]);

        return result;
    }

    /**
     *  Gets the latest valid reply from the server
     *
     *  @return  reply object encapsulating last valid server response
     */
    public FTPReply getLastValidReply() {
        return lastValidReply;
    }


    /**
     *  Switch debug of responses on or off
     *
     *  @param  on  true if you wish to have responses to
     *              stdout, false otherwise
     */
    /*
    public void debugResponses(boolean on) {
        control.debugResponses(on);
    }
     */

     /**
      *  Set the logging stream, replacing
      *  stdout
      *
      *  @param log  the new logging stream
      */
//     public void setLogStream(PrintWriter log) {
//         control.setLogStream(log);
//     }

    /**
     *  Get the current transfer type
     *
     *  @return  the current type of the transfer,
     *           i.e. BINARY or ASCII
     */
    public FTPTransferType getType() {
        return transferType;
    }

    /**
     *  Set the transfer type
     *
     *  @param  type  the transfer type to
     *                set the server to
     */
    public void setType(FTPTransferType type)
        throws IOException, FTPException {

        // determine the character to send
        String typeStr = FTPTransferType.ASCII_CHAR;
        if (type.equals(FTPTransferType.BINARY))
            typeStr = FTPTransferType.BINARY_CHAR;

        // send the command
        String reply = control.sendCommand("TYPE " + typeStr);
        lastValidReply = control.validateReply(reply, "200");

        // record the type
        transferType = type;
    }


    /**
     *  Delete the specified remote file
     *
     *  @param  remoteFile  name of remote file to
     *                      delete
     */
    public void delete(String remoteFile)
        throws IOException, FTPException {

        String reply = control.sendCommand("DELE " + remoteFile);
        lastValidReply = control.validateReply(reply, "250");
    }


    /**
     *  Rename a file or directory
     *
     * @param from  name of file or directory to rename
     * @param to    intended name
     */
    public void rename(String from, String to)
        throws IOException, FTPException {

        String reply = control.sendCommand("RNFR " + from);
        lastValidReply = control.validateReply(reply, "350");

        reply = control.sendCommand("RNTO " + to);
        lastValidReply = control.validateReply(reply, "250");
    }


    /**
     *  Delete the specified remote working directory
     *
     *  @param  dir  name of remote directory to
     *               delete
     */
    public void rmdir(String dir)
        throws IOException, FTPException {

        String reply = control.sendCommand("RMD " + dir);

        // some servers return 257, technically incorrect but
        // we cater for it ...
        String[] validCodes = {"250", "257"};
        lastValidReply = control.validateReply(reply, validCodes);
    }


    /**
     *  Create the specified remote working directory
     *
     *  @param  dir  name of remote directory to
     *               create
     */
    public void mkdir(String dir)
        throws IOException, FTPException {

        String reply = control.sendCommand("MKD " + dir);
        lastValidReply = control.validateReply(reply, "257");
    }

    public void cdup()
	throws IOException, FTPException {

	String reply = control.sendCommand("CDUP");
	lastValidReply = control.validateReply(reply, "250");
    }
    
    /**
     *  Change the remote working directory to
     *  that supplied
     *
     *  @param  dir  name of remote directory to
     *               change to
     */
    public void chdir(String dir)
        throws IOException, FTPException {

        String reply = control.sendCommand("CWD " + dir);
        lastValidReply = control.validateReply(reply, "250");
    }

    /**
     *  Get modification time for a remote file
     *
     *  @param    remoteFile   name of remote file 
     *  @return   modification time of file as a date
     */
    public Date modtime(String remoteFile)
        throws IOException, FTPException {

        String reply = control.sendCommand("MDTM " + remoteFile);
        lastValidReply = control.validateReply(reply, "213");
        
        // parse the reply string ...
        Date ts = tsFormat.parse(lastValidReply.getReplyText(), 
                                 new ParsePosition(0));
        return ts;
    }

    /**
     *  Get the current remote working directory
     *
     *  @return   the current working directory
     */
    public String pwd()
        throws IOException, FTPException {

        String reply = control.sendCommand("PWD");
        lastValidReply = control.validateReply(reply, "257");
        
        // get the reply text and extract the dir 
        // listed in quotes, if we can find it. Otherwise
        // just return the whole reply string
        String text = lastValidReply.getReplyText();
        int start = text.indexOf('"')+1;
        int end = text.lastIndexOf('"');                
        if (start >= 0 && end > start)
            return text.substring(start, end);
        else
            return text;        
    }

    /**
     *  Get the type of the OS at the server
     *
     *  @return   the type of server OS
     */
    public String system()
        throws IOException, FTPException {

        String reply = control.sendCommand("SYST");
        lastValidReply = control.validateReply(reply, "215");
        return lastValidReply.getReplyText();
    }

    /**
	* Wrapper for the command <code>size [fileName]</code>.  If the file does
     * not exist, we return -1;
     */
    public long size(String fileName) throws IOException, FTPException {
	        String reply = control.sendCommand("SIZE " + fileName);
        //control.validateReply(reply, "213");
        try {
            return Long.parseLong(reply.substring(4));
        }
        catch (Exception e) {
            return -1L;
        }
    }
        /**
	* NOOP
	 * 200 NOOP command successful.
	 */
    public void noop() throws IOException, FTPException {
        String reply = control.sendCommand("NOOP");
        control.validateReply(reply, "200");
    }
    
    /**
	*  Get the help text for the specified command
     *
     *  @param  command  name of the command to get help on
     *  @return help text from the server for the supplied command
     */
    public String help(String command)
        throws IOException, FTPException {

        String reply = control.sendCommand("HELP " + command);
        String[] validCodes = {"211", "214"};
        lastValidReply = control.validateReply(reply, validCodes);
        return lastValidReply.getReplyText();
    }

    /**
     *  Quit the FTP session
     *
     */
    public void quit() throws IOException, FTPException {

        try {
            String reply = control.sendCommand("QUIT");
            String[] validCodes = {"221", "226"};
            lastValidReply = control.validateReply(reply, validCodes);
        }
        finally { // ensure we clean up the connection
            control.logout();
            control = null;
        }
    }

}



