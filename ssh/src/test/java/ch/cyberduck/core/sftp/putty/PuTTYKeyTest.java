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

    final static String ppk8192 = "PuTTY-User-Key-File-2: ssh-rsa\n" +
            "Encryption: none\n" +
            "Comment: imported-openssh-key\n" +
            "Public-Lines: 22\n" +
            "AAAAB3NzaC1yc2EAAAADAQABAAAEAQCcasi2SDVGvty6az32C3Uc3F4d8icjefnN\n" +
            "YCaDnBIRQjczX118dT/nG2rEMygR/cgCxmZgcySC7vo5KUNjJhxCMHa5u4H0CVdy\n" +
            "Raey2AOZBfLECjzuXSaakeMCIqyT6IywUBEFnkN6aUesyQtUUf1hR5iWHwPUmJPO\n" +
            "uYLlE4uYnK5hkeH8fSEbYVPcPiBnrHtRk+zh9MF0RR6tK0Gcms5eLfF2V2MNytvU\n" +
            "FnAySqX8mYISeJrg7v41PxtoEsAhGE88h4XAYX57uB4ewwTWQOlbBVgAutLybyLG\n" +
            "rxbw+cDuC3ZOuxU78u5PykcS/mkE2wu1jUtdnCzAmNN8XobAft0wggiEZUBc+t9D\n" +
            "2NmezZFU62SEkjxOWX/idDQrCQ8au8RQZhIgLYusGXDeeYFoPDk/4ObBxz3YkuTu\n" +
            "UqzVTYwoUslTe8cz5J+hDGPeTudkt1K4uXa+3weXrzj0BnSYvGb01bfoam8lShdl\n" +
            "MBg5hmow0ZjE6AvJgdttu+9SKvIp+jGQ2v2fv/m/LmGBKgZ5yslGJb6hhNf7MA5S\n" +
            "ewgHuAk8kfZ9yZIa3UcQDim8yxOkB/Y3885MFpdZqg3XNPCNo0s1SimGGRbngWwg\n" +
            "AxhKT24OzQ+WZn+rU7mlXHT4RehrYNKNukZlwqnSksg+TJ1ZGoj8mfUbAHmz0UnB\n" +
            "DQ7dpNP1DhAKxiFjgHfkDfmF4Bic7I1eHSesigCKImH7Zoomp1NcH0bub3h+Owyp\n" +
            "2fk5evgMBtuGvGGFuCzgyZeeiX6hzOgKyaqCML88OgNSjSMFkdiBYd0rwufimkID\n" +
            "v+vH1uIEcVZ69sn8xg0Vh7U/0aB2mai0EYcDuTa78gqkeSGp8AS+IgahgdwV/HQX\n" +
            "aLC/QFRgFb/NX2YmzKsVYWdObBamkbaJAOfrXb5vEuAyU2aRQouqKH4tYDNpkBYg\n" +
            "8KCq9A/8z8sS1Gwe3UHU9gZOEuTAI7JQQCN7E3U3JuuCFks2jAoh7WE3KxqEu9Lq\n" +
            "sMJn9YRobGyPPMMcQJSAqMUpwEyup8ovI/3v5NRvw+ZSiM4wHyYqzODJu/U6H5Cj\n" +
            "wq+MFCg4JcalRA/qKG4P9QVD9MfyqcX/AYWhdYj18BqstwUVtonhT0kMkKBx9ggU\n" +
            "g/TvVKePf/wX0glqXXw59I1EIzCnxL8QWMkULDkk5GvzSrGFpR04IdOzsz5DMdL3\n" +
            "p8bXOHK+04Rd/VG8w/f7eLfYid875B7m+kG9TKQzAT3lc8cmJ98gRzCG+pTIpzVB\n" +
            "QM2nj4f8DenS1uAO23cXICR9Zyo98/dCv0xYc7g0Gp5HxppRuNLga9bBSg5dferT\n" +
            "QvmP/MTgeNxiKepKFLakVT0MiM6QUlGfV35F6vDL1oQnQlp4OD7H\n" +
            "Private-Lines: 54\n" +
            "AAAEAEM55e/qEvPH/kgk5WmFPR1dXRoTxFyMBSAOzh7MijtesSjkOOLP5donP3j5\n" +
            "36Pz5e3DZabYdf3MRkEhCfRoIccU20IyY8UF6s6TP2MvUkSHePJm0A9Ge9v9DYsS\n" +
            "agfb7/OrRdWbUrce3o5Vjgf8gSE5S0xiIhxSQ1ybALYB84Jw/MW0lGMXSI5jA07q\n" +
            "aLUGPa4vHKV0s1yMhIW6zKVJJ570sg3BuzHnWRnLVwdWbAan126m5TH9pcYuzFGr\n" +
            "lWXj89I5EPRBMsJrvI5OFRscpO7Y2hzeLuHBgDnScNK7FP96b6ug3px4aZJjhq6U\n" +
            "J4DNwDeUdarS/6z7QhH28oVzQQ+jI5P7jHEp5aFcZxPImEjeLsKHs2GdN8iVVwKU\n" +
            "DyjXQKWpaOrpiFk8SfVkVYj+MUDSIXtxbZRSdhAz+lJm1PFTu3GlBlW4Uh8+mwGl\n" +
            "+e+glu4L0AxzAOlhhuHikGRAvSNHY5aBgCmPsYRs6kx3B9bZjoY6kS5XIH8GQfKX\n" +
            "wKLoBDuU02LAeM+BWKjR7hyUWnNKr6bt2IH+AnnSpP3kTBv7Q+yGIMRpDCzLWYbp\n" +
            "5RQf0+PyZlzvbLc9zlsLRsQpRZ6utDANQnnXdyg/DEaL4up7mdJzVTXGc0it9xvp\n" +
            "t93GrFf7klwUETcOnP+hoBL2w5+FcAHd73CoZ8GQIi6CtBJi/85EQ3IfyEXBF5l/\n" +
            "NVtZt14uS+u4XNQFKiMKQnRyZ8I4iz/Ybd8FLvtmiL6kI6Poe92FRFRwLSpqZrYi\n" +
            "WLcuVkFy7wzPOvS+gTbSFTP0xYIidqmjWBrabjxM1a2XUglcFL1lRGMt5pvHsDrz\n" +
            "dDmWZZp2d+Z2AZwL1GdUA8LPaNp+rbkQeeOlu2FGFgBvrt9cmRG7DJWLGf/wLuuC\n" +
            "hSGLOw6ZwVaPqNAuz7esnIUSeA0QdN1gssRhzGnuiDFoN9uirefhuZH6hfFNRRgo\n" +
            "Bm+6cpuzybZYsPE3/+PIEjyTAhJZtGUIuDiqwyLw4rsoK1hKMEkWfe42U6eqCFea\n" +
            "xPIvulUSkjcNa1Xg8SU6uNamlIz4RwAgS/cvmlmyZuzTiaYughl9xZ1/cHCCwFts\n" +
            "Aj8kBuj3s/4GgVx7Q4YV0hUJ9OKRahiTGrOg53Hm7akkMIljqUVM9NNjYBZR/l9N\n" +
            "Bk/KeLspTawHp3XaUdu8HVoDIJn4y2nEMcbhC30I2KEMpZR7cIrWO8lxKg6REJp8\n" +
            "FM+PpkR8VS9nPuU7IFCnxdnlH3XUGsR7tIOhpxhNujxOEH686mgCigR8m1GVD69W\n" +
            "5vE+mDmPGaZiPuNUIu7pCVA7nihPeH+Hyn9L8jJQkJXrwm4Y2bo6L8hT0Wm2o1C6\n" +
            "WoDadMMrioP9hWwacXmfWp48MCEAAAIBAM4gEFhRnSmxl7CMXOI7PWRtp7T2spp4\n" +
            "lPSJIlo7+DE6B+8AGXskGAnJOc8KBNQominGFeoQ6QaEOxajo3wsGgddHjlAAoFX\n" +
            "JorUImC8Tbb4XGXGRI88IF0jgOvvRpHeuL952IjLUzNnXaETwCwZw3Q0iMMPTAHi\n" +
            "VOQLJyFmwkfKVKpMN3/IsoHVCq3oMl2vg9/FYzO6U+s6g9PMC9eV7jx4fh+6hf/9\n" +
            "mkC4QS5cBUWqI1JnwzuOEBSSsDFhN765yB6jiROezMgnkJqZxb6W2zLtDBEYbkFS\n" +
            "keYRfbRRs3QCqxs30rxCFYuzg5kE9/7S0A5nUvI1pCgfR1Fri/ah/UTBi4c9hTPA\n" +
            "2UpyRzQ23NcruAacTIYJpLctFVU1rgabGFzDlWeEKuY2kR/egt9Wykr3ACk26NdD\n" +
            "IvmuxBJg46PH4M6vmthGE4ZRewmFzAFjbJC0LHKSgne1XWli58ELyiFd3pRp4sYF\n" +
            "Zi4iWqYv2KGcRNxtgDoGstD1aEdOpribKcDdWIAba/zuTR9T36+L5gSmGf30VyX9\n" +
            "ZbdG+Up96p913WktXo4Li+C2k70Lu49w4xW9CIO4pCOEe5wzp3MSbonkKdg//u93\n" +
            "hEtYPUaBU9UYUnAWLfu0VKh2TuDsLbN7gEziI5vPkRyyisT7w7s1VSMwpdhtRtZz\n" +
            "aaPsOaGBwXuXAAACAQDCQ6tPD3Dk2H/Q2oychhoN2NJ/K7NP0On+doZ8ACAhciHW\n" +
            "KzbvsmVps3yZfhRRBa23c3oyeeKYFRsKW/b4a8z8QVvI8rmgoAQsw6R/uHdLvmiI\n" +
            "1i8DiIYwr9SI/7e3O9Up5l7G5rzAhp3w2QvWDmC7h48R1gj1P0jbye6EDvsis14v\n" +
            "s34VoKBJyr9NdlOwXtTRdYeRjJpYYVuSzZuZNvihyuJpz7Zd81L6imstcNfC3Tu7\n" +
            "FVDEg9ER0VXkUrh2IHFZ+je6cTZwdoj/ynetti0u41KPevQr3lIQbhQvkXuTjkwE\n" +
            "zpMPdU9PiMrTURh+C7aFCzH6z6/my6XjvJOZLbvLRGEhHMTDPFCsmPmlYGSpbryx\n" +
            "T626I5rtcmFnCEJ2jv2mvTqV79i0OsFUHyi61krV07HO9C7+6Bm8r7zxGVNlFMjX\n" +
            "I+Gs4XF4fkH0b8dvudRpNVQ5+ze3scBL3gCJNGEhmFHmKdosQ2eFwJi17Y6Cx2Tp\n" +
            "Epj1gMDlsBVnEVnV1Mz9tnpZ3OuTaCyAyrbA0XrmfgmFaqIOdcqXTHiE6aaHRDlw\n" +
            "mkVbYyel2WKmtRwi9k9Fy0CdJdA6ATY2QBK/MaayTjP+d0By/4sGPsfYn8Cu5I8l\n" +
            "cGvvQnuPwnnT2kF9qONLcY5otChtJprFga5evBxU6HX+J+TKy75JabcFv1V8UQAA\n" +
            "AgBM3f5IfW1XTRP4EGO18lt1DwdRhy84UdsQaWm/pnAhojOqNMAB2R5OL3bJ+nit\n" +
            "9792p54MgFuX94c8RL34fryeD/zWudwxVo+upcs7rzW+1xG6uYa581qVhfJEOHA8\n" +
            "a4zk7PzrHKW8cmOK5HYBDSXUkGtFRxkqirJeOSGAx6YXhpVuvZfPACYPrl8wjeg7\n" +
            "JWJ2O2rDes2pauK5aIGvkc6CarrPTTWzDbw9M1EzmVzcr/R2GTdDBPD4sQ1AAHto\n" +
            "Io4cOGfdtw0pFrmi5Qu+TSgt7xY4dK+IXTHtUz4FY1OpPNEWBhdbYNGVWDWwQj6z\n" +
            "LibcD5tpfVKzNNczqN5RG9jVu4Jh0vbRaAUW6E4BaWZZ2qh/m5DxAjeewjEyWCFK\n" +
            "2yqD8puzikGTquWBf87azdPbYK0qo5tnvBFhLOee2+mhC+++yWIZT7z/XIWCM2i6\n" +
            "K4jy2qInjrHBamXtYOep776OTY3fvgoYqYBHrT2+tbHIHhBxcHdkxS8qwkfzkg40\n" +
            "5WYmVed7rWvG6xu6XJIWnn7HXVGKogUdPOPyv+qHz+TcqVCwVRVEa0eTX9gaBztr\n" +
            "ttGrDrR3676T2xwsWjeZlSpL9oF1ZH8faxZPUHoT8z9Zhgl0dbOt/pPXZiTRM8VS\n" +
            "erB/l04ZPmqU7zzGXFgpRGaXsOEO9TRpiw3+sragQN/ixg==\n" +
            "Private-MAC: 5405ff514dd17380c68d08f371a9497e827a1054\n";

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
    public void test8192() throws Exception {
        PuTTYKeyFile key = new PuTTYKeyFile();
        key.init(new StringReader(ppk8192));
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
