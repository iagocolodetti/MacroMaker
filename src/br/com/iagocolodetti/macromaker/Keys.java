package br.com.iagocolodetti.macromaker;

import com.github.kwhat.jnativehook.keyboard.NativeKeyEvent;
import com.github.kwhat.jnativehook.mouse.NativeMouseEvent;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author iagocolodetti
 */
public class Keys {
    
    private static final List<KeyboardKey> KEYBOARD_KEYS = Arrays.asList(
        new KeyboardKey(NativeKeyEvent.VC_F1, "F1"),
        new KeyboardKey(NativeKeyEvent.VC_F2, "F2"),
        new KeyboardKey(NativeKeyEvent.VC_F3, "F3"),
        new KeyboardKey(NativeKeyEvent.VC_F4, "F4"),
        new KeyboardKey(NativeKeyEvent.VC_F5, "F5"),
        new KeyboardKey(NativeKeyEvent.VC_F6, "F6"),
        new KeyboardKey(NativeKeyEvent.VC_F7, "F7"),
        new KeyboardKey(NativeKeyEvent.VC_F8, "F8"),
        new KeyboardKey(NativeKeyEvent.VC_F9, "F9"),
        new KeyboardKey(NativeKeyEvent.VC_F10, "F10"),
        new KeyboardKey(NativeKeyEvent.VC_F11, "F11"),
        new KeyboardKey(NativeKeyEvent.VC_F12, "F12"),
        new KeyboardKey(NativeKeyEvent.VC_0, "0"),
        new KeyboardKey(NativeKeyEvent.VC_1, "1"),
        new KeyboardKey(NativeKeyEvent.VC_2, "2"),
        new KeyboardKey(NativeKeyEvent.VC_3, "3"),
        new KeyboardKey(NativeKeyEvent.VC_4, "4"),
        new KeyboardKey(NativeKeyEvent.VC_5, "5"),
        new KeyboardKey(NativeKeyEvent.VC_6, "6"),
        new KeyboardKey(NativeKeyEvent.VC_7, "7"),
        new KeyboardKey(NativeKeyEvent.VC_8, "8"),
        new KeyboardKey(NativeKeyEvent.VC_9, "9"),
        new KeyboardKey(NativeKeyEvent.VC_Q, "Q"),
        new KeyboardKey(NativeKeyEvent.VC_W, "W"),
        new KeyboardKey(NativeKeyEvent.VC_E, "E"),
        new KeyboardKey(NativeKeyEvent.VC_R, "R"),
        new KeyboardKey(NativeKeyEvent.VC_T, "T"),
        new KeyboardKey(NativeKeyEvent.VC_Y, "Y"),
        new KeyboardKey(NativeKeyEvent.VC_U, "U"),
        new KeyboardKey(NativeKeyEvent.VC_I, "I"),
        new KeyboardKey(NativeKeyEvent.VC_O, "O"),
        new KeyboardKey(NativeKeyEvent.VC_P, "P"),
        new KeyboardKey(NativeKeyEvent.VC_A, "A"),
        new KeyboardKey(NativeKeyEvent.VC_S, "S"),
        new KeyboardKey(NativeKeyEvent.VC_D, "D"),
        new KeyboardKey(NativeKeyEvent.VC_F, "F"),
        new KeyboardKey(NativeKeyEvent.VC_G, "G"),
        new KeyboardKey(NativeKeyEvent.VC_H, "H"),
        new KeyboardKey(NativeKeyEvent.VC_J, "J"),
        new KeyboardKey(NativeKeyEvent.VC_K, "K"),
        new KeyboardKey(NativeKeyEvent.VC_L, "L"),
        new KeyboardKey(NativeKeyEvent.VC_Z, "Z"),
        new KeyboardKey(NativeKeyEvent.VC_X, "X"),
        new KeyboardKey(NativeKeyEvent.VC_C, "C"),
        new KeyboardKey(NativeKeyEvent.VC_V, "V"),
        new KeyboardKey(NativeKeyEvent.VC_B, "B"),
        new KeyboardKey(NativeKeyEvent.VC_N, "N"),
        new KeyboardKey(NativeKeyEvent.VC_M, "M"),
        new KeyboardKey(NativeKeyEvent.VC_ESCAPE, "Escape"),
        new KeyboardKey(NativeKeyEvent.VC_TAB, "Tab"),
        new KeyboardKey(NativeKeyEvent.VC_CAPS_LOCK, "CapsLock"),
        new KeyboardKey(NativeKeyEvent.VC_SHIFT, "Shift"),
        new KeyboardKey(NativeKeyEvent.VC_CONTROL, "Control"),
        new KeyboardKey(NativeKeyEvent.VC_META, "LWin"),
        new KeyboardKey(NativeKeyEvent.VC_ALT, "Alt"),
        new KeyboardKey(NativeKeyEvent.VC_SPACE, "Space"),
        new KeyboardKey(NativeKeyEvent.VC_CONTEXT_MENU, "AppsKey"),
        new KeyboardKey(NativeKeyEvent.VC_BACKSPACE, "Backspace"),
        new KeyboardKey(NativeKeyEvent.VC_ENTER, "Enter"),
        new KeyboardKey(NativeKeyEvent.VC_PRINTSCREEN, "PrintScreen"),
        new KeyboardKey(NativeKeyEvent.VC_SCROLL_LOCK, "ScrollLock"),
        new KeyboardKey(NativeKeyEvent.VC_PAUSE, "Pause"),
        new KeyboardKey(NativeKeyEvent.VC_INSERT, "Insert"),
        new KeyboardKey(NativeKeyEvent.VC_DELETE, "Delete"),
        new KeyboardKey(NativeKeyEvent.VC_HOME, "Home"),
        new KeyboardKey(NativeKeyEvent.VC_END, "End"),
        new KeyboardKey(NativeKeyEvent.VC_PAGE_UP, "PgUp"),
        new KeyboardKey(NativeKeyEvent.VC_PAGE_DOWN, "PgDn"),
        new KeyboardKey(NativeKeyEvent.VC_UP, "Up"),
        new KeyboardKey(NativeKeyEvent.VC_DOWN, "Down"),
        new KeyboardKey(NativeKeyEvent.VC_LEFT, "Left"),
        new KeyboardKey(NativeKeyEvent.VC_RIGHT, "Right")
    );

