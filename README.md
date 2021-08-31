# SFTP - RFC913
## CS726: Assignment 1

### Submission
An implementation of the file transfer protocol described in [RFC 913](https://tools.ietf.org/html/rfc913).
This has been implemented in Java, using port 6789 as the default.

Author: Jemimah Martinez  
UPI: jmar948  
ID: 134166382

### File Structure

### Client Commands

### User Details

### Instructions for compiling

### Running the Tests

### Test Cases

#### USER
This command uses your userid on the remote system to get authenticated  
```
$ USER student
FROM SERVER: +User-id valid, send account and password
```
```
$ USER lecturer
FROM SERVER: +User-id valid, send account and password 
```
```
$ USER TA
FROM SERVER: !User-id valid, send password 
```
```
$ USER guest
FROM SERVER: !guest logged in 
```
```
$ USER hello
FROM SERVER: -Invalid user-id, try again 
```
#### ACCT
This command uses your accountid you want to use on the remote system - also used for authentication
```
$ USER student
FROM SERVER: +User-id valid, send account and password
$ ACCT jmar948
FROM SERVER: +Account valid, send password
```
```
$ USER lecturer
FROM SERVER: +User-id valid, send account and password 
$ ACCT abcd123
FROM SERVER: +Account valid, send password 
```
```
$ USER student
FROM SERVER: +User-id valid, send account and password 
$ ACCT jmar944
FROM SERVER: -Invalid account, try again 
$ ACCT jmar948
FROM SERVER: +Account valid, send password 
```
#### PASS
This command uses your password on the remote system to authenticate your userid and accountid 
```
$ USER student
FROM SERVER: +User-id valid, send account and password
$ ACCT jmar948
FROM SERVER: +Account valid, send password
$ PASS 1234
FROM SERVER: !Logged in
```
```
$ USER lecturer
FROM SERVER: +User-id valid, send account and password 
$ ACCT abcd123
FROM SERVER: +Account valid, send password 
$ PASS efg
FROM SERVER: !Logged in 
```
```
$ USER TA
FROM SERVER: !User-id valid, send password 
$ PASS assistant2
FROM SERVER: !Logged in 
```
```
$ USER student
FROM SERVER: +User-id valid, send account and password 
$ ACCT jmar948
FROM SERVER: +Account valid, send password 
$ PASS abcd
FROM SERVER: -Wrong password, try again 
$ PASS 1234
FROM SERVER: !Logged in 
```
> The following commands can only be used when the user is logged in and has been authenticated into the remote system 
#### TYPE
This command tells the server how to store the files through ASCII (A) or Binary (B) or Continuous (C)
```
$ USER guest
FROM SERVER: !guest logged in 
$ TYPE A
FROM SERVER: +Using Ascii mode 
$ TYPE B
FROM SERVER: +Using Binary mode 
$ TYPE C
FROM SERVER: +Using Continuous mode 
```
#### LIST
This command lists the files in the specified directory in either a verbose (V) or formatted (F) way

#### CDIR
This command will change the current working directory on the remote host to the argument passed 
``` 
$ USER guest
FROM SERVER: !guest logged in 
$ CDIR C:\Users\jemje\forTesting
FROM SERVER: !Changed working dir to C:\Users\jemje\forTesting
```
```
$ USER TA
FROM SERVER: !User-id valid, send password 
$ PASS assistant2
FROM SERVER: !Logged in 
$ CDIR random
FROM SERVER: -Can't connect to directory because what you provided does not exist 
```
```
$ USER student
FROM SERVER: +User-id valid, send account and password 
$ ACCT jmar948
FROM SERVER: +Account valid, send password 
$ PASS 1234
FROM SERVER: !Logged in 
$ CDIR C:\Users\jemje\forTesting
FROM SERVER: +directory ok, send account/password 
$ ACCT jmar948
FROM SERVER: +Account valid, send password 
$ PASS 1234
FROM SERVER: !Logged in 	 !Changed working dir to C:\Users\jemje\forTesting
```
#### KILL
This command will delete the file you specified from the remote system

#### NAME
This command renames the old-file-spec to be new-file-spec on the remote system

#### DONE
This command tells the remote system that you are done
```
$ DONE
FROM SERVER: +Server closing connection

Process finished with exit code 0
```
#### RETR
This command requests that the remote system sends the specified file

#### STOR
This command tells the remote system to receive the following file and save it under that name 