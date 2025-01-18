package agh.ics.darwin.presenter;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

public class SettingsPresenter {

    @FXML
    private TextField mapWidthField, mapHeightField, numPlantsStartField, energyBoostField, numPlantsGrowField;
    @FXML
    private ComboBox<String> plantGrowthVariantBox, behaviorVariantBox;
    @FXML
    private TextField numAnimalsStartField, energyValueStartField, minEnergyReproduceField, energyTransferField;
    @FXML
    private TextField minMutationsField, maxMutationsField, genomeLengthField;
    @FXML
    private Button startButton, exportButton, importButton;

    @FXML
    private void initialize() {
        plantGrowthVariantBox.setValue("Forested Equator");
        behaviorVariantBox.setValue("Standard");
    }

    @FXML
    private void handleStartButton() {
        if (validateInputs()) {
            try {
                FXMLLoader loader = new FXMLLoader();
                loader.setLocation(getClass().getClassLoader().getResource("simulation.fxml"));
                Parent root = loader.load();
                Stage simulationStage = new Stage();
                simulationStage.setTitle("Simulation");
                simulationStage.setScene(new Scene(root, 400, 300));
                simulationStage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean validateInputs() {
        try {
            int mapWidth = Integer.parseInt(mapWidthField.getText());
            int mapHeight = Integer.parseInt(mapHeightField.getText());
            int numPlantsStart = Integer.parseInt(numPlantsStartField.getText());
            int energyBoost = Integer.parseInt(energyBoostField.getText());
            int numPlantsGrow = Integer.parseInt(numPlantsGrowField.getText());
            int numAnimalsStart = Integer.parseInt(numAnimalsStartField.getText());
            int energyValueStart = Integer.parseInt(energyValueStartField.getText());
            int minEnergyReproduce = Integer.parseInt(minEnergyReproduceField.getText());
            int energyTransfer = Integer.parseInt(energyTransferField.getText());
            int minMutations = Integer.parseInt(minMutationsField.getText());
            int maxMutations = Integer.parseInt(maxMutationsField.getText());
            int genomeLength = Integer.parseInt(genomeLengthField.getText());

            if (mapWidth <= 0 || mapHeight <= 0 || energyBoost <= 0 || numPlantsGrow <= 0 || numAnimalsStart <= 0 ||
                    energyValueStart <= 0 || minEnergyReproduce <= 0 || energyTransfer <= 0 || minMutations < 0 ||
                    maxMutations < 0 || genomeLength <= 0 || numPlantsStart <= 0 || numPlantsStart >= mapWidth * mapHeight ||
                    numAnimalsStart <= 0 || numAnimalsStart >= mapWidth * mapHeight) {
                showAlert("Validation Error", "All values must be greater than 0 and within valid ranges.");
                return false;
            }
        } catch (NumberFormatException e) {
            showAlert("Validation Error", "All fields must contain numeric values.");
            return false;
        }
        return true;
    }

    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    private void handleExportButton() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Settings");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Darwin Files", "*.dwfile"));

        String date = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss").format(new Date());
        fileChooser.setInitialFileName("my_config_" + date + ".dwfile");

        File file = fileChooser.showSaveDialog(exportButton.getScene().getWindow());
        if (file != null) {
            try (OutputStream output = new FileOutputStream(file)) {
                Properties prop = new Properties();
                prop.setProperty("mapWidth", mapWidthField.getText());
                prop.setProperty("mapHeight", mapHeightField.getText());
                prop.setProperty("numPlantsStart", numPlantsStartField.getText());
                prop.setProperty("energyBoost", energyBoostField.getText());
                prop.setProperty("numPlantsGrow", numPlantsGrowField.getText());
                prop.setProperty("plantGrowthVariant", plantGrowthVariantBox.getValue());
                prop.setProperty("numAnimalsStart", numAnimalsStartField.getText());
                prop.setProperty("energyValueStart", energyValueStartField.getText());
                prop.setProperty("minEnergyReproduce", minEnergyReproduceField.getText());
                prop.setProperty("energyTransfer", energyTransferField.getText());
                prop.setProperty("minMutations", minMutationsField.getText());
                prop.setProperty("maxMutations", maxMutationsField.getText());
                prop.setProperty("genomeLength", genomeLengthField.getText());
                prop.setProperty("behaviorVariant", behaviorVariantBox.getValue());
                prop.store(output, null);
            } catch (IOException io) {
                io.printStackTrace();
            }
        }
    }

    @FXML
    private void handleImportButton() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Settings");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Darwin Files", "*.dwfile"));
        File file = fileChooser.showOpenDialog(importButton.getScene().getWindow());
        if (file != null) {
            try (InputStream input = new FileInputStream(file)) {
                Properties prop = new Properties();
                prop.load(input);
                mapWidthField.setText(prop.getProperty("mapWidth"));
                mapHeightField.setText(prop.getProperty("mapHeight"));
                numPlantsStartField.setText(prop.getProperty("numPlantsStart"));
                energyBoostField.setText(prop.getProperty("energyBoost"));
                numPlantsGrowField.setText(prop.getProperty("numPlantsGrow"));
                plantGrowthVariantBox.setValue(prop.getProperty("plantGrowthVariant"));
                numAnimalsStartField.setText(prop.getProperty("numAnimalsStart"));
                energyValueStartField.setText(prop.getProperty("energyValueStart"));
                minEnergyReproduceField.setText(prop.getProperty("minEnergyReproduce"));
                energyTransferField.setText(prop.getProperty("energyTransfer"));
                minMutationsField.setText(prop.getProperty("minMutations"));
                maxMutationsField.setText(prop.getProperty("maxMutations"));
                genomeLengthField.setText(prop.getProperty("genomeLength"));
                behaviorVariantBox.setValue(prop.getProperty("behaviorVariant"));
            } catch (IOException io) {
                io.printStackTrace();
            }
        }
    }
}