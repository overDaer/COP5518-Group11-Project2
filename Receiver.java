
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

/*
 * Receiver.java
 * Systems and Networks II
 * Project 2
 *
 * This file describes the functions to be implemented by the Receiver class
 * You may also implement any auxillary functions you deem necessary.
 */
public class Receiver {

    private static final int BUFFER_SIZE = 256;
    private DatagramSocket _socket; // the socket for communication with clients
    private int _port; // the port number for communication with this server
    private boolean _continueService;

    /**
     * Constructs a Receiver object.
     */
    public Receiver(int port) {
        _port = port;
    }

    /**
     * Creates a datagram socket and binds it to a free port.
     *
     * @return - 0 or a negative number describing an error code if the connection
     *         could not be established
     */
    public int createSocket() {
        try {
            _socket = new DatagramSocket(_port);
        } catch (SocketException ex) {
            System.err.println("unable to create and bind socket");
            return -1;
        }

        return 0;
    }

    public void run() {
        // run server until gracefully shut down
        _continueService = true;

        while (_continueService) {
            DatagramPacket newDatagramPacket = receiveRequest();

            String request = new String(newDatagramPacket.getData()).trim();

            System.out.println("sender IP: " + newDatagramPacket.getAddress().getHostAddress());
            System.out.println("sender request: " + request);

            if (request.equals("<shutdown/>")) {
                _continueService = false;
            }

            if (request != null) {

                String response = "<echo>" + request + "</echo>";

                sendResponse(
                        response,
                        newDatagramPacket.getAddress().getHostName(),
                        newDatagramPacket.getPort());
            } else {
                System.err.println("incorrect response from server");
            }
        }
    }

    /**
     * Sends a request for service to the server. Do not wait for a reply in this
     * function. This will be
     * an asynchronous call to the server.
     *
     * @param response - the response to be sent
     * @param hostAddr - the ip or hostname of the server
     * @param port     - the port number of the server
     *
     * @return - 0, if no error; otherwise, a negative number indicating the error
     */
    public int sendResponse(String response, String hostAddr, int port) {
        DatagramPacket newDatagramPacket = createDatagramPacket(response, hostAddr, port);
        if (newDatagramPacket != null) {
            try {
                _socket.send(newDatagramPacket);
            } catch (IOException ex) {
                System.err.println("unable to send message to server");
                return -1;
            }

            return 0;
        }

        System.err.println("unable to create message");
        return -1;
    }

    /**
     * Receives a client's request.
     *
     * @return - the datagram containing the client's request or NULL if an error
     *         occured
     */
    public DatagramPacket receiveRequest() {
        byte[] buffer = new byte[BUFFER_SIZE];
        DatagramPacket newDatagramPacket = new DatagramPacket(buffer, BUFFER_SIZE);
        try {
            _socket.receive(newDatagramPacket);
        } catch (IOException ex) {
            System.err.println("unable to receive message from server");
            return null;
        }

        return newDatagramPacket;
    }

    /*
     * Prints the response to the screen in a formatted way.
     *
     * response - the server's response as an XML formatted string
     *
     */
    public static void printResponse(String response) {
        System.out.println("FROM SERVER: " + response);
    }

    /*
     * Closes an open socket.
     *
     * @return - 0, if no error; otherwise, a negative number indicating the error
     */
    public int closeSocket() {
        _socket.close();

        return 0;
    }

    /**
     * The main function. Use this function for
     * testing your code. We will provide a new main function on the day of the lab
     * demo.
     */
    public static void main(String[] args) {
        Receiver server;
        String serverName;
        String req;

        if (args.length != 1) {
            System.err.println("Usage: Receiver <port number>\n");
            return;
        }

        int portNum;
        try {
            portNum = Integer.parseInt(args[0]);
        } catch (NumberFormatException xcp) {
            System.err.println("Usage: Receiver <port number>\n");
            return;
        }

        // construct client and client socket
        server = new Receiver(portNum);
        if (server.createSocket() < 0) {
            return;
        }

        server.run();
        server.closeSocket();
    }

    /**
     * Creates a datagram from the specified request and destination host and port
     * information.
     *
     * @param request  - the request to be submitted to the server
     * @param hostname - the hostname of the host receiving this datagram
     * @param port     - the port number of the host receiving this datagram
     *
     * @return a complete datagram or null if an error occurred creating the
     *         datagram
     */
    private DatagramPacket createDatagramPacket(String request, String hostname, int port) {
        byte buffer[] = new byte[BUFFER_SIZE];

        // empty message into buffer
        for (int i = 0; i < BUFFER_SIZE; i++) {
            buffer[i] = '\0';
        }

        // copy message into buffer
        byte data[] = request.getBytes();
        System.arraycopy(data, 0, buffer, 0, Math.min(data.length, buffer.length));

        InetAddress hostAddr;
        try {
            hostAddr = InetAddress.getByName(hostname);
        } catch (UnknownHostException ex) {
            System.err.println("invalid host address");
            return null;
        }

        return new DatagramPacket(buffer, BUFFER_SIZE, hostAddr, port);
    }
}
