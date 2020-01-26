import java.util.*;

public class ServerLogic {

    private int nextID = 0;
    private Map<Integer, PlayerEntry> players = new HashMap<>();
    private Map<Integer, Match> matches = new HashMap<>();
    private PlayerEntry playerWaitingToPlay = null;
    private Match lobby = null;

    public synchronized int nextID() {
        return nextID++;
    }

    public synchronized void addPlayer(int playerID, String address, int port) {
        players.put(playerID, new PlayerEntry(playerID, address, port));
    }

    public synchronized void removePlayer(int playerID) {
        players.remove(playerID);
    }

    public synchronized Collection<PlayerEntry> getPlayers() {
        return players.values();
    }

    public synchronized Match firstRequestMatchmaking(int playerID) {
        PlayerEntry requestingPlayer = players.get(playerID);
        if (playerWaitingToPlay == null) {
            playerWaitingToPlay = requestingPlayer;
            return null;
        }
        Match match = new Match(playerWaitingToPlay, requestingPlayer);
        lobby = match;
        playerWaitingToPlay = null;
        return match;
    }

    public synchronized Match reRequestMatchmaking(int playerID) {
        if (lobby != null) {
            matches.put(lobby.getMatchID(), lobby);
        }
        return lobby;
    }

}
