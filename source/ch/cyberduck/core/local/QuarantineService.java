package ch.cyberduck.core.local;

import ch.cyberduck.core.Local;

/**
 * @version $Id:$
 */
public interface QuarantineService {
    /**
     * @param originUrl Page that linked to the downloaded file
     * @param dataUrl   Href where the file was downloaded from
     */
    void setQuarantine(Local file, String originUrl, String dataUrl);

    /**
     * @param dataUrl Href where the file was downloaded from
     */
    void setWhereFrom(Local file, String dataUrl);
}
