/*
 * 633-2 project Oliv&Jon - Client - Client.java
 * Author : Jonathan Schnyder
 * Created : 1 déc. 2017
 */

package client;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Client
{
	public static void main(String[] args) throws UnknownHostException, IOException, ClassNotFoundException
	{
		/*Connection to the server*/
		
		//Variables for connecting to the server		
		Socket serverConnectionSocket ;
		String serverName = "192.168.108.10" ;
		String localName = "192.168.108.1" ;
		int port1 = 50000 ;
		int port2 = 50001 ;

		//Data lists
		List<String> clientFileList = null ;
		List<String[]> serverFileList = null ;
		String path = "data/" ;
		
		//Get the client file list
		clientFileList = getClientFiles(path) ;

		//connect to server
		serverConnectionSocket = connectToServer(serverName, port1) ;
		
		//send client file list to server
		sendClientFileList(serverConnectionSocket, clientFileList) ;
		
		//get the available file list from the server
		serverFileList = getServerFileList(serverConnectionSocket) ;
		
		//close connection to server		
		serverConnectionSocket.close();
		
		
		/*Sending a file to another client*/
		
		
		//ServerSocket for incoming connections
		InetAddress localAddress = InetAddress.getByName(localName) ;
		ServerSocket listeningSocket = new ServerSocket(port2, 5, localAddress) ;
		
		//Creating thread for accepting incoming connections
		Thread waitToSendThread = new Thread() {
			@Override
			public void run()
			{
				while(true)
				{
					try
					{
						//accept incoming connection
						Socket clientSendingSocket ;
						clientSendingSocket = acceptClientConnection(listeningSocket) ;
						//Create new thread for connecting client
						Thread sendingThread = new Thread() {
							@Override
							public void run()
							{
								//get requested file name
								String fileName;
								try
								{
									fileName = getRequestedFileName(clientSendingSocket);
									//send requested file to client
									sendFileToClient(clientSendingSocket, path, fileName) ;						
									//close connection to client
									clientSendingSocket.close();
								} catch (ClassNotFoundException | IOException e)
								{
									e.printStackTrace();
								}								
							}
						};
						//start the client thread
						sendingThread.start();
					
					} 
					catch (IOException e)
					{
						e.printStackTrace();
					}
				}		
			}			
		};
		//Start the thread for sending files
		waitToSendThread.start();	
		
		
		/*Donwloading a file from a client*/
		
		//Ask the user which file to download
		Scanner scanner = new Scanner(System.in) ;
		while(true)
		{
			showAvailableFiles(serverFileList);
			System.out.println("Select index of wanted file");
			int fileIndex = scanner.nextInt() ;		
			
			
			//Connect to client which has the file		
			String[] fileInfo = serverFileList.get(fileIndex) ;
			String clientName = fileInfo[0] ;
			String fileName = fileInfo[1] ;
			
			Socket clientDownloadingSocket = connectToClient(clientName, port2) ;
			
			//Send requested file name
			sendRequestedFileName(clientDownloadingSocket, fileName) ;
			
			//DownLoading the file from the client
			downloadFileFromClient(clientDownloadingSocket, fileInfo, path) ;
			
			//close connection to the client
			clientDownloadingSocket.close();	
		}		
	}
	
	

	/*Broadcasting and retrieving file list to/from server*/
	
	public static List<String> getClientFiles(String path)
	{
		File directory = new File(path) ;
		List<String> clientFiles = new ArrayList<>() ;
		File[] files = directory.listFiles() ;
		System.out.println(files) ;
		for (File f : files) {
			clientFiles.add(f.getName()) ;
		}
		return clientFiles ;
	}
	
	public static Socket connectToServer(String serverName, int port) throws UnknownHostException, IOException
	{		
		InetAddress serverAddress = InetAddress.getByName(serverName);		
		Socket serverConnectionSocket = new Socket(serverAddress, port) ;
		return serverConnectionSocket ;
	}
	
	public static boolean sendClientFileList(Socket clientSocket, List<String> clientFileList) throws IOException
	{
		ObjectOutputStream outputStream = new ObjectOutputStream(clientSocket.getOutputStream()) ;
		outputStream.writeObject(clientFileList);
		return true ;
	}
	
	public static List<String[]> getServerFileList(Socket clientSocket) throws IOException, ClassNotFoundException
	{
		List<String[]> serverFileList = null ;
		ObjectInputStream inputStream = new ObjectInputStream(clientSocket.getInputStream()) ;
		serverFileList = (ArrayList<String[]>)inputStream.readObject() ;
		for (String[] fileName: serverFileList)
		{
			System.out.println(fileName[0]+" : "+fileName[1]) ;
		}
		return serverFileList ;
	}
	
	/*Sending a file to a client*/
	
	public static Socket acceptClientConnection(ServerSocket listeningSocket) throws IOException
	{
		Socket clientSendingSocket = listeningSocket.accept() ;
		return clientSendingSocket ;
	}
	
	public static String getRequestedFileName(Socket clientSendingSocket) throws IOException, ClassNotFoundException
	{
		ObjectInputStream inputStream = new ObjectInputStream(clientSendingSocket.getInputStream()) ;
		String fileName = (String)inputStream.readObject() ;
		return fileName ;
	}
	
	public static boolean sendFileToClient(Socket clientSendingSocket, String path, String fileName) throws IOException
	{
		OutputStream outputStream = clientSendingSocket.getOutputStream() ;
		Files.copy(Paths.get(path+fileName), outputStream) ;
		return true ;
	}
	
	/*Recieving a file from a client*/
	
	public static Socket connectToClient(String clientName, int port) throws IOException
	{
		InetAddress clientAddress = InetAddress.getByName(clientName) ;
		Socket clientDownloadingSocket = new Socket(clientAddress, port) ;
		return clientDownloadingSocket ;
	}
	
	private static void sendRequestedFileName(Socket clientDownloadingSocket, String fileName) throws IOException
	{
		ObjectOutputStream output = new ObjectOutputStream(clientDownloadingSocket.getOutputStream()) ;
		output.writeObject(fileName) ;		
	}
	
	public static boolean downloadFileFromClient(Socket clientDownloadingSocket, String[] fileInfo, String path) throws IOException, ClassNotFoundException 
	{
		String fileName = fileInfo[1] ;
		InputStream inputStream =clientDownloadingSocket.getInputStream() ;
		Files.copy(inputStream, Paths.get(path+fileName)) ;
		System.out.println("File '"+fileName+"' downloaded to "+path);
		return true ;
	}
	
	public static void showAvailableFiles(List<String[]> serverFileList)
	{
		System.out.println("Available files :");
		for (int i = 0; i < serverFileList.size(); i++)
		{
			System.out.println(i+" : "+serverFileList.get(i)[0]+" : "+serverFileList.get(i)[1]);
		}
	}
	
}
