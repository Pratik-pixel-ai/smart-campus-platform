import { useState } from 'react';
import { Plus } from 'lucide-react';
import useApiData from '../../hooks/useApiData.js';
import { departmentApi } from '../../services/endpoints.js';
import { useToast } from '../../context/ToastContext.jsx';
import { errorMessage } from '../../services/api.js';
import PageHeader from '../../components/PageHeader.jsx';
import DataTable from '../../components/DataTable.jsx';
import Button from '../../components/Button.jsx';
import Modal from '../../components/Modal.jsx';
import Input from '../../components/Input.jsx';
import ConfirmDialog from '../../components/ConfirmDialog.jsx';

const EMPTY = { name: '', code: '', hodName: '' };

export default function AdminDepartments() {
  const toast = useToast();
  const { data, loading, error, reload } = useApiData(() => departmentApi.list(), []);

  const [open, setOpen] = useState(false);
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState(EMPTY);
  const [saving, setSaving] = useState(false);
  const [toDelete, setToDelete] = useState(null);

  const change = (event) => setForm({ ...form, [event.target.name]: event.target.value });

  const save = async (event) => {
    event.preventDefault();
    setSaving(true);
    try {
      if (editing) {
        await departmentApi.update(editing.id, form);
        toast.success('Department updated');
      } else {
        await departmentApi.create(form);
        toast.success('Department added');
      }
      setOpen(false);
      reload();
    } catch (err) {
      toast.error(errorMessage(err));
    } finally {
      setSaving(false);
    }
  };

  const remove = async () => {
    try {
      await departmentApi.remove(toDelete.id);
      toast.success('Department removed');
      setToDelete(null);
      reload();
    } catch (err) {
      toast.error(errorMessage(err));
    }
  };

  return (
    <div>
      <PageHeader
        title="Departments"
        subtitle="Academic departments and their heads"
        actions={
          <Button
            onClick={() => {
              setEditing(null);
              setForm(EMPTY);
              setOpen(true);
            }}
          >
            <Plus size={16} /> Add department
          </Button>
        }
      />

      <DataTable
        loading={loading}
        error={error}
        onRetry={reload}
        rows={data || []}
        emptyTitle="No departments yet"
        columns={[
          { key: 'code', header: 'Code' },
          { key: 'name', header: 'Name' },
          { key: 'hodName', header: 'Head of department', render: (row) => row.hodName || '-' },
          { key: 'studentCount', header: 'Students' },
          { key: 'facultyCount', header: 'Faculty' },
          {
            key: 'actions',
            header: '',
            render: (row) => (
              <div className="flex gap-1">
                <Button
                  size="sm"
                  variant="ghost"
                  onClick={() => {
                    setEditing(row);
                    setForm({ name: row.name, code: row.code, hodName: row.hodName || '' });
                    setOpen(true);
                  }}
                >
                  Edit
                </Button>
                <Button size="sm" variant="ghost" onClick={() => setToDelete(row)}>
                  Delete
                </Button>
              </div>
            ),
          },
        ]}
      />

      <Modal
        open={open}
        title={editing ? 'Edit department' : 'Add department'}
        onClose={() => setOpen(false)}
        footer={
          <>
            <Button variant="secondary" onClick={() => setOpen(false)}>
              Cancel
            </Button>
            <Button onClick={save} loading={saving}>
              {editing ? 'Save changes' : 'Add'}
            </Button>
          </>
        }
      >
        <form onSubmit={save} className="space-y-4">
          <Input label="Name" name="name" value={form.name} onChange={change} required />
          <Input label="Code" name="code" value={form.code} onChange={change} maxLength={10} required />
          <Input label="Head of department" name="hodName" value={form.hodName} onChange={change} />
        </form>
      </Modal>

      <ConfirmDialog
        open={Boolean(toDelete)}
        title="Remove department"
        message={`${toDelete?.name} will be removed. Departments with students or subjects cannot be deleted.`}
        onCancel={() => setToDelete(null)}
        onConfirm={remove}
      />
    </div>
  );
}
