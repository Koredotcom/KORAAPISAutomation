package platform.api;

import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

import org.testng.Assert;
import org.testng.ITestResult;

import java.util.LinkedHashMap;

public class SearchSkill extends Payloads{

	@AfterMethod
	public void afterMethodSearchSkill(ITestResult result) {
		System.out.println("method name:" + result.getMethod().getMethodName());
		if(result.getStatus() == ITestResult.SUCCESS)
		{
			results_SS.put(result.getMethod().getMethodName(), "Pass");
		}
		else if(result.getStatus() == ITestResult.FAILURE)
		{
			System.out.println("Failed ***********");
			results_SS.put(result.getMethod().getMethodName(), "Fail");
		}	 
		if (results_SS.get(result.getMethod().getMethodName()).equalsIgnoreCase("pass")) {
			SS_passcount++;
		} else if (results_SS.get(result.getMethod().getMethodName()).equalsIgnoreCase("fail")) {
			SS_failcount++;		
		}		
	}
	@Test(priority = 49, enabled = true)
	public static void createUserinSearchSkill() {
		try {
			System.out.println("-01-----------------createUser SearchSkill---------------------------");
			String ssTimeinHHMMSS=Payloads.fntoreturntimeinHHMMSS();
			Response response = RestAssured
					.given().headers(SearchSkill.HeadersWithAPIKey()).log().all() 
					.body(SearchSkill.createUserPayLoad(ssTimeinHHMMSS))
					.when()
					.post(url+internalAccountResource)
					.then().log().all()
					.extract().response();	
			Thread.sleep(5000);
			
			collectappnbotdetails = new LinkedHashMap<String, String>();			
			collectappnbotdetails.put("emailId", response.jsonPath().get("emailId").toString());			
			collectappnbotdetails.put("accountId", response.jsonPath().get("accountId").toString());
			collectappnbotdetails.put("userId", response.jsonPath().get("userId").toString());
			Assert.assertEquals(String.valueOf(response.getStatusCode()),"200");
		}catch(Exception e)
		{
			e.printStackTrace();
		}		
	}

	@Test(priority = 50, enabled = true)
	public static void createAdminAppinSearchSkill() {
		try {						
			System.out.println("---02---------------createAdminApp---------------------------");
			Response responsecreateAdminApp = RestAssured.
					given()
					.headers(SearchSkill.HeadersWithAPIKey()).log().all()
					.body(SearchSkill.createAdminAPPPayLoad(collectappnbotdetails.get("accountId"),collectappnbotdetails.get("userId")))
					.when()
					.post(url+internalClientappResource)					
					.then().log().all()
					.extract().response();	
			Thread.sleep(5000);										
			
			collectappnbotdetails.put("Name",responsecreateAdminApp.jsonPath().get("name").toString());
			collectappnbotdetails.put("cId", responsecreateAdminApp.jsonPath().get("cId").toString());
			collectappnbotdetails.put("cS", responsecreateAdminApp.jsonPath().get("cS").toString());
			collectappnbotdetails.put("nId", responsecreateAdminApp.jsonPath().get("nId").toString());	
			collectappnbotdetails.put("accountId", responsecreateAdminApp.jsonPath().get("accountId").toString());
			collectappnbotdetails.put("userId", responsecreateAdminApp.jsonPath().get("nId").toString());
			TimeinHHMMSS=null;
			Assert.assertEquals(String.valueOf(responsecreateAdminApp.getStatusCode()),"200");
		}catch(Exception e)
		{
			TimeinHHMMSS=null;
			e.printStackTrace();
		}		
	}

