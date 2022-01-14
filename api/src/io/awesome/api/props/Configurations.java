package io.awesome.api.props;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;

import org.apache.log4j.Logger;

import com.google.common.io.Resources;

/**
 * 
 * @author kaniundungu
 *
 *Runtime configurations are set here. 
 *1. Jetty configurations
 *2. App clients specific conf i.e. persistence and services endpoints.
 */
public class Configurations {
	
	static{init();}
	
	/**
	 * App clients specific conf i.e. persistence and services endpoints.
	 */
	public static Properties clientProps = null;
	/**
	 * Jetty configurations
	 */
	public static Properties jettyProps = null;
	
	public static Configurations init(){return new Configurations();}
	
	private Logger log = Logger.getLogger(Configurations.class);
	
	Configurations(){
		InputStream props = null;
		//if(clientProps==null){
		try {
			props = Resources.getResource("clients.props").openStream();
			clientProps = new Properties();
			clientProps.load(props);
			
			MQTT_ENDPOINT = System.getenv("mqtt_endpoint");
			MQTT_ENDPOINT = MQTT_ENDPOINT!=null?MQTT_ENDPOINT:Configurations.clientProps.getProperty("mqtt_endpoint");
			
			MQTT_CA_CERT_FILE = System.getenv("mqtt_ca_cert_file");
			MQTT_CA_CERT_FILE = MQTT_CA_CERT_FILE!=null?MQTT_CA_CERT_FILE:Configurations.clientProps.getProperty("mqtt_ca_cert_file");
			
			MQTT_KEYSTORE_PASS = System.getenv("mqtt_ca_keystore_pass");
			MQTT_KEYSTORE_PASS = MQTT_KEYSTORE_PASS!=null?MQTT_KEYSTORE_PASS:Configurations.clientProps.getProperty("mqtt_ca_keystore_pass");
				
			MQTT_KEYSTORE_FILE = System.getenv("mqtt_keystore_path");
			MQTT_KEYSTORE_FILE = MQTT_KEYSTORE_FILE!=null?MQTT_KEYSTORE_FILE:Configurations.clientProps.getProperty("mqtt_keystore_path");
				
			
			ZMQ_ENDPOINT = System.getenv("zmq_endpoint");
			ZMQ_ENDPOINT = ZMQ_ENDPOINT!=null?ZMQ_ENDPOINT:Configurations.clientProps.getProperty("zmq_endpoint");
			
			
			SIGNING_PRIVATE_SECRET_KEY = System.getenv("shared_secret_private");
			SIGNING_PRIVATE_SECRET_KEY = SIGNING_PRIVATE_SECRET_KEY!=null?SIGNING_PRIVATE_SECRET_KEY:Configurations.clientProps.getProperty("shared_secret_private");
			
			
			SIGNING_SECRET_KEY = System.getenv("shared_secret_public");
			SIGNING_SECRET_KEY = SIGNING_SECRET_KEY!=null?SIGNING_SECRET_KEY:Configurations.clientProps.getProperty("shared_secret_public");
			
			JWT_HEADER_ATTR = System.getenv("jwt_header_attr");
			JWT_HEADER_ATTR = JWT_HEADER_ATTR!=null?JWT_HEADER_ATTR:Configurations.clientProps.getProperty("jwt_header_attr");
			
			String cache_service_urls = System.getenv("cache_service_url_array");
			CACHE_SERVICE_URL_ARRAY = cache_service_urls!=null?cache_service_urls.split(","):(Configurations.clientProps.getProperty("cache_service_url_array",null)!=null
					?Configurations.clientProps.getProperty("cache_service_url_array").split(",")
					:null);

			String search_service_urls = System.getenv("search_service_url_array");
			SEARCH_SERVICE_URL_ARRAY = search_service_urls!=null?search_service_urls.split(","):(Configurations.clientProps.getProperty("search_service_url_array",null)!=null
					?Configurations.clientProps.getProperty("search_service_url_array").split(",")
					:null);
					
			String persitence_service_urls = System.getenv("persistence_service_url_array");
			PERSISTENCE_SERVICE_URL_ARRAY = persitence_service_urls!=null?persitence_service_urls.split(","):(Configurations.clientProps.getProperty("persistence_service_url_array",null)!=null?
					Configurations.clientProps.getProperty("persistence_service_url_array").split(",")
					:null);
			
			String session_token_exp = System.getenv("session_token_exp_sec")!=null?System.getenv("session_token_exp_sec"):Configurations.clientProps.getProperty("session_token_exp_sec");
			SESSION_TOKEN_EXP = session_token_exp!=null&&Integer.valueOf(session_token_exp)!=null?Integer.valueOf(session_token_exp):120;
			
			
			String mongo_db = System.getenv("mongo_db");
			MONGO_DB = mongo_db!=null?mongo_db:Configurations.clientProps.getProperty("mongo_db");
			
			String mongo_source = System.getenv("mongo_source");
			MONGO_SOURCE = mongo_source!=null?mongo_source:Configurations.clientProps.getProperty("mongo_source");
			
			String mongo_user= System.getenv("mongo_user");
			MONGO_USER = mongo_user!=null?mongo_user:Configurations.clientProps.getProperty("mongo_user");
			
			String mongo_pass = System.getenv("mongo_pass");
			MONGO_PASS = mongo_pass!=null?mongo_pass:Configurations.clientProps.getProperty("mongo_pass");
			
			String mongo_server_port = System.getenv("mongo_server_port");
			MONGO_SERVER_PORT_ARRAY = mongo_server_port!=null?Arrays.asList(mongo_server_port.split(",")).stream().map( Integer::parseInt).toArray(Integer[]::new)
					:(Configurations.clientProps.getProperty("mongo_server_port",null)!=null?
							Arrays.asList(Configurations.clientProps.getProperty("mongo_server_port").split(",")).stream().map(Integer::parseInt).toArray(Integer[]::new)
					:null);
			
			String mongo_server_address = System.getenv("mongo_server_address");
			MONGO_SERVER_ADDRESS_ARRAY = mongo_server_address!=null?mongo_server_address.split(","):(Configurations.clientProps.getProperty("mongo_server_address",null)!=null?
					Configurations.clientProps.getProperty("mongo_server_address").split(",")
					:null);
			
			
			String mongo_server_address_port_array = System.getenv("mongo_server_address_port_array");
			MONGO_SERVER_ADDRESS_PORT_ARRAY = mongo_server_address_port_array!=null?mongo_server_address_port_array.split(","):(Configurations.clientProps.getProperty("mongo_server_address_port_array",null)!=null?
					Configurations.clientProps.getProperty("mongo_server_address_port_array").split(",")
					:null);
			
			String mongo_collection = System.getenv("mongo_collection");
			MONGO_COLLECTION = mongo_collection!=null?mongo_collection:(MONGO_COLLECTION!=null?MONGO_COLLECTION:Configurations.clientProps.getProperty("mongo_collection"));
		
			
			try{String mongo_otp_connectionTimeout = System.getenv("mongo_otp_connectionTimeout");
			mongo_otp_connectionTimeout = mongo_otp_connectionTimeout!=null?mongo_otp_connectionTimeout:Configurations.clientProps.getProperty("mongo_otp_connectionTimeout");
			MONGO_OPT_CONNECTIONTIMEOUT = Integer.parseInt(mongo_otp_connectionTimeout);}catch(Throwable e) {
				//log.error(e.getMessage(),e);
				log.warn("setting default MONGO_OPT_CONNECTIONTIMEOUT="+MONGO_OPT_CONNECTIONTIMEOUT+" due to "+e.getMessage());
			}
			
			
			try{String mongo_otp_socketConnectionTimeout = System.getenv("mongo_otp_socketConnectionTimeout");
			mongo_otp_socketConnectionTimeout = mongo_otp_socketConnectionTimeout!=null?mongo_otp_socketConnectionTimeout:Configurations.clientProps.getProperty("mongo_otp_socketConnectionTimeout");
			MONGO_OPT_SOCKETCONNECTIONTIMEOUT = Integer.parseInt(mongo_otp_socketConnectionTimeout);}catch(Throwable e) {
				//log.error(e.getMessage(),e);
				log.warn("setting default MONGO_OPT_SOCKETCONNECTIONTIMEOUT="+MONGO_OPT_SOCKETCONNECTIONTIMEOUT+" due to "+e.getMessage());
			}
			
			
			try{String mongo_opt_poolsize = System.getenv("mongo_opt_poolsize");
			mongo_opt_poolsize = mongo_opt_poolsize!=null?mongo_opt_poolsize:Configurations.clientProps.getProperty("mongo_opt_poolsize");
			MONGO_OPT_POOLSIZE = Integer.parseInt(mongo_opt_poolsize);}catch(Throwable e) {
				//log.error(e.getMessage(),e);
				log.warn("setting default MONGO_OPT_POOLSIZE="+MONGO_OPT_POOLSIZE+" due to "+e.getMessage());
			}
			
			
			try{String mongo_opt_keepalive = System.getenv("mongo_opt_keepalive");
			mongo_opt_keepalive = mongo_opt_keepalive!=null?mongo_opt_keepalive:Configurations.clientProps.getProperty("mongo_opt_keepalive");
			MONGO_OPT_KEEPALIVE = Boolean.parseBoolean(mongo_opt_keepalive);}catch(Throwable e) {
				//log.error(e.getMessage(),e);
				log.warn("setting default MONGO_OPT_KEEPALIVE="+MONGO_OPT_KEEPALIVE+" due to "+e.getMessage());
			}
			
			
			
			
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}finally{
			
				close(props);//.close();
		}
		//}
		
		
		//if(jettyProps==null){
		try {
			props = Resources.getResource("jettyserver.props").openStream();
			jettyProps = new Properties();
			jettyProps.load(props);
			
		} catch (IOException e) {
			System.err.println(e.getMessage());
		}finally{
			close(props);
		}
		//}
		

	}
	
