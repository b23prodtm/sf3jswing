/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.jiga.xtended.kernel;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilePermission;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.channels.FileChannel;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Vector;
import java.util.regex.Matcher;
import javax.imageio.ImageIO;
import net.sf.jiga.xtended.ui.UIMessage;
import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.io.FileUtils;

/**
 *
 * @author tiana
 */
public class FileHelper {

        /**
         * writeable java.io.tmpdir or "." directory
         */
        public static File _USERHOMEDIRECTORY = _findHomeDirectory();
        /*
         * private final static Set<SoftReference<File>> _tmpFiles =
         * Collections.synchronizedSet(new HashSet<SoftReference<File>>());
         */
        /**
         *
         */
        public static File _USERHOMESTOREDIRECTORY = _makeStoreDirectory(_USERHOMEDIRECTORY);
        /**
         *
         */
        public static final int FILE_READ = 1;
        /**
         *
         */
        public static Monitor fileThreads = new Monitor();
        /**
         * all temporary files created from JXA should end with this suffix
         *
         * @default .jxa
         */
        public static final String _tmpFilesSuffix = ".jxa.tmp";
        /**
         *
         */
        private static boolean _ImageIOUseCache = false;
        private static final String _key_reading = "reading";
        /**
         *
         */
        public static final int FILE_EXECUTE = 8;
        /**
         *
         */
        public static final int FILE_DELETE = 4;
        /**
         * for file i/o operations
         *
         * @default 65536 (64k)
         *
         * @see RandomAccessFile
         */
        public static int _SMALLBUFFFER_SIZE = 65536;
        /**
         * writeable user.home or "." directory
         */
        public static File _TMPDIRECTORY = _findTempDirectory();
        /**
         * writeable directory for native libraries
         */
        public static File _LIBDIRECTORY = new File(ExtensionsInstaller._findExtPath(true));
        private static final String _key_read_pgs_val = "value";
        /**
         *
         */
        private static List<Long> abortedThreads = new Vector<Long>();
        private static final String _key_read_string = "string";
        /**
         * timeout delay for streams connections in ms
         *
         * @default 5'000 ms
         */
        public static long _streamsTimeout = 5000;
        /**
         *
         */
        public static File _ImageIOCache = new File(_TMPDIRECTORY, "JavaImageIO");
        /**
         * the tmp directories set
         */
        protected static final Set<File> _tmpDir = Collections.synchronizedSet(new HashSet<File>(Collections.singleton(_TMPDIRECTORY)));
        /**
         * for read/write from url downloads
         *
         * @default 65536 * 16 = 1MB
         */
        public static int _BIGBUFFER_SIZE = 65536 * 16;
        /**
         *
         */
        public static final int FILE_WRITE = 2;
        private static final String _key_read_pgs_max = "max";
        /**
         * writeable java.ext.dirs directory
         */
        public static File _EXTDIRECTORY = new File(ExtensionsInstaller._findExtPath(false));

