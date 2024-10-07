# SMART INVENTORY Project

## Overview

The **SMART INVENTORY Project** is a mobile application designed to streamline warehouse management by offering real-time visibility into inventory levels, efficient communication between users and warehouse managers, and automated processes for managing stock and order fulfillment. Warehouse managers can easily check in items, track stock levels, and respond to user requests. The application also allows users to search for products, receive low stock alerts, and communicate through real-time messaging, making inventory management more efficient and reducing the chances of errors.

By leveraging Firebase for real-time data synchronization, user authentication, and storage, the SMART INVENTORY Project ensures a reliable and scalable solution. Its user-friendly interface and mobile accessibility make it an ideal tool for businesses seeking to optimize their warehouse operations. With key features like filtered search, request tracking, and inventory history, the application provides a comprehensive solution to improve the efficiency of day-to-day warehouse management without the complexity of traditional systems.

---

## Features

### 1. Search Bar & Check-in
- **Inventory Search:** Allows users to search for specific items by name.
- **Filtered Search:** Enables filtering by attributes such as availability, location, or supplier.
- **Search History:** Stores and displays recent searches for quick access.
- **Check-in:** Warehouse managers can check in items from different product owners and store them in the warehouse area.

### 2. Count & Chat
- **Real-Time Messaging:** Enables instant communication between users.
- **User Authentication:** Ensures that only authorized users can access the chat.
- **Message Storage:** Stores chat history using Firebase for later retrieval.
- **Push Notifications:** Sends notifications to users about new messages, even when they are not in the app.
- **Count:** Tracks the number of days the items have been stored in the warehouse and allows other arithmetic operations.

### 3. Request & Notifications
- **Low Stock Alerts:** Notifies users when inventory levels drop below a predefined threshold.
- **Custom Notifications:** Allows users to create notifications for specific inventory items or actions.
- **Email/SMS Notifications:** Integrates with email or SMS services to send notifications outside the app.
- **Request Alerts:** The warehouse manager receives a notification once a product owner raises a request.
- **Request:** Users can make requests to get required items delivered from the warehouse to their address.

### 4. Tracking & Payment
- **Inventory Levels:** Tracks the number of items in stock in real-time.
- **Order Tracking:** Monitors the status of inventory requests and orders.
- **Historical Data:** Maintains records of inventory changes over time for analysis.
- **User Activity Tracking:** Tracks user interactions with inventory information.
- **Payment:** Displays the payment amount based on the number of items stored in the warehouse.

---

## Technologies Used

- **Android Studio:** Used for developing the mobile application interface.
- **Firebase:**
  - **Realtime Database:** Stores inventory, requests, and chat history.
  - **Authentication:** Manages secure user login and access control.
  - **Cloud Functions:** Triggers notifications based on inventory updates or requests.

------------

## Usage

### For Users:

#### Login/Registration:
- Use Firebase Authentication to register or log in to the app.

#### Inventory Search:
- Search for available items using the search bar.
- Apply filters like availability, location, or supplier to narrow down your search.

#### Request Products:
- After selecting the product, specify the required quantity and send a request to the warehouse manager.

#### Real-Time Messaging:
- Use the messaging feature to communicate with the warehouse manager.
- Get instant updates on your requests and orders.

#### Track Requests:
- View your request history and monitor the fulfillment status of your requests.

---------

### For Warehouse Managers:

#### Login:
- Log in using secure credentials to access the warehouse manager dashboard.

#### Check-In Items:
- Check in items to the warehouse from different product owners.

#### Manage Inventory:
- Add, edit, or delete inventory items.
- Adjust stock quantities based on incoming and outgoing items.

#### View & Process Requests:
- Approve, reject, or fulfill user requests based on stock availability.
- Send notifications to users about the status of their requests.

#### Track Inventory:
- Monitor inventory levels in real-time and ensure stock levels are maintained.

#### Manage Payments:
- Track payments based on the number of items stored in the warehouse and fulfill all related financial operations.


### Prerequisites:
- Android Studio (latest version)
- Firebase Account
- AWS Account (optional, for S3 and CloudWatch)
