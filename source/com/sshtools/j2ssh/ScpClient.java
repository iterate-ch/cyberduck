/*
 *  SSHTools - Java SSH2 API
 *
 *  Copyright (C) 2002 Lee David Painter.
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Library General Public License
 *  as published by the Free Software Foundation; either version 2 of
 *  the License, or (at your option) any later version.
 *
 *  You may also distribute it and/or modify it under the terms of the
 *  Apache style J2SSH Software License. A copy of which should have
 *  been provided with the distribution.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  License document supplied with your distribution for more details.
 *
 */
package com.sshtools.j2ssh;

import java.io.BufferedInputStream;
import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import com.sshtools.j2ssh.configuration.ConfigurationLoader;
import com.sshtools.j2ssh.connection.ChannelEventListener;
import com.sshtools.j2ssh.session.SessionChannelClient;

/**
 * <p>
 * Implements a Secure Copy (SCP) client. This may be useful when the server
 * does not support SFTP.
 * </p>
 *
 * @author Lee David Painter
 * @version $Revision$
 *
 * @since 0.2.0
 */
public final class ScpClient {
  private SshClient ssh;
  private File cwd;
  private boolean verbose;
  private ChannelEventListener eventListener;
  /**
   * <p>
   * Creates an SCP client. CWD (Current working directory) will be the CWD
   * of the process that started this JVM.
   * </p>
   *
   * @param ssh A connected SshClient
   * @param verbose Output verbose detail
   * @param eventListener
   *
   * @since 0.2.0
   */
  public ScpClient(SshClient ssh, boolean verbose,
                   ChannelEventListener eventListener) {
    this(new File(ConfigurationLoader.checkAndGetProperty("user.dir", ".")),
         ssh, verbose, eventListener);
  }

  /**
   * <p>
   * Creates an SCP client.
   * </p>
   *
   * @param cwd The current local directory
   * @param ssh A connected SshClient
   * @param verbose Output verbose detail
   * @param eventListener
   *
   * @since 0.2.0
   */
  public ScpClient(File cwd, SshClient ssh, boolean verbose,
                   ChannelEventListener eventListener) {
    this.ssh = ssh;
    this.cwd = cwd;
    this.verbose = verbose;
    this.eventListener = eventListener;
  }

  /**
   * <p>
   * Uploads a <code>java.io.InputStream</code> to a remove server as file.
   * You <strong>must</strong> supply the correct number of bytes that will
   * be written.
   * </p>
   *
   * @param in stream providing file
   * @param length number of bytes that will be written
   * @param localFile local file name
   * @param remoteFile remote file name
   *
   * @throws IOException on any error
   */
  public void put(InputStream in, long length, String localFile,
                  String remoteFile) throws IOException {
    ScpChannel scp = new ScpChannel("scp -t " + (verbose ? "-v " : "")
                                    + remoteFile);
    scp.addEventListener(eventListener);
    if (!ssh.openChannel(scp)) {
      throw new IOException("Failed to open SCP channel");
    }
    scp.waitForResponse();
    scp.writeStreamToRemote(in, length, localFile);
    scp.close();
  }

  /**
   * <p>
   * Gets a remote file as an <code>java.io.InputStream</code>.
   * </p>
   *
   * @param remoteFile remote file name
   *
   * @return stream
   *
   * @throws IOException on any error
   */
  public InputStream get(String remoteFile) throws IOException {
    ScpChannel scp = new ScpChannel("scp " + "-f " + (verbose ? "-v " : "")
                                    + remoteFile);
    scp.addEventListener(eventListener);
    if (!ssh.openChannel(scp)) {
      throw new IOException("Failed to open SCP Channel");
    }
    return scp.readStreamFromRemote();
  }

  /**
   * <p>
   * Uploads a local file onto the remote server.
   * </p>
   *
   * @param localFile The path to the local file relative to the local
   *        current directory; may be a file or directory
   * @param remoteFile The path on the remote server, may be a file or
   *        directory
   * @param recursive Copy the contents of a directory recursivly
   *
   * @throws IOException if an IO error occurs during the operation
   *
   * @since 0.2.0
   */
  public void put(String localFile, String remoteFile, boolean recursive) throws
      IOException {
    File lf = new File(localFile);
    if (!lf.isAbsolute()) {
      lf = new File(cwd, localFile);
    }
    if (!lf.exists()) {
      throw new IOException(localFile + " does not exist");
    }
    if (!lf.isFile() && !lf.isDirectory()) {
      throw new IOException(localFile
                            + " is not a regular file or directory");
    }
    if (lf.isDirectory() && !recursive) {
      throw new IOException(localFile
                            + " is a directory, use recursive mode");
    }
    if ( (remoteFile == null) || remoteFile.equals("")) {
      remoteFile = ".";
    }
    ScpChannel scp = new ScpChannel("scp "
                                    + (lf.isDirectory() ? "-d " : "") + "-t "
                                    + (recursive ? "-r " : "") +
                                    (verbose ? "-v " : "")
                                    + remoteFile);
    scp.addEventListener(eventListener);
    if (!ssh.openChannel(scp)) {
      throw new IOException("Failed to open SCP channel");
    }
    scp.waitForResponse();
    scp.writeFileToRemote(lf, recursive);
    scp.close();
  }

