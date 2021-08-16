package platform.api;

import java.awt.Desktop;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.AfterClass;
import org.testng.ITestContext;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeSuite;

import com.beust.jcommander.Parameters;


public class Payloads {
	public static String internalAccountResource ="/api/internal/account";
	public static String internalWfResource ="/api/internal/setWfAdmin";
	public static String internalClientappResource ="/api/internal/clientapp"; 
	public static String internalEnableSSOResource="/api/internal/enablesso";
	public static String publicEnableTRMChannelsResource="/api/public/channels"; 
	public static String publicGetRolesResource ="/api/public/roles?roleType=bot"; 
	public static String publicadminasUBDevResource ="/api/public/users";
	public static String publicimportBOTresource ="/api/public/bot/import";
	public static String publicimportBOTstatus ="/api/public/bot/import/status/";
	public static String tokenurl="https://developer.kore.ai/tools/jwt/";
	public static String publicuploadFile="/api/public/uploadfile";
	public static String generateAuthToken="/api/oauth/token";	
	public static String publicEnableSdk= "/api/public/bot/";		
	public static String findIntent ="/api/v1.1/rest/streams/";
	public static String urljwtTokenGenerater ="https://demo.kore.net/mock/util/sts";	
	public static LinkedHashMap<String, String> collectappnbotdetails;
	public static String developeremailaddress="ramana.kommula@kore.com";
	public static String devUniversalBOT="st-6dc23651-df04-5b8d-9a5d-50f0430ef992";
	public static String devKnowledgeCollectionBOT= "st-2a3841db-fa61-5708-b9e5-b81fa4d89395";
	public static String devServiceNowBOT= "st-c02d83a7-87b2-5e1e-be6a-73b9ea400542";
	public static String qa1UniversalBOT="st-bce61471-1ba6-5507-b947-7c25050316db";
	public static String qa1KnowledgeCollectionBOT= "st-cd95f308-f819-5ebe-961f-952030056ea6";
	public static String qa1ServiceNowBOT= "st-51cdbe92-4a23-54e4-8a53-2dfd89ba93b4";
	
	public static String stagingUniversalBOT="st-69f7b9b0-c449-5ac3-8ced-1ceb0c5b9553";	
	public static String stagingServiceNowBOT= "st-5357119e-cde0-5f0d-989a-eca1006f3a54";
	public static String stagingKnowledgeCollectionBOT= "st-19ac63cf-395f-5ae2-87fe-b178b4b6f8ef";
	
	public static String Adminpassword="Kore@12345";
	public static int waitincreamentalLoop;

	public static String url="";
	public static String urlKC="/api/public/stream/";
	public static String cloningbot="";
	public static String TimeinHHMMSS = null;
	public static Map<String, String> results_Env = new LinkedHashMap<String, String>();	
	public static Map<String, String> results_Tenant = new LinkedHashMap<String, String>();
	public static Map<String, String> results_KC = new LinkedHashMap<String, String>();
	public static Map<String, String> results_SS = new LinkedHashMap<String, String>();

	static int ENV_passcount =0;
	static int ENV_failcount =0;
	static int Tenant_passcount =0;
	static int Tenant_failcount =0;
	static int KC_passcount =0;
	static int KC_failcount =0;
	static int SS_passcount =0;
	static int SS_failcount =0;

	public static String env = "";
	public static String buildNum = "null";

	@BeforeSuite
	public void extractEnviromentURL(ITestContext ctx) {
		
//		env = "QA1";//System.getenv("ENV");
//		env = "DEV";//System.getenv("ENV");
	env = "STAGING";//System.getenv("ENV");
		buildNum = System.getenv("BUILD_NUMBER");
		System.out.println("------------------------??? "+env);
		System.out.println("------------------------??? "+buildNum);		
	//	String urlfromxmal = ctx.getCurrentXmlTest().getParameter("Environment");
		System.out.println("Current execution is in :=============== :"+env);
		
		if (env.equalsIgnoreCase("DEV")){
			
			url= "https://koradev-bots.kora.ai";
			
		}else if (env.equalsIgnoreCase("QA1")){
			
			url= "https://qa1-bots.kore.ai";
			
		}else if (env.equalsIgnoreCase("STAGING")){
			
			url= "https://staging-bots.korebots.com";
			
		}else if (env.equalsIgnoreCase("PROD")){
			
			url= "null";
			
		}else {
			url= "null";
			System.out.println("Please provide valid environment to run.. : ============"+url);
		}
		
		System.out.println("Executing in folowing Environment ::"+url);
		
	}

