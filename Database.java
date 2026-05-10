
import java.util.*;
import java.io.*;
import java.lang.*;
import java.util.Comparator;
import java.sql.*;

public class Database
{
    // Database connection constants
    private final static String DB_URL = "jdbc:postgresql://localhost:5432/pgAdmin";
    private final static String DB_USER = "postgres";
    private final static String DB_PASSWORD = "tan2003";
    
    // File constants (kept for backward compatibility with reports)
    private final static String STAFF_FILE = "dataFiles/staff.txt";
    private final static String MANAGER_FILE = "dataFiles/manager.txt";
    private final static String MENU_FILE = "dataFiles/menu_item.txt";
    private final static String REPORT_FILE = "dataFiles/reports/report_";
    private final static String PAYMENT_FILE = "dataFiles/reports/payment_";
    private final static String WAGE_INFO_FILE = "dataFiles/wage_info.txt";
    
    private Connection connection;
    private ArrayList<Staff> staffList = new ArrayList<Staff>();
    private ArrayList<MenuItem> menuList = new ArrayList<MenuItem>();
    private ArrayList<Order> orderList = new ArrayList<Order>();
    
    private java.util.Date    date;
    int     todaysOrderCounts;
    /****************************************************************************
     * Constructor
     ***************************************************************************/   
    public Database() throws DatabaseException
    {
        date = new java.util.Date();
        todaysOrderCounts = 0;
        
        try {
            // Establish database connection
            connectToDatabase();
            
            // Create tables if they don't exist
            createTablesIfNotExist();
            
            // Load data from database
            loadStaffFromDatabase();
            loadMenuFromDatabase();
            loadWageInfoFromDatabase();
            
            // Data migration from files is handled separately if needed
            
        } catch (SQLException e) {
            throw new DatabaseException("Database connection error: " + e.getMessage());
        }
    }
    
    /****************************************************************************
     * Database Connection Methods
     ***************************************************************************/   
    private void connectToDatabase() throws SQLException {
        try {
            // First try to connect to the target database
            Class.forName("org.postgresql.Driver");
            connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
            System.out.println("Connected to PostgreSQL database successfully!");
        } catch (SQLException e) {
            // If database doesn't exist, try to create it
            if (e.getMessage().contains("does not exist")) {
                System.out.println("Database 'pgAdmin' does not exist. Attempting to create it...");
                createDatabaseIfNotExists();
                // Now try connecting again
                connection = DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
                System.out.println("Connected to newly created PostgreSQL database successfully!");
            } else {
                throw e;
            }
        } catch (ClassNotFoundException e) {
            throw new SQLException("PostgreSQL JDBC Driver not found: " + e.getMessage());
        }
    }
    
    private void createDatabaseIfNotExists() throws SQLException {
        // Connect to default postgres database to create our database
        String defaultUrl = "jdbc:postgresql://localhost:5432/postgres";
        Connection tempConnection = null;
        try {
            tempConnection = DriverManager.getConnection(defaultUrl, DB_USER, DB_PASSWORD);
            Statement stmt = tempConnection.createStatement();
            try {
                stmt.executeUpdate("CREATE DATABASE \"pgAdmin\"");
                System.out.println("Database 'pgAdmin' created successfully!");
            } catch (SQLException e) {
                if (e.getMessage().contains("already exists")) {
                    System.out.println("Database 'pgAdmin' already exists!");
                } else {
                    throw e;
                }
            }
            stmt.close();
        } finally {
            if (tempConnection != null) {
                tempConnection.close();
            }
        }
    }
    
