/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.jiga.xtended.kernel;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectStreamClass;
import java.io.Serializable;
import java.lang.IllegalArgumentException;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.URL;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import net.sf.jiga.xtended.JXAException;

/**
 *
 * @author www.b23prodtm.info
 */
public class ExtensionsClassLoader {

    private ExtensionsClassLoader(JXAenvUtils env) {
        ECL = new ExtensionsClassLoaderImpl(env);
        env.classLoader = ECL;
    }

    @Deprecated
    private ExtensionsClassLoader(JXAenvUtils env, boolean keepRemote) {
        env.keepReadingOnRemoteJarResources = keepRemote;
        ECL = new ExtensionsClassLoaderImpl(env);
        env.classLoader = ECL;
    }
    static AccessControlContext _acc = AccessController.getContext();
    private static ExtensionsClassLoader _INSTANCE;
    private ClassLoader ECL;

    public static ExtensionsClassLoader getInstance() {
        if (!isResourceLoaded()) {
            throw new JXAException(JXAException.LEVEL.SYSTEM, "classloader instance not loaded yet !! ECL INSTANCE is not ready. !!");
        }
        return _INSTANCE;
    }

    public ClassLoader getClassLoader() {
        return ECL;
    }

    /**
     * Loads a specific classPath context .
     *
     * @param topUrls urls classpath or null if default
     * @param env the environment to read from
     * ({@link JXAenvUtils#getJXAenvPath() classpath}). Ensure to have correctly
     * set {@link JXAenvUtils#setJXAenvPath() envpath} before !
     * @discuss seems like Mac OS JDK doesn'y store static field properly on
     * certain circumstances (booting runtime)
     */
    public static void _load(final JXAenvUtils env) {
        if (isResourceLoaded()) {
            throw new JXAException(JXAException.LEVEL.SYSTEM, "trying to change environment classloader, but it was already loaded");
        }
        _INSTANCE = new ExtensionsClassLoader(env);
        env._switchToClassLoader(_INSTANCE.ECL);
        AccessController.doPrivileged(new PrivilegedAction() {
            public Object run() {
                ThreadWorks.setGlobalClassLoaderContext(_INSTANCE.ECL);
                return null;
            }
        }, _acc);
    }

    @Deprecated
    public static void _load(final JXAenvUtils env, final boolean keepRemote) {
        env.keepReadingOnRemoteJarResources = keepRemote;
        _load(env);
    }

    /**
     * force resolving
     */
    public static Class<?> forName(String name) throws ClassNotFoundException {
        return Class.forName(name, true, _INSTANCE.ECL);
        /*return ((ExtensionsClassLoaderImpl) INSTANCE).loadClass(name, true);*/
    }

    /**
     *
     */
    public static InvocationHandler _proxyClassHandler(Object target) {
        return new ProxyClassHandler(target);
    }

    /**
     * Provides a way to cast external loaded classes (e.g. with
     * ExtensionsClassLoader) to kernel interfaces.
     *
     * @param clazz interfaces (ONLY, no class or primitives) to be casted to
     */
    public static <T> T _proxyClass(Object target, Class<T> clazz) {
        if (!clazz.isInterface()) {
            throw new IllegalArgumentException("Class " + clazz + " is not an interface");
        }
        return target == null ? null : (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz},
                _proxyClassHandler(target));
    }

    public static boolean isResourceLoaded() {
        return _INSTANCE != null;
    }

    /**
     * This class invokes corresponding methods on classes that were loaded by
     * the ExtensionsClassLoaderImpl for interfaces that are declared by another
     * classloader. Then it can be used by a ProxyClass to make correct class
     * castings.
     */
    static class ProxyClassHandler implements InvocationHandler, Serializable {

        /**
         * serial version uid of this class
         */
        private static final long serialVersionUID = 2323;
        Object target;

        public ProxyClassHandler(Object target) {
            this.target = target;
        }

        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            return target.getClass().getMethod(method.getName(), method.getParameterTypes()).invoke(target, args);
        }
    }

    /**
     * This is an Object deserializer used to resolve classes from a stream
     * ({@link JXAenvUtils.env#APP_REMOTE} must be on)
     *
     * @see SpritesCacheManager#JXA_ECLIO
     */
    class ObjectInputStream extends java.io.ObjectInputStream {

        public ObjectInputStream(InputStream in) throws IOException {
            super(in);
        }

        @Override
        protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
            if (!JXAenvUtils.env.APP_REMOTE.isEnv()) {
                if (DebugMap._getInstance().isDebugLevelEnabled(DebugMap._getInstance()._VOID)) {
                    System.out.println("resolveClass classloader " + Thread.currentThread().getContextClassLoader());
                }
                return super.resolveClass(desc);
            }
            if (DebugMap._getInstance().isDebugLevelEnabled(DebugMap._getInstance()._VOID)) {
                System.out.println("resolveClass classloader " + ECL);
            }
            String name = desc.getName();
            try {
                return Class.forName(name, false, ECL);
            } catch (ClassNotFoundException ex) {
                if (DebugMap._getInstance().isDebugLevelEnabled(DebugMap._getInstance()._VOID)) {
                    ex.printStackTrace();
                }
                return super.resolveClass(desc);
            }
        }

        @Override
        protected Class<?> resolveProxyClass(String[] interfaces) throws IOException, ClassNotFoundException {
            if (!JXAenvUtils.env.APP_REMOTE.isEnv()) {
                if (DebugMap._getInstance().isDebugLevelEnabled(DebugMap._getInstance()._VOID)) {
                    System.out.println("resolveProxyClass classloader " + Thread.currentThread().getContextClassLoader());
                }
                return super.resolveProxyClass(interfaces);
            }
            if (DebugMap._getInstance().isDebugLevelEnabled(DebugMap._getInstance()._VOID)) {
                System.out.println("resolveProxyClass classloader " + ECL);
            }
            Class<?>[] classes = new Class<?>[interfaces.length];
            for (int i = 0; i < interfaces.length; i++) {
                classes[i] = Class.forName(interfaces[i], false, ECL);
            }
            try {
                return Proxy.getProxyClass(ECL, classes);
            } catch (IllegalArgumentException ex) {
                if (DebugMap._getInstance().isDebugLevelEnabled(DebugMap._getInstance()._VOID)) {
                    ex.printStackTrace();
                }
                return super.resolveProxyClass(interfaces);
            }
        }
    }
}
