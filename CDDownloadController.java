/* CDDownloadController */

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;

import org.apache.log4j.Logger;

public class CDDownloadController extends NSObject {

    private static Logger log = Logger.getLogger(CDDownloadController.class);

    public CDDownloadController() {
	super();
	log.debug("CDDownloadController");
    }

    public void awakeFromNib() {
	org.apache.log4j.BasicConfigurator.configure();
    }    

    public void download() {
	log.debug("CDDownloadController:download");
    }

    public void upload() {
	log.debug("CDDownloadController:upload");

    }
}
