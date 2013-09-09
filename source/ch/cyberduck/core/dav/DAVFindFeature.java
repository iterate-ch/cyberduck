package ch.cyberduck.core.dav;

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.features.Find;
import ch.cyberduck.core.shared.DefaultFindFeature;

import java.io.IOException;

import com.github.sardine.impl.SardineException;

/**
 * @version $Id$
 */
public class DAVFindFeature implements Find {

    private DAVSession session;

    public DAVFindFeature(final DAVSession session) {
        this.session = session;
    }

    @Override
    public boolean find(final Path file) throws BackgroundException {
        if(new DefaultFindFeature(session).find(file)) {
            return true;
        }
        if(file.attributes().isDirectory()) {
            // Parent directory may not be accessible. Issue #5662
            try {
                try {
                    return session.getClient().exists(new DAVPathEncoder().encode(file));
                }
                catch(SardineException e) {
                    throw new DAVExceptionMappingService().map("Cannot read file attributes", e, file);
                }
                catch(IOException e) {
                    throw new DefaultIOExceptionMappingService().map(e, file);
                }
            }
            catch(NotfoundException e) {
                return false;
            }
        }
        return false;
    }
}
