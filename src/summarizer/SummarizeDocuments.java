package summarizer;

import java.util.ArrayList;
import java.util.HashSet;

import model.Article;
import model.Paragraph;
import model.Sentence;

public class SummarizeDocuments {

	public static Article summarize(Article article, String[] keywords) {
		ArrayList<Paragraph> input = article.getParagraphs();
		ArrayList<Paragraph> output = new ArrayList<Paragraph>();
		for (Paragraph paragraph : input) {
			Paragraph newParagraph = new Paragraph(paragraph.getParagraphId());
			if (paragraph.getSentences().size() > 0) {
				ArrayList<Sentence> summarizedContent = summarizer.Main
						.getSummary(paragraph, keywords);
				HashSet<Integer> lines = new HashSet<Integer>();
				for (Sentence sent : summarizedContent) {
					lines.add(sent.getLineNo());
				}
				for (Sentence out : paragraph.getSentences()) {
					if (lines.contains(out.getLineNo())) {
						newParagraph.addSentence(out);
					}
				}
				if (newParagraph.getSentences().size() > 0) {
					output.add(newParagraph);
				}
			}
		}

		Article summarizedArticle = new Article(output);

		return summarizedArticle;
	}
}
