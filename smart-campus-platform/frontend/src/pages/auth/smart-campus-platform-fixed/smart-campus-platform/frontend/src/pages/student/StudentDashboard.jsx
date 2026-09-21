import { CalendarDays, ClipboardCheck, FileText, GraduationCap } from 'lucide-react';
import { Link } from 'react-router-dom';
import useApiData from '../../hooks/useApiData.js';
import { dashboardApi } from '../../services/endpoints.js';
import { attendanceTone, formatDateTime, percent } from '../../utils/format.js';
import { MINIMUM_ATTENDANCE } from '../../utils/constants.js';
import PageHeader from '../../components/PageHeader.jsx';
import DashboardCard from '../../components/DashboardCard.jsx';
import ChartCard from '../../components/ChartCard.jsx';
import LoadingSpinner from '../../components/LoadingSpinner.jsx';
import ErrorState from '../../components/ErrorState.jsx';
import EmptyState from '../../components/EmptyState.jsx';
import Badge, { statusTone } from '../../components/Badge.jsx';

export default function StudentDashboard() {
  const { data, loading, error, reload } = useApiData(() => dashboardApi.student(), []);

  if (loading) return <LoadingSpinner label="Loading your dashboard..." />;
  if (error) return <ErrorState message={error} onRetry={reload} />;
  if (!data) return null;

  const attendance = data.attendance;
  const chartData = attendance.subjects.map((subject) => ({
    name: subject.subjectCode,
    percentage: subject.percentage,
  }));

  return (
    <div>
      <PageHeader
        title={`Hello, ${data.studentName.split(' ')[0]}`}
        subtitle={`${data.rollNumber} · ${data.departmentName} · Semester ${data.semester}-${data.division}`}
      />

      <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <DashboardCard
          label="Overall attendance"
          value={percent(attendance.overallPercentage)}
          hint={`${attendance.presentLectures} of ${attendance.totalLectures} lectures attended`}
          icon={ClipboardCheck}
          tone={attendance.overallPercentage >= MINIMUM_ATTENDANCE ? 'brand' : 'amber'}
        />
        <DashboardCard
          label="Lectures you can miss"
          value={attendance.lecturesCanMiss}
          hint={`While staying above ${MINIMUM_ATTENDANCE}%`}
          icon={CalendarDays}
          tone="sky"
        />
        <DashboardCard
          label="Pending assignments"
          value={data.pendingAssignments.length}
          hint="Not submitted yet"
          icon={FileText}
          tone="amber"
        />
        <DashboardCard
          label="Academic average"
          value={percent(data.academicPercentage)}
          hint={`${data.subjectsGraded} subjects graded`}
          icon={GraduationCap}
          tone="brand"
        />
      </div>

      <div className="mt-4 grid gap-4 lg:grid-cols-2">
        <ChartCard
          title="Attendance by subject"
          subtitle="Percentage of lectures attended"
          data={chartData}
          xKey="name"
          yKey="percentage"
        />

        <div className="card p-5">
          <h3 className="mb-3 text-sm font-semibold text-slate-900">Today's classes</h3>
          {data.todayClasses.length === 0 ? (
            <EmptyState title="No lectures today" message="Enjoy the break." />
          ) : (
            <ul className="space-y-2">
              {data.todayClasses.map((slot) => (
                <li
                  key={slot.id}
                  className="flex items-center justify-between rounded-lg border border-slate-200 p-3"
                >
                  <div>
                    <p className="text-sm font-medium text-slate-800">{slot.subjectName}</p>
                    <p className="text-xs text-slate-500">
                      {slot.facultyName} · Room {slot.room}
                    </p>
                  </div>
                  <p className="text-xs text-slate-500">
                    {slot.startTime} - {slot.endTime}
                  </p>
                </li>
              ))}
            </ul>
          )}
        </div>
      </div>

      <div className="mt-4 grid gap-4 lg:grid-cols-2">
        <div className="card p-5">
          <div className="mb-3 flex items-center justify-between">
            <h3 className="text-sm font-semibold text-slate-900">Pending assignments</h3>
            <Link to="/student/assignments" className="text-xs font-medium text-brand-700 hover:underline">
              View all
            </Link>
          </div>
          {data.pendingAssignments.length === 0 ? (
            <EmptyState title="Nothing pending" message="You have submitted everything due." />
          ) : (
            <ul className="space-y-2">
              {data.pendingAssignments.map((assignment) => (
                <li key={assignment.id} className="rounded-lg border border-slate-200 p-3">
                  <div className="flex items-center justify-between gap-2">
                    <p className="text-sm font-medium text-slate-800">{assignment.title}</p>
                    <Badge tone={assignment.overdue ? 'danger' : 'warning'}>
                      {assignment.overdue ? 'Overdue' : 'Due soon'}
                    </Badge>
                  </div>
                  <p className="mt-1 text-xs text-slate-500">
                    {assignment.subjectName} · due {formatDateTime(assignment.deadline)}
                  </p>
                </li>
              ))}
            </ul>
          )}
        </div>

        <div className="card p-5">
          <div className="mb-3 flex items-center justify-between">
            <h3 className="text-sm font-semibold text-slate-900">Announcements</h3>
            <Link to="/student/announcements" className="text-xs font-medium text-brand-700 hover:underline">
              View all
            </Link>
          </div>
          {data.recentAnnouncements.length === 0 ? (
            <EmptyState title="No announcements" />
          ) : (
            <ul className="space-y-2">
              {data.recentAnnouncements.map((item) => (
                <li key={item.id} className="rounded-lg border border-slate-200 p-3">
                  <div className="flex items-center gap-2">
                    <p className="text-sm font-medium text-slate-800">{item.title}</p>
                    <Badge tone={statusTone(item.priority)}>{item.priority}</Badge>
                  </div>
                  <p className="mt-1 line-clamp-2 text-xs text-slate-500">{item.message}</p>
                </li>
              ))}
            </ul>
          )}
        </div>
      </div>

      <p className={`mt-4 text-xs ${attendanceTone(attendance.overallPercentage)}`}>
        {attendance.overallPercentage >= MINIMUM_ATTENDANCE
          ? `You are above the ${MINIMUM_ATTENDANCE}% requirement.`
          : `You are below the ${MINIMUM_ATTENDANCE}% requirement. Speak to your class teacher.`}
      </p>
    </div>
  );
}
