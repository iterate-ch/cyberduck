package ch.cyberduck.ui.cocoa;

import ch.cyberduck.binding.HyperlinkAttributedStringFactory;
import ch.cyberduck.core.DescriptiveUrl;
import ch.cyberduck.core.Local;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class HyperlinkAttributedStringFactoryTest {

    @Test
    public void testCreate() {
        assertEquals("", HyperlinkAttributedStringFactory.create((DescriptiveUrl.EMPTY)).string());
        assertEquals("", HyperlinkAttributedStringFactory.create((String) null).string());
        assertEquals("ftp://localhost/d", HyperlinkAttributedStringFactory.create("ftp://localhost/d").string());
    }

    @Test
    public void testCreateNull() {
        assertEquals("", HyperlinkAttributedStringFactory.create(new DescriptiveUrl(StringUtils.EMPTY)).string());
        assertEquals("", HyperlinkAttributedStringFactory.create((String) null).string());
        assertEquals("", HyperlinkAttributedStringFactory.create("", (Local) null).string());
    }
}
