import java.net.ServerSocket;
import java.net.Socket;

public class Server {

    private ServerSocket serverSocket;
    private ServerLogic serverLogic = new ServerLogic();
    private UDPServer udpServer = new UDPServer();

    public Server(int port) {
        try {
            serverSocket = new ServerSocket(port);
        } catch (Exception e) {
            System.out.println("Could not listen");
            System.exit(-1);
        }
        System.out.println("Server listens on port: " + port);
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
        int port = Integer.parseInt(args[0]);
        new Server(port).listen();
    }

}
