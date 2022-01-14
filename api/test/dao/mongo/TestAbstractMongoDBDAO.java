package dao.mongo;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Before;
import org.junit.Test;

import com.google.common.base.Charsets;
import com.google.gson.Gson;
import com.google.protobuf.ByteString;
import com.mongodb.BasicDBObject;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import static com.mongodb.client.model.Projections.excludeId;

import java.util.UUID;

import io.diy.dao.AbstractEatMongoDAO;
import io.mtini.proto.eat.EstateAccountProtos;
import io.mtini.proto.eat.EstateAccountProtos.TenantStatus;
import io.mtini.proto.eat.EstateAccountProtos.LedgerEntries.EstateModel;
import io.mtini.proto.eat.EstateAccountProtos.LedgerEntries.EstateModel.TenantModel;

public class TestAbstractMongoDBDAO {
	
	AbstractEatMongoDAO dao ;
	
	private static String CUSTOMER_COLLECTION1 = "TestCollection1";
	private static String userId = "CustomerId1";
	
	//private static ByteString estate1Id = ByteString.copyFrom("estateId1",Charsets.UTF_8);//ByteString.copyFrom(Base64.getEncoder().encode(UUID.randomUUID().toString().getBytes()));
	//private static ByteString estate2Id = ByteString.copyFrom("estateId2",Charsets.UTF_8);//ByteString.copyFrom(Base64.getEncoder().encode(UUID.randomUUID().toString().getBytes()));
	//private static ByteString tenant1Id = ByteString.copyFrom("tenantId1",Charsets.UTF_8);//ByteString.copyFrom(Base64.getEncoder().encode(UUID.randomUUID().toString().getBytes()));

	private static String estate1Id = UUID.randomUUID().toString();
	private static String estate2Id = UUID.randomUUID().toString();
	private static String tenant1Id = UUID.randomUUID().toString();

	
	@Before
	public void init() {
		
		
		 dao = new AbstractEatMongoDAO() {

			@Override
			protected void initGetNotesListByModelId(String userId, Object ... modelIds) {
				
				searchQuery = new BasicDBObject("Customer",userId);
				/*searchQuery = Filters.and(new BasicDBObject("Customer",userId),
						Filters.or(
								Filters.in("estateData_.id", modelIds),
								Filters.in("estateData_.tenantData_.id_", modelIds)
						));*/
				
				searchProjections = Projections.fields( 
								Projections.include("estateData_.tenantData_.notes_"),
								Projections.include("estateData_.notes_"),
								excludeId() );
			}

			@Override
			protected void initGetImageListByModelId(String userId, Object ... modelIds) {
				
				searchQuery = Filters.and(new BasicDBObject("Customer",userId),
				Filters.or(
						Filters.in("estateData_.id_", modelIds),
						Filters.in("estateData_.tenantData_.id_", modelIds)
				));
		
				searchProjections= Projections.fields( 
						Projections.include("estateData_.tenantData_.images_"),
						Projections.include("estateData_.images_"),
						excludeId() );
			}

			@Override
			protected void initGetWallet(String userId) {
				searchQuery = Filters.and(new BasicDBObject("Customer",userId));
		
				searchProjections= Projections.fields( 
						Projections.include("wallet"),
						excludeId() );
			}

			@Override
			protected void initUpdateNotesConfig() {
				// TODO Auto-generated method stub
				
			}

			@Override
			protected void initUpdateImagesConfig() {
				// TODO Auto-generated method stub
				
			}

			@Override
			protected void initUpdateTenantConfig() {
				// TODO Auto-generated method stub
				
			}

			@Override
			protected void initUpdateEstateConfig() {
				// TODO Auto-generated method stub
				
			}
			
		};
		
		dao.addCredential("testdb", "testuser", "testpass".toCharArray());
		dao.addServerAddress("127.0.0.1", 27017);
		dao.connectDatabase("TestDB");
		
		dao.setCollectionName(CUSTOMER_COLLECTION1);
		
		dao.dropCollection(CUSTOMER_COLLECTION1);


	}
	
	
	@Test
	public void testInsertData() throws Exception{

		//MongoCollection<Document> collection = dao.findCollection(CUSTOMER_COLLECTION1);
		
		EstateAccountProtos.Operation operation = EstateAccountProtos.Operation.ADD_ESTATE;
		EstateAccountProtos.LedgerEntries entries = EstateAccountProtos.LedgerEntries.newBuilder()
				.addEstateData(
						EstateModel.newBuilder()
						//.setId(ByteString.copyFrom(UUID.randomUUID().toString().getBytes()))
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
								)
						)
				.addEstateData(
						EstateModel.newBuilder()
						//.setId(ByteString.copyFrom(UUID.randomUUID().toString().getBytes()))
						.setId(estate2Id)
						.setName("House 1")
						.setType(EstateAccountProtos.EstateType.house)
						.setAddress("Some Address")
						.setContacts("0705551212")
						.setDescription("Is my house")
						)
				.setOperation(operation)
				.build();
		// entries.toByteString();

		//String modelType = "estate";
		
		Gson gson = new Gson();
		/*for(int i = 0 ; i < entries.getEstateDataCount(); i++){
			EstateModel estate  = entries.getEstateData(i);
			String estateData = gson.toJson(estate);//JsonFormat.printer().print(estate);

			JSONObject estateJsonData = new JSONObject(estateData);
			estateJsonData.put("timestamp", System.currentTimeMillis());
			estateJsonData.put("model_type", modelType);
			
			System.out.println(estateJsonData.toString());

			//Document mongoDoc = DocumentJSONCodec.encode(estateJsonData);
			
			JSONObject result = dao.putDocument(CUSTOMER_COLLECTION1, modelType, estate.getId().toString(), estateJsonData);

			//JSONObject res = dao.putRecord(estateJsonData.toString(), esIndex, modelType, UUID.nameUUIDFromBytes(estate.getId().toByteArray()) );
			//JSONObject res = dao.putDocument(userId, "estateList", UUID.nameUUIDFromBytes(estate.getId().toByteArray()).toString(), estateJsonData);
			
			System.out.println(result.toString());
		}*/
		
		String modelType = "entries";
		
		String entriesData = gson.toJson(entries);
		JSONObject entriesJsonData = new JSONObject(entriesData);
		entriesJsonData.put("timestamp", System.currentTimeMillis());
		entriesJsonData.put("model_type", modelType);
		
		JSONObject result = dao.putDocument(CUSTOMER_COLLECTION1, modelType ,userId , entriesJsonData);

		System.out.println("inserted -> "+result.toString());
	}
	
	@Test
	public void testSearchNotes() throws Exception{
		this.testInsertData();
		
		boolean active = true;
		JSONArray array = dao.getNotesListByModelId(userId, active, estate1Id);
		System.out.println(array);
	}
	
	
	@Test
	public void testSearchImages() throws Exception{
		this.testInsertData();
		
		boolean active = true;
		JSONArray array = dao.getImageListByModelId(userId, active, null);
		System.out.println(array);
	}
	

}
