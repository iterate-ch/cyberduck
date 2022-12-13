package ch.cyberduck.core.aquaticprime;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import ch.cyberduck.core.Local;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.text.DefaultLexicographicOrderComparator;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.xml.sax.SAXException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
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

import com.dd.plist.NSData;
import com.dd.plist.NSDictionary;
import com.dd.plist.NSObject;
import com.dd.plist.PropertyListFormatException;
import com.dd.plist.PropertyListParser;
import com.dd.plist.XMLPropertyListParser;

public abstract class DictionaryLicense extends AbstractLicense {
    private static final Logger log = LogManager.getLogger(DictionaryLicense.class);

    private final Local file;
    private final NSDictionary dictionary;

    public DictionaryLicense(final Local file) {
        super(file);
        this.file = file;
        this.dictionary = this.read(file);
    }

    @Override
    public boolean verify(final LicenseVerifierCallback callback) {
        final String publicKey = this.getPublicKey();
        try {
            this.verify(dictionary, publicKey);
            return true;
        }
        catch(InvalidLicenseException e) {
            callback.failure(e);
            return false;
        }
    }

    protected void verify(final NSDictionary dictionary, final String publicKey) throws InvalidLicenseException {
        if(null == dictionary) {
            throw new InvalidLicenseException();
        }
        final NSData signature = (NSData) dictionary.objectForKey("Signature");
        if(null == signature) {
            log.warn(String.format("Missing key 'Signature' in dictionary %s", dictionary));
            throw new InvalidLicenseException();
        }
        // Append all values
        StringBuilder values = new StringBuilder();
        final ArrayList<String> keys = new ArrayList<>(dictionary.keySet());
        // Sort lexicographically by key
        keys.sort(new DefaultLexicographicOrderComparator());
        for(String key : keys) {
            if("Signature".equals(key)) {
                continue;
            }
            values.append(dictionary.objectForKey(key).toString());
        }
        byte[] signaturebytes = signature.bytes();
        byte[] plainbytes = values.toString().getBytes(StandardCharsets.UTF_8);
        try {
            final BigInteger modulus = new BigInteger(StringUtils.removeStart(publicKey, "0x"), 16);
            final BigInteger exponent = new BigInteger(Base64.decodeBase64("Aw=="));
            final KeySpec spec = new RSAPublicKeySpec(modulus, exponent);

            final PublicKey rsa = KeyFactory.getInstance("RSA").generatePublic(spec);
            final Cipher rsaCipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
            rsaCipher.init(Cipher.DECRYPT_MODE, rsa);
            final MessageDigest sha1Digest = MessageDigest.getInstance("SHA1");
            if(!Arrays.equals(rsaCipher.doFinal(signaturebytes), sha1Digest.digest(plainbytes))) {
                throw new InvalidLicenseException();
            }
        }
        catch(NoSuchPaddingException
              | BadPaddingException
              | IllegalBlockSizeException
              | InvalidKeyException
              | InvalidKeySpecException
              | NoSuchAlgorithmException e) {
            log.warn(String.format("Signature verification failure for key %s", file));
            throw new InvalidLicenseException();
        }
        if(log.isInfoEnabled()) {
            log.info(String.format("Valid key in %s", file));
        }
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

    private NSDictionary read(final Local file) {
        try {
            try {
                return (NSDictionary) PropertyListParser.parse(file.getInputStream());
            }
            catch(PropertyListFormatException e) {
                return (NSDictionary) XMLPropertyListParser.parse(file.getInputStream());
            }
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

    protected abstract String getPublicKey();
}
