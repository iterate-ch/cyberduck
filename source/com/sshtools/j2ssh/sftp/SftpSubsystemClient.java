/*
 * Sshtools - Java SSH2 API
 *
 * Copyright (C) 2002 Lee David Painter.
 *
 * Written by: 2002 Lee David Painter <lee@sshtools.com>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public License
 * as published by the Free Software Foundation; either version 2 of
 * the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package com.sshtools.j2ssh.sftp;

import com.sshtools.j2ssh.io.UnsignedInteger32;
import com.sshtools.j2ssh.io.UnsignedInteger64;
import com.sshtools.j2ssh.subsystem.SubsystemClient;
import com.sshtools.j2ssh.subsystem.SubsystemMessage;
import com.sshtools.j2ssh.transport.ServiceOperationException;

import org.apache.log4j.Logger;

import java.io.IOException;

import java.util.List;
import java.util.Vector;


/**
 * This class implements the client side of the 'sftp' subsystem as described
 * in draft-ietf-secsh-filexfer-02.txt. This document specifies the SFTP
 * protocol version 3. To invoke this subsystem first connect to the ssh
 * server using  the <code>SshClient</code> class and authenticate the user.
 * Once the authentication has succeeded open a session channel using:<br>
 * <br>
 * <code>SessionChannelClient session = ssh.openSessionChannel();</code><br>
 * <br>
 * Once the session is created you can create an instance of this class and
 * start the subssytem using the <code>SessionChannelClient</code>
 * startSubsystem method:<br>
 * <br>
 * <tt>SftpClient sftp = new SftpClient();</tt><br>
 * <br>
 * <tt>session.startSubsystem(sftp);</tt><br>
 * <br>
 * TODO:<br>
 * Support for versions 1,2 & 4<br>
 *
 * @author <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 * @version $Id$
 */
public class SftpSubsystemClient extends SubsystemClient {
    /**
    * Flag to open a file for reading using <code>openFile</code>
    */
    public static final int OPEN_READ = SshFxpOpen.FXF_READ;

    /**
    * Flag to open a file for writing using <code>openFile</code>
    */
    public static final int OPEN_WRITE = SshFxpOpen.FXF_WRITE;

    /**
    * Flag to open a file for appending using <code>openFile</code>
    */
    public static final int OPEN_APPEND = SshFxpOpen.FXF_APPEND;

    /**
    * Flag to create a file using <code>openFile</code>
    */
    public static final int OPEN_CREATE = SshFxpOpen.FXF_CREAT;

    /**
    * Flag to truncate the file to zero length <code>openFile</code>
    */
    public static final int OPEN_TRUNCATE = SshFxpOpen.FXF_TRUNC;

    /**
    * Flag to exclusivly open a file using <code>openFile</code>
    */
    public static final int OPEN_EXCLUSIVE = SshFxpOpen.FXF_EXCL;

    /**
    * SFTP protocol version 1
    */
    public static final int VERSION_1 = 1;

    /**
    * SFTP protocol version 2
    */
    public static final int VERSION_2 = 2;

    /**
    * SFTP protocol version 3
    */
    public static final int VERSION_3 = 3;

    /**
    * SFTP protocol version 4
    */
    public static final int VERSION_4 = 4;

    /* Private variables */
    private static Logger log = Logger.getLogger(SftpSubsystemClient.class);
    private List handles = new Vector();
    private UnsignedInteger32 nextRequestId = new UnsignedInteger32(1);
    private int version = VERSION_3;
    private SftpMessageStore messageStore;
    private String workingDirectory = "";

    /**
     * Contructs the object
     */
    public SftpSubsystemClient() {
        // We will use our own message store implementation
        super("sftp", new SftpMessageStore());
        messageStore = (SftpMessageStore) super.messageStore;
        registerMessages();
    }

    /**
     * Gets the name of this subsystem
     *
     * @return 'sftp'
     */
    public String getName() {
        return "sftp";
    }

    /**
     * Closes an open file or directory handle
     *
     * @param handle the open file handle
     *
     * @throws IOException if the handle is invalid
     */
    protected void closeHandle(byte[] handle) throws IOException {
        if (!isValidHandle(handle)) {
            throw new IOException("The handle is invalid!");
        }

        // We will remove the handle first so that even if an excpetion occurs
        // the file as far as were concerned is closed
        handles.remove(handle);

        UnsignedInteger32 requestId = nextRequestId();
        SshFxpClose msg = new SshFxpClose(requestId, handle);
        sendMessage(msg);
        getOKRequestStatus(requestId);
    }

