import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;

public class ClientHandler implements Runnable {

	private String name;
	private Socket client;
	private PrintWriter mOut;
	private OnMessageReceived messageListener;
	private Socket server_out_socket;
	Object obj;
	final BlockingQueue<Question> queue;
	BlockingQueue<Client> clients;
	HashMap<String, String> ports = new HashMap<>();
	HashMap<String, String> ips = new HashMap<>();

	public ClientHandler(Socket socket, OnMessageReceived messageListener,
			String name, Object obj, BlockingQueue<Question> queue,
			BlockingQueue<Client> clients) {
		this.client = socket;
		this.messageListener = messageListener;
		this.name = name;
		this.obj = obj;
		this.queue = queue;
		this.clients = clients;
	}

	public OnMessageReceived getMessageListener() {
		return messageListener;
	}

	/**
	 * Method to send the messages from server to client
	 * 
	 * @param message
	 *            the message sent by the server
	 */
	public void sendMessage(String message) {
		if (mOut != null && !mOut.checkError()) {
			mOut.println(message);
			mOut.flush();
		}
	}

	private InetAddress getClientAddr() {
		return client.getInetAddress();
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		try {
			// sends the message to the client
			clients.offer(new Client(this.name, client));
			Thread.sleep(100);
			mOut = new PrintWriter(new BufferedWriter(new OutputStreamWriter(
					client.getOutputStream())), true);

			synchronized (obj) {
				System.out.println(name + " waiting");
				obj.wait();
				System.out.println(name + " done waiting");
			}
			for (Client e : clients) {
				if (e.getName() == name) {
					sendMessage(e.getName() + client.toString());
					ips.put(e.getName(),
							client.toString().substring(
									client.toString().indexOf("/") + 1,
									client.toString().indexOf(",")));
					ports.put(
							e.getName(),
							client.toString().substring(
									client.toString().indexOf("t=") + 2,
									client.toString().lastIndexOf(",")));
				}
			}
			sendMessage(queue.peek().getQuestion());

			// read the message received from client
			BufferedReader in = new BufferedReader(new InputStreamReader(
					client.getInputStream()));
			String message = in.readLine();

			if (message != null && messageListener != null) {
				// call the method messageReceived from ServerBoard class
				messageListener.messageReceived(name + ": " + message);
				if (message.equals(queue.peek().getAnswer())) {
					sendMessage("Correct");
				} else
					sendMessage("Incorrect");
			}

			// in this while we wait to receive messages from client (it's an
			// infinite loop)
			// this while it's like a listener for messages
			while (true) {

				message = in.readLine();
				if (message.contains("getpictures")) {
					// append n pictures
					sendMessage("GET_PICTURES" + TCPServer.PicturesNumber);

					// send the n pictures
					for (int i = 0; i < TCPServer.PicturesNumber; i++) {
						if (i >= 9) {
							File myFile = new File("image" + (i + 1) + ".png");
							byte[] mybytearray = new byte[(int) myFile.length()];
							sendMessage("sendimage " + "image" + (i + 1)
									+ ".png" + mybytearray.length);
						} else {
							File myFile = new File("image0" + (i + 1) + ".png");
							byte[] mybytearray = new byte[(int) myFile.length()];
							sendMessage("sendimage " + "image0" + (i + 1)
									+ ".png" + mybytearray.length);
						}
					}
				} else if (message != null && messageListener != null) {
					System.out.println("ceva" + message);
					// call the method messageReceived from ServerBoard class
					messageListener.messageReceived(name + ": " + message);
				}

			}

		} catch (IOException | InterruptedException e) {
			e.printStackTrace();

		} finally {
			try {
				client.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("S: Done.");
		}

	}

}
