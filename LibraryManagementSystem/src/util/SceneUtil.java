package util;

import javafx.fxml.FXMLLoader;
import javafx.scene.*;
import javafx.stage.Stage;
import javafx.event.ActionEvent;

public class SceneUtil {

	public static void switchTo(ActionEvent event, String fxml) {
	    try {
	        // We use the full path including 'resources'
	        String path = "/resources/view/" + fxml; 
	        java.net.URL resource = SceneUtil.class.getResource(path);

	        if (resource == null) {
	            System.err.println("❌ Critical Error: Could not find FXML at " + path);
	            return;
	        }

	        Parent root = FXMLLoader.load(resource);
	        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
	        stage.setScene(new Scene(root));
	        stage.show();

	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
}
