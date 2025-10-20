package ch.cyberduck.cli;

/*
 * Copyright (c) 2002-2017 iterate GmbH. All rights reserved.
 * https://cyberduck.io/
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
 */

import ch.cyberduck.core.*;
import ch.cyberduck.core.cdn.Distribution;
import ch.cyberduck.core.editor.DefaultEditorListener;
import ch.cyberduck.core.editor.Editor;
import ch.cyberduck.core.editor.EditorFactory;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.NotfoundException;
import ch.cyberduck.core.io.DisabledStreamListener;
import ch.cyberduck.core.local.Application;
import ch.cyberduck.core.local.ApplicationFinder;
import ch.cyberduck.core.local.ApplicationFinderFactory;
import ch.cyberduck.core.local.TemporaryFileServiceFactory;
import ch.cyberduck.core.logging.LoggerPrintStream;
import ch.cyberduck.core.pool.SessionPool;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.serviceloader.AutoServiceLoaderFactory;
import ch.cyberduck.core.ssl.CertificateStoreX509TrustManager;
import ch.cyberduck.core.ssl.DefaultTrustManagerHostnameCallback;
import ch.cyberduck.core.ssl.PreferencesX509KeyManager;
import ch.cyberduck.core.threading.DisabledAlertCallback;
import ch.cyberduck.core.threading.DisconnectBackgroundAction;
import ch.cyberduck.core.threading.SessionBackgroundAction;
import ch.cyberduck.core.transfer.CopyTransfer;
import ch.cyberduck.core.transfer.DisabledTransferPrompt;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferAction;
import ch.cyberduck.core.transfer.TransferItem;
import ch.cyberduck.core.transfer.TransferOptions;
import ch.cyberduck.core.transfer.TransferPrompt;
import ch.cyberduck.core.transfer.TransferSpeedometer;
import ch.cyberduck.core.vault.LoadingVaultLookupListener;
import ch.cyberduck.core.vault.VaultMetadata;
import ch.cyberduck.core.vault.VaultRegistry;
import ch.cyberduck.core.vault.VaultRegistryFactory;
import ch.cyberduck.core.worker.AttributesWorker;
import ch.cyberduck.core.worker.CreateDirectoryWorker;
import ch.cyberduck.core.worker.DeleteWorker;
import ch.cyberduck.core.worker.DistributionPurgeWorker;
import ch.cyberduck.core.worker.HomeFinderWorker;
import ch.cyberduck.core.worker.ListWorker;
import ch.cyberduck.core.worker.LoadVaultWorker;
import ch.cyberduck.core.worker.MoveWorker;
import ch.cyberduck.core.worker.Worker;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

import com.google.common.util.concurrent.Uninterruptibles;

public class Terminal {
    private static final Logger log = LogManager.getLogger(Terminal.class);

    static {
        System.err.close();
        System.setErr(new LoggerPrintStream());
    }

    private final Preferences preferences;
    private final TerminalController controller;
    private final TerminalPromptReader reader;
    private final TerminalAlertCallback alert;
    private final Cache<Path> cache;
    private final ProgressListener progress;
    private final TranscriptListener transcript;

    private final ProtocolFactory protocols;
    private final CommandLine input;
    private final Options options;

    public Terminal(final TerminalPreferences defaults, final Options options, final CommandLine input) {
        this(ProtocolFactory.get(), defaults, options, input);
    }

    public Terminal(final ProtocolFactory protocols, final TerminalPreferences defaults, final Options options, final CommandLine input) {
        this.protocols = protocols;
        this.preferences = defaults.withDefaults(input);
        for(Protocol p : AutoServiceLoaderFactory.<Protocol>get().load(Protocol.class)) {
            protocols.register(p);
        }
        this.options = options;
        log.info("Parsed options {} from input {}", options, input);
        this.input = input;
        this.cache = new PathCache(preferences.getInteger("browser.cache.size"));
        this.progress = input.hasOption(TerminalOptionsBuilder.Params.quiet.name())
                ? new DisabledListProgressListener() : new TerminalProgressListener();
        this.transcript = input.hasOption(TerminalOptionsBuilder.Params.verbose.name())
                ? new TerminalTranscriptListener() : new DisabledTranscriptListener();
        this.reader = input.hasOption(TerminalOptionsBuilder.Params.assumeyes.name())
                ? new DisabledTerminalPromptReader() : new InteractiveTerminalPromptReader();
        this.controller = new TerminalController(progress, transcript);
        this.alert = new TerminalAlertCallback();
    }

