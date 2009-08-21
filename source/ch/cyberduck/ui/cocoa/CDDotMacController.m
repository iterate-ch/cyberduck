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

JNIEXPORT jstring JNICALL Java_ch_cyberduck_ui_cocoa_CDDotMacController_getAccountNameNative(JNIEnv *env, jobject this) {
	CDDotMacController *c = [[CDDotMacController alloc] init];
    return (*env)->NewStringUTF(env, [[[c account] name] UTF8String]);
}

JNIEXPORT void JNICALL Java_ch_cyberduck_ui_cocoa_CDDotMacController_downloadBookmarksNative(JNIEnv *env, jobject this, jstring file)
{
	CDDotMacController *c = [[CDDotMacController alloc] init];
	[c downloadBookmarksFromDotMacAction:convertToNSString(env, file)];
}

JNIEXPORT void JNICALL Java_ch_cyberduck_ui_cocoa_CDDotMacController_uploadBookmarksNative(JNIEnv *env, jobject this) {
	CDDotMacController *c = [[CDDotMacController alloc] init];
	[c uploadBookmarksToDotMacAction:nil];
}

@implementation CDDotMacController

- (void)dealloc
{
	[e release];
	[super dealloc];
}

- (DMMemberAccount*)account
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
		NSData *data = [self downloadFromDotMac:@"/Documents/Cyberduck/Favorites.plist" usingAccount:[self account]];
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
			NSRunAlertPanel(NSLocalizedStringFromTable(@"Cannot find file", @"IDisk", @""), 
							NSLocalizedStringFromTable(@"The file Favorites.plist in the folder /Documents/Cyberduck/ could not be found on your iDisk.", @"IDisk", @""),
							NSLocalizedString(@"OK", @""),
							nil,
							nil);
		if ([[e name] isEqualToString:CDIOException])
			NSRunAlertPanel(NSLocalizedStringFromTable(@"Connection Error", @"IDisk", @""),
							NSLocalizedStringFromTable(@"Could not connect properly to your iDisk. Please make sure that your .Mac settings are correct.", @"IDisk", @""),
							NSLocalizedString(@"OK", @""),
							nil,
							nil);
		if ([[e name] isEqualToString:CDAccountException])
			NSRunAlertPanel(NSLocalizedStringFromTable(@"Invalid Account", @"IDisk", @""),
							NSLocalizedStringFromTable(@"Could not connect properly to your iDisk. Please make sure that your .Mac settings are correct.", @"IDisk", @""),
							NSLocalizedString(@"OK", @""),
							nil,
							nil);
		NS_ENDHANDLER
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
    //long size = [transaction contentLength];
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
	int returncode = NSRunAlertPanel(NSLocalizedStringFromTable(@"Replace Bookmarks", @"IDisk", @""),
									 NSLocalizedStringFromTable(@"Are you sure you want to replace any existing bookmarks on your iDisk?", @"IDisk", @""),
									 NSLocalizedString(@"Upload", @""),
									 NSLocalizedString(@"Cancel", @""),
									 nil);
	if (returncode == NSAlertDefaultReturn) {
		NS_DURING
			[self uploadToDotMac:[[[[NSHomeDirectory() stringByAppendingPathComponent:@"Library"]
        stringByAppendingPathComponent:@"Application Support"] stringByAppendingPathComponent:@"Cyberduck"] stringByAppendingPathComponent:@"Favorites.plist"] usingAccount:[self account]];
			NSRunInformationalAlertPanel(NSLocalizedStringFromTable(@"Upload successful", @"IDisk", @""), 
										 NSLocalizedStringFromTable(@"Successfully uploaded bookmarks to the iDisk", @"IDisk", @""), 
										 NSLocalizedString(@"OK", @""),
										 nil, 
										 nil);
		NS_HANDLER
			if ([[e name] isEqualToString:CDFileNotFoundException])
				NSRunAlertPanel(NSLocalizedStringFromTable(@"Cannot find file", @"IDisk", @""), 
								NSLocalizedStringFromTable(@"The bookmarks file could not be found at ~/Library/Application Support/Cyberduck/Favorites.plist", @"IDisk", @""),
								NSLocalizedString(@"OK", @""),
								nil,
								nil);
			if ([[e name] isEqualToString:CDIOException])
				NSRunAlertPanel(NSLocalizedStringFromTable(@"Connection Error", @"IDisk", @""),
								NSLocalizedStringFromTable(@"Could not connect properly to your iDisk. Please make sure that your .Mac settings are correct.", @"IDisk", @""),
								NSLocalizedString(@"OK", @""),
								nil,
								nil);
			if ([[e name] isEqualToString:CDAccountException])
				NSRunAlertPanel(NSLocalizedStringFromTable(@"Invalid Account", @"IDisk", @""),
								NSLocalizedStringFromTable(@"Could not connect properly to your iDisk. Please make sure that your .Mac settings are correct.", @"IDisk", @""),
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
