package client.view;

import client.api.Command;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXPasswordField;
import com.jfoenix.controls.JFXTextField;
import javafx.scene.control.TextInputControl;
import server.controller.account.AccountControl;
import javafx.animation.*;
import javafx.collections.ObservableList;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import server.model.existence.Account;
import notification.Notification;
import server.server.Response;

import java.io.*;

public class ProfileProcessor extends Processor {
    private static AccountControl accountControl = AccountControl.getController();
    private Account account;



    public BorderPane mainPane;
    public Pane profileMenusPane;
    public Pane profileInfoPane, profilePasswordPane, profileCreditPane;
    public Pane profileInfoButton, profilePasswordButton, profileCreditButton;
    public HBox optionsHBox;

    public JFXTextField usernameField, firstNameField, lastNameField, emailField, creditField, brandField;
    public Label creditLabel, brandLabel;
    public JFXButton saveChangesButton;

    public StackPane profilePictureStackPane;
    public Circle pictureCircle;
    public ImageView deleteImage;
    public Rectangle rightLine, rightLine1;

    public JFXTextField minimumCreditField;
    public JFXTextField currentCreditField, additionCreditField;
    public JFXTextField bankUsername;
    public JFXPasswordField bankPassword;
    public JFXTextField accountNumber;
    public JFXButton subButton, addButton;

    public JFXPasswordField oldPasswordField, newPasswordField;
    public JFXButton changePasswordButton;

    private boolean doesUserHavePicture;
    private File pictureFile;
    private String pictureExtension;

    public void init(Account account, String location) {
        this.account = account;

        if(location.equals("ProfileMenu")) {
            setStyleSheets();
            adjustMainPanesForAccounts();
        } else if(location.equals("profileInfoMenu")) {
            setProfileInfoSpecifications();
            setProfileInfoFields();
        } else if(location.equals("profileCreditMenu")) {
            setProfileCreditFields();
        } else if(location.equals("profilePasswordMenu")) {
            setProfilePasswordFields();
        }
    }


    private void adjustMainPanesForAccounts() {
        changeCenterPane("profileInfoMenu");
        changeButtonBackGroundColor(profileInfoButton);

        if(account.getType().equals("Admin")) {
            optionsHBox.getChildren().remove(profileCreditButton);
        }
        if(!account.getUsername().equals(getUsername())) {
            optionsHBox.getChildren().remove(profilePasswordButton);
            optionsHBox.getChildren().remove(profileCreditButton);
        }
    }

    private void setProfileInfoSpecifications() {
        setStringFields(firstNameField, 25);
        setStringFields(lastNameField, 25);
        setStringFields(emailField, 35);

        if(account.getType().equals("Vendor"))
            setStringFields(brandField, 35);
    }

    private void setProfileInfoFields() {
        usernameField.setText(account.getUsername());
        firstNameField.setText(account.getFirstName());
        lastNameField.setText(account.getLastName());
        emailField.setText(account.getEmail());

        ImagePattern imagePattern = new ImagePattern(getProfileImage(account.getUsername()));
        pictureCircle.setFill(imagePattern);

        if(!(doesUserHavePicture = doesUserHaveImage())) {
            deleteImage.setDisable(true);
            deleteImage.setOpacity(0.7);
        }
        if(account.getType().equals("Vendor"))
            brandField.setText(account.getBrand());
        else {
            profileInfoPane.getChildren().remove(brandField);
            profileInfoPane.getChildren().remove(brandLabel);
        }

        if(account.getType().equals("Admin")) {
            profileInfoPane.getChildren().remove(creditField);
            profileInfoPane.getChildren().remove(creditLabel);
        } else {
            creditField.setText(Double.toString(account.getCredit()));
        }

        if(!account.getUsername().equals(super.getUsername())) {
            profileInfoPane.getChildren().remove(deleteImage);
            profileInfoPane.getChildren().remove(saveChangesButton);

            firstNameField.setEditable(false);
            firstNameField.setDisable(true);

            lastNameField.setEditable(false);
            lastNameField.setDisable(true);

            emailField.setEditable(false);
            emailField.setDisable(true);

            brandField.setEditable(false);
            brandField.setDisable(true);

            pictureCircle.setOnMouseClicked(null);
            pictureCircle.setOnMouseEntered(null);
            pictureCircle.setOnMouseExited(null);
        }

    }

