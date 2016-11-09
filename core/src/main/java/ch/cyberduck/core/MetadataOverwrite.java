package ch.cyberduck.core;

import java.util.Map;

/**
 * Created by alive on 05.11.2016.
 */
public class MetadataOverwrite {
    /**
     * Stores original key/values per path. Used in WriteMetadataWorker.
     * DO NOT CHANGE
     */
    public final Map<Path, Map<String, String>> originalMetadata;
    /**
     * Used as replacement for previous Map&lt;String, String&gt; used in Read-/WriteMetadataWorker.
     */
    public final Map<String, String> metadata;

    public MetadataOverwrite(Map<Path, Map<String, String>> originalMetadata, Map<String, String> metadata) {
        this.originalMetadata = originalMetadata;
        this.metadata = metadata;
    }
}
