package com.sshtools.j2ssh.sftp;

import com.sshtools.j2ssh.subsystem.SubsystemMessage;

import com.sshtools.j2ssh.io.ByteArrayReader;
import com.sshtools.j2ssh.io.ByteArrayWriter;
import com.sshtools.j2ssh.io.UnsignedInteger32;
/**
 * @author unascribed
 * @version 1.0
 */

public class SshFxpRename extends SubsystemMessage implements MessageRequestId {

  public static final int SSH_FXP_RENAME = 18;

  private UnsignedInteger32 id;
  String oldpath;
  String newpath;

  public SshFxpRename() {
    super(SSH_FXP_RENAME);
  }

  public SshFxpRename(UnsignedInteger32 id, String oldpath, String newpath) {
    super(SSH_FXP_RENAME);
    this.id = id;
    this.oldpath = oldpath;
    this.newpath = newpath;
  }

  public UnsignedInteger32 getId() {
    return id;
  }

  public String getOldPath() {
    return oldpath;
  }

  public String getNewPath() {
    return newpath;
  }

  public void constructMessage(ByteArrayReader bar) throws java.io.IOException, com.sshtools.j2ssh.transport.InvalidMessageException {
    id = bar.readUINT32();
    oldpath = bar.readString();
    newpath = bar.readString();
  }
  public String getMessageName() {
    return "SSH_FXP_RENAME";
  }

  public void constructByteArray(ByteArrayWriter baw) throws java.io.IOException, com.sshtools.j2ssh.transport.InvalidMessageException {
    baw.writeUINT32(id);
    baw.writeString(oldpath);
    baw.writeString(newpath);
  }
}
