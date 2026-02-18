package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import util.SceneUtil;

public class WelcomeController {

    @FXML
    void login(ActionEvent event) {
        SceneUtil.switchTo(event, "Login.fxml");
    }

    @FXML
    void register(ActionEvent event) {
        SceneUtil.switchTo(event, "Register.fxml");
    }
}
