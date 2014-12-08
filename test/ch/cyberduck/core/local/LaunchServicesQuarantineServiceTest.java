package ch.cyberduck.core.local;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Factory;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.NullLocal;
import ch.cyberduck.core.test.Depends;

import org.apache.commons.lang3.StringUtils;
import org.junit.Test;

import java.util.UUID;
import java.util.concurrent.Callable;

/**
 * @version $Id$
 */
@Depends(platform = Factory.Platform.Name.mac)
public class LaunchServicesQuarantineServiceTest extends AbstractTestCase {

    @Test
    public void testSetQuarantineEmptyUrl() throws Exception {
        final QuarantineService q = new LaunchServicesQuarantineService();
        q.setQuarantine(new NullLocal("/", "n"), null, null);
        q.setQuarantine(new NullLocal("/", "n"), StringUtils.EMPTY, StringUtils.EMPTY);
    }

    @Test
    public void testSetWhereEmptyUrl() throws Exception {
        final QuarantineService q = new LaunchServicesQuarantineService();
        q.setWhereFrom(new NullLocal("/", "n"), null);
        q.setWhereFrom(new NullLocal("/", "n"), StringUtils.EMPTY);
    }

    @Test
    public void testSetQuarantine() throws Exception {
        final QuarantineService q = new LaunchServicesQuarantineService();
        Callable<Local> c = new Callable<Local>() {
            @Override
            public Local call() throws Exception {
                final NullLocal l = new NullLocal(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
                LocalTouchFactory.get().touch(l);
                q.setQuarantine(l,
                        "http://cyberduck.ch", "http://cyberduck.ch");
                l.delete();
                return l;
            }
        };
        this.repeat(c, 20);
    }

    @Test
    public void testSetWhereFrom() throws Exception {
        final QuarantineService q = new LaunchServicesQuarantineService();
        Callable<Local> c = new Callable<Local>() {
            @Override
            public Local call() throws Exception {
                final NullLocal l = new NullLocal(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
                LocalTouchFactory.get().touch(l);
                q.setWhereFrom(l,
                        "http://cyberduck.ch");
                l.delete();
                return l;
            }
        };
        this.repeat(c, 20);
    }
}