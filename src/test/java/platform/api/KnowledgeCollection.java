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
			System.out.println("-31-----------------createUser Knowledge Collection---------------------------");
			String kTimeinHHMMSS=Payloads.fntoreturntimeinHHMMSS();
			Response responsecukc = RestAssured
					.given().headers(KnowledgeCollection.HeadersWithAPIKey()) 
					.body(KnowledgeCollection.createUserPayLoad(kTimeinHHMMSS))
					.when()
					.post(url+internalAccountResource)
					.then().log().all()
					.extract().response();	
			Thread.sleep(5000);					  
			collectappnbotdetails = new LinkedHashMap<String, String>();			
			collectappnbotdetails.put("emailId", responsecukc.jsonPath().get("emailId").toString());			
			collectappnbotdetails.put("accountId", responsecukc.jsonPath().get("accountId").toString());
			collectappnbotdetails.put("userId", responsecukc.jsonPath().get("userId").toString());				  			
			Assert.assertEquals(String.valueOf(responsecukc.getStatusCode()),"200");
		}catch(Exception e)
		{			
			e.printStackTrace();

		}		
	}

	@Test(priority = 32, enabled = true)
	public static void createAdminAppinKnowledgeCollection() {
		try {						
			System.out.println("---32---------------createAdminApp---------------------------");
			Response responseadmincreateappkc = RestAssured.
					given()
					.headers(KnowledgeCollection.HeadersWithAPIKey())
					.body(KnowledgeCollection.createAdminAPPPayLoad(collectappnbotdetails.get("accountId"),collectappnbotdetails.get("userId")))
					.when()
					.post(url+internalClientappResource)					
					.then().log().all()
					.extract().response();	
			Thread.sleep(5000);													
			collectappnbotdetails.put("Name",responseadmincreateappkc.jsonPath().get("name").toString());
			collectappnbotdetails.put("cId", responseadmincreateappkc.jsonPath().get("cId").toString());
			collectappnbotdetails.put("cS", responseadmincreateappkc.jsonPath().get("cS").toString());
			collectappnbotdetails.put("nId", responseadmincreateappkc.jsonPath().get("nId").toString());	
			collectappnbotdetails.put("accountId", responseadmincreateappkc.jsonPath().get("accountId").toString());
			collectappnbotdetails.put("userId", responseadmincreateappkc.jsonPath().get("nId").toString());			
			TimeinHHMMSS=null;
			Assert.assertEquals(String.valueOf(responseadmincreateappkc.getStatusCode()),"200");
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
			System.out.println("-33--------genereateJWTtoken------------------"); 
			Response responsejwtTokenfirstkc = RestAssured.
					given()
					.headers(KnowledgeCollection.HeadersWithAPIKey())
					.body(KnowledgeCollection.genereateJWTtokenPayLoad(collectappnbotdetails.get("cId"),collectappnbotdetails.get("cS"),collectappnbotdetails.get("nId")))
					.when()
					.post(urljwtTokenGenerater)					
					.then().log().all()
					.extract().response();					
			collectappnbotdetails.put("jwt",responsejwtTokenfirstkc.jsonPath().get("jwt").toString());
			Assert.assertEquals(String.valueOf(responsejwtTokenfirstkc.getStatusCode()),"200");
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
			System.out.println("-34--------cloning KnowledgeCollection BOT------------------");
			if (url.contains("koradev-bots.kora.ai")) {				
				cloningbot=	devKnowledgeCollectionBOT;
			} else if (url.contains("qa1-bots.kore.ai")) {			
				cloningbot=	qa1KnowledgeCollectionBOT;
			}else if (url.contains("staging-bots.korebots.com")) {			
				cloningbot=	stagingKnowledgeCollectionBOT;	
			} else {
				System.out.println(" Given URL "+ url+" is neither koradev-bots.kora.ai nor qa1-bots.kora.ai");
			}
			Response responsecloningSmapleBotkc = RestAssured.
					given()
					.headers(KnowledgeCollection.headersforcloneBotpayLoad(collectappnbotdetails.get("jwt"),collectappnbotdetails.get("userId"), collectappnbotdetails.get("accountId"))).log().all()				
					.when().log().all()
					.get(url+"/api/public/samplebots/"+cloningbot+"/add")  //hrere  ubVersion=1 wont be there 					
					.then().log().all() 
					.extract().response();
			Thread.sleep(10000);		
			collectappnbotdetails.put("clonnedBot_StreamID",responsecloningSmapleBotkc.jsonPath().get("_id").toString());									
			collectappnbotdetails.put("clonnedBotName",responsecloningSmapleBotkc.jsonPath().get("name").toString());
			Assert.assertEquals(String.valueOf(responsecloningSmapleBotkc.getStatusCode()),"200");
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
			System.out.println("-35--------cloned BOT Setup------------------"); 
			Response responsecloningSmapleBotsetupkc = RestAssured.
					given()
					.headers(KnowledgeCollection.HeaderswithJWTnAccountID(collectappnbotdetails.get("jwt"), collectappnbotdetails.get("accountId")))				
					.body(KnowledgeCollection.clonedBot_SetuppayLoad(collectappnbotdetails.get("clonnedBotName")))
					.when()  
					.put(url+"/api/public/bot/"+ collectappnbotdetails.get("clonnedBot_StreamID")+"/setup")  					
					.then()
					.extract().response();
			Thread.sleep(5000);		
			System.out.println("Clonned bot Setup Status ::" +responsecloningSmapleBotsetupkc.getStatusCode());
			Assert.assertEquals(String.valueOf(responsecloningSmapleBotsetupkc.getStatusCode()),"200");
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
			System.out.println("-36----------------------Create Non-Amin Builder app----------importedBot_streamId-----------------"); 
			Response responsbuilderappkc = RestAssured.
					given()
					.headers(KnowledgeCollection.HeadersWithAPIKey())
					.body(KnowledgeCollection.createbuilderAppnonAdminpayLoad(collectappnbotdetails.get("clonnedBot_StreamID"),collectappnbotdetails.get("accountId"),collectappnbotdetails.get("userId")))
					.when()
					.post(url+internalClientappResource)					
					.then().log().all()
					.extract().response();	
			Thread.sleep(5000);				
			collectappnbotdetails.put("BuilderApp_Name",responsbuilderappkc.jsonPath().get("name").toString());
			collectappnbotdetails.put("BuilderApp_sdkClientId", responsbuilderappkc.jsonPath().get("cId").toString());
			collectappnbotdetails.put("BuilderApp_cS", responsbuilderappkc.jsonPath().get("cS").toString());
			collectappnbotdetails.put("BuilderApp_UserId", responsbuilderappkc.jsonPath().get("nId").toString());	
			Assert.assertEquals(String.valueOf(responsbuilderappkc.getStatusCode()),"200");		
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
			System.out.println("-37---------------------- enableRTM ------------importedBot_streamId---------------"); 
			Response responseadminkc = RestAssured.
					given()
					.headers(KnowledgeCollection.HeadersWithJWTToken(collectappnbotdetails.get("jwt")))
					.body(KnowledgeCollection.enableRTMpayLoad(collectappnbotdetails.get("clonnedBot_StreamID"),collectappnbotdetails.get("BuilderApp_Name"),collectappnbotdetails.get("BuilderApp_sdkClientId"))) 
					.when()
					.post(url+publicEnableTRMChannelsResource)					
					.then().log().all()
					.extract().response();	
			Thread.sleep(5000);					
			Assert.assertEquals(String.valueOf(responseadminkc.getStatusCode()),"200");
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
			System.out.println("--38--------------------------Get Role------------------------------------"); 
			Response responsegetRolekc = RestAssured.
					given()
					.headers(KnowledgeCollection.HeadersWithJWTToken(collectappnbotdetails.get("jwt")))				
					.when()
					.get(url+publicGetRolesResource)					
					.then()
					.extract().response();		

			JsonPath jp = responsegetRolekc.jsonPath();	
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
			Assert.assertEquals(String.valueOf(responsegetRolekc.getStatusCode()),"200");
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
			System.out.println("-----39----------------------Adding Developer as Owner MAP to Owner------------------------------------"); 
			Response responseAddingDeveloperasOwnerkc = RestAssured.
					given()
					.headers(KnowledgeCollection.HeadersWithJWTToken(collectappnbotdetails.get("jwt")))
					.body(KnowledgeCollection.addDeveloperpayLoad(developeremailaddress,collectappnbotdetails.get("Dev_role_id"), collectappnbotdetails.get("clonnedBot_StreamID")))  
					.when()
					.post(url+publicadminasUBDevResource)					
					.then()
					.extract().response();
			Thread.sleep(5000);		
			System.out.println("Adding Developer as Owner Response ::"+responseAddingDeveloperasOwnerkc.asString());
			String MaptoMomainrsponse=responseAddingDeveloperasOwnerkc.jsonPath().get("msg");
			if(MaptoMomainrsponse.contains(developeremailaddress +"created"))
			{
				System.out.println(developeremailaddress +":: user created successfully as added as developer");
			}else {
				System.out.println(responseAddingDeveloperasOwnerkc.jsonPath().toString());
			}
			Assert.assertEquals(String.valueOf(responseAddingDeveloperasOwnerkc.getStatusCode()),"200");
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
			System.out.println("-40--------------------------Extract FAQ's Knowldge ID ------------------------------------");
			Thread.sleep(5000);
  			Response responseExtractFAQskc = RestAssured.
					given()
					.headers(KnowledgeCollection.HeadersWithKAKCHook(collectappnbotdetails.get("jwt")))
					.body(KnowledgeCollection.ExtractFAQsPayload()).log().all()
					.when()
					.post(url+urlKC+collectappnbotdetails.get("clonnedBot_StreamID")+"/qna/import?language=en")					
					.then().log().all()
					.extract().response();				
			System.out.println(responseExtractFAQskc.jsonPath());		
			collectappnbotdetails.put("KE_ID",responseExtractFAQskc.jsonPath().get("_id").toString());
			Thread.sleep(5000);
			Assert.assertEquals(String.valueOf(responseExtractFAQskc.getStatusCode()),"200");			
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
			System.out.println("-41----------------Extract FQA's from URLr--");	
			Thread.sleep(5000);
			Response responseGetQsofExtractkc = RestAssured.
					given()
					.headers(KnowledgeCollection.HeadersWithKAKCHook(collectappnbotdetails.get("jwt"))).log().all()				
					.when().log().all()
					.get(url+urlKC+collectappnbotdetails.get("clonnedBot_StreamID")+"/qna/"+collectappnbotdetails.get("KE_ID")+"/questions")	 				
					.then().log().all()
					.extract().response();	
			Thread.sleep(5000);
			System.out.println(responseGetQsofExtractkc.jsonPath());						
			collectappnbotdetails.put("Q1_id",responseGetQsofExtractkc.jsonPath().get("extractions._id[1]").toString().trim());		
			collectappnbotdetails.put("Q1_Question",responseGetQsofExtractkc.jsonPath().get("extractions.question[1]").toString().trim());
			collectappnbotdetails.put("Q1_answer",responseGetQsofExtractkc.jsonPath().get("extractions.answer[1]").toString().trim());
			collectappnbotdetails.put("Q2_id",responseGetQsofExtractkc.jsonPath().get("extractions._id[2]").toString().trim());		
			collectappnbotdetails.put("Q2_Question",responseGetQsofExtractkc.jsonPath().get("extractions.question[2]").toString().trim());
			collectappnbotdetails.put("Q2_answer",responseGetQsofExtractkc.jsonPath().get("extractions.answer[2]").toString().trim());		
			System.out.println(collectappnbotdetails);
			Assert.assertEquals(String.valueOf(responseGetQsofExtractkc.getStatusCode()),"200");
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
			System.out.println("--------------------42-------GET  Knowledge Task of A Collection------------------------------------"); 
			Response responseGetKTofTaskofCollectionkc = RestAssured.
					given()
					.headers(KnowledgeCollection.HeadersWithKAKCHook(collectappnbotdetails.get("jwt"))).log().all()				
					.when()
					.get(url+urlKC+collectappnbotdetails.get("clonnedBot_StreamID")+"/knowledgeTasks?language=en&state=configured")					
					.then().log().all()
					.extract().response();			  
			Thread.sleep(10000);

			String KT_ID=responseGetKTofTaskofCollectionkc.jsonPath().get("_id[0]").toString();
			collectappnbotdetails.put("KT_ID",KT_ID);		
			Assert.assertEquals(String.valueOf(responseGetKTofTaskofCollectionkc.getStatusCode()),"200");
		}catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Test(priority = 43, enabled = true)
	public static void AddQstoCollectioninKnowledgeCollection() throws InterruptedException
	{
		try {
			int maxqs=2;	
			for(int numberofQs=1;numberofQs<2;numberofQs++)
			{
				System.out.println("--43-----------------------Add Questions : "+maxqs+ "to collection------------------------------------"); 
				Response responseAddQstoCollectionskc = RestAssured.
						given()
						.headers(KnowledgeCollection.HeadersWithKAKCHook(collectappnbotdetails.get("jwt")))
						.body(KnowledgeCollection.AddQstoCollectionPayload(collectappnbotdetails.get("Q"+numberofQs+"_Question"),collectappnbotdetails.get("Q"+numberofQs+"_answer"), collectappnbotdetails.get("KT_ID"),collectappnbotdetails.get("clonnedBot_StreamID"),collectappnbotdetails.get("Q"+numberofQs+"_id"))).log().all()
						.when()
						.post(url+urlKC+collectappnbotdetails.get("clonnedBot_StreamID")+"/faqs/bulk?language=en")					
						.then().log().all()
						.extract().response();		
				Thread.sleep(10000);		
				System.out.println(responseAddQstoCollectionskc.asString()); 			
				if(responseAddQstoCollectionskc.asString().equalsIgnoreCase("Success"))
				{
					System.out.println(" Add Question to collection is succesffuly done and Numer of  Questions are "+numberofQs);

				}else
				{
					System.out.println(" Add Question to collection is FAIL");
				}
				Assert.assertEquals(String.valueOf(responseAddQstoCollectionskc.getStatusCode()),"200");
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
			System.out.println("--44--------------------- Publish Bot ---------------------------");
			Response responsPublishBotkc = RestAssured.
					given()
					.headers(KnowledgeCollection.Headersforpublishbot(collectappnbotdetails.get("jwt")))
					.body(KnowledgeCollection.publishbotPayload()).log().all() 
					.when()
					.post(url+publicEnableSdk+collectappnbotdetails.get("clonnedBot_StreamID")+"/publish")					
					.then()
					.extract().response();	
			Thread.sleep(10000);		
			System.out.println("Publish bot Status code "+responsPublishBotkc.jsonPath().get("status"));
			Assert.assertEquals(String.valueOf(responsPublishBotkc.getStatusCode()),"200");
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
			System.out.println("--45-------------------------GET FAQ's Collection------------------------------------"); 
			Response responseGetFAQsCollectionkc = RestAssured.
					given()
					.headers(KnowledgeCollection.HeadersWithKAKCHook(collectappnbotdetails.get("jwt")))				
					.when()
					.get(url+urlKC+collectappnbotdetails.get("clonnedBot_StreamID")+"/faqs?ktId="+collectappnbotdetails.get("KT_ID")+"&parentId=idPrefix21&limit=50&offset=0&rnd=n3bfxo&withallchild=true&type=all&language=en")					
					.then().log().all()
					.extract().response();			  
			JsonPath jp = responseGetFAQsCollectionkc.jsonPath();		
			collectappnbotdetails.put("QuestionsFetched",jp.get("faqs.questionPayload.question").toString());		
			System.out.println("Questions Fetched FAQ's Collection :"+collectappnbotdetails.get("QuestionsFetched"));
			Assert.assertEquals(String.valueOf(responseGetFAQsCollectionkc.getStatusCode()),"200");
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
			System.out.println("---46------------------------GET EXTRACTION OF COLLECTION------------------------------------"); 
			Response responseGetExtractsofCollectionkc = RestAssured.
					given()
					.headers(KnowledgeCollection.HeadersWithKAKCHook(collectappnbotdetails.get("jwt")))				
					.when()
					.get(url+urlKC+collectappnbotdetails.get("clonnedBot_StreamID")+"/qna/history?language=en")					
					.then()
					.extract().response();
			Thread.sleep(5000);
			JsonPath jp = responseGetExtractsofCollectionkc.jsonPath();		
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
			Assert.assertEquals(String.valueOf(responseGetExtractsofCollectionkc.getStatusCode()),"200");
		}catch(Exception e)
		{
			Assert.fail();
			e.printStackTrace();
		}
	}		

//	@Test(priority = 47, enabled = true)
//	public static void genereateJWTtokenforNonAmdinAppinKnowledgeCollection()
//	{
//		try {
//			System.out.println("-----47----genereateJWTtoken for non Admin app------------------"); 
//			Response responsejwtTokenkc = RestAssured.
//					given()
//					.headers(KnowledgeCollection.HeadersWithAPIKey())
//					.body(KnowledgeCollection.genereateJWTtokenPayLoad(collectappnbotdetails.get("BuilderApp_sdkClientId"),collectappnbotdetails.get("BuilderApp_cS"),collectappnbotdetails.get("BuilderApp_UserId")))
//					.when()
//					.post(urljwtTokenGenerater)					
//					.then()
//					.extract().response();										
//			collectappnbotdetails.put("builderApp_jwt",responsejwtTokenkc.jsonPath().get("jwt").toString());
//			Assert.assertEquals(String.valueOf(responsejwtTokenkc.getStatusCode()),"200");
//		}catch(Exception e)
//		{
//			Assert.fail();
//			e.printStackTrace();
//		}
//
//	}

//	@Test(priority = 48, enabled = true)
//	public static void findIntentinKnowledgeCollection() throws InterruptedException
//	{
//		try {
//			System.out.println("--48--------------------- findIntent  ---------------");
//			Response responsefindIntentkc = RestAssured.
//					given()
//					.headers(KnowledgeCollection.Headersforfindintent(collectappnbotdetails.get("builderApp_jwt")))
//					.body(KnowledgeCollection.findIntentPayload(collectappnbotdetails.get("Q1_Question"))) 
//					.when().log().all()
//					.post(url+findIntent+collectappnbotdetails.get("clonnedBot_StreamID")+"/findIntent")					
//					.then()
//					.extract().response();					
//			System.out.println("findIntent "+responsefindIntentkc.asString());
//			Assert.assertEquals(String.valueOf(responsefindIntentkc.getStatusCode()),"200");
//		}catch(Exception e)
//		{
//			Assert.fail();
//			e.printStackTrace();
//		}
//	}


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
