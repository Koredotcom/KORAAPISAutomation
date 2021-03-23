package platform.api;


import java.io.File;

import org.testng.Assert;
import org.testng.annotations.Test;

import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import java.util.LinkedHashMap;

public class TenantOnboarding extends Payloads{

	@Test(priority = 15, enabled = true)
	public static void createUser() {
		try {
			System.out.println("-01-----------------createUser  Tenant Onboarding---------------------------");
			TimeinHHMMSS=Payloads.fntoreturntimeinHHMMSS();
			Response response = RestAssured
					.given().headers(TenantOnboarding.HeadersWithAPIKey()).log().all() 
					.body(TenantOnboarding.createUserPayLoad(TimeinHHMMSS))
					.when()
					.post(url+internalAccountResource)
					.then().log().all()
					.extract().response();	
			Thread.sleep(5000);
			collectappnbotdetails = new LinkedHashMap<String, String>();			
			collectappnbotdetails.put("emailId", response.jsonPath().get("emailId").toString());			
			collectappnbotdetails.put("accountId", response.jsonPath().get("accountId").toString());
			collectappnbotdetails.put("userId", response.jsonPath().get("userId").toString());	
			System.out.println(collectappnbotdetails);
			Assert.assertEquals(String.valueOf(response.getStatusCode()),"200");
			TimeinHHMMSS=null;
		}catch(Exception e)
		{
			TimeinHHMMSS=null;
			e.printStackTrace();
		}		
	}

	@Test(priority = 16, enabled = true)
	public static void createAdminApp() {
		try {						
			System.out.println("---02---------------createAdminApp---------------------------");
			Response responseadmin = RestAssured.
					given()
					.headers(TenantOnboarding.HeadersWithAPIKey()).log().all()
					.body(TenantOnboarding.createAdminAPPPayLoad(collectappnbotdetails.get("accountId"),collectappnbotdetails.get("userId")))
					.when()
					.post(url+internalClientappResource)					
					.then().log().all()
					.extract().response();	
			Thread.sleep(5000);										
			collectappnbotdetails.put("Name",responseadmin.jsonPath().get("name").toString());
			collectappnbotdetails.put("cId", responseadmin.jsonPath().get("cId").toString());
			collectappnbotdetails.put("cS", responseadmin.jsonPath().get("cS").toString());
			collectappnbotdetails.put("nId", responseadmin.jsonPath().get("nId").toString());	
			collectappnbotdetails.put("accountId", responseadmin.jsonPath().get("accountId").toString());
			collectappnbotdetails.put("userId", responseadmin.jsonPath().get("nId").toString());
			Assert.assertEquals(String.valueOf(responseadmin.getStatusCode()),"200");
			System.out.println(collectappnbotdetails);
		}catch(Exception e)
		{
			e.printStackTrace();
		}		
	}

