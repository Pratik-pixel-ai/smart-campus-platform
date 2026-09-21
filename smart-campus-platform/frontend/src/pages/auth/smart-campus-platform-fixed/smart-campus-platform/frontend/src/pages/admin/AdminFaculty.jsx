import { useState } from 'react';
import { Plus } from 'lucide-react';
import useApiData from '../../hooks/useApiData.js';
import useDebouncedValue from '../../hooks/useDebouncedValue.js';
import { departmentApi, facultyApi } from '../../services/endpoints.js';
import { useToast } from '../../context/ToastContext.jsx';
import { errorMessage } from '../../services/api.js';
import PageHeader from '../../components/PageHeader.jsx';
import DataTable from '../../components/DataTable.jsx';
import Button from '../../components/Button.jsx';
import Modal from '../../components/Modal.jsx';
import Input from '../../components/Input.jsx';
import Select from '../../components/Select.jsx';
import ConfirmDialog from '../../components/ConfirmDialog.jsx';

const EMPTY = {
  fullName: '',
  email: '',
  password: '',
  employeeCode: '',
  departmentId: '',
  designation: 'Assistant Professor',
  phone: '',
};

export default function AdminFaculty() {
  const toast = useToast();
  const [search, setSearch] = useState('');
  const [page, setPage] = useState(0);
  const debouncedSearch = useDebouncedValue(search);

  const { data, loading, error, reload } = useApiData(
    () => facultyApi.list({ search: debouncedSearch || undefined, page, size: 10 }),
    [debouncedSearch, page],
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

  const openEdit = (member) => {
    setEditing(member);
    setForm({
      ...EMPTY,
      fullName: member.fullName,
      email: member.email,
      employeeCode: member.employeeCode,
      departmentId: String(member.departmentId),
      designation: member.designation || '',
      phone: member.phone || '',
    });
    setOpen(true);
  };

  const save = async (event) => {
    event.preventDefault();
    setSaving(true);
    try {
      if (editing) {
        await facultyApi.update(editing.id, {
          fullName: form.fullName,
          departmentId: Number(form.departmentId),
          designation: form.designation,
          phone: form.phone,
          active: true,
        });
        toast.success('Faculty member updated');
      } else {
        await facultyApi.create({ ...form, departmentId: Number(form.departmentId) });
        toast.success('Faculty member added');
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
      await facultyApi.remove(toDelete.id);
      toast.success('Faculty member removed');
      setToDelete(null);
      reload();
    } catch (err) {
      toast.error(errorMessage(err));
    }
  };

  return (
    <div>
      <PageHeader
        title="Faculty"
        subtitle="Teaching staff accounts"
        actions={
          <Button onClick={openCreate}>
            <Plus size={16} /> Add faculty
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
        searchPlaceholder="Search by name or employee code"
        columns={[
          { key: 'employeeCode', header: 'Employee code' },
          { key: 'fullName', header: 'Name' },
          { key: 'email', header: 'Email' },
          { key: 'departmentName', header: 'Department' },
          { key: 'designation', header: 'Designation' },
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
        title={editing ? 'Edit faculty member' : 'Add faculty member'}
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
            label="Employee code"
            name="employeeCode"
            value={form.employeeCode}
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
          <Input label="Designation" name="designation" value={form.designation} onChange={change} />
          <Input label="Phone" name="phone" value={form.phone} onChange={change} />
        </form>
      </Modal>

      <ConfirmDialog
        open={Boolean(toDelete)}
        title="Remove faculty member"
        message={`${toDelete?.fullName} and their login will be deleted.`}
        onCancel={() => setToDelete(null)}
        onConfirm={remove}
      />
    </div>
  );
}