    private void createTablesIfNotExist() throws SQLException {
        Statement stmt = connection.createStatement();
        
        // Create staff table
        String createStaffTable = 
            "CREATE TABLE IF NOT EXISTS staff (" +
            "id SERIAL PRIMARY KEY," +
            "password VARCHAR(255) NOT NULL," +
            "first_name VARCHAR(255) NOT NULL," +
            "last_name VARCHAR(255) NOT NULL," +
            "is_manager BOOLEAN DEFAULT FALSE," +
            "wage_rate DECIMAL(10,2) DEFAULT 0.0" +
            ")";
        stmt.execute(createStaffTable);
        
        // Create menu_items table
        String createMenuTable = 
            "CREATE TABLE IF NOT EXISTS menu_items (" +
            "id SERIAL PRIMARY KEY," +
            "name VARCHAR(255) NOT NULL," +
            "price DECIMAL(10,2) NOT NULL," +
            "type SMALLINT NOT NULL" +
            ")";
        stmt.execute(createMenuTable);
        
        // Create orders table
        String createOrdersTable = 
            "CREATE TABLE IF NOT EXISTS orders (" +
            "id SERIAL PRIMARY KEY," +
            "order_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP," +
            "status VARCHAR(50) DEFAULT 'pending'," +
            "total_amount DECIMAL(10,2) DEFAULT 0.0" +
            ")";
        stmt.execute(createOrdersTable);
        
        // Create order_details table
        String createOrderDetailsTable = 
            "CREATE TABLE IF NOT EXISTS order_details (" +
            "id SERIAL PRIMARY KEY," +
            "order_id INTEGER REFERENCES orders(id)," +
            "menu_item_id INTEGER REFERENCES menu_items(id)," +
            "quantity INTEGER NOT NULL," +
            "price DECIMAL(10,2) NOT NULL" +
            ")";
        stmt.execute(createOrderDetailsTable);
        
        stmt.close();
        System.out.println("Database tables created/verified successfully!");
    }
    
    /****************************************************************************
     * Database Load Methods
     ***************************************************************************/   
    private void loadStaffFromDatabase() throws SQLException {
        staffList.clear();
        String query = "SELECT id, password, first_name, last_name, is_manager, wage_rate FROM staff ORDER BY id";
        PreparedStatement pstmt = connection.prepareStatement(query);
        ResultSet rs = pstmt.executeQuery();
        
        while (rs.next()) {
            int id = rs.getInt("id");
            String password = rs.getString("password");
            String firstName = rs.getString("first_name");
            String lastName = rs.getString("last_name");
            boolean isManager = rs.getBoolean("is_manager");
            double wageRate = rs.getDouble("wage_rate");
            
            Staff staff;
            if (isManager) {
                staff = new Manager(id, lastName, firstName, password);
            } else {
                staff = new Employee(id, lastName, firstName, password);
            }
            staff.setWageRate(wageRate);
            staffList.add(staff);
        }
        
        rs.close();
        pstmt.close();
    }
    
    private void loadMenuFromDatabase() throws SQLException {
        menuList.clear();
        String query = "SELECT id, name, price, type FROM menu_items ORDER BY id";
        PreparedStatement pstmt = connection.prepareStatement(query);
        ResultSet rs = pstmt.executeQuery();
        
        while (rs.next()) {
            int id = rs.getInt("id");
            String name = rs.getString("name");
            double price = rs.getDouble("price");
            byte type = rs.getByte("type");
            
            MenuItem menuItem = new MenuItem(id, name, price, type);
            menuList.add(menuItem);
        }
        
        rs.close();
        pstmt.close();
    }
    
    private void loadWageInfoFromDatabase() throws SQLException {
        // Wage info is now stored in the staff table, so this method is for compatibility
        // The loadStaffFromDatabase method already loads wage rates
    }
    
    /****************************************************************************
     * Getter
     ***************************************************************************/
     public ArrayList<Staff> getStaffList()
     {
         return staffList;
     }
     
     public ArrayList<MenuItem> getMenuList()
     {
         return menuList;
     }
     
     public ArrayList<Order> getOrderList()
     {
         return orderList;
     }
     
     public int getTodaysOrderCount()
     {
         return this.todaysOrderCounts;
     }
     
    //----------------------------------------------------------
    // Find staff from ID
    //----------------------------------------------------------
    public Staff   findStaffByID(int id)
    {
        Iterator<Staff> it = staffList.iterator();
        Staff           re = null;
        boolean         found = false;
        
        if(id < 0){
            return null;
        }
        
        while (it.hasNext() && !found) {
            re = (Staff)it.next();  
            if( re.getID() == id)
            {
                found = true;
            }
        }
        
        if(found)
            return re;
        else        
            return null;
    }
    
