import { axiosClient } from './axiosClient';
import type {
  AuthResponse,
  GuessHistoryItem,
  GuessRequest,
  GuessResponse,
  LeaderboardResponse,
  LoginRequest,
  PaginatedResponse,
  PurchaseHistoryItem,
  RegisterRequest,
  User
} from '../types';

export const api = {
  register: async (payload: RegisterRequest) => {
    const { data } = await axiosClient.post<AuthResponse>('/auth/register', payload);
    return data;
  },
  login: async (payload: LoginRequest) => {
    const { data } = await axiosClient.post<AuthResponse>('/auth/login', payload);
    return data;
  },
  me: async () => {
    const { data } = await axiosClient.get<User>('/users/me');
    return data;
  },
  leaderboard: async () => {
    const { data } = await axiosClient.get<LeaderboardResponse>('/leaderboard');
    return data;
  },
  guess: async (payload: GuessRequest) => {
    const { data } = await axiosClient.post<GuessResponse>('/game/guess', payload);
    return data;
  },
  buyTurns: async () => {
    const { data } = await axiosClient.post<{ message: string; addedTurns: number; currentTurns: number; transactionCode: string }>(
      '/game/buy-turns'
    );
    return data;
  },
  guessHistory: async (page: number, size: number) => {
    const { data } = await axiosClient.get<PaginatedResponse<GuessHistoryItem>>('/game/history', {
      params: { page, size }
    });
    return data;
  },
  purchaseHistory: async (page: number, size: number) => {
    const { data } = await axiosClient.get<PaginatedResponse<PurchaseHistoryItem>>('/game/purchase-history', {
      params: { page, size }
    });
    return data;
  }
};
