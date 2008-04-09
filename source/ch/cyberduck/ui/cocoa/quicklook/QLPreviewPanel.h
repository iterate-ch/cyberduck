#import <Cocoa/Cocoa.h>

@interface QLPreviewPanel : NSPanel

+ (id)sharedPreviewPanel;
- (BOOL)isOpen;
- (void)closeWithEffect:(int)fp8;
- (void)makeKeyAndOrderFront:(id)fp8;
- (void)makeKeyAndOrderFrontWithEffect:(int)fp8;
- (void)makeKeyAndGoFullscreenWithEffect:(int)fp8;
- (void)makeKeyAndOrderFrontWithEffect:(int)fp8 canClose:(BOOL)fp12;
- (void)_makeKeyAndOrderFrontWithEffect:(int)fp8 canClose:(BOOL)fp12 willOpen:(BOOL)fp16 toFullscreen:(BOOL)fp20;
- (void)setURLs:(id)fp8 currentIndex:(unsigned int)fp12 preservingDisplayState:(BOOL)fp16;
- (void)setURLs:(id)fp8 preservingDisplayState:(BOOL)fp12;
- (void)setURLs:(id)fp8;
- (id)URLs;
- (void)selectNextItem;
- (void)selectPreviousItem;
- (void)setDelegate:(id)fp8;

@end