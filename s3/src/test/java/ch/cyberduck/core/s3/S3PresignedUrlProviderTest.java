package ch.cyberduck.core.s3;

/*
 * Copyright (c) 2002-2015 David Kocher. All rights reserved.
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
 * Bug fixes, suggestions and comments should be sent to feedback@cyberduck.ch
 */

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.Profile;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.serializer.impl.dd.ProfilePlistReader;

import org.junit.Before;
import org.junit.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.*;

public class S3PresignedUrlProviderTest {

    private S3Session amazon;
    private S3Session virtualhost;

    @Before
    public void setup() throws Exception {
        amazon = this.session("s3.amazonaws.com");
        virtualhost = this.session("test-eu-central-1-cyberduck.s3.amazonaws.com");
    }

    private S3Session session(final String hostname) throws Exception {
        final ProtocolFactory factory = new ProtocolFactory(new HashSet<>(Collections.singleton(new S3Protocol())));
        final Profile profile = new ProfilePlistReader(factory).read(
                this.getClass().getResourceAsStream("/Amazon S3.cyberduckprofile"));
        final Host host = new Host(profile, hostname, new Credentials(ACCESS_KEY, SECRET));
        return new S3Session(host) {
            @Override
            public S3CredentialsStrategy getAuthentication() {
                return host::getCredentials;
            }
        };
    }

    @Test
    public void testCreateEuWest() throws Exception {
        final Calendar expiry = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        expiry.add(Calendar.MILLISECOND, (int) TimeUnit.DAYS.toMillis(7));
        final String url = new S3PresignedUrlProvider(amazon).create(amazon.getAuthentication().get(),
                "test-eu-west-1-cyberduck", "eu-west-1", "f", "GET", expiry.getTimeInMillis());
        assertNotNull(url);
        assertEquals("test-eu-west-1-cyberduck.s3.dualstack.eu-west-1.amazonaws.com", URI.create(url).getHost());
    }

    @Test
    public void testCreateEuCentral() throws Exception {
        final Calendar expiry = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        expiry.add(Calendar.MILLISECOND, (int) TimeUnit.DAYS.toMillis(7));
        final String url = new S3PresignedUrlProvider(amazon).create(amazon.getAuthentication().get(),
                "test-eu-central-1-cyberduck", "eu-central-1", "f", "GET", expiry.getTimeInMillis());
        assertNotNull(url);
        assertEquals("test-eu-central-1-cyberduck.s3.dualstack.eu-central-1.amazonaws.com", URI.create(url).getHost());
    }

    @Test
    public void testCreateVirtualHost() throws Exception {
        final Calendar expiry = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        expiry.add(Calendar.MILLISECOND, (int) TimeUnit.DAYS.toMillis(7));
        final String url = new S3PresignedUrlProvider(virtualhost).create(virtualhost.getAuthentication().get(),
                "test-eu-central-1-cyberduck", "eu-central-1", "f", "GET", expiry.getTimeInMillis());
        assertNotNull(url);
        assertEquals("test-eu-central-1-cyberduck.s3.dualstack.eu-central-1.amazonaws.com", URI.create(url).getHost());
        assertTrue(url, url.startsWith("https://test-eu-central-1-cyberduck.s3.dualstack.eu-central-1.amazonaws.com/f"));
    }

    @Test
    public void testCreateEuCentralAtSign() throws Exception {
        final Calendar expiry = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        expiry.add(Calendar.MILLISECOND, (int) TimeUnit.DAYS.toMillis(7));
        final String url = new S3PresignedUrlProvider(amazon).create(amazon.getAuthentication().get(),
                "test-eu-central-1-cyberduck", "eu-central-1", "@f", "GET", expiry.getTimeInMillis());
        assertNotNull(url);
        assertEquals("test-eu-central-1-cyberduck.s3.dualstack.eu-central-1.amazonaws.com", URI.create(url).getHost());
        assertTrue(url, url.startsWith("https://test-eu-central-1-cyberduck.s3.dualstack.eu-central-1.amazonaws.com/%40f"));
    }

    @Test
    public void testCreateDefault() throws Exception {
        final Calendar expiry = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        expiry.add(Calendar.MILLISECOND, (int) TimeUnit.DAYS.toMillis(7));
        final String url = new S3PresignedUrlProvider(amazon).create(amazon.getAuthentication().get(),
                "test-us-east-1-cyberduck", "us-east-1", "f", "GET", expiry.getTimeInMillis());
        assertNotNull(url);
        assertEquals("test-us-east-1-cyberduck.s3.dualstack.us-east-1.amazonaws.com", URI.create(url).getHost());
        assertTrue(url.startsWith("https://test-us-east-1-cyberduck.s3.dualstack.us-east-1.amazonaws.com/f"));
    }

