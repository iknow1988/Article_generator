package keywordExtractor;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.LowerCaseFilter;
import org.apache.lucene.analysis.core.StopFilter;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.en.KStemFilter;
import org.apache.lucene.analysis.miscellaneous.ASCIIFoldingFilter;
import org.apache.lucene.analysis.shingle.ShingleFilter;
import org.apache.lucene.analysis.standard.ClassicFilter;
import org.apache.lucene.analysis.standard.ClassicTokenizer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.Version;

import summarizer.Stopwords;

public class KeyWordExtractor {

	private static Version LUCENE_VERSION = Version.LUCENE_48;

	public static class Keyword implements Comparable<Keyword> {

		private String stem;
		private Integer frequency;
		private Set<String> terms;

		/**
		 * Unique constructor.
		 * 
		 * @param stem
		 *            The unique stem this instance must hold.
		 */
		public Keyword(String stem) {
			this.stem = stem;
			terms = new HashSet<String>();
			frequency = 0;
		}

		/**
		 * Add a found corresponding term for this stem. If this term has been
		 * already found, it won't be duplicated but the stem frequency will
		 * still be incremented.
		 * 
		 * @param term
		 *            The term to add.
		 */
		private void add(String term) {
			terms.add(term);
			frequency++;
		}

		/**
		 * Gets the unique stem of this instance.
		 * 
		 * @return The unique stem.
		 */
		public String getStem() {
			return stem;
		}

		/**
		 * Gets the frequency of this stem.
		 * 
		 * @return The frequency.
		 */
		public Integer getFrequency() {
			return frequency;
		}

		/**
		 * Gets the list of found corresponding terms for this stem.
		 * 
		 * @return The list of found corresponding terms.
		 */
		public Set<String> getTerms() {
			return terms;
		}

		/**
		 * Used to reverse sort a list of keywords based on their frequency
		 * (from the most frequent keyword to the least frequent one).
		 */
		@Override
		public int compareTo(Keyword o) {
			return o.frequency.compareTo(frequency);
		}

		/**
		 * Used to keep unicity between two keywords: only their respective
		 * stems are taken into account.
		 */
		@Override
		public boolean equals(Object obj) {
			return obj instanceof Keyword && obj.hashCode() == hashCode();
		}

		/**
		 * Used to keep unicity between two keywords: only their respective
		 * stems are taken into account.
		 */
		@Override
		public int hashCode() {
			return Arrays.hashCode(new Object[] { stem });
		}

		/**
		 * User-readable representation of a keyword: "[stem] x[frequency]".
		 */
		@Override
		public String toString() {
			return stem + " x" + frequency;
		}

	}

	/**
	 * Stemmize the given term.
	 * 
	 * @param term
	 *            The term to stem.
	 * @return The stem of the given term.
	 * @throws IOException
	 *             If an I/O error occured.
	 */
	public static String stemmize(String term) throws IOException {

		// tokenize term
		TokenStream tokenStream = new ClassicTokenizer(LUCENE_VERSION,
				new StringReader(term));
		// stemmize
		// tokenStream = new PorterStemFilter(tokenStream);
		tokenStream = new KStemFilter(tokenStream);

		ArrayList<String> stems = new ArrayList<String>();
		CharTermAttribute token = tokenStream
				.getAttribute(CharTermAttribute.class);
		// for each token
		tokenStream.reset();
		while (tokenStream.incrementToken()) {
			stems.add(token.toString());
		}

		String output = new String();
		for (String stem : stems) {
			if (stem != null) {
				if (stem.matches("[\\w-]+")) {
					if (output.length() > 0) {
						output += " " + stem;
					} else {
						output = stem;
					}
				}
			}
		}

		tokenStream.close();
		return output;
	}

	/**
	 * Tries to find the given example within the given collection. If it hasn't
	 * been found, the example is automatically added in the collection and is
	 * then returned.
	 * 
	 * @param collection
	 *            The collection to search into.
	 * @param example
	 *            The example to search.
	 * @return The existing element if it has been found, the given example
	 *         otherwise.
	 */
	private static <T> T find(Collection<T> collection, T example) {
		for (T element : collection) {
			if (element.equals(example)) {
				return element;
			}
		}
		collection.add(example);
		return example;
	}

	/**
	 * Guesses keywords from given input string.
	 * 
	 * @param input
	 *            The input string.
	 * @return A set of potential keywords. The first keyword is the most
	 *         frequent one, the last the least frequent.
	 * @throws IOException
	 *             If an I/O error occured.
	 */
	public static String[] guessFromString(String input, int num)
			throws IOException {

		// hack to keep dashed words (e.g. "non-specific" rather than "non" and
		// "specific")
		input = input.replaceAll("-+", "-0");
		// replace any punctuation char but dashes and apostrophes and by a
		// space
		input = input.replaceAll("[\\p{Punct}&&[^'-]]+", " ");
		// replace most common English contractions
		input = input.replaceAll("(?:'(?:[tdsm]|[vr]e|ll))+\\b", "");

		// tokenize input

		TokenStream tokenStream = new ClassicTokenizer(LUCENE_VERSION,
				new StringReader(input));
		// to lower case
		tokenStream = new LowerCaseFilter(LUCENE_VERSION, tokenStream);
		// remove dots from acronyms (and "'s" but already done manually above)
		tokenStream = new ClassicFilter(tokenStream);
		// convert any char to ASCII
		tokenStream = new ASCIIFoldingFilter(tokenStream);
		// remove english stop words
		tokenStream = new StopFilter(LUCENE_VERSION, tokenStream,
				EnglishAnalyzer.getDefaultStopSet());
		tokenStream = new ShingleFilter(tokenStream, 2, 2);
		List<Keyword> keywords = new LinkedList<Keyword>();
		CharTermAttribute token = tokenStream
				.getAttribute(CharTermAttribute.class);

		// for each token
		tokenStream.reset();
		while (tokenStream.incrementToken()) {
			String term = token.toString();
			// stemmize
			String stem = stemmize(term);
			if (stem != null && Stopwords.isStopword(stem))
				continue;
			if (stem != null) {
				// create the keyword or get the existing one if any
				Keyword keyword = find(
						keywords,
						new Keyword(stem.replaceAll("_", "").trim()
								.replaceAll("-0", "-")));
				// add its corresponding initial token
				keyword.add(term.replaceAll("-0", "-"));
			}
		}

		tokenStream.end();
		tokenStream.close();

		// reverse sort by frequency
		Collections.sort(keywords);
		String[] returnvalue = new String[num];
		for (int i = 0; i < num; i++) {
			returnvalue[i] = keywords.get(i).getStem();
		}

		return returnvalue;
	}
}
