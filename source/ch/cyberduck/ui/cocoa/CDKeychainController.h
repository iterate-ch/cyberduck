/* CDKeychainController */

#import <Cocoa/Cocoa.h>
#import <Keychain/Keychain.h>
#import <Keychain/KeychainSearch.h>

@interface CDKeychainController : NSObject
{
    IBOutlet NSButton *keyChainCheckbox;
    IBOutlet NSTextField *passwordField;
    IBOutlet NSTextField *servernameField;
	IBOutlet NSTextField *usernameField;
	
	Keychain *keychain;
}
- (IBAction)keychainCheckboxSelectionChanged:(id)sender;
@end