        /**
         * CAUTION : this function will attempt to delete all files in the
         * specified directory (but not the sub-dirs)
         *
         * @param suffix the suffix for filenames (".[a-z|0-9]*")
         * @param dir
         * @throws IllegalArgumentException
         */
        public static void _eraseTmpFiles(final String suffix, File dir) throws IllegalArgumentException {
                if (!suffix.matches("\\..*")) {
                        throw new IllegalArgumentException("suffix must be \".someextension\"");
                }
                if (dir.isDirectory()) {
                        for (File f : dir.listFiles(new FilenameFilter() {
                                public boolean accept(File dir, String name) {
                                        return name.endsWith(suffix);
                                }
                        })) {
                                _eraseTmpFiles(suffix, f);
                        }
                } else if (dir.isFile() && _accessFilePermitted(dir, FILE_DELETE) && dir.getName().endsWith(suffix)) {
                        dir.delete();
                }
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
         */
        public static String _quotedFileSeparator() {
                String splitter = File.separator;
                if (splitter.equals("\\")) {
                        splitter = "\\\\";
                }
                return splitter;
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
         */
        public static void _fileCopy(File src, File dst, boolean progressBar, boolean bigBuffer) throws FileNotFoundException, IOException {
                long id = progressBar ? UIMessage.displayProgress(0, 100, null) : 0;
                _fileCopy(src, dst, progressBar, id, bigBuffer);
                if (progressBar) {
                        UIMessage.kill(id);
                }
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
         */
        public static void _fileCopy(InputStream src, File dst, boolean progressBar, boolean bigBuffer) throws FileNotFoundException, IOException {
                long id = progressBar ? UIMessage.displayProgress(0, 100, null) : 0;
                _fileCopy(src, dst, progressBar, id, bigBuffer);
                if (progressBar) {
                        UIMessage.kill(id);
                }
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
         */
        public static void _fileCopy(URL src, File dst, boolean progressBar, boolean bigBuffer) throws FileNotFoundException, IOException, HttpException {
                long id = progressBar ? UIMessage.displayProgress(0, 100, null) : 0;
                _fileCopy(src, dst, progressBar, id, bigBuffer);
                if (progressBar) {
                        UIMessage.kill(id);
                }
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
         */
        public static void _fileCopy(final File src, final File dst, final boolean progressBar, final long progressBarID, final boolean bigBuffer) throws IOException {
                if (JXAenvUtils._debugSys) {
                        System.out.println("Local file copy");
                }
                try {
                        AccessController.doPrivileged(new PrivilegedExceptionAction() {
                                public Object run() throws IOException {
                                        __fileCopy(src, dst, progressBar, progressBarID, bigBuffer);
                                        return null;
                                }
                        }, JXAenvUtils.acc);
                } catch (PrivilegedActionException ex) {
                        throw (IOException) ex.getException();
                }
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
         */
        public static void _fileCopy(final InputStream src, final File dst, final boolean progressBar, final long progressBarID, final boolean bigBuffer) throws IOException {
                if (JXAenvUtils._debugSys) {
                        System.out.println("InputStream copy");
                }
                try {
                        AccessController.doPrivileged(new PrivilegedExceptionAction() {
                                public Object run() throws IOException {
                                        __fileCopy(src, dst, progressBar, progressBarID, bigBuffer);
                                        return null;
                                }
                        }, JXAenvUtils.acc);
                } catch (PrivilegedActionException ex) {
                        throw (IOException) ex.getException();
                }
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
         */
        public static void _fileCopy(final URL src, final File dst, final boolean progressBar, final long progressBarID, final boolean bigBuffer) throws IOException {
                if (JXAenvUtils._debugSys) {
                        System.out.println("URL copy");
                }
                try {
                        AccessController.doPrivileged(new PrivilegedExceptionAction() {
                                public Object run() throws IOException {
                                        __fileCopy(src, dst, progressBar, progressBarID, bigBuffer);
                                        return null;
                                }
                        }, JXAenvUtils.acc);
                } catch (PrivilegedActionException ex) {
                        throw (IOException) ex.getException();
                }
        }

        /**
         *
         *
         * @param fd
         */
        public static void _makeReadable(File fd) {
                __makeReadable(fd, false);
        }

        /**
         *
         * @param fd the value of fd
         * @param ownerOnly the value of ownerOnly
         */
        private static void __makeWritable(File fd, boolean ownerOnly) {
                if (!_accessFilePermitted(fd, FILE_WRITE) && _accessFilePermitted(fd, FILE_DELETE)) {
                        try {
                                if (fd.getAbsolutePath().endsWith(File.separator)) {
                                        fd.mkdirs();
                                }
                                if (fd.isDirectory()) {
                                        return;
                                }
                                if (!fd.exists()) {
                                        fd.getParentFile().mkdirs();
                                        fd.createNewFile();
                                        return;
                                }
                                File t = File.createTempFile("", _tmpFilesSuffix);
                                ___rawfileCopy(fd, t, true);
                                fd.delete();
                                ___rawfileCopy(t, fd, true);
                                t.delete();
                        } catch (IOException ex) {
                                if (JXAenvUtils._debugSys) {
                                        ex.printStackTrace();
                                }
                        }
                }
        }

        /**
         * finds a directory that is allowed to read, write and execute.
         *
         * @param freeList
         * @return
         */
        public static File _findFreeDirectory(List<File> freeList) {
                File freeDir = null;
                for (File f : freeList) {
                        if (f.isDirectory()) {
                                File access = f;
                                if (_accessFilePermitted(access, FILE_READ | FILE_WRITE)) {
                                        freeDir = f;
                                        break;
                                }
                        }
                }
                if (freeDir == null) {
                        if (JXAenvUtils._debugSys) {
                                System.err.println(JXAenvUtils.log("Sorry ! It's unable to find a writeable directory. Leaving now.", JXAenvUtils.LVL.SYS_ERR));
                        }
                        System.exit(1);
                }
                return freeDir;
        }

        /**
         *
         * @param fd the value of fd
         * @param ownerOnly the value of ownerOnly
         */
        private static void __makeReadable(File fd, boolean ownerOnly) {
                if (!JXAenvUtils.env.JAVA_VERSION_5.isEnv()) {
                        try {
                                JXAenvUtils._callback("setReadable", fd, new Object[]{true, ownerOnly}, new Class[]{boolean.class, boolean.class});
                        } catch (NoSuchMethodException ex) {
                                ex.printStackTrace();
                        } catch (IllegalAccessException ex) {
                                ex.printStackTrace();
                        } catch (InvocationTargetException ex) {
                                ex.printStackTrace();
                        }
                } else if (DebugMap._getInstance().isDebugLevelEnabled(DebugMap._getInstance()._VOID)) {
                        System.out.println("setReadable not available from this version of Java. " + fd);
                }
        }

        /**
         *
         *
         * @param fd
         */
        public static void _makeWritable(File fd) {
                __makeWritable(fd, false);
        }

        /**
         *
         *
         * @param fd
         */
        public static void _makeReadableOwnerOnly(File fd) {
                __makeReadable(fd, true);
        }

        /**
         *
         *
         * @param fd
         */
        public static void _makeWritableOwnerOnly(File fd) {
                __makeWritable(fd, true);
        }

        /**
         *
         *
         * @param s
         * @param d
         * @throws IOException
         */
        public static void ___rawfileCopy(File s, File d, boolean syncFlush) throws IOException {
                FileChannel source = null;
                FileChannel destination = null;
                if (!d.exists()) {
                        d.getParentFile().mkdirs();
                        d.createNewFile();
                }
                source = new FileInputStream(s).getChannel();
                destination = new FileOutputStream(d).getChannel();
                destination.transferFrom(source, 0, source.size());
                source.close();
                destination.force(syncFlush);
                destination.close();
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
         */
        public static boolean _accessFilePermitted(final File file, final int fileMode) {
                return AccessController.doPrivileged(new PrivilegedAction<Boolean>() {
                        public Boolean run() {
                                return __accessFilePermitted(file, fileMode);
                        }
                }, JXAenvUtils.acc);
        }

        /**
         *
         * @param url the value of url
         */
        public static String _getURLFilename(URL url) {
                return url.getFile().substring(url.getFile().lastIndexOf("/") + 1);
        }

        /**
         *
         * @param base the value of base
         */
        private static File _makeStoreDirectory(final File base) {
                try {
                        return AccessController.doPrivileged(new PrivilegedExceptionAction<File>() {
                                public File run() throws IOException {
                                        String basePath = base.getPath();
                                        if (!base.isDirectory()) {
                                                basePath = base.getParent();
                                        }
                                        File store = new File(basePath + File.separator + ".JXAStore");
                                        store.mkdirs();
                                        _makeWritable(new File(basePath));
                                        return store;
                                }
                        }, JXAenvUtils.acc);
                } catch (PrivilegedActionException ex) {
                        if (JXAenvUtils._debugSys) {
                                ex.getException().printStackTrace();
                        }
                        return null;
                }
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
         */
        static boolean __accessFilePermitted(File file, int fileMode) {
                if (file == null || fileMode == 0) {
                        return false;
                }
                if (!file.exists()) {
                        return false;
                }
                Vector<String> actions = new Vector<String>();
                if ((fileMode & FILE_READ) != 0) {
                        actions.add("read");
                }
                if ((fileMode & FILE_WRITE) != 0) {
                        actions.add("write");
                }
                if ((fileMode & FILE_DELETE) != 0) {
                        actions.add("delete");
                }
                if ((fileMode & FILE_EXECUTE) != 0) {
                        actions.add("execute");
                }
                String mode = "";
                String sep = "";
                for (String action : actions) {
                        mode += sep + action;
                        sep = ",";
                }
                if (file.isDirectory()) {
                        file = new File(file.getAbsolutePath() + File.separator + "*");
                }
                FilePermission fp = new FilePermission(file.getAbsolutePath(), mode);
                try {
                        fp.checkGuard(null);
                        /**
                         * additional check * if ((fileMode & FILE_READ) != 0) {
                         * file.length(); } if ((fileMode & FILE_WRITE) != 0) {
                         * File.createTempFile("jxaEnv_", "_createWrite",
                         * file.isDirectory() ? file :
                         * file.getParentFile()).delete(); }
                         */
                        return true;
                } catch (Exception e) {
                        if (JXAenvUtils._debugSys) {
                                System.err.println(JXAenvUtils.log((file.isDirectory() ? "Folder " : "File ") + fp.getName() + " is not allowed for accessing one or more actions : " + fp.getActions() + " " + e.getMessage(), JXAenvUtils.LVL.SYS_ERR));
                        }
                        return false;
                }
        }

        /**
         * completely erase a path (directory or file)
         *
         * @param path
         */
        public static void _erase(File path) {
                try {
                        _erase(path, false);
                } catch (IOException ex) {
                        if (JXAenvUtils._debugSys) {
                                ex.printStackTrace();
                        }
                }
        }

        /**
         * completely erase a path (directory or file)
         *
         * @param path
         * @param reportException
         * @throws IOException
         */
        public static void _erase(File path, boolean reportException) throws IOException {
                if (_accessFilePermitted(path, FILE_DELETE)) {
                        if (path.isDirectory()) {
                                for (File f : path.listFiles()) {
                                        _erase(f, reportException);
                                }
                        }
                        path.delete();
                        if (JXAenvUtils._debugSys) {
                                System.out.println("erased file path " + path);
                        }
                } else {
                        if (path.exists()) {
                                System.err.println("unable to erase " + path);
                                if (reportException) {
                                        throw new IOException("File " + path + " cannot be deleted");
                                }
                        } else {
                                if (JXAenvUtils._debugSys) {
                                        System.err.println("unable to delete a file that does not exist " + path);
                                }
                        }
                }
        }

        /**
         * dis/enables ImageUIO caching
         *
         * @param b true or false, to enable/disable, resp.
         * @param cacheDirectory the folder where ImageIO will put the cache
         * files
         * @default enabled, if disabled, unexpected behaviours may occur with
         * image loading.
         */
        public static void _setImageIOCacheEnabled(final boolean b, final File cacheDirectory) {
                try {
                        ImageIO.setUseCache(_ImageIOUseCache = b);
                        if (b) {
                                cacheDirectory.mkdirs();
                                _makeWritable(cacheDirectory);
                                ImageIO.setCacheDirectory(cacheDirectory);
                                _ImageIOCache = cacheDirectory;
                        }
                } catch (Exception e) {
                        if (JXAenvUtils._debugSys) {
                                e.printStackTrace();
                        }
                        ImageIO.setUseCache(_ImageIOUseCache = false);
                }
        }

        /**
         *
         */
        static File _findTempDirectory() {
                return AccessController.doPrivileged(new PrivilegedAction<File>() {
                        public File run() {
                                Vector<File> tempPath = new Vector<File>();
                                if (JXAenvUtils.rb.getString("tempPathEnabled").equalsIgnoreCase("true")) {
                                        tempPath.add(new File(JXAenvUtils._getSysValue("java.io.tmpdir")));
                                }
                                tempPath.add(new File("."));
                                return _findFreeDirectory(tempPath);
                        }
                }, JXAenvUtils.acc);
        }

        /**
         *
         */
        static File _findHomeDirectory() {
                return AccessController.doPrivileged(new PrivilegedAction<File>() {
                        public File run() {
                                Vector<File> tempPath = new Vector<File>();
                                if (JXAenvUtils.rb.getString("homePathEnabled").equalsIgnoreCase("true")) {
                                        tempPath.add(new File(JXAenvUtils._getSysValue("user.home")));
                                }
                                tempPath.add(new File("."));
                                return _findFreeDirectory(tempPath);
                        }
                }, JXAenvUtils.acc);
        }

        /**
         * Usually, new File("/filename") returns "\filename" as a path on
         * Windows and "//filename" on Unix, this method converts the abstract
         * path name from the specified File to a resource String (i.e.
         * {@linkplain Class#getResourceAsStream(String)} can handle it).
         *
         * @param filePath
         * @return
         */
        public static String _convertToResourceString(File filePath) {
                String resource = "";
                String sep = "";
                String splitter = _quotedFileSeparator();
                if (filePath.getPath().startsWith(splitter)) {
                        sep = "/";
                }
                for (String s : filePath.getPath().split(splitter)) {
                        resource += sep + s;
                        sep = "/";
                }
                return resource.replaceAll(splitter + splitter, splitter);
        }

        /**
         *
         *
         * @return
         */
        public static boolean _isImageIOCacheEnabled() {
                return _ImageIOUseCache;
        }

        /**
         * secure copy of src file to dst file (first a temp. file is created
         * and filled with the data)
         *
         */
        private static void __fileCopy(final Object src, final File dst, final boolean progressBar, final long id, boolean bigBuffer) throws FileNotFoundException, IOException {
                final Map<String, Boolean> readSync = Collections.synchronizedMap(new HashMap<String, Boolean>(Collections.singletonMap(_key_reading, false)));
                final Map<String, String> readProgress = Collections.synchronizedMap(new HashMap<String, String>());
                readProgress.put(_key_read_string, "");
                readProgress.put(_key_read_pgs_val, "0");
                readProgress.put(_key_read_pgs_max, "100");
                final int BUFFER = bigBuffer ? _BIGBUFFER_SIZE : _SMALLBUFFFER_SIZE;
                final RandomAccessFile JXAextension = src instanceof File ? new RandomAccessFile((File) src, "r") : null;
                final HttpClient HTTPConnection = src.toString().startsWith("http:") ? new HttpClient() : null;
                final HttpMethod getData = HTTPConnection instanceof HttpClient ? new GetMethod(src.toString()) : null;
                final URLConnection connection = src instanceof URL ? ((URL) src).openConnection() : null;
                final BufferedInputStream JXAextensionStream = connection instanceof URLConnection ? new BufferedInputStream(connection.getInputStream(), BUFFER) : src instanceof BufferedInputStream ? (BufferedInputStream) src : src instanceof InputStream ? new BufferedInputStream((InputStream) src, BUFFER) : null;
                final boolean closeBufferedStream = !(src instanceof InputStream);
                if (src instanceof File) {
                        if (JXAenvUtils._debugSys) {
                                System.out.println("file copy " + ((File) src).getAbsolutePath() + " -> " + dst.getAbsolutePath());
                        }
                        if (!_accessFilePermitted((File) src, FILE_READ)) {
                                throw new IOException("src path " + ((File) src).getAbsolutePath() + " is not readeable/doesn't exist");
                        }
                } else {
                        if (JXAenvUtils._debugSys) {
                                System.out.println("file copy " + (connection instanceof URLConnection ? connection.getContentLength() : JXAextensionStream.available()) + " -> " + dst.getAbsolutePath());
                        }
                }
                if (dst.getParentFile() != null) {
                        dst.getParentFile().mkdirs();
                        _makeWritable(dst.getParentFile());
                }
                if (!_accessFilePermitted(dst.getParentFile(), FILE_WRITE | FILE_READ)) {
                        throw new IOException("dst path " + dst.getAbsoluteFile().getParent() + " is not writeable");
                }
                final File tmp = _createTempFile("cp_", _TMPDIRECTORY, false);
                final RandomAccessFile JXAtmpPath = new RandomAccessFile(tmp, "rw");
                boolean indeterminateState = false;
                if (progressBar) {
                        indeterminateState = UIMessage.getProgressBar(id).isIndeterminate();
                        UIMessage.getProgressBar(id).setIndeterminate(false);
                        readProgress.put(_key_read_string, "loading " + dst.getName());
                        readProgress.put(_key_read_pgs_val, "0");
                        readProgress.put(_key_read_pgs_max, "100");
                }
                if (JXAenvUtils._debugSys) {
                        System.out.print("loading ");
                }
                Runnable copyRunnable = new Runnable() {
                        public void run() {
                                boolean dstAltered = false;
                                InputStream inputStream = JXAextensionStream;
                                if (HTTPConnection instanceof HttpClient) {
                                        if (JXAenvUtils._debugSys) {
                                                System.out.println("Connection with " + HTTPConnection.getClass().getCanonicalName() + " to " + src.toString());
                                        }
                                        getData.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler(10, true));
                                        int executeMethod;
                                        try {
                                                executeMethod = HTTPConnection.executeMethod(getData);
                                                if (executeMethod != HttpStatus.SC_OK) {
                                                        JXAenvUtils._popExceptionToUser(false, Thread.currentThread(), new HttpException(JXAenvUtils.log("Error occured with HTTP GET connection to " + src, JXAenvUtils.LVL.APP_ERR)));
                                                }
                                                inputStream.close();
                                                inputStream = getData.getResponseBodyAsStream();
                                        } catch (Exception ex) {
                                                if (JXAenvUtils._debugSys) {
                                                        ex.printStackTrace();
                                                }
                                                getData.releaseConnection();
                                                return;
                                        }
                                } else {
                                        if (JXAenvUtils._debugSys) {
                                                System.out.println("Connection with " + src.getClass().getCanonicalName() + " to " + src.toString());
                                        }
                                }
                                byte[] b = new byte[BUFFER];
                                int r = 0;
                                int rBytes = 0;
                                try {
                                        while ((r = src instanceof File ? JXAextension.read(b) : inputStream.read(b)) != -1) {
                                                if (abortedThreads.contains(Thread.currentThread().getId())) {
                                                        abortedThreads.remove(Thread.currentThread().getId());
                                                        return;
                                                }
                                                synchronized (fileThreads) {
                                                        fileThreads.notify();
                                                }
                                                JXAtmpPath.write(b, 0, r);
                                                rBytes += r;
                                                if (progressBar) {
                                                        if (!(src instanceof File)) {
                                                                readProgress.put(_key_read_string, Matcher.quoteReplacement((HTTPConnection instanceof HttpClient ? "downloaded " : "copied ") + FileUtils.byteCountToDisplaySize((long) rBytes)) + " (%1$s remaining)");
                                                        }
                                                        readProgress.put(_key_read_pgs_val, String.valueOf(rBytes));
                                                        readProgress.put(_key_read_pgs_max, String.valueOf((int) (src instanceof File ? JXAextension.length() : connection instanceof URLConnection ? connection.getContentLength() : inputStream.available())));
                                                }
                                        }
                                        JXAtmpPath.close();
                                        if (src instanceof File) {
                                                JXAextension.close();
                                        } else if (closeBufferedStream) {
                                                inputStream.close();
                                        }
                                        if (HTTPConnection instanceof HttpClient) {
                                                getData.releaseConnection();
                                        }
                                        if (progressBar) {
                                                readProgress.put(_key_read_string, "copying " + dst.getName());
                                        }
                                        ___rawfileCopy(tmp, dst, true);
                                        if (JXAenvUtils._debugSys) {
                                                System.out.println("done.");
                                        }
                                } catch (IOException ex) {
                                        if (JXAenvUtils._debugSys) {
                                                ex.printStackTrace();
                                        }
                                        if (dst != null) {
                                                if (dst.exists() && dstAltered) {
                                                        dst.delete();
                                                }
                                        }
                                        JXAenvUtils._popExceptionToUser(false, Thread.currentThread(), ex);
                                } finally {
                                        if (tmp != null) {
                                                if (tmp.exists()) {
                                                        tmp.delete();
                                                }
                                        }
                                        synchronized (fileThreads) {
                                                readSync.put(_key_reading, false);
                                                fileThreads.notify();
                                        }
                                }
                        }
                };
                try {
                        Thread tCopy = new Thread(copyRunnable, "file copy " + src + " -> " + dst);
                        tCopy.setPriority(tCopy.MAX_PRIORITY);
                        boolean retryRequested = false;
                        synchronized (fileThreads) {
                                readSync.put(_key_reading, true);
                                tCopy.start();
                                while (readSync.get(_key_reading)) {
                                        long time = System.currentTimeMillis();
                                        fileThreads.wait(_streamsTimeout);
                                        if (progressBar) {
                                                UIMessage.getProgressBar(id).setString(readProgress.get(_key_read_string));
                                                UIMessage.getProgressBar(id).setMaximum(Integer.parseInt(readProgress.get(_key_read_pgs_max)));
                                                UIMessage.getProgressBar(id).setValue(Integer.parseInt(readProgress.get(_key_read_pgs_val)));
                                                UIMessage.getProgressBar(id).paintImmediately(UIMessage.getProgressBar(id).getVisibleRect());
                                        }
                                        if (System.currentTimeMillis() - time >= _streamsTimeout) {
                                                abortedThreads.add(tCopy.getId());
                                                tCopy.interrupt();
                                                if (JXAenvUtils._debugSys) {
                                                        System.out.println("attempting a retry...");
                                                }
                                                if (progressBar) {
                                                        UIMessage.getProgressBar(id).setString("lost connection, retry attempt...");
                                                }
                                                retryRequested = true;
                                        }
                                }
                        }
                        if (retryRequested) {
                                __fileCopy(src, dst, progressBar, id, bigBuffer);
                        }
                } catch (Exception ex) {
                        if (JXAenvUtils._debugSys) {
                                ex.printStackTrace();
                        }
                } finally {
                        if (progressBar) {
                                UIMessage.getProgressBar(id).setIndeterminate(indeterminateState);
                        }
                }
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
        public static File _createTempFile(String prefix, File dir, boolean deleteOnExit) throws IOException {
                File f = File.createTempFile(prefix, _tmpFilesSuffix, dir);
                _tmpDir.add(dir);
                _makeReadable(f);
                _makeWritable(f);
                if (deleteOnExit) {
                        f.deleteOnExit();
                }
                SpritesCacheManager._cleanup();
                return f;
        }

}
