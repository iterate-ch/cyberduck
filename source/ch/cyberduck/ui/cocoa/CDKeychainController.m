#import "CDKeychainController.h"

@implementation CDKeychainController

- (void)awakeFromNib {
	[keyChainCheckbox setEnabled:true];

    if(keychain) {
        NSLog(@"Sucessfully opened keychain");
		[[NSNotificationCenter defaultCenter] addObserver:self
												 selector:@selector(attributesChanged:)
													 name:NSControlTextDidEndEditingNotification
												   object:servernameField];
		[[NSNotificationCenter defaultCenter] addObserver:self
												 selector:@selector(attributesChanged:)
													 name:NSControlTextDidEndEditingNotification
												   object:usernameField];
    } 
	else {
        NSLog(@"Failed to open keychain");
    }
}

- (void)dealloc {
    [[NSNotificationCenter defaultCenter] removeObserver:self];
    [super dealloc];
}

- (void)attributesChanged:(NSNotification *)notification {
	NSLog(@"attributesChanged");
	if([servernameField stringValue] != nil && [usernameField stringValue] != nil) {
		NSString *password = [keychain passwordForGenericService:[servernameField stringValue]
													  forAccount:[usernameField stringValue]];
		if(password) {
			NSLog(@"Password found in keychain");
			[passwordField setStringValue:password];
		}
		else {
			[passwordField setStringValue:[[NSString alloc] init]];
			NSLog(@"Password NOT found in keychain");
		}
	}
}

- (IBAction)keychainCheckboxSelectionChanged:(id)sender {
	NSLog(@"Keychain checkbox selection changed");
	if([servernameField stringValue] != nil && 
	   [usernameField stringValue] != nil && 
	   [passwordField stringValue] != nil) {
		NSLog(@"Adding password to keychain");
	[keychain addGenericPassword:[passwordField stringValue]
					   onService:[servernameField stringValue] //@todo [protocolField stringValue]
					  forAccount:[usernameField stringValue]
				 replaceExisting:true]; 
	}
}

@end
