package ch.cyberduck.core.local;

import ch.cyberduck.core.Native;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

/**
 * @version $Id$
 */
public final class LaunchServicesQuarantineService implements QuarantineService {
    private static final Logger log = Logger.getLogger(LaunchServicesQuarantineService.class);

    public static void register() {
        QuarantineServiceFactory.addFactory(Factory.NATIVE_PLATFORM, new Factory());
    }

    private static class Factory extends QuarantineServiceFactory {
        @Override
        protected QuarantineService create() {
            return new LaunchServicesQuarantineService();
        }
    }

    private LaunchServicesQuarantineService() {
        Native.load("LaunchServicesQuarantineService");
    }

    private static final Object lock = new Object();

    /**
     * @param originUrl The URL of the resource originally hosting the quarantined item, from the user's point of
     *                  view. For web downloads, this property is the URL of the web page on which the user initiated
     *                  the download. For attachments, this property is the URL of the resource to which the quarantined
     *                  item was attached (e.g. the email message, calendar event, etc.). The origin URL may be a file URL
     *                  for local resources, or a custom URL to which the quarantining application will respond when asked
     *                  to open it. The quarantining application should respond by displaying the resource to the user.
     *                  Note: The origin URL should not be set to the data URL, or the quarantining application may start
     *                  downloading the file again if the user choses to view the origin URL while resolving a quarantine
     *                  warning.
     * @param dataUrl   The URL from which the data for the quarantined item data was
     *                  actaully streamed or downloaded, if available
     */
    @Override
    public void setQuarantine(final Local file, final String originUrl, final String dataUrl) {
        if(StringUtils.isEmpty(originUrl)) {
            log.warn("No origin url given for quarantine");
            return;
        }
        if(StringUtils.isEmpty(dataUrl)) {
            log.warn("No data url given for quarantine");
            return;
        }
        synchronized(lock) {
            setQuarantine(file.getAbsolute(), originUrl, dataUrl);
        }
    }

    /**
     * UKXattrMetadataStore
     *
     * @param path      Absolute path reference
     * @param originUrl Page that linked to the downloaded file
     * @param dataUrl   Href where the file was downloaded from
     */
    private native void setQuarantine(String path, String originUrl, String dataUrl);

    /**
     * Set the kMDItemWhereFroms on the file.
     *
     * @param dataUrl Href where the file was downloaded from
     */
    @Override
    public void setWhereFrom(final Local file, final String dataUrl) {
        if(StringUtils.isEmpty(dataUrl)) {
            log.warn("No data url given for quarantine");
            return;
        }
        synchronized(lock) {
            setWhereFrom(file.getAbsolute(), dataUrl);
        }
    }

    /**
     * Set the kMDItemWhereFroms on the file.
     *
     * @param path    Absolute path reference
     * @param dataUrl Href where the file was downloaded from
     */
    private native void setWhereFrom(String path, String dataUrl);
}
