package com.example.talon.player;

import com.example.talon.TalonApplication;
import javazoom.jl.player.Player;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.nio.file.Paths;

@UtilityClass
@Slf4j
public class MusicPlayerUtil {

  public static void runMusic() {
    String audioFilePath = "sound.mp3";

    try {
      BufferedInputStream buffer = new BufferedInputStream(new FileInputStream(Paths.get("src", "main", "resources", audioFilePath).toFile()));
      Player mp3Player = new Player(buffer);
      mp3Player.play();

    } catch (Exception ex) {
      log.info("Error occurred during playback process:" + ex.getMessage());
    }
  }

}
