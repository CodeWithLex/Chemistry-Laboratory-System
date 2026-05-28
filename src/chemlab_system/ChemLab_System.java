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

    public static void setMaximizeButtonVisible(boolean visible) {
        if (rootContainer == null || rootContainer.getChildren().isEmpty())
            return;
        HBox titleBar = (HBox) rootContainer.getChildren().get(0);

        // Always reset maximized state when switching modes (especially on logout)
        if (mainStage.isMaximized()) {
            mainStage.setMaximized(false);
        }

        if (visible) {
            boolean alreadyExists = false;
            for (javafx.scene.Node node : titleBar.getChildren()) {
                if (node.getStyleClass().contains("maximize-button")) {
                    alreadyExists = true;
                    break;
                }
            }

            if (!alreadyExists) {
                Button maximizeBtn = new Button("▢");
                maximizeBtn.getStyleClass().addAll("title-bar-button", "maximize-button");
                maximizeBtn.setOnAction(e -> {
                    mainStage.setMaximized(!mainStage.isMaximized());
                    maximizeBtn.setText(mainStage.isMaximized() ? "❐" : "▢");
                });
                titleBar.getChildren().add(3, maximizeBtn);

                titleBar.setOnMousePressed(e -> {
                    if (e.getClickCount() == 2) {
                        mainStage.setMaximized(!mainStage.isMaximized());
                        maximizeBtn.setText(mainStage.isMaximized() ? "❐" : "▢");
                    }
                });

                ResizeHelper.addResizeListener(mainStage);
            }
        } else {
            // Logout / Login page view
            titleBar.getChildren().removeIf(node -> node.getStyleClass().contains("maximize-button"));

            // Clean up resizing listeners
            ResizeHelper.removeResizeListener(mainStage);

            // Reset title bar dragging to basic login behavior
            final double[] xOffset = new double[1];
            final double[] yOffset = new double[1];
            titleBar.setOnMousePressed(e -> {
                xOffset[0] = e.getSceneX();
                yOffset[0] = e.getSceneY();
            });
            titleBar.setOnMouseDragged(e -> {
                if (!mainStage.isMaximized()) {
                    mainStage.setX(e.getScreenX() - xOffset[0]);
                    mainStage.setY(e.getScreenY() - yOffset[0]);
                }
            });
        }
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

            // pang change sa title here
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

            // Initial title bar for login (no maximize)
            titleBar.getChildren().addAll(title, spacer, minimizeBtn, closeBtn);
            rootContainer = new VBox();
            rootContainer.getChildren().addAll(titleBar, content);

            Scene scene = new Scene(rootContainer);
            scene.getStylesheets().add(getClass().getResource("/css/style_for_loginPage.css").toExternalForm());

            // pang remove sa bar in a scene
            primaryStage.initStyle(StageStyle.UNDECORATED);
            primaryStage.setScene(scene);

            // Resizing is DISABLED by default (for login page)
            // ResizeHelper.addResizeListener(primaryStage);

            primaryStage.centerOnScreen();
            primaryStage.setWidth(1100);
            primaryStage.setHeight(650);

            // for dragging
            final double[] xOffset = new double[1];
            final double[] yOffset = new double[1];
            titleBar.setOnMousePressed(e -> {
                xOffset[0] = e.getSceneX();
                yOffset[0] = e.getSceneY();
            });
            titleBar.setOnMouseDragged(e -> {
                if (!primaryStage.isMaximized()) {
                    primaryStage.setX(e.getScreenX() - xOffset[0]);
                    primaryStage.setY(e.getScreenY() - yOffset[0]);
                }
            });

            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Error loading FXML: " + e.getMessage());
        }
    }

    // Static helper class for resizing undecorated stage
    public static class ResizeHelper {
        private static ResizeListener currentListener;

        public static void addResizeListener(Stage stage) {
            if (currentListener != null) {
                removeResizeListener(stage);
            }
            currentListener = new ResizeListener(stage);
            stage.getScene().addEventFilter(javafx.scene.input.MouseEvent.MOUSE_MOVED, currentListener);
            stage.getScene().addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED, currentListener);
            stage.getScene().addEventFilter(javafx.scene.input.MouseEvent.MOUSE_DRAGGED, currentListener);
            stage.getScene().addEventFilter(javafx.scene.input.MouseEvent.MOUSE_EXITED, currentListener);
            stage.getScene().addEventFilter(javafx.scene.input.MouseEvent.MOUSE_ENTERED, currentListener);
        }

        public static void removeResizeListener(Stage stage) {
            if (currentListener != null && stage.getScene() != null) {
                stage.getScene().removeEventFilter(javafx.scene.input.MouseEvent.MOUSE_MOVED, currentListener);
                stage.getScene().removeEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED, currentListener);
                stage.getScene().removeEventFilter(javafx.scene.input.MouseEvent.MOUSE_DRAGGED, currentListener);
                stage.getScene().removeEventFilter(javafx.scene.input.MouseEvent.MOUSE_EXITED, currentListener);
                stage.getScene().removeEventFilter(javafx.scene.input.MouseEvent.MOUSE_ENTERED, currentListener);
                stage.getScene().setCursor(javafx.scene.Cursor.DEFAULT);
                currentListener = null;
            }
        }

        static class ResizeListener implements javafx.event.EventHandler<javafx.scene.input.MouseEvent> {
            private Stage stage;
            private javafx.scene.Cursor cursorEvent = javafx.scene.Cursor.DEFAULT;
            private int border = 4;
            private double startX = 0;
            private double startY = 0;

            public ResizeListener(Stage stage) {
                this.stage = stage;
            }

            @Override
            public void handle(javafx.scene.input.MouseEvent mouseEvent) {
                javafx.event.EventType<? extends javafx.scene.input.MouseEvent> mouseEventType = mouseEvent
                        .getEventType();
                Scene scene = stage.getScene();
                if (scene == null)
                    return;

                double mouseEventX = mouseEvent.getSceneX();
                double mouseEventY = mouseEvent.getSceneY();
                double sceneWidth = scene.getWidth();
                double sceneHeight = scene.getHeight();

                if (javafx.scene.input.MouseEvent.MOUSE_MOVED.equals(mouseEventType)) {
                    if (mouseEventX < border && mouseEventY < border) {
                        cursorEvent = javafx.scene.Cursor.NW_RESIZE;
                    } else if (mouseEventX < border && mouseEventY > sceneHeight - border) {
                        cursorEvent = javafx.scene.Cursor.SW_RESIZE;
                    } else if (mouseEventX > sceneWidth - border && mouseEventY < border) {
                        cursorEvent = javafx.scene.Cursor.NE_RESIZE;
                    } else if (mouseEventX > sceneWidth - border && mouseEventY > sceneHeight - border) {
                        cursorEvent = javafx.scene.Cursor.SE_RESIZE;
                    } else if (mouseEventX < border) {
                        cursorEvent = javafx.scene.Cursor.W_RESIZE;
                    } else if (mouseEventX > sceneWidth - border) {
                        cursorEvent = javafx.scene.Cursor.E_RESIZE;
                    } else if (mouseEventY < border) {
                        cursorEvent = javafx.scene.Cursor.N_RESIZE;
                    } else if (mouseEventY > sceneHeight - border) {
                        cursorEvent = javafx.scene.Cursor.S_RESIZE;
                    } else {
                        cursorEvent = javafx.scene.Cursor.DEFAULT;
                    }
                    scene.setCursor(cursorEvent);
                } else if (javafx.scene.input.MouseEvent.MOUSE_EXITED.equals(mouseEventType)
                        || javafx.scene.input.MouseEvent.MOUSE_ENTERED.equals(mouseEventType)) {
                    scene.setCursor(javafx.scene.Cursor.DEFAULT);
                } else if (javafx.scene.input.MouseEvent.MOUSE_PRESSED.equals(mouseEventType)) {
                    startX = stage.getWidth() - mouseEventX;
                    startY = stage.getHeight() - mouseEventY;
                } else if (javafx.scene.input.MouseEvent.MOUSE_DRAGGED.equals(mouseEventType)) {
                    if (!javafx.scene.Cursor.DEFAULT.equals(cursorEvent)) {
                        if (!javafx.scene.Cursor.W_RESIZE.equals(cursorEvent)
                                && !javafx.scene.Cursor.E_RESIZE.equals(cursorEvent)) {
                            double minHeight = stage.getMinHeight() > (border * 2) ? stage.getMinHeight()
                                    : (border * 2);
                            if (javafx.scene.Cursor.NW_RESIZE.equals(cursorEvent)
                                    || javafx.scene.Cursor.N_RESIZE.equals(cursorEvent)
                                    || javafx.scene.Cursor.NE_RESIZE.equals(cursorEvent)) {
                                if (stage.getHeight() > minHeight || mouseEventY < 0) {
                                    stage.setHeight(stage.getY() - mouseEvent.getScreenY() + stage.getHeight());
                                    stage.setY(mouseEvent.getScreenY());
                                }
                            } else {
                                if (stage.getHeight() > minHeight || mouseEventY + startY > stage.getHeight()) {
                                    stage.setHeight(mouseEventY + startY);
                                }
                            }
                        }

                        if (!javafx.scene.Cursor.N_RESIZE.equals(cursorEvent)
                                && !javafx.scene.Cursor.S_RESIZE.equals(cursorEvent)) {
                            double minWidth = stage.getMinWidth() > (border * 2) ? stage.getMinWidth() : (border * 2);
                            if (javafx.scene.Cursor.NW_RESIZE.equals(cursorEvent)
                                    || javafx.scene.Cursor.W_RESIZE.equals(cursorEvent)
                                    || javafx.scene.Cursor.SW_RESIZE.equals(cursorEvent)) {
                                if (stage.getWidth() > minWidth || mouseEventX < 0) {
                                    stage.setWidth(stage.getX() - mouseEvent.getScreenX() + stage.getWidth());
                                    stage.setX(mouseEvent.getScreenX());
                                }
                            } else {
                                if (stage.getWidth() > minWidth || mouseEventX + startX > stage.getWidth()) {
                                    stage.setWidth(mouseEventX + startX);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }

}
