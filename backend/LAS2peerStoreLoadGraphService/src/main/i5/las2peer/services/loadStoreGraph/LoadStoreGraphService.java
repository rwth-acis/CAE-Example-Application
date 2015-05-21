package i5.las2peer.services.loadStoreGraph;

import i5.las2peer.api.Service;
import i5.las2peer.restMapper.HttpResponse;
import i5.las2peer.restMapper.MediaType;
import i5.las2peer.restMapper.RESTMapper;
import i5.las2peer.restMapper.annotations.GET;
import i5.las2peer.restMapper.annotations.POST;
import i5.las2peer.restMapper.annotations.Path;
import i5.las2peer.restMapper.annotations.PathParam;
import i5.las2peer.restMapper.annotations.Produces;
import i5.las2peer.restMapper.annotations.Version;
import i5.las2peer.restMapper.annotations.swagger.ApiInfo;
import i5.las2peer.restMapper.annotations.swagger.ApiResponse;
import i5.las2peer.restMapper.annotations.swagger.ApiResponses;
import i5.las2peer.restMapper.annotations.swagger.ResourceListApi;
import i5.las2peer.restMapper.annotations.swagger.Summary;
import i5.las2peer.restMapper.tools.ValidationResult;
import i5.las2peer.restMapper.tools.XMLCheck;
import i5.las2peer.security.Context;
import i5.las2peer.security.UserAgent;
import i5.las2peer.services.loadStoreGraph.database.DatabaseManager;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import net.minidev.json.JSONObject;

/**
 * LAS2peer Service
 * 
 * This is a template for a very basic LAS2peer service
 * that uses the LAS2peer Web-Connector for RESTful access to it.
 * 
 * Note:
 * If you plan on using Swagger you should adapt the information below
 * in the ApiInfo annotation to suit your project.
 * If you do not intend to provide a Swagger documentation of your service API,
 * the entire ApiInfo annotation should be removed.
 * 
 */
@Path("example")
@Version("0.1")
@ApiInfo(
		  title="LAS2peer Template Service",
		  description="A LAS2peer Template Service for demonstration purposes.",
		  termsOfServiceUrl="http://your-terms-of-service-url.com",
		  contact="john.doe@provider.com",
		  license="BSD License",
		  licenseUrl="http://your-software-license-url.com"
		)
public class LoadStoreGraphService extends Service {

	/*
	 * Database configuration
	 */
	private String jdbcDriverClassName;
	private String jdbcLogin;
	private String jdbcPass;
	private String jdbcUrl;
	private String jdbcSchema;
	private DatabaseManager dbm;

	/*
	 * WebConnector configuration (required by Swagger)
	 */
	private String webconnectorProtocol = "http";
	private String webconnectorIpAdress = "localhost";
	private String webconnectorPort = "8080";
	
	public LoadStoreGraphService() {
		// read and set properties values
		// IF THE SERVICE CLASS NAME IS CHANGED, THE PROPERTIES FILE NAME NEED TO BE CHANGED TOO!
		setFieldValues();
		// instantiate a database manager to handle database connection pooling and credentials
		dbm = new DatabaseManager(jdbcDriverClassName, jdbcLogin, jdbcPass, jdbcUrl, jdbcSchema);
	}

	////////////////////////////////////////////////////////////////////////////////////////
	//  Service methods.
	////////////////////////////////////////////////////////////////////////////////////////


	////////////////////////////////////////////////////////////////////////////////////////
	//  Methods required by the LAS2peer framework.
	////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Method for debugging purposes.
	 * Here the concept of restMapping validation is shown.
	 * It is important to check, if all annotations are correct and consistent.
	 * Otherwise the service will not be accessible by the WebConnector.
	 * Best to do it in the unit tests.
	 * To avoid being overlooked/ignored the method is implemented here and not in the test section.
	 * @return  true, if mapping correct
	 */
	public boolean debugMapping() {
		String XML_LOCATION = "./restMapping.xml";
		String xml = getRESTMapping();

		try {
			RESTMapper.writeFile(XML_LOCATION, xml);
		} catch (IOException e) {
			e.printStackTrace();
		}

		XMLCheck validator = new XMLCheck();
		ValidationResult result = validator.validate(xml);

		if (result.isValid())
			return true;
		return false;
	}

	/**
	 * This method is needed for every RESTful application in LAS2peer. There is no need to change!
	 * 
	 * @return the mapping
	 */
	public String getRESTMapping() {
		String result = "";
		try {
			result = RESTMapper.getMethodsAsXML(this.getClass());
		} catch (Exception e) {

			e.printStackTrace();
		}
		return result;
	}
	
	////////////////////////////////////////////////////////////////////////////////////////
	//  Methods providing a Swagger documentation of the service API.
	////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Returns a listing of all annotated top level resources
	 * for purposes of the Swagger documentation.
	 * 
	 * @return Listing of all top level resources.
	 */
    @GET
    @Path("api-docs")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse getSwaggerResourceListing(){
      return RESTMapper.getSwaggerResourceListing(this.getClass());
    }

    /**
     * Returns the API documentation for a specific annotated top level resource
     * for purposes of the Swagger documentation.
	 * 
	 * Trouble shooting:
	 * Please make sure that the endpoint URL below is  
	 * correct with respect to your service.
	 * 
     * @param tlr A top level resource name.
     * @return The resource's documentation.
     */
    @GET
    @Path("api-docs/{tlr}")
    @Produces(MediaType.APPLICATION_JSON)
    public HttpResponse getSwaggerApiDeclaration(@PathParam("tlr") String tlr){
    	HttpResponse res;
    	Class<LoadStoreGraphService> c = LoadStoreGraphService.class;
    	if (!c.isAnnotationPresent(Path.class)){
    		res = new HttpResponse("Swagger API declaration not available. Service path is not defined.");
    		res.setStatus(404);
    	} else{
    		Path path = (Path) c.getAnnotation(Path.class);
    		String endpoint = webconnectorProtocol + "://" + webconnectorIpAdress + ":"
    				+ webconnectorPort + path.value() + "/";
    		System.out.println(endpoint);
    		res = RESTMapper.getSwaggerApiDeclaration(this.getClass(), tlr, endpoint);
    	}
    	return res;
    }

}
