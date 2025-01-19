package agh.ics.darwin.presenter;

import agh.ics.darwin.Simulation;
import agh.ics.darwin.model.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;

import java.awt.event.WindowAdapter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

import static com.sun.java.accessibility.util.AWTEventMonitor.addWindowListener;

public class SimulationPresenter implements MapChangeListener {

    @FXML
    private Label simulationLabel;

    @FXML
    private GridPane mapGrid;

    @FXML
    private LineChart<Number, Number> statisticsChart;

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
    private Button simulationButton;

    @FXML
    private Button statsButton;

    @FXML
    private ListView<String> animalsList;

    private XYChart.Series<Number, Number> numAnimalsSeries = new XYChart.Series<>();
    private XYChart.Series<Number, Number> numPlantsSeries = new XYChart.Series<>();
    private XYChart.Series<Number, Number> numFreeFieldsSeries = new XYChart.Series<>();
    private XYChart.Series<Number, Number> avgEnergySeries = new XYChart.Series<>();
    private XYChart.Series<Number, Number> avgLifespanSeries = new XYChart.Series<>();
    private XYChart.Series<Number, Number> avgChildrenSeries = new XYChart.Series<>();

    private StackPane selectedCell = null;
    private Simulation simulation;

    private WorldMap map;

    private Boolean isRunning = false;

    private int freeFields = 0;

    private List<Animal> selectedAnimals = new ArrayList<>();

    private ImageView bg;
    private ImageView grass1;


    public void setMap(WorldMap map) {
        this.map = map;
        updateStatistics(this.map);
    }

    public void setSimulation(Simulation simulation) {
        this.simulation = simulation;
    }

    public void setStage(Stage stage) {
        stage.setOnCloseRequest((WindowEvent event) -> {
            if (simulation != null) {
                simulation.stop();
            }
        });
    }

    @FXML
    private void handleSimulationButton() {
        if (simulation != null) {
            if (isRunning) {
                simulation.pause();
                simulationButton.setText("Resume");
            } else {
                if (!simulation.isStarted()) {
                    map.addObserver(this);
                    new Thread(() -> {
                        simulation.run();
                    }).start();
                } else {
                    simulation.resume();
                }
                simulationButton.setText("Pause");
            }
            isRunning = !isRunning;
        }
    }

    @FXML
    private void handleStatsButton() {
        exportStatisticsToCSV("simulation_statistics.csv", simulation.getDailyStatistics());
    }

