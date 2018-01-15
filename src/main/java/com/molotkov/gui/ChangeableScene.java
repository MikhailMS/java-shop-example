package com.molotkov.gui;

import javafx.scene.layout.Pane;
import javafx.stage.Stage;

public abstract class ChangeableScene {

    static void changeScene(final Stage primaryStage, final Pane pane) {
        primaryStage.getScene().setRoot(pane);
    }
}
