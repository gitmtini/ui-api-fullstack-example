package dao;
import java.net.UnknownHostException;
import java.util.Base64;
import java.util.UUID;

import org.bitcoinj.core.Sha256Hash;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

import com.google.gson.Gson;
import com.google.protobuf.ByteString;
import com.google.protobuf.util.JsonFormat;
import com.prelimtek.client.es.ESDAOImpl;

import io.mtini.proto.eat.EstateAccountProtos;
import io.mtini.proto.eat.EstateAccountProtos.LedgerEntries.EstateModel;

public class TestESDAO {

	ESDAOImpl esDao =  new ESDAOImpl("http://localhost:9200");
	
	@Test
	public void testSearchImages() throws UnknownHostException, JSONException{
		String index = "images_105866107162741";
		boolean active = true;
		JSONArray array = esDao.getImageListByModelId(index, active, null);
		System.out.println(array);
	}
	
	
	@Test
	public void testSearchWallet() throws UnknownHostException, JSONException{
		String preHash = "kaniu@mtini.ionull";
		String address ="WALLET_"+Sha256Hash.of(
				preHash.getBytes()).toString();
		String index = "wallets_";
		boolean active = true;
		JSONArray jsonArrayWalletRes = esDao.getWallet(index, true, address);
		
		System.out.println(jsonArrayWalletRes);
		
		 JSONObject jsonWalletObjectRes = jsonArrayWalletRes.length()==0?null:jsonArrayWalletRes.getJSONObject(0);
		 
		 if(jsonWalletObjectRes!=null){
			 System.out.println(jsonWalletObjectRes.get("encodedString").getClass());
			 System.out.println(jsonWalletObjectRes.get("encodedString").toString().getBytes());
			 //decode/decrypt? String to json
			 byte[] decodedWallet = Base64.getDecoder().decode(jsonWalletObjectRes.get("encodedString").toString().getBytes());
		 
		 }
	}
	
	
	@Test
	public void testInsertData() throws Exception{

		EstateAccountProtos.Operation operation = EstateAccountProtos.Operation.ADD_ESTATE;
		EstateAccountProtos.LedgerEntries entries = EstateAccountProtos.LedgerEntries.newBuilder()
				.addEstateData(
						EstateModel.newBuilder()
						//.setId(ByteString.copyFrom(UUID.randomUUID().toString().getBytes()))
						.setId(UUID.randomUUID().toString())
						.setName("Condo 1")
						.setType(EstateAccountProtos.EstateType.condo)
						.setAddress("My new Address")
						.setContacts("0705551212")
						.setDescription("Test data for 1")
						)
				.addEstateData(
						EstateModel.newBuilder()
						//.setId(ByteString.copyFrom(UUID.randomUUID().toString().getBytes()))
						.setId(UUID.randomUUID().toString())
						.setName("House 1")
						.setType(EstateAccountProtos.EstateType.house)
						.setAddress("Some Address")
						.setContacts("0705551212")
						.setDescription("Is my house")
						)
				.setOperation(operation)
				.build();
		// entries.toByteString();

		String esIndex = "customer3";

		String modelType = "estate";
		
		Gson gson = new Gson();
		for(int i = 0 ; i < entries.getEstateDataCount(); i++){
			EstateModel estate  = entries.getEstateData(i);
			String estateData = gson.toJson(estate);//JsonFormat.printer().print(estate);

			JSONObject estateJsonData = new JSONObject(estateData);
			estateJsonData.put("timestamp", System.currentTimeMillis());
			estateJsonData.put("model_type", modelType);
			
			System.out.println(estateJsonData.toString());

			JSONObject res = esDao.putRecord(estateJsonData.toString(), esIndex, modelType, UUID.fromString(estate.getId()));

			System.out.println(res.toString());
		}
	}

	@Test
	public void testUpdateData() throws Exception{
		
		String esIndex = "customer3";

		String modelType = "estate";
		
		EstateModel estate = EstateModel.newBuilder()
		//.setId(ByteString.copyFrom(UUID.randomUUID().toString().getBytes()))
		//.setId(ByteString.copyFrom(Base64.encode(UUID.randomUUID().toString().getBytes())))
		.setName("House 1")
		.setType(EstateAccountProtos.EstateType.house)
		.setAddress("Some Address")
		.setContacts("2221213456")
		.setDescription("Is my house").build()
		;
		UUID id = UUID.fromString("00ddb73d-4cc9-3dc8-ba6f-7ef3f83bbe07");

		Gson gson = new Gson();
		String estateData = gson.toJson(estate);//JsonFormat.printer().print(estate);

		JSONObject estateJsonData = new JSONObject(estateData);
		estateJsonData.put("timestamp", System.currentTimeMillis());

		JSONObject res = esDao.updateDocument(estateJsonData.toString(), esIndex, modelType,id);

		System.out.println(res.toString());
	}
}
