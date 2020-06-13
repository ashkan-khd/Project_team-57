package view;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXTextField;
import controller.account.AccountControl;
import controller.account.AdminControl;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.ImagePattern;
import javafx.scene.shape.Circle;
import javafx.stage.Popup;
import javafx.stage.Stage;
import model.existence.Account;
import model.existence.Discount;
import java.io.IOException;
import java.sql.Date;
import java.util.ArrayList;
import java.util.Optional;

import static model.existence.Account.AccountType.*;

public class TableViewProcessor<T> implements TableHold {

    public JFXButton showDiscountButton;
    public JFXButton deleteDiscountButton;
    public JFXButton addDiscountButton;
    public Label codeLabel;
    public JFXButton customerSearchButton;
    public Label discountAddedUsersCountLabel;
    public JFXListView<String> discountCustomersListView;
    public JFXTextField discountCustomerSearchField;


    public static enum TableViewType {
        CUSTOMERS(CUSTOMER), VENDORS(VENDOR), ADMINS(ADMIN), DISCOUNTS, DISCOUNT_CUSTOMERS;

        Account.AccountType accountType;

        TableViewType(Account.AccountType accountType) {
            this.accountType = accountType;
        }

        TableViewType() {
        }

        public Account.AccountType getAccountType() {
            return accountType;
        }
    }

    public Circle imageCircle;
    public Label nameLabel;
    public JFXButton showProfileButton;
    public JFXButton deleteUserButton;
    public JFXButton approveUserButton;
    public Label typeLabel;
    public JFXButton addAdminButton;
    public BorderPane mainBorderPane;
    public TableView<T> tableView;
    private TableHold parentProcessor;
    private TableViewType tableViewType;
    private T selectedItem;
    private String searchedUsername;

    public void initProcessor(TableViewType tableViewType) {
        this.tableViewType = tableViewType;
        initColumns();
        updateTable();
        initOptions();
    }

    private void initColumns() {
        switch (tableViewType) {
            case ADMINS:
            case VENDORS:
            case CUSTOMERS:
                initAccountColumns();
                break;
            case DISCOUNTS:
                initDiscountColumns();
                break;
            case DISCOUNT_CUSTOMERS:
                initDiscountCustomersColumns();
                break;
        }
    }

    private void initDiscountCustomersColumns() {
        TableColumn<T, String> usernameColumn = makeColumn("Username", "username", 0.30);
        TableColumn<T, String> firstNameColumn = makeColumn("First Name", "firstName", 0.33);
        TableColumn<T, String> lastNameColumn = makeColumn("Last Name", "lastName", 0.33);
        tableView.getColumns().addAll(usernameColumn, firstNameColumn, lastNameColumn);
    }

    private void initDiscountColumns() {
        TableColumn<T, String> codeColumn = makeColumn("Code", "code", 0.20);
        TableColumn<T, Date> startDateColumn = makeColumn("Start Date", "startDate", 0.20);
        TableColumn<T, Date> finishDateColumn = makeColumn("Finish Date", "finishDate", 0.20);
        TableColumn<T, Double> percentColumn = makeColumn("Percentage", "discountPercent", 0.08);
        TableColumn<T, Double> maxValColumn = makeColumn("Max Value", "maxDiscount", 0.18);
        TableColumn<T, Integer> maxRep = makeColumn("Max Repetition", "maxRepetition", 0.10);
        tableView.getColumns().addAll(codeColumn, startDateColumn, finishDateColumn, percentColumn, maxValColumn, maxRep);
    }

    private void initAccountColumns() {
        TableColumn<T, String> usernameColumn = makeColumn("Username", "username", 0.23);
        TableColumn<T, String> firstNameColumn = makeColumn("First Name", "firstName", 0.30);
        TableColumn<T, String> lastNameColumn = makeColumn("Last Name", "lastName", 0.30);
        TableColumn<T, String> approvalColumn = makeColumn("Approval", "isApproved", 0.15);
        tableView.getColumns().addAll(usernameColumn, firstNameColumn, lastNameColumn, approvalColumn);
    }

    private<E> TableColumn<T, E> makeColumn(String text, String property, double sizePercentage){
        TableColumn<T, E> column = new TableColumn<>(text);
        column.prefWidthProperty().bind(tableView.widthProperty().multiply(sizePercentage));
        column.setResizable(false);
        column.setSortable(false);
        column.setEditable(false);
        column.setStyle("-fx-alignment: CENTER");
        column.setCellValueFactory(new PropertyValueFactory<>(property));
        return column;
    }

    public void updateTable() {
        ObservableList<T> tableList = FXCollections.observableArrayList();
        tableView.getItems().remove(0, tableView.getItems().size());
        switch (tableViewType) {
            case ADMINS:
            case VENDORS:
            case CUSTOMERS:
                tableList.addAll((ArrayList<T>)AccountControl.getController().getModifiedAccounts(tableViewType.getAccountType()));
                break;
            case DISCOUNTS:
                tableList.addAll((ArrayList<T>)AdminControl.getController().getAllDiscounts());
                break;
            case DISCOUNT_CUSTOMERS:
                if(searchedUsername != null && searchedUsername.length() > 0) {
                    tableList.addAll((ArrayList<T>)AccountControl.getController().getModifiedAccounts(CUSTOMER, searchedUsername));
                } else
                    tableList.addAll((ArrayList<T>)AccountControl.getController().getModifiedAccounts(CUSTOMER));
                break;
        }
        tableView.getItems().addAll(tableList);
        tableView.getSelectionModel().selectFirst();
        selectedItem = tableView.getSelectionModel().getSelectedItem();
    }

