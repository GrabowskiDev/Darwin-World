package agh.ics.darwin.presenter;

import agh.ics.darwin.Simulation;
import agh.ics.darwin.model.*;
import agh.ics.darwin.model.variants.BehaviourVariant;
import agh.ics.darwin.model.variants.MapVariant;
import agh.ics.darwin.model.variants.MutationVariant;
import agh.ics.darwin.model.variants.PlantGrowthVariant;
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
import java.util.Objects;
import java.util.Properties;

import static java.lang.Integer.parseInt;

public class SettingsPresenter {

    @FXML
    private TextField mapWidthField, mapHeightField, numPlantsStartField, energyBoostField,
            numPlantsGrowField, numAnimalsStartField, energyValueStartField, minEnergyReproduceField,
            energyTransferField, minMutationsField, maxMutationsField, genomeLengthField;
    @FXML
    private ComboBox<String> plantGrowthVariantBox, behaviorVariantBox;
    @FXML
    private Button exportButton, importButton;

    @FXML
    private void initialize() {
        plantGrowthVariantBox.setValue("Forested Equator");
        behaviorVariantBox.setValue("Full Predestination");

        addFocusListeners();
    }

    private void addFocusListeners() {
        TextField[] fields = {mapWidthField, mapHeightField, numPlantsStartField, energyBoostField,
                numPlantsGrowField, numAnimalsStartField, energyValueStartField, minEnergyReproduceField,
                energyTransferField, minMutationsField, maxMutationsField, genomeLengthField};

        for (TextField field : fields) {
            field.focusedProperty().addListener((observable, oldValue, newValue) -> {
                if (!newValue) {
                    validateInputs();
                }
            });
        }
    }

    @FXML
    private void handleStartButton() {
        if (validateInputs()) {
            Parameters parameters = getParameters();
            Simulation simulation = new Simulation(parameters);
            WorldMap map = simulation.getMap();

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/simulation.fxml"));
                Parent root = loader.load();
                SimulationPresenter simulationPresenter = loader.getController();
                simulationPresenter.setSimulation(simulation);
                simulationPresenter.setMap(map);
                simulationPresenter.displayMap(map);

                Stage simulationStage = new Stage();
                simulationStage.setTitle("Simulation");
                Scene scene = new Scene(root, 1000, 800);
                simulationStage.setScene(scene);
                simulationPresenter.setStage(simulationStage);
                simulationStage.setMinWidth(850);
                simulationStage.setMinHeight(700);
                simulationStage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private Parameters getParameters() {
        return new Parameters(
                parseInt(mapWidthField.getText()),
                parseInt(mapHeightField.getText()),
                MapVariant.Globe,
                parseInt(numPlantsStartField.getText()),
                parseInt(energyBoostField.getText()),
                parseInt(numPlantsGrowField.getText()),
                Objects.equals(plantGrowthVariantBox.getValue(), "Forested Equator") ? PlantGrowthVariant.Equator : PlantGrowthVariant.GoodHarvest,
                parseInt(numAnimalsStartField.getText()),
                parseInt(energyValueStartField.getText()),
                parseInt(minEnergyReproduceField.getText()),
                parseInt(energyTransferField.getText()),
                parseInt(minMutationsField.getText()),
                parseInt(maxMutationsField.getText()),
                MutationVariant.Random,
                parseInt(genomeLengthField.getText()),
                Objects.equals(behaviorVariantBox.getValue(), "Full Predestination") ? BehaviourVariant.Predestination : BehaviourVariant.Madness
        );
    }

    private boolean validateInputs() {
        try {
            validateField(mapWidthField, 1, Integer.MAX_VALUE);
            validateField(mapHeightField, 1, Integer.MAX_VALUE);
            validateField(numPlantsStartField, 0, parseInt(mapWidthField.getText()) * parseInt(mapHeightField.getText()));
            validateField(energyBoostField, 0, Integer.MAX_VALUE);
            validateField(numPlantsGrowField, 0, Integer.MAX_VALUE);
            validateField(numAnimalsStartField, 1, Integer.MAX_VALUE);
            validateField(energyValueStartField, 1, Integer.MAX_VALUE);
            validateField(minEnergyReproduceField, 1, Integer.MAX_VALUE);
            validateField(energyTransferField, 1, Integer.MAX_VALUE);
            validateField(minMutationsField, 0, Integer.MAX_VALUE);
            validateField(maxMutationsField, 0, Integer.MAX_VALUE);
            validateField(genomeLengthField, 1, Integer.MAX_VALUE);

            if (parseInt(maxMutationsField.getText()) < parseInt(minMutationsField.getText())) {
                maxMutationsField.setText(minMutationsField.getText());
            }
            if (parseInt(maxMutationsField.getText()) > parseInt(genomeLengthField.getText())) {
                maxMutationsField.setText(genomeLengthField.getText());
            }
            if (parseInt(minEnergyReproduceField.getText()) > parseInt(energyTransferField.getText())) {
                minEnergyReproduceField.setText(energyTransferField.getText());
            }

        } catch (NumberFormatException e) {
            showAlert("Validation Error", "All fields must contain numeric values.");
            return false;
        }
        return true;
    }

    private void validateField(TextField field, int minValue, int maxValue) {
        int value = parseInt(field.getText());
        if (value < minValue) {
            field.setText(String.valueOf(minValue));
        } else if (value > maxValue) {
            field.setText(String.valueOf(maxValue));
        }
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
            savePropertiesToFile(file);
        }
    }

    private void savePropertiesToFile(File file) {
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

    @FXML
    private void handleImportButton() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open Settings");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Darwin Files", "*.dwfile"));
        File file = fileChooser.showOpenDialog(importButton.getScene().getWindow());
        if (file != null) {
            loadPropertiesFromFile(file);
        }
    }

    private void loadPropertiesFromFile(File file) {
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