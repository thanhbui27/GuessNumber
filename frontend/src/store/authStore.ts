import { create } from 'zustand';
import type { AuthResponse, User } from '../types';

const TOKEN_KEY = 'guess_number_token';

interface AuthState {
  accessToken: string | null;
  currentUser: User | null;
  isAuthenticated: boolean;
  setAuth: (response: AuthResponse) => void;
  updateUser: (user: User) => void;
  logout: () => void;
}

const initialToken = localStorage.getItem(TOKEN_KEY);

export const useAuthStore = create<AuthState>((set) => ({
  accessToken: initialToken,
  currentUser: null,
  isAuthenticated: Boolean(initialToken),
  setAuth: (response) => {
    localStorage.setItem(TOKEN_KEY, response.accessToken);
    set({ accessToken: response.accessToken, currentUser: response.user, isAuthenticated: true });
  },
  updateUser: (user) => set({ currentUser: user, isAuthenticated: true }),
  logout: () => {
    localStorage.removeItem(TOKEN_KEY);
    set({ accessToken: null, currentUser: null, isAuthenticated: false });
  }
}));
