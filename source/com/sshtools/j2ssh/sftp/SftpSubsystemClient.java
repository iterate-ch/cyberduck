/*
 *  SSHTools - Java SSH2 API
 *
 *  Copyright (C) 2002-2003 Lee David Painter and Contributors.
 *
 *  Contributions made by:
 *
 *  Brett Smith
 *  Richard Pernavas
 *  Erwin Bolwidt
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Library General Public License
 *  as published by the Free Software Foundation; either version 2 of
 *  the License, or (at your option) any later version.
 *
 *  You may also distribute it and/or modify it under the terms of the
 *  Apache style J2SSH Software License. A copy of which should have
 *  been provided with the distribution.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  License document supplied with your distribution for more details.
 *
 */
package com.sshtools.j2ssh.sftp;

import com.sshtools.j2ssh.SshException;
import com.sshtools.j2ssh.connection.ChannelState;
import com.sshtools.j2ssh.io.UnsignedInteger32;
import com.sshtools.j2ssh.io.UnsignedInteger64;
import com.sshtools.j2ssh.subsystem.SubsystemChannel;
import com.sshtools.j2ssh.subsystem.SubsystemMessage;
import com.sshtools.j2ssh.transport.MessageNotAvailableException;
import com.sshtools.j2ssh.transport.MessageStoreEOFException;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.List;
import java.util.Vector;

/**
 * @author $author$
 * @version $Revision$
 */
public class SftpSubsystemClient extends SubsystemChannel {
	/**  */
	public static final int OPEN_READ = SshFxpOpen.FXF_READ;

	/**  */
	public static final int OPEN_WRITE = SshFxpOpen.FXF_WRITE;

	/**  */
	public static final int OPEN_APPEND = SshFxpOpen.FXF_APPEND;

	/**  */
	public static final int OPEN_CREATE = SshFxpOpen.FXF_CREAT;

	/**  */
	public static final int OPEN_TRUNCATE = SshFxpOpen.FXF_TRUNC;

	/**  */
	public static final int OPEN_EXCLUSIVE = SshFxpOpen.FXF_EXCL;

	/**  */
	public static final int VERSION_1 = 1;

	/**  */
	public static final int VERSION_2 = 2;

	/**  */
	public static final int VERSION_3 = 3;

	/**  */
	public static final int VERSION_4 = 4;

	/* Private variables */
	private static Logger log = Logger.getLogger(SftpSubsystemClient.class);
	private List handles = new Vector();
	private UnsignedInteger32 nextRequestId = new UnsignedInteger32(1);
	private int version = VERSION_3;
	private SftpMessageStore messageStore;

	/**
	 * Creates a new SftpSubsystemClient object.
	 */
	public SftpSubsystemClient(String encoding) {
		// We will use our own message store implementation
		super("sftp", new SftpMessageStore(encoding), encoding);
		messageStore = (SftpMessageStore)super.messageStore;
		this.registerMessages();
	}

	/**
	 * @return
	 */
	public String getName() {
		return "sftp";
	}

	/**
	 * @return
	 */
	protected long availableWindowSpace() {
		return getRemoteWindow().getWindowSpace();
	}

	/**
	 * @return
	 */
	protected long maximumPacketSize() {
		return getRemotePacketSize();
	}

	/**
	 * @param handle
	 * @throws IOException
	 */
	protected synchronized void closeHandle(byte[] handle)
	    throws IOException
    {
		if(!isValidHandle(handle)) {
			throw new SshException("The handle is invalid!");
		}

		// We will remove the handle first so that even if an excpetion occurs
		// the file as far as were concerned is closed
		handles.remove(handle);

		UnsignedInteger32 requestId = nextRequestId();
		SshFxpClose msg = new SshFxpClose(requestId, handle);
		this.sendMessage(msg);
		this.getOKRequestStatus(requestId);
	}

	/**
	 * @param file
	 * @throws IOException
	 */
	public void closeFile(SftpFile file) throws IOException {
		closeHandle(file.getHandle());
	}

	/**
	 * @param handle
	 * @return
	 */
	protected boolean isValidHandle(byte[] handle) {
		return handles.contains(handle);
	}

