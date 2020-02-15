package summarizer;

import java.io.IOException;
import java.io.StringReader;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import keywordExtractor.KeyWordExtractor;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter;
import org.apache.lucene.analysis.shingle.ShingleFilter;
import org.apache.lucene.analysis.standard.ClassicFilter;
import org.apache.lucene.analysis.standard.ClassicTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;

public class TextExtractor {
	private static Version LUCENE_VERSION = Version.LUCENE_48;
	private String text;

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}

	public String[] extractTerms() {
		List<String> words = new ArrayList<String>();
		String input = getText();
		input = input.replaceAll("-+", "-0");
		input = input.replaceAll("[\\p{Punct}&&[^'-]]+", " ");
		input = input.replaceAll("(?:'(?:[tdsm]|[vr]e|ll))+\\b", "");

		TokenStream tokenStream = new ClassicTokenizer(LUCENE_VERSION,
				new StringReader(input));
		tokenStream = new LowerCaseFilter(LUCENE_VERSION, tokenStream);
		tokenStream = new ClassicFilter(tokenStream);
		tokenStream = new ASCIIFoldingFilter(tokenStream);
		tokenStream = new StopFilter(LUCENE_VERSION, tokenStream,
				EnglishAnalyzer.getDefaultStopSet());
		tokenStream = new ShingleFilter(tokenStream, 2, 2);
		CharTermAttribute token = tokenStream
				.getAttribute(CharTermAttribute.class);
		try {
			tokenStream.reset();
			while (tokenStream.incrementToken()) {
				String term = token.toString();
				String stem = KeyWordExtractor.stemmize(term);
				if (stem != null && Stopwords.isStopword(stem))
					continue;
				if (stem != null) {
					stem = stem.replaceAll("_", "").trim().replaceAll("-0", "-");
					if (!stem.isEmpty()) {
						words.add(stem);
					}
				}
			}

			tokenStream.end();
			tokenStream.close();
		} catch (IOException e) {
			e.printStackTrace();
		}


		BreakIterator boundary = BreakIterator.getWordInstance(Locale.US);
		boundary.setText(getText());

		return words.toArray(new String[words.size()]);
	}

	// public String[] extractTerms() {
	// BreakIterator boundary = BreakIterator.getWordInstance(Locale.US);
	// boundary.setText(getText());
	//
	// List<String> words = new ArrayList<String>();
	// int start = boundary.first();
	// int end = boundary.next();
	// while (end != BreakIterator.DONE) {
	// String word = getText().substring(start, end).trim();
	// if (!word.isEmpty()) {
	// words.add(word);
	// }
	// start = end;
	// end = boundary.next();
	// }
	//
	// return words.toArray(new String[words.size()]);
	// }

	public String[] extractSentences() {
		BreakIterator boundary = BreakIterator.getSentenceInstance(Locale.US);
		boundary.setText(getText());

		List<String> sentences = new ArrayList<String>();
		int start = boundary.first();
		int end = boundary.next();
		while (end != BreakIterator.DONE) {
			String sentence = getText().substring(start, end).trim();
			if (!sentence.isEmpty()) {
				sentences.add(sentence);
			}
			start = end;
			end = boundary.next();
		}
		return sentences.toArray(new String[sentences.size()]);
	}
}
