import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    private ServerSocket serverSocket;
    private ServerLogic serverLogic = new ServerLogic();
    private UDPServer udpServer;

    public Server(int portTCP, int portUDP) {
        udpServer = new UDPServer(portUDP);
        try {
            serverSocket = new ServerSocket(portTCP);
        } catch (Exception e) {
            System.out.println("Could not listen");
            System.exit(-1);
        }
        System.out.println("Server listens on ports: " + portTCP + "(TCP), " + portUDP + "(UDP)");
    }

    public void listen() {
        udpServer.listen();
        while (true) {
            Socket client = null;
            try {
                client = serverSocket.accept();
            } catch (Exception e) {
                System.out.println("Accept failed");
                System.exit(-1);
            }
            if (client != null) {
                (new ServerThread(client, serverLogic, udpServer)).start();
            }
        }
    }

    public static void main(String[] args) {
        int portTCP = Integer.parseInt(args[0]);
        int portUDP = Integer.parseInt(args[1]);
        new Server(portTCP, portUDP).listen();
    }

}
