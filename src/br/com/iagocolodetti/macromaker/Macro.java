package br.com.iagocolodetti.macromaker;

import java.io.Serializable;

/**
 *
 * @author iagocolodetti
 */
public class Macro implements Serializable {
    
    private Hardware hardware;
    private int key;
    private Action action;
    private Long next;
    
    public Macro() {
    }

    public Macro(Hardware hardware, int key, Action action, Long next) {
        this.hardware = hardware;
        this.key = key;
        this.action = action;
        this.next = next;
    }

    public Hardware getHardware() {
        return hardware;
    }

    public void setHardware(Hardware hardware) {
        this.hardware = hardware;
    }

    public int getKey() {
        return key;
    }

    public void setKey(int key) {
        this.key = key;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public Long getNext() {
        return next;
    }

    public void setNext(Long next) {
        this.next = next;
    }
}
