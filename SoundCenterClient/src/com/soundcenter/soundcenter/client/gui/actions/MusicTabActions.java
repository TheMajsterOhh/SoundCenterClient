package com.soundcenter.soundcenter.client.gui.actions;

import java.io.File;

import javax.swing.DefaultListModel;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JList;

import com.soundcenter.soundcenter.client.Applet;
import com.soundcenter.soundcenter.client.Client;
import com.soundcenter.soundcenter.client.util.SCFileFilter;
import com.soundcenter.soundcenter.lib.data.GlobalConstants;
import com.soundcenter.soundcenter.lib.data.Song;
import com.soundcenter.soundcenter.lib.tcp.TcpOpcodes;
import com.soundcenter.soundcenter.lib.util.FileOperation;

public class MusicTabActions {

	public static void musicChooserSelected() {
		JComboBox playerComboBox = Applet.gui.musicTab.playerComboBox;
		
		DefaultListModel model = null;
		if (playerComboBox.getSelectedIndex() >= 0) {
			String player = (String) playerComboBox.getSelectedItem();
			model = Client.database.getSongModel(player);
			
			if (model != null) {
				Applet.gui.musicTab.songList.setModel(model);
			} else {
				Applet.gui.musicTab.songList.setModel(new DefaultListModel());
			}
			
			if(player.equals(Client.userName)) {
				Applet.gui.musicTab.songsToUploadPanel.setVisible(true);
				
				Applet.gui.musicTab.addButton.setEnabled(true);
				Applet.gui.musicTab.deleteButton.setEnabled(true);
			} else {
				Applet.gui.musicTab.songsToUploadPanel.setVisible(false);
			
				Applet.gui.musicTab.addButton.setEnabled(false);
				if(Client.database.permissionGranted("sc.others.delete")) {
					Applet.gui.musicTab.deleteButton.setEnabled(true);
				} else {
					Applet.gui.musicTab.deleteButton.setEnabled(false);
				}
			}
			
			if (Client.database.permissionGranted("sc.play.global")) {
				Applet.gui.musicTab.playButton.setEnabled(true);
			} else {
				Applet.gui.musicTab.playButton.setEnabled(false);
			}
			
		} else {
			Applet.gui.musicTab.songList.setModel(new DefaultListModel());
			
			Applet.gui.musicTab.addButton.setEnabled(false);
			Applet.gui.musicTab.deleteButton.setEnabled(false);
			Applet.gui.musicTab.playButton.setEnabled(false);
			
		}
	}
	
	public static void addButtonPressed() {
		JFileChooser fc = new JFileChooser();
		fc.setMultiSelectionEnabled(true);
		fc.setAcceptAllFileFilterUsed(false);
		
		SCFileFilter fileFilter = new SCFileFilter("mp3,midi,mid");

		fc.setFileFilter(fileFilter);

		if (fc.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			File[] files = fc.getSelectedFiles(); // get the selected files
			if (files == null)
				return;

			for (File file : files) { // for each selected file
				if (file.exists()) {
					String ext = FileOperation.getExtension(file);

					if (GlobalConstants.supportedExtensions.contains(ext)) {
						Client.database.addSongToUpload(file);
					} else
						Applet.logger.i("Could not add song. Unsupported extension or missing permission for " + ext + " files.", null);

				}
			}
		}
	}
	
	public static void deleteButtonPressed(JList songList) {
		Song song = (Song) songList.getSelectedValue();
		if (song != null) {			
			Client.tcpClient.sendPacket(TcpOpcodes.SV_DATA_CMD_DELETE_SONG, song, null);
		}
	}

	public static void playButtonPressed(JList songList) {
		if (Applet.gui.musicTab.playButton.getText().equals("Stop Globally")) {
			Client.tcpClient.sendPacket(TcpOpcodes.SV_STREAM_CMD_STOP_GLOBAL, null, null);
		} else {
			Song song = (Song) songList.getSelectedValue();
			if (song != null) {			
				Client.tcpClient.sendPacket(TcpOpcodes.SV_STREAM_CMD_PLAY_GLOBAL, song, null);
			}
		}
	}

	public static void uploadButtonPressed() {
		DefaultListModel model = (DefaultListModel) Applet.gui.musicTab.songsToUploadList.getModel();
		int size = model.getSize();
		if (size > 0) {
			File[] songsToUpload = new File[size];
			for (int i=0; i<songsToUpload.length; i++) {
				songsToUpload[i] = (File) model.getElementAt(i);
			}
			
			Client.tcpClient.startUploadManager(songsToUpload);
		}
	}
	
	public static void deleteSongToUploadButtonPressed(JList songList) {
		int index = songList.getSelectedIndex();
		if (index >= 0) {
			DefaultListModel model = (DefaultListModel) songList.getModel();
			model.remove(index);
		}
	}
	
}
