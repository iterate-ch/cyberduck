package com.sshtools.j2ssh.transport;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2002</p>
 * <p>Company: </p>
 * @author unascribed
 * @version 1.0
 */

public class MessageNotAvailableException extends Exception {

  public MessageNotAvailableException() {
  super("The message is not available");
  }
}
