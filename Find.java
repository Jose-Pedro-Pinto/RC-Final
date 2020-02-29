import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.*;
import java.util.*;
public class Find{
    public static SocketChannel client(String name, Selector selector){
	Set<SelectionKey> keys = selector.keys();
	Iterator<SelectionKey> keyIterator = keys.iterator();
	Client client;
	while(keyIterator.hasNext()) {
	    SelectionKey key = keyIterator.next();
	    client = (Client)key.attachment();
	    if (client==null) continue; //inexistent client
	    else if (client.username==null) continue; //has not set its nick yet
	    else if (client.username.equals(name)){
		return (SocketChannel)key.channel();
	    }
	}
	return null;
    }
}