	@AfterSuite
	public void GeerateHTMLReport()
	{
		String dir = System.getProperty("user.dir");
		BufferedWriter writer;
		File file;		
		String executingENV=null;
		int ENV_total=ENV_passcount+ENV_failcount;
		int Tenant_total=Tenant_passcount+Tenant_failcount;
		int KC_total=KC_passcount+KC_failcount;
		int SS_total=SS_passcount+SS_failcount;
		if(url.contains("dev"))
		{			
			executingENV="DEV";
		}else if(url.contains("qa"))
		{
			executingENV="QA1";
		}else if(url.contains("staging"))
		{
			executingENV="STAGING";
		}
		else {
			System.out.println("Pldase give a valid URL ");
		}

		Format formatter = new SimpleDateFormat("dd-MMM-yy");
	    String todaysDate = formatter.format(new Date());
	    
		try {
			file = new File(dir+"/TCResults.html");
			writer = new BufferedWriter(new FileWriter(file));
			writer.write(
					"<html><head></head>");
			writer.write("<body><h2>Platform API Automation Daily Execution Reports ("+executingENV+") "+todaysDate+"</h2></body>");
			writer.write("<body> <table> <table border='1'> <tr> <th> <b>Environment	</b> </th> <td> "+executingENV+" </td> <tr> <th><b>URL</b> </th> <td><font color=\"blue\"><u>"+Payloads.url+"</u></font></td> </tr> </body>");
			writer.write("<body><h2> </h2></body>");
			writer.write("<table> </table>");
			writer.write("<body>  <table>   <table border='1'>    <tr>     <th>Feature_Name</th>     <th>Pass</th>     <th>Fail</th>     <th>Total</th>    </tr>    <tr>     <td>Environment_Setup</td>     <td><font color=\"green\">"+ENV_passcount+"</font>      </td>     <td><font color=\"red\">"+ENV_failcount+"</font>      </td>     <td>"+ENV_total+"</td>    </tr>    <tr>     <td>Tenant_Onboarding</td>     <td><font color=\"green\">"+Tenant_passcount+"</font>      </td>     <td><font color=\"red\">"+Tenant_failcount+"</font>      </td>     <td>"+Tenant_total+"</td>    </tr>    <tr>     <td>Knowledge_Collection</td>     <td><font color=\"green\">"+KC_passcount+"</font>      </td>     <td><font color=\"red\">"+KC_failcount+"</font>      </td>     <td>"+KC_total+"</td>    </tr>    <tr>     <td>Search_Skill</td>     <td><font color=\"green\">"+SS_passcount+"</font>      </td>     <td><font color=\"red\">"+SS_failcount+"</font>      </td>     <td>"+SS_total+"</td>    </tr> </body>");						
			writer.write("<body><h2>     </h2></body>");
			writer.write("<table> </table>");
			writer.write("<body>  <table>   <table border='1'>  <th><b>Feature</b></th>               <th><b>Description</b></th>               <th><b>Result</b></th>");

			System.out.println("in map split");
			int cnt=1;
			for (Map.Entry<String,String> entry : results_Env.entrySet())    
			{						
				String	Feature="Environment_Setup";
				String TestcaseDesction= entry.getKey().trim();						
				String results= entry.getValue().trim();

				if(results.equalsIgnoreCase("PASS")){
					results = "<font color="+"green"+"><b>Pass</b></font>";
				}else if(results.equalsIgnoreCase("FAIL")){
					results = "<font color="+"red"+"><b>Fail</b></font>";
				}			
				writer.write("<tr>");
				writer.write("<tr>");
				writer.write("<td><font-family:"+"Calibri (Body)>");
				writer.write(Feature);
				writer.write("</td> ");

				writer.write("<td><font-family:"+"Calibri (Body)>");
				writer.write(TestcaseDesction);
				writer.write("</td> ");

				writer.write("<td><font-family:"+"Calibri (Body)>");
				writer.write(results);
				writer.write("</td>");

				writer.write("</tr> ");
			}
			for (Map.Entry<String,String> entry1 : results_Tenant.entrySet())    
			{		

				String	Feature="Tenant_Onboarding";
				String TestcaseDesction= entry1.getKey().trim();						
				String results= entry1.getValue().trim();

				if(results.equalsIgnoreCase("PASS")){
					results = "<font color="+"green"+"><b>Pass</b></font>";
				}else if(results.equalsIgnoreCase("FAIL")){
					results = "<font color="+"red"+"><b>Fail</b></font>";
				}			
				writer.write("<tr>");
				writer.write("<tr>");
				writer.write("<td><font-family:"+"Calibri (Body)>");
				writer.write(Feature);
				writer.write("</td> ");

				writer.write("<td><font-family:"+"Calibri (Body)>");
				writer.write(TestcaseDesction);
				writer.write("</td> ");

				writer.write("<td><font-family:"+"Calibri (Body)>");
				writer.write(results);
				writer.write("</td>");

				writer.write("</tr> ");
			}

			for (Map.Entry<String,String> entry2 : results_KC.entrySet())    
			{		
				String	Feature="Knowledge_Collection";
				String TestcaseDesction= entry2.getKey().trim();						
				String results= entry2.getValue().trim();

				if(results.equalsIgnoreCase("PASS")){
					results = "<font color="+"green"+"><b>Pass</b></font>";
				}else if(results.equalsIgnoreCase("FAIL")){
					results = "<font color="+"red"+"><b>Fail</b></font>";
				}			
				writer.write("<tr>");
				writer.write("<tr>");
				writer.write("<td><font-family:"+"Calibri (Body)>");
				writer.write(Feature);
				writer.write("</td> ");

				writer.write("<td><font-family:"+"Calibri (Body)>");
				writer.write(TestcaseDesction);
				writer.write("</td> ");

				writer.write("<td><font-family:"+"Calibri (Body)>");
				writer.write(results);
				writer.write("</td>");

				writer.write("</tr> ");
			}

			for (Map.Entry<String,String> entry3 : results_SS.entrySet())    
			{		

				String	Feature="Search_Skill";
				String TestcaseDesction= entry3.getKey().trim();						
				String results= entry3.getValue().trim();

				if(results.equalsIgnoreCase("PASS")){
					results = "<font color="+"green"+"><b>Pass</b></font>";
				}else if(results.equalsIgnoreCase("FAIL")){
					results = "<font color="+"red"+"><b>Fail</b></font>";
				}			
				writer.write("<tr>");
				writer.write("<tr>");
				writer.write("<td><font-family:"+"Calibri (Body)>");
				writer.write(Feature);
				writer.write("</td> ");

				writer.write("<td><font-family:"+"Calibri (Body)>");
				writer.write(TestcaseDesction);
				writer.write("</td> ");

				writer.write("<td><font-family:"+"Calibri (Body)>");
				writer.write(results);
				writer.write("</td>");

				writer.write("</tr> ");
			}

			writer.write("</tr></table>" + "</body>" + "</html>");
			//	writer.write("</html>");
			writer.close();
			Desktop.getDesktop().browse(file.toURI());
		} catch (IOException e) {
			System.out.println("IO EXCEPTION-----" + e);
		}

		System.out.println("----------END---------------");
	}


