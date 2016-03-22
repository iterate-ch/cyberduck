package ch.cyberduck.core.dropbox;


import com.dropbox.core.v2.DbxClientV2;

public class DropBoxClient {

    private DbxClientV2 dbxClient;

    public void setDbxClient(DbxClientV2 dbxClient) {
        this.dbxClient = dbxClient;
    }

    public DbxClientV2 getDbxClient () {
        return dbxClient;
    }
}
