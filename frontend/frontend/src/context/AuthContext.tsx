import React, { createContext, useContext, useState, useEffect } from "react";
import type { UserResponse, LoginResponse } from "../api/types";

interface AuthContextType {
  user: UserResponse | null;
  token: string | null;
  login: (data: LoginResponse) => void;
  logout: () => void;
  isAuthenticated: boolean;
  loading: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [user, setUser] = useState<UserResponse | null>(null);
  const [token, setToken] = useState<string | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const savedToken = localStorage.getItem("token");
    const savedUser = localStorage.getItem("user");
    if (savedToken && savedUser) {
      setToken(savedToken);
      setUser(JSON.parse(savedUser));
    }
    setLoading(false);
  }, []);

  const login = (data: LoginResponse) => {
    localStorage.setItem("token", data.token);
    const userObj: UserResponse = {
      id: data.userId,
      email: data.email,
      fullName: data.fullName,
      role: data.role,
      departmentName: null,
      departmentId: null,
      managerName: null,
      managerId: null,
      createdAt: new Date().toISOString(),
    };
    localStorage.setItem("user", JSON.stringify(userObj));
    setToken(data.token);
    setUser(userObj);
  };

  const logout = () => {
    localStorage.removeItem("token");
    localStorage.removeItem("user");
    setToken(null);
    setUser(null);
  };

  return (
    <AuthContext.Provider
      value={{
        user,
        token,
        login,
        logout,
        isAuthenticated: !!token,
        loading,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) throw new Error("useAuth must be used within an AuthProvider");
  return context;
};
