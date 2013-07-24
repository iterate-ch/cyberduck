package ch.cyberduck.core;

/**
 * @version $Id$
 */
public final class DistributionCredentials extends Credentials {

    @Override
    public String getUsernamePlaceholder() {
        return LocaleFactory.localizedString("Access Key ID", "S3");
    }

    @Override
    public String getPasswordPlaceholder() {
        return LocaleFactory.localizedString("Secret Access Key", "S3");
    }
}
