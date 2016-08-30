package ch.cyberduck.core.aquaticprime;

/*
 * Copyright (c) 2002-2013 David Kocher. All rights reserved.
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
 * feedback@cyberduck.ch
 */

import ch.cyberduck.core.Local;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.bouncycastle.asn1.ASN1Encodable;
import org.bouncycastle.asn1.ASN1Integer;
import org.bouncycastle.asn1.ASN1Primitive;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.DEROctetString;
import org.bouncycastle.asn1.DLSequence;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessable;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.Store;

import java.io.FileInputStream;
import java.io.IOException;
import java.net.NetworkInterface;
import java.nio.charset.Charset;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.Security;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;

public class ReceiptVerifier implements LicenseVerifier {
    private static final Logger log = Logger.getLogger(ReceiptVerifier.class);

    private Local file;

    private String guid;

    public ReceiptVerifier(final Local file) {
        this.file = file;
    }

    static {
        Security.addProvider(new BouncyCastleProvider());
    }

    @Override
    public boolean verify() {
        try {
            // For additional security, you may verify the fingerprint of the root CA and the OIDs of the
            // intermediate CA and signing certificate. The OID in the Certificate Policies Extension of the
            // intermediate CA is (1 2 840 113635 100 5 6 1), and the Marker OID of the signing certificate
            // is (1 2 840 113635 100 6 11 1).
            final CMSSignedData s = new CMSSignedData(new FileInputStream(file.getAbsolute()));
            Store certs = s.getCertificates();
            SignerInformationStore signers = s.getSignerInfos();
            for(SignerInformation signer : (Iterable<SignerInformation>) signers.getSigners()) {
                final Collection<X509CertificateHolder> matches = certs.getMatches(signer.getSID());
                for(X509CertificateHolder holder : matches) {
                    if(!signer.verify(new JcaSimpleSignerInfoVerifierBuilder().setProvider(
                            new BouncyCastleProvider()
                    ).build(holder))) {
                        return false;
                    }
                }
            }
            // Extract the receipt attributes
            final CMSProcessable signedContent = s.getSignedContent();
            byte[] originalContent = (byte[]) signedContent.getContent();
            final ASN1Primitive asn = ASN1Primitive.fromByteArray(originalContent);

            byte[] opaque = null;
            String bundleIdentifier = null;
            String bundleVersion = null;
            byte[] hash = null;

            if(asn instanceof ASN1Set) {
                // 2 Bundle identifier      Interpret as an ASN.1 UTF8STRING.
                // 3 Application version    Interpret as an ASN.1 UTF8STRING.
                // 4 Opaque value           Interpret as a series of bytes.
                // 5 SHA-1 hash             Interpret as a 20-byte SHA-1 digest value.
                final ASN1Set set = (ASN1Set) asn;
                final Enumeration enumeration = set.getObjects();
                while(enumeration.hasMoreElements()) {
                    Object next = enumeration.nextElement();
                    if(next instanceof DLSequence) {
                        DLSequence sequence = (DLSequence) next;
                        ASN1Encodable type = sequence.getObjectAt(0);
                        if(type instanceof ASN1Integer) {
                            if(((ASN1Integer) type).getValue().intValue() == 2) {
                                final ASN1Encodable value = sequence.getObjectAt(2);
                                if(value instanceof DEROctetString) {
                                    bundleIdentifier = new String(((DEROctetString) value).getOctets(), "UTF-8");
                                }
                            }
                            else if(((ASN1Integer) type).getValue().intValue() == 3) {
                                final ASN1Encodable value = sequence.getObjectAt(2);
                                if(value instanceof DEROctetString) {
                                    bundleVersion = new String(((DEROctetString) value).getOctets(), "UTF-8");
                                }
                            }
                            else if(((ASN1Integer) type).getValue().intValue() == 4) {
                                final ASN1Encodable value = sequence.getObjectAt(2);
                                if(value instanceof DEROctetString) {
                                    opaque = ((DEROctetString) value).getOctets();
                                }
                            }
                            else if(((ASN1Integer) type).getValue().intValue() == 5) {
                                final ASN1Encodable value = sequence.getObjectAt(2);
                                if(value instanceof DEROctetString) {
                                    hash = ((DEROctetString) value).getOctets();
                                }
                            }
                        }
                    }
                }
            }
            else {
                log.error(String.format("Expected set of attributes for %s", asn));
                return false;
            }
            if(!StringUtils.equals(PreferencesFactory.get().getDefault("application.identifier"), StringUtils.trim(bundleIdentifier))) {
                log.error(String.format("Bundle identifier %s in ASN set does not match", bundleIdentifier));
                return false;
            }
            if(!StringUtils.equals(PreferencesFactory.get().getDefault("application.version"), StringUtils.trim(bundleVersion))) {
                log.warn(String.format("Bundle version %s in ASN set does not match", bundleVersion));
            }
            final NetworkInterface en0 = NetworkInterface.getByName("en0");
            if(null == en0) {
                // Interface is not found when link is down #fail
                log.warn("No network interface en0");
                return true;
            }
            else {
                final byte[] mac = en0.getHardwareAddress();
                if(null == mac) {
                    log.error("Cannot determine MAC address");
                    // Continue without validation
                    return true;
                }
                final String hex = Hex.encodeHexString(mac);
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Interface en0 %s", hex));
                }
                // Compute the hash of the GUID
                final MessageDigest digest = MessageDigest.getInstance("SHA-1");
                digest.update(mac);
                if(null == opaque) {
                    log.error(String.format("Missing opaque string in ASN.1 set %s", asn));
                    return false;
                }
                digest.update(opaque);
                if(null == bundleIdentifier) {
                    log.error(String.format("Missing bundle identifier in ASN.1 set %s", asn));
                    return false;
                }
                digest.update(bundleIdentifier.getBytes(Charset.forName("UTF-8")));
                final byte[] result = digest.digest();
                if(Arrays.equals(result, hash)) {
                    if(log.isInfoEnabled()) {
                        log.info(String.format("Valid receipt for GUID %s", hex));
                    }
                    guid = hex;
                    return true;
                }
                else {
                    log.error(String.format("Failed verification. Hash with GUID %s does not match hash in receipt", hex));
                    return false;
                }
            }
        }
        catch(IOException e) {
            log.error("Receipt validation error", e);
            // Shutdown if receipt is not valid
            return false;
        }
        catch(GeneralSecurityException e) {
            log.error("Receipt validation error", e);
            // Shutdown if receipt is not valid
            return false;
        }
        catch(SecurityException e) {
            log.error("Receipt validation error", e);
            // Shutdown if receipt is not valid
            return false;
        }
        catch(CMSException e) {
            log.error("Receipt validation error", e);
            // Shutdown if receipt is not valid
            return false;
        }
        catch(Exception e) {
            log.error("Unknown receipt validation error", e);
            return true;
        }
    }

    public String getGuid() {
        return guid;
    }
}
