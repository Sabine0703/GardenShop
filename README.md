# GardenShop
Application is a demo/starting code for a web shop mobile app. 
When you run application it connects with API (PHP file that porduces JSON array output) using link and harvests data about shop products form it (application Inserts/Updates products info in DB). Product categories and subcategories are also stored in database. Application allows to review products, add them to cart and do a "test" payment. 
For a future development a real payment can be added to application, with shipping and with purchase info storage. Also data about updated products could be sent out to a source API website with updated product info to exchange products/purchases data.

This application allows user to do different actions:
0. Exit shop
1. Review products (with description)
2. Review products (short version for quick view)
3. Add to cart
4. View cart
5. Pay for products (just emulation of payment without real billing)

Depending on chosen action user are guided through different actions andp possibilities, to do not get stuck at some point.
