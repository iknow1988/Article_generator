package main;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.swing.*;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.DefaultHighlighter.DefaultHighlightPainter;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;

import model.Link;
import model.Paragraph;
import model.Sentence;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import utility.HtmlToPDF;
import utility.Utility;

public class ResultUI extends JFrame implements ActionListener {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private StringBuilder sb = new StringBuilder();
	private JButton back = new JButton();
	private JButton pdf = new JButton();
	private JButton clear = new JButton();
	private JEditorPane editorPane;
	private JTabbedPane tabbedPane = new JTabbedPane();
	private JList<String> pdfList;
	private DefaultListModel<String> pdfModel;
	private ArrayList<Link> results;
	private ArrayList<Paragraph> out;
	private JList<String> videoList;
	private DefaultListModel<String> videoModel;
	private String[] keywords;
	JFrame popup = new JFrame();
	protected UndoHandler undoHandler = new UndoHandler();
	protected UndoManager undoManager = new UndoManager();
	private UndoAction undoAction = null;
	private RedoAction redoAction = null;
	private Clipboard clipboard = Toolkit.getDefaultToolkit()
			.getSystemClipboard();
	private Map<Integer, Integer> used = new HashMap<Integer, Integer>();
	private String title;
	private JList<String> keywordList;
	private JList<LinkModel> linkList;

	public ResultUI(StringBuilder sb, ArrayList<Link> results) {
		this.results = results;
		this.sb = sb;
		initUI();
	}

	public ResultUI(StringBuilder sb, ArrayList<Link> results, String[] keywords) {
		this.results = results;
		this.sb = sb;
		this.keywords = keywords;
		initUI();
	}

	public ResultUI(ArrayList<Paragraph> out, ArrayList<Link> results,
			String[] keywords, String title) {
		this.results = results;
		this.out = out;
		this.keywords = keywords;
		createHtml();
		this.title = title;
		initUI();
		initialHighlight();
		Highlighter hilite = editorPane.getHighlighter();
		Highlighter.Highlight[] hilites = hilite.getHighlights();
		if (hilites.length == 0) {
			clear.setEnabled(false);
		}
	}

	private void initialHighlight() {
		String selectedItem = (String) keywordList.getModel().getElementAt(0);
		Document document = editorPane.getDocument();
		try {
			String find = selectedItem;
			for (int index = 0; index + find.length() < document.getLength(); index++) {
				String match = document.getText(index, find.length());
				if (find.equals(match.toLowerCase())) {
					DefaultHighlightPainter highlightPainter = new DefaultHighlightPainter(
							Color.PINK);
					editorPane.getHighlighter().addHighlight(index,
							index + find.length(), highlightPainter);
				}
			}
		} catch (BadLocationException ex) {
			ex.printStackTrace();
		}

		LinkModel selectedUrl = (LinkModel) linkList.getModel().getElementAt(0);
		selectedItem = null;
		for (Link res : results) {
			if (res.getURL().equals(selectedUrl.getLink())) {
				selectedItem = res.getId() + "";
			}
		}
		org.jsoup.nodes.Document document2 = Jsoup.parse(editorPane.getText());
		Elements spans = document2.select("#" + selectedItem);
		for (Element span : spans) {
			String text = span.text();
			String find = text;
			for (int index = 0; index + find.length() < editorPane
					.getDocument().getLength(); index++) {
				try {
					String match = editorPane.getDocument().getText(index,
							find.length());
					if (find.equals(match)) {
						DefaultHighlightPainter highlightPainter = new DefaultHighlightPainter(
								Color.YELLOW);
						editorPane.getHighlighter().addHighlight(index,
								index + find.length(), highlightPainter);
					}
				} catch (BadLocationException ex) {
					ex.printStackTrace();
				}
			}
		}
	}

