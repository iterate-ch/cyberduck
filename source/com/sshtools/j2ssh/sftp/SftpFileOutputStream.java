package com.sshtools.j2ssh.sftp;

import java.io.*;

import com.sshtools.j2ssh.io.UnsignedInteger64;

/**
 * @author unascribed
 * @version 1.0
 */

public class SftpFileOutputStream extends OutputStream {

  SftpFile file;
  UnsignedInteger64 position = new UnsignedInteger64("0");

  public SftpFileOutputStream(SftpFile file) throws IOException {

    if(file.getHandle()==null)
      throw new IOException("The file does not have a valid handle!");
    if(file.getSFTPSubsystem()==null)
      throw new IOException("The file is not attached to an SFTP subsystem!");

    this.file = file;

  }

  public void write(byte buffer[], int offset, int len) throws IOException {
    file.getSFTPSubsystem().writeFile(file.getHandle(), position, buffer, offset, len);
    position = UnsignedInteger64.add(position, len);
  }

  public void write(int b) throws IOException {
    byte buffer[] = new byte[1];
    buffer[0] = (byte)b;
    file.getSFTPSubsystem().writeFile(file.getHandle(), position, buffer, 0, 1);
    position = UnsignedInteger64.add(position, 1);
  }

  public void close() throws IOException {
    file.getSFTPSubsystem().closeFile(file);
  }

  protected void finalize() throws IOException {
    if(file.getHandle()!=null)
      close();
  }


}
