/* CDFavoritesListView */

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;

import org.apache.log4j.Logger;

public class CDFavoritesListView extends CDListView {
    private static Logger log = Logger.getLogger(CDFavoritesListView.class);

    public CDFavoritesListView() {
	super();
    }

    public CDFavoritesListView(NSRect frameRect) {
	super(frameRect);
    }
}
