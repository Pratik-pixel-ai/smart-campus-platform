export const ROLES = {
  STUDENT: 'ROLE_STUDENT',
  FACULTY: 'ROLE_FACULTY',
  ADMIN: 'ROLE_ADMIN',
};

/** Where each role lands after signing in. */
export const HOME_BY_ROLE = {
  ROLE_STUDENT: '/student/dashboard',
  ROLE_FACULTY: '/faculty/dashboard',
  ROLE_ADMIN: '/admin/dashboard',
};

export const WEEK_DAYS = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY'];

export const PRIORITIES = ['LOW', 'NORMAL', 'HIGH'];

export const MINIMUM_ATTENDANCE = 75;
