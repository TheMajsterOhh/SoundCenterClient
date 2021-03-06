package com.soundcenter.soundcenter.client.audio.player;

import java.io.IOException;
import java.net.URL;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;
import javax.sound.sampled.UnsupportedAudioFileException;

import com.soundcenter.soundcenter.client.Applet;
import com.soundcenter.soundcenter.lib.data.GlobalConstants;

public class RadioPlayer extends PlayerController {

	private AudioFormat decodedFormat = null;
	private String streamUrl = "";

	public RadioPlayer(byte type, short id, String streamUrl) {
		super(type, id);
		this.streamUrl = streamUrl;
	}

	@Override
	public void run() {
		Thread.currentThread().setName("MusicPlayer");

		AudioInputStream encodedAudioStream = null;
		AudioInputStream decodedAudioStream = null;
		try {
			encodedAudioStream = AudioSystem.getAudioInputStream(new URL(streamUrl));
			//Applet.logger.d("Raw-Input-Stream created!", null);

			if (encodedAudioStream != null) {
				init(encodedAudioStream.getFormat());

				decodedAudioStream = AudioSystem.getAudioInputStream(decodedFormat, encodedAudioStream);
				//Applet.logger.d("Decoder-Stream created!", null);

				int numBytesRead = 0;
				while (!exit && line.isOpen()) { // TODO: numBytesRead = -1
													// break?

					byte[] data = new byte[bufferSize];
					numBytesRead = decodedAudioStream.read(data, 0, data.length);
					firstPacketReceived = true; //now we can start fading the volume
					if (numBytesRead > 0 && data != null)
						line.write(data, 0, data.length);
				}
			}
		} catch (LineUnavailableException e) {
			if (!exit)
				Applet.logger.i("Error while playing music stream:", e);
		} catch (IOException e) {
			if (!exit)
				Applet.logger.i("Error while writing to sourceDataLine:", e);
		} catch (UnsupportedAudioFileException e) {
			Applet.logger.i("Error while retrieving audio file format information:", e);
		}

		if (!exit) {
			close();
		}

		try {
			if (encodedAudioStream != null)
				encodedAudioStream.close();
			if (decodedAudioStream != null)
				decodedAudioStream.close();
		} catch (IOException e) {}
		
		// Applet.logger.d("Song stopped/ finished at player " + id + ".",
		// null);
	}

	private void init(AudioFormat baseFormat) throws LineUnavailableException {

		decodedFormat = new AudioFormat(AudioFormat.Encoding.PCM_SIGNED, baseFormat.getSampleRate(), 16,
				baseFormat.getChannels(), baseFormat.getChannels() * 2, baseFormat.getSampleRate(), false);
		DataLine.Info info = new DataLine.Info(SourceDataLine.class, decodedFormat);
		line = (SourceDataLine) AudioSystem.getLine(info);
		line.open(decodedFormat);

		volumeControl = (FloatControl) line.getControl(FloatControl.Type.MASTER_GAIN);
		minGainDB = volumeControl.getMinimum();
		ampGainDB = ((10.0f / 20.0f) * volumeControl.getMaximum()) - volumeControl.getMinimum();
		cste = Math.log(10.0) / 20;

		if (type == GlobalConstants.TYPE_AREA || type == GlobalConstants.TYPE_BOX) {
			volumeControl.setValue((float) minGainDB);
		}
		
		line.start();

		Applet.logger.d("MusicPlayer SourceDataLine started!", null);
	}
}
