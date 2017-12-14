/*
  633-2 project Oliv&Jon - Server - Server.java
  Author : Jonathan Schnyder
  Created : 1 déc. 2017
 */

package server;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class Server
{
	public static void main(String[] args)
	{
		List fileList = new ArrayList<String[]>();
		String serverName = "192.168.108.10" ;
		Socket clientSocket = null ;
		ServerSocket serverSocket ;
		ObjectInputStream inputStream ;
		ObjectOutputStream outputStream ;
		int port = 50000 ;


		try {
			InetAddress serverAddress = InetAddress.getByName(serverName) ;
			serverSocket = new ServerSocket(port, 10, serverAddress) ;
			clientSocket = serverSocket.accept() ;
			InetAddress clientAddress = clientSocket.getInetAddress();
			String clientName = clientAddress.getHostAddress() ;
			System.out.println("Client "+clientName+" has connected");
			List<String> clientFiles ;
			inputStream = new ObjectInputStream(clientSocket.getInputStream()) ;
			clientFiles = (ArrayList<String>)inputStream.readObject();
			for (String s : clientFiles)
			{
				String fileName = s ;
				System.out.println(clientName+" : "+fileName) ;
				String[] fileInfo = {clientName,fileName} ;
				fileList.add(fileInfo) ;
			}
			outputStream = new ObjectOutputStream(clientSocket.getOutputStream()) ;
			outputStream.writeObject(fileList);
			outputStream.flush();
			clientSocket.close();

		} catch (Exception e) {
			e.printStackTrace();
		}





	}
}