  /**
   * <p>
   * Uploads an array of local files onto the remote server.
   * </p>
   *
   * @param localFiles an array of local files; may be files or directories
   * @param remoteFile the path on the remote server, may be a file or
   *        directory1
   * @param recursive Copy the contents of directorys recursivly
   *
   * @throws IOException if an IO error occurs during the operation
   *
   * @since 0.2.0
   */
  public void put(String[] localFiles, String remoteFile, boolean recursive) throws
      IOException {
    if ( (remoteFile == null) || remoteFile.equals("")) {
      remoteFile = ".";
    }
    if (localFiles.length == 1) {
      put(localFiles[0], remoteFile, recursive);
    }
    else {
      ScpChannel scp = new ScpChannel("scp " + "-d -t "
                                      + (recursive ? "-r " : "") +
                                      (verbose ? "-v " : "")
                                      + remoteFile);
      scp.addEventListener(eventListener);
      if (!ssh.openChannel(scp)) {
        throw new IOException("Failed to open SCP channel");
      }
      scp.waitForResponse();
      for (int i = 0; i < localFiles.length; i++) {
        File lf = new File(localFiles[i]);
        if (!lf.isAbsolute()) {
          lf = new File(cwd, localFiles[i]);
        }
        if (!lf.isFile() && !lf.isDirectory()) {
          throw new IOException(lf.getName()
                                + " is not a regular file or directory");
        }
        scp.writeFileToRemote(lf, recursive);
      }
      scp.close();
    }
  }

  /**
   * <p>
   * Downloads an array of remote files to the local computer.
   * </p>
   *
   * @param localFile The local path to place the files
   * @param remoteFiles The path of the remote files
   * @param recursive recursivly copy the contents of a directory
   *
   * @throws IOException if an IO error occurs during the operation
   *
   * @since 0.2.0
   */
  public void get(String localFile, String[] remoteFiles, boolean recursive) throws
      IOException {
    StringBuffer buf = new StringBuffer();
    for (int i = 0; i < remoteFiles.length; i++) {
      buf.append("\"");
      buf.append(remoteFiles[i]);
      buf.append("\" ");
    }
    String remoteFile = buf.toString();
    remoteFile = remoteFile.trim();
    get(localFile, remoteFile, recursive);
  }

  /**
   * <p>
   * Downloads a remote file onto the local computer.
   * </p>
   *
   * @param localFile The path to place the file
   * @param remoteFile The path of the file on the remote server
   * @param recursive recursivly copy the contents of a directory
   *
   * @throws IOException if an IO error occurs during the operation
   *
   * @since 0.2.0
   */
  public void get(String localFile, String remoteFile, boolean recursive) throws
      IOException {
    if ( (localFile == null) || localFile.equals("")) {
      localFile = ".";
    }
    File lf = new File(localFile);
    if (!lf.isAbsolute()) {
      lf = new File(cwd, localFile);
    }
    if (lf.exists() && !lf.isFile() && !lf.isDirectory()) {
      throw new IOException(localFile
                            + " is not a regular file or directory");
    }
    ScpChannel scp = new ScpChannel("scp " + "-f "
                                    + (recursive ? "-r " : "") +
                                    (verbose ? "-v " : "")
                                    + remoteFile);
    scp.addEventListener(eventListener);
    if (!ssh.openChannel(scp)) {
      throw new IOException("Failed to open SCP Channel");
    }
    scp.readFromRemote(lf);
    scp.close();
  }

