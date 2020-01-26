import java.util.List;
import java.util.Scanner;

public class GameConsole {

    private static final int TURN_X = 0b10, TURN_0 = 0b01;

    private ClientSocket clientSocket;
    private int board = 0;
    private boolean isX = false;

    public GameConsole(ClientSocket clientSocket) {
        this.clientSocket = clientSocket;
    }

    private String scanCommand() {
        System.out.println("Enter command (LIST, PLAY, LOGOUT)");
        Scanner scanner = new Scanner(System.in);
        String command = scanner.nextLine();
        scanner.close();
        return command;
    }

    private boolean isValidCommand(String command) {
        return command.equals(Command.LIST.name()) || command.equals(Command.PLAY.name()) || command.equals(Command.LOGOUT.name());
    }

    private void executeCommand(String command) {
        if (command.equals(Command.LIST.name())) {
            list();
        } else if (command.equals(Command.PLAY.name())) {
            play();
        } else {
            logout();
        }
    }

    private void list() {
        System.out.println("Players online:");
        for (PlayerEntry playerEntry : clientSocket.listOfPlayers()) {
            System.out.println(playerEntry);
        }
    }

    private void play() {
        List<Integer> matchInfo = clientSocket.requestOpponent();
        if (matchInfo == null) {
            System.out.println("Match not found");
            return;
        }
        System.out.println("Opponent ID: " + matchInfo.get(0));
        if (matchInfo.get(1) == 2) {
            System.out.println("You are playing as 0 (you start second");
            displayBoard(board);
        } else {
            System.out.println("You are playing as X (you start first");
            isX = true;
            displayBoard(board);
            makeTurn();
        }
        while (checkGameState()) {
            makeTurn();
        }
    }

    private void makeTurn() {
        int position = scanPosition();
        board = makeTurn(board, position, isX);
        displayBoard(board);
        clientSocket.makeTurn(position);
    }

    private boolean checkGameState() {
        List<Integer> state = clientSocket.getGameStatus();
        int position = state.get(0);
        board = makeTurn(board, position, !isX);
        displayBoard(board);
        boolean gameContinues = true;
        if (state.size() > 1) {
            gameResult(state.get(1));
            gameContinues = false;
        }
        return gameContinues;
    }

    private void gameResult(int result) {
        switch (result) {
            case 1:
                System.out.println("You won!");
                break;
            case 2:
                System.out.println("You lost");
                break;
            case 3:
                System.out.println("Draw");
        }
    }

    private int scanPosition() {
        System.out.println("Enter index of a free cell (1-9)");
        Scanner scan = new Scanner(System.in);
        int position = scan.nextInt();
        scan.close();
        return position;
    }

    private void logout() {
        clientSocket.logout();
        System.exit(0);
    }

    private void command() {
        String command = scanCommand();
        while (!isValidCommand(command)) {
            System.out.println("Invalid command");
            command = scanCommand();
        }
        executeCommand(command);
    }

    public void run() {
        System.out.println("Your playerID is " + clientSocket.getPlayerID());
        while (true) {
            command();
        }
    }

    private static int makeTurn(int board, int position, boolean xTurn) {
        int numSymbol = xTurn ? TURN_X : TURN_0;
        numSymbol <<= (position - 1) * 2;
        return board | numSymbol;
    }

    private static void printSolid() {
        for (int i = 0; i < 6; ++i) {
            System.out.print("* ");
        }
        System.out.println("*");
    }

    private static int shiftPrint(int board) {
        if ((board & TURN_X) == TURN_X)
            System.out.print("X");
        else if ((board & TURN_0) == TURN_0)
            System.out.print("0");
        else
            System.out.print(" ");
        return board >>= 2;
    }

    private static int printLine(int board) {
        System.out.print("*");
        System.out.print(" ");
        board = shiftPrint(board);
        System.out.print(" ");
        System.out.print("*");
        System.out.print(" ");
        board = shiftPrint(board);
        System.out.print(" ");
        System.out.print("*");
        System.out.print(" ");
        board = shiftPrint(board);
        System.out.print(" ");
        System.out.print("*");

        System.out.println();
        return board;
    }

    private static void displayBoard(int board) {
        printSolid();
        board = printLine(board);

        printSolid();
        board = printLine(board);

        printSolid();
        printLine(board);
        printSolid();

        System.out.println();
    }

    public static void main(String[] args) {
        String address = args[0];
        int port = Integer.parseInt(args[1]);
        ClientSocket clientSocket = new ClientSocket(address, port);
        new GameConsole(clientSocket).run();
    }

}
