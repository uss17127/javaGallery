package cs1302.gallery;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.layout.*;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.control.*;
import javafx.scene.control.ToolBar;
import javafx.event.*;
import javafx.geometry.Orientation;
import javafx.scene.text.Text;
import javafx.animation.*;
import javafx.scene.image.*;
import java.net.*;
import java.io.InputStreamReader;
import com.google.gson.*;
import java.nio.charset.StandardCharsets;
import javafx.scene.control.ButtonBar.ButtonData;
import java.net.*;
import java.nio.charset.StandardCharsets;
import javafx.util.Duration;
import java.util.Random;
import java.io.*;
import java.lang.Math.*;

/**
 * Represents an iTunes GalleryApp.
 */
public class GalleryApp extends Application {

    public Scene scene;
    public Stage stage;
    public VBox content;
    public MenuBar menu;
    public Button pause;
    public Button updateImages;
    public TextField search;
    public String inputText;
    public ToolBar toolbar;
    public TilePane tilePane = new TilePane();
    public Timeline timeline;
    public URL url;
    public InputStreamReader reader;
    public JsonParser jsonParser;
    public JsonElement je;
    public JsonObject root;
    public JsonArray results;
    public int numResults;
    public ImageView[] paneImages = new ImageView[20];
    public String[] imageUrls;
    public boolean playing = true; //Is the app paused or playing?
    public int counter = 0;
    public Random rand;
    public ProgressBar progressBar = new ProgressBar();
    public HBox progressBox;
    public BorderPane border = new BorderPane();
    public double progressN = 0.0;

    /**
     * Starts the application.
     * @param stage the stage to set up the application
     */
    @Override
    public void start(Stage stage) {
        this.stage = stage;
        content = new VBox();
        content.getChildren().addAll(this.makeMenu(), this.toolBarEventHandler());
        this.addTilePane();
        border.setCenter(content);
        border.setBottom(this.makeProgressBox());
        scene = new Scene(border);
        stage.setTitle("GalleryApp!");
        stage.setMinWidth(505.00);
        stage.setMinHeight(540.00);
        stage.setScene(scene);
        stage.sizeToScene();
        stage.show();
    } // start

    /**
     * Makes the menu bar that can be used to exit the application.
     *
     * @return menu  the menu bar
     */
    public MenuBar makeMenu() {
        Menu file = new Menu("File");
        menu = new MenuBar();
        menu.getMenus().add(file);
        MenuItem exit = new MenuItem("Exit");
        file.getItems().add(exit);
        exit.setOnAction(e -> {
            Platform.exit();
            System.exit(0);
        });
        return menu;
    } //makeMenu

    /**
     * Makes toolbar to deal with the search query and also all its other components.
     *
     * @return toolbar the toolbar with components used for loading tiles
     */
    public ToolBar makeToolBar() {
        toolbar = new ToolBar();
        pause = new Button("Pause");
        Separator separator = new Separator(Orientation.VERTICAL);
        Label query = new Label("Search Query:");
        search = new TextField();
        updateImages = new Button("UpdateImages");
        toolbar.getItems().addAll(pause, separator, query, search, updateImages);
        return toolbar;
    } //makeToolBar

    /**
     * Makes an HBox that ahowa the progress of loaded panes.
     *
     * @return progressBox the HBox that contains the progress bar and label
     */
    public HBox makeProgressBox() {
        progressBox = new HBox();
        progressBox.setMinWidth(505.00);
        progressBox.setMinHeight(20.0);
        progressBar.setLayoutX(100.00);
        progressBar.setLayoutY(18.00);
        Label iTunes = new Label("Images provided courtesy of iTunes");
        progressBox.getChildren().addAll(progressBar,iTunes);
        return progressBox;
    } //makeProgressBar

    /**
     * This method sets the progress amount on the progress bar based off {@code progressN}.
     */
    public void setProgressBar() {
        progressBar.setProgress(progressN);
    } //setProgressBar

    /**
     * Responsible for calling {@code makeToolBar} method also handling the buttons in the toolbar.
     *
     * @return toolbar the HBox that contains the toolbar components
     */
    public ToolBar toolBarEventHandler() {
        this.makeToolBar();
        pause.setOnAction(e -> {
            this.counter();
            this.pauseButtonHandler();
            if (playing == true) {
                this.playAction();
            }
        });
        updateImages.setOnAction(e -> {
            progressBar.setProgress(0.0);
            progressN = 0.0;
            this.checkTimeline();
            String search = this.readInput();
            this.readResults(search);
            this.setTilePane();
            this.playAction();
        });
        return toolbar;
    } //toolBarEventHandler

    /**
     * Called if the {@code pause} button is pressed.
     * If the {@code counter} integer is even this indicates if the application is paused/playing.
     */
    public void counter() {
        counter++;
        if (counter % 2 == 0 || counter != 0) {
            playing = true;
        }
        if (counter % 2 != 0) {
            playing = false;
        }
    } //counter

    /**
     * When the {@code play} button is pressed it changes the buttons to the appropriae text.
     * If the button is paused, the text changes to play and vice versa.
     */
    public void pauseButtonHandler() {
        if (playing == true) {
            pause.setText("Pause");
            timeline.play();
        }
        if (playing == false) {
            pause.setText("Play");
            timeline.pause();
        }
    } //pauseButtonHandler

