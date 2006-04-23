//
//  SUUpdater.m
//  Sparkle
//
//  Created by Andy Matuschak on 1/4/06.
//  Copyright 2006 Andy Matuschak. All rights reserved.
//

#import "SUUpdater.h"
#import "SUAppcast.h"
#import "SUAppcastItem.h"
#import "SUUnarchiver.h"
#import "SUUtilities.h"

#import "SUUpdateAlert.h"
#import "SUAutomaticUpdateAlert.h"
#import "SUStatusController.h"

#import "NSFileManager+Authentication.h"
#import "NSFileManager+Verification.h"
#import "NSApplication+AppCopies.h"

#import <stdio.h>
#import <sys/stat.h>
#import <unistd.h>
#import <signal.h>
#import <dirent.h>

@interface SUUpdater (Private)
- (void)checkForUpdatesAndNotify:(BOOL)verbosity;
- (void)showUpdateErrorAlertWithInfo:(NSString *)info;
- (NSTimeInterval)storedCheckInterval;
- (void)abandonUpdate;
- (IBAction)installAndRestart:sender;
@end

@implementation SUUpdater

- (void)scheduleCheckWithInterval:(NSTimeInterval)interval
{
	if (checkTimer)
	{
		[checkTimer invalidate];
		checkTimer = nil;
	}
	
	checkInterval = interval;
	if (interval > 0)
		checkTimer = [NSTimer scheduledTimerWithTimeInterval:interval target:self selector:@selector(checkForUpdatesInBackground) userInfo:nil repeats:YES];
}

- (void)awakeFromNib
{
	// If there's a scheduled interval, we see if it's been longer than that interval since the last
	// check. If so, we perform a startup check; if not, we don't.	
	if ([self storedCheckInterval])
	{
		NSTimeInterval interval = [self storedCheckInterval];
		NSDate *lastCheck = [[NSUserDefaults standardUserDefaults] objectForKey:SULastCheckTimeKey];
		if (!lastCheck) { lastCheck = [NSDate distantPast]; }
		NSTimeInterval intervalSinceCheck = [[NSDate date] timeIntervalSinceDate:lastCheck];
		if (intervalSinceCheck < interval)
		{
			// Hasn't been long enough; schedule a check for the future.
			[self performSelector:@selector(checkForUpdatesInBackground) withObject:nil afterDelay:intervalSinceCheck];
			[self performSelector:@selector(scheduleCheckWithInterval:) withObject:[NSNumber numberWithLong:interval] afterDelay:intervalSinceCheck];
		}
		else
		{
			[self scheduleCheckWithInterval:interval];
			[self checkForUpdatesInBackground];
		}
	}
	else
	{
		// There's no scheduled check, so let's see if we're supposed to check on startup.
		NSNumber *shouldCheckAtStartup = [[NSUserDefaults standardUserDefaults] objectForKey:SUCheckAtStartupKey];
		if (!shouldCheckAtStartup) // hasn't been set yet; ask the user
		{
			// Let's see if there's a key in Info.plist for a default, though. We'll let that override the dialog if it's there.
			NSNumber *infoStartupValue = SUInfoValueForKey(SUCheckAtStartupKey);
			if (infoStartupValue)
			{
				shouldCheckAtStartup = infoStartupValue;
			}
			else
			{
				shouldCheckAtStartup = [NSNumber numberWithBool:NSRunAlertPanel(SULocalizedString(@"Check for updates on startup?", nil), [NSString stringWithFormat:SULocalizedString(@"Would you like %@ to check for updates on startup? If not, you can initiate the check manually from the application menu.", nil), SUHostAppName()], SULocalizedString(@"Yes", nil), SULocalizedString(@"No", nil), nil) == NSAlertDefaultReturn];
			}
			[[NSUserDefaults standardUserDefaults] setObject:shouldCheckAtStartup forKey:SUCheckAtStartupKey];
		}
		
		if ([shouldCheckAtStartup boolValue])
			[self checkForUpdatesInBackground];
	}
}

- (void)dealloc
{
	[updateItem release];
    [updateAlert release];
	
	[downloadPath release];
	[statusController release];
	[downloader release];
	
	if (checkTimer)
		[checkTimer invalidate];
	[super dealloc];
}

- (void)checkForUpdatesInBackground
{
	[self checkForUpdatesAndNotify:NO];
}

