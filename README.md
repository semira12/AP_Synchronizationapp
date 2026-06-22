# AP_Synchronizationapp
# 🎬 SyncStream — Synchronized Video Streaming System
### Complete Integrated Project | 3-Person Team

---

## 📁 Project Structure

```
SyncStream/
│
├── pom.xml                             ← Maven build file
│
├── sql/
│   ├── 01_schema.sql                   ← Run FIRST — creates all 5 tables
│   ├── 02_seed_data.sql                ← Run SECOND — sample users/rooms
│   └── 03_queries.sql                  ← Analytics queries (reference)
│
├── src/main/
│   ├── java/
│   │   ├── module-info.java            ← Java module declaration
│   │   └── com/syncstream/
│   │       │
│   │       ├── ── PERSON 3 (Database) ──────────────────────────
│   │       │
│   │       ├── db/
│   │       │   └── DBConnection.java   ← MySQL singleton connection
│   │       │
│   │       ├── model/
│   │       │   ├── User.java           ← User POJO
│   │       │   ├── Room.java           ← Room POJO
│   │       │   ├── ChatMessage.java    ← ChatMessage POJO
│   │       │   └── WatchHistory.java   ← WatchHistory POJO
│   │       │
│   │       ├── dao/
│   │       │   ├── UserDAO.java        ← SQL for users table
│   │       │   ├── RoomDAO.java        ← SQL for rooms table
│   │       │   ├── ParticipantDAO.java ← SQL for participants table
│   │       │   ├── ChatMessageDAO.java ← SQL for chat_messages table
│   │       │   └── WatchHistoryDAO.java← SQL for watch_history table
│   │       │
│   │       ├── api/
│   │       │   └── DatabaseAPI.java    ← PUBLIC API used by Person 1 & 2
│   │       │
│   │       ├── util/
│   │       │   ├── PasswordUtil.java   ← SHA-256 password hashing
│   │       │   └── FileUtil.java       ← Export chat logs / session reports
│   │       │
│   │       ├── test/
│   │       │   └── TestSuite.java      ← 10 automated DB tests
│   │       │
│   │       ├── ── PERSON 2 (Server/Networking) ─────────────────
│   │       │
│   │       ├── server/
│   │       │   ├── SyncServer.java     ← Main TCP server (port 5050)
│   │       │   └── ClientHandler.java  ← One thread per connected client
│   │       │
│   │       ├── client/
│   │       │   └── ServerConnection.java ← Client-side socket wrapper
│   │       │
│   │       └── ── PERSON 1 (GUI/Frontend) ──────────────────────
│   │
│   │           ├── gui/
│   │           │   ├── MainApp.java         ← JavaFX entry point
│   │           │   ├── SessionState.java    ← Shared session data
│   │           │   ├── LoginController.java ← Login/Register screen
│   │           │   ├── LobbyController.java ← Create/Join room screen
│   │           │   └── RoomController.java  ← Video player + chat + sync
│   │
│   └── resources/
│       └── fxml/
│           ├── login.fxml              ← Login / Register UI layout
│           ├── lobby.fxml              ← Room browser UI layout
│           └── room.fxml               ← Watch room UI layout
│
├── reports/                            ← Auto-created: exported chat logs
└── lib/
    └── mysql-connector-j-9.7.0.jar     ← MySQL JDBC driver
```

---

## ⚙️ Prerequisites

| Tool        | Version  | Download |
|-------------|----------|----------|
| Java JDK    | 17+      | https://adoptium.net |
| Maven       | 3.8+     | https://maven.apache.org |
| MySQL       | 8.0+     | https://dev.mysql.com/downloads |
| JavaFX SDK  | 21+      | https://openjfx.io (auto via Maven) |

---

## 🗄️ Step 1 — Set Up the Database

Open MySQL Workbench or a terminal and run:

```sql
-- In MySQL terminal or Workbench:
source /full/path/to/SyncStream/sql/01_schema.sql
source /full/path/to/SyncStream/sql/02_seed_data.sql
```

This creates the `syncstream_db` database with these 5 tables:

| Table          | Description                          |
|----------------|--------------------------------------|
| users          | Registered user accounts             |
| rooms          | Video watch rooms                    |
| participants   | Who joined which room                |
| chat_messages  | All chat/reactions in rooms          |
| watch_history  | How long each user watched (analytics)|

