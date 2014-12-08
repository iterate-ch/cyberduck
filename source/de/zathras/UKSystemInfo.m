//
//  UKSystemInfo.m
//  UKSystemInfo
//
//  Created by Uli Kusterer on 23.09.04.
//  Copyright 2004 M. Uli Kusterer. All rights reserved.
//

#import "UKSystemInfo.h"
#include <sys/types.h>
#include <sys/sysctl.h>

void UKGetSystemVersionComponents( SInt32* outMajor, SInt32* outMinor, SInt32* outBugfix )
{
	Gestalt( gestaltSystemVersionMajor, outMajor );
	Gestalt( gestaltSystemVersionMinor, outMinor );
	Gestalt( gestaltSystemVersionBugFix, outBugfix );
}
