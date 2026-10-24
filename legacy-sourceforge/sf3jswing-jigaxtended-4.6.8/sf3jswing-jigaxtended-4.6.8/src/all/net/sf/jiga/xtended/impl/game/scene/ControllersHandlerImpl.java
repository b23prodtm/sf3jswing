/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package net.sf.jiga.xtended.impl.game.scene;

import java.awt.KeyEventDispatcher;
import java.awt.event.KeyEvent;
import java.util.Iterator;
import javax.imageio.ImageIO;
import net.sf.jiga.xtended.impl.GUIConsoleOutput;
import net.sf.jiga.xtended.impl.game.RenderingScene;
import net.sf.jiga.xtended.impl.system.input.ControllersHandler;
import net.sf.jiga.xtended.kernel.DebugMap;

/**
 *
 * @author tiana
 */
public class ControllersHandlerImpl extends ControllersHandler {
        private final RenderingScene scene;

        public ControllersHandlerImpl(String s, final RenderingScene scene) {
                super(s);
                this.scene = scene;
        }

        @Override
        protected void processOneKeyboardEvent(KeyEvent event) {
                if (scene.isDebugEnabled()) {
                        System.out.println(event.paramString());
                }
                boolean b = false;
                if (scene.isKeyboardOptionsEnabled()) {
                        switch (event.getID()) {
                                case KeyEvent.KEY_PRESSED:
                                        keyPressed = true;
                                        float size = scene.getFont().getSize();
                                        switch (event.getKeyCode()) {
                                                case KeyEvent.VK_F6:
                                                case KeyEvent.VK_F5:
                                                        if (event.getKeyCode() == KeyEvent.VK_F6) {
                                                                size = size < 72 ? size + 1 : size;
                                                        } else {
                                                                size = size > 3 ? size - 1 : size;
                                                        }
                                                        scene.setFont(scene.getFont().deriveFont(size));
                                                        scene.getHud_debugActivity().clearResource();
                                                        scene.getHud_helpcommands().clearResource();
                                                        scene.getGCO().setDirty(true);
                                                        b = true;
                                                        break;
                                                case KeyEvent.VK_F2:
                                                        if (event.isAltDown() || event.isShiftDown()) {
                                                                scene.getGCO().clearContents();
                                                        } else {
                                                                scene.setConsoleEnabled(!scene.isConsoleEnabled());
                                                        }
                                                        b = true;
                                                        break;
                                                case KeyEvent.VK_F10:
                                                        ImageIO.setUseCache(!ImageIO.getUseCache());
                                                        b = true;
                                                        break;
                                                case KeyEvent.VK_F1:
                                                        if (event.isAltDown() || event.isShiftDown()) {
                                                                RenderingScene.setDebugRenderEnabled(!RenderingScene.isDebugRenderEnabled());
                                                        } else {
                                                                RenderingScene._paintCommandsEnabled = !RenderingScene._paintCommandsEnabled;
                                                        }
                                                        b = true;
                                                        break;
                                                case KeyEvent.VK_F11:
                                                        switch (scene.getState()) {
                                                                case RUNNING:
                                                                        scene.stopRendering();
                                                                        break;
                                                                default:
                                                                        scene.startRendering();
                                                                        break;
                                                        }
                                                        b = true;
                                                        break;
                                                case KeyEvent.VK_F4:
                                                        scene.switchFullScreen();
                                                        b = true;
                                                        break;
                                                case KeyEvent.VK_PAGE_UP:
                                                        if (scene.isConsoleEnabled()) {
                                                                scene.getGCO().scrollUp();
                                                                b = true;
                                                        }
                                                        break;
                                                case KeyEvent.VK_PAGE_DOWN:
                                                        if (scene.isConsoleEnabled()) {
                                                                scene.getGCO().scrollDown();
                                                                b = true;
                                                        }
                                                        break;
                                                case KeyEvent.VK_F9:
                                                        setMultiThreadingEnabled(!isMultiThreadingEnabled());
                                                        b = true;
                                                        break;
                                                case KeyEvent.VK_F12:
                                                        if (RenderingScene.lwjglEnabled) {
                                                                scene.setLWJGLAccel(!scene.isLWJGLAccel());
                                                        } else {
                                                                setHardwareAccel(!isHardwareAccel());
                                                        }
                                                        b = true;
                                                        break;
                                                case KeyEvent.VK_F7:
                                                        scene.setSoundMute(!scene.isSoundMute());
                                                        b = true;
                                                        break;
                                                case KeyEvent.VK_F3:
                                                        scene.setDebugActivityEnabled(!scene.isDebugActivityEnabled());
                                                        b = true;
                                                        break;
                                                default:
                                                        b = false;
                                                        break;
                                        }
                                        break;
                                default:
                                        keyPressed = false;
                                        break;
                        }
                }
                if (!b) {
                        if (DebugMap._getInstance().isDebuggerEnabled(GUIConsoleOutput.class)) {
                                System.out.println("KED " + event.paramString());
                        }
                        if (scene.isConsoleEnabled()) {
                                b = scene.getGCO().dispatchKeyEvent(event);
                        }
                        synchronized (keds) {
                                for (Iterator<KeyEventDispatcher> i = keds.iterator(); i.hasNext() && !b;) {
                                        b = i.next().dispatchKeyEvent(event);
                                }
                        }
                }
        }
}
