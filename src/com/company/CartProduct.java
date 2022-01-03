package com.company;

public class CartProduct extends Product{
    private int cartProductCount;
    private double cartProductPriceTotal;


    public void setCartProductCount(int cartProductCount)
    {
        this.cartProductCount = cartProductCount;
    }
    public int getCartProductCount()
    {
        return this.cartProductCount;
    }

    public void setCartProductPriceTotal(double cartProductPriceTotal)
    {
        this.cartProductPriceTotal = cartProductPriceTotal;
    }
    public double getCartProductPriceTotal()
    {
        return this.cartProductPriceTotal;
    }
}