    //----------------------------------------------------------
    // Find menu item from ID
    //----------------------------------------------------------
    public MenuItem   findMenuItemByID(int id)
    {
        Iterator<MenuItem> it = menuList.iterator();
        MenuItem           re = null;
        boolean         found = false;
        
        if(id < 0){
            return null;
        }
        
        while (it.hasNext() && !found) {
            re = (MenuItem)it.next();  
            if( re.getID() == id)
            {
                found = true;
            }
        }
        
        if(found)
            return re;
        else        
            return null;
    }
    
    //----------------------------------------------------------
    // Find order from ID
    //----------------------------------------------------------
    public Order   findOrderByID(int id)
    {
        Iterator<Order> it = orderList.iterator();
        Order           re = null;
        boolean         found = false;
        
        if(id < 0){
            return null;
        }
        
        while (it.hasNext() && !found) {
            re = it.next();  
            if( re.getOrderID() == id)
            {
                found = true;
            }
        }
        
        if(found)
            return re;
        else        
            return null;
    }
     /****************************************************************************
     * Manipurate datas
     ***************************************************************************/
     //---------------------------------------------------------------
     // Staff information
     //---------------------------------------------------------------
     //edit staff data
     // rStaff reference the staff 
     // which 1:Lastname 2:Firstname 3:passward
     public final static int EDIT_LAST_NAME = 1;
     public final static int EDIT_FIRST_NAME = 2;
     public final static int EDIT_PASSWORD = 3;
     
     public void editStaffData(int staffID, String newPassword, String newFirstName, String newLastName) throws DatabaseException
     {
        Staff  rStaff = findStaffByID(staffID);
        rStaff.setPassword(newPassword);
        rStaff.setLastName(newLastName);
        rStaff.setFirstName(newFirstName);
    
        try
        {
            if(rStaff instanceof Manager)
            //if(rStaff.getClass().getName().equalsIgnoreCase("Manager"))
            updateStaffFile(true);//update manager file
            else
            updateStaffFile(false);//update employee file
        }
        catch(DatabaseException dbe)
        {
            throw dbe;
        }
    }
     
     public void editStaffData(Staff rStaff, int which, String newData) throws DatabaseException
     {
         switch(which)
         {
             case EDIT_LAST_NAME:
                rStaff.setLastName(newData);
             break;
             case EDIT_FIRST_NAME:
                rStaff.setFirstName(newData);
             break;
             case EDIT_PASSWORD:
                rStaff.setPassword(newData);
             break;
             default:
             break;
         }
         
         try
         {
             if(rStaff instanceof Manager)
             //if(rStaff.getClass().getName().equalsIgnoreCase("Manager"))
                updateStaffFile(true);//update manager file
             else
                updateStaffFile(false);//update employee file
         }
         catch(DatabaseException dbe)
         {
             throw dbe;
         }
     }
     
     public void deleteStaff(Staff rStaff) throws DatabaseException
     {
         boolean isManager = false;
         staffList.remove(rStaff);
         //if(rStaff.getClass().getName().equalsIgnoreCase("Manager"))
        if(rStaff instanceof Manager)
         isManager = true;
        try
        {
            updateStaffFile(isManager);
        }
        catch(DatabaseException dbe)
        {
            throw dbe;
        }
     }
     
     
     public void addStaff(int newID, String newPassward, String newFirstName, String newLastName, boolean isManager) throws DatabaseException
     {
         Staff newStaff;
         if(isManager)
            newStaff = new Manager(newID, newLastName, newFirstName, newPassward);
         else
            newStaff = new Employee(newID, newLastName, newFirstName, newPassward);
         staffList.add(newStaff);
         if(newStaff instanceof Manager)
         //if(newStaff.getClass().getName().equalsIgnoreCase("Manager"))
            isManager = true;
        try
        {
            updateStaffFile(isManager);
        }
        catch(DatabaseException dbe)
        {
            throw dbe;
        }
     }

     //---------------------------------------------------------------
     // MenuItem
     //---------------------------------------------------------------
     //edit menu item data
     // rMenuItem reference the MenuItem 
     // which 1:name 2:price 3:type
     public final static int EDIT_ITEM_NAME = 1;
     public final static int EDIT_ITEM_PRICE = 2;
     public final static int EDIT_ITEM_TYPE = 3;
     