    /**
     * Closes a previously opened SftpFile
     * @param file    the file instance to close
     * @throws IOException  if the handle is invalid or the server cannot close the file
     */
    public void closeFile(SftpFile file) throws IOException {
        closeHandle(file.getHandle());
    }

    /**
     * Determine if a handle is valid
     * @param handle  the open file handle
     * @return  <tt>true</tt> if the handle is valid otherwise <tt>false</tt>
     */
    protected boolean isValidHandle(byte[] handle) {
        return handles.contains(handle);
    }

    /**
     * Lists the children of a directory.
     *
     * @param handle an open directory handle obtained through <code>openDirectory</code>
     * @param children a java.util.List to receive the additional files
     *
     * @return the number of files added to the list, if no more files are available the
     * method returns -1
     *
     * @throws IOException if the communication with the server fails
     */
    protected int listChildren(byte[] handle, List children)
        throws IOException {
        if (!isValidHandle(handle)) {
            throw new IOException("The handle is invalid!");
        }

        UnsignedInteger32 requestId = nextRequestId();
        SshFxpReadDir msg = new SshFxpReadDir(requestId, handle);

        sendMessage(msg);

        SubsystemMessage reply = messageStore.getMessage(requestId);

        if (reply instanceof SshFxpName) {
            SshFxpName names = (SshFxpName) reply;
            SftpFile[] files = names.getFiles();

            for (int i = 0; i < files.length; i++) {
                files[i].setSFTPSubsystem(this);
                children.add(files[i]);
            }

            return files.length;
        } else if (reply instanceof SshFxpStatus) {
            SshFxpStatus status = (SshFxpStatus) reply;

            if (status.getErrorCode().intValue() == SshFxpStatus.STATUS_FX_EOF) {
                return -1;
            } else {
                throw new IOException(status.getErrorMessage());
            }
        } else {
            throw new IOException("Unexpected server response " +
                reply.getMessageName());
        }
    }

    /**
     * List the children of a previoulsy opened file
     * <br>
     * This method should be called untill it returns -1 to indicate that
     * all the files have been returned. Use after a call to
     * <code>openDirectory</code> with each subsequent call of this method
     * returning more files from the directories listing. All the files
     * may be returned in the first call, however this is not gaurenteed.<br>
     * <br>
     * An example of using this mehtod:<br>
     * <br>
     * <code>SftpFile file = sftp.openDirectory("docs");<br>
     * int read;<br>
     * Vector children = new Vector();<br>
     * do {<br>
     *      read = sftp.listChildren(file, children);<br>
     * } while(read > 0);<br>
     * <br></code>
     * Each subsequent call to this method adds the returned files to the
     * list of children. Each object in the list is an <code>SftpFile</code>
     * object.
     *
     * @param file the open file instance for the directory
     * @param children  a list to add the returned files and directories to
     * @return  the number of objects added to the list
     * @throws IOException if the operation fails
     */
    public int listChildren(SftpFile file, List children)
        throws IOException {
        return listChildren(file.getHandle(), children);
    }

    /**
     * Creates a directory on the remote server
     *
     * @param path the new path to create
     *
     * @throws IOException if the directory could not be created or a
     * communication error occurs
     */
    public void makeDirectory(String path) throws IOException {
        UnsignedInteger32 requestId = nextRequestId();
        SshFxpMkdir msg = new SshFxpMkdir(requestId, path, new FileAttributes());

        sendMessage(msg);

        getOKRequestStatus(requestId);
    }

    /**
     * Called by the subsystem framework to indicate that the subsystem has
     * been started
     *
     * @throws IOException if the protocol fails to initialize
     */
    protected void onStart() throws IOException {
        if (!initialize()) {
            throw new ServiceOperationException(
                "The protocol failed to initialize!");
        }

        workingDirectory = getDefaultDirectory();
    }

    /**
     * Opens a directory for reading
     *
     * @param path the path to open
     *
     * @return an SftpFile instance for the open directory
     *
     * @throws IOException if the directory cannot be opened
     */
    public SftpFile openDirectory(String path) throws IOException {
        UnsignedInteger32 requestId = nextRequestId();
        SubsystemMessage msg = new SshFxpOpenDir(requestId, path);

        sendMessage(msg);

        byte[] handle = getHandleResponse(requestId);

        requestId = nextRequestId();

        msg = new SshFxpStat(requestId, path);

        sendMessage(msg);

        SubsystemMessage reply = messageStore.getMessage(requestId);

        if (reply instanceof SshFxpAttrs) {
            FileAttributes attrs = ((SshFxpAttrs) reply).getAttributes();
            SftpFile file = new SftpFile(path, attrs);
            file.setHandle(handle);
            file.setSFTPSubsystem(this);

            return file;
        } else if (reply instanceof SshFxpStatus) {
            throw new IOException(((SshFxpStatus) reply).getErrorMessage());
        } else {
            throw new IOException("Unexpected server response " +
                reply.getMessageName());
        }

    }

