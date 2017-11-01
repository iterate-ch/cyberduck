package ch.cyberduck.core.s3;

import ch.cyberduck.core.Local;
import ch.cyberduck.core.Profile;
import ch.cyberduck.core.ProtocolFactory;
import ch.cyberduck.core.Scheme;
import ch.cyberduck.core.TestProtocol;
import ch.cyberduck.core.serializer.impl.dd.ProfilePlistReader;

import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;

import static org.junit.Assert.*;

public class S3ProtocolTest {

    @Test
    public void testPrefix() {
        assertEquals("ch.cyberduck.core.s3.S3", new S3Protocol().getPrefix());
    }

    @Test
    public void testConfigurable() {
        assertTrue(new S3Protocol().isHostnameConfigurable());
        assertTrue(new S3Protocol().isPortConfigurable());
    }

    @Test
    public void testSchemes() {
        assertTrue(Arrays.asList(new S3Protocol().getSchemes()).contains(Scheme.s3));
        assertTrue(Arrays.asList(new S3Protocol().getSchemes()).contains(Scheme.https));
    }

    @Test
    public void testEquals() throws Exception {
        final ProtocolFactory factory = new ProtocolFactory(new HashSet<>(Collections.singleton(new S3Protocol())));
        assertEquals(new ProfilePlistReader(factory).read(
            new Local("../profiles/S3 (Temporary Credentials).cyberduckprofile")),
            new ProfilePlistReader(factory).read(
                new Local("../profiles/S3 (Temporary Credentials).cyberduckprofile")));
    }

    @Test
    public void testCompareTo() throws Exception {
        final ProtocolFactory factory = new ProtocolFactory(new HashSet<>(Collections.singleton(new S3Protocol())));
        assertEquals(0, new ProfilePlistReader(factory).read(
            new Local("../profiles/S3 (Temporary Credentials).cyberduckprofile")).compareTo(new ProfilePlistReader(factory).read(
            new Local("../profiles/S3 (Temporary Credentials).cyberduckprofile"))));
        assertNotEquals(0, new ProfilePlistReader(factory).read(
            new Local("../profiles/S3 (Temporary Credentials).cyberduckprofile")).compareTo(new TestProtocol()));
    }

    @Test
    public void testCompareMultipleRegions() throws Exception {
        final ProtocolFactory factory = new ProtocolFactory(new HashSet<>(Collections.singleton(new S3Protocol())));
        assertEquals(0, new ProfilePlistReader(factory).read(
            new Local("../profiles/Verizon Cloud Storage (AMS1A).cyberduckprofile")).compareTo(new ProfilePlistReader(factory).read(
            new Local("../profiles/Verizon Cloud Storage (IAD3A).cyberduckprofile"))));
    }

    @Test
    public void testDefaultProfile() throws Exception {
        final ProtocolFactory factory = new ProtocolFactory(new HashSet<>(Collections.singleton(new S3Protocol())));
        final Profile profile = new ProfilePlistReader(factory).read(new Local("../profiles/default/S3 (HTTPS).cyberduckprofile"));
        assertTrue(profile.isHostnameConfigurable());
        assertTrue(profile.isPortConfigurable());
        assertTrue(profile.isUsernameConfigurable());
        assertTrue(profile.isPasswordConfigurable());
    }
}
