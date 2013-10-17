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

#import <Cocoa/Cocoa.h>

id proxyForJavaObject(void* methodInvokedCallback, void* methodSignatureCallback);

@interface ProxyForJava : NSObject {

void (*methodInvokedCallback)(const char*, id);
char* (*methodSignatureCallback)(const char*);

}

- (id) initWithMethodInvokedCallback: (void*) theMethodInvokedCallback methodSignatureCallback: (void*) theMethodSignatureCallback;

- (void)forwardInvocation:(NSInvocation *) anInvocation;

- (NSMethodSignature *)methodSignatureForSelector:(SEL)aSelector;

@end
