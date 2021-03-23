package platform.api;


import java.io.File;
import java.util.LinkedHashMap;
import org.testng.Assert;
import org.testng.annotations.Test;

import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

public class EnviromentSetup extends Payloads{

	@Test(priority = 1, enabled = true)
	public static void createUser() {
		try {
			System.out.println("-01-----------------createUser in Env setup---------------------------");
			Response response = RestAssured
					.given().headers(EnviromentSetup.HeadersWithAPIKey()).log().all() 
					.body(EnviromentSetup.createUserPayLoad()).log().all()
					.when()
					.post(url+internalAccountResource)
					.then().log().all()
					.extract().response();	
			Thread.sleep(5000);
			Assert.assertEquals(String.valueOf(response.getStatusCode()),"200");
			collectappnbotdetails = new LinkedHashMap<String, String>();			
			collectappnbotdetails.put("emailId", response.jsonPath().get("emailId").toString());			
			collectappnbotdetails.put("accountId", response.jsonPath().get("accountId").toString());
			collectappnbotdetails.put("userId", response.jsonPath().get("userId").toString());				  
			System.out.println(collectappnbotdetails);
		}catch(Exception e)
		{
			e.printStackTrace();
		}		
	}

	@Test(priority = 2, enabled = true)
	public static void createAdminApp() {
		try {						
			System.out.println("---02---------------createAdminApp---------------------------");
			Response responseadmin = RestAssured.
					given()
					.headers(EnviromentSetup.HeadersWithAPIKey()).log().all()
					.body(EnviromentSetup.createAdminAPPPayLoad(collectappnbotdetails.get("accountId"),collectappnbotdetails.get("userId")))
					.when()
					.post(url+internalClientappResource)					
					.then().log().all()
					.extract().response();	
			Thread.sleep(5000);
			Assert.assertEquals(String.valueOf(responseadmin.getStatusCode()),"200");
			collectappnbotdetails.put("Name",responseadmin.jsonPath().get("name").toString());
			collectappnbotdetails.put("cId", responseadmin.jsonPath().get("cId").toString());
			collectappnbotdetails.put("cS", responseadmin.jsonPath().get("cS").toString());
			collectappnbotdetails.put("nId", responseadmin.jsonPath().get("nId").toString());	
			collectappnbotdetails.put("accountId", responseadmin.jsonPath().get("accountId").toString());
			collectappnbotdetails.put("userId", responseadmin.jsonPath().get("nId").toString());
			System.out.println(collectappnbotdetails);
		}catch(Exception e)
		{
			e.printStackTrace();
		}		
	}

	@Test(priority = 3, enabled = true)
	public static void genereateJWTtoken()
	{
		System.out.println("-03--------genereateJWTtoken------------------"); 
		Response responsejwtToken = RestAssured.
				given()
				.headers(EnviromentSetup.HeadersWithAPIKey()).log().all()
				.body(EnviromentSetup.genereateJWTtokenPayLoad(collectappnbotdetails.get("cId"),collectappnbotdetails.get("cS"),collectappnbotdetails.get("nId")))
				.when()
				.post(urljwtTokenGenerater)					
				.then().log().all()
				.extract().response();			
		Assert.assertEquals(String.valueOf(responsejwtToken.getStatusCode()),"200");
		collectappnbotdetails.put("jwt",responsejwtToken.jsonPath().get("jwt").toString());				
	}

	/**
	 * 
	 *  This  Steps depends on "configure as wf admin" step after Onboard env level user :: Here are not linking with any bot so we are skipping this step
	 */
	//	@Test(priority = 4, enabled = true)
	//	public static void cloningSmapleBot()
	//	{
	//		System.out.println("-04--------cloningSmapleBot------------------"); 
	//		Response responsecloningSmapleBot = 
	//				given()
	//				.headers(EnviromentSetup.headersforcloneBotpayLoad(collectappnbotdetails.get("jwt"),collectappnbotdetails.get("userId"), collectappnbotdetails.get("accountId")))				
	//				.when()  
	//				.get(url+"/api/public/samplebots/"+"st-80f72c3e-5746-51f7-b5ab-a68fba9187f7"+"/add?")  //hrere  ubVersion=1 wont be there 					
	//				.then().log().all()
	//				.extract().response();			
	//		collectappnbotdetails.put("clonnedBot_StreamID",responsecloningSmapleBot.jsonPath().get("_id").toString());									
	//		collectappnbotdetails.put("clonnedBotName",responsecloningSmapleBot.jsonPath().get("name").toString());
	//		//
	//	}

