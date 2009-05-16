//
//  CTGradientView.m
//
//  Created by Chad Weider on 12/2/05.
//  Copyright (c) 2005 Chad Weider.
//  Some rights reserved: <http://creativecommons.org/licenses/by/2.5/>
//

#import "CTGradientView.h"

@implementation CTGradientView

- (id)initWithFrame:(NSRect)frame {
    self = [super initWithFrame:frame];
    if (self) 
		{
		myGradient = [[CTGradient unifiedNormalGradient] retain];
		
		angle = 90;
		isRadial = NO;
		}
    return self;
}

- (void)dealloc
{
  [myGradient release];
  [super dealloc];
}

- (BOOL)isOpaque
{
  return NO;
}

- (void)drawRect:(NSRect)rect
{
  if(isRadial)
	[myGradient radialFillRect:rect];
  else
	[myGradient fillRect:rect angle:angle];
}

@end