package io.awesome.dao;

import static com.mongodb.client.model.Projections.excludeId;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.log4j.Logger;
import org.bson.BsonValue;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.google.gson.Gson;
import com.mongodb.BasicDBObject;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.Updates;
import com.mongodb.client.result.UpdateResult;
import com.mongodb.client.model.Aggregates;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.Accumulators;
import com.prelimtek.client.mongodb.DocumentJSONCodec;
import com.prelimtek.client.mongodb.MongoDbDAOImpl;

public class ApiMongoDAOImpl extends MongoDbDAOImpl {
	static Logger log = Logger.getLogger(ApiMongoDAOImpl.class);

	protected static ApiMongoDAOImpl instance;

	public static ApiMongoDAOImpl instance() {
		if (instance == null) {
			instance = new ApiMongoDAOImpl();
		}
		return instance;
	}

	private ApiMongoDAOImpl() {
		super();
	}

	
	public JSONArray getTenantListByEstateId(String collectionName, String userId, boolean active, String... estateIds)
			throws JSONException {

		MongoCollection<Document> collection = findCollection(collectionName);

		Bson query = Filters.and(Filters.eq("Customer",userId), Filters.eq("type","entries"), // Filters.and(new BasicDBObject("Customer",userId),
				Filters.or(Filters.in("estateData.id", estateIds),
						Filters.in("estateData.tenantData.estateId", estateIds)));

		Iterable<Document> res = collection.find(query)
				.projection(Projections.fields(Projections.include("estateData.tenantData"), excludeId()));

		JSONArray ret = DocumentJSONCodec.decode(res);
		
		log.debug("getTenantListByEstateId = "+ret);
		
		return ret;
	}

	public JSONArray getTenantListById(String userId, Object... tenantIds)
			throws JSONException {

		MongoCollection<Document> collection = findCollection(collectionName);

		Bson query = Filters.and(Filters.eq("Customer",userId), Filters.eq("type","entries")
				// ,Filters.in("estateData.tenantData.id", modelIds)
				);

		Bson match = Filters.in("estateData.tenantData.id", tenantIds);

		Bson projection = Projections.fields(Projections.include("estateData.tenantData"), excludeId());

		Iterable<Document> res = collection.aggregate(Arrays.asList(

				// where
				Aggregates.match(query), // ,

				// select
				Aggregates.project(projection),

				Aggregates.unwind("$estateData"),
				Aggregates.unwind("$estateData.tenantData"),

				Aggregates.match(match),

				// group by
				Aggregates.group("$_id.estateData.tenantData", Accumulators.push("tenantData", "$estateData.tenantData"))

				));

		res.forEach(d -> System.out.println("getTenantListById   : "+d));
		
		JSONArray ret = DocumentJSONCodec.decode(res);
		
		log.debug("getTenantListById = "+ret);
		
		return ret;
	}

	
	public JSONArray getEstateListByIndex(String userId, boolean active) throws Exception {

		MongoCollection<Document> collection = findCollection(collectionName);

		Bson query = Filters.and(Filters.eq("Customer",userId), Filters.eq("type","entries"));

		Iterable<Document> res = collection.find(query).projection(
				Projections.fields(
						Projections.include("estateData"),
						excludeId()));

		JSONArray ret = DocumentJSONCodec.decode(res);
		
		log.debug("getEstateListByIndex = "+res);
		
		return ret;
	}

	
	public JSONArray getImageListByModelId(String userId, boolean active, Object... modelIds)
			throws JSONException {

		MongoCollection<Document> collection = findCollection(collectionName);

		Bson query = Filters.and(Filters.eq("Customer",userId), Filters.eq("type","entries")
				// ,Filters.in("images_.modelId_", modelIds)
				);

		/*
		 * Bson match = Filters.elemMatch( "images_", Filters.in("modelId_",modelIds )
		 * );
		 */

		Bson match = Filters.in("images.modelId", modelIds);

		Bson projection = Projections.fields(Projections.include("images"), excludeId());

		Iterable<Document> res = collection.aggregate(Arrays.asList(

				// where
				Aggregates.match(query), // ,

				// select
				Aggregates.project(projection),

				Aggregates.unwind("$images"),

				Aggregates.match(match),

				// group by
				Aggregates.group("$_id.images", Accumulators.push("images", "$images"))

				));
		
		JSONArray ret =DocumentJSONCodec.decode(res);
		
		log.debug("getImageListByModelId size="+ret.length());
		
		return ret;
	}

