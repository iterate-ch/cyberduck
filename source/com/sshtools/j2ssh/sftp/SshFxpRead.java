package com.sshtools.j2ssh.sftp;

import com.sshtools.j2ssh.subsystem.SubsystemMessage;

import com.sshtools.j2ssh.io.ByteArrayReader;
import com.sshtools.j2ssh.io.ByteArrayWriter;
import com.sshtools.j2ssh.io.UnsignedInteger32;
import com.sshtools.j2ssh.io.UnsignedInteger64;

/**
 * @author unascribed
 * @version 1.0
 */

public class SshFxpRead extends SubsystemMessage implements MessageRequestId {

  public static final int SSH_FXP_READ = 5;

  private UnsignedInteger32 id;
  private byte[] handle;
  private UnsignedInteger64 offset;
  private UnsignedInteger32 length;

  public SshFxpRead(UnsignedInteger32 id, byte[] handle,
                      UnsignedInteger64 offset, UnsignedInteger32 length) {
    super(SSH_FXP_READ);
    this.id = id;
    this.handle = handle;
    this.offset = offset;
    this.length = length;
  }

  public SshFxpRead() {
    super(SSH_FXP_READ);
  }

  public UnsignedInteger32 getId() {
    return id;
  }

  public byte[] getHandle() {
    return handle;
  }

  public UnsignedInteger64 getOffset() {
    return offset;
  }

  public UnsignedInteger32 getLength() {
    return length;
  }

  public void constructMessage(ByteArrayReader bar) throws java.io.IOException, com.sshtools.j2ssh.transport.InvalidMessageException {
    id = bar.readUINT32();
    handle = bar.readBinaryString();
    offset = bar.readUINT64();
    length = bar.readUINT32();
  }

  public String getMessageName() {
    return "SSH_FXP_READ";
  }

  public void constructByteArray(ByteArrayWriter baw) throws java.io.IOException, com.sshtools.j2ssh.transport.InvalidMessageException {
    baw.writeUINT32(id);
    baw.writeBinaryString(handle);
    baw.writeUINT64(offset);
    baw.writeUINT32(length);
  }
}
