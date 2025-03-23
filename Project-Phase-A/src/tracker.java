import java.lang.reflect.Array;
import java.util.*;
import java.net.*;
import java.io.*;

public class tracker extends Thread implements Serializable {

    ObjectInputStream in;
    ObjectOutputStream out;
    Socket connection;

    public tracker(Socket connection, ObjectInputStream in, ObjectOutputStream out) {
        this.connection = connection;
        this.in = in;
        this.out = out;
    }

    @Override
    public void run() {

        // Socket flag
        boolean flag = true;

        // Keep socket alive!
        while (flag) {

            try {

                // Peer's command
                int command = (int) in.readObject();

                // Server receives command
                switch (command) {

                    // 1. Register
                    case 1: {

                        // Peer's credentials
                        String username = (String) in.readObject();
                        String password = (String) in.readObject();

                        boolean registered = doRegister(username, password);

                        // Tracker sends register status
                        out.writeObject(registered);
                        out.flush();

                        break;

                    } // Register ()

                    // 2. Login
                    case 2: {

                        // Peer's credentials
                        String username = (String) in.readObject();
                        String password = (String) in.readObject();

                        // Set tokenId
                        int tokenId = doLogin(username, password);

                        // Tracker sends tokenId
                        out.writeObject(tokenId);
                        out.flush();

                        // Peer is connecting...
                        if(tokenId > 0) {

                            // Receives ipAddress, port
                            String ipAddress = (String) in.readObject();
                            int port = (int) in.readObject();
                            ArrayList<String> sharedDirectory = (ArrayList<String>) in.readObject();

                            // Inform connectedPeerList
                            connectedPList.add(new connectedPeer(tokenId, ipAddress, port, username));

                            // Inform Directories
                            for (String s : sharedDirectory) {

                                // Inform sharedDirectory
                                if (!getAvailableFiles().contains(s))
                                    availableFiles.add(s);

                                // Finds item
                                if (itemTokenMap.containsKey(s)) {

                                    // Adds token to the list
                                    ArrayList<Integer> tokenList = itemTokenMap.get(s);
                                    tokenList.add(tokenId);

                                    // Replaces value tokenList
                                    itemTokenMap.replace(s,tokenList);

                                } else {

                                    // Initialize String s to list
                                    ArrayList<Integer> tokenList = new ArrayList<>();
                                    tokenList.add(tokenId);
                                    itemTokenMap.put(s, tokenList);

                                }

                            }

                        }

                        break;

                    } // Login ()

                    // 3. Logout
                    case 3: {

                        // Token entry
                        int tokenId = (int) in.readObject();

                        // Is peer offline
                        boolean offline = doLogout(tokenId);

                        out.writeObject(offline);
                        out.flush();

                        break;

                    } // Logout ()

                    // 4. Reply List
                    case 4: {

                        // Send peer a list of available-file names
                        out.writeObject(getAvailableFiles());
                        out.flush();

                        break;

                    } // Reply List

                    // 5. Reply Details
                    case 5: {

                        // File name received
                        String fileName = (String) in.readObject();
                        int myToken = (int) in.readObject();

                        // List with peer information
                        ArrayList<connectedPeer> detailsList = new ArrayList<>();

                        // availableFile iterator
                        Iterator it = itemTokenMap.entrySet().iterator();

                        // Traversing iterator
                        while (it.hasNext()) {

                            // Map element
                            Map.Entry element = (Map.Entry) it.next();

                            // Searching for the correct fileName...
                            if (element.getKey().equals(fileName)) {

                                // Found tokenList
                                ArrayList<Integer> tokenList = ((ArrayList<Integer>) element.getValue());

                                for (int tok : tokenList) {

                                    // Searching for peer
                                    for (connectedPeer cp : getConnectedPList()) {

                                        // Matching token... except self
                                        if (cp.getTokenId() == tok && tok !=myToken) {

                                            // Add to detailsList
                                            detailsList.add(cp);
                                        }

                                    }

                                }


                            }

                        }

                        out.writeObject(detailsList);
                        out.flush();

                        break;

                    } // Reply Details ()

                    // 6. Check Active
                    case 6: {

                        // Receive peer's username
                        String username = (String) in.readObject();

                        // Is peer online
                        boolean online = false;

                        // Searching for peer...
                        for (connectedPeer cp : getConnectedPList())
                            if (cp.getUsername().equals(username)) {
                                online = true;
                                break;
                            }

                        // Return online status
                        out.writeObject(online);
                        out.flush();

                        break;

                    } // Check Active ()

                    // 7. Notify
                    case 7: {

                        String fileName = (String) in.readObject();
                        String peerName = (String) in.readObject();
                        int tokenId = (int) in.readObject();

                        String bestPeer = (String) in.readObject();
                        int val = (int) in.readObject();

                        // Update shared_directory
                        Iterator it = itemTokenMap.entrySet().iterator();

                        // Traversing iterator
                        while (it.hasNext()) {

                            // Map element
                            Map.Entry element = (Map.Entry) it.next();

                            // Searching for the correct fileName...
                            if (element.getKey().equals(fileName)) {

                                ((ArrayList<Integer>) element.getValue()).add(tokenId);

                            }

                        }

                        // Update count values
                        for (connectedPeer cp : connectedPList) {

                            if (cp.getUsername().equals(peerName)) {

                                if (val > 0) cp.setCountDownloads(cp.getCountDownloads()+1);
                                else cp.setCountFailures(cp.getCountFailures()+1);

                            }
                            else System.out.println("\nError! Peer not found!");

                        }

                        break;

                    } // Notify ()

                    // 8. Exit
                    default: {

                        // Close socket!
                        in.close();
                        out.close();
                        connection.close();

                        // Socket closed!
                        System.out.println("Connection closed!");
                        flag = false;

                        break;

                    } // Exit ()

                } // Command

            } catch (Exception e){ e.printStackTrace(); }

        } // While

    } // run ()

