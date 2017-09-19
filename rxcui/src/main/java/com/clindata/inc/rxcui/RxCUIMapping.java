package com.clindata.inc.rxcui;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.opencsv.CSVReader;
import com.opencsv.CSVWriter;



public class RxCUIMapping {
	
	private final static String FILENAME = "/Drug_Master.csv";
	private final static String RESULT_FILE = "RxCUI.csv";
	private final static String QUERY_URL = "https://rxnav.nlm.nih.gov/REST/rxcui?"; 
	
	private static String getRxCUI(String type, String number) throws Exception {
		StringBuffer response = new StringBuffer();
		URL queryURL = new URL(QUERY_URL + "idtype=" + type + "&id=" + number);
		HttpURLConnection con  = (HttpURLConnection) queryURL.openConnection();
		con.setRequestMethod("GET");
		int responseCode = con.getResponseCode();
		if(responseCode == 200) {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(con.getInputStream());
			NodeList list = doc.getElementsByTagName("rxnormId");
			if(list.getLength() > 0) {
				response.append(list.item(0).getTextContent());				
			}
		} else {
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String line;
			while ((line = in.readLine()) != null) {
				response.append(line);
			}
			in.close();			
		}
		return response.toString();
	}
	

	public static void main(String[] args) {
		CSVReader reader = null;
		CSVWriter writer = null;
		try{
			InputStream is = RxCUIMapping.class.getResourceAsStream(FILENAME);			
			reader = new CSVReader(new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8)));
			String[] line;
			writer = new CSVWriter(new FileWriter(RESULT_FILE));
			while((line = reader.readNext()) != null) {
				String applType = line[7];
				String applNo = line[8];
				String queryType = "";
				if("A".equals(applType)) {
					queryType = "ANDA";
				} else if("N".equals(applType)) {
					queryType = "NDA";
				}
				line[13] = getRxCUI(queryType, queryType+applNo);
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
