import React from "react";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import ProtectedRoute from "./ProtectedRoute";
import MainLayout from "../components/Layout/MainLayout";
import Login from "../pages/Login";
import Dashboard from "../pages/Dashboard";
import Users from "../pages/Users";
import Departments from "../pages/Departments";
import Appraisals from "../pages/Appraisals";
import AppraisalDetail from "../pages/AppraisalDetail";
import Goals from "../pages/Goals";
import Notifications from "../pages/Notifications";
import { AuthProvider } from "../context/AuthContext";

const AppRoutes: React.FC = () => {
  return (
    <BrowserRouter>
      <AuthProvider>
        <Routes>
          <Route path="/login" element={<Login />} />
          
          <Route element={<ProtectedRoute />}>
            <Route element={<MainLayout />}>
              <Route path="/dashboard" element={<Dashboard />} />
              <Route path="/appraisals" element={<Appraisals />} />
              <Route path="/appraisals/:id" element={<AppraisalDetail />} />
              <Route path="/goals" element={<Goals />} />
              <Route path="/notifications" element={<Notifications />} />
              
              {/* HR Only Routes */}
              <Route element={<ProtectedRoute roles={["HR"]} />}>
                <Route path="/users" element={<Users />} />
                <Route path="/departments" element={<Departments />} />
              </Route>
            </Route>
          </Route>

          <Route path="/" element={<Navigate to="/dashboard" replace />} />
          <Route path="*" element={<Navigate to="/dashboard" replace />} />
        </Routes>
      </AuthProvider>
    </BrowserRouter>
  );
};

export default AppRoutes;