	public JSONArray getImageList(String userId, boolean active) throws JSONException {

		MongoCollection<Document> collection = findCollection(collectionName);

		Bson query = Filters.and(Filters.eq("Customer",userId), Filters.eq("type","entries"));

		Iterable<Document> res = collection.find(query).projection(Projections.fields(Projections.include("images"),
				// Projections.include("images_.id_"),
				// Projections.include("images_.modelId_"),
				excludeId()));

		JSONArray ret = DocumentJSONCodec.decode(res);
		
		log.debug("getImageList size= "+ret.length());
		
		return ret;
	}

	
	@Deprecated //Use updateEstateData
	public JSONArray addEstateData(@Nonnull String userId, @Nonnull String updateField,
			@Nonnull JSONArray estateDataJson, boolean searchResult) throws JSONException, IOException {

		JSONArray ret = new JSONArray();
		for (int i = 0; i < estateDataJson.length(); i++) {
			JSONObject estateData = estateDataJson.getJSONObject(i);

			setUpdateQuery(Filters.and(Filters.eq("Customer",userId), Filters.eq("type","entries")));

			JSONObject res = updateDocument(userId, updateField, userId, estateData);

			Bson projection = Projections.fields( 
							Projections.include("userName"),
							excludeId()
							);
					
			if (searchResult) {
				JSONArray searchRes = searchCollectionByQuery(updateQuery,projection);

				for (int j = 0; j < searchRes.length(); j++) {
					ret.put(searchRes.get(j));
				}
			} else {
				ret.put(res);
			}

		}
		
		log.debug("addEstateData = "+ret);
		
		return ret;
	}

	public JSONArray searchCollectionByQuery(Bson query, Bson projection) {

		MongoCollection<Document> collection = findCollection(collectionName);

		FindIterable<Document> retIt = collection.find(query).projection(projection);

		/*
		 * ((Iterable<Document>)retIt).forEach(docr -> { try {
		 * System.out.println("searchCollectionByQuery doc  :  "+docr.toJson());
		 * System.out.println("searchCollectionByQuery json :  "+new
		 * JSONObject(docr.toJson())); } catch (JSONException e) { // TODO
		 * Auto-generated catch block e.printStackTrace(); }});
		 */
		JSONArray ret =DocumentJSONCodec.decode(retIt);
		
		log.debug("searchCollectionByQuery = "+ret);
		
		return ret;
	}

	@Deprecated
	public JSONArray updateEstateData(@Nonnull String userId, @Nonnull String updateField,
			@Nonnull JSONArray estateDataJson, boolean searchResult) throws JSONException, IOException {

		JSONArray ret = new JSONArray();
		for (int i = 0; i < estateDataJson.length(); i++) {
			JSONObject estateData = estateDataJson.getJSONObject(i);
			String id = estateData.getString("id");

			JSONObject res = updateEstateData(userId, updateField, id, estateData, false);
			ret.put(i, res);
		}
		
		log.debug("updateEstateData = "+ret);
		
		return ret;
	}