	/*
	 * Setting up KORA BOT
	 */
	@Test(priority = 5, enabled = true)
	public static void uploadFile() throws InterruptedException
	{
		System.out.println("--05----------------uploadFile_botDef---------------------------");   
		File botdeffile = new File("Korabot/botDefinition.json");
		File configfile = new File("Korabot/config.json");
		File iconfile = new File("Korabot/icon.png");

		Response responsejwtTokenbotdef = RestAssured.
				given()
				.headers(EnviromentSetup.HeadersWithJWTTokenforUpload(collectappnbotdetails.get("jwt")))
				.multiPart(botdeffile).multiPart("fileContext", "bulkImport")
				.when()
				.post(url+publicuploadFile)					
				.then().log().all()
				.extract().response();				
		Thread.sleep(10000);
		Assert.assertEquals(String.valueOf(responsejwtTokenbotdef.getStatusCode()),"200");

		collectappnbotdetails.put("BotDef_fileId",responsejwtTokenbotdef.jsonPath().get("fileId").toString()); 		

		System.out.println("------------------uploadFile_config---------------------------");
		Response responsejwtTokenbotconfg = RestAssured.
				given()
				.headers(EnviromentSetup.HeadersWithJWTTokenforUpload(collectappnbotdetails.get("jwt")))
				.multiPart(configfile).multiPart("fileContext", "bulkImport")
				.when()
				.post(url+publicuploadFile)					
				.then().log().all()
				.extract().response();				
		Thread.sleep(5000);	
		Assert.assertEquals(String.valueOf(responsejwtTokenbotconfg.getStatusCode()),"200");
		JsonPath jsonPathEvaluatoradminbotcong = responsejwtTokenbotconfg.jsonPath();											
		collectappnbotdetails.put("BotConfig_fileId",jsonPathEvaluatoradminbotcong.get("fileId").toString()); 

		System.out.println("------------------uploadFile_icon---------------------------");
		Response responsejwtTokenboticon = RestAssured.
				given()
				.headers(EnviromentSetup.HeadersWithJWTTokenforUpload(collectappnbotdetails.get("jwt")))
				.multiPart(iconfile).multiPart("fileContext", "bulkImport")
				.when()
				.post(url+publicuploadFile)					
				.then().log().all()
				.extract().response();				
		Thread.sleep(5000);		
		Assert.assertEquals(String.valueOf(responsejwtTokenboticon.getStatusCode()),"200");
		JsonPath jsonPathEvaluatoradminboticon= responsejwtTokenboticon.jsonPath();											
		collectappnbotdetails.put("BotIcon_fileId",jsonPathEvaluatoradminboticon.get("fileId").toString()); 
	}

	@Test(priority = 6, enabled = true)
	public static void importBot() throws InterruptedException
	{
		System.out.println("-06--------------------------ImportBot------------------------------------"); 
		Response responsejwtToken = RestAssured.
				given()
				.headers(EnviromentSetup.HeadersWithJWTToken(collectappnbotdetails.get("jwt")))
				.body(EnviromentSetup.importBotPayLoad(collectappnbotdetails.get("BotDef_fileId"),collectappnbotdetails.get("BotConfig_fileId"),collectappnbotdetails.get("BotIcon_fileId")))
				.when()
				.post(url+publicimportBOTresource)					
				.then().log().all()
				.extract().response();
		Thread.sleep(60000);
		Assert.assertEquals(String.valueOf(responsejwtToken.getStatusCode()),"200");
		collectappnbotdetails.put("streamRefId",responsejwtToken.jsonPath().get("streamRefId").toString()); 
		collectappnbotdetails.put("bir_id",responsejwtToken.jsonPath().get("_id").toString());
		  
	}

