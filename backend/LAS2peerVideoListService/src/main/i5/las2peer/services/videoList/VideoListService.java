package i5.las2peer.services.videoList;

import i5.las2peer.api.Service;
import i5.las2peer.restMapper.HttpResponse;
import i5.las2peer.restMapper.MediaType;
import i5.las2peer.restMapper.RESTMapper;
import i5.las2peer.restMapper.annotations.GET;
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
import i5.las2peer.services.videoList.database.DatabaseManager;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import org.json.simple.JSONArray;

import com.mysql.jdbc.ResultSetMetaData;

import net.minidev.json.JSONObject;

/**
 * LAS2peerVideoListService
 * 
 *  This microservice is an adaption of the LAS2peer-Video-Details service.
 *  It sole purpose is to provide a list of videos from the database schema used also by the LAS2peer-Video-Details service.
 *  It is part of the CAE-Example Application.
 */
@Path("videos")
@Version("0.1")
@ApiInfo(
		  title="LAS2peerVideoListService",
		  description="A LAS2peer Mircoservice that provides access to a list of videos.",
		  termsOfServiceUrl="http://your-terms-of-service-url.com",
		  contact="lange@dbis.rwth-aachen.de",
		  license="BSD License",
		  licenseUrl="http://your-software-license-url.com"
		)
public class VideoListService extends Service {

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
	
	public VideoListService() {
		// read and set properties values
		// IF THE SERVICE CLASS NAME IS CHANGED, THE PROPERTIES FILE NAME NEED TO BE CHANGED TOO!
		setFieldValues();
		// instantiate a database manager to handle database connection pooling and credentials
		dbm = new DatabaseManager(jdbcDriverClassName, jdbcLogin, jdbcPass, jdbcUrl, jdbcSchema);
	}

	////////////////////////////////////////////////////////////////////////////////////////
	//  Service methods.
	////////////////////////////////////////////////////////////////////////////////////////
	
	/**
	 * Retrieves the video list from the database 
	 * and returns an HTTP response including a JSON object.
	 * 
	 * @return HttpResponse
	 * 
	 */
	@GET
	@Path("/")
	@Produces("application/json")
	@ResourceListApi(description = "Returns a list of videos.")
	@Summary("Return a JSON array with a list of videos.")
	@ApiResponses(value={
			@ApiResponse(code = 200, message = "Video List, JSON Array"),
			@ApiResponse(code = 404, message = "No videos exist"),
			@ApiResponse(code = 500, message = "Internal error"),
	})
	public HttpResponse getVideoList() {
		String result = "";
		String columnName="";
		String selectquery ="";
		int columnCount = 0;
		Connection conn = null;
		PreparedStatement stmnt = null;
		ResultSet rs = null;
		ResultSetMetaData rsmd = null;
		JSONObject ro=null;
		JSONArray qs = new JSONArray();
		try {
			// get connection from connection pool
			conn = dbm.getConnection();
			selectquery = "SELECT * FROM videodetails;";
			// prepare statement
			stmnt = conn.prepareStatement(selectquery);
			
			// retrieve result set
			rs = stmnt.executeQuery();
			rsmd = (ResultSetMetaData) rs.getMetaData();
			columnCount = rsmd.getColumnCount();
			
			// process result set
			while(rs.next()){
				ro = new JSONObject();
				for(int i=1;i<=columnCount;i++){
					result = rs.getString(i);
					columnName = rsmd.getColumnName(i);
					// setup resulting JSON Object
					ro.put(columnName, result);
					
				}
				qs.add(ro);
			}
			if (qs.isEmpty()){
				result = "No results";
				
				// return HTTP Response on error
				HttpResponse er = new HttpResponse(result);
				er.setStatus(404);
				return er;
								
			} else {
				// return HTTP Response on success
				HttpResponse r = new HttpResponse(qs.toJSONString());
				r.setStatus(200);
				return r;
				}
		} catch (Exception e) {
			// return HTTP Response on error
			HttpResponse er = new HttpResponse("Internal error: " + e.getMessage() + stmnt.toString());
			er.setStatus(500);
			return er;
		} finally {
			// free resources
			if (rs != null) {
				try {
					rs.close();
				} catch (Exception e) {
					Context.logError(this, e.getMessage());
					
					// return HTTP Response on error
					HttpResponse er = new HttpResponse("Internal error: " + e.getMessage() + stmnt.toString());
					er.setStatus(500);
					return er;
				}
			}
			if (stmnt != null) {
				try {
					stmnt.close();
				} catch (Exception e) {
					Context.logError(this, e.getMessage());
					
					// return HTTP Response on error
					HttpResponse er = new HttpResponse("Internal error: " + e.getMessage());
					er.setStatus(500);
					return er;
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e) {
					Context.logError(this, e.getMessage());
					
					// return HTTP Response on error
					HttpResponse er = new HttpResponse("Internal error: " + e.getMessage());
					er.setStatus(500);
					return er;
				}
			}
		}
	}
	
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
    	Class<VideoListService> c = VideoListService.class;
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
