package ch.cyberduck.core;

/**
 * @version $Id:$
 */
public interface PasswordStore {

    /**
     * @param protocol    Protocol scheme
     * @param port        Port
     * @param serviceName Hostname
     * @param user        Credentials
     * @return Password if found or null otherwise
     */
    String getPassword(String protocol, int port, String serviceName, String user);

    /**
     * @param serviceName Hostname
     * @param user        Credentials
     * @return Password if found or null otherwise
     */
    String getPassword(String serviceName, String user);

    /**
     * @param serviceName Hostname
     * @param user        Credentials
     * @param password    Password to save for service
     */
    void addPassword(String serviceName, String user, String password);

    /**
     * @param protocol    Scheme
     * @param port        Port
     * @param serviceName Hostname
     * @param user        Credentials
     * @param password    Password to save for service
     */
    void addPassword(String protocol, int port, String serviceName, String user, String password);
}
