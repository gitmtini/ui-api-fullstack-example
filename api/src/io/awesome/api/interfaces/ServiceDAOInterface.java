package io.awesome.api.interfaces;

import java.util.Arrays;

import org.apache.log4j.Logger;

import com.google.common.base.Strings;
import com.prelimtek.client.sawtooth.SawtoothDAO;

import io.awesome.api.props.Configurations;
import io.awesome.dao.ApiMongoDAOImpl;

public interface ServiceDAOInterface {

	static Logger log = Logger.getLogger(ServiceDAOInterface.class);
	static Configurations config = Configurations.init();

	default ApiMongoDAOImpl getMongoDAO() {
		//EatMongoDAOImpl dao = null;
		//if(dao==null || !dao.isConnected()) {
		ApiMongoDAOImpl	dao = (ApiMongoDAOImpl) ApiMongoDAOImpl.instance();
		
		if(!Strings.isNullOrEmpty(config.MONGO_USER)) {
			dao.addCredential(config.MONGO_SOURCE, config.MONGO_USER, config.MONGO_PASS.toCharArray());
		}
		
		Arrays.asList(config.MONGO_SERVER_ADDRESS_PORT_ARRAY).forEach(
				address -> { 
					log.debug("config.MONGO_SOURCE "+ address);
					dao.addServerAddress(address);
				}
		);

		dao.setCollectionName(config.MONGO_COLLECTION);
		//}
		dao.connectDatabase(config.MONGO_DB);
		log.debug("config.MONGO_SOURCE "+ config.MONGO_SOURCE);
		log.debug("config.MONGO_DB "+ config.MONGO_DB);
		log.debug("config.MONGO_USER "+ config.MONGO_USER);
		log.debug("config.MONGO_COLLECTION "+ config.MONGO_COLLECTION);

		return dao;
	}
	

}
