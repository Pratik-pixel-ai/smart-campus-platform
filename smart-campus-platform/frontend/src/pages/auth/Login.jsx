import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { School } from 'lucide-react';
import { useAuth } from '../../context/AuthContext.jsx';
import { useToast } from '../../context/ToastContext.jsx';
import { errorMessage } from '../../services/api.js';
import { HOME_BY_ROLE } from '../../utils/constants.js';
import Button from '../../components/Button.jsx';
import Input from '../../components/Input.jsx';

const DEMO_ACCOUNTS = [
  { label: 'Student', email: 'student@smartcampus.com' },
  { label: 'Faculty', email: 'faculty@smartcampus.com' },
  { label: 'Admin', email: 'admin@smartcampus.com' },
];

const DEMO_PASSWORD = 'Demo@1234';

export default function Login() {
  const { login } = useAuth();
  const toast = useToast();
  const navigate = useNavigate();

  const [form, setForm] = useState({ email: '', password: '' });
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const change = (event) => setForm({ ...form, [event.target.name]: event.target.value });

  const submit = async (event) => {
    event.preventDefault();
    setError('');
    setLoading(true);
    try {
      const profile = await login(form);
      toast.success(`Signed in as ${profile.fullName}`);
      navigate(HOME_BY_ROLE[profile.role] || '/', { replace: true });
    } catch (err) {
      setError(errorMessage(err, 'Invalid email or password'));
    } finally {
      setLoading(false);
    }
  };

  const useDemo = (email) => setForm({ email, password: DEMO_PASSWORD });

  return (
    <div className="flex min-h-screen items-center justify-center bg-slate-50 p-4">
      <div className="w-full max-w-md">
        <div className="mb-6 flex flex-col items-center text-center">
          <div className="mb-3 rounded-xl bg-brand-600 p-2.5 text-white">
            <School size={24} />
          </div>
          <h1 className="text-xl font-semibold text-slate-900">Smart Campus Platform</h1>
          <p className="mt-1 text-sm text-slate-500">
            Attendance, timetable, assignments and results in one place
          </p>
        </div>

        <form onSubmit={submit} className="card space-y-4 p-6">
          {error && (
            <p className="rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">{error}</p>
          )}

          <Input
            label="Email"
            name="email"
            type="email"
            value={form.email}
            onChange={change}
            placeholder="you@smartcampus.com"
            required
          />
          <Input
            label="Password"
            name="password"
            type="password"
            value={form.password}
            onChange={change}
            placeholder="Enter your password"
            required
          />

          <Button type="submit" loading={loading} className="w-full">
            Sign in
          </Button>

          <div className="border-t border-slate-200 pt-4">
            <p className="mb-2 text-xs text-slate-500">Demo accounts (password {DEMO_PASSWORD})</p>
            <div className="flex gap-2">
              {DEMO_ACCOUNTS.map((account) => (
                <Button
                  key={account.email}
                  type="button"
                  variant="secondary"
                  size="sm"
                  className="flex-1"
                  onClick={() => useDemo(account.email)}
                >
                  {account.label}
                </Button>
              ))}
            </div>
          </div>

          <p className="text-center text-sm text-slate-500">
            New here?{' '}
            <Link to="/register" className="font-medium text-brand-700 hover:underline">
              Create an account
            </Link>
          </p>
        </form>
      </div>
    </div>
  );
}
