/***    ΑΜΓΚΑΡ ΒΙΚΤΩΡ 3180007
 *      ΚΟΝΤΟΔΗΜΑΣ ΧΡΗΣΤΟΣ-ΔΗΜΗΤΡΙΟΣ 3180083
 *      1Η ΦΑΣΗ PROJECT
 */

import java.io.*;
import java.net.*;

public class server {

    public static void main(String[] args) throws IOException {

        // Server created!
        ServerSocket server = new ServerSocket(4040);
        System.out.println("Waiting for a connection...");

        // Number of peer created
        int n = 0;

        while (true) {

            Socket connection = null;

            try {

                connection = server.accept();
                System.out.println("A client has connected");
                n++;

                // Output-Input Streams
                ObjectOutputStream out = new ObjectOutputStream(connection.getOutputStream());
                ObjectInputStream in = new ObjectInputStream(connection.getInputStream());

                out.writeObject(n);
                out.flush();

                Thread t = new tracker(connection, in, out);
                t.start();

            } catch (Exception e){
                server.close();
                e.printStackTrace();
            }

        }

    }

}