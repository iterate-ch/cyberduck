package ch.cyberduck.core;

import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.local.DefaultLocalDirectoryFeature;
import ch.cyberduck.core.preferences.SupportDirectoryFinderFactory;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;

public class UnsecureHostPasswordStore extends DefaultHostPasswordStore {
    private static final Logger log = LogManager.getLogger(UnsecureHostPasswordStore.class);

    private final Local file = LocalFactory.get(SupportDirectoryFinderFactory.get().find(), "credentials");

    private Properties load() {
        final Properties properties = new Properties();
        try {
            new DefaultLocalDirectoryFeature().mkdir(file.getParent());
        }
        catch(AccessDeniedException e) {
            log.warn(String.format("Failure saving credentials to %s. %s", file.getAbsolute(), e));
        }
        if(file.exists()) {
            try {
                try (InputStream in = file.getInputStream()) {
                    properties.load(in);
                }
            }
            catch(IllegalArgumentException | AccessDeniedException | IOException e) {
                log.warn(String.format("Failure reading credentials from %s. %s", file.getAbsolute(), e));
            }
        }
        return properties;
    }

    private void save(final Properties properties) {
        try (OutputStream out = file.getOutputStream(false)) {
            properties.store(out, "Credentials");
        }
        catch(AccessDeniedException e) {
            log.warn(String.format("Failure saving credentials to %s. %s", file.getAbsolute(), e));
        }
        catch(IOException e) {
            log.warn(String.format("Failure saving credentials to %s. %s", file.getAbsolute(), e.getMessage()));
        }
    }

    @Override
    public String getPassword(final String serviceName, final String accountName) {
        return this.load().getProperty(String.format("%s@%s", accountName, serviceName), null);
    }

    @Override
    public void addPassword(final String serviceName, final String accountName, final String password) {
        final Properties properties = this.load();
        properties.setProperty(String.format("%s@%s", accountName, serviceName), password);
        this.save(properties);
    }

    @Override
    public String getPassword(final Scheme scheme, final int port, final String hostname, final String user) {
        return this.load().getProperty(String.format("%s://%s@%s:%d", scheme, user, hostname, port), null);
    }

    @Override
    public void addPassword(final Scheme scheme, final int port, final String hostname, final String user, final String password) {
        final Properties properties = this.load();
        properties.setProperty(String.format("%s://%s@%s:%d", scheme, user, hostname, port), password);
        this.save(properties);
    }

    @Override
    public void deletePassword(final String serviceName, final String accountName) {
        final Properties properties = this.load();
        properties.remove(String.format("%s@%s", accountName, serviceName));
        this.save(properties);
    }

    @Override
    public void deletePassword(final Scheme scheme, final int port, final String hostname, final String user) {
        final Properties properties = this.load();
        properties.remove(String.format("%s://%s@%s:%d", scheme, user, hostname, port));
        this.save(properties);
    }
}