	private void initUI() {

		this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		this.setTitle(title);

		this.getContentPane().add(tabbedPane, BorderLayout.CENTER);
		editorPane = new JEditorPane();
		HTMLEditorKit kit = new HTMLEditorKit();
		StyleSheet styleSheet = kit.getStyleSheet();
		Font font = new Font("Segoe UI", Font.PLAIN, 11);
		styleSheet.addRule("p {text-align: justify;" + "font-family: "
				+ font.getFamily() + "; margin: 2px; font-size: "
				+ font.getSize() + "px;}");
		editorPane.setEditorKit(kit);
		Document doc = kit.createDefaultDocument();
		editorPane.setDocument(doc);
		editorPane.setText(sb.toString());
		back.setText("Back");
		back.addActionListener(this);
		clear.setText("Clear Highlights");
		clear.addActionListener(this);
		pdf.setText("PDF");
		pdf.addActionListener(this);

		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(1, 3));
		panel.add(back);
		panel.add(pdf);
		panel.add(clear);
		this.add(panel, BorderLayout.SOUTH);

		JPanel documentTab = new JPanel(new BorderLayout());

		GridBagLayout gridbag = new GridBagLayout();
		JPanel northPanel = new JPanel();
		northPanel.setLayout(gridbag);
		GridBagConstraints c = new GridBagConstraints();
		c.weightx = .3;
		c.weighty = 1.0;
		c.gridx = 0;
		c.gridy = 0;
		JLabel keywordsLabel = new JLabel("Keywords");
		northPanel.add(keywordsLabel, c);
		c.weightx = 1;
		c.weighty = 1.0;
		c.gridx = 1;
		c.gridy = 0;
		JLabel linkLabel = new JLabel("Useful Links");
		northPanel.add(linkLabel, c);
		DefaultListModel<String> keywordModel = new DefaultListModel<String>();
		keywordList = new JList<String>(keywordModel);
		JScrollPane listScroller = new JScrollPane(keywordList);
		MouseListener keywordMouseListener = new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 1) {
					clear.setEnabled(true);
					String selectedItem = (String) keywordList
							.getSelectedValue();
					Document document = editorPane.getDocument();
					try {
						String find = selectedItem;
						for (int index = 0; index + find.length() < document
								.getLength(); index++) {
							String match = document.getText(index,
									find.length());
							if (find.equals(match.toLowerCase())) {
								DefaultHighlightPainter highlightPainter = new DefaultHighlightPainter(
										Color.PINK);
								editorPane.getHighlighter()
										.addHighlight(index,
												index + find.length(),
												highlightPainter);
							}
						}
					} catch (BadLocationException ex) {
						ex.printStackTrace();
					}
				}
			}
		};
		keywordList.addMouseListener(keywordMouseListener);
		for (String key : keywords) {
			keywordModel.addElement(key);
		}
		c.weightx = .3;
		c.weighty = 1.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 0;
		c.gridy = 1;
		northPanel.add(listScroller, c);

		DefaultListModel<LinkModel> linkModel = new DefaultListModel<LinkModel>();
		linkList = new JList<LinkModel>(linkModel);
		JScrollPane linklistScroller = new JScrollPane(linkList);
		Set<Integer> notUsed = new HashSet<Integer>();
		for (Map.Entry<Integer, Integer> entry : used.entrySet()) {
			int key = entry.getKey();
			for (Link res : results) {
				if ((key == res.getId()) && res.getDescription() != null) {
					linkModel.addElement(new LinkModel(Utility
							.getDomainName(res.getURL()), res.getURL()));
				}
			}
		}
		for (Link res : results) {
			if (!used.containsKey(res.getId()) && res.getDescription() != null) {
				notUsed.add(res.getId());
			}
		}

		for (Link res : results) {
			if (res.getDescription() != null && notUsed.contains(res.getId())) {
				linkModel.addElement(new LinkModel(Utility.getDomainName(res
						.getURL()), res.getURL()));
			}
		}
		MouseListener linkMouseListener = new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (SwingUtilities.isRightMouseButton(e)) {
					JPopupMenu popupMenu = linkPopup(e, linkList);
					popupMenu.show(linkList, e.getX(), e.getY());
				} else if (e.getClickCount() == 2) {
					LinkModel selectedItem = (LinkModel) linkList
							.getSelectedValue();
					try {
						Utility.openWebpage(new URL(selectedItem.getLink()));
					} catch (MalformedURLException e1) {
					}
				} else if (e.getClickCount() == 1) {
					clear.setEnabled(true);
					LinkModel selectedUrl = (LinkModel) linkList
							.getSelectedValue();
					String selectedItem = null;
					for (Link res : results) {
						if (res.getURL().equals(selectedUrl.getLink())) {
							selectedItem = res.getId() + "";
						}
					}
					org.jsoup.nodes.Document document = Jsoup.parse(editorPane
							.getText());
					Elements spans = document.select("#" + selectedItem);
					for (Element span : spans) {
						String text = span.text();
						String find = text;
						for (int index = 0; index + find.length() < editorPane
								.getDocument().getLength(); index++) {
							try {
								String match = editorPane.getDocument()
										.getText(index, find.length());
								if (find.equals(match)) {
									DefaultHighlightPainter highlightPainter = new DefaultHighlightPainter(
											Color.YELLOW);
									editorPane.getHighlighter().addHighlight(
											index, index + find.length(),
											highlightPainter);
								}
							} catch (BadLocationException ex) {
								ex.printStackTrace();
							}
						}
					}
				}
			}
		};
		linkList.addMouseListener(linkMouseListener);
		c.weightx = 1;
		c.weighty = 1.0;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridx = 1;
		c.gridy = 1;
		northPanel.add(linklistScroller, c);

		documentTab.add(northPanel, BorderLayout.NORTH);
		documentTab.add(new JScrollPane(editorPane), BorderLayout.CENTER);
		tabbedPane.addTab("Document", documentTab);

		JPanel pdfTab = new JPanel(new BorderLayout());
		pdfModel = new DefaultListModel<String>();
		pdfList = new JList<String>(pdfModel);
		JScrollPane pane = new JScrollPane(pdfList);
		pdfTab.add(pane, BorderLayout.CENTER);
		for (Link res : results) {
			if (res.getContentType().equals("pdf")) {
				pdfModel.addElement(res.getURL());
			}
		}
		MouseListener mouseListener = new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					String selectedItem = (String) pdfList.getSelectedValue();
					try {
						Utility.openWebpage(new URL(selectedItem));
					} catch (MalformedURLException e1) {
					}
				}
			}
		};
		pdfList.addMouseListener(mouseListener);
		tabbedPane.addTab("PDF Attachments", pdfTab);

		JPanel videoTab = new JPanel(new BorderLayout());
		videoModel = new DefaultListModel<String>();
		videoList = new JList<String>(videoModel);
		JScrollPane pane2 = new JScrollPane(videoList);
		videoTab.add(pane2, BorderLayout.CENTER);
		for (Link res : results) {
			if (res.getContentType().startsWith("https")) {
				videoModel.addElement(res.getURL());
			}
		}
		MouseListener mouseListener2 = new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					String selectedItem = (String) videoList.getSelectedValue();
					try {
						Utility.openWebpage(new URL(selectedItem));
					} catch (MalformedURLException e1) {
					}
				}
			}
		};
		videoList.addMouseListener(mouseListener2);
		tabbedPane.addTab("Videos", videoTab);

		this.setSize(1000, 600);
		this.setMinimumSize(new Dimension(1000, 600));

		editorPane.setCaretPosition(0);
		doc.addUndoableEditListener(undoHandler);
		editorPane.setComponentPopupMenu(ccpMenu());
		MouseListener listener = new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (popup != null) {
					popup.setVisible(false);
				}
			}
		};
		editorPane.addMouseListener(listener);
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		this.setLocation(dim.width / 2 - this.getSize().width / 2, dim.height
				/ 2 - this.getSize().height / 2);
		this.setVisible(true);
	}

	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == back) {
			this.setVisible(false);
			InputUI entry = new InputUI();
			entry.setVisible(true);
		} else if (e.getSource() == pdf) {
			JFileChooser fileChooser = new JFileChooser();
			fileChooser.setDialogTitle("Specify a file to save");

			int userSelection = fileChooser.showSaveDialog(this);
			File fileToSave = null;
			if (userSelection == JFileChooser.APPROVE_OPTION) {
				fileToSave = fileChooser.getSelectedFile();
				System.out.println("Save as file: "
						+ fileToSave.getAbsolutePath());
			}
			String fileName = fileToSave.getAbsolutePath();
			if (!fileName.endsWith(".pdf")) {
				fileName = fileName + ".pdf";
			}
			HtmlToPDF.generatePDF(editorPane.getText(), fileName);
		} else if (e.getSource() == clear) {
			Highlighter hilite = editorPane.getHighlighter();
			Highlighter.Highlight[] hilites = hilite.getHighlights();
			for (int i = 0; i < hilites.length; i++) {
				if (hilites[i].getPainter() instanceof DefaultHighlightPainter) {
					hilite.removeHighlight(hilites[i]);
				}
			}
			clear.setEnabled(false);
		}
	}

	private void createHtml() {
		sb.append("<html><head></head><body>");
		for (Paragraph paragraph : out) {
			ArrayList<Sentence> texts = paragraph.getSentences();
			sb.append("<p>");
			for (Sentence text : texts) {
				sb.append("<span id=\"" + text.getLinkId() + "\">");
				sb.append(text.getText());
				sb.append("</span>");
				if (used.containsKey(text.getLinkId())) {
					used.put(text.getLinkId(), used.get(text.getLinkId()) + 1);
				} else {
					used.put(text.getLinkId(), 1);
				}
			}
			sb.append("</p><br>");
		}
		sb.append("</body></html>");
		used = Utility.sortByValue(used);
	}

	private void popup(String html, Point p) {
		popup = new JFrame();
		if (popup.isVisible()) {
			popup.setVisible(false);
			popup = new JFrame();
		}
		SwingUtilities.convertPointToScreen(p, this);
		JEditorPane pane = new JEditorPane();
		HTMLEditorKit kit = new HTMLEditorKit();
		pane.setEditorKit(kit);
		Document doc = kit.createDefaultDocument();
		pane.setDocument(doc);
		pane.setText(html.toString());
		popup.setUndecorated(true);
		popup.getContentPane().add(new JScrollPane(pane), BorderLayout.CENTER);
		popup.setSize(600, 200);
		popup.setResizable(false);
		popup.setLocation(p.x + (popup.getWidth() / 2),
				p.y + (popup.getHeight() / 2));
		popup.setVisible(true);
		MouseListener listener = new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					popup.setVisible(false);
				}
			}
		};
		pane.setCaretPosition(0);
		pane.setEditable(false);
		pane.addMouseListener(listener);

		Action safeCopy = new AbstractAction(DefaultEditorKit.copyAction) {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					StringSelection transferable = new StringSelection(
							pane.getSelectedText());
					clipboard.setContents(transferable, transferable);
				} catch (Exception e1) {
					// do nothing
				}
			}
		};
		pane.getActionMap().put(DefaultEditorKit.copyAction, safeCopy);
		Action cutAction = pane.getActionMap().get(DefaultEditorKit.cutAction);
		Action pasteAction = pane.getActionMap().get(
				DefaultEditorKit.pasteAction);
		JPopupMenu rightMenuPopup = new javax.swing.JPopupMenu();
		rightMenuPopup.add(cutAction);
		rightMenuPopup.add(safeCopy);
		rightMenuPopup.add(pasteAction);
		pane.setComponentPopupMenu(rightMenuPopup);
	}

	public JPopupMenu ccpMenu() {
		Action safeCopy = new AbstractAction(DefaultEditorKit.copyAction) {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				try {

					StringSelection transferable = new StringSelection(
							editorPane.getSelectedText());
					clipboard.setContents(transferable, transferable);
				} catch (Exception e1) {
					// do nothing
				}
			}
		};
		editorPane.getActionMap().put(DefaultEditorKit.copyAction, safeCopy);
		Action cutAction = editorPane.getActionMap().get(
				DefaultEditorKit.cutAction);
		Action pasteAction = editorPane.getActionMap().get(
				DefaultEditorKit.pasteAction);
		KeyStroke undoKeystroke = KeyStroke.getKeyStroke(KeyEvent.VK_Z,
				InputEvent.CTRL_MASK);
		KeyStroke redoKeystroke = KeyStroke.getKeyStroke(KeyEvent.VK_Y,
				InputEvent.CTRL_MASK);

		undoAction = new UndoAction();
		editorPane.getInputMap().put(undoKeystroke, "undoKeystroke");
		editorPane.getActionMap().put("undoKeystroke", undoAction);

		redoAction = new RedoAction();
		editorPane.getInputMap().put(redoKeystroke, "redoKeystroke");
		editorPane.getActionMap().put("redoKeystroke", redoAction);
		JPopupMenu rightMenuPopup = new javax.swing.JPopupMenu();
		rightMenuPopup.add(cutAction);
		rightMenuPopup.add(safeCopy);
		rightMenuPopup.add(pasteAction);
		rightMenuPopup.add(undoAction);
		rightMenuPopup.add(redoAction);
		return rightMenuPopup;
	}

	public JPopupMenu linkPopup(MouseEvent e, JList<LinkModel> linkList) {
		JPopupMenu popupMenu = new javax.swing.JPopupMenu();
		JMenuItem jmi1, jmi2;
		popupMenu.add(jmi1 = new JMenuItem("Show Text"));
		popupMenu.add(new JPopupMenu.Separator());
		popupMenu.add(jmi2 = new JMenuItem("Delete From Document"));
		jmi1.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				@SuppressWarnings("unchecked")
				JList<LinkModel> l = (JList<LinkModel>) e.getSource();
				ListModel<LinkModel> m = l.getModel();
				int index = l.locationToIndex(e.getPoint());
				if (index > -1) {
					StringBuilder html = new StringBuilder();
					html.append("<html><head></head><body>");
					for (Link res : results) {
						if (res.getURL()
								.equals(m.getElementAt(index).getLink())) {
							for (String para : res.getParagraphs()) {
								html.append("<p>");
								html.append("<span id=\"" + res.getId() + "\">");
								html.append(para);
								html.append("</span>");
								html.append("</p><br>");
							}
						}
					}
					html.append("</body></html>");
					popup(html.toString(), e.getPoint());
				}
			}
		});
		jmi2.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent ae) {
				LinkModel selectedUrl = (LinkModel) linkList.getSelectedValue();
				String selectedItem = null;
				for (Link res : results) {
					if (res.getURL().equals(selectedUrl.getLink())) {
						selectedItem = res.getId() + "";
					}
				}
				org.jsoup.nodes.Document document = Jsoup.parse(editorPane
						.getText());
				Elements spans = document.select("#" + selectedItem);
				for (Element span : spans) {
					String text = span.text();
					String find = text;
					for (int index = 0; index + find.length() < editorPane
							.getDocument().getLength(); index++) {
						try {
							String match = editorPane.getDocument().getText(
									index, find.length());
							if (find.equals(match)) {
								editorPane.getDocument().remove(index,
										index + find.length());
							}
						} catch (BadLocationException ex) {
							ex.printStackTrace();
						}
					}
				}
			}
		});
		return popupMenu;
	}

	class UndoHandler implements UndoableEditListener {

		/**
		 * Messaged when the Document has created an edit, the edit is added to
		 * <code>undoManager</code>, an instance of UndoManager.
		 */
		public void undoableEditHappened(UndoableEditEvent e) {
			undoManager.addEdit(e.getEdit());
			undoAction.update();
			redoAction.update();
		}
	}

	class UndoAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		public UndoAction() {
			super("Undo");
			setEnabled(false);
		}

		public void actionPerformed(ActionEvent e) {
			try {
				undoManager.undo();
			} catch (CannotUndoException ex) {
			}
			update();
			redoAction.update();
		}

		protected void update() {
			if (undoManager.canUndo()) {
				setEnabled(true);
				putValue(Action.NAME, undoManager.getUndoPresentationName());
			} else {
				setEnabled(false);
				putValue(Action.NAME, "Undo");
			}
		}
	}

	class RedoAction extends AbstractAction {
		private static final long serialVersionUID = 1L;

		public RedoAction() {
			super("Redo");
			setEnabled(false);
		}

		public void actionPerformed(ActionEvent e) {
			try {
				undoManager.redo();
			} catch (CannotRedoException ex) {
			}
			update();
			undoAction.update();
		}

		protected void update() {
			if (undoManager.canRedo()) {
				setEnabled(true);
				putValue(Action.NAME, undoManager.getRedoPresentationName());
			} else {
				setEnabled(false);
				putValue(Action.NAME, "Redo");
			}
		}
	}
}

class LinkModel {
	String domain;
	String link;

	public LinkModel(String domain, String link) {
		this.domain = domain;
		this.link = link;
	}

	public String getDomain() {
		return this.domain;
	}

	public String getLink() {
		return this.link;
	}

	@Override
	public String toString() {
		return this.domain;
	}
}
