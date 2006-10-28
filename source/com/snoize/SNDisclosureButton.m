/*
 Copyright (c) 2002, Kurt Revis.  All rights reserved.

 Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:

 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of Snoize nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.

 THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

#import "SNDisclosureButton.h"


@interface SNDisclosureButton (Private)

- (void)configureDisclosureButton;
- (NSImage *)imageNamed:(NSString *)imageName;

@end


@implementation SNDisclosureButton

- (id)initWithFrame:(NSRect)frame
{
    if (!(self = [super initWithFrame:frame]))
        return nil;

    [self configureDisclosureButton];
    
    return self;
}

- (void)awakeFromNib
{
    [self configureDisclosureButton];
}

@end


@implementation SNDisclosureButton (Private)

- (void)configureDisclosureButton
{
    NSImage *image;

    if ((image = [self imageNamed:@"SNDisclosureArrowRight"]))
        [self setImage:image];
    if ((image = [self imageNamed:@"SNDisclosureArrowDown"]))
    	[self setAlternateImage:image];
	
    [[self cell] setHighlightsBy:NSPushInCellMask];
	if([[NSUserDefaults standardUserDefaults] boolForKey:@"browser.alert.transcript.visible"])
		[self setState:NSOnState];
	else
		[self setState:NSOffState];
}

- (NSImage *)imageNamed:(NSString *)imageName
{
    NSBundle *bundle;
    NSString *imagePath;
    NSImage *image = nil;

    bundle = [NSBundle bundleForClass:[self class]];
    imagePath = [bundle pathForImageResource:imageName];
    if (imagePath) {        
        image = [[[NSImage alloc] initByReferencingFile:imagePath] autorelease];
        if (!image)
            NSLog(@"SNDisclosureButton: couldn't read image: %@", imagePath);
    } else {
        NSLog(@"SNDisclosureButton: couldn't find image: %@", imageName);
    }

    return image;
}

@end