    @Test
    public void testCustomHostname() throws Exception {
        final Calendar expiry = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        expiry.add(Calendar.MILLISECOND, (int) TimeUnit.DAYS.toMillis(7));
        final Host host = new Host(new S3Protocol(), "h");
        final S3Session session = new S3Session(host) {
            @Override
            public S3CredentialsStrategy getAuthentication() {
                return host::getCredentials;
            }
        };
        final String url = new S3PresignedUrlProvider(session).create(session.getAuthentication().get(),
                "test-us-east-1-cyberduck", "us-east-1", "f", "GET", expiry.getTimeInMillis());
        assertNotNull(url);
        assertEquals("test-us-east-1-cyberduck.h", URI.create(url).getHost());
    }

    @Test
    public void testCustomHostnameWithRegion() throws Exception {
        final Calendar expiry = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        expiry.add(Calendar.MILLISECOND, (int) TimeUnit.DAYS.toMillis(7));
        final Host host = new Host(new S3Protocol(), "s3.eu-central-1.wasabisys.com");
        final S3Session session = new S3Session(host) {
            @Override
            public S3CredentialsStrategy getAuthentication() {
                return host::getCredentials;
            }
        };
        final String url = new S3PresignedUrlProvider(session).create(session.getAuthentication().get(),
                "cyberduck", "eu-central-1", "f", "GET", expiry.getTimeInMillis());
        assertNotNull(url);
        assertEquals("cyberduck.s3.eu-central-1.wasabisys.com", URI.create(url).getHost());
    }

    @Test
    public void testDefaultHostnameWithRegionWithProfile() throws Exception {
        final Calendar expiry = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        expiry.add(Calendar.MILLISECOND, (int) TimeUnit.DAYS.toMillis(7));
        final Host host = new Host(new S3Protocol() {
            @Override
            public String getDefaultHostname() {
                return "wasabisys.com";
            }
        }, "wasabisys.com");
        final S3Session session = new S3Session(host) {
            @Override
            public S3CredentialsStrategy getAuthentication() {
                return host::getCredentials;
            }
        };
        final String url = new S3PresignedUrlProvider(session).create(session.getAuthentication().get(),
                "cyberduck", "my-eu-central-1", "f", "GET", expiry.getTimeInMillis());
        assertNotNull(url);
        assertEquals("cyberduck.wasabisys.com", URI.create(url).getHost());
    }

    @Test
    public void testDefaultHostnameWithProfile() throws Exception {
        final Calendar expiry = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        expiry.add(Calendar.MILLISECOND, (int) TimeUnit.DAYS.toMillis(7));
        final Host host = new Host(new S3Protocol() {
            @Override
            public String getDefaultHostname() {
                return "h";
            }
        }, "h");
        final S3Session session = new S3Session(host) {
            @Override
            public S3CredentialsStrategy getAuthentication() {
                return host::getCredentials;
            }
        };
        final String url = new S3PresignedUrlProvider(session).create(session.getAuthentication().get(),
                "test-us-east-1-cyberduck", "us-east-1", "f", "GET", expiry.getTimeInMillis());
        assertNotNull(url);
        assertEquals("test-us-east-1-cyberduck.h", URI.create(url).getHost());
    }

    @Test
    public void testDnsBucketNamingDisabled() throws Exception {
        final Host host = new Host(new S3Protocol(), "mys3", new Credentials(
                ACCESS_KEY, SECRET
        )) {
            @Override
            public String getProperty(final String key) {
                if("s3.bucket.virtualhost.disable".equals(key)) {
                    return String.valueOf(true);
                }
                return super.getProperty(key);
            }
        };
        final S3Session session = new S3Session(host) {
            @Override
            public S3CredentialsStrategy getAuthentication() {
                return host::getCredentials;
            }
        };
        final Calendar expiry = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        expiry.add(Calendar.MILLISECOND, (int) TimeUnit.DAYS.toMillis(7));
        final String url = new S3PresignedUrlProvider(session).create(session.getAuthentication().get(),
                "test bucket", "region", "f", "GET", expiry.getTimeInMillis());
        assertNotNull(url);
        assertEquals("mys3", URI.create(url).getHost());
        assertEquals("/test%20bucket/f", URI.create(url).getRawPath());
        assertEquals("/test bucket/f", URI.create(url).getPath());
    }

