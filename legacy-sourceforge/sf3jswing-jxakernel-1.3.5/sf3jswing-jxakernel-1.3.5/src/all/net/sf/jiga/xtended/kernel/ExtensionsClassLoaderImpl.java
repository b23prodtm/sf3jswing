package net.sf.jiga.xtended.kernel;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import static java.lang.ClassLoader.getSystemClassLoader;
import static java.lang.ClassLoader.getSystemResource;
import static java.lang.ClassLoader.getSystemResources;
import java.net.JarURLConnection;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.security.SecureClassLoader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import net.sf.jiga.xtended.ui.Ant;
import static net.sf.jiga.xtended.kernel.JXAenvUtils.*;

/**
 * this class enables the user to access and load the ExtensionsInstaller
 * classes located outside the classpath (in a temporary folder), which is by
 * default the path return by
 * {@linkplain ExtensionsInstaller#_findExtPath(boolean) _findExtPath(false)}.
 * Unlike the original ClassLoader's that are packaged with the JDK, this
 * implementation fully relies on its own classpath, BEFORE to fallback on
 * System cloaders. This is NOT the general contract of JDK cloader
 * implementations. NOTICE : It loads every classes, every resources and every
 * natives, on-the-fly. DEBUG with system property "jxa.debugECL=true"
 */
public class ExtensionsClassLoaderImpl extends SecureClassLoader implements Debugger {

    /**
     * @see Ant#JXA_DEBUG_ECL
     * @see DebugMap#setDebugLevelEnabled(boolean, net.sf.jiga.xtended.kernel.Level) 
     */
    public static final Level _DEBUG = DebugMap._getInstance().newDebugLevel();

    static {
        DebugMap._getInstance().associateDebugLevel(ExtensionsClassLoaderImpl.class, _DEBUG);
        DebugMap._getInstance().setDebuggerEnabled(JXAenvUtils._getSysBoolean(Ant.JXA_DEBUG_ECL), ExtensionsClassLoaderImpl.class, _DEBUG);
    }
    protected AccessControlContext acc;
    protected URL[] extClassPathUrl;

    /**
     * creates a classloader that will load classes from the
     * {@link ExtensionsInstaller#_getURLClassLoaderURLS(java.io.File) extDir/jigaxtended .jars}
     * and moreURLs .jars .
     *
     * @param extDir base folder for installing extensions (usually user.dir)
     * @param parent a ClassLoader set as parent
     * @param env jar-urls (only jars file names are read, they are
     * supposed to be {@link JXAenvUtils#addEnvJar(String,URL) installed}
     * locally into extDir)
     */
    public ExtensionsClassLoaderImpl(JXAenvUtils env) {
        super(env.getEnvClassLoader());
        extClassPathUrl = ExtensionsInstaller._getURLClassLoaderURLS(env);
        acc = AccessController.getContext();
        if (isDebugEnabled()) {
            String s = "new instance (EXTDIR=" + env.getJXAenvPath() + ") with parent ClassLoader " + env.getEnvClassLoader();
            for (URL url : extClassPathUrl) {
                s += "\nresource url : " + url;
            }
            System.out.println(log(s,LVL.SYS_NOT));
        }
    }

    /**
     * @return byte array fro the given url contents
     */
    public byte[] getData(final URL jarUrl) {
        return AccessController.doPrivileged(new PrivilegedAction<byte[]>() {
            public byte[] run() {
                return _getData(jarUrl);
            }
        }, acc);
    }

    protected byte[] _getData(URL jarUrl) {
        if (jarUrl != null) {
            try {
                JarURLConnection jar = (JarURLConnection) jarUrl.openConnection();
                BufferedInputStream bis = new BufferedInputStream(jar.getInputStream());
                ByteBuffer b = ByteBuffer.allocate(jar.getContentLength()).order(ByteOrder.nativeOrder());
                byte[] r = new byte[512];
                int rB = 0;
                while ((rB = bis.read(r)) != -1) {
                    b.put(r, 0, rB);
                }
                bis.close();
                return b.array();
            } catch (IOException ex) {
                if (isDebugEnabled()) {
                    ex.printStackTrace();
                }
            }
        }
        return null;
    }

