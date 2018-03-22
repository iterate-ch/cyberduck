package ch.cyberduck.core.privasphere;

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

import ch.cyberduck.core.ConnectionCallback;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Bulk;
import ch.cyberduck.core.features.Delete;
import ch.cyberduck.core.privasphere.io.swagger.client.ApiClient;
import ch.cyberduck.core.privasphere.io.swagger.client.ApiException;
import ch.cyberduck.core.privasphere.io.swagger.client.api.UploadsApi;
import ch.cyberduck.core.privasphere.io.swagger.client.model.Certificate;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferItem;
import ch.cyberduck.core.transfer.TransferStatus;

import org.apache.log4j.Logger;

import java.io.ByteArrayInputStream;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Map;

public class PrivasphereBulkFeature implements Bulk<Void> {

    private static final Logger log = Logger.getLogger(PrivasphereBulkFeature.class);

    private final ApiClient client = new ApiClient();
//    private final SMimeGenerator smime = new SMimeGenerator();

    /**
     * Prepare upload, i.e. fetch receiver's certificate, generate session key with associated encryption parameters,
     * sha checksums, mime type, ...
     */
    @Override
    public Void pre(final Transfer.Type type, final Map<TransferItem, TransferStatus> files,
                    final ConnectionCallback callback) throws BackgroundException {
        final List<Certificate> certificates;
        try {
            certificates = new UploadsApi(client).getCertificatesByToken("tokenFromURL");
            if(certificates.size() > 1) {
                log.warn("Multiple certificates are not supported, taking the first one");
            }
            final Certificate certificate = certificates.iterator().next();
            final X509Certificate x509 = this.convert(certificate.getPem());
            // prepare files to be transferred
            for(TransferItem item : files.keySet()) {
//                final SMimeMetadata meta = smime.generateMetadata(new File(item.local.getAbsolute()), certificate);
                final Map<String, String> attributes = item.remote.attributes().getCustom();
//                attributes.put("certificate", x509);
//                attributes.put("sessionKey", meta.getSessionKey());
//                attributes.put("securityParameters", meta.getSecurityParameters());
//                attributes.put("mimeType", meta.getMimeType());
//                attributes.put("sha256", meta.getSha256());
            }
        }
        catch(ApiException e) {
            //TODO exceptionMapper
            log.error("Failure while retrieving certificate", e);
            throw new BackgroundException(e.getMessage(), e);
        }
        catch(CertificateException e) {
            //TODO exceptionMapper
            log.error("Failure processing certificate", e);
            throw new BackgroundException(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public void post(final Transfer.Type type, final Map<TransferItem, TransferStatus> files,
                     final ConnectionCallback callback) throws BackgroundException {
        //
    }

    @Override
    public Bulk<Void> withDelete(final Delete delete) {
        return this;
    }

    private X509Certificate convert(final String pem) throws CertificateException {
        CertificateFactory cf = CertificateFactory.getInstance("X.509");
        return (X509Certificate) cf.generateCertificate(new ByteArrayInputStream(pem.getBytes()));
    }
}
