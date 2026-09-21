import { useAuth } from '../../context/AuthContext.jsx';
import { titleCase } from '../../utils/format.js';
import PageHeader from '../../components/PageHeader.jsx';
import Badge from '../../components/Badge.jsx';

function Row({ label, value }) {
  if (!value) return null;
  return (
    <div className="flex justify-between gap-4 border-b border-slate-100 py-3 last:border-0">
      <span className="text-sm text-slate-500">{label}</span>
      <span className="text-sm font-medium text-slate-800">{value}</span>
    </div>
  );
}

export default function ProfilePage() {
  const { user } = useAuth();
  if (!user) return null;

  const initials = user.fullName
    .split(' ')
    .map((part) => part[0])
    .slice(0, 2)
    .join('');

  return (
    <div>
      <PageHeader title="Profile" subtitle="Your account details" />

      <div className="grid gap-4 lg:grid-cols-3">
        <div className="card flex flex-col items-center p-6 text-center">
          <span className="flex h-16 w-16 items-center justify-center rounded-full bg-brand-600 text-xl font-semibold text-white">
            {initials}
          </span>
          <p className="mt-3 text-base font-semibold text-slate-900">{user.fullName}</p>
          <p className="text-sm text-slate-500">{user.email}</p>
          <Badge tone="success" className="mt-3">
            {titleCase(user.role.replace('ROLE_', ''))}
          </Badge>
        </div>

        <div className="card p-6 lg:col-span-2">
          <h2 className="mb-2 text-sm font-semibold text-slate-900">Details</h2>
          <Row label="Identifier" value={user.identifier} />
          <Row label="Department" value={user.departmentName} />
          <Row label="Semester" value={user.semester} />
          <Row label="Division" value={user.division} />
          <Row label="Designation" value={user.designation} />
          <Row label="Phone" value={user.phone} />
          <Row label="BLE device id" value={user.bleDeviceId} />
        </div>
      </div>
    </div>
  );
}
