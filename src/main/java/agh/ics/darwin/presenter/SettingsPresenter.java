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
import javafx.scene.layout.VBox;
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
        behaviorVariantBox.setValue("Full Predestination");
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
                Objects.equals(behaviorVariantBox.getValue(), "Full Predestination") ? BehaviourVariant.Predestination : BehaviourVariant.Madness // You can add a dropdown in the FXML to select this
        );
    }

    private void displayMap(WorldMap map, VBox vbox) {
        StringBuilder mapDisplay = new StringBuilder();
        for (int y = 0; y < map.getHeight(); y++) {
            for (int x = 0; x < map.getWidth(); x++) {
                Vector2d position = new Vector2d(x, y);
                if (map.isOccupiedByPlant(position)) {
                    mapDisplay.append("* ");
                } else if (map.isOccupiedByAnimal(position)) {
                    Animal animal = map.getAnimals().get(position).get(0);
                    mapDisplay.append(getDirectionArrow(animal.getDirection())).append(" ");
                } else {
                    mapDisplay.append(". ");
                }
            }
            mapDisplay.append("\n");
        }
        Label mapLabel = new Label(mapDisplay.toString());
        vbox.getChildren().add(mapLabel);
    }

    private String getDirectionArrow(MapDirection direction) {
        return switch (direction) {
            case NORTH -> "↑";
            case NORTHEAST -> "↗";
            case EAST -> "→";
            case SOUTHEAST -> "↘";
            case SOUTH -> "↓";
            case SOUTHWEST -> "↙";
            case WEST -> "←";
            case NORTHWEST -> "↖";
        };
    }

    private boolean validateInputs() {
        try {
            int mapWidth = parseInt(mapWidthField.getText());
            int mapHeight = parseInt(mapHeightField.getText());
            int numPlantsStart = parseInt(numPlantsStartField.getText());
            int energyBoost = parseInt(energyBoostField.getText());
            int numPlantsGrow = parseInt(numPlantsGrowField.getText());
            int numAnimalsStart = parseInt(numAnimalsStartField.getText());
            int energyValueStart = parseInt(energyValueStartField.getText());
            int minEnergyReproduce = parseInt(minEnergyReproduceField.getText());
            int energyTransfer = parseInt(energyTransferField.getText());
            int minMutations = parseInt(minMutationsField.getText());
            int maxMutations = parseInt(maxMutationsField.getText());
            int genomeLength = parseInt(genomeLengthField.getText());

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