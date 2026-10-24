/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.jiga.xtended.kernel;

import java.awt.event.ActionEvent;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.AbstractAction;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;
import net.sf.jiga.xtended.JXAException;
import net.sf.jiga.xtended.ui.UIMessage;

/**
 * ThreadWorks gives the developer to "schedule" tasks (or "works") for the the
 * same Thread is going to process them, within a single ThreadWorks instance.
 * E.g. JXA GameActionLayer use a static ThreadWorks instance to load resources
 * over the time, one each after the other. It's safer to invoke long tasks with
 * such a ThreadWorks (as the SwingWorker Swing class does, too) because if the
 * task is cancelled, the application can recover quickly for the task is run
 * over a dedicated Thread of the {@linkplain #workTG works ThreadGroup}.<br>
 * However, it shouldn't be used for tasks that may require a quick startup,
 * because each task instances a new Thread (if
 * {@linkplain #isMultiThreadingEnabled()} joining the
 * {@linkplain #workDT global dispatch Thread}.<br>
 * <i>Implementation notice : The implementation of this class with the
 * java.util.Timer is slightly different from the usual schedules of Timer :
 * scheduled tasks are enqueued in a local Stack to guarranty a (FIFO)
 * first-in-first-out execution, among all methods calls ({@link #doAndWait(java.lang.Runnable)
 * }, {@link #doLater(java.lang.Runnable) },...), e.g. doLater(Task0);
 * doAndWait(Task1); => code will wait that Task0 is over before to run and wait
 * for Task1. </i>
 *
 * @see #securizedInvokeLater(java.lang.Runnable)
 * @author www.b23prodtm.info
 */
public class ThreadWorks implements Threaded {

        private static ClassLoader classLoaderContext = null;

        /**
         *
         * @param classLoaderContext
         */
        public static void setGlobalClassLoaderContext(ClassLoader classLoaderContext) {
                ThreadWorks.classLoaderContext = classLoaderContext;
        }

        private static ClassLoader switchGlobalClassLoaderOn() {
                if (classLoaderContext != null) {
                        return JXAenvUtils._switchToClassLoader(classLoaderContext);
                }
                return null;
        }

        private static void switchGlobalClassLoaderOff(ClassLoader l) {
                if (l != null) {
                        JXAenvUtils._switchToClassLoader(l);
                }
        }

        private static ClassLoader getGlobalClassLoaderContext() {
                return classLoaderContext;
        }

        /**
         *
         * @param r the value of r
         */
        private static Runnable loaderRun(final Runnable r) {
                return new Runnable() {
                        public void run() {
                                ClassLoader c = switchGlobalClassLoaderOn();
                                r.run();
                                switchGlobalClassLoaderOff(c);
                        }
                };
        }

        public boolean isMultiThreadingEnabled() {
                return JXAenvUtils._multiThreading;
        }

        public void setMultiThreadingEnabled(boolean b) {
                JXAenvUtils._multiThreading = b;
        }

        public Monitor[] getGroupMonitor() {
                return new Monitor[]{workMon};
        }

        public void setGroupMonitor(Monitor... tg) {
                workMon = tg[0];
        }

        /**
         * for use with
         * {@linkplain ThreadWorks#doAndWaitReturn(net.sf.jiga.xtended.kernel.ThreadWorks, net.sf.jiga.xtended.kernel.ThreadWorks.DoStaticReturn)}
         *
         * @param <T>
         */
        public static interface DoStaticReturn<T> {

                /**
                 * must return a {@linkplain <T>} value or null
                 *
                 * @return
                 * @throws Throwable
                 */
                public T run() throws Throwable;
        }
        private final static AccessControlContext _acc = AccessController.getContext();

        private static ThreadWorks _loadSecurityThreadWorks() {
                return AccessController.doPrivileged(new PrivilegedAction<ThreadWorks>() {
                        public ThreadWorks run() {
                                return new ThreadWorks("JXA_Security", true);
                        }
                }, _acc);
        }
        /**
         * This the security ThreadWorks, used by some classes to commit tasks
         */
        public final static ThreadWorks securityTWKS = _loadSecurityThreadWorks();

