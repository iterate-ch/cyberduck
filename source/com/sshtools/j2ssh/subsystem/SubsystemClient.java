package com.sshtools.j2ssh.subsystem;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;

import com.sshtools.j2ssh.io.ByteArrayReader;
import com.sshtools.j2ssh.io.ByteArrayWriter;

import com.sshtools.j2ssh.transport.InvalidMessageException;

import org.apache.log4j.Logger;

import com.sshtools.j2ssh.util.StartStopState;

import com.sshtools.j2ssh.configuration.ConfigurationLoader;

public abstract class SubsystemClient implements Runnable {

  private InputStream in;
  private OutputStream out;
  private Thread thread;
  private static Logger log = Logger.getLogger(SubsystemClient.class);
  private String name;
  private StartStopState state = new StartStopState(StartStopState.STOPPED);

  protected SubsystemMessageStore messageStore;

  public SubsystemClient(String name) {
    this.name = name;
    messageStore = new SubsystemMessageStore();
  }

  public SubsystemClient(String name, SubsystemMessageStore messageStore) {
    this.name = name;
    this.messageStore = messageStore;
  }

  public void setInputStream(InputStream in) {
    this.in = in;
  }

  public void setOutputStream(OutputStream out) {
    this.out = out;
  }

  public void start() throws IOException {
    thread = new Thread(this);
    if(ConfigurationLoader.isContextClassLoader())
              thread.setContextClassLoader(ConfigurationLoader.getContextClassLoader());
    thread.setDaemon(true);
    thread.start();
    onStart();
  }

  protected abstract void onStart() throws IOException;

  public String getName() {
    return name;
  }

  protected void sendMessage(SubsystemMessage msg)
                                      throws InvalidMessageException,
                                              IOException {
   log.info("Sending " + msg.getMessageName() + " subsystem message");

   byte msgdata[] = msg.toByteArray();
   // Write the message length
   out.write(ByteArrayWriter.encodeInt(msgdata.length));
   // Write the message data
   out.write(msgdata);
  }


  public void run() {

    int read;
    int len;
    int pos;
    byte buffer[] = new byte[4];
    byte msg[];

    state.setValue(StartStopState.STARTED);

    try {
      // read the first four bytes of data to determine the susbsytem
      // message length
      while(state.getValue()==StartStopState.STARTED) {
        read = in.read(buffer);

        if(read > 0) {
          len  = ByteArrayReader.readInt(buffer,0);
          msg = new byte[len];
          pos = 0;
          while(pos < len)
            pos += in.read(msg,pos,msg.length-pos);

          messageStore.addMessage(msg);
          msg = null;
       }
      }
    } catch(IOException ioe) {
      log.fatal("Subsystem message loop failed!", ioe);
    }

    thread = null;
  }

  public void stop() throws IOException {
    state.setValue(StartStopState.STOPPED);
    in.close();
  }

}