    private void initOptions(MouseEvent... mouseEvents) {
        switch (tableViewType) {
            case ADMINS:
            case VENDORS:
            case CUSTOMERS:
                mainBorderPane.setLeft(initAccountOptions());
                break;
            case DISCOUNTS:
                mainBorderPane.setLeft(initDiscountOptions());
                break;
            case DISCOUNT_CUSTOMERS:
                if(mouseEvents != null && mouseEvents.length != 0) {
                    showPopUp(mouseEvents[0]);
                }
                mainBorderPane.setLeft(initDiscountCustomersOptions());
                break;
        }
    }

    private void showPopUp(MouseEvent mouseEvent) {
        if(selectedItem != null) {
            Popup popup = new Popup();
            VBox vBox = new VBox();
            JFXButton addCustomerButton = new JFXButton("Add Customer");
            JFXButton removeCustomerButton = new JFXButton("Remove Customer");
            if(((DiscountProcessor)parentProcessor).isCustomerAdded(((Account)selectedItem).getUsername()))
                vBox.getChildren().add(removeCustomerButton);
            else
                vBox.getChildren().add(addCustomerButton);
            addCustomerButton.setOnAction(event -> {
                ((DiscountProcessor)parentProcessor).addCustomer(((Account)selectedItem).getUsername());
            });
            removeCustomerButton.setOnAction(event -> {
                ((DiscountProcessor)parentProcessor).removeCustomer(((Account)selectedItem).getUsername());
            });
            addCustomerButton.setPadding(new Insets(10, 10 , 10 , 10));
            removeCustomerButton.setPadding(new Insets(10, 10 , 10 , 10));
            popup.getContent().addAll(vBox);
            popup.show(tableView, mouseEvent.getX(), mouseEvent.getY());
        }
    }

