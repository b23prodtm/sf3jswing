/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.jiga.xtended.kernel;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import net.sf.jiga.xtended.JXAException;
import static net.sf.jiga.xtended.kernel.FileHelper.FILE_READ;
import static net.sf.jiga.xtended.kernel.FileHelper.FILE_WRITE;
import static net.sf.jiga.xtended.kernel.FileHelper._TMPDIRECTORY;
import static net.sf.jiga.xtended.kernel.FileHelper._USERHOMEDIRECTORY;
import static net.sf.jiga.xtended.kernel.JXAenvUtils.*;
import net.sf.jiga.xtended.kernel.JXAenvUtils.LVL;
import static net.sf.jiga.xtended.kernel.JXAenvUtils.env.*;
import net.sf.jiga.xtended.ui.*;

/**
 * Extensions Toolkit. the main function runs a JXAenvUtils instance to load all
 * the required natives. it's not worth to call it locally (if you want to do
 * so, prefer the
 * {@linkplain AntHandler#_load(net.sf.jiga.xtended.ui.Ant, net.sf.jiga.xtended.ui.DisplayInterface, java.util.Map, java.util.List)}!).
 * Please, edit build-youros-yourarch.xml for "extension.properties" !
 *
 * @author www.b23prodtm.info
 */
public class ExtensionsInstaller {

    /**
     * installs all the file-map into the
     * {@link #_findExtPath(boolean) extension path}. It is used to load extra
     * files into the extensions path. All write permissions must have been
     * granted before
     *
     * @param libpath assumes that the map is with U<Map<URL, Boolean>> if true.
     * the map denotes a set of <b>native</b libraries
     * (.dll,.so,.jnilib,.dyllib) that are loaded @param map a map of all
     * resources files to install into {@link #_findExtPath(boolean) extension
     * path}with the following struct. : Map<OSNAME, Map<uniqueRESID,
     * accessibleRESfilename>> the accessibleRESfilename must target an
     * accessible resource file OSNAME denotes a String identifying an os.name
     * or the starting String of os.name, e.g. "Windows" can stand for Windows
     * XP and Windows Vista      <pre>
     * Map<String, Map<String, URL>> map = new LinkedHashMap<String, Map<String, URL>>();
     * Map<String, String> exts;
     * map.put("Mac OS", exts = new LinkedHashMap<String, URL>());
     * exts.put("lwjgl", new URL("/natives/liblwjgl.jnilib"));
     * _installExtensions(map, true);
     * </pre>
     *
     * * @param backupDirectory if set, it will first look if a recent backup
     * has been found or get files from source env.
     * @see AntHandler#_load(net.sf.jiga.xtended.ui.Ant,
     * net.sf.jiga.xtended.ui.DisplayInterface, java.util.Map, java.util.List)
     * @see #_uninstallExtensions(net.sf.jiga.xtended.kernel.JXAenvUtils,
     * java.util.Map, boolean, java.io.File)
     */
    public static <U> JXAenvUtils _installExtensions(Map<String, Map<String, U>> map, boolean libpath, File backupDirectory) throws MalformedURLException {
        JXAenvUtils env = new JXAenvUtils();
        env.setJXAenvPath(_findExtPath(libpath));
        env.setBigBuffer(true);
        return _installExtensions(env, map, libpath, backupDirectory);

    }

    /**
     * uninstalls from the JXA env.
     *
     * @param backupDirectory if set it is where the removed files will be
     * backed up
     */
    public static <U> JXAenvUtils _uninstallExtensions(Map<String, Map<String, U>> map, boolean libpath, File backupDirectory) {
        JXAenvUtils env = new JXAenvUtils();
        env.setJXAenvPath(_findExtPath(libpath));
        return _uninstallExtensions(env, map, libpath, backupDirectory);
    }

    /**
     * finds an available extension path for the current runtime and, if
     * specified, checks for the availability of an extension path that can load
     * natives packages. java.library.path will be checked if the "libpath" arg
     * is set "true".<br> the java.ext.dirs lookup can be en/disabled : it's
     * especially on non-OS X machines that it is required. Windows and Linux
     * systems have it disabled by default (OS X ext.dirs is disabled since JXA
     * 4.4, too), to enable or disable : simply set <b>property
     * <u>jxa.ext.dirs=true,false</u></b> at launching.
     *
     * @param libpath if true, look into the java.library.path for the existence
     * of an extension directory
     * @return a pathname to an available extension path
     */
    public static String _findExtPath(final boolean libpath) {
        return AccessController.doPrivileged(new PrivilegedAction<String>() {
            public String run() {
                return __findExtPath(libpath);
            }
        });
    }

