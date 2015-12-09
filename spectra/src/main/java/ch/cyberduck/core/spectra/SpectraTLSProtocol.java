package ch.cyberduck.core.spectra;

import ch.cyberduck.core.LocaleFactory;
import ch.cyberduck.core.Scheme;

/**
 * @version $Id:$
 */
public class SpectraTLSProtocol extends SpectraProtocol {

    @Override
    public String getName() {
        return "Spectra S3 (TLS)";
    }

    @Override
    public String getDescription() {
        return LocaleFactory.localizedString("Spectra S3 (TLS)", "S3");
    }

    @Override
    public Type getType() {
        return Type.spectra;
    }

    @Override
    public String getIdentifier() {
        return "spectra-tls";
    }

    @Override
    public Scheme getScheme() {
        return Scheme.https;
    }
}
