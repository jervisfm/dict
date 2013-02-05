import static org.junit.Assert.*;

import java.io.IOException;

import org.junit.Test;

import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;


public class DictTest {

	
	
	@Test
	public void getURL() {
		String t1 = "hello";
		String t2 = "hello world";
		//expected
		String base_url = "http://www.google.com/search?q=define:";
		String e1 = base_url + t1;
		String e2 = base_url + t2;
		
		// run function
		String a1 = Dict.getURL(t1);
		String a2 = Dict.getURL(t2);
		
		// check results
		assertEquals(e1, a1);
		assertEquals(e2, a2);
	}
}
