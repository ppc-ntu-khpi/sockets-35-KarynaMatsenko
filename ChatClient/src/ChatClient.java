import java.awt.*;
import java.awt.event.*;
import java.net.*;
import java.io.*;
import java.nio.charset.Charset;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JOptionPane;


public class ChatClient {

  private TextArea output;
  private TextField input;
  private Button sendButton;
  private Button quitButton;
  private Frame frame;
  private TextArea usernames;
  private Dialog aboutDialog;
  private Button connectBt;
  
  private Socket connection = null;
  private BufferedReader serverIn = null;
  private PrintStream serverOut = null;

  public ChatClient() {
    output = new TextArea(7,60);
    input = new TextField(40);
    sendButton = new Button("Send");
    quitButton = new Button("Quit");
    connectBt = new Button("Connect");
    usernames = new TextArea(1,20);
    
  }

  public void launchFrame() throws IOException {
    frame = new Frame("PPC Chat");

    
    frame.setLayout(new BorderLayout());
    
    frame.add(output, BorderLayout.WEST);
    frame.add(input, BorderLayout.SOUTH);

    usernames.setFont(new Font("Delicious", Font.BOLD, 14));
    sendButton.setFont(new Font("Delicious", Font.BOLD, 14));
    quitButton.setFont(new Font("Delicious", Font.BOLD, 14));
    connectBt.setFont(new Font("Delicious", Font.BOLD, 14));
    
    Panel p1 = new Panel(); 
    p1.setLayout(new GridLayout(2,1));
    p1.add(usernames);
    p1.add(connectBt);
    p1.add(sendButton);
    p1.add(quitButton);
      
    frame.add(p1, BorderLayout.CENTER);

    MenuBar mb = new MenuBar();
    Menu file = new Menu("File");
    MenuItem quitMenuItem = new MenuItem("Quit");
    quitMenuItem.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
	System.exit(0);
      }
    });
    file.add(quitMenuItem);
    mb.add(file);
    frame.setMenuBar(mb);

    
    Menu help = new Menu("Help");
    MenuItem aboutMenuItem = new MenuItem("About");
    aboutMenuItem.addActionListener(new AboutHandler());
    help.add(aboutMenuItem);
    mb.setHelpMenu(help);
    
    connectBt.addActionListener(new ActionListener() {
     public void actionPerformed(ActionEvent e) {
            String serverIP = System.getProperty("serverIP", "127.0.0.1");
            String serverPort = System.getProperty("serverPort", "2000");
            try {
                connection = new Socket(serverIP, Integer.parseInt(serverPort));
                InputStream is = connection.getInputStream();
                InputStreamReader isr = new InputStreamReader(is, Charset.forName("UTF-8"));
                serverIn = new BufferedReader(isr);
                serverOut = new PrintStream(connection.getOutputStream());    
                Thread t = new Thread(new RemoteReader());
                t.start();
            } catch (Exception ex) {
                System.err.println("Unable to connect to server!");
                ex.printStackTrace();
            }
        }
    });
    sendButton.addActionListener(new SendHandler());
    input.addActionListener(new SendHandler());
    frame.addWindowListener(new CloseHandler());
    quitButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
          System.exit(0);
        }
    });
    frame.pack();
    frame.setVisible(true);
    frame.setLocationRelativeTo(null);
  }

  private class SendHandler implements ActionListener {
    public void actionPerformed(ActionEvent e) {
       String text = input.getText();
        text = usernames.getText() + ": " + text + "\n";
        serverOut.print(text);
        input.setText("");
    }
  }

  private class CloseHandler extends WindowAdapter {
    public void windowClosing(WindowEvent e) {
      System.exit(0);
    }
  }
  
 
  private class AboutHandler implements ActionListener {
    public void actionPerformed(ActionEvent e) {
      
      JOptionPane.showMessageDialog(frame, "The ChatClient is a neat tool that allows you to talk to other ChatClients via a ChatServer");
    }
  }

  private class AboutDialog extends Dialog implements ActionListener  {
    public AboutDialog(Frame parent, String title, boolean modal) {
      super(parent,title,modal);
      add(new Label("The ChatClient is a neat tool that allows you to talk " +
                 "to other ChatClients via a ChatServer"),BorderLayout.NORTH);
      Button b = new Button("Ok");
      add(b,BorderLayout.SOUTH);
      b.addActionListener(this);
      pack();
    }
   
    public void actionPerformed(ActionEvent e) {
      setVisible(false);
    }
  }
  
  private class RemoteReader implements Runnable {
  public void run() {
    try {
      while ( true ) {
        String nextLine = serverIn.readLine();
        output.append(nextLine + "\n");
      }
    } catch (Exception e) {
        System.err.println("Error");
        e.printStackTrace();
      }
  } 
} 
  
  

  public static void main(String[] args) throws IOException {
    ChatClient c = new ChatClient();
    c.launchFrame();
  }
}