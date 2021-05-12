package platform.api;


import java.util.LinkedHashMap;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.Test;

import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

public class KnowledgeCollection extends Payloads{
		
	@AfterMethod
	public void afterMethodKnowledgeCollection(ITestResult result) {
		if(result.getStatus() == ITestResult.SUCCESS)
		{
			results_KC.put(result.getMethod().getMethodName(), "Pass");
		}
		else if(result.getStatus() == ITestResult.FAILURE)
		{
			System.out.println("Failed ***********");
			results_KC.put(result.getMethod().getMethodName(), "Fail");
		}	 
		if (results_KC.get(result.getMethod().getMethodName()).equalsIgnoreCase("pass")) {
			KC_passcount++;
		} else if (results_KC.get(result.getMethod().getMethodName()).equalsIgnoreCase("fail")) {
			KC_failcount++;		
		}		
	}
	@Test(priority = 31, enabled = true)
	public static void createUserinKnowledgeCollection() {
		try {
			System.out.println("-01-----------------createUser Knowledge Collection---------------------------");
			String kTimeinHHMMSS=Payloads.fntoreturntimeinHHMMSS();
			Response response = RestAssured
					.given().headers(KnowledgeCollection.HeadersWithAPIKey()) 
					.body(KnowledgeCollection.createUserPayLoad(kTimeinHHMMSS))
					.when()
					.post(urlKC+internalAccountResource)
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

	@Test(priority = 32, enabled = true)
	public static void createAdminAppinKnowledgeCollection() {
		try {						
			System.out.println("---02---------------createAdminApp---------------------------");
			Response responseadmin = RestAssured.
					given()
					.headers(KnowledgeCollection.HeadersWithAPIKey()).log().all()
					.body(KnowledgeCollection.createAdminAPPPayLoad(collectappnbotdetails.get("accountId"),collectappnbotdetails.get("userId")))
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
			TimeinHHMMSS=null;
			Assert.assertEquals(String.valueOf(responseadmin.getStatusCode()),"200");
		}catch(Exception e)
		{
			TimeinHHMMSS=null;
			e.printStackTrace();
		}		
	}

	@Test(priority = 33, enabled = true)
	public static void genereateJWTtokeninKnowledgeCollection()
	{
		try {
		System.out.println("-03--------genereateJWTtoken------------------"); 
		Response responsejwtToken = RestAssured.
				given()
				.headers(KnowledgeCollection.HeadersWithAPIKey()).log().all()
				.body(KnowledgeCollection.genereateJWTtokenPayLoad(collectappnbotdetails.get("cId"),collectappnbotdetails.get("cS"),collectappnbotdetails.get("nId")))
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
	@Test(priority = 34, enabled = true)
	public static void cloningSmapleBotinKnowledgeCollection() throws InterruptedException
	{	
		try {
		System.out.println("-04--------cloning KnowledgeCollection BOT------------------");
		if (url.contains("koradev-bots.kora.ai")) {				
			cloningbot=	devKnowledgeCollectionBOT;
		} else if (url.contains("qa1-bots.kore.ai")) {			
			cloningbot=	qa1KnowledgeCollectionBOT;			
		} else {
			System.out.println(" Given URL "+ url+" is neither koradev-bots.kora.ai nor qa1-bots.kora.ai");
		}
		Response responsecloningSmapleBot = RestAssured.
				given()
				.headers(KnowledgeCollection.headersforcloneBotpayLoad(collectappnbotdetails.get("jwt"),collectappnbotdetails.get("userId"), collectappnbotdetails.get("accountId")))				
				.when().log().all()  
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

	@Test(priority = 35, enabled = true)
	public static void clonedBotSetupinKnowledgeCollection() throws InterruptedException
	{
		try {
		System.out.println("-05--------cloned BOT Setup------------------"); 
		Response responsecloningSmapleBotsetup = RestAssured.
				given()
				.headers(KnowledgeCollection.HeaderswithJWTnAccountID(collectappnbotdetails.get("jwt"), collectappnbotdetails.get("accountId")))				
				.body(KnowledgeCollection.clonedBot_SetuppayLoad(collectappnbotdetails.get("clonnedBotName")))
				.when()  
				.put(url+"/api/public/bot/"+ collectappnbotdetails.get("clonnedBot_StreamID")+"/setup")  					
				.then()
				.extract().response();
		Thread.sleep(5000);		
		System.out.println("Clonned bot Setup Status ::" +responsecloningSmapleBotsetup.getStatusCode());
		Assert.assertEquals(String.valueOf(responsecloningSmapleBotsetup.getStatusCode()),"200");
	}catch(Exception e)
	{
		Assert.fail();
		e.printStackTrace();
	}
	}


	@Test(priority = 36, enabled = true)
	public static void createbuilderAppnonAdmininKnowledgeCollection() throws InterruptedException
	{
		try {
		System.out.println("-06----------------------Create Non-Amin Builder app----------importedBot_streamId-----------------"); 
		Response responsbuilderapp = RestAssured.
				given()
				.headers(KnowledgeCollection.HeadersWithAPIKey())
				.body(KnowledgeCollection.createbuilderAppnonAdminpayLoad(collectappnbotdetails.get("clonnedBot_StreamID"),collectappnbotdetails.get("accountId"),collectappnbotdetails.get("userId")))
				.when()
				.post(url+internalClientappResource)					
				.then()
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

	@Test(priority = 37, enabled = true)
	public static void enableRTMinKnowledgeCollection() throws InterruptedException
	{
		try {
		System.out.println("-07---------------------- enableRTM ------------importedBot_streamId---------------"); 
		Response responseadmin = RestAssured.
				given()
				.headers(KnowledgeCollection.HeadersWithJWTToken(collectappnbotdetails.get("jwt")))
				.body(KnowledgeCollection.enableRTMpayLoad(collectappnbotdetails.get("clonnedBot_StreamID"),collectappnbotdetails.get("BuilderApp_Name"),collectappnbotdetails.get("BuilderApp_sdkClientId"))) 
				.when()
				.post(url+publicEnableTRMChannelsResource)					
				.then().log().all()
				.extract().response();	
		Thread.sleep(5000);		
		System.out.println(" Status code Enable RTM "+responseadmin.jsonPath().get("status"));
		Assert.assertEquals(String.valueOf(responseadmin.getStatusCode()),"200");
	}catch(Exception e)
	{
		Assert.fail();
		e.printStackTrace();
	}
	}


	@Test(priority = 38, enabled = true)
	public static void getRoleinKnowledgeCollection() throws InterruptedException
	{
		try {
		System.out.println("--8--------------------------Get Role------------------------------------"); 
		Response responsegetRole = RestAssured.
				given()
				.headers(KnowledgeCollection.HeadersWithJWTToken(collectappnbotdetails.get("jwt")))				
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
	@Test(priority = 39, enabled = true)
	public static void AddingDeveloperasOwnerinKnowledgeCollection() throws InterruptedException
	{
		try {
		System.out.println("-----9----------------------Adding Developer as Owner MAP to Owner------------------------------------"); 
		Response responseAddingDeveloperasOwner = RestAssured.
				given()
				.headers(KnowledgeCollection.HeadersWithJWTToken(collectappnbotdetails.get("jwt")))
				.body(KnowledgeCollection.addDeveloperpayLoad(developeremailaddress,collectappnbotdetails.get("Dev_role_id"), collectappnbotdetails.get("clonnedBot_StreamID")))  
				.when()
				.post(url+publicadminasUBDevResource)					
				.then()
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

	@Test(priority = 40, enabled = true)
	public static void ExtractFAQsinKnowledgeCollection() throws InterruptedException
	{
		try {
		System.out.println("-10--------------------------Extract FAQ's Knowldge ID ------------------------------------"); 
		Response responseExtractFAQs = RestAssured.
				given()
				.headers(KnowledgeCollection.HeadersWithKAKCHook(collectappnbotdetails.get("jwt")))
				.body(KnowledgeCollection.ExtractFAQsPayload())
				.when()
				.post(url+urlKC+collectappnbotdetails.get("clonnedBot_StreamID")+"/qna/import?language=en")					
				.then().log().all()
				.extract().response();				
		System.out.println(responseExtractFAQs.jsonPath());		
		collectappnbotdetails.put("KE_ID",responseExtractFAQs.jsonPath().get("_id").toString());
		Assert.assertEquals(String.valueOf(responseExtractFAQs.getStatusCode()),"200");
	}catch(Exception e)
	{
		Assert.fail();
		e.printStackTrace();
	}
	}

	@Test(priority = 41, enabled = true)
	public static void GetQsofExtractinKnowledgeCollection() throws InterruptedException
	{
		try {
		System.out.println("-11----------------GET Questions of Extract --"); 
		Response responseGetQsofExtract = RestAssured.
				given()
				.headers(KnowledgeCollection.HeadersWithKAKCHook(collectappnbotdetails.get("jwt")))				
				.when()
				.get(url+urlKC+collectappnbotdetails.get("clonnedBot_StreamID")+"/qna/"+collectappnbotdetails.get("KE_ID")+"/questions")	 				
				.then().log().all()
				.extract().response();			
		Thread.sleep(10000);		
		JsonPath jp = responseGetQsofExtract.jsonPath();	
		System.out.println(jp.toString());
		collectappnbotdetails.put("Q1_id",jp.get("extractions._id[1]").toString().trim());		
		collectappnbotdetails.put("Q1_Question",jp.get("extractions.question[1]").toString().trim());
		collectappnbotdetails.put("Q1_answer",jp.get("extractions.answer[1]").toString().trim());
		collectappnbotdetails.put("Q2_id",jp.get("extractions._id[2]").toString().trim());		
		collectappnbotdetails.put("Q2_Question",jp.get("extractions.question[2]").toString().trim());
		collectappnbotdetails.put("Q2_answer",jp.get("extractions.answer[2]").toString().trim());		
		System.out.println(collectappnbotdetails);
		Assert.assertEquals(String.valueOf(responseGetQsofExtract.getStatusCode()),"200");
	}catch(Exception e)
	{
		Assert.fail();
		e.printStackTrace();
	}
	}
	
	@Test(priority = 42, enabled = true)
	public static void GetKTofTaskofCollectioninKnowledgeCollection() throws InterruptedException
	{
		try {
			System.out.println("--------------------12-------GET  Knowledge Task of A Collection------------------------------------"); 
			Response responseGetKTofTaskofCollection = RestAssured.
					given()
					.headers(KnowledgeCollection.HeadersWithKAKCHook(collectappnbotdetails.get("jwt"))).log().all()				
					.when()
					.get(url+urlKC+collectappnbotdetails.get("clonnedBot_StreamID")+"/knowledgeTasks?language=en&state=configured")					
					.then().log().all()
					.extract().response();			  
			Thread.sleep(10000);
						
			String KT_ID=responseGetKTofTaskofCollection.jsonPath().get("_id[0]").toString();
			collectappnbotdetails.put("KT_ID",KT_ID);		
			Assert.assertEquals(String.valueOf(responseGetKTofTaskofCollection.getStatusCode()),"200");
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test(priority = 43, enabled = true)
	public static void AddQstoCollectioninKnowledgeCollection() throws InterruptedException
	{
		try {
		int maxqs=2;	
		for(int numberofQs=1;numberofQs<=maxqs;numberofQs++)
		{
			System.out.println("--13-----------------------Add Questions : "+maxqs+ "to collection------------------------------------"); 
			Response responseAddQstoCollections = RestAssured.
					given()
					.headers(KnowledgeCollection.HeadersWithKAKCHook(collectappnbotdetails.get("jwt")))
					.body(KnowledgeCollection.AddQstoCollectionPayload(collectappnbotdetails.get("Q"+numberofQs+"_Question"),collectappnbotdetails.get("Q"+numberofQs+"_answer"), collectappnbotdetails.get("KT_ID"),collectappnbotdetails.get("clonnedBot_StreamID"),collectappnbotdetails.get("Q"+numberofQs+"_id"))).log().all()
					.when()
					.post(url+urlKC+collectappnbotdetails.get("clonnedBot_StreamID")+"/faqs/bulk?language=en")					
					.then().log().all()
					.extract().response();		
			Thread.sleep(10000);		
			System.out.println(responseAddQstoCollections.asString()); 			
			if(responseAddQstoCollections.asString().equalsIgnoreCase("Success"))
			{
				System.out.println(" Add Question to collection is succesffuly done and Numer of  Questions are "+numberofQs);

			}else
			{
				System.out.println(" Add Question to collection is FAIL");
			}
			Assert.assertEquals(String.valueOf(responseAddQstoCollections.getStatusCode()),"200");
		}
	}catch(Exception e)
	{
		Assert.fail();
		e.printStackTrace();
	}
	}

	@Test(priority = 44, enabled = true)
	public static void publishbotinKnowledgeCollection() throws InterruptedException
	{
		try {
		System.out.println("--14--------------------- Publish Bot ---------------------------");
		Response responsPublishBot = RestAssured.
				given()
				.headers(KnowledgeCollection.Headersforpublishbot(collectappnbotdetails.get("jwt")))
				.body(KnowledgeCollection.publishbotPayload()).log().all() 
				.when()
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

	@Test(priority = 45, enabled = true)
	public static void GetFAQsCollectioninKnowledgeCollection() throws InterruptedException
	{
		try {
		System.out.println("--15-------------------------GET FAQ's Collection------------------------------------"); 
		Response responseGetFAQsCollection = RestAssured.
				given()
				.headers(KnowledgeCollection.HeadersWithKAKCHook(collectappnbotdetails.get("jwt")))				
				.when()
				.get(url+urlKC+collectappnbotdetails.get("clonnedBot_StreamID")+"/faqs?ktId="+collectappnbotdetails.get("KT_ID")+"&parentId=idPrefix21&limit=50&offset=0&rnd=n3bfxo&withallchild=true&type=all&language=en")					
				.then().log().all()
				.extract().response();			  
		
		JsonPath jp = responseGetFAQsCollection.jsonPath();		
		collectappnbotdetails.put("QuestionsFetched",jp.get("faqs.questionPayload.question").toString());		
		System.out.println("Questions Fetched FAQ's Collection :"+collectappnbotdetails.get("QuestionsFetched"));
		Assert.assertEquals(String.valueOf(responseGetFAQsCollection.getStatusCode()),"200");
	}catch(Exception e)
	{
		Assert.fail();
		e.printStackTrace();
	}
	}

	@Test(priority = 46, enabled = true)
	public static void GetExtractsofCollectioninKnowledgeCollection() throws InterruptedException
	{
		try {
		System.out.println("---16------------------------GET EXTRACTION OF COLLECTION------------------------------------"); 
		Response responseGetExtractsofCollection = RestAssured.
				given()
				.headers(KnowledgeCollection.HeadersWithKAKCHook(collectappnbotdetails.get("jwt")))				
				.when()
				.get(url+urlKC+collectappnbotdetails.get("clonnedBot_StreamID")+"/qna/history?language=en")					
				.then()
				.extract().response();
		Thread.sleep(5000);
		
		
		JsonPath jp = responseGetExtractsofCollection.jsonPath();		
		collectappnbotdetails.put("GetExtractsofCollection",jp.get("metaqnas.status[0]").toString());
		if(collectappnbotdetails.get("GetExtractsofCollection").equalsIgnoreCase("success"))
		{
			System.out.println("History of Collection Extract successfully received");
			System.out.println("qnaAddedCount :: "+jp.get("metaqnas.qnaAddedCount[0]").toString());
			System.out.println("qnaExtractedCount  :: "+jp.get("metaqnas.qnaExtractedCount[0]").toString());
			System.out.println("qnaCount :: "+jp.get("metaqnas.qnaCount[0]").toString());            	
		}else {
			System.out.println("failed to Extract History of QnA " );
		}
		Assert.assertEquals(String.valueOf(responseGetExtractsofCollection.getStatusCode()),"200");
	}catch(Exception e)
	{
		Assert.fail();
		e.printStackTrace();
	}
	}		

	@Test(priority = 47, enabled = true)
	public static void genereateJWTtokenforNonAmdinAppinKnowledgeCollection()
	{
		try {
		System.out.println("---------genereateJWTtoken for non Admin app------------------"); 
		Response responsejwtToken = RestAssured.
				given()
				.headers(KnowledgeCollection.HeadersWithAPIKey())
				.body(KnowledgeCollection.genereateJWTtokenPayLoad(collectappnbotdetails.get("BuilderApp_sdkClientId"),collectappnbotdetails.get("BuilderApp_cS"),collectappnbotdetails.get("BuilderApp_UserId")))
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

	@Test(priority = 48, enabled = true)
	public static void findIntentinKnowledgeCollection() throws InterruptedException
	{
		try {
		System.out.println("--18--------------------- findIntent  ---------------");
		Response responsefindIntent = RestAssured.
				given()
				.headers(KnowledgeCollection.Headersforfindintent(collectappnbotdetails.get("builderApp_jwt")))
				.body(KnowledgeCollection.findIntentPayload(collectappnbotdetails.get("Q1_Question"))) 
				.when().log().all()
				.post(url+findIntent+collectappnbotdetails.get("clonnedBot_StreamID")+"/findIntent")					
				.then()
				.extract().response();					
		System.out.println("findIntent "+responsefindIntent.asString());
		Assert.assertEquals(String.valueOf(responsefindIntent.getStatusCode()),"200");
	}catch(Exception e)
	{
		Assert.fail();
		e.printStackTrace();
	}
	}


	/*
	 * delete all qna of knowledgeTask 	
	 */
//		@Test(priority = 19, enabled = true)
//		public static void DeleteKCData() throws InterruptedException
//		{
//			System.out.println("--19--------------------Delete all qna of knowledgeTask ---------------");
//			Response responseDeleteKCData = 
//					given()
//					.headers(KnowledgeCollection.HeadersWithKAKCHooknAccountID(collectappnbotdetails.get("jwt"),collectappnbotdetails.get("accountId")))
//					.body(KnowledgeCollection.DeleteKCDataPayload(collectappnbotdetails.get("KT_ID"))) 
//					.when().log().all()
//					.delete(url+"/api/public/bot/"+collectappnbotdetails.get("clonnedBot_StreamID")+"/faqs")					
//					.then()
//					.extract().response();		
//			System.out.println("Delete all qna of knowledgeTask --------------- "+responseDeleteKCData.asString());
//		}


	

}
