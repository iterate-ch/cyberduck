BUILD_DIR=build
DEFAULT_BUILDSTYLE=Deployment

BUILDSTYLE=$(DEFAULT_BUILDSTYLE)

CP=ditto --rsrc

default:
	xcodebuild -target build -configuration $(BUILDSTYLE)

release:
	xcodebuild -target release -configuration $(BUILDSTYLE)

clean:
	ant clean

install:
	killall Cyberduck || true
	$(CP) $(BUILD_DIR)/Cyberduck.app $(APPLICATIONS_DIR)
	open $(APPLICATIONS_DIR)/Cyberduck.app
