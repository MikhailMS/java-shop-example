[![Codacy Badge](https://api.codacy.com/project/badge/Grade/50ce9e1a567343ee9ac7c134071d97ba)](https://www.codacy.com/app/1488maiklm/java-shop-example?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=MikhailMS/java-shop-example&amp;utm_campaign=Badge_Grade)
[![Build Status](https://travis-ci.org/MikhailMS/java-shop-example.svg?branch=master)](https://travis-ci.org/MikhailMS/java-shop-example)
# Java shop example
Typical Java exercise that shows how to build simple OO project and practice skills such as: how to choose appropriate data type for project, how to write clean and consistent code and etc.

This project will be written using Java 8 approach, rather than Java 7 and older. As Java 9 is released, maybe some of new Java features will be introduced if needed.

Also I am using TDD approach to complete this project as I found this really convenient way to develop projects in Java. And as extra I'm trying to use Travis CI for building and testing project together with Codacy for keeping my code clean and consistent.
## Plan of action:
  - [x] Erase dummy tests for **Product, Basket, Order, Inventory, Shop** classes and re-write them accordingly
      - [x] **ProductTest** class
      - [x] **BasketTest** class
      - [x] **OrderTest** class
      - [x] **InventoryTest** class
      - [x] **ShopTest** class
      
  - [x] Complete classes mentioned above to see, how application works
      - [x] **Product** class
      - [x] **Basket** class
      - [x] **Order** class
      - [x] **Inventory** class
      - [x] **Shop** class
      
  - [x] In simple version of such exercise, to save data it is proposed to write data into files, however I'll write it into PostgreSQL DB
      - [x] Can save basket into DB
      - [x] Can save order into DB
      - [x] Can restore basket from DB
      - [x] Can restore order from DB
      
  - [x] As an optional part, I'd like to introduce user system: administration and clients. First would be able to see all completed orders and inventory stock and its total cost, while customers can fill in baskets, make orders and see theirs basket and orders only
      - [x] Administrator user can
          - [x] See all available products
          - [x] See total cost of all products
          - [x] Add products to inventory
          - [x] Remove products from inventory
          - [x] See orders completed by any user
          - [x] See total cost of all completed orders
          - [x] Add user to the system
          - [x] Delete user from system
          - [x] Filter products by name, price and weight
          - [x] Filter orders by date
          - [x] Filter orders by total price
      - [x] Client user can
          - [x] See all available products
          - [x] Add to basket
          - [x] Remove from basket
          - [x] Save basket
          - [x] Restore basket
          - [x] Save order 
          - [x] Restore order
          - [x] Complete order
          - [x] See all orders (client can see only his orders)
          - [x] Filter products by name, price and weight
          - [x] Filter orders by date
          - [x] Filter orders by total price 
      
  - [ ] As an optional part, I'd like to introduce GUI, which will make it easier to fill basket, see orders and etc
      - [x] Login screen
      - [x] Can login into system and gain appropriate rights
      - [x] Client GUI
          - [x] See all available products
          - [x] Add to basket
          - [x] Remove from basket
          - [x] Complete order
          - [x] See all completed orders (client can see only his orders) 
          - [x] See total cost of all completed orders
          - [x] Sort orders by total price
          - [x] Filter orders by total price
          - [x] Search for specific product by name
          - [x] Sort products by name, price and weight
          - [x] Filter products by name, price and weight
      - [x] Administrator GUI
          - [x] See all available products 
          - [x] See total cost of all products
          - [x] Add new product to inventory - **needs to be connected to DB**
          - [x] Add products to inventory - **needs to be connected to DB**
          - [x] Remove products from inventory - **needs to be connected to DB**
          - [x] See orders completed by any user 
          - [x] See total cost of all completed orders
          - [x] Sort orders by date, total price
          - [x] Filter orders by date/total price
          - [x] Add user to the system - **needs to be connected to DB**
          - [x] Delete user from system - **needs to be connected to DB**
          - [x] Search for specific product by name
          - [x] Sort products by name, price
          - [x] Filter products by price, weight
      - [ ] Bring all GUI components into one Stage
          - [x] Create main stage with TabPane(tab panel, that contains scenes) 
              - [x] Main stage, which creates tab panels depending on the privilege level of the logged user
              - [x] Save basket 
              - [x] Restore basket
          - [ ] Connect GUI to database
              - [x] Login GUI
              - [x] Client GUI
                  - [x] Highlighted parts
              - [ ] Administrator GUI
                  - [ ] Highlighted parts
              
  - [ ] As an optional part, I'd like to write a service, which will keep shop's inventory in sync with DB and completed orders
      - [ ] Once order is completed, service will update inventory in the DB and update local copy of the inventory
      - [ ] It will also check, if order can be made, ie not enough products in the inventory
      - [ ] If product(-s) not available to order, notify user and administrator  
  
## Bug trace
  - [x] When adding new product via form, product is added, but then I cannot change its amount
  - [ ] restoreBasketFromDB method shall be fixed, as currently it's hardcoded to use fixed weight and price for all restored products in Basket

## Improvements, that could be done later
  - [ ] At the moment user can add more products into basket than there exist in inventory -> make them talk to one another
  - [ ] At the moment there is no column to sort orders by date, but could be added. Will require extra column in GUI and extra filed in Order class
  - [ ] At the moment data in rows does not update straight after product has been added/removed to/from basket/inventory, but rather after row been clicked, table been sorted or Details button been clicked
  - [ ] At the moment, when new product added to basket/inventory, in order to display it, I delete all entries from observable list + its underlying list
    
## Build and tested on MacOS and Java 1.8 (_**required**_)