- (IBAction)checkForUpdates:sender
{
	[self checkForUpdatesAndNotify:YES]; // if we're coming from IB, then we want to be more verbose.
}

// If the verbosity flag is YES, Sparkle will say when it can't reach the server and when there's no new update.
// This is generally useful for a menu item--when the check is explicitly invoked.
- (void)checkForUpdatesAndNotify:(BOOL)verbosity
{	
	verbose = verbosity;
	
	if (updateInProgress)
	{
		if (verbose)
		{
			NSBeep();
			if ([[statusController window] isVisible])
				[statusController showWindow:self];
			else if ([[updateAlert window] isVisible])
				[updateAlert showWindow:self];
			else
				[self showUpdateErrorAlertWithInfo:SULocalizedString(@"An update is already in progress!", nil)];
		}
		return;
	}
	updateInProgress = YES;
	
	// A value in the user defaults overrides one in the Info.plist (so preferences panels can be created wherein users choose between beta / release feeds).
	NSString *appcastString = [[NSUserDefaults standardUserDefaults] objectForKey:SUFeedURLKey];
	if (!appcastString)
		appcastString = SUInfoValueForKey(SUFeedURLKey);
	if (!appcastString) { [NSException raise:@"SUNoFeedURL" format:@"No feed URL is specified in the Info.plist or the user defaults!"]; }
	
	SUAppcast *appcast = [[SUAppcast alloc] init];
	[appcast setDelegate:self];
	[appcast fetchAppcastFromURL:[NSURL URLWithString:appcastString]];
}

- (BOOL)automaticallyUpdates
{
	if (![SUInfoValueForKey(SUAllowsAutomaticUpdatesKey) boolValue]) { return NO; }
	if (![[NSUserDefaults standardUserDefaults] objectForKey:SUAutomaticallyUpdateKey]) { return NO; } // defaults to NO
	return [[[NSUserDefaults standardUserDefaults] objectForKey:SUAutomaticallyUpdateKey] boolValue];
}

- (BOOL)isAutomaticallyUpdating
{
	return [self automaticallyUpdates] && !verbose;
}

- (void)showUpdateErrorAlertWithInfo:(NSString *)info
{
	if ([self isAutomaticallyUpdating]) { return; }
	NSRunAlertPanel(SULocalizedString(@"Update Error!", nil), info, NSLocalizedString(@"Cancel", nil), nil, nil);
}

- (NSTimeInterval)storedCheckInterval
{
	// Returns the scheduled check interval stored in the user defaults / info.plist. User defaults override Info.plist.
	if ([[NSUserDefaults standardUserDefaults] objectForKey:SUScheduledCheckIntervalKey])
		return [[[NSUserDefaults standardUserDefaults] objectForKey:SUScheduledCheckIntervalKey] longValue];
	if (SUInfoValueForKey(SUScheduledCheckIntervalKey))
		return [SUInfoValueForKey(SUScheduledCheckIntervalKey) longValue];
	return 0;
}

- (void)beginDownload
{
	if (![self isAutomaticallyUpdating])
	{
		statusController = [[SUStatusController alloc] init];
		[statusController beginActionWithTitle:SULocalizedString(@"Downloading update...", nil) maxProgressValue:0 statusText:nil];
		[statusController setButtonTitle:NSLocalizedString(@"Cancel", nil) target:self action:@selector(cancelDownload:) isDefault:NO];
		[statusController showWindow:self];
	}
	
	downloader = [[NSURLDownload alloc] initWithRequest:[NSURLRequest requestWithURL:[updateItem fileURL]] delegate:self];	
}

- (void)remindMeLater
{
	// Clear out the skipped version so the dialog will actually come back if it was already skipped.
	[[NSUserDefaults standardUserDefaults] setObject:nil forKey:SUSkippedVersionKey];
	
	if (checkInterval)
		[self scheduleCheckWithInterval:checkInterval];
	else
	{
		// If the host hasn't provided a check interval, we'll use 30 minutes.
		[self scheduleCheckWithInterval:30 * 60];
	}
}

