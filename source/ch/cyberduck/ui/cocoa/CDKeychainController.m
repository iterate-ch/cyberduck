#import "CDKeychainController.h"

@implementation CDKeychainController

- (void)awakeFromNib {
	[keyChainCheckbox setEnabled:true];

//    NSArray *keychainList = keychainsForUser(nil);
    keychain = [Keychain defaultKeychain]; 
//	Keychain *current;
//    NSEnumerator *keychainEnumerator = [keychainList objectEnumerator];
	
//    while (current = (Keychain*)[keychainEnumerator nextObject]) {
//        NSLog(@"Keychain path: %@", [current path]);
//    }
	
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
		
//        chain = [[[KeychainSearch keychainSearchWithKeychain:keychain] anySearchResults] retain];
    } else {
        NSLog(@"Failed to open keychain");
    }
}

- (void)dealloc {
    [[NSNotificationCenter defaultCenter] removeObserver:self];
    [super dealloc];
}

/*- (void)servernameChanged:(NSNotification *)notification {
	NSLog(@"servernameChanged");
	[self attributesChanged:[notification object]];
}

- (void)usernameChanged:(NSNotification *)notification {
	NSLog(@"usernameChanged");
	[self attributesChanged:[notification object]];
}
*/

- (void)attributesChanged:(NSNotification *)notification {
	NSLog(@"attributesChanged");
	NSString *password = [keychain passwordForGenericService:[servernameField stringValue]
												  forAccount:[usernameField stringValue]];
	if(password) {
		NSLog(@"Password found in keychain");
		[passwordField setStringValue:password];
	}
	else {
		[passwordField setStringValue:nil];
		NSLog(@"Password NOT found in keychain");
	}
}

- (IBAction)keychainCheckboxSelectionChanged:(id)sender {
	NSLog(@"Keychain checkbox selection changed");
	[keychain addGenericPassword:[passwordField stringValue]
					   onService:[servernameField stringValue] 
					  forAccount:[usernameField stringValue]
				 replaceExisting:true]; 
}

@end
