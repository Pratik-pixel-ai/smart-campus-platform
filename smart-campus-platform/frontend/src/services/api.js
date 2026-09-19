import axios from 'axios';

const TOKEN_KEY = 'smartcampus.token';

/**
 * One axios instance for the whole app.
 * The request interceptor attaches the JWT; the response interceptor sends the user
 * back to the login page when the token is missing or has expired.
 */
const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
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

/** Turns any axios failure into the message the API sent, or a readable fallback. */
export function errorMessage(error, fallback = 'Something went wrong. Please try again.') {
  const data = error?.response?.data;
  if (data?.fieldErrors) {
    const first = Object.values(data.fieldErrors)[0];
    if (first) return first;
  }
  return data?.message || fallback;
}

export default api;
