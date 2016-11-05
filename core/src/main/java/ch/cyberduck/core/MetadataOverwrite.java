package ch.cyberduck.core;

import java.util.Map;

/**
 * Created by alive on 05.11.2016.
 */
public class MetadataOverwrite {
    public final Map<Path, Map<String, String>> originalMetadata;
    public final Map<String, String> metadata;

    public MetadataOverwrite(Map<Path, Map<String, String>> originalMetadata, Map<String, String> metadata) {
        this.originalMetadata = originalMetadata;
        this.metadata = metadata;
    }
}
