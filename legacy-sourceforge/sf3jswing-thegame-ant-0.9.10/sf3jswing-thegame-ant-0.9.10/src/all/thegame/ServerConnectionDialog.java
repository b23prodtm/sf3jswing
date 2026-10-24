package thegame;

import thegame.rem.SQLconnection;
import thegame.rem.PortMapping;
import java.awt.*;
import java.awt.event.*;
import java.sql.SQLException;
import javax.swing.*;
import java.net.Socket;
import java.net.ServerSocket;
import java.io.IOException;
import java.sql.ResultSet;
import java.util.Locale;
import net.sbbi.upnp.messages.UPNPResponseException;
import net.sf.jiga.xtended.kernel.BitStack;
import net.sf.jiga.xtended.kernel.ThreadWorks;
import net.sf.jiga.xtended.ui.UIMessage;

/**
 *  A dialog to wait for a client connection and let the user accept or deny it.<br/>
 *  Simple call Socket socket = ServerConnectionDialog.getConnection(frame);<br/>
 *  It return null if no connection could be established (or if the user cancelled the dialog).<br/>
 *  Otherwise it returns an open socket with connection already established.
 */
public class ServerConnectionDialog extends JPanel {

    /** the connection with the client */
    Socket clientSocket = null;
    /** did an exception occurred while connecting to the server? */
    Exception exception = null;
    /** the listern port assigned by the system */
    int listenPort;
    static BitStack _bits = new BitStack();
    /** @see #state */
    static final int WAITING_FOR_CLIENT_TO_CONNECT = _bits._newBitRange();
    /** @see #state */
    static final int ASKING_FOR_ACCEPTANCE = _bits._newBitRange();
    /***/
    static final int CONNECTED = _bits._newBitRange();
    /***/
    static final int CANCELED = _bits._newBitRange();
    /** the current state of the dialog, WAITING_FOR_CLIENT_TO_CONNECT or ASKING_FOR_ACCEPTANCE */
    int state;
    /** the listen socket */
    ServerSocket serverSocket;
    /** the status field */
    JTextField statusField;
    /** accept button */
    JButton acceptButton;
    /** the deny button */
    JButton refuseButton;
    private FieldGui gui;