---

## 🔧 Step 2 — Configure Database Credentials

Edit `src/main/java/com/syncstream/db/DBConnection.java`:

```java
private static final String URL      = "jdbc:mysql://localhost:3306/syncstream_db";
private static final String USER     = "root";          // ← your MySQL username
private static final String PASSWORD = "your_password"; // ← your MySQL password
```

---

## 🏗️ Step 3 — Build the Project

```bash
cd SyncStream
mvn clean compile
```

---

## 🚀 Step 4 — Run the Application

### You need TWO terminal windows:

**Terminal 1 — Start the Server:**
```bash
mvn exec:java -Dexec.mainClass="com.syncstream.server.SyncServer"
```
You should see:
```
===========================================
  SyncStream Server starting on port 5050
===========================================
[Server] Waiting for connections...
```

**Terminal 2 — Start the GUI (Client):**
```bash
mvn javafx:run
```

---

## 🧪 Step 5 — Run Tests (Optional)

```bash
mvn exec:java -Dexec.mainClass="com.syncstream.test.TestSuite"
```
Expected: 10 tests all PASS ✅

---

## 🎮 How to Use the Application

### Host (creates the room):
1. Launch the GUI → Enter server IP → Login
2. In the Lobby → Enter a room name → Browse for a video file → **Create Room**
3. Share the **Room Code** (shown in the top bar) with friends
4. Use **▶ Play** / **⏸ Pause** buttons to control playback for everyone
5. Chat and send reactions in real time

### Viewer (joins the room):
1. Launch the GUI → Enter the host's IP address → Login
2. In the Lobby → Enter the **Room Code** → **Join Room**
3. Video will sync automatically when the host plays/pauses/seeks
4. Chat and send reactions in real time

---

## 🔌 Communication Protocol

The server and clients communicate via plain TCP text messages:

| Client → Server       | Description                        |
|-----------------------|------------------------------------|
| `LOGIN:user:pass`     | Authenticate user                  |
| `REGISTER:user:email:pass` | Create new account            |
| `CREATE_ROOM:name:videoPath` | Create a room               |
| `JOIN_ROOM:code`      | Join room by short code            |
| `CHAT:message`        | Send a chat message                |
| `REACTION:emoji`      | Send a reaction                    |
| `PLAY:seconds`        | Host: broadcast play at timestamp  |
| `PAUSE:seconds`       | Host: broadcast pause              |
| `SEEK:seconds`        | Host: broadcast seek               |
| `LEAVE`               | Leave the current room             |

| Server → Client       | Description                        |
|-----------------------|------------------------------------|
| `OK:LOGIN:id:user`    | Login success                      |
| `OK:CREATE_ROOM:id:code` | Room created, here's the code  |
| `OK:JOIN_ROOM:id:name:path` | Joined successfully          |
| `ERROR:reason`        | Something went wrong               |
| `CHAT:user:message`   | Broadcast chat message             |
| `REACTION:user:emoji` | Broadcast reaction                 |
| `SYNC_PLAY:seconds`   | Play video at this position        |
| `SYNC_PAUSE:seconds`  | Pause video at this position       |
| `SYNC_SEEK:seconds`   | Seek to this position              |
| `USER_JOINED:user`    | Someone joined the room            |
| `USER_LEFT:user`      | Someone left the room              |

---

## 🧑‍💻 Team Responsibilities

| Person | Role         | Files |
|--------|--------------|-------|
| Person 1 | GUI / Frontend | `gui/`, `resources/fxml/` |
| Person 2 | Server / Networking | `server/`, `client/` |
| Person 3 | Database / Testing | `db/`, `model/`, `dao/`, `api/`, `util/`, `test/`, `sql/` |

---

## 🔐 Security Features
- Passwords hashed with SHA-256 before storage (never stored as plain text)
- Prepared statements prevent SQL injection
- Server validates all commands before processing

---

## 📦 Sample Credentials (from seed data)
After running `02_seed_data.sql`:

| Username | Password    |
|----------|-------------|
| alice    | password123 |
| bob      | password123 |
| charlie  | password123 |
