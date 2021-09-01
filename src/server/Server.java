package server;

import java.io.*;
import java.net.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

public class Server {

    private static final ArrayList<String> usersWithFullAccess = new ArrayList<String>();
    private static final ArrayList<String> usersWithSomeAccess = new ArrayList<String>();
    private static Boolean loggedIN = false;
    private static String currentUser = "";
    private static String currentAccount = "";
    private static Boolean hasFullAccess = false;
    private static final Hashtable<String, String[]> dictAcctUser = new Hashtable<String, String[]>(); // <account, [user, password]>
    private static final Set<String> dict = dictAcctUser.keySet();
    private static final Hashtable<String, String> dictUser = new Hashtable<String, String>(); // user, password
    private static final Set<String> dictUs = dictUser.keySet();
    private static String currentDirectory = System.getProperty("user.dir");
    private static String nextDirectory = "";
    private static Boolean confirmedForCDIR = false;
    private static Boolean readyToSend = false;
    private static int sizeToSend = 0;
    private static String fileToSend = "";
    private static String oldFileSpec = "";
    private static Boolean tobeNext = false;
    private static String storageType = "";
    private static String fileName = "";
    private static final Boolean supportsGenerations = true;

     public static void initialise() {
        try {
            String path = Objects.requireNonNull(Server.class.getResource("data.txt")).getPath();
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

            boolean connectionLost = false;
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
                                for (String aUser: usersWithSomeAccess) {
                                    if (user.equals(aUser)) {
                                        loggedIN = false;
                                        currentUser = user;
                                        hasFullAccess = false;
                                        serverSentence = "!User-id valid, send password \n";
                                        outToClient.writeBytes(serverSentence);
                                        break;
                                    }
                                }
                                for (String key: dict) { // Check if user is valid
                                    if (dictAcctUser.get(key)[1].equals(user)) {
                                        loggedIN = false;
                                        currentUser = user;
                                        currentAccount = "";
                                        hasFullAccess = false;
                                        serverSentence = "+User-id valid, send account and password \n";
                                        outToClient.writeBytes(serverSentence);
                                        break;
                                    }
                                }
                                // user is already logged in
                                if (loggedIN || user.equals(currentUser)) {
                                    serverSentence = "!" + user + " logged in \n";
                                    break;
                                } else {
                                    loggedIN = false;
                                    hasFullAccess = false;
                                    serverSentence = "-Invalid user-id, try again \n";
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
                                if (loggedIN) { // if already logged in
                                    serverSentence = "!Account valid, logged-in \n";
                                }
                                else { // if the account is valid
                                    for (String key: dict) {
                                        // check if the user you are on (through iterating) is the same as the currentUser
                                        if (dictAcctUser.get(key)[1].equals(currentUser)) {
                                            // check if the account associated with the user is the same as the currentAccount
                                            if (key.equals(arg)) {
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
                                    // check if the user you are on (through iterating) is the same as the currentUser
                                    if (dictAcctUser.get(key)[1].equals(currentUser)) {
                                        // check if the account associated with the user is the same as the currentAccount
                                        if (key.equals(currentAccount) && dictAcctUser.get(key)[0].equals(password)) {
                                            loggedIN = true;
                                            if (confirmedForCDIR) {
                                                serverSentence = "!Logged in \t !Changed working dir to " + nextDirectory +"\n";
                                                currentDirectory = nextDirectory;
                                                nextDirectory = "";
                                                confirmedForCDIR = false;
                                            } else {
                                                serverSentence = "!Logged in \n";
                                            }
                                            outToClient.writeBytes(serverSentence);
                                            breakout = true;
                                        }
                                    }
                                }
                                for (String key: dictUs) {
                                    if (key.equals(currentUser)) {
                                        loggedIN = true;
                                        serverSentence = "!Logged in \n";
                                        outToClient.writeBytes(serverSentence);
                                        breakout = true;
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
                            if (arg == null || arg.length() < 1) {
                                serverSentence = "-Type not specified. Try again \n";
                            } else {
                                if (loggedIN) {
                                    switch(arg){
                                        case "A":
                                            serverSentence = "+Using Ascii mode \n";
                                            break;
                                        case "C":
                                            serverSentence = "+Using Continuous mode \n";
                                            break;
                                        default: // B - mentioned default is binary if the type is not specified?????
                                            serverSentence = "+Using Binary mode \n";
                                            break;
                                    }
                                } else {
                                    serverSentence = "-Not logged in. Please log in \n";
                                }
                            }
                            outToClient.writeBytes(serverSentence );
                            // +Using { Ascii | Binary | Continuous } mode
                            // -Type not valid
                            break;

                        /* {F (standard formatted directory listing) | V (verbose directory listing) } directory listing
                        A null directory-path will return the current connected directory listing */
                        case "LIST":
                            if (arg == null || arg.length() < 1) {
                                serverSentence = "Format is not specified. Try again \n";
                                outToClient.writeBytes(serverSentence);
                            } else {
                                if (loggedIN) {
                                    String[] properties = arg.split(" ");
                                    String format = properties[0].toUpperCase();
                                    Path tempPath;
                                    try {
                                        tempPath = Paths.get(properties[1]);
                                        File file = new File(tempPath.toString());
                                    } catch (Exception e) {
                                        serverSentence = "File does not exist " + e + ", using current directory instead\n";
//                                        outToClient.writeBytes(serverSentence);
                                        tempPath = Paths.get(currentDirectory);
                                    }
                                    File file = new File(tempPath.toString());
                                    String[] paths = file.list();

                                    serverSentence = "+" + tempPath + "\t"; // "\n"
                                    System.out.println(Arrays.toString(paths));
                                    if (paths == null) {
                                        serverSentence = serverSentence + "+Empty directory \n";
                                        outToClient.writeBytes(serverSentence);
                                    } else {
                                        for (String path: paths) {
                                            if (format.equals("F")) {
                                                // formatted with just the file names
                                                serverSentence = serverSentence + path + ", \t"; // "\n"
//                                                outToClient.writeBytes(serverSentence);
                                            } else if (format.equals("V")) {
                                                // formatted with details such as the file name, the file size and the date it was last modified
                                                File tempFile = new File(tempPath + System.getProperty("file.separator") + path);
                                                long fileSize = (tempFile.length());
                                                Date fileDate = new Date(tempFile.lastModified());
                                                serverSentence = serverSentence + path + "\t" + fileSize + "Bytes\t" + fileDate + ", \t"; // "\n"
//                                                outToClient.writeBytes(serverSentence);
                                            } else {
                                                serverSentence = "-Invalid format \n";
                                                outToClient.writeBytes(serverSentence);
                                            }
                                        }
//                                        System.out.print(serverSentence);
                                    }
//                                    String[] toOutput = serverSentence.split("\t");
//                                    System.out.println("up to here");
//                                    for (String line: toOutput) {
//                                        System.out.println(line);
//                                        outToClient.writeBytes(line + "\n");
//                                    }
                                    outToClient.writeBytes(serverSentence + "\n");
                                } else {
                                    serverSentence = "-Not Logged in. Please log in \n";
                                    outToClient.writeBytes(serverSentence);
                                }
                            }
                            break;

                        /* New-directory
                        * This will change the current working directory on the remote host to the argument passed */
                        case "CDIR":
                            if (arg == null || arg.length() < 1) {
                                serverSentence = "-Directory is not specified. Try again \n";
                                outToClient.writeBytes(serverSentence);
                            } else {
                                if (loggedIN) {
                                    File newDirectory = new File(arg);
                                    if (newDirectory.exists() && hasFullAccess) {
                                         currentDirectory = arg;
                                         nextDirectory = "";
                                         serverSentence = "!Changed working dir to " + arg + "\n";
                                         outToClient.writeBytes(serverSentence);
                                    } else if (newDirectory.exists() && !hasFullAccess) {
                                         serverSentence = "+directory ok, send account/password \n";
                                         outToClient.writeBytes(serverSentence);
                                         hasFullAccess = false;
                                         nextDirectory = arg;
                                         confirmedForCDIR = true;
                                         currentAccount = "";
                                         loggedIN = false;
                                    } else {
                                        serverSentence = "-Can't connect to directory because what you provided does not exist \n";
                                        outToClient.writeBytes(serverSentence);
                                    }
                                } else {
                                    serverSentence = "-Not Logged in. Please log in \n";
                                    outToClient.writeBytes(serverSentence);
                                }
                            }
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
                            Path path = null;
                            if (arg == null || arg.length() < 1) {
                                serverSentence = "-Can't kill because the filename has not been specified. Try again \n";
                            } else {
                                if (loggedIN) {
                                    try {
                                        path = Paths.get(currentDirectory, arg);
                                        Files.delete(path);
                                        serverSentence = "+" + arg + " deleted";
                                    } catch (Exception e) {
                                        serverSentence = "-Not deleted because " + e;
                                    }
                                } else {
                                    serverSentence = "-Not Logged in. Please log in \n";
                                }
                            }
                            outToClient.writeBytes(serverSentence);
                            // +<file-spec> deleted
                            // -Not deleted because (reason)
                            break;

                        /* Checks if a file exists or not */
                        case "NAME": // arg = file
                            if (arg == null || arg.length() < 1) {
                                serverSentence = "-File name is not specified. Try again \n";
                                outToClient.writeBytes(serverSentence);
                            } else {
                                if (loggedIN) {
                                    File tempDirectory = new File(currentDirectory + System.getProperty("file.separator") + arg);
                                    if (tempDirectory.exists()) {
                                        serverSentence = "+File exists \n";
                                        oldFileSpec = arg;
                                        tobeNext = true;
                                    } else {
                                        serverSentence = "-Can't find " + arg + " NAME command is aborted, don't send TOBE \n";
                                    }
                                    outToClient.writeBytes(serverSentence);
                                    System.out.println(currentDirectory);
                                } else {
                                    serverSentence = "-Not Logged in. Please log in \n";
                                    outToClient.writeBytes(serverSentence);
                                }
                            }
                            // +File exists
                            // -Can't find <old-file-spec> = NAME command is aborted, don't send TOBE
                            // if you receive a '+'
                                // TOBE new-file-spec
                            // The server replies with:
                                // +<old-file-spec> renamed to <new-file-spec>
                                // -File wasn't renamed because (reason)
                            break;

                        /* old-file-spec
                        Renames the old-file-spec to be new-file-spec on the remote system */
                        case "TOBE":
                            String file = arg;
                            if (arg == null || arg.length() < 1) {
                                serverSentence = "-File name is not specified. Try again \n";
                            } else {
                                if (loggedIN) {
                                    File oldFile = new File(currentDirectory + System.getProperty("file.separator") + oldFileSpec);
                                    File newFile = new File(currentDirectory + System.getProperty("file.separator") + file);
                                    if (tobeNext) {
                                        if (oldFile.renameTo(newFile)) {
                                            serverSentence = "+" + oldFileSpec + " renamed to " + file + "\n";
                                            tobeNext = false;
                                        } else {
                                            serverSentence = "-File was not changed because a file in that directory is already called " + file + "\n";
                                        }
                                    } else {
                                        serverSentence = "-File name was not changed you need to do the NAME command first\n";
                                    }
                                } else {
                                    serverSentence = "-Not Logged in. Please log in \n";
                                }
                            }
                            outToClient.writeBytes(serverSentence);
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
                            if (arg == null || arg.length() < 1) {
                                serverSentence = "-File is not specified. Try again \n";
                            } else {
                                if (loggedIN) {
                                    File sendFile = new File(currentDirectory + System.getProperty("file.separator") + arg);
                                    if (sendFile.exists()) {
                                        readyToSend = true;
                                        sizeToSend = (int) sendFile.length();
                                        fileToSend = currentDirectory + System.getProperty("file.separator") + arg;
                                        serverSentence = String.valueOf(sizeToSend) + "\n";
                                    } else {
                                        serverSentence = "-File does not exist \n";
                                    }
                                } else {
                                    serverSentence = "-Not Logged in. Please log in \n";
                                }
                            }
                            outToClient.writeBytes(serverSentence);
                            // receiving a '-' from the server should abord the RETR command
                            // <number-of-bytes-that-will-be-sent> (as ascii digits)
                            // -File doesn't exist
                            // then reply to the remote system with:
                                // SEND (ok, waiting for file)
                                // STOP (You don't have enough space to store file)
                                    // +ok, RETR aborted
                            break;

                        case "STOP":
                            if (loggedIN) {
                                if (readyToSend) {
                                    serverSentence = "+ok, RETR aborted \n";
                                } else {
                                    serverSentence = "-Need to send a RETR command first before doing the STOP command \n";
                                }
                            } else {
                                serverSentence = "-Not Logged in. Please log in \n";
                            }
                            outToClient.writeBytes(serverSentence);
                            readyToSend= false;
                            sizeToSend = 0;
                            fileToSend = "";
                            break;

                        case "SEND":
                            if (loggedIN) {
                                if (readyToSend) {
                                    OutputStream out = connectionSocket.getOutputStream();
                                    BufferedInputStream in = new BufferedInputStream(new FileInputStream(fileToSend));
                                    int counter;
                                    byte[] buffer = new byte[1024];
                                    while ((counter = in.read(buffer)) > 0) {
                                        out.write(buffer, 0, counter);
                                        out.flush();
                                    }
                                    in.close();
                                    outToClient = new DataOutputStream(connectionSocket.getOutputStream());
                                    serverSentence = "\t +Send successful \n";
                                } else {
                                    serverSentence = "-Need to send a RETR command first before doing the SEND command \n";
                                }
                            } else {
                                serverSentence = "-Not Logged in. Please log in \n";
                            }
                            outToClient.writeBytes(serverSentence);
                            readyToSend = false;
                            sizeToSend = 0;
                            fileToSend = "";
                            break;

                        /* {NEW | OLD | APP} file-spec
                        Tells the remote system to receive the following file and save it under that name */
                        case "STOR":
                            if (arg == null || arg.length() < 1) {
                                serverSentence = "-File is not specified. Try again \n";
                                outToClient.writeBytes(serverSentence);
                            } else {
                                if (loggedIN) {
                                    String[] properties = arg.split(" ");
                                    storageType = properties[0].toUpperCase();
                                    String[] directory = properties[1].split("\\\\");
                                    fileName = directory[directory.length - 1];
                                    System.out.println("over here");
                                    System.out.println(fileName);
                                    File tempDirectory = new File (currentDirectory + System.getProperty("file.separator") + fileName);
                                    switch (storageType) {
                                        case "NEW":
                                            if (!supportsGenerations) {
                                                serverSentence = "-File exists, but system doesn't support generations \n";
                                            } else if (tempDirectory.exists()) {
                                                serverSentence = "+File exists, will create new generation of file \n";
                                            } else {
                                                serverSentence = "+File does not exist, will create new file \n";
                                            }
                                            outToClient.writeBytes(serverSentence);
                                            break;
                                        case "OLD":
                                            if (tempDirectory.exists()) {
                                                serverSentence = "+Will write over old file \n";
                                            } else {
                                                serverSentence = "+Will create new file \n";
                                            }
                                            outToClient.writeBytes(serverSentence);
                                            break;
                                        case "APP":
                                            if (tempDirectory.exists()) {
                                                serverSentence = "+Will append to file \n";
                                            } else {
                                                serverSentence = "+Will create file \n";
                                            }
                                            outToClient.writeBytes(serverSentence);
                                            break;
                                    }
                                } else {
                                    serverSentence = "-Not Logged in. Please log in \n";
                                    outToClient.writeBytes(serverSentence);
                                }
                            }
                            // receiving a '-' should abort the STOR command
                            // NEW = specifies it should create a new generation of the file and not delete the existing one
                                // +File exists, will create new generation of file
                                // +File does not exist, will create new file
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

                        case "SIZE":
                            if (arg == null || arg.length() < 1) {
                                serverSentence = "-Size is not specified. Try again \n";
                                outToClient.writeBytes(serverSentence);
                            } else {
                                int size = Integer.parseInt(arg);
                                File fileSpace = new File (currentDirectory);
                                long amountOfSpace = fileSpace.getFreeSpace();
                                boolean isTextFile = true;
                                FileOutputStream FOS = null;
                                if (loggedIN) {
                                    if (size < amountOfSpace) {
                                        switch (storageType) {
                                            case "NEW":
                                                String[] fileInfo = fileName.split("\\.");
                                                String name = fileInfo[0];
                                                String fileType = fileInfo[1];
                                                File tempDirectory = new File(currentDirectory +  System.getProperty("file.separator") + fileName);
                                                int version = 1;
                                                while (tempDirectory.exists()) {
                                                    fileName = name + "(" + version + ")." + fileType;
                                                    tempDirectory = new File(currentDirectory +  System.getProperty("file.separator") + fileName);
                                                    version++;
                                                }
                                                FOS = new FileOutputStream(currentDirectory +  System.getProperty("file.separator") + fileName);
                                                break;
                                            case "OLD":
                                                FOS = new FileOutputStream(currentDirectory +  System.getProperty("file.separator") + fileName);
                                                break;
                                            case "APP":
                                                if (fileName.substring(fileName.length() - 4, fileName.length()).equals(".txt")) {
                                                    FOS = new FileOutputStream(currentDirectory + System.getProperty("file.separator") + fileName, true);
                                                } else {
                                                    serverSentence = "-Cannot append file because it can only append text files \n";
                                                    outToClient.writeBytes(serverSentence);
                                                    isTextFile = false;
                                                }
                                                break;
                                        }
                                        if(isTextFile){
                                            serverSentence = "+ok waiting for file, " + "+Saved " + fileName + "\n";
                                            outToClient.writeBytes(serverSentence);
                                            BufferedOutputStream OUT = new BufferedOutputStream(FOS);
                                            for (int i = 0; i < size; i++) {
                                                OUT.write(inFromClient.read());
                                            }
                                            OUT.close();
                                            FOS.close();
                                        }
                                    } else {
                                        serverSentence = "-No space to send it \n";
                                        outToClient.writeBytes(serverSentence);
                                    }
                                } else {
                                    serverSentence = "-Not Logged in. Please log in \n";
                                    outToClient.writeBytes(serverSentence);
                                }
                            }
                            break;

//                        default:
//                            serverSentence = "-Invalid command. Please try again \n";
//                            outToClient.writeBytes(serverSentence);
//                            break;
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
         String[] elements, value;
         while((line = data.readLine()) != null) {
             elements = line.split(" ");
             if (elements.length == 3) {
                 value = new String[]{elements[1], elements[0]};
                 dictAcctUser.put(elements[2], value);
             } else if (elements.length == 2) {
                 usersWithSomeAccess.add(elements[0]);
                 dictUser.put(elements[0], elements[1]);
             } else if (elements.length == 1) {
                 usersWithFullAccess.add(elements[0]);
             }
         }
         data.close();
         System.out.println(dictAcctUser);
         System.out.println(usersWithFullAccess);
         System.out.println(usersWithSomeAccess);
    }
}

