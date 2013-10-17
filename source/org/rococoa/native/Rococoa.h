#import <Cocoa/Cocoa.h>
#include <objc/objc-runtime.h>

void callOnMainThread(void (*fn)(), BOOL waitUntilDone);

@interface RococoaHelper : NSObject
+ (void) callback: (NSValue*) fn;
@end


