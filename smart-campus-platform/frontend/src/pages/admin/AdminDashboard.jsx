import { Building2, ClipboardCheck, GraduationCap, Users } from 'lucide-react';
import useApiData from '../../hooks/useApiData.js';
import { dashboardApi } from '../../services/endpoints.js';
import { percent } from '../../utils/format.js';
import PageHeader from '../../components/PageHeader.jsx';
import DashboardCard from '../../components/DashboardCard.jsx';
import ChartCard from '../../components/ChartCard.jsx';
import LoadingSpinner from '../../components/LoadingSpinner.jsx';
import ErrorState from '../../components/ErrorState.jsx';
import Badge, { statusTone } from '../../components/Badge.jsx';

export default function AdminDashboard() {
  const { data, loading, error, reload } = useApiData(() => dashboardApi.admin(), []);

  if (loading) return <LoadingSpinner />;
  if (error) return <ErrorState message={error} onRetry={reload} />;
  if (!data) return null;

  return (
    <div>
      <PageHeader title="Campus overview" subtitle="Institution-wide numbers at a glance" />

      <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <DashboardCard label="Students" value={data.totalStudents} icon={Users} />
        <DashboardCard label="Faculty" value={data.totalFaculty} icon={GraduationCap} tone="sky" />
        <DashboardCard
          label="Departments"
          value={data.totalDepartments}
          hint={`${data.totalSubjects} subjects`}
          icon={Building2}
          tone="slate"
        />
        <DashboardCard
          label="Campus attendance"
          value={percent(data.campusAttendancePercentage)}
          hint={`${data.totalSessions} sessions recorded`}
          icon={ClipboardCheck}
          tone={data.campusAttendancePercentage >= 75 ? 'brand' : 'amber'}
        />
      </div>

      <div className="mt-4 grid gap-4 lg:grid-cols-2">
        <ChartCard
          title="Students per department"
          data={data.departmentStats.map((stat) => ({ name: stat.department, students: stat.students }))}
          xKey="name"
          yKey="students"
        />
        <ChartCard
          title="Attendance per department"
          subtitle="Percentage of lectures attended"
          data={data.departmentStats.map((stat) => ({
            name: stat.department,
            attendance: stat.attendancePercentage,
          }))}
          xKey="name"
          yKey="attendance"
        />
      </div>

      <div className="mt-4 grid gap-4 lg:grid-cols-3">
        <div className="card p-5 lg:col-span-2">
          <h3 className="mb-3 text-sm font-semibold text-slate-900">Departments</h3>
          <div className="overflow-x-auto">
            <table className="w-full min-w-[480px]">
              <thead className="bg-slate-50">
                <tr>
                  <th className="table-head">Department</th>
                  <th className="table-head">Students</th>
                  <th className="table-head">Faculty</th>
                  <th className="table-head">Attendance</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-slate-100">
                {data.departmentStats.map((stat) => (
                  <tr key={stat.department}>
                    <td className="table-cell">{stat.department}</td>
                    <td className="table-cell">{stat.students}</td>
                    <td className="table-cell">{stat.faculty}</td>
                    <td className="table-cell">
                      <Badge tone={stat.attendancePercentage >= 75 ? 'success' : 'warning'}>
                        {percent(stat.attendancePercentage)}
                      </Badge>
                    </td>
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        </div>

        <div className="card p-5">
          <h3 className="mb-3 text-sm font-semibold text-slate-900">Activity</h3>
          <dl className="space-y-3 text-sm">
            <div className="flex justify-between">
              <dt className="text-slate-500">Assignments set</dt>
              <dd className="font-medium">{data.totalAssignments}</dd>
            </div>
            <div className="flex justify-between">
              <dt className="text-slate-500">Submissions received</dt>
              <dd className="font-medium">{data.totalSubmissions}</dd>
            </div>
            <div className="flex justify-between">
              <dt className="text-slate-500">Sessions recorded</dt>
              <dd className="font-medium">{data.totalSessions}</dd>
            </div>
            <div className="flex justify-between">
              <dt className="text-slate-500">Sessions still open</dt>
              <dd className="font-medium">{data.openSessions}</dd>
            </div>
          </dl>
        </div>
      </div>

      <div className="mt-4 card p-5">
        <h3 className="mb-3 text-sm font-semibold text-slate-900">Recent announcements</h3>
        <ul className="divide-y divide-slate-100">
          {data.recentAnnouncements.map((item) => (
            <li key={item.id} className="flex items-center justify-between gap-3 py-3">
              <div>
                <p className="text-sm font-medium text-slate-800">{item.title}</p>
                <p className="text-xs text-slate-500">{item.departmentName}</p>
              </div>
              <Badge tone={statusTone(item.priority)}>{item.priority}</Badge>
            </li>
          ))}
        </ul>
      </div>
    </div>
  );
}
