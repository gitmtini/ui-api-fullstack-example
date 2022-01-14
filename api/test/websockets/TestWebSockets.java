package websockets;

import java.io.IOException;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

import javax.websocket.ClientEndpoint;
import javax.websocket.ClientEndpointConfig;
import javax.websocket.ContainerProvider;
import javax.websocket.DeploymentException;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.HandshakeResponse;
import javax.websocket.MessageHandler;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.WebSocketContainer;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpointConfig;
import javax.websocket.ClientEndpointConfig.Configurator;

import org.eclipse.jetty.util.component.LifeCycle;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.websocket.client.WebSocketClient;
import org.eclipse.jetty.websocket.jsr356.ClientContainer;
import org.junit.Test;

import io.mtini.sockets.EATSocket;
import io.mtini.sockets.TenantAndEstateSocket;


public class TestWebSockets {

	static String apikey = "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiJiZjExNDM2OGNlMTQ4OTZjZmMwODI5YTk4MzgxNTJjMTU5MDk5Mjc4MjQ3ZmZiZDBlMDRiNzUzZjQxZmMyYjVmIiwiaWF0IjoxNTQ0NzQzNzQ2LCJzdWIiOiIxMDAgZGF5IEpXVCB0b2tlbiIsImlzcyI6ImFraWxpIiwiZXhwIjoxNTUzMzgwMTQ2fQ.sYavHw6wfugNKaAOfoGyUefZX7Ai77ROMew0H5AKvIM";
	
	/*
	@Test
	public void testSocket(){
		final WebSocketContainer container = ContainerProvider.getWebSocketContainer();    
        final String uri = "ws://localhost:8080/api/v1/events";  
  
        try( Session session = container.connectToServer( BroadcastClientEndpoint.class, URI.create( uri ) ) ) {
            for( int i = 1; i <= 10; ++i ) {
                session.getBasicRemote().sendObject( new Message( client, "Message #" + i ) );
                Thread.sleep( 1000 );
            }
        }
  
        // Application doesn't exit if container's threads are still running
        ( ( ClientContainer )container ).stop();
	}
	*/
	
	@Test
	public void testSocket4(){
	       URI uri = URI.create("ws://localhost:8080/events/ws-events");

	        WebSocketClient client = new WebSocketClient();
	        try
	        {
	            try
	            {
	                client.start();
	                // The socket that receives events
	                EATSocket socket = new EATSocket();
	                // Attempt Connect
	                Future<org.eclipse.jetty.websocket.api.Session> fut = client.connect(socket,uri);
	                // Wait for Connect
	                org.eclipse.jetty.websocket.api.Session session = fut.get();
	                // Send a message
	                session.getRemote().sendString("Hello");
	                // Close session
	                session.close();
	            }
	            finally
	            {
	                client.stop();
	            }
	        }
	        catch (Throwable t)
	        {
	            t.printStackTrace(System.err);
	        }
	    }
	

