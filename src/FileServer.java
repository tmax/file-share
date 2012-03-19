import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class FileServer {

	static ServerSocket server;
	public static List<File> files;
	
	public FileServer(){}
	
	protected void finalize() throws Throwable{
		try{
			System.out.println("(FileServer finalize): closing socket: "+server.getLocalPort());
			server.close();
		}
		finally{
			super.finalize();
		}
	}
	public static void main(String[] args){
		int SERVERPORT = args.length > 0? new Integer(args[0]) : 9999;
		
		try {
			server = new ServerSocket(SERVERPORT);
			files = new LinkedList<File>();
			
//			File f = new File("test.txt");
//			f.createNewFile();
//			System.out.println("file name: "+f.toString());
//			files.add(f);

			System.out.println("listening on port: "+SERVERPORT+"...\n");
			while(true){
				Socket client = server.accept();
				new Thread(new ServerThread(client, files)).start();
			}
		} catch (IOException e) {
			e.printStackTrace();
			System.exit(-1);
		}
	}
}

class ServerThread implements Runnable{
	Socket client;
	static List<File> files;
	
	public ServerThread(Socket s, List<File> l){
		client = s;
	}

	protected void finalize() throws Throwable{
		try{
			//System.out.println("(ServerThread finalize): closing socket: "+client.getPort());
			client.close();
		}
		finally{
			super.finalize();
		}
	}

	@Override
	public void run() {
		try {
			Map<String, String> rm = new HashMap<String, String>();
			rm.put("1", "Download"); 
			rm.put("2", "Upload"); 
			rm.put("3", "Exit");
			rm.put("4", "Resume Download");
			rm.put("5", "Resume Upload");

			String input;
			boolean done = false;
				
			PrintWriter out = new PrintWriter(client.getOutputStream(), true);
			BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
			System.out.println("now connected with client on port: "+client.getPort());

			input = in.readLine();
			String[] clientargs = input.split(" ");
			System.out.println("Received request '"+rm.get(clientargs[0])+
					"' from client at port "+client.getPort());

			if(clientargs[0].equals("1")){
				input = clientargs[1];
				File foundit = new File(input);
				
				if( foundit.exists()){
					out.println("1 "+foundit.length());//export size of file

					//copy contents of file to byte[]
					byte[] buff = new byte[(int)foundit.length()];
					InputStream fis = new FileInputStream(foundit);
					fis.read(buff, 0, buff.length);

					OutputStream bos = new BufferedOutputStream(client.getOutputStream());
					System.out.print("sending...");
					bos.write(buff, 0, buff.length);
					bos.flush();
					System.out.println("done!");
				}
				else{
					out.println("-1");//don't have that file
//							System.out.println("we aint gots that joint cho");
				}
			}
			else if(clientargs[0].equals("2")){
				String upFilename = clientargs[1];//wait for name of file
				File up = new File(upFilename);
				int filesize;
				if(!up.exists()){//accept upload if we don't already have that file
					out.println("1");//onlly receive if file DNE
					filesize = new Integer(in.readLine());//read in filesize
				}else{//already have file
					out.println("-1");//tell them not to send it
					filesize = -1;
				}
				if(filesize > -1){
					byte[] buf = new byte[filesize];
					InputStream is = client.getInputStream();
					int bytesread = is.read(buf, 0, buf.length);
					System.out.println("bytesread: "+bytesread);
					int firstbytes = bytesread;
					int current = bytesread;
					int sum = firstbytes;
					System.out.print("receiveing...");
					File clone = new File(upFilename);
					clone.createNewFile();
					FileOutputStream fos = new FileOutputStream(clone);
					fos.write(buf, 0, current);
					
					
					do{
						bytesread = is.read(buf, current, (buf.length - current));
						
						if(bytesread >= 0){
							fos.write(buf, current, bytesread);
							current +=bytesread;
						}
						sum+=bytesread;
						
						try {
							Thread.sleep(15);
						} catch (InterruptedException e) {}
						
					}while( bytesread!=-1 && bytesread!=0);
					System.out.println("done!");
//					File clone = new File(upFilename);
//					clone.createNewFile();
//					FileOutputStream fos = new FileOutputStream(clone);
//					fos.write(buf);
					fos.close();
				}
			}
			else if(input.equals("3")){
				out.println("bye bye :)");
			}
			else if(clientargs[0].equals("4")){
				String filename = clientargs[1];
				int index = new Integer(clientargs[2]);
				System.out.println("so you wanna continue a download eh?");
				System.out.println("you wanna download: "+filename+" and you have "+index+" bytes so far");
				File f = new File(filename);
				int flength = (int)f.length();
				System.out.println(filename+"is "+flength+" in size");
				byte[] buff = new byte[flength];
				InputStream fis = new FileInputStream(f);
				fis.read(buff, 0, buff.length);
				OutputStream bos = new BufferedOutputStream(client.getOutputStream());
				System.out.println("picking up where last guy left off...");
				bos.write(buff, index, buff.length-index);
				bos.flush();				
				
			}
			else if(clientargs[0].equals("5")){

				String upFile = clientargs[1];
				File f = new File(upFile);
				int curSize = -1;
				if(f.exists())
					curSize = (int)f.length();
				out.println(curSize);//send out current size of file
				int remaining = new Integer(in.readLine());//expect remaining
				
				int totalSize = curSize + remaining;
				
				System.out.println("so tehn its this big? : "+totalSize);
				
				//byte[] remains = new byte[remaining];
				

//------
				byte[] buf = new byte[remaining];
				InputStream is = client.getInputStream();
				int bytesread = is.read(buf, 0, buf.length);
				System.out.println("bytesread: "+bytesread);
				int firstbytes = bytesread;
				int current = bytesread;
				int sum = firstbytes;
				System.out.print("receiveing...");
				do{
					bytesread = is.read(buf, current, (buf.length - current));
					if(bytesread >= 0){
						current +=bytesread;
					}
					sum+=bytesread;
					try {
						Thread.sleep(15);
					} catch (InterruptedException e) {}
					
				}while( bytesread!=-1 && bytesread!=0);
				System.out.println("done!");
//-----
				
				byte[] curBytes = new byte[curSize];
				FileInputStream fis = new FileInputStream(f);
				fis.read(curBytes, 0, curBytes.length);
				
				byte[] buftotal = new byte[totalSize];
				
				System.out.println("buf.length: "+buf.length);
				System.out.println("curbytes.length: "+curBytes.length);
				System.out.println("buftoal.legnth: "+buftotal.length);
				try {
					Thread.sleep(5000);
				} catch (InterruptedException e) {}
				
				for(int i = 0; i < curBytes.length; i++){
					buftotal[i] = curBytes[i];
				}
				for(int i = 0; i < buf.length; i++){
//					System.out.println(buf.length + i);
					buftotal[curBytes.length + i] = buf[i];
				}
				f = new File(upFile);
				f.createNewFile();
				FileOutputStream fos = new FileOutputStream(f);
				fos.write(buftotal, 0, buftotal.length);
				fos.close();
				
				System.out.println("AM I DONE!!!????");
				System.out.println(f.length());
				
				

				
//				FileOutputStream fis = new FileOutputStream(f);
//				fis.write(buf, curSize, buf.length - curSize);
//				
//				System.out.println("new filesize: "+f.length());
//				System.out.println("...done???");
				
				
				
			}
			else{
				out.println("hi i'm the server");
			}
			System.out.println("listening on port: "+client.getLocalPort()+"...\n");
			client.close();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (OutOfMemoryError e){
			System.out.println("out of memory!!!");
		}
	}
}