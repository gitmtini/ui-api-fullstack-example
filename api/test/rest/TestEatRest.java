package rest;

import java.util.UUID;
import java.io.IOException;
import java.security.cert.Certificate;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import com.prelimtek.client.tls.TLSAdapter;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.client.urlconnection.HTTPSProperties;

import io.mtini.proto.RequestResponseProtos.RequestResponse;

public class TestEatRest {
	
	String certDir = "/Volumes/WORKSPACES/prelimtek/security/eatms_gke";
	String serverUri = "https://eatms.mtini.io/api/v1";
	ClientConfig config = new DefaultClientConfig();
    
	String clientId     = null;

	String caFile = null;
	String keystore = null;
	String keystorepass = null;
	
	Client client  = null;
	
	@Before
	public void init() throws Exception {
		clientId =  "TestClient"+UUID.randomUUID()+"";
		caFile=certDir+"/ca-rootCA.crt.pem";
		keystore = certDir+"/client-eatclientkeystore.p12"; 
		keystorepass ="mtinikeystore";
		
		Certificate ca = TLSAdapter.getCA(caFile);
		
		config.getProperties()
        .put(HTTPSProperties.PROPERTY_HTTPS_PROPERTIES,
                new HTTPSProperties(
                        SSLConnectionSocketFactory.ALLOW_ALL_HOSTNAME_VERIFIER,
                        TLSAdapter.getSSLContext(keystore, keystorepass, ca) ));
		
		client = Client.create(config);	
	}
	
	@Test
	public void testPost() throws JSONException, IOException {
		WebResource webResource = client.resource(
	    		UriBuilder.fromUri(serverUri)
	    		.path("security/token/request")
				.build());
		
		JSONObject json = new JSONObject();
		json.put("email","kaniu@mtini.io");
		json.put("phoneNumber","+16505551212");
		json.put("userName","kaniu123");
		
		RequestResponse.Request request =  RequestResponse.Request.newBuilder()
				.setJsonRequest(json.toString())
				.build()
				;
		ClientResponse response = webResource.
				type(MediaType.APPLICATION_OCTET_STREAM_TYPE).
				accept(MediaType.APPLICATION_JSON_TYPE).
				post(ClientResponse.class,RequestResponse.newBuilder().setRequest(request).build().toByteArray());
		
		System.out.println(response.toString());
		RequestResponse res = RequestResponse.parseFrom(response.getEntityInputStream());
		//RequestResponse res = response.getEntity(RequestResponse.class);

		System.out.println(res.getResponse().getJsonResponse());
	}

}
