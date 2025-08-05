package ch.cyberduck.core.irods;

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.Path;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.shared.AbstractHomeFeature;

import org.apache.commons.lang3.StringUtils;

import java.util.EnumSet;

public class IRODSHomeFinderService extends AbstractHomeFeature {

    private final IRODSSession session;

    public IRODSHomeFinderService(final IRODSSession session) {
        this.session = session;
    }

    @Override
    public Path find() throws BackgroundException {
        final String user;
        final Credentials credentials = session.getHost().getCredentials();
        if(StringUtils.contains(credentials.getUsername(), ':')) {
            user = StringUtils.splitPreserveAllTokens(credentials.getUsername(), ':')[1];
        }
        else {
            user = credentials.getUsername();
        }
        return new Path(new StringBuilder()
                .append(Path.DELIMITER).append(session.getRegion())
                .append(Path.DELIMITER).append("home")
                .append(Path.DELIMITER).append(user)
                .toString(), EnumSet.of(Path.Type.directory, Path.Type.volume));
    }
}
