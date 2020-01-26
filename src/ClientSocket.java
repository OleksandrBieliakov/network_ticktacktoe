import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

public class ClientSocket {

    private Socket socket = null;
    private PrintWriter out = null;
    private BufferedReader in = null;
    private int playerID;

    public ClientSocket(String serverAddress, int serverPort) {
        try {
            socket = new Socket(serverAddress, serverPort);
            out = new PrintWriter(socket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        } catch (UnknownHostException e) {
            System.out.println("Unknown host");
            System.exit(-1);
        } catch (Exception e) {
            System.out.println("No I/O");
            System.exit(-1);
        }
        receiveID();
    }

    private void receiveID() {
        try {
            playerID = Integer.parseInt(in.readLine());
        } catch (Exception e) {
            System.out.println("Error during receiving ID from server");
            System.exit(-1);
        }
    }

    public int getPlayerID() {
        return playerID;
    }

    public List<PlayerEntry> listOfPlayers() {
        List<PlayerEntry> players = new ArrayList<>();
        try {
            out.println(Command.LIST);
            String line;
            while (!"END".equals(line = in.readLine())) {
                PlayerEntry playerEntry = new PlayerEntry(line);
                players.add(playerEntry);
            }
        } catch (Exception e) {
            System.out.println("Error during list requesting");
        }
        return players;
    }

    public List<Integer> requestOpponent() {
        List<Integer> opponentIDandTurn = null;
        try {
            out.println(Command.PLAY);
            String answer = in.readLine();
            if (!answer.equals("NOT FOUND")) {
                String[] parts = answer.split(" ");
                int opponentID = Integer.parseInt(parts[0]);
                int turn = Integer.parseInt(parts[1]);
                opponentIDandTurn = new ArrayList<>();
                opponentIDandTurn.add(opponentID);
                opponentIDandTurn.add(turn);
            }
        } catch (Exception e) {
            System.out.println("Error during opponent request");
        }
        return opponentIDandTurn;
    }

    public void makeTurn(int cell) {
        try {
            out.println(cell);
        } catch (Exception e) {
            System.out.println("Error during making turn");
        }
    }

    public List<Integer> getGameStatus() {
        List<Integer> status = new ArrayList<>();
        try {
            String message = in.readLine();
            String[] parts = message.split(" ");
            int cell = Integer.parseInt(parts[0]);
            status.add(cell);
            if (parts.length > 1) {
                if (parts[1].equals("WIN")) {
                    status.add(1);
                } else if (parts[1].equals("LOSE")) {
                    status.add(2);
                } else {
                    status.add(3); // DRAW
                }
            }
        } catch (Exception e) {
            System.out.println("Error during getting game status");
        }
        return status;
    }

    public void logout() {
        try {
            out.println(Command.LOGOUT);
        } catch (Exception e) {
            System.out.println("Error during logout");
        }
        close();
    }

    private void close() {
        try {
            socket.close();
        } catch (Exception e) {
            System.out.println("Cannot close the socket");
            System.exit(-1);
        }
    }

}