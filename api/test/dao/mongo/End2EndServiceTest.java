package dao.mongo;

import static com.mongodb.client.model.Projections.excludeId;

import java.net.UnknownHostException;
import java.util.UUID;

import org.bitcoinj.core.Sha256Hash;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;

import io.diy.api.crud.MediaAndNotesRESTService;
import io.diy.dao.ApiMongoDAOImpl;
import io.mtini.proto.MtiniWalletProtos.MtiniWallet;
import io.mtini.proto.eat.EstateAccountProtos;
import io.mtini.proto.eat.EstateAccountProtos.LedgerEntries.EstateModel;
import io.mtini.rest.model.ModelProtoJsonCodecFactory;

public class End2EndServiceTest {
	
	ApiMongoDAOImpl dao;
	private static String ESTATE_COLLECTION = "estates_collection";
	
	@Before
	public void init() {
		
		dao = (ApiMongoDAOImpl) ApiMongoDAOImpl.instance();
		dao.addCredential("testdb", "testuser", "testpass".toCharArray());
		dao.addServerAddress("127.0.0.1", 27017);
		dao.connectDatabase("TestDB");
		//dao.dropCollection(ESTATE_COLLECTION);
		
		dao.setCollectionName(ESTATE_COLLECTION);
			
	}
	
	@Test
	public void upserthWallet() throws Exception{
		
		String email = "kaniu@mtini.io";
		String phoneNumber = "+16505551212";
		String userId1 = "1623111311070_856";
		//String walletStr = "{\"encrypted\":true,\"publicKeyHex\":\"0240126aeef40130c3179a3d10ade8cdd0f11b232cd34a37c616fb056a3ed61ee5\",\"id2\":\"1e231c66011e7a2d867a9cfae267a6aff103cf4913640b6e71a99850fc0ffbc8\",\"privateKeyHex\":\"c4c9873ab03f3298d8abef4747674be21d9b4d44de54f34905cf293ff9916c674e4c18209cdddd29ff384f06655b3345\",\"id\":\"98426770897f13a5b16782239a516c06135c68a5497fa85ef1b137c13b090abf\",\"initializationVectorHex\":\"754fdd0d1311beded00efe27dcc59eff\"}";
		String walletStr = "{\"id2\":\"1e231c66011e7a2d867a9cfae267a6aff103cf4913640b6e71a99850fc0ffbc8\",\"id\":\"98426770897f13a5b16782239a516c06135c68a5497fa85ef1b137c13b090abf\",\"initializationVectorHex\":\"754fdd0d1311beded00efe27dcc59eff\"}";
		
		
		ModelProtoJsonCodecFactory modelCodecFactory = ModelProtoJsonCodecFactory.instance();
		 
		String walletAddress = MediaAndNotesRESTService.generateWalletAddress(null, email, phoneNumber);
		
		
		JSONObject walletJson = new JSONObject(walletStr);//modelCodecFactory.getCodec(MtiniWallet.class).encode(wallet);
		MtiniWallet proto = modelCodecFactory.getCodec(MtiniWallet.class).decode(walletJson);
		System.out.println("proto = "+proto);
		System.out.println("walletJson = "+walletJson);
		//insert first
		
		JSONObject upsertRes = dao.upsertWalletData(userId1, walletAddress, walletJson);
		System.out.println("upsertRes = "+upsertRes);
		//then query
		System.out.println("walletAddress = "+walletAddress);
		//JSONArray userArr = dao.getUserId(walletAddress, phoneNumber, email, null);
		JSONArray userArr = dao.getUserId(walletAddress, null, null, null);
		
		Assert.assertTrue("getUserId should return at least 1 user instead found "+userArr, userArr!=null && userArr.length()>0);
		
		String userId = userArr.getJSONObject(0).getString("Customer");
		
		Assert.assertTrue("getUserId should return at least 1 must be equal; instead = "+userId, userId!=null && userId.equalsIgnoreCase(userId1));
		
		JSONArray jsonArrayWalletRes = dao.getWallet(userId, walletAddress);
				
		Assert.assertTrue("jsonArrayWalletRes must not be null or empty", jsonArrayWalletRes!=null && jsonArrayWalletRes.length()!=0);
		
		System.out.println("jsonArrayWalletRes ::  "+jsonArrayWalletRes);
		
		MtiniWallet w = modelCodecFactory.getCodec(MtiniWallet.class).decode(jsonArrayWalletRes.getJSONObject(0).getJSONObject(walletAddress));
		
		Assert.assertTrue("wallet id must not be null",w.getId()!=null);
		
		Assert.assertTrue("wallet id2 must not be null",w.getId2()!=null  );
	
	}
	
