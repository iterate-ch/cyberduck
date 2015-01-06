class Duck < Formula
  homepage "https://duck.sh/"
  url "${SOURCE}"
  head "https://svn.cyberduck.io/trunk/"

  depends_on :java => :build
  depends_on :xcode => :build
  depends_on "ant" => :build
  depends_on "openssl" => :recommended

  def install
    system "ant", "-Drevision=${REVISION}", "cli"
    libexec.install Dir['build/duck.bundle/*']
    bin.install_symlink "#{libexec}/Contents/MacOS/duck" => "duck"
  end

  test do
    system "#{bin}/duck", "-version"
  end
end
