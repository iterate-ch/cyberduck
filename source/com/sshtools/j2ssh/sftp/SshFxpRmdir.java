package com.sshtools.j2ssh.sftp;

import com.sshtools.j2ssh.subsystem.*;

import com.sshtools.j2ssh.io.ByteArrayReader;
import com.sshtools.j2ssh.io.ByteArrayWriter;
import com.sshtools.j2ssh.io.UnsignedInteger32;

/**
 * @author unascribed
 * @version 1.0
 */

public class SshFxpRmdir extends SubsystemMessage implements MessageRequestId {

  public static final int SSH_FXP_RMDIR = 15;

  private UnsignedInteger32 id;
  private String path;

  public SshFxpRmdir() {
    super(SSH_FXP_RMDIR);
  }

  public SshFxpRmdir(UnsignedInteger32 id, String path) {
    super(SSH_FXP_RMDIR);
    this.id = id;
    this.path = path;
  }

  public UnsignedInteger32 getId() {
    return id;
  }

  public String getPath() {
    return path;
  }

  public void constructMessage(ByteArrayReader bar) throws java.io.IOException, com.sshtools.j2ssh.transport.InvalidMessageException {
    id = bar.readUINT32();
    path = bar.readString();
  }
  public String getMessageName() {
    return "SSH_FXP_RMDIR";
  }

  public void constructByteArray(ByteArrayWriter baw) throws java.io.IOException, com.sshtools.j2ssh.transport.InvalidMessageException {
    baw.writeUINT32(id);
    baw.writeString(path);
  }

}
