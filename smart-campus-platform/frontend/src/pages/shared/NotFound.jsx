import { Link } from 'react-router-dom';
import Button from '../../components/Button.jsx';

export default function NotFound() {
  return (
    <div className="flex min-h-screen flex-col items-center justify-center gap-3 p-6 text-center">
      <p className="text-4xl font-semibold text-slate-900">404</p>
      <p className="text-sm text-slate-500">That page does not exist.</p>
      <Link to="/login">
        <Button variant="secondary">Back to sign in</Button>
      </Link>
    </div>
  );
}