    private static String __findExtPath(boolean libpath) {
        boolean writeableExts = false;
        String extPath = "---";
        String exts = libpath ? getLibraryPath() : _getSysValue("java.ext.dirs");
        boolean useSystemExtPaths = Boolean.parseBoolean(rb.getString("useSystemExtensionsPaths")) || _getSysBoolean("jxa.ext.dirs");
        if (_debugSys) {
            System.out.println(log("Detecting your home (writeable) folder : " + FileHelper._USERHOMEDIRECTORY.getAbsolutePath(), LVL.SYS_NOT));
            System.out.println(log("Detecting your temp (writeable) folder : " + FileHelper._TMPDIRECTORY.getAbsolutePath(), LVL.SYS_NOT));
            System.out.println(log("Detecting your class-path : " + getClasspath(), LVL.SYS_NOT));
            System.out.println(log("Detecting your extensions path : " + exts, LVL.SYS_NOT));
        }
        if (libpath) {
            if (_debugSys) {
                System.out.println(log("Detecting your Library path : " + getLibraryPath(), LVL.SYS_NOT));
            }
        }
        /**
         * find writeable path
         */
        if (useSystemExtPaths) {
            if (OS_WINDOWS.isEnv()) {
                if (!_hasEnv(OS_WINDOWS_XP.bitMask())) {
                    String dir = JAVA_VERSION_7.isEnv() ? "jre7" : JAVA_VERSION_6.isEnv() ? "jre6" : "jre5";
                    exts = _updatePath(exts, FileHelper._USERHOMEDIRECTORY.getPath() + File.separator + "AppData\\Local\\VirtualStore\\Program Files\\Java\\" + dir + "\\lib\\ext");
                }
            }
            for (String path : exts.split(File.pathSeparator)) {
                if (writeableExts = FileHelper._accessFilePermitted(new File(path), FILE_READ | FILE_WRITE)) {
                    extPath = path;
                    break;
                }
            }
        }
        /**
         * define a library.path outside ext path if noting could be found from
         * ext path (non-OSX specific) if (useSystemExtPaths) { if
         * (!writeableExts && libpath) { for (String libDir :
         * getLibraryPath().split(File.pathSeparator)) { if (writeableExts =
         * _accessFilePermitted(new File(libDir), FILE_READ | FILE_WRITE)) {
         * extPath = libDir; break; } } } }
         */
        if (!writeableExts && !FileHelper._USERHOMEDIRECTORY.equals(new File("."))) {
            if (writeableExts = FileHelper._accessFilePermitted(_USERHOMEDIRECTORY, FILE_READ | FILE_WRITE)) {
                extPath = FileHelper._USERHOMEDIRECTORY.getAbsolutePath();
            }
        }
        if (!writeableExts && !FileHelper._TMPDIRECTORY.equals(new File("."))) {
            if (writeableExts = FileHelper._accessFilePermitted(_TMPDIRECTORY, FILE_READ | FILE_WRITE)) {
                extPath = FileHelper._TMPDIRECTORY.getAbsolutePath();
            }
        }
        if (!writeableExts) {
            extPath = ".";
        }
        if (libpath) {
            if (_debugSys) {
                System.out.println(JXAenvUtils.log("Altering java.library.path may not be successful", LVL.SYS_WRN));
            }
            _setSysValue("java.library.path", _updatePath(getLibraryPath(), extPath));
        }
        if (_debugSys) {
            System.out.println(log("Using " + (libpath ? "lib." : "ext.") + " path : " + extPath, LVL.SYS_NOT));
        }
        return extPath;
    }
    public static boolean showSplash = false;
    public static URL splashPic = ExtensionsInstaller.class.getResource("/net/sf/jiga/xtended/ui/images/Sf3Splash.png");

