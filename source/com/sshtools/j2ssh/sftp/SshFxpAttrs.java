package com.sshtools.j2ssh.sftp;

import com.sshtools.j2ssh.subsystem.SubsystemMessage;

import com.sshtools.j2ssh.io.ByteArrayReader;
import com.sshtools.j2ssh.io.ByteArrayWriter;
import com.sshtools.j2ssh.io.UnsignedInteger32;


/**
 * @author unascribed
 * @version 1.0
 */

public class SshFxpAttrs extends SubsystemMessage implements MessageRequestId {

  public static final int SSH_FXP_ATTRS = 105;

  private UnsignedInteger32 id;
  private FileAttributes attrs;

  public SshFxpAttrs() {
    super(SSH_FXP_ATTRS);
  }

  public SshFxpAttrs(UnsignedInteger32 id, FileAttributes attrs) {
    super(SSH_FXP_ATTRS);
    this.id = id;
    this.attrs = attrs;
  }

  public UnsignedInteger32 getId() {
    return id;
  }

  public FileAttributes getAttributes() {
    return attrs;
  }

  public void constructMessage(ByteArrayReader bar) throws java.io.IOException, com.sshtools.j2ssh.transport.InvalidMessageException {
    id = bar.readUINT32();
    attrs = new FileAttributes(bar);
  }
  public String getMessageName() {
    return "SSH_FXP_ATTRS";
  }

  public void constructByteArray(ByteArrayWriter baw) throws java.io.IOException, com.sshtools.j2ssh.transport.InvalidMessageException {
    baw.writeUINT32(id);
    baw.write(attrs.toByteArray());
  }
}
