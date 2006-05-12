//
//  SUUtilities.m
//  Sparkle
//
//  Created by Andy Matuschak on 3/12/06.
//  Copyright 2006 Andy Matuschak. All rights reserved.
//

#import "SUUtilities.h"

@interface SUUtilities : NSObject
	+(NSString *)localizedStringForKey:(NSString *)key withComment:(NSString *)comment;
@end

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

NSString *SUHostAppVersionString()
{
	if (SUInfoValueForKey(@"CFBundleShortVersionString"))
	{
		return [[[SUInfoValueForKey(@"CFBundleShortVersionString") stringByAppendingString:@" ("] stringByAppendingString:SUHostAppVersion()] stringByAppendingString:@")"];
	}
	// otherwise, fall back on the non-localized version string
	return SUHostAppVersion();
}

NSString *SULocalizedString(NSString *key, NSString *comment) {
	return [SUUtilities localizedStringForKey:key withComment:comment];
}

@implementation SUUtilities

+ (NSString *)localizedStringForKey:(NSString *)key withComment:(NSString *)comment 
{
	return NSLocalizedStringFromTableInBundle(key, @"Sparkle", [NSBundle bundleForClass:[self class]], comment);
}

@end
