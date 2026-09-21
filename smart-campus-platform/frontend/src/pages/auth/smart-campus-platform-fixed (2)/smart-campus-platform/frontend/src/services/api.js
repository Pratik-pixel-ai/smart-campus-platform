import axios from 'axios';

const TOKEN_KEY = 'smartcampus.token';

/**
 * One axios instance for the whole app.
 * The request interceptor attaches the JWT; the response interceptor sends the user
 * back to the login page when the token is missing or has expired.
 */
const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  headers: { 'Content-Type': 'application/json' },
});

export const tokenStore = {
  get: () => localStorage.getItem(TOKEN_KEY),
  set: (token) => localStorage.setItem(TOKEN_KEY, token),
  clear: () => localStorage.removeItem(TOKEN_KEY),
};

api.interceptors.request.use((config) => {
  const token = tokenStore.get();
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response?.status;
    const onLoginPage = window.location.pathname.startsWith('/login');
    if (status === 401 && !onLoginPage) {
      tokenStore.clear();
      window.location.href = '/login';
    }
    return Promise.reject(error);
  },
);

/**
 * Turns any axios failure into the message the API sent, or a readable fallback.
 * When the server sent nothing useful we say *why* (unreachable vs. server error) instead
 * of hiding it behind a generic sentence - that is what makes "backend is down" obvious.
 */
export function errorMessage(error, fallback = 'Something went wrong. Please try again.') {
  const data = error?.response?.data;
  if (data?.fieldErrors) {
    const first = Object.values(data.fieldErrors)[0];
    if (first) return first;
  }
  if (data?.message) return data.message;

  if (error?.isAxiosError) {
    if (!error.response) {
      return 'Cannot reach the server. Check that the backend is running and try again.';
    }
    // A dev proxy or reverse proxy answers 5xx with an empty body when the backend is down.
    if (error.response.status >= 500) {
      return `${fallback} (server error ${error.response.status} - is the backend running?)`;
    }
  }
  return fallback;
}

export default api;
