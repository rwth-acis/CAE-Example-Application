package i5.las2peer.services.loadStoreGraph;

import i5.las2peer.api.Service;
import i5.las2peer.restMapper.HttpResponse;
import i5.las2peer.restMapper.MediaType;
import i5.las2peer.restMapper.RESTMapper;
import i5.las2peer.restMapper.annotations.Consumes;
import i5.las2peer.restMapper.annotations.ContentParam;
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
import i5.las2peer.services.loadStoreGraph.database.DatabaseManager;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.mysql.jdbc.ResultSetMetaData;
import com.mysql.jdbc.Statement;

import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONValue;
import net.minidev.json.parser.ParseException;

/**
 * LAS2peerLoadStoreGraphService
 * 
 *  This microservice is part of the CAE-Example Application.
 *  It purpose is to provide a persistence functionality for graphs used in this application.
 */
@Path("graphs")
@Version("0.1")
@ApiInfo(
		  title="LAS2peerLoadStoreGraphService",
		  description="A LAS2peer service storing and loading graphs from a database.",
		  termsOfServiceUrl="http://your-terms-of-service-url.com",
		  contact="lange@dbis.rwth-aachen.de",
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
	private String webconnectorPort = "8081";
	
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

	/**
	 * Stores a graph to the database.
	 * 
	 * @param graph a JSON object containing the graph
	 * @return HttpResponse in case of success it contains the id of the stored graph
	 * 
	 */
	@POST
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@ResourceListApi(description = "Stores a graph to the database.")
	@Summary("Stores a passed JSON graph to the database.")
	@ApiResponses(value={
			@ApiResponse(code = 201, message = "Graph stored"),
			@ApiResponse(code = 500, message = "Internal error"),
	})
	public HttpResponse storeGraph(@ContentParam String graph) {
		
		JSONObject content;
		try {
			content = (JSONObject) JSONValue.parseWithException(graph);
		} catch (ParseException e) {
			e.printStackTrace();
			HttpResponse er = new HttpResponse("Internal error: " + e.getMessage());
			er.setStatus(500);
			return er;
		}
		String insertQuery ="";
		int id = (int) content.get("graphId");
		if(id == -1){
			id = 0; // in case of a new graph
		}
		String description = (String) content.get("description");
		JSONArray array = (JSONArray) content.get("nodes");
		String nodes = array.toJSONString();
		array = (JSONArray) content.get("links");
		String links = array.toJSONString();
		Connection conn = null;
		PreparedStatement stmnt = null;
		try {
			conn = dbm.getConnection();
			// formulate statement
			insertQuery = "INSERT INTO graphs ( graphId,  description,  nodes,  links ) " +
			"VALUES ('" + id + "', '" + description + "', '" + nodes + "', '" + links + "') ON DUPLICATE KEY UPDATE "+
					"description = + '" + description + "', " + "nodes = + '" + nodes + "', " + "links = + '" + links + "';";
			stmnt = conn.prepareStatement(insertQuery, Statement.RETURN_GENERATED_KEYS);
			// execute query
			stmnt.executeUpdate();
			ResultSet genKeys = stmnt.getGeneratedKeys();
			if (genKeys.next()) {
				int newId = genKeys.getInt(1);
				// return HTTP response on success with new id
				HttpResponse r = new HttpResponse(newId + "");
				r.setStatus(201);
				return r;
			}
			// return HTTP response on success with id of updated graph
			HttpResponse r = new HttpResponse(id + "");
			r.setStatus(201);
			return r;
		} catch (Exception e) {
			// return HTTP Response on error
			HttpResponse er = new HttpResponse("Internal error: " + e.getMessage() + stmnt.toString());
			er.setStatus(500);
			return er;
		} finally {
			// free resources
			if (stmnt != null) {
				try {
					stmnt.close();
				} catch (Exception e) {
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
	
	
	/**
	 * Fetches a graph from the database.
	 *  
	 * @param id the id of the graph
	 * @return HttpResponse
	 * 
	 */
	@GET
	@Path("/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	@ResourceListApi(description = "Loads a graph from the database.")
	@Summary("Returns a graph from the database according to the passed id.")
	@ApiResponses(value={
			@ApiResponse(code = 200, message = "Graph loaded"),
			@ApiResponse(code = 404, message = "Graph not found"),
			@ApiResponse(code = 500, message = "Internal error"),
	})
	public HttpResponse loadGraph(@PathParam(value = "id") int id) {
		String result = "";
		String columnName="";
		String selectquery ="";
		int columnCount = 0;
		Connection conn = null;
		PreparedStatement stmnt = null;
		ResultSet rs = null;
		ResultSetMetaData rsmd = null;
		try {
			// get connection from connection pool
			conn = dbm.getConnection();
			selectquery = "SELECT * FROM graphs WHERE graphId = " + id + " ;" ;
			
			// prepare statement
			stmnt = conn.prepareStatement(selectquery);
			
			// retrieve result set
			rs = stmnt.executeQuery();
			rsmd = (ResultSetMetaData) rs.getMetaData();
			columnCount = rsmd.getColumnCount();
			// process result set
			if (rs.next()) {
				JSONObject ro = new JSONObject();
				for(int i=1;i<=columnCount;i++){
					result = rs.getString(i);
					columnName = rsmd.getColumnName(i);
					// setup resulting JSON Object
					ro.put(columnName, result);
				}
				
				// return HTTP Response on success
				HttpResponse r = new HttpResponse(ro.toJSONString());
				r.setStatus(200);
				return r;
				
			} else {
				result = "No result for graph with id " + id;
				
				// return HTTP Response on error
				HttpResponse er = new HttpResponse(result);
				er.setStatus(404);
				return er;
			}
			
		} catch (Exception e) {
			// return HTTP Response on error
			HttpResponse er = new HttpResponse("Internal error: " + e.getMessage());
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
					HttpResponse er = new HttpResponse("Internal error: " + e.getMessage());
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
	
	/**
	 * Retrieves the graph list from the database 
	 * and returns an HTTP response including a JSON object.
	 * 
	 * @return HttpResponse
	 * 
	 */
	@GET
	@Path("/")
	@Produces("application/json")
	@ResourceListApi(description = "Returns a list of graphs.")
	@Summary("Return a JSON array with a list of graphs.")
	@ApiResponses(value={
			@ApiResponse(code = 200, message = "Graph List, JSON Array"),
			@ApiResponse(code = 404, message = "No graphs exist"),
			@ApiResponse(code = 500, message = "Internal error"),
	})
	public HttpResponse getGraphList() {
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
			selectquery = "SELECT graphId, description FROM graphs;";
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
