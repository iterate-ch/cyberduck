package ch.cyberduck.core.spectra;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class SpectraProtocolTest {

    @Test
    public void testPrefix() {
        assertEquals("ch.cyberduck.core.spectra.Spectra", new SpectraProtocol().getPrefix());
    }
}