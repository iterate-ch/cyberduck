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

import com.sshtools.j2ssh.io.ByteArrayWriter;
import com.sshtools.j2ssh.util.Base64;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;


/**
 *
 *
 * @author $author$
 * @version $Revision$
 */
public abstract class Base64EncodedFileFormat implements SshKeyFormatConversion {
    /**  */
    protected String begin;

    /**  */
    protected String end;
    private Map headers = new HashMap();
    private int MAX_LINE_LENGTH = 70;

    /**
     * Creates a new Base64EncodedFileFormat object.
     *
     * @param begin
     * @param end
     */
    protected Base64EncodedFileFormat(String begin, String end) {
        this.begin = begin;
        this.end = end;
    }

    /**
     *
     *
     * @return
     */
    public String getFormatType() {
        return "Base64Encoded";
    }

    /**
     *
     *
     * @param formattedKey
     *
     * @return
     */
    public boolean isFormatted(byte[] formattedKey) {
        String test = new String(formattedKey);

        if ((test.indexOf(begin) >= 0) && (test.indexOf(end) > 0)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     *
     *
     * @param headerTag
     * @param headerValue
     */
    public void setHeaderValue(String headerTag, String headerValue) {
        headers.put(headerTag, headerValue);
    }

    /**
     *
     *
     * @param headerTag
     *
     * @return
     */
    public String getHeaderValue(String headerTag) {
        return (String) headers.get(headerTag);
    }

    /**
     *
     *
     * @param formattedKey
     *
     * @return
     *
     * @throws InvalidSshKeyException
     */
    public byte[] getKeyBlob(byte[] formattedKey) throws InvalidSshKeyException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                    new ByteArrayInputStream(formattedKey)));
        String line;
        String headerTag;
        String headerValue;
        String blob = "";
        int index;

        try {
            // Read in the lines looking for the start
            while (true) {
                line = reader.readLine();

                if (line == null) {
                    throw new InvalidSshKeyException("Incorrect file format!");
                }

                if (line.endsWith(begin)) {
                    break;
                }
            }

            // Read the headers
            while (true) {
                line = reader.readLine();

                if (line == null) {
                    throw new InvalidSshKeyException("Incorrect file format!");
                }

                index = line.indexOf(": ");

                if (index > 0) {
                    while (line.endsWith("\\")) {
                        line = line.substring(0, line.length() - 1);

                        String tmp = reader.readLine();

                        if (tmp == null) {
                            throw new InvalidSshKeyException(
                                "Incorrect file format!");
                        }

                        line += tmp;
                    }

                    // Record the header
                    headerTag = line.substring(0, index);
                    headerValue = line.substring(index + 2);
                    headers.put(headerTag, headerValue);
                } else {
                    break;
                }
            }

            // This is now the public key blob Base64 encoded
            ByteArrayWriter baw = new ByteArrayWriter();

            while (true) {
                blob += line;
                line = reader.readLine();

                if (line == null) {
                    throw new InvalidSshKeyException("Invalid file format!");
                }

                if (line.endsWith(end)) {
                    break;
                }
            }

            // Convert the blob to some useful data
            return Base64.decode(blob);
        } catch (IOException ioe) {
            throw new InvalidSshKeyException();
        }
    }

    /**
     *
     *
     * @param keyblob
     *
     * @return
     */
    public byte[] formatKey(byte[] keyblob) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            String headerTag;
            String headerValue;
            String line;
            out.write(begin.getBytes());
            out.write('\n');

            int pos;
            Set tags = headers.keySet();
            Iterator it = tags.iterator();

            while (it.hasNext()) {
                headerTag = (String) it.next();
                headerValue = (String) headers.get(headerTag);

                String header = headerTag + ": " + headerValue;
                pos = 0;

                while (pos < header.length()) {
                    line = header.substring(pos,
                            (((pos + MAX_LINE_LENGTH) < header.length())
                            ? (pos + MAX_LINE_LENGTH) : header.length())) +
                        (((pos + MAX_LINE_LENGTH) < header.length()) ? "\\" : "");
                    out.write(line.getBytes());
                    out.write('\n');
                    pos += MAX_LINE_LENGTH;
                }
            }

            String encoded = Base64.encodeBytes(keyblob, false);
            out.write(encoded.getBytes());
            out.write('\n');
            out.write(end.getBytes());
            out.write('\n');

            return out.toByteArray();
        } catch (IOException ioe) {
            return null;
        }
    }
}
