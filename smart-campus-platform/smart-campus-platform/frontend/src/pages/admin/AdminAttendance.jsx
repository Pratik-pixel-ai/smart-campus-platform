import { useState } from 'react';
import useApiData from '../../hooks/useApiData.js';
import { attendanceApi } from '../../services/endpoints.js';
import { formatDate, percent } from '../../utils/format.js';
import PageHeader from '../../components/PageHeader.jsx';
import DataTable from '../../components/DataTable.jsx';
import DashboardCard from '../../components/DashboardCard.jsx';
import Badge, { statusTone } from '../../components/Badge.jsx';
import Button from '../../components/Button.jsx';
import Modal from '../../components/Modal.jsx';
import { ClipboardCheck } from 'lucide-react';

/** Read-only view of every attendance session recorded on campus. */
export default function AdminAttendance() {
  const [page, setPage] = useState(0);
  const { data, loading, error, reload } = useApiData(
    () => attendanceApi.allSessions({ page, size: 20 }),
    [page],
  );
  const { data: overview } = useApiData(() => attendanceApi.overview(), []);
  const [selected, setSelected] = useState(null);

  return (
    <div>
      <PageHeader title="Attendance" subtitle="Every session recorded across the campus" />

      <div className="mb-4 grid gap-4 sm:grid-cols-3">
        <DashboardCard
          label="Campus attendance"
          value={percent(overview?.campusPercentage ?? 0)}
          icon={ClipboardCheck}
        />
        <DashboardCard label="Sessions on this page" value={(data || []).length} tone="slate" />
        <DashboardCard
          label="Open sessions"
          value={(data || []).filter((session) => session.status === 'OPEN').length}
          tone="amber"
        />
      </div>

      <DataTable
        loading={loading}
        error={error}
        onRetry={reload}
        rows={data || []}
        emptyTitle="No attendance sessions yet"
        columns={[
          { key: 'sessionDate', header: 'Date', render: (row) => formatDate(row.sessionDate) },
          { key: 'subjectName', header: 'Subject', render: (row) => `${row.subjectName} (${row.subjectCode})` },
          { key: 'facultyName', header: 'Faculty' },
          { key: 'class', header: 'Class', render: (row) => `Sem ${row.semester}-${row.division}` },
          {
            key: 'attendance',
            header: 'Present',
            render: (row) => `${row.presentCount} / ${row.enrolledCount}`,
          },
          {
            key: 'mode',
            header: 'Detection',
            render: (row) => <Badge tone={row.mode === 'BLE' ? 'info' : 'warning'}>{row.mode}</Badge>,
          },
          {
            key: 'status',
            header: 'Status',
            render: (row) => <Badge tone={statusTone(row.status)}>{row.status}</Badge>,
          },
          {
            key: 'actions',
            header: '',
            render: (row) => (
              <Button size="sm" variant="ghost" onClick={() => setSelected(row)}>
                View
              </Button>
            ),
          },
        ]}
      />

      {(data || []).length >= 20 && (
        <div className="mt-3 flex justify-end gap-2">
          <Button variant="secondary" size="sm" disabled={page === 0} onClick={() => setPage(page - 1)}>
            Previous
          </Button>
          <Button variant="secondary" size="sm" onClick={() => setPage(page + 1)}>
            Next
          </Button>
        </div>
      )}

      <Modal
        open={Boolean(selected)}
        title={selected ? `${selected.subjectName} · ${formatDate(selected.sessionDate)}` : ''}
        onClose={() => setSelected(null)}
        width="max-w-2xl"
      >
        {selected && (
          <div className="space-y-4">
            <p className="text-sm text-slate-600">
              Lecture {selected.lectureNumber} · {selected.durationMinutes} minutes · Room {selected.room} ·{' '}
              {selected.mode} detection
            </p>
            <div className="overflow-x-auto">
              <table className="w-full min-w-[480px]">
                <thead className="bg-slate-50">
                  <tr>
                    <th className="table-head">Roll number</th>
                    <th className="table-head">Student</th>
                    <th className="table-head">Status</th>
                    <th className="table-head">Method</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-100">
                  {selected.records.map((record) => (
                    <tr key={record.id}>
                      <td className="table-cell">{record.rollNumber}</td>
                      <td className="table-cell">{record.studentName}</td>
                      <td className="table-cell">
                        <Badge tone={statusTone(record.status)}>{record.status}</Badge>
                      </td>
                      <td className="table-cell text-xs text-slate-500">{record.detectionMethod}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        )}
      </Modal>
    </div>
  );
}
