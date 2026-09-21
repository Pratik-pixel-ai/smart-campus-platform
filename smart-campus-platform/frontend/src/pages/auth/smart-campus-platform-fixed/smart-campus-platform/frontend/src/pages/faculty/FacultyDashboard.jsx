import { BookOpen, ClipboardCheck, Clock, Users } from 'lucide-react';
import { Link } from 'react-router-dom';
import useApiData from '../../hooks/useApiData.js';
import { dashboardApi } from '../../services/endpoints.js';
import { formatDate } from '../../utils/format.js';
import PageHeader from '../../components/PageHeader.jsx';
import DashboardCard from '../../components/DashboardCard.jsx';
import ChartCard from '../../components/ChartCard.jsx';
import LoadingSpinner from '../../components/LoadingSpinner.jsx';
import ErrorState from '../../components/ErrorState.jsx';
import EmptyState from '../../components/EmptyState.jsx';
import Badge, { statusTone } from '../../components/Badge.jsx';
import Button from '../../components/Button.jsx';

export default function FacultyDashboard() {
  const { data, loading, error, reload } = useApiData(() => dashboardApi.faculty(), []);

  if (loading) return <LoadingSpinner />;
  if (error) return <ErrorState message={error} onRetry={reload} />;
  if (!data) return null;

  const sessionChart = [...data.recentSessions].reverse().map((session) => ({
    name: `${session.subjectCode} L${session.lectureNumber}`,
    present: session.presentCount,
  }));

  return (
    <div>
      <PageHeader
        title={`Hello, ${data.facultyName.split(' ').slice(-1)[0]}`}
        subtitle={`${data.designation} · ${data.departmentName}`}
        actions={
          <Link to="/faculty/attendance">
            <Button>
              <ClipboardCheck size={16} /> Take attendance
            </Button>
          </Link>
        }
      />

      <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <DashboardCard label="Subjects taught" value={data.subjectsTaught} icon={BookOpen} />
        <DashboardCard label="Students in department" value={data.totalStudents} icon={Users} tone="sky" />
        <DashboardCard label="Sessions today" value={data.sessionsToday} icon={Clock} tone="slate" />
        <DashboardCard
          label="Submissions to review"
          value={data.pendingReviews}
          hint="Not graded yet"
          icon={ClipboardCheck}
          tone="amber"
        />
      </div>

      <div className="mt-4 grid gap-4 lg:grid-cols-2">
        <ChartCard
          title="Students present in recent sessions"
          data={sessionChart}
          xKey="name"
          yKey="present"
        />

        <div className="card p-5">
          <h3 className="mb-3 text-sm font-semibold text-slate-900">Today's lectures</h3>
          {data.todayClasses.length === 0 ? (
            <EmptyState title="No lectures today" />
          ) : (
            <ul className="space-y-2">
              {data.todayClasses.map((slot) => (
                <li key={slot.id} className="flex items-center justify-between rounded-lg border border-slate-200 p-3">
                  <div>
                    <p className="text-sm font-medium text-slate-800">{slot.subjectName}</p>
                    <p className="text-xs text-slate-500">
                      Sem {slot.semester}-{slot.division} · Room {slot.room}
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

      <div className="mt-4 card p-5">
        <h3 className="mb-3 text-sm font-semibold text-slate-900">Recent attendance sessions</h3>
        {data.recentSessions.length === 0 ? (
          <EmptyState title="No sessions yet" message="Start one from the attendance screen." />
        ) : (
          <ul className="divide-y divide-slate-100">
            {data.recentSessions.map((session) => (
              <li key={session.id} className="flex flex-wrap items-center justify-between gap-2 py-3">
                <div>
                  <p className="text-sm font-medium text-slate-800">{session.subjectName}</p>
                  <p className="text-xs text-slate-500">
                    {formatDate(session.sessionDate)} · Lecture {session.lectureNumber} · Sem {session.semester}-
                    {session.division}
                  </p>
                </div>
                <div className="flex items-center gap-2">
                  <span className="text-xs text-slate-500">
                    {session.presentCount} present / {session.enrolledCount}
                  </span>
                  <Badge tone={statusTone(session.status)}>{session.status}</Badge>
                </div>
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  );
}
