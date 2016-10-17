package ch.cyberduck.core.logging;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.SimpleLayout;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.Test;

public class SystemLogAppenderTest {

    @Test
    public void testAppend() throws Exception {
        final SystemLogAppender a = new SystemLogAppender();
        a.setLayout(new SimpleLayout());
        a.append(new LoggingEvent("f", Logger.getLogger(SystemLogAppender.class),
                Level.ERROR, "Test", new RuntimeException()));
    }
}
