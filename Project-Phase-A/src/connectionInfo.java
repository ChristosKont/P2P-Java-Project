import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class connectionInfo {

    private connectedPeer cp;
    private Socket connection;
    private ObjectOutputStream out;
    private ObjectInputStream in;

    public connectionInfo(connectedPeer cp, Socket connection, ObjectOutputStream out, ObjectInputStream in) {
        this.cp = cp;
        this.connection = connection;
        this.out = out;
        this.in = in;
    }

    public connectedPeer getCp() {
        return cp;
    }

    public void setCp(connectedPeer cp) {
        this.cp = cp;
    }

    public Socket getConnection() {
        return connection;
    }

    public void setConnection(Socket connection) {
        this.connection = connection;
    }

    public ObjectOutputStream getOut() {
        return out;
    }

    public void setOut(ObjectOutputStream out) {
        this.out = out;
    }

    public ObjectInputStream getIn() {
        return in;
    }

    public void setIn(ObjectInputStream in) {
        this.in = in;
    }
}
