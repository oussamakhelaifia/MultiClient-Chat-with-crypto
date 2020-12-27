
package chat;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

/**
 * Client class represents the client which handles the user application
 * @param  client_gui - the gui of the client.
 * @param sInput - represents the input object message from the socket.
* @param sOutput - represents the output object message from the socket.
*/

public class Client {
    
    
        private ObjectInputStream sInput;		
	private ObjectOutputStream sOutput;		
	private Socket socket;

	private ClientGUI client_gui;
	
	private String server_ip, username;
	private int port;

    
    
        /**
         * constructor - initialize the variables.
         */
        Client(String server_ip, int port, String username, ClientGUI client_gui) {
		this.server_ip = server_ip;
		this.port = port;
		this.username = username;
		this.client_gui = client_gui;
	}
    
        
        /**
         * start function make a connection to the server through the socket
         * and start a thread which listens to the server while the connection is open.
         * @return boolean type to inform if success
         */
    public boolean start() {
		try {
			socket = new Socket(server_ip, port);
		} 
		catch(Exception ec) {
			display("Error connectiong to server :" + ec);
			return false;
		}
		
		String msg = "Connection accepted " + socket.getInetAddress() + " : " + socket.getPort();
		display(msg);
	
                
		try
		{
			sInput  = new ObjectInputStream(socket.getInputStream());
			sOutput = new ObjectOutputStream(socket.getOutputStream());
		}
		catch (IOException eIO) {
			display("Exception creating new Input/output Streams: " + eIO);
			return false;
		}


		new ListenFromServer().start();

		try
		{
			sOutput.writeObject(username); 
		}
		catch (IOException eIO) {
			display("Exception doing login : " + eIO);
			disconnect();
			return false;
		}
		return true;
	}
    
    
    /**
     * display function send and object to the client gui
     * @param msg - the body of the message
     */
    private void display(String msg) {
			client_gui.append(" "+msg + "\n");		
	}
    
    
    /**
     * sendMessage function send a message object to the socket
     * @param msg - the object which represents the message
     */
    void sendMessage(Message msg) {
		try {
			sOutput.writeObject(msg);
		}
		catch(IOException e) {
			display("Exception writing to server: " + e);
		}
	}

    
    /**
     * disconnect function closes all the socket in/out streams.
     */
    private void disconnect() {
		try { 
			if(sInput != null) sInput.close();
		}
		catch(Exception e) {} 
		try {
			if(sOutput != null) sOutput.close();
		}
		catch(Exception e) {} 
               try{
			if(socket != null) socket.close();
		}
		catch(Exception e) {} 
	
			
	}
    
    
    /**
     * ListenFromServer class is an inner class type of thread which listens to the sockets streams
     * in infinite loop until the client closes the connection.
     */
    	class ListenFromServer extends Thread {

		public void run() {
			while(true) {
				try {
					String msg = (String) sInput.readObject();
					
				
						client_gui.append(msg);
				}
				catch(IOException e) {
					display("Server has close the connection: " + e);
					if(client_gui != null) client_gui.connectionFailed();
					break; 
				}
				catch(ClassNotFoundException e2) {
				}
			}
		}
	}
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
}