    private boolean doesUserHaveImage() {
        Command<String> command = new Command<>("does user have image", Command.HandleType.ACCOUNT, account.getUsername());
        Response<Boolean> response = client.postAndGet(command, Response.class, (Class<Boolean>)Boolean.class);
        return response.getData().get(0);
    }

    private void setProfileCreditFields() {
        currentCreditField.setText(Double.toString(account.getCredit()));
        minimumCreditField.setText(getMinimumWallet());

        if(!account.getUsername().equals(super.getUsername())) {
            profileCreditPane.getChildren().remove(addButton);
            profileCreditPane.getChildren().remove(subButton);
        }

        if(getType().equals("Customer")) {
            profileCreditPane.getChildren().remove(subButton);
        }

        setDoubleFields(additionCreditField, Double.MAX_VALUE);
//        additionCreditField.textProperty().addListener(new ChangeListener<String>() {
//            @Override
//            public void changed(ObservableValue<? extends String> observable, String oldValue,
//                                String newValue) {
//                //Todo Checking
//
//                if(newValue.equals(".")) {
//                    additionCreditField.setText("0.");
//                } else if (!newValue.matches("\\d+(.(\\d)+)?")) {
//                    if(additionCreditField.getText().contains(".")) {
//                        additionCreditField.setText(removeDots(additionCreditField.getText()));
//                    } else {
//                        additionCreditField.setText(newValue.replaceAll("[^\\d\\.]", ""));
//                    }
//                }
//            }
//        });
    }

    private void setProfilePasswordFields() {
        setStringFields(oldPasswordField, 16);
        setStringFields(newPasswordField, 16);
    }

    public void setStyleSheets() {
        //Todo
        String accountType = account.getType(), styleSheet;
        ObservableList<String> styleSheets = mainPane.getStylesheets();
        styleSheets.removeAll(styleSheets);

        if(accountType.equals("Admin")) {
            styleSheet = "AdminProfileMenu.css";
        } else if(accountType.equals("Vendor")) {
            styleSheet = "VendorProfileMenu.css";
        } else {
            styleSheet = "CustomerProfileMenu.css";
        }
        styleSheets.add(getClass().getResource(styleSheet).toExternalForm());
    }

    public void upPaneMouseClicked(MouseEvent mouseEvent) {
        changeButtonBackGroundColor((Pane) mouseEvent.getSource());
        changeCenterPane(getCenterPaneName(((Pane) mouseEvent.getSource()).getId()));
        //Todo
    }

    public String getCenterPaneName(String paneId) {
        String centerPaneName1 = paneId.substring(0, paneId.length() - 6).concat("Menu");
        return centerPaneName1;
    }

    public void changeCenterPane(String centerPaneName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(centerPaneName + ".fxml"));
            Parent subRoot = loader.load();
            ProfileProcessor profileProcessor = loader.getController();
            profileProcessor.init(account, centerPaneName);
            mainPane.setBottom(subRoot);
            Pane pane = (Pane) mainPane.getBottom();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void clearButtonBackGrounds() {
        profileInfoButton.setBackground(Background.EMPTY);
        profileCreditButton.setBackground(Background.EMPTY);
        profilePasswordButton.setBackground(Background.EMPTY);
    }

    public void changeButtonBackGroundColor(Pane pane) {
        clearButtonBackGrounds();
        BackgroundFill backgroundFill = new BackgroundFill(getButtonBackGroundColor(), CornerRadii.EMPTY, Insets.EMPTY);
        Background background = new Background(backgroundFill);
        pane.setBackground(background);
    }

    private Color getButtonBackGroundColor() {
        Color color = null;

        switch (getType()) {
            case "Admin":
                color = Color.valueOf("#80CBC4");
                break;
            case "Vendor":
                color = Color.valueOf("#233C5E");
                break;
            case "Customer":
                color = Color.valueOf("#FFB74D");
                break;
        }

        return color;
    }

