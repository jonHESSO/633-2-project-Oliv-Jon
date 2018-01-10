/*
 * 633-2 project Oliv&Jon - Server - Server.java
 * Author : Jonathan Schnyder
 * Created : 1 déc. 2017
 */

package server;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class Server
{
	private final static Logger logger = Logger.getLogger(Server.class.getName());
	static FileHandler fh = null;
	static String currentMonth = new SimpleDateFormat("MMM").format(Calendar.getInstance().get(Calendar.MONTH)).replaceAll("\\.", "") ;;
	static Logger log = Logger.getLogger("");
	//Creating arrylist containgin a list of files with the IP of the client which has the file
	static List<String[]> fileList = new ArrayList<String[]>() ;
	//Creating a thread safe list from the file list
	static List<String[]> syncList  = Collections.synchronizedList(fileList);
	//Local server IP address
	static String serverName = "192.168.0.15" ;
	//Listening ServerSocket
	static ServerSocket serverSocket ;
	//Listening on port 50000
	static int port = 50000 ;

	public static void main(String[] args)
	{

		
		//creating file handler for logger
		try {
			fh = new FileHandler("ServerLogger_"+currentMonth+".log", true);
			fh.setFormatter(new SimpleFormatter());
			log.addHandler(fh);
		} catch (SecurityException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		logger.log(Level.INFO,"Server has started");

		//thread for creating monthly log file
		Thread thread = new Thread() {
			@Override
			public void run()
			{
				while(true)
				{
					String tempMonth = new SimpleDateFormat("MMM").format(Calendar.getInstance().get(Calendar.MONTH)).replaceAll("\\.", "") ;
					if(!tempMonth.equals(currentMonth))
					{
						logger.log(Level.INFO,"Month changed "+currentMonth);
						currentMonth = tempMonth ;
						try {
							log.removeHandler(fh);
							fh = new FileHandler("ServerLogger_"+currentMonth+".log", true);
							fh.setFormatter(new SimpleFormatter());
							log.addHandler(fh);

						} catch (SecurityException | IOException e) {
							e.printStackTrace();
							logger.log(Level.SEVERE,e.toString());
						}

					}
					try {
						sleep(1000);
					} catch (InterruptedException e1) {
						e1.printStackTrace();
						logger.log(Level.SEVERE,e1.toString());
					}
				}

			}
		};
		thread.start();

		//Starting the server
		try 
		{
			//get InetAddress of the server
			InetAddress serverAddress = InetAddress.getByName(serverName) ;
			//create the listening ServerSocket on port 50000
			serverSocket = new ServerSocket(port, 10, serverAddress) ;
			//Indefinitely accept clients
			while(true)
			{
				//Socket for connecting client
				Socket clientSocket ;
				//accpet connecting client
				clientSocket = serverSocket.accept() ;
				//Create new thread for client
				Thread clientThread = new Thread() 
				{
					@Override
					public void run() 
					{
						try 
						{
							//Input and output streams for getting the client's files and
							//sending the available files list
							ObjectInputStream inputStream ;
							ObjectOutputStream outputStream ;
							//Getting the client's IP address
							InetAddress clientAddress = clientSocket.getInetAddress();
							String clientName = clientAddress.getHostAddress() ;
							//List containing the client's files
							List<String> clientFiles ;
							//Getting the client's file list
							inputStream = new ObjectInputStream(clientSocket.getInputStream()) ;
							clientFiles = (ArrayList<String>)inputStream.readObject();
							//Printing how many files the client has
							logger.log(Level.INFO, "Client "+clientName+" has connected with "+clientFiles.size()+" files");
							//For each file in the client's file list
							for (String s : clientFiles)
							{
								//getting the filename
								String fileName = s ;
								//if the file doesn't already exist with this IP in the 
								if(!fileIsInList(syncList, fileName, clientName))
								{
									//creating a String array with the {Client IP, filename.ext}
									String[] fileInfo = {clientName,fileName} ;
									//Addint the String array to the available files list
									syncList.add(fileInfo) ;
								}					
							}
							//Sending the available files list to the client
							outputStream = new ObjectOutputStream(clientSocket.getOutputStream()) ;
							outputStream.writeObject(fileList);
							outputStream.flush();
							//closing the connection to the client
							clientSocket.close();
						}
						catch(Exception e)
						{logger.log(Level.WARNING, e.toString());}
					}
				} ;	
				//starting the client thread
				clientThread.start();
			}
		} 
		catch (Exception e) 
		{
			logger.log(Level.SEVERE, e.toString());
		}
	}

	//method for verifying if the available files list already contains this filename for this IP address
	public static boolean fileIsInList(List<String[]> fileList, String fileName, String clientName)
	{
		//if the file list is not empty
		if(!fileList.isEmpty()) 
		{
			//for each file in the list
			for (int i = 0; i < fileList.size(); i++) 
			{
				//get the filename and IP address
				String currentFileName = fileList.get(i)[1] ;
				String currentClientName = fileList.get(i)[0] ;
				//verify if filename and IP address match
				if(fileName.equals(currentFileName)&&clientName.equals(currentClientName))
				{
					return true ;
				}
			}
		}
		return false ;
	}
}
