BUILD_DIR=build
BUILD_DIR_PLUGIN=Plugin/build
BUILDSTYLE=Release
PROJECT=Plugin/Plugin.xcodeproj

default:
	xcodebuild -project $(PROJECT) -configuration $(BUILDSTYLE)
	cp -R $(BUILD_DIR_PLUGIN)/$(BUILDSTYLE)/Cyberduck.widgetplugin Cyberduck.wdgt
	mkdir -p build
	rsync --archive --exclude=.svn Cyberduck.wdgt/ $(BUILD_DIR)/Cyberduck.wdgt
	cd $(BUILD_DIR)/Cyberduck.wdgt/Cyberduck.widgetplugin/Contents; mv Info.plist Info.plist.tmp; sed s/\\*revision\\*/`svnversion ../../../..`/g Info.plist.tmp > Info.plist; rm Info.plist.tmp
	cd $(BUILD_DIR)/Cyberduck.wdgt; mv Info.plist Info.plist.tmp; sed s/\\*revision\\*/`svnversion ../..`/g Info.plist.tmp > Info.plist; rm Info.plist.tmp
	cd $(BUILD_DIR); zip -vryX Cyberduck.wdgt.zip Cyberduck.wdgt

clean:
	rm -rf $(BUILD_DIR_PLUGIN)
	rm -rf $(BUILD_DIR)
