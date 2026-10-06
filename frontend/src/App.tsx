import React, { useState } from 'react';
import { AuthProvider, useAuth } from './context/AuthContext';
import { Navbar } from './components/Navbar';
import { UserDashboard } from './pages/UserDashboard';
import { ProviderDashboard } from './pages/ProviderDashboard';
import { Login } from './pages/Login';
import { Register } from './pages/Register';

const MainContent: React.FC = () => {
  const { role, isAuthenticated } = useAuth();
  const [currentView, setCurrentView] = useState<string>('home');

  return (
    <div className="min-h-screen flex flex-col bg-slate-50">
      <Navbar currentView={currentView} setCurrentView={setCurrentView} />

      <main className="flex-1">
        {currentView === 'login' && (
          <Login onDone={() => setCurrentView(role === 'SERVICE_PROVIDER' ? 'provider' : 'user')} onSwitchRegister={() => setCurrentView('register')} />
        )}
        {currentView === 'register' && (
          <Register onSwitchLogin={() => setCurrentView('login')} />
        )}
        {(currentView === 'home' || currentView === 'user') && (
          <UserDashboard />
        )}
        {currentView === 'provider' && (
          isAuthenticated && role === 'SERVICE_PROVIDER' ? (
            <ProviderDashboard />
          ) : (
            <div className="p-12 text-center text-sm text-red-600">Access denied. Please log in as a Service Provider.</div>
          )
        )}
      </main>
    </div>
  );
};

export default function App() {
  return (
    <AuthProvider>
      <MainContent />
    </AuthProvider>
  );
}