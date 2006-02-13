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

@implementation UKCrashReporter

-(id) init
{
	if( self = [super init] )
	{
		[self checkForCrash];
	}
	
	return self;
}


-(void)	dealloc
{
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
//		This is written so it works back to OS X 10.2, or at least cracefully
//		fails by just doing nothing on such older OSs. This also should never
//		throw exceptions or anything on failure. This is an additional service
//		for the developer and *mustn't* interfere with regular operation of the
//		application.
// -----------------------------------------------------------------------------

- (void) checkForCrash
{
	NS_DURING
		// Try whether the classes we need to talk to the CGI are present:
		Class			NSMutableURLRequestClass = NSClassFromString( @"NSMutableURLRequest" );
		Class			NSURLConnectionClass = NSClassFromString( @"NSURLConnection" );
		if( NSMutableURLRequestClass == Nil || NSURLConnectionClass == Nil )
			NS_VOIDRETURN;
		
		// Get the log file, it's last change date and last report date:
		NSString*		appName = [[[NSBundle mainBundle] infoDictionary] objectForKey: @"CFBundleExecutable"];
		NSString*		crashLogsFolder = [@"~/Library/Logs/CrashReporter/" stringByExpandingTildeInPath];
		NSString*		crashLogName = [appName stringByAppendingString: @".crash.log"];
		NSString*		crashLogPath = [crashLogsFolder stringByAppendingPathComponent: crashLogName];
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
									NSLocalizedStringFromTable( @"Send", @"Crash", @"" ),
									NSLocalizedStringFromTable( @"Don't Send", @"Crash", @"" ),
									@"", appName ) )
				{
					// Fetch the newest report from the log:
					NSString*			crashLog = [NSString stringWithContentsOfFile: crashLogPath];
					NSArray*			separateReports = [crashLog componentsSeparatedByString: @"\n\n**********\n\n"];
					NSString*			currentReport = [separateReports count] > 0 ? [separateReports objectAtIndex: [separateReports count] -1] : @"*** Couldn't read Report ***";
					NSData*				crashReport = [currentReport dataUsingEncoding: NSUTF8StringEncoding];	// 1 since report 0 is empty (file has a delimiter at the top).
					
					// Prepare a request:
					NSMutableURLRequest *postRequest = [NSMutableURLRequestClass requestWithURL: 
						[NSURL URLWithString: @"http://crash.cyberduck.ch/report/"]];
					NSString            *boundary = @"0xKhTmLbOuNdArY";
					NSURLResponse       *response = nil;
					NSError             *error = nil;
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
					[postRequest setHTTPBody: formData];
					
					(NSData*) [NSURLConnectionClass sendSynchronousRequest: postRequest returningResponse: &response error: &error];
				}
				// Remember we just reported a crash, so we don't ask twice:
				[[NSUserDefaults standardUserDefaults] setFloat: [[NSDate date] timeIntervalSince1970] forKey: @"crashreport.date"];
				[[NSUserDefaults standardUserDefaults] synchronize];
			}
			else {
				//NSLog(@"No new crash log found.");
			}
		}
		else {
			//NSLog(@"No crash log found at all.");
		}
	NS_HANDLER
		NSLog(@"Exception during check for crash: %@",localException);
	NS_ENDHANDLER
}

@end
