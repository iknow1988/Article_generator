package clusterer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import model.Article;
import model.Paragraph;
import model.Sentence;

/** Class for storing a collection of documents to be clustered. */
public class DocumentList implements Iterable<Document> {
	private final List<Document> documents = new ArrayList<Document>();
	private int numFeatures;

	/** Construct an empty DocumentList. */
	public DocumentList() {
	}

	public DocumentList(Article article, boolean bySentence) {
		int i = 0;
		if (bySentence) {
			for (Paragraph paragraph : article.getParagraphs()) {
				for (Sentence sentence : paragraph.getSentences()) {
					String[] words = sentence.getText().split("[^\\w]+");
					if (words.length >= 3 && sentence.getText().length() > 5) {
						Document document = new Document(Integer.parseInt(""
								+ sentence.getLinkRank() + i), sentence, null,
								sentence.getLinkId(), sentence.getLinkRank());
						if (document != null) {
							documents.add(document);
						}
						i++;
					} else {
						System.out.println(sentence.getText());
					}
				}
			}
		} else {
			for (Paragraph paragraph : article.getParagraphs()) {
				if (paragraph.getSentences().size() > 0) {
					Document document = new Document(
							paragraph.getParagraphId(),
							paragraph.getSentences(), null,
							paragraph.getLinkId(), paragraph.getRank());
					if (document != null) {
						documents.add(document);
					}
				}
			}
		}
	}

	/** Add a document to the DocumentList. */
	public void add(Document document) {
		documents.add(document);
	}

	/** Clear all documents from the DocumentList. */
	public void clear() {
		documents.clear();
	}

	/** Mark all documents as not being allocated to a cluster. */
	public void clearIsAllocated() {
		for (Document document : documents) {
			document.clearIsAllocated();
		}
	}

	/** Get a particular document from the DocumentList. */
	public Document get(int index) {
		return documents.get(index);
	}

	/** Get the number of features used to encode each document. */
	public int getNumFeatures() {
		return numFeatures;
	}

	/** Determine whether DocumentList is empty. */
	public boolean isEmpty() {
		return documents.isEmpty();
	}

	@Override
	public Iterator<Document> iterator() {
		return documents.iterator();
	}

	/** Set the number of features used to encode each document. */
	public void setNumFeatures(int numFeatures) {
		this.numFeatures = numFeatures;
	}

	/** Get the number of documents within the DocumentList. */
	public int size() {
		return documents.size();
	}

	/** Sort the documents within the DocumentList by document ID. */
	public void sort() {
		Collections.sort(documents);
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Document document : documents) {
			sb.append("  ");
			sb.append(document);
			sb.append("\n");
		}
		return sb.toString();
	}

	public Document getById(long id) {
		Document document = null;
		for (Document doc : documents) {
			if (doc.getId() == id) {
				document = doc;
				break;
			}
		}

		return document;
	}

	public Sentence getByLine(int line) {
		Sentence document = null;
		for (Document doc : documents) {
			for (Sentence sentence : doc.getSentences()) {
				if (sentence.getLineNo() == line) {
					document = sentence;
					break;
				}
			}
		}

		return document;
	}
}
