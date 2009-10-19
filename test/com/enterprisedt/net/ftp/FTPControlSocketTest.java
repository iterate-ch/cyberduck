package com.enterprisedt.net.ftp;

import junit.framework.Test;
import junit.framework.TestSuite;
import junit.framework.TestCase;
import ch.cyberduck.core.AbstractTestCase;

/**
 * FTPControlSocket Tester.
 *
 * @author <Authors name>
 * @since <pre>01/19/2007</pre>
 * @version 1.0
 */
public class FTPControlSocketTest extends AbstractTestCase {
    public FTPControlSocketTest(String name) {
        super(name);
    }

    @Override public void setUp() {
        super.setUp();
    }

    @Override public void tearDown() throws Exception {
        super.tearDown();
    }

    public void testParsePASVResponse() throws Exception {
        FTPControlSocket s = new FTPControlSocket("utf-8", new FTPMessageListener() {
            public void logCommand(String cmd) {
            }

            public void logReply(String reply) {
            }
        });
        try {
            s.parsePASVResponse("227 (212,27,40,254,15,161)");
        }
        catch(FTPException e) {
            fail(e.getMessage());
        }
        try {
            s.parsePASVResponse("227 (212,27,40,254,13,83)");
        }
        catch(FTPException e) {
            fail(e.getMessage());
        }
        try {
            s.parsePASVResponse("227 Entering Passive Mode (130,59,10,34,217,171)");
        }
        catch(FTPException e) {
            fail(e.getMessage());
        }
    }

    public static Test suite() {
        return new TestSuite(FTPControlSocketTest.class);
    }
}