	public static String fntoreturntimeinHHMMSS()
	{
		SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");  
		Date date = new Date();  
		TimeinHHMMSS= formatter.format(date).replaceAll(":", "").substring(11).trim();
		return TimeinHHMMSS;
	}

	public static Map<String,String> HeadersWithAPIKey()
	{
		HashMap<String, String> ctruserHeaders= new HashMap<String, String>();
		ctruserHeaders.put("apikey","ydsjyderjdzfwhhM3wQkjhsfiaHqSBxTpc4XXOP7v/rHdPYfD");
		ctruserHeaders.put("User-Agent","KoreAssistant");
		ctruserHeaders.put("Content-Type","application/json");
		return ctruserHeaders;
	}


	public static Map<String,String> Headersforfindintent(String jwtToken)
	{
		HashMap<String, String> ctruserHeaders= new HashMap<String, String>();		
		ctruserHeaders.put("auth",jwtToken);	
		return ctruserHeaders;
	}
	public static Map<String,String> HeadersWithJWTToken(String jwtToken)
	{
		HashMap<String, String> ctruserHeaders= new HashMap<String, String>();		
		ctruserHeaders.put("auth",jwtToken);	
		ctruserHeaders.put("User-Agent","KoreAssistant");
		ctruserHeaders.put("Content-Type","application/json");					
		return ctruserHeaders;
	}
	public static Map<String,String> HeadersWithJWTTokenforUpload(String jwtToken)
	{
		HashMap<String, String> ctruserHeaders= new HashMap<String, String>();		
		ctruserHeaders.put("auth",jwtToken);		
		ctruserHeaders.put("User-Agent","KoreAssistant"); 		
		ctruserHeaders.put("Content-Type","multipart/form-data");

		return ctruserHeaders;
	}


