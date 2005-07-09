BUILD_DIR=build
DEFAULT_BUILDSTYLE=Deployment

BUILDSTYLE=$(DEFAULT_BUILDSTYLE)

CP=ditto --rsrc

export JAVA_HOME=/System/Library/Frameworks/JavaVM.framework/Versions/1.4/Home

default:
	make -f Spotlight\ Importer/Makefile
	xcodebuild -target build -configuration $(BUILDSTYLE)

release:
	make -f Spotlight\ Importer/Makefile
	xcodebuild -target release -configuration $(BUILDSTYLE)

clean:
	ant clean
