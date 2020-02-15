package model;

public class Sentence {
	private int linkId;
	private String text;
	private int linkRank;
	private int lineNo;

	public Sentence(int link, StringBuilder text, int rank, int lineNo) {
		this.linkId = link;
		if (!text.toString().startsWith(" ")) {
			text.insert(0, " ");
		}
		if (text.toString().endsWith(" ")) {
			text.deleteCharAt(text.length() - 1);
		}
		if (!text.toString().endsWith(".") && !text.toString().endsWith("!")
				&& !text.toString().endsWith("?")) {
			text.append(".");
		}
		this.text = text.toString();
		this.linkRank = rank;
		this.lineNo = lineNo;
	}

	public String getText() {
		return this.text;
	}

	public int getLinkId() {
		return this.linkId;
	}

	public int getLinkRank() {
		return this.linkRank;
	}

	public int getLineNo() {
		return this.lineNo;
	}
}
