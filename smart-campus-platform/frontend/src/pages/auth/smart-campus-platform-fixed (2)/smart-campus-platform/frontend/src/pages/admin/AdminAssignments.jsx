import useApiData from '../../hooks/useApiData.js';
import { assignmentApi } from '../../services/endpoints.js';
import { formatDateTime } from '../../utils/format.js';
import PageHeader from '../../components/PageHeader.jsx';
import DataTable from '../../components/DataTable.jsx';
import Badge from '../../components/Badge.jsx';

/** Campus-wide view of assignments. Creation and grading stay with the faculty. */
export default function AdminAssignments() {
  const { data, loading, error, reload } = useApiData(() => assignmentApi.list(), []);

  return (
    <div>
      <PageHeader title="Assignments" subtitle="Work set across every department" />

      <DataTable
        loading={loading}
        error={error}
        onRetry={reload}
        rows={data || []}
        emptyTitle="No assignments yet"
        columns={[
          { key: 'title', header: 'Title' },
          { key: 'subjectName', header: 'Subject', render: (row) => `${row.subjectName} (${row.subjectCode})` },
          { key: 'facultyName', header: 'Faculty' },
          { key: 'class', header: 'Class', render: (row) => `Sem ${row.semester}-${row.division}` },
          { key: 'deadline', header: 'Deadline', render: (row) => formatDateTime(row.deadline) },
          { key: 'submissionCount', header: 'Submissions' },
          {
            key: 'status',
            header: 'Status',
            render: (row) => (
              <Badge tone={row.overdue ? 'neutral' : 'info'}>{row.overdue ? 'Closed' : 'Open'}</Badge>
            ),
          },
        ]}
      />
    </div>
  );
}
