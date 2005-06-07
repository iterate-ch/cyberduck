#include <CoreFoundation/CoreFoundation.h>
#include <CoreServices/CoreServices.h> 

#import <Cocoa/Cocoa.h>

Boolean GetMetadataForFile(void* thisInterface, 
						   CFMutableDictionaryRef attributes, 
						   CFStringRef contentTypeUTI,
						   CFStringRef file)
{
    NSAutoreleasePool *pool = [[NSAutoreleasePool alloc] init];
    NSDictionary *bookmark = [NSDictionary dictionaryWithContentsOfFile:(NSString *)file];
	if(bookmark) 
	{
		[(NSMutableDictionary *)attributes setObject:[bookmark objectForKey:@"Hostname"]
											  forKey:@"ch_sudo_cyberduck_hostname"];
	}
	[pool release];
	
    return TRUE;
}
