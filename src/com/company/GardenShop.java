package com.company;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.*;
import java.text.NumberFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GardenShop {
    private Connection conn;
    private Map<String,Product> products = new HashMap();
    private Map<Integer,CartProduct> cartProducts = new HashMap();
    public static final String WHITESPACE = " ";
    public static final String LINEBREAK = System.getProperty("line.separator");

    public void welcome() throws IOException, ParseException, SQLException {
        System.out.println("#############################################");
        System.out.println("Laipni lūgti Dārza Preču Veikala!");
        System.out.println("#############################################");

        System.out.println("Izveidojam veikala preču datu bāzi...");
        this.connectToDatabase();
        this.initDatabase();
        System.out.println("Atjaunojam produktu sarakstu no preču noliktavas API...");
        String url = "https://dev.matchingneeds.eu/~sabine/api.php";
        this.fetchProducts(url);
    }
    public void  connectToDatabase() throws SQLException {
        conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/gardenshop", "root", "sabSTR0703");
        if(conn != null) System.out.println("Savienojamies ar Dārza Preču Veikala datubāzi!");
    }
    public void initDatabase() throws SQLException {
        // 'Category' table
        Statement stmt;

        String sqlCategory = "CREATE TABLE IF NOT EXISTS `category` (`id` INT(11) NOT NULL AUTO_INCREMENT, `parentid` INT(11) NULL DEFAULT NULL, `name` VARCHAR(255) NOT NULL, `description` LONGTEXT NULL, PRIMARY KEY (`id`));";
        stmt = conn.createStatement();
        stmt.execute(sqlCategory);

        // 'Product' table
        String sqlProduct = "CREATE TABLE IF NOT EXISTS `product` (`id` INT(11) NOT NULL AUTO_INCREMENT, `uid` VARCHAR(50) not null, `categoryid` INT NULL DEFAULT NULL, " +
                "`subcategoryid` INT NULL DEFAULT NULL, `name` VARCHAR(255) NOT NULL, `latin_name` varchar(255), `description` LONGTEXT NULL, `price` decimal(10,2), " +
                "`instock` integer(11) NOT NULL DEFAULT '0', PRIMARY KEY (`id`), UNIQUE INDEX `unique_uid` (`uid`), " +
                "INDEX `FK_product_category` (`categoryid`), INDEX `FK_product_category_2` (`subcategoryid`), " +
                "CONSTRAINT `FK_product_category` FOREIGN KEY (`categoryid`) REFERENCES `category` (`id`) ON DELETE SET NULL," +
                "CONSTRAINT `FK_product_category_2` FOREIGN KEY (`subcategoryid`) REFERENCES `category` (`id`) ON DELETE SET NULL);";
        stmt = conn.createStatement();
        stmt.execute(sqlProduct);
    }
    public void actions() throws SQLException {
        boolean askForAction = false;

        do{
            String action = actionsPrint();
            switch (action) {
                case "0":
                    this.actionExit();
                    askForAction = false;
                    break;
                case "1":
                    System.out.println("\nVEIKALA PREČU SARAKSTS (AR APRAKSTU)");
                    actionList(false);
                    askForAction = false;
                    break;
                case "2":
                    System.out.println("\nVEIKALA PREČU SARAKSTS (ĪSS PĀRSKATS)");
                    actionList(true);
                    askForAction = false;
                    break;
                case "3":
                    System.out.println("\nIELIKT PRECI IEPIRKUMU GROZĀ");
                    actionAddToCart();
                    askForAction = false;
                    break;
                case "4":
                    System.out.println("\nJŪSU PIRKUMU GROZS");
                    actionShowCart();
                    askForAction = false;
                    break;
                case "5":
                    System.out.println("\nPIRKUMA APMAKSĀŠANA");
                    actionCheckout();
                    askForAction = false;
                    break;
                default:
                    System.out.println(action);
                    askForAction = true;
                    break;
            }
        }
        while( askForAction );
    }
    public String actionsPrint()
    {
        System.out.println("------------------------------------------------------");
        System.out.println("Lūdzu izvēlieties darbību (rakstiet 0, 1, 2, 3, 4, 5):");
        System.out.println("0. Iziet no veikala");
        System.out.println("1. Preču saraksts (ar aprakstu)");
        System.out.println("2. Preču saraksts (īss pārskats)");
        System.out.println("3. Ielikt preci pirkumu grozā");
        System.out.println("4. Apskatīt pirkumu grozu");
        System.out.println("5. Apmaksāt pirkumu");
        System.out.println("------------------------------------------------------");

        Scanner scan = new Scanner(System.in);
        String action;
        action = scan.nextLine();

        return action;
    }
    public void actionList( boolean shortDescription ) throws SQLException {
        if (countProducts() > 0) {
            for (int i = 0; i < countProducts(); i++) {

                int productNumber = i + 1;
                String name = this.products.get(Integer.toString(i)).getName();
                String latinName = this.products.get(Integer.toString(i)).getLatinName();
                double price = this.products.get(Integer.toString(i)).getPrice();
                String description = this.products.get(Integer.toString(i)).getDescription();
                int inStock = this.products.get(Integer.toString(i)).getInStock();
                int productCategoryId = this.products.get(Integer.toString(i)).getCategoryId();
                int productSubcategoryId = this.products.get(Integer.toString(i)).getSubcategoryId();
                String categoryDescription = "";
                String subcategoryDescription = "";

                Category category = findByCategoryId(productCategoryId);
                Category subcategory = findByCategoryId(productSubcategoryId);

                System.out.println("\n--------------------------------------------------------------------------------------------------");
                System.out.println("Prece Nr." + productNumber + ":");
                System.out.println("--------------------------------------------------------------------------------------------------");
                System.out.println("Nosaukums: " + name);

                if(!shortDescription) {
                    System.out.println("Latīniskais nosaukums: " + latinName);
                }

                if(!category.getDescription().equals(null) && !category.getDescription().equals("") && !shortDescription)
                {
                    categoryDescription = " (" + splitString(category.getDescription(), 12)+ ")";
                }

                if(!subcategory.getDescription().equals(null) && !subcategory.getDescription().equals("") && !shortDescription)
                {
                    subcategoryDescription = " (" + splitString(subcategory.getDescription(), 12)+ ")";
                }

                System.out.println("Kategorija: " + category.getName() + categoryDescription);
                System.out.println("Apakškategorija: " + subcategory.getName() + subcategoryDescription);

                // availability
                Set<Integer> singularNumbers = new HashSet<Integer>(Arrays.asList(new Integer[]{1, 21, 31, 41, 51, 61, 71, 81, 91, 101}));
                String availability = "";
                if(inStock == 0) {
                    availability = "Prece pašlaik nav pieejama!";
                }
                else if(singularNumbers.contains(inStock))
                {
                    availability = inStock + " prece";
                }
                else
                {
                    availability = inStock + " preces";
                }

                System.out.println("Pieejamība: " + availability);
                System.out.println("Cena: " + NumberFormat.getCurrencyInstance(new Locale("lv", "LV")).format(price));

                if(!shortDescription) {
                    System.out.println("\nApraksts: " + splitString(description, 12));
                }

            }
        }
        backToList();
    }
    public void addProduct(Product product) throws SQLException {
        PreparedStatement stmt;
        String sql;
        ResultSet rs;

        sql = "SELECT id FROM product WHERE uid = (?);";
        stmt = conn.prepareStatement(sql);
        stmt.setString(1, product.getUid());
        rs = stmt.executeQuery();

        if(rs.next())
        {
            PreparedStatement stmt2;
            String sql2;
            ResultSet rs2;

            // existing product id
            int productId = rs.getInt("id");

            sql2 = "UPDATE product SET categoryid = ?, subcategoryid = ?, name = ?, latin_name = ?, description = ?, price = ?, instock = ? WHERE id = ? AND uid = ?;";
            stmt2 = conn.prepareStatement(sql2);

            stmt2.setInt(1, product.getCategoryId());
            stmt2.setInt(2, product.getSubcategoryId());
            stmt2.setString(3, product.getName());
            stmt2.setString(4, product.getLatinName());
            stmt2.setString(5, product.getDescription());
            stmt2.setDouble(6, product.getPrice());
            stmt2.setInt(7, product.getInStock());
            stmt2.setInt(8, productId);
            stmt2.setString(9, product.getUid());
            stmt2.executeUpdate();
        }
        else
        {
            sql = "INSERT INTO product( uid, categoryid, subcategoryid, name, latin_name, description, price, instock) VALUES(?,?,?,?,?,?,?,?)";
            stmt = conn.prepareStatement(sql);

            stmt.setString(1, product.getUid());
            stmt.setInt(2, product.getCategoryId());
            stmt.setInt(3, product.getSubcategoryId());
            stmt.setString(4, product.getName());
            stmt.setString(5, product.getLatinName());
            stmt.setString(6, product.getDescription());
            stmt.setDouble(7, product.getPrice());
            stmt.setInt(8, product.getInStock());
            stmt.executeUpdate();
        }
    }
    public int addCategory(Category category) throws SQLException {
        PreparedStatement stmt;
        String sql;
        ResultSet rs;
        int parentId = 0;

        sql = "SELECT id, parentid, name, description FROM category WHERE `name` = (?) ";
        stmt = conn.prepareStatement(sql);
        stmt.setString(1, category.getName());
        rs = stmt.executeQuery();

        if(!rs.next())
        {
            PreparedStatement stmt2;
            ResultSet rs2;

            sql = "INSERT INTO category(`parentid`, `name`, `description`) VALUES(?,?,?)";
            stmt2 = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);

            stmt2.setNull(1, java.sql.Types.NULL);

            if(category.getParentId() != 0)
            {
                stmt2.setInt(1, category.getParentId());
            }

            stmt2.setString(2, category.getName());
            stmt2.setString(3, category.getDescription());
            stmt2.executeUpdate();

            rs2 = stmt2.getGeneratedKeys();

            if (rs2.next()) {
                parentId = rs2.getInt(1);
            }
            rs2.close();
        }
        else
        {
            parentId = rs.getInt(1);
        }
        return parentId;
    }
    public void fetchProducts( String url ) throws IOException, ParseException, SQLException {
        HttpURLConnection c = null;

        URL u = new URL(url);
        c = (HttpURLConnection) u.openConnection();
        c.setRequestMethod("GET");
        c.setRequestProperty("Content-length", "0");
        c.setUseCaches(false);
        c.setAllowUserInteraction(false);
        c.setConnectTimeout(1000);
        c.setReadTimeout(1000);
        c.connect();
        int status = c.getResponseCode();

        switch (status) {
            case 200:
            case 201:
                BufferedReader br = new BufferedReader(new InputStreamReader(c.getInputStream(), "UTF-8"));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                br.close();

                JSONParser parser = new JSONParser();
                JSONArray jsonProducts = (JSONArray) parser.parse(sb.toString());

                System.out.println("Tika atrastas " + jsonProducts.size() + " preces.");

                for (int i = 0; i < jsonProducts.size(); i++) {
                    JSONObject jsonProduct = (JSONObject) jsonProducts.get(i);

                    String categoryName;
                    String categoryDescription;
                    String subcategoryName;
                    String subcategoryDescription;

                    categoryName = (jsonProduct.get("category") != null)?jsonProduct.get("category").toString():"";
                    categoryDescription = (jsonProduct.get("category_description") != null)?jsonProduct.get("category_description").toString():"";
                    subcategoryName = (jsonProduct.get("subcategory") != null)?jsonProduct.get("subcategory").toString():"";
                    subcategoryDescription = (jsonProduct.get("subcategory_description") != null)?jsonProduct.get("subcategory_description").toString():"";

                    Category category = new Category();
                    category.setName(categoryName);
                    category.setDescription(categoryDescription);

                    int catId = this.addCategory(category);

                    Product product = new Product();
                    product.setCategoryId(catId);

                    int catSubId = 0;

                    if( catId != 0 && !subcategoryName.equals(""))
                    {
                        Category subcategory = new Category();
                        subcategory.setParentId(catId);
                        subcategory.setName(subcategoryName);
                        subcategory.setDescription(subcategoryDescription);
                        subcategory.setIsSubcategory(true);

                        catSubId = this.addCategory(subcategory);
                        product.setSubcategoryId(catSubId);
                    }

                    product.setId(i + 1);
                    product.setUid(jsonProduct.get("uid").toString());
                    product.setName(jsonProduct.get("name").toString());
                    product.setLatinName(jsonProduct.get("latin_name").toString());
                    product.setPrice((double) jsonProduct.get("price"));
                    product.setDescription(jsonProduct.get("description").toString());
                    product.setInStock(((Long) jsonProduct.get("instock")).intValue());

                    this.addProduct(product);

                    products.put(Integer.toString(i), product);
                }
                break;
            default:
                throw new IOException("Preces netika atrastas noliktavas API.");
        }
    }
    public Category findByCategoryId( int id ) throws SQLException {

        PreparedStatement stmt;
        String sql;
        ResultSet rs;
        Category category = new Category();
        sql = "SELECT id, parentid, name, description FROM category WHERE `id` = (?) ";
        stmt = conn.prepareStatement(sql);
        stmt.setInt(1, id);
        rs = stmt.executeQuery();

        if(rs.next()) {
            category.setId(rs.getInt("id"));
            category.setParentId(rs.getInt("parentid"));

            if (rs.getInt("parentid") != 0) {
                category.setIsSubcategory(true);
            }

            category.setName(rs.getString("name"));
            category.setDescription(rs.getString("description"));
        }

        return category;
    }
    public void actionShowCart() throws SQLException {
        int counter = 0;

        if (cartProducts.size() > 0) {
            double totalSum = 0;
            System.out.println("---------------------------------------------------------------------------------------");
            System.out.printf("%2s. %-10s %-40s %-10s %-10s", "Nr", "Kods", "Preces nosaukums", "Daudzums", "Cena par vienībām");
            System.out.println("\n---------------------------------------------------------------------------------------");
            for (Map.Entry cartProduct : cartProducts.entrySet()) {
                counter++;
                String productUid = cartProducts.get(cartProduct.getKey()).getUid();
                String productName = cartProducts.get(cartProduct.getKey()).getName();
                productName = (productName.length () > 30 ) ? productName.substring ( 0 , 30 - 1 ).concat ( "…" ) : productName;
                int productCount = cartProducts.get(cartProduct.getKey()).getCartProductCount();
                double productTotalPrice = cartProducts.get(cartProduct.getKey()).getCartProductPriceTotal();
                totalSum += productTotalPrice;
                System.out.printf("%2d. %-10s %-40s %-10d %-10.2f\n", counter, productUid, productName, productCount, productTotalPrice);
            }
            System.out.println("---------------------------------------------------------------------------------------");
            System.out.printf("%-20s %52s", "Apmaksas summa:", NumberFormat.getCurrencyInstance(new Locale("lv", "LV")).format(totalSum));
            System.out.println("\n---------------------------------------------------------------------------------------");
        }
        else
        {
            System.out.println("Jūsu grozs pašlaik ir tukšs. Vai vēlaties pievienot grozam kādu preci? j/n");
            this.doYouWishToBuy();
        }
        backToList();
    }
    public void actionAddToCart() throws SQLException {
        Scanner scan = new Scanner(System.in);
        System.out.println("----------------------------------------------------------------------");
        String value;
        boolean askAgain = true;
        do
        {
            System.out.println("Norādiet preces numuru no saraksta, kuru vēlaties ielikt grozā (1 - "+ countProducts() + "):");

            while(!scan.hasNextInt())
            {
                System.out.println("Ievadīta vērtība nav skaitlis!");
                System.out.println("Norādiet preces numuru no saraksta, kuru vēlaties ielikt grozā (1 - "+ countProducts() + "):");
                scan.nextLine();
            }

            value = scan.nextLine();

            while(Integer.parseInt(value) > countProducts() || Integer.parseInt(value) < 1)
            {
                System.out.println("Nav tādas preces!");
                System.out.println("Norādiet preces numuru no saraksta, kuru vēlaties ielikt grozā (1 - "+ countProducts() + "):");
                value = scan.nextLine();
            }

            askAgain = false;

            String addAnother = "";

            Product product;
            CartProduct cartProduct = new CartProduct();

            int productNumber = Integer.parseInt(value) - 1;
            product = products.get(String.valueOf(productNumber));
            cartProduct.setId(product.getId());
            cartProduct.setUid(product.getUid());
            cartProduct.setName(product.getName());
            cartProduct.setPrice(product.getPrice());

            // before add product in the cart. check if it already exists in cart
            if (cartProducts.get(cartProduct.getId()) == null) {
                cartProduct.setCartProductCount(1);
                cartProduct.setCartProductPriceTotal(cartProduct.getPrice());
                cartProducts.put(cartProduct.getId(), cartProduct);
                System.out.println("Prece pievienota grozam.");
            } else {
                // get active cart product
                cartProduct = cartProducts.get(cartProduct.getId());

                // update product amount
                int productCount = cartProduct.getCartProductCount() + 1;
                cartProduct.setCartProductCount(productCount);

                // update product price for amount
                double productPriceTotal = cartProduct.getPrice() * productCount;
                cartProduct.setCartProductPriceTotal(productPriceTotal);

                cartProducts.remove(cartProduct.getId());
                cartProducts.put(cartProduct.getId(), cartProduct);

                System.out.println("Preces daudzums grozā ir atjaunots.");
            }

            System.out.println("Pievienot vēl preci? j/n");
            addAnother = scan.nextLine();

            while(!addAnother.equalsIgnoreCase("n") && !addAnother.equalsIgnoreCase("j"))
            {
                System.out.println("Ievadiet j/n!");
                addAnother = scan.nextLine();
            }

            if(addAnother.equalsIgnoreCase("j"))
            {
                askAgain = true;
            }
            else
            {
                askAgain = false;
                System.out.println("Vēlaties apmaksāt pirkumu? j/n");
                String doYouWantToPay;
                boolean pay = false;

                do {
                    doYouWantToPay = scan.nextLine();

                    if(!doYouWantToPay.equalsIgnoreCase("n") && !doYouWantToPay.equalsIgnoreCase("j"))
                    {
                        System.out.println("Ievadiet j/n!");
                        continue;
                    }
                    else if(doYouWantToPay.equalsIgnoreCase("j"))
                    {
                        pay = true;
                        this.actionCheckout();
                    }
                    else
                    {
                        this.actions();
                    }
                }
                while(pay == false);
            }
        }
        while(askAgain);
    }
    public void actionCheckout() throws SQLException {

        if( cartProducts.size() > 0 ) {
            Scanner scan = new Scanner(System.in);
            String nameLastname = "";

            System.out.println("Lūdzu ievadiet Jūsu kredītkartes informāciju ->");

            //name on credit card
            boolean correctName = false;

            do {
                System.out.println("Vārds un uzvārds, kas norādīts uz kredītkartes:");
                nameLastname = scan.nextLine();

                int wordCount = 0;
                boolean isAlphabetic = true;

                String[] arrPhrase = nameLastname.split(" ");
                for (int i = 0; i < arrPhrase.length; i++) {
                    if (!isAlphabetic(arrPhrase[i]) || arrPhrase[i].length() < 2) {
                        isAlphabetic = false;
                        break;
                    }
                    wordCount++;
                }

                if (wordCount >= 2 && isAlphabetic) {
                    correctName = true;
                }

                if (!correctName) {
                    System.out.println("Vārdam un uzvārdam ir jāsastāv vismaz no 2 vārdiem garākiem par 1 simbolu un jāsatur tikai burti! Mēģiniet vēlreiz.");
                }
            }
            while (!correctName);

            // card number
            String cardNumber;
            boolean correctCardNumber = false;
            int cardNumberLength = 0;

            do {
                System.out.println("Ievadiet kredītkartes numuru:");
                cardNumber = scan.nextLine();

                if (!isNumeric(cardNumber)) {
                    System.out.println("Kartes numurs nedrīkst saturēt burtus!");
                    //scan.nextLine();
                    continue;
                }

                cardNumberLength = cardNumber.trim().length();

                if (cardNumberLength != 12) {
                    System.out.println("Kartes numuram jāsatur 12 cipari!");
                    continue;
                }
                correctCardNumber = true;
            }
            while (!correctCardNumber);

            // month and year
            String cardMonth;
            boolean correctMonth = false;

            System.out.println("Ievadiet kredītkartes derīguma termiņu -> ");
            // scan.nextLine(); // clear line

            do {
                System.out.println("Mēnesis (01-12):");
                cardMonth = scan.nextLine();

                if (!isNumeric(cardMonth)) {
                    System.out.println("Menesim ir jāsastāv no cipariem!");
                    continue;
                }

                Pattern patternDate = Pattern.compile("^0[1-9]|1[012]$");
                Matcher matchDate = patternDate.matcher(cardMonth);
                boolean dateMatches = matchDate.find();

                if (!dateMatches) {
                    System.out.println("Nepareizs meneša formāts! Ievadiet skaitli no 01 līdz 12:");
                    continue;
                }
                correctMonth = true;
            }
            while (correctMonth == false);

            String cardYear;
            boolean correctYear = false;

            do {
                System.out.println("Gada pēdējie 2 cipari (00-99):");
                cardYear = scan.nextLine();

                if (!isNumeric(cardYear)) {
                    System.out.println("Menesim ir jāsastāv no cipariem!");
                    continue;
                }

                Pattern patternYear = Pattern.compile("^\\d{2}$");
                Matcher matchYear = patternYear.matcher(cardYear);
                boolean yearMatches = matchYear.find();

                if (!yearMatches) {
                    System.out.println("Nepareizs gada formāts!");
                    continue;
                }

                correctYear = true;
            }
            while (correctYear == false);

            String cardCvv;
            boolean correctCvv = false;

            // cvv code
            do {
                System.out.println("Ievadiet CVV kodu (3 cipari):");
                cardCvv = scan.nextLine();

                if (!isNumeric(cardCvv)) {
                    System.out.println("CVV kodam ir jāsastāv no cipariem!");
                    continue;
                }

                Pattern patternYear = Pattern.compile("^\\d{3}$");
                Matcher matchYear = patternYear.matcher(cardCvv);
                boolean yearMatches = matchYear.find();

                if (!yearMatches) {
                    System.out.println("Nepareizs CVV kods! ");
                    continue;
                }

                correctCvv = true;
            }
            while (correctCvv == false);

            System.out.println("\n-------------------------------------------------");
            System.out.println("Jūsu ievadītie kartes dati:");
            System.out.println("-------------------------------------------------");
            System.out.printf("%-25s %s\n", "Vārds/Uzvārds:", nameLastname);
            System.out.printf("%-25s %s\n", "Kredītkartes numurs:", cardNumber);
            System.out.printf("%-25s %s\n", "Gads/Mēnesis:", cardMonth + "/" + cardYear);
            System.out.printf("%-25s %s\n", "CVV:", cardCvv);
            System.out.println("-------------------------------------------------");

            System.out.println("Apmaksāt pirkumu? j/n");
            String answerPay;
            boolean askForPayment = true;

            do{
                answerPay = scan.nextLine();

                if(answerPay.equalsIgnoreCase("j")) {

                    System.out.println("Paldies! Jūsu pirkums ir veiksmīgi apmaksāts!");
                    this.actionExit();
                }
                else if(!answerPay.equalsIgnoreCase("j") && !answerPay.equalsIgnoreCase("n"))
                {
                    System.out.println("Ievadiet j/n:");
                    continue;
                }
                else
                {
                    System.out.println("Pirkuma apmaksa atcelta.");
                    this.actions();
                }
            }
            while( askForPayment );
        }
        else
        {
            System.out.println("\n-----------------------------------------");
            System.out.println("Jums ir jāieliek grozā vismaz viena prece, lai veiktu apmaksu. \nVai vēlaties ielikt preci iepirkumu grozā? j/n");
            System.out.println("-----------------------------------------");
            this.doYouWishToBuy();
        }
    }
    public void doYouWishToBuy() throws SQLException {
        boolean askForAction = true;
        String answer;

        do{
            Scanner scan = new Scanner(System.in);
            answer = scan.nextLine();

            if(answer.equalsIgnoreCase("j")) {
                this.actionAddToCart();
            }
            else if(!answer.equalsIgnoreCase("j") && !answer.equalsIgnoreCase("n"))
            {
                System.out.println("Ievadiet j/n:");
                continue;
            }
            else
            {
                System.out.println("n");
                this.actions();
            }
        }
        while( askForAction );
    }
    public boolean isAlphabetic(String str)
    {
        char[] charArray = str.toCharArray();
        for(char c:charArray)
        {
            if (!Character.isLetter(c))
                return false;
        }
        return true;
    }
    public boolean isNumeric(String str)
    {
        char[] charArray = str.toCharArray();
        for(char c:charArray)
        {
            if (!Character.isDigit(c))
                return false;
        }
        return true;
    }
    public int countProducts(){
        return this.products.size();
    }
    public static String splitString(String text, int wordsPerLine)
    {
        final StringBuilder newText = new StringBuilder();
        final StringTokenizer wordTokenizer = new StringTokenizer(text);
        long wordCount = 1;

        while (wordTokenizer.hasMoreTokens())
        {
            newText.append(wordTokenizer.nextToken());
            if (wordTokenizer.hasMoreTokens())
            {
                if (wordCount++ % wordsPerLine == 0)
                {
                    newText.append(LINEBREAK);
                }
                else
                {
                    newText.append(WHITESPACE);
                }
            }
        }
        return newText.toString();
    }
    public void backToList() throws SQLException {
        boolean askForAction = true;

        do{
            System.out.println("\n-----------------------------------------");
            System.out.println("Atgriezties uz sarakstu? (rakstiet 'j')");
            System.out.println("-----------------------------------------");
            Scanner scan = new Scanner(System.in);
            if(scan.nextLine().equalsIgnoreCase("j")) {
                this.actions();
                askForAction = false;
            }

        }
        while( askForAction );
    }
    public void actionExit(){
        System.out.println("\nIZEJAM NO VEIKALA...");
        System.exit(0);
    }
}
