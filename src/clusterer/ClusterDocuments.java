package clusterer;

import java.util.HashMap;

import net.sf.javaml.clustering.KMeans;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DefaultDataset;
import net.sf.javaml.core.DenseInstance;
import net.sf.javaml.core.Instance;
import model.Article;

public class ClusterDocuments {
	private final int NUM_FEATURES = 1000;
	private static DocumentList documentList;
	private ClusterList clusterList;

	public ClusterDocuments(Article article) {
		documentList = new DocumentList(article, false);
		Encoder encoder = new TfIdfEncoder(NUM_FEATURES);
		encoder.encode(documentList);
	}

	public ClusterList performCluster() {
		HashMap<Integer, DocInstance> map = new HashMap<Integer, DocInstance>();
		Dataset data = new DefaultDataset();
		for (int i = 0; i < documentList.size(); i++) {
			Document doc = documentList.get(i);
			Instance ins = new DenseInstance(doc.getVector().getArray());
			int id = ins.getID();
			DocInstance docInstance = new DocInstance(doc, id);
			map.put(id, docInstance);
			data.add(ins);
		}
		net.sf.javaml.clustering.Clusterer km = new KMeans(20, 10,
				new net.sf.javaml.distance.CosineDistance());
		Dataset[] clusters = km.cluster(data);
		clusterList = new ClusterList();
		for (int i = 0; i < clusters.length; i++) {
			int id = clusters[i].get(0).getID();
			DocInstance docInstance = map.get(id);
			Document doc = docInstance.getDoc();
			Cluster cluster = new Cluster((int) doc.getId(), doc);
			clusterList.add(cluster);
			for (int j = 1; j < clusters[i].size(); j++) {
				id = clusters[i].get(j).getID();
				docInstance = map.get(id);
				doc = docInstance.getDoc();
				cluster.add(doc);
				if (cluster.getId() > doc.getId()) {
					cluster.setId((int) doc.getId());
				}
			}
		}
		System.out.println(clusterList);
		return clusterList;
	}
}

class DocInstance {
	private Document doc;
	private int id;

	public DocInstance(Document doc, int id) {
		this.doc = doc;
		this.id = id;
	}

	public int getId() {
		return this.id;
	}

	public Document getDoc() {
		return this.doc;
	}
}