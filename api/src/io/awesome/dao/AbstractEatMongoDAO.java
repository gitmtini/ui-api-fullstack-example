package io.awesome.dao;

import static com.mongodb.client.model.Projections.excludeId;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.Map;

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
import com.prelimtek.client.DocumentStoreDAO;
import com.prelimtek.client.mongodb.DocumentJSONCodec;
import com.prelimtek.client.mongodb.MongoDBConnector;

public abstract class AbstractEatMongoDAO extends MongoDBConnector implements DocumentStoreDAO{

//TODO use EatMongoDAOImpl type implementation and use a different config

	protected String databaseName;
	protected String collectionName;
	protected Bson updateQuery ;
	protected Bson searchQuery ;
	protected Bson searchProjections ;

	protected static AbstractEatMongoDAO instance;
	
	protected AbstractEatMongoDAO() {
		super();
	}
	

	public void connectDatabase(String database) {
		super.connect(database);
		this.databaseName = database;
	}
	
	public String getDatabaseName() {
		return this.databaseName;
	}
	
	public String getCollectionName() {
		return collectionName;
	}

	public void setCollectionName(String collectionName) {
		this.collectionName = collectionName;
	}
	
	@Override
	public JSONArray getWallet( String userId, boolean active, String walletAddress) throws JSONException {
		
		MongoCollection<Document> collection = findCollection(collectionName);
		
		Bson query = Filters.and(new BasicDBObject("Customer",userId),
				Filters.exists("wallet",true));
		
		Iterable<Document> res = collection.find(query)
				.projection(Projections.fields( Projections.include("wallet"),excludeId()));

		JSONArray ret = DocumentJSONCodec.decode(res);
		 return ret;
	}

	@Override
	public JSONArray getImageListByModelId(String userId, boolean active, Object... modelIds)
			throws UnknownHostException, JSONException {
		
		MongoCollection<Document> collection = findCollection(collectionName);
		
		//((Iterable<Document>)collection.find()).forEach(doc -> System.out.println(doc.toJson()));
				
		/*Bson query = Filters.and(new BasicDBObject("Customer",userId),
				Filters.or(
						Filters.in("estateList.id", modelIds),
						Filters.in("estateList.tenantList.id", modelIds)
				));
		
		Iterable<Document> res = collection.find(query)
				.projection(Projections.fields( 
						Projections.include("estateList.tenantList.imagesList"),
						Projections.include("estateList.imagesList"),
						excludeId() ));

		*/
		//Bson query  = initSearchImagesQueryFilter();
		//Bson projections = initSearchImagesQueryProjection();
		
		initGetImageListByModelId(userId, modelIds);
		
		Iterable<Document> res = collection.find(searchQuery)
		.projection(searchProjections);
		
		res.forEach(doc -> System.out.println(doc.toJson()));
		
		 JSONArray ret = DocumentJSONCodec.decode(res);
		 
		 return ret;
	}


	@Override
	public JSONArray getNotesListByModelId(String userId, boolean active, Object... modelIds)
			throws UnknownHostException, JSONException {
		
		MongoCollection<Document> collection = findCollection(collectionName);
		
		((Iterable<Document>)collection.find()).forEach(doc -> System.out.println(doc.toJson()));
				
		//Bson query  = initSearchNotesQueryFilter();
		//Bson projections = initSearchNotesQueryProjection();
		
		initGetNotesListByModelId(userId, modelIds);
		
		Iterable<Document> res = collection.find(searchQuery)
		.projection(searchProjections);
		
		/*
		 Bson query = Filters.and(new BasicDBObject("Customer",userId),
				Filters.or(
						//Filters.in("estateList.id", modelIds),
						Filters.in("estateList.tenantList.id", modelIds)
				));
		
		Iterable<Document> res = collection.find(query)
				.projection(Projections.fields( 
						Projections.include("estateList.tenantList.notesList"),
						Projections.include("estateList.notesList"),
						excludeId() ));
		 */
		
		res.forEach(doc -> System.out.println(doc.toJson()));
		
		JSONArray ret = DocumentJSONCodec.decode(res);
		
		return ret;
	}


	@Override
	public JSONObject putDocument(String userId, String type, Object id, JSONObject jsondata)
			throws JSONException, IOException {
		
		Document userData ;
		//search type, id
		//append to data
		MongoCollection<Document> collection = findCollection(collectionName);
		
		//prequery
		Bson query = Filters.and(new BasicDBObject("Customer",userId),
		Filters.eq(type,id));

		//data
		Document doc = new Document(type,id);
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
		return DocumentJSONCodec.decode(userData);
	}

	
	@Override
	public JSONObject updateDocument(String userId, String updateField, Object modelId, JSONObject jsondata)
			throws JSONException, IOException {
		
		MongoCollection<Document> collection = findCollection(collectionName);
		
		
		Document doc =DocumentJSONCodec.encode(jsondata);
		
		
		Document result = collection.findOneAndUpdate(updateQuery, Updates.addToSet(updateField ,doc));
		System.out.println("res : "+result.toJson());
		
		
		return DocumentJSONCodec.decode(result);
	}
	
	
	public void setUpdateQuery(Bson query ){
		this.updateQuery = query;
	}

	
	public JSONObject updateImages(String userId, String updateField, String modelId, JSONObject imageJson) throws JSONException, IOException {
		/*setUpdateQuery(		
				Filters.and(new BasicDBObject("Customer",userId),
				Filters.or(
						Filters.in("estateList.imagesList.modelId", modelId),
						Filters.eq("estateList.tenantList.imagesList.modelId", modelId)
				))
				);*/
		initUpdateImagesConfig();
		return updateDocument(userId,updateField,modelId, imageJson);
	}
	
	
	public JSONObject updateNotes(String userId, String updateField, String modelId, JSONObject notesJson) throws JSONException, IOException {
		/*setUpdateQuery(		
				Filters.and(new BasicDBObject("Customer",userId),
				Filters.or(
						Filters.in("estateList.notesList.modelId", modelId),
						Filters.eq("estateList.tenantList.notesList.modelId", modelId)
				))
				);*/
		
		initUpdateNotesConfig();
		return updateDocument(userId,updateField,modelId, notesJson);
	}
	
	protected abstract void initUpdateNotesConfig();
	
	protected abstract void initUpdateImagesConfig();
	
	protected abstract void initUpdateTenantConfig();
	
	protected abstract void initUpdateEstateConfig();
	
	protected abstract void initGetNotesListByModelId(String userId, Object ... modelIds);
	
	protected abstract void initGetImageListByModelId(String userId, Object ... modelIds);
	
	protected abstract void initGetWallet(String userId);
	
}
	

