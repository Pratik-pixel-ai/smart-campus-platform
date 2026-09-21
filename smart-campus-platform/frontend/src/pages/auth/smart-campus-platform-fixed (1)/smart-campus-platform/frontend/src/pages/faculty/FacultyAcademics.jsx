import { useEffect, useState } from 'react';
import { GraduationCap, Save } from 'lucide-react';
import { academicApi, studentApi, subjectApi } from '../../services/endpoints.js';
import { useAuth } from '../../context/AuthContext.jsx';
import { useToast } from '../../context/ToastContext.jsx';
import { errorMessage } from '../../services/api.js';
import PageHeader from '../../components/PageHeader.jsx';
import Button from '../../components/Button.jsx';
import Select from '../../components/Select.jsx';
import Input from '../../components/Input.jsx';
import LoadingSpinner from '../../components/LoadingSpinner.jsx';
import EmptyState from '../../components/EmptyState.jsx';

/**
 * Marks entry. Pick a subject, then enter internal and external marks per student.
 * The total and the grade are calculated by the backend so the rules stay in one place.
 */
export default function FacultyAcademics() {
  const { user } = useAuth();
  const toast = useToast();

  const [subjects, setSubjects] = useState([]);
  const [subjectId, setSubjectId] = useState('');
  const [division, setDivision] = useState('A');
  const [students, setStudents] = useState([]);
  const [marks, setMarks] = useState({});
  const [loading, setLoading] = useState(false);
  const [savingId, setSavingId] = useState(null);

  useEffect(() => {
    subjectApi
      .mine(user.profileId)
      .then(({ data }) => setSubjects(data))
      .catch((err) => toast.error(errorMessage(err)));
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [user?.profileId]);

  const subject = subjects.find((item) => String(item.id) === String(subjectId));

  const loadClass = async () => {
    if (!subject) {
      toast.error('Select a subject first');
      return;
    }
    setLoading(true);
    try {
      const { data } = await studentApi.classList({
        departmentId: subject.departmentId,
        semester: subject.semester,
        division,
      });
      setStudents(data);

      // Pre-fill anything already recorded for this subject.
      const existing = {};
      await Promise.all(
        data.map(async (student) => {
          try {
            const { data: summary } = await academicApi.forStudent(student.id);
            const record = summary.semesters
              .flatMap((semester) => semester.records)
              .find((item) => item.subjectId === subject.id);
            if (record) {
              existing[student.id] = {
                internalMarks: record.internalMarks,
                externalMarks: record.externalMarks,
                grade: record.grade,
              };
            }
          } catch {
            // A student with no record yet simply stays empty.
          }
        }),
      );
      setMarks(existing);
    } catch (err) {
      toast.error(errorMessage(err));
    } finally {
      setLoading(false);
    }
  };

  const save = async (student) => {
    const entry = marks[student.id] || {};
    setSavingId(student.id);
    try {
      const { data } = await academicApi.save({
        studentId: student.id,
        subjectId: subject.id,
        semester: subject.semester,
        internalMarks: Number(entry.internalMarks || 0),
        externalMarks: Number(entry.externalMarks || 0),
      });
      setMarks((current) => ({
        ...current,
        [student.id]: {
          internalMarks: data.internalMarks,
          externalMarks: data.externalMarks,
          grade: data.grade,
        },
      }));
      toast.success(`Saved marks for ${student.fullName}`);
    } catch (err) {
      toast.error(errorMessage(err));
    } finally {
      setSavingId(null);
    }
  };

  const update = (studentId, field, value) =>
    setMarks((current) => ({ ...current, [studentId]: { ...current[studentId], [field]: value } }));

  return (
    <div>
      <PageHeader title="Marks entry" subtitle="Record internal and external marks for your subjects" />

      <div className="card mb-4 flex flex-wrap items-end gap-4 p-5">
        <Select
          label="Subject"
          value={subjectId}
          onChange={(event) => setSubjectId(event.target.value)}
          placeholder="Select a subject"
          className="min-w-[240px]"
          options={subjects.map((item) => ({
            value: item.id,
            label: `${item.name} · Sem ${item.semester}`,
          }))}
        />
        <Input label="Division" value={division} onChange={(event) => setDivision(event.target.value)} className="w-28" />
        <Button onClick={loadClass} loading={loading}>
          Load class
        </Button>
      </div>

      {loading && <LoadingSpinner />}

      {!loading && students.length === 0 && (
        <div className="card">
          <EmptyState
            icon={GraduationCap}
            title="Choose a subject and load the class"
            message="The student list will appear here with a marks row each."
          />
        </div>
      )}

      {!loading && students.length > 0 && (
        <div className="card overflow-x-auto">
          <table className="w-full min-w-[720px]">
            <thead className="bg-slate-50">
              <tr>
                <th className="table-head">Roll number</th>
                <th className="table-head">Student</th>
                <th className="table-head">Internal (40)</th>
                <th className="table-head">External (60)</th>
                <th className="table-head">Grade</th>
                <th className="table-head" />
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-100">
              {students.map((student) => {
                const entry = marks[student.id] || {};
                return (
                  <tr key={student.id}>
                    <td className="table-cell">{student.rollNumber}</td>
                    <td className="table-cell">{student.fullName}</td>
                    <td className="table-cell">
                      <input
                        className="field w-24"
                        type="number"
                        min="0"
                        max="40"
                        value={entry.internalMarks ?? ''}
                        onChange={(event) => update(student.id, 'internalMarks', event.target.value)}
                        aria-label={`Internal marks for ${student.fullName}`}
                      />
                    </td>
                    <td className="table-cell">
                      <input
                        className="field w-24"
                        type="number"
                        min="0"
                        max="60"
                        value={entry.externalMarks ?? ''}
                        onChange={(event) => update(student.id, 'externalMarks', event.target.value)}
                        aria-label={`External marks for ${student.fullName}`}
                      />
                    </td>
                    <td className="table-cell font-medium">{entry.grade || '-'}</td>
                    <td className="table-cell">
                      <Button size="sm" loading={savingId === student.id} onClick={() => save(student)}>
                        <Save size={14} /> Save
                      </Button>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}
    </div>
  );
}
