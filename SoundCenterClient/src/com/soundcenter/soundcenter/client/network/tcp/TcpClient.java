package com.soundcenter.soundcenter.client.network.tcp;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

import com.soundcenter.soundcenter.client.Applet;
import com.soundcenter.soundcenter.client.Client;
import com.soundcenter.soundcenter.lib.tcp.TcpOpcodes;
import com.soundcenter.soundcenter.lib.tcp.TcpPacket;

public class TcpClient implements Runnable {

	private InetAddress addr;
	private int port;
	private Socket socket = null;
	private ObjectOutputStream streamOut = null;
	private ObjectInputStream streamIn = null;
	private UploadManager uploadManager = null;
	
	private boolean exit = false;
	private boolean active = false;
	
	public TcpClient(InetAddress addr, int port) {
		this.addr = addr;
		this.port = port;
	}
	
	public void run() {
		Thread.currentThread().setName("TcpClient");
		if (active) {
			Applet.logger.d("Cannot start a new TCP-Client session while another is active.", null);
			return;
		}
		
		active = true;
		
		try {
			socket = new Socket(addr, port);
			streamOut = new ObjectOutputStream(socket.getOutputStream());
			streamIn = new ObjectInputStream(socket.getInputStream());
			
			Applet.logger.i("TCP-Client started on: " + addr.getHostAddress() + ":" + port + ".", null);
			Applet.gui.controller.setConnectButtonEnabled(true);

			sendPacket(TcpOpcodes.SV_CON_REQ_JOIN, null, null);
		} catch (IOException e) {
			Applet.logger.w("Cannot get the I/O for " + addr.getHostAddress() + ":" + port + ".\n " +
					"Please make sure that port " + port + " is forwarded for TCP and UDP on the server.", e);
			exit = true;
		}
		
		while(!exit) {
			
			try {
				/* receive and process packets */
				Object receivedPacket = streamIn.readObject();
				if (receivedPacket instanceof TcpPacket) {
					
					if (!TcpProtocol.processPacket((TcpPacket) receivedPacket)) {
						Applet.logger.i("Closing connection to server. Reason: " + Client.quitReason, null);
						Client.reconnect = true;
						exit = true;
					}
				}
			} catch (IOException e) {
				if (!exit && !socket.isClosed()) {
					Applet.logger.i("Error while receiving TCP-Packet:", e);
					Client.reconnect = true;
				}
				exit = true;
			} catch(ClassCastException e) {
				Client.reconnect = true;
				Applet.logger.w("An error occured while restoring an object from TCP-Stream.", e);
				exit = true;
			} catch (ClassNotFoundException e) {
				Client.reconnect = true;
				Applet.logger.w("TCP-error while reading packet:", e);
				exit = true;
			}
		}

		exit = true;
		active = false;
		Client.shutdown();
		Applet.logger.i("TCP-Client was shut down!", null);		
	}
	
	public synchronized void sendPacket(Byte opCode, Object key, Object value) {
		TcpPacket packet = new TcpPacket(opCode, key, value);	
		if (!exit && streamOut != null && socket != null && !socket.isClosed()) {
			try {
				streamOut.writeObject(packet);
			} catch(IOException e) {
				if (!exit) {
					Applet.logger.i("TCP-error while sending packet:", e);
				}
				exit = true;
			}
		}
	}
	
	public UploadManager getUploadManager() {
		return uploadManager;
	}
	
	public void startUploadManager(File[] files) {
		if (uploadManager == null || !uploadManager.isActive()) {
			uploadManager = new UploadManager(files);
			new Thread(uploadManager).start();
		} else {
			Applet.logger.i("Could not start new upload-manager, because one was already running.", null);
		}
	}
	
	public void approveUpload() {
		if (uploadManager != null) {
			uploadManager.approve();
		}
	}
	
	public void uploadDone() {
		if (uploadManager != null) {
			uploadManager.uploadDone();
		}
	}
	
	public void skipUpload() {
		if (uploadManager != null) {
			uploadManager.skip();
		}
	}
	
	public boolean isActive() {
		return active;
	}
	
	public void shutdown() {
		exit = true;
		if (uploadManager != null) {
			uploadManager.cancelAll();
		}
		
		if (streamOut != null) {
			try {
				streamOut.close();
			} catch(IOException e) {}
		}
		if (streamIn != null) {
			try {
				streamIn.close();
			} catch(IOException e) {}
		}	
		
		socket = null;
		streamIn = null;
		streamOut = null;
	}
}
