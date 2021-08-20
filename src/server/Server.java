package server;

import java.io.*;
import java.net.*;

class Server {

    private static boolean connectionLost = false;

    public static void main(String[] argv) throws Exception
    {
        String clientSentence;
        String serverSentence;
        Boolean isActive = true;
        String cmd, arg;

        ServerSocket welcomeSocket = new ServerSocket(6789);

        while(true) {
            // Accept connection from client and initialise data streams
            Socket connectionSocket = welcomeSocket.accept();
            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
            DataOutputStream  outToClient = new DataOutputStream(connectionSocket.getOutputStream());

            if (connectionLost) {
                serverSentence = "-Server Out to Lunch \n";
                System.out.println();
                outToClient.writeBytes(serverSentence);
            } else {
                serverSentence = "+Server SFTP Service \n";
                System.out.println();
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
                            // !<user-id> logged in = do not need an account or password/you specified a user-id not needing them
                            // +User-id valid, send account and password
                            // -Invalid user-id, try again
                            break;

                        /* account
                        The account you want to use (usually used for billing) on the remote system */
                        case "ACCT":
                            // !Account valid, logged-in = account was ok/not needed. skip password
                            // +Account valid, send password = account ok/not needed. send your password next
                            // -Invalid account, try again
                            break;

                        /* password
                        Your password on the remote system */
                        case "PASS":
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
                        This will delete the file from the remot system */
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
}

