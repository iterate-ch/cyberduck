//
//  UKSystemInfo.m
//  UKSystemInfo
//
//  Created by Uli Kusterer on 23.09.04.
//  Copyright 2004 M. Uli Kusterer. All rights reserved.
//

#import "UKSystemInfo.h"
#include <Carbon/Carbon.h>
#include <sys/types.h>
#include <sys/sysctl.h>

void	UKGetSystemVersionComponents( SInt32* outMajor, SInt32* outMinor, SInt32* outBugfix )
{
	SInt32		sysVersion = UKSystemVersion();
	if( sysVersion >= MAC_OS_X_VERSION_10_4 )
	{
		Gestalt( gestaltSystemVersionMajor, outMajor );
		Gestalt( gestaltSystemVersionMinor, outMinor );
		Gestalt( gestaltSystemVersionBugFix, outBugfix );
	}
	else
	{
		*outMajor = ((sysVersion & 0x0000F000) >> 12) * 10 + ((sysVersion & 0x00000F00) >> 8);
		*outMinor = (sysVersion & 0x000000F0) >> 4;
		*outBugfix = sysVersion & 0x0000000F;
	}
}

long	UKSystemVersion()
{
	SInt32		sysVersion;

	if( Gestalt( gestaltSystemVersion, &sysVersion ) != noErr )
		return 0;

	return sysVersion;
}
