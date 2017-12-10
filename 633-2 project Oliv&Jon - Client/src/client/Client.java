/*
 * 633-2 project Oliv&Jon - Client - Client.java
 * Author : Jonathan Schnyder
 * Created : 1 déc. 2017
 */

package client;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class Client
{
	public static void main(String[] args)
	{
		String serverName = "192.168.0.15" ;
		InetAddress serverAddress ;
		int port = 50000 ;
		Socket clientSocket ;
		ObjectInputStream inputStream ;
		ObjectOutputStream outputStream ;
		List<String> clientFiles = new ArrayList<>() ;
		List<String[]> fileList = null ;
		try {
		    File directory = new File("data/") ;
			File[] files = directory.listFiles() ;
			System.out.println(files) ;
            for (File f : files) {
                clientFiles.add(f.getName()) ;
            }
			clientSocket = new Socket(InetAddress.getByName(serverName), port) ;
			outputStream = new ObjectOutputStream(clientSocket.getOutputStream()) ;
			outputStream.writeObject(clientFiles);
			outputStream.flush();
			inputStream = new ObjectInputStream(clientSocket.getInputStream()) ;
			fileList = (ArrayList<String[]>)inputStream.readObject() ;
			for (String[] s: fileList)
			{
				System.out.println(s[0]+" : "+s[1]) ;
			}
			clientSocket.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