- (void)updateAlert:(SUUpdateAlert *)alert finishedWithChoice:(SUUpdateAlertChoice)choice
{
	[alert release];
	switch (choice)
	{
		case SUInstallUpdateChoice:
			// Clear out the skipped version so the dialog will come back if the download fails.
			[[NSUserDefaults standardUserDefaults] setObject:nil forKey:SUSkippedVersionKey];
			[self beginDownload];
			break;
			
		case SURemindMeLaterChoice:
			updateInProgress = NO;
			[self remindMeLater];
			break;
			
		case SUSkipThisVersionChoice:
			updateInProgress = NO;
			[[NSUserDefaults standardUserDefaults] setObject:[updateItem fileVersion] forKey:SUSkippedVersionKey];
			break;
	}			
}

- (void)showUpdatePanel
{
	updateAlert = [[SUUpdateAlert alloc] initWithAppcastItem:updateItem];
	[updateAlert setDelegate:self];
	[updateAlert showWindow:self];
}

- (void)appcastDidFailToLoad:(SUAppcast *)ac
{
	[ac autorelease];
	updateInProgress = NO;
	if (verbose)
		[self showUpdateErrorAlertWithInfo:SULocalizedString(@"An error occurred in retrieving update information; are you connected to the internet? Please try again later.", nil)];
}

- (void)appcastDidFinishLoading:(SUAppcast *)ac
{
	@try
	{
		if (!ac) { [NSException raise:@"SUAppcastException" format:@"Couldn't get a valid appcast from the server."]; }

		updateItem = [[ac newestItem] retain];

		// Record the time of the check for host app use and for interval checks on startup.
		[[NSUserDefaults standardUserDefaults] setObject:[NSDate date] forKey:SULastCheckTimeKey];

		if (![updateItem fileVersion])
		{
			[NSException raise:@"SUAppcastException" format:@"Can't extract a version string from the appcast feed. The filenames should look like YourApp_1.5.tgz, where 1.5 is the version number."];
		}

		if (!verbose && [[[NSUserDefaults standardUserDefaults] objectForKey:SUSkippedVersionKey] isEqualToString:[updateItem fileVersion]]) { updateInProgress = NO; return; }

		if ([SUHostAppVersion() isEqualToString:[updateItem fileVersion]])
		{
			if (verbose) // We only notify on no new version when we're being verbose.
			{
				NSRunAlertPanel(SULocalizedString(@"You're up to date!", nil), [NSString stringWithFormat:SULocalizedString(@"%@ %@ is currently the newest version available.", nil), SUHostAppName(), SUHostAppVersion()], NSLocalizedString(@"OK", nil), nil, nil);
			}
			updateInProgress = NO;
		}
		else
		{
			if (checkTimer)	// There's a new version! Let's disable the automated checking timer unless the user cancels.
			{
				[checkTimer invalidate];
				checkTimer = nil;
			}
			
			if ([self isAutomaticallyUpdating])
			{
				[self beginDownload];
			}
			else
			{
				[self showUpdatePanel];
			}
		}
	}
	@catch (NSException *e)
	{
		NSLog([e reason]);
		updateInProgress = NO;
		if (verbose)
			[self showUpdateErrorAlertWithInfo:SULocalizedString(@"An error occurred in retrieving update information. Please try again later.", nil)];
	}
}

- (void)download:(NSURLDownload *)download didReceiveResponse:(NSURLResponse *)response
{
	[statusController setMaxProgressValue:[response expectedContentLength]];
}

- (void)download:(NSURLDownload *)download decideDestinationWithSuggestedFilename:(NSString *)name
{
	// If name ends in .txt, the server probably has a stupid MIME configuration. We'll give
	// the developer the benefit of the doubt and chop that off.
	if ([[name pathExtension] isEqualToString:@"txt"])
		name = [name stringByDeletingPathExtension];
	
	// We create a temporary directory in /tmp and stick the file there.
	NSString *tempDir = [NSTemporaryDirectory() stringByAppendingPathComponent:[[NSProcessInfo processInfo] globallyUniqueString]];
	BOOL success = [[NSFileManager defaultManager] createDirectoryAtPath:tempDir attributes:nil];
	if (!success)
	{
		[NSException raise:@"SUFailTmpWrite" format:@"Couldn't create temporary directory in /tmp"];
		[download cancel];
		[download release];
	}
	
	downloadPath = [[tempDir stringByAppendingPathComponent:name] retain];
	[download setDestination:downloadPath allowOverwrite:YES];
}

