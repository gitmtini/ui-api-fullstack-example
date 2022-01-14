package dao.sawtooth;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyPair;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriBuilder;

import org.bitcoinj.core.ECKey;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;
import org.spongycastle.util.encoders.Hex;

import com.google.gson.Gson;
import com.google.protobuf.ByteString;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.util.JsonFormat;
import com.prelimtek.client.sawtooth.SawtoothDAO;
import com.prelimtek.utils.blockchain.SawtoothUtils;
import com.prelimtek.utils.crypto.JWTManager;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;

import io.jsonwebtoken.Claims;
import io.mtini.proto.eat.EstateAccountProtos;
import io.mtini.proto.eat.EstateAccountProtos.LedgerEntries.EstateModel;
//import sawtooth.sdk.client.Signing;
import sawtooth.sdk.processor.Utils;
import sawtooth.sdk.protobuf.Batch;
import sawtooth.sdk.protobuf.BatchHeader;
import sawtooth.sdk.protobuf.BatchList;
import sawtooth.sdk.protobuf.Transaction;
import sawtooth.sdk.protobuf.TransactionHeader;


/**
 * Notes:
 * 
 * ../blocks 
 * ../batches
 * ../transactions
 * ../state
 * 
 * */
public class TestDao {

	SawtoothDAO dao = new SawtoothDAO();//new SawtoothDAO("http://10.5.0.6:8008");//;
	//id = mtini-test-id
	String jwtStr = null;
	Claims claim = null;
	ByteString data = null;
	KeyPair kp;
	String pkStr;
	
	@Before
	public void init() throws NoSuchAlgorithmException, InvalidAlgorithmParameterException, NoSuchProviderException, IOException{
		
		//WORKS
		//kp = SecurityUtils.generateECKeyPair_112Bit();
		//kp = SecurityUtils.generateSECP256KeyPair();
		//String pkStrHex = SecurityUtils.toASN1Encoded(kp).getPrivateKeyAsHex();
		//END
		
		JWTManager mn =  new JWTManager("kaniu");
		
		String pkStrHex = "bf114368ce14896cfc0829a9838152c159099278247ffbd0e04b753f41fc2b5f";//in combination with  Hex.toHexString(privateKey.getPubKeyHash());  causes unable to parse pubkey; Exception: unknown public key size (expected 33 or 65) 
		//String pkStrHex = new String("00000000003019020101041447ebcea18ed43a06a92f7e5be64e89eb8cc850eb");
		//ECKey privateKey = Signing.generatePrivateKey(new SecureRandom());
		//String pkStrHex = privateKey.getPrivateKeyAsHex();
		
		System.out.println("prejwt pk = "+pkStrHex);
		String jwt = mn.createJWT(pkStrHex, "akili", "100 day JWT token", JWTManager.incrementDate(new Date(), 100));
		System.out.println(jwt);
		jwtStr = jwt;
		//create jwt
		claim = mn.parseJWT(jwtStr);
		
		EstateAccountProtos.Operation operation = EstateAccountProtos.Operation.ADD_ESTATE;
		EstateAccountProtos.LedgerEntries entries = EstateAccountProtos.LedgerEntries.newBuilder()
		.addEstateData(
				EstateModel.newBuilder()
				.setId("EstateId1")
				.setName("Condo 13")
				.setType(EstateAccountProtos.EstateType.condo)
				.setAddress("My new Address 13")
				.setContacts("0705551212")
				.setDescription("Test data"))
				.setOperation(operation)
				.build();
		
		data = entries.toByteString();
		
		System.out.println("data -> "+data.toStringUtf8());
		
	}
	