        /**
         * returns true or false, whether the current Thread owner is the
         * securized Dispatcher Thread (SDT)
         *
         * @return true or false, whether the current Thread owner is the
         * securized Dispatcher Thread (SDT)
         * @see #securizedInvokeAndWait(Runnable)
         * @see #securizedInvokeLater(Runnable)
         */
        public final static boolean isSDT() {
                return securityTWKS.isDT();
        }

        /**
         * invokes a Runnable onto a new separate Thread to securize the running
         * Thread against caught exceptions. This is very useful for not getting
         * the EDT-cycle closed with Swing applications. Unlike with the
         * Swing-EDT methods ({@link SwingUtilities}), all uncaught exceptions
         * appear onto a separate exception handler (static
         * CoalescedThreadsMonitor), it is scheduled on the security timer and
         * the current Thread waits for its termination
         * ({@link Thread#join() join()}) NOTICE : as with the SwingUtilities,
         * this method cannot be called from the {@linkplain #isSDT() SDT}
         * ThreadGroup
         *
         * @param r the Runnable instance to run() in securized environment.
         * @throws InterruptedException
         */
        public final static void securizedInvokeAndWait(Runnable r) throws InterruptedException {
                securityTWKS.doAndWait(r);
        }

        /**
         * invokes a Runnable onto a new separate Thread to securize the running
         * Thread against caught exceptions. This is very useful for not getting
         * the EDT-cycle closed with Swing applications. Unlike with the
         * Swing-EDT methods ({@link SwingUtilities}), all uncaught exceptions
         * appear onto a separate exception handler (static
         * CoalescedThreadsMonitor) and it is scheduled on the security timer.
         *
         *
         * @param r the Runnable instance to run() in securized environment.
         */
        public final static void securizedInvokeLater(Runnable r) {
                securityTWKS.doLater(r);
        }

        /**
         * dynamically compares and, if needed, changes 2 ThreadGroup's
         * priorities so that the first "up" has a higher priority than the
         * second "down". Either the first one or the second one is "pushed up"
         * or "pulled down", resp.
         *
         * @param up the ThreadGroup to "push up"
         * @param down the ThreadGroup to "pull down"
         */
        public final static void prioritizeThreadGroups(ThreadGroup up, ThreadGroup down) {
                if (up.getMaxPriority() > Thread.MIN_PRIORITY) {
                        down.setMaxPriority(Math.min(down.getMaxPriority(), up.getMaxPriority() - 1));
                        if (JXAenvUtils._debugSys) {
                                System.out.println(down.toString() + " TG is pulled down to pty : " + down.getMaxPriority());
                        }
                } else {
                        up.setMaxPriority(Math.max(up.getMaxPriority(), down.getMaxPriority() + 1));
                        if (JXAenvUtils._debugSys) {
                                System.out.println(up.toString() + " TG is pushed up to pty : " + up.getMaxPriority());
                        }
                }
        }

        private Timer newDispatcher(String name) {
                return new java.util.Timer("TWKS-" + name + "-DT", isDaemon);
        }

