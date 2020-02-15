package clusterer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import model.Article;
import model.Paragraph;
import model.Sentence;

public class HAClustering {
	private static DocumentList documentList;
	private double similarity_threshold = 0.5000000;
	private HashMap<Integer, Long> map = new HashMap<Integer, Long>();

	public HAClustering(Article article) {
		documentList = new DocumentList(article, false);
	}

	public class Clusters {
		LinkedList<String> cluster = new LinkedList<String>();
		LinkedList<Integer> index = new LinkedList<Integer>();

		public Clusters(LinkedList<String> list1, LinkedList<Integer> list2,
				boolean merge_st1) {
			for (int i = 0; i < list1.size(); i++) {
				this.cluster.add(list1.get(i).trim());
			}
			for (int j = 0; j < list2.size(); j++) {
				this.index.add(list2.get(j));
			}
		}

		public String index() {
			String out_come = "";
			for (int i = 0; i < this.index.size(); i++) {
				out_come = out_come + "@ " + this.index.get(i).toString();
			}
			return (out_come);
		}

		public void addElements(LinkedList<String> list1,
				LinkedList<Integer> list2) {
			for (int i = 0; i < list2.size(); i++) {
				this.index.add(list2.get(i));
			}
			for (int j = 0; j < list1.size(); j++) {
				this.cluster.add(list1.get(j));
			}
		}

		public LinkedList<String> getCluster() {
			LinkedList<String> cluster_element = new LinkedList<String>();
			for (int i = 0; i < this.cluster.size(); i++) {
				cluster_element.add(this.cluster.get(i));
			}

			return (cluster_element);
		}

		public LinkedList<Integer> getIndex() {
			LinkedList<Integer> element_indx = new LinkedList<Integer>();
			for (int i = 0; i < this.index.size(); i++) {
				element_indx.add(this.index.get(i));
			}

			return (element_indx);
		}
	}

