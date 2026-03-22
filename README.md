# Android Project
## Student Information
Name: Sándor Hunor

Student ID: 95

Course: Android Development

## Project Title
Name: DriveCheck-Client-and-Server
## Description
Project Overview: DriveCheck System
The DriveCheck suite is a professional fleet management and trip logging system consisting of two interconnected Android applications: a Server for administrative control and data persistence, and a Client for driver interactions.

1. DriveCheck Server (Administrator App)
   
-The Server application acts as the central hub for the entire system, managing the database and handling remote requests.

*Database Management: Utilizes a Room database to store trip logs (Uts), drivers (Sofors), and the company vehicle fleet (Autos).

*Socket Server: Runs a background service on port 8080 to listen for incoming Client connections and process data synchronized via JSON.

*Request Handling: Features a dedicated interface to review, approve, or manage incoming trip requests from drivers.

*Admin Tools: Provides advanced operations such as database resets, test data generation, and manual management of the vehicle fleet.

2. DriveCheck Client (Driver App)

-The Client application is designed for drivers to easily submit reports and check vehicle availability in real-time.

*Trip Reporting: Drivers can submit detailed trip logs including distance, fuel consumption, and cost directly to the server.

*Fleet Status: A tabbed interface allows drivers to browse the current status of the fleet, separated into "Available" and "Occupied" vehicles.

*Connection Monitoring: Includes a real-time status checker that monitors the server's availability based on its IP address.

*Auto-Suggest Integration: Features location auto-suggestion using OpenStreetMap (OSM) to simplify address entry.

Technical Stack:

Language: Java

Communication: Raw TCP Sockets with JSON serialization (Gson)

Persistence: Room Persistence Library (SQLite)

UI Components: Material Design, ViewPager2, and TabLayout

## Features

### Driver Client App
- **Real-time Server Connection Monitoring**: Integrated status checker to verify server availability via IP/Socket connection.
- **Trip Request Submission**: Drivers can record and send detailed trip logs, including distance, fuel consumption, and calculated costs.
- **Live Fleet Status**: Dynamic, tab-based view to browse the current availability of the vehicle park ("Available" vs. "Occupied").
- **Smart Location Suggestions**: Integration with OpenStreetMap (OSM) for automated address and location search.
- **Trip History**: View personal list of submitted trip requests and their current status.

### Administrator Server App
- **Centralized Command Center**: Manages the core logic and serves as the data bridge between drivers and the database.
- **Multi-threaded Socket Server**: High-performance TCP server handling concurrent requests from multiple clients.
- **Vehicle Fleet Management**: Dedicated Room database for persistent storage and management of company cars.
- **Advanced Admin Panel**: Specialized tools for administrators to register new drivers/vehicles, manage records, and generate test data.
- **Data Persistence**: Robust storage system for all trip logs, categorized by approval status.

## Screenshots

<img width="448" height="940" alt="image" src="https://github.com/user-attachments/assets/16f940db-9f3a-40a9-957d-e7891bd48b2c" />
<img width="448" height="940" alt="image" src="https://github.com/user-attachments/assets/573f0d4d-4fbc-4da9-9ff3-ead56f5a8eff" />

## Technologies Used
- **Language**: Java (Primary development language).
- **Database**: **Room Persistence Library** (SQLite abstraction) for local data storage.
- **Networking**: **Raw TCP Sockets** for fast, bi-directional communication between Client and Server.
- **Data Interchange**: **Gson** for efficient JSON serialization and deserialization.
- **UI Components**: 
    - **Material Design 3** for modern interface elements.
    - **ViewPager2 & TabLayout** for intuitive navigation.
    - **RecyclerView** with custom Adapters for efficient list rendering.
- **Concurrency**: **ExecutorService & Handlers** for non-blocking background operations.

## Project Structure
- **DriveCheck-Szerver**: The administrative backend and database manager.
- **DriveCheck-Kliens**: The driver-facing frontend application.
- **Shared Models**: Unified data structures (Auto, Sofor, Ut) for seamless data transfer.

## How to Run
1. **Clone the repository**: `git clone [repository-url]`
2. **Open with Android Studio**: Import both the `DriveCheck-Szerver` and `DriveCheck-Kliens` projects.
3. **Start the Server**: 
    - Launch the Server app on an emulator.
    - Note the IP address displayed on the main screen and press "START SERVER".
4. **Launch the Client**:
    - Launch the Client app.
    - Enter the Server's IP address to establish a connection.
5. **Run on emulator or device**: Ensure both devices are on the same local network for socket communication.
