import { Navigate, Route, Routes } from 'react-router-dom';
import { useAuth } from '../context/AuthContext.jsx';
import { HOME_BY_ROLE, ROLES } from '../utils/constants.js';
import ProtectedRoute from '../components/ProtectedRoute.jsx';
import DashboardLayout from '../layouts/DashboardLayout.jsx';

import Login from '../pages/auth/Login.jsx';
import Register from '../pages/auth/Register.jsx';

import StudentDashboard from '../pages/student/StudentDashboard.jsx';
import StudentAttendance from '../pages/student/StudentAttendance.jsx';
import StudentAssignments from '../pages/student/StudentAssignments.jsx';
import StudentAcademics from '../pages/student/StudentAcademics.jsx';

import FacultyDashboard from '../pages/faculty/FacultyDashboard.jsx';
import FacultyAttendance from '../pages/faculty/FacultyAttendance.jsx';
import FacultyStudents from '../pages/faculty/FacultyStudents.jsx';
import FacultyAssignments from '../pages/faculty/FacultyAssignments.jsx';
import FacultyAcademics from '../pages/faculty/FacultyAcademics.jsx';

import AdminDashboard from '../pages/admin/AdminDashboard.jsx';
import AdminStudents from '../pages/admin/AdminStudents.jsx';
import AdminFaculty from '../pages/admin/AdminFaculty.jsx';
import AdminDepartments from '../pages/admin/AdminDepartments.jsx';
import AdminSubjects from '../pages/admin/AdminSubjects.jsx';
import AdminClassrooms from '../pages/admin/AdminClassrooms.jsx';
import AdminTimetable from '../pages/admin/AdminTimetable.jsx';
import AdminAttendance from '../pages/admin/AdminAttendance.jsx';
import AdminAssignments from '../pages/admin/AdminAssignments.jsx';
import AdminReports from '../pages/admin/AdminReports.jsx';

import TimetablePage from '../pages/shared/TimetablePage.jsx';
import AnnouncementsPage from '../pages/shared/AnnouncementsPage.jsx';
import AssistantPage from '../pages/shared/AssistantPage.jsx';
import ProfilePage from '../pages/shared/ProfilePage.jsx';
import NotFound from '../pages/shared/NotFound.jsx';

/**
 * All routes in one file.
 *
 * Pages that look the same for every role (timetable, announcements, assistant,
 * profile) are written once and mounted under each role's path, so there is one
 * implementation to maintain rather than three copies.
 */
export default function AppRoutes() {
  const { user } = useAuth();
  const home = user ? HOME_BY_ROLE[user.role] : '/login';

  return (
    <Routes>
      <Route path="/login" element={user ? <Navigate to={home} replace /> : <Login />} />
      <Route path="/register" element={user ? <Navigate to={home} replace /> : <Register />} />

      {/* Student */}
      <Route
        element={
          <ProtectedRoute allowedRoles={[ROLES.STUDENT]}>
            <DashboardLayout />
          </ProtectedRoute>
        }
      >
        <Route path="/student/dashboard" element={<StudentDashboard />} />
        <Route path="/student/attendance" element={<StudentAttendance />} />
        <Route path="/student/timetable" element={<TimetablePage />} />
        <Route path="/student/assignments" element={<StudentAssignments />} />
        <Route path="/student/academics" element={<StudentAcademics />} />
        <Route path="/student/announcements" element={<AnnouncementsPage />} />
        <Route path="/student/assistant" element={<AssistantPage />} />
        <Route path="/student/profile" element={<ProfilePage />} />
      </Route>

      {/* Faculty */}
      <Route
        element={
          <ProtectedRoute allowedRoles={[ROLES.FACULTY]}>
            <DashboardLayout />
          </ProtectedRoute>
        }
      >
        <Route path="/faculty/dashboard" element={<FacultyDashboard />} />
        <Route path="/faculty/attendance" element={<FacultyAttendance />} />
        <Route path="/faculty/students" element={<FacultyStudents />} />
        <Route path="/faculty/timetable" element={<TimetablePage />} />
        <Route path="/faculty/assignments" element={<FacultyAssignments />} />
        <Route path="/faculty/academics" element={<FacultyAcademics />} />
        <Route path="/faculty/announcements" element={<AnnouncementsPage />} />
        <Route path="/faculty/assistant" element={<AssistantPage />} />
        <Route path="/faculty/profile" element={<ProfilePage />} />
      </Route>

      {/* Admin */}
      <Route
        element={
          <ProtectedRoute allowedRoles={[ROLES.ADMIN]}>
            <DashboardLayout />
          </ProtectedRoute>
        }
      >
        <Route path="/admin/dashboard" element={<AdminDashboard />} />
        <Route path="/admin/students" element={<AdminStudents />} />
        <Route path="/admin/faculty" element={<AdminFaculty />} />
        <Route path="/admin/departments" element={<AdminDepartments />} />
        <Route path="/admin/subjects" element={<AdminSubjects />} />
        <Route path="/admin/classrooms" element={<AdminClassrooms />} />
        <Route path="/admin/timetable" element={<AdminTimetable />} />
        <Route path="/admin/attendance" element={<AdminAttendance />} />
        <Route path="/admin/assignments" element={<AdminAssignments />} />
        <Route path="/admin/announcements" element={<AnnouncementsPage />} />
        <Route path="/admin/reports" element={<AdminReports />} />
        <Route path="/admin/assistant" element={<AssistantPage />} />
        <Route path="/admin/profile" element={<ProfilePage />} />
      </Route>

      <Route path="/" element={<Navigate to={home} replace />} />
      <Route path="*" element={<NotFound />} />
    </Routes>
  );
}
