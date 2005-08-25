/*
** Copyright 1998, 1999, 2001, 2002 by Gregory L. Guerin.
** Terms of use:
**  - Briefly: OPEN SOURCE under Artistic License -- credit fairly, use freely, alter carefully.
**  - Fully: <http://www.amug.org/~glguerin/sw/artistic-license.html>
*/

package glguerin.io;

import java.io.File;


// --- Revision History ---
// 25Mar2002 GLG  factor out of PathnameFormat source file


/**
** PF_Simple is a non-public
** PathnameFormat subclass that represents a generic platform's
** File-compatible format.  It uses the File.separatorChar as separator.
** If the platform's File.separatorChar is '/', then it's configured with a leading separator.
** All other separator-char values imply NO LEADING SEPARATOR.
** As a simple default format, this should work for Unix and Windows.
**<p>
** This class WILL NOT WORK on Mac OS classic or Mac OS X.
** Use the Mac-specific imps for those platforms.
**<p>
** This class may not work on some other platforms, too.
** If this class won't work on your platform, your code is responsible for detecting
** the platform and providing an appropriate PathnameFormat to use as the default.
** You can establish the binding using PathnameFormat.setFormatFor().
**
** @see PF_MacOS
** @see PF_MacOSX
** @see PathnameFormat#setFormatFor
*/

public final class PF_Simple
  extends PathnameFormat
{
	/** Non-public constructor prevents instantiation outside of package. */
	protected
	PF_Simple()
	{  super( File.separatorChar, (File.separatorChar == '/') );  }
}
