import { useState } from 'react';
import useApiData from '../../hooks/useApiData.js';
import useDebouncedValue from '../../hooks/useDebouncedValue.js';
import { attendanceApi, studentApi } from '../../services/endpoints.js';
import { errorMessage } from '../../services/api.js';
import { attendanceTone, percent } from '../../utils/format.js';
import PageHeader from '../../components/PageHeader.jsx';
import DataTable from '../../components/DataTable.jsx';
import Button from '../../components/Button.jsx';
import Modal from '../../components/Modal.jsx';
import Badge from '../../components/Badge.jsx';
import LoadingSpinner from '../../components/LoadingSpinner.jsx';

/** Read-only student directory for faculty, with a per-student attendance view. */
export default function FacultyStudents() {
  const [search, setSearch] = useState('');
  const [page, setPage] = useState(0);
  const [sort, setSort] = useState('rollNumber,asc');
  const debouncedSearch = useDebouncedValue(search);

  const [selected, setSelected] = useState(null);
  const [summary, setSummary] = useState(null);
  const [summaryError, setSummaryError] = useState('');

  const { data, loading, error, reload } = useApiData(
    () => studentApi.list({ search: debouncedSearch || undefined, page, size: 10, sort }),
    [debouncedSearch, page, sort],
  );

  const openSummary = async (student) => {
    setSelected(student);
    setSummary(null);
    setSummaryError('');
    try {
      const { data: result } = await attendanceApi.studentSummary(student.id);
      setSummary(result);
    } catch (err) {
      setSummaryError(errorMessage(err));
    }
  };

  return (
    <div>
      <PageHeader title="Students" subtitle="Search the directory and review attendance" />

      <DataTable
        loading={loading}
        error={error}
        onRetry={reload}
        rows={data?.content || []}
        page={data?.page ?? 0}
        totalPages={data?.totalPages ?? 1}
        totalElements={data?.totalElements}
        onPageChange={setPage}
        search={search}
        onSearchChange={(value) => {
          setSearch(value);
          setPage(0);
        }}
        searchPlaceholder="Search by name or roll number"
        sort={sort}
        onSortChange={setSort}
        columns={[
          { key: 'rollNumber', header: 'Roll number', sortable: true },
          { key: 'fullName', header: 'Name' },
          { key: 'departmentName', header: 'Department' },
          {
            key: 'class',
            header: 'Class',
            render: (row) => `Sem ${row.semester}-${row.division}`,
          },
          {
            key: 'actions',
            header: '',
            render: (row) => (
              <Button size="sm" variant="secondary" onClick={() => openSummary(row)}>
                Attendance
              </Button>
            ),
          },
        ]}
      />

      <Modal
        open={Boolean(selected)}
        title={selected ? `${selected.fullName} · ${selected.rollNumber}` : ''}
        onClose={() => setSelected(null)}
      >
        {summaryError && <p className="text-sm text-red-600">{summaryError}</p>}
        {!summary && !summaryError && <LoadingSpinner label="Loading attendance..." />}
        {summary && (
          <div className="space-y-4">
            <div className="flex items-center justify-between rounded-lg bg-slate-50 p-4">
              <div>
                <p className="text-xs text-slate-500">Overall attendance</p>
                <p className={`text-xl font-semibold ${attendanceTone(summary.overallPercentage)}`}>
                  {percent(summary.overallPercentage)}
                </p>
              </div>
              <div className="text-right text-xs text-slate-500">
                <p>{summary.presentLectures} lectures attended</p>
                <p>{summary.absentLectures} missed</p>
              </div>
            </div>

            <ul className="space-y-2">
              {summary.subjects.map((subject) => (
                <li
                  key={subject.subjectId}
                  className="flex items-center justify-between rounded-lg border border-slate-200 p-3"
                >
                  <div>
                    <p className="text-sm text-slate-800">{subject.subjectName}</p>
                    <p className="text-xs text-slate-500">
                      {subject.presentLectures} of {subject.totalLectures} lectures
                    </p>
                  </div>
                  <Badge tone={subject.percentage >= 75 ? 'success' : 'danger'}>
                    {percent(subject.percentage)}
                  </Badge>
                </li>
              ))}
            </ul>
          </div>
        )}
      </Modal>
    </div>
  );
}
