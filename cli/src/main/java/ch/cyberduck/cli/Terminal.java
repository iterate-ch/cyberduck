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
import ch.cyberduck.core.azure.AzureProtocol;
import ch.cyberduck.core.b2.B2Protocol;
import ch.cyberduck.core.dav.DAVProtocol;
import ch.cyberduck.core.dav.DAVSSLProtocol;
import ch.cyberduck.core.dropbox.DropboxProtocol;
import ch.cyberduck.core.editor.DefaultEditorListener;
import ch.cyberduck.core.editor.Editor;
import ch.cyberduck.core.editor.EditorFactory;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.features.Home;
import ch.cyberduck.core.ftp.FTPProtocol;
import ch.cyberduck.core.ftp.FTPTLSProtocol;
import ch.cyberduck.core.googledrive.DriveProtocol;
import ch.cyberduck.core.googlestorage.GoogleStorageProtocol;
import ch.cyberduck.core.hubic.HubicProtocol;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.irods.IRODSProtocol;
import ch.cyberduck.core.local.Application;
import ch.cyberduck.core.local.ApplicationFinder;
import ch.cyberduck.core.local.ApplicationFinderFactory;
import ch.cyberduck.core.local.ApplicationQuitCallback;
import ch.cyberduck.core.openstack.SwiftProtocol;
import ch.cyberduck.core.pool.SessionPool;
import ch.cyberduck.core.pool.SingleSessionPool;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.s3.S3Protocol;
import ch.cyberduck.core.sftp.SFTPProtocol;
import ch.cyberduck.core.spectra.SpectraProtocol;
import ch.cyberduck.core.ssl.CertificateStoreX509TrustManager;
import ch.cyberduck.core.ssl.DefaultTrustManagerHostnameCallback;
import ch.cyberduck.core.ssl.PreferencesX509KeyManager;
import ch.cyberduck.core.threading.DisconnectBackgroundAction;
import ch.cyberduck.core.threading.SessionBackgroundAction;
import ch.cyberduck.core.threading.WorkerBackgroundAction;
import ch.cyberduck.core.transfer.CopyTransfer;
import ch.cyberduck.core.transfer.DisabledTransferErrorCallback;
import ch.cyberduck.core.transfer.DisabledTransferPrompt;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferAction;
import ch.cyberduck.core.transfer.TransferItem;
import ch.cyberduck.core.transfer.TransferOptions;
import ch.cyberduck.core.transfer.TransferPrompt;
import ch.cyberduck.core.transfer.TransferSpeedometer;
import ch.cyberduck.core.worker.DeleteWorker;
import ch.cyberduck.core.worker.SessionListWorker;
import ch.cyberduck.core.worker.Worker;
import ch.cyberduck.fs.FilesystemFactory;
import ch.cyberduck.fs.FilesystemWorker;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

public class Terminal {
    private static final Logger log = Logger.getLogger(Terminal.class);

    private final Preferences preferences;

    private final TerminalController controller;

    private final TerminalPromptReader reader;

    private final PathCache cache;

    private final ProgressListener progress;

    private final TranscriptListener transcript;

    private enum Exit {
        success,
        failure
    }

    private final CommandLine input;

    private final Options options;

    public Terminal(final TerminalPreferences defaults, final Options options, final CommandLine input) {
        this.preferences = defaults.withDefaults(input);
        ProtocolFactory.register(
                new FTPProtocol(),
                new FTPTLSProtocol(),
                new SFTPProtocol(),
                new DAVProtocol(),
                new DAVSSLProtocol(),
                new SwiftProtocol(),
                new S3Protocol(),
                new GoogleStorageProtocol(),
                new AzureProtocol(),
                new IRODSProtocol(),
                new SpectraProtocol(),
                new B2Protocol(),
                new DriveProtocol(),
                new HubicProtocol(),
                new DriveProtocol(),
                new DropboxProtocol()
        );
        this.options = options;
        if(log.isInfoEnabled()) {
            log.info(String.format("Parsed options %s from input %s", options, input));
        }
        this.input = input;
        this.cache = new PathCache(preferences.getInteger("browser.cache.size"));
        this.progress = input.hasOption(TerminalOptionsBuilder.Params.quiet.name())
                ? new DisabledListProgressListener() : new TerminalProgressListener();
        this.transcript = input.hasOption(TerminalOptionsBuilder.Params.verbose.name())
                ? new TerminalTranscriptListener() : new DisabledTranscriptListener();
        this.reader = input.hasOption(TerminalOptionsBuilder.Params.assumeyes.name())
                ? new DisabledTerminalPromptReader() : new InteractiveTerminalPromptReader();
        this.controller = new TerminalController(progress, transcript);
    }

    /**
     * duck <source> <target>
     *
     * @param args Command line arguments
     */
    public static void main(final String... args) throws IOException {
        open(args, new TerminalPreferences());
    }

