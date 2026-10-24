/**
 *
 * Created on 17 avril 2007, 00:25
 *
 * To change this template, choose Tools | Template Manager and open the
 * template in the editor.
 */
package net.sf.jiga.xtended.kernel;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.*;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.OperatingSystemMXBean;
import java.lang.management.RuntimeMXBean;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.security.*;
import java.util.*;
import java.util.List;
import java.util.logging.LogManager;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.zip.Checksum;
import javax.imageio.ImageIO;
import javax.swing.*;
import net.sf.jiga.xtended.JXAException;
import static net.sf.jiga.xtended.kernel.FileHelper._tmpFilesSuffix;
import net.sf.jiga.xtended.ui.*;
import net.sf.jiga.xtended.ui.TransparentBackground;
import net.sf.jiga.xtended.ui.UIMessage.SwingStaticReturn;
import org.apache.commons.httpclient.*;
import org.apache.commons.io.FileUtils;

/**
 * JXAenvUtils replaces the old LibLoader class, since the loading of libraries
 * has been dramatically improved through the Java Plug-In extensions and also,
 * the Java Security manager has proven that loading external lib's is not
 * always possible without setting permissions accurately to work properly
 * (especially with such "Miss Vista"--MS Windows Vista security walls).
 *
 * <br>This class is Cloneable
 * ({@link ExtensionsInstaller#_installExtensions(java.util.Map, boolean, java.io.File) backup purposes}).
 * <br><b>see {@link #addEnvJars(java.net.URL[])} method for Extensions loading
 * purposes.
 *
 * @author www.b23prodtm.info
 */
public class JXAenvUtils extends Logger implements Cloneable, Resource, Debugger {

        /**
         *
         */
        protected static AccessControlContext acc = AccessController.getContext();

        /**
         * permissive call to {@linkplain System#getProperty(String)}
         *
         * @param name
         * @return the value String or null if no property was set
         */
        public static String _getSysValue(final String name) {
                return AccessController.doPrivileged(new PrivilegedAction<String>() {
                        public String run() {
                                return System.getProperty(name);
                        }
                }, acc);
        }

