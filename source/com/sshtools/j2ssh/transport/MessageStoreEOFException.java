package com.sshtools.j2ssh.transport;

/**
 * @author unascribed
 * @version 1.0
 */

public class MessageStoreEOFException extends TransportProtocolException {

  public MessageStoreEOFException() {
    super("The message store has reached EOF");
  }
}
