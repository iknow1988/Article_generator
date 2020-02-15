package summarizer;

import java.util.ArrayList;

import model.Paragraph;
import model.Sentence;

public class InputDocument {

	protected String name;
	protected String content;
	private ArrayList<Sentence> ouputText;

	public void loadText(String text) {
		setContent(text);
	}

	public void loadText(Paragraph cluster) {
		this.ouputText = cluster.getSentences();
		StringBuilder text = new StringBuilder();
		for (Sentence out : cluster.getSentences()) {
			text.append(out.getText());
		}
		setContent(text.toString());
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String[] getAllTerms() {
		return createTextExtractor().extractTerms();
	}

	public String[] getAllSentences() {
		return createTextExtractor().extractSentences();
	}

	public ArrayList<Sentence> getAllNewSentences() {
		return this.ouputText;
	}

	private TextExtractor createTextExtractor() {
		TextExtractor b = new TextExtractor();
		b.setText(getContent());
		return b;
	}

}