    private static final List<MouseKey> MOUSE_KEYS = Arrays.asList(
        new MouseKey(NativeMouseEvent.BUTTON1, "LButton"),
        new MouseKey(NativeMouseEvent.BUTTON2, "RButton"),
        new MouseKey(NativeMouseEvent.BUTTON3, "MButton"),
        new MouseKey(NativeMouseEvent.BUTTON4, "XButton1"),
        new MouseKey(NativeMouseEvent.BUTTON5, "XButton2")
    );
    
    public static List<KeyboardKey> getKeyboardKeys() {
        return KEYBOARD_KEYS;
    }
    
    public static List<MouseKey> getMouseKeys() {
        return MOUSE_KEYS;
    }
    
    public static boolean keyboardKeysContains(int key) {
        return KEYBOARD_KEYS.stream().anyMatch(keyboardKey -> keyboardKey.getKey() == key);
    }
    
    public static String getKeyboardKeyName(int key) {
        return KEYBOARD_KEYS.get(KEYBOARD_KEYS.indexOf(new KeyboardKey(key))).getName();
    }
    
    public static boolean mouseKeysContains(int key) {
        return MOUSE_KEYS.stream().anyMatch(mouseKey -> mouseKey.getKey() == key);
    }
    
    public static String getMouseKeyName(int key) {
        return MOUSE_KEYS.get(MOUSE_KEYS.indexOf(new MouseKey(key))).getName();
    }
}
