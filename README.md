# Full-Stack HR Appraisal System

This project is a modern, comprehensive Human Resources Appraisal System designed to manage user performance reviews, goal tracking, and organizational feedback workflows.

The application is built using a **Spring Boot** backend and a **React/TypeScript (Vite)** frontend.

## 🛠️ Getting Started

To run the application locally, you will need to open **two separate terminal windows**—one for the backend API and one for the frontend UI.

### 1. Running the Backend (Spring Boot API)
The backend runs a Java Spring Boot server on port `8080`.

1. Open your terminal and navigate to the backend directory:
   ```bash
   cd "m:\Projects\Game\Spring Boot\appraisal\appraisal"
   ```
2. Start the application using the Maven wrapper:
   ```bash
   .\mvnw.cmd spring-boot:run
   ```
*Wait for the console output to display `Started AppraisalApplication`.*

### 2. Running the Frontend (React + Vite)
The frontend runs a Vite development server typically on port `5173` (or `5174` if `5173` is busy).

1. Open a **second, separate terminal** and navigate to the frontend directory:
   ```bash
   cd "m:\Projects\Game\Spring Boot\appraisal\frontend\frontend"
   ```
2. Start the Vite server:
   ```bash
   npm run dev
   ```

3. Open your web browser and navigate to the URL provided in the terminal (usually `http://localhost:5173`).

---

## 🔑 Authentication & Login Information

The application utilizes Role-Based Access Control (RBAC). Upon fresh deployment, a database seeder automatically creates starting accounts with different access roles.

You can log in to test the application using any of the following accounts:

| Role | Email | Password | What they can do |
| :--- | :--- | :--- | :--- |
| **HR Admin** | `hr@company.com` | `admin123` | Can create appraisals, acknowledge reports, manage goals, and create new users. |
| **Manager** | `manager@company.com` | `admin123` | Can view team appraisals, complete manager reviews, and assign goals. |
| **Employee** | `employee@company.com` | `admin123` | Can view their own appraisals, perform self-assessments, and update goal progress. |

---

## ⚙️ How the Project Works

### Backend (Spring Boot)
- **Security**: Secured using JSON Web Tokens (JWT). All subsequent API requests from the frontend pass an `Authorization: Bearer <token>` header to authenticate the active user session.
- **Database**: Connects to a MySQL database (configured in `application.properties`). If configured with `spring.jpa.hibernate.ddl-auto=update`, the schema is managed automatically.
- **Controllers**: Handle logic for `Appraisals`, `Goals`, `Feedback`, `Notifications`, and `Users`.
- **AuthUtils Integration**: Seamlessly retrieves the logged-in user entity instance based on the submitted security token so that backend components do not have to retrieve users blindly, eliminating unauthenticated state crashes.

### Frontend (React + Vite)
- **State & Routing**: Uses modern React functional components with customized hooks and routing pipelines. 
- **API Proxy**: The frontend includes API proxying set up in `vite.config.ts`. All fetch/axios requests made to `/api/*` are automatically routed directly to backend `http://localhost:8080`, bypassing any CORS errors.
- **Dashboards**: The application dynamically determines layout and capabilities displayed based on the JWT token payload role.

## Important Note regarding Terminals

If you ever encounter a `Port 8080 is already in use` error when starting the Spring Boot backend, it means a previous instance is still running in the background. Close the previous terminal or kill the process running on that port to start it freshly.
