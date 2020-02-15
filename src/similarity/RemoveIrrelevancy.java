package similarity;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import model.Article;
import model.Paragraph;
import model.Sentence;

public class RemoveIrrelevancy {
	public static Article removeIrrelevantParagraphs(Article article,
			String[] keywords) {
		Article newArticle = new Article();
		Set<String> set = new HashSet<String>();
		Pattern pattern = Pattern.compile("\\w+");
		for (String key : keywords) {
			set.add(key);
		}
		for (Paragraph para : article.getParagraphs()) {
			int count = 0;
			String paragraph = para.getString();
			Matcher matcher = pattern.matcher(paragraph);
			while (matcher.find()) {
				String word = matcher.group();
				if (set.contains(word)) {
					count++;
				}
			}
			if (count > 5) {
				newArticle.addParagraph(para);
			}
		}

		return newArticle;
	}

	public static Article removeIrrelevantLines(Article article,
			String[] keywords) {
		Article newArticle = new Article();
		Set<String> set = new HashSet<String>();
		Pattern pattern = Pattern.compile("\\w+");
		for (String key : keywords) {
			set.add(key);
		}
		for (Paragraph para : article.getParagraphs()) {
			int count = 0;
			Paragraph newParagraph = new Paragraph(para.getParagraphId());
			for (Sentence sentence : para.getSentences()) {
				Matcher matcher = pattern.matcher(sentence.getText());
				while (matcher.find()) {
					String word = matcher.group();
					if (set.contains(word)) {
						count++;
					}
				}
				if (count > 0) {
					newParagraph.addSentence(sentence);
				}
			}
			if (newParagraph.getSentences().size() > 0) {
				newArticle.addParagraph(newParagraph);
			}
		}

		return newArticle;
	}
}
