import { useState } from 'react';
import { FileText, Upload } from 'lucide-react';
import useApiData from '../../hooks/useApiData.js';
import { assignmentApi, submissionApi } from '../../services/endpoints.js';
import { useToast } from '../../context/ToastContext.jsx';
import { errorMessage } from '../../services/api.js';
import { formatDateTime } from '../../utils/format.js';
import PageHeader from '../../components/PageHeader.jsx';
import Button from '../../components/Button.jsx';
import Badge, { statusTone } from '../../components/Badge.jsx';
import Modal from '../../components/Modal.jsx';
import Input from '../../components/Input.jsx';
import Textarea from '../../components/Textarea.jsx';
import LoadingSpinner from '../../components/LoadingSpinner.jsx';
import ErrorState from '../../components/ErrorState.jsx';
import EmptyState from '../../components/EmptyState.jsx';

export default function StudentAssignments() {
  const toast = useToast();
  const { data, loading, error, reload } = useApiData(() => assignmentApi.list(), []);

  const [active, setActive] = useState(null);
  const [form, setForm] = useState({ submissionUrl: '', remarks: '' });
  const [saving, setSaving] = useState(false);

  const openSubmit = (assignment) => {
    setActive(assignment);
    setForm({ submissionUrl: '', remarks: '' });
  };

  const submit = async (event) => {
    event.preventDefault();
    setSaving(true);
    try {
      await submissionApi.submit({ assignmentId: active.id, ...form });
      toast.success('Assignment submitted');
      setActive(null);
      reload();
    } catch (err) {
      toast.error(errorMessage(err));
    } finally {
      setSaving(false);
    }
  };

  if (loading) return <LoadingSpinner />;
  if (error) return <ErrorState message={error} onRetry={reload} />;

  const assignments = data || [];

  return (
    <div>
      <PageHeader title="Assignments" subtitle="Everything set for your class this semester" />

      {assignments.length === 0 ? (
        <div className="card">
          <EmptyState icon={FileText} title="No assignments yet" message="Your faculty has not set any work." />
        </div>
      ) : (
        <div className="grid gap-4 md:grid-cols-2">
          {assignments.map((assignment) => (
            <article key={assignment.id} className="card flex flex-col p-5">
              <div className="flex items-start justify-between gap-2">
                <div>
                  <h3 className="text-sm font-semibold text-slate-900">{assignment.title}</h3>
                  <p className="text-xs text-slate-500">
                    {assignment.subjectName} ({assignment.subjectCode}) · {assignment.facultyName}
                  </p>
                </div>
                <Badge tone={statusTone(assignment.submissionStatus)}>{assignment.submissionStatus}</Badge>
              </div>

              <p className="mt-3 flex-1 text-sm text-slate-600">{assignment.description}</p>

              <div className="mt-4 flex flex-wrap items-center justify-between gap-2 border-t border-slate-100 pt-3">
                <div className="text-xs text-slate-500">
                  <p>Due {formatDateTime(assignment.deadline)}</p>
                  <p>
                    Maximum marks {assignment.maxMarks}
                    {assignment.marksObtained != null && ` · scored ${assignment.marksObtained}`}
                  </p>
                </div>
                {assignment.submissionStatus === 'GRADED' ? (
                  <Badge tone="success">Graded</Badge>
                ) : (
                  <Button size="sm" onClick={() => openSubmit(assignment)}>
                    <Upload size={14} />
                    {assignment.submissionStatus === 'PENDING' ? 'Submit' : 'Resubmit'}
                  </Button>
                )}
              </div>

              {assignment.overdue && assignment.submissionStatus === 'PENDING' && (
                <p className="mt-2 text-xs text-red-600">The deadline has passed. Submissions are marked late.</p>
              )}
            </article>
          ))}
        </div>
      )}

      <Modal
        open={Boolean(active)}
        title={active ? `Submit: ${active.title}` : ''}
        onClose={() => setActive(null)}
        footer={
          <>
            <Button variant="secondary" onClick={() => setActive(null)}>
              Cancel
            </Button>
            <Button onClick={submit} loading={saving}>
              Submit
            </Button>
          </>
        }
      >
        <form onSubmit={submit} className="space-y-4">
          <Input
            label="Submission link"
            name="submissionUrl"
            value={form.submissionUrl}
            onChange={(event) => setForm({ ...form, submissionUrl: event.target.value })}
            placeholder="https://drive.google.com/..."
            required
          />
          <Textarea
            label="Remarks (optional)"
            name="remarks"
            value={form.remarks}
            onChange={(event) => setForm({ ...form, remarks: event.target.value })}
            placeholder="Anything your faculty should know"
          />
          <p className="text-xs text-slate-500">
            Submissions are stored as links, so no file upload service is needed for the demo.
          </p>
        </form>
      </Modal>
    </div>
  );
}
