package com.soundcenter.soundcenter.client.gui.tabs;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.ListSelectionModel;
import javax.swing.border.EmptyBorder;

import com.soundcenter.soundcenter.client.Client;
import com.soundcenter.soundcenter.client.gui.actions.MusicTabActions;
import com.soundcenter.soundcenter.client.gui.dialogs.UploadSongDialog;
import com.soundcenter.soundcenter.client.gui.renderer.SongListCellRenderer;
import com.soundcenter.soundcenter.client.util.GuiUtil;

public class MusicTab extends JPanel {
	
	public UploadSongDialog uploadSongsDialog = null;
	
	public JComboBox playerComboBox = new JComboBox();
	
	public JList songList = new JList();
	public JList songsToUploadList = new JList();
	public JLabel songsToUploadLabel = new JLabel("Not yet uploaded:");
	public JPanel songsToUploadPanel = new JPanel();
	
	public JButton addButton = new JButton("Add...");
	public JButton deleteButton = new JButton("Delete");
	public JButton deleteSongToUploadButton = new JButton("Delete");
	public JButton playButton = new JButton("Play Globally");
	
	public JButton uploadButton = new JButton("Upload...");
	
	public MusicTab() {
		
		/* initialize components */
		songList.setCellRenderer(new SongListCellRenderer());
		songList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		songList.setLayoutOrientation(JList.VERTICAL);
		songList.setVisibleRowCount(-1);
		JScrollPane musicScroller = new JScrollPane(songList);
		musicScroller.setPreferredSize(new Dimension(450, 280));
		musicScroller.setMinimumSize(new Dimension(200,80));
		
		songsToUploadList.setModel(Client.database.getSongsToUploadModel());
		songsToUploadList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		songsToUploadList.setLayoutOrientation(JList.VERTICAL);
		songsToUploadList.setVisibleRowCount(-1);
		JScrollPane notUploadedScroller = new JScrollPane(songsToUploadList);
		notUploadedScroller.setPreferredSize(new Dimension(210, 140));
		notUploadedScroller.setMinimumSize(new Dimension(150,60));
		
		playerComboBox.setModel(new DefaultComboBoxModel());
		
		GuiUtil.setFixedSize(playerComboBox, new Dimension(150, 30));
		
		addButton.setEnabled(false);
		deleteButton.setEnabled(false);
		playButton.setEnabled(false);
		
		//Actions
		playerComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MusicTabActions.musicChooserSelected();
			}
		});
		
		addButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MusicTabActions.addButtonPressed();
			}
		});
		
		deleteButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MusicTabActions.deleteButtonPressed(songList);
			}
		});
		
		playButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MusicTabActions.playButtonPressed(songList);
			}
		});
		
		deleteSongToUploadButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MusicTabActions.deleteSongToUploadButtonPressed(songsToUploadList);
			}
		});
		
		uploadButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				MusicTabActions.uploadButtonPressed();
			}
		});
		
		
		/* build gui */
		//show only when own player is selected
		songsToUploadPanel.setVisible(false);
		songsToUploadPanel.setLayout(new BoxLayout(songsToUploadPanel, BoxLayout.Y_AXIS));
		
		Box hBox1 = Box.createHorizontalBox();
			hBox1.add(songsToUploadLabel);
			hBox1.add(Box.createHorizontalGlue());
		songsToUploadPanel.add(hBox1);
		songsToUploadPanel.add(notUploadedScroller);
		songsToUploadPanel.add(Box.createRigidArea(new Dimension(0, 5)));
		Box hBox2 = Box.createHorizontalBox();
			hBox2.add(Box.createHorizontalGlue());
			hBox2.add(deleteSongToUploadButton);
		songsToUploadPanel.add(hBox2);
		songsToUploadPanel.add(Box.createRigidArea(new Dimension(0, 10)));
		
		Box uploadBox = Box.createHorizontalBox();
			uploadBox.add(uploadButton);
			uploadBox.add(Box.createHorizontalGlue());
		songsToUploadPanel.add(uploadBox);

		
		setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
		setBorder(new EmptyBorder(new Insets(10, 10, 10, 10)));
		
		Box vBox1 = Box.createVerticalBox();
			Box playerChooserBox = Box.createHorizontalBox();
				JLabel playerLabel = new JLabel("Player:");
				playerLabel.setPreferredSize(new Dimension(40, 30));
				playerChooserBox.add(playerLabel);
				playerChooserBox.add(Box.createRigidArea(new Dimension(20,0)));
				playerChooserBox.add(playerComboBox);
				playerChooserBox.add(Box.createHorizontalGlue());
			vBox1.add(playerChooserBox);
			
			vBox1.add(Box.createVerticalGlue());
			
			vBox1.add(songsToUploadPanel);	
		add(vBox1);
		
		add(Box.createRigidArea(new Dimension(10, 0)));
		JSeparator vseparator1 = new JSeparator(JSeparator.VERTICAL);
		vseparator1.setPreferredSize(new Dimension(15, 320));
		add(vseparator1);
		
		Box vBox2 = Box.createVerticalBox();
			vBox2.add(musicScroller);
		
			vBox2.add(Box.createRigidArea(new Dimension(0,10)));
			
			Box controlsBox = Box.createHorizontalBox();
				controlsBox.add(addButton);
				controlsBox.add(Box.createRigidArea(new Dimension(20, 0)));
				controlsBox.add(deleteButton);
				controlsBox.add(Box.createHorizontalGlue());
				controlsBox.add(playButton);
			vBox2.add(controlsBox);
			
			vBox2.add(Box.createVerticalGlue());
		add(vBox2);	
	}

}