	@Deprecated
	public JSONObject updateEstateData(@Nonnull String userId, @Nonnull String updateField, @Nonnull String estateId,
			@Nonnull JSONObject estateDataJson,boolean searchResult) throws JSONException, IOException {

		setUpdateQuery(Filters.and(Filters.eq("Customer",userId), Filters.eq("type","entries"), Filters.eq("estateData.id", estateId),
				Filters.exists("estateData", true)));

		MongoCollection<Document> collection = findCollection(collectionName);

		Document doc = DocumentJSONCodec.encode(estateDataJson);

		Object result = collection.find(updateQuery);
		if(result!=null && ((FindIterable)result).first()!=null) {
			result = collection.updateOne(updateQuery, Updates.set(updateField, doc));
		}else {
			result = collection.updateOne(Filters.and(Filters.eq("Customer",userId), Filters.eq("type","entries")), Updates.push(updateField, doc));
		}
		
		/*
		if (!searchResult) {
			result = collection.findOneAndUpdate(updateQuery, Updates.set(updateField, doc));
		} else {
			result = collection.updateOne(updateQuery, Updates.push(updateField, doc));
		}
		 */
		
		JSONObject ret = null;
		if (result instanceof UpdateResult) {
			FindIterable<Document> retIt = collection.find(updateQuery);
			/*
			 * ((Iterable<Document>)retIt).forEach(docr ->
			 * System.out.println("update estate find res : "+docr));
			 */
			ret=  DocumentJSONCodec.decode(retIt.first());
		} else {
			ret =  DocumentJSONCodec.decode((Document) result);
		}
		
		log.debug("updateEstateData = "+ret);
		
		return ret;

	}
	
	
	public JSONObject upsertEstateData(@Nonnull String userId, @Nonnull String estateId,
			@Nonnull JSONObject estateDataJson) throws JSONException, IOException {
		
		String updateField = "estateData";
		
		setUpdateQuery(
				Filters.and(
						Filters.eq("Customer",userId), 
						Filters.eq("type","entries"), 
						Filters.exists("estateData", true)
						)
				);

		Bson searchQuery = Filters.and(
				Filters.eq("Customer",userId), 
				Filters.eq("type","entries"), 
				Filters.exists("estateData", true),
				Filters.eq("estateData.id", estateId)
				);
		
		MongoCollection<Document> collection = findCollection(collectionName);

		Document doc = DocumentJSONCodec.encode(estateDataJson);

		Object retResult =null;
			
		JSONArray estatelist = this.getEstateListByEstateIds(userId, estateId, false);
		if(estatelist!=null && estatelist.length()>0 && estatelist.getJSONObject(0).has("estateData") && 
				estatelist.getJSONObject(0).getJSONArray("estateData").length() > 0) {

			
			log.debug("upsertEstateData update ");
			//System.out.println("upsertEstateData update ");
			
			JSONObject oldEstate = estatelist.getJSONObject(0).getJSONArray("estateData").getJSONObject(0);
			Document oldDoc = DocumentJSONCodec.encode(oldEstate);
			System.out.println("upsertEstateData update oldEstate : "+oldDoc);
			
			//delete
			retResult = collection.updateOne(searchQuery, Updates.pull("estateData", oldDoc));
			//System.out.println("upsertEstateData update pull res : "+retResult);

			//insert
			retResult = collection.updateOne(updateQuery, Updates.push("estateData", doc));
			//System.out.println("upsertEstateData update retResult = "+retResult);
		}else {
			
			searchQuery = Filters.and(
					Filters.eq("Customer",userId), 
					Filters.eq("type","entries"),
					Filters.exists("estateData", true)
					);
			
			Iterable<Document> result  = collection.find(searchQuery);
			
			//((Iterable<Document>)result).forEach(p -> System.out.println("upsertEstateData find result 2 -  "+p));
			
			if(result!=null && (((FindIterable<Document>)result).first()!=null 
				&& ((FindIterable<Document>)result).first().containsKey(updateField))) {
				
				log.debug("upsertEstateData push new");
				//System.out.println("upsertEstateData push new ");
				retResult = collection.updateOne(searchQuery, Updates.push("estateData", doc));
				
			}else {
				
				log.debug("upsertEstateData new list ");
				//System.out.println("upsertEstateData new list ");
				Document mainDoc = new Document("Customer",userId);
				mainDoc.put("type","entries");
				mainDoc.put("estateData",Arrays.asList(doc));	

				collection.insertOne(mainDoc);
				
				retResult = collection.updateOne(searchQuery, Updates.set("estateData.$", doc));
					
			}
			
			
		}
		
		JSONObject ret = null;
		if (retResult instanceof UpdateResult) {
			ret=  DocumentJSONCodec.decode((UpdateResult)retResult);
		} else {
			ret =  DocumentJSONCodec.decode((Document) retResult);
		}
		
		log.debug("upsertEstateData = "+ret);
		//System.out.println("upsertEstateData = "+ret);
		return ret;

	}
	
	
	
