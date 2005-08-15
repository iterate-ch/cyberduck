BUILD_DIR=build
DEFAULT_BUILDSTYLE=Deployment

BUILDSTYLE=$(DEFAULT_BUILDSTYLE)

PROJECT=Cyberduck.xcodeproj

CP=ditto --rsrc

export JAVA_HOME=/System/Library/Frameworks/JavaVM.framework/Versions/1.4/Home

default:
#	xcodebuild -project Spotlight\ Importer/Spotlight\ Importer.xcodeproj -target Spotlight\ Importer -configuration $(BUILDSTYLE)
#	xcodebuild -project Upload\ Automator\ Action/Upload\ Automator\ Action.xcodeproj -target Upload\ files -configuration $(BUILDSTYLE)
#	xcodebuild -project Download\ Automator\ Action/Download\ Automator\ Action.xcodeproj -target Download\ files -configuration $(BUILDSTYLE)
	xcodebuild -project $(PROJECT) -target build -configuration $(BUILDSTYLE)

release: default
	xcodebuild -project $(PROJECT) -target release -configuration $(BUILDSTYLE)

clean:
	ant clean
