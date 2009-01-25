package org.jdyna.network.sockets.packets;

import java.io.Serializable;

/**
 * Server information.
 */
public final class ServerInfo implements Serializable
{
    /** Keep the default here. */
    private static final long serialVersionUID = 1L;

    /** Frame data broadcast port. */
    public int UDPBroadcastPort;

    /** Control port. */
    public int TCPControlPort;

    /** UDP feedback port. */
    public int UDPFeedbackPort;

    /** Server IP address. */
    public String serverAddress;

    protected ServerInfo()
    {
        // do nothing.
    }

    public ServerInfo(String serverIP, int TCPControlPort, int UDPBroadcastPort, int UDPFeedbackPort)
    {
        this.TCPControlPort = TCPControlPort;
        this.UDPBroadcastPort = UDPBroadcastPort;
        this.UDPFeedbackPort = UDPFeedbackPort;
        this.serverAddress = serverIP;
    }

    /*
     * 
     */
    @Override
    public String toString()
    {
        return "IP=" + serverAddress + ", TCP control port="
            + TCPControlPort + ", UDPBroadcast=" + UDPBroadcastPort + ", UDPFeedback="
            + UDPFeedbackPort;
    }
}
