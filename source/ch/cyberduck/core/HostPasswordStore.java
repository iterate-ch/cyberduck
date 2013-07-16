package ch.cyberduck.core;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * @version $Id:$
 */
public abstract class HostPasswordStore implements PasswordStore {
    private static final Logger log = Logger.getLogger(KeychainLoginService.class);

    /**
     * @param host Hostname
     * @return the password fetched from the keychain or null if it was not found
     */
    public String find(final Host host) {
        if(log.isInfoEnabled()) {
            log.info(String.format("Fetching password from keychain for %s", host));
        }
        if(StringUtils.isEmpty(host.getHostname())) {
            log.warn("No hostname given");
            return null;
        }
        Credentials credentials = host.getCredentials();
        if(StringUtils.isEmpty(credentials.getUsername())) {
            log.warn("No username given");
            return null;
        }
        String p;
        if(credentials.isPublicKeyAuthentication()) {
            p = this.getPassword(host.getHostname(), credentials.getIdentity().getAbbreviatedPath());
            if(null == p) {
                // Interoperability with OpenSSH (ssh, ssh-agent, ssh-add)
                p = this.getPassword("SSH", credentials.getIdentity().getAbsolute());
            }
            if(null == p) {
                // Backward compatibility
                p = this.getPassword("SSHKeychain", credentials.getIdentity().getAbbreviatedPath());
            }
        }
        else {
            p = this.getPassword(host.getProtocol().getScheme(), host.getPort(),
                    host.getHostname(), credentials.getUsername());
        }
        if(null == p) {
            if(log.isInfoEnabled()) {
                log.info(String.format("Password not found in keychain for %s", host));
            }
        }
        return p;
    }

    /**
     * Adds the password to the login keychain
     *
     * @param host Hostname
     * @see ch.cyberduck.core.Host#getCredentials()
     */
    public void save(final Host host) {
        if(StringUtils.isEmpty(host.getHostname())) {
            log.warn("No hostname given");
            return;
        }
        final Credentials credentials = host.getCredentials();
        if(!credentials.isSaved()) {
            if(log.isInfoEnabled()) {
                log.info(String.format("Skip writing credentials for host %s", host.getHostname()));
            }
            return;
        }
        if(StringUtils.isEmpty(credentials.getUsername())) {
            log.warn(String.format("No username in credentials for host %s", host.getHostname()));
            return;
        }
        if(StringUtils.isEmpty(credentials.getPassword())) {
            log.warn(String.format("No password in credentials for host %s", host.getHostname()));
            return;
        }
        if(credentials.isAnonymousLogin()) {
            if(log.isInfoEnabled()) {
                log.info(String.format("Do not write anonymous credentials for host %s", host.getHostname()));
            }
            return;
        }
        if(log.isInfoEnabled()) {
            log.info(String.format("Add password for host %s", host));
        }
        if(credentials.isPublicKeyAuthentication()) {
            this.addPassword(host.getHostname(), credentials.getIdentity().getAbbreviatedPath(),
                    credentials.getPassword());
        }
        else {
            this.addPassword(host.getProtocol().getScheme(), host.getPort(),
                    host.getHostname(), credentials.getUsername(), credentials.getPassword());
        }
    }
}
