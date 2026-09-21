import { useEffect, useRef, useState } from 'react';
import { Bot, Send, User } from 'lucide-react';
import { aiApi } from '../../services/endpoints.js';
import { errorMessage } from '../../services/api.js';
import PageHeader from '../../components/PageHeader.jsx';
import Button from '../../components/Button.jsx';

const SUGGESTIONS = [
  'What is my attendance percentage?',
  'Which classes do I have today?',
  'What assignments are due this week?',
  'Am I below 75% in any subject?',
];

/**
 * Campus assistant chat.
 *
 * The browser only sends the question. The backend attaches the signed-in user's own
 * campus data before calling the AI provider, so the API key never reaches the client
 * and one user can never ask about another user's records.
 */
export default function AssistantPage() {
  const [messages, setMessages] = useState([]);
  const [question, setQuestion] = useState('');
  const [loading, setLoading] = useState(false);
  const [status, setStatus] = useState(null);
  const endRef = useRef(null);

  useEffect(() => {
    aiApi
      .status()
      .then(({ data }) => setStatus(data))
      .catch(() => setStatus({ configured: false, message: 'Assistant status unavailable' }));
  }, []);

  useEffect(() => {
    endRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages, loading]);

  const ask = async (text) => {
    const trimmed = text.trim();
    if (!trimmed || loading) return;

    setMessages((current) => [...current, { role: 'user', text: trimmed }]);
    setQuestion('');
    setLoading(true);
    try {
      const { data } = await aiApi.chat(trimmed);
      setMessages((current) => [...current, { role: 'assistant', text: data.answer }]);
    } catch (err) {
      setMessages((current) => [...current, { role: 'assistant', text: errorMessage(err) }]);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div>
      <PageHeader
        title="Campus assistant"
        subtitle="Ask about your attendance, timetable, assignments and results"
      />

      {status && !status.configured && (
        <p className="mb-4 rounded-lg border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-800">
          {status.message} Add the key to the backend environment and restart to enable the assistant.
        </p>
      )}

      <div className="card flex h-[65vh] flex-col">
        <div className="flex-1 space-y-4 overflow-y-auto p-5">
          {messages.length === 0 && (
            <div className="flex h-full flex-col items-center justify-center gap-4 text-center">
              <div className="rounded-full bg-brand-50 p-3 text-brand-600">
                <Bot size={24} />
              </div>
              <div>
                <p className="text-sm font-medium text-slate-800">Ask about your campus data</p>
                <p className="mt-1 text-sm text-slate-500">Answers use only your own records.</p>
              </div>
              <div className="flex flex-wrap justify-center gap-2">
                {SUGGESTIONS.map((suggestion) => (
                  <button
                    key={suggestion}
                    type="button"
                    onClick={() => ask(suggestion)}
                    className="rounded-full border border-slate-300 px-3 py-1.5 text-xs text-slate-600 hover:border-brand-500 hover:text-brand-700"
                  >
                    {suggestion}
                  </button>
                ))}
              </div>
            </div>
          )}

          {messages.map((message, index) => (
            <div
              key={index}
              className={`flex gap-3 ${message.role === 'user' ? 'justify-end' : 'justify-start'}`}
            >
              {message.role === 'assistant' && (
                <span className="mt-0.5 h-8 w-8 shrink-0 rounded-full bg-brand-50 p-1.5 text-brand-600">
                  <Bot size={20} />
                </span>
              )}
              <div
                className={`max-w-[75%] whitespace-pre-wrap rounded-2xl px-4 py-2.5 text-sm ${
                  message.role === 'user'
                    ? 'bg-brand-600 text-white'
                    : 'border border-slate-200 bg-slate-50 text-slate-700'
                }`}
              >
                {message.text}
              </div>
              {message.role === 'user' && (
                <span className="mt-0.5 h-8 w-8 shrink-0 rounded-full bg-slate-200 p-1.5 text-slate-600">
                  <User size={20} />
                </span>
              )}
            </div>
          ))}

          {loading && <p className="text-sm text-slate-400">Assistant is thinking...</p>}
          <div ref={endRef} />
        </div>

        <form
          onSubmit={(event) => {
            event.preventDefault();
            ask(question);
          }}
          className="flex items-center gap-2 border-t border-slate-200 p-4"
        >
          <input
            className="field"
            placeholder="Ask a question about your campus data"
            value={question}
            maxLength={500}
            onChange={(event) => setQuestion(event.target.value)}
            aria-label="Your question"
          />
          <Button type="submit" loading={loading} disabled={!question.trim()}>
            <Send size={16} />
            Ask
          </Button>
        </form>
      </div>
    </div>
  );
}
