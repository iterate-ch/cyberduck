// Testing FTP PASV response parsing done in com.enterprisedt.net.ftp.FTPControlSocket's createDataSocketPASV().

package com.enterprisedt.net.ftp;

public class FTPPasvResponseTest extends junit.framework.TestCase {
    public FTPPasvResponseTest(String name) {
        super(name);
    }
    
//    public void testCommonResponse() throws Exception {
//        int actual[] = FTPControlSocket.parsePASVResponse("227 Entering Passive Mode (10,1,2,3,9,136).");
//        assertEquals(6, actual.length);
//        assertEquals(10, actual[0]);
//        assertEquals(1, actual[1]);
//        assertEquals(2, actual[2]);
//        assertEquals(3, actual[3]);
//        assertEquals(9, actual[4]);
//        assertEquals(136, actual[5]);
//    }
//
//    public void testIBMMainframeNoBrackets() throws Exception {
//        int actual[] = FTPControlSocket.parsePASVResponse("227 Entering Passive Mode 10,1,2,3,15,87.");
//        assertEquals(6, actual.length);
//        assertEquals(10, actual[0]);
//        assertEquals(1, actual[1]);
//        assertEquals(2, actual[2]);
//        assertEquals(3, actual[3]);
//        assertEquals(15, actual[4]);
//        assertEquals(87, actual[5]);
//    }
//
//    public void testPublicfileMinimalistic() throws Exception {
//        int actual[] = FTPControlSocket.parsePASVResponse("227 =10,1,2,3,7,8");
//        assertEquals(6, actual.length);
//        assertEquals(10, actual[0]);
//        assertEquals(1, actual[1]);
//        assertEquals(2, actual[2]);
//        assertEquals(3, actual[3]);
//        assertEquals(7, actual[4]);
//        assertEquals(8, actual[5]);
//    }
//
//    public void testJunkAfter() throws Exception {
//        int actual[] = FTPControlSocket.parsePASVResponse("227 =10,1,2,3,7,8meaningless junk");
//        assertEquals(6, actual.length);
//        assertEquals(10, actual[0]);
//        assertEquals(1, actual[1]);
//        assertEquals(2, actual[2]);
//        assertEquals(3, actual[3]);
//        assertEquals(7, actual[4]);
//        assertEquals(8, actual[5]);
//    }
//
//    public void testNothingInFront() throws Exception {
//        int actual[] = FTPControlSocket.parsePASVResponse("227 10,1,2,3,7,8");
//        assertEquals(6, actual.length);
//        assertEquals(10, actual[0]);
//        assertEquals(1, actual[1]);
//        assertEquals(2, actual[2]);
//        assertEquals(3, actual[3]);
//        assertEquals(7, actual[4]);
//        assertEquals(8, actual[5]);
//    }
//
//    public void testRequiresComma() throws Exception {
//        boolean caught = false;
//        try { FTPControlSocket.parsePASVResponse("227 10x1,2,3,7,8");
//        } catch (FTPException expected) { caught = true; }
//        if (!caught) fail("first comma");
//
//        caught = false;
//        try { FTPControlSocket.parsePASVResponse("227 10,1x2,3,7,8");
//        } catch (FTPException expected) { caught = true; }
//        if (!caught) fail("second comma");
//
//        caught = false;
//        try { FTPControlSocket.parsePASVResponse("227 10,1,2x3,7,8");
//        } catch (FTPException expected) { caught = true; }
//        if (!caught) fail("third comma");
//
//        caught = false;
//        try { FTPControlSocket.parsePASVResponse("227 10,1,2,3x7,8");
//        } catch (FTPException expected) { caught = true; }
//        if (!caught) fail("fourth comma");
//
//        caught = false;
//        try { FTPControlSocket.parsePASVResponse("227 10,1,2,3,7x8");
//        } catch (FTPException expected) { caught = true; }
//        if (!caught) fail("fifth comma");
//    }
//
//    public void testEmpty() {
//        boolean caught = false;
//        try { FTPControlSocket.parsePASVResponse("227 10,,2,3,7,8");
//        } catch (FTPException expected) { caught = true; }
//        if (!caught) fail("empty");
//    }
//
//    public void testIncomplete() {
//        boolean caught = false;
//        try { FTPControlSocket.parsePASVResponse("227 10,1,2,3,7,");
//        } catch (FTPException expected) { caught = true; }
//        if (!caught) fail("incomplete");
//
//        caught = false;
//        try { FTPControlSocket.parsePASVResponse("227 10,1,2,3");
//        } catch (FTPException expected) { caught = true; }
//        if (!caught) fail("incomplete");
//    }
}
