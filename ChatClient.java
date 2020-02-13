import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;


public class ChatClient {

    // Vari�veis relacionadas com a interface gr�fica --- * N�O MODIFICAR *
    JFrame frame = new JFrame("Chat Client");
    private JTextField chatBox = new JTextField();
    private JTextArea chatArea = new JTextArea();
    // --- Fim das vari�veis relacionadas coma interface gr�fica

    // Se for necess�rio adicionar vari�veis ao objecto ChatClient, devem
    // ser colocadas aqui
 int num_Port, State;
 String Server;
 Socket clientSocket;
 DataOutputStream outToServer;
 BufferedReader inFromServer;
    
    // M�todo a usar para acrescentar uma string � caixa de texto
    // * N�O MODIFICAR *
    public void printMessage(final String message) {
        chatArea.append(message + "\n");
    }

    
    // Construtor
    public ChatClient(String server, int port) throws IOException {
        
        // Inicializa��o da interface gr�fica --- * N�O MODIFICAR *
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
        // --- Fim da inicializa��o da interface gr�fica
 
        // Se for necess�rio adicionar c�digo de inicializa��o ao
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


    // M�todo invocado sempre que o utilizador insere uma mensagem
    // na caixa de entrada
    public void newMessage(String message) throws IOException {
  outToServer.writeBytes(message + '\n');
    }

    
    // M�todo principal do objecto
    public void run() throws IOException {
        // PREENCHER AQUI
  int index;
  String Server_Sentence;
  String[] Token;

  while(State == 0)
  {
   Token = inFromServer.readLine().split(" ");
   Server_Sentence = "";

   switch (Token[0])
   {
    case "MESSAGE":  Server_Sentence = Token[1] + ": " ;
         for(index = 2; index < Token.length; index++) Server_Sentence += Token[index] + " ";
         break;
    case "NEWNICK":  Server_Sentence = Token[1] + " mudou de nome para " + Token[2];
         break;
    default:   for(index = 0; index < Token.length; index++) Server_Sentence += Token[index] + " ";
         break;
   }
   printMessage(Server_Sentence);
  }
  
  clientSocket.close();
    }
    

    // Instancia o ChatClient e arranca-o invocando o seu m�todo run()
    // * N�O MODIFICAR *
    public static void main(String[] args) throws IOException {
        ChatClient client = new ChatClient(args[0], Integer.parseInt(args[1]));
        client.run();
    }

}