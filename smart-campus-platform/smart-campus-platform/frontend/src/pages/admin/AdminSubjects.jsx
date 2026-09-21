import { useState } from 'react';
import { Plus } from 'lucide-react';
import useApiData from '../../hooks/useApiData.js';
import useDebouncedValue from '../../hooks/useDebouncedValue.js';
import { departmentApi, facultyApi, subjectApi } from '../../services/endpoints.js';
import { useToast } from '../../context/ToastContext.jsx';
import { errorMessage } from '../../services/api.js';
import PageHeader from '../../components/PageHeader.jsx';
import DataTable from '../../components/DataTable.jsx';
import Button from '../../components/Button.jsx';
import Modal from '../../components/Modal.jsx';
import Input from '../../components/Input.jsx';
import Select from '../../components/Select.jsx';
import ConfirmDialog from '../../components/ConfirmDialog.jsx';

const EMPTY = { name: '', code: '', departmentId: '', facultyId: '', semester: '7', credits: '4' };

export default function AdminSubjects() {
  const toast = useToast();
  const [search, setSearch] = useState('');
  const [page, setPage] = useState(0);
  const debouncedSearch = useDebouncedValue(search);

  const { data, loading, error, reload } = useApiData(
    () => subjectApi.list({ search: debouncedSearch || undefined, page, size: 10 }),
    [debouncedSearch, page],
  );
  const { data: departments } = useApiData(() => departmentApi.list(), []);
  const { data: facultyOptions } = useApiData(() => facultyApi.options(), []);

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
      const payload = {
        name: form.name,
        code: form.code,
        departmentId: Number(form.departmentId),
        facultyId: form.facultyId ? Number(form.facultyId) : null,
        semester: Number(form.semester),
        credits: Number(form.credits),
      };
      if (editing) {
        await subjectApi.update(editing.id, payload);
        toast.success('Subject updated');
      } else {
        await subjectApi.create(payload);
        toast.success('Subject added');
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
      await subjectApi.remove(toDelete.id);
      toast.success('Subject removed');
      setToDelete(null);
      reload();
    } catch (err) {
      toast.error(errorMessage(err));
    }
  };

  return (
    <div>
      <PageHeader
        title="Subjects"
        subtitle="Course catalogue and the faculty assigned to each subject"
        actions={
          <Button
            onClick={() => {
              setEditing(null);
              setForm(EMPTY);
              setOpen(true);
            }}
          >
            <Plus size={16} /> Add subject
          </Button>
        }
      />

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
        searchPlaceholder="Search by name or code"
        columns={[
          { key: 'code', header: 'Code' },
          { key: 'name', header: 'Subject' },
          { key: 'departmentName', header: 'Department' },
          { key: 'semester', header: 'Semester' },
          { key: 'credits', header: 'Credits' },
          { key: 'facultyName', header: 'Faculty', render: (row) => row.facultyName || 'Unassigned' },
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
                    setForm({
                      name: row.name,
                      code: row.code,
                      departmentId: String(row.departmentId),
                      facultyId: row.facultyId ? String(row.facultyId) : '',
                      semester: String(row.semester),
                      credits: String(row.credits),
                    });
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
        title={editing ? 'Edit subject' : 'Add subject'}
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
        <form onSubmit={save} className="grid gap-4 sm:grid-cols-2">
          <Input label="Subject name" name="name" value={form.name} onChange={change} required className="sm:col-span-2" />
          <Input label="Code" name="code" value={form.code} onChange={change} required />
          <Select
            label="Department"
            name="departmentId"
            value={form.departmentId}
            onChange={change}
            placeholder="Select a department"
            required
            options={(departments || []).map((department) => ({ value: department.id, label: department.name }))}
          />
          <Select
            label="Faculty"
            name="facultyId"
            value={form.facultyId}
            onChange={change}
            placeholder="Unassigned"
            options={(facultyOptions || []).map((member) => ({
              value: member.id,
              label: `${member.fullName} (${member.employeeCode})`,
            }))}
          />
          <Input label="Semester" name="semester" type="number" min="1" max="8" value={form.semester} onChange={change} required />
          <Input label="Credits" name="credits" type="number" min="1" max="10" value={form.credits} onChange={change} required />
        </form>
      </Modal>

      <ConfirmDialog
        open={Boolean(toDelete)}
        title="Remove subject"
        message={`${toDelete?.name} will be removed from the catalogue.`}
        onCancel={() => setToDelete(null)}
        onConfirm={remove}
      />
    </div>
  );
}