    private String getAttribute(Attributes main, Attributes spec, Attributes.Name name) {
        String v = null;
        if (spec != null) {
            v = spec.getValue(name);
        }
        if (v == null && main != null) {
            v = main.getValue(name);
        }
        return v;
    }

    @Override
    protected Class<?> findClass(final String name) throws ClassNotFoundException {
        if (isDebugEnabled()) {
            System.out.println(log("ECL findClass " + name, LVL.SYS_NOT));
        }
        Class<?> c = cache.get(name);
        if (c == null) {
            if (name instanceof String) {
                String jarEntry = name.replaceAll("\\.", "/") + ".class";
                if (!name.startsWith("java.")) {
                    URL res = findResource(jarEntry);
                    if (res != null) {
                        byte[] classData = getData(res);
                        if (classData != null) {
                            /**
                             * * BEGIN SOURCE :
                             * http://kickjava.com/src/org/netbeans/JarClassLoader.java.htm
                             */
                            String pname = name.substring(0, name.lastIndexOf("."));
                            boolean attributes = false;
                            if (getPackage(pname) == null) {
                                JarURLConnection juc = _findJarConnection(jarEntry);
                                if (juc != null) {
                                    Manifest m;
                                    try {
                                        m = juc.getJarFile().getManifest();
                                        if (m != null) {
                                            Attributes aMain = m.getMainAttributes();
                                            String path = pname.replace('.', '/').concat("/");
                                            Attributes a = m.getAttributes(path);
                                            definePackage(pname, getAttribute(aMain, a, Attributes.Name.SPECIFICATION_TITLE), getAttribute(aMain, a, Attributes.Name.SPECIFICATION_VERSION), getAttribute(aMain, a, Attributes.Name.SPECIFICATION_VENDOR), getAttribute(aMain, a, Attributes.Name.IMPLEMENTATION_TITLE), getAttribute(aMain, a, Attributes.Name.IMPLEMENTATION_VERSION), getAttribute(aMain, a, Attributes.Name.IMPLEMENTATION_VENDOR), "true".equalsIgnoreCase(getAttribute(aMain, a, Attributes.Name.SEALED)) ? juc.getJarFileURL() : null);
                                            if (isDebugEnabled()) {
                                                System.out.println("ECL defined package " + pname + "(" + path + ")" + getPackage(pname).toString());
                                            }
                                            attributes = true;
                                        }
                                    } catch (IOException ex) {
                                        if (isDebugEnabled()) {
                                            ex.printStackTrace();
                                        }
                                    }
                                }
                                if (!attributes) {
                                    definePackage(pname, null, null, null, null, null, null, null);
                                }
                                /**
                                 * END SOURCE*
                                 */
                            }
                            c = defineClass(name, classData, 0, classData.length, getClass().getProtectionDomain());
                            if (isDebugEnabled()) {
                                System.out.println(log("ECL successfully defined class " + name, LVL.SYS_NOT));
                            }
                        }
                    }
                } else {
                    throw new ClassNotFoundException(log("ECL java.* class " + name + " was given", LVL.SYS_NOT));
                }
            } else {
                throw new ClassNotFoundException(log("ECL class null name was given", LVL.SYS_NOT));
            }
        }
        if (c != null) {
            cache.put(name, c);
            return c;
        } else {
            throw new ClassNotFoundException(log("ECL class " + name + " was not found", LVL.SYS_NOT));
        }
    }

    protected JarURLConnection _findJarEntryConnection(final URL jarUrl, final String resourceName) {
        return AccessController.doPrivileged(new PrivilegedAction<JarURLConnection>() {
            public JarURLConnection run() {
                JarURLConnection jarUrlConn = __findJarEntryConnection(jarUrl, resourceName);
                currentSeek.remove();
                return jarUrlConn;
            }
        }, acc);
    }
    /**
     * essential cache
     */
    private Map<String, Class<?>> cache = Collections.synchronizedMap(new HashMap<String, Class<?>>());
    /**
     * enforces this Classloader to load in first place all resources
     * (getParent() comes next)
     */
    public final static String JXA_ECLFIRST = "jxa.ecl.first";

