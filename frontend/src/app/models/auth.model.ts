export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
  phone: string;
  businessName: string;
}

export interface AuthResponse {
  accessToken: string;
  refreshToken: string;
  tokenType: string;
  user: UserInfo;
}

export interface UserInfo {
  id: string;
  email: string;
  firstName: string;
  lastName: string;
  phone: string;
  role: string;
  emailVerified: boolean;
  business?: BusinessInfo;
}

export interface BusinessInfo {
  id: string;
  businessName: string;
  slug: string;
  category: string | null;
}
