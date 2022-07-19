package br.com.iagocolodetti.macromaker;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author iagocolodetti
 */
public class Script implements Serializable {
    
    private int startKey;
    private int stopKey;
    private List<Macro> macroList;
    private boolean loop;

    public Script() {
        macroList = new ArrayList<>();
    }

    public Script(int startKey, int stopKey, List<Macro> macroList, boolean loop) {
        this.startKey = startKey;
        this.stopKey = stopKey;
        this.macroList = macroList;
        this.loop = loop;
    }
    
    public Script(String path) throws IOException, ClassNotFoundException {
        FileInputStream fis = null;
        ObjectInputStream ois = null;
        try {
            fis = new FileInputStream(path);
            ois = new ObjectInputStream(fis);
            Script script = (Script) ois.readObject();
            this.startKey = script.getStartKey();
            this.stopKey = script.getStopKey();
            this.macroList = script.getMacroList();
            this.loop = script.isLoop();
        } finally {
            if (ois != null) {
                ois.close();
            }
            if (fis != null) {
                fis.close();
            }
        }
    }

    public int getStartKey() {
        return startKey;
    }

    public void setStartKey(int startKey) {
        this.startKey = startKey;
    }

    public int getStopKey() {
        return stopKey;
    }

    public void setStopKey(int stopKey) {
        this.stopKey = stopKey;
    }

    public List<Macro> getMacroList() {
        return macroList;
    }

    public void setMacroList(List<Macro> macroList) {
        this.macroList = macroList;
    }
    
    public void addMacroList(Macro macro) {
        macroList.add(macro);
    }

    public boolean isLoop() {
        return loop;
    }

    public void setLoop(boolean loop) {
        this.loop = loop;
    }
    
    public void toFile(String path) throws IOException, NullPointerException {
        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        try {
            fos = new FileOutputStream(path);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(new Script(getStartKey(), getStopKey(), getMacroList(), isLoop()));
        } finally {
            if (oos != null) {
                oos.close();
            }
            if (fos != null) {
                fos.close();
            }
        }
    }
    
    public void generateAhkScript(String path) throws IOException {
        String startKeyName = Keys.getKeyboardKeyName(getStartKey());
        String stopKeyName = Keys.getKeyboardKeyName(getStopKey());
        PrintWriter writer = new PrintWriter(path, "UTF-8");
        writer.print(
            "#NoEnv  ; Recommended for performance and compatibility with future AutoHotkey releases."
            + "\n; #Warn  ; Enable warnings to assist with detecting common errors."
            + "\nSendMode Input  ; Recommended for new scripts due to its superior speed and reliability."
            + "\nSetWorkingDir %A_ScriptDir%  ; Ensures a consistent starting directory."
            + "\n#SingleInstance force"
            + "\n\nmacro := ["
        );
        String _macro = "";
        for (Macro macro: getMacroList()) {
            _macro += _macro.isEmpty() ? "" : ",";
            _macro += "[" + generateSend(macro) + "," + macro.getNext() + "]";
        }
        writer.println(_macro + "]");
        if (getStartKey() == getStopKey()) {
            writer.println(
                "\n#MaxThreadsPerHotkey 2"
                + "\n\nrunning := 0"
                + "\n\n" + startKeyName + "::"
                + "\nif (running == 0)"
                + "\n{"
                + "\n\trunning := 1"
                + (isLoop() ? "\n\tLoop\n\t{\n\t\tMacro()\n\t}" : "\n\tMacro()")
                + "\n}"
                + "\nelse"
                + "\n{"
                + "\n\tReload"
                + "\n}"
                + "\nReturn"
            );
        } else {
            writer.println(
                "\n" + startKeyName + "::"
                + (isLoop() ? "\nLoop\n{\n\tMacro()\n}\nReturn" : "Macro()")
                + "\n\n" + stopKeyName + "::Reload"
            );
        }
        writer.println(
            "\nMacro()"
            + "\n{"
            + "\n\tglobal macro"
            + "\n\tLoop, % macro.Length()"
            + "\n\t{"
            + "\n\t\tSend % macro[A_Index,1]"
            + "\n\t\tSleep, macro[A_Index,2]"
            + "\n\t}"
            + (getStartKey() == getStopKey() && !isLoop() ? "\n\tglobal running := 0" : "")
            + "\n\tReturn"
            + "\n}"
        );
        writer.close();
    }
    
    private String generateSend(Macro macro) {
        String keyName = "";
        if (macro.getHardware().name().equals("KEYBOARD")) {
            keyName = Keys.getKeyboardKeyName(macro.getKey());
        } else if (macro.getHardware().name().equals("MOUSE")) {
            keyName = Keys.getMouseKeyName(macro.getKey());
        }
        String send = "";
        if (macro.getAction().name().equals("PRESS")) {
            send = "{" + keyName + " down}";
        } else if (macro.getAction().name().equals("RELEASE")) {
            send = "{" + keyName + " up}";
        }
        return "\"" + send + "\"";
    }
}