	public static Map<String,String> HeaderswithJWTnAccountID(String jwtToken, String AccountID)
	{
		HashMap<String, String> ctruserHeaders= new HashMap<String, String>();		
		ctruserHeaders.put("auth",jwtToken);
		ctruserHeaders.put("AccountId",AccountID);
		ctruserHeaders.put("Content-Type","application/json");	
		ctruserHeaders.put("bot-language","en");
		ctruserHeaders.put("User-Agent","KoreAssistant"); 								
		return ctruserHeaders;
	}

	public static Map<String,String> HeaderswithBearerToken(String bearerToken, String AccountID)
	{
		HashMap<String, String> ctruserHeaders= new HashMap<String, String>();		
		ctruserHeaders.put("Authorization","bearer "+bearerToken );
		ctruserHeaders.put("AccountId",AccountID);
		ctruserHeaders.put("Content-Type","application/json;charset=UTF-8");	
		ctruserHeaders.put("bot-language","en");									
		return ctruserHeaders;
	}

	public static Map<String,String> Headersforpublishbot(String jwtToken)
	{
		HashMap<String, String> ctruserHeaders= new HashMap<String, String>();		
		ctruserHeaders.put("auth",jwtToken);
		//		ctruserHeaders.put("AccountId",AccountID);
		ctruserHeaders.put("Content-Type","application/json");			
		ctruserHeaders.put("User-Agent","KoreAssistant"); 								
		return ctruserHeaders;
	}

	public static Map<String,String> HeadersWithKAKCHook(String jwtToken)
	{
		HashMap<String, String> ctruserHeaders= new HashMap<String, String>();		
		ctruserHeaders.put("auth",jwtToken);	
		ctruserHeaders.put("User-Agent","KA KC Hook");
		ctruserHeaders.put("Content-Type","application/json");					
		return ctruserHeaders;
	}

	public static Map<String,String> HeadersWithKAKCHooknAccountID(String jwtToken,String AccountId)
	{
		HashMap<String, String> ctruserHeaders= new HashMap<String, String>();		
		ctruserHeaders.put("auth",jwtToken);	
		ctruserHeaders.put("User-Agent","KA KC Hook");
		ctruserHeaders.put("Content-Type","application/json");		
		ctruserHeaders.put("AccountId",AccountId);		
		return ctruserHeaders;
	}

	public static Map<String,String> HeadersAccessToken()
	{
		HashMap<String, String> ctruserHeaders= new HashMap<String, String>();				
		ctruserHeaders.put("Content-Type","application/json");	
		ctruserHeaders.put("Connection","keep-alive");
		ctruserHeaders.put("bot-language","en");
		ctruserHeaders.put("Authorization","Basic YWRtaW46YWRtaW4=");

		return ctruserHeaders;
	}	

	public static String createUserPayLoad(String sTimeinHHMMSS)
	{						
		return "{\n" + 
				"\"emailId\": \"korausr"+sTimeinHHMMSS+"@usr"+sTimeinHHMMSS+".com\",\n" + 
				"\"firstName\": \"koraFname"+sTimeinHHMMSS+"\",\n" + 
				"\"lastName\": \"Admin user\",\n" + 
				"\"isFromBt\": \"true\",\n" + 
				"\"refererPath\": \"botbuilder\",\n" + 
				"\"displayName\": \"koraqa2 com Admin user\",\n" + 
				"\"password\": \""+Payloads.Adminpassword+"\"\n" + 
				"}";
	}



