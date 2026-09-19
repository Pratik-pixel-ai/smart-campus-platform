import useApiData from '../../hooks/useApiData.js';
import { academicApi } from '../../services/endpoints.js';
import { percent } from '../../utils/format.js';
import PageHeader from '../../components/PageHeader.jsx';
import DashboardCard from '../../components/DashboardCard.jsx';
import ChartCard from '../../components/ChartCard.jsx';
import LoadingSpinner from '../../components/LoadingSpinner.jsx';
import ErrorState from '../../components/ErrorState.jsx';
import EmptyState from '../../components/EmptyState.jsx';
import Badge from '../../components/Badge.jsx';
import { GraduationCap } from 'lucide-react';

const GRADE_TONE = { O: 'success', 'A+': 'success', A: 'success', 'B+': 'info', B: 'info', C: 'warning', F: 'danger' };

export default function StudentAcademics() {
  const { data, loading, error, reload } = useApiData(() => academicApi.me(), []);

  if (loading) return <LoadingSpinner />;
  if (error) return <ErrorState message={error} onRetry={reload} />;

  const semesters = data?.semesters || [];

  return (
    <div>
      <PageHeader title="Academic record" subtitle="Internal and external marks with grades" />

      {semesters.length === 0 ? (
        <div className="card">
          <EmptyState icon={GraduationCap} title="No marks published yet" message="Results appear once faculty enter them." />
        </div>
      ) : (
        <>
          <div className="grid gap-4 sm:grid-cols-3">
            <DashboardCard label="Overall percentage" value={percent(data.overallPercentage)} icon={GraduationCap} />
            <DashboardCard label="Semesters recorded" value={semesters.length} tone="sky" />
            <DashboardCard
              label="Subjects graded"
              value={semesters.reduce((total, semester) => total + semester.subjectCount, 0)}
              tone="slate"
            />
          </div>

          <div className="mt-4">
            <ChartCard
              title="Percentage by semester"
              type="line"
              data={semesters.map((semester) => ({
                name: `Sem ${semester.semester}`,
                percentage: semester.percentage,
              }))}
              xKey="name"
              yKey="percentage"
            />
          </div>

          <div className="mt-4 space-y-4">
            {semesters.map((semester) => (
              <div key={semester.semester} className="card overflow-hidden">
                <div className="flex items-center justify-between border-b border-slate-200 px-5 py-3">
                  <h3 className="text-sm font-semibold text-slate-900">Semester {semester.semester}</h3>
                  <p className="text-sm text-slate-500">
                    {semester.totalMarks} / {semester.maxPossible} · {percent(semester.percentage)}
                  </p>
                </div>
                <div className="overflow-x-auto">
                  <table className="w-full min-w-[560px]">
                    <thead className="bg-slate-50">
                      <tr>
                        <th className="table-head">Subject</th>
                        <th className="table-head">Internal</th>
                        <th className="table-head">External</th>
                        <th className="table-head">Total</th>
                        <th className="table-head">Grade</th>
                      </tr>
                    </thead>
                    <tbody className="divide-y divide-slate-100">
                      {semester.records.map((record) => (
                        <tr key={record.id}>
                          <td className="table-cell">
                            {record.subjectName}
                            <span className="ml-2 text-xs text-slate-400">{record.subjectCode}</span>
                          </td>
                          <td className="table-cell">{record.internalMarks} / 40</td>
                          <td className="table-cell">{record.externalMarks} / 60</td>
                          <td className="table-cell font-medium">{record.totalMarks} / 100</td>
                          <td className="table-cell">
                            <Badge tone={GRADE_TONE[record.grade] || 'neutral'}>{record.grade}</Badge>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              </div>
            ))}
          </div>
        </>
      )}
    </div>
  );
}
