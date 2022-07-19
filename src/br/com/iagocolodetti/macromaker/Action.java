package br.com.iagocolodetti.macromaker;

/**
 *
 * @author iagocolodetti
 */
public enum Action {
    
    PRESS(1), RELEASE(2);
    
    private final int action;
    
    Action(int action){
        this.action = action;
    }
    
    public int getAction(){
        return action;
    } 
}
