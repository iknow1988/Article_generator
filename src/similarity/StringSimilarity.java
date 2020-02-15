package similarity;

import java.util.ArrayList;

class StringSimilarity {

	public static double similarity(String s1, String s2) {
		String longer = s1, shorter = s2;
		if (s1.length() < s2.length()) {
			longer = s2;
			shorter = s1;
		}
		int longerLength = longer.length();
		if (longerLength == 0) {
			return 1.0; /* both strings are zero length */
		}

		return (longerLength - editDistance(longer, shorter))
				/ (double) longerLength;

	}

	public static int editDistance(String s1, String s2) {
		s1 = s1.toLowerCase();
		s2 = s2.toLowerCase();

		int[] costs = new int[s2.length() + 1];
		for (int i = 0; i <= s1.length(); i++) {
			int lastValue = i;
			for (int j = 0; j <= s2.length(); j++) {
				if (i == 0)
					costs[j] = j;
				else {
					if (j > 0) {
						int newValue = costs[j - 1];
						if (s1.charAt(i - 1) != s2.charAt(j - 1))
							newValue = Math.min(Math.min(newValue, lastValue),
									costs[j]) + 1;
						costs[j - 1] = lastValue;
						lastValue = newValue;
					}
				}
			}
			if (i > 0)
				costs[s2.length()] = lastValue;
		}
		return costs[s2.length()];
	}

	public static void printSimilarity(String s, String t) {
		System.out.println(String.format(
				"%.3f is the similarity between \"%s\" and \"%s\"",
				similarity(s, t), s, t));
	}

	public static double getSimilarity(String s, String t) {
		return similarity(s, t);
	}

	public static boolean findSimilarity(ArrayList<String> paragraphs,
			String paragraph) {
		boolean result = false;
		double best = 0.0;
		for (String para : paragraphs) {
			double value = getSimilarity(para, paragraph);
			if (value > best) {
				best = value;
			}
		}
		if (best < .90) {
			result = true;
		}
		return result;
	}

	public static void main(String[] args) {
		printSimilarity(
				"When α=1[1], the symmetric Dirichlet distribution is equivalent to a uniform distribution over the open standard (K−1)-simplex, i.e. it is uniform over all points in its support. This particular distribution is known as the flat Dirichlet distribution. Values of the concentration parameter above 1 prefer variates that are dense, evenly distributed distributions, i.e. all the values within a single sample are similar to each other. Values of the concentration parameter below 1 prefer sparse distributions, i.e. most of the values within a single sample will be close to 0, and the vast majority of the mass will be concentrated in a few of the values.",
				"most of the values within a single sample will be close to 0, and the vast majority of the mass will be concentrated in a few of the values.");
	}
}