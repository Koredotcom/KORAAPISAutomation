package platform.api;


import java.io.File;

import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import java.util.LinkedHashMap;

public class TenantOnboarding extends Payloads{

	@AfterMethod
	public void afterMethodTenantOnboarding(ITestResult result) {

		if(result.getStatus() == ITestResult.SUCCESS)
		{
			results_Tenant.put(result.getMethod().getMethodName(), "Pass");
		}
		else if(result.getStatus() == ITestResult.FAILURE)
		{			
			results_Tenant.put(result.getMethod().getMethodName(), "Fail");
		}	 
		if (results_Tenant.get(result.getMethod().getMethodName()).equalsIgnoreCase("pass")) {
			Tenant_passcount++;
		} else if (results_Tenant.get(result.getMethod().getMethodName()).equalsIgnoreCase("fail")) {
			Tenant_failcount++;		
		}		
	}
	@Test(priority = 15, enabled = true)
	public static void createUserinTenantOnboarding() {
		try {
			System.out.println("-15-----------------createUser  Tenant Onboarding---------------------------");
			String toTimeinHHMMSS=Payloads.fntoreturntimeinHHMMSS();
			Response responsecutnt = RestAssured
					.given().headers(TenantOnboarding.HeadersWithAPIKey()) 
					.body(TenantOnboarding.createUserPayLoad(toTimeinHHMMSS))
					.when()
					.post(url+internalAccountResource)
					.then().log().all()
					.extract().response();	
			Thread.sleep(5000);
			collectappnbotdetails = new LinkedHashMap<String, String>();			
			collectappnbotdetails.put("emailId", responsecutnt.jsonPath().get("emailId").toString());			
			collectappnbotdetails.put("accountId", responsecutnt.jsonPath().get("accountId").toString());
			collectappnbotdetails.put("userId", responsecutnt.jsonPath().get("userId").toString());	
			System.out.println(collectappnbotdetails);
			Assert.assertEquals(String.valueOf(responsecutnt.getStatusCode()),"200");			
		}catch(Exception e)
		{			
			Assert.fail();
			e.printStackTrace();
		}		
	}

	@Test(priority = 16, enabled = true)
	public static void createAdminAppinTenantOnboarding() {
		try {						
			System.out.println("---16---------------createAdminApp---------------------------");
			Response responseadmintnt = RestAssured.
					given()
					.headers(TenantOnboarding.HeadersWithAPIKey())
					.body(TenantOnboarding.createAdminAPPPayLoad(collectappnbotdetails.get("accountId"),collectappnbotdetails.get("userId")))
					.when()
					.post(url+internalClientappResource)					
					.then().log().all()
					.extract().response();	
			Thread.sleep(5000);										
			collectappnbotdetails.put("Name",responseadmintnt.jsonPath().get("name").toString());
			collectappnbotdetails.put("cId", responseadmintnt.jsonPath().get("cId").toString());
			collectappnbotdetails.put("cS", responseadmintnt.jsonPath().get("cS").toString());
			collectappnbotdetails.put("nId", responseadmintnt.jsonPath().get("nId").toString());	
			collectappnbotdetails.put("accountId", responseadmintnt.jsonPath().get("accountId").toString());
			collectappnbotdetails.put("userId", responseadmintnt.jsonPath().get("nId").toString());

			System.out.println(collectappnbotdetails);
			TimeinHHMMSS=null;
			Assert.assertEquals(String.valueOf(responseadmintnt.getStatusCode()),"200");
		}catch(Exception e)
		{
			TimeinHHMMSS=null;
			Assert.fail();
			e.printStackTrace();
		}		
	}

	/**
	 * Token Gnerated with Admin is used for only Public APIS'
	 */
	@Test(priority = 17, enabled = true)
	public static void genereateJWTtokeninTenantOnboarding()
	{
		try {
			System.out.println("-17--------genereateJWTtoken------------------"); 
			Response responsejwtTokentnt = RestAssured.
					given()
					.headers(TenantOnboarding.HeadersWithAPIKey())
					.body(TenantOnboarding.genereateJWTtokenPayLoad(collectappnbotdetails.get("cId"),collectappnbotdetails.get("cS"),collectappnbotdetails.get("nId")))
					.when()
					.post(urljwtTokenGenerater)					
					.then().log().all()
					.extract().response();					
			collectappnbotdetails.put("jwt",responsejwtTokentnt.jsonPath().get("jwt").toString());
			Assert.assertEquals(String.valueOf(responsejwtTokentnt.getStatusCode()),"200");
		}catch(Exception e)
		{
			Assert.fail();
			e.printStackTrace();
		}
	}

