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
			System.out.println("-49-----------------createUser SearchSkill---------------------------");
			String ssTimeinHHMMSS=Payloads.fntoreturntimeinHHMMSS();
			Response responsesskill = RestAssured
					.given().headers(SearchSkill.HeadersWithAPIKey()).log().all() 
					.body(SearchSkill.createUserPayLoad(ssTimeinHHMMSS))
					.when()
					.post(url+internalAccountResource)
					.then().log().all()
					.extract().response();	
			Thread.sleep(5000);

			collectappnbotdetails = new LinkedHashMap<String, String>();			
			collectappnbotdetails.put("emailId", responsesskill.jsonPath().get("emailId").toString());			
			collectappnbotdetails.put("accountId", responsesskill.jsonPath().get("accountId").toString());
			collectappnbotdetails.put("userId", responsesskill.jsonPath().get("userId").toString());
			Assert.assertEquals(String.valueOf(responsesskill.getStatusCode()),"200");
		}catch(Exception e)
		{
			e.printStackTrace();
		}		
	}

	@Test(priority = 50, enabled = true)
	public static void createAdminAppinSearchSkill() {
		try {						
			System.out.println("---50--------------createAdminApp---------------------------");
			Response responsecreateAdminAppinss = RestAssured.
					given()
					.headers(SearchSkill.HeadersWithAPIKey()).log().all()
					.body(SearchSkill.createAdminAPPPayLoad(collectappnbotdetails.get("accountId"),collectappnbotdetails.get("userId")))
					.when()
					.post(url+internalClientappResource)					
					.then().log().all()
					.extract().response();	
			Thread.sleep(5000);										

			collectappnbotdetails.put("Name",responsecreateAdminAppinss.jsonPath().get("name").toString());
			collectappnbotdetails.put("cId", responsecreateAdminAppinss.jsonPath().get("cId").toString());
			collectappnbotdetails.put("cS", responsecreateAdminAppinss.jsonPath().get("cS").toString());
			collectappnbotdetails.put("nId", responsecreateAdminAppinss.jsonPath().get("nId").toString());	
			collectappnbotdetails.put("accountId", responsecreateAdminAppinss.jsonPath().get("accountId").toString());
			collectappnbotdetails.put("userId", responsecreateAdminAppinss.jsonPath().get("nId").toString());
			TimeinHHMMSS=null;
			Assert.assertEquals(String.valueOf(responsecreateAdminAppinss.getStatusCode()),"200");
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
			System.out.println("-51-------genereateJWTtoken------------------"); 
			Response responsejwtTokeninss = RestAssured.
					given()
					.headers(SearchSkill.HeadersWithAPIKey()).log().all()
					.body(SearchSkill.genereateJWTtokenPayLoad(collectappnbotdetails.get("cId"),collectappnbotdetails.get("cS"),collectappnbotdetails.get("nId")))
					.when()
					.post(urljwtTokenGenerater)					
					.then()
					.extract().response();			

			collectappnbotdetails.put("jwt",responsejwtTokeninss.jsonPath().get("jwt").toString());
			Assert.assertEquals(String.valueOf(responsejwtTokeninss.getStatusCode()),"200");
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
			System.out.println("-52--------cloning SearchSkill BOT------------------");
			if (url.contains("koradev-bots.kora.ai")) {				
				cloningbot=	devServiceNowBOT;
			} else if (url.contains("qa1-bots.kore.ai")) {			
				cloningbot=	qa1ServiceNowBOT;			
			} else {
				System.out.println(" Given URL "+ url+" is neither koradev-bots.kora.ai nor qa1-bots.kora.ai");
			}
			Response responsecloningSmapleBotinss = RestAssured.
					given()
					.headers(SearchSkill.headersforcloneBotpayLoad(collectappnbotdetails.get("jwt"),collectappnbotdetails.get("userId"), collectappnbotdetails.get("accountId")))				
					.when()  
					.get(url+"/api/public/samplebots/"+cloningbot+"/add")  //hrere  ubVersion=1 wont be there 					
					.then()
					.extract().response();
			Thread.sleep(10000);

			collectappnbotdetails.put("clonnedBot_StreamID",responsecloningSmapleBotinss.jsonPath().get("_id").toString());									
			collectappnbotdetails.put("clonnedBotName",responsecloningSmapleBotinss.jsonPath().get("name").toString());
			Assert.assertEquals(String.valueOf(responsecloningSmapleBotinss.getStatusCode()),"200");
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
			System.out.println("-53-------Setting SearchSkill BOT Setup------------------"); 
			Response responsecloningSmapleBotss = RestAssured.
					given()
					.headers(SearchSkill.HeaderswithJWTnAccountID(collectappnbotdetails.get("jwt"), collectappnbotdetails.get("accountId"))).log().all()				
					.body(SearchSkill.clonedBot_SetuppayLoad(collectappnbotdetails.get("clonnedBotName")))
					.when()  
					.put(url+"/api/public/bot/"+ collectappnbotdetails.get("clonnedBot_StreamID")+"/setup")  					
					.then().log().all()
					.extract().response();
			Thread.sleep(5000);

			System.out.println("Clonned bot Setup Status ::" +responsecloningSmapleBotss.getStatusCode());
			Assert.assertEquals(String.valueOf(responsecloningSmapleBotss.getStatusCode()),"200");
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
			Response responseconfiguringENVvarss = null;
//			if(collectappnbotdetails.get("clonnedBot_StreamID").equalsIgnoreCase("success") || collectappnbotdetails.get("clonnedBot_StreamID").equalsIgnoreCase("200"))
//			{
				System.out.println("----54------------------configure environment variable--------------------------"); 
				responseconfiguringENVvarss = RestAssured.
						given()
						.headers(SearchSkill.HeaderswithJWTnAccountID(collectappnbotdetails.get("jwt"),collectappnbotdetails.get("accountId")))
						.body(SearchSkill.configuringENVvarforSearviceNowpayLoad()).log().all()
						.when()
						.put(url+publicEnableSdk+collectappnbotdetails.get("clonnedBot_StreamID")+"/setup")					
						.then().log().all()
						.extract().response();		

				System.out.println("configure environment variable Status" +"::"+ responseconfiguringENVvarss.getStatusCode()+responseconfiguringENVvarss.getStatusLine());
				collectappnbotdetails.put("configure environment variable Status",responseconfiguringENVvarss.getStatusCode()+responseconfiguringENVvarss.getStatusLine());
				if(collectappnbotdetails.get("configure environment variable Status").contains("401 Unauthorized"))
				{
					SearchSkill.genereateJWTtokeninSearchSkill();
					System.out.println("-----------------------Generating JWT token Again after expiring--------------------------");
					SearchSkill.configuringENVvarinSearchSkill();
				}
//			}else
//			{
//				System.out.println("----------------- ImportBot_Status  Failed  ------------------");
//			}
			Assert.assertEquals(String.valueOf(responseconfiguringENVvarss.getStatusCode()),"200");
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
			System.out.println("-55----------------------Create Non-Amin Builder app----------importedBot_streamId-----------------"); 
			Response responsbuilderappinss = RestAssured.
					given()
					.headers(SearchSkill.HeadersWithAPIKey()).log().all()
					.body(SearchSkill.createbuilderAppnonAdminpayLoad(collectappnbotdetails.get("clonnedBot_StreamID"),collectappnbotdetails.get("accountId"),collectappnbotdetails.get("userId")))
					.when()
					.post(url+internalClientappResource)					
					.then().log().all()
					.extract().response();	
			Thread.sleep(5000);				
			collectappnbotdetails.put("BuilderApp_Name",responsbuilderappinss.jsonPath().get("name").toString());
			collectappnbotdetails.put("BuilderApp_sdkClientId", responsbuilderappinss.jsonPath().get("cId").toString());
			collectappnbotdetails.put("BuilderApp_cS", responsbuilderappinss.jsonPath().get("cS").toString());
			collectappnbotdetails.put("BuilderApp_UserId", responsbuilderappinss.jsonPath().get("nId").toString());
			Assert.assertEquals(String.valueOf(responsbuilderappinss.getStatusCode()),"200");
		}catch(Exception e)
		{
			Assert.fail();
			e.printStackTrace();
		}

	}

	@Test(priority = 56, enabled = false)
	public static void enableWebHookinSearchSkill() throws InterruptedException 
	{
		try {
			System.out.println("-56---------------------- enable WEBHOOK ------------importedBot_streamId---------------"); 
			Response responseadminwebhookss = RestAssured.
					given()
					.headers(SearchSkill.HeadersWithJWTToken(collectappnbotdetails.get("jwt")))
					.body(SearchSkill.enableWebHookpayLoad(collectappnbotdetails.get("clonnedBot_StreamID"),collectappnbotdetails.get("BuilderApp_Name"),collectappnbotdetails.get("BuilderApp_sdkClientId"))) 
					.when()
					.post(url+publicEnableTRMChannelsResource)					
					.then().log().all()
					.extract().response();	
			Thread.sleep(5000);		
			System.out.println(" Status code Enable WebHook "+responseadminwebhookss.jsonPath().get("status"));
			Assert.assertEquals(String.valueOf(responseadminwebhookss.getStatusCode()),"200");
		}catch(Exception e)
		{
			Assert.fail();
			e.printStackTrace();
		}
	}

	@Test(priority = 57, enabled = false)
	public static void publishbotinSearchSkill() throws InterruptedException
	{
		try {
			System.out.println("--57-------------------- Publish Bot ---------------------------");
			Response responsPublishBotinss = RestAssured.
					given()
					.headers(SearchSkill.Headersforpublishbot(collectappnbotdetails.get("jwt")))
					.body(SearchSkill.publishbotPayload()).log().all() 
					.when().log().all()
					.post(url+publicEnableSdk+collectappnbotdetails.get("clonnedBot_StreamID")+"/publish")					
					.then()
					.extract().response();	
			Thread.sleep(10000);		
			System.out.println("Publish bot Status code "+responsPublishBotinss.jsonPath().get("status"));
			Assert.assertEquals(String.valueOf(responsPublishBotinss.getStatusCode()),"200");
		}catch(Exception e)
		{
			Assert.fail();
			e.printStackTrace();
		}
	}


	@Test(priority = 58, enabled = false)
	public static void getRoleinSearchSkill() throws InterruptedException
	{
		try {
			System.out.println("---58--------------------------Get Role------------------------------------"); 
			Response responsegetRoleinsskill = RestAssured.
					given()
					.headers(SearchSkill.HeadersWithJWTToken(collectappnbotdetails.get("jwt")))				
					.when()
					.get(url+publicGetRolesResource)					
					.then()
					.extract().response();		

			JsonPath jp = responsegetRoleinsskill.jsonPath();	
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
			Assert.assertEquals(String.valueOf(responsegetRoleinsskill.getStatusCode()),"200");
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
	@Test(priority = 59, enabled = false)
	public static void AddingDeveloperasOwnerinSearchSkill() throws InterruptedException
	{
		try {
			System.out.println("-----59----------------------Adding Developer as Owner MAP to Owner------------------------------------"); 
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


	@Test(priority = 60, enabled = false)
	public static void genereateJWTtokenforNonAmdinAppinSearchSkill()
	{
		try {
			System.out.println("------60---genereateJWTtoken for non Admin app------------------"); 
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

	@Test(priority = 61, enabled = false)
	public static void triggerWebHookinSearchSkill() throws InterruptedException
	{
		try {
			System.out.println("-61---------------------- trigger WebHook toEngage with bot ---------------------------"); 
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
