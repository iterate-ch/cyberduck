package ch.cyberduck.core.sftp.putty;

import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.io.StringReader;

import net.schmizz.sshj.userauth.keyprovider.PuTTYKeyFile;
import net.schmizz.sshj.userauth.password.PasswordFinder;
import net.schmizz.sshj.userauth.password.Resource;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class PuTTYKeyTest {

    final static String ppk2048 = "PuTTY-User-Key-File-2: ssh-rsa\n" +
            "Encryption: none\n" +
            "Comment: \n" +
            "Public-Lines: 6\n" +
            "AAAAB3NzaC1yc2EAAAADAQABAAABAQC0ITaAE49ievGiREUNxjccle9zJEZkNdkE\n" +
            "2Nnkl0zxlGVwShwRIjtarM0uKiUQAFD1OhkdSA/1FhZKRumIUWD+2Fkj23EEvox0\n" +
            "bTZyPzvQJERchdpvZKJyfxZgnPL6ygY6UQj8oNBhxnuwm9lL11cSJWS+4qJigT7g\n" +
            "59eEhDyBaZ/HoijtDGvfPmJlhdNcq0MlC6ALy7XXpOd4RS2oo8m/TRpLFvoSnAQ/\n" +
            "sr1wv9u7sTZh5C5RAjPN8LWBu7GuodqZ8PhlyT3oT+qbjpA/2e18vQta1ELROBKk\n" +
            "qsLnsN4fjH69eYSZ8h5R07tfvQxTKRwCWKziqMjP4dF9Lz+gVcy1\n" +
            "Private-Lines: 14\n" +
            "AAABAQCqLWasAc7JH5YB07XZmZafrxeWFINcUXNCnQzeZgMPiT98osd5eHnS5MbE\n" +
            "ApUZVPMne0gW3eoVhlRwwCYJ37hfjE5LDhrsfIl9xWBW916u+lSLhPolm1HOEjs1\n" +
            "85GrVgokNkLjSZsVhMt+wv68JCnivuk7XipEHg8ltGNskvIG4AjW97uBqewyvyeG\n" +
            "wsPYyBtiifRxJQ1th5hlLPh+jOBsyz91uC+ZeEW2pil2ftI7XrbKVbA95SRh4W8R\n" +
            "tNBjqUpI+M2mJQ7nwh2gxd/GdNxKyvTUyCAfJo+DzAG+XRGW1Px7ibFw38jiT+CP\n" +
            "tKTjCZRFMPUvmoH3MR1hzjqjqpuBAAAAgQDj4/2h35V/aAEYvQfkwF4k6rWOY15+\n" +
            "gEV+gbfjWlYGxkH6U0AvMQv3c6EAvJNQsKip3/fqOHgdd37CcGVW+NQTucHlxz8K\n" +
            "e4cYs0Dy8g4gcNhy2M99MOy9TuMsC0/mrTQUP0Vewwo7FASWF23sbhZsBM//BC8W\n" +
            "m3LM843RwCbvXQAAAIEAylkY1TU721y22mVA2C+o6ADs55ZtMGJqjI0DjOiWCgxt\n" +
            "j6pXRmJQ05hFZy4pO4AOYMZ5IW7MdqmCu2+GVytA6PxA6C+OGYF0Eh1YJbIh/Qrv\n" +
            "07NMrYQVwQY2+FAJpLAwWJAjlrRRlANgGBHkbppf8RuQFB/euToHCZ6R6goJdTkA\n" +
            "AACAOn0n+B0Lums/2FFmBRak2niTRONt6GMWhtK4e42MnKN3VxMGshAB4SdDAQOY\n" +
            "4Qk66RYmniuhaC3sLwxtXsEKoVnMp9EXVoTPEd+BQCVBOJzZDVtAaejO9bqrvRL8\n" +
            "kmknsX54RBXxrOVvNTofHLiRojncZnRSrM3BR+Xjo0b0+mE=\n" +
            "Private-MAC: c9c10df8a1e3546eedcc08608efd5338de5df723\n";

    final static String ppk4096 = "PuTTY-User-Key-File-2: ssh-rsa\n" +
            "Encryption: none\n" +
            "Comment: \n" +
            "Public-Lines: 12\n" +
            "AAAAB3NzaC1yc2EAAAADAQABAAACAQDMlwE5YNobWP8R47Ms41hnQnATKfJblTxW\n" +
            "k/6nf+5IOknCNFBMQUOnToCmvcVRPzepr3nRFGm/gvo5SjsKdE4b0b9eT7xOGAYM\n" +
            "9y18qO3flt6hARasK8NoivbT8Cm1f0Zj02eLBaiFpFYZOuBZdpluKiYH0wHuSPeq\n" +
            "K3Q3/arsnQj1C0X+h5f4Nm0IYIHRkNsnvZrJf+MlHtcwS+BPXpAK9tkICcP1MJ2x\n" +
            "UvTKh+TgWQJQ8EUq0OUkTBUBdmG+J6O+sdB0V6r06IpcXZUNed02F+bzP/DVUE1b\n" +
            "mJZTx0ynZhKyP6NeXlwuZ3fUZhiwwqMRvCQuq1p8/itG9Vz+eY652KIIrCoVsyH1\n" +
            "gIIRert3ADX5UySMdPcgBoDWYlfyj/fS+dR2o1lIwQXLcl9uL8ZELteSq/sLmapA\n" +
            "YJbIis3r9aJnNSVaQSKn8p20tCWNnazAcSK0RTN2h5r0/r7WfvXafFIMt6VZUxPn\n" +
            "dpFCJtxhgCrszosy87eL92NCoZvdQMOddpV3op04CDccZy0LEAt7o9dXoNNaeYlx\n" +
            "czaUTV/JcuAnk9G0u3xUpTh0AOQauuxn8Dv6yyVLvXNJANp89zAhUukEwdhqOvXD\n" +
            "U5qLLgY0Kf3v+ySj6HUWNBoms6ijF1txT3RDmJdCiVfuZ1nic9tsyp0A77S1oEEQ\n" +
            "QD7Rgmi4rQ==\n" +
            "Private-Lines: 28\n" +
            "AAACABi+/xfonhkGt7t7NjXsvcmnoJTA0x6+u1ChkADEmZbE7hz+ZOQEVOGMvkTs\n" +
            "2UwNgHcW0X43oN7YQdniH6gRD02QHjyTGmy7vSeeUjMs37DWt9Dzp8FlfbpMbLSP\n" +
            "7QuV/HagoHqRUaPwj7V3iKFplf9cO8Ngg3BGBSbhIKqRFTaPfADfvzSdRAVy19dW\n" +
            "jP1DLy7sYSeUP25C/7ZIxzXycyvQVcoCHGCw47IKHa/NpiJ4wa32kfcu0ziDt1q4\n" +
            "7fOpKcYsDdG0tOnwoqOvchLyNY6Qb4/moQO8Nc8pcq1pgt0QnJxQ1Dra4P1/6F+Z\n" +
            "hc0DjePcROgcM9LAj42Cqh/hpiCfCiLJiDts+HhQppgA4fOMy5d/wrG0nuqKfhIv\n" +
            "BsX9nJDj4eHU4eNBAoraUfNLDIq0GHYDcm+jlhqO1SHxymjIhDqS6Cz/FWf07L1Q\n" +
            "5DQ/+xHysVHCcavQk4jA7JwbZrRWo5qyKrdLoWRPFUX5w5ocLnmj0Zx8VOl6a+8M\n" +
            "Q+ehLSZXFoCbao3nES/oEkKH0RFNQsDMJb0uiKQv4b/+6bywtYIFc0eqvEqd1GSF\n" +
            "x3exCdHNhLRycaCgGSh+IdPCRrMj0N7/9pGZmbjfcZ7uKlFwqETVmy1H67NTXUCW\n" +
            "NukVfsqTRewpqjFFeaxW5GEYwEeA34MbIChfdw4/KRr5XDhFAAABAQD3c9w0rWAQ\n" +
            "rjVF1WTeD89Mf+Fnf7NvRaHAaD1EJxfimgqCD5juCIa5WSplzEBpPSG/rpl3HYVz\n" +
            "CZ98rdJSS/bmJieojefvjlz1nuuPlApg2ctCfEZYOFnNP6yt0w88GLp3aMTfIsTf\n" +
            "Z893GZnMFzMMLItLcZBTSmQLiqpyU6lWE+Tr4QOcQeCF8XqHGrLWbwACuKocmCW/\n" +
            "4nI+6gZ4SfucLXKwFgcuhSaXo0XM6HiSgZHb5wEyjS2Boad6vX8t4YdjZbCcnGVm\n" +
            "9TEm0/ow41Cl44SJUU6pLlo4UnSmR7aLmTK4iEG3fIMdEmmy4VX3MJ8fqXuVJwLE\n" +
            "RLzqEjCgIcQzAAABAQDTqB/A3CyJfeHYFO7Et6edAOklejxqRW4UuuOu55v7FOj5\n" +
            "X/yW72rWbndcci+mDXQvDL6P9EG3vF1twPS0konHqVxqj6Jlp1AtUWND2FzVTypY\n" +
            "0X7z4Mif5V0p5bS5Qx6/pBg37XXbisSANSDxFVdH0/OSTYXi4EKmh0LjU5Ls0zIw\n" +
            "MB6TYetuR1hEcCxuVESnOMUgjXMsoIwGR/jeKynle45UwTqUv/oWRQvFeIi5wlwn\n" +
            "82GtUzLxhAo/BbXc3ODWjIGfKSxBJdsn0ZEXtPAk4CTqxM3VF4s3aOFAhHBDSyOv\n" +
            "nHvWXwVRwmhtyXKEkTfAO6K4ptcS57LTNT8ta6+fAAABAQC9dPiPexqC35vWtWQd\n" +
            "Zvm8DVCVscd7IPDn952FUsf2svoQ9MWodpD1craKGadSRsFCTVeYyHzS3Sg8HwKC\n" +
            "NNoanAxpY4IqEPfuaLZZuKQsj3PsVj5rXdSEbmwCR7EhI9oDUDNcSLufR5A5DMpz\n" +
            "wY4EJmg8uC2nO/O9Rzr516pIfDGsNwsdSKGWLlhgRzJxWl7M+cJjJfRlf6XruhLI\n" +
            "WDDIq/jMHb5cLNjXdWTt3jyRQkm3HI6r5C3vc4mdInBm3tNUE+KKBtChegpgDgqg\n" +
            "hZ41/hnd1e+3on3tvrE7arM3t4IHt7grwS/i1vdukV8ilYkTYHMG/Ls+6pUr+Swy\n" +
            "z15x\n" +
            "Private-MAC: a11331fa8b59cfb2be1c8e9f67ead34ac848d514\n";

    final static String ppk1024_passphrase = "PuTTY-User-Key-File-2: ssh-rsa\n" +
            "Encryption: aes256-cbc\n" +
            "Comment: rsa-key-20121215\n" +
            "Public-Lines: 4\n" +
            "AAAAB3NzaC1yc2EAAAABJQAAAIB7KdUyuvGb2ne9G9YDAjaYvX/Mq6Q6ppGjbEQo\n" +
            "bac66VUazxVpZsnAWikcdYAU7odkyt3jg7Nn1NgQS1a5mpXk/j77Ss5C9W4rymrU\n" +
            "p32cmbgB/KIV80DnOyZyOtDWDPM0M0RRXqQvAO6TsnmsNSnBa8puMLHqCtrhvvJD\n" +
            "KU+XEw==\n" +
            "Private-Lines: 8\n" +
            "4YMkPgLQJ9hOI1L1HsdOUnYi57tDy5h9DoPTHD55fhEYsn53h4WaHpxuZH8dTpbC\n" +
            "5TcV3vYTfhh+aFBY0p/FI8L1hKfABLRxhkqkkc7xMmOGlA6HejAc8oTA3VArgSeG\n" +
            "tRBuQRmBAC1Edtek/U+s8HzI2whzTw8tZoUUnT6844oc4tyCpWJUy5T8l+O3/03s\n" +
            "SceJ98DN2k+L358VY8AXgPxP6NJvHvIlwmIo+PtcMWsyZegfSHEnoXN2GN4N0ul6\n" +
            "298RzA9R+I3GSKKxsxUvWfOVibLq0dDM3+CTwcbmo4qvyM2xrRRLhObB2rVW07gL\n" +
            "7+FZpHxf44QoQQ8mVkDJNaT1faF+h/8tCp2j1Cj5yEPHMOHGTVMyaz7gqhoMw5RX\n" +
            "sfSP4ZaCGinLbouPrZN9Ue3ytwdEpmqU2MelmcZdcH6kWbLCqpWBswsxPfuhFdNt\n" +
            "oYhmT2+0DKBuBVCAM4qRdA==\n" +
            "Private-MAC: 40ccc8b9a7291ec64e5be0c99badbc8a012bf220\n";

    final static String ppkdsa_passphrase = "PuTTY-User-Key-File-2: ssh-dss\n" +
            "Encryption: aes256-cbc\n" +
            "Comment: dsa-key-20140507\n" +
            "Public-Lines: 10\n" +
            "AAAAB3NzaC1kc3MAAACBAN6eo/Yh8ih26sKRAHAta/UqKesrXRS83GN7YqAxQzsP\n" +
            "2tJ00UzOqZCdBoHIXLXC07QRJ9SkXOMnILw/KuaZ3paJ6ym92FzKi3BRfpzujIdo\n" +
            "qBAEGSGOWbz2oYPDDSi0bsL84P4O8WD7ZxKhgTb4JAxlVJiW20vPfZA8Ft6xKJyd\n" +
            "AAAAFQD1pnKWpSyHzi6RcVPn16FwmGIgmwAAAIEAiFPw87HVijatNOBeuxoU5PHH\n" +
            "80kMl0TtxoI7rhB8fKO9bu7wLcT79h6xYS4Np6nHv9ajWwwVSLh8NjKgMbCXCz2j\n" +
            "qD4ajvnusS7yz7TbTumeaGqFXEEzqzG4Xe6KXkv7kd7Yg+Dnw29zucgeAvPfuJFW\n" +
            "Gtr4CWPoHSBgpTeyemEAAACBAJYvGi5gIMJQQUhIErKbtZ64V2L0zZtYkzlms03R\n" +
            "cTBFN9++xV8zUvTPAAM8imsoxZ/5JNtNjJCAD+Ghrzyav24gxYG9v/YXtd2WsYa5\n" +
            "0E/5wxcPor82SAqU2fd3IEQ5y9KHamXBuX/5KFDOTMC6cnGsutFkeo5rXQ0fI55C\n" +
            "VSTq\n" +
            "Private-Lines: 1\n" +
            "nLEIBzB8WEaMMgDz5qwlqq7eBxLPIIi9uHyjMf7NOsQ=\n" +
            "Private-MAC: b200c6d801fc8dd8f84a14afc3b94d9f9bb2df90\n";

    @Test
    public void test2048() throws Exception {
        PuTTYKeyFile key = new PuTTYKeyFile();
        key.init(new StringReader(ppk2048));
        assertNotNull(key.getPrivate());
        assertNotNull(key.getPublic());
    }

    @Test
    public void test4096() throws Exception {
        PuTTYKeyFile key = new PuTTYKeyFile();
        key.init(new StringReader(ppk4096));
        assertNotNull(key.getPrivate());
        assertNotNull(key.getPublic());
    }

    @Test
    @Ignore
    public void testCorrectPassphraseRsa() throws Exception {
        PuTTYKeyFile key = new PuTTYKeyFile();
        key.init(new StringReader(ppk1024_passphrase), new PasswordFinder() {
            @Override
            public char[] reqPassword(Resource<?> resource) {
                // correct passphrase
                return "123456".toCharArray();
            }

            @Override
            public boolean shouldRetry(Resource<?> resource) {
                return false;
            }
        });
        assertNotNull(key.getPrivate());
        assertNotNull(key.getPublic());
    }

    @Test(expected = IOException.class)
    public void testWrongPassphraseRsa() throws Exception {
        PuTTYKeyFile key = new PuTTYKeyFile();
        key.init(new StringReader(ppk1024_passphrase), new PasswordFinder() {
            @Override
            public char[] reqPassword(Resource<?> resource) {
                // wrong passphrase
                return "egfsdgdfgsdfsdfasfs523534dgdsgdfa".toCharArray();
            }

            @Override
            public boolean shouldRetry(Resource<?> resource) {
                return false;
            }
        });
        assertNotNull(key.getPublic());
        assertNull(key.getPrivate());
    }

    @Test
    @Ignore
    public void testCorrectPassphraseDsa() throws Exception {
        PuTTYKeyFile key = new PuTTYKeyFile();
        key.init(new StringReader(ppkdsa_passphrase), new PasswordFinder() {
            @Override
            public char[] reqPassword(Resource<?> resource) {
                // correct passphrase
                return "secret".toCharArray();
            }

            @Override
            public boolean shouldRetry(Resource<?> resource) {
                return false;
            }
        });
        assertNotNull(key.getPrivate());
        assertNotNull(key.getPublic());
    }

    @Test(expected = IOException.class)
    public void testWrongPassphraseDsa() throws Exception {
        PuTTYKeyFile key = new PuTTYKeyFile();
        key.init(new StringReader(ppkdsa_passphrase), new PasswordFinder() {
            @Override
            public char[] reqPassword(Resource<?> resource) {
                // wrong passphrase
                return "egfsdgdfgsdfsdfasfs523534dgdsgdfa".toCharArray();
            }

            @Override
            public boolean shouldRetry(Resource<?> resource) {
                return false;
            }
        });
        assertNotNull(key.getPublic());
        assertNull(key.getPrivate());
    }
}
