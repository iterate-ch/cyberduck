package ch.cyberduck.core;

import ch.cyberduck.binding.foundation.NSAppleScript;
import ch.cyberduck.binding.foundation.NSDictionary;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.LocalAccessDeniedException;
import ch.cyberduck.core.local.Application;
import ch.cyberduck.core.local.ApplicationFinder;
import ch.cyberduck.core.local.ApplicationFinderFactory;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.rococoa.ObjCObjectByReference;

import java.text.MessageFormat;

public class ApplescriptTerminalService implements TerminalService {
    private static final Logger log = LogManager.getLogger(ApplescriptTerminalService.class);

    private final ApplicationFinder finder
        = ApplicationFinderFactory.get();

    private final Preferences preferences
        = PreferencesFactory.get();

    @Override
    public void open(final Host host, final Path workdir) throws AccessDeniedException {
        final boolean identity = host.getCredentials().isPublicKeyAuthentication();
        final Application application;
        switch(StringUtils.lowerCase(finder.find(".command").getIdentifier())) {
            case "com.googlecode.iterm2":
            case "com.apple.terminal":
                application = finder.find(".command");
                break;
            default:
                log.warn(String.format("Unsupported application %s assigned", finder.find(".command")));
                application = finder.getDescription(preferences.getProperty("terminal.bundle.identifier"));
        }
        if(!finder.isInstalled(application)) {
            throw new LocalAccessDeniedException("Unable to determine default Terminal application");
        }
        String ssh = MessageFormat.format(preferences.getProperty("terminal.command.ssh"),
            identity ? String.format("-i \"%s\"", host.getCredentials().getIdentity().getAbsolute()) : StringUtils.EMPTY,
            host.getCredentials().getUsername(),
            host.getHostname(),
            String.valueOf(host.getPort()), this.escape(workdir.getAbsolute()));
        if(log.isInfoEnabled()) {
            log.info(String.format("Execute SSH command %s", ssh));
        }
        // Escape
        ssh = StringUtils.replace(ssh, "\\", "\\\\");
        // Escape all " for do script command
        ssh = StringUtils.replace(ssh, "\"", "\\\"");
        if(log.isInfoEnabled()) {
            log.info("Escaped SSH Command for Applescript:" + ssh);
        }
        // Applescript
        final String applescript;
        switch(application.getIdentifier()) {
            case "com.googlecode.iterm2":
                applescript = MessageFormat.format(preferences.getProperty("terminal.command.iterm2"), ssh);
                break;
            default:
                applescript = MessageFormat.format(preferences.getProperty("terminal.command.default"), ssh);
                break;
        }
        final String command
            = "tell application \"" + application.getName() + "\""
            + "\n"
            + "activate"
            + "\n"
            + applescript
            + "\n"
            + "end tell";
        if(log.isInfoEnabled()) {
            log.info(String.format("Execute AppleScript %s", command));
        }
        final NSAppleScript as = NSAppleScript.createWithSource(command);
        final ObjCObjectByReference error = new ObjCObjectByReference();
        if(null == as.executeAndReturnError(error)) {
            final NSDictionary d = error.getValueAs(NSDictionary.class);
            throw new LocalAccessDeniedException(String.format("Failure running script in %s. %s",
                application.getName(), d.objectForKey("NSAppleScriptErrorBriefMessage")));
        }
    }

    protected String escape(final String path) {
        final StringBuilder escaped = new StringBuilder();
        for(char c : path.toCharArray()) {
            if(StringUtils.isAlphanumeric(String.valueOf(c))) {
                escaped.append(c);
            }
            else {
                escaped.append("\\").append(c);
            }
        }
        return escaped.toString();
    }
}
