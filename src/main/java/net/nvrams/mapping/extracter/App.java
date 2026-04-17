package net.nvrams.mapping.extracter;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class App extends Application {

  @Override
  public void start(Stage primaryStage) throws Exception {
    FXMLLoader loader = new FXMLLoader(getClass().getResource("MainView.fxml"));
    Scene scene = new Scene(loader.load(), 900, 820);
    scene.getStylesheets().add(getClass().getResource("style.css").toExternalForm());

    primaryStage.setTitle("VPX ROM Extracter");
    primaryStage.setScene(scene);
    primaryStage.setMinWidth(800);
    primaryStage.setMinHeight(600);
    primaryStage.show();
  }

  public static void main(String[] args) {
    launch(args);
  }
}
