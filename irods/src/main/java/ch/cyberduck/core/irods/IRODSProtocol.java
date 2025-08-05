package ch.cyberduck.core.irods;

import ch.cyberduck.core.AbstractProtocol;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Protocol;
import ch.cyberduck.core.Scheme;

import org.apache.commons.lang3.StringUtils;

import com.google.auto.service.AutoService;

@AutoService(Protocol.class)
public final class IRODSProtocol extends AbstractProtocol {

    @Override
    public String getIdentifier() {
        return this.getScheme().name();
    }

    @Override
    public String getDescription() {
        return LocaleFactory.localizedString("iRODS (Integrated Rule-Oriented Data System)");
    }

    @Override
    public DirectoryTimestamp getDirectoryTimestamp() {
        return DirectoryTimestamp.explicit;
    }

    @Override
    public Statefulness getStatefulness() {
        return Statefulness.stateful;
    }

    @Override
    public Scheme getScheme() {
        return Scheme.irods;
    }

    @Override
    public String disk() {
        return String.format("%s.tiff", "ftp");
    }

    @Override
    public String getPrefix() {
        return String.format("%s.%s", IRODSProtocol.class.getPackage().getName(), StringUtils.upperCase(this.getType().name()));
    }

    @Override
    public VersioningMode getVersioningMode() {
        return VersioningMode.none;
    }
}