	public JSONArray getEstateListByEstateIds(@Nonnull String userId, String estateIds, boolean includeId) throws JSONException {

		MongoCollection<Document> collection = findCollection(collectionName);

		Bson query = Filters.and(
				Filters.eq("Customer",userId), 
				Filters.eq("type","entries"),
				Filters.exists("estateData", true)
				);

		Bson match = Filters.eq("estateData.id", estateIds);

		Bson projection = includeId ? Projections.fields(Projections.include("estateData") )
				: Projections.fields(Projections.include("estateData"), excludeId());

		Iterable<Document> res = collection.aggregate(Arrays.asList(

				// where
				Aggregates.match(query),

				// select
				Aggregates.project(projection),

				Aggregates.unwind("$estateData"),
				//Aggregates.unwind("$estateData.id"),

				Aggregates.match(match),

				// group by
				Aggregates.group("$_id", Accumulators.push("estateData", "$estateData")),
				
				Aggregates.project(projection)

				));

		
		//res.forEach(d -> System.out.println("getEstateListByEstateIds   : "+d));
		
		JSONArray ret = DocumentJSONCodec.decode(res);
		
		log.debug("getEstateListByEstateIds = "+ret);
		//System.out.println("getEstateListByEstateIds ret = "+ret);
		
		return ret;
	}

	public JSONObject addImages(@Nonnull String userId, @Nonnull JSONArray imageJsonArray) throws JSONException {

		List<Document> imageDocs = DocumentJSONCodec.encode(imageJsonArray);

		Bson query = Filters.and(Filters.eq("Customer",userId), Filters.eq("type","entries"));

		MongoCollection<Document> collection = findCollection(collectionName);

		/*
		 * Document result = collection.findOneAndUpdate(query,
		 * Updates.pushEach("images_" , imageDocs));
		 */
		UpdateResult result = collection.updateOne(query, Updates.pushEach("images", imageDocs));
		
		JSONObject ret =  DocumentJSONCodec.decode(result);
		
		log.debug("addImages = "+ret);
		
		return ret;
	}

	public JSONObject deleteImages(@Nonnull String userId, @Nonnull String... imageIds) throws JSONException {

		Bson query = Filters.and(Filters.eq("Customer",userId), Filters.eq("type","entries"), Filters.in("images.id", imageIds));

		MongoCollection<Document> collection = findCollection(collectionName);

		UpdateResult result = collection.updateOne(query, Updates.pull("images", Filters.in("id", imageIds)));
		
		JSONObject ret =  DocumentJSONCodec.decode(result);
		
		log.debug("deleteImages = "+ret);
		
		return ret;
	}

	@Deprecated
	public JSONObject updateImages(@Nonnull String userId, @Nonnull String updateField, @Nonnull String imageId,
			@Nonnull JSONObject imageJson) throws JSONException, IOException {
		setUpdateQuery(Filters.and(Filters.eq("Customer",userId), Filters.eq("type","entries"),

				Filters.in("images.id", imageId)

				));
		return updateDocument(userId, updateField, imageId, imageJson);
	}

	@Override
	public JSONArray getNotesListByModelId(String userId, boolean active, Object... modelIds)
			throws JSONException {

		MongoCollection<Document> collection = findCollection(collectionName);

		Bson query = Filters.and(Filters.eq("Customer",userId), Filters.eq("type","entries"));

		Bson match = Filters.in("notes.modelId", modelIds);

		Bson projection = Projections.fields(Projections.include("notes"), excludeId());

		Iterable<Document> res = collection.aggregate(Arrays.asList(

				// where
				Aggregates.match(query), // ,

				// select
				Aggregates.project(projection),

				Aggregates.unwind("$notes"),

				Aggregates.match(match),

				// group by
				Aggregates.group("$_id.notes", Accumulators.push("notes", "$notes")),

				//order
				Aggregates.sort(Sorts.descending("notes.date"))

				));

		System.out.println("getNotes collection");
		res.forEach(doc -> System.out.println("res " + doc.toJson()));

		JSONArray ret =  DocumentJSONCodec.decode(res);
		
		log.debug("getNotesListByModelId = "+ret);
		
		return ret;
	}

