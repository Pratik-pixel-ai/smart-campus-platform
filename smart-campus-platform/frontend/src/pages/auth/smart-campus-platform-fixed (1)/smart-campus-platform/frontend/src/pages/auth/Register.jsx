import { useCallback, useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { School } from 'lucide-react';
import { useAuth } from '../../context/AuthContext.jsx';
import { useToast } from '../../context/ToastContext.jsx';
import { departmentApi } from '../../services/endpoints.js';
import { errorMessage } from '../../services/api.js';
import { HOME_BY_ROLE, ROLES } from '../../utils/constants.js';
import Button from '../../components/Button.jsx';
import Input from '../../components/Input.jsx';
import Select from '../../components/Select.jsx';

export default function Register() {
  const { register } = useAuth();
  const toast = useToast();
  const navigate = useNavigate();

  const [departments, setDepartments] = useState([]);
  const [departmentsLoading, setDepartmentsLoading] = useState(true);
  const [departmentsError, setDepartmentsError] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [form, setForm] = useState({
    fullName: '',
    email: '',
    password: '',
    role: ROLES.STUDENT,
    departmentId: '',
    rollNumber: '',
    semester: '7',
    division: 'A',
    employeeCode: '',
    designation: 'Assistant Professor',
  });

  const loadDepartments = useCallback(() => {
    setDepartmentsLoading(true);
    setDepartmentsError('');
    departmentApi
      .publicList()
      .then(({ data }) => {
        setDepartments(data);
        if (data.length === 0) {
          setDepartmentsError('The server has no departments yet. Ask an administrator to add one.');
        }
      })
      .catch((err) => {
        setDepartments([]);
        setDepartmentsError(errorMessage(err, 'Could not load departments'));
      })
      .finally(() => setDepartmentsLoading(false));
  }, []);

  useEffect(() => {
    loadDepartments();
  }, [loadDepartments]);

  const change = (event) => setForm({ ...form, [event.target.name]: event.target.value });
  const isStudent = form.role === ROLES.STUDENT;

  const submit = async (event) => {
    event.preventDefault();
    setError('');
    // A disabled <select> is skipped by the browser's own "required" check, so guard here.
    if (!form.departmentId) {
      setError(departmentsError || 'Select a department');
      return;
    }
    setLoading(true);
    try {
      const payload = {
        fullName: form.fullName,
        email: form.email,
        password: form.password,
        role: form.role,
        departmentId: Number(form.departmentId),
        ...(isStudent
          ? { rollNumber: form.rollNumber, semester: Number(form.semester), division: form.division }
          : { employeeCode: form.employeeCode, designation: form.designation }),
      };
      const profile = await register(payload);
      toast.success('Account created');
      navigate(HOME_BY_ROLE[profile.role] || '/', { replace: true });
    } catch (err) {
      setError(errorMessage(err, 'Could not create the account'));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="flex min-h-screen items-center justify-center bg-slate-50 p-4">
      <div className="w-full max-w-xl">
        <div className="mb-6 flex flex-col items-center text-center">
          <div className="mb-3 rounded-xl bg-brand-600 p-2.5 text-white">
            <School size={24} />
          </div>
          <h1 className="text-xl font-semibold text-slate-900">Create your account</h1>
          <p className="mt-1 text-sm text-slate-500">Students and faculty can register here</p>
        </div>

        <form onSubmit={submit} className="card space-y-4 p-6">
          {error && (
            <p className="rounded-lg border border-red-200 bg-red-50 px-3 py-2 text-sm text-red-700">{error}</p>
          )}

          <div className="grid gap-4 sm:grid-cols-2">
            <Input label="Full name" name="fullName" value={form.fullName} onChange={change} required />
            <Input label="Email" name="email" type="email" value={form.email} onChange={change} required />
            <Input
              label="Password"
              name="password"
              type="password"
              value={form.password}
              onChange={change}
              minLength={8}
              required
              placeholder="At least 8 characters"
            />
            <Select
              label="I am a"
              name="role"
              value={form.role}
              onChange={change}
              options={[
                { value: ROLES.STUDENT, label: 'Student' },
                { value: ROLES.FACULTY, label: 'Faculty' },
              ]}
            />
            <Select
              label="Department"
              name="departmentId"
              value={form.departmentId}
              onChange={change}
              placeholder={
                departmentsLoading
                  ? 'Loading departments…'
                  : departments.length === 0
                    ? 'No departments available'
                    : 'Select a department'
              }
              disabled={departmentsLoading || departments.length === 0}
              required
              options={departments.map((department) => ({
                value: department.id,
                label: `${department.name} (${department.code})`,
              }))}
            />

            {isStudent ? (
              <>
                <Input label="Roll number" name="rollNumber" value={form.rollNumber} onChange={change} required />
                <Input
                  label="Semester"
                  name="semester"
                  type="number"
                  min="1"
                  max="8"
                  value={form.semester}
                  onChange={change}
                  required
                />
                <Input label="Division" name="division" value={form.division} onChange={change} required />
              </>
            ) : (
              <>
                <Input
                  label="Employee code"
                  name="employeeCode"
                  value={form.employeeCode}
                  onChange={change}
                  required
                />
                <Input label="Designation" name="designation" value={form.designation} onChange={change} />
              </>
            )}
          </div>

          {departmentsError && !departmentsLoading && (
            <div className="flex items-center justify-between gap-3 rounded-lg border border-amber-200 bg-amber-50 px-3 py-2 text-sm text-amber-800">
              <span>{departmentsError}</span>
              <button type="button" onClick={loadDepartments} className="font-medium underline">
                Retry
              </button>
            </div>
          )}

          <Button type="submit" loading={loading} className="w-full">
            Create account
          </Button>

          <p className="text-center text-sm text-slate-500">
            Already registered?{' '}
            <Link to="/login" className="font-medium text-brand-700 hover:underline">
              Sign in
            </Link>
          </p>
        </form>
      </div>
    </div>
  );
}
