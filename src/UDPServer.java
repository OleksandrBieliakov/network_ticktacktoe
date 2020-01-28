import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.HashSet;
import java.util.Set;

public class UDPServer {

    private DatagramSocket server;
    private Set<ViewerEntry> viewers = new HashSet<>();

    public UDPServer() {
        try {
            server = new DatagramSocket();
        } catch (SocketException e) {
            System.out.println("Error during creating UDP socket");
        }
    }

    public void broadcast(String message) {
        new Thread(() -> {
            byte[] respBuff = message.getBytes();
            for (ViewerEntry viewer : viewers) {
                DatagramPacket resp = new DatagramPacket(respBuff, respBuff.length, viewer.getAddress(), viewer.getPort());
                try {
                    server.send(resp);
                } catch (Exception e) {
                    System.out.println("Error during broadcast");
                }
            }
        }).start();
    }

    public void listen() {
        new Thread(() -> {
            while (true) {
                System.out.println("Listening");
                byte[] buff = new byte[UDP.MAX_DATAGRAM_SIZE];
                DatagramPacket datagram = new DatagramPacket(buff, buff.length);

                try {
                    server.receive(datagram);
                } catch (Exception ex) {
                    System.out.println("Error during viewer login");
                }

                String info = new String(datagram.getData(), 0, datagram.getLength()).trim();
                if (info.equals("VIEWER")) {
                    InetAddress viewerAddress = datagram.getAddress();
                    int viewerPort = datagram.getPort();
                    viewers.add(new ViewerEntry(viewerAddress, viewerPort));
                }
                System.out.println("Listened");
            }
        }).start();
    }

}