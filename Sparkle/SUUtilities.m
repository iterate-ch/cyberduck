//
//  SUUtilities.m
//  Sparkle
//
//  Created by Andy Matuschak on 3/12/06.
//  Copyright 2006 Andy Matuschak. All rights reserved.
//

#import "SUUtilities.h"

id SUInfoValueForKey(NSString *key)
{
	// InfoPlist.strings entries have priority over Info.plist ones.
	if ([[[NSBundle mainBundle] localizedInfoDictionary] objectForKey:key])
		return [[[NSBundle mainBundle] localizedInfoDictionary] objectForKey:key];
	return [[[NSBundle mainBundle] infoDictionary] objectForKey:key];
}

NSString *SUHostAppName()
{
	if (SUInfoValueForKey(@"CFBundleName")) { return SUInfoValueForKey(@"CFBundleName"); }
	return [[[NSFileManager defaultManager] displayNameAtPath:[[NSBundle mainBundle] bundlePath]] stringByDeletingPathExtension];
}

NSString *SUHostAppVersion()
{
	return SUInfoValueForKey(@"CFBundleVersion");
}