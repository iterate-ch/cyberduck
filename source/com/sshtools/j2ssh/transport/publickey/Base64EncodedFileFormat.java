/*
 *  Sshtools - Java SSH2 API
 *
 *  Copyright (C) 2002 Lee David Painter.
 *
 *  Written by: 2002 Lee David Painter <lee@sshtools.com>
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Library General Public License
 *  as published by the Free Software Foundation; either version 2 of
 *  the License, or (at your option) any later version.
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Library General Public License for more details.
 *
 *  You should have received a copy of the GNU Library General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 */
package com.sshtools.j2ssh.transport.publickey;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import com.sshtools.j2ssh.io.ByteArrayWriter;
import com.sshtools.j2ssh.util.Base64;

/**
 *  Provides a Base 64 Encoded file format with headers<br>
 *  The file format is as follows:<br>
 *  <br>
 *  ----BEGIN----<br>
 *  Subject: user<br>
 *  Comment: type of file<br>
 *  AAA43EFFF.........BASE 64 ENCODED DATA........<br>
 *  ----END----<br>
 *
 *
 *@author     <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 *@created    20 December 2002
 *@version    $Id: Base64EncodedFileFormat.java,v 1.3 2002/12/10 00:07:32
 *      martianx Exp $
 */
public class Base64EncodedFileFormat
         implements SshKeyFormatConversion {
    /**
     *  the begin file format marker e.g ----BEGIN BASE64 ENCODED FILE----
     */
    protected String begin;

    /**
     *  the end file format marker e.g ----END BASE64 ENCODED FILE----
     */
    protected String end;
    private Map headers = new HashMap();
    private int MAX_LINE_LENGTH = 70;


    /**
     *  Creates a file format
     *
     *@param  begin  The ----BEGIN---- format tag
     *@param  end    The ----END---- format tag
     */
    protected Base64EncodedFileFormat(String begin, String end) {
        this.begin = begin;
        this.end = end;
    }


    /**
     *  Returns the format type for debugging
     *
     *@return    "Base64Encoded"
     */
    public String getFormatType() {
        return "Base64Encoded";
    }


    /**
     *  Sets a header value
     *
     *@param  headerTag    The header name
     *@param  headerValue  The header value
     */
    public void setHeaderValue(String headerTag, String headerValue) {
        headers.put(headerTag, headerValue);
    }


    /**
     *  Returns a header value
     *
     *@param  headerTag  The name of the header
     *@return            the header value
     */
    public String getHeaderValue(String headerTag) {
        return (String) headers.get(headerTag);
    }


    /**
     *  Gets the key blob from a formatted key
     *
     *@param  formattedKey             a byte array of file formatted data
     *@return                          the decoded data
     *@throws  InvalidSshKeyException  if the file format is invalid
     */
    public byte[] getKeyBlob(byte formattedKey[])
             throws InvalidSshKeyException {
        BufferedReader reader =
                new BufferedReader(new InputStreamReader(new ByteArrayInputStream(formattedKey)));

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
                            throw new InvalidSshKeyException("Incorrect file format!");
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
     *  Outputs the encoded key as a String
     *
     *@param  keyblob  the data to encoded in the file format
     *@return          The base 64 formatted data
     */
    public byte[] formatKey(byte keyblob[]) {
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
                    line =
                            header.substring(pos,
                            (((pos + MAX_LINE_LENGTH) < header.length())
                            ? (pos + MAX_LINE_LENGTH)
                            : header.length()))
                            + (((pos + MAX_LINE_LENGTH) < header.length()) ? "\\" : "");

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
