package com.sshtools.j2ssh.sftp;

import com.sshtools.j2ssh.subsystem.*;

import com.sshtools.j2ssh.io.ByteArrayReader;
import com.sshtools.j2ssh.io.ByteArrayWriter;
import com.sshtools.j2ssh.io.UnsignedInteger32;
import com.sshtools.j2ssh.io.UnsignedInteger64;

/**
 * @author unascribed
 * @version 1.0
 */

public class SshFxpWrite extends SubsystemMessage implements MessageRequestId {

  public static final int SSH_FXP_WRITE = 6;

  private UnsignedInteger32 id;
  private byte[] handle;
  private UnsignedInteger64 offset;
  private byte data[];

  public SshFxpWrite() {
    super(SSH_FXP_WRITE);
  }

  public SshFxpWrite(UnsignedInteger32 id, byte[] handle,
                      UnsignedInteger64 offset, byte data[], int off, int len) {
    super(SSH_FXP_WRITE);
    this.id = id;
    this.handle = handle;
    this.offset = offset;
    this.data = new byte[len];
    System.arraycopy(data,off,this.data,0,len);
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

  public byte[] getData() {
    return data;
  }

  public void constructMessage(ByteArrayReader bar) throws java.io.IOException, com.sshtools.j2ssh.transport.InvalidMessageException {
    id = bar.readUINT32();
    handle = bar.readBinaryString();
    offset = bar.readUINT64();
    data = bar.readBinaryString();
  }
  public String getMessageName() {
    return "SSH_FXP_WRITE";
  }

  public void constructByteArray(ByteArrayWriter baw) throws java.io.IOException, com.sshtools.j2ssh.transport.InvalidMessageException {
    baw.writeUINT32(id);
    baw.writeBinaryString(handle);
    baw.writeUINT64(offset);
    baw.writeBinaryString(data);
  }
}