    @Test
    public void testContextPath() throws Exception {
        final Calendar expiry = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        expiry.add(Calendar.MILLISECOND, (int) TimeUnit.DAYS.toMillis(7));
        final Host host = new Host(new S3Protocol() {
            @Override
            public String getContext() {
                return "/storage/v1/s3";
            }
        }, "example.supabase.co") {
            @Override
            public String getProperty(final String key) {
                if("s3.bucket.virtualhost.disable".equals(key)) {
                    return String.valueOf(true);
                }
                return super.getProperty(key);
            }
        };
        final S3Session session = new S3Session(host) {
            @Override
            public S3CredentialsStrategy getAuthentication() {
                return host::getCredentials;
            }
        };
        final String url = new S3PresignedUrlProvider(session).create(session.getAuthentication().get(),
                "bucket", "eu-west-1", "key.jpg", "GET", expiry.getTimeInMillis());
        assertNotNull(url);
        assertEquals("example.supabase.co", URI.create(url).getHost());
        assertEquals("/storage/v1/s3/bucket/key.jpg", URI.create(url).getPath());
    }

    private static final String ACCESS_KEY = "AKIDEXAMPLE";
    private static final String SECRET = "wJalrXUtnFEMI/K7MDENG/bPxRfiCYEXAMPLEKEY";

    @Test
    public void testVirtualHostContext() throws Exception {
        final String url = this.presign("/s3", "storage.example.com", false, "bucket", "object.txt");
        final URI uri = URI.create(url);
        assertEquals("bucket.storage.example.com", uri.getHost());
        assertEquals("/s3/object.txt", uri.getRawPath());
        this.assertSignature(url, "/s3/object.txt");
        this.assertSignatureMismatch(url, "/object.txt");
    }

    @Test
    public void testPathStyleContext() throws Exception {
        final String url = this.presign("/storage/v1/s3", "example.supabase.co", true, "bucket", "key.jpg");
        final URI uri = URI.create(url);
        assertEquals("example.supabase.co", uri.getHost());
        assertEquals("/storage/v1/s3/bucket/key.jpg", uri.getPath());
        assertEquals("/storage/v1/s3/bucket/key.jpg", uri.getRawPath());
        this.assertSignature(url, "/storage/v1/s3/bucket/key.jpg");
        this.assertSignatureMismatch(url, "/bucket/key.jpg");
    }

    @Test
    public void testNonDnsBucketContext() throws Exception {
        final String url = this.presign("/s3", "storage.example.com", false, "test bucket", "f");
        final URI uri = URI.create(url);
        assertEquals("storage.example.com", uri.getHost());
        assertEquals("/s3/test%20bucket/f", uri.getRawPath());
        this.assertSignature(url, "/s3/test%20bucket/f");
        this.assertSignatureMismatch(url, "/test%20bucket/f");
        this.assertSignatureMismatch(url, "/test%20bucket/s3/f");
    }

    @Test
    public void testEncodedKey() throws Exception {
        final String plain = this.presign(null, "storage.example.com", false, "bucket", "@f");
        assertTrue(plain, plain.startsWith("https://bucket.storage.example.com/%40f"));
        assertEquals("/%40f", URI.create(plain).getRawPath());
        this.assertSignature(plain, "/%40f");
        final String prefixed = this.presign("/s3", "storage.example.com", false, "bucket", "@f");
        final String raw = URI.create(prefixed).getRawPath();
        assertTrue(raw, raw.contains("/s3/") && raw.contains("%40"));
        this.assertSignature(prefixed, raw);
    }

    @Test
    public void testIgnoredContext() throws Exception {
        final String plain = this.presign(null, "storage.example.com", false, "bucket", "object.txt");
        final String root = this.presign("/", "storage.example.com", false, "bucket", "object.txt");
        final String metadata = this.presign("https://169.254.169.254/latest/meta-data/iam/security-credentials/",
                "storage.example.com", false, "bucket", "object.txt");
        assertEquals(URI.create(plain).getHost(), URI.create(root).getHost());
        assertEquals(URI.create(plain).getRawPath(), URI.create(root).getRawPath());
        assertEquals(URI.create(plain).getHost(), URI.create(metadata).getHost());
        assertEquals(URI.create(plain).getRawPath(), URI.create(metadata).getRawPath());
        this.assertSignature(root, URI.create(root).getRawPath());
        this.assertSignature(metadata, URI.create(metadata).getRawPath());
        this.assertSignatureMismatch(metadata, "/latest/meta-data/iam/security-credentials/object.txt");
    }

