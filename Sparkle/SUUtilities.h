//
//  SUUtilities.h
//  Sparkle
//
//  Created by Andy Matuschak on 3/12/06.
//  Copyright 2006 Andy Matuschak. All rights reserved.
//

#import <Cocoa/Cocoa.h>

id SUInfoValueForKey(NSString *key);
NSString *SUHostAppName();
NSString *SUHostAppVersion();

// If running make localizable-strings for genstrings, ignore the error on this line.
#define SULocalizedString(key, comment) NSLocalizedStringFromTable(key, @"Sparkle", comment)
