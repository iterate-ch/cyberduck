package ch.cyberduck.core.identity;

import ch.cyberduck.core.*;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.LoginFailureException;

import org.apache.log4j.Logger;

import java.util.concurrent.Callable;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import com.amazonaws.services.identitymanagement.model.*;

/**
 * @version $Id$
 */
public class AWSIdentityConfiguration implements IdentityConfiguration {
    private static Logger log = Logger.getLogger(AWSIdentityConfiguration.class);

    private LoginController prompt;

    private Host host;

    private UseragentProvider ua = new PreferencesUseragentProvider();

    private AmazonIdentityManagementClient client;

    /**
     * Prefix in preferences
     */
    private static final String prefix = "iam.";

    public AWSIdentityConfiguration(final Host host, final LoginController prompt) {
        this.host = host;
        this.prompt = prompt;
        final int timeout = Preferences.instance().getInteger("connection.timeout.seconds") * 1000;
        final ClientConfiguration configuration = new ClientConfiguration();
        configuration.setConnectionTimeout(timeout);
        configuration.setSocketTimeout(timeout);
        configuration.setUserAgent(ua.get());
        configuration.setMaxErrorRetry(0);
        configuration.setMaxConnections(1);
        configuration.setProxyHost(ProxyFactory.get().getHTTPSProxyHost(host));
        configuration.setProxyPort(ProxyFactory.get().getHTTPSProxyPort(host));
        // Create new IAM credentials
        client = new AmazonIdentityManagementClient(
                new com.amazonaws.auth.AWSCredentials() {
                    @Override
                    public String getAWSAccessKeyId() {
                        return host.getCredentials().getUsername();
                    }

                    @Override
                    public String getAWSSecretKey() {
                        return host.getCredentials().getPassword();
                    }
                }, configuration
        );
    }

    private <T> T authenticated(final Callable<T> run) throws BackgroundException {
        final LoginOptions options = new LoginOptions();
        options.anonymous = false;
        options.publickey = false;
        try {
            final KeychainLoginService login = new KeychainLoginService(prompt, PasswordStoreFactory.get());
            login.validate(host, LocaleFactory.localizedString("AWS Identity and Access Management", "S3"), options);
            return run.call();
        }
        catch(LoginFailureException failure) {
            prompt.prompt(host.getProtocol(), host.getCredentials(),
                    LocaleFactory.localizedString("Login failed", "Credentials"), failure.getMessage(), options);
            return this.authenticated(run);
        }
        catch(BackgroundException e) {
            throw e;
        }
        catch(Exception e) {
            throw new BackgroundException(e);
        }
    }

    @Override
    public void deleteUser(final String username) throws BackgroundException {
        this.authenticated(new Callable<Void>() {
            @Override
            public Void call() throws BackgroundException {
                Preferences.instance().deleteProperty(String.format("%s%s", prefix, username));
                try {
                    final ListAccessKeysResult keys
                            = client.listAccessKeys(new ListAccessKeysRequest().withUserName(username));

                    for(AccessKeyMetadata key : keys.getAccessKeyMetadata()) {
                        client.deleteAccessKey(new DeleteAccessKeyRequest(key.getAccessKeyId()).withUserName(username));
                    }

                    final ListUserPoliciesResult policies = client.listUserPolicies(new ListUserPoliciesRequest(username));
                    for(String policy : policies.getPolicyNames()) {
                        client.deleteUserPolicy(new DeleteUserPolicyRequest(username, policy));
                    }
                    client.deleteUser(new DeleteUserRequest(username));
                }
                catch(NoSuchEntityException e) {
                    log.warn(String.format("User %s already removed", username));
                }
                catch(AmazonServiceException e) {
                    throw new AmazonServiceExceptionMappingService().map("Cannot write user configuration", e);
                }
                return null;
            }
        });
    }

    @Override
    public Credentials getUserCredentials(final String username) {
        // Resolve access key id
        final String id = Preferences.instance().getProperty(String.format("%s%s", prefix, username));
        if(null == id) {
            log.warn(String.format("No access key found for user %s", username));
            return null;
        }
        return new Credentials(id, PasswordStoreFactory.get().getPassword(host.getProtocol().getScheme(), host.getPort(),
                host.getHostname(), id)) {
            @Override
            public String getUsernamePlaceholder() {
                return host.getProtocol().getUsernamePlaceholder();
            }

            @Override
            public String getPasswordPlaceholder() {
                return host.getProtocol().getPasswordPlaceholder();
            }
        };
    }

    @Override
    public void createUser(final String username, final String policy) throws BackgroundException {
        this.authenticated(new Callable<Void>() {
            @Override
            public Void call() throws BackgroundException {
                try {
                    // Create new IAM credentials
                    final User user = client.createUser(new CreateUserRequest().withUserName(username)).getUser();
                    final CreateAccessKeyResult key = client.createAccessKey(
                            new CreateAccessKeyRequest().withUserName(user.getUserName()));

                    // Write policy document to get read access
                    client.putUserPolicy(new PutUserPolicyRequest(user.getUserName(), "Policy", policy));
                    // Map virtual user name to IAM access key
                    final String id = key.getAccessKey().getAccessKeyId();
                    Preferences.instance().setProperty(String.format("%s%s", prefix, username), id);
                    // Save secret
                    PasswordStoreFactory.get().addPassword(
                            host.getProtocol().getScheme(), host.getPort(), host.getHostname(),
                            id, key.getAccessKey().getSecretAccessKey());
                }
                catch(AmazonServiceException e) {
                    throw new AmazonServiceExceptionMappingService().map("Cannot write user configuration", e);
                }
                return null;
            }
        });
    }
}
