package ch.cyberduck.core.s3;

/*
 * Copyright (c) 2013 David Kocher. All rights reserved.
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

import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.exception.InteroperabilityException;
import ch.cyberduck.core.exception.LoginFailureException;

import org.jets3t.service.S3ServiceException;
import org.jets3t.service.ServiceException;
import org.junit.Test;

import javax.net.ssl.SSLException;
import javax.net.ssl.SSLHandshakeException;
import java.net.UnknownHostException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class S3ExceptionMappingServiceTest {

    @Test
    public void testLoginFailure() throws Exception {
        final ServiceException f = new ServiceException("m", "<null/>");
        f.setResponseCode(401);
        f.setErrorMessage("m");
        assertTrue(new S3ExceptionMappingService().map(f) instanceof LoginFailureException);
    }

    @Test
    public void testLoginFailure403() throws Exception {
        final ServiceException f = new ServiceException("m", "<null/>");
        f.setResponseCode(403);
        f.setErrorMessage("m");
        f.setErrorCode("AccessDenied");
        assertTrue(new S3ExceptionMappingService().map(f) instanceof AccessDeniedException);
        f.setErrorCode("InvalidAccessKeyId");
        assertTrue(new S3ExceptionMappingService().map(f) instanceof LoginFailureException);
        f.setErrorCode("SignatureDoesNotMatch");
        assertTrue(new S3ExceptionMappingService().map(f) instanceof LoginFailureException);
    }

    @Test
    public void testBadRequest() {
        final ServiceException f = new ServiceException("m", "<null/>");
        f.setErrorMessage("m");
        f.setResponseCode(400);
        assertTrue(new S3ExceptionMappingService().map(f) instanceof InteroperabilityException);
    }

    @Test
    public void testMapping() {
        assertEquals("Message.", new S3ExceptionMappingService().map(new ServiceException("message")).getDetail());
        assertEquals("Exceeded 403 retry limit (1).", new S3ExceptionMappingService().map(
                new ServiceException("Exceeded 403 retry limit (1).")).getDetail());
        assertEquals("Connection failed", new S3ExceptionMappingService().map(
                new ServiceException("Exceeded 403 retry limit (1).")).getMessage());
    }

    @Test
    public void testDNSFailure() {
        assertEquals("Custom.",
                new S3ExceptionMappingService().map("custom", new ServiceException("message", new UnknownHostException("h"))).getMessage());
        assertEquals("H. DNS is the network service that translates a server name to its Internet address. This error is most often caused by having no connection to the Internet or a misconfigured network. It can also be caused by an unresponsive DNS server or a firewall preventing access to the network.",
                new S3ExceptionMappingService().map("custom", new ServiceException("message", new UnknownHostException("h"))).getDetail());
    }

    @Test
    public void testCustomMessage() {
        assertEquals("Custom.",
                new S3ExceptionMappingService().map("custom", new ServiceException("message")).getMessage());
        assertEquals("Message.",
                new S3ExceptionMappingService().map("custom", new ServiceException("message")).getDetail());
    }

    @Test
    public void testIAMFailure() {
        assertEquals("The IAM policy must allow the action s3:GetBucketLocation on the resource arn:aws:s3:::endpoint-9a527d70-d432-4601-b24b-735e721b82c9.",
                new S3ExceptionMappingService().map("The IAM policy must allow the action s3:GetBucketLocation on the resource arn:aws:s3:::endpoint-9a527d70-d432-4601-b24b-735e721b82c9", new ServiceException("message")).getMessage());
        assertEquals("Message.",
                new S3ExceptionMappingService().map("The IAM policy must allow the action s3:GetBucketLocation on the resource arn:aws:s3:::endpoint-9a527d70-d432-4601-b24b-735e721b82c9", new ServiceException("message")).getDetail());
    }

    @Test
    public void testHandshakeFailure() {
        final SSLHandshakeException f = new SSLHandshakeException("f");
        f.initCause(new CertificateException("c"));
        assertEquals(ConnectionCanceledException.class, new S3ExceptionMappingService().map(
                new ServiceException(f)).getClass());
    }

    @Test
    public void test403NoXml() {
        final ServiceException f = new ServiceException();
        f.setResponseCode(403);
        assertTrue(new S3ExceptionMappingService().map(f) instanceof AccessDeniedException);
    }

    @Test
    public void testWrapped() {
        assertEquals("Access Denied.", new S3ExceptionMappingService().map(new ServiceException(new S3ServiceException("m",
                        "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Error><Code>AccessDenied</Code><Message>Access Denied</Message><RequestId>D84EDAE486BD2D71</RequestId><HostId>tVNWw2hK+FVpFnWUVf2LdDM6rgtjo/cibINRUVc/HpqMZbgNTg311LSltHYvRQdX</HostId></Error>"))).getDetail()
        );
    }

    @Test
    public void testAlgorithmFailure() {
        assertEquals("EC AlgorithmParameters not available. Please contact your web hosting service provider for assistance.",
                new S3ExceptionMappingService().map(new S3ServiceException(
                        new SSLException(
                                new RuntimeException(
                                        new NoSuchAlgorithmException("EC AlgorithmParameters not available")
                                )
                        ))).getDetail());
    }
}