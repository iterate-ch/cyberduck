package ch.cyberduck.core.dav;

import ch.cyberduck.core.DefaultIOExceptionMappingService;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.LoginController;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.features.Delete;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import com.github.sardine.impl.SardineException;

/**
 * @version $Id$
 */
public class DAVDeleteFeature implements Delete {

    private DAVSession session;

    public DAVDeleteFeature(final DAVSession session) {
        this.session = session;
    }

    @Override
    public void delete(final List<Path> files, final LoginController prompt) throws BackgroundException {
        final List<Path> deleted = new ArrayList<Path>();
        for(Path file : files) {
            boolean skip = false;
            for(Path d : deleted) {
                if(file.isChild(d)) {
                    skip = true;
                    break;
                }
            }
            if(skip) {
                continue;
            }
            session.message(MessageFormat.format(LocaleFactory.localizedString("Deleting {0}", "Status"),
                    file.getName()));
            try {
                session.getClient().delete(new DAVPathEncoder().encode(file));
            }
            catch(SardineException e) {
                throw new DAVExceptionMappingService().map("Cannot delete {0}", e, file);
            }
            catch(IOException e) {
                throw new DefaultIOExceptionMappingService().map(e, file);
            }
            deleted.add(file);
        }
    }
}
