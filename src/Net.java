import java.io.File;
import java.io.IOException;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.util.Charsets;


public class Net {

	public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:19.0) Gecko/20100101 Firefox/19.0";

	/**
	 * Loads and returns the given page.  
	 * @return null if the request is not successful
	 * @throws IOException 
	 */
	public static String getPage(String page) throws IOException {
		NetHttpTransport nht = new NetHttpTransport();
		HttpRequestFactory hrf =  nht.createRequestFactory();
		GenericUrl url = new GenericUrl(page);
		HttpRequest req = hrf.buildGetRequest(url);
		req.getHeaders().setUserAgent(USER_AGENT); 
		req.setRetryOnExecuteIOException(true);
		HttpResponse hr = req.execute();
		if (hr.isSuccessStatusCode()) {
			String result = hr.parseAsString();
			System.out.println("We have success:");
			com.google.common.io.Files.write(result, new File("test.html"), Charsets.UTF_8);
			System.out.println(result);
			return result;
		} else {
			return null;
		}
	}
}
