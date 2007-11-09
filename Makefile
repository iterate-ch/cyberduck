BUILD_DIR=build
DEFAULT_BUILDSTYLE=Deployment

BUILDSTYLE=$(DEFAULT_BUILDSTYLE)

PROJECT=Cyberduck.xcodeproj

default:
	cd Spotlight\ Importer; make
	xcodebuild -project $(PROJECT) -target build -configuration $(BUILDSTYLE)

release:
	cd Spotlight\ Importer; make release
	cd Dashboard\ Widget; make release
	xcodebuild -project $(PROJECT) -target release -configuration $(BUILDSTYLE)
	sudo codesign -s "Cyberduck Code Signing Certificate" $(BUILD_DIR)/$(BUILDSTYLE)/Cyberduck.app

nightly:
	cd Spotlight\ Importer; make release
	cd Dashboard\ Widget; make release
	xcodebuild -project $(PROJECT) -target nightly -configuration $(BUILDSTYLE)
	sudo codesign -s "Cyberduck Code Signing Certificate" $(BUILD_DIR)/$(BUILDSTYLE)/Cyberduck.app

clean:
	cd Spotlight\ Importer; make clean
	cd Dashboard\ Widget; make clean
	ant clean
