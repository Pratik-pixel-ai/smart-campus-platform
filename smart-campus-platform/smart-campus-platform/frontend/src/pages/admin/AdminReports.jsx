import useApiData from '../../hooks/useApiData.js';
import { dashboardApi } from '../../services/endpoints.js';
import { percent } from '../../utils/format.js';
import PageHeader from '../../components/PageHeader.jsx';
import ChartCard from '../../components/ChartCard.jsx';
import DashboardCard from '../../components/DashboardCard.jsx';
import LoadingSpinner from '../../components/LoadingSpinner.jsx';
import ErrorState from '../../components/ErrorState.jsx';
import Button from '../../components/Button.jsx';
import { Download } from 'lucide-react';

/**
 * Reporting view. The CSV is produced in the browser from data the API already
 * returned, which keeps a file-export dependency out of the backend.
 */
export default function AdminReports() {
  const { data, loading, error, reload } = useApiData(() => dashboardApi.admin(), []);

  if (loading) return <LoadingSpinner />;
  if (error) return <ErrorState message={error} onRetry={reload} />;
  if (!data) return null;

  const exportCsv = () => {
    const header = 'Department,Students,Faculty,Attendance %';
    const lines = data.departmentStats.map(
      (stat) => `${stat.department},${stat.students},${stat.faculty},${stat.attendancePercentage}`,
    );
    const blob = new Blob([[header, ...lines].join('\n')], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.download = 'department-report.csv';
    link.click();
    URL.revokeObjectURL(url);
  };

  return (
    <div>
      <PageHeader
        title="Reports"
        subtitle="Attendance and activity across departments"
        actions={
          <Button variant="secondary" onClick={exportCsv}>
            <Download size={16} /> Export CSV
          </Button>
        }
      />

      <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-4">
        <DashboardCard label="Campus attendance" value={percent(data.campusAttendancePercentage)} />
        <DashboardCard label="Sessions recorded" value={data.totalSessions} tone="sky" />
        <DashboardCard label="Assignments set" value={data.totalAssignments} tone="slate" />
        <DashboardCard label="Submissions received" value={data.totalSubmissions} tone="amber" />
      </div>

      <div className="mt-4 grid gap-4 lg:grid-cols-2">
        <ChartCard
          title="Attendance by department"
          data={data.departmentStats.map((stat) => ({
            name: stat.department,
            attendance: stat.attendancePercentage,
          }))}
          xKey="name"
          yKey="attendance"
        />
        <ChartCard
          title="Share of students by department"
          type="pie"
          data={data.departmentStats.map((stat) => ({ name: stat.department, students: stat.students }))}
          xKey="name"
          yKey="students"
        />
      </div>

      <div className="mt-4 card overflow-x-auto">
        <table className="w-full min-w-[520px]">
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
                <td className="table-cell">{percent(stat.attendancePercentage)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
}