	public static String createAdminAPPPayLoad(String AccountID,String UserID)
	{		
		return "{\"appName\": \"AdminApp"+TimeinHHMMSS+"\",\n" + 
				"    \"accountId\": \""+AccountID+"\",\n" + 
				"    \"userId\": \""+UserID+"\",\n" + 
				"    \"pns\": {\n" + 
				"      \"enable\": false,\n" + 
				"      \"webhookUrl\": \"\"\n" + 
				"    },\n" + 
				"    \"bots\": [],\n" + 
				"    \"state\": 0,\n" + 
				"    \"algorithm\": \"HS256\",\n" + 
				"    \"isAdminApp\": true,\n" + 
				"    \"__v\": 0,\n" + 
				"    \"scope\": [\n" + 
				"      {\n" + 
				"        \"scopes\": [\n" + 
				"          \"bot_definition:bot_import\",\n" + 
				"          \"bot_definition:bot_export\",\n" + 
				"          \"bot_definition:bot_create\",\n" + 
				"          \"bot_publish:publish_request\",\n" + 
				"          \"test_and_train:ml\",\n" + 
				"          \"test_and_train:faq\",\n" + 
				"          \"test_and_train:ml_import\",\n" + 
				"          \"test_and_train:ml_export\",\n" + 
				"          \"logs:change_logs\",\n" + 
				"          \"logs:admin_console_audit_logs\",\n" + 
				"          \"role_management:role_management\",\n" + 
				"          \"delete:delete_user_data\",\n" + 
				"          \"chat_history:read\",\n" + 
				"          \"bot_sessions:read\",\n" + 
				"          \"user_management:user_management\",\n" + 
				"          \"custom_dashboards:custom_dashboards\",\n" + 
				"          \"knowledge_graph:knowledge_graph\",\n" + 
				"          \"channel_management:channel_management\",\n" + 
				"          \"botkit:botkit\",\n" + 
				"          \"master_admin:master_admin\"\n" + 
				"        ]\n" + 
				"      }\n" + 
				"    ]\n" + 
				"  }";

	}
	public static String genereateJWTtokenPayLoad(String clientId,String clientSecret, String identity)	
	{		
		return "{\n" + 
				"\"clientId\": \""+clientId+"\",\n" + 
				"\"clientSecret\": \""+clientSecret+"\",\n" + 
				"\"identity\": \""+identity+"\",\n" + 
				"\"aud\": \"\",\n" + 
				"\"isAnonymous\": false\n" + 
				"}";

	}

	public static String importBotPayLoad(String BotDef_fileId,String BotConfig_fileId, String BotIcon_fileId)	
	{		
		return "{\n" + 
				"    \"botDefinition\": \""+BotDef_fileId+"\",\n" + 
				"    \"configInfo\": \""+BotConfig_fileId+"\",\n" + 
				"    \"icon\": \""+BotIcon_fileId+"\"\n" + 
				"}";

	}



	public static String enableSDKpayLoad(String BuilderApp_sdkClientId)	
	{		
		return "{\n" + 
				"    \"connectorEnabled\": false,\n" + 
				"    \"PIIMaskingDisabledForAgentTransfer\": false,\n" + 
				"    \"sdkClientId\": \""+BuilderApp_sdkClientId+"\",\n" + 
				"    \"sdkHostUri\": \"https://qa-app.kora.ai/api/1.1/ka/bothandler\",\n" + 
				"    \"subscribedFor\": [\n" + 
				"      \"onMessage\",\n" + 
				"      \"onHook\",\n" + 
				"      \"onEvent\",\n" + 
				"      \"onAlert\",\n" + 
				"      \"onVariableUpdate\"\n" + 
				"    ]\n" + 
				"  }";

	}

	public static String enableSDK_BotKitpayLoad(String clientId)	
	{		
		return "{\n" + 
				"    \"connectorEnabled\": false,\n" + 
				"    \"PIIMaskingDisabledForAgentTransfer\": false,\n" + 
				"    \"sdkClientId\": \""+clientId+"\",\n" + 
				"    \"sdkHostUri\": \"https://qa-app.kora.ai/api/1.1/ka/bothandler\",\n" + 
				"    \"subscribedFor\": [\n" + 
				"      \"onMessage\",\n" + 
				"      \"onHook\",\n" + 
				"      \"onEvent\",\n" + 
				"      \"onAlert\",\n" + 
				"      \"onVariableUpdate\"\n" + 
				"    ]\n" + 
				"  }";

	}

	public static String createbuilderAppnonAdminpayLoad(String streamId,String AccountID,String UserID)
	{		

		return "{\n" + 
				"    \"streamId\": \""+streamId+"\",\n" + 
				"    \"appName\": \"NonAdminBuilderapp"+TimeinHHMMSS+"\",\n" + 
				"    \"accountId\": \""+AccountID+"\",\n" + 
				"    \"userId\": \""+UserID+"\",\n" + 
				"    \"pns\": {\n" + 
				"      \"enable\": false,\n" + 
				"      \"webhookUrl\": \"\"\n" + 
				"    },\n" + 
				"    \"bots\": [],\n" + 
				"    \"state\": 0,\n" + 
				"    \"algorithm\": \"HS256\",\n" + 
				"    \"isAdminApp\": false,\n" + 
				"    \"__v\": 0,\n" + 
				"    \"scope\": [\n" + 
				"      {\n" + 
				"        \"scopes\": [\n" + 
				"          \"intent_entity:detection\",\n" + 
				"          \"chat_history:read\",\n" + 
				"          \"debug_logs:read\",\n" + 
				"          \"bot_definition:bot_export\",\n" + 
				"          \"bot_variables:import\",\n" + 
				"          \"bot_variables:export\",\n" + 
				"          \"test_and_train:ml\",\n" + 
				"          \"test_and_train:faq\",\n" + 
				"          \"test_and_train:ml_import\",\n" + 
				"          \"test_and_train:ml_export\",\n" + 
				"          \"logs:change_logs\",\n" + 
				"          \"bot_sessions:read\",\n" + 
				"          \"custom_dashboards:custom_dashboards\",\n" + 
				"          \"rcs_optin:optin_users\",\n" + 
				"          \"link_external_bots:link_external_bots\"\n" + 
				"        ],\n" + 
				"        \"bot\": \""+streamId+"\"\n" + 
				"      }\n" + 
				"    ]\n" + 
				"  }";

	}

