package ch.cyberduck.core;

import java.nio.charset.Charset;
import java.util.List;

public class DefaultCharsetProvider implements CharsetProvider {
    @Override
    public String[] availableCharsets() {
        List<String> charsets = new Collection<String>();
        for(Charset charset : Charset.availableCharsets().values()) {
            final String name = charset.displayName();
            if(!(name.startsWith("IBM") || ((name.startsWith("x-") && !name.startsWith("x-Mac"))))) {
                charsets.add(name);
            }
        }
        return charsets.toArray(new String[charsets.size()]);
    }
}
