package com.sshtools.j2ssh.sftp;

import com.sshtools.j2ssh.subsystem.SubsystemMessage;
import com.sshtools.j2ssh.io.ByteArrayReader;
import com.sshtools.j2ssh.io.ByteArrayWriter;
import com.sshtools.j2ssh.io.UnsignedInteger32;


/**
 * @author unascribed
 * @version $Id$
 */

public class SshFxpSymlink extends SubsystemMessage implements MessageRequestId {

  public static final int SSH_FXP_SYMLINK = 20;
  private UnsignedInteger32 id;
  private String linkpath;
  private String targetpath;

  public SshFxpSymlink() {
    super(SSH_FXP_SYMLINK);
  }

  public SshFxpSymlink(UnsignedInteger32 id, String targetpath, String linkpath) {
    super(SSH_FXP_SYMLINK);
    this.id = id;
    this.linkpath = linkpath;
    this.targetpath = targetpath;
  }

  public UnsignedInteger32 getId() {
    return id;
  }

  public String getLinkPath() {
    return linkpath;
  }

  public String getTargetPath() {
    return targetpath;
  }

  public void constructMessage(ByteArrayReader bar) throws java.io.IOException, com.sshtools.j2ssh.transport.InvalidMessageException {
    id = bar.readUINT32();
    linkpath = bar.readString();
    targetpath = bar.readString();

  }
  public String getMessageName() {
    return "SSH_FXP_SYMLINK";
  }

  public void constructByteArray(ByteArrayWriter baw) throws java.io.IOException, com.sshtools.j2ssh.transport.InvalidMessageException {
    baw.writeUINT32(id);
    baw.writeString(linkpath);
    baw.writeString(targetpath);
  }
}
