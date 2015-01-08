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
import ch.cyberduck.core.local.Application;
import ch.cyberduck.core.local.ApplicationFinder;
import ch.cyberduck.core.local.ApplicationFinderFactory;
import ch.cyberduck.core.local.ApplicationQuitCallback;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.threading.LoggingUncaughtExceptionHandler;
import ch.cyberduck.core.transfer.CopyTransfer;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferErrorCallback;
import ch.cyberduck.core.transfer.TransferItem;
import ch.cyberduck.core.transfer.TransferOptions;
import ch.cyberduck.core.transfer.TransferSpeedometer;
import ch.cyberduck.core.worker.DisconnectWorker;
import ch.cyberduck.core.worker.SessionListWorker;
import ch.cyberduck.core.worker.SingleTransferWorker;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
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

    private final Preferences preferences;

    private Cache<Path> cache;

    private enum Exit {
        success,
        failure
    }

    private CommandLine input;

    private Options options;

    public Terminal(final Options options, final CommandLine input) {
        this(new TerminalPreferences(), options, input);
    }

    public Terminal(final Preferences defaults, final Options options, final CommandLine input) {
        this.preferences = defaults;
        ProtocolFactory.register();
        this.options = options;
        if(log.isInfoEnabled()) {
            log.info(String.format("Parsed options %s from input %s", options, input));
        }
        this.input = input;
        this.cache = new Cache<Path>();
        LocaleFactory.get().setDefault(Locale.getDefault().getLanguage());
    }

    /**
     * duck <source> <target>
     *
     * @param args Command line arguments
     */
    public static void main(final String... args) throws IOException {
        final TerminalPreferences defaults = new TerminalPreferences();
        PreferencesFactory.set(defaults);
        open(args, defaults);
    }

    protected static void open(final String[] args, final Preferences defaults) {
        final Options options = TerminalOptionsBuilder.options();
        try {
            final CommandLineParser parser = new PosixParser();
            final CommandLine input = parser.parse(options, args);
            final Terminal terminal = new Terminal(defaults, options, input);
            switch(terminal.execute()) {
                case success:
                    System.exit(0);
                case failure:
                    System.exit(1);
            }
        }
        catch(ParseException e) {
            final Console console = new Console();
            console.printf("%s\n", e.getMessage());
            console.printf("Try '%s' for more options.\n", "duck --help");
            System.exit(1);
        }
        catch(Throwable error) {
            error.printStackTrace(System.err);
            System.exit(1);
        }
    }

    protected Exit execute() {
        final Console console = new Console();
        if(input.hasOption(TerminalAction.help.name())) {
            TerminalHelpPrinter.print(options);
            return Exit.success;
        }
        if(input.hasOption(TerminalAction.version.name())) {
            console.printf("%s %s (%s)%n",
                    preferences.getProperty("application.name"),
                    preferences.getProperty("application.version"),
                    preferences.getProperty("application.revision"));
            return Exit.success;
        }
        if(!new TerminalOptionsInputValidator().validate(input)) {
            console.printf("Try '%s' for more options.\n", "duck --help");
            return Exit.failure;
        }
        this.configure(input);
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(final Thread t, final Throwable e) {
                console.printf("Uncaught failure with error message %s. Quitting application…", e.getMessage());
                System.exit(1);
            }
        });
        Session session = null;
        try {
            final TerminalAction action = TerminalActionFinder.get(input);
            if(null == action) {
                return Exit.failure;
            }
            final String uri = input.getOptionValue(action.name());
            final Host host = new UriParser(input).parse(uri);
            if(input.hasOption(TerminalOptionsBuilder.Params.username.name())) {
                final Credentials credentials = host.getCredentials();
                credentials.setUsername(input.getOptionValue(TerminalOptionsBuilder.Params.username.name()));
            }
            if(input.hasOption(TerminalOptionsBuilder.Params.password.name())) {
                final Credentials credentials = host.getCredentials();
                credentials.setPassword(input.getOptionValue(TerminalOptionsBuilder.Params.password.name()));
            }
            if(input.hasOption(TerminalOptionsBuilder.Params.identity.name())) {
                host.getCredentials().setIdentity(LocalFactory.get(input.getOptionValue(TerminalOptionsBuilder.Params.identity.name())));
            }
            session = SessionFactory.create(host);
            final Path remote = new PathParser(input).parse(uri);
            switch(action) {
                case edit:
                    return this.edit(session, remote);
                case list:
                    return this.list(session, remote, input.hasOption(TerminalOptionsBuilder.Params.longlist.name()));
            }
            final Transfer transfer;
            switch(action) {
                case download:
                case upload:
                case synchronize:
                    transfer = TerminalTransferFactory.create(action, host,
                            new ArrayList<TransferItem>(new SingleTransferItemFinder().find(input, action, remote)));
                    break;
                case copy:
                    transfer = new CopyTransfer(
                            host,
                            new UriParser(input).parse(input.getOptionValues(action.name())[1]),
                            Collections.singletonMap(
                                    new PathParser(input).parse(uri),
                                    new PathParser(input).parse(input.getOptionValues(action.name())[1]))
                    );
                    // Connect
                    this.connect(((CopyTransfer) transfer).getDestination());
                    break;
                default:
                    throw new BackgroundException(LocaleFactory.localizedString("Unknown"),
                            String.format("Unknown transfer type %s", action.name()));
            }
            return this.transfer(transfer, session);
        }
        catch(ConnectionCanceledException e) {
            return Exit.success;
        }
        catch(BackgroundException e) {
            final StringAppender b = new StringAppender();
            b.append(e.getMessage());
            b.append(e.getDetail());
            console.printf("\n%s\n", b.toString());
        }
        finally {
            this.disconnect(session);
        }
        return Exit.failure;
    }

    protected void configure(final CommandLine input) {
        final boolean preserve = input.hasOption(TerminalOptionsBuilder.Params.preserve.name());
        preferences.setProperty("queue.upload.permissions.change", preserve);
        preferences.setProperty("queue.upload.timestamp.change", preserve);
        preferences.setProperty("queue.download.permissions.change", preserve);
        preferences.setProperty("queue.download.timestamp.change", preserve);
    }

    protected Exit transfer(final Transfer transfer, final Session session) throws BackgroundException {
        // Connect
        this.connect(session);
        // Transfer
        final TransferSpeedometer meter = new TransferSpeedometer(transfer);
        final SingleTransferWorker worker = new SingleTransferWorker(session, transfer, new TransferOptions().reload(true), meter,
                new TerminalTransferPrompt(transfer), new TerminalTransferErrorCallback(), new TerminalTransferItemCallback(),
                input.hasOption(TerminalOptionsBuilder.Params.quiet.name()) ? new DisabledListProgressListener() : new TerminalProgressListener(),
                new TerminalStreamListener(meter), new TerminalLoginCallback());
        if(worker.run()) {
            return Exit.success;
        }
        return Exit.failure;
    }

    protected Exit list(final Session session, final Path remote, final boolean verbose) throws BackgroundException {
        // Connect
        this.connect(session);
        // List
        final SessionListWorker worker = new SessionListWorker(session, Cache.<Path>empty(), remote,
                new TerminalListProgressListener(verbose));
        worker.run();
        return Exit.success;
    }

    protected Exit edit(final Session session, final Path remote) throws BackgroundException {
        // Connect
        this.connect(session);
        // Edit
        final TerminalController controller = new TerminalController();
        final EditorFactory factory = EditorFactory.instance();
        final Application application;
        final ApplicationFinder finder = ApplicationFinderFactory.get();
        if(StringUtils.isNotBlank(input.getOptionValue(TerminalOptionsBuilder.Params.application.name()))) {
            application = finder.getDescription(input.getOptionValue(TerminalOptionsBuilder.Params.application.name()));
            if(!finder.isInstalled(application)) {
                throw new BackgroundException(LocaleFactory.localizedString("Unknown"),
                        String.format("Application %s not found", input.getOptionValue(TerminalOptionsBuilder.Params.application.name())));
            }
        }
        else {
            application = factory.getEditor(remote.getName());
        }
        if(!finder.isInstalled(application)) {
            throw new BackgroundException(LocaleFactory.localizedString("Unknown"),
                    String.format("No application found to edit %s", remote.getName()));
        }
        final Editor editor = factory.create(controller, session, application, remote);
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

    protected void connect(final Session session) throws BackgroundException {
        final TranscriptListener transcript = input.hasOption(TerminalOptionsBuilder.Params.verbose.name())
                ? new TerminalTranscriptListener() : new DisabledTranscriptListener();
        final ConnectionService connect = new LoginConnectionService(
                new TerminalLoginCallback(), new TerminalHostKeyVerifier(), PasswordStoreFactory.get(),
                input.hasOption(TerminalOptionsBuilder.Params.quiet.name()) ? new DisabledListProgressListener() : new TerminalProgressListener(), transcript);
        connect.connect(session, cache);
    }

    protected void disconnect(final Session session) {
        if(session != null) {
            final DisconnectWorker close = new DisconnectWorker(session, cache);
            close.run();
        }
    }
}
