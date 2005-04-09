#import "NSString+CarbonFSSpecCreation.h"

@implementation NSString (CarbonFSSpecCreation)

- (BOOL) getFSRef:(FSRef*)fsRef createFileIfNecessary:(BOOL)createFile
{
    NSFileManager* fileManager = [NSFileManager defaultManager];
    CFURLRef urlRef;
    Boolean gotFSRef;
    
    // Check whether the file exists already.  If not, create an empty file if requested.
    if (![fileManager fileExistsAtPath:self]) {
        if (createFile) {
            if (![@"" writeToFile:self atomically:YES]) {
                return NO;
            }
        } else {
            return NO;
        }
    }

    // Create a CFURL with the specified POSIX path.
    urlRef = CFURLCreateWithFileSystemPath( kCFAllocatorDefault,
                                            (CFStringRef) self,
                                            kCFURLPOSIXPathStyle,
                                            FALSE /* isDirectory */ );
    if (urlRef == NULL) {
//        printf( "** Couldn't make a CFURLRef for the file.\n" );
        return NO;
    }
    
    // Try to create an FSRef from the URL.  (If the specified file doesn't exist, this
    // function will return false, but if we've reached this code we've already insured
    // that the file exists.)
    gotFSRef = CFURLGetFSRef( urlRef, fsRef );
    CFRelease( urlRef );

    if (!gotFSRef) {
//        printf( "** Couldn't get an FSRef for the file.\n" );
        return NO;
    }
    
    return YES;
}

- (BOOL) getFSSpec:(FSSpec*)fsSpec createFileIfNecessary:(BOOL)createFile
{
    FSRef fsRef;

    if (![self getFSRef:&fsRef createFileIfNecessary:createFile])
        return NO;
    
    if (FSGetCatalogInfo( &fsRef,
                          kFSCatInfoNone,
                          NULL,
                          NULL,
                          fsSpec,
                          NULL ) != noErr) {
        //        printf( "** Couldn't get an FSSpec for the file.\n" );
        return NO;
    }

    return YES;
}

@end
