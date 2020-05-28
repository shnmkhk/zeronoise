package com.rabbit.zeronoise;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineEvent;
import javax.sound.sampled.LineListener;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

@SuppressWarnings("restriction")
public class Main extends Application {
	private AudioFormat audioFormat;
	private TargetDataLine targetDataLine;
	private Clip currentPlayingClip;

	public void start(Stage primaryStage) {
		final Button startButton = new Button("Start");
		final Button stopButton = new Button("Stop");
		final Button playOrStopButton = new Button("Play/ Stop");

		startButton.setLayoutX(40);
		startButton.setLayoutY(40);
		startButton.setDisable(false);

		stopButton.setLayoutX(100);
		stopButton.setLayoutY(40);
		stopButton.setDisable(true);

		playOrStopButton.setLayoutX(160);
		playOrStopButton.setLayoutY(40);
		playOrStopButton.setDisable(false);

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
			}
		});

		playOrStopButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				if (currentPlayingClip != null) {
					currentPlayingClip.stop();
					currentPlayingClip = null;
				} else {
					currentPlayingClip = playAudio();
				}
			}
		});

		Pane pane = new Pane();
		Text text = new Text(20, 20, "Voice Recorder");
		pane.getChildren().addAll(text, startButton, stopButton, playOrStopButton);

		Scene scene = new Scene(pane, 400, 100);
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

	public Clip playAudio() {
		try {
			PlayingThread pt = new PlayingThread();
			return pt.call();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public static void main(String[] args) {
		launch(args);
	}
}