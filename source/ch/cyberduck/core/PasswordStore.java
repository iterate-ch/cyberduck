package ch.cyberduck.core;

/**
 * @version $Id$
 */
public interface PasswordStore {

    /**
     * @param scheme   Protocol scheme
     * @param port     Port
     * @param hostname
     * @param user     Credentials  @return Password if found or null otherwise
     */
    String getPassword(Scheme scheme, int port, String hostname, String user);

    /**
     * @param hostname Hostname
     * @param user     Credentials
     * @return Password if found or null otherwise
     */
    String getPassword(String hostname, String user);

    /**
     * @param serviceName Hostname
     * @param user        Credentials
     * @param password    Password to save for service
     */
    void addPassword(String serviceName, String user, String password);

    /**
     * @param scheme   Protocol scheme
     * @param port     Port
     * @param hostname Servie name
     * @param user     Credentials
     * @param password Password to save for service
     */
    void addPassword(Scheme scheme, int port, String hostname, String user, String password);
}
