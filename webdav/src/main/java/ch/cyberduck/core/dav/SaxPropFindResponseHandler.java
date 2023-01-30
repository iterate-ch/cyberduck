package ch.cyberduck.core.dav;

/*
 * Copyright (c) 2002-2018 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Element;
import org.xml.sax.Attributes;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;

import com.github.sardine.impl.handler.MultiStatusResponseHandler;
import com.github.sardine.model.*;
import com.github.sardine.util.SardineUtil;

public class SaxPropFindResponseHandler extends MultiStatusResponseHandler {
    private static final Logger log = LogManager.getLogger(SaxPropFindResponseHandler.class);

    @Override
    protected Multistatus getMultistatus(final InputStream stream) throws IOException {
        final SAXParserFactory spf = SAXParserFactory.newInstance();
        spf.setNamespaceAware(true);
        try {
            final SAXParser saxParser = spf.newSAXParser();
            final XMLReader xmlReader = saxParser.getXMLReader();
            final SaxHandler handler = new SaxHandler();
            xmlReader.setContentHandler(handler);
            xmlReader.setErrorHandler(new LoggingErrorHandler());
            xmlReader.parse(new InputSource(stream));
            return handler.getMultistatus();
        }
        catch(IOException | SAXException | ParserConfigurationException e) {
            throw new IOException("Not a valid DAV response", e);
        }
    }

    private static final class SaxHandler extends DefaultHandler {

        private Multistatus multistatus;
        private Response response;
        private Propstat propstat;
        private Prop prop;
        private Resourcetype type;
        private Lockdiscovery lockdiscovery;
        private Activelock activelock;
        private Locktoken locktoken;

        private final StringBuilder text = new StringBuilder();
        private final Element root = SardineUtil.createElement(SardineUtil.createQNameWithCustomNamespace("root"));

        @Override
        public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) {
            switch(localName) {
                case "multistatus":
                    multistatus = new Multistatus();
                    break;
                case "response":
                    response = new Response();
                    if(multistatus != null) {
                        multistatus.getResponse().add(response);
                    }
                    break;
                case "propstat":
                    propstat = new Propstat();
                    if(response != null) {
                        response.getPropstat().add(propstat);
                    }
                    break;
                case "prop":
                    prop = new Prop();
                    if(propstat != null) {
                        propstat.setProp(prop);
                    }
                    break;
                case "resourcetype":
                    type = new Resourcetype();
                    if(prop != null) {
                        prop.setResourcetype(type);
                    }
                    break;
                case "lockdiscovery":
                    lockdiscovery = new Lockdiscovery();
                    if(prop != null) {
                        prop.setLockdiscovery(lockdiscovery);
                    }
                    break;
                case "activelock":
                    activelock = new Activelock();
                    if(lockdiscovery != null) {
                        lockdiscovery.getActivelock().add(activelock);
                    }
                    break;
                case "locktoken":
                    locktoken = new Locktoken();
                    if(activelock != null) {
                        activelock.setLocktoken(locktoken);
                    }
                    break;
                case "collection":
                    type.setCollection(new Collection());
                    break;
            }
            text.setLength(0);
            text.trimToSize();
        }

        @Override
        public void characters(final char[] ch, final int start, final int length) {
            text.append(new String(ch, start, length));
        }

        @Override
        public void endElement(final String uri, final String localName, final String qName) {
            if(StringUtils.isBlank(text.toString())) {
                return;
            }
            if(response != null) {
                switch(localName) {
                    case "href": {
                        if(locktoken != null) {
                            locktoken.getHref().add(text.toString());
                        }
                        else {
                            response.getHref().add(text.toString());
                        }
                        break;
                    }
                }
            }
            if(propstat != null) {
                switch(localName) {
                    case "status": {
                        propstat.setStatus(text.toString());
                        break;
                    }
                }
            }
            if(prop != null) {
                switch(localName) {
                    case "creationdate": {
                        final Creationdate value = new Creationdate();
                        value.getContent().add(text.toString());
                        prop.setCreationdate(value);
                        break;
                    }
                    case "displayname": {
                        final Displayname value = new Displayname();
                        value.getContent().add(text.toString());
                        prop.setDisplayname(value);
                        break;
                    }
                    case "getcontentlength": {
                        final Getcontentlength value = new Getcontentlength();
                        value.getContent().add(text.toString());
                        prop.setGetcontentlength(value);
                        break;
                    }
                    case "getcontenttype": {
                        final Getcontenttype value = new Getcontenttype();
                        value.getContent().add(text.toString());
                        prop.setGetcontenttype(value);
                        break;
                    }
                    case "getlastmodified": {
                        final Getlastmodified value = new Getlastmodified();
                        value.getContent().add(text.toString());
                        prop.setGetlastmodified(value);
                        break;
                    }
                    case "getetag": {
                        final Getetag value = new Getetag();
                        value.getContent().add(text.toString());
                        prop.setGetetag(value);
                        break;
                    }
                    case "lastmodified": {
                        final Element element = SardineUtil.createElement(root, DAVTimestampFeature.LAST_MODIFIED_CUSTOM_NAMESPACE);
                        element.setTextContent(text.toString());
                        prop.getAny().add(element);
                        break;
                    }
                }
            }
            switch(localName) {
                case "response": {
                    response = null;
                    break;
                }
                case "propstat": {
                    propstat = null;
                    break;
                }
                case "prop": {
                    prop = null;
                    break;
                }
                case "lockdiscovery": {
                    lockdiscovery = null;
                    break;
                }
                case "activelock": {
                    activelock = null;
                    break;
                }
                case "locktoken": {
                    locktoken = null;
                    break;
                }
            }
            if(!SardineUtil.DEFAULT_NAMESPACE_URI.equals(uri)) {
                // Custom property
                if(prop != null) {
                    final Element element = SardineUtil.createElement(root, new QName(uri, localName, SardineUtil.DEFAULT_NAMESPACE_PREFIX));
                    element.setTextContent(text.toString());
                    prop.getAny().add(element);
                }
            }
        }

        public Multistatus getMultistatus() {
            return multistatus;
        }
    }

    private static final class LoggingErrorHandler implements ErrorHandler {
        @Override
        public void warning(final SAXParseException e) throws SAXException {
            log.warn(String.format("Parser warning %s", e));
        }

        @Override
        public void error(final SAXParseException e) throws SAXException {
            log.error(String.format("Parser error %s", e));
        }

        @Override
        public void fatalError(final SAXParseException e) throws SAXException {
            log.error(String.format("Fatal parser error %s", e));
            throw e;
        }
    }
}