	/**
	 * @param file
	 * @param children
	 * @return
	 * @throws IOException
	 */
	public synchronized int listChildren(SftpFile file, List children)
	    throws IOException
    {
		if(file.isDirectory()) {
			if(!isValidHandle(file.getHandle())) {
				file = openDirectory(file.getAbsolutePath());

				if(!isValidHandle(file.getHandle())) {
					throw new SshException("Failed to open directory");
				}
			}
		}
		else {
			throw new SshException("Cannot list children for this file object");
		}

		UnsignedInteger32 requestId = nextRequestId();
		SshFxpReadDir msg = new SshFxpReadDir(requestId, file.getHandle());
		this.sendMessage(msg);

		try {
			SubsystemMessage reply = messageStore.getMessage(requestId);

			if(reply instanceof SshFxpName) {
				SshFxpName names = (SshFxpName)reply;
				SftpFile[] files = names.getFiles();
				SftpFile f;

				for(int i = 0; i < files.length; i++) {
					f = new SftpFile(file.getAbsolutePath()+"/"+files[i].getFilename(), files[i].getAttributes());
					f.setSFTPSubsystem(this);
					children.add(f);
				}

				return files.length;
			}
			else if(reply instanceof SshFxpStatus) {
				SshFxpStatus status = (SshFxpStatus)reply;

				if(status.getErrorCode().intValue() == SshFxpStatus.STATUS_FX_EOF) {
					return -1;
				}
				else {
					throw new SshException(status.getErrorMessage());
				}
			}
			else {
				throw new SshException("Unexpected server response "+
				    reply.getMessageName());
			}
		}
		catch(InterruptedException ex) {
            throw new IOException(ex.getMessage());
		}
	}

    /**
     *
     * @param path
     * @throws IOException
     */
    public synchronized void makeDirectory(String path)
	    throws IOException
    {
		UnsignedInteger32 requestId = nextRequestId();
		SshFxpMkdir msg = new SshFxpMkdir(requestId, path, new FileAttributes());
		this.sendMessage(msg);
		this.getOKRequestStatus(requestId);
	}

	/**
	 * @param path
	 * @return
	 * @throws IOException
	 */
	public synchronized SftpFile openDirectory(String path)
	    throws IOException
    {
		String absolutePath = getAbsolutePath(path);
		UnsignedInteger32 requestId = nextRequestId();
		SubsystemMessage msg = new SshFxpOpenDir(requestId, absolutePath);
		this.sendMessage(msg);
		byte[] handle = getHandleResponse(requestId);
		requestId = nextRequestId();
		msg = new SshFxpStat(requestId, absolutePath);
		this.sendMessage(msg);
		try {
			SubsystemMessage reply = messageStore.getMessage(requestId);
			if(reply instanceof SshFxpAttrs) {
				SftpFile file = new SftpFile(absolutePath,
				    ((SshFxpAttrs)reply).getAttributes());
				file.setHandle(handle);
				file.setSFTPSubsystem(this);

				return file;
			}
			else if(reply instanceof SshFxpStatus) {
				throw new SshException(((SshFxpStatus)reply).getErrorMessage());
			}
			else {
				throw new SshException("Unexpected server response "+
				    reply.getMessageName());
			}
		}
		catch(InterruptedException ex) {
			throw new IOException(ex.getMessage());
		}
	}

	/**
	 * @return
	 * @throws IOException
	 */
	public String getDefaultDirectory() throws IOException {
		return getAbsolutePath("");
	}

	/**
	 * @param path
	 * @return
	 * @throws IOException
	 */
	public synchronized String getAbsolutePath(String path)
	    throws IOException {
		UnsignedInteger32 requestId = nextRequestId();
		SubsystemMessage msg = new SshFxpRealPath(requestId, path);
		this.sendMessage(msg);

		try {
			SubsystemMessage reply = messageStore.getMessage(requestId);

			if(reply instanceof SshFxpName) {
				SftpFile[] files = ((SshFxpName)reply).getFiles();

				if(files.length != 1) {
					throw new SshException("Server responded to SSH_FXP_REALPATH with too many files!");
				}

				return files[0].getAbsolutePath();
			}
			else if(reply instanceof SshFxpStatus) {
				throw new SshException(((SshFxpStatus)reply).getErrorMessage());
			}
			else {
				throw new SshException("Unexpected server response "+
				    reply.getMessageName());
			}
		}
		catch(InterruptedException ex) {
            throw new IOException(ex.getMessage());
		}
	}

