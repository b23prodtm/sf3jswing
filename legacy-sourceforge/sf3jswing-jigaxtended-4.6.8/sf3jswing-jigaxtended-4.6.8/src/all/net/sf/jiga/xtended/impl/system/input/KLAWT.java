/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.jiga.xtended.impl.system.input;

import java.awt.event.KeyEvent;
import javax.swing.KeyStroke;
import net.sf.jiga.xtended.JXAException;
import org.lwjgl.input.Keyboard;

/**
 * Keyboard enum from LWJGL to AWT and vice versa.
 *
 * @author www.b23prodtm.info
 */
public enum KLAWT {

        /**
         * The special keycode meaning that only the translated character is
         * valid.
         */
        KEY_NONE(KeyEvent.VK_UNDEFINED),
        /**
         * The special character meaning that no character was translated for
         * the event.
         */
        CHAR_NONE(KeyEvent.VK_UNDEFINED),
        KEY_ESCAPE(KeyEvent.VK_ESCAPE),
        KEY_1(KeyEvent.VK_1),
        KEY_2(KeyEvent.VK_2),
        KEY_3(KeyEvent.VK_3),
        KEY_4(KeyEvent.VK_4),
        KEY_5(KeyEvent.VK_5),
        KEY_6(KeyEvent.VK_6),
        KEY_7(KeyEvent.VK_7),
        KEY_8(KeyEvent.VK_8),
        KEY_9(KeyEvent.VK_9),
        KEY_0(KeyEvent.VK_0),
        KEY_MINUS(KeyEvent.VK_MINUS, KeyEvent.KEY_LOCATION_STANDARD), /* - on main keyboard */
        KEY_EQUALS(KeyEvent.VK_EQUALS),
        KEY_BACK(KeyEvent.VK_BACK_SPACE), /* backspace */
        KEY_TAB(KeyEvent.VK_TAB),
        KEY_Q(KeyEvent.VK_Q),
        KEY_W(KeyEvent.VK_W),
        KEY_E(KeyEvent.VK_E),
        KEY_R(KeyEvent.VK_R),
        KEY_T(KeyEvent.VK_T),
        KEY_Y(KeyEvent.VK_Y),
        KEY_U(KeyEvent.VK_U),
        KEY_I(KeyEvent.VK_I),
        KEY_O(KeyEvent.VK_O),
        KEY_P(KeyEvent.VK_P),
        KEY_LBRACKET(KeyEvent.VK_OPEN_BRACKET),
        KEY_RBRACKET(KeyEvent.VK_CLOSE_BRACKET),
        KEY_RETURN(KeyEvent.VK_ENTER, KeyEvent.KEY_LOCATION_STANDARD), /* Enter on main keyboard */
        KEY_LCONTROL(KeyEvent.VK_CONTROL, KeyEvent.KEY_LOCATION_LEFT),
        KEY_A(KeyEvent.VK_A),
        KEY_S(KeyEvent.VK_S),
        KEY_D(KeyEvent.VK_D),
        KEY_F(KeyEvent.VK_F),
        KEY_G(KeyEvent.VK_G),
        KEY_H(KeyEvent.VK_H),
        KEY_J(KeyEvent.VK_J),
        KEY_K(KeyEvent.VK_K),
        KEY_L(KeyEvent.VK_L),
        KEY_SEMICOLON(KeyEvent.VK_SEMICOLON),
        KEY_APOSTROPHE(KeyEvent.VK_QUOTE),
        KEY_GRAVE(KeyEvent.VK_DEAD_GRAVE), /* accent grave */
        KEY_LSHIFT(KeyEvent.VK_SHIFT, KeyEvent.KEY_LOCATION_LEFT),
        KEY_BACKSLASH(KeyEvent.VK_BACK_SLASH),
        KEY_Z(KeyEvent.VK_Z),
        KEY_X(KeyEvent.VK_X),
        KEY_C(KeyEvent.VK_C),
        KEY_V(KeyEvent.VK_V),
        KEY_B(KeyEvent.VK_B),
        KEY_N(KeyEvent.VK_N),
        KEY_M(KeyEvent.VK_M),
        KEY_COMMA(KeyEvent.VK_COMMA),
        KEY_PERIOD(KeyEvent.VK_PERIOD, KeyEvent.KEY_LOCATION_STANDARD), /* . on main keyboard */
        KEY_SLASH(KeyEvent.VK_SLASH), /* / on main keyboard */
        KEY_RSHIFT(KeyEvent.VK_SHIFT, KeyEvent.KEY_LOCATION_RIGHT),
        KEY_MULTIPLY(KeyEvent.VK_MULTIPLY, KeyEvent.KEY_LOCATION_NUMPAD), /* * on numeric keypad */
        KEY_LMENU(KeyEvent.VK_ALT, KeyEvent.KEY_LOCATION_LEFT), /* left Alt */
        KEY_SPACE(KeyEvent.VK_SPACE),
        KEY_CAPITAL(KeyEvent.VK_CAPS_LOCK),
        KEY_F1(KeyEvent.VK_F1),
        KEY_F2(KeyEvent.VK_F2),
        KEY_F3(KeyEvent.VK_F3),
        KEY_F4(KeyEvent.VK_F4),
        KEY_F5(KeyEvent.VK_F5),
        KEY_F6(KeyEvent.VK_F6),
        KEY_F7(KeyEvent.VK_F7),
        KEY_F8(KeyEvent.VK_F8),
        KEY_F9(KeyEvent.VK_F9),
        KEY_F10(KeyEvent.VK_F10),
        KEY_NUMLOCK(KeyEvent.VK_NUM_LOCK),
        KEY_SCROLL(KeyEvent.VK_SCROLL_LOCK), /* Scroll Lock */
        KEY_NUMPAD7(KeyEvent.VK_NUMPAD7),
        KEY_NUMPAD8(KeyEvent.VK_NUMPAD8),
        KEY_NUMPAD9(KeyEvent.VK_NUMPAD9),
        KEY_SUBTRACT(KeyEvent.VK_MINUS, KeyEvent.KEY_LOCATION_NUMPAD), /* - on numeric keypad */
        KEY_NUMPAD4(KeyEvent.VK_NUMPAD4),
        KEY_NUMPAD5(KeyEvent.VK_NUMPAD5),
        KEY_NUMPAD6(KeyEvent.VK_NUMPAD6),
        KEY_ADD(KeyEvent.VK_ADD, KeyEvent.KEY_LOCATION_NUMPAD), /* + on numeric keypad */
        KEY_NUMPAD1(KeyEvent.VK_NUMPAD1),
        KEY_NUMPAD2(KeyEvent.VK_NUMPAD2),
        KEY_NUMPAD3(KeyEvent.VK_NUMPAD3),
        KEY_NUMPAD0(KeyEvent.VK_NUMPAD0),
        KEY_DECIMAL(KeyEvent.VK_DECIMAL, KeyEvent.KEY_LOCATION_NUMPAD), /* . on numeric keypad */
        KEY_F11(KeyEvent.VK_F11),
        KEY_F12(KeyEvent.VK_F12),
        KEY_F13(KeyEvent.VK_F13), /*                     (NEC PC98) */
        KEY_F14(KeyEvent.VK_F14), /*                     (NEC PC98) */
        KEY_F15(KeyEvent.VK_F15), /*                     (NEC PC98) */
        KEY_F16(KeyEvent.VK_F16), /* Extended Function keys - (Mac) */
        KEY_F17(KeyEvent.VK_F17),
        KEY_F18(KeyEvent.VK_F18),
        KEY_KANA(KeyEvent.VK_KANA), /* (Japanese keyboard)            */
        KEY_F19(KeyEvent.VK_F19), /* Extended Function keys - (Mac) */
        KEY_CONVERT(KeyEvent.VK_CONVERT), /* (Japanese keyboard)            */
        KEY_NOCONVERT(KeyEvent.VK_NONCONVERT), /* (Japanese keyboard)            */
        /**
         * => returns $ (dollar sign)
         */
        KEY_YEN(KeyEvent.VK_DOLLAR), /* (Japanese keyboard)            */
        KEY_NUMPADEQUALS(KeyEvent.VK_EQUALS, KeyEvent.KEY_LOCATION_NUMPAD), /*(on numeric keypad (NEC PC98) */
        KEY_CIRCUMFLEX(KeyEvent.VK_CIRCUMFLEX), /* (Japanese keyboard)            */
        KEY_AT(KeyEvent.VK_AT), /*                     (NEC PC98) */
        KEY_COLON(KeyEvent.VK_COLON), /*                     (NEC PC98) */
        KEY_UNDERLINE(KeyEvent.VK_UNDERSCORE), /*                     (NEC PC98) */
        KEY_KANJI(KeyEvent.VK_KANJI), /* (Japanese keyboard)            */
        KEY_STOP(KeyEvent.VK_STOP), /*                     (NEC PC98) */
        /**
         * unknown Japan AX key, returns "undefined"
         */
        KEY_AX(KeyEvent.VK_UNDEFINED), /*                     (Japan AX) */
        /**
         * unknown J3100 key, returns "undefined"
         */
        KEY_UNLABELED(KeyEvent.VK_UNDEFINED), /*                        (J3100) */
        KEY_NUMPADENTER(KeyEvent.VK_ENTER, KeyEvent.KEY_LOCATION_NUMPAD), /* Enter on numeric keypad */
        KEY_RCONTROL(KeyEvent.VK_CONTROL, KeyEvent.KEY_LOCATION_RIGHT),
        /**
         * unknown Section symbol (Mac), returns "undefined"
         */
        KEY_SECTION(KeyEvent.VK_UNDEFINED), /* Section symbol (Mac) */
        KEY_NUMPADCOMMA(KeyEvent.VK_COMMA, KeyEvent.KEY_LOCATION_NUMPAD), /* , on numeric keypad (NEC PC98) */
        KEY_DIVIDE(KeyEvent.VK_DIVIDE, KeyEvent.KEY_LOCATION_NUMPAD), /* / on numeric keypad */
        /**
         * unknown SYSRQ key, returns "undefined"
         */
        KEY_SYSRQ(KeyEvent.VK_UNDEFINED),
        KEY_RMENU(KeyEvent.VK_ALT, KeyEvent.KEY_LOCATION_RIGHT), /* right Alt */
        /**
         * unknown Function key (Mac), returns "undefined"
         */
        KEY_FUNCTION(KeyEvent.VK_UNDEFINED), /* Function (Mac) */
        KEY_PAUSE(KeyEvent.VK_PAUSE), /* Pause */
        KEY_HOME(KeyEvent.VK_HOME), /* Home on arrow keypad */
        KEY_UP(KeyEvent.VK_UP), /* UpArrow on arrow keypad */
        KEY_PRIOR(KeyEvent.VK_PAGE_UP), /* PgUp on arrow keypad */
        KEY_LEFT(KeyEvent.VK_LEFT), /* LeftArrow on arrow keypad */
        KEY_RIGHT(KeyEvent.VK_RIGHT), /* RightArrow on arrow keypad */
        KEY_END(KeyEvent.VK_END), /* End on arrow keypad */
        KEY_DOWN(KeyEvent.VK_DOWN), /* DownArrow on arrow keypad */
        KEY_NEXT(KeyEvent.VK_PAGE_DOWN), /* PgDn on arrow keypad */
        KEY_INSERT(KeyEvent.VK_INSERT), /* Insert on arrow keypad */
        KEY_DELETE(KeyEvent.VK_DELETE), /* Delete on arrow keypad */
        KEY_CLEAR(KeyEvent.VK_CLEAR), /* Clear key (Mac) */
        KEY_LMETA(KeyEvent.VK_WINDOWS, KeyEvent.KEY_LOCATION_LEFT), /* Left Windows/Option key */
        KEY_RMETA(KeyEvent.VK_WINDOWS, KeyEvent.KEY_LOCATION_RIGHT), /* Right Windows/Option key */
        /**
         * unknown AppMenu key, returns 'undefined"
         */
        KEY_APPS(KeyEvent.VK_UNDEFINED), /* AppMenu key */
        /**
         * unknown Power key, returns "undefined"
         */
        KEY_POWER(KeyEvent.VK_UNDEFINED),
        /**
         * unknown Sleep key, returns "undefined"
         */
        KEY_SLEEP(KeyEvent.VK_UNDEFINED);
        /**
         * corresponding keycodes and location on the keyboard
         *
         * @see KeyEvent#KEY_LOCATION_LEFT
         * @see KeyEvent#KEY_LOCATION_NUMPAD
         * @see KeyEvent#KEY_LOCATION_RIGHT
         * @see KeyEvent#KEY_LOCATION_STANDARD
         */
        public final int awtKeyCode, keyLocation, lwjglEventKey;
        /**
         * @see KeyStroke#getKeyStroke(java.lang.Character, int)
         */
        public final char character;

        
        @Deprecated
        private KLAWT(int lwjglEventKey, int awtKeyCode, int keyLocation) {
                this.lwjglEventKey = lwjglEventKey;
                this.awtKeyCode = awtKeyCode;
                this.keyLocation = keyLocation;
                KeyStroke ks = KeyStroke.getKeyStroke(awtKeyCode, 0);
                this.character = ks != null ? ks.getKeyChar() : '\0';
        }

        
        private KLAWT(int awtKeyCode) {
                this(awtKeyCode, KeyEvent.KEY_LOCATION_STANDARD);
        }
        
