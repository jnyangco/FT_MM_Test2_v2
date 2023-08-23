package testcase;

import static io.restassured.RestAssured.*;
import java.io.FileWriter;
import java.util.List;
import org.json.simple.JSONObject;
import org.testng.Assert;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Test;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;

public class API_Test2 {
	
	private String endPoint = "v2/accounting/od/interest_cost_fund";
	private String filterAttribute = "?fields=record_date,fund_id,fund_desc,interest_paid_amt,cost_type_desc";
	private String filterDate = "&filter=record_date:eq:";
	private String limit = "&page[number]=1&page[size]=1000";
	
	private Response response;
	private JsonPath jsonPath = null;
	private List<String> list = null;
	private int recordIndex = 0;
	private int totalRecords = 0;
	
	private String date = "2023-06-30";
	
	@BeforeTest
	public void setupURL() {
		baseURI = "https://api.fiscaldata.treasury.gov/services/api/fiscal_service/";
		basePath = endPoint + filterAttribute + filterDate + date + limit;
	}
	
	@Test
	public void testAPI() {
		
		//Step 1. Send HTTP method Get Request
		sendGetRequest();
		
		
		//Step 2. Save response as JSON file in MM_API folder
		saveResponseAsJsonFile("response.json");
		
		
		//Step 3. Validate record_date for all records is 2023-06-30
		validateResponseRecordDateIsCorrect(date);
		
		
		//Step 4: Get the record with highest interestPaidAmt
		recordIndex = getRecordIndexHighestInterestPaidAmount();
		
		
		//Step 5: Print record with highest interestPaidAmt
		printRecord(recordIndex);
		
	}
	
	
	
	
	public void sendGetRequest() {
		try {
			response =  
					given().
					when().
						get(basePath).
					then().
						statusCode(200).
						contentType(ContentType.JSON).
						extract().response();
			
			jsonPath = new JsonPath(response.asString());	
		} catch (Exception e) {
			Assert.fail("Error in sending request.");
		}
	}
	
	
	public void saveResponseAsJsonFile(String filename) {
		try {
	        FileWriter writer = new FileWriter(System.getProperty("user.dir") +"\\MM_API\\" +filename);
	        writer.write(response.asString());
	        writer.close();
	        System.out.println("JSON response was successfully saved to MM_API folder.");
		} catch (Exception e) {
			Assert.fail("Unable to save JSON response.");
		}
	}
	
	
	public void validateResponseRecordDateIsCorrect(String date) {
		try {
			list = jsonPath.getList("data.record_date");
			for(int i=0; i<list.size(); i++) {
				Assert.assertEquals(list.get(i), date);
			}
		} catch (Exception e) {
			Assert.fail("Error in validating record date.");
		}
		
	}
	
	
	public int getRecordIndexHighestInterestPaidAmount() {
		float interestPaidAmt = 0;
		float highestInterestPaidAmt = 0;
		int index = 0;
		
		try {
			list = jsonPath.getList("data.interest_paid_amt");
			for(int i=0; i<list.size(); i++) {
				totalRecords = totalRecords + 1;
				try {
					interestPaidAmt = Float.valueOf(list.get(i));
					if(interestPaidAmt > highestInterestPaidAmt) {
						highestInterestPaidAmt = interestPaidAmt;
						index = i;
					}
				} catch (NumberFormatException e) {
					
				}
			}
		} catch (Exception e) {
			Assert.fail("Error in getting record with highestInterestPaidAmt");
		} 
		return index;
	}
	
	
	public void printRecord(int recordIndex) {
		try {
			JSONObject jsonObject = new JSONObject(jsonPath.getJsonObject("data["+recordIndex+"]"));
			System.out.println("\n**************************************************************************");
			System.out.println("Highest interest_paid_amt data out of " +totalRecords +" records:");
			System.out.println("{");
			System.out.println("\t\"record_date\": " +"\"" +jsonObject.get("record_date") +"\",");
			System.out.println("\t\"fund_id\": " +"\"" +jsonObject.get("fund_id") +"\",");
			System.out.println("\t\"fund_desc\": " +"\"" +jsonObject.get("fund_desc") +"\",");
			System.out.println("\t\"interest_paid_amt\": " +"\"" +jsonObject.get("interest_paid_amt") +"\",");
			System.out.println("\t\"cost_type_desc\": " +"\"" +jsonObject.get("cost_type_desc") +"\"");
			System.out.println("}");
			System.out.println("**************************************************************************\n");
		} catch (Exception e) {
			Assert.fail("Error in printing record.");
		}
	}
	
}
