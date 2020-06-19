package com.rabbit.zeronoise;

import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

import com.rabbit.zeronoise.util.ZConstants;
import com.sun.media.jfxmedia.AudioClip;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
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

	public void start(Stage primaryStage) {
		final Button startButton = new Button("Start");
		final Button stopButton = new Button("Stop");
		final Button playOrStopButton = new Button("Play/ Stop");
		final Label statusLabel = new Label("Ready");
		final ListView<String> listView = new ListView<String>();
		final VBox vbox = new VBox(listView);
		
		startButton.setLayoutX(40);
		startButton.setLayoutY(40);
		startButton.setDisable(false);

		stopButton.setLayoutX(100);
		stopButton.setLayoutY(40);
		stopButton.setDisable(true);

		playOrStopButton.setLayoutX(160);
		playOrStopButton.setLayoutY(40);
		playOrStopButton.setDisable(false);
		
		listView.setOnMouseClicked(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent click) {
				if (click.getClickCount() == 2) {
					if (currentPlayingClip != null && currentPlayingClip.isPlaying()) {
						currentPlayingClip.stop();
					}
					final String fileNameToPlay = listView.getSelectionModel().getSelectedItem() + ".wav";
					playAudio(fileNameToPlay);
				}
			}
		});

		refreshRecordingsList(listView);
		vbox.getChildren().add(statusLabel);
		vbox.setLayoutX(20);
		vbox.setLayoutY(90);
		vbox.setMinWidth(360);
		vbox.setMaxHeight(410);
		statusLabel.setPadding(new Insets(15, 0, 0, 0));

		startButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				startButton.setDisable(true);
				stopButton.setDisable(false);

				recordAudio();
			}
		});

		stopButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				startButton.setDisable(false);
				stopButton.setDisable(true);

				targetDataLine.stop();
				targetDataLine.close();

				refreshRecordingsList(listView);
			}
		});

		playOrStopButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if (currentPlayingClip != null && currentPlayingClip.isPlaying()) {
					currentPlayingClip.stop();
					currentPlayingClip = null;
				} else {
					playAudio(null);
				}
			}
		});

		Pane pane = new Pane();
		Text text = new Text(20, 20, "Voice Recorder");
		pane.getChildren().addAll(text, startButton, stopButton, playOrStopButton, vbox);

		Scene scene = new Scene(pane, 400, 520);
		primaryStage.setTitle("Zero noise");
		primaryStage.setScene(scene);
		primaryStage.show();

		primaryStage.setOnCloseRequest(new EventHandler<WindowEvent>() {
			@Override
			public void handle(WindowEvent event) {
				Platform.exit();
			}
		});
	}
	
	private void refreshRecordingsList(ListView<String> listView) {
		File dirToReadFrom = new File(ZConstants.SYSTEM_TEMP_DIRECTORY + File.separatorChar + "zeronoise");
		File[] listFiles = dirToReadFrom.listFiles();
		Arrays.sort(listFiles, new Comparator<File>() {
			public int compare(File f1, File f2) {
				return Long.valueOf(f2.lastModified()).compareTo(f1.lastModified());
			}
		});
		listView.getItems().clear();
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