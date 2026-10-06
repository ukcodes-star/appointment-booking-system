import React, { createContext, useContext, useState } from 'react';

interface AuthContextType {
  token: string | null;
  role: 'USER' | 'SERVICE_PROVIDER' | null;
  email: string | null;
  login: (token: string) => void;
  logout: () => void;
  isAuthenticated: boolean;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

function parseJwt(t: string): { sub?: string; role?: 'USER' | 'SERVICE_PROVIDER' } | null {
  try {
    const base64Url = t.split('.')[1];
    const base64 = base64Url.replace(/-/g, '+').replace(/_/g, '/');
    return JSON.parse(window.atob(base64));
  } catch {
    return null;
  }
}

interface AuthState {
  token: string | null;
  role: 'USER' | 'SERVICE_PROVIDER' | null;
  email: string | null;
}

function getInitialState(): AuthState {
  const token = localStorage.getItem('token');
  if (!token) return { token: null, role: null, email: null };
  const payload = parseJwt(token);
  if (payload?.role) {
    return { token, role: payload.role, email: payload.sub || null };
  }
  localStorage.removeItem('token');
  return { token: null, role: null, email: null };
}

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [auth, setAuth] = useState<AuthState>(getInitialState);

  const login = (newToken: string) => {
    localStorage.setItem('token', newToken);
    const payload = parseJwt(newToken);
    setAuth({
      token: newToken,
      role: payload?.role || null,
      email: payload?.sub || null,
    });
  };

  const logout = () => {
    localStorage.removeItem('token');
    setAuth({ token: null, role: null, email: null });
  };

  return (
    <AuthContext.Provider
      value={{
        token: auth.token,
        role: auth.role,
        email: auth.email,
        login,
        logout,
        isAuthenticated: !!auth.token,
      }}
    >
      {children}
    </AuthContext.Provider>
  );
};

// eslint-disable-next-line react-refresh/only-export-components
export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) throw new Error('useAuth must be used within AuthProvider');
  return context;
};