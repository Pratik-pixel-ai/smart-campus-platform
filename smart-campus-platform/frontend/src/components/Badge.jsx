const TONES = {
  neutral: 'bg-slate-100 text-slate-700',
  success: 'bg-brand-50 text-brand-700 border border-brand-200',
  warning: 'bg-amber-50 text-amber-700 border border-amber-200',
  danger: 'bg-red-50 text-red-700 border border-red-200',
  info: 'bg-sky-50 text-sky-700 border border-sky-200',
};

export default function Badge({ children, tone = 'neutral', className = '' }) {
  return (
    <span
      className={`inline-flex items-center rounded-full px-2.5 py-0.5 text-xs font-medium ${TONES[tone]} ${className}`}
    >
      {children}
    </span>
  );
}

/** Shared mapping so the same status always gets the same colour across screens. */
export function statusTone(status) {
  switch (status) {
    case 'PRESENT':
    case 'GRADED':
    case 'CLOSED':
      return 'success';
    case 'ABSENT':
    case 'HIGH':
      return 'danger';
    case 'LATE':
    case 'PENDING':
    case 'OPEN':
      return 'warning';
    case 'SUBMITTED':
    case 'NORMAL':
      return 'info';
    default:
      return 'neutral';
  }
}
