/* CDBrowserDelegate */

import com.apple.cocoa.foundation.*;
import com.apple.cocoa.application.*;

import org.apache.log4j.Logger;

public class CDBrowserDelegate extends NSObject {

    private NSMutableArray[] files;

    public CDBrowserDelegate() {
	super();
    }

    public int browserNumberOfRowsInColumn(NSBrowser sender, int column) {
	return -1;
//	return files[column].size();
    }

    public String browserTitleOfColumn( NSBrowser sender, int column) {
	return "?? files";
    }

    public void browserWillDisplayCell(NSBrowser sender, Object cell, int row, int column) {
	// TODO
    }

}
