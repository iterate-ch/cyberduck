package ch.cyberduck.core.io;

import ch.cyberduck.core.AbstractTestCase;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

import net.schmizz.sshj.userauth.keyprovider.FileKeyProvider;
import net.schmizz.sshj.userauth.keyprovider.OpenSSHKeyFile;

import static org.junit.Assert.assertEquals;

/**
 * @version $Id$
 */
public class MD5ChecksumComputeTest extends AbstractTestCase {

    @Test
    public void testCompute() throws Exception {
        assertEquals("a43c1b0aa53a0c908810c06ab1ff3967",
                new MD5ChecksumCompute().compute(IOUtils.toInputStream("input")).hash);
    }

    @Test
    public void testFingerprint() throws Exception {
        FileKeyProvider f = new OpenSSHKeyFile.Factory().create();
        f.init("", "ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQC/71hmi4R+CZqGvZ+aVdaKIt5yb2H87yNAAcdtPAQBJBqKw/vR0iYeU/tnwKWRfnTK/NcN2H6yG/wx0o9WiavUhUaSUPesJo3/PpZ7fZMUk/Va8I7WI0i25XlWJTE8SMFftIuJ8/AVPNSCmL46qy93BlQb8W70O9XQD/yj/Cy6aPb9wlHxdaswrmdoIzI4BS28Tu1F45TalqarqTLm3wY4RpghxHo8LxCgNbmd0cr6XnOmz1RM+rlbkiuSdNphW3Ah2iCHMif/KdRCFCPi5LyUrdheOtQYvQCmFREczb3kyuQPCElQac4DeL37F9ZLLBHnRVi7KxFqDbcbNLadfExx dkocher@osaka.local");
        assertEquals("87:60:23:a3:56:b5:1a:24:8b:63:43:ea:5a:d4:e1:9d",
                new MD5ChecksumCompute().fingerprint(f.getPublic())
        );
    }
}