    /** create the dialog
     *  @param owner the Frame from which the dialog is displayed
     */
    private ServerConnectionDialog(FieldGui gui) {
        super(true);
        this.gui = gui;
        setBorder(BorderFactory.createTitledBorder("host a game"));
        //initialize the state
        state = WAITING_FOR_CLIENT_TO_CONNECT;
        clientSocket = null;

        //main panel
        JPanel mainPanel = new JPanel();
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        add(mainPanel);


        //Status Panel
        JPanel statusPanel = new JPanel();
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        statusPanel.setLayout(gridbag);
        c.anchor = GridBagConstraints.CENTER;

        JLabel label = new JLabel("Status: ");
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        gridbag.setConstraints(label, c);
        statusPanel.add(label);

        statusField = new JTextField();
        statusField.setEditable(false);
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        gridbag.setConstraints(statusField, c);
        statusPanel.add(statusField);

        mainPanel.add(statusPanel);

        //Buttons panel
        JPanel buttonsPanel = new JPanel();
        mainPanel.add(buttonsPanel);
        FlowLayout flowLayout = new FlowLayout();
        buttonsPanel.setLayout(flowLayout);
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                cancelServerMode();
            }
        });
        buttonsPanel.add(cancelButton);

        acceptButton = new JButton("Accept this client");
        acceptButton.setEnabled(false);
        acceptButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                acceptClient();
            }
        });
        buttonsPanel.add(acceptButton);

        refuseButton = new JButton("Refuse this client");
        refuseButton.setEnabled(false);
        refuseButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                refuseClient();
            }
        });
        buttonsPanel.add(refuseButton);
        validate();
        repaint();
    }
    PortMapping portMap = null;
    SQLconnection sql = null;

    private void anounceServer() {
        log("Announce host...");
        sql = new SQLconnection(true);
        try {
            sql.connect();
            ResultSet localesSet = sql.query("SELECT * FROM locale ORDER BY country ASC");
            int locale = 0;
            Locale loc = Locale.getDefault();
            while (localesSet.next()) {
                if (loc.equals(new Locale(localesSet.getString("language"), localesSet.getString("country")))) {
                    locale = localesSet.getInt("id");
                    break;
                }
            }
            if (locale == 0) {
                sql.update("INSERT INTO locale (language, country) VALUES("
                        + "'" + loc.getLanguage() + "',"
                        + "'" + loc.getCountry() + "'"
                        + ")");
                ResultSet myLocale = sql.query("SELECT id FROM locale WHERE country = '" + loc.getCountry() + "' AND language = '" + loc.getLanguage() + "'");
                if (myLocale.next()) {
                    locale = myLocale.getInt("id");
                }
            }
            sql.update("INSERT INTO host (ip, port, locale, api) VALUES("
                    + "'" + portMap.getWANIP() + "', "
                    + portMap.externalPort + ","
                    + locale + ","
                    + Sf3.class.getPackage().getSpecificationVersion()
                    + ")");
            log("Success !");
        } catch (UPNPResponseException ex) {
            ex.printStackTrace();
            exception = ex;
        } catch (IOException ex) {
            ex.printStackTrace();
            exception = ex;
        } catch (SQLException ex) {
            ex.printStackTrace();
            exception = ex;
        } finally {
            if (exception instanceof Exception) {
                log("Failure." + exception.getLocalizedMessage());
            }
            try {
                sql.disconnect();
            } catch (SQLException ex) {
                ex.printStackTrace();
            } finally {
                sql = null;
            }
        }
    }

    private void unanounceServer() {
        log("Unanounce host...");
        sql = new SQLconnection(true);
        try {
            sql.connect();
            if (portMap != null) {
                sql.update("DELETE FROM host WHERE ip = '" + portMap.getWANIP() + "' AND port = " + portMap.externalPort);
            }
            log("Success !");
        } catch (SQLException ex) {
            ex.printStackTrace();
            exception = ex;
        } catch (UPNPResponseException ex) {
            ex.printStackTrace();
            exception = ex;
        } catch (IOException ex) {
            ex.printStackTrace();
            exception = ex;
        } finally {
            if (exception instanceof Exception) {
                log("Failure." + exception.getLocalizedMessage());
            }
            try {
                sql.disconnect();
            } catch (SQLException ex) {
                ex.printStackTrace();
            } finally {
                sql = null;
            }
        }
    }
    int numPlayers = 2;

    private void anounceGame() {
        log("Anounce game...");
        sql = new SQLconnection(true);
        if (portMap != null) {
            try {
                sql.connect();
                ResultSet hostSet = sql.query("SELECT id FROM host WHERE ip = '" + portMap.getWANIP() + "' AND port = " + portMap.externalPort);
                int host_id = 0;
                if (hostSet.next()) {
                    host_id = hostSet.getInt("id");
                }
                sql.update("INSERT INTO game (host_id, players_num) VALUES (" + host_id + ", " + numPlayers + ")");
                log("Success !");
            } catch (UPNPResponseException ex) {
                ex.printStackTrace();
                exception = ex;
            } catch (IOException ex) {
                ex.printStackTrace();
                exception = ex;
            } catch (SQLException ex) {
                ex.printStackTrace();
                exception = ex;
            } finally {
                if (exception instanceof Exception) {
                    log("Failure." + exception.getLocalizedMessage());
                }
                try {
                    sql.disconnect();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                } finally {
                    sql = null;
                }
            }
        }
    }

    private void unanounceGame() {
        log("Unanounce game...");
        sql = new SQLconnection(true);
        if (portMap != null) {
            try {
                sql.connect();
                ResultSet hostSet = sql.query("SELECT id FROM host WHERE ip = '" + portMap.getWANIP() + "' AND port = " + portMap.externalPort);
                int host_id = 0;
                if (hostSet.next()) {
                    host_id = hostSet.getInt("id");
                }
                sql.update("DELETE FROM game WHERE host_id = " + host_id);
                log("Success !");
            } catch (UPNPResponseException ex) {
                ex.printStackTrace();
                exception = ex;
            } catch (IOException ex) {
                ex.printStackTrace();
                exception = ex;
            } catch (SQLException ex) {
                ex.printStackTrace();
                exception = ex;
            } finally {
                if (exception instanceof Exception) {
                    log("Failure." + exception.getLocalizedMessage());
                }
                try {
                    sql.disconnect();
                } catch (SQLException ex) {
                    ex.printStackTrace();
                } finally {
                    sql = null;
                }
            }
        }
    }

    private void log(String msg) {
        statusField.setText(msg);
        gui.logLn(msg);
    }

    private void _listenConnections() {
        if (state != WAITING_FOR_CLIENT_TO_CONNECT) {
            return;
        }
        Socket socket;
        try {
            log("Opening socket...");
            serverSocket = new ServerSocket(gui.props.defaultPort);
            listenPort = serverSocket.getLocalPort();
            log("Mapping port " + listenPort + "...");
            PortMapping map = new PortMapping("UDP", listenPort, listenPort);
            map.bindUPNPPort();
            portMap = map;
            anounceServer();
            log("Waiting client to connect on port " + listenPort);
            /* waiting*/
            socket = serverSocket.accept();
            /* connection found */
            askForAcceptance(socket);
        } catch (Exception e) {
            clientSocket = null;
            exception = e;
            log("Failure." + e.getLocalizedMessage());
            cancelServerMode();
        } finally {
            try {
                if (serverSocket != null) {
                    serverSocket.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /** ask wether the user accepts the client connection
     *  @param socket the client socket
     */
    private void askForAcceptance(Socket socket) {
        state = ASKING_FOR_ACCEPTANCE;
        clientSocket = socket;
        log("Client " + socket.getInetAddress().getHostName() + " is asking permission to connect");
        acceptButton.setEnabled(true);
        refuseButton.setEnabled(true);
    }

    /** cancel the dialog */
    private void cancelServerMode() {
        try {
            log("Host service cancelled.");
            if (clientSocket != null) {
                clientSocket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            clientSocket = null;
            state = CANCELED;
            try {
                if (serverSocket != null) {
                    serverSocket.close();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                unanounceGame();
                unanounceServer();
                try {
                    if (portMap != null) {
                        portMap.unbindUPNPPort();
                        portMap = null;
                    }
                } catch (IOException ex) {
                    ex.printStackTrace();
                } catch (UPNPResponseException ex) {
                    ex.printStackTrace();
                }
            }
        }
    }
    ThreadWorks serverConnections = new ThreadWorks("serverConn");

    /** accept the client connection */
    private void acceptClient() {
        state = CONNECTED;
        try {
            if (serverSocket != null) {
                serverSocket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            anounceGame();
        }
    }

    /** deny the client connection */
    private void refuseClient() {
        try {
            if (clientSocket != null) {
                clientSocket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            unanounceGame();
            clientSocket = null;
            acceptButton.setEnabled(false);
            refuseButton.setEnabled(false);
            state = WAITING_FOR_CLIENT_TO_CONNECT;
            statusField.setText("Waiting client to connect on port " + listenPort);
        }
    }

    /**
     * create a ServerConnectionDialog dialog, wait for a client connection and let the
     * user accept or deny it.
     * @param owner the Frame from which the dialog is displayed
     * @return null if no connection could be established (or if the user cancelled the dialog)
     *              otherwise it returns an open socket with connection already established
     */
    public static Socket getConnection(FieldGui gui) throws InterruptedException {
        final ServerConnectionDialog dialog = new ServerConnectionDialog(gui);
        gui.switchGui(dialog, null, new AbstractAction() {

            public void actionPerformed(ActionEvent e) {
                if (dialog.clientSocket == null) {
                    dialog.cancelServerMode();
                }
            }
        });
        while ((dialog.state & (CANCELED | CONNECTED)) == 0) {
            dialog.listenConnections();
        }
        if (dialog.exception != null) {
            gui.popMessage(dialog.exception.getMessage(), "Server connection error", UIMessage.ERROR_TYPE);
            dialog.exception = null;
            return null;
        }
        return dialog.clientSocket;
    }

    /** wait for client connections */
    protected void listenConnections() throws InterruptedException {
        Runnable r = new Runnable() {

            public void run() {
                _listenConnections();
            }
        };
        serverConnections.doAndWait(r);
    }
}