     public void editMenuItemData(int id, String newName, double newPrice, byte menuType) throws DatabaseException
     {
         MenuItem rMenuItem = findMenuItemByID(id);
         rMenuItem.setName(newName);
         rMenuItem.setPrice(newPrice);
         rMenuItem.setType(menuType);
         
         try
         {
             updateMenuInDatabase();
         }
         catch(DatabaseException dbe)
         {
             throw dbe;
         }
     }
     
     public void editMenuItemData(MenuItem rMenuItem, int which, String newData) throws DatabaseException
     {
         try
         {
             switch(which)
             {
                 case EDIT_ITEM_NAME:
                    rMenuItem.setName(newData);
                    break;
                 case EDIT_ITEM_PRICE:
                    double newPrice = Double.parseDouble(newData);
                    if(newPrice < 0)
                        throw new DatabaseException("Price must be positive number");
                    else
                        rMenuItem.setPrice(newPrice);
                    break;
                 case EDIT_ITEM_TYPE:
                    byte newType = Byte.parseByte(newData);
                    if(newType < MenuItem.MAIN || MenuItem.DESSERT < newType)
                        throw new DatabaseException("Type must be between " + MenuItem.MAIN
                                            + " and " + MenuItem.DESSERT + ")");
                    else
                        rMenuItem.setType(Byte.parseByte(newData));
                    break;
                 default:
                    break;
             }
             updateMenuInDatabase();
        }
        catch(DatabaseException e)
        {
            throw e;
        }
        catch(Exception e)
        {
            throw new DatabaseException(e.getMessage());
        }
     }
     
     public void setMenuItemAsPromotionItem(MenuItem rMenuItem, double price)
     {
         rMenuItem.setState(MenuItem.PROMOTION_ITEM, price);
     }
     
     public void resetMenuState(MenuItem rMenuItem)
     {
         rMenuItem.resetState();
     }
     
     public void deleteMenuItem(MenuItem rMenuItem) throws DatabaseException
     {
         menuList.remove(rMenuItem);
         try
         {
             updateMenuInDatabase();
         }
         catch(DatabaseException dbe)
         {
             throw dbe;
         }
     }
     
     public void addMenuItem(int newID, String newName, double newPrice, byte newType) throws DatabaseException
     {
         MenuItem newMenuItem = new MenuItem(newID, newName,newPrice, newType);
         menuList.add(newMenuItem);
         Collections.sort(menuList, new MenuItemComparator());
         try
         {
             updateMenuInDatabase();
         }
         catch(DatabaseException dbe)
         {
             throw dbe;
         }
     }
     //---------------------------------------------------------------
     // Order
     //---------------------------------------------------------------
     public int addOrder(int staffID, String staffName)
     {
         int newOrderID = ++todaysOrderCounts;
         Order newOrder = new Order(staffID, staffName);
         newOrder.setOrderID( newOrderID);
         orderList.add(newOrder);
         return newOrderID;
     }
     
     public void addOrderItem(int orderID, MenuItem rItem, byte quantity)
     {
         Order rOrder = findOrderByID(orderID);
         rOrder.addItem(rItem, quantity);
     }
     
     public boolean deleteOrderItem(int orderID, int index)
     {
          Order rOrder = findOrderByID(orderID);
          if(rOrder == null)
            return false;
          return rOrder.deleteItem(index);
     }
     
     
     //Cancel order: order data is not deleted from the database(Just put cancel flag on)
     public boolean cancelOrder(int orderID)
     {
         Order rOrder = findOrderByID(orderID);
        if(rOrder == null)
            return false;
         rOrder.setState(Order.ORDER_CANCELED);
         return true;
     }
     //Delete order: order data is deleted from the database
     public boolean deleteOrder(int orderID)
     {
         Order rOrder = findOrderByID(orderID);
        if(rOrder == null)
            return false;
         orderList.remove(rOrder);
         todaysOrderCounts--;
         return true;
     }
     
     public boolean closeOrder(int orderID)
     {
         Order rOrder = findOrderByID(orderID);
        if(rOrder == null)
            return false;
         rOrder.setState(Order.ORDER_CLOSED);
         return true;
     }
     
     public void closeAllOrder()
     {
        Iterator<Order> it = orderList.iterator();
        Order           re = null;
        
        while (it.hasNext()) {
            re = it.next();  
            if( re.getState() == 0)//neither closed and canceled
            {
                re.setState(Order.ORDER_CLOSED);
            }
        }
     }
     
