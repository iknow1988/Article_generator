package main;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.FileWriter;
import java.io.BufferedWriter;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;
import javax.swing.WindowConstants;

import utility.Utility;
import model.Paragraph;
import model.Sentence;

public class InputUI extends JFrame implements ActionListener, KeyListener {

	private static final long serialVersionUID = 1L;
	private JPanel panel = new JPanel();
	private JLabel label = new JLabel(
			"Enter your Topic to create an article : ");
	private JButton button = new JButton();
	private JTextField textBox = new JTextField();
	private boolean working = false;
	private String queryText = null;

	private void startWork() {
		queryText = textBox.getText();
		if (!queryText.isEmpty()) {
			if (!working) {
				String spellResponse = Utility.getSuggestion(queryText);
				if (spellResponse == null) {
					this.setVisible(false);
					working = true;
					WorkProgressMonitor monitor = new WorkProgressMonitor();
					monitor.start();
				} else {
					int response = JOptionPane.showConfirmDialog(null,
							"Did you mean : " + spellResponse + "?", "Confirm",
							JOptionPane.YES_NO_OPTION,
							JOptionPane.QUESTION_MESSAGE);
					if (response == JOptionPane.YES_OPTION) {
						queryText = spellResponse;
						this.setVisible(false);
						working = true;
						WorkProgressMonitor monitor = new WorkProgressMonitor();
						monitor.start();
					}
				}
			} else {
				JOptionPane.showMessageDialog(null,
						"Working on previous input!");
			}
		} else {
			JOptionPane.showMessageDialog(null, "Input is empty !");
		}
	}

	private void initUI() {
		this.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		this.setTitle("Article Generator");
		panel.setLayout(new GridLayout(3, 1));
		textBox.setPreferredSize(new Dimension(200, 24));
		textBox.addKeyListener(this);
		button.setText("Search");
		button.addActionListener(this);
		panel.setPreferredSize(new Dimension(250, 250));
		panel.add(label);
		panel.add(textBox);
		panel.add(button);

		this.add(panel);
		this.setSize(500, 150);
		this.setLocationRelativeTo(null);
		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

	}

	public InputUI() {
		initUI();
	}

	public void keyTyped(KeyEvent e) {
	}

	public void keyReleased(KeyEvent e) {
	}

	public void keyPressed(KeyEvent e) {
		int key = e.getKeyCode();
		if (key == KeyEvent.VK_ENTER) {
			startWork();
		}
	}

	private void createFile(ArrayList<Paragraph> paragraphs) {
		StringBuilder sb = new StringBuilder();
		for (Paragraph cluster : paragraphs) {
			ArrayList<Sentence> texts = cluster.getSentences();
			for (Sentence text : texts) {
				sb.append(text.getText());
			}
		}
		String outputFileName = "cleaned.txt";
		try (PrintWriter out = new PrintWriter(new BufferedWriter(
				new FileWriter(outputFileName)))) {
			out.println(sb.toString());
		} catch (IOException e) {
			System.out.println("Can not write to a file");
		}
	}

	public void actionPerformed(ActionEvent e) {
		startWork();
	}

	class WorkProgressMonitor extends JPanel implements PropertyChangeListener {

		private static final long serialVersionUID = 1L;
		private ProgressMonitor progressMonitor;
		private JTextArea taskOutput;
		private Task task;
		private int progress = 0;

		class Task extends SwingWorker<Void, Void> {
			@Override
			public Void doInBackground() {
				setProgress(0);
				try {
					Thread.sleep(1000);
					while (progress < 100 && !isCancelled()) {
						ArticleGenerator generator = new ArticleGenerator(
								queryText, task, progressMonitor);
						generator.generate();
						createFile(generator.getArticle().getParagraphs());
						ResultUI result = new ResultUI(generator.getArticle()
								.getParagraphs(), generator.getCrawler()
								.getResults(), generator.getKeywords(),"Article Generator");
						progress = 100;
						setProgress(progress);
						result.setVisible(true);
					}
				} catch (InterruptedException ignore) {
				}
				return null;
			}

			public void set(int val) {
				progress = val;
				setProgress(val);
			}

			@Override
			public void done() {
				try {
					get();
				} catch (ExecutionException e) {
					e.getCause().printStackTrace();
					String msg = String.format("Unexpected problem: %s", e
							.getCause().toString());
					System.out.println(msg);
				} catch (InterruptedException e) {
					String msg = String.format("Unexpected problem: %s", e
							.getCause().toString());
					System.out.println(msg);
				}
				Toolkit.getDefaultToolkit().beep();
			}
		}

		public WorkProgressMonitor() {
			super(new BorderLayout());
			taskOutput = new JTextArea();
			taskOutput.setMargin(new Insets(5, 5, 5, 5));
			taskOutput.setEditable(false);

			add(new JScrollPane(taskOutput), BorderLayout.CENTER);
			setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
		}

		public void start() {
			progressMonitor = new ProgressMonitor(WorkProgressMonitor.this,
					"Crawling data. It may take some time. Please wait.", "",
					0, 100);
			progressMonitor.setProgress(0);
			task = new Task();
			task.addPropertyChangeListener(this);
			task.execute();
		}

		public void propertyChange(PropertyChangeEvent evt) {
			if ("progress" == evt.getPropertyName()) {
				int progress = (Integer) evt.getNewValue();
				progressMonitor.setProgress(progress);
				String message = String.format("Completed %d%%.\n", progress);
				switch (progress) {
				case 10:
					message = "Searching on Google, Yahoo and Bing";
					break;
				case 20:
					message = "Getting results from searh engines";
					break;
				case 30:
					message = "Getting data from each link";
					break;
				case 40:
					message = "Finding the keywords";
					break;
				case 50:
					message = "Finding similarities of each sentences";
					break;
				case 60:
					message = "Removing duplicate lines";
					break;
				case 70:
					message = "Removing irrelevant paragraphs";
					break;
				case 80:
					message = "Merging paragraphs";
					break;
				case 90:
					message = "Summarizing article";
					break;
				case 100:
					message = "Finshed Creating article";
					break;
				default:
					message = "";
					break;
				}

				progressMonitor.setNote(message);
				taskOutput.append(message);
				if (progressMonitor.isCanceled() || task.isDone()) {
					Toolkit.getDefaultToolkit().beep();
					if (progressMonitor.isCanceled()) {
						task.cancel(true);
						taskOutput.append("Task canceled.\n");
						System.exit(0);
					} else {
						taskOutput.append("Task completed.\n");
					}
				}
			}
		}
	}
}
