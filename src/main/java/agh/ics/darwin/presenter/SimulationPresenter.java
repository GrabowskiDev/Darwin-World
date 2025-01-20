package agh.ics.darwin.presenter;

import agh.ics.darwin.Simulation;
import agh.ics.darwin.model.*;
import agh.ics.darwin.model.variants.PlantGrowthVariant;
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

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.function.ToIntFunction;
import java.util.stream.Collectors;

public class SimulationPresenter implements MapChangeListener {

    @FXML
    private Label numAnimalsLabel, numPlantsLabel, numFreeFieldsLabel,
            avgEnergyLabel, avgLifespanLabel, avgChildrenLabel, genomeLabel,
            activatedGenomePartLabel, energyLabel, eatenPlantsLabel, numberOfChildrenLabel,
            numberOfDescendantsLabel, IDLabel, lifespanLabel, dayOfDeathLabel, info;

    @FXML
    private GridPane mapGrid;

    @FXML
    private LineChart<Number, Number> statisticsChart;

    @FXML
    private VBox genotypeList, animalDetails, animalList, content;

    @FXML
    private Slider speedSlider;

    @FXML
    private Button simulationButton, statsButton;

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
    private ImageView bg, bg1, grass1, grass2_0, grass2_1, grass2_2, grass2_3;
    private double initialX, initialY, initialHValue, initialVValue;
    private int cellSize = 30, fontSize = 12;
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
        stage.setOnCloseRequest(event -> {
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
                    new Thread(simulation::run).start();
                } else {
                    simulation.resume();
                }
                simulationButton.setText("Pause");
            }
            isRunning = !isRunning;
            resetSelectedCell();
        }
    }

    private void resetSelectedCell() {
        if (selectedCell != null) {
            int borderWidth = Math.max(1, cellSize / 15);
            selectedCell.setStyle("-fx-border-color: black; -fx-border-width: " + borderWidth + ";");
            selectedCell = null;
            selectedCellPosition = null;
        }
    }

    @FXML
    private void handleStatsButton() {
        exportStatisticsToCSV("simulation_statistics.csv", simulation.getDailyStatistics());
    }

    @FXML
    public void initialize() {
        initializeChart();
        initializeSlider();
        initializeImages();
        initializeMapGrid();
        initializeDirectionImages();
        content.setVisible(false);
    }

    private void initializeChart() {
        numAnimalsSeries.setName("Number of Animals");
        numPlantsSeries.setName("Number of Plants");
        numFreeFieldsSeries.setName("Number of Free Fields");
        avgEnergySeries.setName("Average Energy");
        avgLifespanSeries.setName("Average Lifespan");
        avgChildrenSeries.setName("Average Number of Children");
        statisticsChart.getData().addAll(numAnimalsSeries, numPlantsSeries, numFreeFieldsSeries, avgEnergySeries, avgLifespanSeries, avgChildrenSeries);
    }

    private void initializeSlider() {
        speedSlider.setMin(80);
        speedSlider.setMax(1000);
        speedSlider.setValue(700);
        speedSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
            if (simulation != null) {
                simulation.setSleepDuration(newValue.intValue());
            }
        });
    }

    private void initializeImages() {
        bg = createImageView("/img/bg.png");
        bg1 = createImageView("/img/bg1.png");
        grass1 = createImageView("/img/grass_1.png");
        grass2_0 = createImageView("/img/grass_2_0.png");
        grass2_1 = createImageView("/img/grass_2_1.png");
        grass2_2 = createImageView("/img/grass_2_2.png");
        grass2_3 = createImageView("/img/grass_2_3.png");
    }

    private ImageView createImageView(String path) {
        ImageView imageView = new ImageView(new Image(getClass().getResourceAsStream(path)));
        imageView.setFitWidth(20);
        imageView.setFitHeight(20);
        return imageView;
    }

    private void initializeMapGrid() {
        animalDetails.setVisible(false);
        mapGrid.setOnScroll(event -> adjustCellSize(event.getDeltaY()));
        mapGrid.setPrefSize(800, 600);
        mapScrollPane.setContent(mapGrid);
        mapGrid.setOnMousePressed(event -> recordInitialMousePosition(event.getSceneX(), event.getSceneY()));
        mapGrid.setOnMouseDragged(event -> dragMap(event.getSceneX(), event.getSceneY()));
    }

    private void adjustCellSize(double deltaY) {
        if (deltaY > 0) {
            cellSize = Math.min(100, cellSize + 5);
            fontSize = Math.min(30, fontSize + 1);
        } else {
            cellSize = Math.max(20, cellSize - 5);
            fontSize = Math.max(14, fontSize - 1);
        }
        displayMap(map);
    }

    private void recordInitialMousePosition(double x, double y) {
        initialX = x;
        initialY = y;
        initialHValue = mapScrollPane.getHvalue();
        initialVValue = mapScrollPane.getVvalue();
    }

    private void dragMap(double x, double y) {
        double deltaX = x - initialX;
        double deltaY = y - initialY;
        mapScrollPane.setHvalue(initialHValue - deltaX / mapGrid.getWidth());
        mapScrollPane.setVvalue(initialVValue - deltaY / mapGrid.getHeight());
    }

    private void initializeDirectionImages() {
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
        List<Node> cells = createCells(map);
        mapGrid.getChildren().addAll(cells);
    }

    private List<Node> createCells(WorldMap map) {
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
        return cells;
    }

    private StackPane createCell(Vector2d position) {
        StackPane cell = new StackPane();
        cell.setPrefSize(cellSize, cellSize);
        ImageView bgImageView = getBackgroundImageView(position);
        bgImageView.setFitWidth(cellSize);
        bgImageView.setFitHeight(cellSize);
        cell.getChildren().add(bgImageView);
        Label cellLabel = new Label();
        boolean hasAnimal = addAnimalOrPlantToCell(cell, position, cellLabel);
        cell.getChildren().add(cellLabel);
        addAnimalCountLabel(cell, map.getAnimals().get(position));
        setCellStyle(cell, hasAnimal, position);
        return cell;
    }

    private ImageView getBackgroundImageView(Vector2d position) {
        if (map.getPlantGrowthVariant() == PlantGrowthVariant.Equator && position.getY() >= map.getJungleBottom() && position.getY() <= map.getJungleTop()) {
            return new ImageView(bg1.getImage());
        } else if (map.getPlantGrowthVariant() == PlantGrowthVariant.GoodHarvest && position.follows(map.getSquareJunglePosition()) && position.precedes(map.getSquareJunglePosition().add(new Vector2d(map.getSquareJungleLength() - 1, map.getSquareJungleLength() - 1)))) {
            return new ImageView(bg1.getImage());
        } else {
            return new ImageView(bg.getImage());
        }
    }

    private boolean addAnimalOrPlantToCell(StackPane cell, Vector2d position, Label cellLabel) {
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
            ImageView grassImageView = getGrassImageView(position);
            grassImageView.setFitWidth(cellSize);
            grassImageView.setFitHeight(cellSize);
            cell.getChildren().add(grassImageView);
        } else {
            freeFields++;
        }
        return hasAnimal;
    }

    private ImageView getGrassImageView(Vector2d position) {
        if (map.getPlants().get(position) instanceof SuperPlant) {
            return new ImageView(grass2_0.getImage());
        } else if (map.getPlants().get(position.add(new Vector2d(0, -1))) instanceof SuperPlant) {
            return new ImageView(grass2_2.getImage());
        } else if (map.getPlants().get(position.add(new Vector2d(-1, -1))) instanceof SuperPlant) {
            return new ImageView(grass2_3.getImage());
        } else if (map.getPlants().get(position.add(new Vector2d(-1, 0))) instanceof SuperPlant) {
            return new ImageView(grass2_1.getImage());
        } else {
            return new ImageView(grass1.getImage());
        }
    }

    private Rectangle createHealthBar(Animal animal) {
        Rectangle healthBar = new Rectangle(cellSize, cellSize / 15.0);
        double healthValue = animal.getEnergy() / 100.0;
        healthBar.setWidth(cellSize * Math.min(healthValue, 1.0));
        healthBar.setFill(getHealthBarColor(healthValue));
        healthBar.setStroke(Color.BLACK);
        healthBar.setStrokeWidth(0.5);
        StackPane.setAlignment(healthBar, Pos.TOP_LEFT);
        return healthBar;
    }

    private Color getHealthBarColor(double healthValue) {
        if (healthValue > 1.0) {
            return Color.BLUE;
        } else if (healthValue >= 0.5) {
            return Color.GREEN;
        } else if (healthValue >= 0.2) {
            return Color.RED;
        } else {
            return Color.YELLOW;
        }
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
            cell.setOnMouseClicked(event -> handleCellClick(cell, position, borderWidth));
        }
        if (position.equals(selectedCellPosition)) {
            cell.setStyle("-fx-border-color: yellow; -fx-border-width: " + borderWidth + ";");
            selectedCell = cell;
        }
    }

    private void handleCellClick(StackPane cell, Vector2d position, int borderWidth) {
        if (!isRunning) {
            animalDetails.setVisible(false);
            if (selectedCell != null) {
                selectedCell.setStyle("-fx-border-color: black; -fx-border-width: " + borderWidth + ";");
            }
            cell.setStyle("-fx-border-color: yellow; -fx-border-width: " + borderWidth + ";");
            selectedCell = cell;
            selectedCellPosition = position;
            selectedAnimals = new ArrayList<>(map.getAnimals().get(position));
            updateAnimalList(selectedAnimals);
        }
    }

    private void updateAnimalList(List<Animal> animals) {
        Platform.runLater(() -> {
            animalList.getChildren().clear();
            selectedAnimals = animals;
            info.setVisible(false);
            content.setVisible(true);
            for (int i = 0; i < animals.size(); i++) {
                Animal animal = animals.get(i);
                HBox animalBox = createAnimalBox(animal, i);
                animalList.getChildren().add(animalBox);
            }
        });
    }

    private HBox createAnimalBox(Animal animal, int index) {
        HBox animalBox = new HBox(15);
        ImageView animalImage = getDirectionImage(animal.getDirection());
        animalImage.setFitWidth(30);
        animalImage.setFitHeight(30);
        Label animalIdLabel = new Label("ID: " + animal.getId());
        animalBox.getChildren().addAll(animalImage, animalIdLabel);
        animalBox.setOnMouseClicked(event -> handleAnimalBoxClick(animal, index, animalBox));
        return animalBox;
    }

    private void handleAnimalBoxClick(Animal animal, int index, HBox animalBox) {
        showAnimalDetails(animal, index);
        animalList.getChildren().forEach(node -> node.setStyle("-fx-background-color: #2b2b2b;"));
        animalBox.setStyle("-fx-background-color: #CCCCCC;");
    }

    private void showAnimalDetails(Animal animal, int index) {
        selectedAnimalIndex = index;
        animalDetails.setVisible(true);
        IDLabel.setText("ID: " + animal.getId());
        genomeLabel.setText("Genome: " + animal.getGenes().toString());
        activatedGenomePartLabel.setText("Activated Genome Part: " + animal.getGenes().getCurrentGene());
        energyLabel.setText("Energy: " + Math.max(animal.getEnergy(), 0));
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
        imageView.setFitWidth(20);
        imageView.setFitHeight(20);
        return imageView;
    }

    @Override
    public synchronized void mapChanged(WorldMap worldMap) {
        Platform.runLater(() -> {
            synchronized (worldMap) {
                displayMap(worldMap);
                updateStatistics(worldMap);
                if (!selectedAnimals.isEmpty()) {
                    showAnimalDetails(selectedAnimals.get(selectedAnimalIndex), selectedAnimalIndex);
                }
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
        avgEnergyLabel.setText("Average energy level: " + Math.round(avgEnergy * 100.0) / 100.0);
        avgLifespanLabel.setText("Average lifespan: " + Math.round(avgLifespan * 100.0) / 100.0);
        avgChildrenLabel.setText("Average number of children: " + Math.round(avgChildren * 100.0) / 100.0);
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

        // Count the occurrences of each genotype
        for (Collection<Animal> animals : worldMap.getAnimals().values()) {
            for (Animal animal : animals) {
                genotypeCounts.merge(animal.getGenes(), 1L, Long::sum);
            }
        }

        // Sort the genotypes by their counts in descending order
        List<Map.Entry<Genes, Long>> sortedGenotypes = genotypeCounts.entrySet().stream()
                .sorted(Map.Entry.<Genes, Long>comparingByValue().reversed())
                .limit(5)
                .collect(Collectors.toList());

        // Update the genotype list in the UI
        Platform.runLater(() -> {
            genotypeList.getChildren().clear();
            for (Map.Entry<Genes, Long> entry : sortedGenotypes) {
                Label label = new Label(entry.getKey() + ": " + entry.getValue());
                genotypeList.getChildren().add(label);
            }
        });
    }
}