package summarizer;

import java.util.HashMap;
import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Map;

import model.Paragraph;
import model.Sentence;

public class Generator {
	private InputDocument inputDoc;
	private double compressRatio = 0.5;
	private String[] keywords = null;
	private TermCollection termCollection;

	public void loadText(String text) {
		inputDoc = new InputDocument();
		inputDoc.loadText(text);
	}

	public void loadText(Paragraph cluster) {
		inputDoc = new InputDocument();
		inputDoc.loadText(cluster);
	}

	public void setKeywords(String[] keywords) {

		List<String> processedTermList = new ArrayList<String>();
		TermPreprocessor tp = new TermPreprocessor();

		String resultTerm = null;
		for (String term : keywords) {
			resultTerm = tp.preprocess(term);

			if (resultTerm != null)
				processedTermList.add(resultTerm);
		}

		this.keywords = processedTermList.toArray(new String[processedTermList
				.size()]);

	}

	public ArrayList<Sentence> generateSummary() {
		return generateSignificantSentences();
	}

	public ArrayList<Sentence> generateSignificantSentences() {
		ArrayList<Sentence> allNewSentences = getAllNewSentences();
		double[] scores = calcAllSentenceScores();

		ArrayList<Sentence> significantNewSentences = new ArrayList<Sentence>();
		double senThreshold = calcSentenceThreshold();

		for (int i = 0; i < allNewSentences.size(); i++) {
			if (scores[i] >= senThreshold) {
				significantNewSentences.add(allNewSentences.get(i));
			}
		}
		return significantNewSentences;
	}

	public double calcSentenceThreshold() {
		ArrayList<Sentence> allNewSentences = getAllNewSentences();
		float flen = Math.round(allNewSentences.size() * compressRatio);
		int summaryLength = Math.round(flen);

		double[] scores = calcAllSentenceScores();

		Arrays.sort(scores);
		if (summaryLength != 0)
			return scores[scores.length - summaryLength];
		else
			return scores[scores.length - 1];
	}

	public String[] getAllSentences() {
		return inputDoc.getAllSentences();
	}

	public ArrayList<Sentence> getAllNewSentences() {
		return inputDoc.getAllNewSentences();
	}

	public String[] generateSignificantTerms() {

		if (termCollection == null) {
			TermCollectionProcessor tcp = new TermCollectionProcessor();
			tcp.insertAllTerms(inputDoc.getAllTerms());
			termCollection = tcp.getTermCollection();
		}
		int test[] = termCollection.getFrequencyValues();
		HashMap<Integer, Integer> map = new HashMap<Integer, Integer>();
		for (int i = 0; i < test.length; i++) {
			if (map.containsKey(test[i])) {
				map.put(test[i], map.get(test[i]) + 1);
			} else {
				map.put(test[i], 1);
			}
		}
		float avg = 0;
		for (Map.Entry<Integer, Integer> entry : map.entrySet()) {
			avg += entry.getKey() * entry.getValue();
		}
		avg = avg / test.length;
		int termThreshold = (int) Math.ceil(avg);
		List<String> sigTerms = new ArrayList<String>();
		for (Word term : termCollection) {
			if (term.getFrequency() >= termThreshold) {
				if (!Stopwords.isStopword(term.getValue()))
					sigTerms.add(term.getValue());
			} else
				break;
		}

		return sigTerms.toArray(new String[sigTerms.size()]);
	}

	public double[] calcAllSentenceScores() {
		ArrayList<Sentence> allNewSentences = getAllNewSentences();
		double[] scoresNew = new double[allNewSentences.size()];
		String[] significantTerms = generateSignificantTerms();
		for (int i = 0; i < allNewSentences.size(); i++) {
			scoresNew[i] = calcSentenceScore(significantTerms, allNewSentences
					.get(i).getText());
		}
		return scoresNew;
	}

	public double calcSentenceScore(String[] significantTerms, String sentence) {
		TextExtractor extractor = new TextExtractor();
		extractor.setText(sentence);
		String[] originalTerms = extractor.extractTerms();
		String[] processedTerms;
		List<String> processedTermList = new ArrayList<String>();

		TermPreprocessor tp = new TermPreprocessor();
		String resultTerm = null;
		for (String term : originalTerms) {
			resultTerm = tp.preprocess(term);

			if (resultTerm != null)
				processedTermList.add(resultTerm);
		}

		processedTerms = processedTermList.toArray(new String[processedTermList
				.size()]);

		StringBuilder builder = new StringBuilder();
		for (String term : processedTerms) {
			if (significantTerms != null && term != null) {
				if (contains(term, keywords)) {
					builder.append('2');
				} else if (contains(term, significantTerms)) {
					builder.append('1');
				} else {
					builder.append('0');
				}
			}
		}

		String symbolicSentence = builder.toString();

		return calcSymbolicSentenceScore(symbolicSentence);

	}

	private boolean contains(String str1, String[] strs) {
		if (strs == null)
			return false;

		for (String st : strs) {
			if (st.equals(str1)) {
				return true;
			}
		}

		return false;
	}

	private double calcSymbolicSentenceScore(String sentence) {
		String[] splits = sentence.split("000"); // block distance is 3

		for (int i = 0; i < splits.length; i++) {
			splits[i] = StringTrimmer.trim(splits[i], '0');
		}

		double score = 0;
		double prevScore = 0;
		for (String split : splits) {
			if (split.length() != 0) {
				int sigNum = StringTrimmer.count(split, '1');
				int keywordNum = StringTrimmer.count(split, '2');

				prevScore = Math.pow((keywordNum * 2 + sigNum), 2)
						/ split.length();
				prevScore = 0.01 * Math.pow((keywordNum * 2 + sigNum), 2)
						/ split.length();
				score = Math.max(score, prevScore);
			}
		}

		return score;
	}

}
