
package chat;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.StringTokenizer;

/**
 * Class Server represents the host which handles the users
 * of the chat application.
 * the server process makes a connection listens on sepcific port over the incoming stream of the server socket. 
 * 
 * @param uniqueId - for the client id. clients - list of the users application.
 * @param server_gui - represents the GUI.
 */

public class Server {
    
	private static int uniqueId; 
	private ArrayList<ClientThread> clients; 
	private ServerGUI server_gui; 
	private SimpleDateFormat date; 
        private static final int port = 7777; 
	private boolean isRunning; 
    
    
    
    /**
     * constuctor - initialize the variables
     * @param server_gui 
     */
    public Server(ServerGUI server_gui) {
		this.server_gui = server_gui;
		date = new SimpleDateFormat("HH:mm:ss");
		clients = new ArrayList<ClientThread>();
	}
    
    /**
    start function- the server listens to a defined port by the server socket to the clients
    and make them communicate, and close the connections.  
    */
    public void start() {
		isRunning = true;
		try 
		{
			ServerSocket serverSocket = new ServerSocket(port);

			while(isRunning) 
			{
				display("Server waiting for Clients on port " + port + ".");
				
				Socket socket = serverSocket.accept();  	
				if(!isRunning) break;
				ClientThread client = new ClientThread(socket);  
				clients.add(client);									// save it in the ArrayList
				client.start();
			}
			try {
				serverSocket.close();
				for(int i = 0; i < clients.size(); ++i) {
					ClientThread client = clients.get(i);
					try {
                                     
					client.sInput.close();
					client.sOutput.close();
					client.socket.close();
					}
					catch(IOException ioE) {
					}
				}
			}
			catch(Exception e) {
				display("Exception closing the server and clients: " + e);
			}
		}
		catch (IOException e) {
            String msg = date.format(new Date()) + " Exception on new ServerSocket: " + e + "\n";
			display(msg);
		}
	}		
    
    /**
     * stop function make the server to stop to close the all connections
     */
    protected void stop() {
		isRunning = false;

                
		try {
		new Socket("localhost", port);
		}
		catch(Exception e) {
			
		}
                   
	}
    

   
    
    /**
     * display function display the events to the logger
     * @param msg the body of the event
     */
    	private void display(String msg) { 
		String time = date.format(new Date()) + " | " + msg;
		server_gui.appendEvent(time + "\n");
	}
    
    
    /**
     * broadcast function broadcast a message to all the users and checks
     * if a user was logged out.
     * @param message the body of the message
     */
    private synchronized void broadcast(String message) {
		String time = date.format(new Date());
		String messageLf = time + " | " + message + "\n";
			server_gui.appendRoom(messageLf);     
		

		for(int i = clients.size(); --i >= 0;) {
			ClientThread temp = clients.get(i);
			if(!temp.writeMsg(messageLf)) {
				clients.remove(i);
				display("Disconnected Client " + temp.username + " removed from list.");
			}
		}
	}
    
    /**
     * remove function removes a client by his specific id
     * @param id 
     */
    	synchronized void remove(int id) {
		for(int i = 0; i < clients.size(); ++i) {
			ClientThread temp = clients.get(i);
			// found it
			if(temp.id == id) {
				clients.remove(i);
				return;
			}
		}
	}
    
    
        /**
         *  class ClientThread is a inner class type of thread
         * which makes the server to handle each client by a unique id
         * @param  socket - represents the client socket. 
         * @param sInput - represents the input object message from the socket.
         * @param sOutput - represents the output object message from the socket.
         * @param msg - represents the object.
         */
        class ClientThread extends Thread {
		Socket socket;
		ObjectInputStream sInput;
		ObjectOutputStream sOutput;
		int id;
		String username;
		Message msg;
		String date;

		
                /**
                 * constructor
                 * @param socket 
                 */
		ClientThread(Socket socket) {
			id = ++uniqueId;
			this.socket = socket;
			try
			{
				sOutput = new ObjectOutputStream(socket.getOutputStream());
				sInput  = new ObjectInputStream(socket.getInputStream());
				username = (String) sInput.readObject();
				display(username + " just connected.");
                                broadcast(username + " just connected.");
			}
			catch (IOException e) {
				display("Exception creating new Input/output Streams: " + e);
				return;
			}
			catch (ClassNotFoundException e) {
			}
            date = new Date().toString() + "\n";
		}
        
        
        /**
         * run function is the thread function which listens
         * to the clients input messages and checks whether type it is and make a decision.
         */
        public void run() {
			boolean isRunning = true;
			while(isRunning) {
				try {
					msg = (Message) sInput.readObject();
				}
				catch (IOException e) {
					display(username + " Exception reading Streams: " + e);
					break;				
				}
				catch(ClassNotFoundException e2) {
					break;
				}
				String message = msg.getMessage();

				switch(msg.getType()) {

                                   
				case Message.PUB_MESSAGE:
					broadcast(username + " : " + message);
					break;
                                        
                                       
                                case Message.PVT_MESSAGE: 
                                    StringTokenizer st = new StringTokenizer(message,"$"); // split the incoming message to two parts for convenient 
                                    String user_dest = st.nextToken(); 
                                    message = st.nextToken(); 
                                    boolean ans = false;
                                    
                                    for(int i = 0; i < clients.size(); ++i) {
						ClientThread ct = clients.get(i);
                                                if (ct.username.equals(user_dest)){ 
                                                    ct.writeMsg("<pvt> "+this.username+": " + message+ "\n");
                                                    this.writeMsg("<pvt> "+this.username+": " + message+ "\n");
                                                    ans = true;
                                                    break; 
                                                   }   
                                    }
                                    
                                    if(ans == false) this.writeMsg("no such user \n"); 
                                    break;   
                                        
				case Message.LOGOUT:
					display(username + " disconnected with a LOGOUT message.");
                                        broadcast(username + " disconnected with a LOGOUT message.");
					isRunning = false;
					break;
				case Message.WHOISIN:
					writeMsg("List of the users connected at " + "\n");
					for(int i = 0; i < clients.size(); ++i) {
						ClientThread client = clients.get(i);
						writeMsg((i+1) + ") " + client.username + " since " + client.date);
					}
					break;
				}
			}
			remove(id);
			close();
		}
        
        
        /**
         * close function closes the communication by the socket
         */
        private void close() {
			try {
				if(sOutput != null) sOutput.close();
			}
			catch(Exception e) {}
			try {
				if(sInput != null) sInput.close();
			}
			catch(Exception e) {};
			try {
				if(socket != null) socket.close();
			}
			catch (Exception e) {}
		}

        
        /**
         * writeMsg function sends an object over the socket
         * @param msg - the body of the message
         * @return 
         */
        private boolean writeMsg(String msg) {
			if(!socket.isConnected()) {
				close();
				return false;
			}
			try {
				sOutput.writeObject(msg);
			}
			catch(IOException e) {
				display("Error sending message to " + username);
				display(e.toString());
			}
			return true;
		}
	}

}