    private Pane initDiscountCustomersOptions() {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("TableViewDiscountCustomersOption.fxml"));
        try {
            Pane root = loader.load();
            TableViewProcessor processor = loader.getController();
            processor.setParentProcessor(this);
            processor.discountCustomersListView.getItems().addAll
                    (((DiscountProcessor)((TableViewProcessor)parentProcessor).getParentProcessor()).getDicountAddedCustomers());
            return root;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Pane initDiscountOptions() {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("TableViewDiscountOptions.fxml"));
        try {
            Pane root = loader.load();
            TableViewProcessor processor = loader.getController();
            processor.setParentProcessor(this);
            if(selectedItem != null) {
                Discount discount = (Discount)selectedItem;
                processor.showDiscountButton.setDisable(false);
                processor.deleteDiscountButton.setDisable(false);
                processor.codeLabel.setText(discount.getCode());
            } else {
                processor.terminateOptions();
            }
            return root;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private Pane initAccountOptions() {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("TableViewAccountOptions.fxml"));
        try {
            Pane root = loader.load();
            TableViewProcessor processor = loader.getController();
            processor.setParentProcessor(this);
            if(tableViewType != TableViewType.ADMINS) {
                root.getChildren().remove(processor.addAdminButton);
            }
            if(selectedItem != null) {
                Account account = (Account)selectedItem;
                processor.nameLabel.setText(account.getFirstName() + " " + account.getLastName());
                processor.typeLabel.setText(account.getType());
                processor.imageCircle.setFill
                        (new ImagePattern(AccountControl.getController().getProfileImageByUsername(account.getUsername())));
                switch (account.getType()) {
                    case "Admin" :
                        processor.showProfileButton.setDisable(false);
                        processor.approveUserButton.setDisable(account.isApproved());
                        processor.deleteUserButton.setDisable(true);
                        break;
                    default:
                        processor.showProfileButton.setDisable(false);
                        processor.approveUserButton.setDisable(account.isApproved());
                        processor.deleteUserButton.setDisable(false);
                }
            } else {
                processor.terminateOptions();
            }
            return root;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    private void terminateOptions() {
        switch (((TableViewProcessor)parentProcessor).tableViewType) {
            case ADMINS:
            case VENDORS:
            case CUSTOMERS:
                terminateAccountOptions();
                break;
            case DISCOUNTS:
                terminateDiscountOptions();
                break;
        }
    }

    private void terminateDiscountOptions() {
        showDiscountButton.setDisable(true);
        deleteDiscountButton.setDisable(true);
        codeLabel.setText("Discount Code");
    }

    private void terminateAccountOptions() {
        imageCircle.setFill(new ImagePattern
                (AccountControl.getController().getProfileImageByUsername("1")));
        showProfileButton.setDisable(true);
        approveUserButton.setDisable(true);
        deleteUserButton.setDisable(true);
        nameLabel.setText("Username");
        typeLabel.setText("Type");
    }

    public void updateSelectedItem(MouseEvent... mouseEvents) {
        if(tableView.getSelectionModel().getSelectedItem() != null)
            selectedItem = tableView.getSelectionModel().getSelectedItem();
        else
            tableView.getSelectionModel().selectFirst();
        initOptions(mouseEvents);
    }

    public void onMouse(MouseEvent mouseEvent) {
        ((JFXButton)mouseEvent.getSource()).setStyle("-fx-border-color: #0277bd;" +
                "-fx-border-width: 0 0 3 0;" +
                "-fx-background-radius: 0 0 0 0;" +
                "-fx-cursor: hand;");
    }

    public void outMouse(MouseEvent mouseEvent) {
        ((JFXButton)mouseEvent.getSource()).setStyle("-fx-background-color: #ffffff;" +
                "-fx-background-radius: 10 10 10 10;");
    }

    //Inside Methods

    public void showProfile(ActionEvent actionEvent) {
        try {
            Account selectedAccount = (Account) ((TableViewProcessor)parentProcessor).selectedItem;
            ProfileProcessor.setAccount(selectedAccount);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("ProfileMenu.fxml"));
            Parent root = loader.load();
            Stage newStage = new Stage();
            newStage.setScene(new Scene(root));
            newStage.getIcons().add(new Image(getClass().getResourceAsStream("Profile Icon.png")));
            newStage.setResizable(false);
            newStage.setTitle(selectedAccount.getUsername() + " Profile");
            newStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void deleteUser(ActionEvent actionEvent) {
        Account selectedAccount = (Account) ((TableViewProcessor)parentProcessor).selectedItem;
        Optional<ButtonType> buttonType = new Alert
                (Alert.AlertType.CONFIRMATION, "Are You Sure You Want To Delete " + selectedAccount.getUsername() + "?", ButtonType.YES, ButtonType.NO).showAndWait();
        if(buttonType.get() == ButtonType.YES) {
            AdminControl.getController().deleteUserWithUsername(selectedAccount.getUsername()).getAlert().show();
        }
        ((TableViewProcessor)parentProcessor).updateTable();
        ((TableViewProcessor)parentProcessor).updateSelectedItem();
    }

    public void approveUser(ActionEvent actionEvent) {
        Account selectedAccount = (Account) ((TableViewProcessor)parentProcessor).selectedItem;
        Optional<ButtonType> buttonType = new Alert
                (Alert.AlertType.CONFIRMATION, "Are You Sure You Want To Approve " + selectedAccount.getUsername() + "?", ButtonType.YES, ButtonType.NO).showAndWait();
        if(buttonType.get() == ButtonType.YES) {
            AccountControl.getController().modifyApprove(selectedAccount.getUsername(), 1).getAlert().show();
        }
        ((TableViewProcessor)parentProcessor).updateTable();
        ((TableViewProcessor)parentProcessor).updateSelectedItem();
    }

    public void addNewAdmin(ActionEvent actionEvent) {
        try {
            SignUpProcessor.setIsNormal(false);
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("SignUp.fxml"));
            Parent root = loader.load();
            SignUpProcessor signUpProcessor = loader.getController();
            Stage newStage = new Stage();
            newStage.setScene(new Scene(root));
            signUpProcessor.setMyStage(newStage);
            newStage.getIcons().add(new Image(getClass().getResourceAsStream("Login Icon.png")));
            newStage.setResizable(false);
            newStage.setTitle("Register New Admin");
            newStage.show();
            newStage.setOnCloseRequest(event -> {
                ((TableViewProcessor)parentProcessor).updateTable();
                ((TableViewProcessor)parentProcessor).updateSelectedItem();
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setParentProcessor(TableHold parentProcessor) {
        this.parentProcessor = parentProcessor;
    }

    public TableHold getParentProcessor() {
        return parentProcessor;
    }

    public void showDiscount(ActionEvent actionEvent) {

    }

    public void deleteDiscount(ActionEvent actionEvent) {
        Discount selectedDiscount = (Discount) selectedItem;
        Optional<ButtonType> buttonType = new Alert
                (Alert.AlertType.CONFIRMATION, "Are You Sure You Want To Delete " + selectedDiscount.getCode() + "?", ButtonType.YES, ButtonType.NO).showAndWait();
        if(buttonType.get() == ButtonType.YES) {
            AdminControl.getController().removeDiscountByID(selectedDiscount.getID());
        }
    }

    public void addNewDiscount(ActionEvent actionEvent) {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("DiscountMenu.fxml"));
        try {
            Parent root = loader.load();
            Stage newStage = new Stage();
            newStage.setScene(new Scene(root));
            newStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void customerSearch() {
        ((TableViewProcessor)parentProcessor).searchedUsername = discountCustomerSearchField.getText();
        ((TableViewProcessor)parentProcessor).updateTable();
        ((TableViewProcessor)parentProcessor).updateSelectedItem();
    }

    public void discountCustomerSearchKeyPressed(KeyEvent keyEvent) {
        if(keyEvent.getCode() == KeyCode.ENTER)
            customerSearch();
    }


}