	public JSONArray getAllNotesList(String userId)
			throws JSONException {

		MongoCollection<Document> collection = findCollection(collectionName);

		Bson query = Filters.and(Filters.eq("Customer",userId), Filters.eq("type","entries"));

		Bson projection = Projections.fields(Projections.include("notes"), excludeId());

		Iterable<Document> res = collection.aggregate(Arrays.asList(

				// where
				Aggregates.match(query), // ,

				// select
				Aggregates.project(projection),

				Aggregates.unwind("$notes"),

				// group by
				Aggregates.group("$_id.notes", Accumulators.push("notes", "$notes")),

				//order
				Aggregates.sort(Sorts.descending("notes.date"))

				));

		System.out.println("getNotes collection");
		res.forEach(doc -> System.out.println("res " + doc.toJson()));
		
		JSONArray ret =  DocumentJSONCodec.decode(res);
		
		log.debug("getAllNotesList = "+ret);
		
		return ret;
	}

	public JSONObject addNotes(@Nonnull String userId, @Nonnull JSONArray notesJSONArray) throws JSONException {

		List<Document> notesDocs = DocumentJSONCodec.encode(notesJSONArray);

		Bson query = Filters.and(Filters.eq("Customer",userId), Filters.eq("type","entries"));

		MongoCollection<Document> collection = findCollection(collectionName);

		UpdateResult result = collection.updateOne(query, Updates.pushEach("notes", notesDocs));
		
		JSONObject ret = DocumentJSONCodec.decode(result);
		
		log.debug("addNotes = "+ret);
		
		return ret;
	}

	@Deprecated // Delete the note then add
	public JSONObject updateNotes(@Nonnull String userId, @Nonnull String updateField, @Nonnull String notesId,
			@Nonnull JSONObject notesJson) throws JSONException, IOException {

		setUpdateQuery(Filters.and(Filters.eq("Customer",userId), Filters.eq("type","entries"), // Filters.and(new BasicDBObject("Customer",userId),
				Filters.or(Filters.in("estateData.notes.id", notesId),
						Filters.in("estateData.tenantData.notes.id", notesId))));

		return updateDocument(userId, updateField, notesId, notesJson);
	}

	public JSONObject deleteNotes(@Nonnull String userId, @Nonnull String modelId, @Nullable Long notedate,
			@Nonnull String text) throws JSONException {

		MongoCollection<Document> collection = findCollection(collectionName);

		Bson query = Filters.and(Filters.eq("Customer",userId), Filters.eq("type","entries"), Filters.eq("notes.modelId", modelId),
				Filters.eq("notes.noteText", text),
				notedate != null ? Filters.eq("notes.date", notedate.toString()) : Filters.exists("notes.date", true)

				);

		Bson match = Filters.and(Filters.eq("modelId", modelId), Filters.eq("noteText", text),
				notedate != null ? Filters.eq("date", notedate.toString()) : Filters.exists("date", true));

		Document result = collection.findOneAndUpdate(query, Updates.pull("notes", match));
		
		log.debug("deleteNotes = "+result);
		
		return DocumentJSONCodec.decode(result);
	}

	public JSONObject deleteEstateData(@Nonnull String userId, @Nonnull String estateId) throws JSONException {

		Bson query = Filters.and(Filters.eq("Customer",userId), Filters.eq("type","entries"), Filters.eq("estateData.id", estateId));

		MongoCollection<Document> collection = findCollection(collectionName);

		UpdateResult result = collection.updateMany(query, Updates.pull("estateData", new Document("id", estateId)));
		
		JSONObject ret = DocumentJSONCodec.decode(result);
		
		log.debug("deleteEstateData = "+ret);
		
		return ret;
	}

