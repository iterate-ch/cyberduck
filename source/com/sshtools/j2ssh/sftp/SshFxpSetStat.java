package com.sshtools.j2ssh.sftp;

import com.sshtools.j2ssh.subsystem.SubsystemMessage;

import com.sshtools.j2ssh.io.ByteArrayReader;
import com.sshtools.j2ssh.io.ByteArrayWriter;
import com.sshtools.j2ssh.io.UnsignedInteger32;

/**
 * @author unascribed
 * @version $Id$
 */

public class SshFxpSetStat extends SubsystemMessage implements MessageRequestId {

  public static final int SSH_FXP_SETSTAT = 9;

  private UnsignedInteger32 id;
  private String path;
  private FileAttributes attrs;

  public SshFxpSetStat() {
    super(SSH_FXP_SETSTAT);
  }

  public SshFxpSetStat(UnsignedInteger32 id, String path, FileAttributes attrs) {
    super(SSH_FXP_SETSTAT);
    this.id = id;
    this.path = path;
    this.attrs = attrs;
  }

  public UnsignedInteger32 getId() {
    return id;
  }

  public String getPath() {
    return path;
  }

  public FileAttributes getAttributes() {
    return attrs;
  }

  public void constructMessage(ByteArrayReader bar) throws java.io.IOException, com.sshtools.j2ssh.transport.InvalidMessageException {
    id = bar.readUINT32();
    path = bar.readString();
    attrs = new FileAttributes(bar);
  }

  public String getMessageName() {
    return "SSH_FXP_SETSTAT";
  }

  public void constructByteArray(ByteArrayWriter baw) throws java.io.IOException, com.sshtools.j2ssh.transport.InvalidMessageException {
    baw.writeUINT32(id);
    baw.writeString(path);
    baw.write(attrs.toByteArray());
  }
}
