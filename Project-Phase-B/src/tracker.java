import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class tracker {

    private static ServerSocket server;
    private static Socket connection;
    private static ObjectOutputStream out;
    private static ObjectInputStream in;

    private static List<Integer> ports;
    private static List<String> registeredPList;
    private static List<String> connectedPList;

    public static void main(String[] args) throws IOException, ClassNotFoundException {

        ports = new ArrayList<>();
        registeredPList = new ArrayList<>();
        connectedPList = new ArrayList<>();

        server = new ServerSocket(5050);

        while (true) {

            acceptConnection();

            serve();

            disconnect();

        }


    }

    public static void serve() throws IOException, ClassNotFoundException {

        int code = (int) in.readObject();

        if (code == 1) {
            doRegister();
        }

        if (code == 2) {
            doLogin();
        }

        if (code == 3) {
            randomPort();
        }

    }

    public static void acceptConnection() throws IOException {

        // Accepting connection...
        System.out.println("\n[Peer]: Awaiting Connection...");
        connection = server.accept();

        System.out.println("[Peer]: Connection Established!");

        // Objects created!
        out = new ObjectOutputStream(connection.getOutputStream());
        in = new ObjectInputStream(connection.getInputStream());

    }

    public static void disconnect() throws IOException {

        // Closing objects...
        out.close();
        in.close();

        // Connection Closed!
        connection.close();

    }

    public static void doRegister () throws IOException, ClassNotFoundException {

        String username = (String) in.readObject();

        boolean registered = !registeredPList.contains(username);

        out.writeObject(registered);
        out.flush();

        // Peer is already registered!
        if (!registered) return;

        String password = (String) in.readObject();
        int port = (int) in.readObject();

        registeredPList.add(username);
        connectedPList.add(username+password);
        ports.add(port);

    }

    public static void doLogin () throws IOException, ClassNotFoundException {

        String username = (String) in.readObject();
        String password = (String) in.readObject();

        // Username exists, checking password...
        if (registeredPList.contains(username)) {

            if (connectedPList.contains(username+password)) {

                out.writeObject(true);
                out.flush();

            }

            else {

                out.writeObject(false);
                out.flush();

            }

        }

        else {

            out.writeObject(false);
            out.flush();

        }

    }

    public static void randomPort() throws IOException, ClassNotFoundException {

        int port = (int) in.readObject();

        Random r = new Random();

        int rand;

        while (true) {

            rand = r.nextInt(ports.size());

            if (ports.get(rand) != port) {

                break;

            }

        }

        out.writeObject(ports.get(rand));
        out.flush();

    }

}
