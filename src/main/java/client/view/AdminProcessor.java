package client.view;

import client.api.Command;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import server.controller.account.AdminControl;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import server.model.existence.Account;
import server.model.existence.Comment;
import server.model.existence.Discount;
import server.model.existence.Off;
import server.server.Response;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class AdminProcessor extends AccountProcessor implements Initializable {
    public JFXButton dashboardButton;
    public BorderPane mainPane;
    public JFXButton accountsButton;
    public JFXButton productsButton;
    public JFXButton offsButton;
    public Label usernameLabel;
    public JFXButton mainMenuButton;
    public Pane manageCustomers;
    public Pane manageVendors;
    public Pane manageAdmins;
    public JFXTextField wageField;
    public JFXTextField minimumWalletField;
    private ArrayList<JFXButton> buttons = new ArrayList<>();


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loggedInAccount = getLoggedInAccount();

        if(location.toString().contains("AdminMenu")) {
            initButtons();
            selectThisButton(dashboardButton);
            initLabelsForUsername();
            FXMLLoader loader = new FXMLLoader(getClass().getResource("AdminDashboard.fxml"));
            Parent subRoot = null;
            try {
                subRoot = loader.load();
                ((AdminProcessor)loader.getController()).setParentProcessor(this);
            } catch (IOException e) {
               e.printStackTrace();
            }
            loader.setController(this);
            mainPane.setCenter(subRoot);
            client.postAndGet(new Command("create discount added users", Command.HandleType.GENERAL), Response.class, (Class<Object>)Object.class);
            initAudios();
            initMusicPlayer();
        } else if(location.toString().contains("AdminCashStats")){
            setDoubleFields(wageField, 100.0);
            setDoubleFields(minimumWalletField, Double.MAX_VALUE);
            wageField.setText(getWage());
            minimumWalletField.setText(getMinimumWallet());
        }
    }

    private void initLabelsForUsername() {
        usernameLabel.setText(loggedInAccount.getUsername());
    }

    private void initButtons() {
        buttons.add(dashboardButton);
        buttons.add(accountsButton);
        buttons.add(productsButton);
        buttons.add(offsButton);
        buttons.add(mainMenuButton);
    }

    private void selectThisButton(JFXButton selectedButton) {
        selectedButton.setRipplerFill(Color.valueOf("#80cbc4"));
        selectedButton.setStyle("-fx-background-color: #80cbc4;");
        for (JFXButton button : buttons) {
            if(button != selectedButton) {
                button.setRipplerFill(Color.WHITE);
                button.setStyle("-fx-background-color: #ffffff;");
            }
        }
    }

    public void marketStats(MouseEvent mouseEvent) {
    }

    public void setOptions(ActionEvent actionEvent) {
        JFXButton selectedButton = (JFXButton) actionEvent.getSource();
        selectThisButton(selectedButton);
        try {
            if (selectedButton.equals(dashboardButton)) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("AdminDashboard.fxml"));
                Parent subRoot = loader.load();
                ((AdminProcessor)loader.getController()).setParentProcessor(this);
                mainPane.setCenter(subRoot);
            } else if (selectedButton.equals(accountsButton)) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("AdminAccounts.fxml"));
                Parent subRoot = loader.load();
                ((AdminProcessor)loader.getController()).setParentProcessor(this);
                mainPane.setCenter(subRoot);
            } else if(selectedButton.equals(productsButton)) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("AdminProducts.fxml"));
                Parent subRoot = loader.load();
                ((AdminProcessor)loader.getController()).setParentProcessor(this);
                mainPane.setCenter(subRoot);
            } else if(selectedButton.equals(offsButton)) {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("AdminOffs.fxml"));
                Parent subRoot = loader.load();
                ((AdminProcessor)loader.getController()).setParentProcessor(this);
                mainPane.setCenter(subRoot);
            }
        } catch (IOException e) {
            //:)
        }
    }

    public void manageAccounts(MouseEvent mouseEvent) {
        String title = "";
        Account.AccountType accountType = Account.AccountType.ADMIN;
        switch (((Pane)mouseEvent.getSource()).getId()) {
            case "manageCustomers" :
                accountType = Account.AccountType.CUSTOMER;
                title = "Customers";
                break;
            case "manageVendors" :
                accountType = Account.AccountType.VENDOR;
                title = "Vendors";
                break;
            case "manageAdmins" :
                accountType = Account.AccountType.ADMIN;
                title = "Admins";
                break;
        }
        title += " View";
        openManageAccountsStage(title, accountType);
    }

    private void openManageAccountsStage(String title, Account.AccountType accountType) {
        if (canOpenSubStage(title, parentProcessor)) {
            try {
                TableViewProcessor.TableViewType tableViewType;
                switch (accountType) {
                    case ADMIN:
                        tableViewType = TableViewProcessor.TableViewType.ADMINS;
                        break;
                    case VENDOR:
                        tableViewType = TableViewProcessor.TableViewType.VENDORS;
                        break;
                    default:
                        tableViewType = TableViewProcessor.TableViewType.CUSTOMERS;
                }
                FXMLLoader loader = new FXMLLoader(getClass().getResource("TableViewMenu.fxml"));
                Parent root = loader.load();
                TableViewProcessor<Account> tableViewProcessor = loader.getController();
                tableViewProcessor.setParentProcessor(parentProcessor);
                tableViewProcessor.initProcessor(tableViewType);
                Stage newStage = new Stage();
                newStage.setScene(new Scene(root));
                newStage.getIcons().add(new Image(getClass().getResourceAsStream("view accounts icon.png")));
                newStage.setResizable(false);
                newStage.setTitle(title);
                parentProcessor.addSubStage(newStage);
                newStage.show();
            } catch (IOException e) {
                //:)
            }
        }
    }

    public void manageCategories(MouseEvent mouseEvent) {
        if(canOpenSubStage("Manage Categories", parentProcessor)) {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("CategoriesMenu.fxml"));
            Parent root = null;
            try {
                root = loader.load();
                Stage newStage = new Stage();
                newStage.setScene(new Scene(root));
                //newStage.getIcons().add(new Image(getClass().getResourceAsStream("categories icon.png")));
                newStage.setResizable(false);
                newStage.setTitle("Manage Categories");
                newStage.getIcons().add(new Image(getClass().getResourceAsStream("Manage Categories Menu.png")));
                parentProcessor.addSubStage(newStage);

                //Sepehr's Section
                    CategoryProcessor categoryProcessor = loader.getController();
                    categoryProcessor.setParentProcessor(parentProcessor);
                    categoryProcessor.setMyStage(newStage);
                //Sepehr's Section

                //CategoryProcessor.setCategoriesStage(newStage);
                newStage.show();
            } catch (IOException e) {
                //:)
            }
        }

    }

    public void manageDiscounts(MouseEvent mouseEvent) {
        if (canOpenSubStage("Manage Discounts", parentProcessor)) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("TableViewMenu.fxml"));
                Parent root = loader.load();
                TableViewProcessor<Discount> tableViewProcessor = loader.getController();
                tableViewProcessor.setParentProcessor(parentProcessor);
                tableViewProcessor.initProcessor(TableViewProcessor.TableViewType.DISCOUNTS);
                Stage newStage = new Stage();
                newStage.setScene(new Scene(root));
                newStage.setResizable(false);
                newStage.setTitle("Manage Discounts");
                parentProcessor.addSubStage(newStage);
                tableViewProcessor.setMyStage(newStage);
                newStage.getIcons().add(new Image(Main.class.getResourceAsStream("Discounts Icon.png")));
                newStage.show();
            } catch (IOException e) {
                //:)
            }
        }
    }

    public void manageComments(MouseEvent mouseEvent) {
        if (canOpenSubStage("Manage Comment Requests", parentProcessor)) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("TableViewMenu.fxml"));
                Parent root = loader.load();
                TableViewProcessor<Comment> tableViewProcessor = loader.getController();
                tableViewProcessor.setParentProcessor(parentProcessor);
                tableViewProcessor.initProcessor(TableViewProcessor.TableViewType.ADMIN_COMMENTS);
                Stage newStage = new Stage();
                newStage.setScene(new Scene(root));
                //newStage.getIcons().add(new Image(getClass().getResourceAsStream("view accounts icon.png")));
                newStage.setResizable(false);
                newStage.setTitle("Manage Comment Requests");
                parentProcessor.addSubStage(newStage);
                tableViewProcessor.setMyStage(newStage);
                tableViewProcessor.setTableViewPane((Pane)tableViewProcessor.mainBorderPane.getCenter());
                newStage.getIcons().add(new Image(Main.class.getResourceAsStream("Comments Icon.png")));
                newStage.show();
            } catch (IOException e) {
                //:)
            }
        }
    }

    public void manageProductRequests(MouseEvent mouseEvent) {
        //AdminManageProductRequests
        if(canOpenSubStage("Manage Product Requests", parentProcessor)) {
            Stage stage = new Stage();
            stage.setTitle("Manage Product Requests");
            FXMLLoader loader = new FXMLLoader(getClass().getResource("AdminManageProductRequests.fxml"));
            Parent parent = null;
            try {
                parent = loader.load();
                ProductsProcessor processor = loader.getController();
                processor.initProcessor(ProductsProcessor.ProductsMenuType.ADMIN_PRODUCT_REQUESTS);
                processor.setParentProcessor(parentProcessor);
            } catch (IOException e) {
                //:)
            }
            stage.setScene(new Scene(parent));
            stage.setResizable(false);
            parentProcessor.addSubStage(stage);
            stage.getIcons().add(new Image(Main.class.getResourceAsStream("Product Requests Icon.png")));
            stage.show();
        }

    }

    public void manageOffRequests(MouseEvent mouseEvent) {
        if (canOpenSubStage("Manage Off Requests", parentProcessor)) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("TableViewMenu.fxml"));
                Parent root = loader.load();
                TableViewProcessor<Off> tableViewProcessor = loader.getController();
                tableViewProcessor.setParentProcessor(parentProcessor);
                tableViewProcessor.initProcessor(TableViewProcessor.TableViewType.ADMIN_OFFS);
                Stage newStage = new Stage();
                newStage.setScene(new Scene(root));
                newStage.setResizable(false);
                newStage.setTitle("Manage Off Requests");
                parentProcessor.addSubStage(newStage);
                tableViewProcessor.setMyStage(newStage);
                newStage.getIcons().add(new Image(Main.class.getResourceAsStream("Offs Icon.png")));
                newStage.show();
            } catch (IOException e) {
                //:)
            }
        }
    }

    public void manageCashStats(MouseEvent mouseEvent) {
        if (canOpenSubStage("Manage Cash Stats", parentProcessor)) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("AdminCashStats.fxml"));
                Parent root = loader.load();
                Stage newStage = new Stage();
                newStage.setScene(new Scene(root));
                newStage.setResizable(false);
                newStage.setTitle("Manage Cash Stats");
                parentProcessor.addSubStage(newStage);
                newStage.getIcons().add(new Image(Main.class.getResourceAsStream("Statistics Icon.png")));
                newStage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void saveMinimumWalletMoney(ActionEvent actionEvent) {
        if(!minimumWalletField.getText().isEmpty()) {
            Command<Double> command = new Command<>("set minimum wallet money", Command.HandleType.ACCOUNT, Double.parseDouble(minimumWalletField.getText()));
            client.postAndGet(command, Response.class, (Class<Object>)Object.class).getMessage().getAlert().show();
        } else {
            minimumWalletField.setStyle(errorTextFieldStyle);
        }
    }

    public void saveMarketWage(ActionEvent actionEvent) {
        if(!wageField.getText().isEmpty()) {
            Command<Double> command = new Command<>("set market wage", Command.HandleType.ACCOUNT, Double.parseDouble(wageField.getText()));
            client.postAndGet(command, Response.class, (Class<Object>)Object.class).getMessage().getAlert().show();
        } else {
            wageField.setStyle(errorTextFieldStyle);
        }
    }

    public void fieldOnKey(KeyEvent keyEvent) {
        ((JFXTextField)keyEvent.getSource()).setStyle(null);
        if(keyEvent.getCode() == KeyCode.ENTER) {
            if(((JFXTextField)keyEvent.getSource()).getId().equals("minimumWalletField"))
                saveMinimumWalletMoney(null);
            else
                saveMarketWage(null);
        }
    }

    public void manageLogs(MouseEvent mouseEvent) {
        if (canOpenSubStage("Manage Logs", parentProcessor)) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("TableViewMenu.fxml"));
                Parent root = loader.load();
                TableViewProcessor<Off> tableViewProcessor = loader.getController();
                tableViewProcessor.setParentProcessor(parentProcessor);
                tableViewProcessor.initProcessor(TableViewProcessor.TableViewType.LOGS);
                Stage newStage = new Stage();
                newStage.setScene(new Scene(root));
                newStage.setResizable(false);
                newStage.setTitle("Manage Logs");
                parentProcessor.addSubStage(newStage);
                tableViewProcessor.setMyStage(newStage);
                newStage.getIcons().add(new Image(Main.class.getResourceAsStream("Admin Logs.png")));
                newStage.show();
            } catch (IOException e) {
                //:)
            }
        }
    }

    public void manageSupporters(MouseEvent mouseEvent) {
        if (canOpenSubStage("Manage Supporters", parentProcessor)) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("TableViewMenu.fxml"));
                Parent root = loader.load();
                TableViewProcessor<Account> tableViewProcessor = loader.getController();
                tableViewProcessor.setParentProcessor(parentProcessor);
                tableViewProcessor.initProcessor(TableViewProcessor.TableViewType.ADMIN_SUPPORTERS);
                Stage newStage = new Stage();
                newStage.setScene(new Scene(root));
                newStage.getIcons().add(new Image(getClass().getResourceAsStream("admin supporters icon.png")));
                newStage.setResizable(false);
                newStage.setTitle("Manage Supporters");
                parentProcessor.addSubStage(newStage);
                newStage.show();
            } catch (IOException e) {
                //:)
            }
        }
    }
}