    public void initialize() {
        numAnimalsSeries.setName("Number of Animals");
        numPlantsSeries.setName("Number of Plants");
        numFreeFieldsSeries.setName("Number of Free Fields");
        avgEnergySeries.setName("Average Energy");
        avgLifespanSeries.setName("Average Lifespan");
        avgChildrenSeries.setName("Average Number of Children");
        statisticsChart.getData().addAll(numAnimalsSeries, numPlantsSeries, numFreeFieldsSeries, avgEnergySeries, avgLifespanSeries, avgChildrenSeries);
        speedSlider.setMin(1);
        speedSlider.setMax(1000);
        speedSlider.setValue(700);
        speedSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (simulation != null) {
                simulation.setSleepDuration(newValue.intValue());
            }
        });
        bg = new ImageView(new Image(getClass().getResourceAsStream("/img/bg.png")));
        bg.setFitWidth(20);
        bg.setFitHeight(20);

        grass1 = new ImageView(new Image(getClass().getResourceAsStream("/img/grass_1.png")));
        grass1.setFitWidth(20);
        grass1.setFitHeight(20);
    }



    public void displayMap(WorldMap map) {
        freeFields = 0;
        mapGrid.getChildren().clear();
        mapGrid.setAlignment(Pos.CENTER); // Center the grid
        int cellSize = 30; // Set the size of each cell

        for (int y = 0; y < map.getHeight(); y++) {
            for (int x = 0; x < map.getWidth(); x++) {
                Vector2d position = new Vector2d(x, y);
                StackPane cell = new StackPane();
                cell.setPrefSize(cellSize, cellSize); // Make each cell square
                

                // Add background image
                ImageView bgImageView = new ImageView(bg.getImage());
                bgImageView.setFitWidth(cellSize);
                bgImageView.setFitHeight(cellSize);
                cell.getChildren().add(bgImageView);

                Label cellLabel = new Label();
                boolean hasAnimal = false;
                Collection<Animal> animalsAtPosition;
                if (map.isOccupiedByAnimal(position)) {
                    animalsAtPosition = map.getAnimals().get(position);
                    if (animalsAtPosition != null && !animalsAtPosition.isEmpty()) {
                        List<Animal> animalsList = new CopyOnWriteArrayList<>(animalsAtPosition);
                        Animal animal = animalsList.get(0);
                        cellLabel.setGraphic(getDirectionImage(animal.getDirection()));
                        hasAnimal = true;
                    }
                } else {
                    animalsAtPosition = null;
                    if (map.isOccupiedByPlant(position)) {
                        ImageView grassImageView = new ImageView(grass1.getImage());
                        grassImageView.setFitWidth(cellSize);
                        grassImageView.setFitHeight(cellSize);
                        cell.getChildren().add(grassImageView);
                    } else {
                        freeFields++;
                    }
                }

                cell.getChildren().add(cellLabel);

                // Add mouse click event handler to change cell color only if it has an animal
                if (hasAnimal) {
                    cell.setOnMouseClicked(event -> {
                        if (!isRunning) {
                            if (selectedCell != null) {
                                selectedCell.setBackground(null); // Deselect the previously selected cell
                            }
                            cell.setBackground(new Background(new BackgroundFill(Color.YELLOW, null, null))); // Select the new cell
                            selectedCell = cell; // Update the selected cell reference
                            showSelectedAnimals(animalsAtPosition);
                        }
                    });
                }

                mapGrid.add(cell, x, y);
            }
        }
    }

    private void showSelectedAnimals(Collection<Animal> animals) {
        selectedAnimals = new ArrayList<>(animals);
        Platform.runLater(() -> {
            animalsList.getItems().clear();
            for (Animal animal : selectedAnimals) {
                animalsList.getItems().add(animal.toString());
            }
        });
    }

    private ImageView getDirectionImage(MapDirection direction) {
        String imagePath = switch (direction) {
            case NORTH -> "/img/kot_0.png";
            case NORTHEAST -> "/img/kot_1.png";
            case EAST -> "/img/kot_2.png";
            case SOUTHEAST -> "/img/kot_3.png";
            case SOUTH -> "/img/kot_4.png";
            case SOUTHWEST -> "/img/kot_5.png";
            case WEST -> "/img/kot_6.png";
            case NORTHWEST -> "/img/kot_7.png";
        };
        Image image = new Image(getClass().getResourceAsStream(imagePath));
        ImageView imageView = new ImageView(image);
        imageView.setFitWidth(20); // Set the desired width
        imageView.setFitHeight(20);
        return imageView;
    }

    @Override
    public synchronized void mapChanged(WorldMap worldMap) {
        Platform.runLater(() -> {
            synchronized (worldMap) {
                displayMap(worldMap);
                updateStatistics(worldMap);
                System.out.println("Map changed");
            }
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
        List<Animal> livingAnimals;
        List<Animal> deadAnimals;

        synchronized (worldMap) {
            livingAnimals = worldMap.getAnimals().values().stream()
                    .flatMap(Collection::stream)
                    .filter(Objects::nonNull)
                    .toList();
            deadAnimals = worldMap.getDeadAnimals().values().stream()
                    .flatMap(Collection::stream)
                    .filter(Objects::nonNull)
                    .toList();
        }

        // Create a copy of the list before iterating
        List<Animal> livingAnimalsCopy = new ArrayList<>(livingAnimals);
        List<Animal> deadAnimalsCopy = new ArrayList<>(deadAnimals);



        // Use the copies for further processing
        int numAnimals = livingAnimalsCopy.size();
        int numPlants = worldMap.getPlants().size();
        int numFreeFields = freeFields;
        double avgEnergy = livingAnimalsCopy.stream()
                .mapToInt(Animal::getEnergy)
                .average()
                .orElse(0);
        double avgLifespan = deadAnimalsCopy.stream()
                .mapToInt(Animal::getAge)
                .average()
                .orElse(0);
        double avgChildren = livingAnimalsCopy.stream()
                .mapToInt(Animal::getNumberOfChildren)
                .average()
                .orElse(0);

        avgLifespan = Math.round(avgLifespan * 100.0) / 100.0;
        avgEnergy = Math.round(avgEnergy * 100.0) / 100.0;
        avgChildren = Math.round(avgChildren * 100.0) / 100.0;

        double finalAvgEnergy = avgEnergy;
        double finalAvgLifespan = avgLifespan;
        double finalAvgChildren = avgChildren;
        Platform.runLater(() -> {
            numAnimalsLabel.setText("Number of all animals: " + numAnimals);
            numPlantsLabel.setText("Number of all plants: " + numPlants);
            numFreeFieldsLabel.setText("Number of free fields: " + numFreeFields);
            avgEnergyLabel.setText("Average energy level: " + finalAvgEnergy);
            avgLifespanLabel.setText("Average lifespan: " + finalAvgLifespan);
            avgChildrenLabel.setText("Average number of children: " + finalAvgChildren);

            int day = simulation.getDay();
            numAnimalsSeries.getData().add(new XYChart.Data<>(day, numAnimals));
            numPlantsSeries.getData().add(new XYChart.Data<>(day, numPlants));
            numFreeFieldsSeries.getData().add(new XYChart.Data<>(day, numFreeFields));
            avgEnergySeries.getData().add(new XYChart.Data<>(day, finalAvgEnergy));
            avgLifespanSeries.getData().add(new XYChart.Data<>(day, finalAvgLifespan));
            avgChildrenSeries.getData().add(new XYChart.Data<>(day, finalAvgChildren));
        });

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
        Map<Genes, Long> genotypeCounts = new HashMap<>();
        // Create a copy of the collection to avoid concurrent modification
        List<Animal> animalsCopy = worldMap.getAnimals().values().stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toList());

        animalsCopy.forEach(animal -> genotypeCounts.merge(animal.getGenes(), 1L, Long::sum));

        List<Map.Entry<Genes, Long>> sortedGenotypes = genotypeCounts.entrySet().stream()
                .sorted(Map.Entry.<Genes, Long>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toList());

        Platform.runLater(() -> {
            genotypeList.getChildren().clear();
            for (Map.Entry<Genes, Long> entry : sortedGenotypes) {
                Label label = new Label(entry.getKey() + ": " + entry.getValue());
                genotypeList.getChildren().add(label);
            }
        });
    }
}