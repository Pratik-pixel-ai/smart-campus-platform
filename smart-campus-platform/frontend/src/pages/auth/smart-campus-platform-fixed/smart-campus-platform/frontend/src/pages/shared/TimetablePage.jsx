import useApiData from '../../hooks/useApiData.js';
import { timetableApi } from '../../services/endpoints.js';
import { WEEK_DAYS } from '../../utils/constants.js';
import { titleCase } from '../../utils/format.js';
import PageHeader from '../../components/PageHeader.jsx';
import LoadingSpinner from '../../components/LoadingSpinner.jsx';
import ErrorState from '../../components/ErrorState.jsx';
import EmptyState from '../../components/EmptyState.jsx';

/**
 * Weekly timetable. The backend decides whose timetable this is from the JWT, so the
 * same screen serves students (their class) and faculty (their lectures).
 */
export default function TimetablePage() {
  const { data, loading, error, reload } = useApiData(() => timetableApi.myWeek(), []);
  const today = new Date().toLocaleDateString('en-US', { weekday: 'long' }).toUpperCase();

  const entries = data || [];
  const byDay = WEEK_DAYS.map((day) => ({
    day,
    slots: entries.filter((entry) => entry.dayOfWeek === day),
  }));

  return (
    <div>
      <PageHeader title="Timetable" subtitle="Your weekly lecture schedule" />

      {loading && <LoadingSpinner />}
      {!loading && error && <ErrorState message={error} onRetry={reload} />}

      {!loading && !error && entries.length === 0 && (
        <div className="card">
          <EmptyState
            title="No timetable published yet"
            message="Once the administrator adds lecture slots for your class, they will appear here."
          />
        </div>
      )}

      {!loading && !error && entries.length > 0 && (
        <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
          {byDay.map(({ day, slots }) => (
            <div key={day} className={`card p-4 ${day === today ? 'ring-1 ring-brand-500' : ''}`}>
              <div className="mb-3 flex items-center justify-between">
                <h3 className="text-sm font-semibold text-slate-900">{titleCase(day)}</h3>
                {day === today && <span className="text-xs font-medium text-brand-700">Today</span>}
              </div>

              {slots.length === 0 ? (
                <p className="py-4 text-center text-xs text-slate-400">No lectures</p>
              ) : (
                <ul className="space-y-2">
                  {slots.map((slot) => (
                    <li key={slot.id} className="rounded-lg border border-slate-200 p-3">
                      <div className="flex items-baseline justify-between gap-2">
                        <p className="text-sm font-medium text-slate-800">{slot.subjectName}</p>
                        <p className="shrink-0 text-xs text-slate-500">
                          {slot.startTime} - {slot.endTime}
                        </p>
                      </div>
                      <p className="mt-1 text-xs text-slate-500">
                        {slot.facultyName} · Room {slot.room} · Sem {slot.semester}-{slot.division}
                      </p>
                    </li>
                  ))}
                </ul>
              )}
            </div>
          ))}
        </div>
      )}
    </div>
  );
}
