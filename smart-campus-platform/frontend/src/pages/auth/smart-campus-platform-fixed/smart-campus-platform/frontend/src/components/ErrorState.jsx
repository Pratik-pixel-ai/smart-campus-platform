import { AlertTriangle } from 'lucide-react';
import Button from './Button.jsx';

export default function ErrorState({ message = 'Could not load this page.', onRetry }) {
  return (
    <div className="flex flex-col items-center justify-center gap-3 px-6 py-12 text-center">
      <div className="rounded-full bg-red-50 p-3 text-red-500">
        <AlertTriangle size={22} />
      </div>
      <p className="text-sm text-slate-700">{message}</p>
      {onRetry && (
        <Button variant="secondary" size="sm" onClick={onRetry}>
          Try again
        </Button>
      )}
    </div>
  );
}
