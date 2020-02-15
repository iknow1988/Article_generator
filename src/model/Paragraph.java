package model;

import java.util.ArrayList;

public class Paragraph {
	private int paragraphId;
	private ArrayList<Sentence> sentences = new ArrayList<Sentence>();
	private int rank;
	private int linkId;

	public Paragraph(int paragraphId) {
		this.paragraphId = paragraphId;
	}

	public void addSentence(Sentence text) {
		this.sentences.add(text);
	}

	public ArrayList<Sentence> getSentences() {
		return this.sentences;
	}

	public int getParagraphId() {
		return this.paragraphId;
	}

	public String getString() {
		StringBuilder result = new StringBuilder();
		for (Sentence sentence : sentences) {
			result.append(sentence.getText() + " ");
		}
		return result.toString();
	}

	public void setRank(int rank) {
		this.rank = rank;
	}

	public int getRank() {
		return this.rank;
	}

	public int getLinkId() {
		return this.linkId;
	}

	public void setLinkId(int linkId) {
		this.linkId = linkId;
	}

}
