import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.net.*;
import java.io.*;

public class peer implements Serializable {

    public static void main(String[] args) throws IOException, ClassNotFoundException {

        // Peer created!
        peer p = new peer(new Random().nextInt(4999) + 5000);

        // Peer connected!
        Socket connection = new Socket("127.0.0.1", 4040);

        // Output-Input Streams
        ObjectOutputStream out = new ObjectOutputStream(connection.getOutputStream());
        ObjectInputStream in = new ObjectInputStream(connection.getInputStream());

        // Peer's server created!
        ServerSocket server = new ServerSocket(p.getPort());
        server.setSoTimeout(10000);
        boolean isConnected = false;

        // Number of peer created
        p.setN((int) in.readObject());

        // Scanner
        Scanner sc = new Scanner(System.in);
        Scanner input = new Scanner(System.in);
        Scanner sc2 = new Scanner(System.in);

        // Socket flag
        boolean flag = true;

        // Main Loop
        while (flag) {

            // Show Menu
            System.out.print("\nSelect Command:" + "\n1. Register\n" + "2. Login\n" + "3. Exit\n" + "> ");

            int choice = sc.nextInt();

            // 1. Register
            if (choice == 1) {

                p.register(out, in, p, p.getN());

            } // Register ()

            // 2. Login
            else if (choice == 2) {

                boolean connected = p.login(out, in, p);
                p.inform(out, p);

                // Peer is online
                if (connected) {

                    while (true) {

                        // Show Menu
                        System.out.print("\nSelect Command:" + "\n1. List\n" + "2. Details\n" + "3. Logout\n" + "4. Listen\n" + "5. Simple Download\n" + "6. Exit\n" + "> ");

                        int choiceLog = sc.nextInt();

                        // 1. List
                        if (choiceLog == 1) {

                            p.list(out, in);

                        } // List ()

                        // 2. Details
                        else if (choiceLog == 2) {

                            // File name input
                            System.out.print("\nEnter file name: ");
                            String fileName = input.nextLine();

                            ArrayList<connectedPeer> detailsList = p.details(out, in, p, fileName);

                            // File not found!
                            if (detailsList.isEmpty()) { System.out.println("Error! File \"" + fileName + "\" is not found!"); }

                            // List is not empty!
                            else {

                                // Print peer information
                                for (connectedPeer cp : detailsList) System.out.println("\n" + cp);

                                System.out.print("\nDo you want to start download(y/n)? ");
                                String ans = sc2.next();

                                if (!(ans.equals("y") || ans.equals("Y"))) {
                                    System.out.println("Download interrupted!");
                                } else {

                                    boolean done = false;
                                    for (int i= detailsList.size();i>0; i--) {

                                        // Other peers must confirm checkActive()
                                        System.out.println("Searching for the best peer...");
                                        String bestPeer = null;

                                        try {

                                            bestPeer = p.simpleDownload(detailsList, p, fileName);
                                            System.out.println("Download Completed!");
                                            done = true;
                                            break;

                                        } catch (Exception e) {

                                            System.out.println("Download failed!");
                                            System.out.println("Restarting download...");


                                        }

                                        p.notifyTracker(done, fileName, p.getUsername(), p.getToken_id(), bestPeer, out, in);

                                    }

                                    if (!done) System.out.println("Error! No Peer was successful!");

                                }

                            }

                        } // Details ()

                        // 3. Logout
                        else if (choiceLog == 3) {

                            p.logout(out, in, p);

                            break;

                        } // Logout ()

                        // 4. Listen
                        else if (choiceLog == 4) {

                            p2pConnection = server.accept();

                            // Output-Input Streams
                            write = new ObjectOutputStream(p2pConnection.getOutputStream());
                            read = new ObjectInputStream(p2pConnection.getInputStream());

                            // Is peer online
                            boolean online = (p.getToken_id() > 0);

                            write.writeObject(online);
                            write.flush();

                        }

                        // 5. Simple Download
                        else if (choiceLog == 5) {

                            try {

                                String fileName = (String) read.readObject();
                                byte[] content = Files.readAllBytes(Paths.get("src/directory/peer" + p.getN() + "/shared_directory/" + fileName));
                                write.writeObject(content);
                                p.setCountDownloads(p.getCountDownloads()+1);

                            } catch (Exception e) {

                                System.out.println("Error! File not downloaded!");
                                p.setCountFailures(p.getCountFailures()+1);

                            }

                        } // Simple Download ()

                        // 6. Exit
                        else {
                            System.out.println("Error! Unexpected key!");
                            flag = false;
                            break;
                        } // Exit ()

                    }

                }

            } // Login ()

            // 3. Exit
            else {

                // command (7)
                out.writeObject(7);
                out.flush();

                flag = false;

            } // Exit ()

        } // While

        // EXIT PROGRAM
        in.close();
        out.close();
        sc.close();
        input.close();
        sc2.close();
        connection.close();
    }

