package websockets;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttSecurityException;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONException;
import org.json.JSONObject;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.junit.Before;
import org.junit.Test;

import com.prelimtek.client.mqtt.MQTTClient;

import io.mtini.proto.RequestResponseProtos.RequestResponse;

public class TestPahoMQTT {
	  String topic        = "MQTT Examples";
      String content      = "Message from MqttPublishSample";
      int qos             = 2;
      String broker       = "ws://localhost:1883";//ssh for tls; tcp for native; ws for websockets
      String clientId     = "JavaSample";
	
	@Before
	public void init() {
		
	}
	
	@Test
	public void publishTest() {
		MemoryPersistence persistence = new MemoryPersistence();

        try {
            MqttClient publisher = new MqttClient(broker, clientId, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            connOpts.setAutomaticReconnect(true);
            connOpts.setConnectionTimeout(10);
            
            System.out.println("Connecting to broker: "+broker);
            publisher.connect(connOpts);
            System.out.println("Connected");
            System.out.println("Publishing message: "+content);
            MqttMessage message = new MqttMessage(content.getBytes());
            message.setQos(qos);
            publisher.publish(topic, message);
            System.out.println("Message published");
            publisher.disconnect();
            System.out.println("Disconnected");

        } catch(MqttException me) {
            System.out.println("reason "+me.getReasonCode());
            System.out.println("msg "+me.getMessage());
            System.out.println("loc "+me.getLocalizedMessage());
            System.out.println("cause "+me.getCause());
            System.out.println("excep "+me);
            me.printStackTrace();
        }
	}
	
	@Test
	public void subscribeTest_1() throws MqttException, InterruptedException {
		MemoryPersistence persistence = new MemoryPersistence();
        MqttClient subscriber = new MqttClient(broker, clientId, persistence);
        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(true);
        connOpts.setAutomaticReconnect(true);
        connOpts.setConnectionTimeout(10);
        subscriber.connect(connOpts);
        System.out.println("Connected");
        System.out.println("Subscribing ... ");
        //MqttMessage message = new MqttMessage(content.getBytes());
       //message.setQos(qos);
        
        
		CountDownLatch receivedSignal = new CountDownLatch(10);
		
		subscriber.subscribe(topic, (topic, msg) -> {
		    byte[] payload = msg.getPayload();
		    // ... payload handling omitted
		    System.out.println("Payload - >"+new String(payload));
		    receivedSignal.countDown();
		});    
		receivedSignal.await(1, TimeUnit.MINUTES);
		subscriber.disconnect();
		subscriber.close();
	}
	
	@Test
	public void subscribeTest() throws MqttSecurityException, MqttException, InterruptedException {
		MqttClient subscriber=new MqttClient(broker, MqttClient.generateClientId());
        MqttConnectOptions connOpts = new MqttConnectOptions();
        connOpts.setCleanSession(true);
        connOpts.setAutomaticReconnect(true);
        connOpts.setConnectionTimeout(10);
        subscriber.connect(connOpts);
        subscriber.setCallback(new TestCallBack());
        CountDownLatch receivedSignal = new CountDownLatch(10);
        
        subscriber.subscribe(topic);
        
        receivedSignal.await(1, TimeUnit.MINUTES);
		subscriber.disconnect();
		subscriber.close();
	}
	
	
	
	class TestCallBack implements MqttCallback{

		@Override
		public void connectionLost(Throwable e) {
			System.out.println(e.getMessage());
			System.out.println("Message lost .... ");
		}

		@Override
		public void deliveryComplete(IMqttDeliveryToken arg0) {
			
			
			
		}

		@Override
		public void messageArrived(String arg0, MqttMessage msg) throws Exception {
			
			 System.out.println("Message received:\n\t"+ new String(arg0+" : "+msg.getPayload()) );
		}
		
	}
	
	
	@Test
	public void publishToPhone() throws JSONException {
		MemoryPersistence persistence = new MemoryPersistence();

		topic = "a51f726f3e5671fe764c0cd55e51e7c33dbd6ad27872ae89d156fedf224fd0936ea91969376bbddb8fe9ee5de90d532fe7dc8b6c2a994f5038761a1f1012c802";
		
		JSONObject res = new JSONObject("{\"data\":[{\"id\":\"a51f726f3e5671fe764c0cd55e51e7c33dbd6ad27872ae89d156fedf224fd0936ea91969376bbddb8fe9ee5de90d532fe7dc8b6c2a994f5038761a1f1012c802\",\"invalid_transactions\":[],\"status\":\"COMMITTED\"}],\"link\":\"http://localhost:8008/batch_statuses?id=589e54bc9200749c82af0900e2d66b6f077027bb2e0702b2cf905960b03ce52155451505e74238da571c97c7ccb84d8f102fea11cc4d91fa5af5ab7083543ac0\"}");
		
		RequestResponse.Builder content = RequestResponse.newBuilder();
		content.setResponse(RequestResponse
				.Response
				.newBuilder()
				.setJsonResponse(res.toString())
				);
		
        try {
            MqttClient publisher = new MqttClient(broker, clientId, persistence);
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(true);
            connOpts.setAutomaticReconnect(true);
            connOpts.setConnectionTimeout(10);
            
            System.out.println("Connecting to broker: "+broker);
            publisher.connect(connOpts);
            System.out.println("Connected");
            System.out.println("Publishing message: "+content);
            MqttMessage message = new MqttMessage(content.build().toByteArray());
            message.setQos(qos);
            publisher.publish(topic, message);
            System.out.println("Message published");
            publisher.disconnect();
            System.out.println("Disconnected");

        } catch(MqttException me) {
            System.out.println("reason "+me.getReasonCode());
            System.out.println("msg "+me.getMessage());
            System.out.println("loc "+me.getLocalizedMessage());
            System.out.println("cause "+me.getCause());
            System.out.println("excep "+me);
            me.printStackTrace();
        }
	}

	
	

}
