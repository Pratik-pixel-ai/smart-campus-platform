import { useState } from 'react';
import { FileText, Plus, Trash2, Users } from 'lucide-react';
import useApiData from '../../hooks/useApiData.js';
import { assignmentApi, subjectApi, submissionApi } from '../../services/endpoints.js';
import { useAuth } from '../../context/AuthContext.jsx';
import { useToast } from '../../context/ToastContext.jsx';
import { errorMessage } from '../../services/api.js';
import { formatDateTime, toLocalInputValue } from '../../utils/format.js';
import PageHeader from '../../components/PageHeader.jsx';
import Button from '../../components/Button.jsx';
import Modal from '../../components/Modal.jsx';
import Input from '../../components/Input.jsx';
import Select from '../../components/Select.jsx';
import Textarea from '../../components/Textarea.jsx';
import Badge, { statusTone } from '../../components/Badge.jsx';
import LoadingSpinner from '../../components/LoadingSpinner.jsx';
import ErrorState from '../../components/ErrorState.jsx';
import EmptyState from '../../components/EmptyState.jsx';
import ConfirmDialog from '../../components/ConfirmDialog.jsx';

const emptyForm = () => ({
  title: '',
  description: '',
  subjectId: '',
  deadline: toLocalInputValue(new Date(Date.now() + 7 * 24 * 60 * 60 * 1000)),
  maxMarks: '20',
  attachmentUrl: '',
  division: 'A',
});