	public Article Apply_GAAC() {
		LinkedList<Clusters> Cluster = new LinkedList<Clusters>();
		LinkedList<String> Cleaned_Paragraphs = new LinkedList<String>();
		Pattern pattern = Pattern.compile("\\w+");
		int paragraph_index1 = 0;
		for (Document doc : documentList) {
			StringBuilder temp = new StringBuilder();
			for (Sentence sentence : doc.getSentences()) {
				temp.append(sentence.getText());
			}
			String para = temp.toString();
			LinkedList<String> tmp_words = new LinkedList<String>();
			Matcher matcher = pattern.matcher(para);
			while (matcher.find()) {
				String word = matcher.group();
				tmp_words.add(word);
			}
			Cleaned_Paragraphs.add(para);

			LinkedList<Integer> paragraph_indx = new LinkedList<Integer>();
			paragraph_indx.add(paragraph_index1);
			map.put(paragraph_index1, doc.getId());
			boolean status1 = false;
			Clusters ce1 = new Clusters(tmp_words, paragraph_indx, status1);
			Cluster.add(ce1);
			paragraph_index1++;
		}

		double[][] Element_Similarity_MAT = new double[Cluster.size()][Cluster
				.size()];

		double Max_Similarity = 0.0000000;
		int Sindex_X = 0;
		int Sindex_Y = 0;
		for (int i = 0; i < Cluster.size(); i++) {
			for (int j = 0; j < Cluster.size(); j++) {
				if (i != j) {
					LinkedList<String> list1 = Cluster.get(i).getCluster();
					LinkedList<String> list2 = Cluster.get(j).getCluster();
					double similarity1 = Calculate_List_similarity(list1, list2);
					Element_Similarity_MAT[i][j] = (similarity1);
					if ((similarity1 / 2.0000000) > Max_Similarity) {
						Max_Similarity = (similarity1 / 2.0000000);
						Sindex_X = i;
						Sindex_Y = j;
					}
				}
				if (i == j) {
					Element_Similarity_MAT[i][j] = 0;
				}
			}
		}

		Cluster.get(Sindex_X).addElements(Cluster.get(Sindex_Y).getCluster(),
				Cluster.get(Sindex_Y).getIndex());
		Cluster.remove(Sindex_Y);
		// St-2, Identify the highest similarity and merge them

		while (Max_Similarity > similarity_threshold) {
			System.out.println(Max_Similarity);
			double local_Max_Similarity = 0.0000000;
			int Local_XCoord = 0;
			int Local_YCoord = 0;
			for (int a = 0; a < Cluster.size(); a++) {
				for (int b = 0; b < Cluster.size(); b++) {
					if (a != b) {
						LinkedList<Integer> list1 = Cluster.get(a).getIndex();
						LinkedList<Integer> list2 = Cluster.get(b).getIndex();

						double denominator1 = ((list1.size() + list2.size()) * (list1
								.size() + list2.size() - 1));
						double numerator1 = 0.0000000;
						for (int c = 0; c < list1.size(); c++) {
							for (int d = 0; d < list2.size(); d++) {
								int X_Coord = list1.get(c);
								int Y_coord = list2.get(d);
								numerator1 = numerator1
										+ Element_Similarity_MAT[X_Coord][Y_coord];
							}
						}

						if ((numerator1 / denominator1) > local_Max_Similarity) {
							local_Max_Similarity = (numerator1 / denominator1);
							Local_XCoord = a;
							Local_YCoord = b;
						}
					}
				}
			}

			Max_Similarity = local_Max_Similarity;
			if (local_Max_Similarity > similarity_threshold) {
				// Max_Similarity = local_Max_Similarity;
				// organize the clusters
				Cluster.get(Local_XCoord).addElements(
						Cluster.get(Local_YCoord).getCluster(),
						Cluster.get(Local_YCoord).getIndex());
				Cluster.remove(Local_YCoord);
			}
		}

		ArrayList<Paragraph> output = new ArrayList<Paragraph>();
		int clusterIndex = 0;
		for (int b1 = 0; b1 < Cluster.size(); b1++) {
			String[] tmp_data = Cluster.get(b1).index().split("@");
			Paragraph outCluster = new Paragraph(clusterIndex);
			output.add(outCluster);
			for (int k = 0; k < tmp_data.length; k++) {
				String indx = tmp_data[k].trim();
				if (indx.length() > 0) {
					int S_index = Integer.parseInt(indx);
					Document doc = documentList.getById(map.get(S_index));
					if (doc != null) {
						for (Sentence sentence : doc.getSentences()) {
							outCluster.addSentence(sentence);
						}
					}

				}
			}
			clusterIndex++;
		}
		Article article = new Article(output);

		return article;
	}