	/**
	 * 
	 *  This  Steps depends on "configure as wf admin" step after Onboard env level user :: Here are not linking with any bot so we are skipping this step
	 * @throws InterruptedException 
	 */
	@Test(priority = 18, enabled = true)
	public static void cloningSmapleBotinTenantOnboarding() throws InterruptedException
	{
		try {
			System.out.println("-18--------cloning UB BOT------------------");
			if (url.contains("koradev-bots.kora.ai")) {
				cloningbot=	devUniversalBOT;				
			} else if (url.contains("qa1-bots.kore.ai")) {
				cloningbot=	qa1UniversalBOT;			
			}else if (url.contains("staging-bots.korebots.com")) {
				cloningbot=	stagingUniversalBOT;
			} else {
				System.out.println(" Given URL "+ url+" is neither koradev-bots.kora.ai nor qa1-bots.kora.ai or Staging Env");
			}

			Response responsecloningSmapleBottnt = RestAssured.
					given()
					.headers(TenantOnboarding.headersforcloneBotpayLoad(collectappnbotdetails.get("jwt"),collectappnbotdetails.get("userId"), collectappnbotdetails.get("accountId"))).log().all()				
					.when().log().all()  
					.get(url+"/api/public/samplebots/"+cloningbot+"/add")   					
					.then().log().all()
					.extract().response();		
			Thread.sleep(5000);		
			collectappnbotdetails.put("clonnedBot_StreamID",responsecloningSmapleBottnt.jsonPath().get("_id").toString());									
			collectappnbotdetails.put("clonnedBotName",responsecloningSmapleBottnt.jsonPath().get("name").toString());
			Assert.assertEquals(String.valueOf(responsecloningSmapleBottnt.getStatusCode()),"200");
		}catch(Exception e)
		{
			Assert.fail();
			e.printStackTrace();
		}
	}

	@Test(priority = 19, enabled = true)
	public static void clonedBotSetupinTenantOnboarding() throws InterruptedException
	{
		try {
			System.out.println("-19--------cloned BOT Setup------------------"); 
			Response responsecloningSmapleBottnt = RestAssured.
					given()
					.headers(TenantOnboarding.HeaderswithJWTnAccountID(collectappnbotdetails.get("jwt"), collectappnbotdetails.get("accountId")))				
					.body(TenantOnboarding.clonedBot_SetuppayLoad(collectappnbotdetails.get("clonnedBotName")))
					.when()  
					.put(url+"/api/public/bot/"+ collectappnbotdetails.get("clonnedBot_StreamID")+"/setup")  					
					.then()
					.extract().response();	
			Thread.sleep(5000);		
			System.out.println("Clonned bot Setup Status ::" +responsecloningSmapleBottnt.getStatusCode());
			Assert.assertEquals(String.valueOf(responsecloningSmapleBottnt.getStatusCode()),"200");
		}catch(Exception e)
		{
			Assert.fail();
			e.printStackTrace();
		}
	}

	@Test(priority = 20, enabled = true)
	public static void createbuilderAppnforUBinTenantOnboarding() throws InterruptedException
	{
		try {
			System.out.println("-20----------------------Create Non-Amin Builder app  for UB---------------------------"); 
			Response responsbuilderapptnt = RestAssured.
					given()
					.headers(TenantOnboarding.HeadersWithAPIKey())
					.body(TenantOnboarding.createbuilderAppnonAdminpayLoad(collectappnbotdetails.get("clonnedBot_StreamID"),collectappnbotdetails.get("accountId"),collectappnbotdetails.get("userId")))
					.when()
					.post(url+internalClientappResource)					
					.then().log().all()
					.extract().response();	
			Thread.sleep(5000);				
			collectappnbotdetails.put("UB_BuilderApp_Name",responsbuilderapptnt.jsonPath().get("name").toString());
			collectappnbotdetails.put("UB_BuilderApp_sdkClientId", responsbuilderapptnt.jsonPath().get("cId").toString());
			collectappnbotdetails.put("UB_BuilderApp_cS", responsbuilderapptnt.jsonPath().get("cS").toString());
			collectappnbotdetails.put("UB_BuilderApp_UserId", responsbuilderapptnt.jsonPath().get("nId").toString());
			Assert.assertEquals(String.valueOf(responsbuilderapptnt.getStatusCode()),"200");
		}catch(Exception e)
		{
			Assert.fail();
			e.printStackTrace();
		}
	}

