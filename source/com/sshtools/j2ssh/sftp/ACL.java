package com.sshtools.j2ssh.sftp;

import java.io.IOException;

import com.sshtools.j2ssh.io.ByteArrayWriter;
import com.sshtools.j2ssh.io.ByteArrayReader;

public class ACL {

  public static final int ACE4_ACCESS_ALLOWED_ACE_TYPE = 0;
  public static final int ACE4_ACCESS_DENIED_ACE_TYPE = 1;
  public static final int ACE4_SYSTEM_AUDIT_ACE_TYPE = 2;
  public static final int ACE4_SYSTEM_ALARM_ACE_TYPE = 3;

  /**
   * Flags
   */
  public static final int ACE4_FILE_INHERIT_ACE = 0x00000001;
  public static final int ACE4_DIRECTORY_INHERIT_ACE = 0x00000002;
  public static final int ACE4_NO_PROPAGATE_INHERIT_ACE = 0x00000004;
  public static final int ACE4_INHERIT_ONLY_ACE = 0x00000008;
  public static final int ACE4_SUCCESSFUL_ACCESS_ACE_FLAG = 0x00000010;
  public static final int ACE4_FAILED_ACCESS_ACE_FLAG = 0x00000020;
  public static final int ACE4_IDENTIFIER_GROUP = 0x00000040;

  /**
   * Mask
   */
  public static final int ACE4_READ_DATA = 0x00000001;
  public static final int ACE4_WRITE_DATA = 0x00000002;
  public static final int ACE4_APPEND_DATA = 0x00000004;
  public static final int ACE4_READ_NAMED_ATTRS = 0x00000008;
  public static final int ACE4_WRITE_NAMED_ATTRS = 0x00000010;
  public static final int ACE4_EXECUTE = 0x00000020;
  public static final int ACE4_DELETE_CHILD = 0x00000040;
  public static final int ACE4_READ_ATTRIBUTES = 0x00000080;
  public static final int ACE4_WRITE_ATTRIBUTES = 0x00000100;
  public static final int ACE4_DELETE = 0x0001000;
  public static final int ACE4_READ_ACL = 0x0002000;
  public static final int ACE4_WRITE_ACL = 0x0004000;
  public static final int ACE4_WRITE_OWNER = 0x0008000;
  public static final int ACE4_SYNCHRONIZE = 0x0010000;


  private int type;
  private int flags = 0;
  private int masks = 0;
  String who;

  public ACL(int type) {
    if(type >= 0 && type <=4)
      this.type = type;
    else
      throw new IllegalArgumentException("Invalid ACL type!");
  }

  public ACL(ByteArrayReader bar) throws IOException {

    type = (int)bar.readInt();
    flags =(int)bar.readInt();
    masks = (int)bar.readInt();
    who = bar.readString();

  }

  public void setFlag(int flag) {
    flags |= flag;
  }

  public boolean isFlagSet(int flag) {
    return ((flags & flag)==flag);
  }

  public void setMask(int mask) {
    masks |= mask;
  }

  public boolean isMaskSet(int mask) {
    return ((masks & mask)==mask);
  }

  public void setWho(String who) {
    this.who = who;
  }

  public String getWho() {
    return who;
  }

  public byte[] toByteArray() throws IOException {
    ByteArrayWriter baw = new ByteArrayWriter();
    baw.writeInt(type);
    baw.writeInt(flags);
    baw.writeInt(masks);
    baw.writeString(who);
    return baw.toByteArray();

  }

}