    public void editPersonalInfoMouseClicked(MouseEvent mouseEvent) {

        account.setFirstName(firstNameField.getText());
        account.setLastName(lastNameField.getText());
        account.setEmail(emailField.getText());

        if(account.getType().equals("Vendor"))
            account.setBrand(brandField.getText());

        if(doesUserHavePicture) {
            if(pictureFile != null) {
                sendUserImage(account.getUsername(), pictureFile, pictureExtension);
            }
        } else
            deleteUserImage(account.getUsername());

        Command<Account> command = new Command<>("edit account info", Command.HandleType.ACCOUNT, account);
        Response response = client.postAndGet(command, Response.class, (Class<Object>)Object.class);
        Alert alert = response.getMessage().getAlert();

        if(alert.getTitle().equals("Edit Successful")) {
            account = getAccountByUsername(account.getUsername());
            firstNameField.setText(account.getFirstName());
            lastNameField.setText(account.getLastName());
            lastNameField.setText(account.getLastName());
            emailField.setText(account.getEmail());
            brandField.setText(account.getBrand());
        }

        alert.show();
    }

    private void deleteUserImage(String username) {
        Command<String> command = new Command<>("delete user image", Command.HandleType.ACCOUNT, username);
        client.postAndGet(command, Response.class, (Class<Object>)Object.class);
    }

    private void sendUserImage(String username, File pictureFile, String pictureExtension) {
        Command<String> command = new Command<>("send user image", Command.HandleType.FILE_SEND, username, pictureExtension);
        client.sendImage(command, pictureFile);
    }

    public void changePasswordMouseClicked(MouseEvent mouseEvent) {
//        Alert alert = accountControl.changePassword(oldPasswordField.getText(), newPasswordField.getText()).getAlert();
        Command<String> command = new Command<>("change password", Command.HandleType.ACCOUNT, oldPasswordField.getText(), newPasswordField.getText());
        Response response = client.postAndGet(command, Response.class, (Class<Object>)Object.class);
        Alert alert = response.getMessage().getAlert();

        if(alert.getHeaderText().contains("New") || alert.getHeaderText().equals("Duplicate Password"))
            newPasswordField.setStyle(errorTextFieldStyle);
        if(alert.getHeaderText().contains("Old"))
            oldPasswordField.setStyle(errorTextFieldStyle);

        alert.show();
    }

    public void addMoneyMouseClicked(MouseEvent mouseEvent) {
//        Notification notification = accountControl.addMoney(additionCreditField.getText());
        Command<String> command = new Command<>("add money", Command.HandleType.ACCOUNT,
                additionCreditField.getText(), bankUsername.getText(), bankPassword.getText(), accountNumber.getText());
        Response response = client.postAndGet(command, Response.class, (Class<Object>)Object.class);
        Notification notification = response.getMessage();

        if(notification.equals(Notification.RISE_MONEY_SUCCESSFULLY)) {
            resetMoney();
        } else if(notification == Notification.INVALID_TRANSACTION_INFO) {
            bankUsername.setStyle(errorTextFieldStyle);
            bankPassword.setStyle(errorTextFieldStyle);
            accountNumber.setStyle(errorTextFieldStyle);
        } else {
            additionCreditField.setStyle(errorTextFieldStyle);
        }

        notification.getAlert().show();
    }

    public void subtractMoneyMouseClicked(MouseEvent mouseEvent) {
//        Notification notification = accountControl.getMoney(additionCreditField.getText());
        Command<String> command = new Command<>("subtract money", Command.HandleType.ACCOUNT,
                additionCreditField.getText(), bankUsername.getText(), bankPassword.getText(), accountNumber.getText());
        Response response = client.postAndGet(command, Response.class, (Class<Object>)Object.class);
        Notification notification = response.getMessage();

        if(notification.equals(Notification.GET_MONEY_SUCCESSFULLY)) {
            resetMoney();
        } else if(notification == Notification.INVALID_TRANSACTION_INFO) {
            bankUsername.setStyle(errorTextFieldStyle);
            bankPassword.setStyle(errorTextFieldStyle);
            accountNumber.setStyle(errorTextFieldStyle);
        } else {
            additionCreditField.setStyle(errorTextFieldStyle);
        }

        notification.getAlert().show();
    }

    public void resetMoney() {
        double credit = getCredit(account.getUsername());
        account.setCredit(credit);
        currentCreditField.setText(Double.toString(credit));
    }

    public void textFieldMouseClicked(MouseEvent mouseEvent) {
        TextInputControl textInputControl = (TextInputControl) mouseEvent.getSource();
        textInputControl.setStyle("");
    }

    public void passwordFieldMouseClicked(MouseEvent mouseEvent) {
        JFXPasswordField passwordField = (JFXPasswordField) mouseEvent.getSource();
        passwordField.setStyle("");
    }

