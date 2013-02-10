import java.util.HashMap;

import com.google.gson.Gson;


public class JsonTest {

	public static void main(String[] args) {
		System.out.println("Hello me");
		
		Gson gson = new Gson(); 
		
//		ArrayList<DictResult> drlist = new ArrayList<DictResult>(10);
//		drlist.add(new DictResult(10, "1", "one"));
//		drlist.add(new DictResult(20, "2", "two"));
//		drlist.add(new DictResult(30, "3", "three"));
//		drlist.add(new DictResult(40, "4", "four"));
//		drlist.add(new DictResult(50, "5", "five"));
		
		
		HashMap<String,DictResult> drlist = new HashMap<String, DictResult>(10);
		drlist.put("1k", new DictResult(10, "1", "one"));
		drlist.put("2k", new DictResult(20, "2", "two"));
		drlist.put("3k", new DictResult(30, "3", "three"));
		drlist.put("4k", new DictResult(40, "4", "four"));
		drlist.put("5k", new DictResult(50, "5", "five"));
		
//		DictResult[] drarr = new DictResult[2]; 
//		drarr = drlist.toArray(d);
		
		 
		String result = gson.toJson(drlist);
		System.out.println(result);
		
		if (true) return;
		DictResult[]ans = gson.fromJson(result, DictResult[].class);
		for (DictResult i  : ans) {
			System.out.print(i);
			System.out.print("\n---\n");
		}
		
		
		
		
	}
	
}
