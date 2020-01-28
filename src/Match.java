import java.util.Objects;

public class Match {

    private static final int TURN_X = 0b10, TURN_0 = 0b01;

    private static final int[] WIN_X = {
            0b10_10_10, 0b10_10_10_00_00_00, 0b10_10_10_00_00_00_00_00_00,
            0b00_00_10_00_00_10_00_00_10, 0b00_10_00_00_10_00_00_10_00, 0b10_00_00_10_00_00_10_00_00,
            0b10_00_00_00_10_00_00_00_10, 0b00_00_10_00_10_00_10_00_00
    };
    private static final int[] WIN_0 = {
            0b01_01_01, 0b01_01_01_00_00_00, 0b01_01_01_00_00_00_00_00_00,
            0b00_00_01_00_00_01_00_00_01, 0b00_01_00_00_01_00_00_01_00, 0b01_00_00_01_00_00_01_00_00,
            0b01_00_00_00_01_00_00_00_01, 0b00_00_01_00_01_00_01_00_00
    };

    private static int nextID = 0;

    private int matchID;
    private PlayerEntry playerX;
    private PlayerEntry player0;
    private int lastPlayerXCell;
    private int lastPlayer0Cell;
    private boolean playerXWon = false;
    private boolean player0Won = false;
    private boolean draw = false;
    private int turnCounter = 0;
    private int board = 0;
    private int playerXLastTurn;
    private int player0LastTurn;

    public Match(PlayerEntry playerX, PlayerEntry player0) {
        matchID = nextID();
        this.playerX = playerX;
        this.player0 = player0;
    }

    private static synchronized int nextID() {
        return nextID++;
    }

    public int getMatchID() {
        return matchID;
    }

    public int getOpponentID(int playerID) {
        return playerID == playerX.getPlayerID() ? player0.getPlayerID() : playerX.getPlayerID();
    }

    public boolean hasFirstTurn(int playerID) {
        return playerX.getPlayerID() == playerID;
    }

    public synchronized boolean hasOpponentMadeTurn(int playerID) {
        if (turnCounter == 0) {
            return false;
        }
        boolean result;
        if (playerX.getPlayerID() == playerID) {
            result = turnCounter == player0LastTurn;
        } else {
            result = turnCounter == playerXLastTurn;
        }
        return result;
    }

    // the information about the board state is stored in a single integer and updated using binary shift operations
    public synchronized void submitTurn(int playerID, int position) {
        turnCounter++;
        int numSymbol;
        if (playerX.getPlayerID() == playerID) {
            lastPlayerXCell = position;
            numSymbol = TURN_X;
            playerXLastTurn = turnCounter;
        } else {
            lastPlayer0Cell = position;
            numSymbol = TURN_0;
            player0LastTurn = turnCounter;
        }
        numSymbol <<= (position - 1) * 2;
        board = board | numSymbol;
    }

    // the information about the board state is stored in a single integer and win condition is checked by comparing the board with sets of all possible win conditions
    private synchronized void checkWinCondition() {
        for (int win : WIN_X) {
            if ((board & win) == win) {
                playerXWon = true;
                return;
            }
        }
        for (int win : WIN_0) {
            if ((board & win) == win) {
                player0Won = true;
                return;
            }
        }
        if (turnCounter == 9) {
            draw = true;
        }
    }

    public synchronized int opponentLastTurn(int playerID) {
        int turn;
        if (playerID == playerX.getPlayerID()) {
            turn = lastPlayer0Cell;
        } else {
            turn = lastPlayerXCell;
        }
        return turn;
    }

    public synchronized boolean gameEnded() {
        return draw || playerXWon || player0Won;
    }

    public synchronized int matchResult(int playerID) {
        checkWinCondition();
        int result;
        if (draw) {
            result = 3;
        } else if (playerXWon) {
            if (playerX.getPlayerID() == playerID) {
                result = 1;
            } else {
                result = 2;
            }
        } else if (player0Won) {
            if (player0.getPlayerID() == playerID) {
                result = 1;
            } else {
                result = 2;
            }
        } else {
            result = 0;
        }
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Match)) return false;
        Match match = (Match) o;
        return getMatchID() == match.getMatchID() &&
                playerX.equals(match.playerX) &&
                player0.equals(match.player0);
    }

    @Override
    public int hashCode() {
        return Objects.hash(getMatchID(), playerX, player0);
    }

}
