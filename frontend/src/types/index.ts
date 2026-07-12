export type Role = 'USER' | 'ADMIN';
export type GuessResult = 'WIN' | 'LOSE';
export type PaymentProvider = 'DEMO' | 'VNPAY' | 'MOMO' | 'PAYPAL';
export type PaymentStatus = 'SUCCESS' | 'FAILED' | 'PENDING';

export interface User {
  id: number;
  username: string;
  email: string;
  score: number;
  turns: number;
  rank?: number;
  role: Role;
  createdAt?: string;
}

export interface AuthResponse {
  accessToken: string;
  tokenType: 'Bearer';
  expiresIn: number;
  user: User;
}

export interface LoginRequest {
  usernameOrEmail: string;
  password: string;
}

export interface RegisterRequest {
  username: string;
  email: string;
  password: string;
}

export interface GuessRequest {
  number: number;
}

export interface GuessResponse {
  guessedNumber: number;
  serverNumber: number;
  correct: boolean;
  result: GuessResult;
  message: string;
  score: number;
  remainingTurns: number;
  playedAt: string;
}

export interface LeaderboardItem {
  rank: number;
  userId: number;
  username: string;
  score: number;
  turns: number;
}

export interface LeaderboardResponse {
  items: LeaderboardItem[];
}

export interface GuessHistoryItem {
  id: number;
  guessedNumber: number;
  serverNumber: number;
  result: GuessResult;
  scoreAfter: number;
  turnsAfter: number;
  createdAt: string;
}

export interface PurchaseHistoryItem {
  id: number;
  turnsAdded: number;
  amount: number;
  provider: PaymentProvider;
  transactionCode: string;
  status: PaymentStatus;
  createdAt: string;
}

export interface BuyTurnsResponse {
  message: string;
  addedTurns: number;
  currentTurns: number;
  transactionCode: string;
  paymentUrl?: string | null;
}

export interface BuyTurnsRequest {
  provider: Extract<PaymentProvider, 'DEMO' | 'VNPAY'>;
}

export interface PaginatedResponse<T> {
  content: T[];
  page: number;
  size: number;
  totalElements: number;
  totalPages: number;
  last: boolean;
}

export interface FieldError {
  field: string;
  message: string;
}

export interface ApiError {
  timestamp?: string;
  status: number;
  message: string;
  path?: string;
  fieldErrors?: FieldError[];
}