	@Deprecated
	public JSONObject deleteTenantData(@Nonnull String userId, @Nonnull String tenantId) throws JSONException {

		Bson query = Filters.and(Filters.eq("Customer",userId), Filters.eq("type","entries"), Filters.eq("estateData.tenantData.id", tenantId));

		MongoCollection<Document> collection = findCollection(collectionName);

		UpdateResult result = collection.updateOne(query,
				Updates.pull("estateData.$.tenantData", new Document("id", tenantId)));
		
		JSONObject ret =  DocumentJSONCodec.decode(result);
		
		log.debug("deleteTenantData = "+ret);
		
		return ret;
	}
	
	public JSONObject deleteTenantData(@Nonnull String userId, @Nonnull String estateId, @Nonnull String tenantId) throws JSONException {

		Bson query = Filters.and(Filters.eq("Customer",userId), Filters.eq("type","entries")
				//, Filters.eq("estateData.id", estateId)//causing issues
				,Filters.eq("estateData.tenantData.id", tenantId));

		MongoCollection<Document> collection = findCollection(collectionName);

		UpdateResult result  = null;
		JSONArray tenantList = this.getTenantListById(userId, tenantId);
		if(tenantList.length()==0)
			result = UpdateResult.acknowledged(1L, 1L,null);
		else {
			result = collection.updateOne(query,
				Updates.pull("estateData.$.tenantData", new Document("id", tenantId)));
		}
		JSONObject ret =  DocumentJSONCodec.decode(result);
		
		log.debug("deleteTenantData = "+ret);
		
		return ret;
	}

	public JSONObject addTenantData(@Nonnull String userId, @Nonnull String estateId, @Nonnull JSONArray tenantJson)
			throws JSONException {

		Bson query = Filters.and(Filters.eq("Customer",userId), Filters.eq("type","entries"), Filters.eq("estateData.id", estateId));

		MongoCollection<Document> collection = findCollection(collectionName);

		List<Document> docs = DocumentJSONCodec.encode(tenantJson);

		//Document result = collection.findOneAndUpdate(query, Updates.pushEach("estateData.$.tenantData", docs));
		UpdateResult result = collection.updateOne(query, Updates.pushEach("estateData.$.tenantData", docs));
		
		JSONObject ret = DocumentJSONCodec.decode(result);
		
		log.debug("addTenantData = "+ret);
		
		return ret;
	}

	public JSONArray updateTenantData(@Nonnull String userId, @Nonnull String estateId, @Nonnull JSONArray tenantArray)
			throws JSONException {

		JSONArray ret = new JSONArray();

		for(int i = 0 ; i < tenantArray.length(); i++) {
			JSONObject tenant = tenantArray.getJSONObject(i);
			ret.put(upsertTenantData(userId,estateId, tenant.getString("id"),tenant));
		}
		
		log.debug("updateTenantData 1 = "+ret);
		
		return ret;

	}

	public JSONObject upsertTenantData(@Nonnull String userId, @Nonnull String estateId,@Nonnull String tenantId, @Nonnull JSONObject tenantJson)
			throws JSONException {

		JSONArray array = new JSONArray();
		array.put(tenantJson);
		JSONObject delRes=null;
		JSONArray tenantList = this.getTenantListById(userId, tenantId);
		if(tenantList.length()==0) {
			//add
			delRes = this.addTenantData(userId, estateId,array);
			log.debug("addTenantData = "+delRes);
		}else {
			//update
			delRes = deleteTenantData(userId,estateId,tenantId);
			log.debug("deleteTenantData = "+delRes);
			if(delRes.has("modifiedCount") && delRes.getInt("modifiedCount")==1) {
				delRes = addTenantData(userId,estateId,array );
			}
		}
		 
		log.debug("updateTenantData = "+delRes);
		return delRes;
	}

