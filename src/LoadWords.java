import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;


public class LoadWords {

	public static final String LINE_SEP = "\n= = = = =\n";
	public static final String WORD_SEP = "^^^^";
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		ArrayList<String> words = getList();
		System.out.println("Google Dictionary Loader" + words.size());
		int i = 0; 
		File f = new File("words_goog.txt");
		PrintWriter pw = new PrintWriter(f);
		for (String s : words) {
			Thread.sleep(30 * 1000);
			System.out.print("Iteration " + i + " ... ");
			System.out.println("Saving word: " + s);
			String data = null;
			try { 
				data = Dict.getHtml(s);
			} catch (Exception e) {
				System.out.println("Errorr -- skipping " + s);
				continue;
			}
					
			
			String fmtData = String.format("\n%d)%s %s %s\n", i,s, WORD_SEP, data);
			// appennd to file 
			
			pw.append(fmtData);		
			pw.append(LINE_SEP);
			pw.flush();
			
			++i;
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
