import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;



public class ChatClient {

    // Variáveis relacionadas com a interface gráfica --- * NÃO MODIFICAR *
    JFrame frame = new JFrame("Chat Client");
    private JTextField chatBox = new JTextField();
    private JTextArea chatArea = new JTextArea();
    // --- Fim das variáveis relacionadas coma interface gráfica

    // Se for necessário adicionar variáveis ao objecto ChatClient, devem
    // ser colocadas aqui
	int num_Port, State;
	String Server;
	Socket clientSocket;
	DataOutputStream outToServer;
	BufferedReader inFromServer;
    
    // Método a usar para acrescentar uma string à caixa de texto
    // * NÃO MODIFICAR *
    public void printMessage(final String message) {
        chatArea.append(message + "\n");
    }

    
    // Construtor
    public ChatClient(String server, int port) throws IOException {

        // Inicialização da interface gráfica --- * NÃO MODIFICAR *
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.add(chatBox);
        frame.setLayout(new BorderLayout());
        frame.add(panel, BorderLayout.SOUTH);
        frame.add(new JScrollPane(chatArea), BorderLayout.CENTER);
        frame.setSize(500, 300);
        frame.setVisible(true);
        chatArea.setEditable(false);
        chatBox.setEditable(true);
        chatBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try {
                    newMessage(chatBox.getText());
                } catch (IOException ex) {
                } finally {
                   chatBox.setText("");
                }
            }
        });
        // --- Fim da inicialização da interface gráfica
	
        // Se for necessário adicionar código de inicialização ao
        // construtor, deve ser colocado aqui

		num_Port = port;
		Server = server;
		State = 0;
		clientSocket = new Socket(Server, num_Port);
		outToServer =
			new DataOutputStream(clientSocket.getOutputStream());
		inFromServer =
			new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
    }


    // Método invocado sempre que o utilizador insere uma mensagem
    // na caixa de entrada
    public void newMessage(String message) throws IOException {
		if(!message.equals(""))
		{
			message = message + "\n";
			byte[] ptext = message.getBytes("UTF-8");
			outToServer.write(ptext,0, ptext.length);
			outToServer.flush();
		}
    }

    
    // Método principal do objecto
    public void run() throws IOException {
        // PREENCHER AQUI
		STATE State = STATE.RUNNING;
		int index;
		String Server_Sentence;
		String[] Token;

		while(State == STATE.RUNNING)
		{
			Token = inFromServer.readLine().split(" ");
			Server_Sentence = "";

			switch (Token[0])
			{
				case "MESSAGE":		Server_Sentence = Token[1] + ": " ;
									for(index = 2; index < Token.length; index++) Server_Sentence += Token[index] + " ";
									break;
				case "NEWNICK":		Server_Sentence = Token[1] + " mudou de nome para " + Token[2];
									break;
				case "BYE":			State = STATE.QUIT;
									Server_Sentence = "Until next time!\n";
									break;
				default:			for(index = 0; index < Token.length; index++) Server_Sentence += Token[index] + " ";
									break;
			}
			printMessage(Server_Sentence);
		}
		try
		{
			Thread.sleep(1000);
		}
		catch(Exception e)
		{
			System.out.println("Can't wait. Exiting...");
		}
		clientSocket.close();
		System.exit(0);
    }
    

    // Instancia o ChatClient e arranca-o invocando o seu método run()
    // * NÃO MODIFICAR *
    public static void main(String[] args) throws IOException {
        ChatClient client = new ChatClient(args[0], Integer.parseInt(args[1]));
        client.run();
    }

}
