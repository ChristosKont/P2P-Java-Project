import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class peer {

    private static ServerSocket server;
    private static Socket connection;
    private static ObjectOutputStream out;
    private static ObjectInputStream in;

    private static String username;
    private static String password;
    private static int port;

    private static boolean online;

    private static String[] pieces;

    private static List<Integer> portsList;

    public static void main(String[] args) throws IOException, ClassNotFoundException {

        pieces = new String[] {"", "", "", "", "", "", "", "", "", ""};
        portsList = new ArrayList<>();

        online = false;

        // Scanner Created!
        Scanner sc = new Scanner(System.in);
        int choice;

        while (true) {

            System.out.print("\nPlease select a command:" + "\n1. Register" + "\n2. Login" + "\n3. Exit" + "\n> " );
            choice = sc.nextInt();

            if (choice == 1) {
                register();
            }

            if (choice == 2) {
                login();
            }

            if (choice == 3) {
                break;
            }

            if (online) {

                // Server Created!
                server = new ServerSocket(port);

                while (true) {

                    System.out.print("\nPlease select a command:" + "\n1. Download" + "\n2. Listen" + "\n3. Exit" + "\n> " );
                    choice = sc.nextInt();

                    // 1. Download
                    if (choice == 1) {

                        // Downloading...
                        download();

                        // Close active listen sessions!
                        closePorts();

                        // All files are found!
                        synthesize();

                    }

                    // 2. Listen
                    if (choice == 2) {

                        // Listening...
                        listen();

                    }

                    // 3. Exit
                    if (choice == 3) {
                        break;
                    }

                    // 4. Print
                    if (choice == 4) {

                        // Printing...
                        print();

                    }

                    // 5. Upload
                    if (choice == 5) {

                        // Uploading...
                        upload();

                    }

                }

            }

        }

    }

    public static void connect(String ip, int p) throws IOException {

        if (p != 5050) System.out.println("\n[Peer]: I'm going to connect to port " + p + "...");

        // Connecting...
        connection = new Socket(ip, p);

        // Connected!
        if (p != 5050) System.out.println("[Peer]: Connected successfully!");

        // Connection Established!
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

    public static void acceptConnection() throws IOException {

        // Accepting connection...
        System.out.println("\n[Peer]: Awaiting Connection...");
        connection = server.accept();

        System.out.println("[Peer]: Connection Established!");

        // Objects created!
        out = new ObjectOutputStream(connection.getOutputStream());
        in = new ObjectInputStream(connection.getInputStream());

    }

    // Register
    public static void register () throws IOException, ClassNotFoundException {

        boolean registered = false;

        while (!registered) {

            Scanner input = new Scanner(System.in);

            // Enter username
            System.out.print("\nEnter username: ");
            username = input.nextLine();

            connect("127.0.0.1", 5050);

            // command (1)
            out.writeObject(1);
            out.flush();

            // Send credentials
            out.writeObject(username);
            out.flush();

            // Is registration completed
            registered = (boolean) in.readObject();

            if (registered) {

                // Enter port
                System.out.print("Enter password: ");
                password = input.nextLine();

                // Enter port
                System.out.print("Enter port: ");
                port = input.nextInt();

                // Update to tracker
                out.writeObject(password);
                out.writeObject(port);
                out.flush();

                makeDirectory();

                // Peer is now registered!
                System.out.println("Registered Successfully!");
                online = true;

            } else {

                // Peer is already registered!
                System.out.println("Error! " + username + " already exists!");

            }

        }

    } // Register ()

    // Login
    public static void login () throws IOException, ClassNotFoundException {

        connect("127.0.0.1", 5050);

        // command (2)
        out.writeObject(2);
        out.flush();

        Scanner input = new Scanner(System.in);

        // Enter username
        System.out.print("\nEnter username: ");
        String name = input.next();

        // Enter password
        System.out.print("Enter password: ");
        String pass = input.next();

        // Send credentials
        out.writeObject(name);
        out.writeObject(pass);
        out.flush();

        online = (boolean) in.readObject();

        // Login successful!
        if (online) {
            System.out.println("Welcome back \"" + name + "\"!");
        }

        else {
            System.out.println("Error! Please try again!");
        }

    } // Login ()

    // Download
    public static void download() throws IOException, ClassNotFoundException {

        for (int i=0; i<10; i++) {

            // If file is empty...
            if (pieces[i].equals("")) {

                while (true) {

                    int randPort = randomPort();

                    // Add to active portsList
                    if (!portsList.contains(randPort)) {

                        portsList.add(randPort);

                    }

                    // Connect to random peer
                    connect("127.0.0.1", randPort);

                    System.out.println("\n[Peer]: I'm looking for item " + (i+1) + "!");

                    // I am looking for a piece!
                    out.writeObject(false);
                    out.flush();

                    // Send him missing file
                    out.writeObject(i);
                    out.flush();

                    // Get answer if he has item
                    boolean found = (boolean) in.readObject();

                    // Item found
                    if (found) {

                        System.out.println("[Peer]: Item was found!");

                        // Get piece
                        pieces[i] = (String) in.readObject();

                        // Write piece
                        writePiece(i, pieces[i]);

                        break;

                    }

                    System.out.println("[Peer]: Item was not found!");

                }

            }

        }

    }

    public static void closePorts() throws IOException {

        for (int p : portsList) {

            if (p != port) {

                connect("127.0.0.1", p);

                out.writeObject(true);
                out.flush();

            }

        }

    }

    public static void synthesize() throws IOException {

        // Creating the whole file...
        FileWriter author = new FileWriter("src\\" + username + "\\Downloaded\\test\\test.txt");

        for (String s : pieces) {

            author.write(s + "\n");

        }

        author.close();

    }

    // Listen
    public static void listen() throws IOException, ClassNotFoundException {

        while (true) {

            // Accepting connection...
            acceptConnection();

            boolean end = (boolean) in.readObject();

            if (!end) {

                // Get missing item
                int i = (int) in.readObject();

                // Do i have the item?
                boolean found = !pieces[i].equals("");

                // Send him the answer
                out.writeObject(found);
                out.flush();

                // Send him the file
                if (found) {

                    out.writeObject(pieces[i]);
                    out.flush();

                }

            }

            if (end) {

                break;

            }

        }

    }

    public static void makeDirectory() {

        File file;

        // Peer's directory
        file = new File("src\\" + username);
        file.mkdir();

        // Downloaded directory
        file = new File("src\\" + username + "\\Downloaded");
        file.mkdir();

        // Test directory
        file = new File("src\\" + username + "\\Downloaded\\test");
        file.mkdir();

    }

    public static void writePiece(int i, String piece) throws IOException {

        // Creating and writing the new file!
        FileWriter author = new FileWriter("src\\" + username + "\\Downloaded\\test\\test-" + (i+1) + ".txt");
        author.write(piece);

        author.close();

    }

    public static int randomPort() throws IOException, ClassNotFoundException {

        // Connect to tracker
        connect("127.0.0.1", 5050);

        // Random : 2
        out.writeObject(3);
        out.flush();

        // Cannot connect to himself!
        out.writeObject(port);
        out.flush();

        // Get random port
        int p = (int) in.readObject();

        return p;

    }

    public static void upload() throws IOException {

        // Creating file...
        BufferedReader reader = new BufferedReader(new FileReader("src\\test.txt"));

        for (int i=0; i<10; i++) {

            // Creating each file
            FileWriter author = new FileWriter("src\\" + username + "\\Downloaded\\test\\test-" + (i+1) + ".txt");

            // Reading content...
            String line = reader.readLine();

            // Adding it to the table
            pieces[i] = line;

            // Writing to file...
            author.write(line);
            author.close();

        }

        // Closing the reader...
        reader.close();

    }

    public static void print() {

        // For each file...
        for (int i=0; i<10; i++) {

            // File print
            System.out.println("\n" + (i+1) + ") " + pieces[i]);

        }

    }

}