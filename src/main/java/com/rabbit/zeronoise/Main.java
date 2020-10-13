package com.rabbit.zeronoise;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;
import java.util.prefs.Preferences;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

import org.apache.commons.lang3.StringUtils;

import com.rabbit.zeronoise.util.ZConstants;
import com.sun.media.jfxmedia.AudioClip;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

public class Main extends Application {
	private AudioFormat audioFormat;
	private TargetDataLine targetDataLine;
	private AudioClip currentPlayingClip;
	
	final Label statusLabel = new Label("Ready");
	final Button startButton = new Button("Record (R)");
	final Button stopButton = new Button("Stop (S)");
	final Button playLatestButton = new Button("Play latest record (P)");
	final ListView<String> listView = new ListView<String>();
	final VBox vbox = new VBox(15, listView);
	private String latestRecordingName;
	
	public void startRecord() {
		stopPlayback();
		stopRecord();
		
		this.statusLabel.setText("Recording");
		recordAudio();
	}
	
	public void stopRecord() {
		if(targetDataLine != null){
			targetDataLine.stop();
			targetDataLine.close();
		}
		
		this.statusLabel.setText("Ready");
		refreshRecordingsList(listView);
	}
	
	public void startPlayback() {
		final String selectedItemName = listView.getSelectionModel().getSelectedItem();
		final String fileNameToPlay = StringUtils.isNotEmpty(selectedItemName) ? selectedItemName + ".wav":  (StringUtils.isNotEmpty(latestRecordingName) ? latestRecordingName + ".wav": null);
		System.out.println("[DEBUG] Playing " + fileNameToPlay);
		playAudio(fileNameToPlay);
		this.statusLabel.setText("Playing back");
	}
	
	public boolean stopPlayback() {
		this.statusLabel.setText("Ready");
		if (currentPlayingClip != null && currentPlayingClip.isPlaying()) {
			currentPlayingClip.stop();
			return true;
		}
		return false;
	}

