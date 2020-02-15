package summarizer;

import java.util.ArrayList;

import model.Paragraph;
import model.Sentence;

public class Main {

	public static ArrayList<Sentence> getSummary(Paragraph cluster,
			String[] keywords) {
		ArrayList<Sentence> result = null;

		Generator generator = new Generator();

		generator.loadText(cluster);
		generator.setKeywords(keywords);
		result = generator.generateSummary();

		return result;
	}
}