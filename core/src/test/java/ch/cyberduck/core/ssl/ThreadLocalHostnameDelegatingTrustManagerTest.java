package ch.cyberduck.core.ssl;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ThreadLocalHostnameDelegatingTrustManagerTest {

    @Test
    public void testSetTarget() {
        assertEquals("s3.amazonaws.com",
                new ThreadLocalHostnameDelegatingTrustManager(new DisabledX509TrustManager(), "s3.amazonaws.com").getTarget());
        assertEquals("cyberduck.s3.amazonaws.com",
                new ThreadLocalHostnameDelegatingTrustManager(new DisabledX509TrustManager(), "cyberduck.s3.amazonaws.com").getTarget());
        assertEquals("cyber.duck.s3.amazonaws.com",
                new ThreadLocalHostnameDelegatingTrustManager(new DisabledX509TrustManager(), "cyber.duck.s3.amazonaws.com").getTarget());
    }
}