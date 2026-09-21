/** Formatting helpers shared by the tables and cards. */

export function formatDate(value) {
  if (!value) return '-';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return date.toLocaleDateString('en-IN', { day: '2-digit', month: 'short', year: 'numeric' });
}

export function formatDateTime(value) {
  if (!value) return '-';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return value;
  return date.toLocaleString('en-IN', {
    day: '2-digit',
    month: 'short',
    hour: '2-digit',
    minute: '2-digit',
  });
}

export function titleCase(value) {
  if (!value) return '';
  return value.charAt(0) + value.slice(1).toLowerCase();
}

export function percent(value) {
  return `${Number(value ?? 0).toFixed(1)}%`;
}

/** Colour band for an attendance figure: below 75% needs attention. */
export function attendanceTone(value) {
  if (value >= 75) return 'text-brand-700';
  if (value >= 65) return 'text-amber-600';
  return 'text-red-600';
}

/** Turns a Date into the value an <input type="datetime-local"> expects. */
export function toLocalInputValue(date) {
  const pad = (n) => String(n).padStart(2, '0');
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())}T${pad(
    date.getHours(),
  )}:${pad(date.getMinutes())}`;
}
