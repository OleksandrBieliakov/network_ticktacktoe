import java.net.InetAddress;
import java.util.Objects;

public class ViewerEntry {

    private InetAddress address;
    private int port;

    public ViewerEntry(InetAddress address, int port) {
        this.address = address;
        this.port = port;
    }

    public InetAddress getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ViewerEntry)) return false;
        ViewerEntry that = (ViewerEntry) o;
        return getPort() == that.getPort() &&
                getAddress().equals(that.getAddress());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getAddress(), getPort());
    }

}
