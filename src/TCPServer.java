import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import javax.swing.JFrame;

/**
 * The class extends the Thread class so we can receive and send messages at the
 * same time
 */
public class TCPServer extends Thread implements Runnable {

	public static final int SERVERPORT = 4444;
	private boolean running = false;
	private PrintWriter mOut;
	private OnMessageReceived messageListener;
	public static final int PicturesNumber = 18;
	private int i = 0;
	private int counter = 0;
	Object lock = new Object();
	final BlockingQueue<Question> queue = new LinkedBlockingQueue<Question>();
	BlockingQueue<Client> clients = new LinkedBlockingQueue<Client>();

	public static void main(String[] args) {
		// opens the window where the messages will be received and sent
		ServerBoard frame = new ServerBoard();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.pack();
		frame.setVisible(true);
	}

	/**
	 * Constructor of the class
	 * 
	 * @param messageListener
	 *            listens for the messages
	 */
	public TCPServer(OnMessageReceived messageListener) {
		this.messageListener = messageListener;
	}

	/**
	 * Method to send the messages from server to client
	 * 
	 * @param message
	 *            the message sent by the server
	 * @throws IOException
	 */

	public void sendMessage(String message) throws IOException {
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

			// create a server socket. A server socket waits for requests to
			// come in over the network.
			// create client socket... the method accept() listens for a
			// connection to be made to this socket and accepts it.
			ServerSocket serverSocket = new ServerSocket(SERVERPORT);

			for (counter = 0; counter < 2; counter++) {
				Socket client = serverSocket.accept();
				new Thread(new ClientHandler(client, messageListener, "client_"
						+ i++, lock, queue, clients)).start();
			}

			Thread.sleep(1000);
			System.out.println("NOTIFY");

			setMessage(new Question("Login Name", "Eu"));
			synchronized (lock) {
				lock.notifyAll();
			}

		} catch (Exception e) {
			System.out.println("S: Error");
			e.printStackTrace();
		}

	}

	private void setMessage(Question q) {
		this.queue.offer(q);
	}
}