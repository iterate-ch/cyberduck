package com.sshtools.j2ssh.sftp;

import com.sshtools.j2ssh.subsystem.SubsystemMessage;

import com.sshtools.j2ssh.io.ByteArrayReader;
import com.sshtools.j2ssh.io.ByteArrayWriter;
import com.sshtools.j2ssh.io.UnsignedInteger32;

import java.util.Map;
import java.util.HashMap;
import java.util.Iterator;

/**
 * @author unascribed
 * @version 1.0
 */

public class SshFxpName extends SubsystemMessage implements MessageRequestId {

  public static final int SSH_FXP_NAME = 104;

  private UnsignedInteger32 id;
  private SftpFile files[];

  public SshFxpName(UnsignedInteger32 id, SftpFile files[]) {
    super(SSH_FXP_NAME);
    this.id = id;
    this.files = files;
  }

  public SshFxpName() {
    super(SSH_FXP_NAME);
  }

  public UnsignedInteger32 getId() {
    return id;
  }

  public SftpFile[] getFiles() {
    return files;
  }

  public void constructMessage(ByteArrayReader bar) throws java.io.IOException, com.sshtools.j2ssh.transport.InvalidMessageException {

    id = bar.readUINT32();
    UnsignedInteger32 count = bar.readUINT32();
    files = new SftpFile[count.intValue()];
    String shortname;
    String longname;
    for(int i=0;i<files.length;i++) {
      shortname = bar.readString();
      longname = bar.readString();
      files[i] = new SftpFile(shortname, new FileAttributes(bar));
    }
  }

  public String getMessageName() {
    return "SSH_FXP_NAME";
  }

  public void constructByteArray(ByteArrayWriter baw) throws java.io.IOException, com.sshtools.j2ssh.transport.InvalidMessageException {
    baw.writeUINT32(id);
    baw.writeInt(files.length);
    SftpFile file;
    for(int i=0;i<files.length;i++) {
      baw.writeString(files[i].getFilename());
      baw.writeString(files[i].getLongname());
      baw.write(files[i].getAttributes().toByteArray());
    }
  }
}
