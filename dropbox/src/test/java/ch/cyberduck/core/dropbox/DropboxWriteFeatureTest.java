package ch.cyberduck.core.dropbox;

import ch.cyberduck.core.*;
import ch.cyberduck.core.exception.LoginCanceledException;
import ch.cyberduck.core.transfer.TransferStatus;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

import java.io.OutputStream;
import java.util.EnumSet;
import java.util.UUID;

import static org.junit.Assert.fail;

public class DropboxWriteFeatureTest {
    @Test
    public void writeTest() throws Exception {

        //Preferences preferences = PreferencesFactory.get();
        //System.out.println(preferences.getProperty("dropbox.client.id"));
        //System.out.println(preferences.getProperty("dropbox.client.secret"));
        //System.out.println("access token = "+System.getProperties().getProperty("dropbox.accesstoken"));

        Credentials c = new Credentials();
        //c.setUsername("KhasDenis@gmail.com");
        //c.setPassword("KhasDenis");
        final Host host = new Host(new DropboxProtocol(), "www.dropbox.com", c);
        DropboxSession session = new DropboxSession(host);

        new LoginConnectionService(new DisabledLoginCallback() {
            @Override
            public void prompt(final Host bookmark, final Credentials credentials, final String title, final String reason, final LoginOptions options) throws LoginCanceledException {
                fail(reason);
            }
        }, new DisabledHostKeyCallback(),
                new DisabledPasswordStore() {
                    @Override
                    public String getPassword(Scheme scheme, int port, String hostname, String user) {
                        if(user.equals("Dropbox OAuth2 Access Token")) {
                            return System.getProperties().getProperty("dropbox.accesstoken");
                        }
                        fail("Dropbox access token not found.");
                        return null;
                    }

                    @Override
                    public String getPassword(String hostname, String user) {
                        return super.getPassword(hostname, user);
                    }
                }, new DisabledProgressListener(),
                new DisabledTranscriptListener()).connect(session, PathCache.empty());

        DropboxWriteFeature write = new DropboxWriteFeature(session);

        System.out.println("session.getAccessToken() = "+session.getAccessToken());

        //session.getClient().getDbxClient().

        final Local local = new Local(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
        final byte[] content = "test".getBytes("UTF-8");
        final OutputStream out = local.getOutputStream(false);
        IOUtils.write(content, out);
        IOUtils.closeQuietly(out);

        TransferStatus status = new TransferStatus();
        status.setLength(content.length);

        final Path test = new Path(Path.HOME, EnumSet.of(Path.Type.file));
        final OutputStream out2 = write.write(test, status);
        out2.close();
    }
}
