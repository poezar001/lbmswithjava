package application;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage stage) throws Exception {

    	FXMLLoader loader = new FXMLLoader(
    		    getClass().getResource("/resources/view/Welcome.fxml")
    		);
    	
    	
        Scene scene = new Scene(loader.load());
        
   
       scene.getStylesheets().add(getClass().getResource("/resources/css/application.css").toExternalForm());
     
        stage.setTitle("Library Management System");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
