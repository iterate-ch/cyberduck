package ch.cyberduck.core;

import java.util.ArrayList;

public class Collection extends ArrayList {

    public int indexOf(Object elem) {
        for (int i = 0; i < this.size(); i++) {
            if (this.get(i).equals(elem))
                return i;
        }
        return -1;
    }

    public int lastIndexOf(Object elem) {
        for (int i = this.size()-1; i >= 0; i--) {
            if (this.get(i).equals(elem))
                return i;
        }
        return -1;
    }

}
