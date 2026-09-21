import { useEffect, useState } from 'react';
import { Bell, LogOut, Menu } from 'lucide-react';
import { useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';
import { notificationApi } from '../services/endpoints.js';
import { formatDateTime } from '../utils/format.js';
import EmptyState from './EmptyState.jsx';

export default function Navbar({ onToggleSidebar }) {
  const { user, logout } = useAuth();
  const navigate = useNavigate();
  const [notifications, setNotifications] = useState([]);
  const [open, setOpen] = useState(false);

  useEffect(() => {
    notificationApi
      .mine()
      .then(({ data }) => setNotifications(data))
      .catch(() => setNotifications([]));
  }, []);

  const unread = notifications.filter((item) => !item.read).length;

  const markRead = async (id) => {
    try {
      await notificationApi.markRead(id);
      setNotifications((current) => current.map((item) => (item.id === id ? { ...item, read: true } : item)));
    } catch {
      // A failed read receipt should not interrupt what the user was doing.
    }
  };

  const handleLogout = () => {
    logout();
    navigate('/login', { replace: true });
  };

  const initials = (user?.fullName || '?')
    .split(' ')
    .map((part) => part[0])
    .slice(0, 2)
    .join('');

  return (
    <header className="sticky top-0 z-20 flex h-16 items-center justify-between border-b border-slate-200 bg-white px-4 lg:px-6">
      <button
        type="button"
        className="rounded-lg p-2 text-slate-600 hover:bg-slate-100 lg:hidden"
        onClick={onToggleSidebar}
        aria-label="Toggle navigation"
      >
        <Menu size={20} />
      </button>

      <div className="hidden lg:block">
        <p className="text-sm text-slate-500">
          Welcome back, <span className="font-medium text-slate-800">{user?.fullName}</span>
        </p>
      </div>

      <div className="flex items-center gap-2">
        <div className="relative">
          <button
            type="button"
            className="relative rounded-lg p-2 text-slate-600 hover:bg-slate-100"
            onClick={() => setOpen((value) => !value)}
            aria-label="Notifications"
          >
            <Bell size={19} />
            {unread > 0 && (
              <span className="absolute right-1 top-1 flex h-4 min-w-4 items-center justify-center rounded-full bg-brand-600 px-1 text-[10px] font-semibold text-white">
                {unread}
              </span>
            )}
          </button>

          {open && (
            <div className="absolute right-0 mt-2 w-80 card max-h-96 overflow-y-auto p-0">
              <p className="border-b border-slate-200 px-4 py-3 text-sm font-semibold text-slate-800">
                Notifications
              </p>
              {notifications.length === 0 ? (
                <EmptyState title="You are all caught up" message="New updates will show here." />
              ) : (
                <ul className="divide-y divide-slate-100">
                  {notifications.slice(0, 12).map((item) => (
                    <li key={item.id}>
                      <button
                        type="button"
                        onClick={() => markRead(item.id)}
                        className={`w-full px-4 py-3 text-left hover:bg-slate-50 ${item.read ? 'opacity-60' : ''}`}
                      >
                        <p className="text-sm font-medium text-slate-800">{item.title}</p>
                        <p className="text-xs text-slate-500">{item.message}</p>
                        <p className="mt-1 text-[11px] text-slate-400">{formatDateTime(item.createdAt)}</p>
                      </button>
                    </li>
                  ))}
                </ul>
              )}
            </div>
          )}
        </div>

        <div className="flex items-center gap-2 rounded-lg border border-slate-200 py-1 pl-1 pr-3">
          <span className="flex h-7 w-7 items-center justify-center rounded-full bg-brand-600 text-xs font-semibold text-white">
            {initials}
          </span>
          <span className="hidden text-xs text-slate-600 sm:block">
            {user?.role?.replace('ROLE_', '').toLowerCase()}
          </span>
        </div>

        <button
          type="button"
          onClick={handleLogout}
          className="rounded-lg p-2 text-slate-600 hover:bg-slate-100"
          aria-label="Sign out"
          title="Sign out"
        >
          <LogOut size={18} />
        </button>
      </div>
    </header>
  );
}