        /**
         * permissive call to {@linkplain Boolean#getBoolean(String)}
         *
         * @param name
         * @return
         */
        public static boolean _getSysBoolean(final String name) {
                return AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
                        public Boolean run() {
                                return Boolean.getBoolean(name);
                        }
                }, acc);
        }

        /**
         * permissive call to {@linkplain Integer#getInteger(String)}}
         *
         * @param name
         * @return
         */
        public static int _getSysInteger(final String name) {
                return AccessController.doPrivileged(new PrivilegedAction<Integer>() {
                        public Integer run() {
                                return Integer.getInteger(name);
                        }
                }, acc);
        }

        /**
         * permissive call to {@linkplain System#getProperty(String)}
         *
         * @param name
         * @param value
         */
        public static void _setSysValue(final String name, final String value) {
                AccessController.doPrivileged(new PrivilegedAction() {
                        public String run() {
                                System.setProperty(name, value);
                                if (_debugSys) {
                                        System.out.println(log("system property updated : " + name + " new value : " + value, LVL.SYS_NOT));
                                }
                                return null;
                        }
                }, acc);
        }
        /**
         * the JXAenvUtils ResourceBundle. it contains useful default
         * properties; e.g. the classpath
         */
        public static ResourceBundle rb = ResourceBundle.getBundle("net.sf.jiga.xtended.kernel.jxaenvutils");
        /**
         * switches off/on debugging<br> <b>may be enabled by adding and setting
         * <u>jxa.debug=true</u> at launching</b>
         */
        public static boolean _debug = Boolean.parseBoolean(rb.getString("debugEnabled")) || _getSysBoolean(Ant.JXA_DEBUG);
        /**
         * System debug : all low-level activity (JXAInstaller) is verbosed
         * <b>may be enabled by adding and setting <u>jxa.debugSys</u> at
         * launching</b>
         */
        public static boolean _debugSys = _getSysBoolean(Ant.JXA_DEBUG_SYS);
        /**
         *
         */
        private static BitStack _ENV_BITS = new BitStack();
        private Map<String, Map<String, Map<URL, Boolean>>> envNatives = Collections.synchronizedMap(new LinkedHashMap<String, Map<String, Map<URL, Boolean>>>());
        protected Map<String, Map<String, URL>> envJars = Collections.synchronizedMap(new LinkedHashMap<String, Map<String, URL>>());
        /**
         * load environment keepRemote switch, if true, installs-download into a
         * temp folder or, if false, keeps remote .jar resources loading
         *
         * @default false
         */
        public boolean keepReadingOnRemoteJarResources = false;
        /**
         * disables various errors dialogs in the current environment instance
         * (JXAenvUtils) and blocking exception (best used for backup tasks, and
         * background tasks)
         */
        public boolean silent = false;

        /**
         *
         * @param url the value of url
         * @deprecated Moved to
         * {@link net.sf.jiga.xtended.kernel.FileHelper#_getURLFilename}
         */
        @Deprecated
        public static String _getURLFilename(URL url) {
                return FileHelper._getURLFilename(url);
        }

        /**
         * Will send the jars to the ExtensionsClassLoader, for loading classes
         * and resources.<BR>
         * The <b>Attribute Class-Path</b> in any of the included .jars Manifest
         * files will be fully read, as of the Extensions specifications. <BR>
         * Note that <B>this is a specific behaviour of JXAKernel</b<, unlike
         * JVM which treats .jar as extensions only if they are specified as
         * such in the JNLP or Applet Tags. <br> Note also that this method in
         * called once on loading any of the JFCFrame or JFCApplet, which reads
         * the classpath parameter/property included <b>in the
         * build-osname-arch.xml file</B>.
         *
         * @param jars String is the file name of jar; URL is the fullpath
         */
        public void addEnvJars(URL[] jars) {
                Map<String, URL> map = new LinkedHashMap<String, URL>();
                for (URL u : jars) {
                        if (u == null) {
                                System.out.println(log("jar was not found !! please check input", LVL.SYS_ERR));
                        } else {
                                map.put(_getURLFilename(u), u);
                        }
                }
                addEnvJars(map);
        }

        /**
         * @see #addEnvJars(java.net.URL[]) Caution also add included Manifest
         * "Class-Path :" listed .jar files.
         * @param jars String is the file name of jar; URL is the fullpath
         */
        public void addEnvJars(Map<String, URL> jars) {
                Map<String, URL> jarsMap = this.envJars.get(_getSysValue("os.name"));
                if (jarsMap == null) {
                        this.envJars.put(_getSysValue("os.name"), (jarsMap = new LinkedHashMap<String, URL>()));
                }
                jarsMap.putAll(jars);
                /**
                 * add Class-Path:
                 */
                synchronized (jars) {
                        for (URL jar : jars.values()) {
                                try {
                                        jarsMap.putAll(ExtensionsInstaller._getJarClasspathJars(jar));
                                } catch (IOException ex) {
                                        if (_debugSys) {
                                                ex.printStackTrace();
                                        }
                                }
                        }
                }
        }

        /**
         * @param jars String is the system name of native; URL is the fullpath
         * (including "jar:" urls)
         */
        public void addEnvNatives(Map<String, Map<URL, Boolean>> natives) {
                Map<String, Map<URL, Boolean>> nativesMap = this.envNatives.get(_getSysValue("os.name"));
                if (nativesMap == null) {
                        this.envNatives.put(_getSysValue("os.name"), (nativesMap = new LinkedHashMap<String, Map<URL, Boolean>>()));
                }
                nativesMap.putAll(natives);
        }

        private static abstract class isEnv {

                env e;

                public isEnv(env e) {
                        this.e = e;
                }

                public final boolean check() {
                        return AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
                                public Boolean run() {
                                        return checkProperty();
                                }
                        }, acc);
                }

                protected abstract boolean checkProperty();
        }

        private static class isOS extends isEnv {

                public isOS(env e) {
                        super(e);
                }

                public boolean checkProperty() {
                        String s = JXAenvUtils._getSysValue(e.propertyName());
                        return s == null ? false : s.startsWith(e.propertyValue());
                }
        }
        private final static int envCheckStartsWith = 1;

        /**
         *
         */
        public static enum env {

                /**
                 *
                 */
                _OS() {
                                @Override
                                public String propertyName() {
                                        return "os.name";
                                }
                        },
                /**
                 *
                 */
                OS_WINDOWS(_OS, envCheckStartsWith) {
                                @Override
                                public String propertyValue() {
                                        return "Windows";
                                }
                        },
                /**
                 *
                 */
                OS_WINDOWS_XP(_OS, envCheckStartsWith) {
                                @Override
                                public String propertyValue() {
                                        return "Windows XP";
                                }
                        },
                /**
                 *
                 */
                OS_WINDOWS_VISTA(_OS, envCheckStartsWith) {
                                @Override
                                public String propertyValue() {
                                        return "Windows Vista";
                                }
                        },
                /**
                 *
                 */
                OS_WINDOWS_7(_OS, envCheckStartsWith) {
                                @Override
                                public String propertyValue() {
                                        return "Windows 7";
                                }
                        },
                /**
                 *
                 */
                OS_WINDOWS_8(_OS, envCheckStartsWith) {
                                @Override
                                public String propertyValue() {
                                        return "Windows 8";
                                }
                        },
                /**
                 *
                 */
                OS_WINDOWS_10(_OS, envCheckStartsWith) {
                                @Override
                                public String propertyValue() {
                                        return "Windows 10";
                                }
                        },
                /**
                 *
                 */
                OS_MAC(_OS, envCheckStartsWith) {
                                @Override
                                public String propertyValue() {
                                        return "Mac OS";
                                }
                        },
                /**
                 *
                 */
                OS_LINUX(_OS, envCheckStartsWith) {
                                @Override
                                public String propertyValue() {
                                        return "Linux";
                                }
                        },
                /**
                 *
                 */
                OS_OTHER(_OS, new isOS(_OS) {
                        @Override
                        public boolean checkProperty() {
                                return !OS_LINUX.isEnv() && !OS_MAC.isEnv() && !OS_WINDOWS.isEnv();
                        }
                }) {
                                @Override
                                public String propertyValue() {
                                        return "unknown";
                                }
                        },
                /**
                 *
                 */
                _JAVA(),
                /**
                 *
                 */
                _JAVA_VERSION() {
                                @Override
                                public String propertyName() {
                                        return "java.version";
                                }
                        },
                /**
                 *
                 */
                JAVA_VERSION_5(_JAVA_VERSION, envCheckStartsWith) {
                                @Override
                                public String propertyValue() {
                                        return "1.5";
                                }
                        },
                /**
                 *
                 */
                JAVA_VERSION_6(_JAVA_VERSION, envCheckStartsWith) {
                                @Override
                                public String propertyValue() {
                                        return "1.6";
                                }
                        },
                /**
                 *
                 */
                JAVA_VERSION_7(_JAVA_VERSION, envCheckStartsWith) {
                                @Override
                                public String propertyValue() {
                                        return "1.7";
                                }
                        },
                /**
                 *
                 */
                JAVA_VERSION_8(_JAVA_VERSION, envCheckStartsWith) {
                                @Override
                                public String propertyValue() {
                                        return "1.8";
                                }
                        },
                /**
                 *
                 */
                JAVA_2D_OPENGL(_JAVA, 0) {
                                @Override
                                public String propertyName() {
                                        return "sun.java2d.opengl";
                                }

                                @Override
                                public String propertyValue() {
                                        return "true";
                                }
                        },
                /**
                 *
                 */
                JAVA_2D_D3D(_JAVA, 0) {
                                @Override
                                public String propertyName() {
                                        return "sun.java2d.d3d";
                                }

                                @Override
                                public String propertyValue() {
                                        return "true";
                                }
                        },
                /**
                 *
                 */
                _THREADING(),
                /**
                 *
                 */
                THREADING_MULTI(_THREADING, new isEnv(_THREADING) {
                        @Override
                        public boolean checkProperty() {
                                return Runtime.getRuntime().availableProcessors() > 1 && !_getSysBoolean("jxa.nomt");
                        }
                }),
                /**
                 *
                 */
                _ARCH() {
                                @Override
                                public String propertyName() {
                                        return "os.arch";
                                }
                        },
                /**
                 *
                 */
                ARCH_X86(_ARCH, 0) {
                                @Override
                                public String propertyValue() {
                                        return "x86";
                                }
                        },
                /**
                 *
                 */
                ARCH_AMD64(_ARCH, 0) {
                                @Override
                                public String propertyValue() {
                                        return "amd64";
                                }
                        },
                /**
                 *
                 */
                ARCH_PPC(_ARCH, 0) {
                                @Override
                                public String propertyValue() {
                                        return "ppc";
                                }
                        },
                /**
                 *
                 */
                ARCH_I386(_ARCH, 0) {
                                @Override
                                public String propertyValue() {
                                        return "i386";
                                }
                        },
                /**
                 *
                 */
                ARCH_PPC64(_ARCH, 0) {
                                @Override
                                public String propertyValue() {
                                        return "ppc64";
                                }
                        },
                /**
                 *
                 */
                _APP() {
                                @Override
                                public String propertyName() {
                                        return "jxa.app.type";
                                }
                        },
                /**
                 *
                 */
                APP_APPLET(_APP, 0) {
                                @Override
                                public String propertyValue() {
                                        return "antapplet";
                                }
                        },
                /**
                 *
                 */
                APP_FRAME(_APP, 0) {
                                @Override
                                public String propertyValue() {
                                        return "antframe";
                                }
                        },
                /**
                 * class loading related
                 */
                APP_REMOTE(_APP, 0) {
                                @Override
                                public String propertyName() {
                                        return "jxa.app.remote";
                                }

                                @Override
                                public String propertyValue() {
                                        return "true";
                                }
                        };
                /**
                 * associated bitMask mask
                 */
                int mask;
                isEnv check;
                env parentEnv = null;

                /**
                 * @param useParentPropertyName use property name of the parent
                 * option to make checks
                 * @param check option {@link envCheckStartsWith} to use
                 * propertyName.startwith(propertyValue)
                 */
                env(final env parentEnv, int check) {
                        this.parentEnv = parentEnv;
                        mask = _ENV_BITS._newBit(parentEnv.mask);
                        switch (check) {
                                case envCheckStartsWith:
                                        this.check = new isOS(this);
                                        break;
                                default:
                                        this.check = new isEnv(this) {
                                                @Override
                                                protected boolean checkProperty() {
                                                        String s = JXAenvUtils._getSysValue(e.propertyName());
                                                        return s == null ? false : s.equalsIgnoreCase(e.propertyValue());
                                                }
                                        };
                                        break;
                        }
                }

                env(env parentRangeOption, isEnv check) {
                        this.check = check;
                        parentEnv = parentRangeOption;
                        mask = _ENV_BITS._newBit(parentRangeOption.mask);

                }

                /**
                 * new range of options
                 */
                env() {
                        parentEnv = null;
                        this.check = new isEnv(this) {
                                @Override
                                public boolean checkProperty() {
                                        return true;
                                }
                        };
                        mask = _ENV_BITS._newBitRange();
                }

                /**
                 *
                 * @return
                 */
                public boolean isEnv() {
                        return check.check();
                }

                /**
                 *
                 * @return
                 */
                public int bitMask() {

                        return mask;

                }

                /**
                 *
                 * @return
                 */
                public int compareMask() {
                        return _ENV_BITS._getAllBits() & mask;
                }

                /**
                 *
                 * @return
                 */
                public String osname() {
                        return check instanceof isOS ? propertyValue() : _getSysValue("os.name");
                }

                /**
                 * the exact property name or a simple name String for this
                 * property enum
                 *
                 * @return
                 */
                public String propertyName() {
                        return parentEnv != null ? parentEnv.propertyName() : toString();
                }

                /**
                 * return the current value or the expected value for this
                 * property
                 *
                 * @return
                 */
                public String propertyValue() {
                        return _getSysValue(propertyName());
                }

                /**
                 * by inputing {@linkplain env} constants, this checks if ALL of
                 * the constants are found in the current environment.
                 *
                 * @param envMask
                 * @return true if all of the array is {@linkplain env#isEnv()}
                 * == true
                 */
                public static boolean _isEnv(env[] envMask) {
                        for (env e : envMask) {
                                if (!e.isEnv()) {
                                        return false;
                                }
                        }
                        return true;
                }

                /**
                 * by inputing a {@linkplain env#bitMask()} bitwise OR-ed mask,
                 * this checks if ALL of the bits are found.
                 *
                 * @param envBitsMask
                 * @return
                 * @discussion this method returns faster as it checks
                 * bit-values.
                 */
                public static boolean _isEnv(int envBitsMask) {
                        return _isEnv(envBitsMask, envBitsMask);
                }

                /**
                 * by inputing a {@linkplain env#bitMask()} bitwise OR-ed mask,
                 * this checks if SOME of the bits are found.
                 *
                 * @param envBitsMask
                 * @return
                 * @discussion this method returns faster as it checks
                 * bit-values.
                 */
                public static boolean _hasEnv(int envBitsMask) {
                        return !_isEnv(envBitsMask, 0);
                }

                /**
                 * by inputing a {@linkplain env#bitMask()} bitwise OR-ed mask,
                 * this checks if envBitsMask matches ALL ref bits.
                 *
                 * @return true if and only if envBitsMask matches ALL ref for
                 * the current environment.
                 * @param envBitsMask bits to check
                 * @param ref bits that must be ALL checked for in envBitsMask
                 * @discussion this method returns faster as it checks
                 * bit-values.
                 */
                public static boolean _isEnv(int envBitsMask, int ref) {
                        return (envBitsMask & _getEnvBits() & _ENV_BITS._getAllBits()) == (ref & _ENV_BITS._getAllBits());
                }

                /**
                 * returns the current runtime environment bits from
                 * {@linkplain #env} enum
                 *
                 * @return
                 */
                public static int _getEnvBits() {
                        int bits = 0;
                        for (env e : env.values()) {
                                if (e.isEnv()) {
                                        bits |= e.bitMask();
                                }
                        }
                        return bits;
                }

                /**
                 * returns the current runtime environment bits from
                 * {@linkplain env} enum
                 *
                 * @return
                 */
                public static env[] _getEnv() {
                        ArrayList<env> bits = new ArrayList<env>();
                        for (env e : env.values()) {
                                if (e.isEnv()) {
                                        bits.add(e);
                                }
                        }
                        return bits.toArray(new env[]{});
                }

                public static env[] _getEnv(env property) {
                        ArrayList<env> bits = new ArrayList<env>();
                        for (env e : env.values()) {
                                if (e.isEnv() && (0 != (_ENV_BITS._getMask(property.bitMask()) & e.bitMask()))) {
                                        bits.add(e);
                                }
                        }
                        return bits.toArray(new env[]{});
                }

        }
        /**
         * the default screen device is selected for the default graphics
         * configuration (splash screens and UImessages)
         */
        public static GraphicsConfiguration _defaultGC = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration();
        /**
         * a display for the splash picture
         */
        private DisplayInterface splash;
        /**
         * panel for the splash content to display
         */
        private Container splashContents;
        /**
         * frame for the splash content
         */
        private JFrame splashFrame;

        /**
         * This method uses the Java Security Framework to check the
         * FilePermission for some file.
         *
         * @param file the file path you want to check security
         * @param fileMode the access-mode : a bitwise-OR combination of
         * {@linkplain #FILE_READ}, {@linkplain #FILE_WRITE}, {@linkplain #FILE_EXECUTE}
         * or {@linkplain #FILE_DELETE}
         * @return true or false, whether the FilePermission is returning ALL OK
         * or not for the specified fileMode, resp.
         * @throws SecurityException if the file cannot be accessed using one,
         * more or all the specified access modes
         * @see FilePermission
         * @deprecated Moved to
         * {@link net.sf.jiga.xtended.kernel.FileHelper#_accessFilePermitted}
         */
        @Deprecated
        public static boolean _accessFilePermitted(final File file, final int fileMode) {
                return FileHelper._accessFilePermitted(file, fileMode);
        }

        /**
         * This method uses the Java Security Framework to check the
         * FilePermission for some file.
         *
         * @param file the file path you want to check security
         * @param fileMode the access-mode : a bitwise-OR combination of
         * {@linkplain #FILE_READ}, {@linkplain #FILE_WRITE}, {@linkplain #FILE_EXECUTE}
         * or {@linkplain #FILE_DELETE}
         * @return true or false, whether the FilePermission is returning ALL OK
         * or not for the specified fileMode, resp.
         * @throws SecurityException if the file cannot be accessed using one,
         * more or all the specified access modes
         * @see FilePermission
         * @deprecated Moved to
         * {@link net.sf.jiga.xtended.kernel.FileHelper#__accessFilePermitted}
         */
        @Deprecated
        private static boolean __accessFilePermitted(File file, int fileMode) {
                return FileHelper.__accessFilePermitted(file, fileMode);

        }
        private static final BitStack lvlBits = new BitStack();
        private static final int LVL_USER = lvlBits._newBitRange();
        private static final int LVL_APP = lvlBits._newBitRange();
        private static final int LVL_SYS = lvlBits._newBitRange();
        private static final int TYPE_ERROR = lvlBits._newBitRange();
        private static final int TYPE_NOTICE = lvlBits._newBitRange();
        private static final int TYPE_WARNING = lvlBits._newBitRange();
        /**
         *
         */
        public static final int APP_ERROR = lvlBits._newBit(LVL_APP | TYPE_ERROR);
        /**
         *
         */
        public static final int APP_NOTICE = lvlBits._newBit(LVL_APP | TYPE_NOTICE);
        /**
         *
         */
        public static final int APP_WARNING = lvlBits._newBit(LVL_APP | TYPE_WARNING);
        /**
         *
         */
        public static final int USER_ERROR = lvlBits._newBit(LVL_USER | TYPE_ERROR);
        /**
         *
         */
        public static final int USER_NOTICE = lvlBits._newBit(LVL_USER | TYPE_NOTICE);
        /**
         *
         */
        public static final int USER_WARNING = lvlBits._newBit(LVL_USER | TYPE_WARNING);
        /**
         *
         */
        public static final int SYS_ERROR = lvlBits._newBit(LVL_SYS | TYPE_ERROR);
        /**
         *
         */
        public static final int SYS_NOTICE = lvlBits._newBit(LVL_SYS | TYPE_NOTICE);
        /**
         *
         */
        public static final int SYS_WARNING = lvlBits._newBit(LVL_SYS | TYPE_WARNING);

        private static String log(String s, int level) {
                String levelStr = "";
                String output = "";
                if ((level & LVL_SYS) != 0) {
                        levelStr = "SYSTEM";
                }
                if ((level & LVL_APP) != 0) {
                        levelStr = "APPLICATION";
                }
                if ((level & LVL_USER) != 0) {
                        levelStr = "";
                }
                if ((level & TYPE_ERROR) != 0) {
                        levelStr += " ERROR";
                }
                if ((level & TYPE_NOTICE) != 0) {
                        levelStr += " NOTICE";
                }
                if ((level & TYPE_WARNING) != 0) {
                        levelStr += " WARNING";
                }
                StackTraceElement[] trace = Thread.currentThread().getStackTrace();
                output += "*" + levelStr + "* " + new Date(System.currentTimeMillis()).toString() + " " + JXAenvUtils._getSysValue("line.separator");
                output += "*** " + (trace.length > 2 ? trace[3].toString() : " ") + " < " + trace[2].toString() + JXAenvUtils._getSysValue("line.separator");
                output += "*** [Thread : " + Thread.currentThread().getName() + " ] " + s + " ***" + JXAenvUtils._getSysValue("line.separator");
                output += "***" + JXAenvUtils._getSysValue("line.separator");
                return output;
        }

        /**
         * @return Log styled printing to System.out e.g.
         * <pre>System.out.println(log("this is an output",LVL_SYS));</pre>
         *
         * @param s
         * @param level
         */
        public static String log(String s, LVL level) {
                return log(s, level.level);
        }

        @Override
        public void log(LogRecord record) {
                if (record.getThrown() != null) {
                        System.out.println(log(record.getMessage(), LVL._findJXALevel(LVL_SYS, record.getLevel())));
                } else if (record.getMessage() != null) {
                        System.out.println(log(record.getMessage(), LVL._findJXALevel(LVL_APP, record.getLevel())));
                } else {
                        System.out.println(log(record.getMessage(), LVL._findJXALevel(LVL_USER, record.getLevel())));
                }
        }

        /**
         *
         * see corresponding levels from the Logger class that is implemented :
         * <ul>
         * <li>*_ERR : java.util.logging.Level.SEVERE</li>
         * <li>*_WRN : java.util.logging.Level.WARNING</li>
         * <li>*_NOT : java.util.logging.Level.INFO</li>
         * </ul>
         * Anybody would use Logger.setLevel() to distinguish whatever they want
         * to appear in the log console (system.out).
         */
        public static enum LVL {

                SYS_ERR(LVL_SYS | TYPE_ERROR, java.util.logging.Level.SEVERE), SYS_WRN(LVL_SYS | TYPE_WARNING, java.util.logging.Level.WARNING), SYS_NOT(LVL_SYS | TYPE_NOTICE, java.util.logging.Level.INFO),
                APP_ERR(LVL_APP | TYPE_ERROR, java.util.logging.Level.SEVERE), APP_WRN(LVL_APP | TYPE_WARNING, java.util.logging.Level.WARNING), APP_NOT(LVL_APP | TYPE_NOTICE, java.util.logging.Level.INFO),
                USR_ERR(LVL_USER | TYPE_ERROR, java.util.logging.Level.SEVERE), USR_WRN(LVL_USER | TYPE_WARNING, java.util.logging.Level.WARNING), USR_NOT(LVL_USER | TYPE_NOTICE, java.util.logging.Level.INFO);
                public int level;
                public java.util.logging.Level loggingLevel;

                LVL(int level, java.util.logging.Level loggingLevel) {
                        this.level = level;
                        this.loggingLevel = loggingLevel;
                }

                static LVL _findJXALevel(int level, java.util.logging.Level loggingLevel) {
                        for (LVL l : values()) {
                                if (l.loggingLevel.equals(loggingLevel) && (0 != (l.level & level))) {
                                        return l;
                                }
                        }
                        return USR_NOT;
                }
        }

        /**
         * @deprecated use {@link #log(java.lang.String, net.sf.jiga.xtended.kernel.JXAenvUtils.LVL)
         * }
         */
        public static String _JXAEnvOutput(String s, int level) {
                return log(s, level);
        }

        /**
         * finds a directory that is allowed to read, write and execute.
         *
         * @param freeList
         * @return
         * @deprecated Moved to
         * {@link net.sf.jiga.xtended.kernel.FileHelper#_findFreeDirectory}
         */
        @Deprecated
        public static File _findFreeDirectory(List<File> freeList) {
                return FileHelper._findFreeDirectory(freeList);
        }

        /**
         *
         * @deprecated Moved to
         * {@link net.sf.jiga.xtended.kernel.FileHelper#_findTempDirectory}
         */
        @Deprecated
        static File _findTempDirectory() {
                return FileHelper._findTempDirectory();
        }

        /**
         *
         * @deprecated Moved to
         * {@link net.sf.jiga.xtended.kernel.FileHelper#_findHomeDirectory}
         */
        @Deprecated
        static File _findHomeDirectory() {
                return FileHelper._findHomeDirectory();
        }

        /**
         * default path for the files {@link #_TMPDIRECTORY}
         */
        private final static String DEFAULT_PATH = FileHelper._TMPDIRECTORY.getPath();
        /**
         * env files directory
         *
         * private String envSubDir = ".";
         */
        /**
         * commonclassLoader
         */
        ClassLoader classLoader;
        /**
         * files map
         */
        private Map<String, URL> map = Collections.synchronizedMap(new LinkedHashMap<String, URL>());
        /**
         * env loading path
         */
        private String envPath = DEFAULT_PATH;
        /**
         * java.library.path array retriven from RuntimeMXBean
         */
        String[] libraryPath = getLibraryPath().split(File.pathSeparator);

        /**
         * returns the common ClassLoader associated to this environment and
         * switches the current Thread classLoader to it
         *
         * @return this classLoader can load all jxa extensions .jar specified
         * @see #_switchToClassLoader(ClassLoader)
         */
        public ClassLoader getEnvClassLoader() {
                return classLoader;
        }

        /**
         * switches to the classLoader and returns the replaced context
         * ClassLoader (so that it can be switched back to it) for the current
         * Thread.
         *
         * @param classLoader
         * @return
         */
        public static ClassLoader _switchToClassLoader(ClassLoader classLoader) {
                ClassLoader currentCL = Thread.currentThread().getContextClassLoader();
                Thread.currentThread().setContextClassLoader(classLoader);
                return currentCL;
        }
        /**
         * ImageIO scanfor plugins
         */
        Runnable envImageIOLayer = new Runnable() {
                public void run() {
                        ClassLoader c = _switchToClassLoader(ExtensionsClassLoader.getInstance().getClassLoader());
                        ImageIO.scanForPlugins();
                        _switchToClassLoader(c);
                }
        };
        /**
         * LWJGL settings (library paths)
         */
        Runnable envLWJGLLayer = new Runnable() {
                public void run() {
                        _setSysValue("net.java.games.input.librarypath", ExtensionsInstaller._findExtPath(true));
                        _setSysValue("org.lwjgl.librarypath", _updatePath(JXAenvUtils._getSysValue("org.lwjgl.librarypath"), ExtensionsInstaller._findExtPath(true)));
                }
        };
        /**
         * JMF settings (library paths)
         */
        Runnable envJMFLayer = new Runnable() {
                public void run() {
                        _setSysValue("java.class.path", _updatePath(getClasspath(), FileHelper._USERHOMEDIRECTORY.getAbsolutePath()));
                }
        };

        /**
         *
         */
        public void loadEnvironment() {
                AccessController.doPrivileged(new PrivilegedAction() {
                        public Object run() {
                                _loadEnvironment();
                                return null;
                        }
                }, acc);
        }
        private static boolean exitedJXA = false;
        private static boolean exiting = false;
        private Runnable quitJXAEnv = new Runnable() {
                public void run() {
                        unloadEnvironment(true);
                }
        };

        /**
         *
         */
        public void unloadEnvironment(final boolean cleanTemp) {
                AccessController.doPrivileged(new PrivilegedAction() {
                        public Object run() {
                                _unloadEnvironment(cleanTemp);
                                return null;
                        }
                }, acc);
        }

        /**
         * formatted as of {@link Date#toString()}
         * <br>dow mon dd hh:mm:ss zzz yyyy
         *
         * @param t time in millis
         * @return new Date(t).toString();
         * @deprecated
         */
        public static String _getDateAndTime(long t) {
                return new Date(t).toString();
        }
        private Runnable logUncaughtExceptionsClose = new Runnable() {
                public void run() {
                        ThreadWorks.cLogFile_close();
                }
        }, logUncaughtExceptionsOpen = new Runnable() {
                public void run() {
                        ThreadWorks.cLogFile_open(antClassName);
                        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
                                public void uncaughtException(Thread t, Throwable e) {
                                        ThreadWorks._uncaughtException(true, t, e);
                                }
                        });
                }
        };

        public static void cLogFilePrintStackStrace(final Throwable throwable) {
                ThreadWorks.cLogFilePrintStackStrace(throwable);
        }

        /**
         * @return true if the environment files are known to be locally stored
         * on disk and accessible.
         * @see env#APP_REMOTE
         * @see JFCApplet#_localApplet
         */
        public boolean isLocal() {
                return !env.APP_REMOTE.isEnv() || JFCApplet._localApplet;
        }

        private void _loadEnvironment() {
                if (!ExtensionsClassLoader.isResourceLoaded()) {
                        /**
                         * further installExtensions will write .jar from the
                         * envPath, so classLoader must read there
                         */
                        ExtensionsClassLoader._load(this);
                }
                AntHandler._applyLAF("native");
                boolean swingEdt = ThreadWorks.Swing.isEventDispatchThread();
                if (swingEdt && splashContents == null) {
                        _initSplashContents();
                }
                preEnvLayers.add(logUncaughtExceptionsOpen);
                preEnvLayers.add(new Runnable() {
                        public void run() {
                                if (!exitedJXA) {
                                        Runtime.getRuntime().addShutdownHook(new Thread(quitJXAEnv));
                                }
                        }
                });
                preEnvLayers.add(new Runnable() {
                        public void run() {
                                createLock();
                        }
                });
                preEnvLayers.add(envLWJGLLayer);
                preEnvLayers.add(envJMFLayer);
                preEnvLayers.add(new Runnable() {
                        public void run() {
                                _setImageIOCacheEnabled(true, FileHelper._ImageIOCache);
                        }
                });
                /**
                 *
                 */
                postEnvLayers.add(envImageIOLayer);
                if (_debugSys) {
                        System.out.println(_JXAEnvOutput("loading JXA ENVIRONMENT...", SYS_NOTICE));
                }
                if (swingEdt) {
                        UIMessage.getProgressBar(jpb).setStringPainted(true);
                }
                boolean res = true;
                int i = 0;
                if (swingEdt) {
                        UIMessage.getProgressBar(jpb).setString("Loading JXA pre-Environment...");
                }
                synchronized (preEnvLayers) {
                        for (Runnable a : preEnvLayers) {
                                a.run();
                                if (swingEdt) {
                                        UIMessage.updateProgress(jpb, ++i, preEnvLayers.size());
                                }
                        }
                }
                if (swingEdt) {
                        UIMessage.getProgressBar(jpb).setString("Loading JXA Environment...");
                }
                i = 0;
                try {
                        if (!swingEdt) {
                                ExtensionsInstaller.showSplash = false;
                        }
                        /**
                         * CLASS PATH INSTALLS HERE
                         */
                        /**
                         * copy .jar extension into envPath (this)
                         */
                        if (!keepReadingOnRemoteJarResources) {
                                ExtensionsInstaller._installExtensions(this, envJars, false, new File(FileHelper._USERHOMESTOREDIRECTORY, "" + antClassName));
                        }
                        res = isResourceLoaded() && res;
                        /**
                         * install (extract) natives into the libpath
                         */
                        res = ExtensionsInstaller._installExtensions(ExtensionsInstaller._getJXANatenvFiles(envNatives, false), true, null).isResourceLoaded() && res;
                } catch (MalformedURLException ex) {
                        if (_debugSys) {
                                ex.printStackTrace();
                        }
                } catch (URISyntaxException ex) {
                        ex.printStackTrace();
                } finally {
                        if (swingEdt) {
                                UIMessage.getProgressBar(jpb).setString("Loading JXA post-Environment...");
                        }
                        i = 0;
                        synchronized (postEnvLayers) {
                                for (Runnable a : postEnvLayers) {
                                        a.run();
                                        if (swingEdt) {
                                                UIMessage.updateProgress(jpb, ++i, postEnvLayers.size());
                                        }
                                }
                        }
                        if (_debugSys) {
                                System.out.println(_JXAEnvOutput("JXA ENVIRONMENT " + (res ? "is loaded." : " had errors on loading."), SYS_NOTICE));
                        }
                        if (swingEdt) {
                                UIMessage.getProgressBar(jpb).setString("JXA Environment " + (res ? "is loaded." : " had errors on loading."));
                        }
                }
        }
        ArrayList<Runnable> preEnvLayers = new ArrayList<Runnable>();
        ArrayList<Runnable> postEnvLayers = new ArrayList<Runnable>();

        /**
         *
         * @param a
         */
        public void addPreEnvLayer(Runnable a) {
                preEnvLayers.add(a);
        }

        /**
         *
         * @param a
         */
        public void removePreEnvLayer(Runnable a) {
                preEnvLayers.remove(a);
        }

        /**
         *
         * @param a
         */
        public void addPostEnvLayer(Runnable a) {
                postEnvLayers.add(a);
        }

        /**
         *
         * @param a
         */
        public void removePostEnvLayer(Runnable a) {
                postEnvLayers.remove(a);
        }
        ArrayList<Runnable> preUEnvLayers = new ArrayList<Runnable>();
        ArrayList<Runnable> postUEnvLayers = new ArrayList<Runnable>();

        /**
         *
         * @param a
         */
        public void addPreUEnvLayer(Runnable a) {
                preUEnvLayers.add(a);
        }

        /**
         *
         * @param a
         */
        public void removePreUEnvLayer(Runnable a) {
                preUEnvLayers.remove(a);
        }

        /**
         *
         * @param a
         */
        public void addPostUEnvLayer(Runnable a) {
                postUEnvLayers.add(a);
        }

        /**
         *
         * @param a
         */
        public void removePostUEnvLayer(Runnable a) {
                postUEnvLayers.remove(a);
        }

        /**
         *
         * @return
         */
        public String[] getLibraryPathArray() {
                return libraryPath;
        }
        /**
         *
         */
        public static File fileLock = new File(FileHelper._USERHOMESTOREDIRECTORY, "lock");
        private static boolean startedJXA = false;

        private void createLock() {
                if (startedJXA) {
                        return;
                } else {
                        startedJXA = true;
                }
                if (antClassName == null) {
                        return;
                }
                System.out.println(_JXAEnvOutput("JXAKernel v. " + JXAenvUtils._kernelVersion(), SYS_NOTICE));
                try {
                        if (!fileLock.exists()) {
                                fileLock.createNewFile();
                        }
                        RandomAccessFile raf = new RandomAccessFile(fileLock, "rw");
                        raf.writeLong(System.currentTimeMillis());
                        raf.writeInt(antClassName.hashCode());
                        raf.close();
                        if (!verifyLock(true)) {
                                UIMessage.showLightPopupMessage(
                                        new JLabel("<html>You seem to already run an instance of this program.<br>" + "If you are sure to continue, then close this popup;" + "<br> if unsure, then quit.</html>"), new AbstractAction("quit", UIMessage._getIcon(UIMessage.ABORT_TYPE, true)) {
                                                public void actionPerformed(ActionEvent e) {
                                                        System.exit(0);
                                                }
                                        }, null, UIMessage._BOTTOM_RIGHT);
                        }
                } catch (IOException ex) {
                        if (DebugMap._getInstance().isDebugLevelEnabled(DebugMap._getInstance()._VOID)) {
                                ex.printStackTrace();
                        }
                }
        }

        /**
         *
         */
        private Map<Long, Integer> getLocks() {
                Map<Long, Integer> locks = new LinkedHashMap<Long, Integer>();
                RandomAccessFile locksF = null;
                try {
                        locksF = new RandomAccessFile(fileLock, "r");
                        while (true) {
                                long lock = locksF.readLong();
                                int lockName = locksF.readInt();
                                if (System.currentTimeMillis() - lock < 24L * 3600L * 1000L) {
                                        locks.put(lock, lockName);
                                }
                        }
                } catch (IOException ex) {
                        if (DebugMap._getInstance().isDebugLevelEnabled(DebugMap._getInstance()._VOID)) {
                                System.err.println("EOF detected, OK, locks have been read.");
                                ex.printStackTrace();
                        }
                } finally {
                        return locks;
                }
        }

        /**
         * @return true if AT MOST one instance is detected
         * @param forEnvLoading AT MOST one instance of the same
         * {@linkplain #antClassName}
         */
        private boolean verifyLock(boolean sameClass) {
                if (getLocks().size() <= 1) {
                        return true;
                } else if (sameClass) {
                        Map<Long, Integer> locks = getLocks();
                        int found = 0;
                        for (Iterator<Long> it = locks.keySet().iterator(); it.hasNext();) {
                                if (locks.get(it.next()).equals(antClassName.hashCode())) {
                                        found++;
                                }
                        }
                        return found <= 1;
                } else {
                        return false;
                }
        }

        /**
         *
         */
        private void releaseLock() {
                if (antClassName == null) {
                        return;
                }
                Map<Long, Integer> locks = getLocks();
                RandomAccessFile locksF = null;
                try {
                        File tmp = _createTempFile("lock_", FileHelper._USERHOMESTOREDIRECTORY);
                        locksF = new RandomAccessFile(tmp, "rw");
                        for (Long lck : locks.keySet()) {
                                if (!locks.get(lck).equals(antClassName.hashCode())) {
                                        locksF.writeLong(lck);
                                        locksF.writeInt(locks.get(lck));
                                }
                        }
                        locksF.close();
                        _fileCopy(tmp, fileLock, false, false);
                        tmp.delete();
                } catch (IOException ex) {
                        if (DebugMap._getInstance().isDebugLevelEnabled(DebugMap._getInstance()._VOID)) {
                                ex.printStackTrace();
                        }
                }
        }

        /**
         * completely erase a path (directory or file)
         *
         * @param path
         * @deprecated Moved to
         * {@link net.sf.jiga.xtended.kernel.FileHelper#_erase}
         */
        @Deprecated
        public static void _erase(File path) {
                FileHelper._erase(path);
        }

        /**
         * completely erase a path (directory or file)
         *
         * @param path
         * @param reportException
         * @throws IOException
         * @deprecated Moved to
         * {@link net.sf.jiga.xtended.kernel.FileHelper#_erase}
         */
        @Deprecated
        public static void _erase(File path, boolean reportException) throws IOException {
                FileHelper._erase(path, reportException);
        }

        /**
         * CAUTION : this function will attempt to delete all files in the
         * specified directory (but not the sub-dirs)
         *
         * @param suffix the suffix for filenames (".[a-z|0-9]*")
         * @param dir
         * @throws IllegalArgumentException
         * @deprecated Moved to
         * {@link net.sf.jiga.xtended.kernel.FileHelper#_eraseTmpFiles}
         */
        @Deprecated
        public static void _eraseTmpFiles(final String suffix, File dir) throws IllegalArgumentException {
                FileHelper._eraseTmpFiles(suffix, dir);
        }

        /**
         * will create a jxa temp file, that is monitored by the env (delete on
         * exit, file suffix, etc.)
         *
         * @param prefix
         * @param dir
         * @return
         * @throws IOException
         */
        @Deprecated
        public static File _createTempFile(String prefix, File dir) throws IOException {

                return FileHelper._createTempFile(prefix, dir, !JXAenvUtils.exiting);
        }
        private static Monitor envload = new Monitor();

        /**
         * disables all Swing EDT environment callbacks, because of shutdown
         * hooks that occur on the EDT (e.g. User clicks exit() in
         * actionperformed())
         */
        private void _unloadEnvironment(boolean cleanTemp) {
                boolean swingEdt = ThreadWorks.Swing.isEventDispatchThread();
                addPostUEnvLayer(logUncaughtExceptionsClose);
                if (swingEdt) {
                        UIMessage.getProgressBar(jpb).setStringPainted(true);
                }
                int i = 0;
                if (swingEdt) {
                        UIMessage.getProgressBar(jpb).setString("Unloading JXA pre-Environment...");
                }
                synchronized (preUEnvLayers) {
                        for (Runnable a : preUEnvLayers) {
                                a.run();
                                if (swingEdt) {
                                        UIMessage.updateProgress(jpb, ++i, preUEnvLayers.size());
                                }
                        }
                }

                try {
                        synchronized (envload) {
                                while (exiting) {
                                        envload.wait();
                                }
                                if (!exitedJXA) {
                                        exiting = true;
                                        ExtensionsInstaller.showSplash = false;
                                        /*
                                         * erase .jar from the (extensions) envpath
                                         */
                                        ExtensionsInstaller._uninstallExtensions(this, envJars, false, new File(FileHelper._USERHOMESTOREDIRECTORY, "" + antClassName));
                                        /**
                                         * common to all JXA applications
                                         */
                                        boolean mustExit = verifyLock(false);
                                        if (mustExit) {
                                                if (swingEdt) {
                                                        UIMessage.getProgressBar(jpb).setString("Exiting JXA environment (1-2 mn.)");
                                                }
                                                /*
                                                 * erase natives from the library.path
                                                 */
                                                ExtensionsInstaller._uninstallExtensions(envNatives, true, null);
                                                if (cleanTemp) {
                                                        SpritesCacheManager._cleanFileSwap();
                                                        for (File t : FileHelper._tmpDir) {
                                                                _eraseTmpFiles(_tmpFilesSuffix, t);
                                                        }
                                                }
                                        }
                                }
                        }
                } catch (Exception ex) {
                        if (_debugSys) {
                                ex.printStackTrace();
                        }
                } finally {
                        if (swingEdt) {
                                UIMessage.getProgressBar(jpb).setString("Done.");
                        }
                        synchronized (envload) {
                                releaseLock();
                                exitedJXA = true;
                                exiting = false;
                                envload.notifyAll();
                        }
                        if (swingEdt) {
                                UIMessage.getProgressBar(jpb).setString("Unloading JXA post-Environment...");
                        }
                        i = 0;
                        /**
                         * shutdown hooks
                         */
                        synchronized (postUEnvLayers) {
                                for (Runnable a : postUEnvLayers) {
                                        a.run();
                                        if (swingEdt) {
                                                UIMessage.updateProgress(jpb, ++i, postUEnvLayers.size());
                                        }
                                }
                        }
                        if (swingEdt) {
                                UIMessage.getProgressBar(jpb).setString("Done.");
                        }
                }
        }

        /**
         *
         * @return
         */
        public static String getLibraryPath() {
                return AccessController.doPrivileged(new PrivilegedAction<String>() {
                        public String run() {
                                return java.lang.management.ManagementFactory.getRuntimeMXBean().getLibraryPath();
                        }
                }, acc);
        }
        String[] classpath = getClasspath().split(File.pathSeparator);

        /**
         *
         * @return {@link #getClasspath()} as a list of paths to the current
         * classpath (-cp or -classpath,...).
         */
        public String[] getClasspathArray() {
                return classpath;
        }

        /**
         *
         * @return
         */
        public static String getClasspath() {
                return AccessController.doPrivileged(new PrivilegedAction<String>() {
                        public String run() {
                                return java.lang.management.ManagementFactory.getRuntimeMXBean().getClassPath();
                        }
                }, acc);
        }
        /**
         * the multi-threading switch. can be disabled with the option
         * jxa.nomt=true at startup
         *
         * @default true or false, whether the system detects multithreaded cpu
         */
        public static boolean _multiThreading = Runtime.getRuntime().availableProcessors() > 1 && !_getSysBoolean("jxa.nomt");

        /**
         * returns the current JXA environment path used as the loading folder
         * (+ subDir)
         *
         * @return the current env Path used as the loading folder
         * @see #rb
         */
        public String getJXAenvPath() {
                return envPath;
        }

        /**
         * sets up the environment path. usually an unique path for each
         * application would include the {@link #antClassName} string.
         *
         * @param path
         * @see #getJXAenvPath()
         */
        public void setJXAenvPath(String path) {
                envPath = path;
        }

        /**
         * updates the current path with the specified path if necessary.
         *
         * @param currentPath the String of the current path denotes a list of
         * paths separated by the {@linkplain File#pathSeparator}.
         * @param path the path to add (must end with a "/")
         * @return the updated path
         */
        public static String _updatePath(String currentPath, String path) {
                return _updatePath(currentPath, path, false);
        }

        /**
         * updates the current path with the specified path if necessary.
         *
         * @param currentPath the String of the current path denotes a list of
         * paths separated by the {@linkplain File#pathSeparator}.
         * @param path the path to add (must end with a "/")
         * @param prepend pre-"append" the specified path (thus classloaders
         * will look first in the new path) BE CAREFUL WITH THIS SETTING
         * @return the updated path
         */
        public static String _updatePath(String currentPath, String path, boolean prepend) {
                if (path instanceof String) {
                        if (path.endsWith(".")) {
                                path = path.substring(0, path.lastIndexOf("."));
                        }
                }
                boolean found = false;
                String separator;
                if (currentPath == null) {
                        currentPath = "";
                        separator = "";
                } else {
                        separator = File.pathSeparator;
                }
                for (String regPath : currentPath.split(File.pathSeparator)) {
                        if (regPath instanceof String) {
                                if (regPath.equalsIgnoreCase(path)) {
                                        found = true;
                                        break;
                                }
                        }
                }
                if (!found) {
                        if (prepend) {
                                currentPath = path + separator + currentPath;
                        } else {
                                currentPath += separator + path;
                        }
                }
                return currentPath;
        }

        @Override
        protected Object clone() throws CloneNotSupportedException {
                JXAenvUtils env = (JXAenvUtils) super.clone();
                env.classLoader = classLoader;
                env.classpath = new String[classpath.length];
                for (int i = 0; i < classpath.length; i++) {
                        env.classpath[i] = classpath[i];
                }
                env.libraryPath = new String[libraryPath.length];
                for (int i = 0; i < libraryPath.length; i++) {
                        env.libraryPath[i] = libraryPath[i];
                }
                /*
                 * env.loadedClassHierarchy = new ArrayList<String>(loadedClassHierarchy);
                 */
                env.map = Collections.synchronizedMap(new HashMap<String, URL>(map));
                env.postEnvLayers = new ArrayList<Runnable>(postEnvLayers);
                env.preEnvLayers = new ArrayList<Runnable>(preEnvLayers);
                env.envJars = Collections.synchronizedMap(new LinkedHashMap<String, Map<String, URL>>(envJars));
                env.envNatives = Collections.synchronizedMap(new LinkedHashMap<String, Map<String, Map<URL, Boolean>>>(envNatives));
                env.erroredFiles = Collections.synchronizedSet(new HashSet<String>(erroredFiles));
                return env;
        }

        /**
         * creates a new instance
         *
         * @param classLoader the class to use as ClassLoader
         * @param filesMap the map of files to load
         * @param envSubDir
         * @see #JXAenvUtils(Class, Map)
         */
        public JXAenvUtils(Class classLoader, Map<String, URL> filesMap) {
                this(classLoader.getClassLoader(), filesMap);
        }

        /**
         * name that can be used as the Logger class.
         */
        public final static String LOGGER_NAME = "jxa.logger";

        /**
         * creates a new instance.
         *
         * @param classLoader the class to use as ClassLoader
         * @param filesMap the map of files to load
         */
        public JXAenvUtils(ClassLoader classLoader, Map<String, URL> filesMap) {
                /**
                 * TODO : nothing for localized Logger resourcebundle, yet
                 */
                super(LOGGER_NAME, null);
                this.classLoader = classLoader;
                map.putAll(filesMap);
        }

        /**
         * @return id of the JProgressBar that is associated to this JXAenvUtils
         * instance (loading of libraries is logged with it). This id can be
         * used to get the bar with UIMessage.
         */
        public long getUIMessageProgressBar() {
                return jpb;
        }

        /**
         * creates a new instance
         *
         * @param classLoader the class to use as ClassLoader
         * @param envSubDir
         * @see #JXAenvUtils(Class, Map, String)
         */
        public JXAenvUtils(Class classLoader) {
                this(classLoader.getClassLoader());
        }

        /**
         * creates a new instance
         *
         * @param classLoader the class to use as ClassLoader
         * @param envSubDir
         * @see #JXAenvUtils(Class, Map, String)
         */
        public JXAenvUtils(ClassLoader classLoader) {
                this(classLoader, new HashMap<String, URL>());
        }

        /**
         * usses ExtensionsClassLoader as the classLoader for resources
         */
        public JXAenvUtils() {
                this(ClassLoader.getSystemClassLoader());
        }

        /**
         * Usually, new File("/filename") returns "\filename" as a path on
         * Windows and "//filename" on Unix, this method converts the abstract
         * path name from the specified File to a resource String (i.e.
         * {@linkplain Class#getResourceAsStream(String)} can handle it).
         *
         * @param filePath
         * @return
         * @deprecated Moved to
         * {@link net.sf.jiga.xtended.kernel.FileHelper#_convertToResourceString}
         */
        @Deprecated
        public static String _convertToResourceString(File filePath) {
                return FileHelper._convertToResourceString(filePath);
        }

        /**
         * {@linkplain String#split(String)} won't fail on Windows with the
         * File.separator as splitter ex. The method computes as follows :
         * <pre>String splitter = File.separator;
         * if(splitter.equals("\\")) {
         * splitter = "\\\\";
         * }
         * return splitter;</pre>
         *
         * @return
         * @deprecated Moved to
         * {@link net.sf.jiga.xtended.kernel.FileHelper#_quotedFileSeparator}
         */
        @Deprecated
        public static String _quotedFileSeparator() {
                return FileHelper._quotedFileSeparator();
        }
        private boolean forceInstall = false;

        /**
         * @return @see #setForceInstall(boolean)
         */
        public boolean isForceInstall() {
                return forceInstall;
        }

        /**
         * force overwriting existing files
         *
         * @param forceInstall
         * @default false
         */
        public void setForceInstall(boolean forceInstall) {
                this.forceInstall = forceInstall;
        }
        /*
         * private ArrayList<String> loadedClassHierarchy = new ArrayList<String>();
         */

        /**
         * a pattern that would match a JAR-Entry name of class that will be
         * loaded by {@linkplain #loadEnvironment()} at startup of JFCApplet^s
         *
         * @param classPattern defines the pattern of some class name resource
         * to load (e.g. "net/sf/jiga/xtended/.*" to load the JXA packages)
         * @see #isLoadedHierarchyMemberClass(String)
         *
         * public void addLoadedClassHierarchy(String classPattern) {
         * loadedClassHierarchy.add(classPattern); }
         */
        /**
         * *
         * public void clearLoadedClassHierarchy() {
         * loadedClassHierarchy.clear(); }
         */
        /**
         * @see String#matches(String)* public boolean
         * isLoadedHierarchyMemberClass(String name) { synchronized
         * (loadedClassHierarchy) { for (String pattern : loadedClassHierarchy)
         * { if (name.matches(pattern)) { return true; } } } return false; }
         */
        /**
         * loads the specified file in the environment. it copies the file in
         * the JXAenvPath. if load is set to false, System.load(String) won't be
         * executed and let it available to further calls (it is sometimes
         * required by some librairies loaded internally).
         *
         * @param filename the library filename to load
         * @see #getJXAenvPath()
         * @throws Exception if an error occurred while loading
         */
        private boolean loadEnvFile(final URL filePath) {
                return AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
                        public Boolean run() {
                                return _loadEnvFile(filePath);
                        }
                }, acc);
        }
        public boolean keepFolderHierarchy = false;

        /**
         * @default false
         * @param keepFolderHierarchy
         * @deprecated {@link #keepFolderHierarchy}
         */
        public void setKeepFolderHierarchy(boolean keepFolderHierarchy) {
                this.keepFolderHierarchy = keepFolderHierarchy;
        }

        /**
         * @default false
         * @return
         * @deprecated {@link #keepFolderHierarchy}
         */
        public boolean isKeepFolderHierarchy() {
                return keepFolderHierarchy;
        }

        /**
         * use {@link FileHelper#_BIGBUFFER_SIZE} for buffered file transfer
         */
        public boolean bigBuffer = true;

        /**
         * loads the specified file in the environment. it copies the file in
         * the JXAenvPath. if load is set to false, System.load(String) won't be
         * executed and let it available to further calls (it is sometimes
         * required by some librairies loaded internally).
         *
         * @param filename the library filename to load
         * @see #getJXAenvPath()
         * @throws Exception if an error occurred while loading
         */
        private boolean _loadEnvFile(URL filePath) {
                if (jpb != 0) {
                        UIMessage.getProgressBar(jpb).setIndeterminate(true);
                }
                final File fileLink = findEnvInstalledFile(filePath);
                String dir = fileLink.getParentFile().getAbsolutePath();
                String file = fileLink.getAbsolutePath();
                int pty = Thread.currentThread().getPriority();
                Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
                try {
                        if (JXAenvUtils._debugSys) {
                                System.out.print("looking for installing file " + filePath + "...");
                        }
                        URLConnection connection = filePath.openConnection();
                        InputStream src = connection.getInputStream();
                        if (src instanceof InputStream ? src.available() > 0 : false) {
                                if (JXAenvUtils._debugSys) {
                                        System.out.println(" found " + FileUtils.byteCountToDisplaySize(connection.getContentLength()));
                                }
                        } else {
                                if (JXAenvUtils._debugSys) {
                                        System.out.println(" not found.");
                                }
                                throw new FileNotFoundException("File " + filePath + " was not found.");
                        }
                        src.close();
                        File f, d;
                        if (!(d = new File(dir)).exists()) {
                                d.mkdirs();
                                _makeWritable(d);
                        }
                        f = new File(file);
                        if (JXAenvUtils._debugSys) {
                                System.out.print("Comparing with existing " + f + "...");
                        }
                        boolean install = forceInstall || !f.exists();
                        if (!install) {
                                try {
                                        URLConnection ucheck = null;
                                        long sum = 0;
                                        try {
                                                /**
                                                 * if any filepath.MD5 file
                                                 * exists, read the long
                                                 * checksum
                                                 */
                                                ucheck = new URL(filePath + ".MD5").openConnection();
                                                int l = ucheck.getContentLength();
                                                InputStream bi = ucheck.getInputStream();
                                                if (l == 0) {
                                                        l = bi.available();
                                                }
                                                ByteBuffer sb = ByteBuffer.allocate(l).order(ByteOrder.nativeOrder());
                                                byte[] b = new byte[FileHelper._BIGBUFFER_SIZE];
                                                int r = 0;
                                                while ((r = bi.read(b)) != -1) {
                                                        sb.put(b, 0, r);
                                                }
                                                bi.close();
                                                sum = MD5Checksum.decodeLong(sb.array());
                                        } catch (IOException ex) {
                                                if (DebugMap._getInstance().isDebugLevelEnabled(DebugMap._getInstance()._VOID)) {
                                                        ex.printStackTrace();
                                                }
                                                /**
                                                 * if no .MD5, compute content
                                                 * checksum
                                                 */
                                                connection = filePath.openConnection();
                                                src = connection.getInputStream();
                                                MD5Checksum checksum = new MD5Checksum();
                                                byte[] br = new byte[FileHelper._BIGBUFFER_SIZE];
                                                int r = 0;
                                                while ((r = src.read(br)) != -1) {
                                                        checksum.update(br, 0, r);
                                                }
                                                src.close();
                                                sum = checksum.getValue();
                                        } finally {
                                                install = sum != FileUtils.checksum(f, (Checksum) new MD5Checksum()).getValue();
                                        }
                                } catch (IOException ex) {
                                        if (DebugMap._getInstance().isDebugLevelEnabled(DebugMap._getInstance()._VOID)) {
                                                ex.printStackTrace();
                                        }
                                        connection = filePath.openConnection();
                                        src = connection.getInputStream();
                                        /**
                                         * if checksum read failed, make a file
                                         * length comparison
                                         */
                                        install = f.length() != (connection.getContentLength() == -1 ? src.available() : connection.getContentLength());
                                }
                        }
                        if (JXAenvUtils._debugSys) {
                                System.out.println(install ? " REQUIRES UPDATE" : " REQUIRES NO UPDATE");
                        }
                        if (install) {
                                _fileCopy(filePath, f, jpb != 0, jpb, bigBuffer);
                                if (JXAenvUtils._debugSys) {
                                        System.out.println("installed : " + file + " in " + dir);
                                }
                        }
                        /**
                         * RETURN STATEMENT HERE BECAUSE OF THE OTHER RETURN
                         * STATEMENT IN CATCH WOULD BE MASKED IF IN THE FINALLY
                         * BLOCK
                         */
                        return true;
                } catch (Exception e) {
                        if (DebugMap._getInstance().isDebugLevelEnabled(DebugMap._getInstance()._SYS)) {
                                System.out.println("error while installing : " + file);
                                e.printStackTrace();
                        }
                        return false;
                } finally {
                        Thread.currentThread().setPriority(pty);
                }
        }

        /**
         * dis/enable big byte buffer for file copies/downloads (65535) ENABLE
         * when file copies hang (in a small heap size environment such as the
         * Plug-in Extension Java installer process)
         *
         * @param bigBuffer
         */
        public void setBigBuffer(boolean bigBuffer) {
                this.bigBuffer = bigBuffer;
        }

        /**
         * @return @default true
         */
        public boolean isBigBuffer() {
                return bigBuffer;
        }

        /**
         * unloads/uninstalls the file from {@link #getJXAenvPath() JXAenvPath}.
         * it won't be available to the class classLoader anymore
         *
         * @param filename the file name
         * @see File#delete()
         * @throws Exception if an error occurred with the File
         */
        private void unloadEnvFile(final URL filePath) {
                AccessController.doPrivileged(new PrivilegedAction() {
                        public Object run() {
                                try {
                                        File f = findEnvInstalledFile(filePath);
                                        if (__accessFilePermitted(f, FileHelper.FILE_DELETE)) {
                                                f.delete();
                                        } else {
                                                if (!exiting) {
                                                        f.deleteOnExit();
                                                }
                                        }
                                } catch (Exception e) {
                                        if (DebugMap._getInstance().isDebugLevelEnabled(DebugMap._getInstance()._VOID)) {
                                                e.printStackTrace();
                                        }
                                } finally {
                                        return null;
                                }
                        }
                }, acc);
        }

        /**
         * @param id
         * @return the file name where the object can be located, otherwise an
         * JXAException is thrown (the object mustn't be loaded yet).
         */
        public File getEnvInstalledFile(String id) {
                URL u = map.get(id);
                if (u != null) {
                        return findEnvInstalledFile(u);
                } else {
                        throw new JXAException(JXAException.LEVEL.SYSTEM, "File " + id + " wasn't loaded yet");
                }
        }

        protected File findEnvInstalledFile(URL fileUrl) {
                String p = fileUrl.getFile();
                if (p.startsWith("/")) {
                        p = p.substring(1);
                }
                String fp = p.replaceAll("%20", " ");
                return new File(envPath + File.separator + (keepFolderHierarchy ? fp : new File(fp).getName()));
        }

        /**
         * return the file map of an object added with
         * {@link #addEnvFile(java.lang.String, java.net.URL)}
         *
         * @param id
         * @return
         */
        public URL getEnvSourceFile(String id) {
                return map.get(id);
        }

        /**
         * return the file map of objects added with
         * {@link #addEnvFile(java.lang.String, java.net.URL)}
         *
         * @return
         */
        public Map<String, URL> getEnvSourceFiles() {
                return map;
        }

        /**
         * * return the file map of the location where the JXAEnvUtils
         * {@link #loadAll() installed} (loadall, loadenvironmnent or
         * loadresource) objects added with
         * {@link #addEnvFile(java.lang.String, java.net.URL)}
         *
         * @return
         */
        public Map<String, File> getEnvInstalledFiles() {
                HashMap<String, File> fileMap = new HashMap<String, File>();
                Set<String> set = map.keySet();
                synchronized (map) {
                        for (String id : set) {
                                fileMap.put(id, getEnvInstalledFile(id));
                        }
                }
                return Collections.synchronizedMap(fileMap);
        }

        /**
         * silently unloads/uninstalls all registered files (with no splash)
         */
        public void unloadAll() {
                Set<String> set = map.keySet();
                synchronized (map) {
                        for (Iterator<String> i = set.iterator(); i.hasNext();) {
                                unloadEnvFile(map.get(i.next()));
                        }
                        loaded = LOAD_CLEARED;
                        erroredFiles.clear();
                }
        }

        /**
         * note: iclearResource() clears this set.
         *
         * @return set of id's to see what are the files that were not correctly
         * loaded
         */
        public Set<String> getErroredFiles() {
                return erroredFiles;
        }

        /**
         * adds a new file to the file map to load. subsequent calls to
         * load(boolean) or loadAll(boolean) will include it.
         *
         * @param id the file id you want
         * @param filePath the file name
         * @see #loadAll()
         * @see Map#put(Object, Object)
         */
        public void addEnvFile(String id, URL filePath) {
                if (JXAenvUtils._debugSys) {
                        System.out.println("added env File : " + id + " = " + filePath);
                        if (filePath == null) {
                                System.err.println(log(id + " was not found !! please check input.", LVL.SYS_ERR));
                        }
                }
                map.put(id, filePath);
        }

        /**
         * removes the file from the loading map
         *
         * @param id the file id to remove from map
         * @see Map#remove(Object)
         */
        public void removeEnvFile(String id) {
                if (JXAenvUtils._debugSys) {
                        System.out.println("removed env File : " + id);
                }
                map.remove(id);
        }

        /**
         * loads/installs the id-selected file into the JXA environment path.
         *
         * @param id lib name
         * @return
         * @see #loadEnvFile(URL)
         */
        public boolean load(String id) {
                if (JXAenvUtils._debugSys) {
                        System.out.println("loading " + id + " ...");
                }
                boolean b = false;
                loaded |= LOAD_LOADING;
                b = map.get(id) != null ? loadEnvFile(map.get(id)) : false;
                if (!b) {
                        if (JXAenvUtils._debugSys) {
                                System.err.println(_JXAEnvOutput(id + " " + map.get(id) + " has not been found or has failed to load", SYS_WARNING));
                        }
                }
                loaded &= ~LOAD_LOADING;
                if (!b) {
                        loaded |= LOAD_ERROR;
                        erroredFiles.add(id);
                }
                return b;
        }
        private Set<String> erroredFiles = Collections.synchronizedSet(new HashSet());

        /**
         * unloads/uninstalls the id-selected file from the JXA environment
         * path.
         *
         * @param id lib name
         * @see #loadEnvFile(URL)
         */
        public void unload(String id) {
                if (JXAenvUtils._debugSys) {
                        System.out.println("unloading " + id + " ...");
                }
                loaded |= LOAD_LOADING;
                unloadEnvFile(map.get(id));
                loaded &= ~LOAD_LOADING;
        }

        /**
         * silently loads/installs in all the mapped files into the JXA
         * environment path (no splash).
         *
         * @return
         */
        public boolean loadAll() {
                Set<String> set = map.keySet();
                try {
                        ArrayList<Thread> t = new ArrayList<Thread>();
                        synchronized (map) {
                                for (Iterator<String> i = set.iterator(); i.hasNext();) {
                                        final String lib = i.next();
                                        Runnable r = new Runnable() {
                                                public void run() {
                                                        load(lib);
                                                }
                                        };
                                        if (_multiThreading) {
                                                t.add(new Thread(r, "load-" + lib));
                                        } else {
                                                r.run();
                                        }
                                }
                        }
                        if (_multiThreading) {
                                for (Thread thr : t) {
                                        thr.start();
                                        thr.join();
                                }
                        }
                        if (JXAenvUtils._debugSys) {
                                System.out.println("load all libs done.");
                        }
                } catch (InterruptedException ex) {
                        if (JXAenvUtils._debugSys) {
                                ex.printStackTrace();
                        }
                } finally {
                        loaded &= ~LOAD_CLEARED;
                        loaded |= LOAD_LOADED;
                        return (loaded & LOAD_LOADED) != 0;
                }
        }

        /**
         *
         * @see #getErroredFiles()
         */
        public boolean hasLoadErrors() {
                return (loaded & LOAD_ERROR) != 0;
        }

        private void _initSplashContents() {
                if (_debugSys) {
                        System.out.println(log("Splash screen initializing...", LVL.SYS_NOT));
                }
                splashContents = new TransparentBackground(new GridBagLayout(), true);
                GridBagConstraints c = new GridBagConstraints();
                c.gridwidth = c.REMAINDER;
                c.fill = c.HORIZONTAL;
                jpb = UIMessage.newProgress(0, 0, splashContents);
                UIMessage.getProgressBar(jpb).setIndeterminate(true);
                UIMessage.getProgressBar(jpb).setString(null);
                UIMessage.getProgressBar(jpb).setStringPainted(false);
                splashContents.add(UIMessage.getProgressBar(jpb), c);
        }

        private void _initSplashFrame(GraphicsConfiguration gc) {
                splashFrame = new JFrame(gc);
                splashFrame.setUndecorated(true);
                splashFrame.setContentPane(splashContents);
                UIMessage._makeUncloseable(splashFrame);
                splashFrame.validate();
        }

        /**
         * shows a splash screen, either on the applet
         * {@link #setAntApplet(AntApplet) contents} or on a splash frame
         *
         * @see #setSplashPicture(DisplayInterface)
         * @see #hideSplash()
         */
        public void showSplash() {
                try {
                        UIMessage.invokeSwingAndReturn(new SwingStaticReturn() {
                                public Object run() {
                                        _showSplash();
                                        return null;
                                }
                        });
                } catch (Exception ex) {
                        if (_debugSys) {
                                ex.printStackTrace();
                        }
                }
        }

        private void _addSplashDisplay() {
                if (splashContents == null) {
                        _initSplashContents();
                }
                if (splash != null) {
                        if (_debugSys) {
                                System.out.println(log("A JComponent Display is added.", LVL.SYS_NOT));
                        }
                        GridBagConstraints c = new GridBagConstraints();
                        c.gridwidth = c.REMAINDER;
                        splashContents.add(splash.getJComponentDisplay(), c);
                        splashContents.validate();
                }
        }

        private void _showSplash() {
                _addSplashDisplay();
                if (antapplet instanceof AntApplet && antapplet.getApplet() != null) {
                        ((TransparentBackground) splashContents).setDisableTransparency(true);
                        antContents = antapplet.getApplet().getContentPane();
                        antapplet.getApplet().setContentPane(splashContents);
                        antapplet.getApplet().validate();
                        antapplet.getApplet().setVisible(true);
                        antapplet.getApplet().update(antapplet.getApplet().getGraphics());
                } else {
                        if (splashFrame == null) {
                                _initSplashFrame(_defaultGC);
                        }
                        ((TransparentBackground) splashContents).setDisableTransparency(false);
                        if (env.OS_LINUX.isEnv()) {
                                splashFrame.pack();
                                splashFrame.setLocationRelativeTo(null);
                                splashFrame.setExtendedState(JFrame.NORMAL);
                        } else {
                                /**
                                 * fix for : unshown window at loading with
                                 * JFCFrame.main() within EDT
                                 */
                                Dimension dim = new Dimension(_defaultGC.getDevice().getDisplayMode().getWidth(), _defaultGC.getDevice().getDisplayMode().getHeight());
                                splashContents.setPreferredSize(dim);
                                splashFrame.pack();
                                /**
                                 *
                                 */
                                splashFrame.setExtendedState(JFrame.MAXIMIZED_BOTH);
                        }
                        ((TransparentBackground) splashContents).updateBackground();
                        splashFrame.setAlwaysOnTop(true);
                        splashFrame.setVisible(true);
                        splashFrame.setAlwaysOnTop(false);
                        splashFrame.update(splashFrame.getGraphics());
                }
        }

        /**
         *
         */
        public void hideSplash() {

                try {
                        UIMessage.invokeSwingAndReturn(new SwingStaticReturn() {
                                public Object run() {
                                        _hideSplash();
                                        return null;
                                }
                        });
                } catch (Exception ex) {
                        if (_debugSys) {
                                ex.printStackTrace();
                        }
                }
        }

        private void _hideSplash() {
                if (antapplet instanceof AntApplet && antapplet.getApplet() != null) {
                        antapplet.getApplet().setContentPane(antContents);
                        antapplet.getApplet().validate();
                } else {
                        splashFrame.setVisible(false);
                }
        }
        AntApplet antapplet = null;
        Container antContents = null;

        /**
         *
         * @param ant
         */
        public void setAntApplet(AntApplet ant) {
                this.antapplet = ant;
        }
        /**
         *
         */
        public String antClassName = Ant.class.getName();
        /**
         *
         */
        public long jpb = 0;

        /**
         * when this class is garbage collected, it will try first to erase all
         * loaded file data
         *
         * @see #clearResource()
         */
        public void finalize() throws Throwable {
                if (ThreadWorks.Swing.isEventDispatchThread()) {
                        splashFrame.setVisible(false);
                        splashFrame.dispose();
                }
        }

        /**
         * @param splash
         */
        public void setSplashPicture(final DisplayInterface splash) {
                this.splash = splash;
        }

        /**
         * returns true or false whether the multi-threading is enabled or not,
         * resp.
         *
         * @return true or false
         * @see #setMultiThreadingEnabled(boolean)
         */
        public boolean isMultiThreadingEnabled() {
                return _multiThreading;
        }

        /**
         * dis/enables the multi-threading
         *
         * @param b dis/enables the multi-threading
         */
        public void setMultiThreadingEnabled(boolean b) {
                _multiThreading = b;
        }
        private final static BitStack bits = new BitStack();
        private final static int LOAD_ERROR = bits._newBitRange();
        private final static int LOAD_LOADING = bits._newBitRange();
        private final static int LOAD_CLEARED = bits._newBitRange();
        private final static int LOAD_LOADED = bits._newBitRange();
        int loaded = LOAD_CLEARED;

        /**
         * loads all registered environment files while displaying a splash
         * picture.
         *
         * @return
         * @see #clearResource()
         */
        public Object loadResource() {
                try {
                        showSplash();
                        loadAll();
                        Thread.sleep(3000);
                } catch (InterruptedException ex) {
                        if (JXAenvUtils._debugSys) {
                                ex.printStackTrace();
                        }
                } finally {
                        hideSplash();
                        return null;
                }
        }

        /**
         * @param b
         * @deprecated use {@linkplain #_debugSys}
         */
        public static void _setDebugEnabled(boolean b) {
                _debugSys = b;
        }

        /**
         * @return @deprecated use {@linkplain #_debugSys}
         */
        public static boolean _isDebugEnabled() {
                return _debugSys;
        }

        /**
         * files loaded in the envpath are erased if they were owned by this
         * instance and it clears all the loaded resources and mapped libraries
         *
         * @see #unloadEnvFile(java.net.URL)
         * @return null
         */
        public Object clearResource() {
                long unloading = UIMessage.displayWaiting("please wait...", null);
                unloadAll();
                try {
                        Thread.sleep(3000);
                } catch (InterruptedException ex) {
                        if (JXAenvUtils._debugSys) {
                                ex.printStackTrace();
                        }
                } finally {
                        UIMessage.kill(unloading);
                        return null;
                }
        }

        /**
         * returns true {@linkplain #loadResource()} has been called or false
         * when {@linkplain #clearResource()} has been called
         *
         * @return true
         * @see #hasLoadErrors()
         */
        public boolean isResourceLoaded() {
                return (loaded & LOAD_LOADED) != 0 && (loaded & LOAD_ERROR) == 0;
        }
        /*
         * static MemStatusToolkit memStat = null;
         */

        /**
         * JProgressBar instances that updates status of the memory usage
         *
         * @return MemStatusToolkit instance public static MemStatusToolkit
         * _getMemoryStatusTK() { if (memStat == null) { memStat = new
         * MemStatusToolkit(); } return memStat; }
         */
        /**
         * returns swap disk usage from SpritesCacheManager in bytes.
         *
         * @return
         * @see SpritesCacheManager#_getSwap_global_memory_usage()
         */
        public static long _getSwapUsage() {
                return SpritesCacheManager._getSwap_global_memory_usage();
        }

        /**
         * memory
         *
         * @return
         */
        public static MemoryMXBean _memoryBean() {
                return ManagementFactory.getMemoryMXBean();
        }

        /**
         * operating system
         *
         * @return
         */
        public static OperatingSystemMXBean _systemBean() {
                return ManagementFactory.getOperatingSystemMXBean();
        }

        /**
         * runtime system properties
         *
         * @return
         */
        public static RuntimeMXBean _runtimeBean() {
                return ManagementFactory.getRuntimeMXBean();
        }

        /**
         * @see RenderingScene#_getGraphicsRendererInfo(GraphicsConfiguration)
         * public static Map<String, String> _graphicsInfo() { return
         * RenderingScene._getGraphicsRendererInfo(GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration());
         * }
         */
        /**
         * callbacks to the specified function (target method must be
         * accessible(=public))
         *
         * @param method the method named to make the callback
         * @param target the targeted object by this method callback (uses a
         * Class to invoke on the static context)
         * @param args the Objects arguments of the method callback
         * @param clargs the Class arguments of the method callback
         * @return the returned instance of the called-back method
         * @throws NoSuchMethodException
         * @throws IllegalAccessException
         * @throws InvocationTargetException
         * @see Class#getMethod(String, Class[])
         * @see Method#invoke(Object, Object[])
         */
        public static Object _callback(String method, Object target, Object[] args, Class[] clargs) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
                Method m = null;
                if (target instanceof Class) {
                        m = ((Class) target).getMethod(method, clargs);
                } else {
                        m = target.getClass().getMethod(method, clargs);
                }
                m.setAccessible(true);
                if (target instanceof Class) {
                        return m.invoke(null, args);
                } else {
                        return m.invoke(target, args);
                }
        }

        /**
         * secure copy of src file to dst file (first a temp. file is created
         * and filled with the data)
         *
         * @param src
         * @param bigBuffer
         * @param progressBar
         * @param dst
         * @throws FileNotFoundException
         * @throws IOException
         * @deprecated Moved to
         * {@link net.sf.jiga.xtended.kernel.FileHelper#_fileCopy}
         */
        @Deprecated
        public static void _fileCopy(File src, File dst, boolean progressBar, boolean bigBuffer) throws FileNotFoundException, IOException {
                FileHelper._fileCopy(src, dst, progressBar, bigBuffer);
        }

        /**
         * secure copy of src file to dst file (first a temp. file is created
         * and filled with the data)
         *
         * @param src
         * @param dst
         * @param progressBar
         * @param bigBuffer
         * @throws FileNotFoundException
         * @throws IOException
         * @deprecated Moved to
         * {@link net.sf.jiga.xtended.kernel.FileHelper#_fileCopy}
         */
        @Deprecated
        public static void _fileCopy(InputStream src, File dst, boolean progressBar, boolean bigBuffer) throws FileNotFoundException, IOException {
                FileHelper._fileCopy(src, dst, progressBar, bigBuffer);
        }

        /**
         * secure copy of src file to dst file (first a temp. file is created
         * and filled with the data)
         *
         * @param src
         * @param dst
         * @param bigBuffer
         * @param progressBar
         * @throws FileNotFoundException
         * @throws IOException
         * @throws HttpException
         * @deprecated Moved to
         * {@link net.sf.jiga.xtended.kernel.FileHelper#_fileCopy}
         */
        @Deprecated
        public static void _fileCopy(URL src, File dst, boolean progressBar, boolean bigBuffer) throws FileNotFoundException, IOException, HttpException {
                FileHelper._fileCopy(src, dst, progressBar, bigBuffer);
        }

        /**
         * sets up the priority of the invoking task
         *
         * @see Thread#setPriority(int)
         * @param method the method named to make the callback
         * @param target the targeted object by this method callback
         * @param args the Objects arguments of the method callback
         * @param clargs the Class arguments of the method callback
         * @param level priority value from 0 to 9. (normal value is that one of
         * Thread.NORM_PRIORITY)
         * @return the returned instance of the called-back method
         * @throws IllegalAccessException
         * @throws InvocationTargetException
         * @throws NoSuchMethodException
         * @see #_callback(java.lang.String, java.lang.Object,
         * java.lang.Object[], java.lang.Class[])
         *
         */
        public static Object _doPriority(String method, Object target, Object[] args, Class[] clargs, int level) throws IllegalAccessException, InvocationTargetException, NoSuchMethodException {
                int currentPty = Thread.currentThread().getPriority();
                Thread.currentThread().setPriority(level);
                Object result = _callback(method, target, args, clargs);
                Thread.currentThread().setPriority(currentPty);
                return result;
        }

        /**
         * secure copy of src file to dst file (first a temp. file is created
         * and filled with the data)
         *
         * @param src
         * @param dst
         * @param progressBar
         * @param progressBarID
         * @param bigBuffer
         * @throws IOException
         * @deprecated Moved to
         * {@link net.sf.jiga.xtended.kernel.FileHelper#_fileCopy}
         */
        @Deprecated
        public static void _fileCopy(final File src, final File dst, final boolean progressBar, final long progressBarID, final boolean bigBuffer) throws IOException {
                FileHelper._fileCopy(src, dst, progressBar, progressBarID, bigBuffer);
        }

        /**
         * secure copy of src file to dst file (first a temp. file is created
         * and filled with the data)
         *
         * @param src an InputStream, after the copy, the inputstream is left at
         * the last read postion and not closed UNLESS it is a
         * BufferedInputStream (so don't specify any such stream if you don't
         * want it to be closed !)
         * @param dst
         * @param progressBar
         * @param progressBarID
         * @param bigBuffer
         * @throws IOException
         * @deprecated Moved to
         * {@link net.sf.jiga.xtended.kernel.FileHelper#_fileCopy}
         */
        @Deprecated
        public static void _fileCopy(final InputStream src, final File dst, final boolean progressBar, final long progressBarID, final boolean bigBuffer) throws IOException {
                FileHelper._fileCopy(src, dst, progressBar, progressBarID, bigBuffer);
        }

        /**
         * secure copy of src file to dst file (first a temp. file is created
         * and filled with the data)
         *
         * @param src
         * @param dst
         * @param progressBarID
         * @param progressBar
         * @param bigBuffer
         * @throws IOException
         * @deprecated Moved to
         * {@link net.sf.jiga.xtended.kernel.FileHelper#_fileCopy}
         */
        @Deprecated
        public static void _fileCopy(final URL src, final File dst, final boolean progressBar, final long progressBarID, final boolean bigBuffer) throws IOException {
                FileHelper._fileCopy(src, dst, progressBar, progressBarID, bigBuffer);

        }

        /**
         * pops up a dialog box with the Throwable StackTrace
         *
         * @param modal if true, wait for the user to close the pop up or
         * continue without any user action, resp.
         * @param t the Thread that may have caught the exception
         * @param ex the exception to inspect the stackTrace of
         */
        public static void _popExceptionToUser(boolean modal, Thread t, Throwable ex) {
                ThreadWorks._uncaughtException(modal, t, ex);
        }

        /**
         * runs an -exernal program- into the OS shell (Op. System dependent),
         * such as an internet browser.
         *
         * @param cmd the command splitted into an array where each cell is a
         * token
         * @param confirm dis/enable confirm/verify the command before launching
         * @param waitForFinish dis/enables returning the waitForFinish of the
         * run command, i.e. the Thread will wait until the command terminates
         * gracefully
         * @param log enables logging of the run command
         * @return
         */
        public static Console _runShell(final String[] cmd, final boolean confirm, final boolean waitForFinish, final boolean log) {
                return _runShell(cmd, null, confirm, waitForFinish, log);
        }

        /**
         * runs an -exernal program- into the OS shell (Op. System dependent),
         * such as an internet browser.
         *
         * @param cmd the command splitted into an array where each cell is a
         * token
         * @param confirm dis/enable confirm/verify the command before launching
         * @param waitForFinish dis/enables returning the waitForFinish of the
         * run command, i.e. the Thread will wait until the command terminates
         * gracefully
         * @param log enables logging of the run command
         * @param cwd current working directory, or null to let the current JVM
         * gives it
         * @return
         */
        public static Console _runShell(final String[] cmd, final File cwd, final boolean confirm, final boolean waitForFinish, final boolean log) {
                return AccessController.doPrivileged(new PrivilegedAction<Console>() {
                        public Console run() {
                                return __runShell(cmd, cwd, confirm, waitForFinish, log);
                        }
                }, acc);
        }

        /**
         * runs an -exernal program- into the OS shell (Op. System dependent),
         * such as an internet browser.
         *
         * @param cmd the command splitted into an array where each cell is a
         * token
         * @param confirm dis/enable confirm/verify the command before launching
         * @param waitForFinish dis/enables returning the waitForFinish of the
         * run command, i.e. the Thread will wait until the command terminates
         * gracefully
         * @param log enables logging of the run command (notice : it cannot log
         * if the current thread exits the JVM after this call.)
         * @param cwd current working directory, or null to let the current JVM
         * gives it
         */
        private static Console __runShell(String[] cmd, File cwd, boolean confirm, boolean waitForFinish, boolean log) {
                long frame = UIMessage.displayWaiting("Loading...", null);
                Thread t;
                Console console = new Console();
                String[] shell = new String[]{};
                File shellFile = FileHelper._TMPDIRECTORY.getAbsoluteFile();
                String commandline = "";
                Process p = null;
                try {
                        if (env.OS_WINDOWS.isEnv()) {
                                shellFile = new File(shellFile, "run-tmp.bat");
                                shell = new String[]{"cmd.exe", "/C", "call", shellFile.getAbsolutePath()};
                                commandline += "@echo off" + Console.newLine;
                        } else if (env._hasEnv(env.OS_LINUX.bitMask() | env.OS_MAC.bitMask())) {
                                shellFile = new File(shellFile, ".run");
                                shell = new String[]{"/bin/sh", shellFile.getAbsolutePath()};
                        } else {
                                new UIMessage(true, JXAenvUtils._getSysValue("os.name") + " operating systems are currently not supported. Program is going to exit...", null);
                                return console;
                        }
                        for (String s : cmd) {
                                commandline += s + " ";
                        }
                        String shellString = "";
                        for (String sh : shell) {
                                shellString += " " + sh;
                        }
                        String msg = "LAUNCH :" + shellString + " >> " + commandline;
                        if (_debugSys) {
                                System.out.println(msg);
                        }
                        if (!shellFile.exists()) {
                                shellFile.createNewFile();
                        }
                        RandomAccessFile raf = new RandomAccessFile(shellFile, "rw");
                        raf.setLength(commandline.length());
                        Charset cs = env.OS_WINDOWS.isEnv() ? Charset.forName("US-ASCII") : Charset.forName("UTF-16");
                        raf.write(commandline.getBytes(cs));
                        raf.close();
                        final String MSG = msg;
                        JPanel panel = new JPanel(true);
                        panel.setLayout(new BorderLayout());
                        panel.add(new JLabel("Do run the command ?"), BorderLayout.CENTER);
                        panel.add(new JButton(new AbstractAction("show command...", UIMessage._getIcon(UIMessage.TERMINAL_TYPE, true)) {
                                public void actionPerformed(ActionEvent e) {
                                        new UIMessage(true, MSG, null);
                                }
                        }), BorderLayout.SOUTH);
                        UIMessage.hideProgress(frame);
                        if (confirm) {
                                if (UIMessage.showConfirmDialog(UIMessage._getDisplayFrame(frame), panel, "continue ?", JOptionPane.OK_CANCEL_OPTION) == JOptionPane.CANCEL_OPTION) {
                                        return console;
                                }
                        }
                        ArrayList<String> pbCmd = new ArrayList<String>();
                        for (String c : shell) {
                                pbCmd.add(c);
                        }
                        ProcessBuilder pb = new ProcessBuilder(pbCmd);
                        pb.directory(cwd);
                        p = pb.start();
                        /*
                         * popup = Runtime.getRuntime().exec(shell);
                         */
                        UIMessage.showProgress(frame);
                        if (log) {
                                Thread t_err = console.getNewInput(p.getErrorStream(), "%S");
                                Thread t_in = console.getNewInput(p.getInputStream());
                                t_err.setDaemon(true);
                                t_in.setDaemon(true);
                                t_err.start();
                                t_in.start();
                        }
                        try {
                                if (waitForFinish) {
                                        final Process ps = p;
                                        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                                                public void run() {
                                                        ps.destroy();
                                                }
                                        }));
                                        p.waitFor();
                                        if (log || p.exitValue() != 0) {
                                                new UIMessage(true, commandline + " has exited with status " + p.exitValue() + " (0 for normal termination)", null, (p.exitValue() == 0) ? UIMessage.INFO_TYPE : UIMessage.ERROR_TYPE);
                                        }
                                }
                        } catch (InterruptedException ex) {
                                if (JXAenvUtils._debugSys) {
                                        ex.printStackTrace();
                                }
                        }
                } catch (Exception ex) {
                        if (JXAenvUtils._debugSys) {
                                ex.printStackTrace();
                        }
                } finally {

                        UIMessage.kill(frame);
                        if (shellFile.exists()) {
                                if (JXAenvUtils._debugSys) {
                                        shellFile.deleteOnExit();
                                } else {
                                        shellFile.delete();
                                }
                        }
                        return console;
                }
        }

        /**
         * returns the java executable path for the current operating system and
         * JVM Remember tp enclose it with \\" if you run a shell !
         *
         * @return
         */
        public static String _getJavaExecutablePath() {
                return AccessController.doPrivileged(new PrivilegedAction<String>() {
                        public String run() {
                                return __getJavaExecutablePath();
                        }
                }, acc);
        }

        /**
         * returns the java executable path for the current operating system and
         * JVM Remember tp enclose it with \\" if you run a shell !
         */
        private static String __getJavaExecutablePath() {
                String javaHome = JXAenvUtils._getSysValue("java.home");
                if (env._hasEnv(env.OS_LINUX.bitMask() | env.OS_MAC.bitMask())) {
                        javaHome += File.separator + "bin" + File.separator + "java";
                } else if (env.OS_WINDOWS.isEnv()) {
                        javaHome += File.separator + "bin" + File.separator + "java.exe";
                }
                return javaHome;
        }

        /**
         * runs the default browser with the specified URL and returns a JPanel
         * that can show the opened URL in case the shell command won't run.
         *
         * @param url
         * @return
         */
        public static JPanel _goWeb(final URL url) {
                return AccessController.doPrivileged(new PrivilegedAction<JPanel>() {
                        public JPanel run() {
                                return __goWeb(url);
                        }
                }, acc);
        }

        /**
         * runs the default browser with the specified URL and returns a JPanel
         * that can show the opened URL in case the shell command won't run.
         */
        private static JPanel __goWeb(URL url) {
                String[] run = new String[]{};
                if (env.OS_WINDOWS.isEnv()) {
                        run = new String[]{"start", "\"STARTING THE BROWSER\"", "\"" + url.toString() + "\""};
                } else if (env.OS_LINUX.isEnv()) {
                        run = new String[]{"firefox", "\"" + url.toString() + "\""};
                } else {
                        run = new String[]{"open", "\"" + url.toString() + "\""};
                }
                _runShell(run, false, false, true);
                JPanel panel = new JPanel(new BorderLayout(), true);
                final JTextField urlField = new JTextField(url.toString(), 50);
                urlField.setDragEnabled(true);
                panel.add(new JLabel("<html>Go web :<br>(COPY & PASTE into your browser)"), BorderLayout.WEST);
                panel.add(urlField, BorderLayout.CENTER);
                panel.add(new JButton(new AbstractAction("", UIMessage._getIcon(UIMessage.WEB_TYPE, true)) {
                        public void actionPerformed(ActionEvent e) {
                                try {
                                        _goWeb(new URL(urlField.getText()));
                                        urlField.setToolTipText("If the browser doesn't open, copy-paste the URL into your browser !");
                                } catch (MalformedURLException ex) {
                                        if (JXAenvUtils._debugSys) {
                                                ex.printStackTrace();
                                        }
                                        urlField.setToolTipText("Not a valid URL");
                                }
                        }
                }), BorderLayout.EAST);
                return panel;
        }

        /**
         * @deprecated use {@linkplain #_debug}
         */
        public boolean isDebugEnabled() {
                return _debug;
        }

        /**
         * @deprecated use {@linkplain #_debug}
         */
        public void setDebugEnabled(boolean b) {
                _debug = b;
        }

        /**
         * returns a new JTimeProgressBar instance. It displays the remaining
         * time (hours:minutes) by measuring resting amount against the current
         * time at every call to {@linkplain JProgressBar#setValue(int)}.
         *
         * @param textString The displayed message format may be modified by
         * using something like <pre>"about %1$s remaining"</pre>.
         *
         * @return
         * @see String#format(String, Object...)
         */
        public static JProgressBar _JTimeProgressBar(String textString) {
                JTimeProgressBar jpb = new JTimeProgressBar();
                jpb.setString(textString);
                FontMetrics fm = jpb.getFontMetrics(jpb.getFont());
                Dimension getPref = new Dimension(Math.round(fm.stringWidth(jpb.getString()) * jpb.paddingFactor), Math.round(fm.getHeight() * jpb.paddingFactor));
                jpb.setPreferredSize(getPref);
                return jpb;
        }

        /**
         * returns a new JTimeProgressBar instance. It displays the remaining
         * time (hours:minutes) by measuring resting amount against the current
         * time at every call to {@linkplain JProgressBar#setValue(int)}.
         *
         * @return
         */
        public static JProgressBar _JTimeProgressBar() {
                JTimeProgressBar jpb = new JTimeProgressBar();
                return jpb;
        }
        /**
         * here are the correct values for conversion to hours, minutes and
         * seconds.. from millis long values. Simply multiply currentMillis with
         * the corresponding integer.
         */
        public static BigInteger hours = BigInteger.valueOf(3600L * (long) Math.pow(10, 3)), minutes = BigInteger.valueOf(60L * (long) Math.pow(10, 3)), seconds = BigInteger.valueOf(1L * (long) Math.pow(10, 3));

        static class JTimeProgressBar extends JProgressBar {

                private long wSpeedTimer = 0;
                private int wValTimer = 0;
                private String wTime;

                public JTimeProgressBar() {
                        super();
                }

                public JTimeProgressBar(int orient) {
                        super(orient);
                }

                public JTimeProgressBar(int min, int max) {
                        super(min, max);
                }

                public JTimeProgressBar(int orient, int min, int max) {
                        super(orient, min, max);
                }
                long time = 0;
                final String format_default = "%1$s time left";
                String format = format_default;

                public String getTimeStr(long time) {
                        BigInteger t = BigInteger.valueOf(time);
                        /*
                         * System.out.println("jpb time " + time);
                         */
                        if (time != 0) {
                                String fStr = "";
                                BigInteger[] H = t.divideAndRemainder(hours);
                                BigInteger[] M = H[1].divideAndRemainder(minutes);
                                BigInteger[] S = M[1].divideAndRemainder(seconds);
                                if (H[0].intValue() > 0) {
                                        fStr += String.format("%1$2d:", H[0].intValue());
                                }
                                if (M[0].intValue() > 0) {
                                        fStr += String.format("%1$2d'", M[0].intValue());
                                }
                                fStr += String.format("%1$2ds", S[0].intValue());
                                return fStr;
                        } else {
                                return "N/A";
                        }
                }

                /**
                 * String dimension factor to resize progressbar
                 */
                float paddingFactor = 1.2f;

                @Override
                public void setString(String s) {
                        if (s != null) {
                                String sQ = s.replace("%20", " ");
                                wTime = String.format(sQ, getTimeStr(time));
                                format = sQ;
                                super.setStringPainted(true);
                        } else {
                                wTime = null;
                                format = format_default;
                                super.setStringPainted(false);
                        }
                        super.setString(wTime);
                        /**
                         * fix : do repaint now on edt if possible
                         */
                        if (ThreadWorks.Swing.isEventDispatchThread()) {
                                paintImmediately(getVisibleRect());
                        }

                }
                List<Double> wSpeedTimerValues = Collections.synchronizedList(new ArrayList<Double>());

                @Override
                public void setValue(int n) {
                        long now = System.currentTimeMillis();
                        double avgSpeed = 0;
                        for (double s : wSpeedTimerValues) {
                                avgSpeed += s;
                                /*
                                 * System.out.println("jpb speed " + s + "% / s");
                                 */
                        }
                        /*
                         * arithmetic mean !
                         */
                        time = n == getValue() || wSpeedTimerValues.isEmpty() ? time : (long) Math.round((double) (getMaximum() - n) / (avgSpeed / (double) wSpeedTimerValues.size()));
                        /*
                         * update string
                         */
                        setString(n >= getMaximum() ? null : format);
                        if (n != wValTimer) {
                                /*
                                 * current speed
                                 */
                                if (now - wSpeedTimer > 100L) {
                                        if (wSpeedTimer != 0) {
                                                wSpeedTimerValues.add(((double) n - (double) wValTimer) / (double) (now - wSpeedTimer));
                                        }
                                        wValTimer = n;
                                        wSpeedTimer = now;
                                }
                        }
                        super.setValue(n);
                        if (n >= getMaximum()) {
                                wSpeedTimer = time = wValTimer = 0;
                                wSpeedTimerValues.clear();
                        }
                        /**
                         * fix : do repaint now on edt if possible
                         */
                        if (ThreadWorks.Swing.isEventDispatchThread()) {
                                paintImmediately(getVisibleRect());
                        }
                }
        }

        /**
         * @param os if the os bitmask finds the current os name, then this
         * tries to load the specified library from classpath-libpath
         * @param mapLibrary a map of libraries System name (e.g. Mac is "mylib"
         * for a file name as "libmylib.jnilib" or "libmylib.dylib") associated
         * to a boolean value indicating whether to link it now or simply copy
         * into an accessible library.path.
         * @param link true to load in System.loadLibrary() now
         * @return the env instance, if #isResourceLoaded() returns true, then
         * it's been loaded successfully.
         * @throws MalformedURLException
         * @see #_loadNativeLibraries(env, LinkedHashMap)
         */
        public static JXAenvUtils _loadNativeLibrary(env os, String libname, boolean link) throws MalformedURLException {
                return _loadNativeLibraries(os, new LinkedHashMap<String, Boolean>(Collections.singletonMap(libname, link)));
        }

        /**
         * immediately loads the specified map of libraries (stored in the
         * native .jar as natives/libfilename)
         *
         * @param os the env OS mask that should link the libs
         * @param mapLibrary a map of libraries System name (e.g. Mac is "mylib"
         * for a file name as "libmylib.jnilib" or "mylib.dylib") associated to
         * a boolean value indicating whether to link it now or simply copy into
         * an accessible library.path.
         * @return the env instance, if #isResourceLoaded() returns true, then
         * it's been loaded successfully.
         * @throws MalformedURLException
         */
        public static JXAenvUtils _loadNativeLibraries(env os, LinkedHashMap<String, Boolean> mapLibrary) throws MalformedURLException {
                Map<String, Map<URL, Boolean>> exts = new HashMap<String, Map<URL, Boolean>>();
                Map<String, Boolean> libs = Collections.synchronizedMap(mapLibrary);
                ClassLoader ecl = ExtensionsClassLoader.isResourceLoaded() ? ExtensionsClassLoader.getInstance().getClassLoader() : Thread.currentThread().getContextClassLoader();
                synchronized (mapLibrary) {
                        for (Iterator<String> i = libs.keySet().iterator(); i.hasNext();) {
                                String libname = i.next();
                                if ((os.compareMask() & (env.OS_WINDOWS.bitMask() | env.OS_WINDOWS_7.bitMask() | env.OS_WINDOWS_VISTA.bitMask() | env.OS_WINDOWS_XP.bitMask())) != 0) {
                                        exts.put(libname, Collections.singletonMap(ecl.getResource("/" + libname + ".dll"), libs.get(libname)));
                                }
                                if ((os.compareMask() & env.OS_LINUX.bitMask()) != 0) {
                                        exts.put(libname, Collections.singletonMap(ecl.getResource("/lib" + libname + ".so"), libs.get(libname)));
                                }
                                if ((os.compareMask() & env.OS_MAC.bitMask()) != 0) {
                                        String path = "/lib" + libname + ".jnilib";
                                        if (!(ecl.getResource(path) instanceof URL)) {
                                                path = "/lib" + libname + ".dylib";
                                        }
                                        exts.put(libname, Collections.singletonMap(ecl.getResource(path), libs.get(libname)));
                                }
                        }
                }
                return ExtensionsInstaller._installExtensions(Collections.singletonMap(os.osname(), exts), true, null);
        }

        /**
         *
         * @return
         */
        public static String _kernelVersion() {
                return JXAenvUtils.class.getPackage().getImplementationVersion();
        }

        /**
         *
         *
         * @param fd
         * @deprecated Moved to
         * {@link net.sf.jiga.xtended.kernel.FileHelper#_makeWritableOwnerOnly}
         */
        @Deprecated
        public static void _makeWritableOwnerOnly(File fd) {
                FileHelper._makeWritableOwnerOnly(fd);
        }

        /**
         *
         *
         * @param fd
         * @deprecated Moved to
         * {@link net.sf.jiga.xtended.kernel.FileHelper#_makeWritable}
         */
        @Deprecated
        public static void _makeWritable(File fd) {
                FileHelper._makeWritable(fd);
        }

        /**
         *
         *
         * @param s
         * @param d
         * @throws IOException
         * @deprecated Moved to
         * {@link net.sf.jiga.xtended.kernel.FileHelper#___rawfileCopy}
         */
        @Deprecated
        public static void ___rawfileCopy(File s, File d) throws IOException {
                FileHelper.___rawfileCopy(s, d, true);
        }

        /**
         *
         *
         * @param fd
         * @deprecated Moved to
         * {@link net.sf.jiga.xtended.kernel.FileHelper#_makeReadableOwnerOnly}
         */
        @Deprecated
        public static void _makeReadableOwnerOnly(File fd) {
                FileHelper._makeReadableOwnerOnly(fd);
        }

        /**
         *
         *
         * @param fd
         * @deprecated Moved to
         * {@link net.sf.jiga.xtended.kernel.FileHelper#_makeReadable}
         */
        @Deprecated
        public static void _makeReadable(File fd) {
                FileHelper._makeReadable(fd);
        }

        /**
         *
         *
         * @return @deprecated Moved to
         * {@link net.sf.jiga.xtended.kernel.FileHelper#_isImageIOCacheEnabled}
         */
        @Deprecated
        public static boolean _isImageIOCacheEnabled() {
                return FileHelper._isImageIOCacheEnabled();
        }

        /**
         * dis/enables ImageUIO caching
         *
         * @param b true or false, to enable/disable, resp.
         * @param cacheDirectory the folder where ImageIO will put the cache
         * files
         * @default enabled, if disabled, unexpected behaviours may occur with
         * image loading.
         * @deprecated Moved to
         * {@link net.sf.jiga.xtended.kernel.FileHelper#_setImageIOCacheEnabled}
         */
        @Deprecated
        public static void _setImageIOCacheEnabled(final boolean b, final File cacheDirectory) {
                FileHelper._setImageIOCacheEnabled(b, cacheDirectory);
        }
}
