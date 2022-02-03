// 
// Copyright (c) 2010-2015 Yves Langisch. All rights reserved.
// http://cyberduck.ch/
// 
// This program is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2 of the License, or
// (at your option) any later version.
// 
// This program is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU General Public License for more details.
// 
// Bug fixes, suggestions and comments should be sent to:
// yves@cyberduck.ch
// 

using ch.cyberduck.cli;
using org.apache.commons.cli;
using Console = System.Console;

namespace Ch.Cyberduck.Cli
{
    internal class WindowsTerminal : Terminal
    {
        public WindowsTerminal(TerminalPreferences defaults, Options options, CommandLine input)
            : base(defaults, options, input)
        {
        }

        private static void Main(string[] args)
        {
            // HACK Cyberduck.Cryptomator.dll includes cryptolib v2, which uses java ServiceLoader.
            // Without this hack the ServiceLoader is incapable of finding org.cryptomator.cryptolib.api.v1.CryptorProviderImpl,
            // which results in non-working state of ch.cyberduck.core.cryptomator.CryptoVault.
            // This is a transient dependency coming from Cyberduck.Cryptomator through Cyberduck.Cli,
            // which isn't used in duck. Thus crazy stuff happens, and we have to force-load Cyberduck.Cryptomator here.
            // ref https://github.com/iterate-ch/cyberduck/issues/12812
            _ = typeof(org.cryptomator.cryptolib.api.CryptorProvider);

            // set UTF-8 encoding, tested in mintty (cygwin, babun) and cmd.exe
            java.lang.System.setProperty("file.encoding", "UTF-8");
            Console.OutputEncoding = System.Text.Encoding.UTF8;

            var preferences = new WindowsTerminalPreferences();
            open(args, preferences); // This exits the application, nothing beyond will run.
        }
    }
}
