package ch.cyberduck.core.aquaticprime;

/*
 * Copyright (c) 2002-2015 David Kocher. All rights reserved.
 * http://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * Bug fixes, suggestions and comments should be sent to:
 * feedback@cyberduck.io
 */

import ch.cyberduck.core.Local;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.text.NaturalOrderComparator;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.xml.sax.SAXException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.security.spec.RSAPublicKeySpec;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

import com.dd.plist.NSData;
import com.dd.plist.NSDictionary;
import com.dd.plist.NSObject;
import com.dd.plist.PropertyListFormatException;
import com.dd.plist.XMLPropertyListParser;

public class DonationKey extends AbstractLicense implements LicenseVerifier {
    private static final Logger log = Logger.getLogger(DonationKey.class);

    private static final String pub =
            "0xAF026CFCF552C3D09A051124A596CEF7BBB26B15629504CD163B09675BE507C9C526ED3DBFCB91B78F718E0886A18400B56BC00E9213228CD6D6E9C84D8B6099AA3DE6E6F46F6CC7970982DE93A2A7318351FDFA25AE75B403996E50BB40643384214234E84EDA3E518772A4FF57FE29DD7C77A5EEB14C9023CA18FEC63236EF";

    private Local file;

    private final NSDictionary dictionary;

    /**
     * @param file The license key file.
     */
    public DonationKey(final Local file) {
        super(file);
        this.file = file;
        this.dictionary = this.read();
    }

    /**
     * @return True if valid license key
     */
    @Override
    public boolean verify() {
        if(null == dictionary) {
            return false;
        }
        final NSData signature = (NSData) dictionary.objectForKey("Signature");
        if(null == signature) {
            log.warn(String.format("Missing key 'Signature' in dictionary %s", dictionary));
            return false;
        }
        // Append all values
        StringBuilder values = new StringBuilder();
        final ArrayList<String> keys = new ArrayList<>(dictionary.keySet());
        // Sort lexicographically by key
        Collections.sort(keys, new NaturalOrderComparator());
        for(String key : keys) {
            if("Signature".equals(key)) {
                continue;
            }
            values.append(dictionary.objectForKey(key).toString());
        }
        byte[] signaturebytes = signature.bytes();
        byte[] plainbytes = values.toString().getBytes(Charset.forName("UTF-8"));
        final boolean valid;
        try {
            final BigInteger modulus = new BigInteger(StringUtils.removeStart(this.getPublicKey(), "0x"), 16);
            final BigInteger exponent = new BigInteger(Base64.decodeBase64("Aw=="));
            final KeySpec spec = new RSAPublicKeySpec(modulus, exponent);

            final PublicKey rsa = KeyFactory.getInstance("RSA").generatePublic(spec);
            final Cipher rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            rsaCipher.init(Cipher.DECRYPT_MODE, rsa);
            final MessageDigest sha1Digest = MessageDigest.getInstance("SHA1");
            valid = Arrays.equals(rsaCipher.doFinal(signaturebytes), sha1Digest.digest(plainbytes));
        }
        catch(NoSuchPaddingException
                | BadPaddingException
                | IllegalBlockSizeException
                | InvalidKeyException
                | InvalidKeySpecException
                | NoSuchAlgorithmException e) {
            log.warn(String.format("Signature verification failure for key %s", file));
            return false;
        }
        if(valid) {
            if(log.isInfoEnabled()) {
                log.info(String.format("Valid key in %s", file));
            }
        }
        else {
            log.warn(String.format("Not a valid key in %s", file));
        }
        return valid;
    }

    @Override
    public String getValue(final String property) {
        if(null == dictionary) {
            return null;
        }
        final NSObject value = dictionary.objectForKey(property);
        if(null == value) {
            log.warn(String.format("No value for key %s in dictionary %s", property, dictionary));
            return null;
        }
        return value.toString();
    }

    protected String getPublicKey() {
        return pub;
    }

    private NSDictionary read() {
        try {
            return (NSDictionary) XMLPropertyListParser.parse(file.getInputStream());
        }
        catch(ParserConfigurationException
                | IOException
                | SAXException
                | PropertyListFormatException
                | ParseException
                | AccessDeniedException e) {
            log.warn(String.format("Failure %s reading dictionary from %s", e.getMessage(), file));
        }
        return null;
    }
}
