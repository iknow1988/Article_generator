package word2vec;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unused")
public class Word2VecProcessor {
	private static Logger log = LoggerFactory
			.getLogger(Word2VecProcessor.class);
	private String rawTextFile;
	private int layerSize;
	private List<WordVector> wordVectorList;

	public Word2VecProcessor(String rawTextFile) {
		this.rawTextFile = rawTextFile;
		// Customizing params
		// Nd4j.ENFORCE_NUMERICAL_STABILITY = true;
		this.layerSize = 10;
		this.wordVectorList = new ArrayList<WordVector>();

	}

	public void train() throws IOException, InterruptedException {
		Runtime r = Runtime.getRuntime();
		Process p = r
				.exec("java -jar /Users/wkleung/Downloads/word2vec-java/word2vec.jar "
						+ this.rawTextFile + " " + this.rawTextFile + ".vec");
		p.waitFor();
		BufferedReader b = new BufferedReader(new InputStreamReader(
				p.getInputStream()));
		String line = "";

		while ((line = b.readLine()) != null) {
			System.out.println(line);
		}
		b.close();

		try (BufferedReader br = new BufferedReader(new FileReader(
				this.rawTextFile + ".vec"))) {
			for (String line2; (line2 = br.readLine()) != null;) {
				WordVector wv = new WordVector(line2);
				this.wordVectorList.add(wv);
			}
		}
		return;
	}

	public double[] getWordVector(String word) {
		double[] result = null;

		for (WordVector wv : this.wordVectorList) {
			if (wv.getWord().equals(word)) {
				result = wv.getVector();
				break;
			}
		}

		return result;
	}

	class WordVector {
		String word;
		double[] vector;

		WordVector(String line) {
			String[] token = line.split(" ");
			word = token[0];
			vector = new double[token.length - 1];
			for (int i = 1; i < token.length; i++)
				vector[i - 1] = Double.parseDouble(token[i]);
		}

		public String getWord() {
			return word;
		}

		public void setWord(String word) {
			this.word = word;
		}

		public double[] getVector() {
			return vector;
		}

		public void setVector(double[] vector) {
			this.vector = vector;
		}

	}

}
