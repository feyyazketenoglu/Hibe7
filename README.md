# Hibe7 - Yeditepe University Item Sharing Platform

## 1. Project Charter
### 1.1. Project Name
Hibe7

### 1.2. Core Problem
Students at Yeditepe University frequently discard usable items such as textbooks, electronics, and furniture when moving out of dormitories or graduating. Currently, there is no centralized, trusted platform within the campus network to facilitate the donation of these items to other students who are in need of them. 

### 1.3. Primary Target Audience
The application targets Yeditepe University students, specifically:
* Students residing in dormitories or student housing who need to declutter.
* Incoming students seeking academic materials or household items without cost.
* Environmentally conscious individuals aiming to support sustainability. 

### 1.4. Primary Value Proposition
To provide a secure, localized digital platform that enables the student community to discover, request, and collect free items, thereby fostering a culture of sharing and reducing waste on campus. 

### 1.5. Success Criteria
The project will be considered successful upon the completion of a functional Android prototype that allows a user to:
* Register and authenticate as a verified student.
* Create a new listing with an image and description.
* Browse the feed of available donations.
* Initiate a request for an item and communicate with the donor. 

---

## 2. User Stories & Prioritization (MoSCoW)
The following features have been extracted from the project requirements and prioritized according to the MoSCoW method. 

### Must Have (Critical for MVP)
* **Authentication:** As a student, I want to log in to the application to ensure a secure and trusted environment.
* **Home Feed:** As a user, I want to view a list of available donation items to discover relevant products.
* **Add Product:** As a donor, I want to upload product photos and details via the "Share" interface to list an item for donation.
* **Demand System:** As a receiver, I want to request an item explicitly so that the donor is notified of my interest.
* **Profile Management:** As a user, I want to view my listed items and the status of my requests.

### Should Have (Important but not Vital)
* **In-App Chat:** As a user, I want to communicate with the counterparty to arrange a physical meeting point on campus.
* **Filtering:** As a user, I want to filter items by categories (e.g., Books, Electronics, Furniture) to locate specific items efficiently.

### Could Have (Desirable)
* **Notifications:** As a user, I want to receive push notifications when my request is approved.
* **User Ratings:** As a user, I want to rate the transaction experience.

---

## 3. Technical Specifications (SDD Summary)
This section outlines the technical architecture based on the Software Design Document guidelines. 

### 3.1. System Architecture
* **Architectural Pattern:** MVVM (Model-View-ViewModel)
    * *Justification:* Selected to ensure separation of concerns, enabling easier testing and maintenance of the UI and business logic independently. 

### 3.2. Technology Stack
* **Programming Language:** Kotlin
* **UI Toolkit:** Jetpack Compose (Modern Android Development)
* **Minimum SDK:** API 24 (Android 7.0)
* **Data Source:**
    * **Remote:** Firebase Firestore (for real-time data synchronization of products and chat).
    * **Authentication:** Firebase Auth.
* **Image Loading Library:** Coil

### 3.3. High-Level System Design
The application is structured into the following logical modules based on the user flow:
1.  **Auth Module:** Handles Login and Sign-Up processes.
2.  **Home Module:** Displays the main feed of donations.
3.  **Share Module:** Manages the product creation flow (Camera/Gallery integration).
4.  **Demands Module:** Tracks outgoing requests and incoming approvals.
5.  **Profile Module:** Manages user settings and listing history.

---

## 4. Development Team
* **Ömer Faruk** - Android Developer
* **Feyyaz** - Android Developer
* **Ares** - Android Developer
