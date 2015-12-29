package ch.cyberduck.core;

import org.junit.Test;

import static org.junit.Assert.assertNotNull;

public class CrashReporterTest {

    @Test
    public void testCheckForCrash() throws Exception {
        final CrashReporter reporter = CrashReporter.create();
        assertNotNull(reporter);
        reporter.checkForCrash();
    }
}