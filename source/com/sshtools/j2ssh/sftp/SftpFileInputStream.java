package com.sshtools.j2ssh.sftp;

import java.io.*;

import com.sshtools.j2ssh.io.UnsignedInteger64;

/**
 * @author unascribed
 * @version 1.0
 */

public class SftpFileInputStream extends InputStream {

  SftpFile file;
  UnsignedInteger64 position = new UnsignedInteger64("0");
  public SftpFileInputStream(SftpFile file) throws IOException {
    if(file.getHandle()==null)
      throw new IOException("The file does not have a valid handle!");
    if(file.getSFTPSubsystem()==null)
      throw new IOException("The file is not attached to an SFTP subsystem!");

    this.file = file;

  }

  public int read(byte buffer[], int offset, int len) throws IOException {
    int read = file.getSFTPSubsystem().readFile(file.getHandle(),
                                          position,
                                          buffer,
                                          offset,
                                          len);
    position = UnsignedInteger64.add(position, read);
    return read;

  }


  public int read() throws java.io.IOException {
    byte buffer[] = new byte[1];
    int read = file.getSFTPSubsystem().readFile(file.getHandle(),
                                          position,
                                          buffer,
                                          0,
                                          1);
    position = UnsignedInteger64.add(position, read);
    return buffer[0];

  }

  public void close() throws IOException {
    file.getSFTPSubsystem().closeFile(file);
  }

  protected void finalize() throws IOException {
    if(file.getHandle()!=null)
      close();
  }

}
