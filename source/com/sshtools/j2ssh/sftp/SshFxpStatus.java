package com.sshtools.j2ssh.sftp;

import com.sshtools.j2ssh.subsystem.SubsystemMessage;

import com.sshtools.j2ssh.io.ByteArrayReader;
import com.sshtools.j2ssh.io.ByteArrayWriter;
import com.sshtools.j2ssh.io.UnsignedInteger32;


/**
 * @author unascribed
 * @version 1.0
 */

public class SshFxpStatus extends SubsystemMessage implements MessageRequestId {

  public static final int SSH_FXP_STATUS = 101;

  private UnsignedInteger32 id;
  private UnsignedInteger32 errorCode;
  private String errorMessage;
  private String languageTag;

  public static final int STATUS_FX_OK = 0;
  public static final int STATUS_FX_EOF = 1;
  public static final int STATUS_FX_NO_SUCH_FILE = 2;
  public static final int STATUS_FX_PERMISSION_DENIED = 3;
  public static final int STATUS_FX_FAILURE = 4;
  public static final int STATUS_FX_BAD_MESSAGE = 5;
  public static final int STATUS_FX_NO_CONNECTION = 6;
  public static final int STATUS_FX_CONNECTION_LOST = 7;
  public static final int STATUS_FX_OP_UNSUPPORTED = 8;
  public static final int STATUS_FX_INVALID_HANDLE = 9;
  public static final int STATUS_FX_NO_SUCH_PATH = 10;
  public static final int STATUS_FX_FILE_ALREADY_EXISTS = 11;
  public static final int STATUS_FX_WRITE_PROTECT = 12;


  public SshFxpStatus(UnsignedInteger32 id, UnsignedInteger32 errorCode,
                        String errorMessage, String languageTag) {
    super(SSH_FXP_STATUS);
    this.id = id;
    this.errorCode = errorCode;
    this.errorMessage = errorMessage;
    this.languageTag = languageTag;
  }

  public SshFxpStatus() {
    super(SSH_FXP_STATUS);
  }

  public UnsignedInteger32 getId() {
    return id;
  }

  public UnsignedInteger32 getErrorCode() {
    return errorCode;
  }

  public String getErrorMessage() {
    return errorMessage;
  }

  public String getLanguageTag() {
    return languageTag;
  }

  public void constructMessage(ByteArrayReader bar) throws java.io.IOException, com.sshtools.j2ssh.transport.InvalidMessageException {
    id = bar.readUINT32();
    errorCode = bar.readUINT32();
    errorMessage = bar.readString();
    languageTag = bar.readString();
  }
  public String getMessageName() {
    return "SSH_FXP_STATUS";
  }
  public void constructByteArray(ByteArrayWriter baw) throws java.io.IOException, com.sshtools.j2ssh.transport.InvalidMessageException {
    baw.writeUINT32(id);
    baw.writeUINT32(errorCode);
    baw.writeString(errorMessage);
    baw.writeString(languageTag);
  }
}
