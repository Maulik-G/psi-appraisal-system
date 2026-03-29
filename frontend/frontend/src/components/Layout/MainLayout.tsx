import React from "react";
import { Outlet, NavLink, useNavigate } from "react-router-dom";
import { useAuth } from "../../context/AuthContext";

const MainLayout: React.FC = () => {
  const { user, logout } = useAuth();
  const navigate = useNavigate();

  const handleLogout = () => {
    logout();
    navigate("/login");
  };

  return (
    <div className="layout-container">
      <aside className="sidebar">
        <div className="sidebar-logo">AppraisalPro</div>
        <nav className="sidebar-nav">
          <NavLink to="/dashboard" className={({ isActive }) => `nav-link ${isActive ? "active" : ""}`}>
            <span>Dashboard</span>
          </NavLink>
          
          <NavLink to="/appraisals" className={({ isActive }) => `nav-link ${isActive ? "active" : ""}`}>
            <span>Appraisals</span>
          </NavLink>
          
          <NavLink to="/goals" className={({ isActive }) => `nav-link ${isActive ? "active" : ""}`}>
            <span>Goals</span>
          </NavLink>

          {user?.role === "HR" && (
            <>
              <div className="subtitle" style={{ padding: "1rem 1rem 0.5rem 1rem" }}>Management</div>
              <NavLink to="/users" className={({ isActive }) => `nav-link ${isActive ? "active" : ""}`}>
                <span>Users</span>
              </NavLink>
              <NavLink to="/departments" className={({ isActive }) => `nav-link ${isActive ? "active" : ""}`}>
                <span>Departments</span>
              </NavLink>
            </>
          )}

          <div className="subtitle" style={{ padding: "1rem 1rem 0.5rem 1rem" }}>Account</div>
          <NavLink to="/notifications" className={({ isActive }) => `nav-link ${isActive ? "active" : ""}`}>
            <span>Notifications</span>
          </NavLink>
        </nav>
        
        <div className="sidebar-footer">
          <div className="user-info mb-4">
            <div style={{ fontWeight: 600 }}>{user?.fullName}</div>
            <div className="subtitle" style={{ fontSize: '10px' }}>{user?.role}</div>
          </div>
          <button onClick={handleLogout} className="btn btn-secondary" style={{ width: '100%' }}>
            Logout
          </button>
        </div>
      </aside>

      <main className="main-content">
        <header className="flex justify-between items-center mb-4">
          <h1 style={{ margin: 0 }}>{window.location.pathname.split("/").pop()?.toUpperCase() || "DASHBOARD"}</h1>
          <div className="glass-card" style={{ padding: '0.5rem 1rem', borderRadius: '10px' }}>
            {new Date().toLocaleDateString(undefined, { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' })}
          </div>
        </header>
        <div className="animate-fade">
          <Outlet />
        </div>
      </main>
    </div>
  );
};

export default MainLayout;