    @Test
    public void testNullKey() throws Exception {
        final String virtual = this.presign("/s3", "storage.example.com", false, "bucket", null);
        assertEquals("bucket.storage.example.com", URI.create(virtual).getHost());
        assertEquals("/s3", URI.create(virtual).getRawPath());
        this.assertSignature(virtual, "/s3");
        final String pathStyle = this.presign("/s3", "storage.example.com", true, "bucket", null);
        final String raw = URI.create(pathStyle).getRawPath();
        assertEquals("storage.example.com", URI.create(pathStyle).getHost());
        assertTrue(raw, "/s3/bucket".equals(raw) || "/s3/bucket/".equals(raw));
        this.assertSignature(pathStyle, raw);
        this.assertSignatureMismatch(pathStyle, "/bucket");
        this.assertSignatureMismatch(pathStyle, "/bucket/");
    }

    private String presign(final String context, final String hostname, final boolean disableVirtualHost,
                           final String bucket, final String key) throws Exception {
        final Host host = new Host(new S3Protocol() {
            @Override
            public String getContext() {
                return context;
            }
        }, hostname, new Credentials(ACCESS_KEY, SECRET)) {
            @Override
            public String getProperty(final String property) {
                if("s3.bucket.virtualhost.disable".equals(property)) {
                    return String.valueOf(disableVirtualHost);
                }
                return super.getProperty(property);
            }
        };
        final S3Session session = new S3Session(host) {
            @Override
            public S3CredentialsStrategy getAuthentication() {
                return host::getCredentials;
            }
        };
        final Calendar expiry = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        expiry.add(Calendar.HOUR, 1);
        return new S3PresignedUrlProvider(session).create(session.getAuthentication().get(),
                bucket, "eu-west-1", key, "GET", expiry.getTimeInMillis());
    }

    private void assertSignature(final String url, final String canonicalUri) throws Exception {
        assertEquals(url, this.parameter(URI.create(url).getRawQuery(), "X-Amz-Signature"), this.signature(url, canonicalUri));
    }

    private void assertSignatureMismatch(final String url, final String canonicalUri) throws Exception {
        assertNotEquals(url, this.parameter(URI.create(url).getRawQuery(), "X-Amz-Signature"), this.signature(url, canonicalUri));
    }

    private String signature(final String url, final String canonicalUri) throws Exception {
        final String rawQuery = URI.create(url).getRawQuery();
        final String credential = URLDecoder.decode(this.parameter(rawQuery, "X-Amz-Credential"), "UTF-8");
        final String scope = credential.substring(credential.indexOf('/') + 1);
        final String[] scopeParts = scope.split("/");
        final String dateStamp = scopeParts[0];
        final String region = scopeParts[1];
        final String canonicalRequest = "GET\n"
                + canonicalUri + "\n"
                + this.canonicalQuery(rawQuery) + "\n"
                + "host:" + URI.create(url).getHost() + "\n"
                + "\n"
                + "host\n"
                + "UNSIGNED-PAYLOAD";
        final String stringToSign = "AWS4-HMAC-SHA256\n"
                + this.parameter(rawQuery, "X-Amz-Date") + "\n"
                + scope + "\n"
                + this.hex(MessageDigest.getInstance("SHA-256").digest(canonicalRequest.getBytes(StandardCharsets.UTF_8)));
        byte[] key = ("AWS4" + SECRET).getBytes(StandardCharsets.UTF_8);
        key = this.hmac(key, dateStamp);
        key = this.hmac(key, region);
        key = this.hmac(key, "s3");
        key = this.hmac(key, "aws4_request");
        return this.hex(this.hmac(key, stringToSign));
    }

    private String canonicalQuery(final String rawQuery) {
        final List<String> pairs = new ArrayList<>();
        for(String pair : rawQuery.split("&")) {
            if(!pair.startsWith("X-Amz-Signature=")) {
                pairs.add(pair);
            }
        }
        Collections.sort(pairs);
        final StringBuilder canonical = new StringBuilder();
        for(String pair : pairs) {
            if(canonical.length() > 0) {
                canonical.append('&');
            }
            canonical.append(pair);
        }
        return canonical.toString();
    }

    private String parameter(final String rawQuery, final String name) {
        final String prefix = name + "=";
        for(String pair : rawQuery.split("&")) {
            if(pair.startsWith(prefix)) {
                return pair.substring(prefix.length());
            }
        }
        fail(name);
        return null;
    }

    private byte[] hmac(final byte[] key, final String data) throws Exception {
        final Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(key, "HmacSHA256"));
        return mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
    }

    private String hex(final byte[] data) {
        final StringBuilder hex = new StringBuilder(data.length * 2);
        for(byte value : data) {
            hex.append(String.format("%02x", value));
        }
        return hex.toString();
    }
}
