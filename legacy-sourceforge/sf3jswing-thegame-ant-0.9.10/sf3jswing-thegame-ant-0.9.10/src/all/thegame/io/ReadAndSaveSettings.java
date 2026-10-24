package thegame.io;

import java.io.*;
import java.text.DateFormat;
import java.util.*;
import net.sf.jiga.xtended.kernel.BitStack;
import net.sf.jiga.xtended.kernel.ThreadWorks;
import thegame.CacheLoadListener;
import thegame.GameProperties;
import thegame.RaSSetListener;

public class ReadAndSaveSettings {

    private File leFile;
    private File highScores;
    GameProperties props = new GameProperties();
    Set listeners = Collections.synchronizedSet(new HashSet());

    /**creates a IO pipeline for GameProperties. No data will be loaded until {@linkplain #loadOptions()}
     * is invoked.
    @param optionsFilePath the path of the saved game properties*/
    public ReadAndSaveSettings(String optionsFilePath) {
        leFile = new File(optionsFilePath);
        highScores = new File(optionsFilePath + ".scores");
    }

    /**creates a IO pipeline for GameProperties.
     * @param optionsFilePath the path to save game properties
    @param props the GameProperties to use as default */
    public ReadAndSaveSettings(String optionsFilePath, GameProperties props) {
        this(optionsFilePath);
        this.props = props;
    }

    /**creates a IO pipeline for GameProperties.
     * @throws IOException if the loading of props failed in any way
    @param optionsFilePath the path to store options (a file named optionsFilePath will be created and the InputStream is copied into it)
    @param propsInput a resource-stream input of saved options file to use as defaults */
    public ReadAndSaveSettings(String optionsFilePath, InputStream propsInput) {
        this(optionsFilePath);
        try {
            BufferedInputStream bis = new BufferedInputStream(propsInput);
            RandomAccessFile raf = new RandomAccessFile(leFile, "rw");
            raf.setLength(bis.available());
            byte[] r = new byte[512];
            int rBLen = 0;
            while ((rBLen = bis.read(r)) != -1) {
                raf.write(r, 0, rBLen);
            }
            bis.close();
            raf.close();
            notifyOptions(null, new Date(), saving | done);
        } catch (IOException ex) {
            ex.printStackTrace();
            notifyOptions(ex, new Date(), saving | done);
        }
    }

    public void deleteFile() {
        leFile.delete();
        highScores.delete();
        notifyOptions(null, null, done | delete);
    }
    private static BitStack logBits = new BitStack();
    private final static int logERROR = logBits._newBitRange();
    private final static int logSAVED = logBits._newBitRange();
    private final static int logDELETED = logBits._newBitRange();
    private final static int logLOADED = logBits._newBitRange();
    private final static int logNOTFOUND = logBits._newBitRange();
    private final static int logSAVING = logBits._newBitRange();
    private final static int logLOADING = logBits._newBitRange();
    private int logState = 0;
    private RaSSetListener log = new RaSSetListener() {

        public void optionsSaved(String date) {
            logState = logSAVED;
        }

        public void optionsDeleted() {
            logState = logDELETED;
        }

        public void optionsLoaded(String date) {
            logState = logLOADED;
        }

        public void optionsNotFound() {
            logState = logNOTFOUND;
        }

        public void optionsError(Object e) {
            logState = logERROR;
        }

        public void optionsAreSaving(String m) {
            logState = logSAVING;
        }

        public void optionsAreLoading(String m) {
            logState = logLOADING;
        }
    };

    /** returns true if and only if the last activity had errors and clears the error bits.
    @return true if and only if the last activity had errors     
     * */
    public boolean checkError() {
        int ret = logState;
        logState = 0;
        return (ret & (logERROR | logNOTFOUND)) != 0;
    }

    public void saveOptions() {
/*        try {
            ioThreads.doAndWait(new Runnable() {

                public void run() {
  */                  Date now = new Date();
                    ObjectOutputStream oos = null;
                    FileOutputStream fos = null;
                    RandomAccessFile raf = null;
                    Exception error = null;
                    notifyOptions(error, now, saving);
                    try {
                        raf = new RandomAccessFile(leFile, "rw");
                        raf.setLength(0);
                        oos = new ObjectOutputStream(fos = new FileOutputStream(raf.getFD()));
                        oos.writeObject(props);
                        oos.writeObject(now);
                        fos.close();
                        raf.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                        error = e;
                    } finally {
                        notifyOptions(error, now, done | saving);
                    }
       /*         }
            });
        } catch (InterruptedException e) {
            e.printStackTrace();
        }*/
    }