	/**
	 * Token Gnerated with Admin is used for only Public APIS'
	 */
	@Test(priority = 17, enabled = true)
	public static void genereateJWTtoken()
	{
		System.out.println("-03--------genereateJWTtoken------------------"); 
		Response responsejwtToken = RestAssured.
				given()
				.headers(TenantOnboarding.HeadersWithAPIKey()).log().all()
				.body(TenantOnboarding.genereateJWTtokenPayLoad(collectappnbotdetails.get("cId"),collectappnbotdetails.get("cS"),collectappnbotdetails.get("nId")))
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
	 * @throws InterruptedException 
	 */
	@Test(priority = 18, enabled = true)
	public static void cloningSmapleBot() throws InterruptedException
	{
		System.out.println("-04--------cloning UB BOT------------------");
		if (url.contains("koradev-bots.kora.ai")) {
			cloningbot=	devUniversalBOT;				
		} else if (url.contains("qa1-bots.kore.ai")) {
			cloningbot=	qa1UniversalBOT;			
		} else {
			System.out.println(" Given URL "+ url+" is neither koradev-bots.kora.ai nor qa1-bots.kora.ai");
		}
		 
		Response responsecloningSmapleBot = RestAssured.
				given()
				.headers(TenantOnboarding.headersforcloneBotpayLoad(collectappnbotdetails.get("jwt"),collectappnbotdetails.get("userId"), collectappnbotdetails.get("accountId")))				
				.when()  
				.get(url+"/api/public/samplebots/"+cloningbot+"/add?ubVersion=1")   					
				.then().log().all()
				.extract().response();		
		Thread.sleep(5000);
		Assert.assertEquals(String.valueOf(responsecloningSmapleBot.getStatusCode()),"200");
		collectappnbotdetails.put("clonnedBot_StreamID",responsecloningSmapleBot.jsonPath().get("_id").toString());									
		collectappnbotdetails.put("clonnedBotName",responsecloningSmapleBot.jsonPath().get("name").toString());
		
	}

	@Test(priority = 19, enabled = true)
	public static void clonedBotSetup() throws InterruptedException
	{
		System.out.println("-05--------cloned BOT Setup------------------"); 
		Response responsecloningSmapleBot = RestAssured.
				given()
				.headers(TenantOnboarding.HeaderswithJWTnAccountID(collectappnbotdetails.get("jwt"), collectappnbotdetails.get("accountId")))				
				.body(TenantOnboarding.clonedBot_SetuppayLoad(collectappnbotdetails.get("clonnedBotName")))
				.when()  
				.put(url+"/api/public/bot/"+ collectappnbotdetails.get("clonnedBot_StreamID")+"/setup")  					
				.then().log().all()
				.extract().response();	
		Thread.sleep(5000);
		Assert.assertEquals(String.valueOf(responsecloningSmapleBot.getStatusCode()),"200");
		System.out.println("Clonned bot Setup Status ::" +responsecloningSmapleBot.getStatusCode());	
	}


	@Test(priority = 20, enabled = true)
	public static void createbuilderAppnforUB() throws InterruptedException
	{
		System.out.println("-06----------------------Create Non-Amin Builder app  for UB---------------------------"); 
		Response responsbuilderapp = RestAssured.
				given()
				.headers(TenantOnboarding.HeadersWithAPIKey())
				.body(TenantOnboarding.createbuilderAppnonAdminpayLoad(collectappnbotdetails.get("clonnedBot_StreamID"),collectappnbotdetails.get("accountId"),collectappnbotdetails.get("userId")))
				.when()
				.post(url+internalClientappResource)					
				.then().log().all()
				.extract().response();	
		Thread.sleep(5000);
		Assert.assertEquals(String.valueOf(responsbuilderapp.getStatusCode()),"200");		
		collectappnbotdetails.put("UB_BuilderApp_Name",responsbuilderapp.jsonPath().get("name").toString());
		collectappnbotdetails.put("UB_BuilderApp_sdkClientId", responsbuilderapp.jsonPath().get("cId").toString());
		collectappnbotdetails.put("UB_BuilderApp_cS", responsbuilderapp.jsonPath().get("cS").toString());
		collectappnbotdetails.put("UB_BuilderApp_UserId", responsbuilderapp.jsonPath().get("nId").toString());	
						
	}

	@Test(priority = 21, enabled = true)
	public static void enableRTMforUBbuilderAPP() throws InterruptedException
	{
		System.out.println("-07--------------------- enableRTM for UB bot builder app---------------------------"); 
		Response responseenableRTMforUBbuilderAPP = RestAssured.
				given()
				.headers(TenantOnboarding.HeadersWithJWTToken(collectappnbotdetails.get("jwt"))).log().all()
				.body(TenantOnboarding.enableRTMpayLoad(collectappnbotdetails.get("clonnedBot_StreamID"),collectappnbotdetails.get("UB_BuilderApp_Name"),collectappnbotdetails.get("UB_BuilderApp_sdkClientId"))) 
				.when()
				.post(url+publicEnableTRMChannelsResource)					
				.then().log().all()
				.extract().response();	
		Thread.sleep(5000);
		Assert.assertEquals(String.valueOf(responseenableRTMforUBbuilderAPP.getStatusCode()),"200");
		System.out.println(" Status code Enable RTM for UB bot"+responseenableRTMforUBbuilderAPP.asString());			
	}


	@Test(priority = 22, enabled = true)
	public static void genereateJWTtokenUB_BuilderApp()
	{
		System.out.println("-----08---genereateJWTtokenUB_BuilderApp------------------"); 
		Response responsejwtToken = RestAssured.
				given()
				.headers(TenantOnboarding.HeadersWithAPIKey()).log().all()
				.body(TenantOnboarding.genereateJWTtokenPayLoad(collectappnbotdetails.get("UB_BuilderApp_sdkClientId"),collectappnbotdetails.get("UB_BuilderApp_cS"),collectappnbotdetails.get("UB_BuilderApp_UserId")))
				.when()
				.post(urljwtTokenGenerater)					
				.then().log().all()
				.extract().response();			
		Assert.assertEquals(String.valueOf(responsejwtToken.getStatusCode()),"200");
		collectappnbotdetails.put("UB_BuilderAPP_jwt",responsejwtToken.jsonPath().get("jwt").toString());
		
	}

	/*
	 * Setting up KORA BOT
	 */
	@Test(priority = 23, enabled = true)
	public static void uploadFile() throws InterruptedException
	{
		System.out.println("--09----------------uploadFile_botDef---------------------------");   
		File botdeffile = new File("Korabot/botDefinition.json");
		File configfile = new File("Korabot/config.json");
		File iconfile = new File("Korabot/icon.png");
//		 Bot name abiold
//		File botdeffile = new File("ABIMYIT/botDefinition.json");
//		File configfile = new File("ABIMYIT/config.json");
//		File iconfile = new File("ABIMYIT/icon.png");

		Response responseuploadFile = RestAssured.
				given()
				.headers(TenantOnboarding.HeadersWithJWTTokenforUpload(collectappnbotdetails.get("jwt")))
				.multiPart(botdeffile).multiPart("fileContext", "bulkImport")
				.when()
				.post(url+publicuploadFile)					
				.then().log().all()
				.extract().response();				
		Thread.sleep(10000);
		Assert.assertEquals(String.valueOf(responseuploadFile.getStatusCode()),"200");													
		collectappnbotdetails.put("BotDef_fileId",responseuploadFile.jsonPath().get("fileId").toString()); 		

		System.out.println("------------------uploadFile_config---------------------------");
		Response responsejwtTokenbotconfg = RestAssured.
				given()
				.headers(TenantOnboarding.HeadersWithJWTTokenforUpload(collectappnbotdetails.get("jwt")))
				.multiPart(configfile).multiPart("fileContext", "bulkImport")
				.when()
				.post(url+publicuploadFile)					
				.then().log().all()
				.extract().response();				
		Thread.sleep(5000);														
		collectappnbotdetails.put("BotConfig_fileId",responsejwtTokenbotconfg.jsonPath().get("fileId").toString()); 

		System.out.println("------------------uploadFile_icon---------------------------");
		Response responsejwtTokenboticon = RestAssured.
				given()
				.headers(TenantOnboarding.HeadersWithJWTTokenforUpload(collectappnbotdetails.get("jwt")))
				.multiPart(iconfile).multiPart("fileContext", "bulkImport")
				.when()
				.post(url+publicuploadFile)					
				.then().log().all()
				.extract().response();				
		Thread.sleep(5000);		
		JsonPath jsonPathEvaluatoradminboticon= responsejwtTokenboticon.jsonPath();											
		collectappnbotdetails.put("BotIcon_fileId",jsonPathEvaluatoradminboticon.get("fileId").toString()); 
	}

	@Test(priority = 24, enabled = true)
	public static void importBot() throws InterruptedException
	{
		System.out.println("-10--------------------------ImportBot------------------------------------"); 
		Response responsejwtToken = RestAssured.
				given()
				.headers(TenantOnboarding.HeadersWithJWTToken(collectappnbotdetails.get("jwt")))
				.body(TenantOnboarding.importBotPayLoad(collectappnbotdetails.get("BotDef_fileId"),collectappnbotdetails.get("BotConfig_fileId"),collectappnbotdetails.get("BotIcon_fileId")))
				.when()
				.post(url+publicimportBOTresource)					
				.then().log().all()
				.extract().response();		
		Assert.assertEquals(String.valueOf(responsejwtToken.getStatusCode()),"200");
									
		collectappnbotdetails.put("streamRefId",responsejwtToken.jsonPath().get("streamRefId").toString()); 
		collectappnbotdetails.put("bir_id",responsejwtToken.jsonPath().get("_id").toString());
		
	}

	@Test(priority = 25, enabled = true)
	public static void importBotStatus() throws InterruptedException
	{
		System.out.println("-11----------------------ImportBot_Status---------------------------"); 
		Response responseimportBotStatus = RestAssured.
				given()
				.headers(TenantOnboarding.HeadersWithJWTToken(collectappnbotdetails.get("jwt")))				
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

	@Test(priority = 26, enabled = true)
	public static void createbuilderAppNONAdmin() throws InterruptedException
	{
		System.out.println("-12----------------------Create Non-Amin Builder app---------------------------"); 
		Response responsbuilderapp = RestAssured.
				given()
				.headers(TenantOnboarding.HeadersWithAPIKey()).log().all()
				.body(TenantOnboarding.createbuilderAppnonAdminpayLoad(collectappnbotdetails.get("importedBot_streamId"),collectappnbotdetails.get("accountId"),collectappnbotdetails.get("userId")))
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

	@Test(priority = 27, enabled = true)
	public static void enableRTM() throws InterruptedException
	{
		System.out.println("-13---------------------- enableRTM ---------------------------"); 
		Response responseadmin = RestAssured.
				given()
				.headers(TenantOnboarding.HeadersWithJWTToken(collectappnbotdetails.get("jwt"))).log().all()
				.body(TenantOnboarding.enableRTMpayLoad(collectappnbotdetails.get("importedBot_streamId"),collectappnbotdetails.get("BuilderApp_Name"),collectappnbotdetails.get("BuilderApp_sdkClientId"))) 
				.when()
				.post(url+publicEnableTRMChannelsResource)					
				.then().log().all()
				.extract().response();	
		Thread.sleep(5000);
		Assert.assertEquals(String.valueOf(responseadmin.getStatusCode()),"200");
			
	}

	@Test(priority = 28, enabled = true)
	public static void genereateJWTtokenbuilderapp()
	{
		System.out.println("14--------genereateJWTtokenbuilderapp------------------"); 
		Response responsejwtToken = RestAssured.
				given()
				.headers(TenantOnboarding.HeadersWithAPIKey()).log().all()
				.body(TenantOnboarding.genereateJWTtokenPayLoad(collectappnbotdetails.get("BuilderApp_sdkClientId"),collectappnbotdetails.get("BuilderApp_cS"),collectappnbotdetails.get("BuilderApp_UserId")))
				.when()
				.post(urljwtTokenGenerater)					
				.then().log().all()
				.extract().response();			
		Assert.assertEquals(String.valueOf(responsejwtToken.getStatusCode()),"200");
		collectappnbotdetails.put("builderApp_jwt",responsejwtToken.jsonPath().get("jwt").toString());
		
	}

	@Test(priority = 29, enabled = true)
	public static void publishbotStandardBot() throws InterruptedException
	{
		System.out.println("--15--------------------- Publish Bot standard bot---------------------------");
		Response responsPublishBot = RestAssured.
				given()
				.headers(TenantOnboarding.Headersforpublishbot(collectappnbotdetails.get("jwt")))
				.body(TenantOnboarding.publishbotPayload()).log().all() 
				.when()
				.post(url+publicEnableSdk+collectappnbotdetails.get("importedBot_streamId")+"/publish")					
				.then()
				.extract().response();	
		Thread.sleep(5000);
		System.out.println("Publish bot Status  "+responsPublishBot.jsonPath().get("status"));
		Assert.assertEquals(String.valueOf(responsPublishBot.getStatusCode()),"200");			
	}

	@Test(priority = 30, enabled = true)
	public static void linkChildBot() throws InterruptedException
	{
		System.out.println("---16--------------------Linking Child bot to UB---------------------------"); 
		Response responseadmin = RestAssured.
				given()
				.headers(TenantOnboarding.HeadersWithJWTToken(collectappnbotdetails.get("UB_BuilderAPP_jwt"))).log().all()
				.body(TenantOnboarding.linkChildBotpayLoad(collectappnbotdetails.get("importedBot_streamId"),"KoraBot",collectappnbotdetails.get("builderApp_jwt"))).log().all()
				.when().log().all()
				.post(url+"/api/public/bot/"+collectappnbotdetails.get("clonnedBot_StreamID")+"/universalbot/link")			 		
				.then().log().all()
				.extract().response();	
		Thread.sleep(5000);
		Assert.assertEquals(String.valueOf(responseadmin.getStatusCode()),"200");			

	}




}
