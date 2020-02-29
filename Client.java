import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.*;
import java.util.*;
class Client{
	//buffer to store imcomplete commands
	final ByteBuffer buffer;
	String username;
    String chatroom;
    STATE state;
    
	public Client(){
		buffer = ByteBuffer.allocate( 1000 );
		username = null;
		chatroom = null;
		state = STATE.INIT;
	}
}
