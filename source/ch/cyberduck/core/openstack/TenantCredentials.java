package ch.cyberduck.core.openstack;

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.LocaleFactory;

/**
 * @version $Id:$
 */
public class TenantCredentials extends Credentials {

    @Override
    public String getUsernamePlaceholder() {
        return LocaleFactory.localizedString("Tenant", "Mosso");
    }
}
