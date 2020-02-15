package main;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.ProgressMonitor;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import similarity.CosineSimilarity;
import main.InputUI.WorkProgressMonitor.Task;
import model.BingSearch;
import model.GoogleSearch;
import model.Link;
import model.YahooSearch;

public class Crawler {
	private String query;
	private GoogleSearch google = null;
	private YahooSearch yahoo = null;
	private BingSearch bing = null;
	private Set<String> set = new HashSet<String>();
	private ArrayList<Link> results = new ArrayList<Link>();
	private StringBuilder allSentences = new StringBuilder();
	private ProgressMonitor monitor;
	private Task task;
	public ArrayList<String> allParagraphs = new ArrayList<String>();

	public Crawler(String query, Task task, ProgressMonitor monitor) {
		this.query = query;
		this.task = task;
		this.monitor = monitor;
	}

	public String getQuery() {
		return this.query;
	}

	private void processSearchEngineResults() {
		bing.createResult();
		monitor.setNote("Parsed Bing Result : " + bing.getResults().size());
		System.out.println("Parsed Bing Result : " + bing.getResults().size());

		yahoo.createResult();
		monitor.setNote("Parsed Yahoo Result : " + yahoo.getResults().size());
		System.out
				.println("Parsed Yahoo Result : " + yahoo.getResults().size());

		google.createResult();
		monitor.setNote("Parsed Google Result : " + google.getResults().size());
		System.out.println("Parsed Google Result : "
				+ google.getResults().size());

		results.addAll(bing.getResults());
		results.addAll(yahoo.getResults());
		if (google.getResults().size() > 0) {
			results.addAll(google.getResults());
		}
		Collections.sort(results, new Comparator<Link>() {
			@Override
			public int compare(Link doc1, Link doc2) {
				if (doc1.getRank() > doc2.getRank()) {
					return 1;
				} else
					return -1;
			}
		});
		ArrayList<Link> temp = new ArrayList<Link>();
		Set<String> url = new HashSet<String>();
		int tempid = 0;
		for (Link item : results) {
			String value = item.getURL().toLowerCase().split(":")[1];
			if (!url.contains(value)) {
				url.add(value);
				item.setId(tempid);
				temp.add(item);
				tempid++;
			}
		}
		results.clear();
		results = temp;
	}

	public ArrayList<Link> getResults() {
		return this.results;
	}

	private void crawlAllDocuments() {
		monitor.setNote("Found links in search engines " + results.size());
		System.out.println("Found links in search engines " + results.size());
		for (Link result : results) {
			String URL = result.getURL().toLowerCase();
			monitor.setNote(result.getURL());
			System.out.println(result.getURL());
			if (!set.contains(URL)) {
				set.add(URL);
				StringBuilder texts = getTexts(URL, result);
				if (texts.length() > 0) {
					result.setDescription(texts.toString());
				}
				allSentences.append(texts);
			}
		}
	}

	public StringBuilder getWholeDocument() {
		return this.allSentences;
	}

	public void startWork() {
		task.set(10);
		parseSearchEngines();
		processSearchEngineResults();
		task.set(30);
		crawlAllDocuments();
	}

	private void parseSearchEngines() {
		google = new GoogleSearch(query);
		yahoo = new YahooSearch(query);
		bing = new BingSearch(query);
	}

	private StringBuilder getTexts(String url, Link result) {
		StringBuilder sb = new StringBuilder();
		try {
			Response resp = Jsoup
					.connect(url)
					.userAgent(
							"Mozilla/5.0 (compatible; Googlebot/2.1; +http://www.google.com/bot.html)")
					.timeout(5000).execute();
			String[] contentType = null;
			if (resp.contentType() != null) {
				contentType = resp.contentType().split("/");
			}
			if (extractVideoId(url) != null) {
				result.addContentType("https://www.youtube.com/watch?v="
						+ extractVideoId(url));
			} else if (url.endsWith(".pdf") || url.endsWith(".PDF")) {
				result.addContentType("pdf");
			} else if (contentType != null && contentType[0].equals("text")) {
				Document doc = resp.parse();
				result.addContentType("text");
				Elements paragraphs = doc.select("p");
				for (Element p : paragraphs) {
					if (p.text().length() > 50
							&& !p.text().matches(".*\\<[^>]+>.*")) {
						String text = p.text().replaceAll("(\\[\\d+\\])+", " ");
						text = text.replaceAll("(?<=[,.!?;:])(?!$)", " ");
						text = text.replaceAll(" +", " ");
						if (text.length() < 10) {
							System.out.println("here is : " + text);
						}
						if (CosineSimilarity
								.findSimilarity(allParagraphs, text)) {
							allParagraphs.add(text);
							sb.append(text);
							result.addParagraph(text);
						}
					}
				}
			}
		} catch (IOException e) {
			if (url.endsWith(".pdf") || url.endsWith(".PDF")) {
				result.addContentType("pdf");
			}
		}

		return sb;
	}

	private String extractVideoId(String ytUrl) {
		String vId = null;
		Pattern pattern = Pattern
				.compile(".*(?:youtu.be\\/|v\\/|u\\/\\w\\/|embed\\/|watch\\?v=)([^#\\&\\?]*).*");
		Matcher matcher = pattern.matcher(ytUrl);
		if (matcher.matches()) {
			vId = matcher.group(1);
		}
		return vId;
	}

	@Override
	public String toString() {
		StringBuilder result = new StringBuilder();
		for (Link link : results) {
			result.append(link.getRank() + " : " + link.getURL() + "\n");
		}
		return result.toString();
	}
}
