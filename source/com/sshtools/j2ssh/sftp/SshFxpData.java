package com.sshtools.j2ssh.sftp;

import com.sshtools.j2ssh.subsystem.SubsystemMessage;

import com.sshtools.j2ssh.io.ByteArrayReader;
import com.sshtools.j2ssh.io.ByteArrayWriter;
import com.sshtools.j2ssh.io.UnsignedInteger32;
/**
 * @author unascribed
 * @version 1.0
 */

public class SshFxpData extends SubsystemMessage implements MessageRequestId {

  public static final int SSH_FXP_DATA = 103;

  private UnsignedInteger32 id;
  private byte data[];

  public SshFxpData(UnsignedInteger32 id, byte[] data) {
    super(SSH_FXP_DATA);
    this.id = id;
    this.data = data;
  }

  public SshFxpData() {
    super(SSH_FXP_DATA);
  }

  public UnsignedInteger32 getId() {
    return id;
  }

  public byte[] getData() {
    return data;
  }

  public void constructMessage(ByteArrayReader bar) throws java.io.IOException, com.sshtools.j2ssh.transport.InvalidMessageException {
    id = bar.readUINT32();
    data = bar.readBinaryString();
  }

  public String getMessageName() {
    return "SSH_FXP_DATA";
  }
  public void constructByteArray(ByteArrayWriter baw) throws java.io.IOException, com.sshtools.j2ssh.transport.InvalidMessageException {
    baw.writeUINT32(id);
    baw.writeBinaryString(data);
  }
}
