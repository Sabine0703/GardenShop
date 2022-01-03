package com.company;

/* 2 levels:
    - category
    - subcategory with 1 parent
 */
public class Category {
    private int id;
    private int parentId;
    private String name;
    private String description;
    private boolean isSubcategory;

    public void setId(int id)
    {
        this.id = id;
    }
    public int getId()
    {
        return this.id;
    }

    public void setParentId(int parentId)
    {
        this.parentId = parentId;
    }
    public int getParentId()
    {
        return this.parentId;
    }

    public void setName(String name) { this.name = name; }
    public String getName() { return this.name; }

    public void setDescription(String description)
    {
        this.description = description;
    }
    public String getDescription() { return this.description; }

    public void setIsSubcategory(boolean isSubcategory)
    {
        this.isSubcategory = isSubcategory;
    }
    public boolean getIsSubcategory() { return this.isSubcategory; }
}
