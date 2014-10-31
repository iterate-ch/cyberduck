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
import ch.cyberduck.core.transfer.DownloadTransfer;
import ch.cyberduck.core.transfer.Transfer;
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
import java.io.PrintStream;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @version $Id$
 */
public class Terminal {
    private static final Logger log = Logger.getLogger(Terminal.class);

    static {
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(final Thread t, final Throwable e) {
                // Swallow the exception
                log.error(String.format("Thread %s has thrown uncaught exception: %s",
                        t.getName(), e.getMessage()), e);
            }
        });
    }

    static {
        PreferencesFactory.set(new TerminalPreferences());
        ProtocolFactory.register();
    }

    private final DefaultPathKindDetector detector
            = new DefaultPathKindDetector();

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
        if(input.hasOption("help")) {
            TerminalHelpPrinter.help(options);
            return Exit.success;
        }
        if(input.hasOption("version")) {
            final PrintStream console = System.out;
            console.printf("%s %s%n",
                    Preferences.instance().getProperty("application.name"),
                    Preferences.instance().getProperty("application.version"));
            return Exit.success;
        }
        final List arguments = input.getArgList();
        if(arguments.size() == 0 || arguments.size() > 2) {
            TerminalHelpPrinter.help(options);
            return Exit.failure;
        }
        final String input = arguments.get(0).toString();
        final Host host = HostParser.parse(input);
        if(input.indexOf("://", 0) != -1) {
            final Protocol protocol = ProtocolFactory.forName(input.substring(0, input.indexOf("://", 0)));
            if(null == protocol) {
                TerminalHelpPrinter.help(options);
                return Exit.failure;
            }
            host.setProtocol(protocol);
            host.setPort(protocol.getDefaultPort());
        }
        if(StringUtils.isBlank(host.getHostname())) {
            TerminalHelpPrinter.help(options);
            return Exit.success;
        }
        if(StringUtils.isBlank(host.getDefaultPath())) {
            TerminalHelpPrinter.help(options);
            return Exit.success;
        }
        if(this.input.hasOption("username")) {
            host.getCredentials().setUsername(this.input.getOptionValue("username"));
        }
        if(this.input.hasOption("password")) {
            host.getCredentials().setPassword(this.input.getOptionValue("password"));
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
//                new CertificateStoreX509TrustManager(),
//                new CertificateStoreX509KeyManager());
        final TerminalProgressListener listener = new TerminalProgressListener();
        try {
            final ConnectionService connect = new LoginConnectionService(
                    new TerminalLoginCallback(), new TerminalHostKeyVerifier(), PasswordStoreFactory.get(),
                    listener, this.input.hasOption("verbose") ? new TerminalTranscriptListener() : new DisabledTranscriptListener());
            if(!connect.check(session, Cache.<Path>empty())) {
                throw new ConnectionCanceledException();
            }
            if(this.input.hasOption("edit")) {
                final TerminalController controller = new TerminalController();
                final Editor editor = EditorFactory.instance().create(controller, session, remote);
                editor.open();
                final CountDownLatch lock = new CountDownLatch(1);
                Runtime.getRuntime().addShutdownHook(new Thread() {
                    public void run() {
                        lock.countDown();
                        editor.delete();
                    }
                });
                try {
                    lock.await();
                }
                catch(InterruptedException e) {
                    return Exit.failure;
                }
                return Exit.success;
            }
            else {
                final Transfer.Type type;
                if(this.input.hasOption("download")) {
                    type = Transfer.Type.download;
                }
                else if(this.input.hasOption("upload")) {
                    type = Transfer.Type.upload;
                }
                else {
                    if(arguments.size() == 2) {
                        type = Transfer.Type.upload;
                    }
                    else {
                        type = Transfer.Type.download;
                    }
                }
                final Transfer transfer;
                final Local local;
                switch(type) {
                    case download:
                        if(arguments.size() == 1) {
                            local = LocalFactory.get(System.getProperty("user.dir"), remote.getName());
                        }
                        else if(arguments.size() == 2) {
                            if(LocalFactory.get(arguments.get(1).toString()).isDirectory()) {
                                local = LocalFactory.get(arguments.get(1).toString(), remote.getName());
                            }
                            else {
                                local = LocalFactory.get(arguments.get(1).toString());
                            }
                        }
                        else {
                            return Exit.failure;
                        }
                        transfer = new DownloadTransfer(host, Arrays.asList(new TransferItem(remote, local)));
                        break;

                    case upload:
                        local = LocalFactory.get(arguments.get(1).toString());
                        transfer = new UploadTransfer(host, Arrays.asList(new TransferItem(remote, local)));
                        break;
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
}
