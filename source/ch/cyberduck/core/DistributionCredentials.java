package ch.cyberduck.core;

import ch.cyberduck.core.i18n.Locale;

/**
 * @version $Id:$
 */
public final class DistributionCredentials extends Credentials {

    @Override
    public String getUsernamePlaceholder() {
        return Locale.localizedString("Access Key ID", "S3");
    }

    @Override
    public String getPasswordPlaceholder() {
        return Locale.localizedString("Secret Access Key", "S3");
    }
}
