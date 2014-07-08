import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.JFrame;
 
/**
 * The class extends the Thread class so we can receive and send messages at the same time
 */
public class TCPServer extends Thread implements Runnable{
 
    public static final int SERVERPORT = 4444;
    public final static String FILE_TO_SEND = "bobargb8888.png";
    public final static String FILE_SEND = "image";
    private boolean running = false;
    private PrintWriter mOut;
    private OnMessageReceived messageListener;
    public static final int PicturesNumber = 18;
    public final HashMap<String, String> adreseIP = new HashMap<>();
    public HashMap<String, Integer> ports = new HashMap<>();
    ServerSocket serverSocket;
    Socket client ;
    private int i = 0;
    private int counter = 0;
    private int nr = 0;
    Object lock = new Object();
    final BlockingQueue<Question> queue = new LinkedBlockingQueue<Question>();
    BlockingQueue<Client> clients = new LinkedBlockingQueue<Client>();
    
   
    public static void main(String[] args) {
 
        //opens the window where the messages will be received and sent
        ServerBoard frame = new ServerBoard();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        
 
    }
 
    /**
     * Constructor of the class
     * @param messageListener listens for the messages
     */
    public TCPServer(OnMessageReceived messageListener) {
        this.messageListener = messageListener;
    }
 
    /**
     * Method to send the messages from server to client
     * @param message the message sent by the server
     * @throws IOException 
     */
   
    public void sendMessage(String message) throws IOException{
    	FileInputStream fis = null;
        BufferedInputStream bis = null;
        OutputStream os = null;
        
    	
      /*  if(message.startsWith("sendfile")) {
    		//  TODO send "SEND_FILE" + file.length
        	String filename = message.substring(9);
        	
    		if (mOut != null && !mOut.checkError()) {
    			File myFile = new File (filename);
    			if (!myFile.exists())
    				return;
    			
                mOut.println("SEND_FILE" + (int)myFile.length());
                mOut.flush();
            }
    		
    		try {
    			File myFile = new File (filename);
    			if (!myFile.exists())
    				System.err.println("no file, check path");

    			byte [] mybytearray  = new byte [(int)myFile.length()];
    			fis = new FileInputStream(myFile);
    			bis = new BufferedInputStream(fis);
    			bis.read(mybytearray,0,mybytearray.length);
    			os = client.getOutputStream();
    			System.out.println("Sending " + filename + "(" + mybytearray.length + " bytes)");
    			int offset = 0;
    			int remaining = mybytearray.length;
    			
    			System.out.println("Sending " + remaining + " bytes");
    			
    			while(remaining > 0)
    			{
    				int to_send = Math.min(remaining, 1024);
//    				System.out.println("remaining " + remaining);
    				os.write(mybytearray,offset,to_send);
    				offset += to_send;
    				remaining -= to_send;
//    				try {
//						Thread.sleep(50);
//					} catch (InterruptedException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
    			}
    			//for(int i= 0; i<mybytearray.length;i++){
    				//os.write(mybytearray[i]);
    			//}
    			os.flush();
    			System.out.println("Done.");
    		} catch(FileNotFoundException fnfe) { 
    			fnfe.printStackTrace();
    		} catch(IOException ioe) {
    			ioe.printStackTrace();
    		}
    		 finally {
    	          if (bis != null) bis.close();
//    	          if (os != null) os.close();
//    	          if (client!=null) client.close();
    	        }
    	}*/
         if (mOut != null && !mOut.checkError()) {
            mOut.println(message);
            mOut.flush();
        }
    }
 
    @Override
    public void run() {
        super.run();
 
        running = true;
 
        try {
            System.out.println("S: Connecting...");
            
            //create a server socket. A server socket waits for requests to come in over the network.
            
 
            //create client socket... the method accept() listens for a connection to be made to this socket and accepts it.
         /* serverSocket = new ServerSocket(SERVERPORT);
            client = serverSocket.accept();
            System.out.println("S: Receiving...");*/
            ServerSocket serverSocket = new ServerSocket(SERVERPORT);
            for(counter = 0; counter < 2; counter++) {
              	client = serverSocket.accept();
              	System.out.println("FOR LOOP");
              	ClientHandler ch = new ClientHandler(client,  messageListener, "client_" + i++, lock, queue, clients);
              	new Thread(ch).start();	
            }
            Thread.sleep(1000);
            System.out.println("NOTIFY");
             
           // setMessage(new Question("Cine esti?", "Eu"));   
            synchronized (lock) {
            	lock.notifyAll();
            }   
            
            try {
                //sends the message to the client
                mOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(client.getOutputStream())), true);
 
                //read the message received from client
                BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
                //in this while we wait to receive messages from client (it's an infinite loop)
                //this while it's like a listener for messages
                nr++;
                while (running) { 
                    String message = in.readLine();
 
                    if (message != null && messageListener != null) {
                        //call the method messageReceived from ServerBoard class
                        messageListener.messageReceived(message);
                    }
                }
 
            } catch (Exception e) {
                System.out.println("S: Error");
                e.printStackTrace();
            } finally {
                client.close();
                client.close();
                System.out.println("S: Done.");
            }
 
        } catch (Exception e) {
            System.out.println("S: Error");
            e.printStackTrace();
        }
       
    }
 
    //Declare the interface. The method messageReceived(String message) will must be implemented in the ServerBoard
    //class at on startServer button click
   /* public interface OnMessageReceived {
        public void messageReceived(String message);
    }*/
    private void setMessage(Question q) {
    	this.queue.offer(q);
    }
 
}