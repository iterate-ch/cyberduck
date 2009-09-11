//
//  UKCrashReporter.m
//  NiftyFeatures
//
//  Created by Uli Kusterer on Sat Feb 04 2006.
//  Copyright (c) 2006 M. Uli Kusterer. All rights reserved.
//

// -----------------------------------------------------------------------------
//	Headers:
// -----------------------------------------------------------------------------

#import "UKCrashReporter.h"
#import "UKSystemInfo.h"
#import <Cocoa/Cocoa.h>

NSString*	UKCrashReporterFindTenFiveCrashReportPath( NSString* appName, NSString* crashLogsFolder );

@implementation UKCrashReporter

-(id) init
{
	if( (self = [super init]) )
	{
		[self checkForCrash];
	}

	return self;
}

-(void) dealloc
{
	[connection release];
	connection = nil;

	[super dealloc];
}

// -----------------------------------------------------------------------------
//	checkForCrash:
//		This submits the crash report to a CGI form as a POST request by
//		passing it as the request variable "crashlog".
//	
//		KNOWN LIMITATION:	If the app crashes several times in a row, only the
//							last crash report will be sent because this doesn't
//							walk through the log files to try and determine the
//							dates of all reports.
//
//		This is written so it works back to OS X 10.2, or at least gracefully
//		fails by just doing nothing on such older OSs. This also should never
//		throw exceptions or anything on failure. This is an additional service
//		for the developer and *mustn't* interfere with regular operation of the
//		application.
// -----------------------------------------------------------------------------

- (void) checkForCrash
{
	NSAutoreleasePool*	pool = [[NSAutoreleasePool alloc] init];
	
	NS_DURING
		// Try whether the classes we need to talk to the CGI are present:
		Class			NSMutableURLRequestClass = NSClassFromString( @"NSMutableURLRequest" );
		Class			NSURLConnectionClass = NSClassFromString( @"NSURLConnection" );
		if( NSMutableURLRequestClass == Nil || NSURLConnectionClass == Nil )
		{
			[pool release];
			NS_VOIDRETURN;
		}
		
		SInt32	sysvMajor = 0, sysvMinor = 0, sysvBugfix = 0;
		UKGetSystemVersionComponents( &sysvMajor, &sysvMinor, &sysvBugfix );
		BOOL	isTenFiveOrBetter = sysvMajor >= 10 && sysvMinor >= 5;
		
		// Get the log file, its last change date and last report date:
		NSString*		appName = [[[NSBundle mainBundle] infoDictionary] objectForKey: @"CFBundleExecutable"];
		NSString*		appRevision = [[[NSBundle mainBundle] infoDictionary] objectForKey: @"CFBundleVersion"];
		NSString*		crashLogsFolder = [@"~/Library/Logs/CrashReporter/" stringByExpandingTildeInPath];
		NSString*		crashLogName = [appName stringByAppendingString: @".crash.log"];
		NSString*		crashLogPath = nil;
		if( !isTenFiveOrBetter )
			crashLogPath = [crashLogsFolder stringByAppendingPathComponent: crashLogName];
		else
			crashLogPath = UKCrashReporterFindTenFiveCrashReportPath( appName, crashLogsFolder );
		NSDictionary*	fileAttrs = [[NSFileManager defaultManager] fileAttributesAtPath: crashLogPath traverseLink: YES];
		NSDate*			lastTimeCrashLogged = (fileAttrs == nil) ? nil : [fileAttrs fileModificationDate];
		NSTimeInterval	lastCrashReportInterval = [[NSUserDefaults standardUserDefaults] floatForKey: @"crashreport.date"];
		NSDate*			lastTimeCrashReported = [NSDate dateWithTimeIntervalSince1970: lastCrashReportInterval];
		
		if( lastTimeCrashLogged )	// We have a crash log file and its mod date? Means we crashed sometime in the past.
		{
			// If we never before reported a crash or the last report lies before the last crash:
			if( [lastTimeCrashReported compare: lastTimeCrashLogged] == NSOrderedAscending )
			{
				//NSLog(@"New crash log found! Running alert panel");
				if( NSRunAlertPanel( NSLocalizedStringFromTable( @"Do you want to report the last crash?", @"Crash", @"" ),
									NSLocalizedStringFromTable( @"The application %@ has recently crashed. To help improve it, you can send the crash log to the author.", @"Crash", @"" ),
									NSLocalizedStringFromTable( @"Send", @"Crash", @"" ), // NSAlertDefaultReturn
									NSLocalizedStringFromTable( @"Don't Send", @"Crash", @"" ), // NSAlertAlternateReturn
									@"", appName ) )
				{
                    // Fetch the newest report from the log:
                    NSString*			crashLog = [NSString stringWithContentsOfFile: crashLogPath encoding:NSASCIIStringEncoding error:nil];
                    NSArray*			separateReports = [crashLog componentsSeparatedByString: @"\n\n**********\n\n"];
                    NSString*			currentReport = [separateReports count] > 0 ? [separateReports objectAtIndex: [separateReports count] -1] : @"*** Couldn't read Report ***";	// 1 since report 0 is empty (file has a delimiter at the top).
					NSData*				crashReport = [currentReport dataUsingEncoding: NSUTF8StringEncoding];	// 1 since report 0 is empty (file has a delimiter at the top).

                    NSString            *boundary = @"0xKhTmLbOuNdArY";

                    // Prepare a request:
                    NSString            *url = [[@"http://crash.cyberduck.ch/report" stringByAppendingString:@"?revision="] stringByAppendingString:appRevision];
                    NSMutableURLRequest *postRequest = [NSMutableURLRequestClass requestWithURL:[NSURL URLWithString: url]];
                    NSString            *contentType = [NSString stringWithFormat:@"multipart/form-data; boundary=%@",boundary];
                    NSString			*agent = [NSString stringWithFormat:@"Cyberduck (%@)", [[NSBundle mainBundle] objectForInfoDictionaryKey:@"CFBundleVersion"]];

                    // Add form trappings to crashReport:
                    NSData*			header = [[NSString stringWithFormat:@"--%@\r\nContent-Disposition: form-data; name=\"crashlog\"\r\n\r\n", boundary] dataUsingEncoding:NSUTF8StringEncoding];
                    NSMutableData*	formData = [[header mutableCopy] autorelease];
                    [formData appendData: crashReport];
                    [formData appendData:[[NSString stringWithFormat:@"\r\n--%@--\r\n",boundary] dataUsingEncoding:NSUTF8StringEncoding]];

                    // setting the headers:
                    [postRequest setHTTPMethod: @"POST"];
                    [postRequest setValue: contentType forHTTPHeaderField: @"Content-Type"];
                    [postRequest setValue: agent forHTTPHeaderField: @"User-Agent"];
                    NSString *contentLength = [NSString stringWithFormat:@"%lu", [formData length]];
                    [postRequest setValue: contentLength forHTTPHeaderField: @"Content-Length"];
                    [postRequest setHTTPBody: formData];

                    // Go into progress mode and kick off the HTTP post:
                    connection = [[NSURLConnection connectionWithRequest: postRequest delegate: self] retain];
                }
                else {
                    // Don't ask twice:
                    [[NSUserDefaults standardUserDefaults] setFloat: [[NSDate date] timeIntervalSince1970] forKey: @"crashreport.date"];
                    [[NSUserDefaults standardUserDefaults] synchronize];
                }
			}
		}
	NS_HANDLER
		NSLog(@"Error during check for crash: %@",localException);
	NS_ENDHANDLER
	
	[pool drain];
}