        private void startDispatcher(final Timer dt) {
                dt.scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                                if (!scheduledTasks.empty()) {
                                        dt.schedule(scheduledTasks.pop(), 0);
                                }
                        }
                }, 0, 10);
        }

        boolean isDaemon = false;

        /**
         * {@link #cancelAllSchedules() } if state is changed.
         *
         * @default value is false
         */
        public void setIsDaemon(boolean isDaemon) {
                boolean b = this.isDaemon;
                this.isDaemon = isDaemon;
                if (b != isDaemon) {
                        cancelAllSchedules();
                }
        }

        String name;

        /**
         * namely the ThreadWorks instance is created and it can be monitored
         * through the {@linkplain #workTG ThreadGroup} and the
         * {@linkplain #workDT dispatcher}
         *
         * @param name
         */
        public ThreadWorks(String name) {
                this(name, false, null);
        }

        /**
         * namely the ThreadWorks instance is created and it can be monitored
         * through the {@linkplain #workTG ThreadGroup} and the
         * {@linkplain #workDT dispatcher}
         *
         * @param name
         * @param monitor synch monitor
         */
        public ThreadWorks(String name, Monitor monitor) {
                this(name, false, monitor);
        }

        boolean highestPriority;

        private ThreadWorks(String name, boolean highestPriority, Monitor monitor) {
                workTG = new ThreadGroup("TWKS-" + name + "-TG");
                workMon = monitor;
                this.highestPriority = highestPriority;
                initThreadWorks(name);
        }

        private ThreadWorks(String name, boolean highestPriority) {
                workTG = new ThreadGroup("TWKS-" + name + "-TG");
                workMon = new Monitor();
                this.highestPriority = highestPriority;
                initThreadWorks(name);
        }

        /**
         * if you use {@link #workDT} to invoke tasks, then checkAlive checks
         * that a Timer is alive for dispatching tasks.
         *
         * @return if false, all scheduled tasks must be reloaded again
         */
        public boolean checkAlive() {
                try {
                        workDT.schedule(new TimerTask() {
                                @Override
                                public void run() {
                                        /**
                                         * schedule is tested
                                         */
                                }
                        }, 0);
                        return true;
                } catch (IllegalStateException ex) {
                        int n = workDT.purge();
                        System.err.println(JXAenvUtils.log(n + " tasks have been purged because the timer was dead. (TWKS " + name + ")", JXAenvUtils.LVL.SYS_NOT));
                        if (DebugMap._getInstance().isDebugLevelEnabled(DebugMap._getInstance()._VOID)) {
                                ex.printStackTrace();
                        }
                        initThreadWorks(name);
                        return false;
                }
        }
                
        private void initThreadWorks(String name) {
                workDT = newDispatcher(name);
                startDispatcher(workDT);
                this.name = name;
                if (!highestPriority) {
                        prioritizeThreadGroups(securityTWKS.workTG, workTG);
                }
        }

        /**
         * reports every uncaught Exception of this ThreadGroup. An UIMessage
         * pop-up is shown.
         *
         * @param modal
         * @param thread the Thread that hasn't caught the Exception
         * @param throwable the Throwable Exception that has not been caught
         */
        public static void cLogFilePrintStackStrace(Throwable throwable) {
                throwable.printStackTrace(cLogFilePrintStream);
        }
        /**
         * the current UI message sent to the user
         */
        private static UIMessage currentMessage = null;
        /**
         * the previous UI message sent to the user
         */
        private static String previousMessage = "";

        public static void _uncaughtException(final boolean modal, final Thread thread, final Throwable throwable) {
                cLogFilePrintStackStrace(throwable);
                Runnable r = new Runnable() {
                        public void run() {
                                __uncaughtException(modal, thread, throwable);
                        }
                };
                if (ThreadWorks.Swing.isEventDispatchThread()) {
                        r.run();
                } else {
                        /**
                         * void awtthreadlock blocking
                         */
                        ThreadWorks.Swing.invokeLater(r);
                }
        }

        private static void __uncaughtException(boolean modal, Thread thread, Throwable throwable) {
                throwable.printStackTrace();
                if (currentMessage instanceof UIMessage) {
                        if (currentMessage.isShowing()) {
                                currentMessage.dispose();
                                previousMessage += "\r\n\r\n";
                        } else {
                                previousMessage = "";
                        }
                } else {
                        previousMessage = "";
                }
                String stack = "";
                for (StackTraceElement ste : throwable.getStackTrace()) {
                        stack += "\n" + ste;
                }
                String stack_c = "";
                if (throwable.getCause() instanceof Throwable) {
                        for (StackTraceElement ste : throwable.getCause().getStackTrace()) {
                                stack_c += "\n" + ste;
                        }
                }
                String msg = "an unexpected exception has been caught : " + throwable.getClass().getName() + " : " + throwable.getMessage() + " \n\r" + stack + " \n in Thread " + thread.getName() + ((throwable.getCause() instanceof Throwable) ? (" caused by : " + throwable.getCause().getClass().getName() + " : " + throwable.getCause().getMessage() + "\n\r" + stack_c) : "");
                currentMessage = new UIMessage(
                        modal,
                        previousMessage += msg,
                        null,
                        UIMessage.ERROR_TYPE);
        }
        /**
         *
         */
        public static Console c = new Console();
        /**
         * the log file used to store uncaught exceptions (from
         * {@link #c Console})
         */
        protected static File cLogFile = null;

        /**
         * closes the log file used to store uncaught exceptions (from
         * {@link #c Console})
         */
        protected static void cLogFile_open(String name) {
                FileOutputStream logOutput_fos = null;
                cLogFile = new File(FileHelper._USERHOMESTOREDIRECTORY, "log_" + name + ".txt");
                try {
                        FileHelper._makeWritable(cLogFile);
                        logOutput_fos = new FileOutputStream(cLogFile, true);
                        logOutput_fos.getChannel().truncate(102400L);
                        PrintWriter logOutput = new PrintWriter(new BufferedOutputStream(logOutput_fos));
                        c.setOutput(logOutput);
                        logOutput.println();
                        logOutput.println("Log started on " + new Date(System.currentTimeMillis()).toString());
                } catch (IOException ex) {
                        ex.printStackTrace();
                }
        }

        /**
         * closes the log file used to store uncaught exceptions (from
         * {@link #c Console}) JXAenvUtils closes the file in a Threadhook
         * before the VM exits.
         */
        protected static void cLogFile_close() {
                Appendable p = c.setOutput(null);
                if (p instanceof Closeable) {
                        try {
                                ((Closeable) p).close();
                        } catch (IOException ex) {
                                ex.printStackTrace();
                        }
                }
        }
        /**
         * the uncaught exceptions output stream can be changed to any valid
         * PrintStream
         */
        public static PrintStream cLogFilePrintStream = c.getNewPrintStream(new ErrorListener() {
                public void printStreamError(IOException e) {
                        System.err.println(JXAenvUtils.log(cLogFile.getAbsolutePath() + " Kernel log file error : Printstream lost. " + e.getMessage() + " " + e.getCause(), JXAenvUtils.LVL.SYS_WRN));
                }
        });

        /**
         * reports every uncaught Exception of this ThreadGroup. An UIMessage
         * pop-up is shown.
         *
         * @param thread the Thread that hasn't caught the Exception
         * @param throwable the Throwable Exception that has not been caught
         */
        public void uncaughtException(final Thread thread, final Throwable throwable) {
                _uncaughtException(true, thread, throwable);
        }
        /**
         * This groups the Threads run with.
         *
         * @see #doAndWait(Runnable)
         * @see #doLater(Runnable)
         */
        public final ThreadGroup workTG;
        private Monitor workMon;
        /**
         * this is the work Timer, a.k.a. the Work Dispatcher Thread.
         */
        public java.util.Timer workDT;

        /**
         * returns true or false, whether the current Thread owner is the works
         * Dispatcher Thread (DT)
         *
         * @return true or false, whether the current Thread owner is the works
         * Dispatcher Thread (DT)
         */
        public boolean isDT() {
                return Thread.currentThread().getThreadGroup().equals(workTG);
        }
        /**
         * scheduled tasks are queued here to guaranty a first-in-first-out
         * execution
         */
        private Stack<TimerTask> scheduledTasks = new Stack<TimerTask>();

        /**
         *
         * @param r
         * @throws InterruptedException
         */
        public synchronized void doAndWait(final Runnable r) throws InterruptedException {
                doAndWait(r, null);
        }

        /**
         *
         * @param <T>
         * @param twks
         * @param r
         * @return
         * @throws InterruptedException
         * @throws InvocationTargetException
         */
        public static <T> T doAndWaitReturn(ThreadWorks twks, final DoStaticReturn<T> r) throws InterruptedException, InvocationTargetException {
                return doAndWaitReturn(twks, r, null);
        }

        /**
         * invokes a Runnable onto a new separate Thread to process the running
         * Thread against caught exceptions. This is very useful for not getting
         * the EDT-cycle closed with Swing applications. Unlike with the
         * Swing-EDT methods ({@link SwingUtilities}), all uncaught exceptions
         * appear onto a separate exception handler (static
         * CoalescedThreadsMonitor), it is scheduled on the work timer and the
         * current Thread waits for its termination
         * ({@link Thread#join() join()}) NOTICE : as with the SwingUtilities,
         * this method cannot be called from the {@linkplain #isSDT() SDT}
         * ThreadGroup
         *
         * @param r the Runnable instance to run() in the working environment.
         * @param cacheHandler if the operation is memorySensitive, then a
         * cacheHandler should be specified, with which a
         * memorySensitiveCallback will be attempted.
         * @throws InterruptedException
         * @see UIMessage#_BOTTOM_RIGHT
         * @see UIMessage#_TOP_RIGHT
         * @see UIMessage#_TOP_LEFT
         * @see UIMessage#_BOTTOM_LEFT
         */
        public synchronized void doAndWait(final Runnable r, final SpritesCacheManager cacheHandler) throws InterruptedException {
                try {
                        doAndWaitReturn(this, new DoStaticReturn<Runnable>() {
                                public Runnable run() throws Throwable {
                                        r.run();
                                        return null;
                                }
                        }, cacheHandler);
                } catch (InvocationTargetException ex) {
                        if (JXAenvUtils._debugSys) {
                                ex.printStackTrace();
                        }
                }
        }

        /**
         *
         * @param <T> class that is returned by the DoStaticReturn<T>
         * @param twks ThreadWorks that runs the block of instructions
         * @param r the block of instructions to run
         * @param cacheHandler if the operation is memorySensitive, then a
         * cacheHandler should be specified, with which a
         * memorySensitiveCallback will be attempted.
         * @return T object is returned
         * @throws InvocationTargetException
         * @throws InterruptedException
         */
        public static <T> T doAndWaitReturn(final ThreadWorks twks, final DoStaticReturn<T> r, final SpritesCacheManager cacheHandler) throws InvocationTargetException, InterruptedException {
                twks.checkAlive();
                synchronized (twks) {
                        if (twks.isDT()) {
                                throw new InterruptedException("can't call doAndWait on the work Dispatcher Thread (DT).");
                        }
                        final Map<String, Object> ret = new HashMap<String, Object>();
                        ret.put("return", null);
                        ret.put("throw", null);
                        final Runnable rCache = loaderRun(new Runnable() {
                                public void run() {
                                        try {
                                                if (cacheHandler != null) {
                                                        try {
                                                                ret.put("return", cacheHandler.memorySensitiveCallback("run", r, new Object[]{}, new Class[]{}));
                                                        } catch (Throwable ex) {
                                                                ex.printStackTrace();
                                                        }
                                                } else {
                                                        ret.put("return", r.run());
                                                }
                                        } catch (Throwable ex) {
                                                ret.put("throw", ex);
                                        }
                                }
                        });
                        if (JXAenvUtils._multiThreading) {
                                final Thread t = new Thread(twks.workTG, rCache, "T-work-" + r.toString());
                                t.setPriority(Thread.MAX_PRIORITY);
                                t.start();
                                final String name = Thread.currentThread().getName();
                                twks.scheduledTasks.add(0, new TimerTask() {
                                        public void run() {
                                                try {
                                                        if (JXAenvUtils._debugSys) {
                                                                System.err.println("in " + twks.workTG.getName() + " & wt td " + name);
                                                        }
                                                        t.join();
                                                } catch (InterruptedException ex) {
                                                        if (JXAenvUtils._debugSys) {
                                                                ex.printStackTrace();
                                                        }
                                                }
                                        }
                                });
                                t.join();
                        } else {
                                final int hash = (int) System.nanoTime();
                                twks.mWaits.put(hash, false);
                                twks.scheduledTasks.add(0, new TimerTask() {
                                        @Override
                                        public void run() {
                                                rCache.run();
                                                synchronized (twks) {
                                                        twks.mWaits.put(hash, true);
                                                        twks.notifyAll();
                                                }
                                        }
                                });
                                while (Boolean.FALSE.equals(twks.mWaits.get(hash))) {
                                        twks.wait();
                                }
                                twks.mWaits.remove(hash);
                        }
                        if (ret.get("throw") instanceof Throwable) {
                                throw new InvocationTargetException((Throwable) ret.get("throw"));
                        }
                        return (T) ret.get("return");
                }
        }
        private Map<Integer, Boolean> mWaits = Collections.synchronizedMap(new HashMap<Integer, Boolean>());

        /**
         *
         * @param r
         */
        public void doLater(final Runnable r) {
                doLater(r, null);
        }

        /**
         * invokes a Runnable onto a new separate Thread to process the running
         * Thread against caught exceptions. Unlike with the Swing-EDT methods
         * ({@link SwingUtilities}), all uncaught exceptions appear onto a
         * separate exception handler (static CoalescedThreadsMonitor) and it is
         * scheduled on the work timer.
         *
         *
         * @param r the Runnable instance to run() in th working environment.
         ** @param cacheHandler if the operation is memorySensitive, then a
         * cacheHandler should be specified, with which a
         * memorySensitiveCallback will be attempted.
         * @see #_BOTTOM_RIGHT
         * @see #_TOP_RIGHT
         * @see #_TOP_LEFT
         * @see #_BOTTOM_LEFT
         */
        public void doLater(final Runnable r, final SpritesCacheManager cacheHandler) {
                checkAlive();
                final Runnable rCache = loaderRun(new Runnable() {

                        public void run() {
                                if (cacheHandler != null) {
                                        try {
                                                cacheHandler.memorySensitiveCallback("run", r, new Object[]{}, new Class[]{});
                                        } catch (Throwable ex) {
                                                ex.printStackTrace();
                                        }
                                } else {
                                        r.run();
                                }
                        }
                });
                if (JXAenvUtils._multiThreading) {
                        final Thread t = new Thread(workTG, rCache, "T-work-" + r.toString());
                        t.setPriority(Thread.MAX_PRIORITY);
                        scheduledTasks.add(0, new TimerTask() {
                                public void run() {
                                        t.start();
                                        try {
                                                t.join();
                                        } catch (InterruptedException ex) {
                                                if (JXAenvUtils._debugSys) {
                                                        ex.printStackTrace();
                                                }
                                        }
                                }
                        });
                } else {
                        scheduledTasks.add(0, new TimerTask() {
                                @Override
                                public void run() {
                                        rCache.run();
                                }
                        });
                }
        }
        /**
         *
         */
        private long MILLIS_TO_SHOW_LENGTHY_OPERATION_POPUP = 5000;

        /**
         * milliseconds before to show a popup cancel for a lengthy operation
         * (if in a
         * {@link JXAenvUtils#_multiThreading multithreaded environment only})
         * set a negative value to never show this popup; set zero to always
         * show when a ThreadWorks is in run
         *
         * @param millis set a negative value to never show this popup; set zero
         * to always show when a ThreadWorks is in run
         */
        public void setMillisToShowLengthyOperationPopup(long millis) {
                MILLIS_TO_SHOW_LENGTHY_OPERATION_POPUP = millis;
        }

        /**
         * @return @see #setMillisToShowLengthyOperationPopup(long)
         * @default 5000 ms
         */
        public long getMillisToShowLengthyOperationPopup() {
                return MILLIS_TO_SHOW_LENGTHY_OPERATION_POPUP;
        }
        /**
         *
         */
        public final static int _BOTTOM_RIGHT = 0;
        /**
         *
         */
        public final static int _TOP_RIGHT = 1;
        /**
         *
         */
        public final static int _TOP_LEFT = 2;
        /**
         *
         */
        public final static int _BOTTOM_LEFT = 3;

        private JDialog showPopupLengthyOperation(final Thread t, final JComponent parent, int popupLocation) {
                return UIMessage.showLightPopupMessage(new JLabel("Running Task..."), new AbstractAction("Abort", UIMessage._getIcon(UIMessage.ABORT_TYPE, true)) {
                        public void actionPerformed(ActionEvent e) {
                                t.interrupt();
                        }
                }, parent, popupLocation);
        }

        /**
         * Cancels all scheduled tasks and restarts a new dispatcher Thread.
         */
        public synchronized void cancelAllSchedules() {
                java.util.Timer workDT = this.workDT;
                this.workDT = newDispatcher(name);
                scheduledTasks.clear();
                workDT.cancel();
                mWaits.clear();
                notifyAll();
                startDispatcher(this.workDT);
        }

        /**
         * Swing Thread Wrapper. This performs a context ClassLoader switch
         * before each call to the Swing EDT.
         */
        public static class Swing {

                /**
                 * convenience method. It simply calls
                 * SwingUtilities.isEventDispatchThread().
                 *
                 * @return
                 */
                public static boolean isEventDispatchThread() {
                        return SwingUtilities.isEventDispatchThread();
                }

                /**
                 * WRAPPER performs context switch
                 *
                 * @param r
                 */
                public static void invokeLater(Runnable r) {
                        SwingUtilities.invokeLater(loaderRun(r));
                }

                /**
                 * WRAPPER performs context switch
                 *
                 * @param r
                 * @throws InterruptedException
                 * @throws InvocationTargetException
                 */
                public static void invokeAndWait(Runnable r) throws InterruptedException, InvocationTargetException {
                        if (isEventDispatchThread()) {
                                throw new JXAException("Illegal call to invokeAndWait while being on the Event Dispatcher Thread.");
                        }
                        SwingUtilities.invokeAndWait(loaderRun(r));
                }
        }
}
