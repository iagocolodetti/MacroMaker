package br.com.iagocolodetti.macromaker;

/**
 *
 * @author iagocolodetti
 */
public class KeyboardKey {
    
    private final int key;
        private final String name;
        
        public KeyboardKey(int key) {
            this.key = key;
            this.name = "";
        }

        public KeyboardKey(int key, String name) {
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
            if (!(object instanceof KeyboardKey)) {
                return false;
            }
            KeyboardKey keyboardKey = (KeyboardKey) object;
            return (this.key == keyboardKey.getKey());
        }
}
