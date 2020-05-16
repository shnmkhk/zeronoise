package com.rabbit.zeronoise;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.DataLine;
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

	public void start(Stage primaryStage) {
		final Button startButton = new Button("Start");
		final Button stopButton = new Button("Stop");
		final Button playButton = new Button("Play");

		startButton.setLayoutX(40);
		startButton.setLayoutY(40);
		startButton.setDisable(false);

		stopButton.setLayoutX(100);
		stopButton.setLayoutY(40);
		stopButton.setDisable(true);

		playButton.setLayoutX(160);
		playButton.setLayoutY(40);
		playButton.setDisable(false);

		startButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				startButton.setDisable(true);
				stopButton.setDisable(false);
				playButton.setDisable(true);

				recordAudio();
			}
		});

		stopButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent e) {
				startButton.setDisable(false);
				stopButton.setDisable(true);
				playButton.setDisable(false);

				targetDataLine.stop();
				targetDataLine.close();
			}
		});

		playButton.setOnAction(new EventHandler<ActionEvent>() {
			@Override
			public void handle(ActionEvent event) {
				playButton.setDisable(true);
				playAudio();
				playButton.setDisable(false);
			}
		});

		Pane pane = new Pane();
		Text text = new Text(20, 20, "Voice Recorder");
		pane.getChildren().addAll(text, startButton, stopButton, playButton);

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

	public void playAudio() {
		try {
			PlayingThread pt = new PlayingThread();
			Clip clip = pt.call();
			while (true) {
				if (!clip.isRunning())
					break;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		launch(args);
	}
}