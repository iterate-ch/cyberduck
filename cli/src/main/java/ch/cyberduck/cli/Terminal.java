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
import ch.cyberduck.core.azure.AzureProtocol;
import ch.cyberduck.core.b2.B2Protocol;
import ch.cyberduck.core.brick.BrickProtocol;
import ch.cyberduck.core.dav.DAVProtocol;
import ch.cyberduck.core.dav.DAVSSLProtocol;
import ch.cyberduck.core.dropbox.DropboxProtocol;
import ch.cyberduck.core.editor.DefaultEditorListener;
import ch.cyberduck.core.editor.Editor;
import ch.cyberduck.core.editor.EditorFactory;
import ch.cyberduck.core.exception.AccessDeniedException;
import ch.cyberduck.core.exception.BackgroundException;
import ch.cyberduck.core.exception.ConnectionCanceledException;
import ch.cyberduck.core.features.Vault;
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
import ch.cyberduck.core.local.TemporaryFileServiceFactory;
import ch.cyberduck.core.nextcloud.NextcloudProtocol;
import ch.cyberduck.core.nio.LocalProtocol;
import ch.cyberduck.core.onedrive.OneDriveProtocol;
import ch.cyberduck.core.onedrive.SharepointProtocol;
import ch.cyberduck.core.openstack.SwiftProtocol;
import ch.cyberduck.core.pool.SessionPool;
import ch.cyberduck.core.preferences.Preferences;
import ch.cyberduck.core.preferences.PreferencesFactory;
import ch.cyberduck.core.s3.S3Protocol;
import ch.cyberduck.core.sds.SDSProtocol;
import ch.cyberduck.core.sftp.SFTPProtocol;
import ch.cyberduck.core.spectra.SpectraProtocol;
import ch.cyberduck.core.ssl.CertificateStoreX509TrustManager;
import ch.cyberduck.core.ssl.DefaultTrustManagerHostnameCallback;
import ch.cyberduck.core.ssl.PreferencesX509KeyManager;
import ch.cyberduck.core.storegate.StoregateProtocol;
import ch.cyberduck.core.threading.DisconnectBackgroundAction;
import ch.cyberduck.core.threading.SessionBackgroundAction;
import ch.cyberduck.core.transfer.CopyTransfer;
import ch.cyberduck.core.transfer.DisabledTransferErrorCallback;
import ch.cyberduck.core.transfer.DisabledTransferPrompt;
import ch.cyberduck.core.transfer.Transfer;
import ch.cyberduck.core.transfer.TransferAction;
import ch.cyberduck.core.transfer.TransferItem;
import ch.cyberduck.core.transfer.TransferOptions;
import ch.cyberduck.core.transfer.TransferPrompt;
import ch.cyberduck.core.transfer.TransferSpeedometer;
import ch.cyberduck.core.vault.LoadingVaultLookupListener;
import ch.cyberduck.core.vault.VaultRegistryFactory;
import ch.cyberduck.core.worker.CreateDirectoryWorker;
import ch.cyberduck.core.worker.DeleteWorker;
import ch.cyberduck.core.worker.HomeFinderWorker;
import ch.cyberduck.core.worker.LoadVaultWorker;
import ch.cyberduck.core.worker.SessionListWorker;
import ch.cyberduck.core.worker.Worker;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;

import com.google.common.util.concurrent.Uninterruptibles;

public class Terminal {
    private static final Logger log = Logger.getLogger(Terminal.class);

    private final Preferences preferences;
    private final TerminalController controller;
    private final TerminalPromptReader reader;
    private final Cache<Path> cache;
    private final ProgressListener progress;
    private final TranscriptListener transcript;

    private final ProtocolFactory protocols = ProtocolFactory.get();
    private final CommandLine input;
    private final Options options;

