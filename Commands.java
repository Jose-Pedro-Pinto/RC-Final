import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.*;
import java.util.*;
public class Commands{

	public static String parse(String string){
		String command = string.split(" ")[0];
		return command;
	}

	public static String message(String message, SelectionKey inputkey, Selector selector ,ByteBuffer buffer){
		Client currentClient, otherClient;
		currentClient = (Client)inputkey.attachment();
		if(currentClient.state != STATE.INSIDE) return "ERROR\n";
		try
		{
			buffer.clear();
			buffer.put(message.getBytes());
			buffer.flip();

			Set<SelectionKey> keys = selector.keys();
			Iterator<SelectionKey> it = keys.iterator();
			while(it.hasNext())
			{
				SelectionKey key = it.next();
				otherClient = (Client)key.attachment();

				if(otherClient != null && !currentClient.username.equals(otherClient.username))
				{
					if(otherClient.chatroom.equals(currentClient.chatroom))
					{
						SocketChannel newsc = (SocketChannel)key.channel();
						while(buffer.hasRemaining())	newsc.write(buffer);
						buffer.rewind();
					}
				}
			}
			return message;
		}
		catch(Exception e)
		{
			return "ERROR\n";
		}
	}

	public static String nick(String[] args, SelectionKey inputkey, Selector selector, ByteBuffer buffer){
		if (args.length!=1) return "ERROR\n";

		Client currentClient;
		currentClient = (Client)inputkey.attachment();
      
		//client already has that username
		//remove this if giving the same username is suposed to return error (perguntar ao prof)
		String username=currentClient.username;
		if (username!=null){
			if (username.equals(args[0]))
			return "OK\n";
		}
      
		SocketChannel sc = Find.client(args[0],selector);
		if (sc!=null) return "ERROR\n";
      
		currentClient.username = args[0];
      
		if (currentClient.state == STATE.INIT){
			currentClient.state = STATE.OUTSIDE;
		}
      
		//send message to ppl on the chatroom
		else if (currentClient.state == STATE.INSIDE){
			String spread_message = "NEWNICK " + username + " " + args[0]+"\n";
			message( spread_message, inputkey, selector, buffer);
		}
      
	return "OK\n";
	}

	public static String join(String[] args, SelectionKey inputkey, Selector selector, ByteBuffer buffer){
		if(args.length != 1) return "ERROR\n";

		Client currentClient;
		currentClient = (Client)inputkey.attachment();
		String chatroom = currentClient.chatroom, spread_message = "LEFT " + currentClient.username + "\n";

		//client already in that chatroom
		//remove this if giving the same chatroom is suposed to return error (perguntar ao prof)
		if (chatroom != null)
		{
			if (chatroom.equals(args[0]))	return "OK\n";
		}
	
		if(currentClient.state == STATE.INIT) return "ERROR\n";

		else if (currentClient.state == STATE.INSIDE)	//send message to ppl on the chatroom
		{
			message( spread_message, inputkey, selector, buffer);
		}
		else currentClient.state = STATE.INSIDE;

		currentClient.chatroom = args[0];
		spread_message = "JOINED " + currentClient.username + "\n";
		message( spread_message, inputkey, selector, buffer);
		return "OK\n";
	}

	public static String leave(String[] args, SelectionKey inputkey, Selector selector, ByteBuffer buffer){
		if(args.length != 0)	return "ERROR\n"; 

		Client currentClient;
		currentClient = (Client)inputkey.attachment();

		if(currentClient.state != STATE.INSIDE)	return "ERROR\n";
		message( "LEFT " + currentClient.username + "\n", inputkey, selector, buffer);
		currentClient.chatroom = null;
		currentClient.state = STATE.OUTSIDE;
		return "OK\n";
	}

	public static String bye(String[] args, SelectionKey inputkey, Selector selector, ByteBuffer buffer){
		if(args.length != 0) return "ERROR\n";
		
		Client currentClient;
		currentClient = (Client)inputkey.attachment();
		
		if(currentClient.state == STATE.INSIDE)	leave( new String[0], inputkey, selector, buffer);
		inputkey.attach(null);
		return "BYE\n";
	}
	public static String priv(String[] priv_message, SelectionKey inputkey, Selector selector ,ByteBuffer buffer){
		if(priv_message.length >= 2)
		{
			Client currentClient, otherClient;
			currentClient = (Client)inputkey.attachment();
			String sent_message = "PRIVATE " + currentClient.username + " ";
			int index;

			for(index = 1; index < priv_message.length; index++) sent_message += priv_message[index] + " ";
			sent_message += "\n";
			if(currentClient.state == STATE.INIT) return "ERROR\n";
			try
			{
				buffer.clear();
				buffer.put(sent_message.getBytes());
				buffer.flip();
				SocketChannel newsc = Find.client(priv_message[0], selector);
				while(buffer.hasRemaining())	newsc.write(buffer);
				buffer.rewind();
				return "OK\n";				
			}
			catch(Exception e)
			{
				return "ERROR\n";
			}
		}
		return "ERROR\n";
	}

}

