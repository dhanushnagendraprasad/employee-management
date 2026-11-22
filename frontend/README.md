# Employee Management System — Frontend

A modern Single Page Application (SPA) built with React 19, TypeScript, and Vite, featuring a glassmorphism design system and full integration with the Spring Boot REST API.

---

## 🛠️ Tech Stack & Dependencies

* **Framework:** React 19 (`react`, `react-dom`)
* **Language:** TypeScript 5
* **Build Tool & Dev Server:** Vite 8
* **Routing:** React Router v7 (`react-router-dom`)
* **State Management:** Zustand (Client-side authentication & user session persistence)
* **HTTP Client:** Axios with JWT interceptors
* **Icons:** Lucide React (`lucide-react`)

---

## 📂 Architecture & Directory Structure

```text
frontend/src/
├── assets/          # Static logos and hero illustrations
├── components/      # Reusable UI modals (EmployeeModal, ConfirmModal, HikeModal)
├── pages/           # Application route pages
│   ├── Login.tsx       # JWT login with animated feedback
│   ├── Signup.tsx      # Account registration with role selection
│   └── Dashboard.tsx   # Employee directory grid, filters, pagination, and search
├── services/        # API communication services
│   ├── api.ts             # Axios instance configured with base URL
│   ├── authService.ts     # Auth endpoints (/api/auth/login, /api/auth/register)
│   └── employeeService.ts # Employee CRUD & search query handlers
├── store/           # Zustand state store
│   └── authStore.ts       # Authentication tokens and decoded role state
├── App.tsx          # Root routing and ProtectedRoute guard wrappers
└── main.tsx         # React application entry point
```

---

## 🚀 Local Development

```powershell
# Install dependencies
npm install

# Start Vite dev server (defaults to http://localhost:5173)
npm run dev

# Type-check and build production distribution package
npm run build

# Run linter
npm run lint
```
