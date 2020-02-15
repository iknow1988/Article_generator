package model;

import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Locale;

public class Article {
	private ArrayList<Paragraph> paragraphs;

	@SuppressWarnings("unchecked")
	public <T> Article(ArrayList<T> list) {
		Object obj = null;
		for (Object o : list) {
			obj = o;
			break;
		}
		if (obj instanceof model.Paragraph) {
			this.paragraphs = (ArrayList<Paragraph>) list;
		} else if (obj instanceof model.Link) {
			int lineNo = 0;
			paragraphs = new ArrayList<Paragraph>();
			ArrayList<Link> webLinks = (ArrayList<Link>) list;
			for (int i = 0; i < webLinks.size(); i++) {
				Link link = webLinks.get(i);
				for (int j = 0; j < link.getParagraphs().size(); j++) {
					String linkParagraph = link.getParagraphs().get(j);
					Paragraph articleParagraph = new Paragraph(link.getRank()
							+ j);
					String text = linkParagraph;
					BreakIterator iterator = BreakIterator
							.getSentenceInstance(Locale.US);
					String source = text;
					iterator.setText(source);
					int start = iterator.first();
					for (int end = iterator.next(); end != BreakIterator.DONE; start = end, end = iterator
							.next()) {
						String line = source.substring(start, end);
						if (line.length() > 4) {
							articleParagraph.addSentence(new Sentence(link
									.getId(), new StringBuilder(line), link
									.getRank(), lineNo++));
						}
					}
					articleParagraph.setRank(link.getRank());
					articleParagraph.setLinkId(link.getId());
					if (paragraphs.size() < 200) {
						if (articleParagraph.getSentences().size() > 0) {
							paragraphs.add(articleParagraph);
						}
					}
				}
			}
			System.out.println("Crawled Paragraph : " + paragraphs.size());
		}
	}

	public Article() {
		paragraphs = new ArrayList<Paragraph>();
	}

	public ArrayList<Paragraph> getParagraphs() {
		return this.paragraphs;
	}

	public void addParagraph(Paragraph p) {
		this.paragraphs.add(p);
	}
}
