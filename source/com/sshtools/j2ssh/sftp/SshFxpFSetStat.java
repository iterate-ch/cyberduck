package com.sshtools.j2ssh.sftp;
import com.sshtools.j2ssh.subsystem.SubsystemMessage;

import com.sshtools.j2ssh.io.ByteArrayReader;
import com.sshtools.j2ssh.io.ByteArrayWriter;
import com.sshtools.j2ssh.io.UnsignedInteger32;

/**
 * @author unascribed
 * @version $Id$
 */

public class SshFxpFSetStat extends SubsystemMessage implements MessageRequestId {

  public static final int SSH_FXP_FSETSTAT = 10;

  private UnsignedInteger32 id;
  private byte[] handle;
  private FileAttributes attrs;

  public SshFxpFSetStat() {
    super(SSH_FXP_FSETSTAT);
  }

  public SshFxpFSetStat(UnsignedInteger32 id, byte[] handle, FileAttributes attrs) {
    super(SSH_FXP_FSETSTAT);
    this.id = id;
    this.handle = handle;
    this.attrs = attrs;
  }

  public UnsignedInteger32 getId() {
    return id;
  }

  public byte[] getHandle() {
    return handle;
  }

  public FileAttributes getAttributes() {
    return attrs;
  }

  public void constructMessage(ByteArrayReader bar) throws java.io.IOException, com.sshtools.j2ssh.transport.InvalidMessageException {
    id = bar.readUINT32();
    handle = bar.readBinaryString();
    attrs = new FileAttributes(bar);
  }

  public String getMessageName() {
    return "SSH_FXP_FSETSTAT";
  }

  public void constructByteArray(ByteArrayWriter baw) throws java.io.IOException, com.sshtools.j2ssh.transport.InvalidMessageException {
    baw.writeUINT32(id);
    baw.writeBinaryString(handle);
    baw.write(attrs.toByteArray());
  }
}
