package com.enterprisedt.net.ftp;

import java.io.IOException;

/**
 * @version $Id$
 */
public class FTPNullReplyException extends FTPException {

    public FTPNullReplyException() {
        super("Unexpected null reply received");
    }

    public FTPNullReplyException(String message) {
        super(message);
    }

}