    public peer(int port) {
        this.ipAddress = "127.0.0.1";
        this.port = port;
    }

    private ArrayList<String> sharedDirectory = new ArrayList<>();
    private String username;
    private String password;
    private int token_id;
    private int countDownloads;
    private int countFailures;
    private String ipAddress;
    private int port;
    Scanner input = new Scanner(System.in);
    private static Socket p2pConnection;
    private static ObjectOutputStream write;
    private static ObjectInputStream read;
    private int n;

    public void register (ObjectOutputStream out, ObjectInputStream in, peer p, int n) throws IOException, ClassNotFoundException {

        // command (1)
        out.writeObject(1);
        out.flush();

        boolean registered = false;

        while (!registered) {

            //*-*-*-* CHANGE TO NEXT LINE *-*-*-*

            // Enter username
            System.out.print("\nEnter username: ");
            String username = input.next();

            // Enter password
            System.out.print("Enter password: ");
            String password = input.next();

            // Send credentials
            out.writeObject(username);
            out.writeObject(password);
            out.flush();

            // Is registration completed
            registered = (boolean) in.readObject();

            if (registered) {

                p.setUsername(username);
                p.setPassword(password);
                p.setSharedDirectory(n);

                // Peer is now registered!
                System.out.println("Registered Successfully!");

            } else {

                // Peer is already registered!
                System.out.println("Error! " + username + " already exists!");
                registered = true;
            }

        }

    } // Register ()

    public boolean login (ObjectOutputStream out, ObjectInputStream in, peer p) throws IOException, ClassNotFoundException {

        // command (2)
        out.writeObject(2);
        out.flush();

        // Enter username
        System.out.print("\nEnter username: ");
        String username = input.next();

        // Enter password
        System.out.print("Enter password: ");
        String password = input.next();

        // Send credentials
        out.writeObject(username);
        out.writeObject(password);
        out.flush();

        // Receive tokenId
        int tokenId = (int) in.readObject();

        // Peer tries to log-in while connected!
        if (tokenId == -1) {
            System.out.println("Peer \"" + username + "\" is already connected!");
        }

        // Peer username or password don't match!
        else if (tokenId == 0) {
            System.out.println("Error! username or password is incorrect!");
        }

        // Peer found - Token was given!
        else {
            System.out.println("Welcome back \"" + username + "\"!");
            p.setToken_id(tokenId);
            return true;
        }

        return false;

    } // Login ()

    public void inform (ObjectOutputStream out, peer p) throws IOException {

        // Peer is connecting...
        if (p.getToken_id() > 0) {

            out.writeObject(p.getIpAddress());
            out.writeObject(p.getPort());
            out.writeObject(p.getSharedDirectory());
            out.flush();

        }

    }  // Inform ()

    public void logout (ObjectOutputStream out, ObjectInputStream in, peer p) throws IOException, ClassNotFoundException {

        // command (3)
        out.writeObject(3);
        out.flush();

        // Token validation
        out.writeObject(p.getToken_id());
        out.flush();

        boolean offline = (boolean) in.readObject();

        // Print result
        if (offline) System.out.println("\nPeer \"" + p.getUsername() + "\" has disconnected!");
        else System.out.println("\nPeer \"" + p.getUsername() + "\" is not connected!");

    }

    public void list (ObjectOutputStream out, ObjectInputStream in) throws IOException, ClassNotFoundException {

        // command (4)
        out.writeObject(4);
        out.flush();

        // Get list of available-file names
        ArrayList<String> availableFiles = (ArrayList<String>) in.readObject();

        // Print list
        System.out.println("\nList of available files:");
        for (String s : availableFiles) System.out.println("-" + s);

    }

    public ArrayList<connectedPeer> details (ObjectOutputStream out, ObjectInputStream in, peer p, String fileName) throws IOException, ClassNotFoundException {

        int myToken = p.getToken_id();

        // command (5)
        out.writeObject(5);
        out.flush();

        // File name sent!
        out.writeObject(fileName);
        out.writeObject(myToken);
        out.flush();

        // Receive peer info list
        ArrayList<connectedPeer> detailsList = (ArrayList<connectedPeer>) in.readObject();

        return detailsList;

    }

    public boolean checkActive (ObjectInputStream in) throws IOException, ClassNotFoundException {

        boolean online = (boolean) in.readObject();

        return online;
    }

