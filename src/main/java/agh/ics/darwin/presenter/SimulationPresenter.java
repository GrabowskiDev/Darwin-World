package agh.ics.darwin.presenter;

import javafx.fxml.FXML;
import javafx.scene.control.Label;

public class SimulationPresenter {

    @FXML
    private Label simulationLabel;

    @FXML
    private void initialize() {
        simulationLabel.setText("Simulation in progress...");
    }
}