    // Variables ()
    public static ArrayList<registeredPeer> registeredPList = new ArrayList<>();
    public static ArrayList<connectedPeer> connectedPList = new ArrayList<>();
    public static ArrayList<String> availableFiles = new ArrayList<>();
    public static HashMap<String, ArrayList<Integer>> itemTokenMap = new HashMap<>();

    // Methods ()
    public tracker() {}
    public static boolean doRegister (String username, String password) {

        // Is peer registered?
        for (registeredPeer rp : getRegisteredPList()) {

            // First registration
            if (rp.getUsername().equals(username)) {

                // Peer is already registered!
                return false;
            }

        }

        // Peer was not found!
        registeredPList.add(new registeredPeer(username, password));

        return true;
    }
    public static int doLogin (String username, String password) {

        // Peer is not online
        int tokenId = 0;

        // Peer is already online!
        for (connectedPeer cp : getConnectedPList()) {

            if (cp.getUsername().equals(username))
            return -1;
        }

        // Searching for Peer's credentials
        for (registeredPeer rp : getRegisteredPList()) {

            // Peer was found
            if (rp.getUsername().equals(username) && rp.getPassword().equals(password)) {

                // Give token
                Random random = new Random();
                tokenId = random.nextInt(8999) + 1000;

                break;
            }

        }

        return tokenId;
    }
    public static boolean doLogout (int tokenId) {

        for (connectedPeer cp : getConnectedPList()) {

            if (cp.getTokenId() == tokenId) {

                // Remove peer from online list
                getConnectedPList().remove(cp);
                return true;
            }

        }

        return false;

    } // logout ()

    // Getters ()
    public static ArrayList<registeredPeer> getRegisteredPList() { return registeredPList; }
    public static ArrayList<connectedPeer> getConnectedPList() { return connectedPList; }
    public static ArrayList<String> getAvailableFiles() { return availableFiles; }
    public static HashMap<String, ArrayList<Integer>> getItemTokenMap() { return itemTokenMap; }

}