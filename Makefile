BUILD_DIR=build
DEFAULT_BUILDSTYLE=Release

BUILDSTYLE=$(DEFAULT_BUILDSTYLE)

PROJECT=Cyberduck.xcodeproj

default:
	xcodebuild -project $(PROJECT) -target build -configuration $(BUILDSTYLE)

release:
	cd Spotlight\ Importer; make release
	xcodebuild -project $(PROJECT) -target release -configuration $(BUILDSTYLE)

beta:
	cd Spotlight\ Importer; make release
	xcodebuild -project $(PROJECT) -target beta -configuration $(BUILDSTYLE)

nightly:
	cd Spotlight\ Importer; make release
	xcodebuild -project $(PROJECT) -target nightly -configuration $(BUILDSTYLE)

clean:
	cd Spotlight\ Importer; make clean
	ant clean