	public void start(Stage primaryStage) {
		startButton.setLayoutX(20);
		startButton.setLayoutY(40);
		startButton.setDisable(false);

		stopButton.setLayoutX(130);
		stopButton.setLayoutY(40);
		stopButton.setDisable(true);
		
		playLatestButton.setLayoutX(220);
		playLatestButton.setLayoutY(40);
		playLatestButton.setDisable(false);
		
		listView.setOnKeyReleased(new EventHandler<KeyEvent>() {
			public void handle(KeyEvent event) {
				if (!event.getCode().equals(KeyCode.ENTER) && 
						!event.getCode().equals(KeyCode.SPACE)) return;
				if (!stopPlayback()) startPlayback();
			};
		});
		
		listView.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent click) {
				if (click.getClickCount() == 2) {
					if (!stopPlayback()) startPlayback();
				}
			}
		});

		refreshRecordingsList(listView);
		vbox.getChildren().add(statusLabel);
		vbox.setLayoutX(20);
		vbox.setLayoutY(90);
		vbox.setMinWidth(360);
		vbox.setMaxHeight(410);

		startButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				startButton.setDisable(true);
				stopButton.setDisable(false);
				playLatestButton.setDisable(true);

				startRecord();
			}
		});

		stopButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				startButton.setDisable(false);
				stopButton.setDisable(true);
				playLatestButton.setDisable(false);

				stopRecord();
			}
		});
		
		playLatestButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				listView.getSelectionModel().select(0);
				if (!stopPlayback()) startPlayback();
			}
		});

		Pane pane = new Pane();
		Text text = new Text(20, 20, "Voice Recorder");
		pane.getChildren().addAll(text, startButton, stopButton, playLatestButton, vbox);
		
		Scene scene = new Scene(pane, 400, 520);
		primaryStage.setTitle("Zeronoise - Voice recorder");
		primaryStage.setScene(scene);
		primaryStage.getIcons().add(new Image(Thread.currentThread().getContextClassLoader().getResourceAsStream("record.png")));
		primaryStage.show();

		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				Platform.exit();
			}
		});
		
		KeyCombination R_Record_Key = new KeyCodeCombination(KeyCode.R);
		KeyCombination S_Stop_Key = new KeyCodeCombination(KeyCode.S);
		KeyCombination P_Play_Latest_Recording_Key = new KeyCodeCombination(KeyCode.P);
		KeyCombination CTRL_F5_Refresh_Recordings = new KeyCodeCombination(KeyCode.F5);
		KeyCombination CTRL_R_Reset_Sequence_Key = new KeyCodeCombination(KeyCode.R, KeyCombination.CONTROL_DOWN);
		KeyCombination CTRL_D_Delete_All_Recordings = new KeyCodeCombination(KeyCode.D, KeyCombination.CONTROL_DOWN);
		scene.getAccelerators().put(R_Record_Key, ()-> {
			startButton.setDisable(true);
			stopButton.setDisable(false);
			playLatestButton.setDisable(true);

			startRecord();
		});		
		
		scene.getAccelerators().put(S_Stop_Key, ()-> {
			startButton.setDisable(false);
			stopButton.setDisable(true);
			playLatestButton.setDisable(false);

			stopRecord();
		});	
		
		scene.getAccelerators().put(P_Play_Latest_Recording_Key, ()-> {
			listView.getSelectionModel().select(0);
			if (!stopPlayback()) startPlayback();
		});		
		
		scene.getAccelerators().put(CTRL_R_Reset_Sequence_Key, ()-> {
			Preferences prefs = Preferences.userRoot().node(ZConstants.PREF_ROOT_NODE_NAME_ZERONOISE_MEDIA);
			prefs.putInt(ZConstants.PREF_KEY_PREV_SEQUENCE, 0);
			System.out.println("Sequence has been reset to zero");
		});	
		
		scene.getAccelerators().put(CTRL_F5_Refresh_Recordings, ()-> {
			refreshRecordingsList(listView);
			System.out.println("Sequence has been reset to zero");
		});	
		
		scene.getAccelerators().put(CTRL_D_Delete_All_Recordings, ()-> {
			File recordingsDir = new File(ZConstants.SYSTEM_TEMP_DIRECTORY + File.separatorChar + "zeronoise");
			if (!recordingsDir.exists()) {
				System.out.println("No records found to delete");
			}
			Alert deleteRecsAlert = new Alert(AlertType.CONFIRMATION);
			deleteRecsAlert.setContentText("Are you sure you want to delete all the previous recordings");
			Optional<ButtonType> result = deleteRecsAlert.showAndWait();
			if (result.get() == ButtonType.OK){
				// Lists all files in folder
				File fList[] = recordingsDir.listFiles(new FilenameFilter() {
					@Override
					public boolean accept(File dir, String name) {
						if (name.endsWith(".wav")) {
							return true;
						}
						return false;
					}
				});
				// Searchs .lck
				int filesDeletedCount = 0;
				for (int i = 0; i < fList.length; i++) {
					File record = fList[i];
					if (!record.exists()) continue;
				    if (!fList[i].delete()) System.out.println("Unable to delete: " + record.getName());
				    else ++filesDeletedCount;
				}
				refreshRecordingsList(listView);
				System.out.println(filesDeletedCount + " recording(s) have been deleted");
			} else {
				System.out.println("User cancelled the request");
			}
		});	
	}
	
	private void refreshRecordingsList(ListView<String> listView) {
		File dirToReadFrom = new File(ZConstants.SYSTEM_TEMP_DIRECTORY + File.separatorChar + "zeronoise");
		File[] listFiles = dirToReadFrom.listFiles();
		listView.getItems().clear();
		latestRecordingName = null;
		if (listFiles == null || listFiles.length == 0) return;
		Arrays.sort(listFiles, new Comparator<File>() {
			public int compare(File f1, File f2) {
				return Long.valueOf(f2.lastModified()).compareTo(f1.lastModified());
			}
		});
		latestRecordingName = listFiles[0].getName();
		for (File file : listFiles) {
			listView.getItems().add(file.getName().replaceAll(".wav", ""));
		}
	}

	private void recordAudio() {

		try {
			this.audioFormat = new AudioFormat(8000.0F, 16, 1, true, false);
			DataLine.Info dataLineInfo = new DataLine.Info(TargetDataLine.class, audioFormat);
			this.targetDataLine = (TargetDataLine) AudioSystem.getLine(dataLineInfo);

			RecordingThread recording = new RecordingThread(this.targetDataLine, this.audioFormat);
			recording.start();
		}

		catch (LineUnavailableException e) {
			e.printStackTrace();
			System.exit(0);
		}
	}

	public void playAudio(String fileNameToPlay) {
		if (fileNameToPlay == null) return;
		try {
			PlayingThread pt = new PlayingThread(fileNameToPlay);
			currentPlayingClip = pt.call();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}