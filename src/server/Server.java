package server;

import javax.swing.plaf.basic.BasicInternalFrameTitlePane;
import java.io.*;
import java.net.*;
import java.nio.Buffer;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

class Server {

    private static boolean connectionLost = false;
//    private static String[] hasFullAccess;
    private static final ArrayList<String> usersWithFullAccess = new ArrayList<String>();
    private static Boolean loggedIN = false;
    private static String currentUser = "";
    private static String currentAccount = "";
    private static Boolean hasFullAccess = false;
    private static Hashtable<String, String[]> dictAcctUser = new Hashtable<String, String[]>(); // <user, [account, password]>
     private static Set<String> dict = dictAcctUser.keySet();

     public static void initialise() {
        try {
            String path = Server.class.getResource("data.txt").getPath();
            loadData(String.valueOf(path));
        } catch (IOException e) {
            System.out.println(e);
            System.out.println("Cannot find data.txt");
        }
     }

    public static void main(String[] argv) throws Exception
    {
        String clientSentence;
        String serverSentence;
        boolean isActive = true;
        String cmd, arg;
        boolean breakout = false;

        ServerSocket welcomeSocket = new ServerSocket(6789);

        initialise();

        while(true) {
            // Accept connection from client and initialise data streams
            Socket connectionSocket = welcomeSocket.accept();
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            DataOutputStream  outToClient = new DataOutputStream(connectionSocket.getOutputStream());

            if (connectionLost) {
                serverSentence = "-Server Out to Lunch \n";
                outToClient.writeBytes(serverSentence);
            } else {
                serverSentence = "+Server SFTP Service \n";
                outToClient.writeBytes(serverSentence);
                while (isActive) {
                    inFromClient =
                            new BufferedReader(new
                                    InputStreamReader(connectionSocket.getInputStream()));
                    outToClient =
                            new DataOutputStream(connectionSocket.getOutputStream());
                    clientSentence = inFromClient.readLine();
                    if (clientSentence.length() >= 4) {
                        cmd = clientSentence.substring(0, 4).toUpperCase();
                        try {
                            arg = clientSentence.substring(5);
                        } catch (StringIndexOutOfBoundsException e) {
                            arg = null;
                        }
                    } else {
                        cmd = "";
                        arg = "";
                    }
                    switch(cmd) {
                        /* user-id
                        Your userid on the remote system */
                        case "USER":
                            String user = arg;
                            if (arg == null || arg.length() < 1) {
                                // -Invalid user-id, try again
                                serverSentence = "-Invalid user-id, try again \n";
                            } else {
                                for (String aUser : usersWithFullAccess) {
                                    if (user.equals(aUser)) {
                                        loggedIN = true;
                                        currentUser = user;
                                        hasFullAccess = true;
                                        serverSentence = "!" + user + " logged in \n";
                                        outToClient.writeBytes(serverSentence);
                                        break;
                                    }
                                }
                                // user is already logged in
                                if (loggedIN || user.equals(currentUser)) {
                                    serverSentence = "!" + user + " logged in \n";
                                    break;
                                } else { // Check if user is valid
                                    currentUser = user;
                                    loggedIN = false;
                                    hasFullAccess = false;
                                    serverSentence = "+User-id valid, send account and password \n";
                                }
                            }
                            outToClient.writeBytes(serverSentence);
                            // !<user-id> logged in = do not need an account or password/you specified a user-id not needing them
                            // +User-id valid, send account and password
                            break;

                        /* account
                        The account you want to use (usually used for billing) on the remote system */
                        case "ACCT":
                            String account = arg;
                            if (arg == null || arg.length() < 1) {
                                serverSentence = "-Invalid account, try again \n";
                                outToClient.writeBytes(serverSentence);
                            } else if (hasFullAccess) {
                                serverSentence = "!Account valid, logged-in \n";
                                outToClient.writeBytes(serverSentence);
                            } else {
                                if (loggedIN || account.equals(currentAccount)) { // if already logged in
                                    serverSentence = "!Account valid, logged-in \n";
                                }
                                else { // if the account is valid
                                    for (String key: dict) {
                                        // check if the account associated with the user is the same as the currentAccount
                                        if (dictAcctUser.get(key)[0].equals(currentUser)) { // dictAcctUser.get(key)[1].equals(arg)
                                            // check if the user you are on (through iterating) is the same as the currentUser
                                            if (key.equals(arg)) { // key.equals(currentUser)
                                                currentAccount = account;
                                                loggedIN = false;
                                                serverSentence = "+Account valid, send password \n";
                                                outToClient.writeBytes(serverSentence);
                                                breakout = true;

                                            }
                                        }
                                    }
                                    if (breakout) {
                                        breakout = false;
                                        break;
                                    }
                                    // if account is invalid
                                    loggedIN = false;
                                    serverSentence = "-Invalid account, try again \n";
                                }
                                outToClient.writeBytes(serverSentence);
                            }
                            // !Account valid, logged-in = account was ok/not needed. skip password
                            // +Account valid, send password = account ok/not needed. send your password next
                            // -Invalid account, try again
                            break;

                        /* password
                        Your password on the remote system */
                        case "PASS":
                            String password = arg;
                            if (arg == null || arg.length() < 1) {
                                serverSentence = "-Wrong password, try again \n";
                                outToClient.writeBytes(serverSentence);
                            } else if (hasFullAccess || loggedIN) {
                                serverSentence = "!Logged in \n";
                                outToClient.writeBytes(serverSentence);
                            } else {
                                for (String key: dict) {
                                    // check if the account associated with the user is the same as the currentAccount
                                    if (dictAcctUser.get(key)[0].equals(currentUser)) { // dictAcctUser.get(key)[0].equals(arg)
                                        // check if the user you are on (through iterating) is the same as the currentUser
                                        if (key.equals(currentAccount) && dictAcctUser.get(key)[1].equals(password)) { // key.equals(currentUser) && dictAcctUser.get(key)[1].equals(password)
                                            loggedIN = true;
                                            serverSentence = "!Logged in \n";
                                            outToClient.writeBytes(serverSentence);
                                            breakout = true;
                                        }
                                    }
                                }
                                if (breakout) {
                                    breakout = false;
                                    break;
                                }
                                loggedIN = false;
                                serverSentence = "-Wrong password, try again \n";
                                outToClient.writeBytes(serverSentence);
                            }
                            // ! Logged in = password is ok and you can begin file transfers
                            // +Send account = password ok but you haven't specified the account
                            // -Wrong password, try again
                            break;

                        /* {A (ASCII) | B (Binary) | C (Continuous)}
                        The mapping of the stored file to the transmission byte stream is controlled by the type.
                        The default is binary if they type is not specified */
                        case "TYPE":
                            // +Using { Ascii | Binary | Continuous } mode
                            // -Type not valid
                            break;

                        /* {F (standard formatted directory listing) | V (verbose directory listing) } directory listing
                        A null directory-path will return the current connected directory listing */
                        case "LIST":
                            break;

                        /* New-directory
                        * This will change the current working directory on the remote host to the argument passed */
                        case "CDIR":
                            // !Changed working dir to <new-directory>
                            // -Can't connect to directory because: (reason)
                            // +directory ok, send account/password
                            // if the server replies with '+'
                                // ACCT command
                                    // !Changed working dir to <new-directory>
                                    // +account ok, send password
                                    // -invalid account
                                // PASS command
                                    // !Changed working dir to <new-directory>
                                    // +password ok, send account
                                    // -invalid password
                            break;

                        /* file-spec
                        This will delete the file from the remote system */
                        case "KILL":
                            // +<file-spec> deleted
                            // -Not deleted because (reason)
                            break;

                        /* old-file-spec
                        Renames the old-file-spec to be new-file-spec on the remote system */
                        case "NAME":
                            // +File exists
                            // -Can't find <old-file-spec> = NAME command is aborted, don't send TOBE
                            // if you receive a '+'
                                // TOBE new-file-spec
                            // The server replies with:
                                // +<old-file-spec> renamed to <new-file-spec>
                                // -File wasn't renamed because (reason)
                            break;

                        /* Tells the remote system you are done */
                        case "DONE":
                            serverSentence = "+Server closing connection"; // +(the message may be charge/accounting info)
                            outToClient.writeBytes(serverSentence);
                            isActive = false; // then both systems close the connection
                            connectionSocket.close();
                            break;

                        /* file-spec
                        Requests that the remote system send the specified file */
                        case "RETR":
                            // receiving a '-' from the server should abord the RETR command
                            // <number-of-bytes-that-will-be-sent> (as ascii digits)
                            // -File doesn't exist
                            // then reply to the remote system with:
                                // SEND (ok, waiting for file)
                                // STOP (You don't have enough space to store file)
                                    // +ok, RETR aborted
                            break;

                        /* {NEW | OLD | APP} file-spec
                        Tells the remote system to receive the following file and save it under that name */
                        case "STOR":
                            // receiving a '-' should abort the STOR command
                            // NEW = specifies it should create a new generation of the file and not delete the existing one
                                // +File exists, will create new generation of file
                                // -File exists, but system doesn't support generations
                            // OLD = specifies it should write over the existing file, if any, or else create a new file with the specified name
                                // +Will write over old file
                                // +Will create new file
                            // APP = specifies that what you send should be appended to the file on the remote site.
                            // If the file doesn't exist it will be created
                                // +Will append to file
                                // +Will create file
                            // You then send:
                                // SIZE <number-of-bytes-in-file> (as ASCII digits) = exact number of 8-bit bytes you will be sending
                                    // +ok, waiting for file

                            break;
                        default:
                            break;
                    }
                }
            }
//            clientSentence = inFromClient.readLine();
//            serverSentence = clientSentence.toUpperCase() + '\n';
//            outToClient.writeBytes(serverSentence);
        }
    }

    private static void loadData(String path) throws IOException {
         System.out.println("Data loaded successfully! \n");
         FileReader file = new FileReader(String.valueOf(path));
         BufferedReader data = new BufferedReader(file);
         String line;
         String[] element, value;
         while((line = data.readLine()) != null) {
             element = line.split(" ");
             if (element.length == 3) {
                 value = new String[]{element[0], element[2]};
                 dictAcctUser.put(element[1], value);
             } else if (element.length == 1) {
                 usersWithFullAccess.add(element[0]);
             }
         }
         data.close();
         System.out.println(dictAcctUser);
         System.out.println(usersWithFullAccess);
    }
}