	@Test(priority = 21, enabled = true)
	public static void enableRTMforUBbuilderAPPinTenantOnboarding() throws InterruptedException
	{
		try {
			System.out.println("-21--------------------- enableRTM for UB bot builder app---------------------------"); 
			Response responseenableRTMforUBbuilderAPPtnt = RestAssured.
					given()
					.headers(TenantOnboarding.HeadersWithJWTToken(collectappnbotdetails.get("jwt")))
					.body(TenantOnboarding.enableRTMpayLoad(collectappnbotdetails.get("clonnedBot_StreamID"),collectappnbotdetails.get("UB_BuilderApp_Name"),collectappnbotdetails.get("UB_BuilderApp_sdkClientId"))) 
					.when()
					.post(url+publicEnableTRMChannelsResource)					
					.then().log().all()
					.extract().response();	
			Thread.sleep(5000);
			System.out.println(" Status code Enable RTM for UB bot"+responseenableRTMforUBbuilderAPPtnt.asString());
			Assert.assertEquals(String.valueOf(responseenableRTMforUBbuilderAPPtnt.getStatusCode()),"200");
		}catch(Exception e)
		{
			Assert.fail();
			e.printStackTrace();
		}
	}


	@Test(priority = 22, enabled = true)
	public static void genereateJWTtokenUB_BuilderAppinTenantOnboarding()
	{
		try {
			System.out.println("-----22---genereateJWTtokenUB_BuilderApp------------------"); 
			Response responsejwtTokenbuilderapptnt = RestAssured.
					given()
					.headers(TenantOnboarding.HeadersWithAPIKey())
					.body(TenantOnboarding.genereateJWTtokenPayLoad(collectappnbotdetails.get("UB_BuilderApp_sdkClientId"),collectappnbotdetails.get("UB_BuilderApp_cS"),collectappnbotdetails.get("UB_BuilderApp_UserId")))
					.when()
					.post(urljwtTokenGenerater)					
					.then()
					.extract().response();
			collectappnbotdetails.put("UB_BuilderAPP_jwt",responsejwtTokenbuilderapptnt.jsonPath().get("jwt").toString());
			Assert.assertEquals(String.valueOf(responsejwtTokenbuilderapptnt.getStatusCode()),"200");		
		}catch(Exception e)
		{
			Assert.fail();
			e.printStackTrace();
		}
	}

	
	// * Setting up KORA BOT
	 
	@Test(priority = 23, enabled = true)
	public static void uploadFileinTenantOnboarding() throws InterruptedException
	{
		try {
			System.out.println("--23----------------uploadFile_botDef---------------------------");   
			File botdeffile = new File("Korabot/botDefinition.json");
			File configfile = new File("Korabot/config.json");
			File iconfile = new File("Korabot/icon.png");
			//		 Bot name abiold
			//		File botdeffile = new File("ABIMYIT/botDefinition.json");
			//		File configfile = new File("ABIMYIT/config.json");
			//		File iconfile = new File("ABIMYIT/icon.png");

			Response responseuploadFiletnt = RestAssured.
					given()
					.headers(TenantOnboarding.HeadersWithJWTTokenforUpload(collectappnbotdetails.get("jwt")))
					.multiPart(botdeffile).multiPart("fileContext", "bulkImport")
					.when()
					.post(url+publicuploadFile)					
					.then()
					.extract().response();				
			Thread.sleep(10000);

			collectappnbotdetails.put("BotDef_fileId",responseuploadFiletnt.jsonPath().get("fileId").toString()); 		

			System.out.println("------------------uploadFile_config---------------------------");
			Response responsejwtTokenbotconfgtnt = RestAssured.
					given()
					.headers(TenantOnboarding.HeadersWithJWTTokenforUpload(collectappnbotdetails.get("jwt")))
					.multiPart(configfile).multiPart("fileContext", "bulkImport")
					.when()
					.post(url+publicuploadFile)					
					.then()
					.extract().response();				
			Thread.sleep(5000);														
			collectappnbotdetails.put("BotConfig_fileId",responsejwtTokenbotconfgtnt.jsonPath().get("fileId").toString()); 

			System.out.println("------------------uploadFile_icon---------------------------");
			Response responsejwtTokenboticontnt = RestAssured.
					given()
					.headers(TenantOnboarding.HeadersWithJWTTokenforUpload(collectappnbotdetails.get("jwt")))
					.multiPart(iconfile).multiPart("fileContext", "bulkImport")
					.when()
					.post(url+publicuploadFile)					
					.then()
					.extract().response();				
			Thread.sleep(5000);		
			JsonPath jsonPathEvaluatoradminboticon= responsejwtTokenboticontnt.jsonPath();											
			collectappnbotdetails.put("BotIcon_fileId",jsonPathEvaluatoradminboticon.get("fileId").toString());
			Assert.assertEquals(String.valueOf(responsejwtTokenboticontnt.getStatusCode()),"200");
		}catch(Exception e)
		{
			Assert.fail();
			e.printStackTrace();
		}
	}

