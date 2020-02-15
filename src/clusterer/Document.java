package clusterer;

import java.util.ArrayList;

import model.Sentence;

/** Class containing an individual document. */
public class Document implements Comparable<Document> {
	private final String title;

	private ArrayList<Sentence> sentences;
	private final long id;
	private boolean allocated;
	private Vector histogram;
	private Vector vector;
	private int numFeatures;
	private int linkId;
	private int rank;
	private int size = 0;

	public Document(long id, ArrayList<Sentence> sentences, String title,
			int linkId, int rank) {
		this.id = id;
		this.sentences = sentences;

		this.title = title;
		this.linkId = linkId;
		this.rank = rank;
		size = sentences.size();
	}

	public Document(long id, Sentence sentence, String title, int linkId,
			int rank) {
		this.id = id;
		this.sentences = new ArrayList<Sentence>();
		sentences.add(sentence);
		StringBuilder temp = new StringBuilder();
		for (Sentence sent : sentences) {
			temp.append(sent.getText());
		}
		// this.contents = temp.toString();
		this.title = title;
		this.linkId = linkId;
		this.rank = rank;
		size = 1;
	}

	/** Mark document as not being allocated to a cluster. */
	public void clearIsAllocated() {
		allocated = false;
	}

	/** Allow documents to be sorted by ID. */
	@Override
	public int compareTo(Document document) {
		if (id > document.getId()) {
			return 1;
		} else if (id < document.getId()) {
			return -1;
		} else {
			return 0;
		}
	}

	public ArrayList<Sentence> getSentences() {
		return this.sentences;
	}

	/**
	 * Get document word histogram. The exact format is determined by the
	 * Encoder.
	 */
	public Vector getHistogram() {
		return histogram;
	}

	/** Get the document ID. */
	public long getId() {
		return id;
	}

	/** Get number of features in feature vector. */
	public int getNumFeatures() {
		return numFeatures;
	}

	/**
	 * Get feature vector for a document. This is typically a version of the
	 * histogram normalized for word frequency. The exact format is determined
	 * by the Encoder.
	 */
	public Vector getVector() {
		return vector;
	}

	/** Determine whether document has been allocated to a cluster. */
	public boolean isAllocated() {
		return allocated;
	}

	/** Set the word histogram for a document. */
	public void setHistogram(Vector histogram) {
		this.histogram = histogram;
	}

	/** Mark document as having been allocated to a cluster. */
	public void setIsAllocated() {
		allocated = true;
	}

	/**
	 * Set the feature vector for a document.
	 */
	public void setVector(Vector vector) {
		this.vector = vector;
		this.numFeatures = vector.size();
	}

	@Override
	public String toString() {
		StringBuilder temp = new StringBuilder();
		for (Sentence sentence : sentences) {
			temp.append(sentence.getText());
		}
		return "Document: " + id + ", Content: " + temp.toString();
	}

	public String getTitle() {
		return this.title;
	}

	public int getLinkId() {
		return this.linkId;
	}

	public int getRank() {
		return this.rank;
	}

	public int getSize() {
		return this.size;
	}
}
