package com.sshtools.j2ssh.sftp;

import java.io.IOException;
/**
* @author unascribed
* @version 1.0
*/

public class SftpFile {

    private String filename;
    private byte handle[];
    private FileAttributes attrs;
    private SftpSubsystemClient sftp;

    protected SftpFile(String filename, FileAttributes attrs) {
	this.filename = filename;
	if(attrs==null)
	    this.attrs = new FileAttributes();
	else
	    this.attrs = attrs;
    }

    public boolean isOpen() {
	if(sftp==null)
	    return false;

	return sftp.isValidHandle(handle);
    }


    protected void setHandle(byte handle[]) {
	this.handle = handle;
    }

    protected byte[] getHandle() {
	return handle;
    }

    protected void setSFTPSubsystem(SftpSubsystemClient sftp) {
	this.sftp = sftp;
    }

    protected SftpSubsystemClient getSFTPSubsystem() {
	return sftp;
    }

    public String getFilename() {
	return filename;
    }

    private String pad(int num) {
	String str = "";
	if(num>0) {
	    for(int i=0;i<num;i++)
		str+=" ";
	}
	return str;
    }

    public String getLongname() {
	StringBuffer str = new StringBuffer();
	str.append(pad(10-attrs.getPermissionsString().length()) + attrs.getPermissionsString());
	str.append("    1 ");
	str.append(attrs.getUID().toString() + pad(8-attrs.getUID().toString().length())); //uid
	str.append(" ");
	str.append(attrs.getGID().toString() + pad(8-attrs.getGID().toString().length())); //gid
	str.append(" ");
	str.append(pad(8-attrs.getSize().toString().length()) + attrs.getSize().toString());
	str.append(" ");
	str.append(pad(12-attrs.getModTimeString().length()) + attrs.getModTimeString());
	str.append(" ");
	str.append(filename);
	return str.toString();
    }

    public FileAttributes getAttributes() {
	return attrs;
    }

    public String getAbsolutePath() throws IOException {
	return sftp.getAbsolutePath(this);
    }

    public void close() throws IOException {
	sftp.closeFile(this);
    }

    public boolean isDirectory() {
	return (attrs.getPermissions().intValue()
	 & FileAttributes.S_IFDIR)
	== FileAttributes.S_IFDIR;
    }

    public boolean isFile() {
	return (attrs.getPermissions().intValue()
	 & FileAttributes.S_IFREG)
	== FileAttributes.S_IFREG;
    }

    public boolean isLink() {
	return (attrs.getPermissions().intValue()
	 & FileAttributes.S_IFLNK)
	== FileAttributes.S_IFLNK;

    }

    public boolean isFifo() {
	return (attrs.getPermissions().intValue()
	 & FileAttributes.S_IFIFO)
	== FileAttributes.S_IFIFO;
    }

    public boolean isBlock() {
	return (attrs.getPermissions().intValue()
	 & FileAttributes.S_IFBLK)
	== FileAttributes.S_IFBLK;
    }

    public boolean isCharacter() {
	return (attrs.getPermissions().intValue()
	 & FileAttributes.S_IFCHR)
	== FileAttributes.S_IFCHR;
    }

    public boolean isSocket() {
	return (attrs.getPermissions().intValue()
	 & FileAttributes.S_IFSOCK)
	== FileAttributes.S_IFSOCK;
    }

}