    public Terminal(final TerminalPreferences defaults, final Options options, final CommandLine input) {
        this.preferences = defaults.withDefaults(input);
        this.protocols.register(
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
            new DropboxProtocol(),
            new DropboxProtocol(),
            new OneDriveProtocol(),
            new SharepointProtocol(),
            new LocalProtocol(),
            new SDSProtocol(),
            new StoregateProtocol(),
            new BrickProtocol(),
            new NextcloudProtocol()
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
            console.printf("%s%n", e.getMessage());
            System.exit(1);
        }
        catch(Throwable error) {
            error.printStackTrace(System.err);
            System.exit(1);
        }
        finally {
            // Clear temporary files
            TemporaryFileServiceFactory.get().shutdown();
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
        if(input.hasOption(TerminalOptionsBuilder.Params.profile.name())) {
            final String file = input.getOptionValue(TerminalOptionsBuilder.Params.profile.name());
            final Protocol profile;
            try {
                profile = ProfileReaderFactory.get().read(LocalFactory.get(file));
            }
            catch(AccessDeniedException e) {
                console.printf("%s%n", e.getDetail(false));
                return Exit.failure;
            }
            if(null != profile) {
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Register profile %s", profile));
                }
                protocols.register(profile);
            }
            else {
                protocols.loadDefaultProfiles();
            }
        }
        else {
            protocols.loadDefaultProfiles();
        }
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
        SessionPool source = SessionPool.DISCONNECTED;
        SessionPool destination = SessionPool.DISCONNECTED;
        try {
            final TerminalAction action = TerminalActionFinder.get(input);
            if(null == action) {
                return Exit.failure;
            }
            final String uri = input.getOptionValue(action.name());
            final Host host = new CommandLineUriParser(input, protocols).parse(uri);
            final LoginConnectionService connect = new LoginConnectionService(new TerminalLoginService(input
            ), new TerminalLoginCallback(reader), new TerminalHostKeyVerifier(reader), progress);
            source = SessionPoolFactory.create(connect, transcript, cache, host,
                new CertificateStoreX509TrustManager(new DisabledCertificateTrustCallback(), new DefaultTrustManagerHostnameCallback(host), new TerminalCertificateStore(reader)),
                new PreferencesX509KeyManager(host, new TerminalCertificateStore(reader)),
                VaultRegistryFactory.create(new TerminalPasswordCallback()));
            final Path remote;
            if(StringUtils.startsWith(new CommandLinePathParser(input).parse(uri).getAbsolute(), TildePathExpander.PREFIX)) {
                final Path home = this.execute(new TerminalBackgroundAction<Path>(controller, source, new HomeFinderWorker()));
                remote = new TildePathExpander(home).expand(new CommandLinePathParser(input).parse(uri));
            }
            else {
                remote = new CommandLinePathParser(input).parse(uri);
            }
            if(input.hasOption(TerminalOptionsBuilder.Params.vault.name())) {
                final Path vault;
                if(StringUtils.startsWith(input.getOptionValue(action.name()), TildePathExpander.PREFIX)) {
                    final Path home = this.execute(new TerminalBackgroundAction<Path>(controller, source, new HomeFinderWorker()));
                    vault = new TildePathExpander(home).expand(new Path(input.getOptionValue(action.name()), EnumSet.of(Path.Type.directory, Path.Type.vault)));
                }
                else {
                    vault = new Path(input.getOptionValue(TerminalOptionsBuilder.Params.vault.name()), EnumSet.of(Path.Type.directory, Path.Type.vault));
                }
                if(log.isDebugEnabled()) {
                    log.debug(String.format("Attempting to load vault from %s", vault));
                }
                final LoadVaultWorker worker = new LoadVaultWorker(new LoadingVaultLookupListener(source.getVault(),
                    PasswordStoreFactory.get(), new TerminalPasswordCallback()), vault);
                try {
                    this.execute(new TerminalBackgroundAction<Vault>(controller, source, worker));
                }
                catch(TerminalBackgroundException e) {
                    return Exit.failure;
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
            }
            switch(action) {
                case download:
                case upload:
                case synchronize:
                    return this.transfer(new TerminalTransferFactory().create(input, host, remote,
                        new ArrayList<TransferItem>(new SingleTransferItemFinder().find(input, action, remote))),
                        source, SessionPool.DISCONNECTED);
                case copy:
                    final Host target = new CommandLineUriParser(input).parse(input.getOptionValues(action.name())[1]);
                    destination = SessionPoolFactory.create(connect, transcript, cache, target,
                        new CertificateStoreX509TrustManager(new DisabledCertificateTrustCallback(), new DefaultTrustManagerHostnameCallback(target), new TerminalCertificateStore(reader)),
                        new PreferencesX509KeyManager(target, new TerminalCertificateStore(reader)),
                        VaultRegistryFactory.create(new TerminalPasswordCallback()));
                    return this.transfer(new CopyTransfer(
                            host, target, Collections.singletonMap(remote, new CommandLinePathParser(input).parse(input.getOptionValues(action.name())[1]))),
                        source, destination);
                default:
                    throw new BackgroundException(LocaleFactory.localizedString("Unknown"),
                        String.format("Unknown transfer type %s", action.name()));
            }
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
            this.disconnect(source);
            this.disconnect(destination);
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
            preferences.setProperty("queue.connections.limit",
                NumberUtils.toInt(input.getOptionValue(TerminalOptionsBuilder.Params.parallel.name()), 2));
        }
        preferences.setProperty("connection.login.keychain", !input.hasOption(TerminalOptionsBuilder.Params.nokeychain.name()));
    }

    protected Exit transfer(final Transfer transfer, final SessionPool source, final SessionPool destination) {
        // Transfer
        final TransferSpeedometer meter = new TransferSpeedometer(transfer);
        final TransferPrompt prompt;
        final Host host = transfer.getSource();
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
            source, destination,
            transfer.withCache(cache), new TransferOptions().reload(true), prompt, meter,
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
        final SessionListWorker worker = new SessionListWorker(cache, remote,
            new TerminalListProgressListener(reader, verbose));
        final SessionBackgroundAction<AttributedList<Path>> action = new TerminalBackgroundAction<AttributedList<Path>>(
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
        final SessionBackgroundAction<List<Path>> action = new TerminalBackgroundAction<List<Path>>(controller, session, worker);
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
        final SessionBackgroundAction<Path> action = new TerminalBackgroundAction<Path>(controller, session, worker);
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
        }, new DisabledTransferErrorCallback(), new DefaultEditorListener(controller, session, editor, new DefaultEditorListener.Listener() {
            @Override
            public void saved() {
                //
            }
        }));
        final SessionBackgroundAction<Transfer> action = new TerminalBackgroundAction<Transfer>(controller, session, worker);
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
                throw new TerminalBackgroundException();
            }
            return result;
        }
        catch(InterruptedException | ExecutionException e) {
            throw new TerminalBackgroundException(e);
        }
    }

    private final class TerminalBackgroundException extends BackgroundException {
        public TerminalBackgroundException() {
        }

        public TerminalBackgroundException(final Throwable cause) {
            super(cause);
        }
    }

    private enum Exit {
        success,
        failure
    }
}
