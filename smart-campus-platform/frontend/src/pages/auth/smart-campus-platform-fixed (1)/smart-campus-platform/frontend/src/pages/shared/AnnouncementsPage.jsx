import { useState } from 'react';
import { Megaphone, Plus, Trash2 } from 'lucide-react';
import useApiData from '../../hooks/useApiData.js';
import { announcementApi, departmentApi } from '../../services/endpoints.js';
import { useAuth } from '../../context/AuthContext.jsx';
import { useToast } from '../../context/ToastContext.jsx';
import { errorMessage } from '../../services/api.js';
import { PRIORITIES, ROLES } from '../../utils/constants.js';
import { formatDateTime, titleCase } from '../../utils/format.js';
import PageHeader from '../../components/PageHeader.jsx';
import Button from '../../components/Button.jsx';
import Badge, { statusTone } from '../../components/Badge.jsx';
import Modal from '../../components/Modal.jsx';
import Input from '../../components/Input.jsx';
import Select from '../../components/Select.jsx';
import Textarea from '../../components/Textarea.jsx';
import LoadingSpinner from '../../components/LoadingSpinner.jsx';
import ErrorState from '../../components/ErrorState.jsx';
import EmptyState from '../../components/EmptyState.jsx';
import ConfirmDialog from '../../components/ConfirmDialog.jsx';

const EMPTY = { title: '', message: '', departmentId: '', priority: 'NORMAL' };

export default function AnnouncementsPage() {
  const { user } = useAuth();
  const toast = useToast();
  const { data, loading, error, reload } = useApiData(() => announcementApi.list(), []);
  const { data: departments } = useApiData(() => departmentApi.list(), []);

  const [form, setForm] = useState(EMPTY);
  const [open, setOpen] = useState(false);
  const [saving, setSaving] = useState(false);
  const [toDelete, setToDelete] = useState(null);

  const canPost = user?.role === ROLES.FACULTY || user?.role === ROLES.ADMIN;
  const change = (event) => setForm({ ...form, [event.target.name]: event.target.value });

  const submit = async (event) => {
    event.preventDefault();
    setSaving(true);
    try {
      await announcementApi.create({
        ...form,
        departmentId: form.departmentId ? Number(form.departmentId) : null,
      });
      toast.success('Announcement posted');
      setOpen(false);
      setForm(EMPTY);
      reload();
    } catch (err) {
      toast.error(errorMessage(err));
    } finally {
      setSaving(false);
    }
  };

  const remove = async () => {
    try {
      await announcementApi.remove(toDelete.id);
      toast.success('Announcement removed');
      setToDelete(null);
      reload();
    } catch (err) {
      toast.error(errorMessage(err));
    }
  };

  return (
    <div>
      <PageHeader
        title="Announcements"
        subtitle="Notices from your department and the campus administration"
        actions={
          canPost && (
            <Button onClick={() => setOpen(true)}>
              <Plus size={16} /> New announcement
            </Button>
          )
        }
      />

      {loading && <LoadingSpinner />}
      {!loading && error && <ErrorState message={error} onRetry={reload} />}
      {!loading && !error && (data || []).length === 0 && (
        <div className="card">
          <EmptyState icon={Megaphone} title="No announcements yet" message="New notices will appear here." />
        </div>
      )}

      <div className="space-y-3">
        {(data || []).map((item) => (
          <article key={item.id} className="card p-5">
            <div className="flex items-start justify-between gap-3">
              <div className="min-w-0">
                <div className="flex flex-wrap items-center gap-2">
                  <h3 className="text-sm font-semibold text-slate-900">{item.title}</h3>
                  <Badge tone={statusTone(item.priority)}>{titleCase(item.priority)}</Badge>
                  <Badge>{item.departmentName}</Badge>
                </div>
                <p className="mt-2 text-sm text-slate-600">{item.message}</p>
                <p className="mt-3 text-xs text-slate-400">
                  {item.createdByName} · {formatDateTime(item.createdAt)}
                </p>
              </div>
              {canPost && (
                <button
                  type="button"
                  onClick={() => setToDelete(item)}
                  className="rounded-lg p-2 text-slate-400 hover:bg-red-50 hover:text-red-600"
                  aria-label="Delete announcement"
                >
                  <Trash2 size={16} />
                </button>
              )}
            </div>
          </article>
        ))}
      </div>

      <Modal
        open={open}
        title="Post an announcement"
        onClose={() => setOpen(false)}
        footer={
          <>
            <Button variant="secondary" onClick={() => setOpen(false)}>
              Cancel
            </Button>
            <Button onClick={submit} loading={saving}>
              Post
            </Button>
          </>
        }
      >
        <form onSubmit={submit} className="space-y-4">
          <Input label="Title" name="title" value={form.title} onChange={change} required />
          <Textarea label="Message" name="message" value={form.message} onChange={change} required />
          <div className="grid gap-4 sm:grid-cols-2">
            <Select
              label="Department"
              name="departmentId"
              value={form.departmentId}
              onChange={change}
              placeholder="All departments"
              options={(departments || []).map((department) => ({
                value: department.id,
                label: department.name,
              }))}
            />
            <Select
              label="Priority"
              name="priority"
              value={form.priority}
              onChange={change}
              options={PRIORITIES.map((priority) => ({ value: priority, label: titleCase(priority) }))}
            />
          </div>
        </form>
      </Modal>

      <ConfirmDialog
        open={Boolean(toDelete)}
        title="Delete announcement"
        message={`"${toDelete?.title}" will be removed for everyone.`}
        onCancel={() => setToDelete(null)}
        onConfirm={remove}
      />
    </div>
  );
}
