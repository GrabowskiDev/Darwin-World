package agh.ics.darwin.presenter;

import agh.ics.darwin.Simulation;
import agh.ics.darwin.model.*;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
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
import java.util.function.ToIntFunction;
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
    private HBox animalImages;
    @FXML
    private VBox animalDetails;
    @FXML
    private Label genomeLabel;
    @FXML
    private Label activatedGenomePartLabel;
    @FXML
    private Label energyLabel;
    @FXML
    private Label eatenPlantsLabel;
    @FXML
    private Label numberOfChildrenLabel;
    @FXML
    private Label numberOfDescendantsLabel;
    @FXML
    private Label IDLabel;
    @FXML
    private Label lifespanLabel;
    @FXML
    private Label dayOfDeathLabel;
    @FXML
    private ScrollPane mapScrollPane;

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
    private int selectedAnimalIndex = 0;

    private ImageView bg;
    private ImageView grass1;

    private double initialX;
    private double initialY;
    private double initialHValue;
    private double initialVValue;

    private int cellSize = 30;
    private int fontSize = 12;

    private Vector2d selectedCellPosition = null;

    private Map<MapDirection, Image> directionImages = new HashMap<>();


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
            if (selectedCell != null) {
                int borderWidth = Math.max(1, cellSize / 15);
                selectedCell.setStyle("-fx-border-color: black; -fx-border-width: " + borderWidth + ";");
                selectedCell = null;
                selectedCellPosition = null;
            }
        }
    }

    @FXML
    private void handleStatsButton() {
        exportStatisticsToCSV("simulation_statistics.csv", simulation.getDailyStatistics());
    }

    @FXML
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

        animalDetails.setVisible(true);

        // Add scroll event handler to mapGrid
        mapGrid.setOnScroll(event -> {
            if (event.getDeltaY() > 0) {
                cellSize = cellSize + 5; // Increase cell size but not more than 100
                fontSize = fontSize + 1; // Increase font size but not more than 30
            } else {
                cellSize = Math.max(20, cellSize - 5); // Decrease cell size but not less than 10
                fontSize = Math.max(14, fontSize - 1); // Decrease font size but not less than 8
            }
            displayMap(map); // Redraw the map with the new cell size and font size
        });

        // Set fixed size for mapGrid and wrap it in a ScrollPane
        mapGrid.setPrefSize(800, 600); // Set the desired fixed size
        mapScrollPane.setContent(mapGrid);

        // Add mouse event handlers for dragging
        mapGrid.setOnMousePressed(event -> {
            initialX = event.getSceneX();
            initialY = event.getSceneY();
            initialHValue = mapScrollPane.getHvalue();
            initialVValue = mapScrollPane.getVvalue();
        });

        mapGrid.setOnMouseDragged(event -> {
            double deltaX = event.getSceneX() - initialX;
            double deltaY = event.getSceneY() - initialY;
            double newHValue = initialHValue - deltaX / mapGrid.getWidth();
            double newVValue = initialVValue - deltaY / mapGrid.getHeight();
            mapScrollPane.setHvalue(newHValue);
            mapScrollPane.setVvalue(newVValue);
        });

        directionImages.put(MapDirection.NORTH, new Image(getClass().getResourceAsStream("/img/kot_0.png")));
        directionImages.put(MapDirection.NORTHEAST, new Image(getClass().getResourceAsStream("/img/kot_1.png")));
        directionImages.put(MapDirection.EAST, new Image(getClass().getResourceAsStream("/img/kot_2.png")));
        directionImages.put(MapDirection.SOUTHEAST, new Image(getClass().getResourceAsStream("/img/kot_3.png")));
        directionImages.put(MapDirection.SOUTH, new Image(getClass().getResourceAsStream("/img/kot_4.png")));
        directionImages.put(MapDirection.SOUTHWEST, new Image(getClass().getResourceAsStream("/img/kot_5.png")));
        directionImages.put(MapDirection.WEST, new Image(getClass().getResourceAsStream("/img/kot_6.png")));
        directionImages.put(MapDirection.NORTHWEST, new Image(getClass().getResourceAsStream("/img/kot_7.png")));

    }



    public void displayMap(WorldMap map) {
        freeFields = 0;
        mapGrid.getChildren().clear();
        mapGrid.setAlignment(Pos.CENTER);

        List<Node> cells = new ArrayList<>();
        for (int y = 0; y < map.getHeight(); y++) {
            for (int x = 0; x < map.getWidth(); x++) {
                Vector2d position = new Vector2d(x, y);
                StackPane cell = createCell(position);
                cells.add(cell);
                GridPane.setColumnIndex(cell, x);
                GridPane.setRowIndex(cell, y);
            }
        }
        mapGrid.getChildren().addAll(cells);
    }

    private StackPane createCell(Vector2d position) {
        StackPane cell = new StackPane();
        cell.setPrefSize(cellSize, cellSize);

        ImageView bgImageView = new ImageView(bg.getImage());
        bgImageView.setFitWidth(cellSize);
        bgImageView.setFitHeight(cellSize);
        cell.getChildren().add(bgImageView);

        Label cellLabel = new Label();
        boolean hasAnimal = false;
        Collection<Animal> animalsAtPosition = map.getAnimals().get(position);

        if (animalsAtPosition != null && !animalsAtPosition.isEmpty()) {
            Animal animal = animalsAtPosition.iterator().next();
            ImageView animalImageView = getDirectionImage(animal.getDirection());
            animalImageView.setFitWidth(cellSize - 10);
            animalImageView.setFitHeight(cellSize - 10);
            cellLabel.setGraphic(animalImageView);
            hasAnimal = true;

            if (animalsAtPosition.size() == 1) {
                Rectangle healthBar = createHealthBar(animal);
                cell.getChildren().add(healthBar);
            }
        } else if (map.isOccupiedByPlant(position)) {
            ImageView grassImageView = new ImageView(grass1.getImage());
            grassImageView.setFitWidth(cellSize);
            grassImageView.setFitHeight(cellSize);
            cell.getChildren().add(grassImageView);
        } else {
            freeFields++;
        }

        cell.getChildren().add(cellLabel);
        addAnimalCountLabel(cell, animalsAtPosition);
        setCellStyle(cell, hasAnimal, position);

        return cell;
    }

    private Rectangle createHealthBar(Animal animal) {
        Rectangle healthBar = new Rectangle(cellSize, cellSize / 15.0);
        double healthValue = animal.getEnergy() / 100.0;
        healthBar.setWidth(cellSize * Math.min(healthValue, 1.0));
        healthBar.setFill(healthValue > 1.0 ? Color.BLUE : (healthValue >= 0.5) ? Color.GREEN : (healthValue >= 0.2) ? Color.RED : Color.YELLOW);
        healthBar.setStroke(Color.BLACK);
        healthBar.setStrokeWidth(0.5);
        StackPane.setAlignment(healthBar, Pos.TOP_LEFT);
        return healthBar;
    }

    private void addAnimalCountLabel(StackPane cell, Collection<Animal> animalsAtPosition) {
        Label animalCountLabel = new Label(animalsAtPosition != null && animalsAtPosition.size() > 1 ? String.valueOf(animalsAtPosition.size()) : "");
        animalCountLabel.setStyle("-fx-font-weight: bold; -fx-font-size: " + fontSize + "px; -fx-text-fill: red;");
        StackPane.setAlignment(animalCountLabel, Pos.BOTTOM_RIGHT);
        cell.getChildren().add(animalCountLabel);
    }

    private void setCellStyle(StackPane cell, boolean hasAnimal, Vector2d position) {
        int borderWidth = Math.max(1, cellSize / 15);
        cell.setStyle("-fx-border-color: black; -fx-border-width: " + borderWidth + ";");

        if (hasAnimal) {
            cell.setOnMouseClicked(event -> {
                if (!isRunning) {
                    if (selectedCell != null) {
                        selectedCell.setStyle("-fx-border-color: black; -fx-border-width: " + borderWidth + ";");
                    }
                    cell.setStyle("-fx-border-color: yellow; -fx-border-width: " + borderWidth + ";");
                    selectedCell = cell;
                    selectedCellPosition = position;
                    showSelectedAnimals(map.getAnimals().get(position));
                }
            });
        }

        if (position.equals(selectedCellPosition)) {
            cell.setStyle("-fx-border-color: yellow; -fx-border-width: " + borderWidth + ";");
            selectedCell = cell;
        }
    }


    private void showSelectedAnimals(Collection<Animal> animals) {
        selectedAnimals = new ArrayList<>(animals);
        Platform.runLater(() -> {
            animalImages.getChildren().clear();
            animalDetails.setVisible(false);
            for (int i = 0; i < selectedAnimals.size(); i++) {
                Animal animal = selectedAnimals.get(i);
                StackPane animalStack = createAnimalStack(animal, i);
                animalImages.getChildren().add(animalStack);
            }
            updateHealthbars(animals);
        });
    }

    private StackPane createAnimalStack(Animal animal, int index) {
        StackPane animalStack = new StackPane();
        ImageView animalImageView = getDirectionImage(animal.getDirection());
        animalImageView.setFitWidth(40);
        animalImageView.setFitHeight(40);
        animalImageView.setOnMouseClicked(event -> showAnimalDetails(animal, index));
        animalStack.getChildren().add(animalImageView);
        return animalStack;
    }

    private void updateHealthbars(Collection<Animal> animals) {
        selectedAnimals = new ArrayList<>(animals);
        for (int i = 0; i < selectedAnimals.size(); i++) {
            Animal animal = selectedAnimals.get(i);
            StackPane animalStack = (StackPane) animalImages.getChildren().get(i);
            animalStack.getChildren().removeIf(node -> node instanceof Rectangle);
            Rectangle healthBar = createHealthBar(animal);
            animalStack.getChildren().add(healthBar);
        }
    }

    private void showAnimalDetails(Animal animal, int index) {
        selectedAnimalIndex = index;
        IDLabel.setText("ID: " + animal.getId());
        genomeLabel.setText("Genome: " + animal.getGenes().toString());
        activatedGenomePartLabel.setText("Activated Genome Part: " + animal.getGenes().getCurrentGene());
        energyLabel.setText("Energy: " + animal.getEnergy());
        eatenPlantsLabel.setText("Eaten Plants: " + animal.getPlantsEaten());
        numberOfChildrenLabel.setText("Number of Children: " + animal.getNumberOfChildren());
        numberOfDescendantsLabel.setText("Number of Descendants: " + animal.getNumberOfDescendants());
        lifespanLabel.setText("Lifespan: " + animal.getAge());
        dayOfDeathLabel.setText("Day of Death: " + (animal.getDayOfDeath() > 0 ? animal.getDayOfDeath() : "Still alive"));
        animalDetails.setVisible(true);
    }

    private ImageView getDirectionImage(MapDirection direction) {
        Image image = directionImages.get(direction);
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

        int numAnimals = livingAnimals.size();
        int numPlants = worldMap.getPlants().size();
        int numFreeFields = freeFields;
        double avgEnergy = calculateAverage(livingAnimals, Animal::getEnergy);
        double avgLifespan = calculateAverage(deadAnimals, Animal::getAge);
        double avgChildren = calculateAverage(livingAnimals, Animal::getNumberOfChildren);

        Platform.runLater(() -> {
            updateStatisticsLabels(numAnimals, numPlants, numFreeFields, avgEnergy, avgLifespan, avgChildren);
            updateStatisticsChart(numAnimals, numPlants, numFreeFields, avgEnergy, avgLifespan, avgChildren);
            if (!selectedAnimals.isEmpty()) {
                updateHealthbars(selectedAnimals);
                showAnimalDetails(selectedAnimals.get(selectedAnimalIndex), selectedAnimalIndex);
            }
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

    private double calculateAverage(List<Animal> animals, ToIntFunction<Animal> mapper) {
        return animals.stream()
                .mapToInt(mapper)
                .average()
                .orElse(0);
    }

    private void updateStatisticsLabels(int numAnimals, int numPlants, int numFreeFields, double avgEnergy, double avgLifespan, double avgChildren) {
        numAnimalsLabel.setText("Number of all animals: " + numAnimals);
        numPlantsLabel.setText("Number of all plants: " + numPlants);
        numFreeFieldsLabel.setText("Number of free fields: " + numFreeFields);
        avgEnergyLabel.setText("Average energy level: " + avgEnergy);
        avgLifespanLabel.setText("Average lifespan: " + avgLifespan);
        avgChildrenLabel.setText("Average number of children: " + avgChildren);
    }

    private void updateStatisticsChart(int numAnimals, int numPlants, int numFreeFields, double avgEnergy, double avgLifespan, double avgChildren) {
        int day = simulation.getDay();
        numAnimalsSeries.getData().add(new XYChart.Data<>(day, numAnimals));
        numPlantsSeries.getData().add(new XYChart.Data<>(day, numPlants));
        numFreeFieldsSeries.getData().add(new XYChart.Data<>(day, numFreeFields));
        avgEnergySeries.getData().add(new XYChart.Data<>(day, avgEnergy));
        avgLifespanSeries.getData().add(new XYChart.Data<>(day, avgLifespan));
        avgChildrenSeries.getData().add(new XYChart.Data<>(day, avgChildren));
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