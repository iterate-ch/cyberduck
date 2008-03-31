//
//  CTGradientView.h
//
//  Created by Chad Weider on 12/2/05.
//  Copyright (c) 2005 Chad Weider.
//  Some rights reserved: <http://creativecommons.org/licenses/by/2.5/>
//

#import <Cocoa/Cocoa.h>
#import "CTGradient.h"

@interface CTGradientView : NSView
	{
	CTGradient *myGradient;
	
	float angle;
	bool  isRadial;
	}

- (IBAction)changeType :(id)sender;
- (IBAction)changeAngle:(id)sender;
- (IBAction)changeStyle:(id)sender;

@end
