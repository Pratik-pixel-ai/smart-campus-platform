import { NavLink } from 'react-router-dom';
import {
  Bot,
  Building2,
  CalendarDays,
  ClipboardCheck,
  DoorOpen,
  FileBarChart,
  FileText,
  GraduationCap,
  LayoutDashboard,
  Megaphone,
  School,
  UserCircle,
  Users,
} from 'lucide-react';
import { ROLES } from '../utils/constants.js';

/** One place that decides which links each role sees. */
const NAV_BY_ROLE = {
  [ROLES.STUDENT]: [
    { to: '/student/dashboard', label: 'Dashboard', icon: LayoutDashboard },
    { to: '/student/attendance', label: 'Attendance', icon: ClipboardCheck },
    { to: '/student/timetable', label: 'Timetable', icon: CalendarDays },
    { to: '/student/assignments', label: 'Assignments', icon: FileText },
    { to: '/student/academics', label: 'Academics', icon: GraduationCap },
    { to: '/student/announcements', label: 'Announcements', icon: Megaphone },
    { to: '/student/assistant', label: 'Campus assistant', icon: Bot },
    { to: '/student/profile', label: 'Profile', icon: UserCircle },
  ],
  [ROLES.FACULTY]: [
    { to: '/faculty/dashboard', label: 'Dashboard', icon: LayoutDashboard },
    { to: '/faculty/attendance', label: 'Take attendance', icon: ClipboardCheck },
    { to: '/faculty/students', label: 'Students', icon: Users },
    { to: '/faculty/timetable', label: 'Timetable', icon: CalendarDays },
    { to: '/faculty/assignments', label: 'Assignments', icon: FileText },
    { to: '/faculty/academics', label: 'Marks', icon: GraduationCap },
    { to: '/faculty/announcements', label: 'Announcements', icon: Megaphone },
    { to: '/faculty/assistant', label: 'Campus assistant', icon: Bot },
    { to: '/faculty/profile', label: 'Profile', icon: UserCircle },
  ],
  [ROLES.ADMIN]: [
    { to: '/admin/dashboard', label: 'Dashboard', icon: LayoutDashboard },
    { to: '/admin/students', label: 'Students', icon: Users },
    { to: '/admin/faculty', label: 'Faculty', icon: School },
    { to: '/admin/departments', label: 'Departments', icon: Building2 },
    { to: '/admin/subjects', label: 'Subjects', icon: FileText },
    { to: '/admin/classrooms', label: 'Classrooms', icon: DoorOpen },
    { to: '/admin/timetable', label: 'Timetable', icon: CalendarDays },
    { to: '/admin/attendance', label: 'Attendance', icon: ClipboardCheck },
    { to: '/admin/assignments', label: 'Assignments', icon: FileText },
    { to: '/admin/announcements', label: 'Announcements', icon: Megaphone },
    { to: '/admin/reports', label: 'Reports', icon: FileBarChart },
    { to: '/admin/assistant', label: 'Campus assistant', icon: Bot },
    { to: '/admin/profile', label: 'Profile', icon: UserCircle },
  ],
};

export default function Sidebar({ role, open, onNavigate }) {
  const items = NAV_BY_ROLE[role] || [];

  return (
    <aside
      className={`fixed inset-y-0 left-0 z-30 w-64 transform border-r border-slate-200 bg-white
                  transition-transform lg:static lg:translate-x-0
                  ${open ? 'translate-x-0' : '-translate-x-full'}`}
    >
      <div className="flex h-16 items-center gap-2 border-b border-slate-200 px-5">
        <div className="rounded-lg bg-brand-600 p-1.5 text-white">
          <School size={18} />
        </div>
        <div>
          <p className="text-sm font-semibold text-slate-900">Smart Campus</p>
          <p className="text-[11px] text-slate-500">Platform</p>
        </div>
      </div>

      <nav className="flex h-[calc(100%-4rem)] flex-col gap-0.5 overflow-y-auto p-3">
        {items.map(({ to, label, icon: Icon }) => (
          <NavLink
            key={to}
            to={to}
            onClick={onNavigate}
            className={({ isActive }) =>
              `flex items-center gap-3 rounded-lg px-3 py-2 text-sm transition ${
                isActive ? 'bg-brand-50 font-medium text-brand-700' : 'text-slate-600 hover:bg-slate-100'
              }`
            }
          >
            <Icon size={17} />
            {label}
          </NavLink>
        ))}
      </nav>
    </aside>
  );
}
