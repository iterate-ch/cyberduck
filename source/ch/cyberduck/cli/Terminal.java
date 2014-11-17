package ch.cyberduck.cli;

/*
 * Copyright (c) 2002-2014 David Kocher. All rights reserved.
 * http://cyberduck.io/
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * Bug fixes, suggestions and comments should be sent to:
 * feedback@cyberduck.io
 */

import ch.cyberduck.core.*;
import ch.cyberduck.core.editor.Editor;
import ch.cyberduck.core.editor.EditorFactory;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.local.ApplicationFinder;
import ch.cyberduck.core.local.ApplicationFinderFactory;
import ch.cyberduck.core.local.ApplicationQuitCallback;
import ch.cyberduck.core.threading.LoggingUncaughtExceptionHandler;
import ch.cyberduck.core.transfer.DownloadTransfer;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferErrorCallback;
import ch.cyberduck.core.transfer.TransferItem;
import ch.cyberduck.core.transfer.TransferOptions;
import ch.cyberduck.core.transfer.TransferSpeedometer;
import ch.cyberduck.core.transfer.UploadTransfer;
import ch.cyberduck.ui.action.SingleTransferWorker;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @version $Id$
 */
public class Terminal {
    private static final Logger log = Logger.getLogger(Terminal.class);

    static {
        Thread.setDefaultUncaughtExceptionHandler(new LoggingUncaughtExceptionHandler());
    }

    static {
        PreferencesFactory.set(new TerminalPreferences());
        ProtocolFactory.register();
    }

    private final DefaultPathKindDetector detector
            = new DefaultPathKindDetector();

    private final Preferences preferences = Preferences.instance();

    private final ApplicationFinder finder = ApplicationFinderFactory.get();

    private enum Exit {
        success,
        failure
    }

    private CommandLine input;

    private Options options;

    public Terminal(final Options options, final CommandLine input) {
        this.options = options;
        this.input = input;
    }

    /**
     * duck <source> <target>
     *
     * @param arguments Command line arguments
     */
    public static void main(final String... args) throws IOException {
        final Options options = TerminalOptionsBuilder.options();
        try {
            final CommandLineParser parser = new PosixParser();
            final CommandLine input = parser.parse(options, args);
            if(log.isInfoEnabled()) {
                log.info(String.format("Parsed options %s", input));
            }
            final Terminal terminal = new Terminal(options, input);
            switch(terminal.execute()) {
                case success:
                    System.exit(0);
                case failure:
                    System.exit(1);
            }
        }
        catch(ParseException e) {
            System.err.println(e.getMessage());
            TerminalHelpPrinter.help(options);
            System.exit(1);
        }
    }

