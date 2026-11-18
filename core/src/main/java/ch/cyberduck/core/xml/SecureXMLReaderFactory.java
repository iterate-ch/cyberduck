package ch.cyberduck.core.xml;

/*
 * Copyright (c) 2002-2026 iterate GmbH. All rights reserved.
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

import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;

import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParserFactory;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Creates SAX readers safe to parse untrusted input to prevent XML external entity (XXE) injection.
 * {@link XMLConstants#FEATURE_SECURE_PROCESSING} is always enabled and cannot be overridden.
 */
public final class SecureXMLReaderFactory {

    public static final String FEATURE_DISALLOW_DOCTYPE_DECL = "http://apache.org/xml/features/disallow-doctype-decl";
    public static final String FEATURE_EXTERNAL_GENERAL_ENTITIES = "http://xml.org/sax/features/external-general-entities";
    public static final String FEATURE_EXTERNAL_PARAMETER_ENTITIES = "http://xml.org/sax/features/external-parameter-entities";
    public static final String FEATURE_LOAD_EXTERNAL_DTD = "http://apache.org/xml/features/nonvalidating/load-external-dtd";

    /**
     * Features for parsers reading remote content. DOCTYPE declarations are rejected and external
     * entities and DTDs are never resolved.
     */
    public static final Map<String, Boolean> DEFAULT_FEATURES;

    static {
        final Map<String, Boolean> features = new LinkedHashMap<>();
        features.put(FEATURE_DISALLOW_DOCTYPE_DECL, true);
        features.put(FEATURE_EXTERNAL_GENERAL_ENTITIES, false);
        features.put(FEATURE_EXTERNAL_PARAMETER_ENTITIES, false);
        features.put(FEATURE_LOAD_EXTERNAL_DTD, false);
        DEFAULT_FEATURES = Collections.unmodifiableMap(features);
    }

    private SecureXMLReaderFactory() {
        //
    }

    /**
     * @return Namespace aware reader configured with {@link #DEFAULT_FEATURES}
     * @throws SAXException If the parser does not support the required security features
     */
    public static XMLReader create() throws SAXException {
        return create(DEFAULT_FEATURES);
    }

    /**
     * @param features Feature flags to set on the parser factory in addition to secure processing
     * @return Namespace aware reader configured with the given features
     * @throws SAXException If the parser does not support one of the features
     */
    public static XMLReader create(final Map<String, Boolean> features) throws SAXException {
        try {
            final SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(true);
            for(Map.Entry<String, Boolean> feature : features.entrySet()) {
                factory.setFeature(feature.getKey(), feature.getValue());
            }
            // Set last to make sure it cannot be disabled by the given features
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            return factory.newSAXParser().getXMLReader();
        }
        catch(ParserConfigurationException e) {
            throw new SAXException(e);
        }
    }
}
