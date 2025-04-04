package ch.cyberduck.core.ctera.directio;

public class EncryptInfo {
    public EncryptInfo(String wrappedKey, String wrappingKey, boolean isDataEncrypted) {
        mWrappedKey = wrappedKey;
        mWrappingKey = wrappingKey;
        mIsDataEncrypted = isDataEncrypted;
    }

    String mWrappedKey;
    String mWrappingKey;
    boolean mIsDataEncrypted;
}