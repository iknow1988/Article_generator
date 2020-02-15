package clusterer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import model.Article;
import model.Paragraph;
import model.Sentence;

/**
 * A class for storing a list of clusters. This is the output of the clustering
 * process.
 */
public class ClusterList implements Iterable<Cluster> {
	private ArrayList<Cluster> clusters = new ArrayList<Cluster>();

	/** Add a cluster to the ClusterList. */
	public void add(Cluster cluster) {
		clusters.add(cluster);
	}

	/**
	 * Calculate average intercluster distance, which is the distance between
	 * cluster centroids.
	 */
	private double calcInterClusterDistance(DistanceMetric distance) {
		if (clusters.isEmpty()) {
			return 0;
		}
		double sumInterDist = 0;
		for (Cluster cluster1 : clusters) {
			for (Cluster cluster2 : clusters) {
				if (cluster1 != cluster2) {
					double dist = distance.calcDistance(cluster1, cluster2);
					sumInterDist += dist;
				}
			}
		}
		// there are N * N-1 unique pairs of clusters
		double check = sumInterDist / (clusters.size() * (clusters.size() - 1));
		return check;
	}

	/**
	 * Calculate average intracluster distance, which is the average distance
	 * between the constituent documents in a cluster and the cluster centroid.
	 */
	private double calcIntraClusterDistance(DistanceMetric distance) {
		double sumIntraClusterDistance = 0;
		int numDocuments = 0;
		for (Cluster cluster : clusters) {
			double intraClusterDistance = 0;
			for (Document document : cluster.getDocuments()) {
				intraClusterDistance += distance
						.calcDistance(document, cluster);
			}
			numDocuments += cluster.size();
			sumIntraClusterDistance += intraClusterDistance;
		}
		return sumIntraClusterDistance / numDocuments;
	}

	/**
	 * Calculate ratio of average intracluster distance to average intercluster
	 * distance. Used to optimize number of clusters k.
	 */
	public double calcIntraInterDistanceRatio(DistanceMetric distance) {
		if (clusters.isEmpty()) {
			return Double.MAX_VALUE;
		}
		double interClusterDistance = calcInterClusterDistance(distance);
		if (interClusterDistance > 0.0) {
			return calcIntraClusterDistance(distance) / interClusterDistance;
		} else {
			return Double.MAX_VALUE;
		}
	}

	/**
	 * Clear out documents from within each cluster. Used to cleanup after each
	 * clustering iteration.
	 */
	public void clear() {
		for (Cluster cluster : clusters) {
			cluster.clear();
		}
	}

	/**
	 * Find document with maximum distance to clusters in ClusterList. Distance
	 * to ClusterList is defined as the minimum of the distances to each
	 * constituent Cluster's centroid. This method is used during the cluster
	 * initialization in k-means clustering.
	 */
	public Document findFurthestDocument(DistanceMetric distance,
			DocumentList documentList) {
		double furthestDistance = Double.MIN_VALUE;
		Document furthestDocument = null;
		for (Document document : documentList) {
			if (!document.isAllocated()) {
				double documentDistance = distance.calcDistance(document, this);
				if (documentDistance > furthestDistance) {
					furthestDistance = documentDistance;
					furthestDocument = document;
				}
			}
		}
		if (furthestDocument == null) {
			System.out.println("Please do something");
		}
		return furthestDocument;
	}

	/** Find cluster whose centroid is closest to a document. */
	public Cluster findNearestCluster(DistanceMetric distance, Document document) {
		Cluster nearestCluster = null;
		double nearestDistance = Double.MAX_VALUE;
		for (Cluster cluster : clusters) {
			double clusterDistance = distance.calcDistance(document, cluster);
			if (clusterDistance < nearestDistance) {
				nearestDistance = clusterDistance;
				nearestCluster = cluster;
			}
		}
		return nearestCluster;
	}

	@Override
	public Iterator<Cluster> iterator() {
		return clusters.iterator();
	}

	/** Return the number of clusters in this ClusterList. */
	public int size() {
		return clusters.size();
	}

	/**
	 * Sort order of documents within each cluster, then sort order of clusters
	 * within ClusterList.
	 */
	private void sort() {
		for (Cluster cluster : this) {
			cluster.sort();
		}
		Collections.sort(clusters);
	}

	/**
	 * Display clusters in sorted order.
	 */
	@Override
	public String toString() {
		// sort();
		StringBuilder sb = new StringBuilder();
		int clusterIndex = 0;
		for (Cluster cluster : clusters) {
			sb.append("Cluster ");
			sb.append(clusterIndex++);
			sb.append("\n");
			sb.append(cluster);
		}
		return sb.toString();
	}

	/**
	 * Update centroids of all clusters within ClusterList.
	 */
	public void updateCentroids() {
		for (Cluster cluster : clusters) {
			cluster.updateCentroid();
		}
	}

	public ArrayList<Cluster> getClusters() {
		return this.clusters;
	}

	public Article getArticle() {
		Collections.sort(clusters);
		ArrayList<Paragraph> output = new ArrayList<Paragraph>();
		int clusterIndex = 0;
		for (Cluster cluster : clusters) {
			Paragraph outCluster = new Paragraph(clusterIndex);
			for (Document doc : cluster.getDocuments()) {
				for (Sentence sentence : doc.getSentences()) {
					outCluster.addSentence(sentence);
				}
			}
			clusterIndex++;
			output.add(outCluster);
		}
		Article article = new Article(output);

		return article;
	}
}
