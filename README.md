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

#### TYPE

#### LIST

#### CDIR

#### KILL

#### NAME

### DONE
```
$ DONE
FROM SERVER: +Server closing connection

Process finished with exit code 0
```
#### RETR

#### STOR