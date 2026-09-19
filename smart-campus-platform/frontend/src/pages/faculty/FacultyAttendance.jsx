import { useEffect, useState } from 'react';
import { Bluetooth, CheckCircle2, CircleSlash, Radio, StopCircle } from 'lucide-react';
import { attendanceApi, classroomApi, studentApi, subjectApi } from '../../services/endpoints.js';
import { useAuth } from '../../context/AuthContext.jsx';
import { useToast } from '../../context/ToastContext.jsx';
import { errorMessage } from '../../services/api.js';
import { formatDate } from '../../utils/format.js';
import PageHeader from '../../components/PageHeader.jsx';
import Button from '../../components/Button.jsx';
import Select from '../../components/Select.jsx';
import Input from '../../components/Input.jsx';
import Badge, { statusTone } from '../../components/Badge.jsx';
import LoadingSpinner from '../../components/LoadingSpinner.jsx';
import EmptyState from '../../components/EmptyState.jsx';

/**
 * Attendance capture screen.
 *
 * In demo mode the "detect" button simulates a BLE detection so the flow can be shown
 * without hardware; the banner says so plainly. In BLE mode the same endpoint is fed
 * by real advertisements scanned by the faculty device.
 */
export default function FacultyAttendance() {
  const { user } = useAuth();
  const toast = useToast();

  const [mode, setMode] = useState('DEMO');
  const [subjects, setSubjects] = useState([]);
  const [classrooms, setClassrooms] = useState([]);
  const [session, setSession] = useState(null);
  const [classList, setClassList] = useState([]);
  const [history, setHistory] = useState([]);
  const [loading, setLoading] = useState(true);
  const [starting, setStarting] = useState(false);
  const [busyStudentId, setBusyStudentId] = useState(null);

  const [form, setForm] = useState({
    subjectId: '',
    classroomId: '',
    lectureNumber: '1',
    durationMinutes: '60',
    division: 'A',
  });

  useEffect(() => {
    async function load() {
      try {
        const [modeRes, subjectRes, roomRes, sessionRes] = await Promise.all([
          attendanceApi.mode(),
          subjectApi.mine(user.profileId),
          classroomApi.options(),
          attendanceApi.mySessions(),
        ]);
        setMode(modeRes.data.mode);
        setSubjects(subjectRes.data);
        setClassrooms(roomRes.data);
        setHistory(sessionRes.data);

        const open = sessionRes.data.find((item) => item.status === 'OPEN');
        if (open) {
          setSession(open);
        }
      } catch (err) {
        toast.error(errorMessage(err));
      } finally {
        setLoading(false);
      }
    }
    load();
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user?.profileId]);

  // Whenever a session is active, load the class list so every student can be detected.
  useEffect(() => {
    if (!session) {
      setClassList([]);
      return;
    }
    const subject = subjects.find((item) => item.id === session.subjectId);
    if (!subject) return;
    studentApi
      .classList({
        departmentId: subject.departmentId,
        semester: session.semester,
        division: session.division,
      })
      .then(({ data }) => setClassList(data))
      .catch((err) => toast.error(errorMessage(err)));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [session?.id, subjects]);

  const change = (event) => setForm({ ...form, [event.target.name]: event.target.value });

  const startSession = async (event) => {
    event.preventDefault();
    const subject = subjects.find((item) => String(item.id) === String(form.subjectId));
    if (!subject) {
      toast.error('Select a subject to start the session');
      return;
    }
    setStarting(true);
    try {
      const { data } = await attendanceApi.createSession({
        subjectId: Number(form.subjectId),
        classroomId: form.classroomId ? Number(form.classroomId) : null,
        lectureNumber: Number(form.lectureNumber),
        durationMinutes: Number(form.durationMinutes),
        semester: subject.semester,
        division: form.division,
      });
      setSession(data);
      toast.success('Attendance session started');
    } catch (err) {
      toast.error(errorMessage(err));
    } finally {
      setStarting(false);
    }
  };

  const detect = async (student) => {
    setBusyStudentId(student.id);
    try {
      const { data } = await attendanceApi.detect(session.id, {
        studentId: student.id,
        bleDeviceId: student.bleDeviceId,
        rssi: -60,
      });
      setSession(data);
    } catch (err) {
      toast.error(errorMessage(err));
    } finally {
      setBusyStudentId(null);
    }
  };

  const detectAll = async () => {
    for (const student of classList) {
      if (!presentIds.has(student.id)) {
        // Sequential on purpose: this mirrors detections arriving one at a time.
        // eslint-disable-next-line no-await-in-loop
        await detect(student);
      }
    }
  };

  const closeSession = async () => {
    try {
      const { data } = await attendanceApi.close(session.id);
      toast.success(`Session closed. ${data.presentCount} present, ${data.absentCount} absent.`);
      setSession(null);
      const { data: sessions } = await attendanceApi.mySessions();
      setHistory(sessions);
    } catch (err) {
      toast.error(errorMessage(err));
    }
  };

  if (loading) return <LoadingSpinner />;

  const presentIds = new Set(
    (session?.records || []).filter((record) => record.status === 'PRESENT').map((record) => record.studentId),
  );

  return (
    <div>
      <PageHeader
        title="Take attendance"
        subtitle="Start a session, detect students, then close it to record absentees"
      />

      <div
        className={`mb-4 flex items-start gap-3 rounded-lg border px-4 py-3 text-sm ${
          mode === 'BLE'
            ? 'border-sky-200 bg-sky-50 text-sky-800'
            : 'border-amber-200 bg-amber-50 text-amber-800'
        }`}
      >
        <Bluetooth size={18} className="mt-0.5 shrink-0" />
        {mode === 'BLE' ? (
          <p>
            <strong>BLE mode.</strong> Detections come from real Bluetooth Low Energy advertisements scanned by
            the faculty device and are accepted only above the configured signal threshold.
          </p>
        ) : (
          <p>
            <strong>Demo BLE detection.</strong> No Bluetooth hardware is in use. Pressing detect simulates a
            scan so the full attendance flow can be demonstrated. Every record created this way is stored with
            method <code className="rounded bg-amber-100 px-1">DEMO</code>. Set{' '}
            <code className="rounded bg-amber-100 px-1">ATTENDANCE_MODE=ble</code> to switch to real scanning.
          </p>
        )}
      </div>

      {!session ? (
        <form onSubmit={startSession} className="card space-y-4 p-5">
          <h2 className="text-sm font-semibold text-slate-900">Start a new session</h2>
          <div className="grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
            <Select
              label="Subject"
              name="subjectId"
              value={form.subjectId}
              onChange={change}
              placeholder="Select a subject"
              required
              options={subjects.map((subject) => ({
                value: subject.id,
                label: `${subject.name} (${subject.code}) · Sem ${subject.semester}`,
              }))}
            />
            <Select
              label="Classroom"
              name="classroomId"
              value={form.classroomId}
              onChange={change}
              placeholder="Not specified"
              options={classrooms.map((room) => ({
                value: room.id,
                label: `${room.roomNumber} · ${room.building}`,
              }))}
            />
            <Input label="Division" name="division" value={form.division} onChange={change} required />
            <Input
              label="Lecture number"
              name="lectureNumber"
              type="number"
              min="1"
              max="12"
              value={form.lectureNumber}
              onChange={change}
              required
            />
            <Input
              label="Duration (minutes)"
              name="durationMinutes"
              type="number"
              min="5"
              max="240"
              value={form.durationMinutes}
              onChange={change}
              required
            />
          </div>
          <Button type="submit" loading={starting}>
            <Radio size={16} /> Start session
          </Button>

          {subjects.length === 0 && (
            <p className="text-xs text-amber-700">
              No subjects are assigned to you yet. Ask the administrator to assign one.
            </p>
          )}
        </form>
      ) : (
        <div className="space-y-4">
          <div className="card p-5">
            <div className="flex flex-wrap items-center justify-between gap-3">
              <div>
                <div className="flex items-center gap-2">
                  <h2 className="text-sm font-semibold text-slate-900">{session.subjectName}</h2>
                  <Badge tone={statusTone(session.status)}>{session.status}</Badge>
                  <Badge tone={session.mode === 'BLE' ? 'info' : 'warning'}>{session.mode} detection</Badge>
                </div>
                <p className="mt-1 text-xs text-slate-500">
                  Session #{session.id} · {formatDate(session.sessionDate)} · Lecture {session.lectureNumber} · Sem{' '}
                  {session.semester}-{session.division} · Room {session.room}
                </p>
              </div>
              <div className="flex gap-2">
                <Button variant="secondary" onClick={detectAll} disabled={classList.length === 0}>
                  Detect all (demo)
                </Button>
                <Button variant="danger" onClick={closeSession}>
                  <StopCircle size={16} /> Close session
                </Button>
              </div>
            </div>

            <div className="mt-4 grid grid-cols-3 gap-3 text-center">
              <div className="rounded-lg bg-slate-50 p-3">
                <p className="text-xs text-slate-500">In class list</p>
                <p className="text-lg font-semibold text-slate-900">{session.enrolledCount}</p>
              </div>
              <div className="rounded-lg bg-brand-50 p-3">
                <p className="text-xs text-brand-700">Detected</p>
                <p className="text-lg font-semibold text-brand-800">{session.presentCount}</p>
              </div>
              <div className="rounded-lg bg-amber-50 p-3">
                <p className="text-xs text-amber-700">Not yet detected</p>
                <p className="text-lg font-semibold text-amber-800">
                  {Math.max(session.enrolledCount - session.presentCount, 0)}
                </p>
              </div>
            </div>
          </div>

          <div className="card overflow-hidden">
            <h3 className="border-b border-slate-200 px-5 py-3 text-sm font-semibold text-slate-900">
              Class list
            </h3>
            {classList.length === 0 ? (
              <EmptyState title="No students in this class" message="Check the semester and division." />
            ) : (
              <ul className="divide-y divide-slate-100">
                {classList.map((student) => {
                  const present = presentIds.has(student.id);
                  return (
                    <li key={student.id} className="flex items-center justify-between gap-3 px-5 py-3">
                      <div className="min-w-0">
                        <p className="text-sm font-medium text-slate-800">{student.fullName}</p>
                        <p className="text-xs text-slate-500">
                          {student.rollNumber}
                          {student.bleDeviceId ? ` · ${student.bleDeviceId}` : ' · no BLE device registered'}
                        </p>
                      </div>
                      {present ? (
                        <span className="flex items-center gap-1.5 text-sm font-medium text-brand-700">
                          <CheckCircle2 size={16} /> Present
                        </span>
                      ) : (
                        <Button
                          size="sm"
                          variant="secondary"
                          loading={busyStudentId === student.id}
                          onClick={() => detect(student)}
                        >
                          <Radio size={14} /> {mode === 'BLE' ? 'Accept scan' : 'Simulate detection'}
                        </Button>
                      )}
                    </li>
                  );
                })}
              </ul>
            )}
          </div>
        </div>
      )}

      <div className="mt-6">
        <h3 className="mb-3 text-sm font-semibold text-slate-900">Past sessions</h3>
        {history.length === 0 ? (
          <div className="card">
            <EmptyState icon={CircleSlash} title="No sessions recorded yet" />
          </div>
        ) : (
          <div className="card divide-y divide-slate-100">
            {history.slice(0, 10).map((item) => (
              <div key={item.id} className="flex flex-wrap items-center justify-between gap-2 px-5 py-3">
                <div>
                  <p className="text-sm font-medium text-slate-800">{item.subjectName}</p>
                  <p className="text-xs text-slate-500">
                    {formatDate(item.sessionDate)} · Lecture {item.lectureNumber} · {item.mode} detection
                  </p>
                </div>
                <div className="flex items-center gap-3">
                  <span className="text-xs text-slate-500">
                    {item.presentCount} present · {item.absentCount} absent
                  </span>
                  <Badge tone={statusTone(item.status)}>{item.status}</Badge>
                </div>
              </div>
            ))}
          </div>
        )}
      </div>
    </div>
  );
}
