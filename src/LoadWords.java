import java.io.File;
import com.google.gson.Gson;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;


public class LoadWords {

	public static final String LINE_SEP = "\n= = = = =\n";
	public static final String WORD_SEP = "^^^^";
	public static final int JOB_SIZE = 2271;
	public static final int SLEEP_TIME = 2 * 1000;
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		if (args.length != 1 || Integer.parseInt(args[0]) < 1) {
			System.out.println("Please enter only 1 postive number argumnet" +
								"to append to the word_goog_json file");
			System.exit(-1);
		} 
		final int JOB_NO = Integer.parseInt(args[0]); 
		// final int JOB_NO = 1;
		ArrayList<String> words = getList();
		System.out.println("Google Dictionary Loader" + words.size());
		File f = new File(String.format("words_goog_json_%d.txt", JOB_NO));
		PrintWriter pw = new PrintWriter(f);
		HashMap<String, DictResult> hm = new HashMap<String, DictResult>(46000);
		
		for (int i = (JOB_NO - 1) * JOB_SIZE, c = 0; i < JOB_SIZE; ++i, ++c) {
			
			Thread.sleep(SLEEP_TIME);
			String s = words.get(i);
			System.out.print(
					String.format("Iteration %d of %d ...", c, JOB_SIZE));
			System.out.println("Saving word: " + s);
			String data = null;
			try {
				data = Dict.getHtml(s);
			} catch (Exception e) {
				System.out.println("Error -- skipping " + s);
				continue;
			}
			
			// Make Dict Result
			DictResult dr = new DictResult(i, s, data);
			hm.put(s, dr);
			
			// Get JSON
			Gson gson = new Gson();
			String json = gson.toJson(hm);
			
			// Write it out to disk
			pw.print(json);
			pw.flush(); 
		}
		
		pw.close();
		System.out.println("data loading complete");
	}
	
	
	private static ArrayList<String> getList() throws FileNotFoundException {
		File f = new File("words.txt");
		Scanner in = new Scanner(f); 
		ArrayList<String> result = new ArrayList<String>(45403);
		while (in.hasNextLine()) {
			String val = in.nextLine();
			result.add(val);
		}
		return result;
	}
	

}
