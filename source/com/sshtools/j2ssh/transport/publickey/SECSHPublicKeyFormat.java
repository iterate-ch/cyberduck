/*
 * Sshtools - Java SSH2 API
 *
 * Copyright (C) 2002 Lee David Painter.
 *
 * Written by: 2002 Lee David Painter <lee@sshtools.com>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Library General Public License
 * as published by the Free Software Foundation; either version 2 of
 * the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Library General Public License for more details.
 *
 * You should have received a copy of the GNU Library General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package com.sshtools.j2ssh.transport.publickey;

/**
 * Implements the SECSH Public Key format as described in
 * draft-ietf-secsh-publickeyfile.txt
 *
 * @author <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 * @version $Id$
 */
public class SECSHPublicKeyFormat
    extends Base64EncodedFileFormat
    implements SshPublicKeyFormat {
    private static String BEGIN = "---- BEGIN SSH2 PUBLIC KEY ----";
    private static String END = "---- END SSH2 PUBLIC KEY ----";

    /**
     * Creates an SSH public key format instance
     *
     * @param subject The subject to write to file
     * @param comment The comment to write to file
     */
    public SECSHPublicKeyFormat(String subject, String comment) {
        super(BEGIN, END);

        setHeaderValue("Subject", subject);

        /**
         * The following extract from the SECSH Public Key File specification
         * "Currently, common practice is to quote the Header-value of the
         * Comment, and some existing implementations fail if these quotes are
         * omitted. Compliant implementations MUST function correctly if the
         * quotes are omitted. During an interim period implementations MAY
         * include the quotes. If the first and last characters of the
         * Header-value are matching quotes, implementations SHOULD remove
         * them before using the value." Most implementations I have tested on
         * still fail without quotes which is why I am including them at the
         * moment in this implementation
         */
        setComment(comment);
    }

    /**
     * Create an SSH public key format instance with default settings, used
     * typically to load a file.
     */
    public SECSHPublicKeyFormat() {
        super(BEGIN, END);
    }

    /**
     * Sets the comment
     *
     * @param comment the comment value
     */
    public void setComment(String comment) {
        setHeaderValue("Comment",
                       (comment.trim().startsWith("\"") ? "" : "\"")
                       + comment.trim()
                       + (comment.trim().endsWith("\"") ? "" : "\""));
    }

    /**
     * Gets the comment
     *
     * @return the comment value
     */
    public String getComment() {
        return getHeaderValue("Comment");
    }

    /**
     * Returns the format name for degugging purposes only
     *
     * @return "SECSH-PublicKey-Base64Encoded"
     */
    public String getFormatType() {
        return "SECSH-PublicKey-" + super.getFormatType();
    }

    /**
     * The SECSH Public key algorithm will support any installed public key as
     * the keyblob is encoded as specified in [SSH-TRANS]
     *
     * @param algorithm the algorithm name
     *
     * @return <tt>true</tt> for all supported and configured public key
     *         algorithms otherwise <tt>false</tt>
     */
    public boolean supportsAlgorithm(String algorithm) {
        return SshKeyPairFactory.supportsKey(algorithm);
    }
}