	@Test(priority = 7, enabled = true)
	public static void importBotStatus() throws InterruptedException
	{

		System.out.println("-07----------------------ImportBot_Status---------------------------"); 		
		Response responseimportBotStatus = RestAssured.
				given()
				.headers(EnviromentSetup.HeadersWithJWTToken(collectappnbotdetails.get("jwt")))				
				.when().log().all()
				.get(url+publicimportBOTstatus+collectappnbotdetails.get("bir_id"))				
				.then().log().all()
				.extract().response();
		Assert.assertEquals(String.valueOf(responseimportBotStatus.getStatusCode()),"200");
		waitincreamentalLoop=1;
		doloop: do {
			waitincreamentalLoop++;
			Thread.sleep(10000);
			if(responseimportBotStatus.jsonPath().get("status").toString().equalsIgnoreCase("success"));
			{
				break doloop;
			}
		}
		while (waitincreamentalLoop <= 5 || (!responseimportBotStatus.jsonPath().get("status").toString().equalsIgnoreCase("success"))) ;
		collectappnbotdetails.put("importedBot_status",responseimportBotStatus.jsonPath().get("status").toString());
		collectappnbotdetails.put("importedBot_streamId",responseimportBotStatus.jsonPath().get("streamId").toString());
		  
	}

	@Test(priority = 8, enabled = true)
	public static void configuringENVvar() throws InterruptedException
	{
		if(collectappnbotdetails.get("importedBot_status").equalsIgnoreCase("success"))
		{
			System.out.println("----08-------------------configure environment variable--------------------------"); 
			Response responseconfiguringENVvar = RestAssured.
					given()
					.headers(EnviromentSetup.HeaderswithJWTnAccountID(collectappnbotdetails.get("jwt"),collectappnbotdetails.get("accountId")))
					.body(EnviromentSetup.configuringENVvarpayLoad())
					.when()
					.put(url+publicEnableSdk+collectappnbotdetails.get("importedBot_streamId")+"/setup")					
					.then().log().all()
					.extract().response();		
			Assert.assertEquals(String.valueOf(responseconfiguringENVvar.getStatusCode()),"200");
			System.out.println("configure environment variable Status" +"::"+ responseconfiguringENVvar.getStatusCode()+responseconfiguringENVvar.getStatusLine());
			collectappnbotdetails.put("configure environment variable Status",responseconfiguringENVvar.getStatusCode()+responseconfiguringENVvar.getStatusLine());
			if(collectappnbotdetails.get("configure environment variable Status").contains("401 Unauthorized"))
			{
				EnviromentSetup.genereateJWTtoken();
				System.out.println("-----------------------Generating JWT token Again after expiring--------------------------");
				EnviromentSetup.configuringENVvar();

			}
		}else
		{
			System.out.println("----------------- ImportBot_Status  Failed  ------------------");
		}

	}

	@Test(priority = 9, enabled = true)
	public static void createbuilderAppnonAdmin() throws InterruptedException
	{
		System.out.println("-09----------------------Create Non-Amin Builder app---------------------------"); 
		Response responsbuilderapp = RestAssured.
				given()
				.headers(EnviromentSetup.HeadersWithAPIKey()).log().all()
				.body(EnviromentSetup.createbuilderAppnonAdminpayLoad(collectappnbotdetails.get("importedBot_streamId"),collectappnbotdetails.get("accountId"),collectappnbotdetails.get("userId")))
				.when()
				.post(url+internalClientappResource)					
				.then().log().all()
				.extract().response();	
		Thread.sleep(5000);
		Assert.assertEquals(String.valueOf(responsbuilderapp.getStatusCode()),"200");		
		collectappnbotdetails.put("BuilderApp_Name",responsbuilderapp.jsonPath().get("name").toString());
		collectappnbotdetails.put("BuilderApp_sdkClientId", responsbuilderapp.jsonPath().get("cId").toString());
		collectappnbotdetails.put("BuilderApp_cS", responsbuilderapp.jsonPath().get("cS").toString());
		collectappnbotdetails.put("BuilderApp_UserId", responsbuilderapp.jsonPath().get("nId").toString());	
		  				
	}

	@Test(priority = 10, enabled = true)
	public static void enableRTM() throws InterruptedException
	{
		System.out.println("-10---------------------- enableRTM ---------------------------"); 
		Response responseadmin = RestAssured.
				given()
				.headers(EnviromentSetup.HeadersWithJWTToken(collectappnbotdetails.get("jwt"))).log().all()
				.body(EnviromentSetup.enableRTMpayLoad(collectappnbotdetails.get("importedBot_streamId"),collectappnbotdetails.get("BuilderApp_Name"),collectappnbotdetails.get("BuilderApp_sdkClientId"))) 
				.when()
				.post(url+publicEnableTRMChannelsResource)					
				.then().log().all()
				.extract().response();	
		Thread.sleep(5000);
		Assert.assertEquals(String.valueOf(responseadmin.getStatusCode()),"200");
		System.out.println(" Status code Enable RTM "+responseadmin.jsonPath().get("status"));			
	}

