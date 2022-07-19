package br.com.iagocolodetti.macromaker;

/**
 *
 * @author iagocolodetti
 */
public enum Hardware {
    
    KEYBOARD(1), MOUSE(2);
    
    private final int hardware;
    
    Hardware(int hardware){
        this.hardware = hardware;
    }
    
    public int getHardware(){
        return hardware;
    } 
}