NSString*	UKCrashReporterFindTenFiveCrashReportPath( NSString* appName, NSString* crashLogsFolder )
{
	NSDirectoryEnumerator*	enny = [[NSFileManager defaultManager] enumeratorAtPath: crashLogsFolder];
	NSString*				currName = nil;
	NSString*				crashLogPrefix = [NSString stringWithFormat: @"%@_",appName];
	NSString*				crashLogSuffix = @".crash";
	NSString*				foundName = nil;
	NSDate*					foundDate = nil;
	
	// Find the newest of our crash log files:
	while(( currName = [enny nextObject] ))
	{
		if( [currName hasPrefix: crashLogPrefix] && [currName hasSuffix: crashLogSuffix] )
		{
			NSDate*	currDate = [[enny fileAttributes] fileModificationDate];
			if( foundName )
			{
				if( [currDate isGreaterThan: foundDate] )
				{
					foundName = currName;
					foundDate = currDate;
				}
			}
			else
			{
				foundName = currName;
				foundDate = currDate;
			}
		}
	}
	
	if( !foundName )
		return nil;
	else
		return [crashLogsFolder stringByAppendingPathComponent: foundName];
}

-(void)	connectionDidFinishLoading:(NSURLConnection *)conn
{
	[connection release];
	connection = nil;
	
	// Now that we successfully sent this crash, don't report it again:
    [[NSUserDefaults standardUserDefaults] setFloat: [[NSDate date] timeIntervalSince1970] forKey: @"crashreport.date"];
    [[NSUserDefaults standardUserDefaults] synchronize];
}


-(void)	connection:(NSURLConnection *)conn didFailWithError:(NSError *)error
{
	[connection release];
	connection = nil;
}

@end