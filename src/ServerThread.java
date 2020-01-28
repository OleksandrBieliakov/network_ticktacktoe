import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

public class ServerThread extends Thread {

    private ServerLogic serverLogic;
    private UDPServer udpServer;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private int playerID;
    private Match currentMatch;

    public ServerThread(Socket socket, ServerLogic serverLogic, UDPServer udpServer) {
        super();
        this.socket = socket;
        this.serverLogic = serverLogic;
        this.udpServer = udpServer;
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
        } catch (Exception e) {
            System.out.println("No I/O");
            System.exit(-1);
        }
    }

    private void list() {
        try {
            for (PlayerEntry playerEntry : serverLogic.getPlayers()) {
                out.println(playerEntry);
            }
            out.println("END");
        } catch (Exception e) {
            System.out.println("Error during sending the list of players");
        }
    }

    private void requestMatchmaking() {
        Match match = serverLogic.firstRequestMatchmaking(playerID);
        while (match == null) {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            match = serverLogic.reRequestMatchmaking(playerID);
        }
        currentMatch = match;
        try {
            out.println(currentMatch.getOpponentID(playerID) + " " + (currentMatch.hasFirstTurn(playerID) ? 1 : 2)); // 1 - first turn, 2 - second turn
            udpServer.broadcast("Match " + currentMatch.getMatchID() + " started (player " + playerID + ")");
        } catch (Exception e) {
            System.out.println("Error during matchmaking");
        }
    }

    private void receiveTurn() {
        try {
            int cell = Integer.parseInt(in.readLine());
            currentMatch.submitTurn(playerID, cell);
            udpServer.broadcast("Match " + currentMatch.getMatchID() + " player " + playerID + " ("+ (currentMatch.hasFirstTurn(playerID) ? "X" : "0") + ") - " + cell);
        } catch (Exception e) {
            System.out.println("Error during receiving turn");
        }
    }

    private boolean sendGameStatus() {
        while (!currentMatch.gameEnded() && !currentMatch.hasOpponentMadeTurn(playerID)) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        boolean gameEnded = false;
        try {
            String message = currentMatch.opponentLastTurn(playerID) + "";
            int matchResult = currentMatch.matchResult(playerID);
            if (matchResult == 1) {
                message = message + " WIN";
                gameEnded = true;
                udpServer.broadcast("Match " + currentMatch.getMatchID() + " player " + playerID + " won");
            } else if (matchResult == 2) {
                message = message + " LOSE";
                gameEnded = true;
                udpServer.broadcast("Match " + currentMatch.getMatchID() + " player " + playerID + " lost");
            } else if (matchResult == 3) {
                message = message + " DRAW";
                gameEnded = true;
                udpServer.broadcast("Match " + currentMatch.getMatchID() + " draw (player " + playerID + ")");
            }
            out.println(message);
        } catch (Exception e) {
            System.out.println("Error during sending game status");
        }
        return gameEnded;
    }

    private void play() {
        requestMatchmaking();
        if (currentMatch.hasFirstTurn(playerID)) {
            receiveTurn();
        }
        while (!sendGameStatus()) {
            receiveTurn();
        }
    }

    private void logout() {
        try {
            serverLogic.removePlayer(playerID);
            udpServer.broadcast("Player " + playerID + " logged out");
        } catch (Exception e) {
            System.out.println("Error during logout");
        }
    }

    private void close() {
        try {
            socket.close();
        } catch (Exception e) {
            System.out.println("Cannot close the socket");
            System.exit(-1);
        }
    }

    private void addPlayer() {
        try {
            playerID = serverLogic.nextID();
            serverLogic.addPlayer(playerID, socket.getInetAddress().toString(), socket.getPort());
            out.println(playerID);
            udpServer.broadcast("Player " + playerID + " logged in");
        } catch (Exception e) {
            System.out.println("Error during sending playerID");
        }
    }

    private String receiveCommand() {
        String command = null;
        try {
            command = in.readLine();
        } catch (Exception e) {
            System.out.println("Error during receiving a command");
        }
        return command;
    }

    public void run() {
        addPlayer();

        String command;
        while (true) {
            command = receiveCommand();

            if (command != null) {
                if (command.equals(Command.LIST.name())) {
                    list();
                } else if (command.equals(Command.PLAY.name())) {
                    play();
                } else if (command.equals(Command.LOGOUT.name())) {
                    logout();
                    break;
                }
            }
        }
        close();
    }

}
