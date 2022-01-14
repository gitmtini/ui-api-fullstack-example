package dao.mongo;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.google.protobuf.InvalidProtocolBufferException;
import com.mongodb.client.model.Filters;

import io.diy.dao.ApiMongoDAOImpl;
import io.mtini.proto.MtiniWalletProtos.MtiniWallet;
import io.mtini.proto.eat.EstateAccountProtos;
import io.mtini.proto.eat.EstateAccountProtos.LedgerEntries;
import io.mtini.proto.eat.EstateAccountProtos.LedgerEntries.EstateModel;
import io.mtini.proto.eat.EstateAccountProtos.LedgerEntries.EstateModel.TenantModel;
import io.mtini.rest.model.ModelProtoJsonCodecFactory;
import io.mtini.rest.model.ModelProtoJsonCodecFactory.ModelProtoJsonCodec;
import io.mtini.proto.eat.EstateAccountProtos.LedgerEntries.ImageModel;
import io.mtini.proto.eat.EstateAccountProtos.LedgerEntries.NotesModel;
import io.mtini.proto.eat.EstateAccountProtos.TenantStatus;

public class TestMongoEATDBDAO {

	ApiMongoDAOImpl dao;
	private static String CUSTOMER_COLLECTION1 = "TestCollection1";
	private static String INTERNAL_COLLECTION = "InternalCollection";
	private static String phoneNumber = "6505551212";
	private static String userEmail = "test@someplace.com";
	private static String userName = "myonlineName";
	
	private static String userId = (phoneNumber+userEmail).hashCode()+"";//userId = "CustomerId1";
	private static String walletAddress = "shageneratedadrress";

	private static String estate1Id = UUID.randomUUID().toString();
	private static String tenant1Id = UUID.randomUUID().toString();
	private static String tenant2Id = UUID.randomUUID().toString();
	private static String image1Id 	= UUID.randomUUID().toString();
	private static String image2Id  = UUID.randomUUID().toString();
	private static String estate2Id = UUID.randomUUID().toString();
	
	private ModelProtoJsonCodecFactory modelCodecFactory = ModelProtoJsonCodecFactory.instance();
	
