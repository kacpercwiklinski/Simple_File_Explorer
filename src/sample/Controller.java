package sample;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.*;
import java.util.ResourceBundle;


import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Button;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.input.ContextMenuEvent;
import javafx.scene.layout.*;
import javafx.scene.text.Text;


/**
 * @author Kacper
 */
public class Controller implements Initializable {

    GridPane iconsGrid = new GridPane();
    GridPane treeGrid = new GridPane();

    Path actualPath;
    Path copyPath;
    Path cutPath;



    File[] drives ;
    final ContextMenu drivesContextMenu = new ContextMenu();




    int BUTTON_PADDING = 7;

    @FXML
    private ScrollPane iconsField;
    @FXML
    private ScrollPane treeField;
    @FXML
    private SplitPane sp;
    @FXML
    private Text pathText;


    private void clearChildrens(){
        iconsGrid.getChildren().clear();
        treeGrid.getChildren().clear();
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        drives = File.listRoots();

        sp.setDividerPositions(0.25);

        if(drives != null && drives.length >0 ){
            for (File drive : drives){
                MenuItem item = new MenuItem(drive.getAbsolutePath());
                item.setOnAction((ActionEvent) -> {
                    setTree(drive.getAbsolutePath());
                    pathText.setText(drive.getAbsolutePath());
                });
                drivesContextMenu.getItems().add(item);
            }
        }
        treeField.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {
            @Override
            public void handle(ContextMenuEvent event) {
                drivesContextMenu.show(treeField,event.getScreenX(),event.getScreenY());
            }
        });



        iconsGrid.setPadding(new Insets(BUTTON_PADDING));
        iconsGrid.setHgap(BUTTON_PADDING);
        iconsGrid.setVgap(BUTTON_PADDING);
        iconsGrid.setAlignment(Pos.CENTER);

        treeGrid.setPadding(new Insets(BUTTON_PADDING));
        treeGrid.setHgap(BUTTON_PADDING);
        treeGrid.setVgap(BUTTON_PADDING);
        treeGrid.setAlignment(Pos.CENTER);

        treeField.maxWidthProperty().bind(sp.widthProperty().multiply(0.25));
        treeField.minWidthProperty().bind(sp.widthProperty().multiply(0.25));

    }

    private void setTree(String path){
        actualPath = Paths.get(path);
        int icol=1 , irow =1,trow = 1;
        double treeWidth = treeField.getWidth() - 5*BUTTON_PADDING;
        float imaxCols = Math.round(iconsField.getWidth() /( 100 + (2*BUTTON_PADDING)));

        Boolean isDrive = false;
        File folder =new File(path);

        clearChildrens();

        for (File drive : drives){
            if(drive.getAbsolutePath().equals(path)){
                isDrive = true;
            }
        }

        if(isDrive){

        }else{
            Button back = new Button();
            back.setText("...");
            back.setOnAction((ActionEvent) ->{
                setTree(folder.getParentFile().getAbsolutePath());
                pathText.setText(folder.getParentFile().getAbsolutePath());
            });
            back.setStyle("-fx-max-width:"+treeWidth+";-fx-min-width:"+treeWidth+";");

            treeGrid.add(back,1,trow);
            trow++;
        }




        for (final File file : folder.listFiles()) {

            if (file.isDirectory()) {

                if(!Files.isExecutable(file.toPath()) || file.isHidden() || !Files.isReadable(file.toPath())){

                }else{
                    Button treeButton = new Button();
                    treeButton.setText(String.valueOf(file.getName()));
                    treeButton.setOnAction((ActionEvent) -> {
                        setTree(file.getAbsolutePath());
                        pathText.setText(file.getAbsolutePath());
                    } );
                  treeButton.setStyle("-fx-max-width:"+treeWidth+";-fx-min-width:"+treeWidth+";");


                    treeGrid.add(treeButton,1,trow);
                    trow++;
                }


            } else {
                if(file.isHidden() || !Files.isReadable(file.toPath()) || Files.isSymbolicLink(file.toPath())){

                }else {
                    Button fileButton = new Button();
                    fileButton.setStyle("-fx-min-height: 75px;-fx-min-width: 100px;-fx-max-height: 75px;-fx-max-width: 100px");
                    fileButton.setText(String.valueOf(file.getName()));
                    fileButton.setTooltip(new Tooltip(file.getName()));
                     ContextMenu fileContextMenu = new ContextMenu();

                    MenuItem copy = new MenuItem("Kopiuj");
                    MenuItem cut = new MenuItem("Wytnij");
                    MenuItem delete = new MenuItem("Usuń");
                    MenuItem paste = new MenuItem("Wklej");

                    paste.setOnAction((ActionEvent) -> {
                        CopyOption[] options = new CopyOption[]{
                                StandardCopyOption.REPLACE_EXISTING,
                                StandardCopyOption.COPY_ATTRIBUTES
                        };

                        if(copyPath != null && cutPath != null) {
                            if (cutPath.equals(Paths.get("")) && !copyPath.equals(Paths.get(""))) {
                                try {
                                    Files.copy(copyPath, actualPath.resolve(copyPath.getFileName()), options);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            } else if (copyPath.equals(Paths.get("")) && !cutPath.equals(Paths.get(""))) {
                                try {
                                    Files.copy(cutPath, actualPath.resolve(cutPath.getFileName()), options);
                                    Files.delete(cutPath);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                            }
                            copyPath = Paths.get("");
                            actualPath = Paths.get("");
                            setTree(actualPath.toString());
                        }

                    });

                    copy.setOnAction((ActionEvent) -> {
                        copyPath = file.toPath();
                        cutPath = Paths.get("");
                    });

                    cut.setOnAction((ActionEvent) -> {
                        cutPath = file.toPath();
                        copyPath = Paths.get("");
                    });

                    delete.setOnAction((ActionEvent) -> {
                        try {
                            Files.delete(file.toPath());
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        setTree(actualPath.toString());
                    });



                    fileContextMenu.getItems().addAll(cut,copy,delete);
                    fileContextMenu.getItems().add(paste);

                    fileButton.setOnContextMenuRequested(new EventHandler<ContextMenuEvent>() {
                        @Override
                        public void handle(ContextMenuEvent event) {
                            fileContextMenu.show(fileButton,event.getScreenX(),event.getScreenY());
                        }
                    });

                    fileButton.setOnAction((ActionEvent) -> {
                        try {
                            Desktop.getDesktop().open(file);
                        } catch (IOException ex) {
                            Alert alert = new Alert(Alert.AlertType.WARNING);
                            alert.setTitle("Błąd");
                            alert.setContentText("Brak domyslnego programu.");
                            alert.showAndWait();
                        }
                    });
                    iconsGrid.add(fileButton, icol, irow);
                    icol++;
                    if (icol > imaxCols) {
                        irow++;
                        icol = 1;
                    }
                }
            }

        }

        iconsField.setContent(iconsGrid);
        treeField.setContent(treeGrid);
    }

}