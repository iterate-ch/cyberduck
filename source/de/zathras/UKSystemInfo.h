//
//  UKSystemInfo.h
//  UKSystemInfo
//
//  Created by Uli Kusterer on 23.09.04.
//  Copyright 2004 M. Uli Kusterer. All rights reserved.
//

#import <Cocoa/Cocoa.h>


void		UKGetSystemVersionComponents( SInt32* outMajor, SInt32* outMinor, SInt32* outBugfix );	// System version as the separate components (Major.Minor.Bugfix).

// Don't use the following for new code:
//	(Since the number is in BCD, the maximum for minor and bugfix revisions is 9, so this returns 1049 for 10.4.10)
long		UKSystemVersion();							// System version as BCD number, I.e. 0xMMmb