        /**
         * the Keyboard.class declared final static int field is automatically fetched from the NAME() of this enum field.
         * for example : KLAWT.KEY_A is Keyboard.KEY_A 
         */
        private KLAWT(int awtKeyCode, int keyLocation) {
                try {
                        this.lwjglEventKey = Keyboard.class.getDeclaredField(name()).getInt(Keyboard.class);
                        this.awtKeyCode = awtKeyCode;
                        this.keyLocation = keyLocation;
                        KeyStroke ks = KeyStroke.getKeyStroke(awtKeyCode, 0);
                        this.character = ks != null ? ks.getKeyChar() : '\0';
                } catch (Exception ex) {
                        throw new JXAException(ex);
                }
        }

        public static KLAWT _findL(int lwjglEventKey) {
                for (KLAWT k : values()) {
                        if (k.lwjglEventKey == lwjglEventKey) {
                                return k;
                        }
                }
                return CHAR_NONE;
        }

        public static KLAWT _findA(int awtKeyCode) {
                for (KLAWT k : values()) {
                        if (k.awtKeyCode == awtKeyCode) {
                                return k;
                        }
                }
                return CHAR_NONE;
        }

        public static KLAWT _find(char character) {
                for (KLAWT k : values()) {
                        if (k.character == character) {
                                return k;
                        }
                }
                return CHAR_NONE;
        }
}