    /**
     * installs all the file-map into the
     * {@link #_findExtPath(boolean) extension path}. It is used to load extra
     * files into the extensions path. All write permissions must have been
     * granted before
     *
     * @param env if you already have instanciated an env it can be used here
     * @param libpath assumes that the map is with U<Map<URL, Boolean>> if true.
     * the map denotes a set of <b>native</b libraries
     * (.dll,.so,.jnilib,.dyllib) that are loaded @param map a map of all
     * resources files to install into {@link #_findExtPath(boolean) extension
     * path} with the following struct. : Map<OSNAME, Map<uniqueRESID,
     * accessibleRESfilename>> the accessibleRESfilename must target an
     * accessible resource file OSNAME denotes a String identifying an os.name
     * or the starting String of os.name, e.g. "Windows" can stand for Windows
     * XP and Windows Vista      <pre>
     * Map<String, Map<String, URL>> map = new LinkedHashMap<String, Map<String, URL>>();
     * Map<String, String> exts;
     * map.put("Mac OS", exts = new LinkedHashMap<String, URL>());
     * exts.put("lwjgl", new URL("/natives/liblwjgl.jnilib"));
     * _installExtensions(map, true);
     * </pre>
     *
     * * @param backupDirectory if set, it will first look if a recent backup
     * has been found or get files from source env.
     * @see AntHandler#_load(net.sf.jiga.xtended.ui.Ant,
     * net.sf.jiga.xtended.ui.DisplayInterface, java.util.Map, java.util.List)
     * @see #_uninstallExtensions(net.sf.jiga.xtended.kernel.JXAenvUtils,
     * java.util.Map, boolean, java.io.File)
     */
    public static <U> JXAenvUtils _installExtensions(final JXAenvUtils env, final Map<String, Map<String, U>> map, final boolean libpath, final File backupDirectory) throws MalformedURLException {
        try {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<JXAenvUtils>() {
                public JXAenvUtils run() throws MalformedURLException, CloneNotSupportedException {
                    if (backupDirectory instanceof File) {
                        JXAenvUtils backenv = (JXAenvUtils) env.clone();
                        backenv.silent = true;
                        System.out.println(log("Loading backup " + (libpath ? "natives" : "extensions") + " (if available)...", LVL.SYS_NOT));
                        if (libpath) {
                            __installExtensions(backenv, __baseFileMapNat(map, backupDirectory.toURI().toURL(), false), libpath);
                        } else {
                            __installExtensions(backenv, __baseFileMapExt(map, backupDirectory.toURI().toURL(), false), libpath);
                        }
                        /*
                         * VOID if (backenv.isResourceLoaded()) { return
                         * backenv; }
                         */
                    }
                    System.out.println(log("Loading updated " + (libpath ? "natives" : "extensions") + " (if available)...", LVL.SYS_NOT));
                    /**
                     * make sure latest version is there
                     */
                    return __installExtensions(env, map, libpath);
                }
            });
        } catch (PrivilegedActionException ex) {
            if (ex.getException() instanceof CloneNotSupportedException) {
                if (_debugSys) {
                    ex.getException().printStackTrace();
                }
                return env;
            } else {
                throw (MalformedURLException) ex.getException();
            }
        }
    }

    /**
     * @param libpath assumes that the map is with U<Map<URL, Boolean>>
     */
    private static <U> JXAenvUtils __installExtensions(JXAenvUtils env, Map<String, Map<String, U>> map, boolean libpath) {
        if (map == null) {
            System.out.println(log("Nothing loaded for extensions here.", LVL.SYS_WRN));
            return env;
        }
        if (map.isEmpty()) {
            System.out.println(log("Nothing loaded for extensions here.", LVL.SYS_WRN));
            return env;
        }
        if (!_getSysValue("os.name").startsWith(rb.getString("os"))) {
            _popExceptionToUser(true, Thread.currentThread(), new UnsupportedOperationException("The running OS " + _getSysValue("os.name") + " does not match with " + rb.getString("os")));
            System.exit(1);
        }
        Map<String, U> extsMap = null;
        synchronized (map) {
            for (String os : map.keySet()) {
                if (_getSysValue("os.name").startsWith(os)) {
                    extsMap = map.get(os);
                    break;
                }
            }
        }
        if (extsMap instanceof Map) {
            synchronized (extsMap) {
                for (String id : extsMap.keySet()) {
                    if (libpath) {
                        env.addEnvFile(id, ((Map<URL, Boolean>) extsMap.get(id)).keySet().iterator().next());
                    } else {
                        env.addEnvFile(id, (URL) extsMap.get(id));
                    }
                }
            }
            if (!env.silent && showSplash && ThreadWorks.Swing.isEventDispatchThread()) {
                try {
                    env.setSplashPicture(Display._Display(splashPic, null));
                } catch (Exception ex) {
                    if (_debugSys) {
                        ex.printStackTrace();
                    }
                } finally {
                    env.loadResource();
                }
            } else {
                env.loadAll();
            }
            if (env.hasLoadErrors()) {
                if (!env.silent) {
                    String errors = "";
                    for (String id : env.getErroredFiles()) {
                        errors += id + " > " + env.getEnvSourceFile(id) + File.pathSeparator + " ";
                    }
                    throw new JXAException(JXAException.LEVEL.SYSTEM, (libpath ? "Native libraries" : "Jar resources") + " environment loading process had one or more errors." + Console.newLine + "Set jxa.debugSys=true jxa.debugECL=true to see more info. " + Console.newLine + errors);
                }
            } else if (libpath) {
                synchronized (extsMap) {
                    ClassLoader cL = _switchToClassLoader(env.getEnvClassLoader());
                    for (String id : extsMap.keySet()) {
                        if (!((Map<URL, Boolean>) extsMap.get(id)).values().iterator().next()) {
                            continue;
                        }
                        try {
                            if (_debugSys) {
                                System.out.print("System.loadLibrary " + id);
                            }
                            System.loadLibrary(id);
                            if (_debugSys) {
                                System.out.println(" OK");
                            }
                        } catch (Throwable e) {
                            if (_debugSys) {
                                System.out.println(" FAILED " + e.getMessage());
                            }
                            String fullPath = env.getEnvInstalledFile(id).getAbsolutePath();
                            if (_debugSys) {
                                System.out.print("System.load library " + id + " from " + fullPath);
                            }
                            try {
                                System.load(fullPath);
                                if (_debugSys) {
                                    System.out.println(" OK");
                                }
                            } catch (Throwable ex) {
                                if (_debugSys) {
                                    System.out.println(" FAILED " + ex.getMessage());
                                }
                                if (!env.silent) {
                                    throw new JXAException(e.getLocalizedMessage(), ex);
                                }
                            }
                        }
                    }
                    _switchToClassLoader(cL);
                }
            }
        }
        if (env.isResourceLoaded()) {
            if (_debugSys) {
                System.out.println(log("Loaded all environment resources !", LVL.SYS_NOT));
            }
        } else {
            if (!env.silent) {
                System.err.println(log("Some of the environment resources failed to load. Use jxa.debugSys=true system property to see details !", LVL.SYS_ERR));
            }
        }
        return env;
    }

    /**
     * uninstalls from the specified env.
     *
     * @param backupDirectory if set, it is where the removed files will be
     * backed up
     */
    public static <U> JXAenvUtils _uninstallExtensions(final JXAenvUtils env, final Map<String, Map<String, U>> map, final boolean libpath, final File backupDirectory) {
        return AccessController.doPrivileged(new PrivilegedAction<JXAenvUtils>() {
            public JXAenvUtils run() {
                if (backupDirectory instanceof File) {
                    Map<String, U> osfileMap = map.get(_getSysValue("os.name"));
                    synchronized (osfileMap) {
                        int i = 0;
                        for (String id : osfileMap.keySet()) {
                            try {
                                if (libpath) {
                                    File dst = new File(backupDirectory + File.separator + new File(((Map<URL, Boolean>) osfileMap.get(id)).keySet().iterator().next().toString()).getName());
                                    URL file = ((Map<URL, Boolean>) osfileMap.get(id)).keySet().iterator().next();
                                    FileHelper._fileCopy(file, dst, false, true);
                                } else {
                                    File dst;
                                    URL u = (URL) osfileMap.get(id);
                                    if (_debugSys) {
                                        System.out.println("extensions .jar file : " + u);
                                    }
                                    dst = new File(backupDirectory + File.separator + new File(u.toString()).getName());
                                    FileHelper._fileCopy((URL) osfileMap.get(id), dst, false, true);
                                }
                            } catch (Exception ex) {
                                if (_debugSys) {
                                    ex.printStackTrace();
                                }
                            }
                        }
                    }
                }
                return __uninstallExtensions(env, map, libpath);
            }
        });
    }

    private static <U> JXAenvUtils __uninstallExtensions(JXAenvUtils env, Map<String, Map<String, U>> map, boolean libpath) {
        if (map == null) {
            System.out.println(log("Nothing loaded for extensions here.", LVL.SYS_WRN));
            return env;
        }
        if (map.isEmpty()) {
            System.out.println(log("Nothing loaded for extensions here.", LVL.SYS_WRN));
            return env;
        }
        synchronized (map) {
            for (String os : map.keySet()) {
                Map<String, U> extsMap = map.get(os);
                if (_getSysValue("os.name").startsWith(os)) {
                    synchronized (extsMap) {
                        for (String id : extsMap.keySet()) {
                            if (libpath) {
                                env.addEnvFile(id, ((Map<URL, Boolean>) extsMap.get(id)).keySet().iterator().next());
                            } else {
                                env.addEnvFile(id, (URL) extsMap.get(id));
                            }
                        }
                    }
                    if (showSplash && ThreadWorks.Swing.isEventDispatchThread()) {
                        env.clearResource();
                    } else {
                        env.unloadAll();
                    }
                    if (env.hasLoadErrors()) {
                        System.err.println(log("environment unloading process had error(s) on unloading", LVL.SYS_ERR));
                    }
                    break;
                }
            }
        }
        if (!env.isResourceLoaded()) {
            if (_debugSys) {
                System.out.println(log("Successfully unloaded all environment resources !", LVL.SYS_NOT));
            }
        } else {
            System.err.println(log("Some of the environment resources failed to unload.", LVL.SYS_ERR));
        }
        return env;
    }
    /**
     * resource properties
     * (resources)/os.name/all/net/sf/jiga/xtended/kernel/extension.properties
     */
    public static ResourceBundle rb = ResourceBundle.getBundle("net.sf.jiga.xtended.kernel.extension", Locale.getDefault(), ExtensionsInstaller.class.getClassLoader());

    /**
     * env map for JXA natives that can be loaded using
     * {@linkplain #_installExtensions(java.util.Map, boolean, java.io.File)}
     *
     * @param map a map to store the files mapping or null and a map will be
     * created and filled with the JXA env files
     * @param base a base URL that will be used as the base path for env files
     * in the returned map
     */
    public static Map<String, Map<String, Map<URL, Boolean>>> _getJXANatenvFiles(Map<String, Map<String, Map<URL, Boolean>>> map, URL base) throws MalformedURLException, URISyntaxException {
        return __baseFileMapNat(_getJXANatenvFiles(map, true), base, false);
    }

    /**
     * @param map a map to add the files mapping in or null and a map will be
     * created and filled with the JXA env files (into map key with the name
     * System.property("os.name"))
     * @param local use URL (false) or File (true) to find native resources; by
     * local (File) it will look in the current folder (/.), by default JXA
     * projects loads every native resources from .jar!/ URLS.
     */
    public static Map<String, Map<String, Map<URL, Boolean>>> _getJXANatenvFiles(Map<String, Map<String, Map<URL, Boolean>>> map, boolean local) throws MalformedURLException, URISyntaxException {
        map = map instanceof Map ? map : Collections.synchronizedMap(new LinkedHashMap<String, Map<String, Map<URL, Boolean>>>());
        Map<String, Map<URL, Boolean>> exts = map.get(_getSysValue("os.name"));
        if (exts == null) {
            map.put(_getSysValue("os.name"), exts = new LinkedHashMap<String, Map<URL, Boolean>>());
        }
        String natives = rb.getString("extNatives");
        ClassLoader extLoader = ExtensionsClassLoader.getInstance().getClassLoader();/*
         * new
         * ExtensionsClassLoader(ExtensionsInstaller.class.getClassLoader());
         */

        for (String nat : natives.split("\\s")) {
            String libname = nat.split(":")[0];
            String libfile = nat.split(":")[1];
            boolean loadLib = nat.split(":").length == 3 ? Boolean.parseBoolean(nat.split(":")[2]) : false;
            while (libfile.startsWith("/") && libfile.length() > 1) {
                libfile = libfile.substring(1);
            }
            URL url = local ? new File("." + File.separator + libfile).toURI().toURL() : extLoader.getResource(libfile);
            exts.put(libname, Collections.singletonMap(url, loadLib));
        }
        if (_debugSys) {
            System.out.println("ExtensionsInstaller (natives) :: current running directory is : " + new File("."));
        }
        return map;
    }

    private static Map<String, URL> _getJXAExtFile(String jar, URL base) throws MalformedURLException {
        while (jar.startsWith("/") && jar.length() > 1) {
            jar = jar.substring(1);
        }
        URL url = new URL(base, jar);
        return Collections.singletonMap(FileHelper._getURLFilename(url), url);
    }

    /**
     * @param map a map to add the files mapping (i.e. where the .jar resources files are) or null and a map will be
     * created and filled with the standard JXA environment files 
     * .The map key with the name System.property("os.name")); <br>
     * a LinkedHashMap is the preferred map type (ordered and unique id).
     * @param local use URL (false) or File (true) to find jar files (relative
     * to the current directory)
     * @param withInstaller adds the
     * {@link #rb ResourceBundle.extInstallerJar path-value}
     */
    public static Map<String, Map<String, URL>> _getJXAExtenvFiles(Map<String, Map<String, URL>> map, boolean local, boolean withInstaller) throws MalformedURLException, URISyntaxException {
        String jars = rb.getString("extJars");
        String jarsPathStr = rb.getString("extPathURL");
        if (jarsPathStr.endsWith("/")) {
            jarsPathStr += "/";
        }        
        File appRoot = new File(System.getProperty("jxa.appRoot", "."));
        URL jarsPath = local ? new File(appRoot,"api" + File.separator).toURI().toURL() : new URL(jarsPathStr);
        /**
         * rebase .jar all in the same directory (appBundler 1.0)
         */
        if (Boolean.getBoolean("jxa.appBundle")) {
            jarsPath = appRoot.toURI().toURL();
            if (DebugMap._getInstance().isJXADebugSysEnabled()) {
                UIMessage.showLightPopupMessage(new JLabel("appbundle " + jarsPath), null, null, UIMessage.UI_BOTTOM_LEFT);
            }
        }
        String jarInstaller = rb.getString("extInstallerJar");
        map = map instanceof Map ? map : Collections.synchronizedMap(new LinkedHashMap<String, Map<String, URL>>());
        Map<String, URL> exts = map.get(_getSysValue("os.name"));
        if (exts == null) {
            map.put(_getSysValue("os.name"), exts = new LinkedHashMap<String, URL>());
        }
        for (String jar : jars.split("\\s")) {
            exts.putAll(_getJXAExtFile(local ? jar.replaceAll("%20", " ") : jar, jarsPath));
        }
        if (withInstaller) {
            exts.putAll(_getJXAExtFile(local ? jarInstaller.replaceAll("%20", " ") : jarInstaller, jarsPath));
        }
        if (_debugSys) {
            if (local) {
                System.out.println("ExtensionsInstaller (api) :: current running directory is : " + new File(".").getAbsolutePath());
            } else {
                System.out.println("ExtensionsInstaller (api) :: current codebase is : " + ExtensionsInstaller.class.getProtectionDomain().getCodeSource().getLocation());
            }
        }
        return map;
    }

    /**
     * "move" URLS to another base; used to get the local version of a remote
     * (native library) file connection
     *
     * @param fullPath if true, folder path will be used as is, if false, the
     * file names only will be kept
     */
    private static <U> Map<String, Map<String, U>> __baseFileMapNat(Map<String, Map<String, U>> map, URL base, boolean fullPath) throws MalformedURLException {
        Map<String, Map<String, URL>> baseFileMap = Collections.synchronizedMap(new LinkedHashMap<String, Map<String, URL>>());
        synchronized (map) {
            for (String os : map.keySet()) {
                LinkedHashMap<String, URL> osMap = new LinkedHashMap<String, URL>();
                for (Map.Entry<String, U> file : map.get(os).entrySet()) {
                    osMap.put(file.getKey(), ((Map<URL, Boolean>) file.getValue()).keySet().iterator().next());
                }
                baseFileMap.put(os, osMap);
            }
        }
        baseFileMap = __baseFileMapExt(baseFileMap, base, fullPath);
        Map<String, Map<String, U>> baseFileMapNat = new LinkedHashMap<String, Map<String, U>>();
        synchronized (baseFileMap) {
            for (String os : baseFileMap.keySet()) {
                LinkedHashMap<String, U> osMap = new LinkedHashMap<String, U>();
                for (Map.Entry<String, URL> file : baseFileMap.get(os).entrySet()) {
                    osMap.put(file.getKey(), (U) Collections.singletonMap(file.getValue(), ((Map<URL, Boolean>) map.get(os).get(file.getKey())).values().iterator().next()));
                }
                baseFileMapNat.put(os, osMap);
            }
        }
        return Collections.synchronizedMap(baseFileMapNat);
    }

    /**
     * "move" URLS to another base; used to get the local version of a remote
     * file connection
     *
     * @param fullPath if true, folder path (which is right after
     * server-hostname/) will be used as is, if false, the file names only will
     * be kept
     */
    private static <U> Map<String, Map<String, U>> __baseFileMapExt(Map<String, Map<String, U>> map, URL base, boolean fullPath) throws MalformedURLException {
        Map<String, Map<String, U>> baseFileMap = Collections.synchronizedMap(new LinkedHashMap<String, Map<String, U>>());
        synchronized (map) {
            for (Map.Entry<String, Map<String, U>> osMap : map.entrySet()) {
                Map<String, URL> osfileMap = (Map<String, URL>) osMap.getValue();
                Map<String, U> osbasefileMap = new LinkedHashMap<String, U>();
                for (String id : osfileMap.keySet()) {
                    URL path = osfileMap.get(id);
                    if (path == null) {
                        osbasefileMap.put(id, null);
                    } else {
                        if (base == null) {
                            osbasefileMap.put(id, (U) path);
                        } else {
                            String sPath = fullPath ? path.getPath() : new File(path.getPath()).getName();
                            while (sPath.startsWith("/") && sPath.length() > 1) {
                                sPath = sPath.substring(1);
                            }
                            osbasefileMap.put(id, (U) new URL(base, sPath));
                        }
                    }
                }
                baseFileMap.put(osMap.getKey(), osbasefileMap);
            }
        }
        return baseFileMap;
    }

    private static Map<String, Map<String, URL>> __urlFileMap(Map<String, Map<String, File>> map) throws MalformedURLException {
        Map<String, Map<String, URL>> urlMap = new LinkedHashMap<String, Map<String, URL>>();
        synchronized (map) {
            for (String os : map.keySet()) {
                Map<String, URL> osfileMap = new LinkedHashMap<String, URL>();
                for (String id : map.get(os).keySet()) {
                    if (map.get(os).get(id) == null) {
                        osfileMap.put(id, null);
                    } else {
                        osfileMap.put(id, map.get(os).get(id).toURI().toURL());
                    }
                }
                urlMap.put(os, osfileMap);
            }
        }
        return Collections.synchronizedMap(urlMap);
    }

    /**
     * @param map a map to add the files or null to create a new map
     * @param withInstaller adds the ResourceBundle.extInstallerJar path-value
     * @param base a base URL that will be used as the base path for env files
     * in the returned map
     * @param fullPath if true, folder path will be used as is and appended to
     * base; if false, the file names (in map) only will be kept
     */
    public static Map<String, Map<String, URL>> _getJXAExtenvFilesFromBase(Map<String, Map<String, URL>> map, URL base, boolean withInstaller, boolean fullPath) throws MalformedURLException, URISyntaxException {
        return __baseFileMapExt(_getJXAExtenvFiles(map, true, withInstaller), base, fullPath);
    }

    /**
     * This method won't do anything and return if you're running a Java more
     * recent than the 5th edition, since the new Java plugin does replace the
     * classic Extension install.
     *
     * @param params a set of Bitwise-OR combination of parmeters to directly do
     * the install or 0 "zero" to ask the user.
     * @see #DO_INSTALL
     * @see #DO_UNINSTALL
     * @see #DO_APPLET
     * @see #DO_APPLICATION
     * @see #main(String[])
     * @deprecated for testing file permission only
     */
    public static void _externalInstall(int params) {
        if (!JAVA_VERSION_5.isEnv()) {
            JLabel label = new JLabel("<html>Click OK;"
                    + "<br> and wait for the installation process to finish.</p>"
                    + "<br>API rev. " + _kernelVersion() + "</html>");
            String[] browser = new String[]{"Test .jar extensions install !", "Don't test it !"};
            String ret = UIMessage.showSelectDialog(null, label, "ExtensionsInstaller Test Unit", browser, browser[0]);
            if (!browser[0].equals(ret)) {
                new UIMessage(true, new JLabel("Setup is complete."), null);
                if ((params & bits._getMask(_SET) & SET_KEEPALIVE) == 0) {
                    System.exit(0);
                } else {
                    return;
                }
            }
        }
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            public void uncaughtException(Thread t, Throwable e) {
                ThreadWorks._uncaughtException(true, t, e);
            }
        });
        JXAenvUtils env = new JXAenvUtils();
        if (!ExtensionsClassLoader.isResourceLoaded()) {
            ExtensionsClassLoader._load(env);
        }
        if ((params & _DO) == 0) {
            String[] options = new String[]{"Java SE Installer", "Java Applet Installer", "Java SE Installer w/ Log"};
            Object res = null;
            while (res == null) {
                int set = params & bits._getMask(_SET);
                res = UIMessage.showSelectDialog(null, new JLabel("<html><center>Please choose the setup mode : <ol>"
                        + "<li>Java SE Installer;</li>"
                        + "<li>Java Applet Installer;</li>"
                        + "</ol>(w/ Log means that debugging may be enabled <br>by launching a <b>Jconsole</b> to monitor)</center></html>"), "Setup", options, options[0]);
                if (options[1].equals(res)) {
                    params |= DO_APPLET;
                }
                if (options[0].equals(res)) {
                    params |= DO_APPLICATION;
                }
                if (options[2].equals(res)) {
                    params |= DO_APPLICATION | SET_LOGTRACE;
                }
                if (res == null) {
                    if (JOptionPane.YES_OPTION == UIMessage.showConfirmDialog(null, "Are your sure to cancel the setup process ?", "Cancel setup ?", JOptionPane.YES_NO_OPTION)) {
                        if ((set & SET_KEEPALIVE) == 0) {
                            System.exit(0);
                        } else {
                            return;
                        }
                    }
                }
            }
        }
        int doIt = params & bits._getMask(_DO);
        int setIt = params & bits._getMask(_SET);
        try {
            if ((setIt & SET_LOGTRACE) != 0) {
                _debugSys = true;
            }
            if ((doIt & DO_APPLET) != 0) {
                JPanel panel = _goWeb(new URL(rb.getString("appletInstallerURL")));
                UIMessage.showConfirmDialog(null, panel, "Load Applet", JOptionPane.DEFAULT_OPTION);
                if ((setIt & SET_KEEPALIVE) == 0) {
                    System.exit(0);
                } else {
                    return;
                }
            }
            if ((doIt & DO_APPLICATION) != 0) {
                /*
                 * "\"" + _getJavaExecutablePath() + "\""
                 */
                String[] options = new String[]{"Update", "Uninstall", "Update w/ Log"};
                Object res = null;
                while (res == null) {
                    res = UIMessage.showSelectDialog(null, new JLabel("<html><center>Please choose : <ol>"
                            + "<li>Update;</li>"
                            + "<li>Uninstall;</li>"
                            + "</ol>(w/ Log means that debugging may be enabled <br>by launching a <b>JConsole</b> to monitor)</center></html>"), "Setup", options, options[0]);
                    if (options[1].equals(res)) {
                        params |= DO_UNINSTALL;
                    }
                    if (options[0].equals(res)) {
                        params |= DO_INSTALL;
                    }
                    if (options[2].equals(res)) {
                        params |= DO_INSTALL | SET_LOGTRACE;
                    }
                    if (res == null) {
                        if (JOptionPane.YES_OPTION == UIMessage.showConfirmDialog(null, "Are your sure to cancel the setup process ?", "Cancel setup ?", JOptionPane.YES_NO_OPTION)) {
                            break;
                        }
                    }
                }
                if (res == null) {
                    if ((setIt & SET_KEEPALIVE) == 0) {
                        System.exit(0);
                    } else {
                        return;
                    }
                } else {

                    doIt = params & bits._getMask(_DO);
                    setIt = params & bits._getMask(_SET);

                }
            }
            final LinkedList<String> envPaths = new LinkedList<String>();
            String installerPath = new File(FileHelper._USERHOMEDIRECTORY, "Desktop" + File.separator + "JXA Manager").getAbsolutePath();
            if ((doIt & DO_UNINSTALL) != 0) {
                ExtensionsInstaller.showSplash = (setIt & SET_SPLASH) != 0 && ThreadWorks.Swing.isEventDispatchThread();
                env = _uninstallExtensions(_getJXANatenvFiles(null, (setIt & SET_LOCAL) != 0), true, null);
                env = new JXAenvUtils();
                env.setJXAenvPath(FileHelper._USERHOMESTOREDIRECTORY.getPath());
                env = _uninstallExtensions(_getJXANatenvFiles(null, (setIt & SET_LOCAL) != 0), true, null);
                if (env.isResourceLoaded() || env.hasLoadErrors()) {
                    throw new Exception("JXANat env failed.");
                }
                envPaths.add(env.getJXAenvPath());
                env = _uninstallExtensions(_getJXAExtenvFiles(null, (setIt & SET_LOCAL) != 0, true), false, null);
                env = new JXAenvUtils();
                env.setJXAenvPath(FileHelper._USERHOMESTOREDIRECTORY.getPath());
                env = _uninstallExtensions(_getJXAExtenvFiles(null, (setIt & SET_LOCAL) != 0, true), false, null);
                if (env.isResourceLoaded() || env.hasLoadErrors()) {
                    throw new Exception("JXA env failed.");
                }
                envPaths.add(env.getJXAenvPath());
                File installer = new File(installerPath);
                _erase(installer);
            }
            if ((doIt & DO_INSTALL) != 0) {
                ExtensionsInstaller.showSplash = (setIt & SET_SPLASH) != 0 && ThreadWorks.Swing.isEventDispatchThread();
                env = _installExtensions(_getJXAExtenvFiles(null, (setIt & SET_LOCAL) != 0, true), false, FileHelper._USERHOMESTOREDIRECTORY);
                if (!env.isResourceLoaded() || env.hasLoadErrors()) {
                    throw new Exception("JXA env failed.");
                } else {
                    /**
                     * backup new installed files
                     */
                    env.setJXAenvPath(FileHelper._USERHOMESTOREDIRECTORY.getPath());
                    env.loadResource();
                }
                envPaths.add(env.getJXAenvPath());
                env = _installExtensions(_getJXANatenvFiles(null, (setIt & SET_LOCAL) != 0), true, FileHelper._USERHOMESTOREDIRECTORY);
                if (!env.isResourceLoaded() || env.hasLoadErrors()) {
                    throw new Exception("JXANat env failed.");
                } else {
                    /**
                     * backup new installed files
                     */
                    env.setJXAenvPath(FileHelper._USERHOMESTOREDIRECTORY.getPath());
                    env.loadResource();
                }
                envPaths.add(env.getJXAenvPath());
                InputStream inAppInstaller = ExtensionsInstaller.class.getResourceAsStream("/setup.zip");
                if (inAppInstaller != null) {
                    String message = "<html>installing " + installerPath + "...</html>";
                    long wait = UIMessage.displayProgress(message, env.getUIMessageProgressBar(), 0, 100, null);
                    ZipInputStream unzip = new ZipInputStream(inAppInstaller);
                    ZipEntry ze = null;
                    while ((ze = unzip.getNextEntry()) != null) {
                        File newFile = new File(installerPath, ze.getName());
                        if (ze.isDirectory()) {
                            newFile.mkdirs();
                        } else {
                            _fileCopy(unzip, newFile, false, true);
                        }
                        unzip.closeEntry();
                    }
                    unzip.close();
                    inAppInstaller.close();
                    UIMessage.updateProgress(wait, 50, 100);
                    Map<String, Map<String, URL>> map = new LinkedHashMap<String, Map<String, URL>>();
                    URL url = new URL(rb.getString("extPathURL") + rb.getString("extInstallerJar"));
                    map.put(_getSysValue("os.name"), Collections.singletonMap(url.getPath(), url));
                    env = new JXAenvUtils();
                    if (OS_MAC.isEnv()) {
                        env.setJXAenvPath(installerPath + File.separator + rb.getString("installerAppName") + File.separator + "Contents" + File.separator + "Resources" + File.separator + "Java");
                    } else {
                        env.setJXAenvPath(installerPath);
                    }
                    env.setBigBuffer(true);
                    env = _installExtensions(env, map, false, new File(_findExtPath(false)));
                    if (OS_MAC.isEnv()) {
                        _runShell(new String[]{"/bin/chmod", "755", "\"" + installerPath + File.separator + rb.getString("installerAppName") + File.separator + "Contents" + File.separator + "MacOS" + File.separator + "sf3jswing-jxakernel\""}, false, true, false);
                    }
                    if (OS_LINUX.isEnv()) {
                        _runShell(new String[]{"/bin/chmod", "755", "\"" + installerPath + File.separator + rb.getString("installerAppName") + "\""}, false, true, false);
                    }
                    UIMessage.updateProgress(wait, 100, 100);
                    UIMessage.hideProgress(wait);
                }
            }
            String paths = "", sep = "";
            for (String path : envPaths) {
                paths += sep + path;
                sep = File.pathSeparator;
            }
            final String myPaths = paths;
            final String[] installFinishedTxt = new String[]{"<html><b>Successfully " + ((doIt & DO_UNINSTALL) != 0 ? "uninstalled" : "installed") + "</b> Java Extension(s) into ",
                ((doIt & DO_INSTALL) != 0 ? "<br><b>You need to <u>restart your browser</u> and the applet you were starting.</b><br>Notice : To uninstall, open a terminal/shell and run : <pre>java " + ExtensionsInstaller.class.getCanonicalName() + " -uninstall</pre> !" : "") + "</html>"
            };
            final JLabel installFinished = new JLabel(installFinishedTxt[0] + paths
                    + installFinishedTxt[1]);
            installFinished.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseExited(MouseEvent e) {
                    super.mouseExited(e);
                    installFinished.setText(installFinishedTxt[0] + myPaths + installFinishedTxt[1]);
                    installFinished.repaint();
                }

                @Override
                public void mouseEntered(MouseEvent e) {
                    super.mouseEntered(e);
                    installFinished.setText(installFinishedTxt[0] + "<u>" + myPaths + "</u>" + installFinishedTxt[1]);
                    installFinished.repaint();
                }

                @Override
                public void mouseClicked(MouseEvent e) {
                    super.mouseClicked(e);
                    for (String p : envPaths) {
                        try {
                            _goWeb(new File(p).toURI().toURL());
                        } catch (MalformedURLException ex) {
                            if (_debugSys) {
                                ex.printStackTrace();
                            }
                        }
                    }
                }
            });
            new UIMessage(true, installFinished, null, UIMessage.ENABLED_TYPE);
            if (OS_MAC.isEnv()) {
                InputStream inScript = ExtensionsInstaller.class.getResourceAsStream("/net/sf/jiga/xtended/kernel/restartSafari.applescript");
                File tmpScript = _createTempFile("appleScript_", _TMPDIRECTORY);
                _fileCopy(inScript, tmpScript, false, true);
                inScript.close();
                _runShell(new String[]{"osascript", tmpScript.getAbsolutePath()}, false, true, (setIt & SET_LOGTRACE) != 0);
            }
            if ((setIt & SET_KEEPALIVE) == 0) {
                System.exit(0);
            }
        } catch (Exception ex) {
            if (_debugSys) {
                ex.printStackTrace();
            }
            new UIMessage(true, new JLabel("<html><b>Failed to " + ((doIt & DO_UNINSTALL) != 0 ? "uninstall" : "install") + "</b> Java Extension(s) into " + env.getJXAenvPath() + " .<br>Click ok to see the stack trace ! <br> You may CAREFULLY delete recently modified files from " + _findExtPath(false) + " and " + _findExtPath(true) + ".</html>"), null, UIMessage.ERROR_TYPE);
            _popExceptionToUser(true, Thread.currentThread(), ex);
            if ((setIt & SET_KEEPALIVE) == 0) {
                System.exit(1);
            }
        }
    }
    static BitStack bits = new BitStack();
    static final int _DO = bits._newBitRange();
    static final int _SET = bits._newBitRange();
    /**
     * {@linkplain #_externalInstall(int)} install
     */
    public static final int DO_INSTALL = bits._newBit(_DO);
    /**
     * {@linkplain #_externalInstall(int)} uninstall
     */
    public static final int DO_UNINSTALL = bits._newBit(_DO);
    /**
     * {@linkplain #_externalInstall(int)} use local files only NO HTTP
     */
    public static final int SET_LOCAL = bits._newBit(_SET);
    /**
     * {@linkplain #_externalInstall(int)} don't exit Java on return
     */
    public static final int SET_KEEPALIVE = bits._newBit(_SET);
    /**
     * {@linkplain #_externalInstall(int)} launch the external applet installer
     */
    public static final int DO_APPLET = bits._newBit(_DO);
    /**
     * {@linkplain #_externalInstall(int)} launch the external application
     * installer
     */
    public static final int DO_APPLICATION = bits._newBit(_DO);
    /**
     * {@linkplain #_externalInstall(int)} enable the splash screen
     */
    public static final int SET_SPLASH = bits._newBit(_SET);
    /**
     * {@linkplain #_externalInstall(int)} try verbosing the install
     * (experimental)
     */
    public static final int SET_LOGTRACE = bits._newBit(_SET);

    /**
     * Usually when run by the extensions Java Plugin (v1.5), all the required
     * natives and jar resources of JXA environment are installed in
     * {@link #_findExtPath(boolean) extension path}.<br> Please, edit
     * build-youros-yourarch.xml for "extension.properties" ! <br> <br> extJars=
     * to set the jars to install as Java extensions <br>extInstallerJar= to set
     * the installer jar that will be run args are : <br><ul> <li>-local : use
     * local paths only</li>
     * <li>-uninstall : uninstalls the extensions from
     * {@link #_findExtPath(boolean) extension path}</li>
     * <li>-install : performs direct install (should be avoided and let the
     * user choose the method to install)</li> <li>-app : define the method
     * install to load an external application (recommended way)</li>
     * <li>-applet : define the method install to load an external applet</li>
     * <li>-log : ensure that full log of the install shows up to the user
     * (debugging)</li> <li>-nosplash : disables the splash screen</li>
     * <li>-keepAlive : do not exit when the method returns</li> </ul> By
     * default the Java SE installer is run in the current process with a splash
     * screen.
     */
    public static void main(String[] args) {
        int params = 0;
        params = SET_SPLASH | DO_INSTALL;
        if (args instanceof String[]) {
            for (String arg : args) {
                if (arg.equals("-local")) {
                    params |= SET_LOCAL;
                }
                if (arg.equals("-uninstall")) {
                    params = params & ~bits._getMask(_DO) | DO_UNINSTALL;
                }
                if (arg.equals("-nosplash")) {
                    params &= ~(SET_SPLASH & bits._getMask(_SET));
                }
                if (arg.equals("-keepAlive")) {
                    params |= SET_KEEPALIVE;
                }
                if (arg.equals("-applet")) {
                    params = params & ~bits._getMask(_DO) | DO_APPLET;
                }
                if (arg.equals("-install")) {
                    params = params & ~bits._getMask(_DO) | DO_INSTALL;
                }
                if (arg.equals("-log")) {
                    params |= SET_LOGTRACE;
                }
                if (arg.equals("-app")) {
                    params = params & ~bits._getMask(_DO) | DO_APPLICATION;
                }
            }
        }
        _externalInstall(params);
    }

    /**
     * adds the
     * {@link #_getJXAExtenvFilesFromBase(java.util.Map, java.net.URL, boolean, boolean) jxa extension files}
     * to the urls array. Environment settings are kept from the specified
     * JXAenvUtils instance.
     */
    public static URL[] _getURLClassLoaderURLS(JXAenvUtils env) {
        /*if (extDir == null) {
         extDir = new File(_findExtPath(false));
         }*/
        /**
         * add extensions jar files
         */
        try {
            Map<String, URL> jarMap = _getJXAExtenvFiles(env.envJars, env.isLocal(), false).get(_getSysValue("os.name"));
            /**
             * for classloading
             */
            List<URL> classloaderJars = new ArrayList<URL>();
            /**
             * recover previous jars
             */
            synchronized (jarMap) {
                for (URL jarUrl : jarMap.values()) {
                    try {
                        if (env.keepReadingOnRemoteJarResources) {
                            classloaderJars.add(jarUrl);
                        } else {
                            classloaderJars.add(env.findEnvInstalledFile(jarUrl).toURI().toURL());
                        }
                    } catch (MalformedURLException ex) {
                        ex.printStackTrace();
                    }
                }
            }
            return classloaderJars.toArray(new URL[]{});
        } catch (Exception ex) {
            if (_debugSys) {
                ex.printStackTrace();
            }
            return null;
        }
    }

    /**
     * look for any "Class-Path:" manifest .jar-entry (folder-classpaths are
     * ignored)
     */
    public static Map<String, URL> _getJarClasspathJars(URL jarUrl) throws IOException {
        URL u = new URL("jar:" + jarUrl + "!/");
        Map<String, URL> jarUrls = new LinkedHashMap<String, URL>();
        URLConnection jar = u.openConnection();
        if (jar instanceof JarURLConnection) {
            /**
             *
             */
            Manifest m = ((JarURLConnection) jar).getManifest();
            if (m != null) {
                Attributes a = m.getMainAttributes();
                if (a != null) {
                    String classpath = a.getValue(Attributes.Name.CLASS_PATH);
                    if (classpath != null) {
                        for (String path : classpath.split("\\s")) {
                            if (path != null && path.endsWith(".jar")) {
                                URL nJarUrl = new URL(jarUrl.toString().substring(0, jarUrl.toString().lastIndexOf("/")) + "/" + path);
                                String name = FileHelper._getURLFilename(u);
                                jarUrls.put(name, nJarUrl);
                            }
                        }
                    }
                }
            }
        }
        return jarUrls;
    }
}
