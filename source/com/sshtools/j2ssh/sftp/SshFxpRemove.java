package com.sshtools.j2ssh.sftp;

import com.sshtools.j2ssh.subsystem.*;

import com.sshtools.j2ssh.io.ByteArrayReader;
import com.sshtools.j2ssh.io.ByteArrayWriter;
import com.sshtools.j2ssh.io.UnsignedInteger32;
/**
 * @author unascribed
 * @version 1.0
 */

public class SshFxpRemove extends SubsystemMessage implements MessageRequestId {

  public static final int SSH_FXP_REMOVE = 13;

  private UnsignedInteger32 id;
  private String filename;

  public SshFxpRemove() {
    super(SSH_FXP_REMOVE);
  }

  public SshFxpRemove(UnsignedInteger32 id, String filename) {
    super(SSH_FXP_REMOVE);
    this.id = id;
    this.filename = filename;
  }

  public UnsignedInteger32 getId() {
    return id;
  }

  public String getFilename() {
    return filename;
  }

  public void constructMessage(ByteArrayReader bar) throws java.io.IOException, com.sshtools.j2ssh.transport.InvalidMessageException {
    id = bar.readUINT32();
    filename = bar.readString();
  }
  public String getMessageName() {
    return "SSH_FXP_REMOVE";
  }

  public void constructByteArray(ByteArrayWriter baw) throws java.io.IOException, com.sshtools.j2ssh.transport.InvalidMessageException {
    baw.writeUINT32(id);
    baw.writeString(filename);
  }
}
