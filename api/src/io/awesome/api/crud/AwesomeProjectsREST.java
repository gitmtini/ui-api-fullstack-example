package io.awesome.api.crud;


import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;




import io.awesome.proto.WalletProtos.CustomerDetails;

import org.apache.log4j.Logger;

import io.awesome.api.interfaces.ServiceDAOInterface;


@Path("/")
public class AwesomeProjectsREST {//implements ServiceDAOInterface, LedgerEntriesServiceValidatorInterface{
	
	static Logger log = Logger.getLogger(AwesomeProjectsREST.class);
	
	@GET
	@Produces(MediaType.TEXT_PLAIN)
	public String hello() {
	   return "awesome project services.";
	}
	 
	@GET
	@Path("customer/{customerId}")
	@Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_OCTET_STREAM})
	public Response getCustomerDetails(@PathParam("customerId") String customerId){
		
		log.debug("getCustomerDetails  customerId:"+customerId );
		
		CustomerDetails customer = CustomerDetails
				.newBuilder()
				.setEmail("someone@somewhere.com")
				.setTelephone("16505551212")
				.setName("John Doe")
				.setUserName("eser123")
				.setId("1234")
				.build();
		
		
		
		return Response.status(200).entity(customer.toByteArray()).build(); 
	}
	
	
	@PUT
	@Path("customer/")
	@Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_OCTET_STREAM})
	public Response insertNewCustomer(){
		
		String json = "";
		return Response.status(200).entity(json).build(); 
	}
	
	
	
	@POST
	@Path("customer/{customerId}")
	@Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_OCTET_STREAM})
	public Response updateCustomerDetails(@PathParam("customerId") String customerId){
		
		String json = "";
		return Response.status(200).entity(json).build(); 
	}
	
	
	@PUT
	@Path("{customerId}/project")
	@Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_OCTET_STREAM})
	public Response insertNewProject(@PathParam("customerId") String customerId){
		
		String json = "";
		return Response.status(200).entity(json).build(); 
	}
	
	@POST
	@Path("{customerId}/project/{projectId}")
	@Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_OCTET_STREAM})
	public Response updateProjectDetails(@PathParam("customerId") String customerId,@PathParam("projectId") String projectId){
		
		String json = "";
		return Response.status(200).entity(json).build(); 
	}
	
	
	@PUT
	@Path("{customerId}/project/{projectId}/work")
	@Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_OCTET_STREAM})
	public Response insertNewWork(@PathParam("customerId") String customerId,@PathParam("projectId") String projectId){
		
		String json = "";
		return Response.status(200).entity(json).build(); 
	}
	
	
	@POST
	@Path("{customerId}/project/{projectId}/work/{workId}")
	@Produces({MediaType.APPLICATION_JSON,MediaType.APPLICATION_OCTET_STREAM})
	public Response updateWorkDetails(@PathParam("customerId") String customerId,@PathParam("projectId") String projectId,@PathParam("workId") String workId){
		
		String json = "";
		return Response.status(200).entity(json).build(); 
	}
	
	

}
