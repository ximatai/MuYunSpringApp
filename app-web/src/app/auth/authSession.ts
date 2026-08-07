import { createAuthClient, createHttpClient, normalizeError, userFacingErrorMessage } from '@ximatai/muyun-web-app';

const tokenKey = 'muyun.app.token';
const authClient = createAuthClient(createHttpClient());

export interface AuthSession {
  token: string;
  passwordChangeRequired: boolean;
}

export function restoreAuthSession(): AuthSession | undefined {
  const token = localStorage.getItem(tokenKey);
  return token ? { token, passwordChangeRequired: false } : undefined;
}

export async function signIn(username: string, password: string): Promise<AuthSession> {
  const result = await authClient.login({ username, password });
  const session = { token: result.token, passwordChangeRequired: result.passwordChangeRequired === true };
  localStorage.setItem(tokenKey, session.token);
  return session;
}

export function authorizationOf(session: AuthSession) {
  return `Bearer ${session.token}`;
}

export async function changeOwnPassword(session: AuthSession, currentPassword: string, newPassword: string) {
  await authClient.changeOwnPassword({ currentPassword, newPassword }, session.token);
  localStorage.removeItem(tokenKey);
}

export function authErrorMessage(cause: unknown) {
  return userFacingErrorMessage(normalizeError(cause));
}

export function isAuthenticationFailure(cause: unknown) {
  const error = normalizeError(cause);
  return error.code === 'AUTH_REQUIRED' || error.code === 'AUTH_EXPIRED';
}
