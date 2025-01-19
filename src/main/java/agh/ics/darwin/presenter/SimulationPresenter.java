package agh.ics.darwin.presenter;

import agh.ics.darwin.Simulation;
import agh.ics.darwin.model.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SimulationPresenter implements MapChangeListener {

    @FXML
    private Label simulationLabel;

    @FXML
    private GridPane mapGrid;

    @FXML
    private Label numAnimalsLabel;

    @FXML
    private Label numPlantsLabel;

    @FXML
    private Label numFreeFieldsLabel;

    @FXML
    private Label avgEnergyLabel;

    @FXML
    private Label avgLifespanLabel;

    @FXML
    private Label avgChildrenLabel;

    @FXML
    private VBox genotypeList;

    @FXML
    private Slider speedSlider;

    @FXML
    private Button startButton;

    @FXML
    private Button statsButton;


    private Simulation simulation;

    private WorldMap map;

    public void setMap(WorldMap map) {
        this.map = map;
    }

    public void setSimulation(Simulation simulation) {
        this.simulation = simulation;
    }

    @FXML
    private void handleStartButton() {
        startButton.setVisible(false);
        if (simulation != null) {
            map.addObserver(this);
            new Thread(() -> {
                simulation.run();
            }).start();
        }
    }

    @FXML
    private void handleStatsButton() {
        exportStatisticsToCSV("simulation_statistics.csv", simulation.getDailyStatistics());
    }

    public void initialize() {
        speedSlider.setMin(1);
        speedSlider.setMax(1000);
        speedSlider.setValue(700);
        speedSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (simulation != null) {
                simulation.setSleepDuration(newValue.intValue());
            }
        });
    }

    public void displayMap(WorldMap map) {
        mapGrid.getChildren().clear();
        mapGrid.setAlignment(Pos.CENTER); // Center the grid
        int cellSize = 30; // Set the size of each cell

        for (int y = 0; y < map.getHeight(); y++) {
            for (int x = 0; x < map.getWidth(); x++) {
                Vector2d position = new Vector2d(x, y);
                Label cellLabel = new Label();
                if (map.isOccupiedByPlant(position)) {
                    cellLabel.setText("*");
                } else if (map.isOccupiedByAnimal(position)) {
                    Animal animal = map.getAnimals().get(position).get(0);
                    cellLabel.setText(getDirectionArrow(animal.getDirection()));
                } else {
                    cellLabel.setText(".");
                }

                StackPane cell = new StackPane(cellLabel);
                cell.setPrefSize(cellSize, cellSize); // Make each cell square
                cell.setBorder(new Border(new BorderStroke(Color.BLACK, BorderStrokeStyle.SOLID, null, new BorderWidths(1)))); // Add border to each cell

                mapGrid.add(cell, x, y);
            }
        }
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

    @Override
    public void mapChanged(WorldMap worldMap) {
        System.out.println("Map changed");
        Platform.runLater(() -> {
            displayMap(worldMap);
            updateStatistics(worldMap);
        });
    }

    private void exportStatisticsToCSV(String fileName, List<DailyStatistics> statistics) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save File");
        String date = new SimpleDateFormat("dd_MM_yyyy_HH_mm_ss").format(new Date());
        fileChooser.setInitialFileName("simulation_stats_" + date + ".csv");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showSaveDialog(statsButton.getScene().getWindow());

        if (file != null) {
            try (FileWriter writer = new FileWriter(file)) {
                writer.append("Day,Number of Animals,Number of Plants,Average Energy,Average Lifespan,Average Number of Children\n");
                for (DailyStatistics stat : statistics) {
                    writer.append(String.valueOf(stat.getDay())).append(",")
                            .append(String.valueOf(stat.getNumAnimals())).append(",")
                            .append(String.valueOf(stat.getNumPlants())).append(",")
                            .append(String.valueOf(stat.getAvgEnergy())).append(",")
                            .append(String.valueOf(stat.getAvgLifespan())).append(",")
                            .append(String.valueOf(stat.getAvgChildren())).append("\n");
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void updateStatistics(WorldMap worldMap) {
        int numAnimals = worldMap.getLivingAnimals().values().stream().mapToInt(List::size).sum();
        int numPlants = worldMap.getPlants().size();
        int numFreeFields = worldMap.getWidth() * worldMap.getHeight() - numAnimals - numPlants;
        double avgEnergy = worldMap.getAnimals().values().stream()
                .flatMap(Collection::stream)
                .mapToInt(Animal::getEnergy)
                .average()
                .orElse(0);
        double avgLifespan = worldMap.getDeadAnimals().values().stream()
                .flatMap(Collection::stream)
                .mapToInt(Animal::getAge)
                .average()
                .orElse(0);
        double avgChildren = worldMap.getLivingAnimals().values().stream()
                .flatMap(Collection::stream)
                .mapToInt(Animal::getNumberOfChildren)
                .average()
                .orElse(0);

        avgLifespan = Math.round(avgLifespan * 100.0) / 100.0;
        avgEnergy = Math.round(avgEnergy * 100.0) / 100.0;
        avgChildren = Math.round(avgChildren * 100.0) / 100.0;

        numAnimalsLabel.setText("Number of all animals: " + numAnimals);
        numPlantsLabel.setText("Number of all plants: " + numPlants);
        numFreeFieldsLabel.setText("Number of free fields: " + numFreeFields);
        avgEnergyLabel.setText("Average energy level: " + avgEnergy);
        avgLifespanLabel.setText("Average lifespan: " + avgLifespan);
        avgChildrenLabel.setText("Average number of children: " + avgChildren);

        DailyStatistics dailyStats = new DailyStatistics(
                simulation.getDay(),
                numAnimals,
                numPlants,
                avgEnergy,
                avgLifespan,
                avgChildren
        );
        simulation.addDailyStatistics(dailyStats);

        updateGenotypeList(worldMap);
    }

    private void updateGenotypeList(WorldMap worldMap) {
        Map<Genes, Long> genotypeCounts = worldMap.getAnimals().values().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.groupingBy(Animal::getGenes, Collectors.counting()));

        List<Map.Entry<Genes, Long>> sortedGenotypes = genotypeCounts.entrySet().stream()
                .sorted(Map.Entry.<Genes, Long>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toList());

        genotypeList.getChildren().clear();
        for (Map.Entry<Genes, Long> entry : sortedGenotypes) {
            Label label = new Label(entry.getKey() + ": " + entry.getValue());
            genotypeList.getChildren().add(label);
        }
    }
}