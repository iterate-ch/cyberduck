/*
 *  SSHTools - Java SSH2 API
 *
 *  Copyright (C) 2002-2003 Lee David Painter and Contributors.
 *
 *  Contributions made by:
 *
 *  Brett Smith
 *  Richard Pernavas
 *  Erwin Bolwidt
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Library General Public License
 *  as published by the Free Software Foundation; either version 2 of
 *  the License, or (at your option) any later version.
 *
 *  You may also distribute it and/or modify it under the terms of the
 *  Apache style J2SSH Software License. A copy of which should have
 *  been provided with the distribution.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  License document supplied with your distribution for more details.
 *
 */
package com.sshtools.j2ssh.transport.publickey;


/**
 *
 *
 * @author $author$
 * @version $Revision$
 */
public class SECSHPublicKeyFormat extends Base64EncodedFileFormat
    implements SshPublicKeyFormat {
    private static String BEGIN = "---- BEGIN SSH2 PUBLIC KEY ----";
    private static String END = "---- END SSH2 PUBLIC KEY ----";

    /**
     * Creates a new SECSHPublicKeyFormat object.
     *
     * @param subject
     * @param comment
     */
    public SECSHPublicKeyFormat(String subject, String comment) {
        super(BEGIN, END);
        setHeaderValue("Subject", subject);
        setComment(comment);
    }

    /**
     * Creates a new SECSHPublicKeyFormat object.
     */
    public SECSHPublicKeyFormat() {
        super(BEGIN, END);
    }

    /**
     *
     *
     * @param comment
     */
    public void setComment(String comment) {
        setHeaderValue("Comment",
            (comment.trim().startsWith("\"") ? "" : "\"") + comment.trim() +
            (comment.trim().endsWith("\"") ? "" : "\""));
    }

    /**
     *
     *
     * @return
     */
    public String getComment() {
        return getHeaderValue("Comment");
    }

    /**
     *
     *
     * @return
     */
    public String getFormatType() {
        return "SECSH-PublicKey-" + super.getFormatType();
    }

    /**
     *
     *
     * @param algorithm
     *
     * @return
     */
    public boolean supportsAlgorithm(String algorithm) {
        return SshKeyPairFactory.supportsKey(algorithm);
    }
}