	@Test
	public void upserthWallet2() throws Exception{
		
		String email = "kaniu@mtini.io";
		String phoneNumber = "+16505551212";
		String phoneNumberHash= Sha256Hash.of(phoneNumber.getBytes()).toString();
		String emailHash = Sha256Hash.of(email.getBytes()).toString();
		
		ModelProtoJsonCodecFactory modelCodecFactory = ModelProtoJsonCodecFactory.instance();
		 
		String walletAddress = MediaAndNotesRESTService.generateWalletAddress(null, email, phoneNumber);
		
		/*MtiniWallet wallet = MtiniWallet.newBuilder()
		.setId(phoneNumber)
		.setId2(email)
		.setEncrypted(false)
		.build();
		*/
		MtiniWallet wallet = MtiniWallet.newBuilder()
				.setId(phoneNumberHash)
				.setId2(emailHash)
				.setEncrypted(false)
				.build();
		
		JSONObject walletJson = modelCodecFactory.getCodec(MtiniWallet.class).encode(wallet);
		//insert first
		
		JSONObject upsertRes = dao.upsertWalletData("1234", walletAddress, walletJson);
		System.out.println("upsertRes = "+upsertRes);
		//then query
		System.out.println("walletAddress = "+walletAddress);
		JSONArray userArr = dao.getUserId(walletAddress, phoneNumberHash, emailHash, null);
		//JSONArray userArr = dao.getUserId(walletAddress, null, null, null);
		
		Assert.assertTrue("getUserId should return at least 1 user instead found "+userArr, userArr!=null && userArr.length()>0);
		
		String userId = userArr.getJSONObject(0).getString("Customer");
		
		JSONArray jsonArrayWalletRes = dao.getWallet(userId, walletAddress);
				
		Assert.assertTrue("jsonArrayWalletRes must not be null or empty", jsonArrayWalletRes!=null && jsonArrayWalletRes.length()!=0);
		
		
		System.out.println("jsonArrayWalletRes ::  "+jsonArrayWalletRes);
		
		MtiniWallet w = modelCodecFactory.getCodec(MtiniWallet.class).decode(jsonArrayWalletRes.getJSONObject(0).getJSONObject(walletAddress));
		
		
		Assert.assertTrue("wallet id must not be null",w.getId()!=null);
		Assert.assertTrue("wallet id2 must not be null",w.getId2()!=null &&
					( w.getId().contentEquals(emailHash) ||  w.getId().contentEquals(phoneNumberHash) ) );
	
	}
	@Test
	public void testSearchWallet() throws Exception{
		
		String email = "kaniu@mtini.io";
		String phoneNumber = "+16505551212";
		String phoneNumberHash= Sha256Hash.of(phoneNumber.getBytes()).toString();
		String emailHash = Sha256Hash.of(email.getBytes()).toString();
		
		ModelProtoJsonCodecFactory modelCodecFactory = ModelProtoJsonCodecFactory.instance();
		 
		String walletAddress = MediaAndNotesRESTService.generateWalletAddress(null, email, phoneNumber);
		

		//then query
		System.out.println("walletAddress = "+walletAddress);
		JSONArray userArr = dao.getUserId(walletAddress, phoneNumberHash, emailHash, null);
		//JSONArray userArr = dao.getUserId(walletAddress, null, null, null);
		
		Assert.assertTrue("getUserId should return at least 1 user instead found "+userArr, userArr!=null && userArr.length()>0);
		
		String userId = userArr.getJSONObject(0).getString("Customer");
		
		JSONArray jsonArrayWalletRes = dao.getWallet(userId, walletAddress);
				
		Assert.assertTrue("jsonArrayWalletRes must not be null or empty", jsonArrayWalletRes!=null && jsonArrayWalletRes.length()!=0);
		
		
		System.out.println("jsonArrayWalletRes ::  "+jsonArrayWalletRes);
		
		MtiniWallet w = modelCodecFactory.getCodec(MtiniWallet.class).decode(jsonArrayWalletRes.getJSONObject(0).getJSONObject(walletAddress));
		
		
		Assert.assertTrue("wallet id must not be null",w.getId()!=null);
		Assert.assertTrue("wallet id2 must not be null",w.getId2()!=null );
	
	}
	
	@Test
	public void testSearchEstate() throws Exception{
		
		String email = "kaniu@mtini.io";
		String phoneNumber = "+16505551212";
		String phoneNumberHash= Sha256Hash.of(phoneNumber.getBytes()).toString();
		String emailHash = Sha256Hash.of(email.getBytes()).toString();
		
		
		ModelProtoJsonCodecFactory modelCodecFactory = ModelProtoJsonCodecFactory.instance();
		 
		String walletAddress = MediaAndNotesRESTService.generateWalletAddress(null, email, phoneNumber);
		
		JSONArray userArr = dao.getUserId(walletAddress, phoneNumberHash, emailHash, null);

		Assert.assertTrue("getUserId should return at least 1 user instead found "+userArr, userArr!=null && userArr.length()>0);
		
		String userId = userArr.getJSONObject(0).getString("Customer");
		
		JSONArray estates = dao.getEstateListByIndex(userId, true);
		
		System.out.println(estates);
	}
	