    public String getDefaultDirectory() throws IOException {
        return getAbsolutePath("");
    }

    public String getAbsolutePath(String path) throws IOException {
        UnsignedInteger32 requestId = nextRequestId();
        SubsystemMessage msg = new SshFxpRealPath(requestId, path);

        sendMessage(msg);

        SubsystemMessage reply = messageStore.getMessage(requestId);

        if (reply instanceof SshFxpName) {
            SftpFile[] files = ((SshFxpName) reply).getFiles();

            if (files.length != 1) {
                throw new IOException(
                    "Server responded to SSH_FXP_REALPATH with too many files!");
            }

            return files[0].getFilename();
        } else if (reply instanceof SshFxpStatus) {
            throw new IOException(((SshFxpStatus) reply).getErrorMessage());
        } else {
            throw new IOException("Unexpected server response " +
                reply.getMessageName());
        }
    }

    public String getAbsolutePath(SftpFile file) throws IOException {
        return getAbsolutePath(file.getFilename());
    }

    public SftpFile openFile(String filename, int flags)
        throws IOException {
        return openFile(filename, flags, null);
    }

    /**
     * Opens a file for use<br>
     * <br>
     * For example, to open the file for reading:<br>
     * <br>
     * <code>SftpFile file = sftp.openFile("readme.txt", SftpSubsystemClient.OPEN_READ);</code><br>
     * <br>
     * To open the file for writing:<br>
     * <br>
     * <code>SftpFile file = sftp.openFile("readme.txt", SftpSubsystemClient.OPEN_WRITE);</code><br>
     * <br>
     * To create the file and open for writing:<br>
     * <br>
     * <code>SftpFile file = sftp.openFile("readme.txt", SftpSubsystemClient.OPEN_CREATE | SftpSubsystemClient.OPEN_WRITE);</code><br>
     *
     * @param filename the file to open
     * @param flags the flags to determine the access required
     * @param attrs default attributes for the file
     *
     * @return an open file instance
     *
     * @throws IOException if the operation fails
     */
    public SftpFile openFile(String filename, int flags, FileAttributes attrs)
        throws IOException {
        if (attrs == null) {
            attrs = new FileAttributes();
        }

        UnsignedInteger32 requestId = nextRequestId();
        SubsystemMessage msg = new SshFxpOpen(requestId, filename,
                new UnsignedInteger32(flags), attrs);

        sendMessage(msg);

        byte[] handle = getHandleResponse(requestId);

        requestId = nextRequestId();

        msg = new SshFxpFStat(requestId, handle);

        sendMessage(msg);

        SubsystemMessage reply = messageStore.getMessage(requestId);

        if (reply instanceof SshFxpAttrs) {
            attrs = ((SshFxpAttrs) reply).getAttributes();

            SftpFile file = new SftpFile(filename, attrs);
            file.setHandle(handle);
            file.setSFTPSubsystem(this);

            return file;
        } else if (reply instanceof SshFxpStatus) {
            throw new IOException(((SshFxpStatus) reply).getErrorMessage());
        } else {
            throw new IOException("Unexpected server response " +
                reply.getMessageName());
        }
    }

    /**
     * Reads data from the server for a previoulsy opened file
     *
     * @param handle the file handle
     * @param offset the offset from the start of the file to read from
     * @param output an output buffer
     * @param off the offset to write into the output buffer
     * @param len the length of data to read
     *
     * @return the number of bytes read
     *
     * @throws IOException if the operation fails
     */
    protected int readFile(byte[] handle, UnsignedInteger64 offset,
        byte[] output, int off, int len) throws IOException {
        if (!handles.contains(handle)) {
            throw new IOException("The file handle is invalid!");
        }

        if ((output.length - off) < len) {
            throw new IOException(
                "Output array size is smaller than read length!");
        }

        UnsignedInteger32 requestId = nextRequestId();
        SshFxpRead msg = new SshFxpRead(requestId, handle, offset,
                new UnsignedInteger32(len));
        sendMessage(msg);

        SubsystemMessage reply = messageStore.getMessage(requestId);

        if (reply instanceof SshFxpData) {
            byte[] msgdata = ((SshFxpData) reply).getData();
            System.arraycopy(msgdata, 0, output, off, msgdata.length);

            return msgdata.length;
        } else if (reply instanceof SshFxpStatus) {
            SshFxpStatus status = (SshFxpStatus) reply;

            if (status.getErrorCode().intValue() == SshFxpStatus.STATUS_FX_EOF) {
                return -1;
            } else {
                throw new IOException(((SshFxpStatus) reply).getErrorMessage());
            }
        } else {
            throw new IOException("Unexpected server response " +
                reply.getMessageName());
        }
    }

