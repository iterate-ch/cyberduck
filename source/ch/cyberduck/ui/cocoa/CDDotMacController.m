/*
 *  Copyright (c) 2005 David Kocher. All rights reserved.
 *  http://cyberduck.ch/
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  Bug fixes, suggestions and comments should be sent to:
 *  dkocher@cyberduck.ch
 */

#import "CDDotMacController.h"

#define	CDFileNotFoundException			@"CDFileNotFoundException"
#define	CDIOException					@"CDIOException"
#define CDAccountException				@"CDAccountException"

// Simple utility to convert java strings to NSStrings
NSString *convertToNSString(JNIEnv *env, jstring javaString)
{
    NSString *converted = nil;
    const jchar *unichars = NULL;
	
    if (javaString == NULL) {
        return nil;	
    }                   
    unichars = (*env)->GetStringChars(env, javaString, NULL);
    if ((*env)->ExceptionOccurred(env)) {
        return @"";
    }
    converted = [NSString stringWithCharacters:unichars length:(*env)->GetStringLength(env, javaString)]; // auto-released
    (*env)->ReleaseStringChars(env, javaString, unichars);
    return converted;
}

JNIEXPORT void JNICALL Java_ch_cyberduck_ui_cocoa_CDDotMacController_downloadBookmarksFromDotMacActionNative(JNIEnv *env, jobject this, jstring file) 
{
	CDDotMacController *c = [[CDDotMacController alloc] init];
	[c downloadBookmarksFromDotMacAction:convertToNSString(env, file)];
	[c release];
}

JNIEXPORT void JNICALL Java_ch_cyberduck_ui_cocoa_CDDotMacController_uploadBookmarksToDotMacActionNative(JNIEnv *env, jobject this) {
	CDDotMacController *c = [[CDDotMacController alloc] init];
	[c uploadBookmarksToDotMacAction:nil];
	[c release];
}

@implementation CDDotMacController

- (void)dealloc
{
	[e release];
	[super dealloc];
}

- (DMMemberAccount*)getUserAccount
{
	DMMemberAccount* account = [DMMemberAccount accountFromPreferencesWithApplicationID:@"CYCK"];
	[account setApplicationName:@"Cyberduck"];
	if(kDMInvalidCredentials == [account validateCredentials])
	{
		e = [NSException exceptionWithName:CDAccountException
									reason:nil
								  userInfo:nil];
		[e raise];
	}
	return account;
}

#pragma mark Download

- (IBAction)downloadBookmarksFromDotMacAction:(id)sender
{
	tmpBookmarkFile = sender;
	
	NS_DURING
		NSData *data = [self downloadFromDotMac:@"/Documents/Cyberduck/Favorites.plist" usingAccount:[self getUserAccount]];
		if(data) {
			//		NSString *localPath = [[[[NSHomeDirectory() stringByAppendingPathComponent:@"Library"] stringByAppendingPathComponent:@"Application Support"] stringByAppendingPathComponent:@"Cyberduck"] stringByAppendingPathComponent:@"Favorites.plist"];
			NSString *localPath = tmpBookmarkFile;
			if (!data || ![data writeToFile:localPath atomically:YES]) {
				[[NSException exceptionWithName:CDIOException
										 reason:nil
									   userInfo:nil] raise];
			}
		}			
	NS_HANDLER
		if ([[e name] isEqualToString:CDFileNotFoundException])
			NSRunAlertPanel(NSLocalizedString(@"Could not find a file on your iDisk", @""),
							NSLocalizedString(@"Please make sure that you have a file called Favorites.plist in the folder /Documents/Cyberduck/ on your iDisk.", @""),
							NSLocalizedString(@"OK", @""),
							nil,
							nil);
		if ([[e name] isEqualToString:CDIOException])
			NSRunAlertPanel(NSLocalizedString(@"Connection Error", @""),
							NSLocalizedString(@"Could not connect properly to your iDisk. Please make sure that your .Mac settings are correct.", @""),
							NSLocalizedString(@"OK", @""),
							nil,
							nil);
		if ([[e name] isEqualToString:CDAccountException])
			NSRunAlertPanel(NSLocalizedString(@"Invalid Account", @""),
							NSLocalizedString(@"Could not login properly to your iDisk. Please make sure that your .Mac settings are correct.", @""),
							NSLocalizedString(@"OK", @""),
							nil,
							nil);
		NS_ENDHANDLER
}

