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
```
FROM SERVER: +Server SFTP Service 
$ TYPE A
FROM SERVER: -Not logged in. Please log in 
$ TYPE
FROM SERVER: -Type not specified. Try again 
$ USER guest
FROM SERVER: !guest logged in 
$ TYPE A
FROM SERVER: +Using Ascii mode 
```
#### LIST
This command lists the files in the specified directory in either a verbose (V) or formatted (F) way
```
$ USER guest
FROM SERVER: !guest logged in 
$ LIST F C:\Users\jemje\forTesting
FROM SERVER: +C:\Users\jemje\forTesting	file1.txt, 	file2.txt, 	Folder, 	
```
```
$ USER guest
FROM SERVER: !guest logged in 
$ LIST V C:\Users\jemje\forTesting
FROM SERVER: +C:\Users\jemje\forTesting	file1.txt	12Bytes	Mon Aug 30 20:31:45 NZST 2021, 	file2.txt	15Bytes	Tue Aug 31 17:17:27 NZST 2021, 	Folder	0Bytes	Mon Aug 30 21:16:53 NZST 2021, 	
```
```
FROM SERVER: +Server SFTP Service 
$ LIST F C:\Users\jemje\forTesting
FROM SERVER: -Not Logged in. Please log in 
```
```
$ USER guest
FROM SERVER: !guest logged in 
$ LIST
FROM SERVER: Format is not specified. Try again 
$ LIST F
FROM SERVER: +C:\Users\jemje\Documents\2021\SEMESTER2\COMPSYS725\assignment1\jmar948\CS725_A1	.classpath, 	.git, 	.idea, 	.project, 	CS725_A1.iml, 	README.md, 	src, 	test.db,
$ LIST F random
FROM SERVER: +random	+Empty directory  
```
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
FROM SERVER: +Server SFTP Service 
$ CDIR C:\Users\jemje\forTesting
FROM SERVER: -Not Logged in. Please log in 
$ USER guest
FROM SERVER: !guest logged in 
$ CDIR C:\Users\jemje\forTesting
FROM SERVER: !Changed working dir to C:\Users\jemje\forTesting
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
```
$ USER guest
FROM SERVER: !guest logged in 
$ CDIR C:\Users\jemje\forTesting
FROM SERVER: !Changed working dir to C:\Users\jemje\forTesting
$ LIST F C:\Users\jemje\forTesting
FROM SERVER: +C:\Users\jemje\forTesting	file1.txt, 	file2.txt, 	Folder, 
$ KILL file2.txt
FROM SERVER: +file2.txt deleted
```
```
FROM SERVER: +Server SFTP Service 
$ KILL file2.txt
FROM SERVER: -Not Logged in. Please log in
$ USER guest
FROM SERVER: !guest logged in 
$ CDIR C:\Users\jemje\forTesting
FROM SERVER: !Changed working dir to C:\Users\jemje\forTesting
$ LIST F C:\Users\jemje\forTesting
FROM SERVER: +C:\Users\jemje\forTesting	file1.txt, 	file2.txt, 	Folder, 
$ KILL
FROM SERVER: -Can't kill because the filename has not been specified. Try again
$ KILL file2.txt
FROM SERVER: +file2.txt deleted
```
#### NAME
This command renames the old-file-spec to be new-file-spec on the remote system
```
FROM SERVER: +Server SFTP Service 
$ USER guest
FROM SERVER: !guest logged in 
$ CDIR C:\Users\jemje\forTesting
FROM SERVER: !Changed working dir to C:\Users\jemje\forTesting
$ LIST F C:\Users\jemje\forTesting
FROM SERVER: +C:\Users\jemje\forTesting	file1.txt, 	file2.txt, 	Folder, 	
$ NAME file1.txt
FROM SERVER: +File exists 
$ NAME
FROM SERVER: -File name is not specified. Try again 
$ NAME file3.txt
FROM SERVER: -Can't find file3.txt NAME command is aborted, don't send TOBE 
```
```
FROM SERVER: +Server SFTP Service 
$ NAME file1.txt
FROM SERVER: -Not Logged in. Please log in 
```
##### TOBE
This command can only be used after the NAME command and allows the user to provide the new file name as an argument. Within this command, the server will take the chosen file from the NAME command and rename the file with the new file name given from the TOBE command.
```
FROM SERVER: +Server SFTP Service 
$ USER guest
FROM SERVER: !guest logged in 
$ CDIR C:\Users\jemje\forTesting
FROM SERVER: !Changed working dir to C:\Users\jemje\forTesting
$ LIST F C:\Users\jemje\forTesting
FROM SERVER: +C:\Users\jemje\forTesting	file1.txt, 	file2.txt, 	Folder, 	
$ NAME file2.txt
FROM SERVER: +File exists 
$ TOBE newfilename.txt
FROM SERVER: +file2.txt renamed to newfilename.txt
$ LIST F C:\Users\jemje\forTesting
FROM SERVER: +C:\Users\jemje\forTesting	file1.txt, 	Folder, 	newfilename.txt, 	
```
```
FROM SERVER: +Server SFTP Service 
$ TOBE file2.txt
FROM SERVER: -Not Logged in. Please log in 
$ USER guest
FROM SERVER: !guest logged in 
$ TOBE file2.txt
FROM SERVER: -File name was not changed you need to do the NAME command first
$ CDIR C:\Users\jemje\forTesting
FROM SERVER: !Changed working dir to C:\Users\jemje\forTesting
$ LIST F C:\Users\jemje\forTesting
FROM SERVER: +C:\Users\jemje\forTesting	file1.txt, 	Folder, 	newfilename.txt, 	
$ NAME newfilename.txt
FROM SERVER: +File exists 
$ TOBE file2.txt
FROM SERVER: +newfilename.txt renamed to file2.txt
$ LIST F C:\Users\jemje\forTesting
FROM SERVER: +C:\Users\jemje\forTesting	file1.txt, 	file2.txt, 	Folder, 
```
#### DONE
This command tells the remote system that you are done
```
FROM SERVER: +Server SFTP Service 
$ USER guest
FROM SERVER: !guest logged in 
$ DONE
FROM SERVER: +Server closing connection

Process finished with exit code 0
```
```
$ DONE
FROM SERVER: +Server closing connection

Process finished with exit code 0
```
#### RETR
This command requests that the remote system sends the specified file and returns the number of characters used in this file
```
$ USER guest
FROM SERVER: !guest logged in 
$ CDIR C:\Users\jemje\forTesting
FROM SERVER: !Changed working dir to C:\Users\jemje\forTesting
$ RETR file1.txt
FROM SERVER: 12
```
```
$ USER guest
FROM SERVER: !guest logged in 
$ CDIR C:\Users\jemje\forTesting
FROM SERVER: !Changed working dir to C:\Users\jemje\forTesting
$ RETR invalidfile
FROM SERVER: -File does not exist 
```
```
FROM SERVER: +Server SFTP Service 
$ RETR randomfile
FROM SERVER: -Not Logged in. Please log in 
$ USER guest
FROM SERVER: !guest logged in 
$ CDIR C:\Users\jemje\forTesting
FROM SERVER: !Changed working dir to C:\Users\jemje\forTesting
$ LIST F C:\Users\jemje\forTesting
FROM SERVER: +C:\Users\jemje\forTesting	file1.txt, 	file2.txt, 	Folder, 	
$ RETR randomfile
FROM SERVER: -File does not exist 
$ RETR file1.txt
FROM SERVER: 12
```
##### SEND
```
$ USER guest
FROM SERVER: !guest logged in 
$ CDIR C:\Users\jemje\forTesting
FROM SERVER: !Changed working dir to C:\Users\jemje\forTesting
$ LIST F C:\Users\jemje\forTesting
FROM SERVER: +C:\Users\jemje\forTesting	file1.txt, 	file2.txt, 	Folder, 	
$ RETR file2.txt
FROM SERVER: 15
$ SEND
FROM SERVER: hello world x2!	 +Send successful 
```
```
FROM SERVER: +Server SFTP Service 
$ SEND
FROM SERVER: -Not Logged in. Please log in 
$ USER guest
FROM SERVER: !guest logged in 
$ SEND
FROM SERVER: -Need to send a RETR command first before doing the SEND command 
$ CDIR C:\Users\jemje\forTesting
FROM SERVER: !Changed working dir to C:\Users\jemje\forTesting
$ LIST F C:\Users\jemje\forTesting
FROM SERVER: +C:\Users\jemje\forTesting	file1.txt, 	file2.txt, 	Folder, 	
$ RETR file2.txt
FROM SERVER: 15
$ SEND
FROM SERVER: hello world x2!	 +Send successful 
```
##### STOP
```
$ USER guest
FROM SERVER: !guest logged in 
$ CDIR C:\Users\jemje\forTesting
FROM SERVER: !Changed working dir to C:\Users\jemje\forTesting
$ LIST F C:\Users\jemje\forTesting
FROM SERVER: +C:\Users\jemje\forTesting	file1.txt, 	file2.txt, 	Folder, 	
$ RETR file2.txt
FROM SERVER: 15
$ STOP
FROM SERVER: +ok, RETR aborted 
```
```
FROM SERVER: +Server SFTP Service 
$ STOP
FROM SERVER: -Not Logged in. Please log in 
$ USER guest
FROM SERVER: !guest logged in 
$ STOP
FROM SERVER: -Need to send a RETR command first before doing the STOP command 
$ CDIR C:\Users\jemje\forTesting
FROM SERVER: !Changed working dir to C:\Users\jemje\forTesting
$ LIST F C:\Users\jemje\forTesting
FROM SERVER: +C:\Users\jemje\forTesting	file1.txt, 	file2.txt, 	Folder, 	
$ RETR file1.txt
FROM SERVER: 12
$ STOP
FROM SERVER: +ok, RETR aborted 
```
#### STOR
This command tells the remote system to receive the following file and save it under that name 

#### SIZE