	public static String enableRTMpayLoad(String streamId,String Appname,String clientId)	
	{		
		return "{\n" + 
				"    \"streamId\": \""+streamId+"\", \n" + 
				"    \"channelDetails\": {\n" + 
				"      \"type\": \"rtm\",\n" + 
				"      \"name\": \"Web / Mobile Client\",\n" + 
				"      \"displayName\": \"Web / Mobile Client\",\n" + 
				"      \"app\": {\n" + 
				"        \"appName\": \""+Appname+"\", \n" + 
				"        \"clientId\": \""+clientId+"\" \n" + 
				"      },\n" + 
				"      \"isAlertsEnabled\": false,\n" + 
				"      \"enable\": true,\n" + 
				"      \"sttEnabled\": false,\n" + 
				"      \"sttEngine\": \"kore\"\n" + 
				"    },\n" + 
				"    \"type\": \"rtm\"\n" + 
				"  }";

	}

	public static String enableWebHookpayLoad(String streamId,String Appname,String clientId)	
	{		
		return "{\n" + 
				"\"streamId\": \""+streamId+"\",\n" + 
				"\"channelDetails\": {\n" + 
				"\"app\": {\n" + 
				"\"appName\": \""+Appname+"\",\n" + 
				"\"clientId\": \""+clientId+"\"\n" + 
				"},\n" + 
				"\"createInstance\": false,\n" + 
				"\"displayName\": \"webhook\",\n" + 
				"\"enable\": true,\n" + 
				"\"isAsync\": false,\n" + 
				"\"type\": \"ivr\"\n" + 
				"},\n" + 
				"\"type\":\"ivr\"\n" + 
				"}";

	}
	public static String linkChildBotpayLoad(String childbotID,String BotName,String jwtToken)
	{
		return "{\n" + 
				"    \"childBotId\": \""+childbotID+"\",\n" + 
				"    \"childBotName\": \""+BotName+"\",\n" + 
				"    \"authToken\": \""+jwtToken+"\"\n" + 
				"  }";		
	}

	public static String linkChildBotBuilder_payLoad(String childbotID,String botName)
	{
		return "{   \n" + 
				"     \"botId\": \""+childbotID+"\",\n" + 
				"     \"linkedBotDetails\": {\n" + 
				"          \"_id\": \""+childbotID+"\",\n" + 
				"          \"displayName\": \""+botName+"\"\n" + 
				"     }\n" + 
				"}";		
	}

	public static Map<String,String> headersforcloneBotpayLoad(String jwtToken,String UserID,String AccountId)
	{
		HashMap<String, String> HeadersforcloneBotmap= new HashMap<String, String>();
		HeadersforcloneBotmap.put("auth",jwtToken);
		HeadersforcloneBotmap.put("userId",UserID);
		HeadersforcloneBotmap.put("AccountId",AccountId);
		HeadersforcloneBotmap.put("Content-Type","application/json");
		HeadersforcloneBotmap.put("bot-language", "en");		
		HeadersforcloneBotmap.put("User-Agent","KoreAssistant"); 						
		return HeadersforcloneBotmap;
	}

	public static String clonedBot_SetuppayLoad(String ClonnedBotName)
	{		 						
		return "{\n" + 
				"    \"variableUpdate\": true,\n" + 
				"    \"updateSet\": {\n" + 
				"      \"name\": \""+ClonnedBotName+"\",\n" + 
				"      \"color\": \"#009dab\",\n" + 
				"      \"state\": \"default\"\n" + 
				"    }\n" + 
				"  }";
	}