  /**
   * <p>
   * Implements an SCP channel by extending the
   * <code>SessionChannelClient</code>.
   * </p>
   *
   * @since 0.2.0
   */
  class ScpChannel
      extends SessionChannelClient {
    byte[] buffer = new byte[16384];
    String cmd;
    /**
     * <p>
     * Contruct the channel with the specified scp command.
     * </p>
     *
     * @param cmd The scp command
     *
     * @since 0.2.0
     */
    ScpChannel(String cmd) {
      this.cmd = cmd;
      setName("scp");
    }

    /**
     * <p>
     * This implementation executes the scp command when the channel is
     * opened.
     * </p>
     *
     * @throws IOException
     *
     * @since 0.2.0
     */
    protected void onChannelOpen() throws IOException {
      if (!executeCommand(cmd)) {
        throw new IOException("Failed to execute the command " + cmd);
      }
    }

    /**
     * <p>
     * Writes a directory to the remote server.
     * </p>
     *
     * @param dir The source directory
     * @param recursive Add the contents of the directory recursivley
     *
     * @return true if the file was written, otherwise false
     *
     * @throws IOException if an IO error occurs
     *
     * @since 0.2.0
     */
    private boolean writeDirToRemote(File dir, boolean recursive) throws
        IOException {
      if (!recursive) {
        writeError("File " + dir.getName()
                   + " is a directory, use recursive mode");
        return false;
      }
      String cmd = "D0755 0 " + dir.getName() + "\n";
      out.write(cmd.getBytes());
      waitForResponse();
      String[] list = dir.list();
      for (int i = 0; i < list.length; i++) {
        File f = new File(dir, list[i]);
        writeFileToRemote(f, recursive);
      }
      out.write("E\n".getBytes());
      return true;
    }

    /**
     * <p>
     * Write a stream as a file to the remote server. You
     * <strong>must</strong> supply the correct number of bytes that will
     * be written.
     * </p>
     *
     * @param in stream
     * @param length number of bytes to write
     * @param localName local file name
     *
     * @throws IOException if an IO error occurs
     *
     * @since 0.2.0
     */
    private void writeStreamToRemote(InputStream in, long length,
                                     String localName) throws IOException {
      String cmd = "C0644 " + length + " " + localName + "\n";
      out.write(cmd.getBytes());
      waitForResponse();
      writeCompleteFile(in, length);
      writeOk();
      waitForResponse();
    }

    /**
     * <p>
     * Write a file to the remote server.
     * </p>
     *
     * @param file The source file
     * @param recursive Add the contents of the directory recursivley
     *
     * @throws IOException if an IO error occurs
     *
     * @since 0.2.0
     */
    private void writeFileToRemote(File file, boolean recursive) throws
        IOException {
      if (file.isDirectory()) {
        if (!writeDirToRemote(file, recursive)) {
          return;
        }
      }
      else if (file.isFile()) {
        String cmd = "C0644 " + file.length() + " " + file.getName()
            + "\n";
        out.write(cmd.getBytes());
        waitForResponse();
        FileInputStream fi = new FileInputStream(file);
        writeCompleteFile(fi, file.length());
        writeOk();
      }
      else {
        throw new IOException(file.getName() + " not valid for SCP");
      }
      waitForResponse();
    }

    private void readFromRemote(File file) throws IOException {
      String cmd;
      String[] cmdParts = new String[3];
      writeOk();
      while (true) {
        try {
          cmd = readString();
        }
        catch (EOFException e) {
          return;
        }
        char cmdChar = cmd.charAt(0);
        switch (cmdChar) {
          case 'E':
            writeOk();
            return;
          case 'T':
            throw new IOException("SCP time not supported: " + cmd);
          case 'C':
          case 'D':
            String targetName = file.getAbsolutePath();
            parseCommand(cmd, cmdParts);
            if (file.isDirectory()) {
              targetName += (File.separator + cmdParts[2]);
            }
            File targetFile = new File(targetName);
            if (cmdChar == 'D') {
              if (targetFile.exists()) {
                if (!targetFile.isDirectory()) {
                  String msg = "Invalid target "
                      + targetFile.getName()
                      + ", must be a directory";
                  writeError(msg);
                  throw new IOException(msg);
                }
              }
              else {
                if (!targetFile.mkdir()) {
                  String msg = "Could not create directory: "
                      + targetFile.getName();
                  writeError(msg);
                  throw new IOException(msg);
                }
              }
              readFromRemote(targetFile);
              continue;
            }
            FileOutputStream fo = new FileOutputStream(targetFile);
            writeOk();
            long len = Long.parseLong(cmdParts[1]);
            readCompleteFile(fo, len);
            waitForResponse();
            writeOk();
            break;
          default:
            writeError("Unexpected cmd: " + cmd);
            throw new IOException("SCP unexpected cmd: " + cmd);
        }
      }
    }

    private InputStream readStreamFromRemote() throws IOException {
      String cmd;
      String[] cmdParts = new String[3];
      writeOk();
      try {
        cmd = readString();
      }
      catch (EOFException e) {
        return null;
      }
      char cmdChar = cmd.charAt(0);
      switch (cmdChar) {
        case 'E':
          writeOk();
          return null;
        case 'T':
          throw new IOException("SCP time not supported: " + cmd);
        case 'D':
          throw new IOException(
              "Directories cannot be copied to a stream");
        case 'C':
          parseCommand(cmd, cmdParts);
          writeOk();
          long len = Long.parseLong(cmdParts[1]);
          return new BufferedInputStream(new ScpInputStream(len, in, this),
                                         16 * 1024);
        default:
          writeError("Unexpected cmd: " + cmd);
          throw new IOException("SCP unexpected cmd: " + cmd);
      }
    }

    private void parseCommand(String cmd, String[] cmdParts) throws IOException {
      int l;
      int r;
      l = cmd.indexOf(' ');
      r = cmd.indexOf(' ', l + 1);
      if ( (l == -1) || (r == -1)) {
        writeError("Syntax error in cmd");
        throw new IOException("Syntax error in cmd");
      }
      cmdParts[0] = cmd.substring(1, l);
      cmdParts[1] = cmd.substring(l + 1, r);
      cmdParts[2] = cmd.substring(r + 1);
    }

    private String readString() throws IOException {
      int ch;
      int i = 0;
      while ( ( (ch = in.read()) != ( (int) '\n')) && (ch >= 0)) {
        buffer[i++] = (byte) ch;
      }
      if (ch == -1) {
        throw new EOFException("SCP returned unexpected EOF");
      }
      if (buffer[0] == (byte) '\n') {
        throw new IOException("Unexpected <NL>");
      }
      if ( (buffer[0] == (byte) '\02') || (buffer[0] == (byte) '\01')) {
        String msg = new String(buffer, 1, i - 1);
        if (buffer[0] == (byte) '\02') {
          throw new IOException(msg);
        }
        throw new IOException("SCP returned an unexpected error: "
                              + msg);
      }
      return new String(buffer, 0, i);
    }

    private void waitForResponse() throws IOException {
      int r = in.read();
      if (r == 0) {
        // All is well, no error
        return;
      }
      if (r == -1) {
        throw new EOFException("SCP returned unexpected EOF");
      }
      String msg = readString();
      if (r == (byte) '\02') {
        throw new IOException(msg);
      }
      throw new IOException("SCP returned an unexpected error: " + msg);
    }

    private void writeOk() throws IOException {
      out.write(0);
    }

    private void writeError(String reason) throws IOException {
      out.write(1);
      out.write(reason.getBytes());
    }

    private void writeCompleteFile(InputStream file, long size) throws
        IOException {
      int count = 0;
      int read;
      try {
        while (count < size) {
          read = file.read(buffer, 0,
                           (int) ( ( (size - count) < buffer.length)
                                  ? (size - count) : buffer.length));
          if (read == -1) {
            throw new EOFException("SCP received an unexpected EOF");
          }
          count += read;
          out.write(buffer, 0, read);
        }
      }
      finally {
        file.close();
      }
    }

    private void readCompleteFile(FileOutputStream file, long size) throws
        IOException {
      int count = 0;
      int read;
      try {
        while (count < size) {
          read = in.read(buffer, 0,
                         (int) ( ( (size - count) < buffer.length)
                                ? (size - count) : buffer.length));
          if (read == -1) {
            throw new EOFException("SCP received an unexpected EOF");
          }
          count += read;
          file.write(buffer, 0, read);
        }
      }
      finally {
        file.close();
      }
    }
  }

  class ScpInputStream
      extends InputStream {
    long length;
    InputStream in;
    long count;
    ScpChannel channel;
    ScpInputStream(long length, InputStream in, ScpChannel channel) {
      this.length = length;
      this.in = in;
      this.channel = channel;
    }

    public int read() throws IOException {
      if (count == length) {
        return -1;
      }
      if (count >= length) {
        throw new EOFException("End of file.");
      }
      int r = in.read();
      if (r == -1) {
        throw new EOFException("Unexpected EOF.");
      }
      count++;
      if (count == length) {
        channel.waitForResponse();
        channel.writeOk();
      }
      return r;
    }

    public void close() throws IOException {
      channel.close();
    }
  }
}