     public int getOrderState(int orderID)
     {
         Order  re = findOrderByID(orderID);
         if(re == null)
             return -1;
         return re.getState();
     }
     
     public double getOrderTotalCharge(int orderID)
     {
         Order  re = findOrderByID(orderID);
         if(re == null)
             return -1;
         return re.getTotal();
     }
     
     public boolean checkIfAllOrderClosed()
     {
        Iterator<Order> it = orderList.iterator();
        Order           re = null;
        
        while (it.hasNext()) {
            re = it.next();  
            if( re.getState() == 0)//neither closed and canceled
            {
                return false;
            }
        }
        return true;
     }
     
     public boolean checkIfAllStaffCheckout()
     {
        Iterator<Staff> it = staffList.iterator();
        Staff           re = null;
        
        while (it.hasNext()) {
            re = it.next();  
            if( re.getWorkState() == Staff.WORKSTATE_ACTIVE)
            {
                return false;
            }
        }
        return true;
     }
     
     public void forthClockOutAllStaff()
     {
         Iterator<Staff> it = staffList.iterator();
         Staff           re = null;
        
         while (it.hasNext()) {
            re = it.next();  
            if( re.getWorkState() == Staff.WORKSTATE_ACTIVE)
            {
                re.clockOut();
            }
         }
     }
     /****************************************************************************
    * Database Update Methods
    ***************************************************************************/
    public void updateStaffInDatabase() throws DatabaseException {
        try {
            // Clear existing data
            String deleteQuery = "DELETE FROM staff";
            PreparedStatement deleteStmt = connection.prepareStatement(deleteQuery);
            deleteStmt.executeUpdate();
            deleteStmt.close();
            
            // Insert all staff data
            String insertQuery = "INSERT INTO staff (id, password, first_name, last_name, is_manager, wage_rate) VALUES (?, ?, ?, ?, ?, ?)";
            PreparedStatement insertStmt = connection.prepareStatement(insertQuery);
            
            for (Staff staff : staffList) {
                insertStmt.setInt(1, staff.getID());
                insertStmt.setString(2, staff.getPassword());
                insertStmt.setString(3, staff.getFirstName());
                insertStmt.setString(4, staff.getLastName());
                insertStmt.setBoolean(5, staff instanceof Manager);
                insertStmt.setDouble(6, staff.getWageRate());
                insertStmt.executeUpdate();
            }
            
            insertStmt.close();
            System.out.println("Staff data updated in database successfully!");
            
        } catch (SQLException e) {
            throw new DatabaseException("Error updating staff in database: " + e.getMessage());
        }
    }
    
    // Keep the old method name for compatibility but redirect to database method
    public void updateStaffFile(boolean isManager) throws DatabaseException {
        updateStaffInDatabase();
    }
    
    public void updateMenuInDatabase() throws DatabaseException {
        try {
            // Clear existing data
            String deleteQuery = "DELETE FROM menu_items";
            PreparedStatement deleteStmt = connection.prepareStatement(deleteQuery);
            deleteStmt.executeUpdate();
            deleteStmt.close();
            
            // Insert all menu data
            String insertQuery = "INSERT INTO menu_items (id, name, price, type) VALUES (?, ?, ?, ?)";
            PreparedStatement insertStmt = connection.prepareStatement(insertQuery);
            
            for (MenuItem menuItem : menuList) {
                insertStmt.setInt(1, menuItem.getID());
                insertStmt.setString(2, menuItem.getName());
                insertStmt.setDouble(3, menuItem.getPrice());
                insertStmt.setByte(4, menuItem.getType());
                insertStmt.executeUpdate();
            }
            
            insertStmt.close();
            System.out.println("Menu data updated in database successfully!");
            
        } catch (SQLException e) {
            throw new DatabaseException("Error updating menu in database: " + e.getMessage());
        }
    }
    