	@Test(priority = 51, enabled = true)
	public static void genereateJWTtokeninSearchSkill()
	{
		try {
		System.out.println("-03--------genereateJWTtoken------------------"); 
		Response responsejwtToken = RestAssured.
				given()
				.headers(SearchSkill.HeadersWithAPIKey()).log().all()
				.body(SearchSkill.genereateJWTtokenPayLoad(collectappnbotdetails.get("cId"),collectappnbotdetails.get("cS"),collectappnbotdetails.get("nId")))
				.when()
				.post(urljwtTokenGenerater)					
				.then()
				.extract().response();			
		
		collectappnbotdetails.put("jwt",responsejwtToken.jsonPath().get("jwt").toString());
		Assert.assertEquals(String.valueOf(responsejwtToken.getStatusCode()),"200");
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
	@Test(priority = 52, enabled = true)
	public static void cloningSmapleBotinSearchSkill() throws InterruptedException
	{	
		try {
		System.out.println("-04--------cloning SearchSkill BOT------------------");
		if (url.contains("koradev-bots.kora.ai")) {				
			cloningbot=	devServiceNowBOT;
		} else if (url.contains("qa1-bots.kore.ai")) {			
			cloningbot=	qa1ServiceNowBOT;			
		} else {
			System.out.println(" Given URL "+ url+" is neither koradev-bots.kora.ai nor qa1-bots.kora.ai");
		}
		Response responsecloningSmapleBot = RestAssured.
				given()
				.headers(SearchSkill.headersforcloneBotpayLoad(collectappnbotdetails.get("jwt"),collectappnbotdetails.get("userId"), collectappnbotdetails.get("accountId")))				
				.when()  
				.get(url+"/api/public/samplebots/"+cloningbot+"/add")  //hrere  ubVersion=1 wont be there 					
				.then()
				.extract().response();
		Thread.sleep(10000);
		
		collectappnbotdetails.put("clonnedBot_StreamID",responsecloningSmapleBot.jsonPath().get("_id").toString());									
		collectappnbotdetails.put("clonnedBotName",responsecloningSmapleBot.jsonPath().get("name").toString());
		Assert.assertEquals(String.valueOf(responsecloningSmapleBot.getStatusCode()),"200");
		}catch(Exception e)
		{
			Assert.fail();
			e.printStackTrace();
		}
		
	}

	@Test(priority = 53, enabled = true)
	public static void clonedBotSetupinSearchSkill() throws InterruptedException
	{
		try {
		System.out.println("-05--------Setting SearchSkill BOT Setup------------------"); 
		Response responsecloningSmapleBot = RestAssured.
				given()
				.headers(SearchSkill.HeaderswithJWTnAccountID(collectappnbotdetails.get("jwt"), collectappnbotdetails.get("accountId")))				
				.body(SearchSkill.clonedBot_SetuppayLoad(collectappnbotdetails.get("clonnedBotName")))
				.when()  
				.put(url+"/api/public/bot/"+ collectappnbotdetails.get("clonnedBot_StreamID")+"/setup")  					
				.then()
				.extract().response();
		Thread.sleep(5000);
		
		System.out.println("Clonned bot Setup Status ::" +responsecloningSmapleBot.getStatusCode());
		Assert.assertEquals(String.valueOf(responsecloningSmapleBot.getStatusCode()),"200");
		}catch(Exception e)
		{
			Assert.fail();
			e.printStackTrace();
		}
	}


	@Test(priority = 54, enabled = true)
	public static void configuringENVvarinSearchSkill() throws InterruptedException
	{
		try {
	
			Response responseconfiguringENVvar = null;
		if(collectappnbotdetails.get("clonnedBot_StreamID").equalsIgnoreCase("success"))
		{
			System.out.println("----06-------------------configure environment variable--------------------------"); 
			 responseconfiguringENVvar = RestAssured.
					given()
					.headers(SearchSkill.HeaderswithJWTnAccountID(collectappnbotdetails.get("jwt"),collectappnbotdetails.get("accountId")))
					.body(SearchSkill.configuringENVvarforSearviceNowpayLoad())
					.when()
					.put(url+publicEnableSdk+collectappnbotdetails.get("clonnedBot_StreamID")+"/setup")					
					.then()
					.extract().response();		
			
			System.out.println("configure environment variable Status" +"::"+ responseconfiguringENVvar.getStatusCode()+responseconfiguringENVvar.getStatusLine());
			collectappnbotdetails.put("configure environment variable Status",responseconfiguringENVvar.getStatusCode()+responseconfiguringENVvar.getStatusLine());
			if(collectappnbotdetails.get("configure environment variable Status").contains("401 Unauthorized"))
			{
				SearchSkill.genereateJWTtokeninSearchSkill();
				System.out.println("-----------------------Generating JWT token Again after expiring--------------------------");
				SearchSkill.configuringENVvarinSearchSkill();
			}
		}else
		{
			System.out.println("----------------- ImportBot_Status  Failed  ------------------");
		}
		Assert.assertEquals(String.valueOf(responseconfiguringENVvar.getStatusCode()),"200");
		}catch(Exception e)
		{
			Assert.fail();
			e.printStackTrace();
		}
	}
	
	@Test(priority = 55, enabled = true)
	public static void createbuilderAppnonAdmininSearchSkill() throws InterruptedException
	{
		try {
		System.out.println("-07----------------------Create Non-Amin Builder app----------importedBot_streamId-----------------"); 
		Response responsbuilderapp = RestAssured.
				given()
				.headers(SearchSkill.HeadersWithAPIKey()).log().all()
				.body(SearchSkill.createbuilderAppnonAdminpayLoad(collectappnbotdetails.get("clonnedBot_StreamID"),collectappnbotdetails.get("accountId"),collectappnbotdetails.get("userId")))
				.when()
				.post(url+internalClientappResource)					
				.then().log().all()
				.extract().response();	
		Thread.sleep(5000);				
		collectappnbotdetails.put("BuilderApp_Name",responsbuilderapp.jsonPath().get("name").toString());
		collectappnbotdetails.put("BuilderApp_sdkClientId", responsbuilderapp.jsonPath().get("cId").toString());
		collectappnbotdetails.put("BuilderApp_cS", responsbuilderapp.jsonPath().get("cS").toString());
		collectappnbotdetails.put("BuilderApp_UserId", responsbuilderapp.jsonPath().get("nId").toString());
		Assert.assertEquals(String.valueOf(responsbuilderapp.getStatusCode()),"200");
		}catch(Exception e)
		{
			Assert.fail();
			e.printStackTrace();
		}
						
	}

	@Test(priority = 56, enabled = true)
	public static void enableWebHookinSearchSkill() throws InterruptedException 
	{
		try {
		System.out.println("-08---------------------- enable WEBHOOK ------------importedBot_streamId---------------"); 
		Response responseadmin = RestAssured.
				given()
				.headers(SearchSkill.HeadersWithJWTToken(collectappnbotdetails.get("jwt")))
				.body(SearchSkill.enableWebHookpayLoad(collectappnbotdetails.get("clonnedBot_StreamID"),collectappnbotdetails.get("BuilderApp_Name"),collectappnbotdetails.get("BuilderApp_sdkClientId"))) 
				.when()
				.post(url+publicEnableTRMChannelsResource)					
				.then().log().all()
				.extract().response();	
		Thread.sleep(5000);		
		System.out.println(" Status code Enable WebHook "+responseadmin.jsonPath().get("status"));
		Assert.assertEquals(String.valueOf(responseadmin.getStatusCode()),"200");
		}catch(Exception e)
		{
			Assert.fail();
			e.printStackTrace();
		}
	}

	@Test(priority = 57, enabled = true)
	public static void publishbotinSearchSkill() throws InterruptedException
	{
		try {
		System.out.println("--09-------------------- Publish Bot ---------------------------");
		Response responsPublishBot = RestAssured.
				given()
				.headers(SearchSkill.Headersforpublishbot(collectappnbotdetails.get("jwt")))
				.body(SearchSkill.publishbotPayload()).log().all() 
				.when().log().all()
				.post(url+publicEnableSdk+collectappnbotdetails.get("clonnedBot_StreamID")+"/publish")					
				.then()
				.extract().response();	
		Thread.sleep(10000);		
		System.out.println("Publish bot Status code "+responsPublishBot.jsonPath().get("status"));
		Assert.assertEquals(String.valueOf(responsPublishBot.getStatusCode()),"200");
		}catch(Exception e)
		{
			Assert.fail();
			e.printStackTrace();
		}
	}
 

	@Test(priority = 58, enabled = true)
	public static void getRoleinSearchSkill() throws InterruptedException
	{
		try {
		System.out.println("---10--------------------------Get Role------------------------------------"); 
		Response responsegetRole = RestAssured.
				given()
				.headers(SearchSkill.HeadersWithJWTToken(collectappnbotdetails.get("jwt")))				
				.when()
				.get(url+publicGetRolesResource)					
				.then()
				.extract().response();		
		
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
		Assert.assertEquals(String.valueOf(responsegetRole.getStatusCode()),"200");
		}catch(Exception e)
		{
			Assert.fail();
			e.printStackTrace();
		}
	}

	/*
	 * As testing practice here Giving developer email address same everytime
	 * ?? Here we have to give linked botID
	 */
	@Test(priority = 59, enabled = true)
	public static void AddingDeveloperasOwnerinSearchSkill() throws InterruptedException
	{
		try {
		System.out.println("-----11----------------------Adding Developer as Owner MAP to Owner------------------------------------"); 
		Response responseAddingDeveloperasOwner = RestAssured.
				given()
				.headers(SearchSkill.HeadersWithJWTToken(collectappnbotdetails.get("jwt")))
				.body(SearchSkill.addDeveloperpayLoad(developeremailaddress,collectappnbotdetails.get("Dev_role_id"), collectappnbotdetails.get("clonnedBot_StreamID")))  
				.when()
				.post(url+publicadminasUBDevResource)					
				.then().log().all()
				.extract().response();
		Thread.sleep(5000);
		
		System.out.println("Adding Developer as Owner Response ::"+responseAddingDeveloperasOwner.asString());
		String MaptoMomainrsponse=responseAddingDeveloperasOwner.jsonPath().get("msg");
		
		if(MaptoMomainrsponse.contains(developeremailaddress +"created"))
		{
			System.out.println(developeremailaddress +":: user created successfully as added as developer");
		}else {
			System.out.println(responseAddingDeveloperasOwner.jsonPath().toString());
		}
		Assert.assertEquals(String.valueOf(responseAddingDeveloperasOwner.getStatusCode()),"200");
		}catch(Exception e)
		{
			Assert.fail();
			e.printStackTrace();
		}
	}	 

	
	@Test(priority = 60, enabled = true)
	public static void genereateJWTtokenforNonAmdinAppinSearchSkill()
	{
		try {
		System.out.println("------12---genereateJWTtoken for non Admin app------------------"); 
		Response responsejwtToken = RestAssured.
				given()
				.headers(SearchSkill.HeadersWithAPIKey())
				.body(SearchSkill.genereateJWTtokenPayLoad(collectappnbotdetails.get("BuilderApp_sdkClientId"),collectappnbotdetails.get("BuilderApp_cS"),collectappnbotdetails.get("BuilderApp_UserId")))
				.when()
				.post(urljwtTokenGenerater)					
				.then()
				.extract().response();							
		collectappnbotdetails.put("builderApp_jwt",responsejwtToken.jsonPath().get("jwt").toString());
		Assert.assertEquals(String.valueOf(responsejwtToken.getStatusCode()),"200");
		}catch(Exception e)
		{
			Assert.fail();
			e.printStackTrace();
		}
	}
	
	@Test(priority = 61, enabled = true)
	public static void triggerWebHookinSearchSkill() throws InterruptedException
	{
		try {
		System.out.println("-13---------------------- trigger WebHook toEngage with bot ---------------------------"); 
		Response responsetriggerWebHook = RestAssured.
				given()
				.headers(SearchSkill.HeadersWithJWTToken(collectappnbotdetails.get("builderApp_jwt"))).log().all()
				.body(SearchSkill.TriggerWebHookChannelPayload(collectappnbotdetails.get("emailId"),"symphony")) 
				.when()
				.post(url+"/chatbot/hooks/"+collectappnbotdetails.get("clonnedBot_StreamID"))					
				.then().log().all()
				.extract().response();	
		Thread.sleep(5000);
		Assert.assertEquals(String.valueOf(responsetriggerWebHook.getStatusCode()),"200");	
		}catch(Exception e)
		{
			Assert.fail();
			e.printStackTrace();
		}
	}
		
}
