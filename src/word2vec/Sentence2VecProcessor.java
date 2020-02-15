package word2vec;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

public class Sentence2VecProcessor {
	private int layerSize;
	private ArrayList<SentenceVector> sentenceVectorList;
	private String rawTextFile;

	public Sentence2VecProcessor(String rawTextFile) {
		this.rawTextFile = rawTextFile;
		// Customizing params
		// Nd4j.ENFORCE_NUMERICAL_STABILITY = true;
		this.layerSize = 100;
		this.sentenceVectorList = new ArrayList<SentenceVector>();

	}

	public void train() throws IOException, InterruptedException {
//		Runtime r = Runtime.getRuntime();
//		String command = "D:/python/python.exe D:/EclipseWorkspace/ArticleGenerator/lib/sentence2vec-master/demo.py "
//				+ this.rawTextFile;
//		System.out.println(command);
//		Process p = r.exec(command);
		Process p = new ProcessBuilder(
				"D:/python/python.exe",
				"D:/EclipseWorkspace/ArticleGenerator/lib/sentence2vec-master/demo.py",
				this.rawTextFile).start();

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
				if (line2.startsWith("sent")) {
					SentenceVector wv = new SentenceVector(line2);
					this.sentenceVectorList.add(wv);
				}
			}
		}
		return;
	}

	public void setParagraph(int sentenceId, String paragraph) {
		this.sentenceVectorList.get(sentenceId).setParagraph(paragraph);
	}

	public double[] getSentenceVector(int id) {
		double[] result = null;
		String sent_id = "sent_" + id;
		for (SentenceVector wv : this.sentenceVectorList) {
			if (wv.getSentenceId().equals(sent_id)) {
				result = wv.getVector();
				break;
			}
		}

		return result;
	}

	public int getLayerSize() {
		return layerSize;
	}

	public ArrayList<SentenceVector> getSentenceVectorList() {
		return sentenceVectorList;
	}

	public String getRawTextFile() {
		return rawTextFile;
	}

	public ArrayList<SentenceVector> getNearestSentence(int id, int size) {
		String sent_id = "sent_" + id;
		SentenceVector source = null;
		for (SentenceVector wv : this.sentenceVectorList) {
			if (wv.getSentenceId().equals(sent_id)) {
				source = wv;
				break;
			}
		}

		Map<Double, SentenceVector> result = new HashMap<Double, SentenceVector>();
		for (SentenceVector wv : this.sentenceVectorList) {
			if (!wv.getSentenceId().equals(source.getSentenceId())) {
				double sim = CosineDistance.calculate(source.getVector(),
						wv.getVector());
				result.put(sim, wv);
			}
		}

		Map<Double, SentenceVector> treeMap = new TreeMap<Double, SentenceVector>(
				new Comparator<Double>() {

					@Override
					public int compare(Double o1, Double o2) {
						return o2.compareTo(o1);
					}

				});

		treeMap.putAll(result);

		ArrayList<SentenceVector> result2 = new ArrayList<SentenceVector>();

		int i = 0;
		for (Entry<Double, SentenceVector> entry : treeMap.entrySet()) {
			if (i++ > size)
				break;
			result2.add(entry.getValue());
		}
		return result2;
	}

	public ArrayList<SentenceVector> getNearestSentenceBySimilarity(int id,
			double similary) {
		String sent_id = "sent_" + id;
		SentenceVector source = null;
		for (SentenceVector wv : this.sentenceVectorList) {
			if (wv.getSentenceId().equals(sent_id)) {
				source = wv;
				break;
			}
		}

		Map<Double, SentenceVector> result = new HashMap<Double, SentenceVector>();
		for (SentenceVector wv : this.sentenceVectorList) {
			if (!wv.getSentenceId().equals(source.getSentenceId())) {
				double sim = CosineDistance.calculate(source.getVector(),
						wv.getVector());
				if (sim > similary) {
					result.put(sim, wv);
				}
			}
		}

		Map<Double, SentenceVector> treeMap = new TreeMap<Double, SentenceVector>(
				new Comparator<Double>() {

					@Override
					public int compare(Double o1, Double o2) {
						return o2.compareTo(o1);
					}

				});

		treeMap.putAll(result);

		ArrayList<SentenceVector> result2 = new ArrayList<SentenceVector>();

		for (Entry<Double, SentenceVector> entry : treeMap.entrySet()) {
			result2.add(entry.getValue());
		}
		return result2;
	}

	static <K, V extends Comparable<? super V>> List<Entry<K, V>> entriesSortedByValues(
			Map<K, V> map) {

		List<Entry<K, V>> sortedEntries = new ArrayList<Entry<K, V>>(
				map.entrySet());

		Collections.sort(sortedEntries, new Comparator<Entry<K, V>>() {
			@Override
			public int compare(Entry<K, V> e1, Entry<K, V> e2) {
				return e2.getValue().compareTo(e1.getValue());
			}
		});

		return sortedEntries;
	}

}
