require "formula"

class Duck < Formula
  homepage "https://duck.sh/"
  # Reference to the CLI version of Cyberduck
  url "https://update.cyberduck.io/cli/duck-${VERSION}.${REVISION}.tar.gz"
  version "${VERSION}.${REVISION}"
  sha1 "${SHA1}"

  def install
    # Because compiling would need a JDK and xcodebuild we just use the pre-compiled binary.
    libexec.install Dir['*']
    bin.install_symlink "#{libexec}/Contents/MacOS/duck" => "duck"
  end

  test do
    system "#{bin}/duck", "-version"
  end

  def caveats;
    <<-EOS.undent
    For more information refer to
      https://trac.cyberduck.io/wiki/help/en/howto/cli
    EOS
  end
end