    /**
     * duck <source> <target>
     *
     * @param args Command line arguments
     */
    public static void main(final String... args) {
        open(args, new LinuxTerminalPreferences());
    }

    protected static void open(final String[] args, final TerminalPreferences defaults) {
        // Register preferences
        PreferencesFactory.set(defaults);
        final Options options = TerminalOptionsBuilder.options();
        final Console console = new Console();
        try {
            final CommandLineParser parser = new DefaultParser();
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
            console.printf("Configuration error %s%n", e.getMessage());
            System.exit(1);
        }
        catch(Throwable error) {
            console.printf("Fatal error %s%n", error.getMessage());
            error.printStackTrace(new LoggerPrintStream());
            System.exit(1);
        }
        finally {
            // Clear temporary files
            TemporaryFileServiceFactory.get().shutdown();
        }
    }

    protected Exit execute() {
        return this.execute(new TerminalLoginCallback(reader));
    }

    protected Exit execute(final LoginCallback login) {
        final Console console = new Console();
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(final Thread t, final Throwable e) {
                console.printf("Uncaught failure with error message %s. Quitting application…", e.getMessage());
                System.exit(1);
            }
        });
        if(input.hasOption(TerminalOptionsBuilder.Params.profile.name())) {
            final String file = input.getOptionValue(TerminalOptionsBuilder.Params.profile.name());
            try {
                final Profile profile = ProfileReaderFactory.get().read(LocalFactory.get(file));
                log.debug("Register profile {}", profile);
                protocols.register(profile);
            }
            catch(AccessDeniedException e) {
                console.printf("%s%n", e.getDetail(false));
                return Exit.failure;
            }
        }
        else {
            protocols.load();
        }
        if(input.hasOption(TerminalAction.help.name())) {
            TerminalHelpPrinter.print(options);
            return Exit.success;
        }
        if(input.hasOption(TerminalAction.version.name())) {
            TerminalVersionPrinter.print(preferences);
            return Exit.success;
        }
        if(!new TerminalOptionsInputValidator(protocols).validate(input)) {
            console.printf("Try '%s' for more options.%n", "duck --help");
            return Exit.failure;
        }
        this.configure(input);
        SessionPool source = SessionPool.DISCONNECTED;
        SessionPool destination = SessionPool.DISCONNECTED;
        try {
            final TerminalAction action = TerminalActionFinder.get(input);
            if(null == action) {
                return Exit.failure;
            }
            final String uri = input.getOptionValue(action.name());
            final Host host = new CommandLineUriParser(input, protocols).parse(uri);
            final LoginConnectionService connect = new LoginConnectionService(new TerminalLoginService(input),
                    login, new TerminalHostKeyVerifier(reader), progress);
            final VaultRegistry registry = VaultRegistryFactory.get(login);
            source = SessionPoolFactory.create(connect, transcript, host,
                    new CertificateStoreX509TrustManager(new DisabledCertificateTrustCallback(), new DefaultTrustManagerHostnameCallback(host), new TerminalCertificateStore(reader)),
                    new PreferencesX509KeyManager(host, new TerminalCertificateStore(reader)), registry);
            final Path remote;
            if(StringUtils.startsWith(new CommandLinePathParser(input, protocols).parse(uri).getAbsolute(), TildePathExpander.PREFIX)) {
                final Path home = this.execute(new TerminalBackgroundAction<>(controller, source, new HomeFinderWorker()));
                remote = new Path(new TildePathExpander(home).expand(new CommandLinePathParser(input, protocols).parse(uri).getAbsolute()), EnumSet.of(Path.Type.directory));
            }
            else {
                remote = new CommandLinePathParser(input, protocols).parse(uri);
            }
            if(input.hasOption(TerminalOptionsBuilder.Params.vault.name())) {
                final Path vault;
                if(StringUtils.startsWith(input.getOptionValue(action.name()), TildePathExpander.PREFIX)) {
                    final Path home = this.execute(new TerminalBackgroundAction<>(controller, source, new HomeFinderWorker()));
                    vault = new Path(new TildePathExpander(home).expand(input.getOptionValue(action.name())), EnumSet.of(Path.Type.directory, Path.Type.vault));
                }
                else {
                    vault = new Path(input.getOptionValue(TerminalOptionsBuilder.Params.vault.name()), EnumSet.of(Path.Type.directory, Path.Type.vault));
                }
                log.debug("Attempting to load vault from {}", vault);
                try {
                    this.execute(new TerminalBackgroundAction<>(controller, source, new LoadVaultWorker(new LoadingVaultLookupListener(source.getVaultRegistry(),
                            new TerminalPasswordCallback()), new VaultMetadata(vault, VaultMetadata.Type.valueOf(preferences.getProperty("cryptomator.vault.default"))))));
                }
                catch(TerminalBackgroundException e) {
                    return Exit.failure;
                }
            }
            else {
                if(StringUtils.containsAny(remote.getName(), '*')) {
                    // Try to find parent instead
                    remote.getParent().withAttributes(this.execute(new TerminalBackgroundAction<>(controller, source,
                            new AttributesWorker(cache, remote.getParent()))));
                }
                else {
                    try {
                        // Set remote file attributes of existing file on server
                        remote.withAttributes(this.execute(new TerminalBackgroundAction<>(controller, source, new AttributesWorker(cache, remote),
                                controller, new DisabledAlertCallback())));
                    }
                    catch(TerminalBackgroundException e) {
                        if(e.getCause() instanceof NotfoundException) {
                            switch(action) {
                                case edit:
                                case download:
                                case copy:
                                case move:
                                case synchronize:
                                case delete:
                                    // We expect file on server to exist
                                    alert.print(e);
                                    throw e;
                                default:
                                    // Try to find parent instead
                                    remote.getParent().withAttributes(this.execute(new TerminalBackgroundAction<>(controller, source,
                                            new AttributesWorker(cache, remote.getParent()))));
                            }
                        }
                        else {
                            throw e;
                        }
                    }
                }
            }
            switch(action) {
                case edit:
                    return this.edit(source, remote);
                case list:
                case longlist:
                    return this.list(source, remote, input.hasOption(TerminalOptionsBuilder.Params.longlist.name()));
                case delete:
                    return this.delete(source, remote);
                case mkdir:
                    return this.mkdir(source, remote, input.getOptionValue(TerminalOptionsBuilder.Params.region.name()));
                case purge:
                    return this.purge(source, remote);
            }
            switch(action) {
                case download:
                case upload:
                case synchronize: {
                    final Transfer transfer = new TerminalTransferFactory().create(input, host, remote,
                            new ArrayList<>(new SingleTransferItemFinder().find(input, action, remote)));
                    return this.transfer(login, transfer, source, SessionPool.DISCONNECTED);
                }
                case copy: {
                    final Host target = new CommandLineUriParser(input).parse(input.getOptionValues(action.name())[1]);
                    destination = SessionPoolFactory.create(connect, transcript, target,
                            new CertificateStoreX509TrustManager(new DisabledCertificateTrustCallback(), new DefaultTrustManagerHostnameCallback(target), new TerminalCertificateStore(reader)),
                            new PreferencesX509KeyManager(target, new TerminalCertificateStore(reader)), registry);
                    final CopyTransfer transfer = new CopyTransfer(host, target,
                            Collections.singletonMap(remote, new CommandLinePathParser(input, protocols).parse(input.getOptionValues(action.name())[1])));
                    return this.transfer(login, transfer.withCache(cache), source, destination);
                }
                case move:
                    final Path target = new CommandLinePathParser(input, protocols).parse(input.getOptionValues(action.name())[1]);
                    return this.move(source, remote, target);
                default:
                    throw new BackgroundException(LocaleFactory.localizedString("Unknown"),
                            String.format("Unknown transfer type %s", action.name()));
            }
        }
        catch(BackgroundException e) {
            alert.print(e);
            return Exit.failure;
        }
        finally {
            this.disconnect(source);
            this.disconnect(destination);
        }
    }

    protected void configure(final CommandLine input) {
        final boolean preserve = input.hasOption(TerminalOptionsBuilder.Params.preserve.name());
        preferences.setDefault("queue.upload.acl.change", String.valueOf(preserve));
        preferences.setDefault("queue.upload.permissions.change", String.valueOf(preserve));
        preferences.setDefault("queue.upload.timestamp.change", String.valueOf(preserve));
        preferences.setDefault("queue.upload.file.metadata.change", String.valueOf(preserve));
        preferences.setDefault("queue.upload.file.redundancy.change", String.valueOf(preserve));
        preferences.setDefault("queue.upload.file.encryption.change", String.valueOf(preserve));
        preferences.setDefault("queue.download.permissions.change", String.valueOf(preserve));
        preferences.setDefault("queue.download.timestamp.change", String.valueOf(preserve));
        final boolean retry = input.hasOption(TerminalOptionsBuilder.Params.retry.name());
        if(retry) {
            if(StringUtils.isNotBlank(input.getOptionValue(TerminalOptionsBuilder.Params.retry.name()))) {
                preferences.setDefault("connection.retry",
                        String.valueOf(NumberUtils.toInt(input.getOptionValue(TerminalOptionsBuilder.Params.retry.name()), 1)));
            }
            else {
                preferences.setDefault("connection.retry", String.valueOf(1));
            }
        }
        else {
            preferences.setDefault("connection.retry", String.valueOf(0));
        }
        final boolean udt = input.hasOption(TerminalOptionsBuilder.Params.udt.name());
        if(udt) {
            preferences.setDefault("s3.download.udt.threshold", String.valueOf(0L));
            preferences.setDefault("s3.upload.udt.threshold", String.valueOf(0L));
        }
        if(input.hasOption(TerminalOptionsBuilder.Params.parallel.name())) {
            preferences.setDefault("queue.connections.limit",
                    String.valueOf(NumberUtils.toInt(input.getOptionValue(TerminalOptionsBuilder.Params.parallel.name()), 2)));
        }
        preferences.setDefault("connection.login.keychain", String.valueOf(!input.hasOption(TerminalOptionsBuilder.Params.nokeychain.name())));
    }

    protected Exit transfer(final LoginCallback login, final Transfer transfer, final SessionPool source, final SessionPool destination) {
        // Transfer
        final TransferSpeedometer meter = new TransferSpeedometer(transfer);
        final TransferPrompt prompt;
        final TerminalTransferErrorCallback error;
        final Host host = transfer.getSource();
        if(input.hasOption(TerminalOptionsBuilder.Params.parallel.name())) {
            host.setTransfer(Host.TransferType.concurrent);
        }
        else {
            host.setTransfer(Host.TransferType.newconnection);
        }
        if(input.hasOption(TerminalOptionsBuilder.Params.quiet.name())) {
            error = new TerminalTransferErrorCallback(reader -> false);
        }
        else {
            error = new TerminalTransferErrorCallback(reader);
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
        final TerminalTransferBackgroundAction action = new TerminalTransferBackgroundAction(controller,
                source, destination,
                transfer.withCache(cache), new TransferOptions().reload(true), prompt, login, error, meter,
                input.hasOption(TerminalOptionsBuilder.Params.quiet.name())
                        ? new DisabledStreamListener() : new TerminalStreamListener(meter)
        );
        try {
            this.execute(action);
        }
        catch(TerminalBackgroundException e) {
            return Exit.failure;
        }
        return Exit.success;
    }

    protected Exit list(final SessionPool session, final Path remote, final boolean verbose) {
        final ListWorker worker = new ListWorker(cache, remote,
                new TerminalListProgressListener(verbose));
        final SessionBackgroundAction<AttributedList<Path>> action = new TerminalBackgroundAction<>(
                controller,
                session, worker);
        try {
            this.execute(action);
        }
        catch(TerminalBackgroundException e) {
            return Exit.failure;
        }
        return Exit.success;
    }

    protected Exit delete(final SessionPool session, final Path remote) {
        final List<Path> files = new ArrayList<>();
        for(TransferItem i : new DeletePathFinder().find(input, TerminalAction.delete, remote)) {
            files.add(i.remote);
        }
        final DeleteWorker worker;
        if(StringUtils.containsAny(remote.getName(), '*')) {
            worker = new DeleteWorker(new TerminalLoginCallback(reader), files, progress, new DownloadGlobFilter(remote.getName()));
        }
        else {
            worker = new DeleteWorker(new TerminalLoginCallback(reader), files, progress, new NullFilter<>());
        }
        final SessionBackgroundAction<List<Path>> action = new TerminalBackgroundAction<>(controller, session, worker);
        try {
            this.execute(action);
        }
        catch(TerminalBackgroundException e) {
            return Exit.failure;
        }
        return Exit.success;
    }

    protected Exit move(final SessionPool session, final Path remote, final Path target) {
        final MoveWorker worker = new MoveWorker(Collections.singletonMap(remote, target), session, cache, progress, new TerminalLoginCallback(reader));
        final SessionBackgroundAction<Map<Path, Path>> action = new TerminalBackgroundAction<>(controller, session, worker);
        try {
            this.execute(action);
        }
        catch(TerminalBackgroundException e) {
            return Exit.failure;
        }
        return Exit.success;
    }

    protected Exit purge(final SessionPool session, final Path remote) {
        final DistributionPurgeWorker purge = new DistributionPurgeWorker(Collections.singletonList(remote),
                new TerminalLoginCallback(reader), Distribution.DOWNLOAD, Distribution.WEBSITE_CDN, Distribution.CUSTOM);
        final SessionBackgroundAction<Boolean> action = new TerminalBackgroundAction<>(controller, session, purge);
        try {
            this.execute(action);
        }
        catch(TerminalBackgroundException e) {
            return Exit.failure;
        }
        return Exit.success;
    }

    protected Exit mkdir(final SessionPool session, final Path remote, final String region) {
        final CreateDirectoryWorker worker = new CreateDirectoryWorker(remote, region);
        final SessionBackgroundAction<Path> action = new TerminalBackgroundAction<>(controller, session, worker);
        try {
            this.execute(action);
        }
        catch(TerminalBackgroundException e) {
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
            application = EditorFactory.getEditor(remote.getName());
        }
        if(!finder.isInstalled(application)) {
            throw new BackgroundException(LocaleFactory.localizedString("Unknown"),
                    String.format("No application found to edit %s", remote.getName()));
        }
        final Editor editor = factory.create(session.getHost(), remote, controller);
        final CountDownLatch lock = new CountDownLatch(1);
        final Worker<Transfer> worker = editor.open(application,
                lock::countDown, new DefaultEditorListener(controller, session, editor, new DefaultEditorListener.Listener() {
                    @Override
                    public void saved() {
                        //
                    }
                }));
        final SessionBackgroundAction<Transfer> action = new TerminalBackgroundAction<>(controller, session, worker);
        try {
            this.execute(action);
        }
        catch(TerminalBackgroundException e) {
            return Exit.failure;
        }
        Uninterruptibles.awaitUninterruptibly(lock);
        return Exit.success;
    }

    protected void disconnect(final SessionPool session) {
        if(session == SessionPool.DISCONNECTED) {
            return;
        }
        try {
            this.execute(new DisconnectBackgroundAction(controller, session) {
                @Override
                public void message(final String message) {
                    // No output
                }
            });
        }
        catch(TerminalBackgroundException e) {
            // Ignore failure
        }
    }

    protected <T> T execute(final SessionBackgroundAction<T> action) throws TerminalBackgroundException {
        try {
            final T result = controller.background(action).get();
            if(action.hasFailed()) {
                throw new TerminalBackgroundException(action.getFailure());
            }
            return result;
        }
        catch(InterruptedException e) {
            final TerminalBackgroundException error = new TerminalBackgroundException(e);
            alert.print(error);
            throw error;
        }
        catch(ExecutionException e) {
            final TerminalBackgroundException error = new TerminalBackgroundException(e.getCause());
            alert.print(error);
            throw error;
        }
    }

    public static final class TerminalBackgroundException extends BackgroundException {
        public TerminalBackgroundException(final Throwable cause) {
            super(cause);
        }
    }

    protected enum Exit {
        success,
        failure
    }
}