	public Article Apply_Sentence_GAAC() {
		LinkedList<Clusters> Cluster = new LinkedList<Clusters>();
		LinkedList<String> Cleaned_Sentences = new LinkedList<String>();
		Pattern pattern = Pattern.compile("\\w+");
		for (Document doc : documentList) {
			for (Sentence sentence : doc.getSentences()) {
				String line = sentence.getText();
				LinkedList<String> tmp_words = new LinkedList<String>();
				Matcher matcher = pattern.matcher(line);
				while (matcher.find()) {
					String word = matcher.group().replaceAll(
							"[^\\p{L}\\p{Nd}]+", "");
					tmp_words.add(word);
				}
				Cleaned_Sentences.add(line);
				LinkedList<Integer> sentence_indx = new LinkedList<Integer>();
				sentence_indx.add(sentence.getLineNo());
				map.put(sentence.getLineNo(), doc.getId());
				boolean status1 = false;
				Clusters ce1 = new Clusters(tmp_words, sentence_indx, status1);
				Cluster.add(ce1);
			}
		}

		// ------------------------------------------------------------------------------------
		// St-1: Calculate a matrix of all pair Similarity
		double[][] Element_Similarity_MAT = new double[Cluster.size()][Cluster
				.size()];
		// initialize the matrix
		double Max_Similarity = 0.0000000;
		int Sindex_X = 0;
		int Sindex_Y = 0;
		for (int i = 0; i < Cluster.size(); i++) {
			for (int j = 0; j < Cluster.size(); j++) {
				if (i != j) {
					LinkedList<String> list1 = Cluster.get(i).getCluster();
					LinkedList<String> list2 = Cluster.get(j).getCluster();
					double similarity1 = Calculate_List_similarity(list1, list2);
					Element_Similarity_MAT[i][j] = (similarity1);
					if ((similarity1 / 2.0000000) > Max_Similarity) {
						Max_Similarity = (similarity1 / 2.0000000);
						Sindex_X = i;
						Sindex_Y = j;
					}
				}
				if (i == j) {
					Element_Similarity_MAT[i][j] = 0;
				}
			}
		}

		Cluster.get(Sindex_X).addElements(Cluster.get(Sindex_Y).getCluster(),
				Cluster.get(Sindex_Y).getIndex());
		Cluster.remove(Sindex_Y);

		while (Max_Similarity > similarity_threshold) {
			System.out.println(Max_Similarity);
			double local_Max_Similarity = 0.0000000;
			int Local_XCoord = 0;
			int Local_YCoord = 0;
			for (int a = 0; a < Cluster.size(); a++) {
				for (int b = 0; b < Cluster.size(); b++) {
					if (a != b) {
						LinkedList<Integer> list1 = Cluster.get(a).getIndex();
						LinkedList<Integer> list2 = Cluster.get(b).getIndex();

						double denominator1 = ((list1.size() + list2.size()) * (list1
								.size() + list2.size() - 1));
						double numerator1 = 0.0000000;
						for (int c = 0; c < list1.size(); c++) {
							for (int d = 0; d < list2.size(); d++) {
								int X_Coord = list1.get(c);
								int Y_coord = list2.get(d);
								numerator1 = numerator1
										+ Element_Similarity_MAT[X_Coord][Y_coord];
							}
						}

						if ((numerator1 / denominator1) > local_Max_Similarity) {
							local_Max_Similarity = (numerator1 / denominator1);
							Local_XCoord = a;
							Local_YCoord = b;
						}
					}
				}
			}

			Max_Similarity = local_Max_Similarity;
			if (local_Max_Similarity > similarity_threshold) {
				// Max_Similarity = local_Max_Similarity;
				// organize the clusters
				Cluster.get(Local_XCoord).addElements(
						Cluster.get(Local_YCoord).getCluster(),
						Cluster.get(Local_YCoord).getIndex());
				Cluster.remove(Local_YCoord);
			}
		}

		ArrayList<Paragraph> output = new ArrayList<Paragraph>();
		int clusterIndex = 0;
		for (int b1 = 0; b1 < Cluster.size(); b1++) {
			String[] tmp_data = Cluster.get(b1).index().split("@");
			Paragraph outCluster = new Paragraph(clusterIndex);
			output.add(outCluster);
			for (int k = 0; k < tmp_data.length; k++) {
				String indx = tmp_data[k].trim();
				if (indx.length() > 0) {
					int S_index = Integer.parseInt(indx);
					Sentence sentence = documentList.getByLine(S_index);
					outCluster.addSentence(sentence);
				}
			}
			clusterIndex++;
		}
		Article article = new Article(output);

		return article;
	}

	// calculate similarity between two lists
	public int Calculate_List_similarity(LinkedList<String> list1,
			LinkedList<String> list2) {
		int sim_count = 0;
		HashMap<String, Integer> D1 = new HashMap<String, Integer>();
		HashMap<String, Integer> D2 = new HashMap<String, Integer>();

		for (int i = 0; i < list1.size(); i++) {
			if (D1.containsKey(list1.get(i))) {
				int freq = D1.get(list1.get(i)) + 1;
				D1.put(list1.get(i), freq);
			} else {
				D1.put(list1.get(i), 1);
			}
		}

		for (int j = 0; j < list2.size(); j++) {
			if (D2.containsKey(list2.get(j))) {
				int freq = D2.get(list2.get(j)) + 1;
				D2.put(list2.get(j), freq);
			} else {
				D2.put(list2.get(j), 1);
			}
		}

		// Now calculate the similarity
		// 1. transfer all keys of D1 to an iterator
		Set<String> key_set = D1.keySet();
		Iterator<String> keys = key_set.iterator();
		while (keys.hasNext()) {
			String tmp_key = keys.next();
			if (D2.containsKey(tmp_key)) {
				sim_count = sim_count
						+ Math.min(D1.get(tmp_key), D2.get(tmp_key));
			}
		}

		return (sim_count);
	}
}