    public void chooseAccountPictureMouseClicked(MouseEvent mouseEvent) {
        FileChooser pictureChooser = new FileChooser();

        FileChooser.ExtensionFilter jpgExtensionFilter = new FileChooser.ExtensionFilter("JPG Files", "*.JPG");
        FileChooser.ExtensionFilter jpegExtensionFilter = new FileChooser.ExtensionFilter("JPEG Files", "*.JPEG");
        FileChooser.ExtensionFilter pngExtensionFilter = new FileChooser.ExtensionFilter("PNG Files", "*.PNG");
        FileChooser.ExtensionFilter bmpExtensionFilter = new FileChooser.ExtensionFilter("BMP Files", "*.BMP");

        pictureChooser.getExtensionFilters().add(jpgExtensionFilter);
        pictureChooser.getExtensionFilters().add(jpegExtensionFilter);
        pictureChooser.getExtensionFilters().add(pngExtensionFilter);
        pictureChooser.getExtensionFilters().add(bmpExtensionFilter);

        File pictureFile = pictureChooser.showOpenDialog(null);

        try {
            if(pictureFile != null) {
                FileInputStream fileInputStream = new FileInputStream(pictureFile);
                Image image = new Image(fileInputStream);

                //Todo Sending Image To Controller
                //Todo Showing Image

                doesUserHavePicture = true;
                this.pictureFile = pictureFile;
                this.pictureExtension = pictureChooser.getSelectedExtensionFilter().getExtensions().get(0).substring(2);

                pictureCircle.setFill(new ImagePattern(image));
                deleteImage.setDisable(false);
                deleteImage.setOpacity(1.0);
                fileInputStream.close();
            }
        } catch (IOException e) {
            //:)
        }

    }

    public void deleteImage(MouseEvent mouseEvent) {
        try {
            FileInputStream imageFileInputStream = new FileInputStream("src\\main\\resources\\" + IMAGE_FOLDER_URL + "DefaultProfilePicture.png");
            pictureCircle.setFill(new ImagePattern(new Image(imageFileInputStream)));
            doesUserHavePicture = false;
            pictureFile = null;
            deleteImage.setDisable(true);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public ImageView getDeleteImageButton() {
        ImageView deleteImage = new ImageView();
        deleteImage.getStyleClass().add("Delete_Image");

        deleteImage.setFitWidth(32);
        deleteImage.setFitHeight(32);

        deleteImage.setLayoutX(147);
        deleteImage.setLayoutY(32);

        deleteImage.setId("deleteImage");
        //Todo Set On MouseClicked

        return deleteImage;
    }

    public void profilePictureMouseEntered(MouseEvent mouseEvent) {
        Circle circle = new Circle(45);

        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream("src\\main\\resources\\" + IMAGE_FOLDER_URL + "ProfileInfoMenu - Camera.png");
            Image image = new Image(fileInputStream);
            ImagePattern imagePattern = new ImagePattern(image);
            circle.setFill(imagePattern);
            fileInputStream.close();
        } catch (IOException e) {
            //:)
        }

        profilePictureStackPane.getChildren().remove(1, profilePictureStackPane.getChildren().size());
        profilePictureStackPane.getChildren().add(circle);
        profilePictureStackPane.setStyle("-fx-cursor: hand");
        circle.setDisable(true);

        circle.translateYProperty().set(20);
        Timeline timeline = new Timeline();
        KeyValue keyValue = new KeyValue(circle.translateYProperty(), 0, Interpolator.EASE_IN);
        KeyFrame keyFrame = new KeyFrame(Duration.seconds(0.5), keyValue);
        timeline.getKeyFrames().add(keyFrame);
        timeline.play();

    }

    public void profilePictureMouseExited(MouseEvent mouseEvent) {
        Circle circle = (Circle) profilePictureStackPane.getChildren().get(1);

        Timeline timeline = new Timeline();
        circle.translateYProperty().set(0);
        KeyValue keyValue = new KeyValue(circle.translateYProperty(), 20, Interpolator.EASE_OUT);
        KeyFrame keyFrame = new KeyFrame(Duration.seconds(0.5), keyValue);
        timeline.getKeyFrames().add(keyFrame);
        timeline.setOnFinished(event -> profilePictureStackPane.getChildren().remove(circle));
        timeline.play();

    }

}