	/**
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public String getAbsolutePath(SftpFile file) throws IOException {
		return getAbsolutePath(file.getFilename());
	}

	/**
	 * @param filename
	 * @param flags
	 * @return
	 * @throws IOException
	 */
	public SftpFile openFile(String filename, int flags)
	    throws IOException {
		return openFile(filename, flags, null);
	}

	/**
	 * @param absolutePath
	 * @param flags
	 * @param attrs
	 * @return
	 * @throws IOException
	 */
	public synchronized SftpFile openFile(String absolutePath, int flags,
	                                      FileAttributes attrs)
            throws IOException
    {
		if(attrs == null) {
			attrs = new FileAttributes();
		}

		UnsignedInteger32 requestId = nextRequestId();
		SubsystemMessage msg = new SshFxpOpen(requestId, absolutePath,
		    new UnsignedInteger32(flags), attrs);
		this.sendMessage(msg);

		byte[] handle = getHandleResponse(requestId);
		SftpFile file = new SftpFile(absolutePath, null);
		file.setHandle(handle);
		file.setSFTPSubsystem(this);

		return file;
	}

	/**
	 * @param path
	 * @return
	 * @throws IOException
	 */
	public synchronized FileAttributes getAttributes(String path)
	    throws IOException
    {
		SubsystemMessage msg;
		UnsignedInteger32 requestId = nextRequestId();
		msg = new SshFxpStat(requestId, path);
		sendMessage(msg);

		try {
			SubsystemMessage reply = messageStore.getMessage(requestId);

			if(reply instanceof SshFxpAttrs) {
				return ((SshFxpAttrs)reply).getAttributes();
			}
			else if(reply instanceof SshFxpStatus) {
				throw new SshException(((SshFxpStatus)reply).getErrorMessage());
			}
			else {
				throw new SshException("Unexpected server response "+
				    reply.getMessageName());
			}
		}
		catch(InterruptedException ex) {
            throw new IOException(ex.getMessage());
		}
	}

	/**
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public synchronized FileAttributes getAttributes(SftpFile file)
	    throws IOException
    {
		SubsystemMessage msg;
		UnsignedInteger32 requestId = nextRequestId();

		if(!isValidHandle(file.getHandle())) {
			msg = new SshFxpStat(requestId, file.getAbsolutePath());
		}
		else {
			msg = new SshFxpFStat(requestId, file.getHandle());
		}

		this.sendMessage(msg);

		try {
			SubsystemMessage reply = messageStore.getMessage(requestId);

			if(reply instanceof SshFxpAttrs) {
				return ((SshFxpAttrs)reply).getAttributes();
			}
			else if(reply instanceof SshFxpStatus) {
				throw new SshException(((SshFxpStatus)reply).getErrorMessage());
			}
			else {
				throw new SshException("Unexpected server response "+
				    reply.getMessageName());
			}
		}
		catch(InterruptedException ex) {
            throw new IOException(ex.getMessage());
		}
	}

	/**
	 * @param handle
	 * @param offset
	 * @param output
	 * @param off
	 * @param len
	 * @return
	 * @throws IOException
	 */
	protected synchronized int readFile(byte[] handle,
	                                    UnsignedInteger64 offset, byte[] output, int off, int len)
	    throws IOException
    {
		if(!handles.contains(handle)) {
			throw new SshException("The file handle is invalid!");
		}

		if((output.length-off) < len) {
			throw new SshException("Output array size is smaller than read length!");
		}

		UnsignedInteger32 requestId = nextRequestId();
		SshFxpRead msg = new SshFxpRead(requestId, handle, offset,
		    new UnsignedInteger32(len));
		this.sendMessage(msg);

		try {
			SubsystemMessage reply = messageStore.getMessage(requestId);

			if(reply instanceof SshFxpData) {
				byte[] msgdata = ((SshFxpData)reply).getData();
				System.arraycopy(msgdata, 0, output, off, msgdata.length);

				return msgdata.length;
			}
			else if(reply instanceof SshFxpStatus) {
				SshFxpStatus status = (SshFxpStatus)reply;

				if(status.getErrorCode().intValue() == SshFxpStatus.STATUS_FX_EOF) {
					return -1;
				}
				else {
					throw new SshException(((SshFxpStatus)reply).getErrorMessage());
				}
			}
			else {
				throw new SshException("Unexpected server response "+
				    reply.getMessageName());
			}
		}
		catch(InterruptedException ex) {
            throw new IOException(ex.getMessage());
		}
	}