	@Before
	public void init() {
	
		dao = (ApiMongoDAOImpl) ApiMongoDAOImpl.instance();
		dao.addCredential("testdb", "testuser", "testpass".toCharArray());
		dao.addServerAddress("127.0.0.1", 27017);
		dao.connectDatabase("TestDB");
		dao.dropCollection(CUSTOMER_COLLECTION1);
		
		dao.setCollectionName(CUSTOMER_COLLECTION1);
		
		
		try {
			testInsertData();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	//@Test
	public void testInsertData() throws Exception{

		//INSERT WALLET
		
		MtiniWallet wallet = MtiniWallet.newBuilder()
		.setId(phoneNumber)
		.setId2(userEmail)
		.setEncrypted(false)
		.build();
		
		JSONObject walletJsonData = this.modelCodecFactory.getCodec(MtiniWallet.class).encode(wallet);
		JSONArray walletArray = new JSONArray();
		walletArray.put(walletJsonData);

		//dao.upsertUserName(userId, userName);
		
		JSONObject result = dao.upsertWalletData(userId, walletAddress, walletJsonData);
		
		System.out.println("wallet res:  "+result.toString());

		//INSERT DATA
		
		ImageModel image1 = ImageModel.newBuilder()
			.setId(image1Id)
			.setModelId(tenant1Id)
			.setEncodedBitmap("blahblah").build();
		ImageModel image2 = ImageModel.newBuilder()
				.setId(image2Id)
				.setModelId(estate1Id)
				.setEncodedBitmap("hehehehe").build();
		
		NotesModel note1 = NotesModel.newBuilder()
				.setDate(System.currentTimeMillis())
				.setModelId(tenant1Id)
				.setNoteText("Tenant1 note 1").build();
		NotesModel note2 = NotesModel.newBuilder()
				.setDate(System.currentTimeMillis())
				.setModelId(estate1Id)
				.setNoteText("Estate1 note 1").build();
		
		EstateModel estate1  = EstateModel.newBuilder()
		.setId(estate1Id)
		.setName("Condo 1")
		.setType(EstateAccountProtos.EstateType.condo)
		.setAddress("My new Address")
		.setContacts("0705551212")
		.setDescription("Test data for 1")
		.addTenantData( TenantModel.newBuilder()
				.setContacts("0705551212")
				.setCurrency("KSH")
				.setName("Tenant 1")
				.setStatus(TenantStatus.moved)
				.setId(tenant1Id)
				.setNotes("Initial insert of tenant1 ")
				).build();
			
		EstateAccountProtos.Operation operation = EstateAccountProtos.Operation.ADD_ESTATE;
		EstateAccountProtos.LedgerEntries entries = EstateAccountProtos.LedgerEntries.newBuilder()
				.addEstateData(
						estate1
						)
				.addAllImages(Arrays.asList(image1, image2))
				.addAllNotes(Arrays.asList(note1, note2))
				.setOperation(operation)
				.build();
		
		String modelType = "entries";
		
		ModelProtoJsonCodec<LedgerEntries> protoJsonCodec = modelCodecFactory.getCodec(EstateAccountProtos.LedgerEntries.class);
		JSONObject entriesJsonData = protoJsonCodec.encode(entries);
		entriesJsonData.put("timestamp", System.currentTimeMillis());
		result = dao.putDocument(userId, modelType , entriesJsonData);

		System.out.println("newdata1 res:  "+result.toString());
		
		
		/// INSERT MORE DATA
		EstateModel estate2 = EstateModel.newBuilder()
		.setId(estate2Id)
		.setName("House 2")
		.setType(EstateAccountProtos.EstateType.house)
		.setAddress("Some Address")
		.setContacts("0705551212")
		.setDescription("Test data for 2").build();
		
		EstateAccountProtos.LedgerEntries entries2 = EstateAccountProtos.LedgerEntries.newBuilder()
				.addEstateData(
						estate2
						)
				.setOperation(operation)
				.build();
		
		JSONObject estateJsonData2 = this.modelCodecFactory.getCodec(EstateModel.class).encode(estate2);
		estateJsonData2.put("timestamp", System.currentTimeMillis());

		JSONObject result2 = dao.upsertEstateData(userId, estate2Id, estateJsonData2);
		System.out.println("newdata2 res:  "+result2.toString());
		
	}
	
	//@Test
	@Deprecated
	public void testSearchWallet() throws Exception{
		
		
		JSONArray jsonArrayWalletRes = dao.getWallet(userId, true, phoneNumber)
				.getJSONObject(0).getJSONArray("walletData");
				
		Assert.assertTrue("jsonArrayWalletRes must not be null or empty", jsonArrayWalletRes!=null && jsonArrayWalletRes.length()!=0);
		
		
		System.out.println("jsonArrayWalletRes ::  "+jsonArrayWalletRes);
		
		List<MtiniWallet> walletProtos = this.modelCodecFactory.getCodec(MtiniWallet.class).decode(jsonArrayWalletRes);
		
		walletProtos.forEach(w -> {
			Assert.assertTrue("wallet id must not be null",w.getId()!=null);
			Assert.assertTrue("wallet id2 must not be null",w.getId2()!=null &&
					( w.getId().contentEquals(userEmail) ||  w.getId().contentEquals(phoneNumber) ) );
		});
		
		
		//using email 
		
		jsonArrayWalletRes = dao.getWallet(userId, true, userEmail)
				.getJSONObject(0).getJSONArray("walletData");
		
		Assert.assertTrue("jsonArrayWalletRes must not be null or empty", jsonArrayWalletRes!=null && jsonArrayWalletRes.length()!=0);
		
		
		System.out.println("jsonArrayWalletRes ::  "+jsonArrayWalletRes);
		
		walletProtos = this.modelCodecFactory.getCodec(MtiniWallet.class).decode(jsonArrayWalletRes);
		
		walletProtos.forEach(w -> {
			Assert.assertTrue("wallet id must not be null",w.getId()!=null);
			Assert.assertTrue("wallet id2 must not be null",w.getId2()!=null && 
					( w.getId().contentEquals(userEmail) ||  w.getId().contentEquals(phoneNumber) ) );
		});
		
		
		//phone and email
		jsonArrayWalletRes = dao.getWallet(userId, phoneNumber, userEmail)
				.getJSONObject(0).getJSONArray("walletData");
		
		Assert.assertTrue("jsonArrayWalletRes must not be null or empty", jsonArrayWalletRes!=null && jsonArrayWalletRes.length()!=0);
		
		
		System.out.println("jsonArrayWalletRes ::  "+jsonArrayWalletRes);
		
		walletProtos = this.modelCodecFactory.getCodec(MtiniWallet.class).decode(jsonArrayWalletRes);
		
		walletProtos.forEach(w -> {
			Assert.assertTrue("wallet id must not be null",w.getId()!=null);
			Assert.assertTrue("wallet id2 must not be null",w.getId2()!=null && 
					( w.getId().contentEquals(userEmail) ||  w.getId().contentEquals(phoneNumber) ) );
		});
		
		
		//email and phoneNumber
		jsonArrayWalletRes = dao.getWallet(userId, userEmail, phoneNumber)
				.getJSONObject(0).getJSONArray("walletData");
		
		Assert.assertTrue("jsonArrayWalletRes must not be null or empty", jsonArrayWalletRes!=null && jsonArrayWalletRes.length()!=0);
		
		
		System.out.println("jsonArrayWalletRes ::  "+jsonArrayWalletRes);
		
		walletProtos = this.modelCodecFactory.getCodec(MtiniWallet.class).decode(jsonArrayWalletRes);
		
		walletProtos.forEach(w -> {
			Assert.assertTrue("wallet id must not be null",w.getId()!=null);
			Assert.assertTrue("wallet id2 must not be null",w.getId2()!=null && 
					( w.getId().contentEquals(userEmail) ||  w.getId().contentEquals(phoneNumber) ) );
		});
		
	}
	
	//@Test 
	@Deprecated
	public void upsertWallet() throws JSONException, IOException {
		//initially
		
		JSONArray jsonArrayWalletRes = dao.getWallet(userId, phoneNumber, userEmail)
				.getJSONObject(0).getJSONArray("walletData");
		
		Assert.assertTrue("jsonArrayWalletRes must not be null or empty", jsonArrayWalletRes!=null && jsonArrayWalletRes.length()!=0);
		
		
		System.out.println("jsonArrayWalletRes ::  "+jsonArrayWalletRes);
		
		ModelProtoJsonCodec<MtiniWallet> codec = this.modelCodecFactory.getCodec(MtiniWallet.class);
		List<MtiniWallet> walletProtos = codec.decode(jsonArrayWalletRes);
		
		MtiniWallet wallet1 = walletProtos.get(0);
		Assert.assertTrue("expected id equals "+userEmail,wallet1.getId().contentEquals(userEmail) || wallet1.getId2().contentEquals(userEmail));

		//after
		String newEmail = "2ndemail@mine.com";
		
		MtiniWallet wallet = walletProtos.get(0).toBuilder()
		.setId(phoneNumber)
		.setId2(newEmail)
		.setEncrypted(false)
		.build();
		
		
		JSONObject result = dao.upsertWalletData(userId, phoneNumber, newEmail, codec.encode(wallet));
		System.out.println("wallet res:  "+result.toString());
		
		
		//verify
		jsonArrayWalletRes = dao.getWallet(userId, phoneNumber, newEmail)
				.getJSONObject(0).getJSONArray("walletData");
		
		List<MtiniWallet> walletProtos2 = codec.decode(jsonArrayWalletRes);
		MtiniWallet wallet2 = walletProtos2.get(0);
		
		System.out.println("verify wallet res:  "+wallet2.toString());
		Assert.assertTrue("expected id equals "+newEmail,wallet2.getId().contentEquals(newEmail) || wallet2.getId2().contentEquals(newEmail));
		
		
	}
	
	
	@Test
	public void testSearchWallet2() throws Exception{
		
		
		JSONArray jsonArrayWalletRes = dao.getWallet(userId, walletAddress);
				
		Assert.assertTrue("jsonArrayWalletRes must not be null or empty", jsonArrayWalletRes!=null && jsonArrayWalletRes.length()!=0);
		
		
		System.out.println("jsonArrayWalletRes ::  "+jsonArrayWalletRes);
		
		MtiniWallet w = this.modelCodecFactory.getCodec(MtiniWallet.class).decode(jsonArrayWalletRes.getJSONObject(0).getJSONObject(walletAddress));
		
		
		Assert.assertTrue("wallet id must not be null",w.getId()!=null);
		Assert.assertTrue("wallet id2 must not be null",w.getId2()!=null &&
					( w.getId().contentEquals(userEmail) ||  w.getId().contentEquals(phoneNumber) ) );
	
		
		
	}
	
	@Test 
	public void upsertWallet2() throws JSONException, IOException {
		//initially
		
		JSONArray jsonArrayWalletRes = dao.getWallet(userId, walletAddress);
		
		Assert.assertTrue("jsonArrayWalletRes must not be null or empty", jsonArrayWalletRes!=null && jsonArrayWalletRes.length()!=0);
		
		
		System.out.println("jsonArrayWalletRes ::  "+jsonArrayWalletRes);
		
		ModelProtoJsonCodec<MtiniWallet> codec = this.modelCodecFactory.getCodec(MtiniWallet.class);
		MtiniWallet wallet1 = codec.decode(jsonArrayWalletRes.getJSONObject(0).getJSONObject(walletAddress));
		
		Assert.assertTrue("expected id equals "+userEmail,wallet1.getId().contentEquals(userEmail) || wallet1.getId2().contentEquals(userEmail));

		//after
		String newEmail = "2ndemail@mine.com";
		
		MtiniWallet wallet = wallet1.toBuilder()
		.setId(phoneNumber)
		.setId2(newEmail)
		.setEncrypted(false)
		.build();
		
		
		JSONObject result = dao.upsertWalletData(userId, walletAddress, codec.encode(wallet));
		System.out.println("wallet res:  "+result.toString());
		
		
		//verify
		jsonArrayWalletRes = dao.getWallet(userId, walletAddress);
		
		MtiniWallet wallet2  = codec.decode(jsonArrayWalletRes.getJSONObject(0).getJSONObject(walletAddress));

		
		System.out.println("verify wallet res:  "+wallet2.toString());
		Assert.assertTrue("expected id equals "+newEmail,wallet2.getId().contentEquals(newEmail) || wallet2.getId2().contentEquals(newEmail));
		
		
	}

	@Test
	public void testSearchForEstateData() throws Exception{

		String id = estate2Id;
		System.out.println("search by estate2Id ->"+id);
		
		//verify different searches
		System.out.println("");
		System.out.println("getEstateListByIndex ");
		System.out.println("---------------------------");
		JSONArray res = dao.getEstateListByIndex(userId, true);
		System.out.println("results = "+res);
		Assert.assertTrue("expected 2 estates ",res.length()>0 && res.getJSONObject(0).has("estateData") && res.getJSONObject(0).getJSONArray("estateData").length() == 2 );
		
		
		System.out.println("");
		System.out.println("getEstateListByEstateIds ");
		System.out.println("--------------------------- ");
		res = dao.getEstateListByEstateIds(userId, id, false);
		System.out.println("results = "+res);
		Assert.assertTrue("expected 1 estate containing id '"+id+"'  ",res.length()>0 && res.getJSONObject(0).has("estateData") && res.getJSONObject(0).getJSONArray("estateData").length() == 1
				&& res.getJSONObject(0).has("estateData") && res.getJSONObject(0).getJSONArray("estateData").getJSONObject(0).has("id") 
				&&  id.contentEquals( res.getJSONObject(0).getJSONArray("estateData").getJSONObject(0).getString("id") ) );

	}

	@Test
	public void testUpdateOneEstateData() throws Exception{

		//before
		System.out.println("");
		System.out.println("testUpdateOneEstateData before getEstateListByIndex  ");
		System.out.println("---------------------------");
		JSONArray res = dao.getEstateListByIndex(userId, true);
		System.out.println("results = "+res);
		

		String id = estate2Id;
		String expectedDescription = "Is house 2 with change of address";
		System.out.println("update estate1Id ->"+id);
		
		EstateModel estate = EstateModel.newBuilder()
		.setId(id)
		.setName("House 2")
		.setType(EstateAccountProtos.EstateType.house)
		.setAddress("Updated Address for house 2")
		.setContacts("2221213456")
		.setDescription(expectedDescription).build();
		
		
		JSONObject estateJsonData =  this.modelCodecFactory.getCodec(EstateModel.class).encode(estate);

		System.out.println("");
		System.out.println("upsertEstateData ");
		System.out.println("---------------------------");
		
		JSONObject updateres = dao.upsertEstateData(userId, id, estateJsonData);
		System.out.println("updateres :  "+updateres);
		Assert.assertTrue("updated estate result should have committed instead "+updateres, updateres!=null && updateres.has("modifiedCount") && updateres.getInt("modifiedCount") == 1 && updateres.has("matchedCount") && updateres.getInt("matchedCount") == 1  );

		//verify changes
		
		System.out.println("");
		System.out.println("getEstateListByIndex ");
		System.out.println("---------------------------");
		res = dao.getEstateListByIndex(userId, true);
		System.out.println("results = "+res);
		Assert.assertTrue("expected 2 estates ",res.length()>0 && res.getJSONObject(0).has("estateData") && res.getJSONObject(0).getJSONArray("estateData").length() == 2 );
		
		System.out.println("");
		System.out.println("getEstateListByEstateIds ");
		System.out.println("--------------------------- ");
		res = dao.getEstateListByEstateIds(userId, id, false);
		System.out.println("results = "+res);
		Assert.assertTrue("expected 1 estate containing '"+expectedDescription+"'  ",res.length()>0 && res.getJSONObject(0).has("estateData") && res.getJSONObject(0).getJSONArray("estateData").length() == 1
				&& res.getJSONObject(0).has("estateData") && res.getJSONObject(0).getJSONArray("estateData").getJSONObject(0).has("description") 
				&&  expectedDescription.contentEquals( res.getJSONObject(0).getJSONArray("estateData").getJSONObject(0).getString("description") ) );

	}
	
	
	@Test
	public void testInsertListOfEstateData() throws Exception{
		String modelType = "estateData";
		String id1 = UUID.randomUUID().toString();
		String expectedDescription1 = "Is new house 3";
		//System.out.println("update estateId3 ->"+id1);
		
		EstateModel estate1 = EstateModel.newBuilder()
		.setId(id1)
		.setName("House 3")
		.setType(EstateAccountProtos.EstateType.house)
		.setAddress("Address for New House 3")
		.setContacts("2221213456")
		.setDescription(expectedDescription1).build();
		

		String id2 = UUID.randomUUID().toString();
		String expectedDescription2 = "Is new house 4";
		//System.out.println("update estateId4 ->"+id2);
		
		EstateModel estate = EstateModel.newBuilder()
		.setId(id2)
		.setName("House 4")
		.setType(EstateAccountProtos.EstateType.house)
		.setAddress("Address for house 4")
		.setContacts("2221213456")
		.setDescription(expectedDescription2).build();
		
		ModelProtoJsonCodec<EstateModel> codec = this.modelCodecFactory.getCodec(EstateModel.class);

		JSONArray estateJsonData = codec.encode(Arrays.asList(estate1,estate));
		
		JSONArray multi_res = dao.addEstateData(userId, modelType , estateJsonData, true);
		
		int numberofResults = multi_res.length();
		
		Assert.assertTrue("res must have 2 results",  numberofResults == 2);
		
		boolean found = false;
		
		for(int c = 0 ; c < numberofResults ; c++) {
		//verify changes
		int reslength = multi_res.getJSONObject(c).getJSONArray(modelType).length();
		Assert.assertTrue("must have at least 1 estate objects", reslength>=0);
		
		/*
		System.out.println("res:  "+multi_res);
		System.out.println("jsonObj:  "+multi_res.getJSONObject(c));
		System.out.println("jsonArr:  "+multi_res.getJSONObject(c).getJSONArray(modelType));
		*/
		for(int i = 0 ; i < reslength; i++) {
			String desc = multi_res.getJSONObject(c).getJSONArray(modelType).getJSONObject(i).getString("description");
			String id = multi_res.getJSONObject(c).getJSONArray(modelType).getJSONObject(i).getString("id");
			/*
			System.out.println(i+"  "+id+" : "+desc);
			System.out.println(id2+" :: "+expectedDescription2);
			*/
			if(id.contentEquals(id2)
					&& desc.contentEquals(expectedDescription2)
					) {
				found = true;
				System.out.println(found);
			}
		}
		}
		Assert.assertTrue("an estate with expected description was expected",found);

	}
	
	//TODO complete this test by either accepting results-last-insert or changing return with confirmation message
	@Test
	public void testInsertListOfEstateData_WithConfirmation() throws Exception{
		String modelType = "estateData";
		String id1 = UUID.randomUUID().toString();
		String expectedDescription1 = "Is new house 3";
		System.out.println("update estateId3 ->"+id1);
		
		EstateModel estate1 = EstateModel.newBuilder()
		.setId(id1)
		.setName("House 3")
		.setType(EstateAccountProtos.EstateType.house)
		.setAddress("Address for New House 3")
		.setContacts("2221213456")
		.setDescription(expectedDescription1).build();
		

		String id2 = UUID.randomUUID().toString();
		String expectedDescription2 = "Is new house 4";
		System.out.println("update estateId4 ->"+id2);
		
		EstateModel estate = EstateModel.newBuilder()
		.setId(id2)
		.setName("House 4")
		.setType(EstateAccountProtos.EstateType.house)
		.setAddress("Address for house 4")
		.setContacts("2221213456")
		.setDescription(expectedDescription2).build();
		
		ModelProtoJsonCodec<EstateModel> codec = this.modelCodecFactory.getCodec(EstateModel.class);
		
		JSONArray estateJsonData = codec.encode(Arrays.asList(estate1,estate));
		
		JSONArray multi_res = dao.addEstateData(userId, modelType , estateJsonData, false);
		
		int numberofResults = multi_res.length();
		
		Assert.assertTrue("res must have 2 results",  numberofResults == 2);
		
		boolean found = false;
		
		for(int c = 0 ; c < numberofResults ; c++) {
			System.out.println("res:  "+multi_res);
			System.out.println("jsonObj:  "+multi_res.getJSONObject(c));
			System.out.println("jsonArr:  "+multi_res.getJSONObject(c).getJSONArray(modelType));
		}
	}
	
	
	@Test
	public void testUpdateListOfEstateData() throws Exception{
		
		String modelType = "estateData";
		String id1 = estate1Id;
		String expectedDescription1 = "Is house 1 with change of address";
		//System.out.println("update estateId1 ->"+id1);
		
		EstateModel estate1 = EstateModel.newBuilder()
		.setId(id1)
		.setName("Condo 1")
		.setType(EstateAccountProtos.EstateType.house)
		.setAddress("New Address for house 1")
		.setContacts("2221213456")
		.setDescription(expectedDescription1).build();
		

		String id2 = estate2Id;
		String expectedDescription2 = "Is house 2 with change of address";
		//System.out.println("update estateId2 ->"+id2);
		
		EstateModel estate = EstateModel.newBuilder()
		.setId(id2)
		.setName("House 2")
		.setType(EstateAccountProtos.EstateType.house)
		.setAddress("New Address for house 2")
		.setContacts("2221213456")
		.setDescription(expectedDescription2).build();
		
		
		ModelProtoJsonCodec<EstateModel> estateCodec = this.modelCodecFactory.getCodec(EstateModel.class);
		
		JSONArray estateJsonData = estateCodec.encode(Arrays.asList(estate1,estate));
			
		JSONArray res = dao.updateEstateData(userId, "estateData.$", estateJsonData, true);
		/*JSONArray res = new JSONArray();
		Arrays.asList(estate1,estate).forEach(proto -> {
			try {
				JSONObject estateJsonData = estateCodec.encode(proto);
				System.out.println("editing estate "+estateJsonData);
				JSONObject esResult  = dao.upsertEstateData(userId, proto.getId(), estateJsonData);
				if(esResult!=null)res.put(esResult);
			} catch (JSONException | IOException e) {
				System.out.println(e.getMessage());
				res.put(e.getMessage());
			}
			
		});*/
		
		System.out.println("res = "+res);
		//verify changes
		int reslength = res.getJSONObject(0).getJSONArray("estateData").length();
		Assert.assertTrue("must have two estate objects", reslength==2);
		
		boolean found = false;
		for(int i = 0 ; i < reslength; i++) {
			if(res.getJSONObject(0).getJSONArray("estateData").getJSONObject(i).getString("id").contentEquals(id1)
					&& res.getJSONObject(0).getJSONArray("estateData").getJSONObject(i).getString("description").contentEquals(expectedDescription1)
					) {
				found = true;
				System.out.println(found);
			}
		}
		Assert.assertTrue("an estate with expected description was expected",found);
		
	}
	
	@Test
	public void testSearchForTenantData() throws UnknownHostException, JSONException{
		
		//search
		JSONArray ret = dao.getTenantListByEstateId(dao.getCollectionName(), userId, true, estate1Id);
		
		boolean found = false;
		for (int i = 0 ; i < ret.length(); i++) {
			JSONObject o = ret.getJSONObject(i);
			System.out.println("o ::"+o);
			if(o.toString().contains(tenant1Id.toString()) || o.get("id").equals(tenant1Id.toString())){
				found = true;
			}
		}
		
		Assert.assertTrue("expected tenant1Id to be found", found);
		
		ret = dao.getTenantListById(userId, tenant1Id, tenant2Id);
		System.out.println("res get Tenant :: "+ret);
		Assert.assertTrue("expected tenant1Id to be found", ret.length()==1 && ret.getJSONObject(0).toString().contains(tenant1Id.toString()));
		
		ret = dao.getTenantListById(userId, tenant2Id);
		System.out.println("res get Tenant 2 :: "+ret);
		Assert.assertTrue("expected nothing to be found",ret==null || ret.length()==0);
	}
	
	@Test
	public void testAddTenantData() throws JSONException, InvalidProtocolBufferException, UnknownHostException{
		TenantModel tenant = TenantModel.newBuilder()
		.setContacts("0705551212")
		.setCurrency("KSH")
		.setName("Tenant 2")
		.setStatus(TenantStatus.new_tenant)
		.setId(tenant2Id)
		.setNotes("Another tenant 2 ")
		.build();
		
		JSONArray tenantArray = modelCodecFactory.getCodec(TenantModel.class).encode(Arrays.asList(tenant));
		
		//insert/push new to tenantData_ array
		JSONObject res = dao.addTenantData(userId, estate2Id.toString(),tenantArray );
		System.out.println("addTenantData res ::"+res);
		//search
		JSONArray ret = dao.getTenantListByEstateId(dao.getCollectionName(), userId, true, estate2Id);
		
		boolean found = false;
		for (int i = 0 ; i < ret.length(); i++) {
			JSONObject o = ret.getJSONObject(i);
			System.out.println("o ::"+o);
			if(o.toString().contains(tenant2Id.toString())  || o.get("id").equals(tenant2Id.toString())){
				found = true;
			}
		}
		
		Assert.assertTrue("expected tenant2Id to be found", found);
	}
	
	@Test
	public void testAddTenantDataToExistingTenantList() throws JSONException, InvalidProtocolBufferException, UnknownHostException{
		String tenant21Id = UUID.randomUUID().toString();
		
		TenantModel tenant = TenantModel.newBuilder()
		.setContacts("0705551212")
		.setCurrency("KSH")
		.setName("Tenant 2")
		.setStatus(TenantStatus.new_tenant)
		.setId(tenant21Id)
		.setEstateId(estate1Id)
		.setNotes("Another estate 1 tenant 2 ")
		.build();
		
		JSONArray tenantArray = modelCodecFactory.getCodec(TenantModel.class).encode(Arrays.asList(tenant));
		
		//insert/push new to tenantData_ array
		JSONObject res = dao.addTenantData(userId, estate1Id.toString(),tenantArray );
		System.out.println("addTenantData res ::"+res);
		//search
		JSONArray ret = dao.getTenantListByEstateId(dao.getCollectionName(), userId, true, estate1Id);
		
		boolean found = false;
		for (int i = 0 ; i < ret.length(); i++) {
			JSONObject o = ret.getJSONObject(i);
			System.out.println("o ::"+o);
			if(o.toString().contains(tenant21Id.toString())  || o.get("id").equals(tenant21Id.toString())){
				found = true;
			}
		}
		
		Assert.assertTrue("expected tenant21Id to be found", found);
	}
	
	@Test
	public void testUpdateTenantData() throws JSONException, InvalidProtocolBufferException, UnknownHostException{
		
		
		String tenantField = "tenantData";
		//before
		
		
		JSONArray ret = dao.getTenantListById(userId, tenant1Id).getJSONObject(0).getJSONArray(tenantField);
		//System.out.println("res get Tenant :: "+ret);
		Assert.assertTrue("expected tenant1Id to be found", ret.length()==1 
				&& ret.toString().contains(tenant1Id.toString()));
		
		TenantModel tenantProtoInit = modelCodecFactory.getCodec(TenantModel.class).decode(ret.getJSONObject(0));
		System.out.println("tenantProtoInit :: "+tenantProtoInit);
		
		TenantModel tenant = tenantProtoInit.toBuilder().clone()
		.setStatus(TenantStatus.late)
		.setNotes("Update tenant 1 status ")
		.build();
		
		JSONObject tenantJson = modelCodecFactory.getCodec(TenantModel.class).encode(tenant);
		

		JSONObject res = dao.upsertTenantData(userId, estate2Id.toString(),tenant1Id.toString() ,tenantJson );
		
		//search
		
		ret = dao.getTenantListById(userId, tenant1Id).getJSONObject(0).getJSONArray(tenantField);
		//System.out.println("res get Tenant :: "+ret);
		Assert.assertTrue("expected tenant1Id to be found", ret.length()==1 
				&& ret.getJSONObject(0).toString().contains(tenant1Id.toString()));
		TenantModel tenantProto2 = modelCodecFactory.getCodec(TenantModel.class).decode(ret.getJSONObject(0));
		System.out.println("tenantProto2 :: "+tenantProto2);
		
		Assert.assertTrue("test Tenant must be equal to updated tenant", tenant.toString().equals(tenantProto2.toString()));
		
		Assert.assertTrue("expected initial Tenant must not be equal to updated tenant", !tenantProtoInit.toString().equals(tenantProto2.toString()));
	}
	
	@Test
	public void testDeleteTenantData() throws JSONException, UnknownHostException{
		
		JSONObject res = dao.deleteTenantData(userId, tenant1Id);
		
		//search for tenant1Id ; must return null/empty
		//search
		JSONArray ret = dao.getTenantListByEstateId(dao.getCollectionName(), userId, true, estate1Id);
		
		boolean found = false;
		
		for (int i = 0 ; i < ret.length(); i++) {
			JSONObject o = ret.getJSONObject(i);
			System.out.println("o ::"+o);
			if(o.toString().contains(tenant1Id.toString()) ){
				found = true;
			}
		}
		
		Assert.assertFalse("expected tenant1Id to NOT be found", found);
	}
	
	@Test
	public void testSearchImages() throws Exception{
		
		String imageField = "images";
		
		boolean active = true;
		JSONArray 		array = dao.getImageListByModelId(userId, active, this.estate1Id,this.tenant1Id)
				.getJSONObject(0).getJSONArray(imageField);
		
		System.out.println("res2:  "+array);
		Assert.assertTrue("result should contain data size 2", array!=null&& array.length()==2);
	
		array = dao.getImageListByModelId(userId, active,tenant1Id)
				.getJSONObject(0).getJSONArray(imageField);

		System.out.println("res1:  "+array);
		Assert.assertTrue("result should contain data size 1", array!=null&& array.length()==1);
		
		
		array = dao.getImageListByModelId(userId, active,this.estate1Id)
				.getJSONObject(0).getJSONArray(imageField);
		System.out.println("res3:  "+array);
		Assert.assertTrue("result should contain data size 1",  array!=null&& array.length()==1);
		
		array = dao.getImageListByModelId(userId, active,this.estate2Id);
		System.out.println("res4:  "+array);
		Assert.assertTrue("result should NOT contain data",  array.length()==0 || array.getJSONObject(0).isNull(imageField) );
		

		array = dao.getImageListByModelId(userId, active,this.estate1Id,estate2Id)
				.getJSONObject(0).getJSONArray(imageField);
		System.out.println("res5:  "+array);
		Assert.assertTrue("result should contain data size 1",  array!=null&& array.length()==1);
		
	}
	
	@Test
	public void testAddImageData() throws JSONException, InvalidProtocolBufferException, UnknownHostException{
		
		String imageField = "images";
		//before
		JSONArray array = dao.getImageListByModelId(userId, true, tenant1Id,estate1Id).getJSONObject(0).getJSONArray(imageField);
		
		System.out.println("before res1:  "+array);
		Assert.assertTrue("result should contain data size 2", array!=null&& array.length()==2);

		
		ImageModel image3 = ImageModel.newBuilder()
				.setEncodedBitmap("encodedString3")
				.setId(UUID.randomUUID().toString())
				.setModelId(tenant1Id)
				.build();
		
		ImageModel image4 = ImageModel.newBuilder()
				.setEncodedBitmap("encodedString4")
				.setId(UUID.randomUUID().toString())
				.setModelId(estate1Id)
				.build();
		
		
		
		JSONObject image3Json = modelCodecFactory.getCodec(ImageModel.class).encode(image3);
		JSONObject image4Json = modelCodecFactory.getCodec(ImageModel.class).encode(image4);

		JSONArray imageJSONArray = new JSONArray();
		imageJSONArray.put(image3Json);
		imageJSONArray.put(image4Json);
		
		//insert/push new to images array
		JSONObject res = dao.addImages(userId, imageJSONArray);
		System.out.println("res :: "+res);
		
		//search
		array = dao.getImageListByModelId(userId, true, tenant1Id,estate1Id).getJSONObject(0).getJSONArray(imageField);
		
		System.out.println("after res2:  "+array);
		Assert.assertTrue("result should contain data size 4 instead of "+ array.length(), array!=null&& array.length()==4);

		
		int found = 0;
		for (int i = 0 ; i < array.length(); i++) {
			JSONObject o = array.getJSONObject(i);
			System.out.println("o ::"+o);
			if(o.toString().contains("encodedString3")  || o.toString().contains("encodedString4") ){
				found++;
			}
		}
		
		Assert.assertTrue("expected 2 counts to be found instead of "+found, found==2);
	}
	
	@Test
	public void testDeleteImageData() throws JSONException, UnknownHostException{
		
		String imageField = "images";
		//before
		JSONArray array = dao.getImageListByModelId(userId, true, tenant1Id,estate1Id).getJSONObject(0).getJSONArray(imageField);
		
		System.out.println("before res1:  "+array);
		Assert.assertTrue("result should contain data size 2", array!=null&& array.length()==2);

		List<String> imgId = new ArrayList<String>();
		for(int i = 0 ; i < array.length();i++) {
			imgId.add(array.getJSONObject(i).getString("id"));
		}
		
		JSONObject res = dao.deleteImages(userId, imgId.toArray(new String[imgId.size()]));
		
		//search for tenant1Id ; must return null/empty
		//search
		JSONArray array2 = dao.getImageListByModelId(userId, true, tenant1Id,estate1Id);
		
		System.out.println("after res2:  "+array2);
		Assert.assertTrue("result should contain data size 0 instead of "+array2.length(), array2==null || array2.length()==0);

		
	}
	
	
	@Test
	public void testSearchNotes() throws Exception{
		
		String notesField = "notes";
		boolean active = true;
		JSONArray array = dao.getNotesListByModelId(userId, active, estate1Id)
				.getJSONObject(0).getJSONArray(notesField);
		
		Assert.assertTrue("result should contain data length 1", array!=null&& array.length()==1);
		
		array = dao.getNotesListByModelId(userId, active, tenant1Id)
				.getJSONObject(0).getJSONArray(notesField);
		
		Assert.assertTrue("result should contain data length 1", array!=null&& array.length()==1);

		array = dao.getNotesListByModelId(userId, active, estate1Id,tenant1Id)
				.getJSONObject(0).getJSONArray(notesField);
		
		Assert.assertTrue("result should contain data length 2", array!=null&& array.length()==2);
		
		array = dao.getNotesListByModelId(userId, active, estate2Id);
		
		Assert.assertFalse("result should NOT contain data", array!=null && array.length()>=1);
		
	}
	
	
	@Test
	public void testAddNotesData() throws JSONException, UnknownHostException{
		String notesField = "notes";
		//before
		JSONArray array = dao.getNotesListByModelId(userId, true, tenant1Id,estate1Id).getJSONObject(0).getJSONArray(notesField);
		
		System.out.println("before res1:  "+array);
		
		Assert.assertTrue("result should contain data size 2", array!=null&& array.length()==2);

		
		NotesModel note3 = NotesModel.newBuilder()
				.setNoteText("Note3")
				.setDate(System.currentTimeMillis())
				.setModelId(tenant1Id)
				.build();
		
		NotesModel note4 = NotesModel.newBuilder()
				.setNoteText("Note4")
				.setDate(System.currentTimeMillis())
				.setModelId(estate1Id)
				.build();
		
		JSONArray notesJSONArray = modelCodecFactory.getCodec(NotesModel.class).encode(Arrays.asList(note3,note4));

		//insert/push new to images array
		JSONObject res = dao.addNotes(userId, notesJSONArray);
		System.out.println("res :: "+res);
		
		//search
		array = dao.getNotesListByModelId(userId, true, tenant1Id,estate1Id).getJSONObject(0).getJSONArray(notesField);
		
		System.out.println("after res2:  "+array);
		Assert.assertTrue("result should contain data size 4 instead of "+ array.length(), array!=null&& array.length()==4);

		
		int found = 0;
		for (int i = 0 ; i < array.length(); i++) {
			JSONObject o = array.getJSONObject(i);
			System.out.println("o ::"+o);
			if(o.toString().contains("Note3")  || o.toString().contains("Note4") ){
				found++;
			}
		}
		
		Assert.assertTrue("expected 2 counts to be found instead of "+found, found==2);

	}
	
	@Test
	public void testDeleteNotesData() throws JSONException, UnknownHostException{
		String notesField = "notes";
		//before
		JSONArray array = dao.getNotesListByModelId(userId, true, tenant1Id,estate1Id).getJSONObject(0).getJSONArray(notesField);
		
		System.out.println("before res1:  "+array);
		Assert.assertTrue("result should contain data size 2", array!=null&& array.length()==2);

		//delete notes
		String text , modelId;
		Long notedate;
		for(int i = 0 ; i < array.length();i++) {
			text = array.getJSONObject(i).getString("noteText");
			notedate = Long.parseLong(array.getJSONObject(i).getString("date"));
			modelId = array.getJSONObject(i).getString("modelId");
			dao.deleteNotes(userId, modelId, notedate, text);
		}
			
		//search for notes ; must return null/empty
		//search
		JSONArray array2 = dao.getNotesListByModelId(userId, true, tenant1Id,estate1Id);
		
		System.out.println("after res2:  "+array2);
		Assert.assertTrue("result should contain data size 0 instead of "+array2.length(), array2==null || array2.length()==0);

	}
	
	/***
	 * UserId has many walletAdresses
	 * @throws JSONException */
	@Test
	public void createUserId() throws JSONException {
		
		dao.dropCollection(INTERNAL_COLLECTION);
		dao.setCollectionName(INTERNAL_COLLECTION);
		String username = "myonlineid";
		String userId="123456789";
		String username2 = "myonlineid2";
		//String[] walletAddresses = {"walletaddress1","walletaddress2"};
		
		JSONObject res = dao.upsertUserName(userId,username);
		System.out.println(res);
		
		//verify
		JSONArray res1 = dao.getUserName(userId);
		System.out.println(res1);
		Assert.assertTrue("expects userName ", res1.toString().contains(username));

		
		//update
		res = dao.upsertUserName(userId,username2);
		System.out.println(res);
		
		//verify update
		res1 = dao.getUserName(userId);
		System.out.println(res1);
		Assert.assertTrue("expects userName2 ", res1.toString().contains(username2));
		
		//
	}
	
	@Test
	public void getUserIdByWalletInfo() throws JSONException {
		
		JSONArray res = dao.getUserId(walletAddress, phoneNumber, userEmail, userName);
		System.out.println(res);
		Assert.assertTrue("result is not null", res!=null && res.length()>0);
		Assert.assertTrue("expected exactly one result instead of "+res.length(),  res.length()==1 );
		Assert.assertTrue("userId expected = "+userId, res.getJSONObject(0).getString("Customer").contentEquals(userId));
	
		//null username
		res = dao.getUserId(walletAddress, phoneNumber, userEmail, null);
		System.out.println(res);
		Assert.assertTrue("result is not null", res!=null && res.length()>0);
		Assert.assertTrue("expected exactly one result instead of "+res.length(),  res.length()==1 );
		Assert.assertTrue("userId expected = "+userId, res.getJSONObject(0).getString("Customer").contentEquals(userId));
	
		//wrong username - username not used
		//res = dao.getUserId(walletAddress, phoneNumber, userEmail, "someusrname");
		//System.out.println(res);
		//Assert.assertTrue("result expected null", res==null ||  res.length()==0);
		
		//wrong email and number 
		res = dao.getUserId(walletAddress, phoneNumber, "someemail@wherever.co", userName);
		System.out.println(res);
		Assert.assertTrue("result expected null", res==null ||  res.length()==0);
		
		res = dao.getUserId(walletAddress, "1235554444", userEmail, userName);
		System.out.println(res);
		Assert.assertTrue("result expected null", res==null ||  res.length()==0);
		
		res = dao.getUserId(walletAddress, null, userEmail, userName);
		System.out.println(res);
		Assert.assertTrue("result expected null", res==null ||  res.length()==0);
		
	}
}
