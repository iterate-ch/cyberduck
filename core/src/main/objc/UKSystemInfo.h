//
//  UKSystemInfo.h
//  UKSystemInfo
//
//  Created by Uli Kusterer on 23.09.04.
//  Copyright 2004 M. Uli Kusterer. All rights reserved.
//

#include <Carbon/Carbon.h>

void UKGetSystemVersionComponents( SInt32* outMajor, SInt32* outMinor, SInt32* outBugfix );	// System version as the separate components (Major.Minor.Bugfix).
