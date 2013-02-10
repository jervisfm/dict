
public class DictResult {

	private int id; 
	private String word;
	private String html;
	
	public DictResult(int id, String word, String html) {
		this.id = id;
		this.word = word;
		this.html = html;
	}
	
	public void setID(int id) { this.id = id; }
	public void setWord(String word) { this.word = word; }
	public void setHtml(String html) { this.html = html; }
	
	public int getID() { return id; }
	public String getWord() { return word; }
	public String getHtml() { return html; }
	
	@Override
	public String toString() {
		// TODO Auto-generated method stub
		String msg_format = "id: %d\n" +
							"word: %s\n" +
							"html: \n%s";
		return String.format(msg_format, id, word, html);
	}
	
}
