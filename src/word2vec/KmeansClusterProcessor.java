package word2vec;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.TreeMap;

import model.Article;
import model.Paragraph;
import model.Sentence;
import weka.clusterers.SimpleKMeans;
import weka.core.Instances;

public class KmeansClusterProcessor {
	private String textFilePath = "D:/EclipseWorkspace/ArticleGenerator/lib/sentence2vec-master/paragraph.txt";
	private Sentence2VecProcessor s2v;
	private Article article;

	private TreeMap<Integer, Paragraph> paragraphList;
	private TreeMap<Integer, Sentence> sentenceList;

	private ArrayList<String> lines;
	private boolean isParagraph;

	public KmeansClusterProcessor(Article article) {
		this.article = article;
		int pid = 0;
		int sid = 0;
		
		this.paragraphList = new TreeMap<Integer, Paragraph>();
		this.sentenceList = new TreeMap<Integer, Sentence>();
		
		for (Paragraph p : this.article.getParagraphs()) {
			this.paragraphList.put(pid++, p);
			for (Sentence s : p.getSentences()) {
				this.sentenceList.put(sid++, s);
			}
		}
	}

	public Article run(boolean isParagraph, int numOfCluster) {
		this.isParagraph = isParagraph;
		this.lines = new ArrayList<String>();
		if (isParagraph) {
			for (int i = 0; i < this.paragraphList.size(); i++) {
				this.lines.add(this.paragraphList.get(i).getString());
			}
		} else {
			for (int i = 0; i < this.sentenceList.size(); i++) {
				this.lines.add(this.sentenceList.get(i).getText());
			}
		}
		this.dumpSentenceFile();
		this.runSentence2Vec();
		Article result = null;
		try {
			result = this.runCluster(numOfCluster);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return result;
	}

	private void dumpSentenceFile() {

		String path = this.textFilePath;
		try {
			PrintWriter writer = new PrintWriter(path, "UTF-8");
			for (String line : this.lines) {
				writer.println(line);
			}
			writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void runSentence2Vec() {
		this.s2v = new Sentence2VecProcessor(this.textFilePath);
		try {
			s2v.train();
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private Article runCluster(int numOfCluster) throws Exception {
		SimpleKMeans kmeans = new SimpleKMeans();

		kmeans.setSeed(15);

		// important parameter to set: preserver order, number of cluster.
		kmeans.setPreserveInstancesOrder(true);
		kmeans.setNumClusters(numOfCluster);

		String path2 = this.textFilePath + ".vec.csv";
		try {
			PrintWriter writer = new PrintWriter(path2, "UTF-8");

			writer.println("@relation 'paragraph'");

			for (int i = 0; i < s2v.getSentenceVector(0).length; i++) {
				writer.println("@attribute e" + i + " real");
			}
			writer.println("@data");

			for (SentenceVector sv : s2v.getSentenceVectorList()) {
				for (double d : sv.getVector()) {
					writer.print(d);
					writer.print(",");
				}
				writer.println();
			}
			writer.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		BufferedReader datafile = readDataFile(path2);
		Instances data = new Instances(datafile);

		kmeans.buildClusterer(data);

		// This array returns the cluster number (starting with 0) for each
		// instance
		// The array has as many elements as the number of instances
		int[] assignments = kmeans.getAssignments();

		ArrayList<Paragraph> tempOutput = new ArrayList<Paragraph>();
		// int clusterIndex = 0;
		// for (Cluster cluster : clusters) {
		// Paragraph outCluster = new Paragraph(clusterIndex);
		// for (Document doc : cluster.getDocuments()) {
		// for (Sentence sentence : doc.getSentences()) {
		// outCluster.addSentence(sentence);
		// }
		// }
		// clusterIndex++;
		// tempOutput.add(outCluster);
		// }
		// Article article = new Article(tempOutput);

		for (int k = 0; k < kmeans.getNumClusters(); k++) {
			int i = 0;
			Paragraph outCluster = new Paragraph(k);
			for (int clusterNum : assignments) {
				if (clusterNum == k) {
					if (this.isParagraph) {
						for (Sentence s : this.paragraphList.get(i).getSentences())
							outCluster.addSentence(s);
					} else {
						outCluster.addSentence(this.sentenceList.get(i));
					}
				}
				i++;
			}
			tempOutput.add(outCluster);
			// System.out.println("");
		}
		Article article = new Article(tempOutput);
		return article;
	}

	public static BufferedReader readDataFile(String filename) {
		BufferedReader inputReader = null;

		try {
			inputReader = new BufferedReader(new FileReader(filename));
		} catch (FileNotFoundException ex) {
			System.err.println("File not found: " + filename);
		}

		return inputReader;
	}

}
