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
package com.sshtools.j2ssh.configuration;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

/**
 * DOCUMENT ME!
 *
 * @author <A HREF="mailto:lee@sshtools.com">Lee David Painter</A>
 * @version $Id$
 */
public class AuthorizedKeys
    extends DefaultHandler {
    private List authorizedKeys = new ArrayList();
    private String currentElement = null;

    /**
     * Creates a new AuthorizedKeys object.
     *
     * @param in DOCUMENT ME!
     *
     * @throws SAXException DOCUMENT ME!
     * @throws ParserConfigurationException DOCUMENT ME!
     * @throws IOException DOCUMENT ME!
     */
    public AuthorizedKeys(InputStream in)
                   throws SAXException, ParserConfigurationException,
                          IOException {
        SAXParserFactory saxFactory = SAXParserFactory.newInstance();
        SAXParser saxParser = saxFactory.newSAXParser();
        authorizedKeys.clear();
        saxParser.parse(in, this);
    }

    /**
     * DOCUMENT ME!
     *
     * @param ch DOCUMENT ME!
     * @param start DOCUMENT ME!
     * @param length DOCUMENT ME!
     *
     * @throws SAXException DOCUMENT ME!
     */
    public void characters(char ch[], int start, int length)
                    throws SAXException {
        if (currentElement!=null) {
            if (currentElement.equals("Key")) {
                String key = new String(ch, start, length);
                authorizedKeys.add(key);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param uri DOCUMENT ME!
     * @param localName DOCUMENT ME!
     * @param qname DOCUMENT ME!
     *
     * @throws SAXException DOCUMENT ME!
     */
    public void endElement(String uri, String localName, String qname)
                    throws SAXException {
        if (currentElement!=null) {
            if (!currentElement.equals(qname)) {
                throw new SAXException("Unexpected end element found " + qname);
            }

            if (currentElement.equals("Key")) {
                currentElement = "AuthorizedKeys";
            } else if (currentElement.equals("AuthorizedKeys")) {
                currentElement = null;
            } else {
                throw new SAXException("Unexpected end element " + qname);
            }
        }
    }

    /**
     * DOCUMENT ME!
     *
     * @param uri DOCUMENT ME!
     * @param localName DOCUMENT ME!
     * @param qname DOCUMENT ME!
     * @param attrs DOCUMENT ME!
     *
     * @throws SAXException DOCUMENT ME!
     */
    public void startElement(String uri, String localName, String qname,
                             Attributes attrs)
                      throws SAXException {
        if (currentElement==null) {
            if (!qname.equals("AuthorizedKeys")) {
                throw new SAXException("Unexpected root element " + qname);
            }
        } else {
            if (currentElement.equals("AuthorizedKeys")) {
                if (!qname.equals("Key")) {
                    throw new SAXException("Unexpected element " + qname);
                }
            } else {
                throw new SAXException("Unexpected element " + qname);
            }
        }

        currentElement = qname;
    }

    /**
     * DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public String toString() {
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
        xml += "<!-- Sshtools User Authorized Keys File -->\n";
        xml += "<AuthorizedKeys>\n";
        xml+= "<!-- Enter authorized public key elements here -->\n";

        Iterator it = authorizedKeys.iterator();

        while (it.hasNext()) {
            xml += ("   <Key>" + it.next().toString() + "</Key>\n");
        }

        xml += "</AuthorizedKeys>";

        return xml;
    }
}