    /**
     * Removes a directory from the remote server.
     *
     * @param path the path to remove
     *
     * @throws IOException if the remove operation fails
     */
    public void removeDirectory(String path) throws IOException {
        UnsignedInteger32 requestId = nextRequestId();
        SshFxpRmdir msg = new SshFxpRmdir(requestId, path);

        sendMessage(msg);

        getOKRequestStatus(requestId);
    }

    /**
     * Removes a file from the remote server
     *
     * @param filename the filename to remove
     *
     * @throws IOException if the operation fails
     */
    public void removeFile(String filename) throws IOException {
        UnsignedInteger32 requestId = nextRequestId();
        SshFxpRemove msg = new SshFxpRemove(requestId, filename);

        sendMessage(msg);

        getOKRequestStatus(requestId);
    }

    /**
     * Rename a file on the remote server.
     *
     * @param oldpath the current path of the file
     * @param newpath the new required path
     *
     * @throws IOException if the operation fails
     */
    public void renameFile(String oldpath, String newpath)
        throws IOException {
        UnsignedInteger32 requestId = nextRequestId();
        SshFxpRename msg = new SshFxpRename(requestId, oldpath, newpath);

        sendMessage(msg);

        getOKRequestStatus(requestId);
    }

    /**
     * Write data to a file on the remote server using an open file handle
     *
     * @param handle the open file handle
     * @param offset the offset to write to in the remote file
     * @param data a buffer containing data to write
     * @param off the offset in the data buffer to start from
     * @param len the length of data to write
     *
     * @throws IOException if the operation fails
     */
    protected void writeFile(byte[] handle, UnsignedInteger64 offset,
        byte[] data, int off, int len) throws IOException {
        if (!handles.contains(handle)) {
            throw new IOException("The handle is not valid!");
        }

        if ((data.length - off) < len) {
            throw new IOException("Incorrect data array size!");
        }

        UnsignedInteger32 requestId = nextRequestId();
        SshFxpWrite msg = new SshFxpWrite(requestId, handle, offset, data, off,
                len);
        sendMessage(msg);

        getOKRequestStatus(requestId);
    }

    /**
     * Creates a symbolic link
     * @param targetpath  the target of the symlink
     * @param linkpath    the path of the symlink to be created
     * @throws IOException  if an error occurs or the operation is unsupported
     */
    public void createSymbolicLink(String targetpath, String linkpath)
        throws IOException {
        UnsignedInteger32 requestId = nextRequestId();
        SubsystemMessage msg = new SshFxpSymlink(requestId, targetpath, linkpath);
        sendMessage(msg);

        getOKRequestStatus(requestId);
    }

    /**
     * Gets the target for a symbolic link
     * @param linkpath
     * @return
     * @throws IOException
     */
    public String getSymbolicLinkTarget(String linkpath)
        throws IOException {
        UnsignedInteger32 requestId = nextRequestId();
        SubsystemMessage msg = new SshFxpReadlink(requestId, linkpath);
        sendMessage(msg);

        SubsystemMessage reply = messageStore.getMessage(requestId);

        if (reply instanceof SshFxpName) {
            SftpFile[] files = ((SshFxpName) reply).getFiles();

            if (files.length != 1) {
                throw new IOException(
                    "Server responded to SSH_FXP_REALLINK with too many files!");
            }

            return files[0].getFilename();
        } else if (reply instanceof SshFxpStatus) {
            throw new IOException(((SshFxpStatus) reply).getErrorMessage());
        } else {
            throw new IOException("Unexpected server response " +
                reply.getMessageName());
        }
    }

