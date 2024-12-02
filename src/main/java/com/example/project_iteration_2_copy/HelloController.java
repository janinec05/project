package com.example.project_iteration_2_copy;

// Importing necessary JavaFX classes
import javafx.fxml.FXML;
import javafx.scene.control.Label;

/**
 * The HelloController class manages the UI components and handles user interactions.
 * This is connected to an FXML file that defines the user interface layout.
 */
public class HelloController {

    // The Label in the FXML file that displays a welcome message.
    @FXML
    private Label welcomeText;

    /**
     * This method is called when the associated button in the UI is clicked.
     * It updates the welcomeText Label with a welcome message.
     * The method is linked to the button's onAction event in the FXML file.
     */
    @FXML
    protected void onHelloButtonClick() {
        // Set the text of the welcomeText Label to a welcome message.
        welcomeText.setText("Welcome to JavaFX Application!");
    }
}