	//@Test
	public void testInsertEstate() throws Exception{
		
		this.dropCollection();
		
		this.upserthWallet2();

		System.out.println("START testInsertEstate ");
		
		((Iterable<Document>)dao.findCollection(ESTATE_COLLECTION).find()).forEach(p -> System.out.println("before :: "+p));

		
		String email = "kaniu@mtini.io";
		String phoneNumber = "+16505551212";
		String phoneNumberHash= Sha256Hash.of(phoneNumber.getBytes()).toString();
		String emailHash = Sha256Hash.of(email.getBytes()).toString();
		
		
		EstateModel estateProto = EstateModel.newBuilder()
		.setId(UUID.randomUUID().toString())
		.setName("Condo 1")
		.setType(EstateAccountProtos.EstateType.condo)
		.setAddress("My new Address")
		.setContacts("0705551212")
		.setDescription("Test data for 1")
		.build();
		
		ModelProtoJsonCodecFactory modelCodecFactory = ModelProtoJsonCodecFactory.instance();
		 
		String walletAddress = MediaAndNotesRESTService.generateWalletAddress(null, email, phoneNumber);
		
		JSONArray userArr = dao.getUserId(walletAddress, phoneNumberHash, emailHash, null);
		
		Assert.assertTrue("getUserId should return at least 1 user instead found "+userArr, userArr!=null && userArr.length()>0);
		
		String userId = userArr.getJSONObject(0).getString("Customer");
		System.out.println("userId = "+userId);
		
		System.out.println("testInsertEstate insert first ");
		System.out.println("-------------------------------");
		
		String estateId = estateProto.getId();
		JSONObject estateDataJson = modelCodecFactory.getCodec(EstateModel.class).encode(estateProto);
		JSONObject res = dao.upsertEstateData(userId, estateId, estateDataJson);
		System.out.println(res);
		
		Assert.assertTrue("estate result should have committed instead "+res, res!=null && res.has("modifiedCount") && res.getInt("modifiedCount") == 1 ||  res.getInt("matchedCount") == 1 );
		JSONArray searchRes = dao.getEstateListByEstateIds(userId, estateId, false);
		System.out.println("searchRes = "+searchRes);
		//update estate
		System.out.println("testInsertEstate update first ");
		System.out.println("-------------------------------");
		
		EstateModel updatedProto = estateProto.toBuilder().setAddress("Condo1 address updated").build();
		estateDataJson = modelCodecFactory.getCodec(EstateModel.class).encode(updatedProto);
		res = dao.upsertEstateData(userId, estateId, estateDataJson);
		
		System.out.println(res);
		Assert.assertTrue("updated estate result should have committed instead "+res, res!=null && res.has("modifiedCount") && res.getInt("modifiedCount") == 1 );
		System.out.println("searchRes = "+searchRes);
		((Iterable<Document>)dao.findCollection(ESTATE_COLLECTION).find()).forEach(p -> System.out.println(p));
		 searchRes = dao.getEstateListByEstateIds(userId, estateId, false);
		System.out.println("searchRes = "+searchRes);
		
		//insert another one
		System.out.println("testInsertEstate insert second ");
		System.out.println("-------------------------------");
		
		String estateId2 = UUID.randomUUID().toString();
		estateProto = EstateModel.newBuilder()
				.setId(estateId2)
				.setName("Condo 2")
				.setType(EstateAccountProtos.EstateType.condo)
				.setAddress("Condo 2 address")
				.setContacts("0705551212")
				.setDescription("Test data for 2")
				.build();
		
		updatedProto = estateProto.toBuilder().setAddress("Condo1 address").build();
		estateDataJson = modelCodecFactory.getCodec(EstateModel.class).encode(updatedProto);
		res = dao.upsertEstateData(userId, estateId2, estateDataJson);
			
		System.out.println(res);
		Assert.assertTrue("updated estate result should have committed instead "+res, res!=null && res.has("modifiedCount") && res.getInt("modifiedCount") == 1 );
		
		searchRes = dao.getEstateListByEstateIds(userId, estateId2, false);
		System.out.println("searchRes = "+searchRes);
			
		((Iterable<Document>)dao.findCollection(ESTATE_COLLECTION).find()).forEach(p -> System.out.println("after : "+p));

	}
	
	
	@Test
	public void showImages() throws JSONException, UnknownHostException {
		
		String email = "kaniu@mtini.io";
		String phoneNumber = "+16505551212";
		String phoneNumberHash= Sha256Hash.of(phoneNumber.getBytes()).toString();
		String emailHash = Sha256Hash.of(email.getBytes()).toString();
		
		String walletAddress = MediaAndNotesRESTService.generateWalletAddress(null, email, phoneNumber);
		
		JSONArray userArr = dao.getUserId(walletAddress, phoneNumberHash, emailHash, null);
		
		Assert.assertTrue("getUserId should return at least 1 user instead found "+userArr, userArr!=null && userArr.length()>0);
		
		String userId = userArr.getJSONObject(0).getString("Customer");
		
		JSONArray imageArray = dao.getImageList(userId, true);
		
		int length = imageArray.getJSONObject(0).getJSONArray("images").length();
		System.out.println(length);
		//System.out.println(imageArray.getJSONObject(0).getJSONArray("images"));
		for(int i = 0 ; i < length ; i++) {
			JSONObject image = imageArray.getJSONObject(0).getJSONArray("images").getJSONObject(i);
			System.out.println(image.getString("id")+" : "+image.getString("modelId"));
		}
			
		
	}
	
