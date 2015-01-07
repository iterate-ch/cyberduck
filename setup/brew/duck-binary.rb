class Duck < Formula
  homepage "https://duck.sh/"
  url "${ARCHIVE}"
  version "${VERSION}.${REVISION}"
  sha1 "${ARCHIVE.SHA1}"

  def install
    # Because compiling would need a JDK and xcodebuild we just use the pre-compiled binary.
    libexec.install Dir["*"]
    bin.install_symlink "#{libexec}/Contents/MacOS/duck" => "duck"
  end

  test do
    system "#{bin}/duck", "-version"
  end
end