	/*
	 * As testing practice here Giving developer email address same everytime
	 */
	public static String addDeveloperpayLoad(String DevloperEmailAddress,String devRoleID, String devBotID)
	{		 						
		return "{\n" + 
				"  \"users\": [\n" + 
				"    {\n" + 
				"      \"userInfo\": {\n" + 
				"        \"emailId\": \""+DevloperEmailAddress+"\",\n" + 
				"        \"firstName\": \"RamanaR\",\n" + 
				"        \"lastName\": \"Kommula\"\n" + 
				"      },\n" + 
				"      \"isDeveloper\": true,\n" + 
				"      \"roles\": [\n" + 
				"        {\n" + 
				"          \"roleId\": \""+devRoleID+"\",\n" + 
				"          \"botId\": \""+devBotID+"\"\n" + 
				"        }\n" + 
				"      ]\n" + 
				"    }\n" + 
				"  ],\n" + 
				"  \"sendEmail\": false,\n" + 
				"  \"isAppUser\": true\n" + 
				"}";
	}

	public static String enableSlackpayLoad(String streamId,String botName,String AccessToken, String clientId,String clientSecret, String verificationToken)
	{
		return "{\n" + 
				"    \"streamId\": \""+streamId+"\",\n" + 
				"    \"channelDetails\": {\n" + 
				"      \"displayName\": \"slack\",\n" + 
				"      \"botName\": \""+botName+"\",\n" + 
				"      \"enable\": true,\n" + 
				"      \"type\": \"slack\",\n" + 
				"      \"accessToken\": \""+AccessToken+"\",\n" + 
				"      \"clientId\": \""+clientId+"\",\n" + 
				"      \"clientSecret\": \""+clientSecret+"\",\n" + 
				"      \"verificationToken\": \""+verificationToken+"\"\n" + 
				"    },\n" + 
				"    \"type\": \"slack\"\n" + 
				"  }";
	}

	public static String enableSAMLssopayLoad(String AccountId,String accountName)
	{
		return "{\n" + 
				"    \"accountId\": \""+AccountId+"\",\n" + 
				"    \"idpInfo\": {\n" + 
				"      \"method\": \"saml\",\n" + 
				"      \"providername\": \"others\",\n" + 
				"      \"config\": {\n" + 
				"        \"entryPoint\": \"https://qa-app.kora.ai/api/sso/provider/saml\",\n" + 
				"        \"issuer\": \"https://app.kora.ai/api/samlp/metadata\",\n" + 
				"        \"cert\": \"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\\nBVUXHCa0lVrj3sOwdwL8NSw=\"\n" + 
				"      },\n" + 
				"      \"allusers\": true\n" + 
				"    },\n" + 
				"    \"accountName\": \""+accountName+"\"\n" + 
				"  }";
	}

	public static String configuringENVvarpayLoad() 
	{
		return "{\n" + 
				"    \"variableUpdate\": true,\n" + 
				"    \"updateSet\": [\n" + 
				"      {\n" + 
				"        \"variableType\": \"env\",\n" + 
				"        \"scope\": \"askOnInstall\",\n" + 
				"        \"state\": \"configured\",\n" + 
				"        \"key\": \"KAServerBaseURL\",\n" + 
				"        \"value\": \"https://qa-app.kora.ai/api/1.1\",\n" + 
				"        \"hint\": \"Environment specific Kore Assistant server URL.\",\n" + 
				"        \"audioTag\": \"\",\n" + 
				"        \"group\": \"\"\n" + 
				"      }\n" + 
				"    ]\n" + 
				"  }";
	}


	public static String TriggerWebHookChannelPayload(String emailAddress,String message) 
	{
		return "{\n" + 
				"     \"from\": {\n" + 
				"          \"id\": \""+emailAddress+"\"\n" + 
				"     },\n" + 
				"     \"message\": {\n" + 
				"          \"text\": \""+message+"\"\n" + 
				"     }\n" + 
				"}";
	}
	public static String configuringENVvarforSearviceNowpayLoad() 
	{
		return "{\n" + 
				"  \"variableType\": \"env\",\n" + 
				"  \"authUrl\": \"https://dev80567.service-now.com/oauth_auth.do?state=123\",\n" + 
				"  \"clientId\": \"3ac77482ccd30010efc6d57a469f3117\",\n" + 
				"  \"secret\": \"Kore@123\",\n" + 
				"  \"tenant\": \"dev80567\",\n" + 
				"  \"tokenUrl\": \"https://dev80567.service-now.com/oauth_token.do\",\n" + 
				"  \"audioTag\": \"\",\n" + 
				"  \"group\": \"\"\n" + 
				"}";
	}

	public static String configureAgentTransferpayLoad( ) // Same as  enableSDKpayLoad
	{
		return "";
	}

	public static String publishbotPayload( )  
	{
		return "{\n" + 
				"    \"versionComment\": \"Created Sample bot\"\n" + 
				"  }";
	}

