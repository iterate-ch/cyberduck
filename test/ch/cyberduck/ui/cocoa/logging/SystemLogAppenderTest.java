package ch.cyberduck.ui.cocoa.logging;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Factory;
import ch.cyberduck.core.test.Depends;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @version $Id$
 */
@Ignore
@Depends(platform = Factory.Platform.Name.mac)
public class SystemLogAppenderTest extends AbstractTestCase {

    @Test
    public void testAppend() throws Exception {
        final SystemLogAppender a = new SystemLogAppender();
        a.setLayout(new SimpleLayout());
        a.append(new LoggingEvent("f", Logger.getLogger(SystemLogAppender.class),
                Level.ERROR, "Test", new RuntimeException()));
    }
}
