package com.company;

public class Product {
    private int id;
    private String uid;
    private int categoryId;
    private int subcategoryId;
    private double price;
    private String name;
    private String latinName;
    private String description;
    private int inStock;

    public void setId(int id)
    {
        this.id = id;
    }
    public int getId()
    {
        return this.id;
    }

    public void setUid(String uid) { this.uid = uid; }
    public String getUid()
    {
        return this.uid;
    }

    public void setCategoryId(int categoryId) { this.categoryId = categoryId; }
    public int getCategoryId()
    {
        return this.categoryId;
    }

    public void setSubcategoryId(int subcategoryId) { this.subcategoryId = subcategoryId; }
    public int getSubcategoryId()
    {
        return this.subcategoryId;
    }

    public void setPrice(double price)
    {
        this.price = price;
    }
    public double getPrice()
    {
        return this.price;
    }

    public void setName(String name)
    {
        this.name = name;
    }
    public String getName()
    {
        return this.name;
    }

    public void setLatinName(String latinName)
    {
        this.latinName = latinName;
    }
    public String getLatinName()
    {
        return this.latinName;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }
    public String getDescription()
    {
        return this.description;
    }

    public void setInStock(int inStock)
    {
        this.inStock = inStock;
    }
    public int getInStock()
    {
        return this.inStock;
    }
}
