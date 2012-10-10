package ch.cyberduck.core;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;

/**
 * @version $Id:$
 */
public class DefaultPathKindDetector implements PathKindDetector {

    @Override
    public int detect(final String path) {
        if(StringUtils.isBlank(path)) {
            return Path.DIRECTORY_TYPE;
        }
        if(path.endsWith(String.valueOf(Path.DELIMITER))) {
            return Path.DIRECTORY_TYPE;
        }
        if(StringUtils.isBlank(FilenameUtils.getExtension(path))) {
            return Path.DIRECTORY_TYPE;
        }
        return Path.FILE_TYPE;
    }
}
