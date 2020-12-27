
package chat;

import java.io.Serializable;

/**
 *
 * Class Message defines the type of a message
 * that the client could send on the chat and builds a message object according to the type
 */
public class Message implements Serializable
{

protected static final long serialVersionUID = 1112122200L;

/**
 * WHOISIN - show online users list
 * PUB_MESSAGE - message for all the users
 * PVT_MESSAGE - message to specific user
 * LOGOUT - terminate the connection and close the socket
 */
static final int WHOISIN = 0, PUB_MESSAGE = 1, LOGOUT = 2, PVT_MESSAGE = 3;
private int type;
private String msg;


/**
 * Constructor
 * @param type represents the type of the message
 * @param msg represents the body of the message
 */
Message(int type, String msg) { 
		this.type = type;
		this.msg = msg;
	}


	/**
         * Getters
         * @return type
         */
        
	int getType() {
		return type;
	}
        
        /**
         * Getters
         * @return msg 
         */
	String getMessage() {
		return msg;
	}



    
}
