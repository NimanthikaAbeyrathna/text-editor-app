package lk.ijse.dep10.editer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;
import java.net.URL;

public class AppInitializer extends Application {
    private double mouseX = 0;
    private double mouseY = 0;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws IOException {

        URL fxmlFile = getClass().getResource("/view/EditorScene.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader(fxmlFile);
        AnchorPane root = fxmlLoader.load();
        root.setOnMousePressed(mouseEvent -> {
            mouseX = mouseEvent.getX();
            mouseY = mouseEvent.getY();

        });
        root.setOnMouseReleased(mouseEvent -> {
            root.setCursor(Cursor.DEFAULT);
        });

        root.setOnMouseDragged(mouseEvent -> {
            if (mouseEvent.getButton() == MouseButton.PRIMARY) {
//            System.out.println(mouseEvent.getScreenX());
//            System.out.println(mouseEvent.getScreenY());
                primaryStage.setX(mouseEvent.getScreenX() - mouseX);
                primaryStage.setY(mouseEvent.getScreenY() - mouseY);
                root.setCursor(Cursor.MOVE);
            }
        });
        root.setBackground(Background.fill(Color.TRANSPARENT));
        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        primaryStage.setScene(scene);
        primaryStage.initStyle(StageStyle.TRANSPARENT);

        //primaryStage.setScene(new Scene
               // (new FXMLLoader(getClass().getResource("/view/EditorScene.fxml")).load()));
        primaryStage.show();
        primaryStage.centerOnScreen();
        primaryStage.setTitle("Simple text editor");


    }
}