    /**
     * This method is in charge of changing random tiles in the pane as well as
         handling if the application paused.
     */
    public void playAction() {
        EventHandler<ActionEvent> handler = (event -> {
            if (pause.getText().equals("Play")) {
                timeline.pause();
                return;
            }
            if (results.size() > 21) {
                tilePane.getChildren().clear();
                Random rand = new Random();
                int random = rand.nextInt(((results.size() - 1) - 0) + 0);
                Random rand1 = new Random();
                int randomIndex = rand1.nextInt((20 - 1) - 1);
                JsonObject randomResult = results.get(random).getAsJsonObject();
                JsonElement artworkUrl100 = randomResult.get("artworkUrl100");
                if (artworkUrl100 != null) {
                    String albumUrl = artworkUrl100.getAsString();
                    Image albumArt = new Image(albumUrl);
                    paneImages[randomIndex].setImage(albumArt);
                    paneImages[randomIndex].setFitWidth(100.0);
                    paneImages[randomIndex].setFitHeight(100.0);
                }
                for (int i = 0; i < 20; i++) {
                    tilePane.getChildren().add(paneImages[i]);
                }
            }
        });
        setTimeline(handler);
    } //playAction

    /** Checks to see of timeline is playing or is paused. */
    public void checkTimeline() {
        boolean timelinePlay = false;
        if (timeline != null) {
            if (timeline.getStatus() == Animation.Status.RUNNING) {
                timelinePlay = true;
                timeline.pause();
            }
        }
    } //checkTimeLine

    /**
     * Reads user input and changes it to allow for its url to be encoded.
     *
     * @return inputText changed version of the user input to aid in url encoding process
     */
    public String readInput() {
        inputText = search.getText();
        String[] input = inputText.split(" ");
        inputText = "";
        for (int i = 0; i < input.length; i++) {
            if (i < input.length - 1) {
                inputText = inputText + input[i] + "+";
            } else {
                inputText = inputText + input[i];
            }
        }
        return inputText;
    } //readInput

    /**
     * This methods reads the results of the JSON query and stores these results in an array.
     *
     * @param input what the users type into the textfield
     */
    public void readResults(String input) {
        String sUrl = "http://itunes.apple.com/search?term=" + inputText;
        this.encode(sUrl);
        this.getResults(sUrl);
        if (numResults < 21) {
            this.errorMessage();
            return;
        }
        for (int i = 0; i < 20; i++) {
            JsonObject result = results.get(i).getAsJsonObject();
            JsonElement artworkUrl100 = result.get("artworkUrl100"); // artworkUrl100 member
            if (artworkUrl100 != null) {
                String albumUrl = artworkUrl100.getAsString();
                Image albumArt = new Image(albumUrl);
                paneImages[i] = new ImageView();
                imageUrls[i] = albumUrl;
                paneImages[i].setImage(new Image(imageUrls[i]));

            }
            progressN += 0.05;
            this.setProgressBar();
        }
    } //readResults

    /**
     * Adds the tilepane to the the {@code content} VBox as well as reading user input.
     */
    public void addTilePane() {
        Thread thread = new Thread(() -> {
            tilePane.setPrefColumns(5);
            tilePane.setPrefRows(4);
            this.readResults(inputText);
            Platform.runLater(() -> {
                content.getChildren().add(setTilePane());
            });
        });
        thread.setDaemon(true);
        thread.start();
    } //addTilePane

    /**
     * Parses the Json results for a query.
     * @param sUrl the encoded url
     */
    public void getResults(String sUrl) {
        try {
            url = new URL(sUrl);
            reader = new InputStreamReader(url.openStream());
            jsonParser = new JsonParser();
            je = jsonParser.parseReader(reader);
            root = je.getAsJsonObject();
            results = root.getAsJsonArray("results");
            numResults = results.size();
            imageUrls = new String[numResults];
        } catch (MalformedURLException e) {
            throw new RuntimeException("Malformed URL has occured");
        } catch (IOException e) {
            throw new RuntimeException("I/O exception has occured");
        }

    } //getResults

    /**
     * Encodes the url.
     *
     * @param url the url that is used to search the query
     * @return encoded Url
     */
    public String encode(String url) {
        try {
            return URLEncoder.encode(url, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("The character encoding is not supported");
        }
    } //encode

    /**
     * Pops up a dialog box if the specific search comes up with less than 20 results.
     */
    public void errorMessage() {
        Dialog<String> dialog = new Dialog<String>();
        dialog.setTitle("Error");
        ButtonType ok = new ButtonType("Ok", ButtonData.OK_DONE);
        dialog.setContentText("This search does not have enough results");
        dialog.getDialogPane().getButtonTypes().add(ok);
        dialog.setWidth(300);
        dialog.setHeight(300);
        dialog.showAndWait();
    } //errorMessage

    /**
     * Sets images into the tilepane and sets their widths and heights.
     * @return tilePane the fird that shows the result of the search with images
     */
    public TilePane setTilePane() {
        if (numResults < 21) {
            return tilePane;
        }
        tilePane.getChildren().clear();
        for (int i = 0; i < 20; i++) {
            paneImages[i].setImage(new Image(imageUrls[i]));
            paneImages[i].setFitHeight(100.0);
            paneImages[i].setFitWidth(100.0);
            tilePane.getChildren().add(paneImages[i]);
        }
        return tilePane;
    }

    /** Sets the timeline to run the animation.
     * @param handler the event handler used to change random images
     */
    public void setTimeline(EventHandler<ActionEvent> handler) {
        KeyFrame keyFrame = new KeyFrame(Duration.seconds(2), handler);
        timeline = new Timeline();
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.getKeyFrames().add(keyFrame);
        timeline.play();
    }

} // GalleryApp