    public String generateOrderReport( String todaysDate) throws DatabaseException
    {
        Writer          writer = null;
        String          line;
        int             state;
        double          totalAllOrder = 0;
        String          generateFileName;
        File            newFile;
        int             orderCnt = 0;
        int             cancelCnt = 0;
        double          cancelTotal = 0;
        
        String[] record = todaysDate.split("/");
        String today = record[0].trim() + "_" + record[1].trim() + "_" + record[2].trim();
        
        // Save to user's Downloads folder instead of dataFiles/reports
        String userHome = System.getProperty("user.home");
        generateFileName = userHome + "/Downloads/report_" + today + ".txt";
        newFile = new File(generateFileName);
        
        try{
            writer = new BufferedWriter(new FileWriter(newFile));

            line = "*********** Order List (" + today + ") ***********\r\n";
            writer.write(line);
            
            Iterator<Order> it = orderList.iterator();
            while (it.hasNext())
            {
                Order re = it.next();
                state = re.getState();
                String stateString = "";
                double totalOfEachOrder = re.getTotal();
                switch(state)
                {
                    case Order.ORDER_CLOSED:
                        stateString = "";
                        totalAllOrder += totalOfEachOrder;
                        orderCnt++;
                    break;
                    case Order.ORDER_CANCELED:
                        stateString = "Canceled";
                        cancelTotal += totalOfEachOrder;
                        cancelCnt++;
                    break;
                    default:
                        stateString = "";
                        totalAllOrder += totalOfEachOrder;
                        orderCnt++;
                    break;
                }
                String output = String.format("Order ID:%4d  StaffName:%-30s  Total:₹%-5.2f %s\r\n",
                                            re.getOrderID(),re.getStaffName(),totalOfEachOrder, stateString);
                writer.write(output);
                
                
            }
            writer.write("-------------------------------------------------------\r\n");
            
            writer.write("Total sales:₹" + totalAllOrder + "(" + orderCnt + ")" +
                    "  Canceled:₹" + cancelTotal + "(" + cancelCnt + ")\r\n");
            writer.flush();
            writer.close();
        }
        catch(IOException e)
        {
            String message = e.getMessage() + e.getStackTrace();
            newFile.delete();
            throw new DatabaseException(message);
        }
        return generateFileName;
        //System.out.println("File <" + generateFileName + "> has been generated.");
    }
    
    public String generatePaymentReport( String todaysDate) throws DatabaseException
    {
        Writer          writer = null;
        String          line;
        double          totalPayment = 0;
        String          generateFileName;
        File            newFile;
        int             staffNum = 0;
        
        String[] record = todaysDate.split("/");
        String today = record[0].trim() + "_" + record[1].trim() + "_" + record[2].trim();
        
        // Save to user's Downloads folder instead of dataFiles/reports
        String userHome = System.getProperty("user.home");
        generateFileName = userHome + "/Downloads/payment_" + today + ".txt";
        newFile = new File(generateFileName);
        
        try{
            writer = new BufferedWriter(new FileWriter(newFile));

            line = "*********** Payment List (" + today + ") ***********\r\n";
            writer.write(line);
            
            Iterator<Staff> it = staffList.iterator();
            while (it.hasNext())
            {
                Staff re = it.next();
 
                if(re.getWorkState() == Staff.WORKSTATE_FINISH)
                {
                    double pay = re.culculateWages();
                    String output = String.format("Order ID:%4d  StaffName:%-30s  Work time:%-5.2f Pay:%-5.2f\r\n",
                                                re.getID(),re.getFullName(),re.culculateWorkTime(), pay);
                    writer.write(output);
                    staffNum++;
                    totalPayment += pay;
                }
            }
            writer.write("-------------------------------------------------------\r\n");
            
            writer.write("Total payment:₹" + totalPayment + "(" + staffNum + ")\r\n");
            writer.flush();
            writer.close();
        }
        catch(IOException e)
        {
            String message = e.getMessage() + e.getStackTrace();
            newFile.delete();
            throw new DatabaseException(message);
        }
        return generateFileName;
    }
    
    /****************************************************************************
    * Comparator
    ***************************************************************************/
    private class StaffComparator implements Comparator<Staff> {

        @Override
        public int compare(Staff s1, Staff s2) {
            return s1.getID() < s2.getID() ? -1 : 1;
        }
    }
    
    private class MenuItemComparator implements Comparator<MenuItem> {

        @Override
        public int compare(MenuItem m1, MenuItem m2) {
            return m1.getID() < m2.getID() ? -1 : 1;
        }
    }
}