    protected static void open(final String[] args, final TerminalPreferences defaults) {
        // Register preferences
        PreferencesFactory.set(defaults);
        final Options options = TerminalOptionsBuilder.options();
        final Console console = new Console();
        try {
            final CommandLineParser parser = new PosixParser();
            final CommandLine input = parser.parse(options, args);
            final Terminal terminal = new Terminal(defaults, options, input);
            switch(terminal.execute()) {
                case success:
                    console.printf("%s%n", StringUtils.EMPTY);
                    System.exit(0);
                case failure:
                    console.printf("%s%n", StringUtils.EMPTY);
                    System.exit(1);
            }
        }
        catch(ParseException e) {
            console.printf("%s%n", e.getMessage());
            console.printf("Try '%s' for more options.%n", "duck --help");
            System.exit(1);
        }
        catch(FactoryException e) {
            console.printf("%s%n", e.getMessage());
            System.exit(1);
        }
        catch(Throwable error) {
            error.printStackTrace(System.err);
            System.exit(1);
        }
    }

    protected Exit execute() {
        final Console console = new Console();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(final Thread t, final Throwable e) {
                console.printf("Uncaught failure with error message %s. Quitting application…", e.getMessage());
                System.exit(1);
            }
        });
        if(input.hasOption(TerminalAction.help.name())) {
            TerminalHelpPrinter.print(options);
            return Exit.success;
        }
        if(input.hasOption(TerminalAction.version.name())) {
            TerminalVersionPrinter.print(preferences);
            return Exit.success;
        }
        if(!new TerminalOptionsInputValidator().validate(input)) {
            console.printf("Try '%s' for more options.%n", "duck --help");
            return Exit.failure;
        }
        this.configure(input);
        SessionPool pool = null;
        try {
            final TerminalAction action = TerminalActionFinder.get(input);
            if(null == action) {
                return Exit.failure;
            }
            final String uri = input.getOptionValue(action.name());
            final Host host = new CommandLineUriParser(input).parse(uri);
            final LoginConnectionService connect = new LoginConnectionService(new TerminalLoginService(input,
                    new TerminalLoginCallback(reader)), new TerminalHostKeyVerifier(reader), progress, transcript);
            final Session<?> session = SessionFactory.create(host,
                    new CertificateStoreX509TrustManager(
                            new DefaultTrustManagerHostnameCallback(host),
                            new TerminalCertificateStore(reader)
                    ),
                    new PreferencesX509KeyManager(host, new TerminalCertificateStore(reader)), PasswordStoreFactory.get(),
                    new TerminalPasswordCallback());
            pool = new SingleSessionPool(connect, session, cache);
            final Path remote;
            if(new CommandLinePathParser(input).parse(uri).getAbsolute().startsWith(TildePathExpander.PREFIX)) {
                final Home home = pool.getFeature(Home.class);
                remote = new TildePathExpander(home.find()).expand(new CommandLinePathParser(input).parse(uri));
            }
            else {
                remote = new CommandLinePathParser(input).parse(uri);
            }
            switch(action) {
                case edit:
                    return this.edit(pool, remote);
                case list:
                case longlist:
                    return this.list(pool, remote, input.hasOption(TerminalOptionsBuilder.Params.longlist.name()));
                case mount:
                    return this.mount(pool);
                case delete:
                    return this.delete(pool, remote);
            }
            final Transfer transfer;
            switch(action) {
                case download:
                case upload:
                case synchronize:
                    transfer = new TerminalTransferFactory().create(input, host, remote,
                            new ArrayList<TransferItem>(new SingleTransferItemFinder().find(input, action, remote)));
                    break;
                case copy:
                    final Host target = new CommandLineUriParser(input).parse(input.getOptionValues(action.name())[1]);
                    transfer = new CopyTransfer(host, session,
                            Collections.singletonMap(
                                    remote, new CommandLinePathParser(input).parse(input.getOptionValues(action.name())[1])
                            )
                    );
                    break;
                default:
                    throw new BackgroundException(LocaleFactory.localizedString("Unknown"),
                            String.format("Unknown transfer type %s", action.name()));
            }
            return this.transfer(transfer, pool);
        }
        catch(ConnectionCanceledException e) {
            log.warn("Connection canceled", e);
            return Exit.success;
        }
        catch(BackgroundException e) {
            final StringAppender b = new StringAppender();
            b.append(e.getMessage());
            b.append(e.getDetail());
            console.printf("%n%s", b.toString());
        }
        finally {
            this.disconnect(pool);
        }
        return Exit.failure;
    }

    protected void configure(final CommandLine input) {
        final boolean preserve = input.hasOption(TerminalOptionsBuilder.Params.preserve.name());
        preferences.setProperty("queue.upload.permissions.change", preserve);
        preferences.setProperty("queue.upload.timestamp.change", preserve);
        preferences.setProperty("queue.download.permissions.change", preserve);
        preferences.setProperty("queue.download.timestamp.change", preserve);
        final boolean retry = input.hasOption(TerminalOptionsBuilder.Params.retry.name());
        if(retry) {
            if(StringUtils.isNotBlank(input.getOptionValue(TerminalOptionsBuilder.Params.retry.name()))) {
                preferences.setProperty("connection.retry",
                        NumberUtils.toInt(input.getOptionValue(TerminalOptionsBuilder.Params.retry.name()), 1));
            }
            else {
                preferences.setProperty("connection.retry", 1);
            }
        }
        else {
            preferences.setProperty("connection.retry", 0);
        }
        final boolean udt = input.hasOption(TerminalOptionsBuilder.Params.udt.name());
        if(udt) {
            preferences.setProperty("s3.download.udt.threshold", 0L);
            preferences.setProperty("s3.upload.udt.threshold", 0L);
        }
        if(input.hasOption(TerminalOptionsBuilder.Params.parallel.name())) {
            preferences.setProperty("queue.maxtransfers",
                    NumberUtils.toInt(input.getOptionValue(TerminalOptionsBuilder.Params.parallel.name()), 2));
        }
    }

    protected Exit transfer(final Transfer transfer, final SessionPool session) {
        // Transfer
        final TransferSpeedometer meter = new TransferSpeedometer(transfer);
        final TransferPrompt prompt;
        final Host host = transfer.getHost();
        if(input.hasOption(TerminalOptionsBuilder.Params.parallel.name())) {
            host.setTransfer(Host.TransferType.concurrent);
        }
        else {
            host.setTransfer(Host.TransferType.newconnection);
        }
        if(input.hasOption(TerminalOptionsBuilder.Params.existing.name())) {
            prompt = new DisabledTransferPrompt() {
                @Override
                public TransferAction prompt(final TransferItem file) {
                    return TransferAction.forName(input.getOptionValue(TerminalOptionsBuilder.Params.existing.name()));
                }
            };
        }
        else if(input.hasOption(TerminalOptionsBuilder.Params.quiet.name())) {
            prompt = new DisabledTransferPrompt() {
                @Override
                public TransferAction prompt(final TransferItem file) {
                    return TransferAction.comparison;
                }
            };
        }
        else {
            prompt = new TerminalTransferPrompt(transfer.getType());
        }
        final TerminalTransferBackgroundAction action = new TerminalTransferBackgroundAction(controller, reader,
                session,
                transfer, new TransferOptions().reload(true), prompt, meter,
                input.hasOption(TerminalOptionsBuilder.Params.quiet.name())
                        ? new DisabledStreamListener() : new TerminalStreamListener(meter)
        );
        this.execute(action);
        if(action.hasFailed()) {
            return Exit.failure;
        }
        return Exit.success;
    }

    protected Exit mount(final SessionPool session) {
        final SessionBackgroundAction<Path> action = new WorkerBackgroundAction<Path>(
                controller, session, new FilesystemWorker(FilesystemFactory.get(controller, session.getHost(), cache)
        ));
        this.execute(action);
        if(action.hasFailed()) {
            return Exit.failure;
        }
        return Exit.success;
    }

    protected Exit list(final SessionPool session, final Path remote, final boolean verbose) {
        final SessionListWorker worker = new SessionListWorker(cache, remote,
                new TerminalListProgressListener(reader, verbose));
        final SessionBackgroundAction<AttributedList<Path>> action = new TerminalBackgroundAction<AttributedList<Path>>(
                controller,
                session, worker);
        this.execute(action);
        if(action.hasFailed()) {
            return Exit.failure;
        }
        return Exit.success;
    }

    protected Exit delete(final SessionPool session, final Path remote) throws BackgroundException {
        final List<Path> files = new ArrayList<Path>();
        for(TransferItem i : new DeletePathFinder().find(input, TerminalAction.delete, remote)) {
            files.add(i.remote);
        }
        final DeleteWorker worker;
        if(StringUtils.containsAny(remote.getName(), '*')) {
            worker = new DeleteWorker(new TerminalLoginCallback(reader), files, cache, new DownloadGlobFilter(remote.getName()), progress);
        }
        else {
            worker = new DeleteWorker(new TerminalLoginCallback(reader), files, cache, progress);
        }
        final SessionBackgroundAction action = new TerminalBackgroundAction<List<Path>>(
                controller,
                session, worker);
        this.execute(action);
        if(action.hasFailed()) {
            return Exit.failure;
        }
        return Exit.success;
    }

    protected Exit edit(final SessionPool session, final Path remote) throws BackgroundException {
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
        final Worker<Transfer> worker = editor.open(new ApplicationQuitCallback() {
            @Override
            public void callback() {
                lock.countDown();
            }
        }, new DisabledTransferErrorCallback(), new DefaultEditorListener(controller, session, editor));
        final SessionBackgroundAction action = new TerminalBackgroundAction<Transfer>(controller, session, worker);
        this.execute(action);
        if(action.hasFailed()) {
            return Exit.failure;
        }
        try {
            lock.await();
        }
        catch(InterruptedException e) {
            return Exit.failure;
        }
        return Exit.success;
    }

    protected void disconnect(final SessionPool session) {
        if(session != null) {
            controller.background(new DisconnectBackgroundAction(controller, session));
        }
    }

    protected <T> boolean execute(final SessionBackgroundAction<T> action) {
        try {
            controller.background(action).get();
            if(action.hasFailed()) {
                return false;
            }
            return true;
        }
        catch(InterruptedException e) {
            return false;
        }
        catch(ExecutionException e) {
            return false;
        }
    }

}