	private static void close(InputStream props ){
		if(props!=null){
			try {
				props.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	
	/**
	 * Used to sign jwt token. It is shared with clients to establish trust.
	 */
	public String SIGNING_SECRET_KEY;
	
	/**
	 * Used to verify jwt token
	 */
	public String SIGNING_PRIVATE_SECRET_KEY;
	/**
	 * Used as http header request key for jwt token
	 */
	public String JWT_HEADER_ATTR;
	/**
	 * Used by memcached urls
	 */
	public String[] CACHE_SERVICE_URL_ARRAY;
	/**
	 * Used by Elasticsearch urls
	 */
	public String[] SEARCH_SERVICE_URL_ARRAY;
	/**
	 * Used for sawtooth urls
	 */
	public String[] PERSISTENCE_SERVICE_URL_ARRAY;

	/**
	 * Session token expiration in seconds
	 * */
	public int SESSION_TOKEN_EXP = 120;
	
	
	public String MONGO_DB;
	public String MONGO_SOURCE;
	public String MONGO_USER;
	public String MONGO_PASS;
	public Integer[] MONGO_SERVER_PORT_ARRAY;
	public String[] MONGO_SERVER_ADDRESS_ARRAY;
	public String[] MONGO_SERVER_ADDRESS_PORT_ARRAY;
	public String MONGO_COLLECTION;

	/*mongo_otp_connectionTimeout=360000
	mongo_otp_socketConnectionTimeout=360000
	mongo_opt_poolsize=10
	mongo_opt_keepalive=true*/
	public int MONGO_OPT_CONNECTIONTIMEOUT=360000;
	public int MONGO_OPT_SOCKETCONNECTIONTIMEOUT=360000;
	public int MONGO_OPT_POOLSIZE=10;
	public boolean MONGO_OPT_KEEPALIVE=true;
	
	public String ZMQ_ENDPOINT,MQTT_ENDPOINT,MQTT_CA_CERT_FILE,MQTT_KEYSTORE_PASS,MQTT_KEYSTORE_FILE;

}
