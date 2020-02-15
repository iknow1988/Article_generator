package clusterer;

/** Class for calculating cosine distance between Vectors. */
public class CosineDistance extends DistanceMetric {
	@Override
	protected double calcDistance(Vector vector1, Vector vector2) {
		double inner = vector1.innerProduct(vector2);
		double norm1 = vector1.norm();

		double norm2 = vector2.norm();

		double result = inner / (norm1 * norm2);

		return result;
	}
}
