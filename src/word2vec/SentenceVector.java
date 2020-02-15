package word2vec;



public class SentenceVector{
	String sentenceId;
	double[] vector;
	String paragraph;

	public String getParagraph() {
		return paragraph;
	}

	public void setParagraph(String paragraph) {
		this.paragraph = paragraph;
	}

	public SentenceVector(String line) {
		String[] token = line.split(" ");
		sentenceId = token[0];
		vector = new double[token.length - 1];
		for (int i = 1; i < token.length; i++)
			vector[i - 1] = Double.parseDouble(token[i]);
	}

	public String getSentenceId() {
		return sentenceId;
	}

	public void setSentenceId(String word) {
		this.sentenceId = word;
	}

	public double[] getVector() {
		return vector;
	}

	public void setVector(double[] vector) {
		this.vector = vector;
	}

}