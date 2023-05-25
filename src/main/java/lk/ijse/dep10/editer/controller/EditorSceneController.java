package lk.ijse.dep10.editer.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.print.JobSettings;
import javafx.print.PageLayout;
import javafx.print.PrinterJob;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.DragEvent;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.TransferMode;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import lk.ijse.dep10.editer.AppInitializer;
import lk.ijse.dep10.editer.util.SearchResult;

import java.io.*;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class EditorSceneController extends AppInitializer {


    private static Stage stage;
    public Label lblTitle;
    public Button btnMinimize;
    public Button btnMaximize;
    public Button btnClose;
    public Label lblEditing;
    public Menu mnNew;
    public TextField txtReplace;
    public Button btnReplace;
    public Button btnReplaceAll;
    public Label lblResult;
    public Button btnDown;
    public TextField txtFind;
    public Button btnUp;
    public TextArea txteditor;
    public CheckBox chkMatchcase;
    public Menu mnPrint;
    private boolean isMaximized = false;
    private ArrayList<SearchResult> searchResultList = new ArrayList<>();
    private int pos = 0;
    private File savedFile;
    private boolean isFileSaved = false;
    private File openFile;
    private boolean isOpenedFile = false;


    public void initialize() {
        isOpenedFile = false;
        btnUp.setDisable(true);
        txtFind.textProperty().addListener((ov1, previous, current) -> {

            if (current.isEmpty()) {
                lblResult.setText("0 Results");
            } else {
                calculateResult();
            }

        });
        txteditor.textProperty().addListener((ov1, previous, current) -> {
            if (current.isEmpty()) {
                lblResult.setText("0 Results");
            } else {
                calculateResult();
            }
            if (!current.equals(previous)) {
                isFileSaved = false;
            }
        });
        chkMatchcase.selectedProperty().addListener((ov, previous, current) -> {

            calculateResult();
        });


    }

    private void calculateResult() {
        String query = txtFind.getText();

        searchResultList.clear();
        pos = 0;
        Pattern pattern;


        try {
            if (chkMatchcase.isSelected()) {
                pattern = Pattern.compile(query);
            } else pattern = Pattern.compile(query, Pattern.CASE_INSENSITIVE);

        } catch (RuntimeException e) {
            return;
        }

        Matcher matcher = pattern.matcher(txteditor.getText());
        while (matcher.find()) {
            int start = matcher.start();
            int end = matcher.end();
            lk.ijse.dep10.editer.util.SearchResult result = new lk.ijse.dep10.editer.util.SearchResult(start, end);
            searchResultList.add(result);
        }
        lblResult.setText(String.format("%d Result", searchResultList.size()));
        select();

    }

    private void select() {
        if (searchResultList.isEmpty()) return;
        SearchResult searchResult = searchResultList.get(pos);
        txteditor.selectRange(searchResult.getStart(), searchResult.getEnd());
        lblResult.setText(String.format("%d /%d Result", (pos + 1), searchResultList.size()));

    }

    @Override
    public void start(Stage primaryStage) throws IOException {

        stage = primaryStage;


    }


    public void closeAction() throws IOException {
        if (txteditor.getText().isEmpty()) {
            Platform.exit();
        } else {
            if (isOpenedFile == false) {
                if (isFileSaved == true) {
                    Platform.exit();
                } else {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION, "Do you want to save the Document",
                            ButtonType.YES, ButtonType.NO);
                    alert.showAndWait();
                    if (alert.getResult() == ButtonType.NO) {
                        txteditor.setText("");
                    } else {
                        save();
                    }

                }
            }
        }

    }

    @FXML
    void mnNewOnAction(ActionEvent event) {//1
        txteditor.setText("");

    }

    @FXML
    void mnOpenOnAction(ActionEvent event) throws IOException {//2
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Open a text file");
        openFile = fileChooser.showOpenDialog(txteditor.getScene().getWindow());

        lblTitle.setText(openFile.getName());
        if (openFile == null) return;

        FileInputStream fis = new FileInputStream(openFile);
        byte[] bytes = fis.readAllBytes();
        fis.close();

        txteditor.setText(new String(bytes));
        //String title = EditorSceneController.getStage().getTitle();
        txteditor.accessibleTextProperty().addListener((value, previous, current) -> {
            lblTitle.setText("*" + openFile.getName());
        });
        isFileSaved = true;
        isOpenedFile = true;

    }

    public void mnSaveOnAction(ActionEvent actionEvent) throws IOException {//3

        save();

    }

    private void save() throws IOException {
        if (isOpenedFile == false) {
            if (savedFile == null) {
                // Show the file chooser dialog for the first save
                FileChooser fileChooser = new FileChooser();
                fileChooser.setTitle("Save the text file");
                savedFile = fileChooser.showSaveDialog(txteditor.getScene().getWindow());

            }
            // Write the content to the file
            FileOutputStream fos = new FileOutputStream(savedFile, false);
            String text = txteditor.getText();
            byte[] bytes = text.getBytes();
            fos.write(bytes);
            fos.close();
            isFileSaved = true;
        } else {
            try {
                FileWriter fileWriter = new FileWriter(openFile.getAbsolutePath());
                fileWriter.write(txteditor.getText());
                fileWriter.close();
                System.out.println("File saved successfully.");
            } catch (IOException e) {
                System.out.println("Error saving file: " + e.getMessage());
            }
        }
    }


    public void mnSaveAsOnAction(ActionEvent actionEvent) throws IOException {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save As");
        fileChooser.setInitialDirectory(new File(File.separator + "home" + File.separator + System.getProperty("user.name")));
        File file = fileChooser.showSaveDialog(txteditor.getScene().getWindow());
        if (file == null) return;

        FileOutputStream fos = new FileOutputStream(file, false);
        String text = txteditor.getText();
        byte[] bytes = text.getBytes();
        fos.write(bytes);
        fos.close();
        isFileSaved = true;

    }

    public void rootOnDragOver(DragEvent dragEvent) {//4
        try {
            dragEvent.acceptTransferModes(TransferMode.ANY);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }

    }

    public void rootOnDragDropped(DragEvent dragEvent) throws IOException {//5

        try {
            File droppedFile = dragEvent.getDragboard().getFiles().get(0);
            lblTitle.setText(droppedFile.getName());
            FileInputStream fis = new FileInputStream(droppedFile);
            byte[] bytes = fis.readAllBytes();

            fis.close();
            txteditor.setText(new String(bytes));
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
        isOpenedFile = true;
        isFileSaved = true;

    }


    @FXML
    void mnAboutOnAction(ActionEvent event) throws IOException {//6
        Stage stage = new Stage();
        stage.setScene(new Scene(new FXMLLoader(getClass().getResource("/view/AboutScene.fxml")).load()));
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setTitle("About");
        stage.setResizable(false);
        stage.centerOnScreen();
        stage.show();
    }

    public void mnHelpOnAction(ActionEvent actionEvent) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "For help contact via nimanthikaabeyrathna@gmail.com ");
        alert.setTitle("Help");
        alert.setHeaderText(null);
        alert.showAndWait();

    }

    @FXML
    void mnCloseOnAction(ActionEvent event) throws IOException {
        closeAction();
        mnNew.fire();
        isFileSaved = false;


    }


    @FXML
    void mnPrintOnAction(ActionEvent event) {

        PrinterJob job = PrinterJob.createPrinterJob();

        if (job == null) {
            System.out.println("Error");
            return;
        }

        boolean proceed = job.showPrintDialog(lblEditing.getScene().getWindow());

        JobSettings ss1 = job.getJobSettings();

        PageLayout pageLayout1 = ss1.getPageLayout();

        double pgW1 = pageLayout1.getPrintableWidth();
        double pgH1 = pageLayout1.getPrintableHeight();

        Label tempText = new Label();
        tempText.setPrefWidth(pgW1);
        tempText.setPrefHeight(pgH1);
        tempText.setWrapText(true);
        tempText.setText(txteditor.getText());


        if (proceed) {
            job.printPage(tempText);
            job.endJob();
        }
    }


    public void btnMinimizeOnAction(ActionEvent actionEvent) {
        ((Stage) ((Button) actionEvent.getSource()).getScene().getWindow()).setIconified(true);

    }

    public void btnMaximizeOnAction(ActionEvent actionEvent) {

        Stage s = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
        // RichTextFX
        if (isMaximized == false) {
            s.setMaximized(true);
            //s.setFullScreen(true);
            isMaximized = true;
        } else {
            s.setMaximized(false);
            //s.setFullScreen(false);
            isMaximized = false;
        }

    }


    public void btnCloseOnAction(ActionEvent actionEvent) throws IOException {
        closeAction();
        isFileSaved = false;
        isOpenedFile = false;
        System.exit(0);
    }


    public void txteditorOnKeyPressed(KeyEvent keyEvent) {
        lblEditing.setVisible(true);
    }

    public void txteditorOnKeyReleased(KeyEvent keyEvent) {
        lblEditing.setVisible(false);
    }


    public void btnUpOnAction(ActionEvent actionEvent) {
        pos--;
        if (pos < 0) {
            pos = searchResultList.size();
            return;
        }
        select();
    }

    public void btnDownOnAction(ActionEvent actionEvent) {
        btnUp.setDisable(false);
        pos++;
        if (pos == searchResultList.size()) {
            pos = -1;
            return;
        }
        select();

    }

    public void btnReplaceOnAction(ActionEvent actionEvent) {
        if (!(txtFind.getText().isEmpty() || txtReplace.getText().isEmpty() ||
                searchResultList.isEmpty())) {


            SearchResult result = searchResultList.get(pos);
            String text = txteditor.getText(result.getStart(), result.getEnd());

            Pattern pattern = Pattern.compile(text);
            Matcher matcher = pattern.matcher(txteditor.getText());

            txteditor.replaceSelection(txtReplace.getText());

            searchResultList.remove(result);
            lblResult.setText("Result " + (searchResultList.size()));

        }

    }

    public void btnReplaceAllOnAction(ActionEvent actionEvent) {
        if (!(txtFind.getText().isEmpty() || txtReplace.getText().isEmpty() ||
                searchResultList.isEmpty())) {

            SearchResult result = null;
            String text;

            result = searchResultList.get(pos);
            text = txteditor.getText(result.getStart(), result.getEnd());
            Pattern replaceAl = Pattern.compile(text);
            Matcher matcher = replaceAl.matcher(txteditor.getText());
            txteditor.setText(matcher.replaceAll(txtReplace.getText()));
            lblResult.setText("Result " + 0);
        }
    }


}
