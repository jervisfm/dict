import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.util.Charsets;

public class Dict {

	public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:19.0) Gecko/20100101 Firefox/19.0"; 
	public static final String URL_STRING_TEMPLATE = "http://www.google.com/search?q=define:%s";
	
	public static void main(String[] args) throws Exception {
		t();
		
	}
	
	public static String getURL(String word) {
		return String.format(URL_STRING_TEMPLATE, word);
	}
	
	/**
	 * Returns HTML contain the defintion of the given word
	 * @param word english word to lookup
	 * @return
	 * @throws IOException if an error occurs
	 */
	public static String getHtml(String word) throws IOException {
		
		String url = getURL(word);
		String data = Net.getPage(url); 
		
		/*
		 * Data we want is in a "dict" class in a "ol" tag
		 */
		Document doc = Jsoup.parse(data);
		Elements elms = doc.getElementsByClass("dict");
		ArrayList<Element> defs = new ArrayList<Element>();
		StringBuffer sb = new StringBuffer();

		for (int i = 0; i < elms.size(); ++i) {
			Element e = elms.get(i);
			if (e.tagName().equalsIgnoreCase("ol")) {
				defs.add(e);
				sb.append(e.html());
				sb.append("\n");
			}
		}
		return sb.toString();
	}
	
	
	// test method
	private static void t () throws Exception {
		String url_string = "http://www.google.com/search?q=define:you";
		NetHttpTransport nht = new NetHttpTransport();
		
		HttpRequestFactory hrf =  nht.createRequestFactory();
		GenericUrl url = new GenericUrl(url_string);
		System.out.println("Not yet implemented7");
		HttpRequest req = hrf.buildGetRequest(url);
		
		System.out.println("Getting URL: "+ req.getUrl());
		HttpHeaders old_hh = req.getHeaders(); 
		old_hh.setUserAgent(USER_AGENT);
		
		if (req.getHeaders() == old_hh)
			System.out.println("HEADER CHANGED SUCCESS");
		else 
			System.out.println("HEADER FAILURE!!!!");
		
		HttpResponse hr = req.execute();
		System.out.println("Not yet implemented-last");
		if (hr.isSuccessStatusCode()) {
			String result = hr.parseAsString();
			System.out.println("We have success:");
			com.google.common.io.Files.write(result, new File("test.html"), Charsets.UTF_8);
			
			Document doc = Jsoup.parse(result);
			Elements elms = doc.getElementsByClass("dict");
			ArrayList<Element> defs = new ArrayList<Element>();
			for (int i = 0; i < elms.size(); ++i) {
				Element e = elms.get(i);
				if (e.tagName().equalsIgnoreCase("ol")) {
					defs.add(e);
				}
			}
			
			// Print Result.
			int counter = 0;
			for (Element e : defs) {
				System.out.println(++counter);
				System.out.println(e.html());
				System.out.println("");
			}
			
			// System.out.println(result);
			
		} else {
			System.out.println("GET Request Failed");
		
		};
	}
	
	/**
	 * Returns the text content contained in the word defintion
	 * retrieved from google
	 * @param word word to lookup
	 * @return
	 * @throws IOException if an error occurs (e.g. network failure)
	 */
	public static String lookup(String word) throws IOException {
		
		String url = getURL(word);
		String data = Net.getPage(url); 
		
		/*
		 * Data we want is in a "dict" class in a "ol" tag
		 */
		Document doc = Jsoup.parse(data);
		Elements elms = doc.getElementsByClass("dict");
		ArrayList<Element> defs = new ArrayList<Element>();
		StringBuffer sb = new StringBuffer();

		for (int i = 0; i < elms.size(); ++i) {
			Element e = elms.get(i);
			if (e.tagName().equalsIgnoreCase("ol")) {
				defs.add(e);
				sb.append(e.text());
				sb.append("\n");
			}
		}
		return sb.toString();
	}
}
