package com.sshtools.j2ssh.sftp;

import com.sshtools.j2ssh.subsystem.SubsystemMessage;

import com.sshtools.j2ssh.io.ByteArrayReader;
import com.sshtools.j2ssh.io.ByteArrayWriter;
import com.sshtools.j2ssh.io.UnsignedInteger32;

/**
 * @author unascribed
 * @version 1.0
 */

public class SshFxpFStat extends SubsystemMessage implements MessageRequestId {

  public static final int SSH_FXP_FSTAT = 8;

  private UnsignedInteger32 id;
  private byte[] handle;

  public SshFxpFStat() {
    super(SSH_FXP_FSTAT);
  }

  public SshFxpFStat(UnsignedInteger32 id, byte[] handle) {
    super(SSH_FXP_FSTAT);
    this.id = id;
    this.handle = handle;
  }

  public void constructMessage(ByteArrayReader bar) throws java.io.IOException, com.sshtools.j2ssh.transport.InvalidMessageException {
    id = bar.readUINT32();
    handle = bar.readBinaryString();
  }

  public String getMessageName() {
    return "SSH_FXP_FSTAT";
  }

  public UnsignedInteger32 getId() {
    return id;
  }

  public byte[] getHandle() {
    return handle;
  }

  public void constructByteArray(ByteArrayWriter baw) throws java.io.IOException, com.sshtools.j2ssh.transport.InvalidMessageException {
    baw.writeUINT32(id);
    baw.writeBinaryString(handle);
  }
}