	@Test
	public void testSockets1() throws Exception {
		URI uri = URI.create("ws://localhost:8080/api/v1/events/");
		//URI uri = URI.create("ws://localhost:8080/events");
	    final AtomicReference message = new AtomicReference<>();
	    final CountDownLatch latch = new CountDownLatch(1);
	    Endpoint endpoint = new Endpoint() {
	        @Override
	        public void onOpen(Session session, EndpointConfig config) {
	            session.addMessageHandler(new MessageHandler.Whole() {
	    
					@Override
					public void onMessage(Object content) {
						// TODO Auto-generated method stub
						 message.set(content);
		                    latch.countDown();
					}
	            });
	        }
	    };

	    ClientEndpointConfig.Configurator configurator = new ClientEndpointConfig.Configurator() {
	        public void beforeRequest(Map headers) {
	            headers.put(HEADER_NAME, Arrays.asList(HEADER_VALUE));
	        }
	    };
	    ClientEndpointConfig authorizationConfiguration = ClientEndpointConfig.Builder.create()
	            .configurator(configurator)
	            .build();

	    Session session = ContainerProvider.getWebSocketContainer()
	            .connectToServer(
	                    endpoint, authorizationConfiguration,
	                    uri);
	    
	    latch.await(10, TimeUnit.SECONDS);
	    session.getBasicRemote().sendText("Hello");
	    session.close();

	    System.out.println(message.get());
	    //assertEquals("Hello Tomitribe", message.get());
	}
	
	
	@Test
	public void testSockets2() throws Exception{
		
		URI uri = URI.create("ws://localhost:8080/api/v1/events/");

        WebSocketContainer container = ContainerProvider.getWebSocketContainer();

        Map<String, List<String>> headers = new HashMap<String, List<String>>();
        headers.put("x-mtini-apikey", Arrays.asList(apikey));
            	
        Configurator conf = new Configurator();
        conf.beforeRequest(headers);
        
        ClientEndpointConfig endPointConf = ClientEndpointConfig.Builder.create().configurator(conf).build();//set conf = new ClientEndPointConfig();
            	//Endpoint param = EndPointParam.
          
                // Attempt Connect
                Session session = container.connectToServer(TenantAndEstateSocket.class,endPointConf,uri);
                
                // Send a message
                session.getBasicRemote().sendText("Hello");
             
                // Close session
                session.close();
            
                // Force lifecycle stop when done with container.
                // This is to free up threads and resources that the
                // JSR-356 container allocates. But unfortunately
                // the JSR-356 spec does not handle lifecycles (yet)
                if (container instanceof LifeCycle)
                {
                    ((LifeCycle)container).stop();
                }
            
    }
	
	
	@Test
	public void testSockets3() throws Exception{
		
		
		
		URI uri = URI.create("ws://localhost:8080/api/v1/events/");

		boolean trustAll = Boolean.getBoolean("org.eclipse.jetty.websocket.jsr356.ssl-trust-all");

		//WebSocketClient client = new WebSocketClient(scope, new SslContextFactory(trustAll)
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();

          
                // Attempt Connect
                Session session = container.connectToServer(MyClientEndpoint.class,uri);
                // Send a message
                session.getBasicRemote().sendText("Hello");
                
               
                // Close session
                session.close();
            
            
                // Force lifecycle stop when done with container.
                // This is to free up threads and resources that the
                // JSR-356 container allocates. But unfortunately
                // the JSR-356 spec does not handle lifecycles (yet)
                if (container instanceof LifeCycle)
                {
                    ((LifeCycle)container).stop();
                }
            


    }
	
	
	
	static String HEADER_NAME = "x-mtini-apikey";
	static String HEADER_VALUE = apikey;
	
	@ClientEndpoint(configurator = MyClientConfigurator.class)
	public static class MyClientEndpoint {
	    public static final CountDownLatch messageLatch = new CountDownLatch(1);
	    public static volatile String receivedMessage;


	    /*
	    @OnOpen
	    public void onOpen(Session session) throws IOException {
	        session.getBasicRemote();//.sendText("Some message");
	    }

	    @OnMessage
	    public void onMessage(String message) {
	        receivedMessage = message;
	        messageLatch.countDown();
	    }*/
	    
	    @OnMessage
	    public String onMessage(String message){
	    	return message;
	    }
	    
	}
	
	public static class MyClientConfigurator extends ClientEndpointConfig.Configurator {
	    static volatile boolean called = false;

	    
	    @Override
	    public void beforeRequest(Map<String, List<String>> headers) {
	        called = true;
	        headers.put(HEADER_NAME, Arrays.asList(HEADER_VALUE));
	        //headers.put("Origin", Arrays.asList("myOrigin"));
	    }

	    @Override
	    public void afterResponse(HandshakeResponse handshakeResponse) {
	        final Map<String, List<String>> headers = handshakeResponse.getHeaders();
	        System.out.println("handshakeResponse: "+handshakeResponse.toString());
	        //assertEquals(HEADER_VALUE[0], headers.get(HEADER_NAME).get(0));
	        //assertEquals(HEADER_VALUE[1], headers.get(HEADER_NAME).get(1));
	       // assertEquals(HEADER_VALUE[2], headers.get(HEADER_NAME).get(2));
	       // assertEquals("myOrigin", headers.get("origin").get(0));
	    }
	}
	
		
	}


