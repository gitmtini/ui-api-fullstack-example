package dao.sawtooth;

import java.io.UnsupportedEncodingException;

import org.bitcoinj.core.ECKey;
import org.bouncycastle.util.encoders.Hex;
import org.junit.Before;
import org.junit.Test;
import org.zeromq.ZMQ;

import com.google.protobuf.InvalidProtocolBufferException;
import com.prelimtek.utils.blockchain.SawtoothUtils;

import sawtooth.sdk.messaging.Future;
import sawtooth.sdk.messaging.Stream;
import sawtooth.sdk.processor.exceptions.ValidatorConnectionError;
import sawtooth.sdk.protobuf.ClientEventsSubscribeRequest;
import sawtooth.sdk.protobuf.ClientEventsSubscribeResponse;
import sawtooth.sdk.protobuf.Event;
import sawtooth.sdk.protobuf.EventFilter;
import sawtooth.sdk.protobuf.EventFilter.FilterType;
import sawtooth.sdk.protobuf.EventList;
import sawtooth.sdk.protobuf.EventSubscription;
import sawtooth.sdk.protobuf.Message;
import sawtooth.sdk.protobuf.Message.MessageType;

public class TestSawtoothZeroMQ {
	String address;
	private String subscriptionKey="address";
	private String subscriptionMatchString;
	
	@Before
	public void init() throws UnsupportedEncodingException{
	
		//calculate address
		//address = Utils.hash512(SawtoothUtils.FAMILY.getBytes()).substring(0, 6)+".";
		
		String privateKeyHex = "8aa754e6d81c5d50058df6a724bb4713b35f62242880ae7160968a4081ac6bb2";
		ECKey privateKey = ECKey.fromPrivate(Hex.decode(privateKeyHex));
		System.out.println(privateKey.getPrivateKeyAsHex());
		byte[] publicKeyBytes = privateKey.getPubKey();
		
		address = SawtoothUtils.calculateAddress(SawtoothUtils.FAMILY,publicKeyBytes);
		System.out.println(address);
		//address = "7a754a172fb0d6010d5b55f4314a8b5f7691bda99c99381bd4170593a136729d284cf8";
		subscriptionMatchString = address;
		
	}
	
	/***TODO do  more research because currently this is not returning results; perhaps due to 
	 * 'received a message of type CLIENT_BATCH_LIST_REQUEST from <> but have no handler for that type' warning in sawtooth console*/
	@Test
    public  void testRecieve_SUB() throws InvalidProtocolBufferException {
		
		EventSubscription subscription = EventSubscription.newBuilder()
				.setEventType("sawtooth/state-delta")
				//.setEventType("sawtooth/block-commit")
				.addFilters( 
					EventFilter.newBuilder()
						.setKey(subscriptionKey)
						.setMatchString(subscriptionMatchString)
						.setFilterType(FilterType.REGEX_ANY)
						)
				.build();
		
		
        ClientEventsSubscribeRequest request = ClientEventsSubscribeRequest.newBuilder()
        		.addSubscriptions(subscription)
        		.build();
        
        String identity = "Kaniu"+Math.random();//make unique. UUID?
        
        
        Message message = Message.newBuilder()
        		.setCorrelationId(identity)
        		.setMessageType(MessageType.CLIENT_EVENTS_SUBSCRIBE_REQUEST)
        		.setContent(request.toByteString())
        		.build();
     
        System.out.println("Connecting to eatms sawtooth …");
        
        ZMQ.Context context = ZMQ.context(1);
        ZMQ.Socket socket = context.socket(ZMQ.DEALER);
        socket.connect("tcp://localhost:8800");
        
        boolean sent = socket.send(message.toByteArray());

        while(sent){
        	Message msg = Message.parseFrom(socket.recv());
        	System.out.println("receiving from eatms sawtooth …");
        	MessageType mesType=null;
        	if ( (mesType = msg.getMessageType() )!= MessageType.CLIENT_EVENTS_SUBSCRIBE_RESPONSE ) {
        	    System.out.println("Unexpected message type : "+mesType);
        	    continue;
        	}

        	ClientEventsSubscribeResponse response = ClientEventsSubscribeResponse.parseFrom(msg.getContent());
        	System.out.println("processing response from eatms sawtooth …");
        	//Validate the response status
        	if (response.getStatus() != ClientEventsSubscribeResponse.Status.OK) {
        		System.out.println(String.format("Subscription failed: { %s : %s }",response.getStatus(),response.getResponseMessage()));;
        		continue;
        	}
        	
        }
        
        //socket.close();
        //context.term();
    }
	
	/***TODO do  more research because currently this is not returning results; perhaps due to 
	 * 'received a message of type CLIENT_BATCH_LIST_REQUEST from <> but have no handler for that type' warning in sawtooth console*/
	@Test
	public void testStream_SUB() throws InvalidProtocolBufferException, InterruptedException, ValidatorConnectionError {
		EventSubscription subscription = EventSubscription.newBuilder()
				.setEventType("sawtooth/state-delta")
				//.setEventType("sawtooth/block-commit")
				.addFilters( 
					EventFilter.newBuilder()
						.setKey(subscriptionKey)
						//.setMatchString(subscriptionMatchString)
						.setMatchString("*")
						.setFilterType(FilterType.REGEX_ANY)
						)
				.build();
		
		
        ClientEventsSubscribeRequest request = ClientEventsSubscribeRequest.newBuilder()
        		.addSubscriptions(subscription)
        		.build();
        
        
		Stream eventStream = new Stream("tcp://localhost:8800"); // make this in the constructor of class NOT here
        
		Future sawtoothSubsFuture = eventStream.send(MessageType.CLIENT_BATCH_LIST_REQUEST,//MessageType.CLIENT_EVENTS_SUBSCRIBE_REQUEST,
				request.toByteString());

		System.out.println("Sent sawtooth subscribe stream request "+sawtoothSubsFuture.isDone());
        ClientEventsSubscribeResponse eventSubsResp = ClientEventsSubscribeResponse
                .parseFrom(sawtoothSubsFuture.getResult());

        System.out.println("eventSubsResp.getStatus() :: " + eventSubsResp.getStatus());
        boolean isActive = false;
         if (eventSubsResp.getStatus().equals(ClientEventsSubscribeResponse.Status.UNKNOWN_BLOCK)) {
             System.out.println("Unknown block ");
             // retry connection if this happens by calling this same method
             
         }
        if(!eventSubsResp.getStatus().equals(ClientEventsSubscribeResponse.Status.OK)) {
            System.out.println("Subscription failed with status " + eventSubsResp.getStatus());
            throw new RuntimeException("cannot connect ");
        } else {
            isActive = true;
            System.out.println("Making active ");
        }
        
        
        while(isActive) {
            Message eventMsg =  eventStream.receive();
            EventList eventList;
			try {
				eventList = EventList.parseFrom(eventMsg.getContent());
		
            for (Event event : eventList.getEventsList()) {                 
                System.out.println("An event ::::");
                System.out.println(event);
            }
			} catch (InvalidProtocolBufferException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            
        }

	}

}
