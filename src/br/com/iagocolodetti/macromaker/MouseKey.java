package br.com.iagocolodetti.macromaker;

/**
 *
 * @author iagocolodetti
 */
public class MouseKey {
    
    private final int key;
    private final String name;

    public MouseKey(int key) {
        this.key = key;
        this.name = "";
    }

    public MouseKey(int key, String name) {
        this.key = key;
        this.name = name;
    }

    public int getKey() {
        return key;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object object) {
        if (!(object instanceof MouseKey)) {
            return false;
        }
        MouseKey mouseKey = (MouseKey) object;
        return (this.key == mouseKey.getKey());
    }
}
