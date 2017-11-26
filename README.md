[![Codacy Badge](https://api.codacy.com/project/badge/Grade/50ce9e1a567343ee9ac7c134071d97ba)](https://www.codacy.com/app/1488maiklm/java-shop-example?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=MikhailMS/java-shop-example&amp;utm_campaign=Badge_Grade)
[![Build Status](https://travis-ci.org/MikhailMS/java-shop-example.svg?branch=master)](https://travis-ci.org/MikhailMS/java-shop-example)
# Java shop example
Typical Java exercise that shows how to build simple OO project and practice skills such as: how to choose appropriate data type for project, how to write clean and consistent code and etc.

This project will be written using Java 8 approach, rather than Java 7 and older. As Java 9 is released, maybe some of new Java features will be introduced if needed.

Also I am using TDD approach to complete this project as I found this really convinient way to develop projects in Java. And as extra I'm trying to use Travis CI for building and testing project together with Codacy for keeping my code clean and consistent.
## Plan of action:
  - [ ] Erase dummy tests for **Product, Basket, Order, Inventory, Shop** classes and re-write them accordingly
      - [ ] **ProductTest** class
      - [ ] **BasketTest** class
      - [ ] **OrderTest** class
      - [ ] **InventoryTest** class
      - [ ] **ShopTest** class
      
  - [ ] Complete classes mentioned above to see, how application works
      - [ ] **Product** class
      - [ ] **Basket** class
      - [ ] **Order** class
      - [ ] **Inventory** class
      - [ ] **Shop** class
      
  - [ ] In simple version of such exercise, to save data it is proposed to write data into files, however I'll write it into PostgreSQL DB
      - [ ] Can save basket into DB
      - [ ] Can save order into DB
      - [ ] Can restore basket from DB
      - [ ] Can restore order from DB
      
  - [ ] As an optional part, I'd like to introduce user system: administration and clients. First would be able to see all completed orders and inventory stock and its total cost, while customers can fill in baskets, make orders and see theirs basket and orders only
      - [ ] Can create administrator user
      - [ ] Can create client user
      - [ ] Can login into system and gain appropriate rights
      
  - [ ] As an optional part, I'd like to introduce GUI, which will make it easier to fill basket, see orders and etc
      - [ ] Login screen
      - [ ] Client 
          - [ ] See all available products
          - [ ] Add to basket
          - [ ] Remove from basket
          - [ ] Save basket
          - [ ] Restore basket
          - [ ] Make order
          - [ ] Save order
          - [ ] Complete order
          - [ ] Restore order
          - [ ] See all completed orders (client can see only his orders)
          - [ ] Search for specific product by name
          - [ ] Filter products by price, weight
          - [ ] Filter orders by date/total price
      - [ ] Administrator
          - [ ] See all available products
          - [ ] See total cost of all products
          - [ ] Add products to inventory
          - [ ] Remove products from inventory
          - [ ] See orders completed by any user
          - [ ] See total cost of all completed orders
          - [ ] Add user to the system
          - [ ] Delete user from system
          
  - [ ] As an optional part, I'd like to write a service, which will keep shop's inventory in sync with DB and completed orders
      - [ ] Once order is completed, service will update inventory in the DB and update local copy of the inventory
      - [ ] It will also check, if order can be made, ie not enough products in the inventory
      - [ ] If product(-s) not available to order, notify user and administrator  
    
## Build and tested on MacOS and Java 1.8
