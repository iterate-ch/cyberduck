package com.sshtools.j2ssh.sftp;

import com.sshtools.j2ssh.subsystem.SubsystemMessage;

import com.sshtools.j2ssh.io.ByteArrayReader;
import com.sshtools.j2ssh.io.ByteArrayWriter;
import com.sshtools.j2ssh.io.UnsignedInteger32;

import java.io.IOException;

import com.sshtools.j2ssh.transport.InvalidMessageException;

public class SshFxpOpen extends SubsystemMessage implements MessageRequestId {

  public static final int SSH_FXP_OPEN = 3;

  private UnsignedInteger32 id;
  private String filename;
  private UnsignedInteger32 pflags;
  private FileAttributes attrs;

  public static final int FXF_READ = 0x00000001;
  public static final int FXF_WRITE = 0x00000002;
  public static final int FXF_APPEND = 0x00000004;
  public static final int FXF_CREAT = 0x00000008;
  public static final int FXF_TRUNC = 0x00000010;
  public static final int FXF_EXCL = 0x00000020;
  //public static final int FXF_TEXT = 0x00000040;

  public SshFxpOpen(UnsignedInteger32 id, String filename,
                      UnsignedInteger32 pflags, FileAttributes attrs) {
   super(SSH_FXP_OPEN);
   this.id = id;
   this.filename = filename;
   this.pflags = pflags;
   this.attrs = attrs;
  }

  public SshFxpOpen() {
    super(SSH_FXP_OPEN);
  }

  public UnsignedInteger32 getId() {
    return id;
  }

  public String getFilename() {
    return filename;
  }

  public UnsignedInteger32 getPflags() {
    return pflags;
  }

  public FileAttributes getAttributes() {
    return attrs;
  }

  public void constructMessage(ByteArrayReader bar) throws IOException,
                                                  InvalidMessageException {
    id = bar.readUINT32();
    filename = bar.readString();
    pflags = bar.readUINT32();
    attrs = new FileAttributes(bar);
  }

  public String getMessageName() {
    return "SSH_FXP_OPEN";
  }

  public void constructByteArray(ByteArrayWriter baw) throws IOException,
                                                    InvalidMessageException {

      baw.writeUINT32(id);
      baw.writeString(filename);
      baw.writeUINT32(pflags);
      baw.writeBinaryString(attrs.toByteArray());
  }
}
