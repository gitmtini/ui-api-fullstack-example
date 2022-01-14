import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriBuilderException;

import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import com.google.protobuf.Any;
import com.google.protobuf.ByteString;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

import io.mtini.proto.RequestResponseProtos.RequestResponse;
import io.mtini.proto.eat.EstateAccountProtos;
import io.mtini.proto.eat.EstateAccountProtos.LedgerEntries.EstateModel;

public class TestREST {
    String host = "http://localhost:8080";
	
	ByteString data ;
	EstateAccountProtos.LedgerEntries entries ;
	@Before
	public void init(){
		EstateAccountProtos.Operation operation = EstateAccountProtos.Operation.ADD_ESTATE;
		entries = EstateAccountProtos.LedgerEntries.newBuilder()
		.addEstateData(
				EstateModel.newBuilder()
				//.setId(ByteString.copyFrom("eyJhbGciOiJIUzI1".getBytes()))
				.setId("eyJhbGciOiJIUzI1")
				.setName("Condo 1")
				//.setType(EstateAccountProtos.EstateType.condo)
				.setAddress("My new Address")
				.setContacts("0705551212")
				.setDescription("Test data for 1"))
				.setOperation(operation)
				.build();
		data = entries.toByteString();
	}
	
	
	@Test
	public void testPost1() throws IOException{
		
		String apikey = "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiJiZjExNDM2OGNlMTQ4OTZjZmMwODI5YTk4MzgxNTJjMTU5MDk5Mjc4MjQ3ZmZiZDBlMDRiNzUzZjQxZmMyYjVmIiwiaWF0IjoxNTQ0NzQzNzQ2LCJzdWIiOiIxMDAgZGF5IEpXVCB0b2tlbiIsImlzcyI6ImFraWxpIiwiZXhwIjoxNTUzMzgwMTQ2fQ.sYavHw6wfugNKaAOfoGyUefZX7Ai77ROMew0H5AKvIM";
		//URL url = new URL("http://localhost:8080/DSGatewayWebService/api/Gateway/queue");
		
		//String apikey = "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiJzb21la2V5IiwiaWF0IjoxNTQ0NjY5MDYzLCJzdWIiOiIxMDAgZGF5IEpXVCB0b2tlbiIsImlzcyI6ImFraWxpIiwiZXhwIjoxNTUzMzA1NDYzfQ.EH7aiVNTGE3BE4R83FLFQCPN6ILMdyD-wfW98rurj50";

		String customerId = "CustomerRestTest";
		//URL url = new URL(host+"/api/v1/taesm/estate/"+customerId+"/add_estate");
	
		URL url = new URL(host+"/api/v1/taesm/estate/CustomerId1/EDIT_ESTATE");
		URLConnection connection = url.openConnection();
		connection.setDoOutput(true);
		connection.setRequestProperty("x-mtini-apikey", apikey);
		//connection.setRequestProperty("Content-Type", "application/json");
		//connection.setRequestProperty("Content-Type", "text/plain");
		connection.setRequestProperty("Content-Type", MediaType.APPLICATION_OCTET_STREAM);
		connection.setConnectTimeout(50000);
		connection.setReadTimeout(50000);
		OutputStreamWriter out = new OutputStreamWriter(connection.getOutputStream());
		
		
		
		//String dataStrLine = "some data";
		//while (( dataStrLine = datain.readLine()) != null) {
			out.write(data.toStringUtf8());
		//}
		out.close();
				
		//response
		try{
		BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
		String inStr = null;
		while (( inStr = in.readLine()) != null) {
			System.out.println(inStr);
		}
		in.close();
		}catch(IOException e){
			
			System.out.println(e.getLocalizedMessage());
			throw e;
		}
	}
	
	@Test
	public void testPost() throws MalformedURLException, IllegalArgumentException, UriBuilderException, URISyntaxException{
		
		RequestResponse reqRes = RequestResponse.newBuilder()
                .setRequest(RequestResponse.Request.newBuilder().setEntries(entries!=null?entries.toByteString():null).build())
                .build();
		
		String apikey = "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiJiZjExNDM2OGNlMTQ4OTZjZmMwODI5YTk4MzgxNTJjMTU5MDk5Mjc4MjQ3ZmZiZDBlMDRiNzUzZjQxZmMyYjVmIiwiaWF0IjoxNTQ0NzQzNzQ2LCJzdWIiOiIxMDAgZGF5IEpXVCB0b2tlbiIsImlzcyI6ImFraWxpIiwiZXhwIjoxNTUzMzgwMTQ2fQ.sYavHw6wfugNKaAOfoGyUefZX7Ai77ROMew0H5AKvIM";
				
	    ClientConfig config = new DefaultClientConfig();
		
	    Client client = Client.create(config);
		
		String customerId = "customer3";
		//String path = "/api/v1/taesm/estate/update";//has the effect of adding another version in ES
		String path = "/api/v1/taesm/estate/"+customerId+"/add_estate";
	    
	    WebResource webResource = client.resource(UriBuilder.fromUri(host)
				.build());
	    
		ClientResponse response = webResource.
				path(path).
				type(MediaType.APPLICATION_OCTET_STREAM).
				accept(MediaType.APPLICATION_OCTET_STREAM).
				header("x-mtini-apikey",apikey).
				post(ClientResponse.class, reqRes.toByteArray());
		
		System.out.println(response.getClientResponseStatus().toString());
		
		System.out.println(response.toString());
		
		System.out.println(response.getEntity(String.class));
		
	}
	
