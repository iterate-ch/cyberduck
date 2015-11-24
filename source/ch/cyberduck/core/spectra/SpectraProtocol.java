package ch.cyberduck.core.spectra;

import ch.cyberduck.core.AbstractProtocol;
import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Scheme;

/**
 * @version $Id:$
 */
public class SpectraProtocol extends AbstractProtocol {
    @Override
    public String getName() {
        return "Spectra S3";
    }

    @Override
    public String getDescription() {
        return LocaleFactory.localizedString("Spectra S3", "S3");
    }

    @Override
    public String getIdentifier() {
        return "spectra";
    }

    @Override
    public boolean isPortConfigurable() {
        return false;
    }

    @Override
    public Scheme getScheme() {
        return Scheme.http;
    }

    @Override
    public boolean isHostnameConfigurable() {
        return true;
    }

    @Override
    public String getUsernamePlaceholder() {
        return LocaleFactory.localizedString("Access Key ID", "S3");
    }

    @Override
    public String getPasswordPlaceholder() {
        return LocaleFactory.localizedString("Secret Access Key", "S3");
    }
}