- (IBAction)downloadPreferencesFromDotMacAction:(id)sender
{
	int returncode = NSRunAlertPanel(NSLocalizedString(@"Replace Preferences", @""),
									 NSLocalizedString(@"Are you sure you want to download the preferences from your iDisk and replace your current settings?", @""),
									 NSLocalizedString(@"Download", @""),
									 NSLocalizedString(@"Cancel", @""),
									 nil);
	
	if (returncode == NSAlertDefaultReturn) {
		NS_DURING
			NSData *data = [self downloadFromDotMac:@"/Documents/Cyberduck/ch.sudo.cyberduck.plist" usingAccount:[self getUserAccount]];
			if(data) {
				NSString *localPath = [[[NSHomeDirectory() stringByAppendingPathComponent:@"Library"] stringByAppendingPathComponent:@"Preferences"] stringByAppendingPathComponent:@"ch.sudo.cyberduck.plist"];
				if (!data || ![data writeToFile:localPath atomically:YES]) {
					[[NSException exceptionWithName:CDIOException
											 reason:nil
										   userInfo:nil] raise];
				}
				
				NSDictionary *defaultsDictionary = [NSDictionary dictionaryWithContentsOfFile:localPath];
				if (defaultsDictionary)
				{
					[[NSUserDefaults standardUserDefaults] registerDefaults:defaultsDictionary];
				}
				else
				{
					e = [NSException exceptionWithName:CDIOException
												reason:nil
											  userInfo:nil];
					[e raise];
				}
				[[NSUserDefaults standardUserDefaults] synchronize];

				NSRunInformationalAlertPanel(NSLocalizedString(@"Preferences udpated", @""), 
											 NSLocalizedString(@"Successfully downloaded preferences from the iDisk", @""), 
											 NSLocalizedString(@"OK", @""),
											 nil, 
											 nil);
			}				
		NS_HANDLER
			if ([[e name] isEqualToString:CDFileNotFoundException])
				NSRunAlertPanel(NSLocalizedString(@"Could not find a file on your iDisk", @""),
								NSLocalizedString(@"Please make sure that you have a file called ch.sudo.cyberduck.plist in the folder /Documents/Cyberduck/ on your iDisk.", @""),
								NSLocalizedString(@"OK", @""),
								nil,
								nil);
			if ([[e name] isEqualToString:CDIOException])
				NSRunAlertPanel(NSLocalizedString(@"Connection Error", @""),
								NSLocalizedString(@"Could not connect properly to your iDisk. Please make sure that your .Mac settings are correct.", @""),
								NSLocalizedString(@"OK", @""),
								nil,
								nil);
			if ([[e name] isEqualToString:CDAccountException])
				NSRunAlertPanel(NSLocalizedString(@"Invalid Account", @""),
								NSLocalizedString(@"Could not login properly to your iDisk. Please make sure that your .Mac settings are correct.", @""),
								NSLocalizedString(@"OK", @""),
								nil,
								nil);
			NS_ENDHANDLER
	}
}

- (NSData*)downloadFromDotMac:(NSString *)remoteFile usingAccount:(DMMemberAccount*)account
{
	DMiDiskSession *session = [DMiDiskSession iDiskSessionWithAccount:account];
	if (!session) {
		e = [NSException exceptionWithName:CDIOException
									reason:nil
								  userInfo:nil];
		[e raise];
	}
	
	if (![session fileExistsAtPath:remoteFile]) {
		e = [NSException exceptionWithName:CDFileNotFoundException
									reason:nil
								  userInfo:nil];
		[e raise];
	}
	DMTransaction *transaction = [session getDataAtPath:remoteFile];
	if (!transaction) {
		e = [NSException exceptionWithName:CDIOException
									reason:nil
								  userInfo:nil];
		[e raise];
	}
	long size = [transaction contentLength];
	while(![transaction isFinished]) {
		//block; update progress with
		//[transaction bytesTransferred];
	}
	if([transaction isSuccessful]) {
		NSData *data = [transaction result];
		return data;
	}
	return nil;
}
	
#pragma mark Upload

- (IBAction)uploadBookmarksToDotMacAction:(id)sender
{
	int returncode = NSRunAlertPanel(NSLocalizedString(@"Replace Bookmarks", @""),
									 NSLocalizedString(@"Are you sure you want to replace any existing bookmarks on your iDisk?", @""),
									 NSLocalizedString(@"Upload", @""),
									 NSLocalizedString(@"Cancel", @""),
									 nil);
	if (returncode == NSAlertDefaultReturn) {
		NS_DURING
			[self uploadToDotMac:[[[[NSHomeDirectory() stringByAppendingPathComponent:@"Library"]
        stringByAppendingPathComponent:@"Application Support"] stringByAppendingPathComponent:@"Cyberduck"] stringByAppendingPathComponent:@"Favorites.plist"] usingAccount:[self getUserAccount]];
			NSRunInformationalAlertPanel(NSLocalizedString(@"Upload successful", @""), 
										 NSLocalizedString(@"Successfully uploaded bookmarks to the iDisk", @""), 
										 NSLocalizedString(@"OK", @""),
										 nil, 
										 nil);
		NS_HANDLER
			if ([[e name] isEqualToString:CDFileNotFoundException])
				NSRunAlertPanel(NSLocalizedString(@"Could not find your local file", @""), 
								NSLocalizedString(@"Please check the installation of Cyberduck and that you have a bookmarks file at ~/Library/Application Support/Cyberduck/Favorites.plist", @""),
								NSLocalizedString(@"OK", @""),
								nil,
								nil);
			if ([[e name] isEqualToString:CDIOException])
				NSRunAlertPanel(NSLocalizedString(@"Connection Error", @""),
								NSLocalizedString(@"Could not connect properly to your iDisk. Please make sure that your .Mac settings are correct.", @""),
								NSLocalizedString(@"OK", @""),
								nil,
								nil);
			if ([[e name] isEqualToString:CDAccountException])
				NSRunAlertPanel(NSLocalizedString(@"Invalid Account", @""),
								NSLocalizedString(@"Could not login properly to your iDisk. Please make sure that your .Mac settings are correct.", @""),
								NSLocalizedString(@"OK", @""),
								nil,
								nil);
			NS_ENDHANDLER
	}}

