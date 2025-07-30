import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.net.URL;
import java.net.MalformedURLException;

public class SwingUI extends JFrame{
	private static final long serialVersionUID = 1L;
	
	// Frame contents
	JButton convertButton, storyButton;
	JTextField urlField;
	JLabel helloLabel, filepathLabel, outputLabel;
	JFileChooser filepathChooser;
	// Fonts
	private Font font = new Font("Comfortaa", Font.PLAIN, 20);
	private Font bigFont = new Font("Comfortaa", Font.PLAIN, 35);
	
	
	// Constructor
	public SwingUI() {
		// Set frame parameters
		setTitle("Wattpad To Word");
		ImageIcon icon = new ImageIcon(getClass().getResource("/icon.png"));
		setIconImage(icon.getImage());
		setSize(800,600);
		setLocationRelativeTo(null);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		// FileChooser
		filepathChooser = new JFileChooser();
		String userDir = System.getProperty("user.home");
		File desktop = new File(userDir + "/Desktop");
		filepathChooser.setSelectedFile(desktop);
		filepathChooser.setCurrentDirectory(desktop);
		filepathChooser.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		// Buttons and Action Listeners
		convertButton = new JButton("Convert single chapter");
		convertButton.setFont(bigFont);
		convertButtonAE convertAE = new convertButtonAE();
		convertButton.addActionListener(convertAE);
		convertButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		storyButton = new JButton("Convert whole story");
		storyButton.setFont(bigFont);
		storyButtonAE storyAE = new storyButtonAE();
		storyButton.addActionListener(storyAE);
		storyButton.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		// TextField
		urlField = new JTextField("Enter WattPad chapter or story URL Here");
		urlField.setFont(font);
		urlField.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		// Labels
		helloLabel = new JLabel("WattPad To Word Converter");
		helloLabel.setFont(bigFont);
		helloLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		filepathLabel = new JLabel("Please navigate to the location where you want the word document(s)");
		filepathLabel.setFont(font);
		filepathLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		outputLabel = new JLabel("Awaiting Input");
		outputLabel.setFont(font);
		outputLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		// Fill panel through helper method
		addComponents(getContentPane());
		
		// Set visible
		setVisible(true);
	}
	
	// Helper method to add the components to the frame
	private void addComponents(Container pane) {
		// Create layout
		pane.setLayout(new BoxLayout(pane, BoxLayout.Y_AXIS));
		// Add components
		add(helloLabel);
		add(urlField);
		add(filepathChooser);
		add(filepathLabel);
		add(convertButton);
		add(storyButton);
		add(outputLabel);
	}

	
	// Action events
	private class convertButtonAE implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			// Set filepathLabel
			filepathLabel.setText("Saving to " + getfilePath());
			// Check for valid URL
			try {
				new URL(urlField.getText());
			} catch(MalformedURLException a) {
				outputLabel.setText("Please input a valid URL");
				return;
			}
			// Call convertPage()
			outputLabel.setText("Converting wattpad chapter...");
			try {
				WattpadScraper.convertPage(getURL(), getfilePath(), "wattpadChapter");
				outputLabel.setText("Chapter saved successfully to " + getfilePath());
			} catch (Exception ex) {
				outputLabel.setText(ex.getMessage());
			}
		}
	}
	
	private class storyButtonAE implements ActionListener {
		public void actionPerformed(ActionEvent e) {
			// Set filepathLabel
			filepathLabel.setText("Saving to " + getfilePath());
			// Check for valid URL
			try {
				new URL(urlField.getText());
			} catch(MalformedURLException a) {
				outputLabel.setText("Please input a valid URL");
				return;
			}
			// Call convertStory()
			outputLabel.setText("Converting wattpad story...");
			try {
				WattpadScraper.convertStory(getURL(), getfilePath());
				outputLabel.setText("Full story saved successfully to " + getfilePath());
			} catch (Exception ex) {
				outputLabel.setText(ex.getMessage());
			}
		}
	}

	// Get statements
	public String getURL() {
		return urlField.getText();
	}
	public String getfilePath() {
		return filepathChooser.getCurrentDirectory().getAbsolutePath();
	}
	
}
