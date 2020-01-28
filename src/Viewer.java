import java.net.*;

public class Viewer {

    private DatagramSocket socket;
    private InetAddress serverAddress;
    private int serverPort;

    public Viewer(String serverAddress, int serverPort) {
        try {
            socket = new DatagramSocket();
        } catch (SocketException e) {
            System.out.println("Error during socket creation");
        }
        try {
            this.serverAddress = InetAddress.getByName(serverAddress);
        } catch (UnknownHostException e) {
            System.out.println("Unknown host");
        }
        this.serverPort = serverPort;
    }

    public void run() {
        introduceToServer();
        while (true) {
            receiveInfo();
        }
    }

    private void introduceToServer() {
        byte[] queryBuff = "VIEWER".getBytes();
        DatagramPacket query = new DatagramPacket(queryBuff, queryBuff.length, serverAddress, serverPort);
        try {
            socket.send(query);
        } catch (Exception e) {
            System.out.println("Error during introducing");
        }
        System.out.println("Introduced");
    }

    private void receiveInfo() {
        byte[] buff = new byte[UDP.MAX_DATAGRAM_SIZE];
        DatagramPacket packet = new DatagramPacket(buff, buff.length);
        try {
            socket.receive(packet);
        } catch (Exception e) {
            System.out.println("Error during receiving info from server");
        }
        String info = new String(packet.getData(), 0, packet.getLength()).trim();
        System.out.println(info);
    }

    private void logout() {
        byte[] queryBuff = "LOGOUT".getBytes();
        DatagramPacket query = new DatagramPacket(queryBuff, queryBuff.length, serverAddress, serverPort);
        try {
            socket.send(query);
        } catch (Exception e) {
            System.out.println("Error during logout");
        }
    }

    public static void main(String[] args) {
        String address = args[0];
        int port = Integer.parseInt(args[1]);
        new Viewer(address, port).run();
    }

}