- (IBAction)uploadPreferencesToDotMacAction:(id)sender
{
	int returncode = NSRunAlertPanel(NSLocalizedString(@"Replace Preferences", @""),
									 NSLocalizedString(@"Are you sure you want to replace the existing file on your iDisk if it exists?", @""),
									 NSLocalizedString(@"Upload", @""),
									 NSLocalizedString(@"Cancel", @""),
									 nil);
	if (returncode == NSAlertDefaultReturn) {
		NS_DURING
			[self uploadToDotMac:[[[NSHomeDirectory() stringByAppendingPathComponent:@"Library"]
        stringByAppendingPathComponent:@"Preferences"] stringByAppendingPathComponent:@"ch.sudo.cyberduck.plist"] usingAccount:[self getUserAccount]];
			NSRunInformationalAlertPanel(NSLocalizedString(@"Upload successful", @""), 
										 NSLocalizedString(@"Successfully uploaded preferences to the iDisk", @""), 
										 NSLocalizedString(@"OK", @""),
										 nil, 
										 nil);
		NS_HANDLER
			if ([[e name] isEqualToString:CDFileNotFoundException])
				NSRunAlertPanel(NSLocalizedString(@"Could not find your local file", @""), NSLocalizedString(@"Please check the installation of Cyberduck and that you have a preference file at ~/Library/Preferences/ch.sudo.cyberduck.plist", @""),
								NSLocalizedString(@"OK", @""),
								nil,
								nil);
			if ([[e name] isEqualToString:CDIOException])
				NSRunAlertPanel(NSLocalizedString(@"Connection Error", @""),
								NSLocalizedString(@"Could not connect properly to your iDisk. Please make sure that your .Mac settings are correct.", @""),
								NSLocalizedString(@"OK", @""),
								nil,
								nil);
			if ([[e name] isEqualToString:CDAccountException])
				NSRunAlertPanel(NSLocalizedString(@"Invalid Account", @""),
								NSLocalizedString(@"Could not login properly to your iDisk. Please make sure that your .Mac settings are correct.", @""),
								NSLocalizedString(@"OK", @""),
								nil,
								nil);
			NS_ENDHANDLER
	}
}

- (void)uploadToDotMac:(NSString *)localFile usingAccount:(DMMemberAccount*)account
{
	if (![[NSFileManager defaultManager] fileExistsAtPath:localFile]) {
		e = [NSException exceptionWithName:CDFileNotFoundException
									reason:nil
								  userInfo:nil];
		[e raise];
	}
	
	DMiDiskSession *session = [DMiDiskSession iDiskSessionWithAccount:[DMMemberAccount accountFromPreferencesWithApplicationID:@"CYCK"]];
	if (!session) {
		e = [NSException exceptionWithName:CDIOException
									reason:nil
								  userInfo:nil];
		[e raise];
	}
	
	BOOL isDirectory;
	if (![session fileExistsAtPath:@"/Documents/Cyberduck" isDirectory:&isDirectory] || !isDirectory) {
		if (![session createDirectoryAtPath:@"/Documents/Cyberduck" attributes:nil]) {
			e = [NSException exceptionWithName:CDIOException
										reason:nil
									  userInfo:nil];
			[e raise];
		}
	}
	
	DMTransaction *transaction = [session putLocalFileAtPath:localFile 
													  toPath:[[NSString stringWithString:@"/Documents/Cyberduck"] stringByAppendingPathComponent:[[localFile pathComponents] lastObject]]];
	
	if (!transaction) {
		e = [NSException exceptionWithName:CDIOException
									reason:nil
								  userInfo:nil];
		[e raise];
	}
}

#pragma mark DMTransaction Delegate Methods

- (void)transactionSuccessful:(DMTransaction *)theTransaction
{

}

- (void)transactionHadError:(DMTransaction *)theTransaction
{
	e = [NSException exceptionWithName:CDIOException
								reason:nil
							  userInfo:nil];
	[e raise];
}

- (void)transactionAborted:(DMTransaction *)theTransaction
{
	e = [NSException exceptionWithName:CDIOException
								reason:nil
							  userInfo:nil];
	[e raise];
}

@end
