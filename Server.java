import java.io.*;
import java.net.*;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.*;
import java.util.*;


public class Server
{
	// A pre-allocated buffer for the received data
	static private final ByteBuffer buffer = ByteBuffer.allocate( 16384 );

	// Decoder for incoming text -- assume UTF-8
	static private final Charset charset = Charset.forName("UTF8");
	static private final CharsetDecoder decoder = charset.newDecoder();


	static public void main( String args[] ) throws Exception {
		// Parse port from command line
		int port = Integer.parseInt( args[0] );
    
		try 
		{
			// Instead of creating a ServerSocket, create a ServerSocketChannel
			ServerSocketChannel ssc = ServerSocketChannel.open();

			// Set it to non-blocking, so we can use select
			ssc.configureBlocking( false );

			// Get the Socket connected to this channel, and bind it to the
			// listening port
			ServerSocket ss = ssc.socket();
			InetSocketAddress isa = new InetSocketAddress( port );
			ss.bind( isa );

			// Create a new Selector for selecting
			Selector selector = Selector.open();

			// Register the ServerSocketChannel, so we can listen for incoming
			// connections
			ssc.register( selector, SelectionKey.OP_ACCEPT );
			System.out.println( "Listening on port "+port );

			while (true) 
			{
				// See if we've had any activity -- either an incoming connection,
				// or incoming data on an existing connection
				int num = selector.select();

				// If we don't have any activity, loop around and wait again
				if (num == 0) continue;
   

				// Get the keys corresponding to the activity that has been
				// detected, and process them one by one
				Set<SelectionKey> keys = selector.selectedKeys();
				Iterator<SelectionKey> it = keys.iterator();
				while (it.hasNext()) 
				{
					// Get a key representing one of bits of I/O activity
					SelectionKey key = it.next();

					// What kind of activity is it?
					if ((key.readyOps() & SelectionKey.OP_ACCEPT) == SelectionKey.OP_ACCEPT) 
					{

						// It's an incoming connection.  Register this socket with
						// the Selector so we can listen for input on it
						Socket s = ss.accept();
						System.out.println( "Got connection from "+s );

						// Make sure to make it non-blocking, so we can use a selector
						// on it.
						SocketChannel sc = s.getChannel();
						sc.configureBlocking( false );

						// Register it with the selector, for reading
						sc.register( selector, SelectionKey.OP_READ );

					} 

					else if ((key.readyOps() & SelectionKey.OP_READ) == SelectionKey.OP_READ) 
					{

						SocketChannel sc = null;

						try 
						{

							// It's incoming data on a connection -- process it
							sc = (SocketChannel)key.channel();
							boolean ok = processInput( sc , selector , key );

							// If the connection is dead, remove it from the selector
							// and close it
							if (!ok) 
							{
								key.cancel();

				                Socket s = null;
								try 
								{
									s = sc.socket();
									System.out.println( "Closing connection to "+s );
									s.close();
								} 
								catch( IOException ie ) 
								{
									System.err.println( "Error closing socket "+s+": "+ie );
								}
							}

			            } 
						catch( IOException ie ) 
						{

							// On exception, remove this channel from the selector
							key.cancel();

							try 
							{
								sc.close();
							} catch( IOException ie2 ) { System.out.println( ie2 ); }

							System.out.println( "Closed "+sc );
						}
					}
				}

				// We remove the selected keys, because we've dealt with them.
				keys.clear();
			}
		} 
		catch( IOException ie ) 
		{
			System.err.println( ie );
		}
	}

	// Just read the message from the socket and send it to stdout
	static private boolean processInput( SocketChannel sc, Selector selector , SelectionKey inputkey) throws IOException {
		Client currentClient;

		// Read the message to the buffer
		buffer.clear();
		sc.read( buffer );
		buffer.flip();

		// If no data, close the connection
		if (buffer.limit()==0)	return false;
    
		// attach class with username and current chat room
		if (inputkey.attachment()==null)
		{
			// client stores buffer,username,chatroom,state
			// individual buffer to store partial commands
			inputkey.attach(new Client());
		}

		//just a pointer for more readable code
		ByteBuffer clientBuffer = ((Client)inputkey.attachment()).buffer;
		clientBuffer.flip();
		currentClient = (Client)inputkey.attachment();

	    //new input
		String input = decoder.decode(buffer).toString();
		buffer.flip();

	    //partial command left before
		String partial = decoder.decode(clientBuffer).toString();
		//partial command with the new input
		partial = partial + input;
	
	    //split into diferent commands separated by /n(ENTER)
		String[] command_list = partial.split("(?<=\n)");
		//run once for every command
		clientBuffer.clear();

		for (int i = 0;i<command_list.length;i++)
		{
			//obtain last char of first command
			String command = command_list[i];
			char c = command.charAt(command.length()-1);

			//if different than \n then save the incomplete command
			if (c!='\n')
			{
				for (int j = i;j<command_list.length;j++)  clientBuffer.put(command_list[j].getBytes());
				break;
			}

			String return_message=null;

			switch(command_list[i].charAt(0))
			{
				case '/': 
				{
					//delete the first '/'
					StringBuilder sb = new StringBuilder(command_list[i]);
					sb.deleteCharAt(0);
					command_list[i]=sb.toString();
					switch(command_list[i].charAt(0)){
						case '/':
						{
						    return_message = Commands.message("MESSAGE " + currentClient.username + " " +command_list[i], inputkey, selector,buffer);
						    break;
						}
						default:
						{
						    if (command_list[command_list.length-1].length()<=1){
							return_message="ERROR\n";
							break;
						    }
							//very ineficient way to remove the '\n' from the last argument
							if (command_list.length!=0)	command_list[command_list.length-1] = command_list[command_list.length-1].split("\n")[0];
					
							//obtain fist word(the command)
							command = Commands.parse(command_list[i]);
							String[] args = command_list[i].split(" ");

							//remove the command, to obtain just the arguments
							args = AuxArray.remove(args,0);

			    
							switch(command)
							{
								case "nick":
								{
									return_message = Commands.nick(args, inputkey, selector, buffer);
									break;
								}
								case "join":
								{
									return_message = Commands.join(args, inputkey, selector, buffer);
									break;
								}
								case "leave":
								{
									return_message = Commands.leave(args, inputkey, selector, buffer);
									break;
								}
								case "bye":
								{

									return_message = Commands.bye(args, inputkey, selector, buffer);
									break;
						
								}
								case "priv":
								{
									return_message = Commands.priv(args, inputkey, selector, buffer);
									break;
								}
								default:
								{
									return_message = "ERROR\n";
									break;
								}
							}
							break;
						}
					}
					break;
				}
				//if it doesnt start with '/' then it is a message
				default: 
				{
					return_message = Commands.message("MESSAGE " + currentClient.username + " " + command_list[i], inputkey, selector, buffer);
					break;
				}
			}
			//clear buffer and return the message (OK,ERROR,etc)
			buffer.clear();
			buffer.put(return_message.getBytes());
			buffer.flip();

			while(buffer.hasRemaining())	sc.write( buffer );
			
			//go back and do the same for the other commands
		}
		return true;
	}
}



