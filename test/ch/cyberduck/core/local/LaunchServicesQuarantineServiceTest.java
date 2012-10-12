package ch.cyberduck.core.local;

import ch.cyberduck.core.AbstractTestCase;
import ch.cyberduck.core.Local;
import ch.cyberduck.core.NullLocal;

import org.junit.Before;
import org.junit.Test;

import java.util.UUID;
import java.util.concurrent.Callable;

/**
 * @version $Id:$
 */
public class LaunchServicesQuarantineServiceTest extends AbstractTestCase {
    @Before
    @Override
    public void register() {
        super.register();
        LaunchServicesQuarantineService.register();
    }

    @Test
    public void testSetQuarantine() throws Exception {
        final QuarantineService q = QuarantineServiceFactory.instance();
        Callable<Local> c = new Callable<Local>() {
            @Override
            public Local call() throws Exception {
                final NullLocal l = new NullLocal(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
                l.touch();
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
        final QuarantineService q = QuarantineServiceFactory.instance();
        Callable<Local> c = new Callable<Local>() {
            @Override
            public Local call() throws Exception {
                final NullLocal l = new NullLocal(System.getProperty("java.io.tmpdir"), UUID.randomUUID().toString());
                l.touch();
                q.setWhereFrom(l,
                        "http://cyberduck.ch");
                l.delete();
                return l;
            }
        };
        this.repeat(c, 20);
    }
}