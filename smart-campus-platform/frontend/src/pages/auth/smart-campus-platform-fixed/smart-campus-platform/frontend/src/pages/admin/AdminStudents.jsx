import { useState } from 'react';
import { Plus } from 'lucide-react';
import useApiData from '../../hooks/useApiData.js';
import useDebouncedValue from '../../hooks/useDebouncedValue.js';
import { departmentApi, studentApi } from '../../services/endpoints.js';
import { useToast } from '../../context/ToastContext.jsx';
import { errorMessage } from '../../services/api.js';
import PageHeader from '../../components/PageHeader.jsx';
import DataTable from '../../components/DataTable.jsx';
import Button from '../../components/Button.jsx';
import Modal from '../../components/Modal.jsx';
import Input from '../../components/Input.jsx';
import Select from '../../components/Select.jsx';
import Badge from '../../components/Badge.jsx';
import ConfirmDialog from '../../components/ConfirmDialog.jsx';

const EMPTY = {
  fullName: '',
  email: '',
  password: '',
  rollNumber: '',
  departmentId: '',
  semester: '7',
  division: 'A',
  phone: '',
  bleDeviceId: '',
};

export default function AdminStudents() {
  const toast = useToast();
  const [search, setSearch] = useState('');
  const [page, setPage] = useState(0);
  const [sort, setSort] = useState('rollNumber,asc');
  const debouncedSearch = useDebouncedValue(search);

  const { data, loading, error, reload } = useApiData(
    () => studentApi.list({ search: debouncedSearch || undefined, page, size: 10, sort }),
    [debouncedSearch, page, sort],
  );
  const { data: departments } = useApiData(() => departmentApi.list(), []);

  const [open, setOpen] = useState(false);
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState(EMPTY);
  const [saving, setSaving] = useState(false);
  const [toDelete, setToDelete] = useState(null);

  const change = (event) => setForm({ ...form, [event.target.name]: event.target.value });

  const openCreate = () => {
    setEditing(null);
    setForm(EMPTY);
    setOpen(true);
  };

  const openEdit = (student) => {
    setEditing(student);
    setForm({
      ...EMPTY,
      fullName: student.fullName,
      email: student.email,
      rollNumber: student.rollNumber,
      departmentId: String(student.departmentId),
      semester: String(student.semester),
      division: student.division,
      phone: student.phone || '',
      bleDeviceId: student.bleDeviceId || '',
    });
    setOpen(true);
  };

  const save = async (event) => {
    event.preventDefault();
    setSaving(true);
    try {
      if (editing) {
        await studentApi.update(editing.id, {
          fullName: form.fullName,
          departmentId: Number(form.departmentId),
          semester: Number(form.semester),
          division: form.division,
          phone: form.phone,
          bleDeviceId: form.bleDeviceId,
          active: true,
        });
        toast.success('Student updated');
      } else {
        await studentApi.create({
          ...form,
          departmentId: Number(form.departmentId),
          semester: Number(form.semester),
        });
        toast.success('Student added');
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
      await studentApi.remove(toDelete.id);
      toast.success('Student removed');
      setToDelete(null);
      reload();
    } catch (err) {
      toast.error(errorMessage(err));
    }
  };

  return (
    <div>
      <PageHeader
        title="Students"
        subtitle="Add, edit and remove student accounts"
        actions={
          <Button onClick={openCreate}>
            <Plus size={16} /> Add student
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
        searchPlaceholder="Search by name or roll number"
        sort={sort}
        onSortChange={setSort}
        columns={[
          { key: 'rollNumber', header: 'Roll number', sortable: true },
          { key: 'fullName', header: 'Name' },
          { key: 'email', header: 'Email' },
          { key: 'departmentName', header: 'Department' },
          { key: 'class', header: 'Class', render: (row) => `Sem ${row.semester}-${row.division}` },
          {
            key: 'bleDeviceId',
            header: 'BLE device',
            render: (row) =>
              row.bleDeviceId ? <Badge tone="info">{row.bleDeviceId}</Badge> : <span className="text-xs text-slate-400">Not set</span>,
          },
          {
            key: 'actions',
            header: '',
            render: (row) => (
              <div className="flex gap-1">
                <Button size="sm" variant="ghost" onClick={() => openEdit(row)}>
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
        title={editing ? 'Edit student' : 'Add student'}
        onClose={() => setOpen(false)}
        footer={
          <>
            <Button variant="secondary" onClick={() => setOpen(false)}>
              Cancel
            </Button>
            <Button onClick={save} loading={saving}>
              {editing ? 'Save changes' : 'Add student'}
            </Button>
          </>
        }
      >
        <form onSubmit={save} className="grid gap-4 sm:grid-cols-2">
          <Input label="Full name" name="fullName" value={form.fullName} onChange={change} required />
          <Input
            label="Email"
            name="email"
            type="email"
            value={form.email}
            onChange={change}
            disabled={Boolean(editing)}
            required
          />
          {!editing && (
            <Input
              label="Password"
              name="password"
              type="password"
              value={form.password}
              onChange={change}
              minLength={8}
              required
            />
          )}
          <Input
            label="Roll number"
            name="rollNumber"
            value={form.rollNumber}
            onChange={change}
            disabled={Boolean(editing)}
            required
          />
          <Select
            label="Department"
            name="departmentId"
            value={form.departmentId}
            onChange={change}
            placeholder="Select a department"
            required
            options={(departments || []).map((department) => ({
              value: department.id,
              label: department.name,
            }))}
          />
          <Input label="Semester" name="semester" type="number" min="1" max="8" value={form.semester} onChange={change} required />
          <Input label="Division" name="division" value={form.division} onChange={change} required />
          <Input label="Phone" name="phone" value={form.phone} onChange={change} />
          <Input
            label="BLE device id"
            name="bleDeviceId"
            value={form.bleDeviceId}
            onChange={change}
            placeholder="BLE-IT-1"
            className="sm:col-span-2"
          />
        </form>
      </Modal>

      <ConfirmDialog
        open={Boolean(toDelete)}
        title="Remove student"
        message={`${toDelete?.fullName} and their login will be deleted.`}
        onCancel={() => setToDelete(null)}
        onConfirm={remove}
      />
    </div>
  );
}