    protected Exit execute() {
        final Console console = new Console();
        if(input.hasOption("help")) {
            TerminalHelpPrinter.help(options);
            return Exit.success;
        }
        if(input.hasOption("version")) {
            console.printf("%s %s (%s)%n",
                    preferences.getProperty("application.name"),
                    preferences.getProperty("application.version"),
                    preferences.getProperty("application.revision"));
            return Exit.success;
        }
        if(TerminalOptionsInputValidator.validate(input)) {
            final List arguments = input.getArgList();
            final String uri = arguments.get(0).toString();
            final Host host = HostParser.parse(uri);
            if(uri.indexOf("://", 0) != -1) {
                final Protocol protocol = ProtocolFactory.forName(uri.substring(0, uri.indexOf("://", 0)));
                host.setProtocol(protocol);
                host.setPort(protocol.getDefaultPort());
            }
            if(input.hasOption("username")) {
                host.getCredentials().setUsername(input.getOptionValue("username"));
            }
            if(input.hasOption("password")) {
                host.getCredentials().setPassword(input.getOptionValue("password"));
            }
            final Path remote;
            switch(host.getProtocol().getType()) {
                case s3:
                case googlestorage:
                case swift:
                case azure:
                    if(StringUtils.isBlank(host.getProtocol().getDefaultHostname())) {
                        remote = new Path(host.getDefaultPath(), EnumSet.of(detector.detect(host.getDefaultPath())));
                    }
                    else {
                        final String container = host.getHostname();
                        final String key = host.getDefaultPath();
                        remote = new Path(new Path(container, EnumSet.of(Path.Type.volume, Path.Type.directory)),
                                key, EnumSet.of(detector.detect(host.getDefaultPath())));
                        host.setHostname(host.getProtocol().getDefaultHostname());
                    }
                    break;
                default:
                    remote = new Path(host.getDefaultPath(), EnumSet.of(detector.detect(host.getDefaultPath())));
            }
            host.setDefaultPath(remote.getParent().getAbsolute());
            final Session session = SessionFactory.create(host);
            final TerminalProgressListener listener = new TerminalProgressListener();
            Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                @Override
                public void uncaughtException(final Thread t, final Throwable e) {
                    listener.message(String.format("Uncaught failure with error message %s. Quitting application…", e.getMessage()));
                    System.exit(1);
                }
            });
            try {
                final ConnectionService connect = new LoginConnectionService(
                        new TerminalLoginCallback(), new TerminalHostKeyVerifier(), PasswordStoreFactory.get(),
                        listener, input.hasOption("verbose") ? new TerminalTranscriptListener() : new DisabledTranscriptListener());
                if(!connect.check(session, Cache.<Path>empty())) {
                    throw new ConnectionCanceledException();
                }
                if(input.hasOption("edit")) {
                    return this.edit(remote, session);
                }
                else {
                    final Transfer.Type type = TerminalOptionsTransferTypeFinder.get(input);
                    final Local local;
                    switch(type) {
                        case download:
                            if(arguments.size() == 1) {
                                local = LocalFactory.get(System.getProperty("user.dir"), remote.getName());
                            }
                            else {
                                if(LocalFactory.get(arguments.get(1).toString()).isDirectory()) {
                                    local = LocalFactory.get(arguments.get(1).toString(), remote.getName());
                                }
                                else {
                                    local = LocalFactory.get(arguments.get(1).toString());
                                }
                            }
                            break;
                        case upload:
                            local = LocalFactory.get(arguments.get(1).toString());
                            break;
                        default:
                            return Exit.failure;
                    }
                    return this.transfer(type, host, remote, local, session, listener);
                }
            }
            catch(ConnectionCanceledException e) {
                return Exit.success;
            }
            catch(BackgroundException e) {
                final StringAppender b = new StringAppender();
                b.append(e.getMessage());
                b.append(e.getDetail());
                listener.message(b.toString());
            }
            return Exit.failure;
        }
        else {
            TerminalHelpPrinter.help(options);
            return Exit.failure;
        }
    }

    protected Exit transfer(final Transfer.Type type,
                            final Host host,
                            final Path remote, final Local local,
                            final Session session,
                            final TerminalProgressListener listener) throws BackgroundException {
        final Transfer transfer;
        switch(type) {
            case download:
                transfer = new DownloadTransfer(host, Arrays.asList(new TransferItem(remote, local)));
                break;
            case upload:
                transfer = new UploadTransfer(host, Arrays.asList(new TransferItem(remote, local)));
            default:
                return Exit.failure;
        }
        final TransferSpeedometer meter = new TransferSpeedometer(transfer);
        final SingleTransferWorker worker = new SingleTransferWorker(session, transfer, new TransferOptions().reload(true), meter,
                new TerminalTransferPrompt(transfer), new TerminalTransferErrorCallback(), new TerminalTransferItemCallback(),
                listener, new TerminalStreamListener(meter), new TerminalLoginCallback());
        worker.run();
        return Exit.success;
    }

    protected Exit edit(final Path remote, final Session session) {
        final TerminalController controller = new TerminalController();
        final EditorFactory factory = EditorFactory.instance();
        final Editor editor;
        if(StringUtils.isNotBlank(input.getOptionValue("edit"))) {
            editor = factory.create(controller, session,
                    finder.getDescription(input.getOptionValue("edit")), remote);
        }
        else {
            editor = factory.create(controller, session, remote);
        }
        final CountDownLatch lock = new CountDownLatch(1);
        final AtomicBoolean failed = new AtomicBoolean();
        final TransferErrorCallback error = new TransferErrorCallback() {
            @Override
            public boolean prompt(final BackgroundException failure) throws BackgroundException {
                final StringAppender appender = new StringAppender();
                appender.append(failure.getMessage());
                appender.append(failure.getDetail());
                System.err.println(appender.toString());
                failed.set(true);
                return false;
            }
        };
        editor.open(new ApplicationQuitCallback() {
            @Override
            public void callback() {
                lock.countDown();
            }
        }, error);
        if(failed.get()) {
            return Exit.failure;
        }
        controller.message("Close the editor application to exit…");
        try {
            lock.await();
        }
        catch(InterruptedException e) {
            return Exit.failure;
        }
        return Exit.success;
    }
}
