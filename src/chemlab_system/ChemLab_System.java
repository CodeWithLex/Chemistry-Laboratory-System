/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/javafx/FXML.java to edit this template
 */
package chemlab_system;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 *
 * @author User
 */
public class ChemLab_System extends Application {

    private static Stage mainStage;
    private static VBox rootContainer;

    public static void setContent(Parent content, double width, double height) {
        if (rootContainer.getChildren().size() > 1) {
            rootContainer.getChildren().remove(1);
        }
        VBox.setVgrow(content, Priority.ALWAYS);
        rootContainer.getChildren().add(content);
        if (width > 0 && height > 0) {
            mainStage.setWidth(width);
            mainStage.setHeight(height + 40); // Add title bar height
        }
        mainStage.centerOnScreen();
    }

    public static void setContent(Parent content) {
        if (rootContainer.getChildren().size() > 1) {
            rootContainer.getChildren().remove(1);
        }
        VBox.setVgrow(content, Priority.ALWAYS);
        rootContainer.getChildren().add(content);
        mainStage.sizeToScene();
        mainStage.centerOnScreen();
    }

    public static void setTitle(String title) {
        Label titleLabel = (Label) ((HBox) rootContainer.getChildren().get(0)).getChildren().get(0);
        titleLabel.setText(title);
    }

    @Override
    public void start(Stage primaryStage) {
        mainStage = primaryStage;
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("/ui/loginPage.fxml"));
            Parent content = loader.load();
            
            // Create custom title bar
            HBox titleBar = new HBox();
            titleBar.getStyleClass().add("custom-title-bar");
            
            Label title = new Label("Chemistry Laboratory System - Login");
            title.getStyleClass().add("title-bar-title");
            
            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);
            
            Button minimizeBtn = new Button("—");
            minimizeBtn.getStyleClass().add("title-bar-button");
            minimizeBtn.setOnAction(e -> primaryStage.setIconified(true));
            
            Button closeBtn = new Button("✕");
            closeBtn.getStyleClass().addAll("title-bar-button", "close-button");
            closeBtn.setOnAction(e -> primaryStage.close());
            
            titleBar.getChildren().addAll(title, spacer, minimizeBtn, closeBtn);
            
            // Combine title bar and content
            rootContainer = new VBox();
            rootContainer.getChildren().addAll(titleBar, content);
            
            Scene scene = new Scene(rootContainer);
            scene.getStylesheets().add(getClass().getResource("/css/style_for_loginPage.css").toExternalForm());
            
            // Remove native title bar
            primaryStage.initStyle(StageStyle.UNDECORATED);
            primaryStage.setScene(scene);
            primaryStage.centerOnScreen();
            primaryStage.setWidth(1100);
            primaryStage.setHeight(650);
            
            // Enable dragging window by title bar
            final double[] xOffset = new double[1];
            final double[] yOffset = new double[1];
            titleBar.setOnMousePressed(e -> {
                xOffset[0] = e.getSceneX();
                yOffset[0] = e.getSceneY();
            });
            titleBar.setOnMouseDragged(e -> {
                primaryStage.setX(e.getScreenX() - xOffset[0]);
                primaryStage.setY(e.getScreenY() - yOffset[0]);
            });

            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error loading FXML: " + e.getMessage());
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
