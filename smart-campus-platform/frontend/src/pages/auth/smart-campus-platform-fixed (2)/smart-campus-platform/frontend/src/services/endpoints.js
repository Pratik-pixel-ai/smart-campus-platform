import api from './api.js';

/**
 * Every backend call the app makes, grouped by module.
 * Components import from here instead of building URLs themselves, so a route change
 * is a one-line edit.
 */

export const authApi = {
  login: (payload) => api.post('/auth/login', payload),
  register: (payload) => api.post('/auth/register', payload),
  me: () => api.get('/auth/me'),
};

export const dashboardApi = {
  student: () => api.get('/dashboard/student'),
  faculty: () => api.get('/dashboard/faculty'),
  admin: () => api.get('/dashboard/admin'),
};

export const studentApi = {
  list: (params) => api.get('/students', { params }),
  classList: (params) => api.get('/students/class', { params }),
  get: (id) => api.get(`/students/${id}`),
  me: () => api.get('/students/me'),
  create: (payload) => api.post('/students', payload),
  update: (id, payload) => api.put(`/students/${id}`, payload),
  remove: (id) => api.delete(`/students/${id}`),
};

export const facultyApi = {
  list: (params) => api.get('/faculty', { params }),
  options: () => api.get('/faculty/options'),
  me: () => api.get('/faculty/me'),
  create: (payload) => api.post('/faculty', payload),
  update: (id, payload) => api.put(`/faculty/${id}`, payload),
  remove: (id) => api.delete(`/faculty/${id}`),
};

export const departmentApi = {
  list: () => api.get('/departments'),
  publicList: () => api.get('/departments/public'),
  create: (payload) => api.post('/departments', payload),
  update: (id, payload) => api.put(`/departments/${id}`, payload),
  remove: (id) => api.delete(`/departments/${id}`),
};

export const subjectApi = {
  list: (params) => api.get('/subjects', { params }),
  options: () => api.get('/subjects/options'),
  mine: (facultyId) => api.get('/subjects/mine', { params: { facultyId } }),
  create: (payload) => api.post('/subjects', payload),
  update: (id, payload) => api.put(`/subjects/${id}`, payload),
  remove: (id) => api.delete(`/subjects/${id}`),
};

export const classroomApi = {
  list: (params) => api.get('/classrooms', { params }),
  options: () => api.get('/classrooms/options'),
  create: (payload) => api.post('/classrooms', payload),
  update: (id, payload) => api.put(`/classrooms/${id}`, payload),
  remove: (id) => api.delete(`/classrooms/${id}`),
};

export const timetableApi = {
  myWeek: () => api.get('/timetable/me'),
  today: () => api.get('/timetable/today'),
  all: () => api.get('/timetable'),
  create: (payload) => api.post('/timetable', payload),
  update: (id, payload) => api.put(`/timetable/${id}`, payload),
  remove: (id) => api.delete(`/timetable/${id}`),
};

export const attendanceApi = {
  mode: () => api.get('/attendance/mode'),
  createSession: (payload) => api.post('/attendance/sessions', payload),
  detect: (sessionId, payload) => api.post(`/attendance/sessions/${sessionId}/detect`, payload),
  close: (sessionId) => api.post(`/attendance/sessions/${sessionId}/close`),
  session: (sessionId) => api.get(`/attendance/sessions/${sessionId}`),
  mySessions: () => api.get('/attendance/sessions'),
  allSessions: (params) => api.get('/attendance/sessions/all', { params }),
  mySummary: () => api.get('/attendance/me/summary'),
  studentSummary: (studentId) => api.get(`/attendance/student/${studentId}/summary`),
  studentHistory: (studentId) => api.get(`/attendance/student/${studentId}`),
  overview: () => api.get('/attendance/overview'),
};

export const assignmentApi = {
  list: () => api.get('/assignments'),
  get: (id) => api.get(`/assignments/${id}`),
  create: (payload) => api.post('/assignments', payload),
  update: (id, payload) => api.put(`/assignments/${id}`, payload),
  remove: (id) => api.delete(`/assignments/${id}`),
};

export const submissionApi = {
  submit: (payload) => api.post('/submissions', payload),
  mine: () => api.get('/submissions/me'),
  forAssignment: (assignmentId) => api.get(`/submissions/assignment/${assignmentId}`),
  grade: (id, payload) => api.put(`/submissions/${id}/grade`, payload),
};

export const announcementApi = {
  list: () => api.get('/announcements'),
  create: (payload) => api.post('/announcements', payload),
  remove: (id) => api.delete(`/announcements/${id}`),
};

export const academicApi = {
  me: () => api.get('/academics/me'),
  forStudent: (studentId) => api.get(`/academics/student/${studentId}`),
  save: (payload) => api.post('/academics', payload),
  remove: (id) => api.delete(`/academics/${id}`),
};

export const notificationApi = {
  mine: () => api.get('/notifications/me'),
  unreadCount: () => api.get('/notifications/me/unread-count'),
  markRead: (id) => api.put(`/notifications/${id}/read`),
};

export const aiApi = {
  status: () => api.get('/ai/status'),
  chat: (question) => api.post('/ai/chat', { question }),
};
