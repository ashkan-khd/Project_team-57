package view;

import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXToggleButton;
import controller.account.AdminControl;
import controller.account.CustomerControl;
import controller.account.VendorControl;
import controller.product.ProductControl;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.stage.Stage;
import model.existence.Category;
import model.existence.Product;
import notification.Notification;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class ProductsProcessor extends Processor{
    private static final int PRODUCT_SCROLL_PANE_WIDTH = 1050;
    private static final int PRODUCT_FIELD_HEIGHT = 335;
    private static final int PRODUCT_PAGES_BAR_HEIGHT = 50;
    //UserProducts
    public BorderPane vendorProductsMainBorderPane;
    public ScrollPane userProductsScrollPane;
    public Pane userProductsOptionPane;
    public JFXButton previousProductButton;
    public CheckBox approveProductCheckBox;
    public ImageView previousPageImageView;
    private HashMap<String, CheckBox> productsApprovalMap;



    public static enum ProductsMenuType {
        MAIN_PRODUCTS, VENDOR_PRODUCTS, CUSTOMER_CART, ADMIN_PRODUCT_REQUESTS;
    }
    private ProductsMenuType menuType;

    public ScrollPane productsScrollPane;
    //ProductPane
    public ImageView productImage;
    public Label productNameLabel;
    public Label viewLabel;
    public ImageView availableImage;
    public Label availableLabel;
    public Label oldPriceLabel;
    public Label newPriceLabel;
    public Label pageNumberLabel;
    public ImageView nextPageButton;
    public ImageView previousPageButton;
    //Product Pane
    public JFXButton viewSortButton;
    public JFXButton timeSortButton;
    public JFXButton nameSortButton;
    public JFXButton scoreSortButton;
    public JFXToggleButton descendingSortButton;
    private JFXButton selectedSort;

    //Categories Pane
    public TreeTableView<Category> categoriesTableTreeView;

    public JFXTextField searchTextField;

    //Filter Pane
    public Pane mainFilterPane;
    public VBox filteredCategoriesVBox;
    public Label filterNameLabel;
    private Category filterCategory;
    private String filterName;

    //Filter Price Pane
    public JFXTextField toPriceTextField, fromPriceTextField;

    private ArrayList<Product> allProducts;
    private int pageSize = 12;
    private int pageNumber = 0;
    private int productFieldsNumber;
    private int pageLim;
    private ProductsProcessor parentProcessor;
    private ProductControl productControl = ProductControl.getController();


    public void initProcessor (ProductsMenuType menuType) {
        this.menuType = menuType;
        switch (menuType) {
            case MAIN_PRODUCTS:
                initMainProductsMenu();
                break;
            case VENDOR_PRODUCTS:
                initVendorProductsMenu();
                break;
            case CUSTOMER_CART:
                initProductsPage();
                break;
            case ADMIN_PRODUCT_REQUESTS:
                initAdminProductRequestsMenu();
        }
    }

    private void initAdminProductRequestsMenu() {
        productsApprovalMap = new HashMap<>();
        initProductsPage();
    }

    private void initVendorProductsMenu() {
        Stop[] stops = new Stop[] {
                new Stop(0, Color.valueOf("#360033")),
                new Stop(1, Color.valueOf("#127183"))
        };
        LinearGradient linearGradient = new LinearGradient(0, 0, 0, 1, true, CycleMethod.NO_CYCLE, stops);
        BackgroundFill backgroundFill = new BackgroundFill(linearGradient, CornerRadii.EMPTY, Insets.EMPTY);
        userProductsOptionPane.setBackground(new Background(backgroundFill));
        initProductsPage();
    }

    private void initMainProductsMenu() {
        productControl.initSort(); productControl.initFilter();
        selectedSort = viewSortButton;
        selectSort();
        initCategoriesTableTreeView();
        initPriceFiltersTextFields();
    }

    private void initCategoriesTableTreeView() {
        categoriesTableTreeView.setRowFactory( tv -> {
            TreeTableRow<Category> row = new TreeTableRow<>();
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 /*&& (!row.isEmpty())*/ ) {
                    Category category = row.getItem();
                    addCategoryToFilters(category);
                }
            });
            row.getStylesheets().add(getClass().getResource("FilterCategoryRowTable.css").toExternalForm());
            row.getStyleClass().add("Row");
            return row ;
        });

        categoriesTableTreeView.setRoot(ProductControl.getController().getCategoryTableRoot());
        categoriesTableTreeView.setShowRoot(false);
        //categoriesTableTreeView.getSelectionModel().selectFirst();
    }

    private void initPriceFiltersTextFields() {
        setPriceFields(toPriceTextField);
        setPriceFields(fromPriceTextField);
    }

    private void setPriceFields(JFXTextField priceTextField) {
        priceTextField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue,
                                String newValue) {
                //Todo Checking

                if(newValue.equals(".")) {
                    priceTextField.setText("0.");
                } else if (!newValue.matches("\\d+(.(\\d)+)?")) {
                    if(priceTextField.getText().contains(".")) {
                        priceTextField.setText(removeDots(priceTextField.getText()));
                    } else {
                        priceTextField.setText(newValue.replaceAll("[^\\d\\.]", ""));
                    }
                }

                setPriceFilter(/*priceTextField*/);
            }
        });
    }

    private void setPriceFilter(/*JFXTextField priceTextField*/) {
        double minPrice = fromPriceTextField.getText().isEmpty() ? 0 : Double.parseDouble(fromPriceTextField.getText());
        double maxPrice = toPriceTextField.getText().isEmpty() ? Double.MAX_VALUE : Double.parseDouble(toPriceTextField.getText());
        controller.Control.getController().setPriceFilters(minPrice, maxPrice);
        initProductsPage();
    }

    private void addCategoryToFilters(Category category) {
        if(!controller.Control.getController().isThereFilteringCategoryWithName(category.getName())) {
            try {
                loadFilterPane(category, null);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadFilterPane(Category filterCategory, String filterName) throws IOException {
            FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("FilterCategoryPane.fxml"));
            Node node = fxmlLoader.load();
            ProductsProcessor productsProcessor = fxmlLoader.getController();
            productsProcessor.parentProcessor = this;

            if (filterCategory == null) {
                productsProcessor.filterName = filterName;
                productsProcessor.filterNameLabel.setText(" " + filterName + " ");
                controller.Control.getController().addToFilterNameList(filterName);
                searchTextField.setText(null);
            } else if (filterName == null) {
                productsProcessor.filterCategory = filterCategory;
                productsProcessor.filterNameLabel.setText(" " + filterCategory.getName() + " ");
                controller.Control.getController().addToFilterCategoryList(filterCategory.getName());
            }

            initProductsPage();
            filteredCategoriesVBox.getChildren().add(node);
    }

    public void deleteFilterCategoryMouseClicked(MouseEvent mouseEvent) {
        if(filterCategory == null)
            controller.Control.getController().removeFromFilterNameList(filterName);
        else if(filterName == null)
            controller.Control.getController().removeFromFilterCategoryList(filterCategory.getName());

        parentProcessor.initProductsPage();
        parentProcessor.filteredCategoriesVBox.getChildren().remove(mainFilterPane);
    }

    //initProductsScrollPane
    public void initProductsPage() {
        switch (menuType) {
            case MAIN_PRODUCTS:
                allProducts = productControl.getAllShowingProducts();
                initCertainProductsPage(productsScrollPane);
                break;
            case VENDOR_PRODUCTS:
                allProducts = VendorControl.getController().getAllProducts();
                initCertainProductsPage(userProductsScrollPane);
                break;
            case CUSTOMER_CART:
                allProducts = CustomerControl.getController().getAllCartProducts();
                initCertainProductsPage(userProductsScrollPane);
                break;
            case ADMIN_PRODUCT_REQUESTS:
                allProducts = AdminControl.getController().getAllNotApprovedProducts();
                initCertainProductsPage(userProductsScrollPane);
                break;
        }
    }

    private void initCertainProductsPage(ScrollPane scrollPane) {
        try {
            BorderPane borderPane = new BorderPane();
            pageLim = (allProducts.size() -(pageNumber * pageSize) < 12 ? (allProducts.size() -(pageNumber * pageSize)) : 12);
            productFieldsNumber = (pageLim < 9 ? 8 : 12);
            double borderPaneHeight = ((productFieldsNumber/4) * PRODUCT_FIELD_HEIGHT) + PRODUCT_PAGES_BAR_HEIGHT;
            borderPane.setPrefSize(PRODUCT_SCROLL_PANE_WIDTH - 50, borderPaneHeight);
            scrollPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
            Pane root = setPageNumberBar();
            borderPane.setBottom(root);
            GridPane gridPane = new GridPane();
            gridPane.getChildren().addAll(getProductsPanes());
            gridPane.setMinWidth(Control.USE_COMPUTED_SIZE); gridPane.setMaxWidth(Control.USE_COMPUTED_SIZE); gridPane.setPrefWidth(Control.USE_COMPUTED_SIZE);
            gridPane.setMinHeight(Control.USE_COMPUTED_SIZE); gridPane.setMaxHeight(Control.USE_COMPUTED_SIZE); gridPane.setPrefHeight(Control.USE_COMPUTED_SIZE);
            borderPane.setCenter(gridPane);
            scrollPane.setContent(borderPane);
            scrollPane.setVvalue(0);
        } catch (IOException e) { e.printStackTrace(); }
    }

    private Pane setPageNumberBar() throws IOException {
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("ProductsPagesBar.fxml"));
        Pane root = loader.load();
        ProductsProcessor pagesBarProcessor = loader.getController();
        pagesBarProcessor.setParentProcessor(this);
        if((int)Math.ceil(allProducts.size()/12.0) != 0)
            pagesBarProcessor.pageNumberLabel.setText("Page " + (pageNumber + 1) + " of " + (int)Math.ceil(allProducts.size()/12.0));
        else {
            pagesBarProcessor.pageNumberLabel.setText("Page " + (pageNumber + 1) + " of " + 1);
            pagesBarProcessor.nextPageButton.setDisable(true);
            pagesBarProcessor.nextPageButton.setOpacity(0.3);
            pagesBarProcessor.previousPageButton.setDisable(true);
            pagesBarProcessor.previousPageButton.setOpacity(0.3);
        }
        if(pageNumber == 0) {
            pagesBarProcessor.previousPageButton.setDisable(true);
            pagesBarProcessor.previousPageButton.setOpacity(0.3);
        }
        if(pageNumber == Math.ceil(allProducts.size()/12.0) - 1) {
            pagesBarProcessor.nextPageButton.setDisable(true);
            pagesBarProcessor.nextPageButton.setOpacity(0.3);
        }
        return root;
    }

    private ArrayList<HBox> getProductsPanes() throws IOException {
        //System.out.println("Start");
        ArrayList<HBox> hBoxes = new ArrayList<>();
        for(int y = 0; y < productFieldsNumber/4; ++y) {
            for(int x = 0; x < 4; ++ x) {
                HBox hBox = new HBox();
                hBox.setAlignment(Pos.CENTER);
                GridPane.setConstraints(hBox, x, y);
                hBox.setMinWidth(257); hBox.setMaxWidth(257); hBox.setPrefWidth(257);
                hBox.setMinHeight(PRODUCT_FIELD_HEIGHT); hBox.setMaxHeight(PRODUCT_FIELD_HEIGHT); hBox.setPrefHeight(PRODUCT_FIELD_HEIGHT);
                hBoxes.add(hBox);
            }
        }
        if(menuType != ProductsMenuType.ADMIN_PRODUCT_REQUESTS) {
            for(int i = 0; i < pageLim; ++i) {
                hBoxes.get(i).getChildren().add(getCommonProductPane(i));
            }
        } else {
            for(int i = 0; i < pageLim; ++i) {
                hBoxes.get(i).getChildren().add(getAdminProductRequestsProductPane(i));
            }
        }

        return hBoxes;
    }

    private Pane getAdminProductRequestsProductPane(int productNumberInPage) throws IOException {
        Product product = allProducts.get(pageNumber * pageSize + productNumberInPage);
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("ProductPaneAdmin.fxml"));
        Pane productPane = loader.load();
        ProductsProcessor paneProcessor = loader.getController();
        paneProcessor.setParentProcessor(this);
        if(product.getStatus() == 3) {
            paneProcessor.previousProductButton.setOnAction(event -> {
                FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource("ProductMenu.fxml"));
                try {
                    Parent root = fxmlLoader.load();
                    ProductProcessor processor = fxmlLoader.getController();
                    processor.setParentProcessor(parentProcessor);
                    processor.initProcessor(productControl.getProductById(product.getID()), ProductProcessor.ProductMenuType.ADMIN);
                    Stage stage = new Stage();
                    stage.setScene(new Scene(root));
                    stage.setTitle(product.getName());
                    processor.setMyStage(stage);
                    stage.show();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        } else {
            productPane.getChildren().remove(paneProcessor.previousProductButton);
            productPane.getChildren().remove(paneProcessor.previousPageImageView);
        }
        paneProcessor.approveProductCheckBox.setOnMouseClicked(event -> {
            if(productsApprovalMap.containsKey(product.getID())) {
                if(!paneProcessor.approveProductCheckBox.isSelected())
                    productsApprovalMap.remove(product.getID());
                else
                    productsApprovalMap.replace(product.getID(), paneProcessor.approveProductCheckBox);
            } else
                productsApprovalMap.put(product.getID(), paneProcessor.approveProductCheckBox);
        });
        paneProcessor.availableImage.setImage(new Image("Images\\Icons\\ProductsMenu\\unavailable.png"));
        paneProcessor.availableLabel.setText(product.getTheStatus());
        if(product.getStatus() != 3) {
            paneProcessor.productImage.setImage(productControl.getProductImageByID(product.getID(), 1));
        } else {
            paneProcessor.productImage.setImage(productControl.getEditingProductImage(product.getID(), 1));
        }
        paneProcessor.productNameLabel.setText(product.getName());
        if (productControl.isThereProductInOff(product.getID())) {
            //TODO
            System.out.println("Product In Off");
        } else {
            productPane.getChildren().remove(paneProcessor.newPriceLabel);
            paneProcessor.oldPriceLabel.setText(product.getPrice() + "$");
        }
        setProductPaneOnMouseClick(productPane, product, this);
        return productPane;
    }

    private Pane getCommonProductPane(int productNumberInPage) throws IOException {
        Product product = allProducts.get(pageNumber * pageSize + productNumberInPage);
        FXMLLoader loader = new FXMLLoader(Main.class.getResource("ProductPane.fxml"));
        Pane productPane = loader.load();
        ProductsProcessor productsProcessor = loader.getController();
        if(product.getStatus() != 3) {
            productsProcessor.productImage.setImage(productControl.getProductImageByID(product.getID(), 1));
        } else {
            productsProcessor.productImage.setImage(productControl.getEditingProductImage(product.getID(), 1));
        }
        productsProcessor.productNameLabel.setText(product.getName());
        if (productControl.isThereProductInOff(product.getID())) {
            //TODO
            System.out.println("Product In Off");
        } else {
            productPane.getChildren().remove(productsProcessor.newPriceLabel);
            productsProcessor.oldPriceLabel.setText(product.getPrice() + "$");
        }
        productsProcessor.viewLabel.setText("" + product.getSeen());
        if (!(product.getStatus() == 1 && (product.getCount() > 0 || product.getAmount() > 0))) {
            productsProcessor.availableImage.setImage(new Image("Images\\Icons\\ProductsMenu\\unavailable.png"));
            if(product.getStatus() != 1)
                productsProcessor.availableLabel.setText(product.getTheStatus());
            else
                productsProcessor.availableLabel.setText("Out Of Stock");
        }
        setProductPaneOnMouseClick(productPane, product, this);
        return productPane;
    }

    private void setProductPaneOnMouseClick(Pane productPane, Product product, ProductsProcessor parentProcessor) {
        productPane.setOnMouseClicked(event -> {
            ProductProcessor.ProductMenuType productMenuType = null;
            switch (menuType) {
                case VENDOR_PRODUCTS:
                    productMenuType = ProductProcessor.ProductMenuType.VENDOR_EDIT;
                    break;
                case MAIN_PRODUCTS:
                    if(controller.Control.getType() != null && controller.Control.getType().equals("Admin")){
                        productMenuType = ProductProcessor.ProductMenuType.ADMIN;
                    } else if(controller.Control.getType() != null && controller.Control.getType().equals("Customer")) {
                        productMenuType = ProductProcessor.ProductMenuType.PRODUCTS_CUSTOMER;
                    } else if(controller.Control.getType() != null && controller.Control.getType().equals("Vendor"))
                        productMenuType = ProductProcessor.ProductMenuType.PRODUCTS_VENDOR;
                    else
                        productMenuType = ProductProcessor.ProductMenuType.PRODUCTS;
                    break;
                case CUSTOMER_CART:
                    productMenuType = ProductProcessor.ProductMenuType.CART;
                    break;
                case ADMIN_PRODUCT_REQUESTS:
                    productMenuType = ProductProcessor.ProductMenuType.ADMIN;
                //TODO(MORE)
            }
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("ProductMenu.fxml"));
            try {
                if(menuType == ProductsMenuType.MAIN_PRODUCTS) {
                    productControl.addSeenToProduct(product.getID());
                }
                Parent root = loader.load();
                ProductProcessor processor = loader.getController();
                processor.setParentProcessor(parentProcessor);
                processor.initProcessor(product, productMenuType);
                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.setTitle(product.getName());
                processor.setMyStage(stage);
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

    public void setParentProcessor(ProductsProcessor parentProcessor) {
        this.parentProcessor = parentProcessor;
    }

    public void changePage(MouseEvent mouseEvent) {
        ImageView button = (ImageView) mouseEvent.getSource();
        if(button == nextPageButton) {
            parentProcessor.pageNumber += 1;
        } else {
            parentProcessor.pageNumber -= 1;
        }
        parentProcessor.initProductsPage();
    }

    public void changePageOnMouse(MouseEvent mouseEvent) {
        String style = "-fx-opacity: 1; -fx-cursor: hand;";
        ((ImageView)mouseEvent.getSource()).setStyle(style);
    }

    public void changePageOutMouse(MouseEvent mouseEvent) {
        String style = "-fx-opacity: 0.7;";
        ((ImageView)mouseEvent.getSource()).setStyle(style);
    }

    public void sortButtonOnMouse(MouseEvent mouseEvent) {
        if ((JFXButton) mouseEvent.getSource() != selectedSort) {
            String style ="-fx-background-radius: 10 10 10 10; -fx-background-color: #90a4ae; -fx-cursor: hand;";
            ((JFXButton)mouseEvent.getSource()).setStyle(style);
        }
    }

    public void sortButtonOutMouse(MouseEvent mouseEvent) {
        if ((JFXButton) mouseEvent.getSource() != selectedSort) {
            String style ="-fx-background-radius: 10 10 10 10;";
            ((JFXButton)mouseEvent.getSource()).setStyle(style);
        }
    }

    public void changeSort(ActionEvent actionEvent) {
        String style ="-fx-background-radius: 10 10 10 10;";
        selectedSort.setStyle(style);
        selectedSort = (JFXButton) actionEvent.getSource();
        selectSort();
    }

    private void selectSort() {
        String style ="-fx-background-radius: 10 10 10 10; -fx-background-color: #607d8b;";
        selectedSort.setStyle(style);
        setSort();
    }

    public void setSort() {
        productControl.setSort(selectedSort.getText(), !descendingSortButton.isSelected());
        initProductsPage();
    }

    public void openAccountMenu(ActionEvent actionEvent) {
        new WelcomeProcessor().openAccountMenu();
    }

    public void filterByNameKeyTyped(KeyEvent keyEvent) {
        //System.out.println("here : " + keyEvent.getCharacter() + "\nhere : ");
        if((int) keyEvent.getCharacter().charAt(0) == 13) {
            //System.out.println((int) keyEvent.getCharacter().charAt(0));
            filterByNameMouseClicked();
        }
    }

    public void filterByNameMouseClicked() {
        String searchFieldText = searchTextField.getText();

        if(searchFieldText == null || searchFieldText.isEmpty()) {
            searchTextField.requestFocus();
        } else if(!controller.Control.getController().isThereFilteringNameWithName(searchFieldText)) {
            try {
                loadFilterPane(null, searchTextField.getText());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


    public void addNewProduct(MouseEvent mouseEvent) {
        if(canOpenSubStage("Add New Product", this)) {
            FXMLLoader loader = new FXMLLoader(Main.class.getResource("ProductMenu.fxml"));
            try {
                Parent root = loader.load();
                ProductProcessor processor = loader.getController();
                processor.initProcessor(new Product(), ProductProcessor.ProductMenuType.VENDOR_ADD);
                processor.setParentProcessor(this);
                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.setTitle("Add New Product");
                addSubStage(stage);
                processor.setMyStage(stage);
                stage.show();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void addNewOff(MouseEvent mouseEvent) {
        //TODO
    }

    public void purchaseProducts(MouseEvent mouseEvent) {
        //TODO
    }

    public void saveChangesForAdminProductRequests(MouseEvent mouseEvent) {
        ArrayList<Notification> results = new ArrayList<>();
        AdminControl adminControl = AdminControl.getController();
        for (String productID : productsApprovalMap.keySet()) {
            //System.out.println(productID);
            //System.out.println(productControl.getProductById(productID));
            if(productControl.getProductById(productID).getStatus() == 3) {
                System.out.println("Hello");
                results.add(adminControl.modifyEditingProductApprove(productID, productsApprovalMap.get(productID).isSelected()));
            } else {
                System.out.println("Hi");
                results.add(adminControl.modifyProductApprove(productID, productsApprovalMap.get(productID).isSelected()));
            }
        }
        showManageProductRequestsResult(results);
        //System.out.println("Hello");
        initProductsPage();
    }

    private void showManageProductRequestsResult(ArrayList<Notification> results) {
        int errorCount = 0;
        int addProductAcceptCount = 0;
        int editProductAcceptCount = 0;
        int addProductDeclineCount = 0;
        int editProductDeclineCount = 0;
        for (Notification result : results) {
            switch (result) {
                case UNKNOWN_ERROR:
                    errorCount++;
                    break;
                case ACCEPT_EDITING_PRODUCT:
                    editProductAcceptCount++;
                    break;
                case ACCEPT_ADDING_PRODUCT:
                    addProductAcceptCount++;
                    break;
                case DECLINE_EDITING_PRODUCT:
                    editProductDeclineCount++;
                    break;
                case REMOVE_PRODUCT_SUCCESSFULLY:
                    addProductDeclineCount++;
                    break;
            }
        }
        new Alert(Alert.AlertType.INFORMATION, "Process Executed. Results:\n" +
                "Errors: " + errorCount + "\n" +
                "Accepted New Products: " + addProductAcceptCount +"\n" +
                "Accepted Edited Products: " + editProductAcceptCount +"\n" +
                "Declined New Products: " + addProductDeclineCount +"\n" +
                "Declined Edited Products: " + editProductDeclineCount +"\n" +
                "").show();
    }

    public void backToMainMenu(ActionEvent actionEvent) {
        Parent root = null;
        try {
            root = FXMLLoader.load(Main.class.getResource("WelcomeMenu.fxml"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        Main.getStage().getIcons().remove(0);
        Main.getStage().getIcons().add(new Image(Main.class.getResourceAsStream("Main Icon.png")));
        Main.setScene("Boos Market", root);
    }
}
