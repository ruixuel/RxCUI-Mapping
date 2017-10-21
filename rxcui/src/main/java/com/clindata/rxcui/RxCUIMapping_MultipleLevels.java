package com.clindata.rxcui;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

//import org.json.JSONArray;
import org.json.*;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;



public class RxCUIMapping_MultipleLevels {

	private final static String FILENAME = "/DrugMaster_wQueries_Levels.csv";
	private final static String RESULT_FILE = "RxCUI_levels.csv";
	private final static String QUERY_URL = "https://rxnav.nlm.nih.gov/REST/rxcui.json?";
	private static Map<String, String> drugIDMap = new HashMap<String, String>();

	//	private static String getRxCUI(String type, String number) throws Exception {
	//		StringBuffer response = new StringBuffer();
	//		URL queryURL = new URL(QUERY_URL + "idtype=" + type + "&id=" + number+"&allsrc=1");
	//		HttpURLConnection con  = (HttpURLConnection) queryURL.openConnection();
	//		con.setRequestMethod("GET");
	//		int responseCode = con.getResponseCode();
	//		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
	//		String line;
	//		while ((line = in.readLine()) != null) {
	//			response.append(line);
	//		}
	//		in.close();
	//		String rxcui = "";
	//		if(responseCode == 200) {
	//			JSONObject jo = new JSONObject(response.toString());
	//			if(jo.has("idGroup")) {
	//				JSONObject idJo = jo.getJSONObject("idGroup");
	//				if(idJo.has("rxnormId")) {
	//					rxcui = idJo.getJSONArray("rxnormId").toString();
	//					return rxcui.substring(1, rxcui.length()-1);
	//				}	
	//			}
	//		}
	//		return rxcui;
	//	}

	private static String findRxCUIByName(String name) throws Exception {
		StringBuffer response = new StringBuffer();
		String urlEncoding = QUERY_URL + "name="+URLEncoder.encode(name, "UTF-8")+"&allsrc=1&search=2";
		URL queryURL = new URL(urlEncoding);
		HttpURLConnection con  = (HttpURLConnection) queryURL.openConnection();
		con.setRequestMethod("GET");
		int responseCode = con.getResponseCode();
		BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
		String line;
		while ((line = in.readLine()) != null) {
			response.append(line);
		}
		in.close();
		String rxcui = "";
		if(responseCode == 200) {
			JSONObject jo = new JSONObject(response.toString());
			if(jo.has("idGroup")) {
				JSONObject idJo = jo.getJSONObject("idGroup");
				if(idJo.has("rxnormId")) {
					JSONArray ja = idJo.getJSONArray("rxnormId");
					if(ja.length() == 1) {
						rxcui = ja.getString(0);
					} else {
						for(int i = 0; i < ja.length(); i++) {
							rxcui += ja.getString(i) + ";";
						}						
					}
				}	
			}
		}
		if(!"".equals(rxcui)){
		drugIDMap.put(name, rxcui);}
		return rxcui;
	}

	//	private static String findRxCUIByApplCode(String[] line) throws Exception {
	//		String applType = line[7];
	//		String applNo = line[8];
	//		String queryType = "";
	//		if("A".equals(applType)) {
	//			queryType = "ANDA";
	//		} else if("N".equals(applType)) {
	//			queryType = "NDA";
	//		}
	//		return getRxCUI(queryType, queryType+applNo);
	//	}

	public static void main(String[] args) {
		CSVReader reader = null;
		CSVWriter writer = null;
		try{
			InputStream is = RxCUIMapping.class.getResourceAsStream(FILENAME);
			reader = new CSVReader(new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8)));
			String[] line;
			writer = new CSVWriter(new FileWriter(RESULT_FILE));
			int i = 0;
			while((line = reader.readNext()) != null) {
				if(i%100==0){
					System.out.println(i);
				}
				i++;
				//System.out.println(line[20]);
				//				String rxcui = findRxCUIByApplCode(line);
				String queryString = line[16];
				if(drugIDMap.containsKey(queryString)) {
					line[20] = drugIDMap.get(queryString);
				} else {
					// Ingredient + Strength + Dose
					if(drugIDMap.containsKey(queryString)) {
						line[20] = drugIDMap.get(queryString);
					}else{
						String rxcui = findRxCUIByName(line[16]);
						if(!"".equals(rxcui)) {
							line[20] = rxcui;
						}else{
							// Ingredient + Strength
							queryString = line[17];
							if(drugIDMap.containsKey(queryString)) {
								line[20] = drugIDMap.get(queryString);
							}else{
								rxcui = findRxCUIByName(line[17]);
								if(!"".equals(rxcui)) {
									line[20] = rxcui;
								}else{
									//Ingredient
									queryString = line[18];
									if(drugIDMap.containsKey(queryString)) {
										line[20] = drugIDMap.get(queryString);
									}else{
										rxcui = findRxCUIByName(line[18]);
										if(!"".equals(rxcui)) {
											line[20] = rxcui;
										}else{
											//Trade Name
											queryString = line[19];
											if(drugIDMap.containsKey(queryString)) {
												line[20] = drugIDMap.get(queryString);
											}else{
												rxcui = findRxCUIByName(line[19]);
												if(!"".equals(rxcui)) {
													line[20] = rxcui;
												}else{
													line[20] = "";
												}
											}
										}
									}
								}

							}					
						}
					}
				}
				writer.writeNext(line);
			}
		}catch(Exception e) {
			e.printStackTrace();
		}finally {
			if(reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if(writer != null) {
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

	}

}
