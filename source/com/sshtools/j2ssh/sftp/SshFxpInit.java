package com.sshtools.j2ssh.sftp;

import com.sshtools.j2ssh.subsystem.*;

import com.sshtools.j2ssh.io.ByteArrayReader;
import com.sshtools.j2ssh.io.ByteArrayWriter;
import com.sshtools.j2ssh.io.UnsignedInteger32;

import java.io.IOException;

import java.util.Map;
import java.util.Iterator;
import java.util.HashMap;

import com.sshtools.j2ssh.transport.InvalidMessageException;

public class SshFxpInit extends SubsystemMessage {

  public static final int SSH_FXP_INIT = 1;

  private UnsignedInteger32 version;
  private Map extended;

  public SshFxpInit() {
    super(SSH_FXP_INIT);
  }

  public SshFxpInit(UnsignedInteger32 version, Map extended) {
    super(SSH_FXP_INIT);
    this.version = version;
    this.extended = extended;
  }

  public UnsignedInteger32 getVersion() {
    return version;
  }

  public Map getExtended() {
    return extended;
  }

  public void constructMessage(ByteArrayReader bar) throws IOException,
                                                  InvalidMessageException {
    version = bar.readUINT32();
    extended = new HashMap();
    String key;
    String value;
    while(bar.available() > 0) {
      key = bar.readString();
      value = bar.readString();
      extended.put(key,value);
    }
  }

  public String getMessageName() {
    return "SSH_FXP_INIT";
  }

  public void constructByteArray(ByteArrayWriter baw) throws IOException,
                                                    InvalidMessageException {
    baw.writeUINT32(version);
    if(extended!=null) {
      if(extended.size() > 0) {
        Iterator it = extended.entrySet().iterator();
        Map.Entry entry;
        while(it.hasNext()) {
          entry = (Map.Entry)it.next();
          baw.writeString((String)entry.getKey());
          baw.writeString((String)entry.getValue());
        }
      }
    }
  }
}
