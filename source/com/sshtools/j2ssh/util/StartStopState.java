package com.sshtools.j2ssh.util;

/**
 * @author unascribed
 * @version $Id$
 */

public class StartStopState extends State {

  public static final int STARTED = 1;
  public static final int STOPPED = 2;

  public StartStopState(int initial) {
    super(initial);
  }

  public boolean isValidState(int state) {
    return (state==STARTED) || (state==STOPPED);
  }
}
