package ch.cyberduck.core.dav;

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Move;

import java.io.IOException;

import com.github.sardine.impl.SardineException;

/**
 * @version $Id$
 */
public class DAVMoveFeature implements Move {

    private DAVSession session;

    public DAVMoveFeature(final DAVSession session) {
        this.session = session;
    }

    @Override
    public void move(final Path file, final Path renamed) throws BackgroundException {
        try {
            session.getClient().move(new DAVPathEncoder().encode(file), new DAVPathEncoder().encode(renamed));
        }
        catch(SardineException e) {
            throw new DAVExceptionMappingService().map("Cannot rename {0}", e, file);
        }
        catch(IOException e) {
            throw new DefaultIOExceptionMappingService().map(e, file);
        }
    }

    @Override
    public boolean isSupported(final Path file) {
        return true;
    }
}
