/* CDDownloadController */

package ch.cyberduck.ui.cocoa;

import ch.cyberduck.core.Host;

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;

import org.apache.log4j.Logger;

public class CDTransferController extends NSObject {

    private static Logger log = Logger.getLogger(CDTransferController.class);

    public NSTableView transferTable;

    public CDTransferController() {
	super();
	log.debug("CDDownloadController");
    }

    public void awakeFromNib() {

    }    

    public void download() {
	log.debug("download");
    }

    public void upload() {
	log.debug("upload");

    }
}