	@Test(priority = 24, enabled = true)
	public static void importBotinTenantOnboarding() throws InterruptedException
	{
		try {
			System.out.println("-24--------------------------ImportBot------------------------------------"); 
			Response responsejwtTokentenant = RestAssured.
					given()
					.headers(TenantOnboarding.HeadersWithJWTToken(collectappnbotdetails.get("jwt")))
					.body(TenantOnboarding.importBotPayLoad(collectappnbotdetails.get("BotDef_fileId"),collectappnbotdetails.get("BotConfig_fileId"),collectappnbotdetails.get("BotIcon_fileId")))
					.when()
					.post(url+publicimportBOTresource)					
					.then().log().all()
					.extract().response();											
			collectappnbotdetails.put("streamRefId",responsejwtTokentenant.jsonPath().get("streamRefId").toString()); 
			collectappnbotdetails.put("bir_id",responsejwtTokentenant.jsonPath().get("_id").toString());
			Assert.assertEquals(String.valueOf(responsejwtTokentenant.getStatusCode()),"200");
		}catch(Exception e)
		{
			Assert.fail();
			e.printStackTrace();
		}

	}

	@Test(priority = 25, enabled = true)
	public static void importBotStatusinTenantOnboarding() throws InterruptedException
	{
		try {
			System.out.println("-25----------------------ImportBot_Status---------------------------"); 
			Response responseimportBotStatustnt = RestAssured.
					given()
					.headers(TenantOnboarding.HeadersWithJWTToken(collectappnbotdetails.get("jwt")))				
					.when()
					.get(url+publicimportBOTstatus+collectappnbotdetails.get("bir_id"))				
					.then()
					.extract().response();

			waitincreamentalLoop=1;
			doloop: do {
				waitincreamentalLoop++;
				Thread.sleep(10000);
				if(responseimportBotStatustnt.jsonPath().get("status").toString().equalsIgnoreCase("success"));
				{
					break doloop;
				}
			}
			while (waitincreamentalLoop <= 5 || (!responseimportBotStatustnt.jsonPath().get("status").toString().equalsIgnoreCase("success"))) ;
			collectappnbotdetails.put("importedBot_status",responseimportBotStatustnt.jsonPath().get("status").toString());
			collectappnbotdetails.put("importedBot_streamId",responseimportBotStatustnt.jsonPath().get("streamId").toString());
			Assert.assertEquals(String.valueOf(responseimportBotStatustnt.getStatusCode()),"200");
		}catch(Exception e)
		{
			Assert.fail();
			e.printStackTrace();
		}
	}

	@Test(priority = 26, enabled = true)
	public static void createbuilderAppNONAdmininTenantOnboarding() throws InterruptedException
	{
		try {
			System.out.println("-26----------------------Create Non-Amin Builder app---------------------------"); 
			Response responsbuilderappnonadmintnt = RestAssured.
					given()
					.headers(TenantOnboarding.HeadersWithAPIKey())
					.body(TenantOnboarding.createbuilderAppnonAdminpayLoad(collectappnbotdetails.get("importedBot_streamId"),collectappnbotdetails.get("accountId"),collectappnbotdetails.get("userId")))
					.when()
					.post(url+internalClientappResource)					
					.then().log().all()
					.extract().response();	
			Thread.sleep(5000);
			collectappnbotdetails.put("BuilderApp_Name",responsbuilderappnonadmintnt.jsonPath().get("name").toString());
			collectappnbotdetails.put("BuilderApp_sdkClientId", responsbuilderappnonadmintnt.jsonPath().get("cId").toString());
			collectappnbotdetails.put("BuilderApp_cS", responsbuilderappnonadmintnt.jsonPath().get("cS").toString());
			collectappnbotdetails.put("BuilderApp_UserId", responsbuilderappnonadmintnt.jsonPath().get("nId").toString());
			Assert.assertEquals(String.valueOf(responsbuilderappnonadmintnt.getStatusCode()),"200");
		}catch(Exception e)
		{
			Assert.fail();
			e.printStackTrace();
		}
	}