	@Deprecated
	public JSONObject upsertWalletData(@Nonnull String userId, @Nonnull String id1, @Nonnull String id2 , @Nonnull JSONObject wallet)
			throws JSONException, IOException {

		Bson query = Filters.and(Filters.eq("Customer",userId), Filters.eq("type","wallets") );
		Bson pullfilter =  Filters.and(Filters.in("id", id1,id2), Filters.in("id2", id1,id2));

		MongoCollection<Document> collection = findCollection(collectionName);

		Document doc = DocumentJSONCodec.encode(wallet);

		UpdateResult result = collection.updateOne(query, Updates.pullByFilter(pullfilter));

		System.out.println("upsertWalletData pull res :: "+result);
		if(result.getMatchedCount()>=1 && result.getModifiedCount()>=1) {
			result = collection.updateOne(query, Updates.push("walletData", doc));
			//System.out.println("upsertWalletData push res :: "+result);
		}


		JSONObject ret = null;

		if(result==null || result.getModifiedCount()==0 ) {

			Document mainDoc = new Document("Customer",userId);
			mainDoc.put("type", "wallets");
			mainDoc.put("walletData", Arrays.asList(doc));
			//ret  = this.putDocument(userId, "wallets", incoming);
			collection.insertOne(mainDoc);
			ret = DocumentJSONCodec.decode(UpdateResult.acknowledged(1L, 1L, null));

			//System.out.println("upsertWalletData ret :: "+ret);
		}else {
			ret = DocumentJSONCodec.decode(result);
		}

		return ret;
	}

	@Deprecated
	public JSONArray getWallet(@Nonnull String userId, @Nonnull String id1 ,@Nonnull String id2) throws JSONException {

		MongoCollection<Document> collection = findCollection(collectionName);

		Bson query = Filters.and(
				Filters.eq("Customer",userId),
				Filters.eq("type","wallets"),
				//Filters.or(
				Filters.in("walletData.id", id1,id2),
				Filters.in("walletData.id2", id1,id2)
				//		)
				//,Filters.exists("walletData",true)
				);

		Iterable<Document> res = collection.find(query)
				.projection(
						Projections.fields( 
								Projections.include("walletData"),
								excludeId()
								));

		JSONArray ret = DocumentJSONCodec.decode(res);

		return ret;
	}

	public JSONObject upsertWalletData(@Nonnull String userId, @Nonnull String address, @Nonnull JSONObject wallet)
			throws JSONException, IOException {

		Document walletDoc = DocumentJSONCodec.encode(wallet);

		MongoCollection<Document> collection = findCollection(collectionName);

		Bson query = Filters.and(Filters.eq("Customer",userId), Filters.eq("type","wallets") );	

		FindIterable<Document> findret = collection.find(query);

		UpdateResult result = null;
		if(findret == null || findret.first() ==null) {
			Document mainDoc = new Document("Customer",userId);
			mainDoc.put("type", "wallets");
			mainDoc.put(address, walletDoc);

			collection.insertOne(mainDoc);
			//result = UpdateResult.acknowledged(1L, 1L, null);
			//this verifies insert worked
			result = collection.updateOne(query,Updates.set(address, walletDoc));

			System.out.println("upsertWalletData 1 res :: "+result);

		}else{

			result = collection.updateOne(query,Updates.set(address, walletDoc));

			System.out.println("upsertWalletData 2 ret :: "+result);
		}
		
		JSONObject ret =  DocumentJSONCodec.decode(result);
		
		log.debug("upsertWalletData = "+ret);
		
		return ret;

	}

	public JSONArray getWallet(@Nonnull String userId, @Nonnull String address ) throws JSONException {

		MongoCollection<Document> collection = findCollection(collectionName);

		Bson query = Filters.and(
				Filters.eq("Customer",userId),
				Filters.eq("type","wallets"),
				Filters.exists(address, true)
				);

		FindIterable<Document> res = collection.find(query)
				.projection(
						Projections.fields( 
								Projections.include(address),
								excludeId()
								)
						);
		
		JSONArray ret = DocumentJSONCodec.decode(res);
		
		log.debug("getWallet = "+ret);
		
		return ret;
	}

