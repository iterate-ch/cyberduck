BUILD_DIR=build
DEFAULT_BUILDSTYLE=Deployment
#DEFAULT_BUILDSTYLE=Development
APPLICATIONS_DIR=/Applications

BUILDSTYLE=$(DEFAULT_BUILDSTYLE)

CP=ditto --rsrc

default:
	xcodebuild -target build -buildstyle $(BUILDSTYLE)

release:
	xcodebuild -target release -buildstyle $(BUILDSTYLE)

clean:
	ant clean

install:
	killall Cyberduck || true
	$(CP) $(BUILD_DIR)/Cyberduck.app $(APPLICATIONS_DIR)
	open $(APPLICATIONS_DIR)/Cyberduck.app
