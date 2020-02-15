package clusterer;

/** Class representing a cluster of Documents on related topics. */
public class Cluster implements Comparable<Cluster> {
	private Vector centroid;
	private final DocumentList documents = new DocumentList();
	private final int numFeatures;
	private int size = 0;
	private int id;

	/** Construct a cluster with a single member document. */
	public Cluster(Document document) {
		add(document);
		centroid = new Vector(document.getVector());
		numFeatures = document.getNumFeatures();
	}

	public Cluster(int id, Document document) {
		add(document);
		centroid = new Vector(document.getVector());
		numFeatures = document.getNumFeatures();
		this.id = id;
	}

	/** Add document to cluster and mark document as allocated. */
	public void add(Document document) {
		document.setIsAllocated();
		documents.add(document);
		size += document.getSize();
	}

	/** Remove all documents from a cluster. */
	public void clear() {
		documents.clearIsAllocated();
		documents.clear();
	}

	/** Allows sorting of Clusters by comparing ID of first document. */
	// @Override
	// public int compareTo(Cluster cluster) {
	// if (documents.isEmpty() || cluster.documents.isEmpty()) {
	// return 0;
	// }
	// return documents.get(0).compareTo(cluster.documents.get(0));
	// }
	@Override
	public int compareTo(Cluster cluster) {
		if (id > cluster.getId()) {
			return 1;
		} else if (id < cluster.getId()) {
			return -1;
		} else {
			return 0;
		}
	}

	/** Get centroid of cluster. */
	public Vector getCentroid() {
		return centroid;
	}

	/** Get documents in cluster. */
	public DocumentList getDocuments() {
		return documents;
	}

	/** Get the number of documents in the cluster. */
	public int size() {
		return documents.size();
	}

	/** Sort the documents within a cluster by document ID. */
	public void sort() {
		documents.sort();
	}

	@Override
	public String toString() {
		return documents.toString();
	}

	/** Update centroids and centroidNorms for this cluster. */
	public void updateCentroid() {
		centroid = new Vector(numFeatures);
		for (Document document : documents) {
			centroid = centroid.add(document.getVector());
		}
		centroid = centroid.divide(size());
	}

	public int getLineCount() {
		return this.size;
	}

	public int getId() {
		return this.id;
	}

	public void setId(int id) {
		this.id = id;
	}
}