    public GameProperties loadOptions(final CacheLoadListener ll, final long prg) {
       /* try {
            ioThreads.doAndWait(new Runnable() {

                public void run() {
         */           Exception error = null;
                    ObjectInputStream ois = null;
                    FileInputStream fis = null;
                    RandomAccessFile raf = null;
                    Date date = null;
                    notifyOptions(error, date, loading);
                    try {
                        raf = new RandomAccessFile(leFile, "r");
                        ois = new ObjectInputStream(fis = new FileInputStream(raf.getFD()));
                        props = (GameProperties) ois.readObject();
                        date = (Date) ois.readObject();
                        if (props == null && date == null) {
                            error = new FileNotFoundException("no data could be read");
                        }
                        ois.close();
                        fis.close();
                        raf.close();
                        props.deployContentsCaches(ll, prg);
                    } catch (Exception e) {                        
                       e.printStackTrace();
                       error = e;
                    } finally {
                        notifyOptions(error, date, done | loading);
           /*         }
                }
            });
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            */
                        return props;
        }
    }
    final static int done = 1;
    final static int loading = 2;
    final static int saving = 4;
    final static int delete = 8;

    private void _notifyOptions(RaSSetListener l, final Exception error, final Date date, final int status) {
        switch (status) {
            case loading:
                l.optionsAreLoading("loading file...");
                break;
            case saving:
                l.optionsAreSaving("saving file...");
                break;
            default:
                if (error == null) {
                    if ((status & done) != 0) {
                        String dateFormat = date == null ? "" : DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(date);
                        if ((status & loading) != 0) {
                            l.optionsLoaded(dateFormat);
                        }
                        if ((status & saving) != 0) {
                            l.optionsSaved(dateFormat);
                        }
                        if ((status & delete) != 0) {
                            l.optionsDeleted();
                        }
                    }
                } else if (error instanceof FileNotFoundException) {
                    l.optionsNotFound();
                } else {
                    l.optionsError(error);
                }
                break;
        }
    }

    private void notifyOptions(final Exception error, final Date date, final int status) {
        _notifyOptions(log, error, date, status);
        Runnable r = new Runnable() {

            public void run() {
                synchronized (listeners) {
                    for (Iterator<RaSSetListener> i = listeners.iterator(); i.hasNext();) {
                        _notifyOptions(i.next(), error, date, status);
                    }
                }
            }
        };
        if (ThreadWorks.Swing.isEventDispatchThread()) {
            r.run();
        } else {
            ThreadWorks.Swing.invokeLater(r);
        }
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }
    StringBuffer highScoresSB = new StringBuffer();

    public StringBuffer getHighScores() {
        return highScoresSB;
    }
    private boolean readError = false;

    public boolean readScores() {
/*        try {
            ioThreads.doAndWait(new Runnable() {

                public void run() {
  */                  readError = false;
                    FileReader fr = null;
                    BufferedReader br = null;
                    StringBuffer sb = highScoresSB = new StringBuffer();
                    RandomAccessFile raf = null;
                    try {
                        raf = new RandomAccessFile(highScores, "r");
                        fr = new FileReader(raf.getFD());
                        br = new BufferedReader(fr);
                        String s;
                        while ((s = br.readLine()) instanceof String) {
                            sb.append(s);
                        }
                        br.close();
                        fr.close();
                        raf.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                        readError = true;
   /*                 }
                }
            });
        } catch (InterruptedException e) {
            e.printStackTrace();
     */   } finally {
            return readError;
        }
    }
    private boolean writeError = false;

    /**ThreadWorks ioThreads = new ThreadWorks("rassThreads");*/
    public boolean writeScores(String pplayerName, double[] presults) {
        final double[] results = presults;
        final String playerName = pplayerName;

        /*try {
            ioThreads.doAndWait(new Runnable() {

                public void run() {
              */      writeError = false;
                    FileWriter fw = null;
                    BufferedWriter bw = null;
                    RandomAccessFile raf = null;
                    try {
                        raf = new RandomAccessFile(highScores, "rw");
                        raf.setLength(0);
                        fw = new FileWriter(raf.getFD());
                        bw = new BufferedWriter(fw);
                        for (double result : results) {
                            bw.write(playerName + "=" + result + "\n");
                        }
                        bw.close();
                        fw.close();
                        raf.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                        writeError = true;
                 /*   }
                }
            });
        } catch (InterruptedException e) {
            e.printStackTrace();
        */} finally {
            return writeError;
        }
    }

    public void addRaSSetListener(RaSSetListener listener) {
        listeners.add(listener);
    }

    public void removeRaSSetListener(RaSSetListener listener) {
        listeners.remove(listener);
    }
}
