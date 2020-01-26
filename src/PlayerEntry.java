import java.util.Objects;

public class PlayerEntry {

    private int playerID;
    private String address;
    private int port;

    public PlayerEntry(int playerID, String address, int port) {
        this.playerID = playerID;
        this.address = address;
        this.port = port;
    }

    public PlayerEntry(String line) {
        String[] parts = line.split(" ");
        this.playerID = Integer.parseInt(parts[0]);
        this.address = parts[1];
        this.port = Integer.parseInt(parts[2]);
    }

    public int getPlayerID() {
        return playerID;
    }

    public String getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    @Override
    public String toString() {
        return playerID + " " + address + " " + port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlayerEntry)) return false;
        PlayerEntry that = (PlayerEntry) o;
        return getPlayerID() == that.getPlayerID() &&
                getPort() == that.getPort() &&
                getAddress().equals(that.getAddress());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPlayerID(), getAddress(), getPort());
    }
}
