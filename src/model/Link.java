package model;

import java.util.ArrayList;

public class Link {
	private int id;
	private String url;
	private String title;
	private String snippet;
	private String description;
	private String searchEngine;
	private int rank;
	private ArrayList<String> paragraphs = new ArrayList<String>();
	private String contentType = null;

	public Link(String searchEngine, String url, String title, String snippet,
			int rank) {
		this.url = url;
		this.title = title;
		this.snippet = snippet;
		this.searchEngine = searchEngine;
		this.rank = rank;
	}

	public String getURL() {
		return this.url;
	}

	public String getTitle() {
		return this.title;
	}

	public String getSnippet() {
		return this.snippet;
	}

	public String getSearchEngine() {
		return this.searchEngine;
	}

	public void setDescription(String description) {
		this.description = description.replace("<p>", System.lineSeparator())
				.replace("</p>", System.lineSeparator());
	}

	public String getDescription() {
		return this.description;
	}

	public int getRank() {
		return this.rank;
	}

	public void addParagraph(String paragraph) {
		this.paragraphs.add(paragraph);
	}

	public ArrayList<String> getParagraphs() {
		return this.paragraphs;
	}

	public void addContentType(String content) {
		this.contentType = content;
	}

	public String getContentType() {
		if (this.contentType == null) {
			return "Not available";
		} else
			return this.contentType;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getId() {
		return this.id;
	}

	@Override
	public String toString() {
		return this.rank + " : " + url;
	}
}
