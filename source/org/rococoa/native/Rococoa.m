#include "Rococoa.h"

void callOnMainThread(void (*fn)(), BOOL waitUntilDone) {
	// NSLog(@"callOnMainThread function at address %p", fn);
	// Pool is required as we're being called from Java, which probably doesn't have a pool to 
	// allocate the NSValue from.
	NSAutoreleasePool * pool = [[NSAutoreleasePool alloc] init];
	[RococoaHelper performSelectorOnMainThread: @selector(callback:) 
		withObject: [NSValue valueWithPointer: fn] waitUntilDone: waitUntilDone];
	[pool release];
}

@implementation RococoaHelper : NSObject

+ (void) callback: (NSValue*) fnAsValue {
	void (*fn)() = [fnAsValue pointerValue]; 
	(*fn)();
} 

@end