	@Test(priority = 27, enabled = true)
	public static void enableRTMinTenantOnboarding() throws InterruptedException
	{
		try {
			System.out.println("-27---------------------- enableRTM ---------------------------"); 
			Response responseadmin = RestAssured.
					given()
					.headers(TenantOnboarding.HeadersWithJWTToken(collectappnbotdetails.get("jwt")))
					.body(TenantOnboarding.enableRTMpayLoad(collectappnbotdetails.get("importedBot_streamId"),collectappnbotdetails.get("BuilderApp_Name"),collectappnbotdetails.get("BuilderApp_sdkClientId"))) 
					.when()
					.post(url+publicEnableTRMChannelsResource)					
					.then().log().all()
					.extract().response();	
			Thread.sleep(5000);
			Assert.assertEquals(String.valueOf(responseadmin.getStatusCode()),"200");
		}
		catch(Exception e)
		{
			Assert.fail();
			e.printStackTrace();
		}
	}

	@Test(priority = 28, enabled = true)
	public static void genereateJWTtokenbuilderappinTenantOnboarding()
	{
		try {
			System.out.println("---28-----genereateJWTtokenbuilderapp------------------"); 
			Response responsejwtTokentntsecond = RestAssured.
					given()
					.headers(TenantOnboarding.HeadersWithAPIKey())
					.body(TenantOnboarding.genereateJWTtokenPayLoad(collectappnbotdetails.get("BuilderApp_sdkClientId"),collectappnbotdetails.get("BuilderApp_cS"),collectappnbotdetails.get("BuilderApp_UserId")))
					.when()
					.post(urljwtTokenGenerater)					
					.then()
					.extract().response();
			collectappnbotdetails.put("builderApp_jwt",responsejwtTokentntsecond.jsonPath().get("jwt").toString());
			Assert.assertEquals(String.valueOf(responsejwtTokentntsecond.getStatusCode()),"200");
		}catch(Exception e)
		{
			Assert.fail();
			e.printStackTrace();
		}
	}

	@Test(priority = 29, enabled = true)
	public static void publishbotStandardBotinTenantOnboarding() throws InterruptedException
	{
		try {
			System.out.println("--29--------------------- Publish Bot standard bot---------------------------");
			Thread.sleep(5000);
			Response responsPublishBottenantonboarding = RestAssured.
					given()
					.headers(TenantOnboarding.Headersforpublishbot(collectappnbotdetails.get("jwt")))
					.body(TenantOnboarding.publishbotPayload()) 
					.when()
					.post(url+publicEnableSdk+collectappnbotdetails.get("importedBot_streamId")+"/publish")					
					.then()
					.extract().response();	
			Thread.sleep(5000);
			System.out.println("Publish bot Status  "+responsPublishBottenantonboarding.jsonPath().get("status"));
			Assert.assertEquals(String.valueOf(responsPublishBottenantonboarding.getStatusCode()),"200");
		}catch(Exception e)
		{
			Assert.fail();
			e.printStackTrace();
		}
	}

	@Test(priority = 30, enabled = true)
	public static void linkChildBotinTenantOnboarding() throws InterruptedException
	{
		try {
			System.out.println("---30--------------------Linking Child bot to UB---------------------------"); 
			Response responseadminlnkchildbottnt = RestAssured.
					given()
					.headers(TenantOnboarding.HeadersWithJWTToken(collectappnbotdetails.get("UB_BuilderAPP_jwt")))
					.body(TenantOnboarding.linkChildBotpayLoad(collectappnbotdetails.get("importedBot_streamId"),"KoraBot",collectappnbotdetails.get("builderApp_jwt")))
					.when().log().all()
					.post(url+"/api/public/bot/"+collectappnbotdetails.get("clonnedBot_StreamID")+"/universalbot/link")			 		
					.then().log().all()
					.extract().response();	
			Thread.sleep(5000);
			Assert.assertEquals(String.valueOf(responseadminlnkchildbottnt.getStatusCode()),"200");			
		}catch(Exception e)
		{
			Assert.fail();
			e.printStackTrace();
		}

	}

}