    public void setAttributes(String path, FileAttributes attrs)
        throws IOException {
        UnsignedInteger32 requestId = nextRequestId();
        SubsystemMessage msg = new SshFxpSetStat(requestId, path, attrs);
        sendMessage(msg);

        getOKRequestStatus(requestId);
    }

    public void setAttributes(SftpFile file, FileAttributes attrs)
        throws IOException {
        if (!isValidHandle(file.getHandle())) {
            throw new IOException("The handle is not an open file handle!");
        }

        UnsignedInteger32 requestId = nextRequestId();
        SubsystemMessage msg = new SshFxpFSetStat(requestId, file.getHandle(),
                attrs);
        sendMessage(msg);

        getOKRequestStatus(requestId);
    }

    public void changePermissions(SftpFile file, int permissions)
        throws IOException {
        FileAttributes attrs = new FileAttributes();
        attrs.setPermissions(new UnsignedInteger32(permissions));
        setAttributes(file, attrs);
    }

    public void changePermissions(String filename, int permissions)
        throws IOException {
        FileAttributes attrs = new FileAttributes();
        attrs.setPermissions(new UnsignedInteger32(permissions));
        setAttributes(filename, attrs);
    }

    /**
     * Initialize the SFTP protocol
     *
     * @return <tt>true</tt> if the protocol initialized otherwise
     *         <tt>false</tt>
     *
     * @throws IOException if the subsystem fails to initialize
     */
    protected boolean initialize() throws IOException {
        log.info("Initializing SFTP protocol version " +
            String.valueOf(version));

        boolean result = false;
        SshFxpInit msg = new SshFxpInit(new UnsignedInteger32(version), null);
        sendMessage(msg);

        SubsystemMessage reply = messageStore.nextMessage();

        if (reply instanceof SshFxpVersion) {
            result = true;
            version = ((SshFxpVersion) reply).getVersion().intValue();
            log.info("Server responded with version " +
                String.valueOf(version));
        }

        return result;
    }

    /**
     * Waits for an SSH_FXP_HANDLE response from the server adn returns
     * the handle. If the server responds with a failure SSH_FXP_STATUS
     * message an exception is thrown
     *
     * @return the open handle
     *
     * @throws IOException if the operation fails
     */
    private byte[] getHandleResponse(UnsignedInteger32 requestId)
        throws IOException {
        SubsystemMessage reply = messageStore.getMessage(requestId);

        if (reply instanceof SshFxpHandle) {
            byte[] handle = ((SshFxpHandle) reply).getHandle();

            // Add the handle to our managed list
            handles.add(handle);

            return handle;
        } else if (reply instanceof SshFxpStatus) {
            throw new IOException(((SshFxpStatus) reply).getErrorMessage());
        } else {
            throw new IOException("Unexpected server response " +
                reply.getMessageName());
        }
    }

    /**
     * Waits for a SSH_FXP_STATUS message, throwing an exception for
     * any other result other than SshFxpStatus.STATUS_FX_OK
     *
     * @throws IOException if the operation fails
     */
    private void getOKRequestStatus(UnsignedInteger32 requestId)
        throws IOException {
        SubsystemMessage reply = messageStore.getMessage(requestId);

        if (reply instanceof SshFxpStatus) {
            SshFxpStatus status = (SshFxpStatus) reply;

            if (status.getErrorCode().intValue() != SshFxpStatus.STATUS_FX_OK) {
                throw new IOException(((SshFxpStatus) reply).getErrorMessage());
            }
        } else {
            throw new IOException("Unexpected server response " +
                reply.getMessageName());
        }
    }

    /**
     * Returns the available next request id
     *
     * @return the next available request id
     */
    private UnsignedInteger32 nextRequestId() {
        nextRequestId = UnsignedInteger32.add(nextRequestId, 1);

        return nextRequestId;
    }

    /**
     * Registers the subsystem messages with the message store
     */
    private void registerMessages() {
        messageStore.registerMessage(SshFxpVersion.SSH_FXP_VERSION,
            SshFxpVersion.class);
        messageStore.registerMessage(SshFxpAttrs.SSH_FXP_ATTRS,
            SshFxpAttrs.class);
        messageStore.registerMessage(SshFxpData.SSH_FXP_DATA, SshFxpData.class);
        messageStore.registerMessage(SshFxpHandle.SSH_FXP_HANDLE,
            SshFxpHandle.class);
        messageStore.registerMessage(SshFxpStatus.SSH_FXP_STATUS,
            SshFxpStatus.class);
        messageStore.registerMessage(SshFxpName.SSH_FXP_NAME, SshFxpName.class);
    }
}
