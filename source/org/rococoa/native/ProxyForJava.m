/*
 * Copyright 2007, 2008 Duncan McGregor
 *
 * This file is part of Rococoa, a library to allow Java to talk to Cocoa.
 *
 * Rococoa is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Rococoa is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Rococoa.  If not, see <http://www.gnu.org/licenses/>.
 */

#import "ProxyForJava.h"

id proxyForJavaObject(void* methodInvokedCallback, void* methodSignatureCallback) {
	return [[ProxyForJava alloc] initWithMethodInvokedCallback: methodInvokedCallback methodSignatureCallback: methodSignatureCallback];
}

@interface ProxyForJava (Private)
+ (char *)cstringPtrForSelector:(CFStringRef) cfstring;
@end

@implementation ProxyForJava

- (id) initWithMethodInvokedCallback: (void*) theMethodInvokedCallback methodSignatureCallback: (void*) theMethodSignatureCallback {
	self = [super init];
	if (self != nil) {
		methodInvokedCallback = theMethodInvokedCallback;
		methodSignatureCallback = theMethodSignatureCallback;
	}
	return self;
}

// Passes a given invocation to the real object the proxy represents.
- (void)forwardInvocation:(NSInvocation *) anInvocation {
	// calls back to Java on methodInvokedCallback,
	SEL selector = [anInvocation selector];
	NSString* selectorName = NSStringFromSelector(selector);
	// NSLog(@"forwardInvocation for %@", selectorName);
	methodInvokedCallback([ProxyForJava cstringPtrForSelector:(CFStringRef) selectorName], anInvocation);
}

- (BOOL)respondsToSelector:(SEL)aSelector {
	// NSString* selectorName = NSStringFromSelector(aSelector);
	// NSLog(@"respondsToSelector %@", selectorName);
	NSMethodSignature* signature = [self methodSignatureForSelector:aSelector];
	return signature != nil;
}

// Override this method in your concrete subclass to return a proper NSMethodSignature object for the
// given selector and the class your proxy objects stand in for.
- (NSMethodSignature *)methodSignatureForSelector:(SEL)aSelector {
	NSString* selectorName = NSStringFromSelector(aSelector);
	// NSLog(@"methodSignatureForSelector %@", selectorName);
	const char* methodSignature = methodSignatureCallback([ProxyForJava cstringPtrForSelector:(CFStringRef) selectorName]);
	if (NULL == methodSignature) {
		// No method with signature of selector implemented
		// NSLog(@"No method with signature of selector implemented: %@", selectorName);
		return nil;
	}
	NSMethodSignature* result = [NSMethodSignature signatureWithObjCTypes: methodSignature];
	return result;
}

+ (const char *)cstringPtrForSelector:(CFStringRef) selectorName {
	const char* selectorNameChar = CFStringGetCStringPtr(selectorName, CFStringGetFastestEncoding(selectorName));
	if (NULL == selectorNameChar) {
		// NSLog(@"CFStringGetCStringPtr failed for selector %@", selectorName);
		return NULL;
	}
	return selectorNameChar;
}

- (NSUInteger)hash {
	SEL selector = @selector(hashCode);
	NSMethodSignature *signature = [self methodSignatureForSelector:selector];
	if(nil == signature) {
		return [super hash];
	}
	NSInvocation *invocation = [NSInvocation invocationWithMethodSignature:signature];
	[self forwardInvocation:invocation];
	NSUInteger result;
	[invocation getReturnValue:&result];
	return result;
}

- (BOOL)isEqual:(id)object {
	SEL selector = @selector(equals:);
	NSMethodSignature *signature = [self methodSignatureForSelector:selector];
	if(nil == signature) {
		return [super isEqual:object];
	}
	NSInvocation *invocation = [NSInvocation invocationWithMethodSignature:signature];
	[self forwardInvocation:invocation];
	BOOL result;
	[invocation getReturnValue:&result];
	return result;
}

- (BOOL)isKindOfClass:(Class)aClass {
	return NO;
}

- (BOOL) isMemberOfClass: (Class)aClass {
	return NO;
}

@end
