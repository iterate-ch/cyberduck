package ch.cyberduck.core.logging;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.message.SimpleMessage;
import org.junit.Test;

public class SystemLogAppenderTest {

    @Test
    public void testAppend() {
        final SystemLogAppender a = new SystemLogAppender(PatternLayout.newBuilder().withPattern("%level - %m%n").build());
        a.append(new Log4jLogEvent.Builder().setLoggerName(SystemLogAppender.class.getCanonicalName()).setLevel(Level.ERROR).setThrown(new RuntimeException()).setMessage(new SimpleMessage("Test")).build());
    }
}