- (void)download:(NSURLDownload *)download didReceiveDataOfLength:(unsigned)length
{
	[statusController setProgressValue:[statusController progressValue] + length];
	[statusController setStatusText:[NSString stringWithFormat:SULocalizedString(@"%.0lfk of %.0lfk", nil), [statusController progressValue] / 1024.0, [statusController maxProgressValue] / 1024.0]];
}

- (void)unarchiver:(SUUnarchiver *)ua extractedLength:(long)length
{
	if ([self isAutomaticallyUpdating]) { return; }
	if ([statusController maxProgressValue] == 0)
		[statusController setMaxProgressValue:[[[[NSFileManager defaultManager] fileAttributesAtPath:downloadPath traverseLink:NO] objectForKey:NSFileSize] longValue]];
	[statusController setProgressValue:[statusController progressValue] + length];
}

- (void)unarchiverDidFinish:(SUUnarchiver *)ua
{
	[ua autorelease];
	
	if ([self isAutomaticallyUpdating])
	{
		[self installAndRestart:self];
	}
	else
	{
		[statusController beginActionWithTitle:SULocalizedString(@"Ready to install!", nil) maxProgressValue:1 statusText:nil];
		[statusController setProgressValue:1]; // fill the bar
		[statusController setButtonTitle:SULocalizedString(@"Install and Relaunch", nil) target:self action:@selector(installAndRestart:) isDefault:YES];
		[NSApp requestUserAttention:NSInformationalRequest];
	}
}

- (void)unarchiverDidFail:(SUUnarchiver *)ua
{
	[ua autorelease];
	[self showUpdateErrorAlertWithInfo:SULocalizedString(@"An error occurred while extracting the archive. Please try again later.", nil)];
	[self abandonUpdate];
}

- (void)extractUpdate
{
	// Now we have to extract the downloaded archive.
	if (![self isAutomaticallyUpdating])
		[statusController beginActionWithTitle:SULocalizedString(@"Extracting update...", nil) maxProgressValue:0 statusText:nil];
	
	@try 
	{
		// If the developer's provided a sparkle:md5Hash attribute on the enclosure, let's verify that.
		if ([updateItem MD5Sum] && ![[NSFileManager defaultManager] validatePath:downloadPath withMD5Hash:[updateItem MD5Sum]])
		{
			[NSException raise:@"SUUnarchiveException" format:@"MD5 verification of the update archive failed."];
		}
		
		// DSA verification, if activated by the developer
		if ([SUInfoValueForKey(SUExpectsDSASignatureKey) boolValue])
		{
			NSString *dsaSignature = [updateItem DSASignature];
			if (![[NSFileManager defaultManager] validatePath:downloadPath withEncodedDSASignature:dsaSignature])
			{
				[NSException raise:@"SUUnarchiveException" format:@"DSA verification of the update archive failed."];
			}
		}
		
		SUUnarchiver *unarchiver = [[SUUnarchiver alloc] init];
		[unarchiver setDelegate:self];
		[unarchiver unarchivePath:downloadPath]; // asynchronous extraction!
	}
	@catch(NSException *e) {
		NSLog([e reason]);
		[self showUpdateErrorAlertWithInfo:SULocalizedString(@"An error occurred while extracting the archive. Please try again later.", nil)];
		[self abandonUpdate];
	}	
}

- (void)downloadDidFinish:(NSURLDownload *)download
{
	[download release];
	downloader = nil;
	[self extractUpdate];
}

- (void)abandonUpdate
{
	[statusController close];
	[statusController release];
	updateInProgress = NO;	
}

- (void)download:(NSURLDownload *)download didFailWithError:(NSError *)error
{
	[self abandonUpdate];
	
	NSLog(@"Download error: %@", [error localizedDescription]);
	[self showUpdateErrorAlertWithInfo:SULocalizedString(@"An error occurred while trying to download the file. Please try again later.", nil)];
}

