import javax.swing.*;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.*;
public class ServerBoard extends JFrame {
    private JTextArea messagesArea;
    private JButton sendButton;
    private JTextField message;
    private JButton startServer;
    private TCPServer mServer;
 
    public ServerBoard() {
 
        super("ServerBoard");
 
        JPanel panelFields = new JPanel();
        panelFields.setLayout(new BoxLayout(panelFields,BoxLayout.X_AXIS));
 
        JPanel panelFields2 = new JPanel();
        panelFields2.setLayout(new BoxLayout(panelFields2,BoxLayout.X_AXIS));
 
        //here we will have the text messages screen
        messagesArea = new JTextArea();
        messagesArea.setColumns(30);
        messagesArea.setRows(10);
        messagesArea.setEditable(false);
 
        sendButton = new JButton("Send");
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // get the message from the text view
                String messageText = message.getText();
                // add message to the message area
                messagesArea.append("\n" + messageText);
                // send the message to the client
                try {
					mServer.sendMessage(messageText);
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
                // clear text
                message.setText("");
            }
        });
 
        startServer = new JButton("Start");
        startServer.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // disable the start button
                startServer.setEnabled(false);
 
                //creates the object OnMessageReceived asked by the TCPServer constructor
                mServer = new TCPServer(new OnMessageReceived() {
                    @Override
                    //this method declared in the interface from TCPServer class is implemented here
                    //this method is actually a callback method, because it will run every time when it will be called from
                    //TCPServer class (at while)
                    public void messageReceived(String message) {
                    	if(message.startsWith("request")){
                    		try {
								mServer.sendMessage("reply " + mServer.adreseIP.get(message.substring(8)) + " " + mServer.ports.get(message.substring(8)));
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
                    	}
                    	if(message.startsWith("init")){
                    		mServer.adreseIP.put(message.substring(message.lastIndexOf(" ") + 1), message.substring(message.indexOf(" ") + 1,message.indexOf(" ", 5)));
                    		mServer.ports.put(message.substring(message.lastIndexOf(" ") + 1), Integer.parseInt(message.substring(message.indexOf(" ", 5) + 1,message.lastIndexOf(" "))));
                    		
                    		System.out.println(message.substring(message.lastIndexOf(" ") + 1));
                    		System.out.println( message.substring(message.indexOf(" ") + 1,message.indexOf(" ", 5)));
                    		System.out.println( Integer.parseInt(message.substring(message.indexOf(" ", 5) + 1,message.lastIndexOf(" "))));
                    		
                    		try {
								mServer.sendMessage(mServer.adreseIP.get(message.substring(message.lastIndexOf(" ") + 1)) + " " + mServer.ports.get(message.substring(message.lastIndexOf(" ") + 1)));
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
                    	}
                    	
                    	if (message.startsWith("getpictures")) {
                    		try {
                    			// append n pictures
								mServer.sendMessage("GET_PICTURES" + TCPServer.PicturesNumber);
								
								// for loop
								for(int i = 0; i < TCPServer.PicturesNumber; i++){
									if(i >= 9){
										File myFile = new File ("image" + (i+1) + ".png");
										byte [] mybytearray  = new byte [(int)myFile.length()];
										mServer.sendMessage("sendimage " + "image" + (i+1) + ".png" + mybytearray.length);
									}
									else{
										File myFile = new File ("image0" + (i+1) + ".png");
										byte [] mybytearray  = new byte [(int)myFile.length()];
										mServer.sendMessage("sendimage " + "image0" + (i+1) + ".png" + mybytearray.length);
									}
								}
							} catch (IOException e) {
								e.printStackTrace();
							}
                    	}
                    	
                        messagesArea.append("\n "+message);
                    }
                });
                mServer.start();
 
            }
        });
 
        //the box where the user enters the text (EditText is called in Android)
        message = new JTextField();
        message.setSize(200, 20);
 
        //add the buttons and the text fields to the panel
        panelFields.add(messagesArea);
        panelFields.add(startServer);
 
        panelFields2.add(message);
        panelFields2.add(sendButton);
 
        getContentPane().add(panelFields);
        getContentPane().add(panelFields2);
 
        getContentPane().setLayout(new BoxLayout(getContentPane(),BoxLayout.Y_AXIS));
 
        setSize(300, 170);
        setVisible(true);
    }
}
