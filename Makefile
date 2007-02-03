BUILD_DIR=build
DEFAULT_BUILDSTYLE=Deployment

BUILDSTYLE=$(DEFAULT_BUILDSTYLE)

PROJECT=Cyberduck.xcodeproj

export JAVA_HOME=/System/Library/Frameworks/JavaVM.framework/Versions/1.4/Home

default:
	cd Spotlight\ Importer; make
	xcodebuild -project $(PROJECT) -target build -configuration $(BUILDSTYLE)

release:
	cd Spotlight\ Importer; make release
	cd Dashboard\ Widget; make release
	xcodebuild -project $(PROJECT) -target release -configuration $(BUILDSTYLE)

nightly:
	cd Spotlight\ Importer; make release
	cd Dashboard\ Widget; make release
	xcodebuild -project $(PROJECT) -target nightly -configuration $(BUILDSTYLE)

clean:
	cd Spotlight\ Importer; make clean
	cd Dashboard\ Widget; make clean
	ant clean
