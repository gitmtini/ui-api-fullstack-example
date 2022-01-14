package socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import org.java_websocket.WebSocket;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_10;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import com.sun.jersey.spi.container.ContainerProvider;

import sawtooth.sdk.processor.Utils;

public class TestSockets {
	String hashedNS;
	String familyName = "eatms";
	@Before
	public void initial() throws UnsupportedEncodingException{
		String familyName = "eatms";
		hashedNS = Utils.hash512(familyName.getBytes("UTF-8")).substring(0, 6);

	}
	

	@Test
	public void testSubscription() throws IOException{
		
		Socket socket = new Socket();
		
		socket.bind(InetSocketAddress.createUnresolved("127.0.0.1", 8008));
		
		InputStream stream = socket.getInputStream();
		InputStreamReader crunchifyReader = new InputStreamReader(stream);
		BufferedReader br = new BufferedReader(crunchifyReader);
		
		System.out.println(br.readLine());
	}
	
	@Test
	public void testSubscription2() throws URISyntaxException, JSONException, MalformedURLException{
		//URI uri = URI.create("ws://localhost:8080/api/v1/events/");
		URI uri = new URI("ws://localhost:8008/subscriptions");	
		System.out.println(uri.getHost());
		System.out.println(uri.getPort());
		System.out.println(uri.getPath());
		System.out.println(uri.getRawPath());
		
        JSONObject msg = new JSONObject()
        		.put("action", "subscribe")
        		.put("address_prefixes", hashedNS);
		
		WebSocketClient mWebSocketClient = new WebSocketClient(uri) {
		    @Override
		    public void onOpen(ServerHandshake serverHandshake) {
		    	System.out.println("Openned connection!");
		    }

		    @Override
		    public void onMessage(String s) {
		    	System.out.print("Recieved message: ");
		    	System.out.println(s);
		    }

		    @Override
		    public void onClose(int i, String s, boolean b) {
		       // Log.i("Websocket", "Closed " + s);
		    	System.out.println("Websocket closed -> i="+i+" s='"+s+"' b ="+b+"");

		    }

		    @Override
		    public void onError(Exception e) {
		       // Log.i("Websocket", "Error " + e.getMessage());
		    	e.printStackTrace();
		    	System.out.println("Websocket Error -> " + e.getMessage());
		    }

		};
		
	

		    
		 
	        
	       try {
	    	   
			mWebSocketClient.connectBlocking();
		} catch (InterruptedException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
	       
	    mWebSocketClient.send(msg.toString());

/*
		WebSocket socket = mWebSocketClient.getConnection();
		
		while(socket.isConnecting()){
			try {
				Thread.sleep(100000);
			} catch (InterruptedException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			System.out.println("Connecting ....");
		}
		
		if(socket.isOpen()){
			socket.send(msg.toString());
		}*/
		

		
	}
	



}
