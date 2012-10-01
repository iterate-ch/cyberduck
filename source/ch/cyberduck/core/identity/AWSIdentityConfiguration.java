package ch.cyberduck.core.identity;

import ch.cyberduck.core.Credentials;
import ch.cyberduck.core.ErrorListener;
import ch.cyberduck.core.Host;
import ch.cyberduck.core.KeychainFactory;
import ch.cyberduck.core.Preferences;
import ch.cyberduck.core.PreferencesUseragentProvider;
import ch.cyberduck.core.ProxyFactory;
import ch.cyberduck.core.threading.BackgroundException;

import org.apache.log4j.Logger;

import com.amazonaws.AmazonClientException;
import com.amazonaws.ClientConfiguration;
import com.amazonaws.services.identitymanagement.AmazonIdentityManagementClient;
import com.amazonaws.services.identitymanagement.model.*;

/**
 * @version $Id$
 */
public class AWSIdentityConfiguration implements IdentityConfiguration {
    private static Logger log = Logger.getLogger(AWSIdentityConfiguration.class);

    private Host host;

    /**
     * Callback
     */
    private ErrorListener listener;

    private PreferencesUseragentProvider ua;

    /**
     * Prefix in preferences
     */
    private static final String prefix = "iam.";

    public AWSIdentityConfiguration(final Host host, final ErrorListener listener) {
        this.host = host;
        this.listener = listener;
    }

    @Override
    public void deleteUser(final String username) {
        Preferences.instance().deleteProperty(String.format("%s%s", prefix, username));
        try {
            // Create new IAM credentials
            AmazonIdentityManagementClient iam = new AmazonIdentityManagementClient(
                    new com.amazonaws.auth.AWSCredentials() {
                        @Override
                        public String getAWSAccessKeyId() {
                            return host.getCredentials().getUsername();
                        }

                        @Override
                        public String getAWSSecretKey() {
                            return host.getCredentials().getPassword();
                        }
                    }
            );
            final ListAccessKeysResult keys
                    = iam.listAccessKeys(new ListAccessKeysRequest().withUserName(username));

            for(AccessKeyMetadata key : keys.getAccessKeyMetadata()) {
                iam.deleteAccessKey(new DeleteAccessKeyRequest(key.getAccessKeyId()).withUserName(username));
            }

            final ListUserPoliciesResult policies = iam.listUserPolicies(new ListUserPoliciesRequest(username));
            for(String policy : policies.getPolicyNames()) {
                iam.deleteUserPolicy(new DeleteUserPolicyRequest(username, policy));
            }
            iam.deleteUser(new DeleteUserRequest(username));
        }
        catch(NoSuchEntityException e) {
            log.warn(String.format("User %s already removed", username));
        }
        catch(AmazonClientException e) {
            listener.error(new BackgroundException(host, null, "Cannot write user configuration", e));
        }
    }

    @Override
    public Credentials getUserCredentials(final String username) {
        // Resolve access key id
        final String id = Preferences.instance().getProperty(String.format("%s%s", prefix, username));
        if(null == id) {
            log.warn(String.format("No access key found for user %s", username));
            return null;
        }
        return new Credentials(id, KeychainFactory.instance().getPassword(host.getProtocol().getScheme().name(), host.getPort(),
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
    public void createUser(final String username, final String policy) {
        try {
            // Create new IAM credentials
            final int timeout = Preferences.instance().getInteger("connection.timeout.seconds") * 1000;
            final ClientConfiguration configuration = new ClientConfiguration();
            configuration.setConnectionTimeout(timeout);
            configuration.setSocketTimeout(timeout);
            configuration.setUserAgent(ua.get());
            configuration.setMaxErrorRetry(0);
            configuration.setMaxConnections(1);
            configuration.setProxyHost(ProxyFactory.instance().getHTTPSProxyHost(host));
            configuration.setProxyPort(ProxyFactory.instance().getHTTPSProxyPort(host));
            AmazonIdentityManagementClient iam = new AmazonIdentityManagementClient(
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
            final User user = iam.createUser(new CreateUserRequest().withUserName(username)).getUser();
            final CreateAccessKeyResult key = iam.createAccessKey(
                    new CreateAccessKeyRequest().withUserName(user.getUserName()));

            // Write policy document to get read access
            iam.putUserPolicy(new PutUserPolicyRequest(user.getUserName(), "Policy", policy));
            // Map virtual user name to IAM access key
            final String id = key.getAccessKey().getAccessKeyId();
            Preferences.instance().setProperty(String.format("%s%s", prefix, username), id);
            // Save secret
            KeychainFactory.instance().addPassword(
                    host.getProtocol().getScheme().name(), host.getPort(), host.getHostname(),
                    id, key.getAccessKey().getSecretAccessKey());
        }
        catch(AmazonClientException e) {
            listener.error(new BackgroundException(host, null, "Cannot write user configuration", e));
        }
    }
}
