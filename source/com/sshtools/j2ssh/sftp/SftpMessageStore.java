package com.sshtools.j2ssh.sftp;

import com.sshtools.j2ssh.subsystem.SubsystemMessageStore;
import com.sshtools.j2ssh.subsystem.SubsystemMessage;
import com.sshtools.j2ssh.io.UnsignedInteger32;
import java.util.Iterator;
import com.sshtools.j2ssh.util.OpenClosedState;

/**
 * @author unascribed
 * @version 1.0
 */

public class SftpMessageStore extends SubsystemMessageStore {

  public SftpMessageStore() {
  }

  /**
   * Wait and get a message from the message store with the required request id.
   * To support this feature the message class must implement the MessageRequestId
   * interface.
   * @param requestId the request id
   * @return  the message
   */
  public synchronized SubsystemMessage getMessage(UnsignedInteger32 requestId) {

        Iterator it;
        SubsystemMessage msg;
       // If there ae no messages available then wait untill there are.
       while(getState().getValue()==OpenClosedState.OPEN) {
          if(messages.size() > 0) {
            it = messages.iterator();
            while(it.hasNext()) {
              msg = (SubsystemMessage)it.next();
              if(msg instanceof MessageRequestId) {
                if(((MessageRequestId)msg).getId().equals(requestId)) {
                  messages.remove(msg);
                  return msg;
                }
              }
            }
          }
          try {
            wait();
          } catch(InterruptedException e) {
          }
      }

      return null;
  }
}