	@Test(priority = 11, enabled = true)
	public static void genereateJWTtokenforNonAmdinApp()
	{
		System.out.println("-11--------genereateJWTtoken for non Admin app------------------"); 
		Response responsegenereateJWTtokenforNonAmdinApp = RestAssured.
				given()
				.headers(EnviromentSetup.HeadersWithAPIKey())
				.body(EnviromentSetup.genereateJWTtokenPayLoad(collectappnbotdetails.get("BuilderApp_sdkClientId"),collectappnbotdetails.get("BuilderApp_cS"),collectappnbotdetails.get("BuilderApp_UserId")))
				.when()
				.post(urljwtTokenGenerater)					
				.then().log().all()
				.extract().response();			

		Assert.assertEquals(String.valueOf(responsegenereateJWTtokenforNonAmdinApp.getStatusCode()),"200");
		collectappnbotdetails.put("builderApp_jwt",responsegenereateJWTtokenforNonAmdinApp.jsonPath().get("jwt").toString());
		  		
	}

	@Test(priority = 12, enabled = true)
	public static void publishbot() throws InterruptedException
	{
		System.out.println("--12--------------------- Publish Bot ---------------------------");
		Response responsPublishBot = RestAssured.
				given()
				.headers(EnviromentSetup.Headersforpublishbot(collectappnbotdetails.get("jwt")))
				.body(EnviromentSetup.publishbotPayload()).log().all() 
				.when()
				.post(url+publicEnableSdk+collectappnbotdetails.get("importedBot_streamId")+"/publish")					
				.then()
				.extract().response();
		Assert.assertEquals(String.valueOf(responsPublishBot.getStatusCode()),"200");
		System.out.println("Publish bot Status code "+responsPublishBot.jsonPath().get("status"));
		System.out.println(collectappnbotdetails);
	}	

	@Test(priority = 13, enabled = true)
	public static void getRole() throws InterruptedException
	{

		System.out.println("-13--------------------------Get Role------------------------------------"); 
		Response responsegetRole = RestAssured.
				given()
				.headers(EnviromentSetup.HeadersWithJWTToken(collectappnbotdetails.get("jwt"))).log().all()				
				.when()
				.get(url+publicGetRolesResource)					
				.then().log().all()
				.extract().response();		
		Assert.assertEquals(String.valueOf(responsegetRole.getStatusCode()),"200");
		JsonPath jp = responsegetRole.jsonPath();	
		int numberofroles = jp.getInt("roles.size()");
		String DeveloperRole_id= "";
		for(int i=0; i<numberofroles;i++)
		{
			String role=jp.get("roles["+i+"].role");
			String role_id=jp.get("roles["+i+"]._id");
			System.out.println("Role : "+role +"Role _id : "+role_id);

			if(role.equalsIgnoreCase("Bot Developer"))
			{
				DeveloperRole_id= role_id;
			}
		}
		collectappnbotdetails.put("Dev_role_id",DeveloperRole_id); 				  
	}

	/*
	 * As testing practice here Giving developer email address same everytime
	 */
	@Test(priority = 14, enabled = true)
	public static void AddingDeveloperasOwner() throws InterruptedException
	{		
		System.out.println("-----14----------------------Adding Developer as Owner MAP to Owner------------------------------------"); 
		Response responseAddingDeveloperasOwner = RestAssured.
				given()
				.headers(EnviromentSetup.HeadersWithJWTToken(collectappnbotdetails.get("jwt")))
				.body(EnviromentSetup.addDeveloperpayLoad(developeremailaddress,collectappnbotdetails.get("Dev_role_id"), collectappnbotdetails.get("importedBot_streamId")))  
				.when()
				.post(url+publicadminasUBDevResource)					
				.then().log().all()
				.extract().response();
		Thread.sleep(5000);
		Assert.assertEquals(String.valueOf(responseAddingDeveloperasOwner.getStatusCode()),"200");
		System.out.println("Adding Developer as Owner Response ::"+responseAddingDeveloperasOwner.asString());
		String MaptoMomainrsponse=responseAddingDeveloperasOwner.jsonPath().get("msg");
		if(MaptoMomainrsponse.contains(developeremailaddress +"user created successfully"))
		{
			System.out.println(developeremailaddress +":: user created successfully as added as developer");
		}else {
			System.out.println(responseAddingDeveloperasOwner);
		}
	}

}