	@Test
	public void testGet(){
		
		String apikey = "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiJiZjExNDM2OGNlMTQ4OTZjZmMwODI5YTk4MzgxNTJjMTU5MDk5Mjc4MjQ3ZmZiZDBlMDRiNzUzZjQxZmMyYjVmIiwiaWF0IjoxNTQ0NzQzNzQ2LCJzdWIiOiIxMDAgZGF5IEpXVCB0b2tlbiIsImlzcyI6ImFraWxpIiwiZXhwIjoxNTUzMzgwMTQ2fQ.sYavHw6wfugNKaAOfoGyUefZX7Ai77ROMew0H5AKvIM";
				
	    ClientConfig config = new DefaultClientConfig();
		
	    Client client = Client.create(config);
	    
		
		//String path = "/api/v1/taesm/estate/update";//has the effect of adding another version in ES
		String path = "/api/v1/taesm/estate/customer3";
	    
	    WebResource webResource = client.resource(UriBuilder.fromUri(host)
				.build());
	    
		ClientResponse response = webResource.
				path(path).
				accept(MediaType.APPLICATION_JSON).
				header("x-mtini-apikey",apikey).
				post(ClientResponse.class);
		
		System.out.println("Response " + response.getEntity(String.class));
		
	}
	
	
	@Test
	public void testSecurityToken() throws JSONException{

		JSONObject secObj =	new JSONObject();
		//secObj.put("phoneNumber",null);
		secObj.put("email", "someone@someplace.com");
		//secObj.put("token", null);
		
		RequestResponse reqRes = RequestResponse.newBuilder()
                .setRequest(RequestResponse.Request.newBuilder().setJsonRequest(secObj.toString())).build();
		

		String apikey = "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiJiZjExNDM2OGNlMTQ4OTZjZmMwODI5YTk4MzgxNTJjMTU5MDk5Mjc4MjQ3ZmZiZDBlMDRiNzUzZjQxZmMyYjVmIiwiaWF0IjoxNTQ0NzQzNzQ2LCJzdWIiOiIxMDAgZGF5IEpXVCB0b2tlbiIsImlzcyI6ImFraWxpIiwiZXhwIjoxNTUzMzgwMTQ2fQ.sYavHw6wfugNKaAOfoGyUefZX7Ai77ROMew0H5AKvIM";
		
	    ClientConfig config = new DefaultClientConfig();
		
	    Client client = Client.create(config);
	    
		
		//String path = "/api/v1/taesm/estate/update";//has the effect of adding another version in ES
		String path = "/api/v1/security/token/request";
	    
	    WebResource webResource = client.resource(UriBuilder.fromUri(host)
				.build());
	    
		ClientResponse response = webResource.
				path(path).
				type(MediaType.APPLICATION_OCTET_STREAM).
				accept(MediaType.APPLICATION_OCTET_STREAM).
				//header("x-mtini-apikey",apikey).
				post(ClientResponse.class, reqRes.toByteArray());
		
		
		System.out.println(response.toString());
		
		System.out.println(response.getEntity(String.class));
		
	}
	
	@Test
	public void testVerifyTokenAndRetrieveServerKey() throws JSONException{

		JSONObject secObj =	new JSONObject();
		//secObj.put("phoneNumber",null);
		secObj.put("email", "someone@someplace.com");
		//secObj.put("token", null);
		
		//EATRequestResponseProtos.EATRequestResponse reqRes = EATRequestResponseProtos.EATRequestResponse.newBuilder()
         //       .setRequest(EATRequestResponseProtos.EATRequestResponse.Request.newBuilder().setJsonRequest(secObj.toString())).build();
		

		//String apikey = "eyJhbGciOiJIUzI1NiJ9.eyJqdGkiOiJiZjExNDM2OGNlMTQ4OTZjZmMwODI5YTk4MzgxNTJjMTU5MDk5Mjc4MjQ3ZmZiZDBlMDRiNzUzZjQxZmMyYjVmIiwiaWF0IjoxNTQ0NzQzNzQ2LCJzdWIiOiIxMDAgZGF5IEpXVCB0b2tlbiIsImlzcyI6ImFraWxpIiwiZXhwIjoxNTUzMzgwMTQ2fQ.sYavHw6wfugNKaAOfoGyUefZX7Ai77ROMew0H5AKvIM";
		
	    ClientConfig config = new DefaultClientConfig();
		
	    Client client = Client.create(config);
	    

		//String path = "/api/v1/taesm/estate/update";//has the effect of adding another version in ES
		String path = "/api/v1/security/pk";
	    
	    WebResource webResource = client.resource(UriBuilder.fromUri(host)
				.build());
	    
		ClientResponse response = webResource.
				path(path).
				queryParam("userEmail", "someone@someplace.com").
				queryParam("token", "46028").
				type(MediaType.APPLICATION_OCTET_STREAM).
				accept(MediaType.APPLICATION_OCTET_STREAM).
				//header("x-mtini-apikey",apikey).
				get(ClientResponse.class);
		
		
		System.out.println(response.toString());
		
		System.out.println(response.getEntity(String.class));
		
	}
	
	

}
