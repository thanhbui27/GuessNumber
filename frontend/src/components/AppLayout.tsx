import { LogOut, Trophy } from 'lucide-react';
import { Outlet } from 'react-router-dom';
import { useAuthStore } from '../store/authStore';

export function AppLayout() {
  const user = useAuthStore((state) => state.currentUser);
  const logout = useAuthStore((state) => state.logout);

  return (
    <div className="min-h-screen bg-slate-100 text-slate-950">
      <header className="border-b border-slate-200 bg-white">
        <div className="mx-auto flex max-w-7xl items-center justify-between px-4 py-4 sm:px-6 lg:px-8">
          <div className="flex items-center gap-3">
            <span className="grid h-10 w-10 place-items-center rounded-lg bg-emerald-600 text-white">
              <Trophy className="h-5 w-5" aria-hidden="true" />
            </span>
            <div>
              <p className="text-lg font-bold">Guess Number Game</p>
              <p className="text-xs text-slate-500">Doan so 1 den 5</p>
            </div>
          </div>
          <div className="flex items-center gap-3">
            {user ? <span className="hidden text-sm font-medium text-slate-700 sm:inline">{user.username}</span> : null}
            <button
              type="button"
              onClick={logout}
              className="grid h-10 w-10 place-items-center rounded-lg border border-slate-300 bg-white text-slate-700 focus:outline-none focus:ring-2 focus:ring-emerald-500"
              aria-label="Dang xuat"
              title="Dang xuat"
            >
              <LogOut className="h-4 w-4" aria-hidden="true" />
            </button>
          </div>
        </div>
      </header>
      <main className="mx-auto max-w-7xl px-4 py-6 sm:px-6 lg:px-8">
        <Outlet />
      </main>
    </div>
  );
}