export default function FacultyAssignments() {
  const { user } = useAuth();
  const toast = useToast();

  const assignments = useApiData(() => assignmentApi.list(), []);
  const { data: subjects } = useApiData(() => subjectApi.mine(user.profileId), [user?.profileId]);

  const [form, setForm] = useState(emptyForm());
  const [editing, setEditing] = useState(null);
  const [open, setOpen] = useState(false);
  const [saving, setSaving] = useState(false);
  const [toDelete, setToDelete] = useState(null);

  const [submissionsFor, setSubmissionsFor] = useState(null);
  const [submissions, setSubmissions] = useState([]);
  const [grading, setGrading] = useState({});

  const change = (event) => setForm({ ...form, [event.target.name]: event.target.value });

  const openCreate = () => {
    setEditing(null);
    setForm(emptyForm());
    setOpen(true);
  };

  const openEdit = (assignment) => {
    setEditing(assignment);
    setForm({
      title: assignment.title,
      description: assignment.description,
      subjectId: String(assignment.subjectId),
      deadline: assignment.deadline?.slice(0, 16) || emptyForm().deadline,
      maxMarks: String(assignment.maxMarks),
      attachmentUrl: assignment.attachmentUrl || '',
      division: assignment.division,
    });
    setOpen(true);
  };

  const save = async (event) => {
    event.preventDefault();
    const subject = (subjects || []).find((item) => String(item.id) === String(form.subjectId));
    if (!subject) {
      toast.error('Select a subject');
      return;
    }
    setSaving(true);
    try {
      const payload = {
        title: form.title,
        description: form.description,
        subjectId: Number(form.subjectId),
        deadline: form.deadline.length === 16 ? `${form.deadline}:00` : form.deadline,
        maxMarks: Number(form.maxMarks),
        attachmentUrl: form.attachmentUrl || null,
        semester: subject.semester,
        division: form.division,
      };
      if (editing) {
        await assignmentApi.update(editing.id, payload);
        toast.success('Assignment updated');
      } else {
        await assignmentApi.create(payload);
        toast.success('Assignment created');
      }
      setOpen(false);
      assignments.reload();
    } catch (err) {
      toast.error(errorMessage(err));
    } finally {
      setSaving(false);
    }
  };

  const remove = async () => {
    try {
      await assignmentApi.remove(toDelete.id);
      toast.success('Assignment removed');
      setToDelete(null);
      assignments.reload();
    } catch (err) {
      toast.error(errorMessage(err));
    }
  };

  const openSubmissions = async (assignment) => {
    setSubmissionsFor(assignment);
    setSubmissions([]);
    try {
      const { data } = await submissionApi.forAssignment(assignment.id);
      setSubmissions(data);
    } catch (err) {
      toast.error(errorMessage(err));
    }
  };

  const grade = async (submission) => {
    const entry = grading[submission.id] || {};
    try {
      const { data } = await submissionApi.grade(submission.id, {
        marksObtained: Number(entry.marks ?? submission.marksObtained ?? 0),
        feedback: entry.feedback ?? submission.feedback ?? '',
      });
      setSubmissions((current) => current.map((item) => (item.id === data.id ? data : item)));
      toast.success('Marks saved');
    } catch (err) {
      toast.error(errorMessage(err));
    }
  };

  if (assignments.loading) return <LoadingSpinner />;
  if (assignments.error) return <ErrorState message={assignments.error} onRetry={assignments.reload} />;

  return (
    <div>
      <PageHeader
        title="Assignments"
        subtitle="Set work, track submissions and enter marks"
        actions={
          <Button onClick={openCreate}>
            <Plus size={16} /> New assignment
          </Button>
        }
      />

      {(assignments.data || []).length === 0 ? (
        <div className="card">
          <EmptyState icon={FileText} title="No assignments yet" message="Create one to get started." />
        </div>
      ) : (
        <div className="grid gap-4 md:grid-cols-2">
          {assignments.data.map((assignment) => (
            <article key={assignment.id} className="card flex flex-col p-5">
              <div className="flex items-start justify-between gap-2">
                <div>
                  <h3 className="text-sm font-semibold text-slate-900">{assignment.title}</h3>
                  <p className="text-xs text-slate-500">
                    {assignment.subjectName} · Sem {assignment.semester}-{assignment.division}
                  </p>
                </div>
                <Badge tone={assignment.overdue ? 'neutral' : 'info'}>
                  {assignment.overdue ? 'Closed' : 'Open'}
                </Badge>
              </div>

              <p className="mt-3 flex-1 text-sm text-slate-600">{assignment.description}</p>

              <div className="mt-4 flex flex-wrap items-center justify-between gap-2 border-t border-slate-100 pt-3 text-xs text-slate-500">
                <span>
                  Due {formatDateTime(assignment.deadline)} · {assignment.submissionCount} submissions
                </span>
                <div className="flex gap-1">
                  <Button size="sm" variant="secondary" onClick={() => openSubmissions(assignment)}>
                    <Users size={14} /> Submissions
                  </Button>
                  <Button size="sm" variant="ghost" onClick={() => openEdit(assignment)}>
                    Edit
                  </Button>
                  <Button size="sm" variant="ghost" onClick={() => setToDelete(assignment)}>
                    <Trash2 size={14} />
                  </Button>
                </div>
              </div>
            </article>
          ))}
        </div>
      )}

      <Modal
        open={open}
        title={editing ? 'Edit assignment' : 'New assignment'}
        onClose={() => setOpen(false)}
        footer={
          <>
            <Button variant="secondary" onClick={() => setOpen(false)}>
              Cancel
            </Button>
            <Button onClick={save} loading={saving}>
              {editing ? 'Save changes' : 'Create'}
            </Button>
          </>
        }
      >
        <form onSubmit={save} className="space-y-4">
          <Input label="Title" name="title" value={form.title} onChange={change} required />
          <Textarea label="Description" name="description" value={form.description} onChange={change} required />
          <div className="grid gap-4 sm:grid-cols-2">
            <Select
              label="Subject"
              name="subjectId"
              value={form.subjectId}
              onChange={change}
              placeholder="Select a subject"
              required
              options={(subjects || []).map((subject) => ({
                value: subject.id,
                label: `${subject.name} · Sem ${subject.semester}`,
              }))}
            />
            <Input label="Division" name="division" value={form.division} onChange={change} required />
            <Input
              label="Deadline"
              name="deadline"
              type="datetime-local"
              value={form.deadline}
              onChange={change}
              required
            />
            <Input
              label="Maximum marks"
              name="maxMarks"
              type="number"
              min="1"
              value={form.maxMarks}
              onChange={change}
              required
            />
          </div>
          <Input
            label="Attachment link (optional)"
            name="attachmentUrl"
            value={form.attachmentUrl}
            onChange={change}
            placeholder="https://..."
          />
        </form>
      </Modal>

      <Modal
        open={Boolean(submissionsFor)}
        title={submissionsFor ? `Submissions: ${submissionsFor.title}` : ''}
        onClose={() => setSubmissionsFor(null)}
        width="max-w-2xl"
      >
        {submissions.length === 0 ? (
          <EmptyState title="No submissions yet" message="Students have not submitted this assignment." />
        ) : (
          <ul className="space-y-3">
            {submissions.map((submission) => (
              <li key={submission.id} className="rounded-lg border border-slate-200 p-4">
                <div className="flex items-center justify-between gap-2">
                  <div>
                    <p className="text-sm font-medium text-slate-800">{submission.studentName}</p>
                    <p className="text-xs text-slate-500">
                      {submission.rollNumber} · submitted {formatDateTime(submission.submittedAt)}
                    </p>
                  </div>
                  <Badge tone={statusTone(submission.status)}>{submission.status}</Badge>
                </div>

                <a
                  href={submission.submissionUrl}
                  target="_blank"
                  rel="noreferrer"
                  className="mt-2 block break-all text-xs text-brand-700 hover:underline"
                >
                  {submission.submissionUrl}
                </a>

                <div className="mt-3 flex flex-wrap items-end gap-2">
                  <Input
                    label={`Marks (max ${submission.maxMarks})`}
                    type="number"
                    min="0"
                    max={submission.maxMarks}
                    className="w-32"
                    defaultValue={submission.marksObtained ?? ''}
                    onChange={(event) =>
                      setGrading((current) => ({
                        ...current,
                        [submission.id]: { ...current[submission.id], marks: event.target.value },
                      }))
                    }
                  />
                  <Input
                    label="Feedback"
                    className="flex-1"
                    defaultValue={submission.feedback ?? ''}
                    onChange={(event) =>
                      setGrading((current) => ({
                        ...current,
                        [submission.id]: { ...current[submission.id], feedback: event.target.value },
                      }))
                    }
                  />
                  <Button size="sm" onClick={() => grade(submission)}>
                    Save
                  </Button>
                </div>
              </li>
            ))}
          </ul>
        )}
      </Modal>

      <ConfirmDialog
        open={Boolean(toDelete)}
        title="Delete assignment"
        message={`"${toDelete?.title}" and all of its submissions will be removed.`}
        onCancel={() => setToDelete(null)}
        onConfirm={remove}
      />
    </div>
  );
}
