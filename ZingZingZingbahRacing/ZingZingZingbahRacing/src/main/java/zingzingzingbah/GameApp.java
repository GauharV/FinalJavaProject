package zingzingzingbah;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import zingzingzingbah.model.Character;
import zingzingzingbah.screen.CharacterSelectScreen;
import zingzingzingbah.screen.RaceScreen;

/**
 * Main JavaFX Application class.
 * Manages the primary Stage and transitions between screens.
 */
public class GameApp extends Application {

    private Stage primaryStage;

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        stage.setTitle("Zing Zing Zingbah Racing");
        stage.setResizable(false);
        showCharacterSelect();
        stage.show();
    }

    /**
     * Shows the character selection screen.
     */
    public void showCharacterSelect() {
        CharacterSelectScreen selectScreen = new CharacterSelectScreen(this);
        Scene scene = new Scene(selectScreen, 900, 600);
        primaryStage.setScene(scene);
    }

    /**
     * Starts the race with the given selected character.
     * @param selectedCharacter the character the player chose
     */
    public void startRace(Character selectedCharacter) {
        RaceScreen raceScreen = new RaceScreen(this, selectedCharacter);
        Scene scene = new Scene(raceScreen, 900, 600);
        scene.setOnKeyPressed(raceScreen::handleKeyPressed);
        scene.setOnKeyReleased(raceScreen::handleKeyReleased);
        primaryStage.setScene(scene);
        raceScreen.startGame();
    }
}
