package io.awesome.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.SecureRequestCustomizer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;

import com.sun.jersey.api.core.ClassNamesResourceConfig;
import com.sun.jersey.spi.container.servlet.ServletContainer;

import io.awesome.api.crud.AwesomeProjectsREST;
import io.awesome.api.crud.HealthCheck;
import io.awesome.api.props.Configurations;

/**
 * Main class for this package. main method of this class starts up a jetty
 * instance.
 * 
 * @author kndungu
 */
public class JettyServerRun {
	static final Logger log = Logger.getLogger(JettyServerRun.class);

	private Properties properties = null;

	/**
	 * Constructor expects a properties file in package, jettyserver.props that has
	 * two properties: context.path service.port
	 * 
	 * @throws IOException
	 */
	public JettyServerRun() throws IOException {

		properties = Configurations.init().jettyProps;
	}

	/**
	 * Constructor
	 * 
	 * @param propsfile that has two properties: context.path service.port
	 * @throws IOException
	 */
	public JettyServerRun(File propsfile) throws IOException {
		InputStream props = new FileInputStream(propsfile);
		properties = new Properties();
		properties.load(props);

	}

	/**
	 * main method
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String args[]) throws Exception {
		BasicConfigurator.configure();// log4j conf

		if (args != null && args.length > 0) {
			File propsfile = new File(args[0]);
			if (propsfile.exists()) {
				new JettyServerRun(propsfile).start();
			} else {
				System.err.println("File " + args[0] + " does not exist or is not reachable");
			}

		} else
			new JettyServerRun().start();
	}

	public void start() throws Exception {

		log.debug("context path= " + properties.getProperty("context.path"));
		log.debug("service address= " + properties.getProperty("service.address"));
		log.debug("service port= " + properties.getProperty("service.port"));

		Server server = new Server();
		
		configureTlsConnection(server);
		
		configureHttpConnection(server);
		
		ClassNamesResourceConfig classResourceConfig = new ClassNamesResourceConfig(
				//HealthCheck.class,
				AwesomeProjectsREST.class
				);
		ServletHolder servletHolder = new ServletHolder(new ServletContainer(classResourceConfig));
		servletHolder.setName("EATM API");


     ServletContextHandler context = new ServletContextHandler(
        		server,
        		"/", 
        		ServletContextHandler.NO_SESSIONS);

     context.addServlet(servletHolder, properties.getProperty("context.path")+"/*");
     context.addServlet(HealthCheck.class, "/");

		// security
		/*RESTAuthenticationHandler securityHandler = new RESTAuthenticationHandler();
		Configurations conf = Configurations.init();
		securityHandler.setAuthenticator(new JWTAuthenticator(conf.JWT_HEADER_ATTR, conf.SIGNING_SECRET_KEY));

		Constraint constraint = new Constraint();
		constraint.setAuthenticate(true);
		ConstraintMapping constraintMappings = new ConstraintMapping();
		constraintMappings.setConstraint(constraint);
		constraintMappings.setPathSpec(properties.getProperty("context.path"));
		securityHandler.setConstraintMappings(Arrays.asList(constraintMappings));
		context.setSecurityHandler(securityHandler);
		*/
     
		server.setHandler(context);
		
		server.start();

	}
	
	private void configureHttpConnection(Server server) {
		
		ServerConnector connector = new ServerConnector(server);
		connector.setHost(properties.getProperty("service.address"));
		connector.setPort(Integer.parseInt(properties.getProperty("service.port")));
		connector.setAcceptQueueSize(100);
		connector.setDefaultProtocol("http/1.1");//"SPDY/2"
		server.addConnector(connector);
		
	}
	
	private void configureTlsConnection(Server server) {
		
		HttpConfiguration httpsConf = new HttpConfiguration();
		httpsConf.setSecurePort(8443);
		httpsConf.setSecureScheme("https");
		httpsConf.addCustomizer(new SecureRequestCustomizer());
		
		HttpConnectionFactory httpConFactory = new HttpConnectionFactory(httpsConf);
		
		SslContextFactory sslContext = new SslContextFactory();
		sslContext.setKeyStorePath("./ssl/jettykeystore");
		sslContext.setKeyStorePassword("mtinikeystore");
		//sslContext.setKeyManagerPassword("mtini");
		//sslContext.setTrustStorePassword("mtini");
		
		SslConnectionFactory sslFactory = new SslConnectionFactory(sslContext,"http/1.1");
		
		ServerConnector sslConnector = new ServerConnector(server,sslFactory,httpConFactory);
		sslConnector.setHost(properties.getProperty("service.address"));
		sslConnector.setPort(Integer.parseInt(properties.getProperty("service.tls.port")));
		sslConnector.setAcceptQueueSize(100);
		sslConnector.addConnectionFactory(sslFactory);
		server.addConnector(sslConnector);
		
	}

}