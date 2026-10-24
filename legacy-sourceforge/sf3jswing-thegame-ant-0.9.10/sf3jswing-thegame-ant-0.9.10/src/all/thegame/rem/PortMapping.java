package thegame.rem;

import java.io.IOException;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Enumeration;
import net.sbbi.upnp.impls.InternetGatewayDevice;
import net.sbbi.upnp.messages.UPNPResponseException;
import net.sf.jiga.xtended.kernel.JXAenvUtils;
import thegame.Sf3;

public class PortMapping {

    boolean mapped = false;
    InternetGatewayDevice gateway = null;
    public int externalPort;
    public String protocol;
    public int internalPort;
    public String localHostIP = null;

    static InternetGatewayDevice[] getDevices(int discoveryTimeout) throws IOException {
        return InternetGatewayDevice.getDevices(discoveryTimeout);
    }
    AccessControlContext acc = Sf3.acc;

    public PortMapping() throws IOException, UPNPResponseException {
        this("TCP", 9090, 9090);
    }

    public PortMapping(String protocol, int internalPort, int externalPort) throws IOException, UPNPResponseException {
        super();
        this.protocol = protocol;
        this.internalPort = internalPort;
        this.externalPort = externalPort;
        findLocalHostIP();
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {

                public Object run() throws Exception {
                    bindGateway();
                    return null;
                }
            }, acc);
        } catch (PrivilegedActionException ex) {
            if (ex.getException() instanceof IOException) {
                throw (IOException) ex.getException();
            }
            if (ex.getException() instanceof UPNPResponseException) {
                throw (UPNPResponseException) ex.getException();
            }
        }
    }

    public boolean unbindUPNPPort() throws IOException, UPNPResponseException {
        try {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<Boolean>() {

                public Boolean run() throws Exception {
                    return gateway == null ? null : gateway.deletePortMapping(null, externalPort, protocol);
                }
            }, acc);
        } catch (PrivilegedActionException ex) {
            if (ex.getException() instanceof IOException) {
                throw (IOException) ex.getException();
            }
            if (ex.getException() instanceof UPNPResponseException) {
                throw (UPNPResponseException) ex.getException();
            }
            return false;
        }

    }
    /** secs to receive a response from devices */
    int discoveryTimeout = 5000;

    private void bindGateway() throws IOException {
        InternetGatewayDevice[] IGDs = getDevices(discoveryTimeout);
        if(IGDs != null) {
            // let's the the first device found
            gateway = IGDs[0];
            System.out.println(JXAenvUtils._JXAEnvOutput("UPNP Found Gateway device " + gateway.getIGDRootDevice().getModelName(), JXAenvUtils.APP_NOTICE));
        } else {
            System.out.println(JXAenvUtils._JXAEnvOutput("No UPNP Gateway device.", JXAenvUtils.APP_NOTICE));
        }
    }

    public String getWANIP() throws UPNPResponseException, IOException {
        try {
            return AccessController.doPrivileged(new PrivilegedExceptionAction<String>() {

                public String run() throws Exception {
                    if (gateway != null) {
                        return gateway.getExternalIPAddress();
                    } else {
                        return localHostIP;
                    }
                }
            }, acc);
        } catch (PrivilegedActionException ex) {
            if (ex.getException() instanceof IOException) {
                throw (IOException) ex.getException();
            }
            if (ex.getException() instanceof UPNPResponseException) {
                throw (UPNPResponseException) ex.getException();
            }
            return localHostIP;
        }
    }

    private void findLocalHostIP() throws UnknownHostException, SocketException {
        Enumeration<NetworkInterface> nic = NetworkInterface.getNetworkInterfaces();
        while (nic.hasMoreElements()) {
            NetworkInterface anic = nic.nextElement();
            Enumeration<InetAddress> addrs = anic.getInetAddresses();
            while (addrs.hasMoreElements()) {
                InetAddress addr = addrs.nextElement();
                if (!addr.isLoopbackAddress() && addr.isSiteLocalAddress()) {
                    localHostIP = addr.getHostAddress();
                    break;
                }
            }
            if (localHostIP != null) {
                break;
            }
        }
        if (localHostIP == null) {
            localHostIP = InetAddress.getLocalHost().getHostAddress();
        }
    }

    public void bindUPNPPort() throws IOException, UPNPResponseException {
        try {
            AccessController.doPrivileged(new PrivilegedExceptionAction<Object>() {

                public Object run() throws IOException, UPNPResponseException {
                    if (gateway != null) {
                        // now let's open the port
                        // we assume that localHostIP is something else than 127.0.0.1
                        mapped = gateway.addPortMapping("Some mapping description", null, internalPort, externalPort, localHostIP, 0, protocol);
                        if (mapped) {
                            System.out.println(JXAenvUtils._JXAEnvOutput("UPNP Port " + externalPort + " mapped for " + protocol + " to " + localHostIP + " : " + internalPort, JXAenvUtils.APP_NOTICE));
                        } else {
                            throw new IOException("UPNP Port " + externalPort + " not mapped");
                        }
                    }
                    return null;
                }
            }, acc);
        } catch (PrivilegedActionException ex) {
            if (ex.getException() instanceof IOException) {
                throw (IOException) ex.getException();
            }
            if (ex.getException() instanceof UPNPResponseException) {
                throw (UPNPResponseException) ex.getException();
            }
        }
    }
}