	/**
	 * @param path
	 * @throws IOException
	 */
	public synchronized void removeDirectory(String path)
	    throws IOException
    {
		UnsignedInteger32 requestId = nextRequestId();
		SshFxpRmdir msg = new SshFxpRmdir(requestId, path);
		this.sendMessage(msg);
		this.getOKRequestStatus(requestId);
	}

	/**
	 * @param filename
	 * @throws IOException
	 */
	public synchronized void removeFile(String filename)
	    throws IOException
    {
		UnsignedInteger32 requestId = nextRequestId();
		SshFxpRemove msg = new SshFxpRemove(requestId, filename);
		this.sendMessage(msg);
		this.getOKRequestStatus(requestId);
	}

	/**
	 * @param oldpath
	 * @param newpath
	 * @throws IOException
	 */
	public synchronized void renameFile(String oldpath, String newpath)
	    throws IOException
    {
		UnsignedInteger32 requestId = nextRequestId();
		SshFxpRename msg = new SshFxpRename(requestId, oldpath, newpath);
		this.sendMessage(msg);
		this.getOKRequestStatus(requestId);
	}

	/**
	 * @param handle
	 * @param offset
	 * @param data
	 * @param off
	 * @param len
	 * @throws IOException
	 */
	protected synchronized void writeFile(byte[] handle,
	                                      UnsignedInteger64 offset, byte[] data, int off, int len)
	    throws IOException
    {
		if(!handles.contains(handle)) {
			throw new SshException("The handle is not valid!");
		}

		if((data.length-off) < len) {
			throw new SshException("Incorrect data array size!");
		}

		UnsignedInteger32 requestId = nextRequestId();
		SshFxpWrite msg = new SshFxpWrite(requestId, handle, offset, data, off,
		    len);
		this.sendMessage(msg);
		this.getOKRequestStatus(requestId);
	}

	/**
	 * @param targetpath
	 * @param linkpath
	 * @throws IOException
	 */
	public synchronized void createSymbolicLink(String targetpath,
	                                            String linkpath)
            throws IOException
    {
		UnsignedInteger32 requestId = nextRequestId();
		SubsystemMessage msg = new SshFxpSymlink(requestId, targetpath, linkpath);
		this.sendMessage(msg);
		this.getOKRequestStatus(requestId);
	}

	/**
	 * @param linkpath
	 * @return
	 * @throws IOException
	 */
	public synchronized String getSymbolicLinkTarget(String linkpath)
	    throws IOException
    {
		UnsignedInteger32 requestId = nextRequestId();
		SubsystemMessage msg = new SshFxpReadlink(requestId, linkpath);
		this.sendMessage(msg);

		try {
			SubsystemMessage reply = messageStore.getMessage(requestId);

			if(reply instanceof SshFxpName) {
				SftpFile[] files = ((SshFxpName)reply).getFiles();

				if(files.length != 1) {
					throw new SshException("Server responded to SSH_FXP_REALLINK with too many files!");
				}

				return files[0].getAbsolutePath();
			}
			else if(reply instanceof SshFxpStatus) {
				throw new SshException(((SshFxpStatus)reply).getErrorMessage());
			}
			else {
				throw new SshException("Unexpected server response "+
				    reply.getMessageName());
			}
		}
		catch(InterruptedException ex) {
            throw new IOException(ex.getMessage());
		}
	}

