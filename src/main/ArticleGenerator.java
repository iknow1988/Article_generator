package main;

import java.io.IOException;
import java.util.HashSet;

import javax.swing.ProgressMonitor;

import main.InputUI.WorkProgressMonitor.Task;
import model.Article;
import model.Paragraph;
import model.Sentence;
import similarity.CosineSimilarity;
import similarity.RemoveIrrelevancy;
import summarizer.SummarizeDocuments;
import word2vec.KmeansClusterProcessor;
import clusterer.ClusterDocuments;
import clusterer.ClusterList;
import clusterer.HAClustering;
import keywordExtractor.KeyWordExtractor;

public class ArticleGenerator {
	private Crawler crawler;
	private String[] keywords;
	private String query;
	private Article article;
	private Task task;
	private ProgressMonitor monitor;

	private void generateKeywords() {
		try {
			keywords = KeyWordExtractor.guessFromString(crawler
					.getWholeDocument().toString(), 20);
		} catch (IOException e) {
		}
	}

	public ArticleGenerator(String query, Task task, ProgressMonitor monitor) {
		this.query = query;
		this.task = task;
		this.monitor = monitor;
	}

	public Article getArticle() {
		return this.article;
	}

	public Crawler getCrawler() {
		return this.crawler;
	}

	public void generate() {
		//crawls the data
		crawler = new Crawler(query, task, monitor);
		crawler.startWork();
		
		//Find the keywords
		task.set(40);
		generateKeywords();
		article = new Article(crawler.getResults());
		
		//Use GAAC to find unnecessary lines
		task.set(50);
		removeUnncessaryLines();
		task.set(80);

		// K means Cluster method with sentence2vec
		// KmeansClusterProcessor kcp = new KmeansClusterProcessor(article);
		// article = kcp.run(true, 20);
		// Article test2 = kcp.run(false, 20);

		// K Means with TF-IDF
		ClusterDocuments cluster = new ClusterDocuments(article);
		ClusterList clusterlist = cluster.performCluster();
		article = clusterlist.getArticle();
		task.set(90);

		// Summarize paragraphs
		article = SummarizeDocuments.summarize(article, keywords);

		// Remove irrelevant paragraphs
		article = RemoveIrrelevancy.removeIrrelevantParagraphs(article,
				keywords);
	}

	private void removeUnncessaryLines() {
		HAClustering sc1 = new HAClustering(article);
		Article test = sc1.Apply_Sentence_GAAC();
		
		task.set(60);
		test = CosineSimilarity.removeDuplicateLines(test);
		
		task.set(70);
		test = RemoveIrrelevancy.removeIrrelevantLines(test, keywords);

		HashSet<Integer> lines = new HashSet<Integer>();
		for (Paragraph para : test.getParagraphs()) {
			for (Sentence sent : para.getSentences()) {
				lines.add(sent.getLineNo());
			}
		}
		Article newArticle = new Article();
		for (Paragraph para : article.getParagraphs()) {
			Paragraph newParagraph = new Paragraph(para.getParagraphId());
			for (Sentence sent : para.getSentences()) {
				if (lines.contains(sent.getLineNo())) {
					newParagraph.addSentence(sent);
				}
			}
			if (newParagraph.getSentences().size() > 0) {
				newArticle.addParagraph(newParagraph);
			}
		}
		article = newArticle;

	}

	public String[] getKeywords() {
		return this.keywords;
	}

	public String getQuery() {
		return this.query;
	}
}