	@Test
	public void sendBatchData() throws NoSuchAlgorithmException, IOException, JSONException{

		byte[] response = dao.sendData(claim, data).getEntity(String.class).getBytes();
		
		JSONObject json = new JSONObject(new String(response));
		
	    System.out.println(json.get("link"));
		
	}
	
	
	@Test
	public void testSigning(){
		
		ECKey privateKey = new ECKey(new SecureRandom());//Signing.generatePrivateKey(new SecureRandom());
	    String publicKey = privateKey.getPrivateKeyAsHex();//Signing.getPublicKey(privateKey);

	    ByteString publicKeyByteString = ByteString.copyFromUtf8(publicKey);

	    String payload = "{'key':1, 'value':'value comes here'}";
	    String payloadBytes = Utils.hash512(payload.getBytes());
	    ByteString payloadByteString  = ByteString.copyFrom(payload.getBytes());

	    TransactionHeader txnHeader = TransactionHeader.newBuilder()
	            .setBatcherPublicKeyBytes(publicKeyByteString)
	            .setSignerPublicKeyBytes(publicKeyByteString)
	            .setFamilyName("plain_info")
	            .setFamilyVersion("1.0")
	            .addInputs("1cf1266e282c41be5e4254d8820772c5518a2c5a8c0c7f7eda19594a7eb539453e1ed7")
	            .setNonce("1")
	            .addOutputs("1cf1266e282c41be5e4254d8820772c5518a2c5a8c0c7f7eda19594a7eb539453e1ed7")
	            .setPayloadSha512(payloadBytes)
	            .setSignerPublicKey(publicKey)
	            .build();
	    ByteString txnHeaderBytes = txnHeader.toByteString();
	    String txnHeaderSignature = SawtoothUtils.sign(txnHeaderBytes.toByteArray(), privateKey);//Signing.sign(privateKey, txnHeaderBytes.toByteArray());

	    Transaction txn = Transaction.newBuilder()
	            .setHeader(txnHeaderBytes)
	            .setPayload(payloadByteString)
	            .setHeaderSignature(txnHeaderSignature)
	            .build();

	    BatchHeader batchHeader = BatchHeader.newBuilder()
	            .setSignerPublicKey(publicKey)
	            .addTransactionIds(txn.getHeaderSignature())
	            .build();
	    ByteString batchHeaderBytes = batchHeader.toByteString();
	    String batchHeaderSignature = SawtoothUtils.sign(txnHeaderBytes.toByteArray(), privateKey);//Signing.sign(privateKey, batchHeaderBytes.toByteArray());

	    Batch batch = Batch.newBuilder()
	            .setHeader(batchHeaderBytes)
	            .setHeaderSignature(batchHeaderSignature)
	            .addTransactions(txn)
	            .build();

	    BatchList batchList = BatchList.newBuilder()
	            .addBatches(batch)
	            .build();
	    ByteString batchBytes = batchList.toByteString();

	    
	    ClientConfig config = new DefaultClientConfig();
		Client client = Client.create(config);
		WebResource webResource = client.resource(UriBuilder.fromUri("http://localhost:8008/batches")
				.build());
		
		ClientResponse response = webResource
				.type(MediaType.APPLICATION_OCTET_STREAM_TYPE).
		post(ClientResponse.class, batchBytes.toByteArray());
		
	    System.out.println(response.getEntity(String.class));
		
	}
	
	

	
	@Test
	public void testSigning2() throws UnsupportedEncodingException, NoSuchAlgorithmException{
		
		//ECKey privateKey = ECKey.fromPrivate(Hex.decode(claim.getId()));
		ECKey privateKey = ECKey.fromPrivate(Hex.decode("bf114368ce14896cfc0829a9838152c159099278247ffbd0e04b753f41fc2b5f"));
		//ECKey privateKey = Signing.generatePrivateKey(new SecureRandom());

		String publicKey = privateKey.getPublicKeyAsHex();////Signing.getPublicKey(privateKey);

	    ByteString publicKeyByteString = ByteString.copyFromUtf8(publicKey);

		String hashedAddress = SawtoothUtils.calculateAddress(SawtoothUtils.FAMILY,publicKeyByteString.toByteArray());
		
		Transaction transaction = SawtoothUtils.createTransaction(data, privateKey,publicKeyByteString, Arrays.asList(hashedAddress), Arrays.asList(hashedAddress));
		
		Batch trxnBatch = SawtoothUtils.createTransactionBatch(Arrays.asList(transaction), privateKey, publicKeyByteString);
		
		BatchList trxnBatchList = BatchList.newBuilder().addBatches(trxnBatch).build();
		
	    ByteString batchBytes = trxnBatchList.toByteString();
	    
	    ClientConfig config = new DefaultClientConfig();
		
	    Client client = Client.create(config);
		
	    WebResource webResource = client.resource(UriBuilder.fromUri("http://localhost:8008/batches")
				.build());
		
		ClientResponse response = webResource.
				type(MediaType.APPLICATION_OCTET_STREAM_TYPE).
				accept(MediaType.APPLICATION_JSON_TYPE).
				post(ClientResponse.class, batchBytes.toByteArray());
		
	    System.out.println(response.getEntity(String.class));

	}
	
	
	