    private static boolean isFirst() {
        return Boolean.getBoolean(JXA_ECLFIRST);
    }

    @Override
    protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        if (isDebugEnabled()) {
            System.out.println("ECL loadClass " + name + " resolve " + resolve);
        }
        ClassNotFoundException cnf = null;
        Class<?> c = cache.containsKey(name) ? cache.get(name) : findLoadedClass(name);
        if (!isFirst()) {
            if (c == null) {
                try {
                    if (getParent() != null) {
                        c = getParent().loadClass(name);
                    } else {
                        c = getSystemClassLoader().loadClass(name);
                    }
                } catch (ClassNotFoundException ex) {
                    cnf = ex;
                }
            }
        }
        if (c == null) {
            try {
                c = findClass(name);
            } catch (ClassNotFoundException ex) {
                cnf = ex;
            }
        }
        if (isFirst()) {
            if (c == null) {
                try {
                    if (getParent() != null) {
                        c = getParent().loadClass(name);
                    } else {
                        c = getSystemClassLoader().loadClass(name);
                    }
                } catch (ClassNotFoundException ex) {
                    cnf = ex;
                }
            }
        }
        if (c == null) {
            throw new ClassNotFoundException(name, cnf);
        } else {
            if (isDebugEnabled()) {
                System.out.println("System successfully loaded class " + name);
            }
        }
        if (resolve) {
            resolveClass(c);
        }
        return c;
    }
    private ThreadLocal<List<URL>> currentSeek = new ThreadLocal<List<URL>>() {
        @Override
        protected List<URL> initialValue() {
            return new ArrayList<URL>();
        }
    };

    /**
     * will try in the current jarUrl<br>
     * will be recursively called if a Manifest is including the
     * {@linkplain java.util.jar.Attributes.Name#CLASS_PATH Class-Path:}
     * attribute
     *
     * @param resourceName resource in the jar. beginning with "/", it denotes
     * the root of the jar
     * @param jarUrl url of a jar file. (not beginning with "jar:" )
     */
    protected JarURLConnection __findJarEntryConnection(URL jarUrl, String resourceName) {
        JarURLConnection entryUrl = null;
        try {
            if (jarUrl == null || !jarUrl.toString().endsWith(".jar")) {
                return null;
            }
            /**
             * look for an existing entry in the jarUrl
             */
            URL u = new URL(new URL("jar:" + jarUrl + "!/"), resourceName);
            URLConnection jar = u.openConnection();
            if (jar instanceof JarURLConnection) {
                if (((JarURLConnection) jar).getJarEntry() != null) {
                    entryUrl = (JarURLConnection) jar;
                }
            }
        } catch (IOException ex) {
            if (isDebugEnabled() && DebugMap._getInstance().isDebugLevelEnabled(DebugMap._getInstance()._VOID)) {
                System.err.println(ex.getMessage());
            }
        } finally {
            try {
                if (entryUrl == null && jarUrl != null) {
                    Map<String, URL> jarUrls = ExtensionsInstaller._getJarClasspathJars(jarUrl);
                    for (URL ju : jarUrls.values()) {
                        if (!currentSeek.get().contains(ju)) {
                            entryUrl = __findJarEntryConnection(ju, resourceName);
                        }
                    }
                }
            } catch (IOException ex) {
                if (isDebugEnabled() && DebugMap._getInstance().isDebugLevelEnabled(DebugMap._getInstance()._VOID)) {
                    ex.printStackTrace();
                }
            } finally {
                return entryUrl;
            }
        }
    }

    @Override
    public InputStream getResourceAsStream(String name) {
        try {
            URL u = getResource(name);
            if (u != null) {
                return u.openStream();
            } else {
                return null;
            }
        } catch (IOException ex) {
            if (isDebugEnabled()) {
                ex.printStackTrace();
            }
            return null;
        }
    }

    /**
     * find one URL to access resource name.
     */
    @Override
    public URL getResource(String name) {
        if (isDebugEnabled()) {
            System.out.println("ECL look for resource : " + name);
        }
        URL r = null;
        if (!isFirst()) {
            if (getParent() != null) {
                r = getParent().getResource(name);
            } else {
                r = getSystemResource(name);
            }
        }
        if (r == null) {
            r = findResource(name);
        }
        if (isFirst() && r == null) {
            if (getParent() != null) {
                r = getParent().getResource(name);
            } else {
                r = getSystemResource(name);
            }
        }
        if (r != null) {
            if (isDebugEnabled()) {
                System.err.println("System found resource " + name);
            }
        }
        return r;
    }

    /**
     * @return null if no resource entry was found in this classLoader
     */
    protected JarURLConnection _findJarConnection(String resourceName) {
        JarURLConnection jarUrl = null;
        while (resourceName.startsWith("/") && resourceName.length() > 1) {
            resourceName = resourceName.substring(1);
        }
        for (URL url : extClassPathUrl) {
            jarUrl = _findJarEntryConnection(url, resourceName);
            if (jarUrl != null) {
                break;
            }
        }
        return jarUrl;
    }

    @Override
    protected URL findResource(String name) {
        JarURLConnection juc = _findJarConnection(name);
        if (juc != null) {
            URL jarUrl = juc.getURL();
            if (isDebugEnabled()) {
                System.out.println(log("found resource : " + jarUrl, LVL.SYS_NOT));
            }
            return jarUrl;
        } else {
            if (isDebugEnabled()) {
                System.out.println(log("resource not found : " + name, LVL.SYS_NOT));
            }
            return null;
        }
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
        LinkedList<URL> r = new LinkedList<URL>();
        /**
         * will enum all possible URL's
         */
        Enumeration<URL> e = null;
        if (!isFirst()) {
            if (getParent() != null) {
                e = getParent().getResources(name);
            } else {
                e = getSystemResources(name);
            }
            while (e.hasMoreElements()) {
                URL u = e.nextElement();
                if (!r.contains(u)) {
                    r.add(u);
                }
            }
        }
        e = findResources(name);
        while (e.hasMoreElements()) {
            r.add(e.nextElement());
        }
        if (isFirst()) {
            if (getParent() != null) {
                e = getParent().getResources(name);
            } else {
                e = getSystemResources(name);
            }
            while (e.hasMoreElements()) {
                URL u = e.nextElement();
                if (!r.contains(u)) {
                    r.add(u);
                }
            }
        }
        return Collections.enumeration(r);
    }

    @Override
    protected Enumeration<URL> findResources(String resourceName) throws IOException {

        LinkedList<URL> l = new LinkedList<URL>();
        while (resourceName.startsWith("/") && resourceName.length() > 1) {
            resourceName = resourceName.substring(1);
        }
        for (URL url : extClassPathUrl) {
            JarURLConnection u = _findJarEntryConnection(url, resourceName);
            if (u != null) {
                l.add(u.getURL());
            }
        }
        return Collections.enumeration(l);
    }

    @Override
    protected String findLibrary(final String libname) {
        File ret = null;
        try {
            ret = AccessController.doPrivileged(new PrivilegedExceptionAction<File>() {
                /**
                 * look for natives in the usual local install folder
                 */
                public File run() throws MalformedURLException, URISyntaxException {
                    Map<URL, Boolean> map = ExtensionsInstaller._getJXANatenvFiles(null, new File(ExtensionsInstaller._findExtPath(true)).toURI().toURL()).get(_getSysValue("os.name")).get(libname);
                    if (map instanceof Map) {
                        return new File(map.keySet().iterator().next().toURI());
                    } else {
                        return null;
                    }
                }
            }, acc);
        } catch (PrivilegedActionException ex) {
            if (isDebugEnabled()) {
                ex.getException().printStackTrace();
            }
        } finally {
            return ret != null ? ret.getAbsolutePath() : null;
        }
    }

    public boolean isDebugEnabled() {
        return DebugMap._getInstance().isDebuggerEnabled(ExtensionsClassLoaderImpl.class);
    }

    public void setDebugEnabled(boolean b) {
        DebugMap._getInstance().setDebuggerEnabled(b, ExtensionsClassLoaderImpl.class, _DEBUG);
    }
}
