package ch.cyberduck.core.ftps;

/*
 * ====================================================================
 *
 *  Copyright 2002-2004 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation.  For more
 * information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 *
 */

import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateExpiredException;
import java.security.cert.CertificateNotYetValidException;
import java.security.cert.X509Certificate;

import org.apache.log4j.Logger;

/**
 * <p/>
 * CustomCustomEasyX509TrustManager unlike default {@link X509TrustManager} accepts
 * self-signed certificates.
 * </p>
 * <p/>
 * This trust manager SHOULD NOT be used for productive systems
 * due to security reasons, unless it is a concious decision and
 * you are perfectly aware of security implications of accepting
 * self-signed certificates
 * </p>
 *
 * @author <a href="mailto:adrian.sutton@ephox.com">Adrian Sutton</a>
 * @author <a href="mailto:oleg@ural.ru">Oleg Kalnichevski</a>
 *         <p/>
 *         <p/>
 *         DISCLAIMER: HttpClient developers DO NOT actively support this component.
 *         The component is provided as a reference material, which may be inappropriate
 *         for use without additional customization.
 *         </p>
 */

public abstract class AbstractX509TrustManager implements X509TrustManager {
    private static Logger log = Logger.getLogger(AbstractX509TrustManager.class);

    private X509TrustManager standardTrustManager = null;

    protected void init(KeyStore keystore) throws NoSuchAlgorithmException, KeyStoreException {
        TrustManagerFactory factory = TrustManagerFactory.getInstance("SunX509"); //todo get apple keychain
        factory.init(keystore);
        TrustManager[] trustmanagers = factory.getTrustManagers();
        if (trustmanagers.length == 0) {
            throw new NoSuchAlgorithmException("SunX509 trust manager not supported");
        }
        this.standardTrustManager = (X509TrustManager) trustmanagers[0];
    }

    public void checkClientTrusted(X509Certificate[] x509Certificates, String authType) throws CertificateException {
        this.standardTrustManager.checkClientTrusted(x509Certificates, authType);
    }

    public void checkServerTrusted(X509Certificate[] x509Certificates, String authType) throws CertificateException {
        if ((x509Certificates != null)) {
            log.info("Server certificate chain:");
            for (int i = 0; i < x509Certificates.length; i++) {
                log.info("X509Certificate[" + i + "]=" + x509Certificates[i]);
            }
        }
        this.standardTrustManager.checkServerTrusted(x509Certificates, authType);
//        if ((x509Certificates != null) && (x509Certificates.length == 1)) {
//            X509Certificate certificate = x509Certificates[0];
//            try {
//                certificate.checkValidity();
//            }
//            catch (CertificateExpiredException e) {
//                log.warn(e.toString());
//                throw e;
//            }
//            catch (CertificateNotYetValidException e) {
//                log.warn(e.toString());
//                throw e;
//            }
//        }
//        else {
//            this.standardTrustManager.checkServerTrusted(x509Certificates, authType);
//        }
    }

    public X509Certificate[] getAcceptedIssuers() {
        return this.standardTrustManager.getAcceptedIssuers();
    }
}