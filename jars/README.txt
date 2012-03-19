@author: Tarek Almiski
Usage Instructions for upload/download client

There are two programs being supplied: FileServer and Client.  Let me tell you how to run and test them both.  

##########
FileServer
##########

1. The FileServer program can be started with the server.jar file using the command: 

	"java -jar server.jar <port>"

If a valid, empty port is not supplied, the server will default to port 9999.  This is fine for one instance, but further instances will not start unless a free port is supplied on the command line.  

2. The FileServer program has no further interactivity after starting it and can only be stopped using a force-quit like ctrl-c or killing the process.  

#######
Client
#######

1. The Client program can be started using the command: 

	"java -jar client.jar"

2. Once the client program is started, the user will have 3 options: 1 - download, 2 - upload, and 3 - exit

Each option can be chosen by inputting the number associated with it and hitting a carriage return.  

3. The download option: 

	Selecting 1 will then prompt the user to supply the hostname and port of the desired server to connect.  They should be supplied on the same line with a space as in "hostname port".  

	After successfully connecting to a server, user is prompted for the name of desired file.  It is assumed that the user knows the name of the file beforehand.  
	
	It should be noted that in the case that the server fails, the user is prompted to select another server to continue the download.  This server will resume the download where the previous server left off.  It should also be noted that very short sleep statements have been inserted during transfers in order to give the ability to "force" failures on servers to see this functionality.  

4. The upload option

	Selecting 2 will give user the ability to upload.  The user is asked for the hostname and port of the server to upload to and they should be supplied similar to download (hostname port).  User can then upload files to server from the directory where the client is running to the directory where the server is running.  
	It should be noted that in the case of a server failure, the client is prompted to select another server to continue the upload and that server will continue where the failed server left off.  
	It should also be noted that servers will not accept duplicate files to be uploaded.  

5. Exit option
	Selecting 3 will exit the client program.  There is no need to force-quit the client program.  