- (IBAction)installAndRestart:sender
{
	NSString *currentAppPath = [[NSBundle mainBundle] bundlePath];
	NSString *newAppDownloadPath = [[downloadPath stringByDeletingLastPathComponent] stringByAppendingPathComponent:[SUInfoValueForKey(@"CFBundleName") stringByAppendingPathExtension:@"app"]];
	@try 
	{
		if (![self isAutomaticallyUpdating])
		{
			[statusController beginActionWithTitle:SULocalizedString(@"Installing update...", nil) maxProgressValue:0 statusText:nil];
			[statusController setButtonEnabled:NO];
			NSEvent *event;
			while((event = [NSApp nextEventMatchingMask:NSAnyEventMask untilDate:nil inMode:NSDefaultRunLoopMode dequeue:YES]))
				[NSApp sendEvent:event];			
		}
		
		// We assume that the archive will contain a file named {CFBundleName}.app
		// (where, obviously, CFBundleName comes from Info.plist)
		if (!SUInfoValueForKey(@"CFBundleName")) { [NSException raise:@"SUInstallException" format:@"This application has no CFBundleName! This key must be set to the application's name."]; }
		if (![[NSFileManager defaultManager] fileExistsAtPath:newAppDownloadPath])
		{
			[NSException raise:@"SUInstallException" format:@"The update archive didn't contain an application with the proper name: %@. Remember, the updated app's file name must be identical to {CFBundleName}.app", [SUInfoValueForKey(@"CFBundleName") stringByAppendingPathExtension:@"app"]];
		}
	}
	@catch(NSException *e) 
	{
		NSLog([e reason]);
		[self showUpdateErrorAlertWithInfo:SULocalizedString(@"An error occurred during installation. Please try again later.", nil)];
		[self abandonUpdate];		
	}
	
	if ([self isAutomaticallyUpdating]) // Don't do authentication if we're automatically updating; that'd be surprising.
	{
		int tag = 0;
		BOOL result = [[NSWorkspace sharedWorkspace] performFileOperation:NSWorkspaceRecycleOperation source:[currentAppPath stringByDeletingLastPathComponent] destination:@"" files:[NSArray arrayWithObject:[currentAppPath lastPathComponent]] tag:&tag];
		result &= [[NSFileManager defaultManager] movePath:newAppDownloadPath toPath:currentAppPath handler:nil];
		if (!result)
		{
			[self abandonUpdate];
			return;
		}
	}
	else // But if we're updating by the action of the user, do an authenticated move.
	{
		// Outside of the @try block because we want to be a little more informative on this error.
		if (![[NSFileManager defaultManager] movePathWithAuthentication:newAppDownloadPath toPath:currentAppPath])
		{
			[self showUpdateErrorAlertWithInfo:[NSString stringWithFormat:SULocalizedString(@"%@ does not have permission to write to the application's directory! Are you running off a disk image? If not, ask your system administrator for help.", nil), SUHostAppName()]];
			[self abandonUpdate];
			return;
		}
	}
	
	// Delete the temp folder where the archive was downloaded and extracted.
	[[NSFileManager defaultManager] removeFileAtPath:[downloadPath stringByDeletingLastPathComponent] handler:nil];		
	
	// Prompt for permission to restart if we're automatically updating.
	if ([self isAutomaticallyUpdating])
	{
		SUAutomaticUpdateAlert *alert = [[SUAutomaticUpdateAlert alloc] initWithAppcastItem:updateItem];
		if ([NSApp runModalForWindow:[alert window]] == NSAlertAlternateReturn)
		{
			[alert release];
			return;
		}
	}
	
	[[NSNotificationCenter defaultCenter] postNotificationName:SUUpdaterWillRestartNotification object:self];

	// Thanks to Allan Odgaard for this restart code, which is much more clever than mine was.
	setenv("LAUNCH_PATH", [currentAppPath UTF8String], 1);
	system("/bin/bash -c '{ for (( i = 0; i < 3000 && $(echo $(/bin/ps -xp $PPID|/usr/bin/wc -l))-1; i++ )); do\n"
		   "    /bin/sleep .2;\n"
		   "  done\n"
		   "  if [[ $(/bin/ps -xp $PPID|/usr/bin/wc -l) -ne 2 ]]; then\n"
		   "    /usr/bin/open \"${LAUNCH_PATH}\"\n"
		   "  fi\n"
		   "} &>/dev/null &'");
	[NSApp terminate:self];	
}

- (IBAction)cancelDownload:sender
{
	if (downloader)
	{
		[downloader cancel];
		[downloader release];
	}
	[self abandonUpdate];
	
	if (checkInterval)
	{
		[self scheduleCheckWithInterval:checkInterval];
	}
}

@end