	/**
	 * @param path
	 * @param attrs
	 * @throws IOException
	 */
	public synchronized void setAttributes(String path, FileAttributes attrs)
	    throws IOException
    {
		UnsignedInteger32 requestId = nextRequestId();
		SubsystemMessage msg = new SshFxpSetStat(requestId, path, attrs);
		this.sendMessage(msg);
		this.getOKRequestStatus(requestId);
	}

	/**
	 * @param file
	 * @param attrs
	 * @throws IOException
	 */
	public synchronized void setAttributes(SftpFile file, FileAttributes attrs)
	    throws IOException
    {
		if(!isValidHandle(file.getHandle())) {
			throw new SshException("The handle is not an open file handle!");
		}
		UnsignedInteger32 requestId = nextRequestId();
		SubsystemMessage msg = new SshFxpFSetStat(requestId, file.getHandle(), attrs);
		this.sendMessage(msg);
		this.getOKRequestStatus(requestId);
	}

	/**
	 * @param path
	 * @param permissions
	 * @throws IOException
	 */
	public void changePermissions(String path, String permissions)
	    throws IOException
    {
		FileAttributes attrs = new FileAttributes();
		attrs.setPermissions(permissions);
		this.setAttributes(path, attrs);
	}

	/**
	 * @return
	 * @throws IOException
	 */
	public synchronized boolean initialize()
            throws IOException
    {
		log.info("Initializing SFTP protocol version "+
		    String.valueOf(version));

		if(!this.startSubsystem()) {
			return false;
		}

		boolean result = false;
		SshFxpInit msg = new SshFxpInit(new UnsignedInteger32(version), null);
		this.sendMessage(msg);

		// Lets give the sftp subsystem 30 seconds to reply
		SubsystemMessage reply = null;

		for(int i = 0; i < 30; i++) {
			try {
				reply = messageStore.nextMessage(1000);

				break;
			}
			catch(MessageNotAvailableException ex) {
				// We timed out so just continue by looking at the session state
			}
			catch(MessageStoreEOFException ex) {
				return false;
			}

			if(getState().getValue() != ChannelState.CHANNEL_OPEN) {
				return false;
			}

			// Try again
		}

		if(reply instanceof SshFxpVersion) {
			result = true;
			version = ((SshFxpVersion)reply).getVersion().intValue();
			log.info("Server responded with version "+
			    String.valueOf(version));
		}

		return result;
	}

	private byte[] getHandleResponse(UnsignedInteger32 requestId)
	    throws IOException {
		try {
			SubsystemMessage reply = messageStore.getMessage(requestId);

			if(reply instanceof SshFxpHandle) {
				byte[] handle = ((SshFxpHandle)reply).getHandle();

				// Add the handle to our managed list
				handles.add(handle);

				return handle;
			}
			else if(reply instanceof SshFxpStatus) {
				throw new SshException(((SshFxpStatus)reply).getErrorMessage());
			}
			else {
				throw new SshException("Unexpected server response "+
				    reply.getMessageName());
			}
		}
		catch(InterruptedException ex) {
            throw new IOException(ex.getMessage());
		}
	}

	private void getOKRequestStatus(UnsignedInteger32 requestId)
	    throws IOException {
		try {
			if(log.isDebugEnabled()) {
				log.info("Waiting for response");
			}

			SubsystemMessage reply = messageStore.getMessage(requestId);

			if(reply instanceof SshFxpStatus) {
				SshFxpStatus status = (SshFxpStatus)reply;

				if(status.getErrorCode().intValue() != SshFxpStatus.STATUS_FX_OK) {
					throw new SshException(((SshFxpStatus)reply).getErrorMessage());
				}
			}
			else {
				throw new SshException("Unexpected server response "+
				    reply.getMessageName());
			}
		}
		catch(InterruptedException ex) {
            throw new IOException(ex.getMessage());
		}
	}

	private UnsignedInteger32 nextRequestId() {
		nextRequestId = UnsignedInteger32.add(nextRequestId, 1);

		return nextRequestId;
	}

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

	protected int getMinimumWindowSpace() {
		return 1024;
	}

	/**
	 * @return
	 */
	protected int getMaximumWindowSpace() {
		return 131070;
	}

	/**
	 * @return
	 */
	protected int getMaximumPacketSize() {
		return 65535;
	}
}
