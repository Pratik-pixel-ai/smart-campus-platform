import { useState } from 'react';
import { Plus } from 'lucide-react';
import useApiData from '../../hooks/useApiData.js';
import useDebouncedValue from '../../hooks/useDebouncedValue.js';
import { classroomApi } from '../../services/endpoints.js';
import { useToast } from '../../context/ToastContext.jsx';
import { errorMessage } from '../../services/api.js';
import PageHeader from '../../components/PageHeader.jsx';
import DataTable from '../../components/DataTable.jsx';
import Button from '../../components/Button.jsx';
import Modal from '../../components/Modal.jsx';
import Input from '../../components/Input.jsx';
import ConfirmDialog from '../../components/ConfirmDialog.jsx';

const EMPTY = { roomNumber: '', building: '', capacity: '60' };

export default function AdminClassrooms() {
  const toast = useToast();
  const [search, setSearch] = useState('');
  const [page, setPage] = useState(0);
  const debouncedSearch = useDebouncedValue(search);

  const { data, loading, error, reload } = useApiData(
    () => classroomApi.list({ search: debouncedSearch || undefined, page, size: 10 }),
    [debouncedSearch, page],
  );

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
      const payload = { ...form, capacity: Number(form.capacity) };
      if (editing) {
        await classroomApi.update(editing.id, payload);
        toast.success('Classroom updated');
      } else {
        await classroomApi.create(payload);
        toast.success('Classroom added');
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
      await classroomApi.remove(toDelete.id);
      toast.success('Classroom removed');
      setToDelete(null);
      reload();
    } catch (err) {
      toast.error(errorMessage(err));
    }
  };

  return (
    <div>
      <PageHeader
        title="Classrooms"
        subtitle="Rooms available for lectures and labs"
        actions={
          <Button
            onClick={() => {
              setEditing(null);
              setForm(EMPTY);
              setOpen(true);
            }}
          >
            <Plus size={16} /> Add classroom
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
        searchPlaceholder="Search by room or building"
        columns={[
          { key: 'roomNumber', header: 'Room' },
          { key: 'building', header: 'Building' },
          { key: 'capacity', header: 'Capacity' },
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
                      roomNumber: row.roomNumber,
                      building: row.building,
                      capacity: String(row.capacity),
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
        title={editing ? 'Edit classroom' : 'Add classroom'}
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
          <Input label="Room number" name="roomNumber" value={form.roomNumber} onChange={change} required />
          <Input label="Building" name="building" value={form.building} onChange={change} required />
          <Input label="Capacity" name="capacity" type="number" min="1" value={form.capacity} onChange={change} required />
        </form>
      </Modal>

      <ConfirmDialog
        open={Boolean(toDelete)}
        title="Remove classroom"
        message={`Room ${toDelete?.roomNumber} will be removed.`}
        onCancel={() => setToDelete(null)}
        onConfirm={remove}
      />
    </div>
  );
}
