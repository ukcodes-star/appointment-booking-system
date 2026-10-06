import React from 'react';
import { useAuth } from '../context/AuthContext';
import { Calendar, User, LogOut, Briefcase } from 'lucide-react';

interface NavbarProps {
  currentView: string;
  setCurrentView: (view: string) => void;
}

export const Navbar: React.FC<NavbarProps> = ({ currentView, setCurrentView }) => {
  const { role, email, logout, isAuthenticated } = useAuth();

  return (
    <nav className="bg-white border-b border-slate-200 sticky top-0 z-50 shadow-xs">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 h-16 flex items-center justify-between">
        <div className="flex items-center gap-2 cursor-pointer" onClick={() => setCurrentView('home')}>
          <div className="w-10 h-10 rounded-xl bg-indigo-600 flex items-center justify-center text-white shadow-md shadow-indigo-200">
            <Calendar className="w-6 h-6" />
          </div>
          <div>
            <span className="text-xl font-bold text-slate-900 tracking-tight">SlotBook</span>
            <span className="text-xs ml-2 px-2 py-0.5 rounded-full bg-indigo-50 text-indigo-700 font-semibold border border-indigo-200">
              Assessment Demo
            </span>
          </div>
        </div>

        <div className="flex items-center gap-4">
          {isAuthenticated ? (
            <>
              {role === 'SERVICE_PROVIDER' ? (
                <button
                  onClick={() => setCurrentView('provider')}
                  className={`px-3 py-1.5 rounded-lg text-sm font-medium transition ${
                    currentView === 'provider' ? 'bg-indigo-600 text-white' : 'text-slate-700 hover:bg-slate-100'
                  }`}
                >
                  <Briefcase className="w-4 h-4 inline mr-1.5" />
                  Provider Portal
                </button>
              ) : (
                <button
                  onClick={() => setCurrentView('user')}
                  className={`px-3 py-1.5 rounded-lg text-sm font-medium transition ${
                    currentView === 'user' ? 'bg-indigo-600 text-white' : 'text-slate-700 hover:bg-slate-100'
                  }`}
                >
                  <User className="w-4 h-4 inline mr-1.5" />
                  Browse & Book
                </button>
              )}

              <div className="flex items-center gap-2 border-l border-slate-200 pl-4">
                <span className="text-xs px-2.5 py-1 rounded-full font-semibold bg-emerald-50 text-emerald-700 border border-emerald-200">
                  {role}
                </span>
                <span className="text-xs text-slate-500 hidden sm:inline">{email}</span>
                <button
                  onClick={logout}
                  className="p-1.5 text-slate-500 hover:text-red-600 hover:bg-red-50 rounded-lg transition"
                  title="Logout"
                >
                  <LogOut className="w-4 h-4" />
                </button>
              </div>
            </>
          ) : (
            <div className="flex items-center gap-2">
              <button
                onClick={() => setCurrentView('login')}
                className="px-3.5 py-1.5 text-sm font-medium text-slate-700 hover:text-indigo-600 transition"
              >
                Sign In
              </button>
              <button
                onClick={() => setCurrentView('register')}
                className="px-3.5 py-1.5 text-sm font-medium text-white bg-indigo-600 hover:bg-indigo-700 rounded-lg shadow-xs transition"
              >
                Register
              </button>
            </div>
          )}
        </div>
      </div>
    </nav>
  );
};