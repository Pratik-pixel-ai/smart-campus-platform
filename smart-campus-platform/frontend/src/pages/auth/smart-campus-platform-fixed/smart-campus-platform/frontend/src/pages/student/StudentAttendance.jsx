import useApiData from '../../hooks/useApiData.js';
import { attendanceApi } from '../../services/endpoints.js';
import { MINIMUM_ATTENDANCE } from '../../utils/constants.js';
import { attendanceTone, formatDate, percent } from '../../utils/format.js';
import PageHeader from '../../components/PageHeader.jsx';
import DashboardCard from '../../components/DashboardCard.jsx';
import ChartCard from '../../components/ChartCard.jsx';
import DataTable from '../../components/DataTable.jsx';
import LoadingSpinner from '../../components/LoadingSpinner.jsx';
import ErrorState from '../../components/ErrorState.jsx';
import Badge, { statusTone } from '../../components/Badge.jsx';
import { CalendarCheck, CalendarX, Percent } from 'lucide-react';
import { useAuth } from '../../context/AuthContext.jsx';

export default function StudentAttendance() {
  const { user } = useAuth();
  const summary = useApiData(() => attendanceApi.mySummary(), []);
  const history = useApiData(
    () => attendanceApi.studentHistory(user.profileId),
    [user?.profileId],
  );

  if (summary.loading) return <LoadingSpinner />;
  if (summary.error) return <ErrorState message={summary.error} onRetry={summary.reload} />;

  const data = summary.data;

  return (
    <div>
      <PageHeader title="My attendance" subtitle="Subject-wise figures and the full lecture history" />

      <div className="grid gap-4 sm:grid-cols-3">
        <DashboardCard
          label="Overall"
          value={percent(data.overallPercentage)}
          hint={`${data.presentLectures} present of ${data.totalLectures}`}
          icon={Percent}
          tone={data.overallPercentage >= MINIMUM_ATTENDANCE ? 'brand' : 'amber'}
        />
        <DashboardCard label="Lectures attended" value={data.presentLectures} icon={CalendarCheck} tone="sky" />
        <DashboardCard label="Lectures missed" value={data.absentLectures} icon={CalendarX} tone="amber" />
      </div>

      <div className="mt-4 grid gap-4 lg:grid-cols-2">
        <ChartCard
          title="Attendance by subject"
          data={data.subjects.map((subject) => ({ name: subject.subjectCode, percentage: subject.percentage }))}
          xKey="name"
          yKey="percentage"
        />

        <div className="card p-5">
          <h3 className="mb-3 text-sm font-semibold text-slate-900">Subject breakdown</h3>
          <ul className="space-y-3">
            {data.subjects.map((subject) => (
              <li key={subject.subjectId}>
                <div className="flex items-center justify-between text-sm">
                  <span className="text-slate-700">{subject.subjectName}</span>
                  <span className={`font-medium ${attendanceTone(subject.percentage)}`}>
                    {percent(subject.percentage)}
                  </span>
                </div>
                <div className="mt-1 h-2 w-full overflow-hidden rounded-full bg-slate-100">
                  <div
                    className={`h-full rounded-full ${
                      subject.percentage >= MINIMUM_ATTENDANCE ? 'bg-brand-500' : 'bg-amber-500'
                    }`}
                    style={{ width: `${Math.min(subject.percentage, 100)}%` }}
                  />
                </div>
                <p className="mt-1 text-xs text-slate-500">
                  {subject.presentLectures} of {subject.totalLectures} lectures
                </p>
              </li>
            ))}
          </ul>
        </div>
      </div>

      <div className="mt-4">
        <h3 className="mb-3 text-sm font-semibold text-slate-900">Lecture history</h3>
        <DataTable
          loading={history.loading}
          error={history.error}
          onRetry={history.reload}
          rows={history.data || []}
          emptyTitle="No attendance records yet"
          columns={[
            { key: 'sessionDate', header: 'Date', render: (row) => formatDate(row.sessionDate) },
            { key: 'subjectName', header: 'Subject' },
            {
              key: 'status',
              header: 'Status',
              render: (row) => <Badge tone={statusTone(row.status)}>{row.status}</Badge>,
            },
            {
              key: 'detectionMethod',
              header: 'Marked by',
              render: (row) => <span className="text-xs text-slate-500">{row.detectionMethod}</span>,
            },
          ]}
        />
      </div>
    </div>
  );
}
