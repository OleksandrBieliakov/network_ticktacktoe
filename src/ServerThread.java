import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

public class ServerThread extends Thread {

    private ServerLogic serverLogic;
    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private int playerID;
    private Match currentMatch;

    public ServerThread(Socket socket, ServerLogic serverLogic) {
        super();
        this.socket = socket;
        this.serverLogic = serverLogic;
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
        } catch (Exception e) {
            System.out.println("Error during matchmaking");
        }
    }

    private void receiveTurn() {
        try {
            int cell = Integer.parseInt(in.readLine());
            currentMatch.submitTurn(playerID, cell);
        } catch (Exception e) {
            System.out.println("Error during receiving turn");
        }
    }

    private boolean sendGameStatus() {
        boolean gameEnded = false;
        try {
            String message = currentMatch.opponentLastTurn(playerID) + "";
            int matchResult = currentMatch.matchResult(playerID);
            if (matchResult == 1) {
                message = message + " WIN";
                gameEnded = true;
            } else if (matchResult == 2) {
                message = message + " LOSE";
                gameEnded = true;
            } else if (matchResult == 3) {
                message = message + " DRAW";
                gameEnded = true;
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
        while (sendGameStatus()) {
            receiveTurn();
        }
    }

    private void logout() {
        try {
            serverLogic.removePlayer(playerID);
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
