// User related interfaces
export interface UserRegistrationDto {
  username: string;
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  phoneNumber?: string;
  address?: string;
  dateOfBirth?: string; // YYYY-MM-DD format
}

export interface UserResponseDto {
  id: number;
  username: string;
  email: string;
  firstName: string;
  lastName: string;
  phoneNumber?: string;
  address?: string;
  dateOfBirth?: string;
  role: string;
  isActive: boolean;
  isVerified: boolean;
  createdAt: string; // ISO date string
}

export interface LoginDto {
  username: string;
  password: string;
}

export interface JwtResponseDto {
  token: string;
  type: string; // "Bearer"
  username: string;
  email: string;
  role: string;
  userId: number;
}

// Account related interfaces
export interface CreateAccountDto {
  userId: number;
  accountType: AccountType;
  dailyLimit: number;
}

export interface AccountDto {
  id: number;
  accountNumber: string;
  accountType: AccountType;
  balance: number;
  availableBalance: number;
  dailyLimit: number;
  isActive: boolean;
  isFrozen: boolean;
  userId: number;
  userName: string;
  createdAt: string; // ISO date string
}

// Transaction related interfaces
export interface TransactionDto {
  id: number;
  transactionReference: string;
  transactionType: TransactionType;
  amount: number;
  description?: string;
  status: TransactionStatus;
  fromAccountId?: number;
  fromAccountNumber?: string;
  toAccountId?: number;
  toAccountNumber?: string;
  balanceBefore: number;
  balanceAfter: number;
  failureReason?: string;
  createdAt: string; // ISO date string
  processedAt?: string; // ISO date string
}

export interface TransferDto {
  fromAccountNumber: string;
  toAccountNumber: string;
  amount: number;
  description?: string;
}

export interface DepositDto {
  accountId: number;
  amount: number;
  description?: string;
}

export interface WithdrawalDto {
  accountId: number;
  amount: number;
  description?: string;
}

// API Response interfaces
export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

export interface ErrorResponseDto {
  status: number;
  error: string;
  message: string;
  details?: string[];
  path: string;
  timestamp: string;
}

// Enums
export enum AccountType {
  SAVINGS = "SAVINGS",
  CHECKING = "CHECKING",
  BUSINESS = "BUSINESS",
  FIXED_DEPOSIT = "FIXED_DEPOSIT",
}

export enum TransactionType {
  DEPOSIT = "DEPOSIT",
  WITHDRAWAL = "WITHDRAWAL",
  TRANSFER = "TRANSFER",
  TRANSFER_IN = "TRANSFER_IN",
  TRANSFER_OUT = "TRANSFER_OUT",
}

export enum TransactionStatus {
  PENDING = "PENDING",
  COMPLETED = "COMPLETED",
  FAILED = "FAILED",
  CANCELLED = "CANCELLED",
}

export enum UserRole {
  USER = "USER",
  ADMIN = "ADMIN",
  MANAGER = "MANAGER",
}

// Additional utility interfaces for frontend use
export interface LoginCredentials {
  username: string;
  password: string;
  rememberMe?: boolean;
}

export interface UserSession {
  user: UserResponseDto;
  token: string;
  expiresAt: string;
}

export interface TransactionFilter {
  accountId?: number;
  transactionType?: TransactionType;
  status?: TransactionStatus;
  fromDate?: string;
  toDate?: string;
  minAmount?: number;
  maxAmount?: number;
  page?: number;
  size?: number;
}

export interface AccountBalance {
  accountId: number;
  accountNumber: string;
  balance: number;
  availableBalance: number;
  lastUpdated: string;
}

export interface DashboardData {
  user: UserResponseDto;
  accounts: AccountDto[];
  recentTransactions: TransactionDto[];
  totalBalance: number;
  monthlySpending: number;
  monthlyIncome: number;
}

// Form validation interfaces
export interface ValidationError {
  field: string;
  message: string;
}

export interface FormErrors {
  [key: string]: string | undefined;
}

// Pagination interface
export interface PagedResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
  first: boolean;
  last: boolean;
}

// HTTP client response type
export interface HttpResponse<T> {
  data: T;
  status: number;
  statusText: string;
  headers: any;
}

// Chart data interfaces for dashboard
export interface ChartData {
  labels: string[];
  datasets: {
    label: string;
    data: number[];
    backgroundColor?: string[];
    borderColor?: string;
    borderWidth?: number;
  }[];
}

export interface TransactionSummary {
  month: string;
  income: number;
  expenses: number;
  balance: number;
}
