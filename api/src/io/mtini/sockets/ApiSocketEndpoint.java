package io.mtini.sockets;


import java.util.ArrayList;

import javax.websocket.ClientEndpoint;
import javax.websocket.CloseReason;
import javax.websocket.Endpoint;
import javax.websocket.EndpointConfig;
import javax.websocket.HandshakeResponse;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.HandshakeRequest;
import javax.websocket.server.ServerEndpoint;
import javax.websocket.server.ServerEndpointConfig;

@ClientEndpoint
@ServerEndpoint(value="/events/", configurator=ApiSocketEndpoint.CustomConfigurator.class)
public class ApiSocketEndpoint extends Endpoint{
	


    @OnOpen
    public void onWebSocketConnect(Session sess)
    {
        System.out.println("Socket Connected: " + sess);
    }
    
    @OnMessage
    public void onWebSocketText(String message)
    {
        System.out.println("Received TEXT message: " + message);
    }
    
    @OnClose
    public void onWebSocketClose(CloseReason reason)
    {
        System.out.println("Socket Closed: " + reason);
    }
    
    @OnError
    public void onWebSocketError(Throwable cause)
    {
        cause.printStackTrace(System.err);
    }

	@Override
	public void onOpen(Session paramSession, EndpointConfig paramEndpointConfig) {
		// TODO Auto-generated method stub
		 System.out.println("Socket Opened -> paramSession : " + paramSession.toString());
		 System.out.println("Socket Opened:-> paramEndpointConfig" + paramEndpointConfig.toString());
		
	}
	




public static class CustomConfigurator extends ServerEndpointConfig.Configurator{
	 //private HttpSession httpSession;
    @Override
    public void modifyHandshake(ServerEndpointConfig sec, HandshakeRequest request, HandshakeResponse response) {
        //httpSession = (HttpSession) request.getHttpSession();
        super.modifyHandshake(sec, request, response);
        response.getHeaders().put(HandshakeResponse.SEC_WEBSOCKET_ACCEPT, new ArrayList<String>());
    }
    
}

}