package com.sshtools.j2ssh.util;

/**
 * @author unascribed
 * @version $Id$
 */

public class OpenClosedState extends State {

  public static final int OPEN = 1;
  public static final int CLOSED = 2;

  public OpenClosedState(int initial) {
    super(initial);
  }

  public boolean isValidState(int state) {
    return (state==OPEN) || (state==CLOSED);
  }
}