	@Test
	public void query() throws UnsupportedEncodingException{
		
		
	    StringBuilder filters = new StringBuilder();
	    filters
	    //.append("wait")
	    .append("&")
	    .append("count=1")
	    .append("&").append("limit=2");
	    
	
	    ClientConfig config = new DefaultClientConfig();
		
	    Client client = Client.create(config);
		
	    WebResource webResource = client.resource(UriBuilder
	    		//.fromUri("http://localhost:8008/batch_statuses?id=cae99c4ad91953a14b90721578968a5eb5f3aabdadd7e613ca258a14126339fc42dc96c3adfb2bef34f382ab237f406d93968629097273adb1136ecb57c06264")
	    		//.fromUri("http://localhost:8008/blocks")
	    		.fromUri("http://localhost:8008/transactions")

	    		//.queryParam("max", 2)
	    		//.queryParam("min", 1)
	    		.replaceQuery(filters.toString())
	    		.build());
		
		ClientResponse response = webResource.
				type(MediaType.APPLICATION_OCTET_STREAM_TYPE).
				accept(MediaType.APPLICATION_JSON_TYPE).
				get(ClientResponse.class);
		
	    System.out.println(response.getEntity(String.class));
		
	}
	
	
	@Test
	public void query_StateByPublicKey() throws UnsupportedEncodingException, JSONException, InvalidProtocolBufferException{
		
		ECKey privateKey = ECKey.fromPrivate(Hex.decode(claim.getId()));
		
		String publicKey = privateKey.getPrivateKeyAsHex();//Signing.getPublicKey(privateKey);

	    ByteString publicKeyByteString = ByteString.copyFromUtf8(publicKey);

		String hashedAddress = SawtoothUtils.calculateAddress(SawtoothUtils.FAMILY,publicKeyByteString.toByteArray());
		
	    ClientConfig config = new DefaultClientConfig();
		
	    Client client = Client.create(config);
		
	    StringBuilder filters = new StringBuilder();
	    
	    filters
	    //.append("wait").append("&")
	    .append("count=2")
	    .append("&").append("min=1");
	    //.append("&").append("max=10");
	    
	    WebResource webResource = client.resource(
	    		UriBuilder.fromUri("http://localhost:8008/state/"+hashedAddress)
	    		//.replaceQuery(filters.toString())
	    		//.queryParam("count", 2)
	    		//.queryParam("min", 1)
				.build());
		
		ClientResponse response = webResource.
				type(MediaType.APPLICATION_OCTET_STREAM_TYPE).
				accept(MediaType.APPLICATION_JSON_TYPE).
				get(ClientResponse.class);
		
		String resStr = response.getEntity(String.class);
		
		System.out.println(resStr);
		int status = response.getStatus();
		
		System.out.println("status : "+status);
		
		JSONObject json = new JSONObject(resStr);
		if(status <=202){

		String bas64EncodedData = (String) json.get("data");
	    
	    ByteString decoded = ByteString.copyFrom(Base64.getMimeDecoder().decode(bas64EncodedData.getBytes()));
	    
	    System.out.println(decoded.toStringUtf8());
	    
	    EstateAccountProtos.LedgerEntries entries = EstateAccountProtos.LedgerEntries.newBuilder().mergeFrom(decoded).build();
		
	    System.out.println(entries.getEstateDataCount());
		}else{
			System.out.println("No data");
		}
		
	}
	
	@Test
	public void testJson2Proto() throws InvalidProtocolBufferException{

		//EstateAccountProtos.LedgerEntries entries = EstateAccountProtos.LedgerEntries.parseFrom(data);
		
		//String jsonStr = JsonFormat.printer().print(entries.getEstateData(0));//.toString();
		
		//System.out.println(jsonStr);
		//String encodedId = new String(Base64.getEncoder().encode("50a58931-0a65-490d-b941-3553f23397b5".getBytes()));

		String encodedId = Base64.getEncoder().encodeToString("50a58931-0a65-490d-b941-3553f23397b5".getBytes());

		String jsonStr = "{\"address\":\"Kiambu Rd\",\"contacts\":\"5551213\",\"description\":\"New apartment block\",\"id\":\""+encodedId+"\",\"name\":\"KN Apartment\",\"tenantCount\":0,\"type\":\"apartment\"}";
		System.out.println(jsonStr);
		EstateModel.Builder estateBuilder = EstateModel.newBuilder();
		Gson gson = new Gson();
		//JsonFormat.parser().merge(jsonStr,estateBuilder);

		estateBuilder = gson.fromJson(jsonStr, EstateModel.Builder.class);
		EstateModel model = estateBuilder.build();
		
		System.out.println(model.toString());
		
	}
	
}