    public String simpleDownload (ArrayList<connectedPeer> bestPList, peer p, String fileName) throws IOException, ClassNotFoundException {

        HashMap<connectedPeer, Double> bestPeer = new HashMap<>();
        HashMap<connectedPeer, connectionInfo> socketQueue = new HashMap<>();

        // Start timer
        double start = System.currentTimeMillis();
        for (connectedPeer cp : bestPList) {

            // Socket created!
            Socket connection = new Socket(cp.getIpAddress(), cp.getPort());

            // Input - Output Streams
            ObjectInputStream in = new ObjectInputStream(connection.getInputStream());
            ObjectOutputStream out = new ObjectOutputStream(connection.getOutputStream());

            // Get connection info
            connectionInfo ci = new connectionInfo(cp, connection, out, in);
            socketQueue.put(cp, ci);

            // Check Active result
            boolean online = p.checkActive(in);
            if (online) System.out.println(cp.getUsername() + " is online!");
            else System.out.println(cp.getUsername() + " is offline!");

            // End timer
            double end = System.currentTimeMillis();
            double responseTime = (end - start) / 1000;

            double factor = responseTime * Math.pow(0.9, cp.getCountDownloads()) * Math.pow(1.2, cp.getCountFailures());

            bestPeer.put(cp, factor);
        }

        // bestPeer iterator
        Iterator it = bestPeer.entrySet().iterator();

        Map.Entry elm = (Map.Entry) it.next();
        connectedPeer bestCp = (connectedPeer) elm.getKey();
        double min = (double) elm.getValue();

        // Traversing iterator
        while (it.hasNext()) {

            // Map element
            Map.Entry element = (Map.Entry) it.next();

            if ((double) element.getValue() < min) {

                min = (double) element.getValue();
                bestCp = (connectedPeer) element.getKey();
            }

        }

        System.out.println("Best peer = " + bestCp.getUsername() + "\nScore = " + min);
        bestCp.setCountDownloads(bestCp.getCountDownloads()+1);

        p.transfer(bestCp, socketQueue, fileName, p);

        return bestCp.getUsername();
    }

    public void transfer (connectedPeer bestCp, HashMap<connectedPeer, connectionInfo> socketQueue, String fileName, peer p) throws IOException, ClassNotFoundException {

        connectionInfo ci = null;

        System.out.println("Downloading...");

        // Find socket
        Iterator it = socketQueue.entrySet().iterator();

        // Traversing iterator
        while (it.hasNext()) {

            // Map element
            Map.Entry element = (Map.Entry) it.next();

            if (((connectedPeer) element.getKey()).getUsername().equals(bestCp.getUsername())) {

                ci = ((connectionInfo) element.getValue());
            }

        }

        ci.getOut().writeObject(fileName);

        byte[] content = (byte[])  ci.getIn().readObject();
        Files.write(Paths.get("src/directory/peer" + p.getN() + "/shared_directory/" + fileName), content);

        ci.getOut().close();
        ci.getIn().close();
        ci.getConnection().close();
    }

    public void notifyTracker(boolean done, String fileName, String peerName, int tokenId, String bestPeer, ObjectOutputStream out, ObjectInputStream in) throws IOException {

        out.writeObject(7);
        out.flush();

        int val = 0;

        if (done) val = 1;
        else val = -1;

        out.writeObject(fileName);
        out.writeObject(peerName);
        out.writeObject(tokenId);
        out.writeObject(bestPeer);
        out.writeObject(val);
        out.flush();
    }

    // Getters + Setters
    public String getUsername () { return username; }
    public void setUsername (String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public int getToken_id() { return token_id; }
    public void setToken_id(int token_id) { this.token_id = token_id; }

    public int getCountDownloads() { return countDownloads; }
    public void setCountDownloads(int countDownloads) { this.countDownloads = countDownloads; }

    public int getCountFailures() { return countFailures; }
    void setCountFailures(int countFailures) { this.countFailures = countFailures; }

    public String getIpAddress() { return ipAddress; }
    public void setIpAddress(String ipAddress) { this.ipAddress = ipAddress; }

    public int getPort() { return port; }
    public void setPort(int port) { this.port = port; }

    public ArrayList<String> getSharedDirectory() { return sharedDirectory; }
    public void setSharedDirectory(int n) throws IOException {

        File folder = new File("src/directory/peer"+n + "/shared_directory" );
        File[] listOfFiles = folder.listFiles();

        for (File f : listOfFiles)
            getSharedDirectory().add(f.getName());

    }

    public int getN() { return n; }

    public void setN(int n) { this.n = n; }
}