	@Test
	public void showNotes() throws JSONException, UnknownHostException {
		
		String email = "kaniu@mtini.io";
		String phoneNumber = "+16505551212";
		String phoneNumberHash= Sha256Hash.of(phoneNumber.getBytes()).toString();
		String emailHash = Sha256Hash.of(email.getBytes()).toString();
		
		String walletAddress = MediaAndNotesRESTService.generateWalletAddress(null, email, phoneNumber);
		
		JSONArray userArr = dao.getUserId(walletAddress, phoneNumberHash, emailHash, null);
		
		Assert.assertTrue("getUserId should return at least 1 user instead found "+userArr, userArr!=null && userArr.length()>0);
		
		String userId = userArr.getJSONObject(0).getString("Customer");
		
		JSONArray notesArray = dao.getAllNotesList(userId);
		
		int length = notesArray.getJSONObject(0).getJSONArray("notes").length();
		System.out.println(length);
		//System.out.println(imageArray.getJSONObject(0).getJSONArray("images"));
		for(int i = 0 ; i < length ; i++) {
			JSONObject image = notesArray.getJSONObject(0).getJSONArray("notes").getJSONObject(i);
			System.out.println(image.getString("date")+" : "+image.getString("modelId"));
		}
			
		
	}
	
	@Test
	public void tenantFunctionTests() throws JSONException, UnknownHostException {
		String email = "kaniu@mtini.io";
		String phoneNumber = "+16505551212";
		String phoneNumberHash= Sha256Hash.of(phoneNumber.getBytes()).toString();
		String emailHash = Sha256Hash.of(email.getBytes()).toString();
		
		String walletAddress = MediaAndNotesRESTService.generateWalletAddress(null, email, phoneNumber);
		
		JSONArray userArr = dao.getUserId(walletAddress, phoneNumberHash, emailHash, null);
		
		Assert.assertTrue("getUserId should return at least 1 user instead found "+userArr, userArr!=null && userArr.length()>0);
		
		String userId = userArr.getJSONObject(0).getString("Customer");
		
		//tenant : [{"paidDate":"1625088190617","paySchedule":"monthly","estateId":"69cd0928-8f08-4fd9-8ea1-cb342ba7c93b","dueDate":"1624694333281","name":"tenant4","buildingNumber":"r44","id":"b3a14868-b860-4eee-b997-c7a0abbee769","contacts":"+1 3105551212","status":"new_tenant"}]  
		String estateId = "69cd0928-8f08-4fd9-8ea1-cb342ba7c93b";
		String tenantId = "b3a14868-b860-4eee-b997-c7a0abbee769";
		
		//view all estateData
		Bson query = Filters.and(
				Filters.eq("Customer",userId),
				Filters.eq("type","entries"),
				Filters.exists("estateData", true)
				);
		Bson projection = Projections.fields( 
				Projections.include("estateData.tenantData"),
				excludeId()
				);
		JSONArray estateData = dao.searchCollectionByQuery(query, projection);

		System.out.println("estateData array -> "+estateData);
		
		JSONArray tenants = dao.getTenantListByEstateId(ESTATE_COLLECTION, userId, true, estateId);
		System.out.println("tenant array -> "+tenants);
	}
	
	//@Test
	public void dropCollection() {
		dao.dropCollection(ESTATE_COLLECTION);
	}
	
	@Test
	public void testShowCollection() throws Exception{
		
		((Iterable<Document>)dao.findCollection(ESTATE_COLLECTION).find()).forEach(p -> System.out.println(p));
		
	}
}
