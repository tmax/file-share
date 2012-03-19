import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Date;

public class Client {
	
	private static Socket socket;
	private static int serverPort;
	
	public Client(){}
	
	public Client(int port){
		serverPort = port;
	}
	
	protected void finalize() throws Throwable{
		try{
			System.out.println("(client finalize): closing socket: "+socket.getPort());
			socket.close();
		}
		finally{
			super.finalize();
		}
	}
	public static void main(String[] args){
		try {
			
			String menu = 
				"\nWelcome to the FileServer system. Select an option\n\n" +
				"1. download a file\n" +
				"2. upload a file\n" +
				"3. exit\n";			

			boolean done = false;
			BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
			String toServer, fromServer;
				
			do{
				System.out.println(menu);
				toServer = stdin.readLine();//read selection
				
				if(toServer.equals("1")){//download case
					
					System.out.println("which server would you like to work with???");
					System.out.println("specify hostname then port number separated by space");
					String[] serverString = stdin.readLine().split(" ");
					
					String inetaddress = serverString[0];
					int port = new Integer(serverString[1]);
					
					socket = new Socket(InetAddress.getByName(inetaddress), port);
					System.out.println("Now connected with server at port "+socket.getPort());
					System.out.println("my port is: "+socket.getLocalPort());
					PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
					BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

					System.out.println("specify file name");
					String filename = stdin.readLine();
					toServer += " "+filename;
					
					out.println(toServer);//send filename to server

					fromServer = in.readLine();//output regarding our file: 1 = its there; -1 = not there
					String[] serverargs = fromServer.split(" ");//expecting "1 filesize" back froms server

					if(serverargs[0].equals("1")){//file exists, begin transfer of bytes

						int filesize = new Integer(new Integer(serverargs[1]));
						byte[] buff = new byte[filesize];
						
						InputStream is = socket.getInputStream();
						int bytesread = is.read(buff, 0, buff.length);
						int firstbytes = bytesread;
						int current = bytesread;
						int sum = firstbytes;
						System.out.print("\ndownloading...");
						
						
					    do {
					    	try{
					        bytesread = is.read(buff, current, (buff.length-current));
					    	}catch(SocketException e){
					    		System.out.println("disconnected from server");
					    		System.out.println("would you like to continue download from another server? y/n");
					    		String c = stdin.readLine();
					    		if(c.toLowerCase().equals("y")){
					    			System.out.println("specify hostname and port separated by a space");
					    			String[] rs = stdin.readLine().split(" ");
					    			socket = new Socket(InetAddress.getByName(rs[0]), new Integer(rs[1]));
					    			is = socket.getInputStream();
					    			out = new PrintWriter(socket.getOutputStream(), true);
					    			
					    			System.out.println("downloaded so far: "+sum);
					    			System.out.println("bytesread: "+bytesread);
//					    			try {
//										Thread.sleep(5000);
//									} catch (InterruptedException e1) {}
					    			String resume = "4"+" "+filename+" "+(sum+bytesread);
					    			out.println(resume);
//					    			break;
					    			
					    		}
					    		else if(c.toLowerCase().equals("n"))
					    			break;
					    		else
					    			System.out.println("invalid choice");
					    	}
					        
					    	if(bytesread >= 0) {
					        	current += bytesread;
					        	sum+= bytesread;
					    	}
					    	try {
								Thread.sleep(15);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
//							System.out.println("filesize: "+filesize);
//			    			System.out.println("downloaded so far: "+sum);
//			    			System.out.println("bytesread: "+bytesread);

					     } while( (bytesread != -1) && bytesread != 0);
					    
					    

					    System.out.println("done!");
						//make new file to transfer bytes
						File download = new File(filename);
						download.createNewFile();
						OutputStream fos = new FileOutputStream(download);
						//transfer bytes into file
						if(current == 0)
							fos.write(buff, 0, buff.length);
						else
							fos.write(buff, 0, buff.length);
						fos.close();
					}
					else if(serverargs[0].equals("-1")){
						System.out.println("file doesn't exist on server");						
					}
				}
				else if(toServer.equals("2")){//upload case
					
					System.out.println("which server would you like to work with???");
					System.out.println("specify hostname then port number separated by space");
					String[] serverString = stdin.readLine().split(" ");
					
					String inetaddress = serverString[0];
					int port = new Integer(serverString[1]);
					
					socket = new Socket(InetAddress.getByName(inetaddress), port);
					System.out.println("Now connected with server at port "+socket.getPort());
					System.out.println("my port is: "+socket.getLocalPort());
					PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
					BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

					
					System.out.println("\nspecify file to upload");
					String upFilename = stdin.readLine();
					File f = new File(upFilename);
					if(f.exists()){
						toServer += " "+upFilename;
						out.println(toServer);
						fromServer = in.readLine();
						if(fromServer.equals("1")){
							out.println(f.length());
							InputStream fis = new FileInputStream(f);
							byte[] buf = new byte[(int) f.length()];
							fis.read(buf, 0, buf.length);
							OutputStream os = socket.getOutputStream();
							System.out.print("\nuploading...");
							try{
								os.write(buf);
								os.flush();
							}catch(SocketException e){

					    		System.out.println("disconnected from server");
					    		System.out.println("would you like to continue upload to another server? y/n");
					    		String c = stdin.readLine();
					    		if(c.toLowerCase().equals("y")){
					    			System.out.println("specify hostname and port separated by a space");
					    			String[] rs = stdin.readLine().split(" ");
					    			socket = new Socket(InetAddress.getByName(rs[0]), new Integer(rs[1]));
					    			out = new PrintWriter(socket.getOutputStream(), true);
					    			//send resume code and filename
					    			System.out.println("we have: "+socket.getLocalPort());
					    			String resumeUp = "5"+" "+upFilename;
					    			out.println(resumeUp);
					    			//send filename that was uploaded
					    			
					    			
					    			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
					    			//expect file size so far response
					    			int sofar = new Integer(in.readLine());
					    			//send back size of whole file
					    			System.out.println("FILE IS THIS BIG: "+f.length());
					    			out.println(f.length() - sofar);
					    			os = socket.getOutputStream();
					    			//write remaining bytes to stream
					    			int remBytes = (int) f.length();
					    			remBytes -= sofar;
					    			byte[] remaining = new byte[remBytes];
					    			for(int i = 0; i< remaining.length; i++){
					    				remaining[i] = buf[sofar+i];
					    			}
					    			
					    			
					    			os.write(remaining);
					    			os.flush();
					    			//wait to hear how much 
					    		}
					    		else if(c.toLowerCase().equals("n"))
					    			;
					    		else
					    			System.out.println("invalid choice");
					    	}
							//System.out.println("done!");
						}else
							System.out.println("filename already exists on server. sorry no duplicates :)");
					}else
						System.out.println("that file don't exist");
				}
				else if(toServer.equals("3")){//exit
					System.out.println("bye bye :'(");
					done = true;
				}
				else{
					System.out.println("you typed: \""+toServer+"\"...that doesn't make any sense");
				}
				if(socket != null)
					socket.close();
			}while(!done);				
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (OutOfMemoryError e){
			System.out.println("out of memory!!!");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}