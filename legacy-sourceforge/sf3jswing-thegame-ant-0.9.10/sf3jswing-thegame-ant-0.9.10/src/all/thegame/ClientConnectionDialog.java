package thegame;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.Socket;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Vector;
import javax.swing.*;
import net.sbbi.upnp.messages.UPNPResponseException;
import net.sf.jiga.xtended.kernel.ThreadWorks;
import thegame.rem.PortMapping;
import thegame.rem.SQLconnection;

/**
 *  A dialog to specify the server parameters and attempt a connection to it.<br/>
 *  Simple call Socket socket = ClientConnectionDialog.getConnection(frame);<br/>
 *  It return null if no connection could be established (or if the user cancelled the dialog).<br/>
 *  Otherwise it returns an open socket with connection already established
 */
public class ClientConnectionDialog extends JPanel {

    /** the connection to the server */
    Socket socket;
    /** did an exception occurred while connecting to the server? */
    Exception exception;
    /** the server hostname */
    JComboBox serverField;
    /** the server port name */
    JTextField portField;
    /** it should be disabled while connecting */
    JButton connectButton;
    FieldGui gui;

    /** create the dialog
     *  @param owner the Frame from which the dialog is displayed
     */
    private ClientConnectionDialog(FieldGui gui) {
        super(true);
        this.gui = gui;
        setBorder(BorderFactory.createTitledBorder("join a game"));
        //create the main panel and set the layout     
        JPanel mainPanel = new JPanel();
        //mainPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));

        //Parameters Panel
        JPanel parametersPanel = new JPanel();
        mainPanel.add(parametersPanel);
        parametersPanel.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createTitledBorder("Server address"), BorderFactory.createEmptyBorder(1, 1, 1, 1)));
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();
        parametersPanel.setLayout(gridbag);
        c.anchor = GridBagConstraints.CENTER;

        JLabel label;
        label = new JLabel("Host: ");
        c.gridwidth = GridBagConstraints.RELATIVE;
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        gridbag.setConstraints(label, c);
        parametersPanel.add(label);
        serverField = new JComboBox();
        serverField.setEditable(true);
        serverField.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                Host host = (Host) serverField.getSelectedItem();
                portField.setText("" + host.port);
                connectButton.setEnabled(connecting);
            }
        });
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        gridbag.setConstraints(serverField, c);
        parametersPanel.add(serverField);

        label = new JLabel("Port: ");
        c.gridwidth = GridBagConstraints.RELATIVE;
        c.weightx = 0;
        c.fill = GridBagConstraints.NONE;
        gridbag.setConstraints(label, c);
        parametersPanel.add(label);
        portField = new JTextField();
        c.gridwidth = GridBagConstraints.REMAINDER;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        gridbag.setConstraints(portField, c);
        parametersPanel.add(portField);


        //Buttons panel
        JPanel buttonsPanel = new JPanel();
        mainPanel.add(buttonsPanel);
        FlowLayout flowLayout = new FlowLayout();
        buttonsPanel.setLayout(flowLayout);
        JButton cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                connectionCancel();
            }
        });
        buttonsPanel.add(cancelButton);

        connectButton = new JButton("Connect");
        connectButton.addActionListener(new ActionListener() {

            public void actionPerformed(ActionEvent e) {
                connectionProceed();
            }
        });
        buttonsPanel.add(connectButton);

        //pack
        add(mainPanel, BorderLayout.CENTER);
        validate();
        repaint();
    }
    SQLconnection sql = null;

    class Host {

        int id;
        String ip;
        int port;
        Locale locale;
        String api;

        public Host(int id, String ip, int port, Locale locale, String api) {
            this.id = id;
            this.ip = ip;
            this.port = port;
            this.locale = locale;
            this.api = api;
        }

        @Override
        public String toString() {
            return "(" + locale.getDisplayCountry() + " " + locale.getLanguage() + ") " + ip;
        }
    }
    Vector<Host> hosts = new Vector<Host>();

    private void refreshServers() {
        sql = new SQLconnection();
        try {
            sql.connect();
            ResultSet localesSet = sql.query("SELECT * FROM locale ORDER BY country ASC");
            Map<Integer, Locale> locales = new HashMap<Integer, Locale>();
            while (localesSet.next()) {
                locales.put(localesSet.getInt("id"), new Locale(localesSet.getString("language"), localesSet.getString("country")));
            }
            ResultSet countrySet = sql.query("SELECT * FROM host WHERE id IN (" +
                    "SELECT id FROM locale " +
                    "WHERE country = '" + Locale.getDefault().getCountry() + "'" +
                    ") AND id NOT IN (" +
                    "SELECT host_id FROM game" +
                    ") ORDER BY ip ASC LIMIT 20");
            ResultSet otherCountriesSet = sql.query("SELECT * FROM host WHERE id NOT IN (" +
                    "SELECT id FROM locale " +
                    "WHERE country = '" + Locale.getDefault().getCountry() + "'" +
                    ") AND id NOT IN (" +
                    "SELECT host_id FROM game" +
                    ") GROUP BY locale ORDER BY ip ASC LIMIT 20");
            while (countrySet.next()) {
                hosts.add(new Host(countrySet.getInt("id"), countrySet.getString("ip"), countrySet.getInt("port"), locales.get(countrySet.getInt("locale")), countrySet.getString("api")));
            }
            while (otherCountriesSet.next()) {
                hosts.add(new Host(otherCountriesSet.getInt("id"), otherCountriesSet.getString("ip"), otherCountriesSet.getInt("port"), locales.get(otherCountriesSet.getInt("locale")), countrySet.getString("api")));
            }
            for (Host h : hosts) {
                if (GameProperties.getApiSpecVersion().equals(h.api)) {
                    serverField.addItem(h);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            exception = ex;
        } finally {
            try {
                sql.disconnect();
            } catch (SQLException ex) {
                ex.printStackTrace();
            } finally {
                sql = null;
                serverField.validate();
                serverField.repaint();
            }
        }
    }

    /** init the dialog */
    private void initDialog() {
        socket = null;
        exception = null;
        connecting = true;
        refreshServers();
    }
    PortMapping portMap = null;

    /** establish the connection */
    private synchronized void _connectionProceed() {
        connectButton.setEnabled(false);
        try {
            /*Here we should verify that the parameters are valid, instead of just throwing an exception...-**/
            int port = Integer.parseInt(portField.getText());
            PortMapping map = new PortMapping("UDP", port, port);
            map.bindUPNPPort();
            portMap = map;
            socket = new Socket(serverField.getSelectedItem().toString(), port);
            connecting = false;
            notify();
        } catch (Exception e) {
            e.printStackTrace();
            exception = e;
            connectionCancel();
        }
    }
    boolean connecting;

    /** cancel the dialog */
    protected synchronized void connectionCancel() {
        try {
            if (socket != null) {
                socket.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (portMap != null) {
                try {
                    portMap.unbindUPNPPort();
                } catch (IOException ex) {
                    ex.printStackTrace();
                } catch (UPNPResponseException ex) {
                    ex.printStackTrace();
                }
            }
            socket = null;
            connecting = false;
            notify();
        }
    }
    ThreadWorks clientConnections = new ThreadWorks("cliConn");

    protected synchronized void connectionProceed() {
        Runnable r = new Runnable() {

            public void run() {
                _connectionProceed();
            }
        };
        clientConnections.doLater(r);
    }

    /**
     * create a ClientConnectionDialog dialog, let the user specifies the parameters and
     * attempt a connection.
     * @param owner the Frame from which the dialog is displayed
     * @return null if no connection could be established (or if the user cancelled the dialog)
     *              otherwise it returns an open socket with connection already established
     */
    public static Socket getConnection(final FieldGui gui) throws InterruptedException {
        final ClientConnectionDialog dialog = new ClientConnectionDialog(gui);
        dialog.initDialog();
        gui.switchGui(dialog, null, new AbstractAction() {

            public void actionPerformed(ActionEvent e) {
                if (dialog.socket == null) {
                    dialog.connectionCancel();
                }
            }
        });
        synchronized (dialog) {
            while (dialog.connecting) {
                dialog.wait();
            }
        }
        if (dialog.exception != null) {
            gui.popExceptionMessage(dialog.exception, ClientConnectionDialog.class);
            return null;
        }
        return dialog.socket;
    }
}
