package com.company;

import java.sql.*;

public class Main {

    public static void main(String[] args) throws SQLException {
        GardenShop shop = new GardenShop();

        try{
            shop.welcome();
            shop.actions();
        }
        catch(Exception exc) {
            System.out.println("Notikusi kļūda: " + exc.getMessage());
            System.exit(0);
        }
    }
}