	/*
	 * Here Question will be static {{url}}/api/{{version}}/users/{{SanityUserID1}}/builder/faqs
	 */
	public static String addFAQpayLoad(String KGTaskID,String SanityBotStreamId,String KGParentID,String SanityBotName)  
	{
		return "{\n" + 
				"    \"questionPayload\": {\n" + 
				"        \"question\": \"How can i check the logs?\",\n" + 
				"        \"tagsPayload\": []\n" + 
				"    },\n" + 
				"    \"knowledgeTaskId\": \""+KGTaskID+"\",\n" + 
				"    \"subQuestions\": [],\n" + 
				"    \"responseType\": \"message\",\n" + 
				"    \"streamId\": \""+SanityBotStreamId+"\",\n" + 
				"    \"parent\": \""+KGParentID+"\",\n" + 
				"    \"leafterm\": \""+SanityBotName+"\",\n" + 
				"    \"answerPayload\": [\n" + 
				"        {\n" + 
				"            \"text\": \"You%20need%20to%20have%20admin%20access%20to%20verify%20the%20logs\",\n" + 
				"            \"type\": \"basic\",\n" + 
				"            \"channel\": \"default\"\n" + 
				"        }\n" + 
				"    ],\n" + 
				"    \"subAnswers\": []\n" + 
				"}";
	}


	public static String findIntentPayload(String FAQ)  
	{
		return "{\n" + 
				"    \"input\": \""+FAQ+"\",\n" + 
				"    \"streamName\": \"kc_sample_bot\",\n" + 
				"    \"expandAns\":true\n" + 
				"}";
	}

	public static String DeleteKCDataPayload(String KTID)  
	{
		return "{\n" + 
				"    \"ktId\": \""+KTID+"\",\n" + 
				"    \"all\": true\n" + 
				"  }";
	}


	/*
	 * oAuth Token
	 */
	public static String getAccessTokenPayload(String emailID)  
	{
		return "{\n" + 
				"    \"client_id\": \"1\",\n" + 
				"    \"client_secret\": \"1\",\n" + 
				"    \"scope\": \"1\",\n" + 
				"    \"grant_type\": \"password\",\n" + 
				"    \"username\": \""+emailID+"\",\n" + 
				"    \"password\": \""+Payloads.Adminpassword+"\"\n" + 
				"}";
	}

	public static String ExtractFAQsPayload()  
	{
		
		// https://seller.flipkart.com/sell-online/faq		
		// https://seller.flipkart.com/slp/faqs
		// https://www.axisbank.com/bank-smart/internet-banking/faqs
		// https://www.icicibank.com/nri-banking/money_transfer/faq/m2i-rewards-program/loyalty-program.page?\
		return "{\n" + 
				"     \"fileUrl\": \"https://www.icicibank.com/nri-banking/money_transfer/faq/m2i-rewards-program/loyalty-program.page?\",\n" + 
				"     \"name\": \"icicibank\"\n" + 
				"}";
		//https://www.axisbank.com/bank-smart/internet-banking/faqs
//		return"{\n" + 
//				"     \"fileUrl\": \"https://seller.flipkart.com/slp/faqs\",\n" + 
//				"     \"fileId\": \"\",\n" + 
//				"     \"fileName\": \"\",\n" + 
//				"     \"name\": \"Extract930\"\n" + 
//				"}";
	}

	public static String AddQstoCollectionPayload(String question,String answer,String knowledgeTaskId,String BotID,String qsId)
	{
		return "{\n" + 
				"     \"faqs\": [\n" + 
				"          {\n" + 
				"               \"questionPayload\": {\n" + 
				"                    \"question\": \""+question+"\"\n" + 
				"               },\n" + 
				"               \"answerPayload\": [\n" + 
				"                    {\n" + 
				"                         \"text\": \""+answer+"\",\n" + 
				"                         \"type\": \"basic\",\n" + 
				"                         \"channel\": \"default\"\n" + 
				"                    }\n" + 
				"               ],\n" + 
				"               \"knowledgeTaskId\": \""+knowledgeTaskId+"\",\n" + 
				"               \"subQuestions\": [],\n" + 
				"               \"responseType\": \"message\",\n" + 
				"               \"subAnswers\": [],\n" + 
				"               \"streamId\": \""+BotID+"\",\n" + 
				"               \"parent\": \"idPrefix21\",\n" + 
				"               \"leafterm\": \"NewCheck\",\n" + 
				"               \"qsId\": \""+qsId+"\"\n" + 
				"          }\n" + 
				"     ]\n" + 
				"}";
	}


}
