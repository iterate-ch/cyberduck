package ch.cyberduck.core.aquaticprime;

/*
 * Copyright (c) 2002-2011 David Kocher. All rights reserved.
 *
 * http://cyberduck.ch/
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
 * dkocher@cyberduck.ch
 */

import ch.cyberduck.core.Local;
import ch.cyberduck.core.LocalFactory;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.i18n.Locale;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.io.filefilter.FalseFileFilter;
import org.apache.commons.io.filefilter.NameFileFilter;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.bouncycastle.asn1.*;
import org.bouncycastle.cms.CMSProcessable;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.jce.PKCS7SignedData;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.io.File;
import java.io.FileInputStream;
import java.net.NetworkInterface;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.security.Security;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;

/**
 * @version $Id:$
 */
public class Receipt extends AbstractLicense {
    private static Logger log = Logger.getLogger(Receipt.class);

    public static void register() {
        LicenseFactory.addFactory(Factory.NATIVE_PLATFORM, new Factory());
    }

    /**
     *
     */
    private static final int APPSTORE_VALIDATION_FAILURE = 173;

    private static class Factory extends LicenseFactory {
        @Override
        protected License open(final Local file) {
            AbstractLicense l = new Receipt(file);
            // Verify immediatly and exit if not a valid receipt
            l.verify();
            return l;
        }

        @Override
        protected License open() {
            Local receipt = LocalFactory.createLocal(Preferences.instance().getProperty("application.receipt.path"));
            if(receipt.exists()) {
                final Collection<File> receipts = FileUtils.listFiles(
                        new File(receipt.getAbsolute()),
                        new NameFileFilter("receipt"), FalseFileFilter.FALSE);
                for(File key : receipts) {
                    return open(LocalFactory.createLocal(key));
                }
            }
            log.info("No receipt found");
            System.exit(APPSTORE_VALIDATION_FAILURE);
            return LicenseFactory.EMPTY_LICENSE;
        }

        @Override
        protected License create() {
            return this.open();
        }
    }

    /**
     * @param file The license key file.
     */
    public Receipt(Local file) {
        super(file);
    }

    /**
     * Verifies the App Store Receipt
     *
     * @return
     */
    public boolean verify() {
        try {
            Security.addProvider(new BouncyCastleProvider());
            PKCS7SignedData signature = new PKCS7SignedData(IOUtils.toByteArray(new FileInputStream(
                    this.getFile().getAbsolute()
            )));

            signature.verify();
            // For additional security, you may verify the fingerprint of the root CA and the OIDs of the
            // intermediate CA and signing certificate. The OID in the Certificate Policies Extension of the
            // intermediate CA is (1 2 840 113635 100 5 6 1), and the Marker OID of the signing certificate
            // is (1 2 840 113635 100 6 11 1).

            // Extract the receipt attributes
            CMSSignedData s = new CMSSignedData(new FileInputStream(
                    this.getFile().getAbsolute()
            ));
            CMSProcessable signedContent = s.getSignedContent();
            byte[] originalContent = (byte[]) signedContent.getContent();
            ASN1Object asn = ASN1Object.fromByteArray(originalContent);

            byte[] opaque = null;
            String bundleIdentifier = null;
            String bundleVersion = null;
            byte[] hash = null;

            if(asn instanceof DERSet) {
                // 2 Bundle identifier      Interpret as an ASN.1 UTF8STRING.
                // 3 Application version    Interpret as an ASN.1 UTF8STRING.
                // 4 Opaque value           Interpret as a series of bytes.
                // 5 SHA-1 hash             Interpret as a 20-byte SHA-1 digest value.
                DERSet set = (DERSet) asn;
                Enumeration enumeration = set.getObjects();
                while(enumeration.hasMoreElements()) {
                    Object next = enumeration.nextElement();
                    if(next instanceof DERSequence) {
                        DERSequence sequence = (DERSequence) next;
                        DEREncodable type = sequence.getObjectAt(0);
                        if(type instanceof DERInteger) {
                            if(((DERInteger) type).getValue().intValue() == 2) {
                                DEREncodable value = sequence.getObjectAt(2);
                                if(value instanceof DEROctetString) {
                                    bundleIdentifier = new String(((DEROctetString) value).getOctets(), "utf-8");
                                }
                            }
                            else if(((DERInteger) type).getValue().intValue() == 3) {
                                DEREncodable value = sequence.getObjectAt(2);
                                if(value instanceof DEROctetString) {
                                    bundleVersion = new String(((DEROctetString) value).getOctets(), "utf-8");
                                }
                            }
                            else if(((DERInteger) type).getValue().intValue() == 4) {
                                DEREncodable value = sequence.getObjectAt(2);
                                if(value instanceof DEROctetString) {
                                    opaque = ((DEROctetString) value).getOctets();
                                }
                            }
                            else if(((DERInteger) type).getValue().intValue() == 5) {
                                DEREncodable value = sequence.getObjectAt(2);
                                if(value instanceof DEROctetString) {
                                    hash = ((DEROctetString) value).getOctets();
                                }
                            }
                        }
                    }
                }
            }
            else {
                log.error("Expected set of attributes for:" + asn);
                System.exit(APPSTORE_VALIDATION_FAILURE);
            }
            if(!StringUtils.equals("ch.sudo.cyberduck", StringUtils.trim(bundleIdentifier))) {
                log.error("Bundle identifier in ASN set does not match");
                System.exit(APPSTORE_VALIDATION_FAILURE);
            }
            if(!StringUtils.equals(Preferences.instance().getDefault("CFBundleShortVersionString"),
                    StringUtils.trim(bundleVersion))) {
                log.warn("Bundle version in ASN set does not match");
                System.exit(APPSTORE_VALIDATION_FAILURE);
            }

            NetworkInterface en0 = NetworkInterface.getByName("en0");
            if(null == en0) {
                // Interface is not found when link is down #fail
                log.warn("No network interface en0");
            }
            else {
                byte[] mac = en0.getHardwareAddress();
                if(null == mac) {
                    log.error("Cannot determine MAC address");
                    // Shutdown if receipt is not valid
                    System.exit(APPSTORE_VALIDATION_FAILURE);
                }
                if(log.isDebugEnabled()) {
                    log.debug("Interface en0:" + Hex.encodeHexString(mac));
                }
                // Compute the hash of the GUID
                MessageDigest digest = MessageDigest.getInstance("SHA-1");
                digest.update(mac);
                digest.update(opaque);
                digest.update(bundleIdentifier.getBytes(Charset.forName("utf-8")));
                byte[] result = digest.digest();
                if(Arrays.equals(result, hash)) {
                    if(log.isInfoEnabled()) {
                        log.info("Valid receipt for Computer GUID:" + Hex.encodeHexString(mac));
                    }
                }
                else {
                    log.error("Failed verfification. Hash with GUID "
                            + Hex.encodeHexString(mac) + " does not match hash in receipt");
                    System.exit(APPSTORE_VALIDATION_FAILURE);
                }
            }
        }
        catch(Throwable e) {
            log.error("Unknown receipt validation error:" + e.getMessage());
            // Shutdown if receipt is not valid
            System.exit(APPSTORE_VALIDATION_FAILURE);
        }
        // Always return true to dismiss donation prompt.
        return true;
    }

    @Override
    public boolean isReceipt() {
        return true;
    }

    public String getValue(String property) {
        return StringUtils.EMPTY;
    }

    @Override
    public String getName() {
        return Locale.localizedString("Unknown");
    }
}
