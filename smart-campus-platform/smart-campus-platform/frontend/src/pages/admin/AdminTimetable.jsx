import { useState } from 'react';
import { Plus } from 'lucide-react';
import useApiData from '../../hooks/useApiData.js';
import { classroomApi, facultyApi, subjectApi, timetableApi } from '../../services/endpoints.js';
import { useToast } from '../../context/ToastContext.jsx';
import { errorMessage } from '../../services/api.js';
import { WEEK_DAYS } from '../../utils/constants.js';
import { titleCase } from '../../utils/format.js';
import PageHeader from '../../components/PageHeader.jsx';
import DataTable from '../../components/DataTable.jsx';
import Button from '../../components/Button.jsx';
import Modal from '../../components/Modal.jsx';
import Input from '../../components/Input.jsx';
import Select from '../../components/Select.jsx';
import ConfirmDialog from '../../components/ConfirmDialog.jsx';

const EMPTY = {
  subjectId: '',
  facultyId: '',
  classroomId: '',
  dayOfWeek: 'MONDAY',
  startTime: '09:00',
  endTime: '10:00',
  semester: '7',
  division: 'A',
};

export default function AdminTimetable() {
  const toast = useToast();
  const { data, loading, error, reload } = useApiData(() => timetableApi.all(), []);
  const { data: subjects } = useApiData(() => subjectApi.options(), []);
  const { data: facultyOptions } = useApiData(() => facultyApi.options(), []);
  const { data: classrooms } = useApiData(() => classroomApi.options(), []);

  const [open, setOpen] = useState(false);
  const [editing, setEditing] = useState(null);
  const [form, setForm] = useState(EMPTY);
  const [saving, setSaving] = useState(false);
  const [toDelete, setToDelete] = useState(null);
  const [dayFilter, setDayFilter] = useState('');

  const change = (event) => setForm({ ...form, [event.target.name]: event.target.value });

  const save = async (event) => {
    event.preventDefault();
    setSaving(true);
    try {
      const payload = {
        ...form,
        subjectId: Number(form.subjectId),
        facultyId: Number(form.facultyId),
        classroomId: form.classroomId ? Number(form.classroomId) : null,
        semester: Number(form.semester),
      };
      if (editing) {
        await timetableApi.update(editing.id, payload);
        toast.success('Timetable entry updated');
      } else {
        await timetableApi.create(payload);
        toast.success('Timetable entry added');
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
      await timetableApi.remove(toDelete.id);
      toast.success('Timetable entry removed');
      setToDelete(null);
      reload();
    } catch (err) {
      toast.error(errorMessage(err));
    }
  };

  const rows = (data || []).filter((entry) => !dayFilter || entry.dayOfWeek === dayFilter);

  return (
    <div>
      <PageHeader
        title="Timetable"
        subtitle="Weekly lecture slots for every class"
        actions={
          <Button
            onClick={() => {
              setEditing(null);
              setForm(EMPTY);
              setOpen(true);
            }}
          >
            <Plus size={16} /> Add slot
          </Button>
        }
      />

      <DataTable
        loading={loading}
        error={error}
        onRetry={reload}
        rows={rows}
        emptyTitle="No timetable entries"
        toolbar={
          <Select
            value={dayFilter}
            onChange={(event) => setDayFilter(event.target.value)}
            placeholder="All days"
            className="w-44"
            options={WEEK_DAYS.map((day) => ({ value: day, label: titleCase(day) }))}
          />
        }
        columns={[
          { key: 'dayOfWeek', header: 'Day', render: (row) => titleCase(row.dayOfWeek) },
          { key: 'time', header: 'Time', render: (row) => `${row.startTime} - ${row.endTime}` },
          { key: 'subjectName', header: 'Subject', render: (row) => `${row.subjectName} (${row.subjectCode})` },
          { key: 'facultyName', header: 'Faculty' },
          { key: 'room', header: 'Room' },
          { key: 'class', header: 'Class', render: (row) => `Sem ${row.semester}-${row.division}` },
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
                      subjectId: String(row.subjectId),
                      facultyId: String(row.facultyId),
                      classroomId: row.classroomId ? String(row.classroomId) : '',
                      dayOfWeek: row.dayOfWeek,
                      startTime: row.startTime,
                      endTime: row.endTime,
                      semester: String(row.semester),
                      division: row.division,
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
        title={editing ? 'Edit timetable slot' : 'Add timetable slot'}
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
          <Select
            label="Subject"
            name="subjectId"
            value={form.subjectId}
            onChange={change}
            placeholder="Select a subject"
            required
            options={(subjects || []).map((subject) => ({
              value: subject.id,
              label: `${subject.name} (${subject.code})`,
            }))}
          />
          <Select
            label="Faculty"
            name="facultyId"
            value={form.facultyId}
            onChange={change}
            placeholder="Select a faculty member"
            required
            options={(facultyOptions || []).map((member) => ({ value: member.id, label: member.fullName }))}
          />
          <Select
            label="Classroom"
            name="classroomId"
            value={form.classroomId}
            onChange={change}
            placeholder="Not specified"
            options={(classrooms || []).map((room) => ({ value: room.id, label: room.roomNumber }))}
          />
          <Select
            label="Day"
            name="dayOfWeek"
            value={form.dayOfWeek}
            onChange={change}
            options={WEEK_DAYS.map((day) => ({ value: day, label: titleCase(day) }))}
          />
          <Input label="Start time" name="startTime" type="time" value={form.startTime} onChange={change} required />
          <Input label="End time" name="endTime" type="time" value={form.endTime} onChange={change} required />
          <Input label="Semester" name="semester" type="number" min="1" max="8" value={form.semester} onChange={change} required />
          <Input label="Division" name="division" value={form.division} onChange={change} required />
        </form>
      </Modal>

      <ConfirmDialog
        open={Boolean(toDelete)}
        title="Remove timetable slot"
        message="This lecture slot will be removed from the weekly schedule."
        onCancel={() => setToDelete(null)}
        onConfirm={remove}
      />
    </div>
  );
}
