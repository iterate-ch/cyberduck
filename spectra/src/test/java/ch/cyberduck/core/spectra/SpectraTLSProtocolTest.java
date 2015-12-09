package ch.cyberduck.core.spectra;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @version $Id:$
 */
public class SpectraTLSProtocolTest {

    @Test
    public void testPrefix() {
        assertEquals("ch.cyberduck.core.spectra.Spectra", new SpectraTLSProtocol().getPrefix());
    }
}