	public JSONObject putDocument(String userId, String type, JSONObject jsondata)
			throws JSONException, IOException {

		Document userData ;
		//search type, id
		//append to data
		MongoCollection<Document> collection = findCollection(collectionName);

		//prequery
		Bson query = Filters.and(new BasicDBObject("Customer",userId),
				Filters.eq("type",type));

		//data
		Document doc = new Document("type",type);
		Map data = new Gson().fromJson(jsondata.toString(),Map.class);
		doc.putAll(data);


		FindIterable<Document> res = collection.find(query);
		if(res.iterator().hasNext()) {
			userData = collection.findOneAndUpdate(query, doc);
		}else {
			userData = new Document("Customer",userId);
			userData.putAll( doc);

			insertData(collectionName, userData );
		}
		
		JSONObject ret =  DocumentJSONCodec.decode(userData);
		
		log.debug("putDocument = "+ret);
		
		return ret;
	}

	public JSONObject upsertUserName(@Nonnull String userId,@Nonnull String username) throws JSONException {
		// TODO Auto-generated method stub
		MongoCollection<Document> collection = findCollection(collectionName);
		
		Bson query = Filters.and(Filters.eq("Customer",userId), Filters.eq("type","wallets") );

		FindIterable<Document> findret = collection.find(query);

		UpdateResult result = null;
		if(findret == null || findret.first() ==null) {
			Document mainDoc = new Document("Customer",userId);
			mainDoc.put("type", "wallets");
			mainDoc.put("userName", username);

			collection.insertOne(mainDoc);
			result = UpdateResult.acknowledged(1L, 1L, null);

		}else{

			result = collection.updateOne(query,Updates.set("userName", username));

			System.out.println("upsertUserName 2 ret :: "+result);
		}

		
		JSONObject ret =  DocumentJSONCodec.decode(result);
		log.debug("upsertUserName = "+ret);
		return ret;
	}

	public JSONArray getUserName(String userId) throws JSONException {

		MongoCollection<Document> collection = findCollection(collectionName);

		Bson query = Filters.and(
				Filters.eq("Customer",userId),
				Filters.eq("type","wallets")
				);

		FindIterable<Document> res = collection.find(query)
				.projection(
						Projections.fields( 
								Projections.include("userName"),
								excludeId()
								)
						);
		
		JSONArray ret =  DocumentJSONCodec.decode(res);
		
		log.debug("getUserName = "+ret);
		
		return ret;
		
	}

	public JSONArray getUserId(@Nonnull String walletAddress,@Nonnull String phoneNumber, @Nonnull String email, String userName) throws JSONException {

		MongoCollection<Document> collection = findCollection(collectionName);
		
		Bson query=null;
		if(walletAddress!=null && (email!=null && phoneNumber!=null)) {

			query = Filters.and(
					Filters.eq("type","wallets"),
					Filters.exists(walletAddress,true),
					Filters.in(walletAddress+".id",email,phoneNumber),
					Filters.in(walletAddress+".id2",email,phoneNumber)
					);

		}else if(walletAddress!=null && email!=null) {

			query = Filters.and(
					Filters.eq("type","wallets"),
					Filters.exists(walletAddress,true),
					Filters.in(walletAddress+".id",email),
					Filters.in(walletAddress+".id2",email)
					);

		}else if(walletAddress!=null && phoneNumber!=null) {

			query = Filters.and(
					Filters.eq("type","wallets"),
					Filters.exists(walletAddress,true),
					Filters.in(walletAddress+".id",phoneNumber),
					Filters.in(walletAddress+".id2",phoneNumber)
					);

		}else if(walletAddress!=null){
			query = Filters.and(
					Filters.eq("type","wallets"),
					Filters.exists(walletAddress,true)
					);
		}else if(userName!=null) {
			query = Filters.and(
					Filters.eq("type","wallets"),
					Filters.eq("userName",userName)
					);
		}
		
		List<String> projectionList = new ArrayList<String>();
		projectionList.add("Customer");
		projectionList.add("type");//
		if(walletAddress!=null)projectionList.add(walletAddress);
		
		FindIterable<Document> res = collection.find(query).projection(
				Projections.fields( 
					Projections.include(projectionList),
					excludeId()
				)
		);

		JSONArray ret = DocumentJSONCodec.decode(res);
		
		return ret;
	}

}
